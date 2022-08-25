/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.navigation.findNavController
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.ui.IcalListFragmentDirections
import java.io.File
import java.io.IOException


// this is necessary for the app permission, 100  and 200 ist just a freely chosen value
const val CONTACT_READ_PERMISSION_CODE = 100
const val RECORD_AUDIO_PERMISSION_CODE = 200

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


        // Register Notification Channel for Reminders
        createNotificationChannel()


/*
        billingManager?.isProPurchasedLoaded?.observe(this) {
            // we show the dialog only when we are sure that the purchase was loaded
            if(it)
                showProInfoDialog(billingManager.isProPurchased.value?: false)
        }
        //TODO!
 */
    }





    // this is called when the user accepts a permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == CONTACT_READ_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, R.string.permission_read_contacts_granted, Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, R.string.permission_read_contacts_denied, Toast.LENGTH_SHORT).show()
        } else if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, R.string.permission_record_audio_granted, Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, R.string.permission_record_audio_denied, Toast.LENGTH_SHORT).show()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_reminder_name)
            val descriptionText = getString(R.string.notification_channel_reminder_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_REMINDER_DUE, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
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
