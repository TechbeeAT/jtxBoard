/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R


class ListWidgetUpdateWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(
    context,
    workerParameters
) {

    override suspend fun getForegroundInfo(): ForegroundInfo {

        //https://stackoverflow.com/questions/69627330/expedited-workrequests-require-a-listenableworker-to-provide-an-implementation-f
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val widgetUpdateChannelId = "WIDGET_UPDATE_CHANNEL"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(widgetUpdateChannelId, "Widget Update Channel", NotificationManager.IMPORTANCE_MIN)
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, widgetUpdateChannelId)
                .setContentIntent(
                    PendingIntent.getActivity(context, 0, Intent(context, MainActivity2::class.java), PendingIntent.FLAG_IMMUTABLE)
                )
                .setSmallIcon(R.drawable.ic_widget_jtx)
                .setOngoing(true)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentTitle(context.getString(R.string.app_name))
                .setLocalOnly(true)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setContentText(context.getString(R.string.widget_list_compat_updating_widget_notification))
                .build()
            return ForegroundInfo(1337, notification)
        } else {
            return super.getForegroundInfo()
        }
    }

    override suspend fun doWork(): Result {
        ListWidget().updateAll(context)
        return Result.success()
    }
}