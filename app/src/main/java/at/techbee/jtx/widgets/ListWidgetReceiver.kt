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
import android.content.Intent
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
        //setPeriodicWork(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        //if(intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            setOneTimeWork(context)
            //setPeriodicWork(context)
        //}
    }


    companion object {
        val list = stringSetPreferencesKey("list")
        val subtasks = stringSetPreferencesKey("subtasks")
        val subnotes = stringSetPreferencesKey("subnotes")
        val filterConfig = stringPreferencesKey("filter_config")

        /*
        private fun setPeriodicWork(context: Context) {

            val work: PeriodicWorkRequest = PeriodicWorkRequestBuilder<ListWidgetUpdateWorker>(5, TimeUnit.MINUTES).build()
            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork("listWidgetWorker", ExistingPeriodicWorkPolicy.KEEP, work)
            Log.d(TAG, "Work enqueued")
        }
         */

        fun setOneTimeWork(context: Context) {
            val work: OneTimeWorkRequest = OneTimeWorkRequestBuilder<ListWidgetUpdateWorker>().build()
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork("listWidgetOneTimeWorker", ExistingWorkPolicy.KEEP, work)
            Log.d(TAG, "Work enqueued")
        }

        fun setDelayedOneTimeWork(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                setOneTimeWork(context)
            } else {
                val work: OneTimeWorkRequest = OneTimeWorkRequestBuilder<ListWidgetUpdateWorker>()
                    .setInitialDelay((2).seconds.toJavaDuration())
                    .build()
                WorkManager
                    .getInstance(context)
                    .enqueueUniqueWork("listWidgetOneTimeWorker", ExistingWorkPolicy.REPLACE, work)
                Log.d(TAG, "Work enqueued")
            }
        }
    }
}