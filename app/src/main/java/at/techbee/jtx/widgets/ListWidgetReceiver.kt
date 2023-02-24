/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

private const val TAG = "ListWidgetRec"


class ListWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ListWidget()

    companion object {
        val list = stringSetPreferencesKey("list")
        val subtasks = stringSetPreferencesKey("subtasks")
        val subnotes = stringSetPreferencesKey("subnotes")
        val listExceedsLimits = booleanPreferencesKey("list_exceed_limits")
        val filterConfig = stringPreferencesKey("filter_config")

        /**
         * Sets a worker to update the widget
         * @param delay: null updates immediately, otherwise updates after the given Kotlin Duration
         */
        fun setOneTimeWork(context: Context, delay: Duration? = null) {
            val work: OneTimeWorkRequest = OneTimeWorkRequestBuilder<ListWidgetUpdateWorker>()
                .apply {
                    if (delay != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        setInitialDelay(delay.toJavaDuration())
                    if(delay == null)
                        setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                }.build()
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    "listWidgetOneTimeWorker",
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    work
                )
            Log.d(TAG, "Work enqueued")
        }

        /**
         * Sets a periodic worker to update the widget every 10 minutes
         * This is a workaround to make sure the widget gets updated regularly
         * in order to catch color changes (e.g. day/night change)
         */
        fun setPeriodicWork(context: Context) {
            val work: PeriodicWorkRequest = PeriodicWorkRequestBuilder<ListWidgetUpdateWorker>((10).minutes.toJavaDuration()).build()
            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork("listWidgetPeriodicWorker", ExistingPeriodicWorkPolicy.KEEP, work)
            Log.d(TAG, "Periodic work enqueued")
        }
    }
}
