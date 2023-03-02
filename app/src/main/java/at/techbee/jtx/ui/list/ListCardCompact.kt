/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.database.*
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.reusable.cards.SubtaskCardCompact
import at.techbee.jtx.ui.reusable.elements.ListStatusBar
import at.techbee.jtx.ui.theme.jtxCardCornerShape
import at.techbee.jtx.util.DateTimeUtils
import com.google.accompanist.flowlayout.FlowRow


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ListCardCompact(
    iCalObject: ICal4List,
    subtasks: List<ICal4List>,
    progressUpdateDisabled: Boolean,
    selected: List<Long>,
    modifier: Modifier = Modifier,
    onProgressChanged: (itemId: Long, newPercent: Int) -> Unit,
    onClick: (itemId: Long, list: List<ICal4List>) -> Unit,
    onLongClick: (itemId: Long, list: List<ICal4List>) -> Unit
) {

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected.contains(iCalObject.id)) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = iCalObject.colorItem?.let { BorderStroke(1.dp, Color(it)) }, 
        modifier = modifier
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth()
        ) {
            FlowRow(
                mainAxisSpacing = 4.dp,
                crossAxisSpacing = 2.dp
            ) {
                Badge(
                    containerColor = iCalObject.colorCollection?.let { Color(it) } ?: MaterialTheme.colorScheme.primaryContainer,
                    contentColor = iCalObject.colorCollection?.let { contentColorFor(backgroundColor = Color(it)) } ?: MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(iCalObject.collectionDisplayName?.firstOrNull()?.toString() ?: " ")
                }

                if (iCalObject.categories?.isNotBlank() == true) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) {
                        Text(
                            iCalObject.categories ?: "",
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(horizontal = 2.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (iCalObject.module == Module.JOURNAL.name && iCalObject.dtstart != null) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) {
                        Text(
                            DateTimeUtils.convertLongToShortDateTimeString(
                                iCalObject.dtstart,
                                iCalObject.dtstartTimezone
                            ),
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
                if (iCalObject.module == Module.TODO.name && iCalObject.due != null) {
                    Badge(
                        containerColor = if (ICalObject.isOverdue(
                                iCalObject.percent,
                                iCalObject.due,
                                iCalObject.dueTimezone
                            ) == true
                        ) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (ICalObject.isOverdue(
                                iCalObject.percent,
                                iCalObject.due,
                                iCalObject.dueTimezone
                            ) == true
                        ) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text(
                            ICalObject.getDueTextInfo(due = iCalObject.due, dueTimezone = iCalObject.dueTimezone, percent = iCalObject.percent, context = LocalContext.current),
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            color = if (ICalObject.isOverdue(
                                    iCalObject.percent,
                                    iCalObject.due,
                                    iCalObject.dueTimezone
                                ) == true
                            ) MaterialTheme.colorScheme.error else LocalContentColor.current,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }

                AnimatedVisibility(iCalObject.status in listOf(Status.CANCELLED.status, Status.DRAFT.status, Status.CANCELLED.status)) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        ListStatusBar(status = iCalObject.status)
                    }
                }

                AnimatedVisibility(iCalObject.classification in listOf(Classification.CONFIDENTIAL.classification, Classification.PRIVATE.classification)) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        ListStatusBar(classification = iCalObject.classification)
                    }
                }

                AnimatedVisibility(iCalObject.priority in 1..9) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        ListStatusBar(priority = iCalObject.priority)
                    }
                }

                AnimatedVisibility(
                    iCalObject.numAttendees > 0
                            || iCalObject.numAttachments > 0
                            || iCalObject.numComments > 0
                            || iCalObject.numResources > 0
                            || iCalObject.numAlarms > 0 || iCalObject.numSubtasks > 0
                            || iCalObject.numSubnotes > 0
                            || iCalObject.url?.isNotEmpty() == true
                            || iCalObject.location?.isNotEmpty() == true
                            || iCalObject.contact?.isNotEmpty() == true
                ) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        ListStatusBar(
                            numAttendees = iCalObject.numAttendees,
                            numAttachments = iCalObject.numAttachments,
                            numComments = iCalObject.numComments,
                            numResources = iCalObject.numResources,
                            numAlarms = iCalObject.numAlarms,
                            numSubtasks = iCalObject.numSubtasks,
                            numSubnotes = iCalObject.numSubnotes,
                            hasURL = iCalObject.url?.isNotBlank() == true,
                            hasLocation = iCalObject.location?.isNotBlank() == true,
                            hasContact = iCalObject.contact?.isNotBlank() == true
                        )
                    }
                }

                AnimatedVisibility(
                    iCalObject.isReadOnly
                            || iCalObject.uploadPending
                            || iCalObject.rrule != null
                            || iCalObject.recurid != null
                ) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        ListStatusBar(
                            isReadOnly = iCalObject.isReadOnly,
                            uploadPending = iCalObject.uploadPending,
                            isRecurring = iCalObject.rrule != null || iCalObject.recurid != null,
                            isRecurringModified = iCalObject.recurid != null && iCalObject.sequence > 0,
                        )
                    }
                }
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
                    Text(
                        text = iCalObject.description?.trim() ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (iCalObject.module == Module.TODO.name) {
                    Checkbox(
                        checked = iCalObject.percent == 100,
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
                Text(
                    text = iCalObject.description?.trim() ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                subtasks.forEach { subtask ->

                    SubtaskCardCompact(
                        subtask = subtask,
                        selected = selected.contains(subtask.id),
                        onProgressChanged = onProgressChanged,
                        modifier = Modifier
                            .clip(jtxCardCornerShape)
                            .combinedClickable(
                                onClick = { onClick(subtask.id, subtasks) },
                                onLongClick = {
                                    if (!subtask.isReadOnly)
                                        onLongClick(subtask.id, subtasks)
                                }
                            )
                    )

                    if (subtask.id != subtasks.last().id)
                        Divider(
                            thickness = 1.dp,
                            modifier = Modifier.alpha(0.25f)
                        )
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
            emptyList(),
            progressUpdateDisabled = true,
            selected = emptyList(),
            onProgressChanged = { _, _ -> },
            onClick = { _, _ -> },
            onLongClick = { _, _ -> }
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
            emptyList(),
            progressUpdateDisabled = true,
            selected = emptyList(),
            onProgressChanged = { _, _ -> },
            onClick = { _, _ -> },
            onLongClick = { _, _ -> }
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
            emptyList(),
            progressUpdateDisabled = true,
            selected = emptyList(),
            onProgressChanged = { _, _ -> },
            onClick = { _, _ -> },
            onLongClick = { _, _ -> }
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
            subtasks = listOf(icalobject, icalobject),
            progressUpdateDisabled = true,
            selected = emptyList(),
            onProgressChanged = { _, _ -> },
            onClick = { _, _ -> },
            onLongClick = { _, _ -> }
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
            subtasks = listOf(icalobject, icalobject),
            progressUpdateDisabled = true,
            selected = emptyList(),
            onProgressChanged = { _, _ -> },
            onClick = { _, _ -> },
            onLongClick = { _, _ -> }
        )
    }
}
