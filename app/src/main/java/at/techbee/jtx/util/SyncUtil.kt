/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.util

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import at.techbee.jtx.R
import at.techbee.jtx.SYNC_PROVIDER_AUTHORITY
import at.techbee.jtx.database.ICalCollection

class SyncUtil {

    companion object {

        const val DAVX5_PACKAGE_NAME = "at.bitfire.davdroid"


        /**
         * @return true if a sync is running for the JTX Sync Provider Authority (no matter which account)
         */
        fun isJtxSyncRunning(): Boolean {
            val allJtxSyncs = ContentResolver.getCurrentSyncs().filter { it.authority == SYNC_PROVIDER_AUTHORITY }
            return allJtxSyncs.isNotEmpty()
        }

        /**
         * @param [account] for which the sync should be checked
         * @return true if a sync is running for the JTX Sync Provider Authority and the given account
         */
        fun isJtxSyncRunningForAccount(account: Account): Boolean {
            return ContentResolver.isSyncActive(account, SYNC_PROVIDER_AUTHORITY)
        }

        /**
         * Immediately starts Sync for all Accounts for the JTX Sync Provider Authority
         */
        fun syncAllAccounts(context: Context) {

            val accounts = AccountManager.get(context).getAccountsByType(ICalCollection.DAVX5_ACCOUNT_TYPE)

            val extras = Bundle(2)
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)        // manual sync
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)     // run immediately (don't queue)

            accounts.forEach { account ->
                ContentResolver.requestSync(account, SYNC_PROVIDER_AUTHORITY, extras)
            }
        }

        /**
         * Immediately starts Sync for the given account
         * @param [account] that should be synced
         */
        fun syncAccount(account: Account) {

            val extras = Bundle(2)
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)        // manual sync
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)     // run immediately (don't queue)
            ContentResolver.requestSync(account, SYNC_PROVIDER_AUTHORITY, extras)
        }

        /**
         * @return true if DAVx5 was found through the packageManager, else false
         */
        fun isDAVx5Available(application: Application): Boolean {
            try {
                application.packageManager?.getApplicationInfo(DAVX5_PACKAGE_NAME, 0) ?: return false
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                return false
            }
        }

        /**
         * @return true if DAVx5 was found and the versioon is compatible/includes jtx Board syncthrough the packageManager, else false
         */
        fun isDAVx5CompatibleWithJTX(application: Application): Boolean {
            try {
                val davx5Info = application.packageManager?.getPackageInfo(DAVX5_PACKAGE_NAME, 0) ?: return false
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    davx5Info.longVersionCode >= 402000000L
                } else {
                    davx5Info.versionCode >= 402000000
                }
            } catch (e: PackageManager.NameNotFoundException) {
                return false
            }
        }

        /**
         * Starts an intent to open DAVx5 Login Activity (to add a new account)
         */
        fun openDAVx5LoginActivity(context: Context?) {
            // open davx5
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClassName(DAVX5_PACKAGE_NAME,"${DAVX5_PACKAGE_NAME}.ui.setup.LoginActivity")
            try {
                context?.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, R.string.sync_toast_intent_open_davx5_failed, Toast.LENGTH_LONG).show()
                Log.w("SyncFragment", "DAVx5 should be there but opening the Activity failed. \n$e")
            }
        }


        /**
         * Starts an intent to open DAVx5 Accounts Activity (to add a new account)
         */
        fun openDAVx5AccountsActivity(context: Context?) {
            // open davx5
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClassName(DAVX5_PACKAGE_NAME,"${DAVX5_PACKAGE_NAME}.ui.AccountsActivity")
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            try {
                context?.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, R.string.sync_toast_intent_open_davx5_failed, Toast.LENGTH_LONG).show()
                Log.w("SyncFragment", "DAVx5 should be there but opening the Activity failed. \n$e")
            }
        }
    }
}