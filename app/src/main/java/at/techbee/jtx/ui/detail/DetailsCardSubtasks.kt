/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.reusable.cards.SubtaskCard
import at.techbee.jtx.ui.reusable.dialogs.CreateMultipleSubtasksDialog
import at.techbee.jtx.ui.reusable.dialogs.EditSubtaskDialog
import at.techbee.jtx.ui.reusable.dialogs.SingleOrMultipleSubtasks
import at.techbee.jtx.ui.reusable.dialogs.emptyPreviousText
import at.techbee.jtx.ui.reusable.elements.DragHandle
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.ui.theme.jtxCardCornerShape
import net.fortuna.ical4j.model.Component
import sh.calvin.reorderable.ReorderableColumn


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailsCardSubtasks(
    subtasks: List<ICal4List>,
    isEditMode: MutableState<Boolean>,
    enforceSavingSubtask: Boolean,
    sliderIncrement: Int,
    showSlider: Boolean,
    isSubtaskDragAndDropEnabled: Boolean,
    onSubtaskAdded: (subtask: ICalObject) -> Unit,
    onProgressChanged: (itemId: Long, newPercent: Int) -> Unit,
    onSubtaskUpdated: (icalobjectId: Long, text: String) -> Unit,
    onSubtaskDeleted: (subtaskId: Long) -> Unit,
    onUnlinkSubEntry: (icalobjectId: Long) -> Unit,
    goToDetail: (itemId: Long, editMode: Boolean, list: List<Long>) -> Unit,
    onShowLinkExistingDialog: () -> Unit,
    onUpdateSortOrder: (List<ICal4List>) -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.subtasks)
    var newSubtaskText by rememberSaveable { mutableStateOf("") }
    var previousText by rememberSaveable { mutableStateOf(emptyPreviousText) }

    if(enforceSavingSubtask && newSubtaskText.isNotEmpty()) {
        onSubtaskAdded(ICalObject.createTask(newSubtaskText))
        newSubtaskText = ""
    }

    fun onSubtaskDone(value: String) {
        val listOfSubtasks = value.split("\r\n", "\n", "\r")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (listOfSubtasks.size <= 1) {
            // handle single sub task right now
            onSubtaskAdded(ICalObject.createTask(value))
        } else {
            // handle multiple sub tasks within a dialog
            previousText = SingleOrMultipleSubtasks(single = value, listOfSubtasks = listOfSubtasks)
        }
    }

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeadlineWithIcon(icon = Icons.Outlined.Task, iconDesc = headline, text = headline, modifier = Modifier.weight(1f))

                AnimatedVisibility(isEditMode.value) {
                    IconButton(onClick = { onShowLinkExistingDialog() }) {
                        Icon(painterResource(id = R.drawable.ic_link_variant_plus), stringResource(R.string.details_link_existing_subentry_dialog_title))
                    }
                }
            }


            AnimatedVisibility(isEditMode.value) {
                OutlinedTextField(
                    value = newSubtaskText,
                    trailingIcon = {
                        AnimatedVisibility(newSubtaskText.isNotEmpty()) {
                            IconButton(onClick = {
                                if (newSubtaskText.isNotEmpty()) {
                                    onSubtaskDone(newSubtaskText)
                                }
                                newSubtaskText = ""
                            }) {
                                Icon(
                                    Icons.Outlined.AddTask,
                                    stringResource(id = R.string.edit_subtasks_add_helper)
                                )
                            }
                        }
                    },
                    label = { Text(stringResource(id = R.string.edit_subtasks_add_helper)) },
                    onValueChange = { newValue -> newSubtaskText = newValue },
                    isError = newSubtaskText.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.dp, Color.Transparent)
                        .padding(bottom = 8.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (newSubtaskText.isNotEmpty()) {
                            onSubtaskDone(newSubtaskText)
                        }
                        newSubtaskText = ""
                    })
                )
            }


            AnimatedVisibility(subtasks.isNotEmpty()) {
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
                ) {index, subtask, isDragging ->
                    key(subtask.id) {

                        var showEditSubtaskDialog by rememberSaveable { mutableStateOf(false) }

                        if (showEditSubtaskDialog) {
                            EditSubtaskDialog(
                                text = subtask.summary,
                                onConfirm = { newText -> onSubtaskUpdated(subtask.id, newText) },
                                onDismiss = { showEditSubtaskDialog = false }
                            )
                        }

                        SubtaskCard(
                            subtask = subtask,
                            selected = false,
                            isEditMode = isEditMode.value,
                            showProgress = showSlider,
                            sliderIncrement = sliderIncrement,
                            onProgressChanged = onProgressChanged,
                            onDeleteClicked = { onSubtaskDeleted(subtask.id) },
                            onUnlinkClicked = { onUnlinkSubEntry(subtask.id) },
                            dragHandle = { if(isSubtaskDragAndDropEnabled) DragHandle(this) },
                            modifier = Modifier
                                .clip(jtxCardCornerShape)
                                .combinedClickable(
                                    onClick = {
                                        if (!isEditMode.value)
                                            goToDetail(subtask.id, false, subtasks.map { it.id })
                                        else
                                            showEditSubtaskDialog = true
                                    },
                                    onLongClick = {
                                        if (!isEditMode.value && !subtask.isReadOnly && BillingManager.getInstance().isProPurchased.value == true)
                                            goToDetail(subtask.id, true, subtasks.map { it.id })
                                    }
                                )
                        )
                    }
                }
            }
        }
    }

    if (previousText != emptyPreviousText) {
        CreateMultipleSubtasksDialog(
            numberOfSubtasksDetected = previousText.listOfSubtasks.size,
            onCreateSingle = {
                onSubtaskAdded(ICalObject.createTask(previousText.single))
                previousText = emptyPreviousText
            },
            onCreateMultiple = {
                previousText.listOfSubtasks.forEach { onSubtaskAdded(ICalObject.createTask(it)) }
                previousText = emptyPreviousText
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardSubtasks_Preview() {
    MaterialTheme {

        DetailsCardSubtasks(
            subtasks = listOf(
                        ICal4List.getSample().apply {
                            this.component = Component.VTODO
                            this.module = Module.TODO.name
                            this.summary = "My Subtask"
                        }
                    ),
            isEditMode = remember { mutableStateOf(false) },
            enforceSavingSubtask = false,
            sliderIncrement = 25,
            showSlider = true,
            isSubtaskDragAndDropEnabled = true,
            onSubtaskAdded = { },
            onProgressChanged = { _, _ -> },
            onSubtaskUpdated = { _, _ ->  },
            onSubtaskDeleted = { },
            onUnlinkSubEntry = { },
            goToDetail = { _, _, _ -> },
            onShowLinkExistingDialog = {},
            onUpdateSortOrder = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardSubtasks_Preview_edit() {
    MaterialTheme {
        DetailsCardSubtasks(
            subtasks = listOf(
                ICal4List.getSample().apply {
                    this.component = Component.VTODO
                    this.module = Module.TODO.name
                    this.summary = "My Subtask"
                }
            ),
            isEditMode = remember { mutableStateOf(true) },
            enforceSavingSubtask = false,
            sliderIncrement = 25,
            showSlider = true,
            isSubtaskDragAndDropEnabled = true,
            onSubtaskAdded = { },
            onProgressChanged = { _, _ -> },
            onSubtaskUpdated = { _, _ ->  },
            onSubtaskDeleted = { },
            onUnlinkSubEntry = { },
            goToDetail = { _, _, _ -> },
            onShowLinkExistingDialog = {},
            onUpdateSortOrder = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardSubtasks_Preview_edit_without_Slider() {
    MaterialTheme {
        DetailsCardSubtasks(
            subtasks = listOf(
                ICal4List.getSample().apply {
                    this.component = Component.VTODO
                    this.module = Module.TODO.name
                    this.summary = "My Subtask"
                }
            ),
            isEditMode = remember { mutableStateOf(true) },
            enforceSavingSubtask = false,
            sliderIncrement = 25,
            showSlider = false,
            isSubtaskDragAndDropEnabled = true,
            onSubtaskAdded = { },
            onProgressChanged = { _, _ -> },
            onSubtaskUpdated = { _, _ ->  },
            onSubtaskDeleted = { },
            onUnlinkSubEntry = { },
            goToDetail = { _, _, _ -> },
            onShowLinkExistingDialog = {},
            onUpdateSortOrder = { }
        )
    }
}