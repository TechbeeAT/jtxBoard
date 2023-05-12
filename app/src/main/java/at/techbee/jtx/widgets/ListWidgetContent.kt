/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.ui.list.GroupBy
import at.techbee.jtx.ui.list.SortOrder
import at.techbee.jtx.widgets.elements.ListEntry


@Composable
fun ListWidgetContent(
    listWidgetConfig: ListWidgetConfig,
    list: List<ICal4ListRel>,
    subtasks: List<ICal4ListRel>,
    subnotes: List<ICal4ListRel>,
    textColor: ColorProvider,
    entryColor: ColorProvider,
    entryTextColor: ColorProvider,
    entryOverdueTextColor: ColorProvider,
    modifier: GlanceModifier = GlanceModifier
) {

    val context = LocalContext.current

    val addNewIntent = Intent(context, MainActivity2::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        action = when (listWidgetConfig.module) {
            Module.JOURNAL -> MainActivity2.INTENT_ACTION_ADD_JOURNAL
            Module.NOTE -> MainActivity2.INTENT_ACTION_ADD_NOTE
            Module.TODO -> MainActivity2.INTENT_ACTION_ADD_TODO
        }
        listWidgetConfig.searchCollection.firstOrNull()?.let {
            putExtra(MainActivity2.INTENT_EXTRA_COLLECTION2PRESELECT, it)
        }
    }

    val openModuleIntent = Intent(context, MainActivity2::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        action = when (listWidgetConfig.module) {
            Module.JOURNAL -> MainActivity2.INTENT_ACTION_OPEN_JOURNALS
            Module.NOTE -> MainActivity2.INTENT_ACTION_OPEN_NOTES
            Module.TODO -> MainActivity2.INTENT_ACTION_OPEN_TODOS
        }
    }


    // first apply a proper sort order, then group
    var sortedList = when (listWidgetConfig.groupBy) {
        GroupBy.STATUS -> list.sortedBy {
            if (listWidgetConfig.module == Module.TODO && it.iCal4List.percent != 100)
                try {
                    Status.valueOf(it.iCal4List.status ?: Status.NEEDS_ACTION.name).ordinal
                } catch (e: java.lang.IllegalArgumentException) {
                    -1
                }
            else
                try {
                    Status.valueOf(it.iCal4List.status ?: Status.FINAL.name).ordinal
                } catch (e: java.lang.IllegalArgumentException) {
                    -1
                }
        }
        GroupBy.CLASSIFICATION -> list.sortedBy {
            try {
                Classification.valueOf(it.iCal4List.classification ?: Classification.PUBLIC.name).ordinal
            } catch (e: java.lang.IllegalArgumentException) {
                -1
            }
        }
        else -> list
    }
    if ((listWidgetConfig.groupBy == GroupBy.STATUS || listWidgetConfig.groupBy == GroupBy.CLASSIFICATION) && listWidgetConfig.sortOrder == SortOrder.DESC)
        sortedList = sortedList.asReversed()

    val groupedList = sortedList.groupBy {
        when (listWidgetConfig.groupBy) {
            GroupBy.STATUS -> Status.values().firstOrNull { status -> status.status == it.iCal4List.status }?.stringResource?.let { stringRes -> context.getString(stringRes) } ?: it.iCal4List.status ?: context.getString(R.string.status_no_status)
            GroupBy.CLASSIFICATION -> Classification.values().firstOrNull { classif -> classif.classification == it.iCal4List.classification }?.stringResource?.let { stringRes -> context.getString(stringRes) } ?: it.iCal4List.classification ?: context.getString(R.string.classification_no_classification)
            GroupBy.ACCOUNT -> it.iCal4List.accountName ?:""
            GroupBy.COLLECTION -> it.iCal4List.collectionDisplayName ?:""
            GroupBy.PRIORITY -> {
                when (it.iCal4List.priority) {
                    null -> context.resources.getStringArray(R.array.priority)[0]
                    in 0..9 -> context.resources.getStringArray(R.array.priority)[it.iCal4List.priority ?: 0]
                    else -> it.iCal4List.priority.toString()
                }
            }
            GroupBy.DATE -> ICalObject.getDtstartTextInfo(module = Module.JOURNAL, dtstart = it.iCal4List.dtstart, dtstartTimezone = it.iCal4List.dtstartTimezone, daysOnly = true, context = context)
            GroupBy.START -> ICalObject.getDtstartTextInfo(module = Module.TODO, dtstart = it.iCal4List.dtstart, dtstartTimezone = it.iCal4List.dtstartTimezone, daysOnly = true, context = context)
            GroupBy.DUE -> ICalObject.getDueTextInfo(status = it.iCal4List.status, due = it.iCal4List.due, dueTimezone = it.iCal4List.dueTimezone, percent = it.iCal4List.percent, daysOnly = true, context = context)
            else -> {
                it.iCal4List.module
            }
        }
    }

    /*
    val subtasksGrouped = subtasks.groupBy { it.iCal4List.parentUID }
    val subnotesGrouped = subnotes.groupBy { it.iCal4List.parentUID }
     */

    val imageSize = 36.dp

    Column(
        modifier = modifier,
    ) {
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
                    .clickable(actionStartActivity(openModuleIntent))
                    .padding(8.dp)
                    .size(imageSize),
                colorFilter = ColorFilter.tint(textColor)
            )

            Text(
                text =
                listWidgetConfig.widgetHeader.ifEmpty {
                    when (listWidgetConfig.module) {
                        Module.JOURNAL -> context.getString(R.string.list_tabitem_journals)
                        Module.NOTE -> context.getString(R.string.list_tabitem_notes)
                        Module.TODO -> context.getString(R.string.list_tabitem_todos)
                    }
                },
                style = TextStyle(
                    color = GlanceTheme.colors.onPrimaryContainer,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier
                    .clickable(actionStartActivity(openModuleIntent))
                    .defaultWeight()
            )

            Image(
                provider = ImageProvider(R.drawable.ic_widget_settings),
                contentDescription = context.getString(R.string.widget_list_configuration),
                modifier = GlanceModifier
                    .clickable(actionRunCallback<ListWidgetOpenConfigActionCallback>())
                    .padding(8.dp)
                    .size(imageSize),
                colorFilter = ColorFilter.tint(textColor)
            )

            Image(
                provider = ImageProvider(R.drawable.ic_widget_add),
                contentDescription = context.getString(R.string.add),
                modifier = GlanceModifier
                    .clickable(actionStartActivity(addNewIntent))
                    .padding(8.dp)
                    .size(imageSize),
                colorFilter = ColorFilter.tint(textColor)
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

                    group.forEach group@{ entry ->
                        if (listWidgetConfig.isExcludeDone && (entry.iCal4List.percent == 100 || entry.iCal4List.status == Status.COMPLETED.status))
                            return@group

                        if (entry.iCal4List.summary.isNullOrEmpty() && entry.iCal4List.description.isNullOrEmpty())
                            return@group

                        item {
                            ListEntry(
                                obj = entry.iCal4List,
                                entryColor = entryColor,
                                textColor = entryTextColor,
                                textColorOverdue = entryOverdueTextColor,
                                checkboxEnd = listWidgetConfig.checkboxPositionEnd,
                                showDescription = listWidgetConfig.showDescription,
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .padding(
                                        bottom = 2.dp,
                                    )
                            )
                        }

                        if (!listWidgetConfig.flatView && listWidgetConfig.showSubtasks) {
                            /*
                            subtasksGrouped[entry.iCal4List.uid]?.forEach subtasks@{ subtask ->

                                if (listWidgetConfig.isExcludeDone && (subtask.percent == 100 || subtask.status == Status.COMPLETED.status))
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

                             */
                        }

                        if (!listWidgetConfig.flatView && listWidgetConfig.showSubnotes) {
                            /*
                            subnotesGrouped[entry.uid]?.forEach subnotes@{ subnote ->

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

                             */
                        }
                    }
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