/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.relations.ICal4ListWithRelatedto
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.IcalListFragmentDirections
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.ui.theme.Typography


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ICalObjectListCard(
    iCalObjectWithRelatedto: ICal4ListWithRelatedto,
    subtasks: List<ICal4List>,
    navController: NavController,
    settingShowSubtasks: Boolean = true,
    settingShowAttachments: Boolean = true,
    settingShowProgressMaintasks: Boolean = false,
    settingShowProgressSubtasks: Boolean = true,
    onEditRequest: (Long) -> Unit
) {

    val iCalObject = iCalObjectWithRelatedto.property

    ElevatedCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .combinedClickable(
                onClick = {
                    navController.navigate(
                        IcalListFragmentDirections
                            .actionIcalListFragmentToIcalViewFragment()
                            .setItem2show(iCalObject.id)
                    )
                },
                onLongClick = {
                    onEditRequest(iCalObject.id)
                }
            )
    ) {


        Box {

            ColoredEdge(iCalObject.colorItem, iCalObject.colorCollection)

            Column {

                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
                        Text(
                            iCalObject.collectionDisplayName?:iCalObject.accountName?:"",
                            style = Typography.titleSmall
                        )
                        Text(
                            iCalObject.categories?:"", modifier = Modifier.padding(start = 8.dp),
                            style = Typography.titleSmall,
                            fontStyle = FontStyle.Italic
                        )
                    }
                    Row(modifier = Modifier.padding(end = 8.dp, top = 8.dp)) {
                        if (iCalObject.uploadPending)
                            Icon(Icons.Outlined.CloudSync, stringResource(R.string.upload_pending))
                        if (iCalObject.isRecurringOriginal || (iCalObject.isRecurringInstance && iCalObject.isLinkedRecurringInstance))
                            Icon(
                                Icons.Default.EventRepeat,
                                stringResource(R.string.list_item_recurring),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        if (iCalObject.isRecurringInstance && !iCalObject.isLinkedRecurringInstance)
                            Icon(
                                painter = painterResource(R.drawable.ic_recur_exception),
                                stringResource(R.string.list_item_recurring),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                    }
                }

                Row(verticalAlignment = Alignment.Top) {

                    if(iCalObject.module == Module.JOURNAL.name)
                        VerticalDateBlock(
                            iCalObject.dtstart ?: System.currentTimeMillis(),
                            iCalObject.dtstartTimezone
                        )

                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {

                        val summarySize = if(iCalObject.module == Module.JOURNAL.name) 18.sp else Typography.bodyMedium.fontSize

                        iCalObject.summary?.let {
                            Text(
                                text = it,
                                fontWeight = FontWeight.Bold,
                                fontSize = summarySize
                            )
                        }
                        iCalObject.description?.let {
                            Text(text = it,
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                if(settingShowAttachments && (iCalObjectWithRelatedto.attachment?.size ?: 0) > 0) {
                    LazyRow(
                        content = {
                        items(iCalObjectWithRelatedto.attachment?: emptyList()) { attachment ->
                            AttachmentCard(attachment)
                        }
                    },
                        modifier = Modifier.padding(end = 8.dp))
                }


                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    StatusClassificationBlock(
                        component = iCalObject.component,
                        status = iCalObject.status,
                        classification = iCalObject.classification,
                        modifier = Modifier.padding(8.dp)
                    )

                    if (iCalObject.numAttendees > 0 || iCalObject.numAttachments > 0 || iCalObject.numComments > 0)
                        Row(modifier = Modifier.padding(8.dp)) {

                            if(iCalObject.numAttendees > 0)
                                IconWithText(
                                    icon = Icons.Outlined.Group,
                                    iconDesc = stringResource(R.string.attendees),
                                    text = iCalObject.numAttendees.toString(),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            if(iCalObject.numAttachments > 0)
                                IconWithText(
                                    icon = Icons.Outlined.Attachment,
                                    iconDesc = stringResource(R.string.attachments),
                                    text = iCalObject.numAttachments.toString(),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            if(iCalObject.numComments > 0)
                                IconWithText(
                                    icon = Icons.Outlined.Comment,
                                    iconDesc = stringResource(R.string.comments),
                                    text = iCalObject.numComments.toString()
                                )
                        }
                    }

                if(iCalObject.component == Component.VTODO.name)
                    ProgressElement(iCalObject.percent)


                if(settingShowSubtasks && (subtasks.size) > 0) {
                    Column {
                        subtasks.forEach { subtask ->
                            SubtaskCard(
                                subtask = subtask,
                                navController = navController,
                                onEditRequest = onEditRequest
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ICalObjectListCardPreview_JOURNAL() {
    JtxBoardTheme {

        val icalobject = ICal4ListWithRelatedto.getSample()
        ICalObjectListCard(
            icalobject,
            listOf(ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            }),
            rememberNavController(),
            onEditRequest = {  }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ICalObjectListCardPreview_NOTE() {
    JtxBoardTheme {

        val icalobject = ICal4ListWithRelatedto.getSample().apply {
            property.component = Component.VJOURNAL.name
            property.module = Module.NOTE.name
            property.dtstart = null
            property.dtstartTimezone = null
        }
        ICalObjectListCard(
            icalobject,
            listOf(ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            }),
            rememberNavController(),
            onEditRequest = {  }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ICalObjectListCardPreview_TODO() {
    JtxBoardTheme {

        val icalobject = ICal4ListWithRelatedto.getSample().apply {
            property.component = Component.VTODO.name
            property.module = Module.TODO.name
            property.percent = 89
            property.status = StatusTodo.`IN-PROCESS`.name
            property.classification = Classification.CONFIDENTIAL.name
        }
        ICalObjectListCard(
            icalobject,
            listOf(ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            }),
            rememberNavController(),
            onEditRequest = {  }
        )
    }
}

@Composable
fun StatusClassificationBlock(
    component: String,
    status: String?,
    classification: String?,
    modifier: Modifier = Modifier
) {

    val statusText: String? = when {
        //component == Component.VTODO.name && status == StatusTodo.`NEEDS-ACTION`.name -> stringResource(id = R.string.todo_status_needsaction)
        //component == Component.VTODO.name && status == StatusTodo.`IN-PROCESS`.name -> stringResource(id = R.string.todo_status_inprocess)
        component == Component.VTODO.name && status == StatusTodo.CANCELLED.name -> stringResource(
            id = R.string.todo_status_cancelled
        )
        //component == Component.VTODO.name && status == StatusTodo.COMPLETED.name -> stringResource(id = R.string.todo_status_completed)

        component == Component.VJOURNAL.name && status == StatusJournal.DRAFT.name -> stringResource(
            id = R.string.journal_status_draft
        )
        //component == Component.VJOURNAL.name && status == StatusJournal.FINAL.name -> stringResource(id = R.string.journal_status_final)
        component == Component.VJOURNAL.name && status == StatusJournal.CANCELLED.name -> stringResource(
            id = R.string.journal_status_cancelled
        )
        else -> null
    }
    val classificationText: String? = when (classification) {
        Classification.PRIVATE.name -> stringResource(id = R.string.classification_private)
        Classification.CONFIDENTIAL.name -> stringResource(id = R.string.classification_confidential)
        //Classification.PUBLIC.name -> stringResource(id = R.string.classification_public)
        else -> null
    }

    Row(modifier = modifier) {

        statusText?.let {
            if(it != StatusJournal.FINAL.name && it != StatusTodo.`NEEDS-ACTION`.name && it != StatusTodo.`IN-PROCESS`.name && it != StatusTodo.COMPLETED.name)
                IconWithText(
                    icon = Icons.Outlined.PublishedWithChanges,
                    iconDesc = stringResource(R.string.status),
                    text = it,
                    modifier = Modifier.padding(end = 8.dp)
                )
        }
        classificationText?.let {
            if(it != Classification.PUBLIC.name)
                IconWithText(
                    icon = Icons.Outlined.AdminPanelSettings,
                    iconDesc = stringResource(R.string.classification),
                    text = it,
                    modifier = Modifier.padding(end = 8.dp)
                )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatusClassificationBlock_Preview_nothingDisplayed() {
    JtxBoardTheme {
        StatusClassificationBlock(
            component = Component.VJOURNAL.name,
            status = StatusJournal.FINAL.name,
            classification = Classification.PUBLIC.name
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatusClassificationBlock_Preview_bothDisplayed() {
    JtxBoardTheme {
        StatusClassificationBlock(
            component = Component.VJOURNAL.name,
            status = StatusJournal.DRAFT.name,
            classification = Classification.CONFIDENTIAL.name
        )
    }
}