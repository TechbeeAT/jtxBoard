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
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.util.SyncUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class NotificationPublisher : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = intent.getParcelableExtra<Notification>(NOTIFICATION)
        val id = intent.getLongExtra(NOTIFICATION_ID, 0L)

        // onReceive is triggered when the Alarm Manager calls it (the initial notification, action is null)
        // but also when one of the actions is clicked in the notification (action is one of the defined actions)
        if(intent.action == ACTION_SNOOZE_1D || intent.action == ACTION_SNOOZE_1H) {
            notificationManager.cancel(id.toInt())
            val nextAlarm = when(intent.action) {
                ACTION_SNOOZE_1D -> System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
                ACTION_SNOOZE_1H -> System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
                else -> null
            } ?: return

            CoroutineScope(Dispatchers.IO).launch {
                val alarm = ICalDatabase.getInstance(context).iCalDatabaseDao.getAlarmSync(id) ?: return@launch
                alarm.alarmId = 0L   //  we insert a new alarm
                alarm.triggerTime = nextAlarm
                ICalDatabase.getInstance(context).iCalDatabaseDao.insertAlarm(alarm)
                ICalDatabase.getInstance(context).iCalDatabaseDao.updateSetDirty(alarm.icalObjectId, System.currentTimeMillis())
                SyncUtil.notifyContentObservers(context)
                alarm.scheduleNotification(context, nextAlarm)
            }
        } else if (intent.action == ACTION_DONE) {
            notificationManager.cancel(id.toInt())
            CoroutineScope(Dispatchers.IO).launch {
                val alarm = ICalDatabase.getInstance(context).iCalDatabaseDao.getAlarmSync(id) ?: return@launch
                val icalobject = ICalDatabase.getInstance(context).iCalDatabaseDao.getICalObjectByIdSync(alarm.icalObjectId) ?: return@launch
                icalobject.setUpdatedProgress(100)
                ICalDatabase.getInstance(context).iCalDatabaseDao.update(icalobject)
                SyncUtil.notifyContentObservers(context)
            }
        } else {
            // no action, so here we notify
            CoroutineScope(Dispatchers.IO).launch {
                val alarm = ICalDatabase.getInstance(context).iCalDatabaseDao.getAlarmSync(id)
                if (alarm != null)     // notify only if the alarm still exists
                    notificationManager.notify(id.toInt(), notification)
            }
        }
    }


    companion object {
        var NOTIFICATION_ID = "notification-id"   // identifier behind the value for alarmId
        var NOTIFICATION = "alarmNotification"

        const val ACTION_SNOOZE_1D = "actionSnooze1d"
        const val ACTION_SNOOZE_1H = "actionSnooze1h"
        const val ACTION_DONE = "actionDone"

    }
}