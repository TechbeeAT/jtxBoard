/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.cards

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.reusable.dialogs.EditSubtaskDialog
import at.techbee.jtx.ui.reusable.elements.ProgressElement

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun SubtaskCard(
    subtask: ICal4List,
    modifier: Modifier = Modifier,
    showProgress: Boolean = true,
    isEditMode: Boolean = false,
    sliderIncrement: Int,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit,
    onDeleteClicked: (itemId: Long) -> Unit,
    onSubtaskUpdated: (newText: String) -> Unit
) {
    var showEditSubtaskDialog by remember { mutableStateOf(false) }

    if (showEditSubtaskDialog) {
        EditSubtaskDialog(
            text = subtask.summary,
            onConfirm = { newText -> onSubtaskUpdated(newText) },
            onDismiss = { showEditSubtaskDialog = false }
        )
    }

    if (isEditMode) {
        OutlinedCard(
            modifier = modifier,
            onClick = { showEditSubtaskDialog = true }
        ) {
            SubtaskCardContent(
                subtask = subtask,
                showProgress = showProgress,
                isEditMode = isEditMode,
                sliderIncrement = sliderIncrement,
                onProgressChanged = onProgressChanged,
                onDeleteClicked = onDeleteClicked,
            )
        }
    } else {

        ElevatedCard(modifier = modifier) {
            SubtaskCardContent(
                subtask = subtask,
                showProgress = showProgress,
                isEditMode = isEditMode,
                sliderIncrement = sliderIncrement,
                onProgressChanged = onProgressChanged,
                onDeleteClicked = onDeleteClicked,
            )
        }
    }
}


@SuppressLint("UnrememberedMutableState")
@Composable
fun SubtaskCardContent(
    subtask: ICal4List,
    modifier: Modifier = Modifier,
    showProgress: Boolean = true,
    isEditMode: Boolean = false,
    sliderIncrement: Int,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit,
    onDeleteClicked: (itemId: Long) -> Unit,
) {

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {

            var subtaskText = subtask.summary ?: subtask.description ?: ""
            if (subtask.numSubtasks > 0)
                subtaskText += " (+${subtask.numSubtasks})"


            Text(
                subtaskText.trim(),
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            ProgressElement(
                iCalObjectId = subtask.id,
                progress = subtask.percent,
                isReadOnly = subtask.isReadOnly,
                isLinkedRecurringInstance = subtask.isLinkedRecurringInstance,
                sliderIncrement = sliderIncrement,
                showProgressLabel = false,
                showSlider = showProgress,
                onProgressChanged = onProgressChanged,
                modifier = Modifier.width(if(showProgress) 200.dp else 50.dp),
            )

            if (isEditMode) {
                IconButton(onClick = { onDeleteClicked(subtask.id) }) {
                    Icon(Icons.Outlined.Delete, stringResource(id = R.string.delete))
                }
            }


        }
    }
}


@Preview(showBackground = true)
@Composable
fun SubtaskCardPreview() {
    MaterialTheme {
        SubtaskCard(
            ICal4List.getSample().apply {
                this.summary = null
                this.description =
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
                this.isReadOnly = false
                this.numSubtasks = 0
            },
            onProgressChanged = { _, _, _ -> },
            onDeleteClicked = { },
            onSubtaskUpdated = { },
            sliderIncrement = 10,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubtaskCardPreview_readonly() {
    MaterialTheme {
        SubtaskCard(
            ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
                this.isReadOnly = true
                this.numSubtasks = 7
            },
            onProgressChanged = { _, _, _ -> },
            onDeleteClicked = { },
            onSubtaskUpdated = { },
            sliderIncrement = 20,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubtaskCardPreview_without_progress() {
    MaterialTheme {
        SubtaskCard(
            ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            },
            showProgress = false,
            sliderIncrement = 50,
            onProgressChanged = { _, _, _ -> },
            onDeleteClicked = { },
            onSubtaskUpdated = { },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubtaskCardPreview_edit() {
    MaterialTheme {
        SubtaskCard(
            ICal4List.getSample().apply {
                this.summary = "Subtask here"
                this.description =
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
                this.isReadOnly = false
                this.numSubtasks = 0
            },
            onProgressChanged = { _, _, _ -> },
            onDeleteClicked = { },
            onSubtaskUpdated = { },
            isEditMode = true,
            sliderIncrement = 10,
            modifier = Modifier.fillMaxWidth()
        )
    }
}