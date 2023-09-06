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
            ACTION_SNOOZE_1D -> CoroutineScope(Dispatchers.IO).launch { addPostponedAlarm(alarmId, (1).days.inWholeMilliseconds, context) }
            ACTION_SNOOZE_1H -> CoroutineScope(Dispatchers.IO).launch { addPostponedAlarm(alarmId, (1).hours.inWholeMilliseconds, context) }
            ACTION_DONE -> CoroutineScope(Dispatchers.IO).launch { setToDone(alarmId, settingsStateHolder.settingKeepStatusProgressCompletedInSync.value, settingsStateHolder.settingLinkProgressToSubtasks.value, context) }
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

        const val PREFS_SCHEDULED_ALARMS = "prefsScheduledNotifications"  // ICalObjectIds as StringSet

        private const val MAX_ALARMS_SCHEDULED = 5
        private const val MAX_DUE_ALARMS_SCHEDULED = 5



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

            // determine obsolete Request Codes
            val stillActiveAlarms = mutableListOf<Int>()
            prefs.getStringSet(PREFS_SCHEDULED_ALARMS, null)
                ?.map { try { it.toInt()} catch (e: NumberFormatException) { return } }
                ?.toMutableList()
                ?.apply { removeAll(alarms.map { it.icalObjectId.toInt() }) }
                ?.let {
                    //val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    //cancel obsolete alarms
                    it.forEach { iCalObjectId ->
                        val iCalObject = database.getICalObjectByIdSync(iCalObjectId.toLong())
                        if(iCalObject == null || iCalObject.percent == 100 || iCalObject.status == Status.COMPLETED.name)
                            NotificationManagerCompat.from(context).cancel(iCalObjectId)
                        else
                            stillActiveAlarms.add(iCalObjectId)
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
            prefs.edit().putStringSet(PREFS_SCHEDULED_ALARMS, scheduledAlarms).apply()
        }

        suspend fun addPostponedAlarm(alarmId: Long, delay: Long, context: Context) {
            val database = ICalDatabase.getInstance(context).iCalDatabaseDao()
            val alarm = database.getAlarmSync(alarmId) ?: return
            alarm.alarmId = 0L   //  we insert a new alarm
            alarm.triggerTime = System.currentTimeMillis() + delay
            alarm.alarmId = database.insertAlarm(alarm)
            database.updateSetDirty(alarm.icalObjectId, System.currentTimeMillis())
            SyncUtil.notifyContentObservers(context)
            scheduleNextNotifications(context)
        }

        suspend fun setToDone(alarmId: Long, keepStatusProgressCompletedInSync: Boolean, linkProgressToSubtasks: Boolean, context: Context) {
            val database = ICalDatabase.getInstance(context).iCalDatabaseDao()
            val alarm = database.getAlarmSync(alarmId) ?: return
            val icalobject = database.getICalObjectByIdSync(alarm.icalObjectId) ?: return
            icalobject.setUpdatedProgress(100, keepStatusProgressCompletedInSync)
            database.update(icalobject)

            if(linkProgressToSubtasks) {
                ICalObject.findTopParent(icalobject.id, database)?.let {
                    ICalObject.updateProgressOfParents(it.id, database, keepStatusProgressCompletedInSync)
                }
            }
            SyncUtil.notifyContentObservers(context)
            scheduleNextNotifications(context)
        }
    }
}