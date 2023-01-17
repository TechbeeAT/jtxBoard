/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.layout.*
import androidx.glance.text.*
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.ui.list.GroupBy
import at.techbee.jtx.ui.list.SortOrder
import at.techbee.jtx.widgets.elements.ListEntry
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class ListWidget : GlanceAppWidget() {

    companion object {
        const val MAX_ENTRIES = 50
    }

    @Composable
    override fun Content() {

        val context = LocalContext.current

        Log.d("ListWidget", "appWidgetId in ListWidget: ${GlanceAppWidgetManager(context).getAppWidgetId(LocalGlanceId.current)}")
        Log.d("ListWidget", "glanceId in ListWidget: ${LocalGlanceId.current}")

        val prefs = currentState<Preferences>()
        val listWidgetConfig = prefs[ListWidgetReceiver.filterConfig]?.let { filterConfig ->
            Json.decodeFromString<ListWidgetConfig>(filterConfig)
        }

        val list = prefs[ListWidgetReceiver.list]?.map { Json.decodeFromString<ICal4ListWidget>(it) } ?: emptyList()
        // first apply a proper sort order, then group
        var sortedList = when (listWidgetConfig?.groupBy) {
            GroupBy.STATUS -> list.sortedBy {
                if (listWidgetConfig.module == Module.TODO && it.percent != 100)
                    try {
                        Status.valueOf(it.status ?: Status.NEEDS_ACTION.name).ordinal
                    } catch (e: java.lang.IllegalArgumentException) {
                        -1
                    }
                else
                    try {
                        Status.valueOf(it.status ?: Status.FINAL.name).ordinal
                    } catch (e: java.lang.IllegalArgumentException) {
                        -1
                    }
            }
            GroupBy.CLASSIFICATION -> list.sortedBy {
                try {
                    Classification.valueOf(it.classification ?: Classification.PUBLIC.name).ordinal
                } catch (e: java.lang.IllegalArgumentException) {
                    -1
                }
            }
            else -> list
        }
        if ((listWidgetConfig?.groupBy == GroupBy.STATUS || listWidgetConfig?.groupBy == GroupBy.CLASSIFICATION) && listWidgetConfig.sortOrder == SortOrder.DESC)
            sortedList = sortedList.asReversed()
        
        val groupedList = sortedList.groupBy {
            when (listWidgetConfig?.groupBy) {
                GroupBy.STATUS -> Status.values().find { status -> status.status == it.status}?.stringResource?.let { stringRes -> stringResource(id = stringRes) }?: it.status?: stringResource(id = R.string.status_no_status)
                GroupBy.CLASSIFICATION -> Classification.values().find { classif -> classif.classification == it.classification}?.stringResource?.let { stringRes -> stringResource(id = stringRes) }?: it.classification?:stringResource(id = R.string.classification_no_classification)
                GroupBy.PRIORITY -> {
                    when (it.priority) {
                        null -> context.resources.getStringArray(R.array.priority)[0]
                        in 0..9 -> context.resources.getStringArray(R.array.priority)[it.priority?:0]
                        else -> it.priority.toString()
                    }
                }
                GroupBy.DATE -> ICalObject.getDtstartTextInfo(module = Module.JOURNAL, dtstart = it.dtstart, dtstartTimezone = it.dtstartTimezone, daysOnly = true, context = context)
                GroupBy.START -> ICalObject.getDtstartTextInfo(module = Module.TODO, dtstart = it.dtstart, dtstartTimezone = it.dtstartTimezone, daysOnly = true, context = context)
                GroupBy.DUE -> ICalObject.getDueTextInfo(due = it.due, dueTimezone = it.dueTimezone, percent = it.percent, daysOnly = true, context = context)
                else -> {
                    it.module
                }
            }
        }


        val subtasks = prefs[ListWidgetReceiver.subtasks]?.map { Json.decodeFromString<ICal4ListWidget>(it) } ?: emptyList()
        val subnotes = prefs[ListWidgetReceiver.subnotes]?.map { Json.decodeFromString<ICal4ListWidget>(it) } ?: emptyList()
        val listExceedLimits = prefs[ListWidgetReceiver.listExceedsLimits] ?: false

        val subtasksGrouped = subtasks.groupBy { it.vtodoUidOfParent }
        val subnotesGrouped = subnotes.groupBy { it.vjournalUidOfParent }


        val mainIntent = Intent(context, MainActivity2::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val imageSize = 36.dp

        val backgorundColor = GlanceTheme.colors.primaryContainer.getColor(context).copy(alpha = listWidgetConfig?.widgetAlpha ?: 1F)
        val textColor = GlanceTheme.colors.onPrimaryContainer
        val entryColor = GlanceTheme.colors.surface.getColor(context).copy(alpha = listWidgetConfig?.widgetAlphaEntries ?: 1F)
        val entryTextColor = GlanceTheme.colors.onSurface
        val entryOverdueTextColor = GlanceTheme.colors.error

        GlanceTheme {
            Column(
                modifier = GlanceModifier
                    .appWidgetBackground()
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
                    .background(backgorundColor),
            ) {

                val addNewIntent = Intent(context, MainActivity2::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    action = when (listWidgetConfig?.module) {
                        Module.JOURNAL -> MainActivity2.INTENT_ACTION_ADD_JOURNAL
                        Module.NOTE -> MainActivity2.INTENT_ACTION_ADD_NOTE
                        Module.TODO -> MainActivity2.INTENT_ACTION_ADD_TODO
                        else -> MainActivity2.INTENT_ACTION_ADD_NOTE
                    }
                }

                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_jtx),
                        contentDescription = context.getString(R.string.app_name),
                        modifier = GlanceModifier
                            .clickable(actionStartActivity(mainIntent))
                            .padding(8.dp)
                            .size(imageSize)
                    )

                    Text(
                        text = when (listWidgetConfig?.module) {
                            Module.JOURNAL -> context.getString(R.string.list_tabitem_journals)
                            Module.NOTE -> context.getString(R.string.list_tabitem_notes)
                            Module.TODO -> context.getString(R.string.list_tabitem_todos)
                            else -> context.getString(R.string.list_tabitem_notes)
                        },
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimaryContainer,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier
                            .defaultWeight()
                    )

                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_settings),
                        contentDescription = context.getString(R.string.widget_list_configuration),
                        modifier = GlanceModifier
                            .clickable(actionRunCallback<ListWidgetOpenConfigActionCallback>())
                            .padding(8.dp)
                            .size(imageSize)
                    )

                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_add),
                        contentDescription = context.getString(R.string.add),
                        modifier = GlanceModifier
                            .clickable(actionStartActivity(addNewIntent))
                            .padding(8.dp)
                            .size(imageSize)
                    )
                }

                if (groupedList.isNotEmpty()) {
                    LazyColumn(
                        modifier = GlanceModifier
                            .padding(bottom = 2.dp, start = 2.dp, end = 2.dp, top = 0.dp)
                            .cornerRadius(8.dp)
                    ) {

                        groupedList.forEach { (key, group) ->
                            if (groupedList.keys.size > 1) {
                                item {
                                    Text(
                                        text = key,
                                        style = TextStyle(
                                            color = GlanceTheme.colors.onPrimaryContainer,
                                            //fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = GlanceModifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            group.forEach group@ { entry ->
                                if (listWidgetConfig?.isExcludeDone == true && entry.percent == 100)
                                    return@group

                                if (entry.summary.isNullOrEmpty() && entry.description.isNullOrEmpty())
                                    return@group

                                item {
                                    ListEntry(
                                        obj = entry,
                                        entryColor = entryColor,
                                        textColor = entryTextColor,
                                        textColorOverdue = entryOverdueTextColor,
                                        checkboxEnd = listWidgetConfig?.checkboxPositionEnd ?: false,
                                        showDescription = listWidgetConfig?.showDescription ?: true,
                                        modifier = GlanceModifier
                                            .fillMaxWidth()
                                            .padding(
                                                bottom = 2.dp,
                                            )
                                    )
                                }

                                if (listWidgetConfig?.flatView == false && listWidgetConfig.showSubtasks) {
                                    subtasksGrouped[entry.uid]?.forEach subtasks@ { subtask ->

                                        if (listWidgetConfig.isExcludeDone && subtask.percent == 100)
                                            return@subtasks

                                        if (subtask.summary.isNullOrEmpty() && subtask.description.isNullOrEmpty())
                                            return@subtasks

                                        item {
                                            ListEntry(
                                                obj = subtask,
                                                entryColor = entryColor,
                                                textColor = entryTextColor,
                                                textColorOverdue = entryOverdueTextColor,
                                                checkboxEnd = listWidgetConfig.checkboxPositionEnd,
                                                showDescription = listWidgetConfig.showDescription,
                                                modifier = GlanceModifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        bottom = 2.dp,
                                                        start = 16.dp
                                                    )
                                            )
                                        }
                                    }
                                }

                                if (listWidgetConfig?.flatView == false && listWidgetConfig.showSubnotes) {
                                    subnotesGrouped[entry.uid]?.forEach subnotes@ { subnote ->

                                        if (subnote.summary.isNullOrEmpty() && subnote.description.isNullOrEmpty())
                                            return@subnotes

                                        item {
                                            ListEntry(
                                                obj = subnote,
                                                entryColor = entryColor,
                                                textColor = entryTextColor,
                                                textColorOverdue = entryOverdueTextColor,
                                                checkboxEnd = listWidgetConfig.checkboxPositionEnd,
                                                showDescription = listWidgetConfig.showDescription,
                                                modifier = GlanceModifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        bottom = 2.dp,
                                                        start = 16.dp
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }


                        if (listExceedLimits)
                            item {
                                Text(
                                    text = context.getString(R.string.widget_list_maximum_entries_reached, MAX_ENTRIES),
                                    style = TextStyle(
                                        color = textColor,
                                        fontSize = 10.sp,
                                        fontStyle = FontStyle.Italic,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = GlanceModifier.fillMaxWidth().padding(8.dp)
                                )
                            }
                    }
                } else {
                    Column(
                        modifier = GlanceModifier.padding(8.dp).fillMaxWidth().fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = context.getString(R.string.widget_list_no_entries_found),
                            style = TextStyle(
                                color = textColor,
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }
                }
            }
        }
    }
}
