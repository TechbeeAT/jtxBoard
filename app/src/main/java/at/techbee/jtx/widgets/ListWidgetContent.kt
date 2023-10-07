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
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.database.relations.ICal4ListRel


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
    val groupedList = ICal4ListRel.getGroupedList(
        initialList = list,
        groupBy = listWidgetConfig.groupBy,
        sortOrder = listWidgetConfig.sortOrder,
        module = listWidgetConfig.module,
        context = context
    )

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

                if (list.size == MAX_WIDGET_ENTRIES) {
                    item {
                        Text(
                            text = context.getString(R.string.widget_list_maximum_entries_reached, MAX_WIDGET_ENTRIES),
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