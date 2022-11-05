/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.*
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.list.OrderBy
import at.techbee.jtx.ui.list.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

private const val TAG = "JournalsWidgetRec"


class ListWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ListWidget()

    private val coroutineScope = MainScope()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        observeData(context)
        setWork(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        //if(intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            observeData(context)
            setWork(context)
        //}
    }

    private fun observeData(context: Context) {
        coroutineScope.launch(Dispatchers.IO) {

            GlanceAppWidgetManager(context).getGlanceIds(ListWidget::class.java).forEach { glanceId ->

                glanceId.let {
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, it) { pref ->

                        val listWidgetConfig = pref[filterConfig]?.let { filterConfig -> Json.decodeFromString<ListWidgetConfig>(filterConfig) }
                        Log.d(TAG, "filterConfig: $listWidgetConfig")
                        Log.v(TAG, "Loading data ...")
                        val entries = ICalDatabase.getInstance(context)
                            .iCalDatabaseDao
                            .getIcal4ListSync(ICal4List.constructQuery(
                                module = listWidgetConfig?.module ?: Module.TODO,
                                searchCategories = listWidgetConfig?.searchCategories ?: emptyList(),
                                searchStatusTodo = listWidgetConfig?.searchStatusTodo ?: emptyList(),
                                searchStatusJournal = listWidgetConfig?.searchStatusJournal ?: emptyList(),
                                searchClassification = listWidgetConfig?.searchClassification ?: emptyList(),
                                searchCollection = listWidgetConfig?.searchCollection ?: emptyList(),
                                searchAccount = listWidgetConfig?.searchAccount ?: emptyList(),
                                orderBy = listWidgetConfig?.orderBy ?: OrderBy.CREATED,
                                sortOrder = listWidgetConfig?.sortOrder ?: SortOrder.ASC,
                                orderBy2 = listWidgetConfig?.orderBy2 ?: OrderBy.SUMMARY,
                                sortOrder2 = listWidgetConfig?.sortOrder2 ?: SortOrder.ASC,
                                isExcludeDone = listWidgetConfig?.isExcludeDone ?: false,
                                isFilterOverdue = listWidgetConfig?.isFilterOverdue ?: false,
                                isFilterDueToday = listWidgetConfig?.isFilterDueToday ?: false,
                                isFilterDueTomorrow = listWidgetConfig?.isFilterDueTomorrow ?: false,
                                isFilterDueFuture = listWidgetConfig?.isFilterDueFuture ?: false,
                                isFilterStartInPast = listWidgetConfig?.isFilterStartInPast ?: false,
                                isFilterStartToday = listWidgetConfig?.isFilterStartToday?: false,
                                isFilterStartTomorrow = listWidgetConfig?.isFilterStartTomorrow ?: false,
                                isFilterStartFuture = listWidgetConfig?.isFilterStartFuture ?: false,
                                isFilterNoDatesSet =  listWidgetConfig?.isFilterNoDatesSet ?: false
                            ))

                        pref.toMutablePreferences().apply {
                            this[list] = entries.map { entry -> Json.encodeToString(entry) }.toSet()
                            //listWidgetConfig?.let {this[filterConfig] = Json.encodeToString(it) }
                        }
                    }
                    glanceAppWidget.update(context, it)
                    Log.d(TAG, "Widget updated")
                }
            }
        }
    }

    private fun setWork(context: Context) {

        val work: PeriodicWorkRequest = PeriodicWorkRequestBuilder<ListWidgetUpdateWorker>(5, TimeUnit.MINUTES).build()
        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork("listWidgetWorker", ExistingPeriodicWorkPolicy.KEEP, work)
        Log.d(TAG, "Work enqueued")
    }

    companion object {
        val list = stringSetPreferencesKey("list")
        val filterConfig = stringPreferencesKey("filter_config")

        fun updateListWidgets(context: Context) {
            val widgetProvider = ListWidgetReceiver::class.java
            val comp = ComponentName(context, widgetProvider)
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(comp)
            val intent = Intent(context, widgetProvider).apply {
                this.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                this.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}