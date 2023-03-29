/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx

import android.Manifest
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.ui.settings.DropdownSetting
import at.techbee.jtx.ui.settings.DropdownSettingOption
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.util.SyncUtil
import at.techbee.jtx.util.getParcelableExtraCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class NotificationPublisher : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val notificationManager = NotificationManagerCompat.from(context)
        val notification = intent.getParcelableExtraCompat(NOTIFICATION, Notification::class)
        val alarmId = intent.getLongExtra(ALARM_ID,  0L)
        val icalObjectId = intent.getLongExtra(ICALOBJECT_ID, 0L)
        val settingsStateHolder = SettingsStateHolder(context)
        val database = ICalDatabase.getInstance(context).iCalDatabaseDao

        // onReceive is triggered when the Alarm Manager calls it (the initial notification, action is null)
        // but also when one of the actions is clicked in the notification (action is one of the defined actions)
        when (intent.action) {
            ACTION_SNOOZE_1D, ACTION_SNOOZE_1H -> {
                notificationManager.cancel(icalObjectId.toInt())
                val nextAlarm = when(intent.action) {
                    ACTION_SNOOZE_1D -> System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
                    ACTION_SNOOZE_1H -> System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
                    else -> null
                } ?: return

                CoroutineScope(Dispatchers.IO).launch {
                    val alarm = database.getAlarmSync(alarmId) ?: return@launch
                    alarm.alarmId = 0L   //  we insert a new alarm
                    alarm.triggerTime = nextAlarm
                    alarm.alarmId = database.insertAlarm(alarm)
                    database.updateSetDirty(alarm.icalObjectId, System.currentTimeMillis())
                    SyncUtil.notifyContentObservers(context)
                    scheduleNextNotifications(context)
                }
            }
            ACTION_DONE -> {
                notificationManager.cancel(icalObjectId.toInt())
                CoroutineScope(Dispatchers.IO).launch {
                    val alarm = database.getAlarmSync(alarmId) ?: return@launch
                    val icalobject = database.getICalObjectByIdSync(alarm.icalObjectId) ?: return@launch
                    icalobject.setUpdatedProgress(100, settingsStateHolder.settingKeepStatusProgressCompletedInSync.value)
                    database.update(icalobject)

                    if(settingsStateHolder.settingLinkProgressToSubtasks.value) {
                        ICalObject.findTopParent(icalObjectId, database)?.let {
                            ICalObject.updateProgressOfParents(it.id, database, settingsStateHolder.settingKeepStatusProgressCompletedInSync.value)
                        }
                    }

                    SyncUtil.notifyContentObservers(context)
                    scheduleNextNotifications(context)
                }
            }
            else -> {
                // no action, so here we notify. if we offer snooze depends on the intent (this was decided already on creation of the intent)
                CoroutineScope(Dispatchers.IO).launch {
                    val iCalObject = database.getICalObjectByIdSync(icalObjectId)
                    if( iCalObject != null
                        && (database.getAlarmSync(alarmId) != null || alarmId == 0L)   // alarmI is null when it's an implicit alarm
                        && iCalObject.percent != 100
                        && iCalObject.status != Status.COMPLETED.status
                        && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                        && notification != null
                    ) {
                        notificationManager.notify(icalObjectId.toInt(), notification)
                    } else {
                        Log.d("notificationManager", "Notification skipped")
                    }
                    scheduleNextNotifications(context)
                }
            }
        }
    }


    companion object {
        const val ALARM_ID = "alarm-id"
        const val ICALOBJECT_ID = "icalobject-id"
        const val NOTIFICATION = "alarmNotification"

        const val ACTION_SNOOZE_1D = "actionSnooze1d"
        const val ACTION_SNOOZE_1H = "actionSnooze1h"
        const val ACTION_DONE = "actionDone"

        private const val PREFS_SCHEDULED_NOTIFICATIONS = "prefsScheduledNotifications"  // ICalObjectIds as StringSet

        private const val MAX_ALARMS_SCHEDULED = 5
        private const val MAX_DUE_ALARMS_SCHEDULED = 5



        fun scheduleNextNotifications(context: Context) {

            // Due to necessity of PendingIntent.FLAG_IMMUTABLE, the notification functionality can only be used from Build Versions > M (Api-Level 23)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                return

            val database = ICalDatabase.getInstance(context).iCalDatabaseDao
            val alarms = database.getNextAlarms(MAX_ALARMS_SCHEDULED).toMutableList()

            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val settingAutoAlarm = prefs.getString(DropdownSetting.SETTING_AUTO_ALARM.key, DropdownSetting.SETTING_AUTO_ALARM.default.key)
            if(settingAutoAlarm == DropdownSettingOption.AUTO_ALARM_ALWAYS_ON_DUE.key) {
                val dueEntries = database.getNextDueEntries(MAX_DUE_ALARMS_SCHEDULED)
                dueEntries.forEach { dueEntry ->
                    alarms.add(Alarm(
                        icalObjectId = dueEntry.id,
                        triggerTime = dueEntry.due,
                        triggerTimezone = dueEntry.dueTimezone
                    ))
                }
            }

            // determine obsolete Request Codes
            val stillActiveAlarms = mutableListOf<Int>()
            prefs.getStringSet(PREFS_SCHEDULED_NOTIFICATIONS, null)
                ?.map { try { it.toInt()} catch (e: NumberFormatException) { return } }
                ?.toMutableList()
                ?.apply { removeAll(alarms.map { it.icalObjectId.toInt() }) }
                ?.let {
                    //val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    //cancel obsolete alarms
                    it.forEach { iCalObjectId ->
                        val iCalObject = database.getICalObjectByIdSync(iCalObjectId.toLong())
                        if(iCalObject == null || iCalObject.percent == 100 || iCalObject.status == Status.COMPLETED.name) {
                            /*
                            val pendingIntent = PendingIntent.getBroadcast(
                                context,
                                iCalObjectId,
                                Intent(context, NotificationPublisher::class.java),
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            )
                            alarmManager.cancel(pendingIntent)
                             */
                            NotificationManagerCompat.from(context).cancel(iCalObjectId)
                        } else {
                            stillActiveAlarms.add(iCalObjectId)
                        }
                    }
                }

            alarms.sortedBy { it.triggerTime }.asReversed() .forEach { alarm ->
                val iCalObject = database.getICalObjectByIdSync(alarm.icalObjectId) ?: return@forEach
                val collection = database.getCollectionByIdSync(iCalObject.collectionId) ?: return@forEach
                alarm.scheduleNotification(context = context, requestCode = iCalObject.id.toInt(), isReadOnly = collection.readonly, notificationSummary = iCalObject.summary, notificationDescription = iCalObject.description)
            }
            val scheduledAlarms = mutableSetOf<String>().apply {
                addAll(stillActiveAlarms.map { it.toString() })
                addAll(alarms.map { it.icalObjectId.toInt().toString() })
            }
            prefs.edit().putStringSet(PREFS_SCHEDULED_NOTIFICATIONS, scheduledAlarms).apply()
        }
    }
}