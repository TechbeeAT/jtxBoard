package at.techbee.jtx

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.Status
import at.techbee.jtx.ui.settings.SwitchSetting
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

                    // coroutine scope copied from NotificationPublisher.kt
                    // notification and intents copied from Alarm.kt

                    CoroutineScope(Dispatchers.IO).launch {
                        val iCalObject = database.getICalObjectByIdSync(iCalObjectId)
                        if( iCalObject != null
                            && iCalObject.percent != 100
                            && iCalObject.status != Status.COMPLETED.status
                            && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                        ) {

                            val mainActivityIntent = Intent(context, MainActivity2::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                this.action = MainActivity2.INTENT_ACTION_OPEN_ICALOBJECT
                                this.putExtra(MainActivity2.INTENT_EXTRA_ITEM2SHOW, iCalObjectId)
                            }
                            val contentIntent: PendingIntent = PendingIntent.getActivity(context, iCalObjectId.toInt(), mainActivityIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

                            // create a new notification
                            val notification = NotificationCompat.Builder(context, MainActivity2.CHANNEL_REMINDER_DUE).apply {
                                setSmallIcon(R.drawable.ic_notification)
                                iCalObject.summary?.let { setContentTitle(it) }
                                iCalObject.description?.let { setContentText(it) }
                                setContentIntent(contentIntent)
                                priority = NotificationCompat.PRIORITY_MAX
                                setCategory(NotificationCompat.CATEGORY_REMINDER)     //  CATEGORY_REMINDER might also be an alternative
                                if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SwitchSetting.SETTING_STICKY_ALARMS.key, SwitchSetting.SETTING_STICKY_ALARMS.default)) {
                                    setAutoCancel(false)
                                    setOngoing(true)
                                } else {
                                    setAutoCancel(true)
                                }
                            }.build()
                            notificationManager.notify(iCalObjectId.toInt(), notification)
                        } else {
                            Log.d("notificationManager", "Notification skipped")
                        }
                        NotificationPublisher.scheduleNextNotifications(context)
                    }
                }
        }
    }
}