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
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.reusable.elements.ProgressElement

@SuppressLint("UnrememberedMutableState")
@Composable
fun SubtaskCard(
    subtask: ICal4List,
    selected: Boolean,
    modifier: Modifier = Modifier,
    showProgress: Boolean = true,
    isEditMode: Boolean = false,
    sliderIncrement: Int,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit,
    onDeleteClicked: (itemId: Long) -> Unit
) {


    Card(
        modifier = modifier,
        colors = if(isEditMode) CardDefaults.outlinedCardColors()
            else if (selected) CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            else CardDefaults.elevatedCardColors(),
        elevation = if(isEditMode) CardDefaults.outlinedCardElevation() else CardDefaults.elevatedCardElevation(),
        border = if(isEditMode) CardDefaults.outlinedCardBorder() else null,
    ) {

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {

            var subtaskText = subtask.summary ?: subtask.description ?: ""
            if (subtask.numSubtasks > 0)
                subtaskText += " (+${subtask.numSubtasks})"

            ProgressElement(
                label = subtaskText.trim(),
                iCalObjectId = subtask.id,
                progress = subtask.percent,
                isReadOnly = subtask.isReadOnly,
                isLinkedRecurringInstance = subtask.isLinkedRecurringInstance,
                sliderIncrement = sliderIncrement,
                showSlider = showProgress,
                onProgressChanged = onProgressChanged,
                modifier = Modifier.weight(1f),
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
            selected = false,
            onProgressChanged = { _, _, _ -> },
            onDeleteClicked = { },
            sliderIncrement = 10,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubtaskCardPreview_selected() {
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
            selected = true,
            onProgressChanged = { _, _, _ -> },
            onDeleteClicked = { },
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
            selected = false,
            onProgressChanged = { _, _, _ -> },
            onDeleteClicked = { },
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
            selected = false,
            showProgress = false,
            sliderIncrement = 50,
            onProgressChanged = { _, _, _ -> },
            onDeleteClicked = { },
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
            selected = false,
            onProgressChanged = { _, _, _ -> },
            onDeleteClicked = { },
            isEditMode = true,
            sliderIncrement = 10,
            modifier = Modifier.fillMaxWidth()
        )
    }
}