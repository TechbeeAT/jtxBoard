/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.media.MediaPlayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredResource
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.reusable.cards.SubtaskCardCompact
import at.techbee.jtx.ui.reusable.elements.AudioPlaybackElement
import at.techbee.jtx.ui.reusable.elements.DragHandle
import at.techbee.jtx.ui.theme.jtxCardBorderStrokeWidth
import at.techbee.jtx.ui.theme.jtxCardCornerShape
import com.arnyminerz.markdowntext.MarkdownText
import sh.calvin.reorderable.ReorderableColumn


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListCardCompact(
    iCalObject: ICal4List,
    categories: List<Category>,
    resources: List<Resource>,
    subtasks: List<ICal4List>,
    storedCategories: List<StoredCategory>,
    storedResources: List<StoredResource>,
    storedStatuses: List<ExtendedStatus>,
    progressUpdateDisabled: Boolean,
    markdownEnabled: Boolean,
    settingIsAccessibilityMode: Boolean,
    selected: List<Long>,
    player: MediaPlayer?,
    isSubtaskDragAndDropEnabled: Boolean,
    modifier: Modifier = Modifier,
    onProgressChanged: (itemId: Long, newPercent: Int) -> Unit,
    onClick: (itemId: Long, list: List<ICal4List>, isReadOnly: Boolean) -> Unit,
    onLongClick: (itemId: Long, list: List<ICal4List>) -> Unit,
    onUpdateSortOrder: (List<ICal4List>) -> Unit
    ) {

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected.contains(iCalObject.id)) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            contentColor = if (selected.contains(iCalObject.id)) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        ),
        border = iCalObject.colorItem?.let { BorderStroke(jtxCardBorderStrokeWidth, Color(it)) },
        modifier = modifier
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 0.dp)
                .fillMaxWidth()
        ) {

            ListTopRow(
                ical4List = iCalObject,
                categories = categories,
                resources = resources,
                storedCategories = storedCategories,
                storedResources = storedResources,
                extendedStatuses = storedStatuses,
                includeJournalDate = true,
                isAccessibilityMode = settingIsAccessibilityMode
            )

            iCalObject.getAudioAttachmentAsUri()?.let {
                AudioPlaybackElement(
                    uri = it,
                    player = player,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 4.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {

                if (!iCalObject.summary.isNullOrEmpty()) {
                    Text(
                        text = iCalObject.summary?.trim() ?: "",
                        textDecoration = if (iCalObject.status == Status.CANCELLED.status) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                } else if (!iCalObject.description.isNullOrEmpty()) {
                    if(markdownEnabled)
                        MarkdownText(
                            markdown = iCalObject.description?.trim() ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    else
                        Text(
                            text = iCalObject.description?.trim() ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                }

                if (iCalObject.module == Module.TODO.name) {
                    Checkbox(
                        checked = iCalObject.percent == 100 || iCalObject.status == Status.COMPLETED.status,
                        enabled = !iCalObject.isReadOnly && !progressUpdateDisabled,
                        onCheckedChange = {
                            onProgressChanged(
                                iCalObject.id,
                                if (it) 100 else 0
                            )
                        }
                    )
                }
            }

            // put the description in the second row only if the first row was not already occupied by the description due to a missing summary
            if (!iCalObject.summary.isNullOrEmpty() && !iCalObject.description.isNullOrEmpty()) {
                if(markdownEnabled)
                    MarkdownText(
                        markdown = iCalObject.description?.trim() ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                else
                    Text(
                        text = iCalObject.description?.trim() ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
            }

            ReorderableColumn(
                list = subtasks,
                onSettle = { fromIndex, toIndex ->
                    val reordered = subtasks.toMutableList().apply {
                        add(toIndex, removeAt(fromIndex))
                    }
                    onUpdateSortOrder(reordered)
                },
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) { index, subtask, isDragging ->
                key(subtask.id) {

                    SubtaskCardCompact(
                        subtask = subtask,
                        selected = selected.contains(subtask.id),
                        onProgressChanged = onProgressChanged,
                        dragHandle = { if(isSubtaskDragAndDropEnabled) DragHandle(scope = this) },
                        modifier = Modifier
                            .clip(jtxCardCornerShape)
                            .combinedClickable(
                                onClick = { onClick(subtask.id, subtasks, subtask.isReadOnly) },
                                onLongClick = {
                                    if (!subtask.isReadOnly)
                                        onLongClick(subtask.id, subtasks)
                                }
                            )
                    )

                    if (subtask.id != subtasks.last().id)
                        HorizontalDivider(modifier = Modifier.alpha(0.25f))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListCardCompact_JOURNAL() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            dtstart = System.currentTimeMillis()
            colorItem = Color.Blue.toArgb()
            colorCollection = Color.Magenta.toArgb()
        }
        ListCardCompact(
            icalobject,
            categories = emptyList(),
            resources = emptyList(),
            subtasks = emptyList(),
            storedCategories = listOf(StoredCategory("Test", Color.Cyan.toArgb())),
            storedResources = listOf(StoredResource("Projector", Color.Green.toArgb())),
            storedStatuses = listOf(ExtendedStatus("Individual", Module.JOURNAL, Status.FINAL, Color.Green.toArgb())),
            progressUpdateDisabled = true,
            settingIsAccessibilityMode = false,
            markdownEnabled = false,
            selected = emptyList(),
            player = null,
            isSubtaskDragAndDropEnabled = true,
            onProgressChanged = { _, _ -> },
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            onUpdateSortOrder = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListCardCompact_JOURNAL2() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            dtstart = System.currentTimeMillis()
            colorItem = Color.Blue.toArgb()
            colorCollection = Color.Magenta.toArgb()
            categories = null
        }
        ListCardCompact(
            icalobject,
            subtasks = emptyList(),
            categories = emptyList(),
            resources = emptyList(),
            storedCategories = listOf(StoredCategory("Test", Color.Cyan.toArgb())),
            storedResources = listOf(StoredResource("Projector", Color.Green.toArgb())),
            storedStatuses = listOf(ExtendedStatus("Individual", Module.JOURNAL, Status.FINAL, Color.Green.toArgb())),
            progressUpdateDisabled = true,
            settingIsAccessibilityMode = false,
            markdownEnabled = false,
            selected = emptyList(),
            player = null,
            isSubtaskDragAndDropEnabled = true,
            onProgressChanged = { _, _ -> },
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            onUpdateSortOrder = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListCardCompact_NOTE() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            component = Component.VJOURNAL.name
            module = Module.NOTE.name
            dtstart = null
            dtstartTimezone = null
            status = Status.CANCELLED.status
        }
        ListCardCompact(
            icalobject,
            categories = emptyList(),
            resources = emptyList(),
            subtasks = emptyList(),
            storedCategories = listOf(StoredCategory("Test", Color.Cyan.toArgb())),
            storedResources = listOf(StoredResource("Projector", Color.Green.toArgb())),
            storedStatuses = listOf(ExtendedStatus("Individual", Module.JOURNAL, Status.FINAL, Color.Green.toArgb())),
            progressUpdateDisabled = true,
            settingIsAccessibilityMode = false,
            markdownEnabled = false,
            selected = emptyList(),
            player = null,
            isSubtaskDragAndDropEnabled = true,
            onProgressChanged = { _, _ -> },
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            onUpdateSortOrder = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListCardCompact_TODO() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            component = Component.VTODO.name
            module = Module.TODO.name
            percent = 89
            status = Status.IN_PROCESS.status
            classification = Classification.CONFIDENTIAL.name
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis() - 1
            summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        }
        ListCardCompact(
            icalobject,
            categories = emptyList(),
            resources = emptyList(),
            subtasks = listOf(icalobject, icalobject),
            storedCategories = listOf(StoredCategory("Test", Color.Cyan.toArgb())),
            storedResources = listOf(StoredResource("Projector", Color.Green.toArgb())),
            storedStatuses = listOf(ExtendedStatus("Individual", Module.JOURNAL, Status.FINAL, Color.Green.toArgb())),
            progressUpdateDisabled = true,
            settingIsAccessibilityMode = false,
            markdownEnabled = false,
            selected = emptyList(),
            player = null,
            isSubtaskDragAndDropEnabled = true,
            onProgressChanged = { _, _ -> },
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            onUpdateSortOrder = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ListCardCompact_TODO_only_summary() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            id = 1L
            component = Component.VTODO.name
            module = Module.TODO.name
            categories = null
            percent = null
            status = Status.IN_PROCESS.status
            classification = Classification.PUBLIC.name
            dtstart = null
            due = null
            numAttachments = 0
            numSubnotes = 0
            numSubtasks = 0
            numAttendees = 0
            numResources = 0
            numAlarms = 0
            numSubnotes = 0
            numComments = 0
            uploadPending = false
            isReadOnly = false
            summary = "Lorem ipsum"
            description = null
            categories = "Simpsons"
        }
        ListCardCompact(
            icalobject,
            categories = listOf(Category(text = "Category1"), Category(text = "Category2")),
            resources = listOf(Resource(text = "Resource1")),
            subtasks = listOf(icalobject, icalobject),
            storedCategories = listOf(StoredCategory("Test", Color.Cyan.toArgb())),
            storedResources = listOf(StoredResource("Projector", Color.Green.toArgb())),
            storedStatuses = listOf(ExtendedStatus("Individual", Module.JOURNAL, Status.FINAL, Color.Green.toArgb())),
            progressUpdateDisabled = true,
            settingIsAccessibilityMode = false,
            markdownEnabled = false,
            selected = listOf(icalobject.id),
            player = null,
            isSubtaskDragAndDropEnabled = true,
            onProgressChanged = { _, _ -> },
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            onUpdateSortOrder = { }
        )
    }
}
