/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
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
import at.techbee.jtx.ui.list.CheckboxPosition
import kotlin.math.ceil
import kotlin.math.max

/**
 * Minimum number of lines shown for the description of an entry in the list widget.
 */
const val MIN_WIDGET_DESCRIPTION_LINES = 2

/** Estimated height of the [TitleBar] of the widget */
private val WIDGET_TITLE_BAR_HEIGHT = 56.dp
/** Estimated ratio between the height of a line of text and its font size */
private const val TEXT_LINE_HEIGHT_FACTOR = 1.2f
/** Estimated ratio between the average width of a character (including word wrapping losses) and the font size */
private const val TEXT_CHAR_WIDTH_FACTOR = 0.55f
/** Estimated height of the row with the meta information (dates, priority, status, classification) of an entry */
private val WIDGET_META_INFO_HEIGHT = 18.dp

/**
 * Estimates the number of lines a [text] needs when it is displayed with the given
 * [fontSize] in a text field of the given [width].
 * @return the estimated number of lines, 0 if the text is null or empty
 */
fun estimateWidgetTextLines(text: String?, fontSize: Dp, width: Dp): Int {
    if (text.isNullOrEmpty())
        return 0
    if (width <= 0.dp || fontSize <= 0.dp)
        return text.lines().size
    val charsPerLine = max(1, (width / (fontSize * TEXT_CHAR_WIDTH_FACTOR)).toInt())
    return text.lines().sumOf { line -> max(1, ceil(line.length.toFloat() / charsPerLine).toInt()) }
}

/**
 * Estimates how many lines of the description each entry in the list widget may show,
 * such that the entries make use of the available height of the widget
 * (see https://github.com/TechbeeAT/jtxBoard/issues/1950).
 *
 * The result is a heuristic based on estimated heights. If it overestimates, the list
 * simply becomes scrollable.
 * If only a single entry is shown, its description is not limited at all, such that the
 * whole description can be read by scrolling.
 *
 * @param heightForDescriptions the estimated height of the widget that remains for all
 * descriptions, i.e. the widget height minus the title bar, paddings, group headers and
 * the parts of the entries other than the description
 * @param numEntries number of shown entries (including subtasks and subnotes)
 * @param numEntriesWithDescription number of shown entries that display a description
 * @param descriptionLineHeight estimated height of one line of the description
 * @return the maximum number of description lines per entry, at least [MIN_WIDGET_DESCRIPTION_LINES],
 * or [Int.MAX_VALUE] if only a single entry is shown
 */
fun calculateWidgetDescriptionMaxLines(
    heightForDescriptions: Dp,
    numEntries: Int,
    numEntriesWithDescription: Int,
    descriptionLineHeight: Dp
): Int {
    if (numEntries == 1)
        return Int.MAX_VALUE
    if (numEntriesWithDescription <= 0 || descriptionLineHeight <= 0.dp)
        return MIN_WIDGET_DESCRIPTION_LINES
    val lines = heightForDescriptions / numEntriesWithDescription / descriptionLineHeight
    return max(MIN_WIDGET_DESCRIPTION_LINES, lines.toInt())
}


