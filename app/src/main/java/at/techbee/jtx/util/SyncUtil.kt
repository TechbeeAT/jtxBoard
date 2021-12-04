/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.util

import android.accounts.AccountManager
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
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
         * @return true if DAVx5 was found through the packageManager, else false
         */
        fun isDAVx5Available(activity: Activity?): Boolean {
            try {
                activity?.packageManager?.getApplicationInfo(DAVX5_PACKAGE_NAME, 0)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                return false
            }
        }


    }
}