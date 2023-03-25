/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import at.techbee.jtx.ui.theme.Typography
import at.techbee.jtx.ui.theme.jtxCardCornerShape
import at.techbee.jtx.util.DateTimeUtils


@OptIn(ExperimentalFoundationApi::class)
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

    val statusBarVisible by remember {
        mutableStateOf(
            iCalObject.numAttendees > 0
                    || iCalObject.numAttachments > 0
                    || iCalObject.numComments > 0
                    || iCalObject.numResources > 0
                    || iCalObject.numAlarms > 0 || iCalObject.numSubtasks > 0
                    || iCalObject.numSubnotes > 0
                    || iCalObject.isReadOnly
                    || iCalObject.uploadPending
                    || iCalObject.url?.isNotEmpty() == true
                    || iCalObject.location?.isNotEmpty() == true
                    || iCalObject.contact?.isNotEmpty() == true
                    || iCalObject.rrule != null || iCalObject.recurid != null
                    || iCalObject.priority in 1..9
                    || iCalObject.status in listOf(Status.CANCELLED.status, Status.DRAFT.status, Status.CANCELLED.status)
                    || iCalObject.classification in listOf(Classification.CONFIDENTIAL.classification, Classification.PRIVATE.classification)
        )
    }
    val color =
        iCalObject.colorItem?.let { Color(it) } ?: iCalObject.colorCollection?.let { Color(it) }
        ?: Color.Transparent

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .background(if(selected.contains(iCalObject.id)) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
    ) {

        Box(
            modifier = Modifier
                .width(10.dp)
                .alpha(0.5f)
                .fillMaxHeight()
                .background(color, RoundedCornerShape(8.dp))
        )

        Column(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .fillMaxWidth()
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {

                    if (iCalObject.categories?.isNotEmpty() == true
                        || (iCalObject.module == Module.TODO.name && iCalObject.due != null)
                        || (iCalObject.module == Module.JOURNAL.name && iCalObject.dtstart != null)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (iCalObject.categories?.isNotBlank() == true) {
                                Text(
                                    iCalObject.categories ?: "",
                                    style = Typography.labelMedium,
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))  // make sure dtstart/due move to the right
                            }
                            if (iCalObject.module == Module.JOURNAL.name && iCalObject.dtstart != null) {
                                Text(
                                    DateTimeUtils.convertLongToShortDateTimeString(
                                        iCalObject.dtstart,
                                        iCalObject.dtstartTimezone
                                    ),
                                    style = Typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (iCalObject.module == Module.TODO.name && iCalObject.due != null) {
                                Text(
                                    ICalObject.getDueTextInfo(status = iCalObject.status, due = iCalObject.due, dueTimezone = iCalObject.dueTimezone, percent = iCalObject.percent, context = LocalContext.current),
                                    style = Typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    color = if(ICalObject.isOverdue(iCalObject.status, iCalObject.percent, iCalObject.due, iCalObject.dueTimezone) == true) MaterialTheme.colorScheme.error else LocalContentColor.current,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {

                        if (iCalObject.summary?.isNotBlank() == true)
                            Text(
                                text = iCalObject.summary?.trim() ?: "",
                                textDecoration = if (iCalObject.status == Status.CANCELLED.status) TextDecoration.LineThrough else TextDecoration.None,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                    }

                    if (iCalObject.description?.isNotBlank() == true)
                        Text(
                            text = iCalObject.description?.trim() ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                }


                if (iCalObject.module == Module.TODO.name)
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

            AnimatedVisibility(visible = statusBarVisible) {
                ListStatusBar(
                    numAttendees = iCalObject.numAttendees,
                    numAttachments = iCalObject.numAttachments,
                    numComments = iCalObject.numComments,
                    numResources = iCalObject.numResources,
                    numAlarms = iCalObject.numAlarms,
                    numSubtasks = iCalObject.numSubtasks,
                    numSubnotes = iCalObject.numSubnotes,
                    isReadOnly = iCalObject.isReadOnly,
                    uploadPending = iCalObject.uploadPending,
                    hasURL = iCalObject.url?.isNotBlank() == true,
                    hasLocation = iCalObject.location?.isNotBlank() == true,
                    hasContact = iCalObject.contact?.isNotBlank() == true,
                    isRecurring = iCalObject.rrule != null || iCalObject.recurid != null,
                    isRecurringModified = iCalObject.recurid != null && iCalObject.sequence > 0,
                    status = iCalObject.status,
                    classification = iCalObject.classification,
                    priority = iCalObject.priority,
                    modifier = Modifier.padding(top = 4.dp)

                )
            }

            Column(modifier = Modifier.padding(top = 4.dp)) {
                subtasks.forEach { subtask ->

                    SubtaskCardCompact(
                        subtask = subtask,
                        selected = selected.contains(subtask.id),
                        onProgressChanged = onProgressChanged,
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp)
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
            due = System.currentTimeMillis()-1
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
