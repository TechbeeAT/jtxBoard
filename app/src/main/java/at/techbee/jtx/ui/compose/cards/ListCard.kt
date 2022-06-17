/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.cards

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import at.techbee.jtx.ui.compose.elements.ColoredEdge
import at.techbee.jtx.ui.compose.elements.ListStatusBar
import at.techbee.jtx.ui.compose.elements.ProgressElement
import at.techbee.jtx.ui.compose.elements.VerticalDateBlock
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.ui.theme.Typography


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ICalObjectListCard(
    iCalObjectWithRelatedto: ICal4ListWithRelatedto,
    subtasks: List<ICal4List>,
    subnotes: List<ICal4List>,
    modifier: Modifier = Modifier,
    player: MediaPlayer?,
    isSubtasksExpandedDefault: Boolean = true,
    isSubnotesExpandedDefault: Boolean = true,
    isAttachmentsExpandedDefault: Boolean = true,
    settingShowProgressMaintasks: Boolean = false,
    settingShowProgressSubtasks: Boolean = true,
    goToView: (itemId: Long) -> Unit,
    goToEdit: (itemId: Long) -> Unit,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit,
    onExpandedChanged: (itemId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isAttachmentsExpanded: Boolean) -> Unit
) {

    val context = LocalContext.current
    val iCalObject = iCalObjectWithRelatedto.property

    /*
    var markwon = Markwon.builder(LocalContext.current)
        .usePlugin(StrikethroughPlugin.create())
        .build()
     */

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

                    Row(modifier = Modifier.weight(1f)) {
                        Text(
                            iCalObject.collectionDisplayName ?: iCalObject.accountName ?: "",
                            style = Typography.labelMedium,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 16.dp)
                        )

                        iCalObject.categories?.let {
                            Text(
                                it,
                                style = Typography.labelMedium,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .weight(1f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (iCalObject.module == Module.TODO.name) {
                            iCalObject.dtstart?.let {
                                Text(
                                    iCalObject.getDtstartTextInfo(LocalContext.current)
                                        ?: "",
                                    style = Typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier.padding(end = 16.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            iCalObject.due?.let {
                                Text(
                                    iCalObject.getDueTextInfo(LocalContext.current) ?: "",
                                    style = Typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 16.dp)
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
                            iCalObject.dtstart ?: System.currentTimeMillis(),
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
                                    text = iCalObject.summary ?: "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = summarySize,
                                    textDecoration = summaryTextDecoration,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .weight(1f)
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
                                text = iCalObject.description ?: "",
                                maxLines = 6,
                                overflow = TextOverflow.Ellipsis
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
                    Column(modifier = Modifier.padding(bottom = 4.dp)) {
                        iCalObjectWithRelatedto.attachment?.forEach { attachment ->
                            AttachmentCard(
                                attachment,
                                modifier = Modifier
                                    .padding(start = 8.dp, end = 8.dp)
                                    .combinedClickable(
                                        onClick = { attachment.openFile(context) }
                                    ))
                        }
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


                AnimatedVisibility(visible = isSubtasksExpanded) {
                    Column(modifier = Modifier.padding(bottom = 4.dp)) {
                        subtasks.forEach { subtask ->

                            SubtaskCard(
                                subtask = subtask,
                                showProgress = settingShowProgressSubtasks,
                                onProgressChanged = onProgressChanged,
                                modifier = Modifier
                                    .padding(start = 8.dp, end = 8.dp)
                                    .combinedClickable(
                                        onClick = { goToView(subtask.id)  },
                                        onLongClick = {
                                            if (!subtask.isReadOnly && BillingManager.getInstance()?.isProPurchased?.value == true)
                                                goToEdit(subtask.id)
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
                                    .combinedClickable(
                                        onClick = { goToView(subnote.id) },
                                        onLongClick = {
                                            if (!subnote.isReadOnly && BillingManager.getInstance()?.isProPurchased?.value == true)
                                                goToEdit(subnote.id)
                                        },
                                    )
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

        val icalobject = ICal4ListWithRelatedto.getSample().apply {
            this.property.uploadPending = false
            this.property.isRecurringInstance = false
            this.property.isLinkedRecurringInstance = false
            this.property.isRecurringOriginal = false
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
            goToView = { },
            goToEdit = { },
            onProgressChanged = { _, _, _ -> },
            onExpandedChanged = { _, _, _, _ -> },
            player = null
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
            listOf(ICal4List.getSample().apply {
                this.component = Component.VJOURNAL.name
                this.module = Module.NOTE.name
            }),
            goToView = { },
            goToEdit = { },
            onProgressChanged = { _, _, _ -> },
            onExpandedChanged = { _, _, _, _ -> },
            player = null
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
            //property.categories = null
            property.categories =
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
            goToView = { },
            goToEdit = { },
            settingShowProgressMaintasks = true,
            onProgressChanged = { _, _, _ -> },
            onExpandedChanged = { _, _, _, _ -> },
            player = null
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
            listOf(ICal4List.getSample().apply {
                this.component = Component.VJOURNAL.name
                this.module = Module.NOTE.name
            }),
            goToView = { },
            goToEdit = { },
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
    JtxBoardTheme {

        val icalobject = ICal4ListWithRelatedto.getSample().apply {
            property.summary =
                "This is a longer summary that would break the line and continue on the second one.\nHere is even a manual line break, let's see what happens."
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
            listOf(ICal4List.getSample().apply {
                this.component = Component.VJOURNAL.name
                this.module = Module.NOTE.name
            }),
            goToView = { },
            goToEdit = { },
            settingShowProgressMaintasks = false,
            onProgressChanged = { _, _, _ -> },
            onExpandedChanged = { _, _, _, _ -> },
            player = null
        )
    }
}
