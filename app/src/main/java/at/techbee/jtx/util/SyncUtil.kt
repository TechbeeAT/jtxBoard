/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.util

import android.accounts.Account
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import at.techbee.jtx.R
import at.techbee.jtx.SYNC_PROVIDER_AUTHORITY
import at.techbee.jtx.contract.JtxContract

const val TAG = "SyncUtil"

class SyncUtil {

    companion object {

        /**
         * @param [accounts] for which the sync should be checked
         * @return true if a sync is running for the jtx Sync Provider Authority for any of the given accounts
         */
        fun isJtxSyncRunningFor(accounts: Set<Account>): Boolean {
            return accounts.any { ContentResolver.isSyncActive(it, SYNC_PROVIDER_AUTHORITY) }
        }

        /**
         * Immediately starts Sync for the given account
         * @param [accounts] that should be synced
         */
        fun syncAccounts(accounts: Set<Account>) {
            accounts.forEach { account ->
                val extras = Bundle(2)
                extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)        // manual sync
                extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)     // run immediately (don't queue)
                ContentResolver.requestSync(account, SYNC_PROVIDER_AUTHORITY, extras)
            }
        }

        /**
         * @return true if a known sync app found
         */
        fun availableSyncApps(context: Context): List<SyncApp> {
            val availableSyncApps = mutableListOf<SyncApp>()
            SyncApp.values().forEach { syncApp ->
                try {
                    if(context.packageManager?.getPackageInfoCompat(syncApp.packageName, 0) != null)
                        availableSyncApps.add(syncApp)
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.d("SyncAppNotFound", e.stackTraceToString())
                }
            }
            return availableSyncApps
        }


        /**
         * @return true if [syncApp] was found and the known minVersion is compatible/includes jtx Board sync through the packageManager, else false
         */
        fun isSyncAppCompatible(syncApp: SyncApp, context: Context): Boolean {
            try {
                val syncAppInfo = context.packageManager?.getPackageInfoCompat(syncApp.packageName, 0) ?: return false
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    syncAppInfo.longVersionCode >= syncApp.minVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    syncAppInfo.versionCode >= syncApp.minVersionCode
                }
            } catch (e: PackageManager.NameNotFoundException) {
                return false
            }
        }

        /**
         * Starts an intent to open DAVx5 Login Activity (to add a new account)
         */
        fun openSyncAppLoginActivity(syncApp: SyncApp, context: Context?) {
            if(context == null)
                return

            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClassName(syncApp.packageName,"${syncApp.packageName}.ui.setup.LoginActivity")
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, context.getString(R.string.sync_toast_intent_open_sync_app_failed, syncApp.appName), Toast.LENGTH_LONG).show()
                Log.w("SyncFragment", "${syncApp.appName} should be there but opening the Activity failed. \n$e")
            }
        }


        /**
         * Starts an intent to open the known sync apps Accounts Activity (to add a new account)
         */
        fun openSyncAppAccountsActivity(syncApp: SyncApp, context: Context?) {
            if(context == null)
                return

            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClassName(syncApp.packageName,"${syncApp.packageName}.ui.AccountsActivity")
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, context.getString(R.string.sync_toast_intent_open_sync_app_failed, syncApp.appName), Toast.LENGTH_LONG).show()
                Log.w(TAG, "${syncApp.appName} should be there but opening the Activity failed. \n$e")
            }
        }

        /**
         * Starts an intent to open DAVx5 Accounts Activity (to add a new account)
         */
        fun openSyncAppAccountActivity(syncApp: SyncApp, account: Account, context: Context?) {
            if(context == null)
                return

            // open davx5
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClassName(syncApp.packageName,"${syncApp.packageName}.ui.account.AccountActivity")
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("account", account)

            if(intent.resolveActivity(context.packageManager) != null) {
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, context.getString(R.string.sync_toast_intent_open_sync_app_failed, syncApp.appName), Toast.LENGTH_LONG).show()
                    Log.w(TAG, "${syncApp.appName} should be there but opening the Activity failed. \n${e.stackTraceToString()}")
                } catch (e: SecurityException) {
                    Toast.makeText(context, context.getString(R.string.sync_toast_intent_open_sync_app_failed, syncApp.appName), Toast.LENGTH_LONG).show()
                    Log.w(TAG, "${syncApp.appName} is old, AccountActivity is not exposed yet. \n${e.stackTraceToString()}")
                }
            } else {
                openSyncAppAccountsActivity(syncApp, context)
            }
        }


        fun notifyContentObservers(context: Context?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context?.contentResolver?.notifyChange(JtxContract.JtxICalObject.CONTENT_URI, null, ContentResolver.NOTIFY_SYNC_TO_NETWORK)
            } else {
                @Suppress("DEPRECATION")
                context?.contentResolver?.notifyChange(JtxContract.JtxICalObject.CONTENT_URI, null, true)
            }
        }

        fun openSyncAppInPlayStore(syncApp: SyncApp, context: Context?) {
            try {
                context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${syncApp.packageName}")))
            } catch (anfe: ActivityNotFoundException) {
                context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${syncApp.packageName}")))
            }
        }
    }
}

enum class SyncApp(
    val appName: String,
    val packageName: String,
    val accountType: String,
    val minVersionCode: Long,
    val minVersionName: String
) {
    DAVX5("DAVx⁵", "at.bitfire.davdroid", "bitfire.at.davdroid", 403010000L, "4.3.1"),
    KSYNC("kSync", "com.infomaniak.sync", "infomaniak.com.sync", 403010000L, "4.3.1");

    companion object {
        fun fromAccountType(accountType: String?): SyncApp? {
            return values().firstOrNull { it.accountType == accountType }
        }
    }
}