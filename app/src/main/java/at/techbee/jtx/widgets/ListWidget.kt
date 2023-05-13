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
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.background
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.unit.ColorProvider
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.list.ListSettings
import at.techbee.jtx.ui.theme.getContrastSurfaceColorFor
import kotlinx.serialization.json.Json


class ListWidget : GlanceAppWidget() {

    companion object {
        val filterConfig = stringPreferencesKey("filter_config")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d("ListWidgetUpdateWorker", "GlanceId on updateWidgetState: $id")

        val listWidgetConfig =
            getAppWidgetState(context, PreferencesGlanceStateDefinition, id)[filterConfig]?.let { filterConfig -> Json.decodeFromString<ListWidgetConfig>(filterConfig) }
                ?: ListWidgetConfig()
        Log.d("ListWidgetUpdateWorker", "GlanceId $id : filterConfig: $listWidgetConfig")
        //Log.v(TAG, "Loading data ...")

        val allEntries = ICalDatabase.getInstance(context)
            .iCalDatabaseDao
            .getIcal4ListSync(
                ICal4List.constructQuery(
                    modules = listOf(listWidgetConfig.module),
                    searchCategories = listWidgetConfig.searchCategories,
                    searchResources = listWidgetConfig.searchResources,
                    searchStatus = listWidgetConfig.searchStatus,
                    searchClassification = listWidgetConfig.searchClassification,
                    searchCollection = listWidgetConfig.searchCollection,
                    searchAccount = listWidgetConfig.searchAccount,
                    orderBy = listWidgetConfig.orderBy,
                    sortOrder = listWidgetConfig.sortOrder,
                    orderBy2 = listWidgetConfig.orderBy2,
                    sortOrder2 = listWidgetConfig.sortOrder2,
                    isExcludeDone = listWidgetConfig.isExcludeDone,
                    isFilterOverdue = listWidgetConfig.isFilterOverdue,
                    isFilterDueToday = listWidgetConfig.isFilterDueToday,
                    isFilterDueTomorrow = listWidgetConfig.isFilterDueTomorrow,
                    isFilterDueFuture = listWidgetConfig.isFilterDueFuture,
                    isFilterStartInPast = listWidgetConfig.isFilterStartInPast,
                    isFilterStartToday = listWidgetConfig.isFilterStartToday,
                    isFilterStartTomorrow = listWidgetConfig.isFilterStartTomorrow,
                    isFilterStartFuture = listWidgetConfig.isFilterStartFuture,
                    isFilterNoDatesSet = listWidgetConfig.isFilterNoDatesSet,
                    isFilterNoStartDateSet = listWidgetConfig.isFilterNoStartDateSet,
                    isFilterNoDueDateSet = listWidgetConfig.isFilterNoDueDateSet,
                    isFilterNoCompletedDateSet = listWidgetConfig.isFilterNoCompletedDateSet,
                    isFilterNoCategorySet = listWidgetConfig.isFilterNoCategorySet,
                    isFilterNoResourceSet = listWidgetConfig.isFilterNoResourceSet,
                    flatView = listWidgetConfig.flatView,  // always true in Widget, we handle the flat view in the code
                    searchSettingShowOneRecurEntryInFuture = listWidgetConfig.showOneRecurEntryInFuture,
                    hideBiometricProtected = ListSettings.getProtectedClassificationsFromSettings(context)  // protected entries are always hidden
                )
            )

        val subtasksQuery = ICal4List.getQueryForAllSubEntriesOfParents(
            component = Component.VTODO,
            hideBiometricProtected = ListSettings.getProtectedClassificationsFromSettings(context),  // protected entries are always hidden
            parents = allEntries.map { it.iCal4List.uid ?: "" },
            orderBy = listWidgetConfig.subtasksOrderBy,
            sortOrder = listWidgetConfig.subtasksSortOrder
        )
        val subnotesQuery = ICal4List.getQueryForAllSubEntriesOfParents(
            component = Component.VJOURNAL,
            hideBiometricProtected = ListSettings.getProtectedClassificationsFromSettings(context),  // protected entries are always hidden
            parents = allEntries.map { it.iCal4List.uid ?: "" },
            orderBy = listWidgetConfig.subnotesOrderBy,
            sortOrder = listWidgetConfig.subnotesSortOrder
        )
        val subtasks = ICalDatabase.getInstance(context).iCalDatabaseDao.getSubEntriesSync(subtasksQuery)
        val subnotes = ICalDatabase.getInstance(context).iCalDatabaseDao.getSubEntriesSync(subnotesQuery)

        provideContent {
            //Log.d("ListWidget", "appWidgetId in ListWidget: ${GlanceAppWidgetManager(context).getAppWidgetId(LocalGlanceId.current)}")
            //Log.d("ListWidget", "glanceId in ListWidget: ${LocalGlanceId.current}")

            val backgorundColor = if (listWidgetConfig.widgetAlpha == 1F && listWidgetConfig.widgetColor == null)
                GlanceTheme.colors.primaryContainer
            else if ((listWidgetConfig.widgetAlpha) < 1F && listWidgetConfig.widgetColor == null)
                ColorProvider(GlanceTheme.colors.primaryContainer.getColor(context).copy(alpha = listWidgetConfig.widgetAlpha))
            else
                ColorProvider(Color(listWidgetConfig.widgetColor!!).copy(alpha = listWidgetConfig.widgetAlpha))

            val textColor = if (listWidgetConfig.widgetColor == null)
                GlanceTheme.colors.onPrimaryContainer
            else
                ColorProvider(MaterialTheme.colorScheme.getContrastSurfaceColorFor(Color(listWidgetConfig.widgetColor!!).copy(alpha = listWidgetConfig.widgetAlpha)))

            val entryColor = if (listWidgetConfig.widgetAlphaEntries == 1F && listWidgetConfig.widgetColorEntries == null)
                GlanceTheme.colors.surface
            else if (listWidgetConfig.widgetAlphaEntries < 1F && listWidgetConfig.widgetColorEntries == null)
                ColorProvider(GlanceTheme.colors.surface.getColor(context).copy(alpha = listWidgetConfig.widgetAlphaEntries))
            else
                ColorProvider(Color(listWidgetConfig.widgetColorEntries!!).copy(alpha = listWidgetConfig.widgetAlphaEntries))

            val entryTextColor = if (listWidgetConfig.widgetColorEntries == null)
                GlanceTheme.colors.onSurface
            else
                ColorProvider(MaterialTheme.colorScheme.getContrastSurfaceColorFor(Color(listWidgetConfig.widgetColorEntries!!).copy(alpha = listWidgetConfig.widgetAlphaEntries)))

            val entryOverdueTextColor = GlanceTheme.colors.error

            GlanceTheme {
                ListWidgetContent(
                    listWidgetConfig,
                    list = allEntries,
                    subtasks = subtasks,
                    subnotes = subnotes,
                    textColor = textColor,
                    entryColor = entryColor,
                    entryTextColor = entryTextColor,
                    entryOverdueTextColor = entryOverdueTextColor,
                    modifier = GlanceModifier
                        .appWidgetBackground()
                        .fillMaxSize()
                        .padding(horizontal = 4.dp)
                        .background(backgorundColor)
                )
            }
        }
    }
}
