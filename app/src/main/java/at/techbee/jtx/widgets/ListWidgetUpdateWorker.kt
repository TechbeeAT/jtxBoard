/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
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
            Log.d("ListWidgetUpdateWorker", "GlanceId on updateWidgetState: $glanceId")

            // need to purge first, otherwise the list is not updated if only the sort order changes
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { pref ->
                pref.toMutablePreferences().apply {
                    this[ListWidgetReceiver.list] = emptySet()
                    this[ListWidgetReceiver.subnotes] = emptySet()
                    this[ListWidgetReceiver.subtasks] = emptySet()
                }
            }

            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { pref ->

                val listWidgetConfig = pref[ListWidgetReceiver.filterConfig]?.let { filterConfig ->
                    Json.decodeFromString<ListWidgetConfig>(filterConfig)
                }
                Log.d("ListWidgetUpdateWorker", "GlanceId $glanceId : filterConfig: $listWidgetConfig")
                //Log.v(TAG, "Loading data ...")
                val allEntries = ICalDatabase.getInstance(context)
                    .iCalDatabaseDao
                    .getIcal4ListSync(
                        ICal4List.constructQuery(
                            module = listWidgetConfig?.module ?: Module.TODO,
                            searchCategories = listWidgetConfig?.searchCategories ?: emptyList(),
                            searchResources = listWidgetConfig?.searchResources ?: emptyList(),
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
                            isFilterNoStatusSet = listWidgetConfig?.isFilterNoStatusSet ?: false,
                            isFilterNoClassificationSet = listWidgetConfig?.isFilterNoClassificationSet ?: false,
                            isFilterNoCategorySet = listWidgetConfig?.isFilterNoCategorySet ?: false,
                            isFilterNoResourceSet = listWidgetConfig?.isFilterNoResourceSet ?: false,
                            flatView = listWidgetConfig?.flatView?: false,  // always true in Widget, we handle the flat view in the code
                            searchSettingShowOneRecurEntryInFuture = listWidgetConfig?.showOneRecurEntryInFuture ?: false
                        )
                    )

                val entries = if(allEntries.isEmpty()) emptyList() else if (allEntries.size > ListWidget.MAX_ENTRIES) allEntries.subList(0,ListWidget.MAX_ENTRIES) else allEntries

                val subtasks = ICalDatabase.getInstance(context).iCalDatabaseDao.getSubtasksSyncOf(entries.map { it.uid?:"" })
                val subnotes = ICalDatabase.getInstance(context).iCalDatabaseDao.getSubnotesSyncOf(entries.map { it.uid?:"" })

                pref.toMutablePreferences().apply {
                    this[ListWidgetReceiver.list] = entries.map { entry -> Json.encodeToString(ICal4ListWidget.fromICal4List(entry)) }.toSet()
                    this[ListWidgetReceiver.subtasks] = subtasks.map { entry -> Json.encodeToString(ICal4ListWidget.fromICal4List(entry)) }.toSet()
                    this[ListWidgetReceiver.subnotes] = subnotes.map { entry -> Json.encodeToString(ICal4ListWidget.fromICal4List(entry)) }.toSet()
                    this[ListWidgetReceiver.listExceedsLimits] = allEntries.size > ListWidget.MAX_ENTRIES
                    //Log.d("ListWidgetUpdateWorker", this[ListWidgetReceiver.list].toString())
                }
            }
        }
        ListWidget().updateAll(context)
        return Result.success()
    }
}