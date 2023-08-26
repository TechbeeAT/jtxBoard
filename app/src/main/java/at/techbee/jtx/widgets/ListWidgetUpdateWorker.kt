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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.list.AnyAllNone
import at.techbee.jtx.ui.list.ListSettings
import at.techbee.jtx.ui.list.OrderBy
import at.techbee.jtx.ui.list.SortOrder
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


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
                    .iCalDatabaseDao()
                    .getIcal4ListSync(
                        ICal4List.constructQuery(
                            modules = listOf(listWidgetConfig?.module ?: Module.TODO),
                            searchCategories = listWidgetConfig?.searchCategories ?: emptyList(),
                            searchCategoriesAnyAllNone = listWidgetConfig?.searchCategoriesAnyAllNone ?: AnyAllNone.ANY,
                            searchResources = listWidgetConfig?.searchResources ?: emptyList(),
                            searchResourcesAnyAllNone = listWidgetConfig?.searchResourcesAnyAllNone ?: AnyAllNone.ANY,
                            searchStatus = listWidgetConfig?.searchStatus?: emptyList(),
                            searchXStatus = listWidgetConfig?.searchXStatus?: emptyList(),
                            searchClassification = listWidgetConfig?.searchClassification?: emptyList(),
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
                            isFilterNoStartDateSet = listWidgetConfig?.isFilterNoStartDateSet ?: false,
                            isFilterNoDueDateSet = listWidgetConfig?.isFilterNoDueDateSet ?: false,
                            isFilterNoCompletedDateSet = listWidgetConfig?.isFilterNoCompletedDateSet ?: false,
                            isFilterNoCategorySet = listWidgetConfig?.isFilterNoCategorySet ?: false,
                            isFilterNoResourceSet = listWidgetConfig?.isFilterNoResourceSet ?: false,
                            flatView = listWidgetConfig?.flatView?: false,  // always true in Widget, we handle the flat view in the code
                            searchSettingShowOneRecurEntryInFuture = listWidgetConfig?.showOneRecurEntryInFuture ?: false,
                            hideBiometricProtected = ListSettings.getProtectedClassificationsFromSettings(context)  // protected entries are always hidden
                        )
                    )

                val entries = if(allEntries.isEmpty()) emptyList() else if (allEntries.size > ListWidget.MAX_ENTRIES) allEntries.subList(0,ListWidget.MAX_ENTRIES) else allEntries

                val subtasksQuery = ICal4List.getQueryForAllSubEntriesOfParents(
                    component = Component.VTODO,
                    hideBiometricProtected = ListSettings.getProtectedClassificationsFromSettings(context),  // protected entries are always hidden
                    parents = entries.map { it.uid ?:"" },
                    orderBy = listWidgetConfig?.subtasksOrderBy ?: OrderBy.CREATED,
                    sortOrder = listWidgetConfig?.subtasksSortOrder ?: SortOrder.ASC
                )
                val subnotesQuery = ICal4List.getQueryForAllSubEntriesOfParents(
                    component = Component.VJOURNAL,
                    hideBiometricProtected = ListSettings.getProtectedClassificationsFromSettings(context),  // protected entries are always hidden
                    parents = entries.map { it.uid ?:"" },
                    orderBy = listWidgetConfig?.subnotesOrderBy ?: OrderBy.CREATED,
                    sortOrder = listWidgetConfig?.subnotesSortOrder ?: SortOrder.ASC)
                val subtasks = ICalDatabase.getInstance(context).iCalDatabaseDao().getSubEntriesSync(subtasksQuery)
                val subnotes = ICalDatabase.getInstance(context).iCalDatabaseDao().getSubEntriesSync(subnotesQuery)

                val subtasksList = mutableListOf<ICal4ListWidget>().apply {
                    subtasks.forEach { subtask ->
                        subtask.relatedto.forEach { relatedto ->
                            relatedto.text?.let {this.add(ICal4ListWidget.fromICal4List(subtask.iCal4List, it)) }
                        }
                    }
                }

                val subnotesList = mutableListOf<ICal4ListWidget>().apply {
                    subnotes.forEach { subnote ->
                        subnote.relatedto.forEach { relatedto ->
                            relatedto.text?.let {this.add(ICal4ListWidget.fromICal4List(subnote.iCal4List, it)) }
                        }
                    }
                }

                pref.toMutablePreferences().apply {
                    this[ListWidgetReceiver.list] = entries.map { entry -> Json.encodeToString(ICal4ListWidget.fromICal4List(entry)) }.toSet()
                    this[ListWidgetReceiver.subtasks] = subtasksList.map { entry -> Json.encodeToString(entry) }.toSet()
                    this[ListWidgetReceiver.subnotes] = subnotesList.map { entry -> Json.encodeToString(entry) }.toSet()
                    this[ListWidgetReceiver.listExceedsLimits] = allEntries.size > ListWidget.MAX_ENTRIES
                    //Log.d("ListWidgetUpdateWorker", this[ListWidgetReceiver.list].toString())
                }
            }
        }
        ListWidget().updateAll(context)
        return Result.success()
    }
}