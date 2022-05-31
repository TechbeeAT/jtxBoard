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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.IcalListFragmentDirections
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.ui.theme.Typography
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin


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
    onEditRequest: (iCalObjectId: Long) -> Unit,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit
) {

    val iCalObject = iCalObjectWithRelatedto.property
    var markwon = Markwon.builder(LocalContext.current)
        .usePlugin(StrikethroughPlugin.create())
        .build()

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
                    if (!iCalObject.isReadOnly && BillingManager.getInstance()?.isProPurchased?.value == true)
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
                            iCalObject.collectionDisplayName ?: iCalObject.accountName ?: "",
                            style = Typography.titleSmall
                        )
                        Text(
                            iCalObject.categories ?: "", modifier = Modifier.padding(start = 8.dp),
                            style = Typography.titleSmall,
                            fontStyle = FontStyle.Italic
                        )
                    }
                    Row(modifier = Modifier.padding(end = 8.dp, top = 8.dp)) {
                        if (iCalObject.isReadOnly)
                            Icon(
                                painter = painterResource(id = R.drawable.ic_readonly),
                                contentDescription = stringResource(id = R.string.readyonly)
                            )
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

                if (iCalObject.module == Module.TODO.name && (iCalObject.dtstart != null || iCalObject.due != null)) {
                    Row( modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        iCalObject.dtstart?.let {
                            Text(
                                iCalObject.getDtstartTextInfo(LocalContext.current)?:"",
                                style = Typography.titleSmall,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        }
                        iCalObject.due?.let {
                            Text(
                                iCalObject.getDueTextInfo(LocalContext.current)?:"",
                                style = Typography.titleSmall
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    if (iCalObject.module == Module.JOURNAL.name)
                        VerticalDateBlock(
                            iCalObject.dtstart ?: System.currentTimeMillis(),
                            iCalObject.dtstartTimezone
                        )

                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .weight(1f)

                    ) {

                        val summarySize = if (iCalObject.module == Module.JOURNAL.name) 18.sp else Typography.bodyMedium.fontSize
                        val summaryTextDecoration = if(iCalObject.status == StatusJournal.CANCELLED.name || iCalObject.status == StatusTodo.CANCELLED.name) TextDecoration.LineThrough else TextDecoration.None

                        iCalObject.summary?.let {
                            Text(
                                text = it,
                                fontWeight = FontWeight.Bold,
                                fontSize = summarySize,
                                textDecoration = summaryTextDecoration
                            )
                        }
                        iCalObject.description?.let {
                            Text(
                                text = it,
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (iCalObject.module == Module.TODO.name && !settingShowProgressMaintasks)
                        Checkbox(
                            checked = iCalObject.percent == 100,
                            enabled = !iCalObject.isReadOnly,
                            onCheckedChange = {
                                onProgressChanged(
                                    iCalObject.id,
                                    if (it) 100 else 0,
                                    iCalObject.isLinkedRecurringInstance
                                )
                            })
                }

                if (settingShowAttachments && (iCalObjectWithRelatedto.attachment?.size ?: 0) > 0) {
                    LazyRow(
                        content = {
                            items(iCalObjectWithRelatedto.attachment ?: emptyList()) { attachment ->
                                AttachmentCard(attachment)
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }


                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    StatusClassificationPriorityBlock(
                        component = iCalObject.component,
                        status = iCalObject.status,
                        classification = iCalObject.classification,
                        priority = iCalObject.priority,
                        modifier = Modifier.padding(8.dp)
                    )

                    if (iCalObject.numAttendees > 0 || iCalObject.numAttachments > 0 || iCalObject.numComments > 0)
                        Row(modifier = Modifier.padding(8.dp)) {

                            if (iCalObject.numAttendees > 0)
                                IconWithText(
                                    icon = Icons.Outlined.Group,
                                    iconDesc = stringResource(R.string.attendees),
                                    text = iCalObject.numAttendees.toString(),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            if (iCalObject.numAttachments > 0)
                                IconWithText(
                                    icon = Icons.Outlined.Attachment,
                                    iconDesc = stringResource(R.string.attachments),
                                    text = iCalObject.numAttachments.toString(),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            if (iCalObject.numComments > 0)
                                IconWithText(
                                    icon = Icons.Outlined.Comment,
                                    iconDesc = stringResource(R.string.comments),
                                    text = iCalObject.numComments.toString()
                                )
                        }
                }

                if (iCalObject.component == Component.VTODO.name && settingShowProgressMaintasks)
                    ProgressElement(
                        iCalObjectId = iCalObject.id,
                        progress = iCalObject.percent,
                        isReadOnly = iCalObject.isReadOnly,
                        isLinkedRecurringInstance = iCalObject.isLinkedRecurringInstance,
                        onProgressChanged = onProgressChanged
                    )


                if (settingShowSubtasks && (subtasks.size) > 0) {
                    Column {
                        subtasks.forEach { subtask ->
                            SubtaskCard(
                                subtask = subtask,
                                navController = navController,
                                showProgress = settingShowProgressSubtasks,
                                onEditRequest = onEditRequest,
                                onProgressChanged = onProgressChanged
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
            onEditRequest = { },
            onProgressChanged = { _, _, _ -> }
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
            property.status = StatusJournal.CANCELLED.name
        }
        ICalObjectListCard(
            icalobject,
            listOf(ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            }),
            rememberNavController(),
            onEditRequest = { },
            onProgressChanged = { _, _, _ -> }
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
            property.dtstart = System.currentTimeMillis()
            property.due = System.currentTimeMillis()
        }
        ICalObjectListCard(
            icalobject,
            listOf(ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            }),
            rememberNavController(),
            onEditRequest = { },
            settingShowProgressMaintasks = true,
            onProgressChanged = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ICalObjectListCardPreview_TODO_no_progress() {
    JtxBoardTheme {

        val icalobject = ICal4ListWithRelatedto.getSample().apply {
            property.component = Component.VTODO.name
            property.module = Module.TODO.name
            property.percent = 89
            property.status = StatusTodo.`IN-PROCESS`.name
            property.classification = Classification.CONFIDENTIAL.name
            property.uploadPending = false
            property.isRecurringInstance = false
            property.isRecurringOriginal = false
            property.isReadOnly = true
            property.dtstart = null
            property.due = null
        }
        ICalObjectListCard(
            icalobject,
            listOf(ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            }),
            rememberNavController(),
            onEditRequest = { },
            settingShowProgressMaintasks = false,
            onProgressChanged = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ICalObjectListCardPreview_TODO_recur_exception() {
    JtxBoardTheme {

        val icalobject = ICal4ListWithRelatedto.getSample().apply {
            property.component = Component.VTODO.name
            property.module = Module.TODO.name
            property.percent = 89
            property.status = StatusTodo.`IN-PROCESS`.name
            property.classification = Classification.CONFIDENTIAL.name
            property.uploadPending = false
            property.isRecurringInstance = true
            property.isLinkedRecurringInstance = false
            property.isRecurringOriginal = false
            property.isReadOnly = true
        }
        ICalObjectListCard(
            icalobject,
            listOf(ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            }),
            rememberNavController(),
            onEditRequest = { },
            settingShowProgressMaintasks = false,
            onProgressChanged = { _, _, _ -> }
        )
    }
}

@Composable
fun StatusClassificationPriorityBlock(
    component: String,
    status: String?,
    classification: String?,
    priority: Int?,
    modifier: Modifier = Modifier
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
    val priorityText = if (priority in 0..9) priorityArray[priority!!] else null

    Row(modifier = modifier) {

        statusText?.let {
            IconWithText(
                icon = Icons.Outlined.PublishedWithChanges,
                iconDesc = stringResource(R.string.status),
                text = it,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        classificationText?.let {
            IconWithText(
                icon = Icons.Outlined.AdminPanelSettings,
                iconDesc = stringResource(R.string.classification),
                text = it,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        priorityText?.let {
            IconWithText(
                icon = Icons.Outlined.WorkOutline,
                iconDesc = stringResource(R.string.priority),
                text = it,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatusClassificationPriorityBlock_Preview_nothingDisplayed() {
    JtxBoardTheme {
        StatusClassificationPriorityBlock(
            component = Component.VJOURNAL.name,
            status = StatusJournal.FINAL.name,
            classification = Classification.PUBLIC.name,
            priority = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatusClassificationPriorityBlock_Preview_bothDisplayed() {
    JtxBoardTheme {
        StatusClassificationPriorityBlock(
            component = Component.VJOURNAL.name,
            status = StatusJournal.DRAFT.name,
            classification = Classification.CONFIDENTIAL.name,
            priority = null
        )
    }
}


@Preview(showBackground = true)
@Composable
fun StatusClassificationPriorityBlock_Preview_w_prio() {
    JtxBoardTheme {
        StatusClassificationPriorityBlock(
            component = Component.VJOURNAL.name,
            status = StatusJournal.DRAFT.name,
            classification = Classification.CONFIDENTIAL.name,
            priority = 2
        )
    }
}