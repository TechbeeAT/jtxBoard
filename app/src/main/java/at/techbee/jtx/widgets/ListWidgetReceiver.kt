/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private const val TAG = "ListWidgetRec"


class ListWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ListWidget()


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        setOneTimeWork(context)
    }

    companion object {
        val list = stringSetPreferencesKey("list")
        val subtasks = stringSetPreferencesKey("subtasks")
        val subnotes = stringSetPreferencesKey("subnotes")
        val filterConfig = stringPreferencesKey("filter_config")


        /**
         * Sets a worker to update the widget
         * @param delay: if true the update of the widget is delayed by 2 seconds and the existing work is replaced ( in order to not trigger multiple works in a row )
         * if false the update of the widget is done immediately
         */
        fun setOneTimeWork(context: Context, delay: Boolean? = false) {
            val work: OneTimeWorkRequest = OneTimeWorkRequestBuilder<ListWidgetUpdateWorker>()
                .apply {
                    if (delay == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        setInitialDelay((2).seconds.toJavaDuration())
                }.build()
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    "listWidgetOneTimeWorker",
                    if (delay == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
                    work
                )
            Log.d(TAG, "Work enqueued")
        }
    }
}
