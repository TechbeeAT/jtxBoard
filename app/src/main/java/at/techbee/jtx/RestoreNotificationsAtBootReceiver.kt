package at.techbee.jtx

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.properties.Alarm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RestoreNotificationsAtBootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            //Log.d("jtx Board", "Restart completed received")
            val notificationManager = NotificationManagerCompat.from(context)
            val database = ICalDatabase.getInstance(context).iCalDatabaseDao
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            prefs.getStringSet(NotificationPublisher.PREFS_SCHEDULED_NOTIFICATIONS, null)
                ?.map { try { it.toLong()} catch (e: NumberFormatException) { return } }
                ?.forEach { iCalObjectId ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val iCalObject = database.getICalObjectByIdSync(iCalObjectId)
                        if( iCalObject != null
                            && iCalObject.percent != 100
                            && iCalObject.status != Status.COMPLETED.status
                            && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                        ) {

                            val notification = Alarm.createNotification(iCalObject.id, 0L, iCalObject.summary, iCalObject.description, true, context)
                            notificationManager.notify(iCalObjectId.toInt(), notification)
                        }
                    }
                }
        }
    }
}