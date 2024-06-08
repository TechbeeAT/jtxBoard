package at.techbee.jtx

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
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
                NotificationPublisher.restoreAlarms(context)
                ListWidget().updateAll(context)
                GeofenceClient(context).setGeofences()
            }
        }
    }
}