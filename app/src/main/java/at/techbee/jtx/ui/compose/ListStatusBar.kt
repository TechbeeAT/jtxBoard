/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
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
    numAttendees: Int,
    numAttachments: Int,
    numComments: Int,
    numResources: Int,
    numAlarms: Int,
    isReadOnly: Boolean,
    uploadPending: Boolean,
    isRecurringOriginal: Boolean,
    isRecurringInstance: Boolean,
    isLinkedRecurringInstance: Boolean,
    hasURL: Boolean,
    hasLocation: Boolean,
    hasContact: Boolean,
    component: String,
    status: String?,
    classification: String?,
    priority: Int?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
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
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        classificationText?.let {
            IconWithText(
                icon = Icons.Outlined.AdminPanelSettings,
                iconDesc = stringResource(R.string.classification),
                text = it,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        priorityText?.let {
            IconWithText(
                icon = Icons.Outlined.WorkOutline,
                iconDesc = stringResource(R.string.priority),
                text = it,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        if (numAttendees > 0)
            IconWithText(
                icon = Icons.Outlined.Group,
                iconDesc = stringResource(R.string.attendees),
                text = numAttendees.toString(),
                modifier = Modifier.padding(start = 4.dp)
            )
        if (numAttachments > 0)
            IconWithText(
                icon = Icons.Outlined.Attachment,
                iconDesc = stringResource(R.string.attachments),
                text = numAttachments.toString(),
                modifier = Modifier.padding(start = 4.dp)
            )
        if (numComments > 0)
            IconWithText(
                icon = Icons.Outlined.Comment,
                iconDesc = stringResource(R.string.comments),
                text = numComments.toString(),
                modifier = Modifier.padding(start = 4.dp)
            )
        if (numResources > 0)
            IconWithText(
                icon = Icons.Outlined.WorkOutline,
                iconDesc = stringResource(R.string.resources),
                text = numResources.toString(),
                modifier = Modifier.padding(start = 4.dp)
            )
        if (numAlarms > 0)
            IconWithText(
                icon = Icons.Outlined.Alarm,
                iconDesc = stringResource(R.string.alarms),
                text = numAlarms.toString(),
                modifier = Modifier.padding(start = 4.dp)
            )

        if (hasURL)
            Icon(
                Icons.Outlined.Link,
                stringResource(R.string.url),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(14.dp)
            )
        if (hasLocation)
            Icon(
                Icons.Outlined.PinDrop,
                stringResource(R.string.location),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(14.dp)
            )

        if (hasContact)
            Icon(
                Icons.Outlined.ContactMail,
                stringResource(R.string.contact),
                modifier = Modifier
                    .padding(start = 4.dp, end = 2.dp)
                    .size(14.dp)
            )

        if (isReadOnly)
            Icon(
                painter = painterResource(id = R.drawable.ic_readonly),
                contentDescription = stringResource(id = R.string.readyonly),
                modifier = Modifier.size(14.dp)
            )
        if (uploadPending)
            Icon(
                Icons.Outlined.CloudSync,
                stringResource(R.string.upload_pending),
                modifier = Modifier.size(14.dp)
            )

        if (isRecurringOriginal || (isRecurringInstance && isLinkedRecurringInstance))
            Icon(
                Icons.Outlined.EventRepeat,
                stringResource(R.string.list_item_recurring),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(14.dp)
            )
        if (isRecurringInstance && !isLinkedRecurringInstance)
            Icon(
                painter = painterResource(R.drawable.ic_recur_exception),
                stringResource(R.string.list_item_recurring),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(14.dp)
            )
    }
}


@Preview(showBackground = true)
@Composable
fun ListStatusBar_Preview1() {
    JtxBoardTheme {
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
    JtxBoardTheme {
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
    JtxBoardTheme {
        ListStatusBar(
            numAttendees = 0,
            numAttachments = 0,
            numComments = 0,
            numResources = 0,
            numAlarms = 0,
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