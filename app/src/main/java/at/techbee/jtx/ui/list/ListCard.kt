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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.SubdirectoryArrowRight
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.techbee.jtx.R
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredResource
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.reusable.cards.AttachmentCard
import at.techbee.jtx.ui.reusable.cards.SubnoteCard
import at.techbee.jtx.ui.reusable.cards.SubtaskCard
import at.techbee.jtx.ui.reusable.elements.AudioPlaybackElement
import at.techbee.jtx.ui.reusable.elements.DragHandle
import at.techbee.jtx.ui.reusable.elements.ProgressElement
import at.techbee.jtx.ui.reusable.elements.VerticalDateBlock
import at.techbee.jtx.ui.settings.DropdownSettingOption
import at.techbee.jtx.ui.theme.Typography
import at.techbee.jtx.ui.theme.jtxCardBorderStrokeWidth
import at.techbee.jtx.ui.theme.jtxCardCornerShape
import com.arnyminerz.markdowntext.MarkdownText
import sh.calvin.reorderable.ReorderableColumn


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListCard(
    iCalObject: ICal4List,
    categories: List<Category>,
    resources: List<Resource>,
    subtasks: List<ICal4List>,
    subnotes: List<ICal4List>,
    parents: List<ICal4List>,
    selected: List<Long>,
    attachments: List<Attachment>,
    storedCategories: List<StoredCategory>,
    storedResources: List<StoredResource>,
    storedStatuses: List<ExtendedStatus>,
    modifier: Modifier = Modifier,
    player: MediaPlayer?,
    isSubtasksExpandedDefault: Boolean = true,
    isSubnotesExpandedDefault: Boolean = true,
    isParentsExpandedDefault: Boolean = true,
    isAttachmentsExpandedDefault: Boolean = true,
    settingShowProgressMaintasks: Boolean = false,
    settingShowProgressSubtasks: Boolean = true,
    settingDisplayTimezone: DropdownSettingOption,
    settingIsAccessibilityMode: Boolean,
    progressIncrement: Int,
    linkProgressToSubtasks: Boolean,
    markdownEnabled: Boolean,
    isSubtaskDragAndDropEnabled: Boolean,
    isSubnoteDragAndDropEnabled: Boolean,
    onClick: (itemId: Long, list: List<ICal4List>, isReadOnly: Boolean) -> Unit,
    onLongClick: (itemId: Long, list: List<ICal4List>) -> Unit,
    onProgressChanged: (itemId: Long, newPercent: Int) -> Unit,
    onExpandedChanged: (itemId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isParentsExpanded: Boolean, isAttachmentsExpanded: Boolean) -> Unit,
    onUpdateSortOrder: (List<ICal4List>) -> Unit,
    dragHandle:@Composable () -> Unit = { }
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
    var isParentsExpanded by remember {
        mutableStateOf(
            iCalObject.isParentsExpanded ?: isParentsExpandedDefault
        )
    }
    var isAttachmentsExpanded by remember {
        mutableStateOf(
            iCalObject.isAttachmentsExpanded ?: isAttachmentsExpandedDefault
        )
    }

    val summarySize =
        if (iCalObject.module == Module.JOURNAL.name) 18.sp else Typography.bodyMedium.fontSize
    val summaryDescriptionTextDecoration =
        if (iCalObject.status == Status.CANCELLED.status) TextDecoration.LineThrough else null


    @Composable
    fun getFormattedDescription() {
        return if(markdownEnabled)
            MarkdownText(
                markdown = iCalObject.description?.trim() ?: "",
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                textDecoration = summaryDescriptionTextDecoration,
                modifier = Modifier.fillMaxWidth()
            )
        else
            Text(
                text = iCalObject.description?.trim() ?: "",
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                textDecoration = summaryDescriptionTextDecoration,
                modifier = Modifier.fillMaxWidth()
            )
    }

    Card(
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected.contains(iCalObject.id)) MaterialTheme.colorScheme.primaryContainer else CardDefaults.elevatedCardColors().containerColor,
            contentColor = if (selected.contains(iCalObject.id)) MaterialTheme.colorScheme.onPrimaryContainer else CardDefaults.elevatedCardColors().contentColor,
        ),
        elevation = CardDefaults.elevatedCardElevation(),
        border = iCalObject.colorItem?.let { BorderStroke(jtxCardBorderStrokeWidth, Color(it)) },
        modifier = modifier
    ) {

        Column(
            modifier = Modifier.padding(top = 4.dp, bottom = 0.dp, start = 8.dp, end = 8.dp)
        ) {

            ListTopRow(
                ical4List = iCalObject,
                categories = categories,
                resources = resources,
                storedCategories = storedCategories,
                storedResources = storedResources,
                extendedStatuses = storedStatuses,
                showAttachments = false,
                showSubtasks = false,
                showSubnotes = false,
                isAccessibilityMode = settingIsAccessibilityMode
            )

            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {


                if (iCalObject.module == Module.JOURNAL.name)
                    VerticalDateBlock(
                        datetime = iCalObject.dtstart,
                        timezone = iCalObject.dtstartTimezone,
                        settingDisplayTimezone = settingDisplayTimezone,
                        modifier = Modifier
                            .padding(
                                start = 4.dp,
                                end = 12.dp
                            )
                            .widthIn(min = 48.dp)
                    )

                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)

                ) {
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        dragHandle()

                        if (iCalObject.summary?.isNotBlank() == true) {
                            Text(
                                text = iCalObject.summary?.trim() ?: "",
                                fontWeight = FontWeight.Bold,
                                fontSize = summarySize,
                                textDecoration = summaryDescriptionTextDecoration,
                                modifier = Modifier.weight(1f)
                            )
                        } else if (iCalObject.description?.isNotBlank() == true) {
                            getFormattedDescription()
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        if (iCalObject.module == Module.TODO.name && !settingShowProgressMaintasks)
                            Checkbox(
                                checked = iCalObject.percent == 100 || iCalObject.status == Status.COMPLETED.status,
                                enabled = !iCalObject.isReadOnly && !(linkProgressToSubtasks && subtasks.isNotEmpty()),
                                onCheckedChange = {
                                    onProgressChanged(
                                        iCalObject.id,
                                        if (it) 100 else 0
                                    )
                                },
                                modifier = Modifier.padding(0.dp)
                            )
                    }

                    if(iCalObject.summary?.isNotBlank() == true && iCalObject.description?.isNotBlank() == true) {
                        getFormattedDescription()
                    }


                }
            }

            if (iCalObject.component == Component.VTODO.name && settingShowProgressMaintasks) {
                ProgressElement(
                    label = null,
                    iCalObjectId = iCalObject.id,
                    progress = iCalObject.percent,
                    status = iCalObject.status,
                    isReadOnly = iCalObject.isReadOnly || (linkProgressToSubtasks && subtasks.isNotEmpty()),
                    sliderIncrement = progressIncrement,
                    onProgressChanged = onProgressChanged,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (iCalObject.numAttachments > 0 || subtasks.isNotEmpty() || subnotes.isNotEmpty() || parents.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
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
                                    isParentsExpanded,
                                    isAttachmentsExpanded
                                )
                            },
                            selected = isAttachmentsExpanded,
                            //border = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    if (subtasks.isNotEmpty())
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
                                        subtasks.size.toString(),
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
                                    isParentsExpanded,
                                    isAttachmentsExpanded
                                )
                            },
                            selected = isSubtasksExpanded,
                            //border = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    if (subnotes.isNotEmpty())
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
                                        subnotes.size.toString(),
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
                                    isParentsExpanded,
                                    isAttachmentsExpanded
                                )
                            },
                            selected = isSubnotesExpanded,
                            //border = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    if (parents.isNotEmpty())
                        ElevatedFilterChip(
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.SubdirectoryArrowRight,
                                        stringResource(R.string.view_subtask_of),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        parents.size.toString(),
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            },
                            onClick = {
                                isParentsExpanded = !isParentsExpanded
                                onExpandedChanged(
                                    iCalObject.id,
                                    isSubtasksExpanded,
                                    isSubnotesExpanded,
                                    isParentsExpanded,
                                    isAttachmentsExpanded
                                )
                            },
                            selected = isParentsExpanded,
                            //border = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                }
            }

            AnimatedVisibility(visible = isAttachmentsExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {

                    Row(
                        modifier = Modifier
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
                                    player = player,
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
                            player = player,
                            onAttachmentDeleted = { /* nothing to do, no edit here */ },
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isSubtasksExpanded) {
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
                        .padding(top = 4.dp)
                ) { _, subtask, _ ->
                    key(subtask.id) {

                        SubtaskCard(
                            subtask = subtask,
                            selected = selected.contains(subtask.id),
                            showProgress = settingShowProgressSubtasks,
                            onProgressChanged = onProgressChanged,
                            onDeleteClicked = { },   // no edit possible here
                            onUnlinkClicked = { },
                            sliderIncrement = progressIncrement,
                            dragHandle = { if(isSubtaskDragAndDropEnabled) DragHandle(scope = this) },
                            modifier = Modifier
                                .clip(jtxCardCornerShape)
                                .combinedClickable(
                                    onClick = { onClick(subtask.id, subtasks, subtask.isReadOnly) },
                                    onLongClick = {
                                        if (!subtask.isReadOnly && BillingManager.getInstance().isProPurchased.value == true)
                                            onLongClick(subtask.id, subtasks)
                                    }
                                )
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isSubnotesExpanded) {
                ReorderableColumn(
                    list = subnotes,
                    onSettle = { fromIndex, toIndex ->
                        val reordered = subnotes.toMutableList().apply {
                            add(toIndex, removeAt(fromIndex))
                        }
                        onUpdateSortOrder(reordered)
                    },
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) { _, subnote, _ ->
                    key(subnote.id) {


                        SubnoteCard(
                            subnote = subnote,
                            selected = selected.contains(subnote.id),
                            player = player,
                            modifier = Modifier
                                .clip(jtxCardCornerShape)
                                .combinedClickable(
                                    onClick = { onClick(subnote.id, subnotes, subnote.isReadOnly) },
                                    onLongClick = {
                                        if (!subnote.isReadOnly && BillingManager.getInstance().isProPurchased.value == true)
                                            onLongClick(subnote.id, subnotes)
                                    },
                                ),
                            dragHandle = { if(isSubnoteDragAndDropEnabled) DragHandle(scope = this) },
                            isEditMode = false, //no editing here
                            onDeleteClicked = { }, //no editing here
                            onUnlinkClicked = { }, //no editing here
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isParentsExpanded) {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    parents.forEach { parent ->

                        if(parent.module == Module.TODO.name) {
                            SubtaskCard(
                                subtask = parent,
                                selected = selected.contains(parent.id),
                                showProgress = settingShowProgressSubtasks,
                                onProgressChanged = onProgressChanged,
                                onDeleteClicked = { },   // no edit possible here
                                onUnlinkClicked = { },
                                sliderIncrement = progressIncrement,
                                blockProgressUpdates = linkProgressToSubtasks,
                                modifier = Modifier
                                    .clip(jtxCardCornerShape)
                                    .combinedClickable(
                                        onClick = {
                                            onClick(
                                                parent.id,
                                                parents,
                                                parent.isReadOnly
                                            )
                                        },
                                        onLongClick = {
                                            if (!parent.isReadOnly && BillingManager.getInstance().isProPurchased.value == true)
                                                onLongClick(parent.id, parents)
                                        }
                                    )
                            )
                        } else {
                            SubnoteCard(
                                subnote = parent,
                                selected = selected.contains(parent.id),
                                player = player,
                                modifier = Modifier
                                    .clip(jtxCardCornerShape)
                                    .combinedClickable(
                                        onClick = {
                                            onClick(
                                                parent.id,
                                                parents,
                                                parent.isReadOnly
                                            )
                                        },
                                        onLongClick = {
                                            if (!parent.isReadOnly && BillingManager.getInstance().isProPurchased.value == true)
                                                onLongClick(parent.id, parents)
                                        },
                                    ),
                                isEditMode = false, //no editing here
                                onDeleteClicked = { }, //no editing here
                                onUnlinkClicked = { }, //no editing here
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
        }
        ListCard(
            iCalObject = icalobject,
            categories = listOf(Category(text = "Test"), Category(text = "two")),
            resources = listOf(Resource(text = "Projector"), Resource(text="another")),
            subtasks = listOf(ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            }),
            subnotes = listOf(ICal4List.getSample().apply {
                this.component = Component.VJOURNAL.name
                this.module = Module.NOTE.name
            }),
            parents = listOf(ICal4List.getSample().apply {
                this.component = Component.VJOURNAL.name
                this.module = Module.NOTE.name
            }),
            storedCategories = listOf(StoredCategory("Test", Color.Cyan.toArgb())),
            storedResources = listOf(StoredResource("Projector", Color.Green.toArgb())),
            storedStatuses = listOf(ExtendedStatus("Individual", Module.JOURNAL, Status.FINAL, Color.Green.toArgb())),
            selected = listOf(),
            attachments = listOf(Attachment(uri = "https://www.orf.at/file.pdf")),
            progressIncrement = 1,
            linkProgressToSubtasks = false,
            settingDisplayTimezone = DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL,
            settingIsAccessibilityMode = false,
            markdownEnabled = false,
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            onProgressChanged = { _, _ -> },
            onExpandedChanged = { _, _, _, _, _ -> },
            player = null,
            isSubtaskDragAndDropEnabled = true,
            isSubnoteDragAndDropEnabled = true,
            onUpdateSortOrder = { }
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
            status = Status.CANCELLED.status
        }
        ListCard(
            iCalObject = icalobject,
            categories = emptyList(),
            resources = emptyList(),
            subtasks = listOf(ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            }),
            subnotes  = listOf(ICal4List.getSample().apply {
                this.component = Component.VJOURNAL.name
                this.module = Module.NOTE.name
            }),
            parents = emptyList(),
            storedCategories = listOf(StoredCategory("Test", Color.Cyan.toArgb())),
            storedResources = listOf(StoredResource("Projector", Color.Green.toArgb())),
            storedStatuses = listOf(ExtendedStatus("Individual", Module.JOURNAL, Status.FINAL, Color.Green.toArgb())),
            selected = listOf(),
            attachments = listOf(Attachment(uri = "https://www.orf.at/file.pdf")),
            progressIncrement = 1,
            linkProgressToSubtasks = false,
            settingDisplayTimezone = DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL,
            settingIsAccessibilityMode = false,
            markdownEnabled = false,
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            onProgressChanged = { _, _ -> },
            onExpandedChanged = { _, _, _, _, _ -> },
            player = null,
            isSubtaskDragAndDropEnabled = true,
            isSubnoteDragAndDropEnabled = true,
            onUpdateSortOrder = { }
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
            status = Status.IN_PROCESS.status
            classification = Classification.CONFIDENTIAL.name
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis()
            //property.categories = null
            categories =
                "Long category 1, long category 2, long category 3, long category 4"
        }
        ListCard(
            iCalObject = icalobject,
            categories = emptyList(),
            resources = emptyList(),
            subtasks = listOf(ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            }),
            listOf(ICal4List.getSample().apply {
                this.component = Component.VJOURNAL.name
                this.module = Module.NOTE.name
            }),
            parents = emptyList(),
            storedCategories = listOf(StoredCategory("Test", Color.Cyan.toArgb())),
            storedResources = listOf(StoredResource("Projector", Color.Green.toArgb())),
            storedStatuses = listOf(ExtendedStatus("Individual", Module.JOURNAL, Status.FINAL, Color.Green.toArgb())),
            selected = listOf(),
            attachments = listOf(Attachment(uri = "https://www.orf.at/file.pdf")),
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            settingShowProgressMaintasks = true,
            settingDisplayTimezone = DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL,
            settingIsAccessibilityMode = false,
            progressIncrement = 1,
            linkProgressToSubtasks = false,
            markdownEnabled = false,
            onProgressChanged = { _, _ -> },
            onExpandedChanged = { _, _, _, _, _ -> },
            player = null,
            isSubtaskDragAndDropEnabled = true,
            isSubnoteDragAndDropEnabled = true,
            onUpdateSortOrder = { }
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
            status = Status.IN_PROCESS.status
            classification = Classification.CONFIDENTIAL.name
            uploadPending = false
            isReadOnly = true
            dtstart = null
            due = null
        }
        ListCard(
            iCalObject = icalobject,
            categories = emptyList(),
            resources = emptyList(),
            subtasks = listOf(ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            }),
            subnotes = listOf(ICal4List.getSample().apply {
                this.component = Component.VJOURNAL.name
                this.module = Module.NOTE.name
            }),
            parents = emptyList(),
            storedCategories = listOf(StoredCategory("Test", Color.Cyan.toArgb())),
            storedResources = listOf(StoredResource("Projector", Color.Green.toArgb())),
            storedStatuses = listOf(ExtendedStatus("Individual", Module.JOURNAL, Status.FINAL, Color.Green.toArgb())),
            selected = listOf(),
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            attachments = listOf(Attachment(uri = "https://www.orf.at/file.pdf")),
            progressIncrement = 1,
            linkProgressToSubtasks = false,
            settingShowProgressMaintasks = false,
            settingDisplayTimezone = DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL,
            settingIsAccessibilityMode = false,
            markdownEnabled = false,
            onProgressChanged = { _, _ -> },
            onExpandedChanged = { _, _, _, _, _ -> },
            player = null,
            isSubtaskDragAndDropEnabled = true,
            isSubnoteDragAndDropEnabled = true,
            onUpdateSortOrder = { }
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
            status = Status.IN_PROCESS.status
            classification = Classification.CONFIDENTIAL.name
            uploadPending = false
            rrule = ""
            isReadOnly = true
            colorItem = null
        }
        ListCard(
            iCalObject = icalobject,
            categories = emptyList(),
            resources = emptyList(),
            subtasks = listOf(ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            }),
            subnotes = listOf(ICal4List.getSample().apply {
                this.component = Component.VJOURNAL.name
                this.module = Module.NOTE.name
            }),
            parents = emptyList(),
            storedCategories = listOf(StoredCategory("Test", Color.Cyan.toArgb())),
            storedResources = listOf(StoredResource("Projector", Color.Green.toArgb())),
            storedStatuses = listOf(ExtendedStatus("Individual", Module.JOURNAL, Status.FINAL, Color.Green.toArgb())),
            selected = listOf(),
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            attachments = listOf(Attachment(uri = "https://www.orf.at/file.pdf")),
            settingShowProgressMaintasks = false,
            settingDisplayTimezone = DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL,
            settingIsAccessibilityMode = false,
            progressIncrement = 1,
            linkProgressToSubtasks = false,
            markdownEnabled = false,
            onProgressChanged = { _, _ -> },
            onExpandedChanged = { _, _, _, _, _ -> },
            player = null,
            isSubtaskDragAndDropEnabled = true,
            isSubnoteDragAndDropEnabled = true,
            onUpdateSortOrder = { }
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
            status = Status.FINAL.status
            classification = Classification.PUBLIC.name
            uploadPending = false
            recurid = ""
            isReadOnly = true
            numAttachments = 0
            numAttendees = 0
            numAlarms = 0
            numResources = 0
            numSubtasks = 0
            numSubnotes = 0
            numComments = 0
            colorItem = Color.Blue.toArgb()
        }
        ListCard(
            iCalObject = icalobject,
            categories = emptyList(),
            resources = emptyList(),
            subtasks = emptyList(),
            subnotes = emptyList(),
            parents = emptyList(),
            storedCategories = listOf(StoredCategory("Test", Color.Cyan.toArgb())),
            storedResources = listOf(StoredResource("Projector", Color.Green.toArgb())),
            storedStatuses = listOf(ExtendedStatus("Individual", Module.JOURNAL, Status.FINAL, Color.Green.toArgb())),
            selected = listOf(),
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            attachments = listOf(),
            settingShowProgressMaintasks = false,
            settingDisplayTimezone = DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL,
            settingIsAccessibilityMode = false,
            progressIncrement = 1,
            linkProgressToSubtasks = false,
            markdownEnabled = false,
            onProgressChanged = { _, _ -> },
            onExpandedChanged = { _, _, _, _, _ -> },
            player = null,
            isSubtaskDragAndDropEnabled = true,
            isSubnoteDragAndDropEnabled = true,
            onUpdateSortOrder = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ICalObjectListCardPreview_TASK_one_liner() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            summary = "Short task"
            description = null
            component = Component.VTODO.name
            module = Module.TODO.name
            percent = 0
            status = null
            classification = Classification.PUBLIC.name
            uploadPending = false
            recurid = ""
            isReadOnly = true
            numAttachments = 0
            numAttendees = 0
            numAlarms = 0
            numResources = 0
            numSubtasks = 0
            numSubnotes = 0
            numComments = 0
            colorItem = Color.Blue.toArgb()
        }
        ListCard(
            iCalObject = icalobject,
            categories = emptyList(),
            resources = emptyList(),
            subtasks = emptyList(),
            subnotes = emptyList(),
            parents = emptyList(),
            storedCategories = emptyList(),
            storedResources = emptyList(),
            storedStatuses = emptyList(),
            selected = listOf(),
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            attachments = listOf(),
            settingShowProgressMaintasks = false,
            settingDisplayTimezone = DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL,
            settingIsAccessibilityMode = false,
            progressIncrement = 1,
            linkProgressToSubtasks = false,
            markdownEnabled = false,
            onProgressChanged = { _, _ -> },
            onExpandedChanged = { _, _, _, _, _ -> },
            player = null,
            isSubtaskDragAndDropEnabled = true,
            isSubnoteDragAndDropEnabled = true,
            onUpdateSortOrder = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ICalObjectListCardPreview_NOTE_one_liner() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            summary = "Short note"
            description = null
            component = Component.VJOURNAL.name
            module = Module.NOTE.name
            percent = 0
            status = null
            classification = Classification.PUBLIC.name
            uploadPending = false
            recurid = ""
            isReadOnly = true
            numAttachments = 0
            numAttendees = 0
            numAlarms = 0
            numResources = 0
            numSubtasks = 0
            numSubnotes = 0
            numComments = 0
            colorItem = Color.Blue.toArgb()
        }
        ListCard(
            iCalObject = icalobject,
            categories = emptyList(),
            resources = emptyList(),
            subtasks = emptyList(),
            subnotes = emptyList(),
            parents = emptyList(),
            storedCategories = emptyList(),
            storedResources = emptyList(),
            storedStatuses = emptyList(),
            selected = listOf(icalobject.id),
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            attachments = listOf(),
            settingShowProgressMaintasks = false,
            settingDisplayTimezone = DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL,
            settingIsAccessibilityMode = false,
            progressIncrement = 1,
            linkProgressToSubtasks = false,
            markdownEnabled = false,
            onProgressChanged = { _, _ -> },
            onExpandedChanged = { _, _, _, _, _ -> },
            player = null,
            isSubtaskDragAndDropEnabled = true,
            isSubnoteDragAndDropEnabled = true,
            onUpdateSortOrder = { }
        )
    }
}