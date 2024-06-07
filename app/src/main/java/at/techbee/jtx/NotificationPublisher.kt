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
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours


class NotificationPublisher : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val notificationManager = NotificationManagerCompat.from(context)
        val notification = intent.getParcelableExtraCompat(NOTIFICATION, Notification::class)
        val alarmId = intent.getLongExtra(ALARM_ID,  0L)
        val icalObjectId = intent.getLongExtra(ICALOBJECT_ID, 0L)
        val settingsStateHolder = SettingsStateHolder(context)
        val database = ICalDatabase.getInstance(context).iCalDatabaseDao()

        // onReceive is triggered when the Alarm Manager calls it (the initial notification, action is null)
        // but also when one of the actions is clicked in the notification (action is one of the defined actions)
        if(intent.action != null)
            notificationManager.cancel(icalObjectId.toInt())

        when (intent.action) {
            ACTION_SNOOZE_1D -> CoroutineScope(Dispatchers.IO).launch {
                addPostponedAlarm(alarmId, (1).days.inWholeMilliseconds, context)
                database.setAlarmNotification(icalObjectId, false)
            }
            ACTION_SNOOZE_1H -> CoroutineScope(Dispatchers.IO).launch {
                addPostponedAlarm(alarmId, (1).hours.inWholeMilliseconds, context)
                database.setAlarmNotification(icalObjectId, false)
            }
            ACTION_DONE -> CoroutineScope(Dispatchers.IO).launch {
                database.updateProgress(
                    id = icalObjectId,
                    newPercent = 100,
                    settingKeepStatusProgressCompletedInSync = settingsStateHolder.settingKeepStatusProgressCompletedInSync.value,
                    settingLinkProgressToSubtasks = settingsStateHolder.settingLinkProgressToSubtasks.value
                )
                database.setAlarmNotification(icalObjectId, false)
                SyncUtil.notifyContentObservers(context)
                scheduleNextNotifications(context)
            }
            ACTION_DISMISS -> CoroutineScope(Dispatchers.IO).launch {
                Log.d("NotificationPublisher", "Notification dismissed")
                if(settingsStateHolder.settingStickyAlarms.value)
                    restoreAlarms(context)
                else
                    database.setAlarmNotification(icalObjectId, false)
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
                        //Log.d("notificationManager", "Can use FullScreenIntent: ${notificationManager.canUseFullScreenIntent()}")
                        notificationManager.notify(icalObjectId.toInt(), notification)
                        database.setAlarmNotification(icalObjectId, true)
                    } else {
                        //Log.d("notificationManager", "Notification skipped")
                        database.setAlarmNotification(icalObjectId, false)
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
        const val ACTION_DISMISS = "actionDismiss´"


        private const val MAX_ALARMS_SCHEDULED = 5
        private const val MAX_DUE_ALARMS_SCHEDULED = 5

        @Deprecated("Not in use anymore, only for legacy handling")
        const val PREFS_SCHEDULED_ALARMS = "prefsScheduledNotifications"  // ICalObjectIds as StringSet




        fun scheduleNextNotifications(context: Context) {

            // Due to necessity of PendingIntent.FLAG_IMMUTABLE, the notification functionality can only be used from Build Versions > M (Api-Level 23)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                return

            val database = ICalDatabase.getInstance(context).iCalDatabaseDao()
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

            alarms.sortedBy { it.triggerTime }.asReversed() .forEach { alarm ->
                val iCalObject = database.getICalObjectByIdSync(alarm.icalObjectId) ?: return@forEach
                val collection = database.getCollectionByIdSync(iCalObject.collectionId) ?: return@forEach
                alarm.scheduleNotification(context = context, requestCode = iCalObject.id.toInt(), isReadOnly = collection.readonly, notificationSummary = iCalObject.summary, notificationDescription = iCalObject.description)
            }


            // Legacy handling - TODO: Remove in Future
            prefs.getStringSet(PREFS_SCHEDULED_ALARMS, null)
                ?.map { try { it.toInt()} catch (e: NumberFormatException) { return } }
                ?.let {
                    it.forEach { iCalObjectId ->
                        database.setAlarmNotification(iCalObjectId.toLong(), true)
                    }
                }
            prefs.edit().remove(PREFS_SCHEDULED_ALARMS).apply()
            // End Legacy handling
        }

        suspend fun addPostponedAlarm(alarmId: Long, delay: Long, context: Context) {
            val database = ICalDatabase.getInstance(context).iCalDatabaseDao()
            val alarm = database.getAlarmSync(alarmId) ?: return
            alarm.alarmId = 0L   //  we insert a new alarm
            alarm.triggerTime = System.currentTimeMillis() + delay
            alarm.triggerRelativeTo = null
            alarm.triggerRelativeDuration = null
            alarm.alarmId = database.insertAlarm(alarm)
            database.updateSetDirty(alarm.icalObjectId)
            SyncUtil.notifyContentObservers(context)
            scheduleNextNotifications(context)
        }


        fun triggerImmediateAlarm(iCalObject: ICalObject, context: Context) {
            if((iCalObject.summary.isNullOrEmpty() && iCalObject.description.isNullOrEmpty())
                || iCalObject.percent == 100
                || iCalObject.status == Status.COMPLETED.status
                )
                return

            val notification = Alarm.createNotification(
                iCalObject.id,
                0L,
                iCalObject.summary,
                iCalObject.description,
                false,   // can never be read only
                MainActivity2.NOTIFICATION_CHANNEL_ALARMS,
                context
            )
            val notificationManager = NotificationManagerCompat.from(context)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(iCalObject.id.toInt(), notification)
                ICalDatabase
                    .getInstance(context)
                    .iCalDatabaseDao()
                    .setAlarmNotification(iCalObject.id, true)
            }
        }

        fun restoreAlarms(context: Context) {
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
        }
    }
}