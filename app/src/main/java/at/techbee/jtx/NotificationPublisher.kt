/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.util.SyncUtil
import at.techbee.jtx.util.getParcelableExtraCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class NotificationPublisher : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = intent.getParcelableExtraCompat(NOTIFICATION, Notification::class)
        val alarmId = intent.getLongExtra(ALARM_ID,  0L)
        val icalObjectId = intent.getLongExtra(ICALOBJECT_ID, 0L)
        val isImplicitAlarm = intent.getBooleanExtra(IS_IMPLICIT_ALARM, false)
        val settingsStateHolder = SettingsStateHolder(context)

        if(alarmId == 0L && !isImplicitAlarm)
            return

        val database = ICalDatabase.getInstance(context).iCalDatabaseDao

        // onReceive is triggered when the Alarm Manager calls it (the initial notification, action is null)
        // but also when one of the actions is clicked in the notification (action is one of the defined actions)
        when (intent.action) {
            ACTION_SNOOZE_1D, ACTION_SNOOZE_1H -> {
                notificationManager.cancel(alarmId.toInt())
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
                    Alarm.scheduleNextNotifications(context)
                }
            }
            ACTION_DONE -> {
                notificationManager.cancel(alarmId.toInt())
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
                    Alarm.scheduleNextNotifications(context)
                }
            }
            else -> {
                // no action, so here we notify. if we offer snooze depends on the intent (this was decided already on creation of the intent)
                CoroutineScope(Dispatchers.IO).launch {
                    val iCalObject = database.getICalObjectByIdSync(icalObjectId)
                    if( iCalObject != null
                        && (database.getAlarmSync(alarmId) != null || (isImplicitAlarm))
                        && iCalObject.percent != 100
                        && iCalObject.status != Status.COMPLETED.status
                    ) {
                        notificationManager.notify(alarmId.toInt(), notification)
                    } else {
                        Log.d("notificationManager", "Notification skipped")
                    }
                    Alarm.scheduleNextNotifications(context)
                }
            }
        }
    }

    companion object {
        const val ALARM_ID = "alarm-id"
        const val ICALOBJECT_ID = "icalobject-id"
        const val IS_IMPLICIT_ALARM = "isImplicitAlarm"
        const val NOTIFICATION = "alarmNotification"

        const val ACTION_SNOOZE_1D = "actionSnooze1d"
        const val ACTION_SNOOZE_1H = "actionSnooze1h"
        const val ACTION_DONE = "actionDone"

    }
}