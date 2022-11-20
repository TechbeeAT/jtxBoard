/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.list.OrderBy
import at.techbee.jtx.ui.list.SortOrder
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class ListWidgetUpdateWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(
    context,
    workerParameters
) {
    override suspend fun doWork(): Result {

            GlanceAppWidgetManager(context).getGlanceIds(ListWidget::class.java).forEach { glanceId ->

                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { pref ->

                        val listWidgetConfig = pref[ListWidgetReceiver.filterConfig]?.let { filterConfig -> Json.decodeFromString<ListWidgetConfig>(filterConfig) }
                        //Log.d(TAG, "filterConfig: $listWidgetConfig")
                        //Log.v(TAG, "Loading data ...")
                        val entries = ICalDatabase.getInstance(context)
                            .iCalDatabaseDao
                            .getIcal4ListSync(
                                ICal4List.constructQuery(
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
                                    isFilterNoDatesSet =  listWidgetConfig?.isFilterNoDatesSet ?: false,
                                    flatView = true,  // always true in Widget, we handle the flat view in the code
                                )
                            )

                        pref.toMutablePreferences().apply {
                            this[ListWidgetReceiver.list] = entries.map { entry -> Json.encodeToString(entry) }.toSet()
                            //listWidgetConfig?.let {this[filterConfig] = Json.encodeToString(it) }
                        }
                    }

                    ListWidget().update(context = context, glanceId = glanceId)
                    //glanceAppWidget.update(context, it)
                    //Log.d(TAG, "Widget updated")

        }
        return Result.success()
    }
}