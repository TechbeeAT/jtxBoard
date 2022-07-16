/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.StatusJournal
import at.techbee.jtx.database.StatusTodo
import at.techbee.jtx.ui.theme.JtxBoardTheme

@Composable
fun ListStatusBar(
    numAttendees: Int? = null,
    numAttachments: Int? = null,
    numComments: Int? = null,
    numResources: Int? = null,
    numAlarms: Int? = null,
    isReadOnly: Boolean? = null,
    uploadPending: Boolean? = null,
    isRecurringOriginal: Boolean? = null,
    isRecurringInstance: Boolean? = null,
    isLinkedRecurringInstance: Boolean? = null,
    hasURL: Boolean? = null,
    hasLocation: Boolean? = null,
    hasContact: Boolean? = null,
    component: String,
    status: String? = null,
    classification: String? = null,
    priority: Int? = null,
    numSubtasks: Int? = null,
    numSubnotes: Int? = null,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
        ) {
        val statusText: String? = when {
            component == Component.VTODO.name && status == StatusTodo.CANCELLED.name -> stringResource(
                id = R.string.todo_status_cancelled
            )
            component == Component.VJOURNAL.name && status == StatusJournal.DRAFT.name -> stringResource(
                id = R.string.journal_status_draft
            )
            component == Component.VJOURNAL.name && status == StatusJournal.CANCELLED.name -> stringResource(
                id = R.string.journal_status_cancelled
            )
            else -> null
        }
        val classificationText: String? = when (classification) {
            Classification.PRIVATE.name -> stringResource(id = R.string.classification_private)
            Classification.CONFIDENTIAL.name -> stringResource(id = R.string.classification_confidential)
            else -> null
        }

        val priorityArray = stringArrayResource(id = R.array.priority)
        val priorityText = if (priority in 1..9) priorityArray[priority!!] else null



        statusText?.let {
            IconWithText(
                icon = Icons.Outlined.PublishedWithChanges,
                iconDesc = stringResource(R.string.status),
                text = it,
            )
        }
        classificationText?.let {
            IconWithText(
                icon = Icons.Outlined.AdminPanelSettings,
                iconDesc = stringResource(R.string.classification),
                text = it,
            )
        }
        priorityText?.let {
            IconWithText(
                icon = Icons.Outlined.WorkOutline,
                iconDesc = stringResource(R.string.priority),
                text = it,
            )
        }

        if (numAttendees != null && numAttendees > 0)
            IconWithText(
                icon = Icons.Outlined.Group,
                iconDesc = stringResource(R.string.attendees),
                text = numAttendees.toString(),
            )
        if (numAttachments != null && numAttachments > 0)
            IconWithText(
                icon = Icons.Outlined.Attachment,
                iconDesc = stringResource(R.string.attachments),
                text = numAttachments.toString(),
            )
        if (numComments != null && numComments > 0)
            IconWithText(
                icon = Icons.Outlined.Comment,
                iconDesc = stringResource(R.string.comments),
                text = numComments.toString(),
            )
        if (numResources != null && numResources > 0)
            IconWithText(
                icon = Icons.Outlined.WorkOutline,
                iconDesc = stringResource(R.string.resources),
                text = numResources.toString(),
            )
        if (numAlarms != null && numAlarms > 0)
            IconWithText(
                icon = Icons.Outlined.Alarm,
                iconDesc = stringResource(R.string.alarms),
                text = numAlarms.toString(),
            )

        if (hasURL == true)
            Icon(
                Icons.Outlined.Link,
                stringResource(R.string.url),
                modifier = Modifier
                    .size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        if (hasLocation == true)
            Icon(
                Icons.Outlined.PinDrop,
                stringResource(R.string.location),
                modifier = Modifier
                    .size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

        if (hasContact == true)
            Icon(
                Icons.Outlined.ContactMail,
                stringResource(R.string.contact),
                modifier = Modifier
                    .padding(end = 2.dp)
                    .size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

        if (numSubtasks != null && numSubtasks > 0)
            IconWithText(
                icon = Icons.Outlined.TaskAlt,
                iconDesc = stringResource(R.string.subtasks),
                text = numSubtasks.toString(),
            )

        if (numSubnotes != null && numSubnotes > 0)
            IconWithText(
                icon = Icons.Outlined.Note,
                iconDesc = stringResource(R.string.note),
                text = numSubnotes.toString(),
            )

        if (isReadOnly == true)
            Icon(
                painter = painterResource(id = R.drawable.ic_readonly),
                contentDescription = stringResource(id = R.string.readyonly),
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        if (uploadPending == true)
            Icon(
                Icons.Outlined.CloudSync,
                stringResource(R.string.upload_pending),
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

        if (isRecurringOriginal == true || (isRecurringInstance == true && isLinkedRecurringInstance == true))
            Icon(
                Icons.Outlined.EventRepeat,
                stringResource(R.string.list_item_recurring),
                modifier = Modifier
                    .size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        if (isRecurringInstance == true && isLinkedRecurringInstance == false)
            Icon(
                painter = painterResource(R.drawable.ic_recur_exception),
                stringResource(R.string.list_item_recurring),
                modifier = Modifier
                    .size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
    }
}


@Preview(showBackground = true)
@Composable
fun ListStatusBar_Preview1() {
    MaterialTheme {
        ListStatusBar(
            numAttendees = 3,
            numAttachments = 4,
            numComments = 11,
            numResources = 3729,
            numAlarms = 8,
            isReadOnly = false,
            uploadPending = true,
            hasLocation = true,
            hasURL = true,
            hasContact = true,
            isRecurringOriginal = false,
            isRecurringInstance = false,
            isLinkedRecurringInstance = false,
            component = Component.VJOURNAL.name,
            status = StatusJournal.FINAL.name,
            classification = Classification.PUBLIC.name,
            priority = null
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ListStatusBar_Preview2() {
    MaterialTheme {
        ListStatusBar(
            numAttendees = 155,
            numAttachments = 0,
            numComments = 2345,
            numResources = 88,
            numAlarms = 2,
            isReadOnly = true,
            uploadPending = false,
            hasLocation = true,
            hasContact = true,
            hasURL = true,
            isRecurringOriginal = true,
            isRecurringInstance = false,
            isLinkedRecurringInstance = false,
            component = Component.VJOURNAL.name,
            status = StatusJournal.DRAFT.name,
            classification = Classification.CONFIDENTIAL.name,
            priority = null
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ListStatusBar_Preview3() {
    MaterialTheme {
        ListStatusBar(
            numAttendees = 0,
            numAttachments = 0,
            numComments = 0,
            numResources = 0,
            numAlarms = 0,
            numSubtasks = 44,
            numSubnotes = 23,
            isReadOnly = false,
            uploadPending = true,
            hasLocation = false,
            hasURL = false,
            hasContact = false,
            isRecurringOriginal = false,
            isRecurringInstance = true,
            isLinkedRecurringInstance = false,
            component = Component.VJOURNAL.name,
            status = StatusJournal.DRAFT.name,
            classification = Classification.CONFIDENTIAL.name,
            priority = 2
        )
    }
}