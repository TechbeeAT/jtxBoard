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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NotificationPublisher : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = intent.getParcelableExtra<Notification>(NOTIFICATION)
        val id = intent.getLongExtra(NOTIFICATION_ID, 0L)

        CoroutineScope(Dispatchers.IO).launch {
            val alarm = ICalDatabase.getInstance(context).iCalDatabaseDao.getAlarmSync(id)
            if(alarm != null)     // notify only if the alarm still exists
                notificationManager.notify(id.toInt(), notification)
        }
    }

    companion object {
        var NOTIFICATION_ID = "notification-id"   // identifier behind the value for alarmId
        var NOTIFICATION = "alarmNotification"
    }
}