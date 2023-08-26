/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

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
import at.techbee.jtx.R
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.ui.list.GroupBy
import at.techbee.jtx.ui.list.SortOrder


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
    onCheckedChange: (iCalObjectId: Long, checked: Boolean) -> Unit,
    onOpenWidgetConfig: () -> Unit,
    onAddNew: () -> Unit,
    onOpenFilteredList: () -> Unit,
    modifier: GlanceModifier = GlanceModifier
) {

    val context = LocalContext.current

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

    val groupedList = when (listWidgetConfig.groupBy) {
        GroupBy.CATEGORY -> mutableMapOf<String, MutableList<ICal4ListWidget>>().apply {
            //TODO: replace by actual list!
            sortedList.forEach { sortedEntry ->
                if (!sortedEntry.categories.isNullOrEmpty()) {
                    sortedEntry.categories!!.split(", ").forEach { category ->
                        if (this.containsKey(category))
                            this[category]?.add(sortedEntry)
                        else
                            this[category] = mutableListOf(sortedEntry)
                    }
                } else {
                    if (this.containsKey(context.getString(R.string.filter_no_category)))
                        this[context.getString(R.string.filter_no_category)]?.add(sortedEntry)
                    else
                        this[context.getString(R.string.filter_no_category)] = mutableListOf(sortedEntry)
                }
            }
        }
        GroupBy.RESOURCE -> mutableMapOf<String, MutableList<ICal4ListWidget>>().apply {
            //TODO: replace by actual list!
            sortedList.forEach { sortedEntry ->
                if (!sortedEntry.resources.isNullOrEmpty()) {
                    sortedEntry.resources!!.split(", ").forEach { resource ->
                        if (this.containsKey(resource))
                            this[resource]?.add(sortedEntry)
                        else
                            this[resource] = mutableListOf(sortedEntry)
                    }
                } else {
                    if (this.containsKey(context.getString(R.string.filter_no_resource)))
                        this[context.getString(R.string.filter_no_resource)]?.add(sortedEntry)
                    else
                        this[context.getString(R.string.filter_no_resource)] = mutableListOf(sortedEntry)
                }
            }
        }
        //GroupBy.CATEGORY -> sortedList.groupBy { it.categories ?: context.getString(R.string.filter_no_category) }.toSortedMap()
        //GroupBy.RESOURCE -> sortedList.groupBy { it.resources ?: context.getString(R.string.filter_no_resource) }.toSortedMap()
        GroupBy.STATUS -> sortedList.groupBy { Status.values().firstOrNull { status -> status.status == it.iCal4List.status }?.stringResource?.let { stringRes -> context.getString(stringRes) } ?: it.iCal4List.status ?: context.getString(R.string.status_no_status) }
        GroupBy.CLASSIFICATION -> sortedList.groupBy { Classification.values().firstOrNull { classif -> classif.classification == it.iCal4List.classification }?.stringResource?.let { stringRes -> context.getString(stringRes) } ?: it.iCal4List.classification ?: context.getString(R.string.classification_no_classification) }
        GroupBy.ACCOUNT -> sortedList.groupBy { it.iCal4List.accountName ?:"" }
        GroupBy.COLLECTION -> sortedList.groupBy { it.iCal4List.collectionDisplayName ?:"" }
        GroupBy.PRIORITY -> sortedList.groupBy {
            when (it.iCal4List.priority) {
                null -> context.resources.getStringArray(R.array.priority)[0]
                in 0..9 -> context.resources.getStringArray(R.array.priority)[it.priority ?: 0]
                else -> it.iCal4List.priority.toString()
            }
        }
        GroupBy.DATE -> sortedList.groupBy { ICalObject.getDtstartTextInfo(module = Module.JOURNAL, dtstart = it.iCal4List.dtstart, dtstartTimezone = it.iCal4List.dtstartTimezone, daysOnly = true, context = context) }
        GroupBy.START -> sortedList.groupBy { ICalObject.getDtstartTextInfo(module = Module.TODO, dtstart = it.iCal4List.dtstart, dtstartTimezone = it.iCal4List.dtstartTimezone, daysOnly = true, context = context) }
        GroupBy.DUE -> sortedList.groupBy { ICalObject.getDueTextInfo(status = it.iCal4List.status, due = it.iCal4List.due, dueTimezone = it.iCal4List.dueTimezone, percent = it.iCal4List.percent, daysOnly = true, context = context) }
        null -> sortedList.groupBy { it.module }
    }


    val imageSize = 36.dp

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable { onOpenFilteredList() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                provider = ImageProvider(R.drawable.ic_widget_jtx),
                contentDescription = context.getString(R.string.app_name),
                modifier = GlanceModifier
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
                modifier = GlanceModifier.defaultWeight()
            )

            Image(
                provider = ImageProvider(R.drawable.ic_widget_settings),
                contentDescription = context.getString(R.string.widget_list_configuration),
                modifier = GlanceModifier
                    .clickable { onOpenWidgetConfig() }
                    .padding(8.dp)
                    .size(imageSize),
                colorFilter = ColorFilter.tint(textColor)
            )

            Image(
                provider = ImageProvider(R.drawable.ic_widget_add),
                contentDescription = context.getString(R.string.add),
                modifier = GlanceModifier
                    .clickable{ onAddNew() }
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
                                onCheckedChange = onCheckedChange,
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .padding(
                                        bottom = 2.dp,
                                    )
                            )
                        }

                        if (!listWidgetConfig.flatView && listWidgetConfig.showSubtasks) {

                            subtasks
                                .filter { it.relatedto.any { subtaskRel -> subtaskRel.text == entry.iCal4List.uid && subtaskRel.reltype == Reltype.PARENT.name } }
                                .forEach subtasks@{ subtask ->

                                if (listWidgetConfig.isExcludeDone && (subtask.iCal4List.percent == 100 || subtask.iCal4List.status == Status.COMPLETED.status))
                                    return@subtasks

                                if (subtask.iCal4List.summary.isNullOrEmpty() && subtask.iCal4List.description.isNullOrEmpty())
                                    return@subtasks

                                item {
                                    ListEntry(
                                        obj = subtask.iCal4List,
                                        entryColor = entryColor,
                                        textColor = entryTextColor,
                                        textColorOverdue = entryOverdueTextColor,
                                        checkboxEnd = listWidgetConfig.checkboxPositionEnd,
                                        showDescription = listWidgetConfig.showDescription,
                                        onCheckedChange = onCheckedChange,
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

                        if (!listWidgetConfig.flatView && listWidgetConfig.showSubnotes) {
                            subnotes
                                .filter { it.relatedto.any { subnoteRel -> subnoteRel.text == entry.iCal4List.uid && subnoteRel.reltype == Reltype.PARENT.name } }
                                .forEach subnotes@{ subnote ->

                                if (subnote.iCal4List.summary.isNullOrEmpty() && subnote.iCal4List.description.isNullOrEmpty())
                                    return@subnotes

                                item {
                                    ListEntry(
                                        obj = subnote.iCal4List,
                                        entryColor = entryColor,
                                        textColor = entryTextColor,
                                        textColorOverdue = entryOverdueTextColor,
                                        checkboxEnd = listWidgetConfig.checkboxPositionEnd,
                                        showDescription = listWidgetConfig.showDescription,
                                        onCheckedChange = onCheckedChange,
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