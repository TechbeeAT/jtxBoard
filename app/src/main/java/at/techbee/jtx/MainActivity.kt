/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


const val AUTHORITY_FILEPROVIDER = "at.techbee.jtx.fileprovider"

/**
 * This main activity is just a container for our fragments,
 * where the real action is.
 */
class MainActivity : AppCompatActivity()  {

    companion object {
        const val CHANNEL_REMINDER_DUE = "REMINDER_DUE"

        const val BUILD_FLAVOR_OSE = "ose"
        const val BUILD_FLAVOR_GOOGLEPLAY = "gplay"
        const val BUILD_FLAVOR_HUAWEI = "huawei"

        private const val SETTINGS_PRO_INFO_SHOWN = "settingsProInfoShown"

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

/*
        billingManager?.isProPurchasedLoaded?.observe(this) {
            // we show the dialog only when we are sure that the purchase was loaded
            if(it)
                showProInfoDialog(billingManager.isProPurchased.value?: false)
        }
        //TODO!
 */
    }



    private fun showProInfoDialog(isPurchased: Boolean) {
        // show a one time message for users who did not buy the app

        /*
        val proInfoShown = settings?.getBoolean(SETTINGS_PRO_INFO_SHOWN, false) ?: false
        val jtxProDialogMessage =
            if(!proInfoShown && isPurchased && this.packageManager.getPackageInfo(this.packageName, 0).firstInstallTime < 1654034400000L) {
                "Hello! We have removed all ad-code from jtx Board and now offer a Pro-version in addition to the free download that has restricted sync-capabilities. As you had purchased the ad-free option anyway, we automatically migrated you to the Pro-version! There is no change for you, this is just to let you know! :)"
            } else if (!proInfoShown && !isPurchased && this.packageManager.getPackageInfo(this.packageName, 0).firstInstallTime < 1654034400000L) {
                "Hello! We have removed all ad-code from jtx Board and now offer a Pro-version in addition to the free version that has restricted sync-capabilities. However, your installation will remain fully functional with all Pro features, just without ads - this is just to let you know! :)"
            } else if (!proInfoShown && !isPurchased) {
                getString(R.string.buypro_initial_dialog_message)
            } else {
                null
            }

        // show dialog only if conditions for message were fulfilled
        jtxProDialogMessage?.let {  message ->
            MaterialAlertDialogBuilder(this)
                .setTitle("jtx Board Pro")
                .setMessage(message)
                .setIcon(R.drawable.ic_adinfo)
                .setPositiveButton(R.string.ok) { _, _ -> }
                .setNeutralButton(R.string.more) { _, _ ->
                    //findNavController(R.id.nav_host_fragment)
                    //    .navigate(R.id.action_global_buyProFragment)
                }
                .show()

            settings?.edit()?.putBoolean(SETTINGS_PRO_INFO_SHOWN, true)?.apply()
          }
         */


    }
}
