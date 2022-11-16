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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.reusable.cards.SubtaskCard
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.ui.theme.jtxCardCornerShape
import net.fortuna.ical4j.model.Component


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailsCardSubtasks(
    subtasks: List<ICal4List>,
    isEditMode: MutableState<Boolean>,
    onSubtaskAdded: (subtask: ICalObject) -> Unit,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit,
    onSubtaskUpdated: (icalobjectId: Long, text: String) -> Unit,
    onSubtaskDeleted: (subtaskId: Long) -> Unit,
    goToDetail: (itemId: Long, editMode: Boolean, list: List<Long>) -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.subtasks)
    var newSubtaskText by rememberSaveable { mutableStateOf("") }


    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            HeadlineWithIcon(icon = Icons.Outlined.Task, iconDesc = headline, text = headline)

            AnimatedVisibility(subtasks.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    subtasks.forEach { subtask ->
                        SubtaskCard(
                            subtask = subtask,
                            isEditMode = isEditMode.value,
                            showProgress = true, /* TODO */
                            sliderIncrement = 1, /* TODO */
                            onProgressChanged = onProgressChanged,
                            onDeleteClicked = { icalObjectId ->  onSubtaskDeleted(icalObjectId) },
                            onSubtaskUpdated = { newText -> onSubtaskUpdated(subtask.id, newText) },
                            modifier = Modifier
                                .clip(jtxCardCornerShape)
                                .combinedClickable(
                                onClick = { if(!isEditMode.value) goToDetail(subtask.id, false, subtasks.map { it.id }) },
                                onLongClick = {
                                    if (!isEditMode.value &&!subtask.isReadOnly && BillingManager.getInstance().isProPurchased.value == true)
                                        goToDetail(subtask.id, true, subtasks.map { it.id })
                                }
                            )
                        )
                    }
                }
            }

            AnimatedVisibility(isEditMode.value) {
                OutlinedTextField(
                    value = newSubtaskText,
                    trailingIcon = {
                        AnimatedVisibility(newSubtaskText.isNotEmpty()) {
                            IconButton(onClick = {
                                if(newSubtaskText.isNotEmpty())
                                    onSubtaskAdded(ICalObject.createTask(newSubtaskText))
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
                    //colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
                    isError = newSubtaskText.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.dp, Color.Transparent),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if(newSubtaskText.isNotEmpty())
                            onSubtaskAdded(ICalObject.createTask(newSubtaskText))
                        newSubtaskText = ""
                    }),
                    textStyle = TextStyle(textDirection = TextDirection.Content)
                )
            }
        }
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
            onSubtaskAdded = { },
            onProgressChanged = { _, _, _ -> },
            onSubtaskUpdated = { _, _ ->  },
            onSubtaskDeleted = { },
            goToDetail = { _, _, _ -> }
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
            onSubtaskAdded = { },
            onProgressChanged = { _, _, _ -> },
            onSubtaskUpdated = { _, _ ->  },
            onSubtaskDeleted = { },
            goToDetail = { _, _, _ -> }
        )
    }
}