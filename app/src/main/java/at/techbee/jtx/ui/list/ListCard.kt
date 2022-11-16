/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.reusable.cards.AttachmentCard
import at.techbee.jtx.ui.reusable.cards.SubnoteCard
import at.techbee.jtx.ui.reusable.cards.SubtaskCard
import at.techbee.jtx.ui.reusable.elements.ColoredEdge
import at.techbee.jtx.ui.reusable.elements.ListStatusBar
import at.techbee.jtx.ui.reusable.elements.ProgressElement
import at.techbee.jtx.ui.reusable.elements.VerticalDateBlock
import at.techbee.jtx.ui.theme.Typography
import at.techbee.jtx.ui.theme.jtxCardCornerShape
import com.google.accompanist.flowlayout.FlowRow


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ICalObjectListCard(
    iCalObject: ICal4List,
    subtasks: List<ICal4List>,
    subnotes: List<ICal4List>,
    attachments: List<Attachment>,
    modifier: Modifier = Modifier,
    player: MediaPlayer?,
    isSubtasksExpandedDefault: Boolean = true,
    isSubnotesExpandedDefault: Boolean = true,
    isAttachmentsExpandedDefault: Boolean = true,
    settingShowProgressMaintasks: Boolean = false,
    settingShowProgressSubtasks: Boolean = true,
    progressIncrement: Int,
    goToDetail: (itemId: Long, editMode: Boolean, list: List<ICal4List>) -> Unit,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit,
    onExpandedChanged: (itemId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isAttachmentsExpanded: Boolean) -> Unit
) {

    var isSubtasksExpanded by remember {
        mutableStateOf(
            iCalObject.isSubtasksExpanded ?: isSubtasksExpandedDefault
        )
    }
    var isSubnotesExpanded by remember {
        mutableStateOf(
            iCalObject.isSubnotesExpanded ?: isSubnotesExpandedDefault
        )
    }
    var isAttachmentsExpanded by remember {
        mutableStateOf(
            iCalObject.isAttachmentsExpanded ?: isAttachmentsExpandedDefault
        )
    }


    ElevatedCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {

        Box {

            ColoredEdge(iCalObject.colorItem, iCalObject.colorCollection)

            Column {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 8.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    FlowRow(modifier = Modifier.weight(1f)) {
                        Text(
                            iCalObject.collectionDisplayName ?: iCalObject.accountName ?: "",
                            style = Typography.labelMedium,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 16.dp).weight(0.2f),
                        )

                        iCalObject.categories?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.labelMedium,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .weight(0.2f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (iCalObject.module == Module.TODO.name) {
                            iCalObject.dtstart?.let {
                                Text(
                                    iCalObject.getDtstartTextInfo(LocalContext.current),
                                    style = Typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier.padding(end = 16.dp).weight(0.2f),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            iCalObject.due?.let {
                                Text(
                                    iCalObject.getDueTextInfo(LocalContext.current),
                                    style = Typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    color = if(iCalObject.isOverdue() == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 16.dp).weight(0.2f),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    ListStatusBar(
                        isReadOnly = iCalObject.isReadOnly,
                        uploadPending = iCalObject.uploadPending,
                        isRecurringOriginal = iCalObject.isRecurringOriginal,
                        isRecurringInstance = iCalObject.isRecurringInstance,
                        isLinkedRecurringInstance = iCalObject.isLinkedRecurringInstance,
                        component = iCalObject.component,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {

                    if (iCalObject.module == Module.JOURNAL.name)
                        VerticalDateBlock(
                            iCalObject.dtstart,
                            iCalObject.dtstartTimezone,
                            modifier = Modifier.padding(
                                start = 8.dp,
                                end = 4.dp,
                                bottom = 4.dp,
                                top = 4.dp
                            )
                        )

                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp)
                            .weight(1f)

                    ) {

                        val summarySize =
                            if (iCalObject.module == Module.JOURNAL.name) 18.sp else Typography.bodyMedium.fontSize
                        val summaryTextDecoration =
                            if (iCalObject.status == StatusJournal.CANCELLED.name || iCalObject.status == StatusTodo.CANCELLED.name) TextDecoration.LineThrough else TextDecoration.None

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (iCalObject.summary?.isNotBlank() == true)
                                Text(
                                    text = iCalObject.summary?.trim() ?: "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = summarySize,
                                    textDecoration = summaryTextDecoration,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .weight(1f),
                                    style = TextStyle(textDirection = TextDirection.Content)
                                )

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

                        if (iCalObject.description?.isNotBlank() == true)
                            Text(
                                text = iCalObject.description?.trim() ?: "",
                                maxLines = 6,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(),
                                style = TextStyle(textDirection = TextDirection.Content)
                            )

                        if (iCalObject.numAttendees > 0 || iCalObject.numAttachments > 0
                            || iCalObject.numComments > 0 || iCalObject.numResources > 0
                            || iCalObject.numAlarms > 0 || iCalObject.contact?.isNotEmpty() == true
                            || iCalObject.url?.isNotEmpty() == true || iCalObject.location?.isNotEmpty() == true
                            || iCalObject.priority in 1..9 || iCalObject.status in listOf(
                                StatusJournal.CANCELLED.name,
                                StatusJournal.DRAFT.name,
                                StatusTodo.CANCELLED.name
                            )
                            || iCalObject.classification in listOf(
                                Classification.CONFIDENTIAL.name,
                                Classification.PRIVATE.name
                            )
                        )
                            ListStatusBar(
                                numAttendees = iCalObject.numAttendees,
                                //numAttachments = iCalObject.numAttachments,
                                numComments = iCalObject.numComments,
                                numResources = iCalObject.numResources,
                                numAlarms = iCalObject.numAlarms,
                                hasURL = iCalObject.url?.isNotBlank() == true,
                                hasLocation = iCalObject.location?.isNotBlank() == true,
                                hasContact = iCalObject.contact?.isNotBlank() == true,
                                component = iCalObject.component,
                                status = iCalObject.status,
                                classification = iCalObject.classification,
                                priority = iCalObject.priority,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                    }


                }


                if (iCalObject.numAttachments > 0 || iCalObject.numSubtasks > 0 || iCalObject.numSubnotes > 0) {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (iCalObject.numAttachments > 0)
                            ElevatedFilterChip(
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Outlined.AttachFile,
                                            stringResource(R.string.attachments),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(iCalObject.numAttachments.toString())
                                    }
                                },
                                onClick = {
                                    isAttachmentsExpanded = !isAttachmentsExpanded
                                    onExpandedChanged(
                                        iCalObject.id,
                                        isSubtasksExpanded,
                                        isSubnotesExpanded,
                                        isAttachmentsExpanded
                                    )
                                },
                                selected = isAttachmentsExpanded,
                                //border = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        if (iCalObject.numSubtasks > 0)
                            ElevatedFilterChip(
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Outlined.TaskAlt,
                                            stringResource(R.string.subtasks),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            iCalObject.numSubtasks.toString(),
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    isSubtasksExpanded = !isSubtasksExpanded
                                    onExpandedChanged(
                                        iCalObject.id,
                                        isSubtasksExpanded,
                                        isSubnotesExpanded,
                                        isAttachmentsExpanded
                                    )
                                },
                                selected = isSubtasksExpanded,
                                //border = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        if (iCalObject.numSubnotes > 0)
                            ElevatedFilterChip(
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Outlined.Forum,
                                            stringResource(R.string.note),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            iCalObject.numSubnotes.toString(),
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    isSubnotesExpanded = !isSubnotesExpanded
                                    onExpandedChanged(
                                        iCalObject.id,
                                        isSubtasksExpanded,
                                        isSubnotesExpanded,
                                        isAttachmentsExpanded
                                    )
                                },
                                selected = isSubnotesExpanded,
                                //border = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                    }
                }

                AnimatedVisibility(visible = isAttachmentsExpanded) {
                    Column(modifier = Modifier.padding(bottom = 4.dp, start = 8.dp, end = 8.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {

                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            attachments.asReversed().filter { it.fmttype?.startsWith("image/") == true }
                                .forEach { attachment ->
                                    AttachmentCard(
                                        attachment = attachment,
                                        isEditMode = false,
                                        isRemoteCollection = false,   // ATTENTION: We pass false here, because the warning for large file sizes is only relevant for edit mode
                                        withPreview = true,
                                        onAttachmentDeleted = { /* nothing to do, no edit here */ },
                                        modifier = Modifier.size(100.dp, 140.dp)
                                    )
                                }
                        }

                        attachments.asReversed().filter { it.fmttype == null || it.fmttype?.startsWith("image/") == false }.forEach { attachment ->
                            AttachmentCard(
                                attachment = attachment,
                                isEditMode = false,
                                isRemoteCollection = false,   // ATTENTION: We pass false here, because the warning for large file sizes is only relevant for edit mode
                                withPreview = false,
                                onAttachmentDeleted = { /* nothing to do, no edit here */ },
                            )
                        }
                    }
                }

                if (iCalObject.component == Component.VTODO.name && settingShowProgressMaintasks)
                    ProgressElement(
                        iCalObjectId = iCalObject.id,
                        progress = iCalObject.percent,
                        isReadOnly = iCalObject.isReadOnly,
                        isLinkedRecurringInstance = iCalObject.isLinkedRecurringInstance,
                        sliderIncrement = progressIncrement,
                        onProgressChanged = onProgressChanged,
                        modifier = Modifier.fillMaxWidth()
                    )


                AnimatedVisibility(visible = isSubtasksExpanded) {
                    Column(modifier = Modifier.padding(bottom = 4.dp)) {
                        subtasks.forEach { subtask ->

                            SubtaskCard(
                                subtask = subtask,
                                showProgress = settingShowProgressSubtasks,
                                onProgressChanged = onProgressChanged,
                                onDeleteClicked = { },   // no edit possible here
                                onSubtaskUpdated = { },  // no edit possible here
                                sliderIncrement = progressIncrement,
                                modifier = Modifier
                                    .padding(start = 8.dp, end = 8.dp)
                                    .clip(jtxCardCornerShape)
                                    .combinedClickable(
                                        onClick = { goToDetail(subtask.id, false, subtasks)  },
                                        onLongClick = {
                                            if (!subtask.isReadOnly && BillingManager.getInstance().isProPurchased.value == true)
                                                goToDetail(subtask.id, true, subtasks)
                                        }
                                    )
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = isSubnotesExpanded) {
                    Column(modifier = Modifier.padding(bottom = 4.dp)) {
                        subnotes.forEach { subnote ->

                            SubnoteCard(
                                subnote = subnote,
                                player = player,
                                modifier = Modifier
                                    .padding(start = 8.dp, end = 8.dp)
                                    .clip(jtxCardCornerShape)
                                    .combinedClickable(
                                        onClick = { goToDetail(subnote.id, false, subnotes) },
                                        onLongClick = {
                                            if (!subnote.isReadOnly && BillingManager.getInstance().isProPurchased.value == true)
                                                goToDetail(subnote.id, true, subnotes)
                                        },
                                    ),
                                isEditMode = false, //no editing here
                                onDeleteClicked = { }, //no editing here
                                onSubnoteUpdated = { } //no editing here
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
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            uploadPending = false
            isRecurringInstance = false
            isLinkedRecurringInstance = false
            isRecurringOriginal = false
        }
        ICalObjectListCard(
            icalobject,
            listOf(ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            }),
            listOf(ICal4List.getSample().apply {
                this.component = Component.VJOURNAL.name
                this.module = Module.NOTE.name
            }),
            attachments = listOf(Attachment(uri = "https://www.orf.at/file.pdf")),
            progressIncrement = 1,
            goToDetail = { _, _, _ -> },
            onProgressChanged = { _, _, _ -> },
            onExpandedChanged = { _, _, _, _ -> },
            player = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ICalObjectListCardPreview_NOTE() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            component = Component.VJOURNAL.name
            module = Module.NOTE.name
            dtstart = null
            dtstartTimezone = null
            status = StatusJournal.CANCELLED.name
        }
        ICalObjectListCard(
            icalobject,
            listOf(ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            }),
            listOf(ICal4List.getSample().apply {
                this.component = Component.VJOURNAL.name
                this.module = Module.NOTE.name
            }),
            attachments = listOf(Attachment(uri = "https://www.orf.at/file.pdf")),
            progressIncrement = 1,
            goToDetail = { _, _, _ -> },
            onProgressChanged = { _, _, _ -> },
            onExpandedChanged = { _, _, _, _ -> },
            player = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ICalObjectListCardPreview_TODO() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            component = Component.VTODO.name
            module = Module.TODO.name
            percent = 89
            status = StatusTodo.`IN-PROCESS`.name
            classification = Classification.CONFIDENTIAL.name
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis()
            //property.categories = null
            categories =
                "Long category 1, long category 2, long category 3, long category 4"
        }
        ICalObjectListCard(
            icalobject,
            listOf(ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            }),
            listOf(ICal4List.getSample().apply {
                this.component = Component.VJOURNAL.name
                this.module = Module.NOTE.name
            }),
            attachments = listOf(Attachment(uri = "https://www.orf.at/file.pdf")),
            goToDetail = { _, _, _ -> },
            settingShowProgressMaintasks = true,
            progressIncrement = 1,
            onProgressChanged = { _, _, _ -> },
            onExpandedChanged = { _, _, _, _ -> },
            player = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ICalObjectListCardPreview_TODO_no_progress() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            component = Component.VTODO.name
            module = Module.TODO.name
            percent = 89
            status = StatusTodo.`IN-PROCESS`.name
            classification = Classification.CONFIDENTIAL.name
            uploadPending = false
            isRecurringInstance = false
            isRecurringOriginal = false
            isReadOnly = true
            dtstart = null
            due = null
        }
        ICalObjectListCard(
            icalobject,
            listOf(ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            }),
            listOf(ICal4List.getSample().apply {
                this.component = Component.VJOURNAL.name
                this.module = Module.NOTE.name
            }),
            goToDetail = { _, _, _ -> },
            attachments = listOf(Attachment(uri = "https://www.orf.at/file.pdf")),
            progressIncrement = 1,
            settingShowProgressMaintasks = false,
            onProgressChanged = { _, _, _ -> },
            onExpandedChanged = { _, _, _, _ -> },
            player = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ICalObjectListCardPreview_TODO_recur_exception() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            summary =
                "This is a longer summary that would break the line and continue on the second one.\nHere is even a manual line break, let's see what happens."
            component = Component.VTODO.name
            module = Module.TODO.name
            percent = 89
            status = StatusTodo.`IN-PROCESS`.name
            classification = Classification.CONFIDENTIAL.name
            uploadPending = false
            isRecurringInstance = true
            isLinkedRecurringInstance = false
            isRecurringOriginal = false
            isReadOnly = true
        }
        ICalObjectListCard(
            icalobject,
            listOf(ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            }),
            listOf(ICal4List.getSample().apply {
                this.component = Component.VJOURNAL.name
                this.module = Module.NOTE.name
            }),
            goToDetail = { _, _, _ -> },
            attachments = listOf(Attachment(uri = "https://www.orf.at/file.pdf")),
            settingShowProgressMaintasks = false,
            progressIncrement = 1,
            onProgressChanged = { _, _, _ -> },
            onExpandedChanged = { _, _, _, _ -> },
            player = null
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ICalObjectListCardPreview_NOTE_simple() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            summary =
                "This is a longer summary that would break the line and continue on the second one.\nHere is even a manual line break, let's see what happens."
            component = Component.VJOURNAL.name
            module = Module.NOTE.name
            percent = 100
            status = StatusJournal.FINAL.name
            classification = Classification.PUBLIC.name
            uploadPending = false
            isRecurringInstance = true
            isLinkedRecurringInstance = false
            isRecurringOriginal = false
            isReadOnly = true
            numAttachments = 0
            numAttendees = 0
            numAlarms = 0
            numResources = 0
            numSubtasks = 0
            numSubnotes = 0
            numComments = 0
        }
        ICalObjectListCard(
            icalobject,
            emptyList(),
            emptyList(),
            goToDetail = { _, _, _ -> },
            attachments = listOf(),
            settingShowProgressMaintasks = false,
            progressIncrement = 1,
            onProgressChanged = { _, _, _ -> },
            onExpandedChanged = { _, _, _, _ -> },
            player = null
        )
    }
}