@Composable
fun ListWidgetContent(
    listWidgetConfig: ListWidgetConfig,
    list: List<ICal4ListRel>,
    subtasks: List<ICal4ListRel>,
    subnotes: List<ICal4ListRel>,
    backgroundColor: ColorProvider,
    textColor: ColorProvider,
    entryColor: ColorProvider,
    entryTextColor: ColorProvider,
    entryTextCancelledColor: ColorProvider,
    entryHeaderTextColor: ColorProvider,
    onCheckedChange: (iCalObjectId: Long, checked: Boolean) -> Action,
    onOpenWidgetConfig: () -> Unit,
    onAddNew: () -> Unit,
    onOpenFilteredList: () -> Unit
) {

    val context = LocalContext.current
    val groupedList = ICal4ListRel.getGroupedList(
        sortedList = list,
        groupBy = listWidgetConfig.groupBy,
        sortOrder = listWidgetConfig.sortOrder,
        module = listWidgetConfig.module,
        context = context
    )

    val entryPaddingBottom = 1.dp
    val subEntryPaddingStart = 16.dp

    fun isShown(entry: ICal4ListRel) =
        !(listWidgetConfig.isExcludeDone && (entry.iCal4List.percent == 100 || entry.iCal4List.status == Status.COMPLETED.status))
                && !(entry.iCal4List.summary.isNullOrEmpty() && entry.iCal4List.description.isNullOrEmpty())

    fun shownSubtasksOf(entry: ICal4ListRel) =
        if (listWidgetConfig.flatView || !listWidgetConfig.showSubtasks)
            emptyList()
        else
            subtasks
                .filter { it.relatedto.any { subtaskRel -> subtaskRel.text == entry.iCal4List.uid && subtaskRel.reltype == Reltype.PARENT.name } }
                .filter { isShown(it) }

    fun shownSubnotesOf(entry: ICal4ListRel) =
        if (listWidgetConfig.flatView || !listWidgetConfig.showSubnotes)
            emptyList()
        else
            subnotes
                .filter { it.relatedto.any { subnoteRel -> subnoteRel.text == entry.iCal4List.uid && subnoteRel.reltype == Reltype.PARENT.name } }
                .filter { !(it.iCal4List.summary.isNullOrEmpty() && it.iCal4List.description.isNullOrEmpty()) }

    // Estimate the height that remains for the descriptions to let them use the available space of the widget
    val displayMetrics = context.resources.displayMetrics
    fun TextUnit.toDp() = (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, displayMetrics) / displayMetrics.density).dp
    fun TextUnit.lineHeightInDp() = toDp() * TEXT_LINE_HEIGHT_FACTOR
    val shownEntries = groupedList.values.flatten().filter { isShown(it) }
        .flatMap { listOf(it) + shownSubtasksOf(it) + shownSubnotesOf(it) }
    val numGroupHeaders = if (groupedList.keys.size > 1) groupedList.keys.size else 0
    val textWidth = LocalSize.current.width - 40.dp    // paddings of the scaffold, the list and the entry
    val heightOfEntriesWithoutDescription = shownEntries.fold(0.dp) { height, entry ->
        val hasCheckbox = entry.iCal4List.module == Module.TODO.name && listWidgetConfig.checkboxPosition != CheckboxPosition.OFF && !entry.iCal4List.isReadOnly
        val summaryWidth = if (hasCheckbox) textWidth - 48.dp else textWidth
        height + 12.dp + entryPaddingBottom +
                (if (entry.iCal4List.hasWidgetMetaInfo()) WIDGET_META_INFO_HEIGHT else 0.dp) +
                14.sp.lineHeightInDp() * estimateWidgetTextLines(entry.iCal4List.summary, 14.sp.toDp(), summaryWidth)
    }
    val heightForDescriptions = LocalSize.current.height -
            WIDGET_TITLE_BAR_HEIGHT - 18.dp -    // title bar, paddings and spacer at the end of the list
            (14.sp.lineHeightInDp() + 8.dp) * numGroupHeaders -
            heightOfEntriesWithoutDescription
    val descriptionMaxLines = calculateWidgetDescriptionMaxLines(
        heightForDescriptions = heightForDescriptions,
        numEntries = shownEntries.size,
        numEntriesWithDescription = if (listWidgetConfig.showDescription) shownEntries.count { !it.iCal4List.description.isNullOrEmpty() } else 0,
        descriptionLineHeight = 12.sp.lineHeightInDp()
    )

    Scaffold(
        backgroundColor = backgroundColor,
        titleBar = {
            TitleBar(
                startIcon = ImageProvider(R.drawable.ic_widget_jtx),
                iconColor = textColor,
                title = listWidgetConfig.widgetHeader.ifEmpty {
                    when (listWidgetConfig.module) {
                        Module.JOURNAL -> context.getString(R.string.list_tabitem_journals)
                        Module.NOTE -> context.getString(R.string.list_tabitem_notes)
                        Module.TODO -> context.getString(R.string.list_tabitem_todos)
                    }
                },
                textColor = textColor,
                actions = {

                    CircleIconButton(
                        imageProvider = ImageProvider(R.drawable.ic_open_in_new),
                        contentDescription = context.getString(R.string.widget_list_configuration),
                        onClick = { onOpenFilteredList() },
                        contentColor = textColor,
                        backgroundColor = null
                    )

                    CircleIconButton(
                        imageProvider = ImageProvider(R.drawable.ic_widget_settings),
                        contentDescription = context.getString(R.string.widget_list_configuration),
                        onClick = { onOpenWidgetConfig() },
                        contentColor = textColor,
                        backgroundColor = null
                    )

                    CircleIconButton(
                        imageProvider = ImageProvider(R.drawable.ic_widget_add),
                        contentDescription = context.getString(R.string.add),
                        onClick = { onAddNew() },
                        contentColor = textColor,
                        backgroundColor = null
                    )
                }
            )
        }
    ) {

        Column(
            modifier = GlanceModifier
                .padding(bottom = 8.dp)
                .fillMaxSize(),
        ) {

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
                                        color = textColor,
                                        //fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = GlanceModifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        group.forEach group@{ entry ->
                            if (!isShown(entry))
                                return@group

                            item {
                                ListEntry(
                                    obj = entry.iCal4List,
                                    entryColor = entryColor,
                                    textColor = if(entry.iCal4List.status == Status.CANCELLED.status) entryTextCancelledColor else entryTextColor,
                                    headerTextColor = entryHeaderTextColor,
                                    checkboxPosition = listWidgetConfig.checkboxPosition,
                                    showDescription = listWidgetConfig.showDescription,
                                    descriptionMaxLines = descriptionMaxLines,
                                    onCheckedChange = onCheckedChange,
                                    modifier = GlanceModifier
                                        .fillMaxWidth()
                                        .padding(
                                            bottom = entryPaddingBottom,
                                        )
                                )
                            }

                            shownSubtasksOf(entry).forEach { subtask ->
                                item {
                                    ListEntry(
                                        obj = subtask.iCal4List,
                                        entryColor = entryColor,
                                        textColor = if(subtask.iCal4List.status == Status.CANCELLED.status) entryTextCancelledColor else entryTextColor,
                                        headerTextColor = entryHeaderTextColor,
                                        checkboxPosition = listWidgetConfig.checkboxPosition,
                                        showDescription = listWidgetConfig.showDescription,
                                        descriptionMaxLines = descriptionMaxLines,
                                        onCheckedChange = onCheckedChange,
                                        modifier = GlanceModifier
                                            .fillMaxWidth()
                                            .padding(
                                                bottom = entryPaddingBottom,
                                                start = subEntryPaddingStart
                                            )
                                    )
                                }
                            }

                            shownSubnotesOf(entry).forEach { subnote ->
                                item {
                                    ListEntry(
                                        obj = subnote.iCal4List,
                                        entryColor = entryColor,
                                        textColor = if(subnote.iCal4List.status == Status.CANCELLED.status) entryTextCancelledColor else entryTextColor,
                                        headerTextColor = entryHeaderTextColor,
                                        checkboxPosition = listWidgetConfig.checkboxPosition,
                                        showDescription = listWidgetConfig.showDescription,
                                        descriptionMaxLines = descriptionMaxLines,
                                        onCheckedChange = onCheckedChange,
                                        modifier = GlanceModifier
                                            .fillMaxWidth()
                                            .padding(
                                                bottom = entryPaddingBottom,
                                                start = subEntryPaddingStart
                                            )
                                    )
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

                    item {
                        Box(modifier = GlanceModifier.height(8.dp)) {}
                    }
                }
            } else {
                Column(
                    modifier = GlanceModifier.padding(8.dp).fillMaxWidth().fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = context.getString(R.string.widget_list_nothing_here),
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