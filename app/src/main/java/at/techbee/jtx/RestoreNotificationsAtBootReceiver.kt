package at.techbee.jtx

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.updateAll
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.flavored.GeofenceClient
import at.techbee.jtx.widgets.ListWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RestoreNotificationsAtBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            //Log.d("jtx Board", "Restart completed received")
            CoroutineScope(Dispatchers.IO).launch {

                val notificationManager = NotificationManagerCompat.from(context)
                val database = ICalDatabase.getInstance(context).iCalDatabaseDao()

                database.getICalObjectsWithActiveAlarms().forEach { iCalObject ->
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                            val notification = Alarm.createNotification(
                                iCalObject.id,
                                0L,
                                iCalObject.summary,
                                iCalObject.description,
                                true,
                                MainActivity2.NOTIFICATION_CHANNEL_ALARMS,
                                context
                            )
                            notificationManager.notify(iCalObject.id.toInt(), notification)
                        }
                    }

                ListWidget().updateAll(context)
                GeofenceClient(context).setGeofences()
            }
        }
    }
}