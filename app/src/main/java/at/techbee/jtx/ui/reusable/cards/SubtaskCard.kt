/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.reusable.dialogs.UnlinkEntryDialog
import at.techbee.jtx.ui.reusable.elements.ProgressElement

@Composable
fun SubtaskCard(
    subtask: ICal4List,
    selected: Boolean,
    modifier: Modifier = Modifier,
    showProgress: Boolean = true,
    isEditMode: Boolean = false,
    sliderIncrement: Int,
    blockProgressUpdates: Boolean = false,
    allowDeletion: Boolean = true,
    onProgressChanged: (itemId: Long, newPercent: Int) -> Unit,
    onDeleteClicked: () -> Unit,
    onUnlinkClicked: () -> Unit
) {

    var showUnlinkFromParentDialog by rememberSaveable { mutableStateOf(false) }
    if(showUnlinkFromParentDialog) {
        UnlinkEntryDialog(
            onConfirm = { onUnlinkClicked() },
            onDismiss = { showUnlinkFromParentDialog = false }
        )
    }


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
                status = subtask.status,
                isReadOnly = subtask.isReadOnly || blockProgressUpdates,
                sliderIncrement = sliderIncrement,
                showSlider = showProgress,
                onProgressChanged = onProgressChanged,
                modifier = Modifier.weight(1f),
            )

            if (isEditMode) {
                VerticalDivider(modifier = Modifier.height(28.dp))

                if(allowDeletion) {
                    IconButton(onClick = { onDeleteClicked() }) {
                        Icon(Icons.Outlined.Delete, stringResource(id = R.string.delete))
                    }
                }
                IconButton(onClick = { showUnlinkFromParentDialog = true }) {
                    Icon(painterResource(id = R.drawable.ic_link_variant_remove), stringResource(R.string.dialog_unlink_from_parent_title))
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
            onProgressChanged = { _, _ -> },
            onDeleteClicked = { },
            onUnlinkClicked = { },
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
            onProgressChanged = { _, _ -> },
            onDeleteClicked = { },
            onUnlinkClicked = { },
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
            onProgressChanged = { _, _ -> },
            onDeleteClicked = { },
            onUnlinkClicked = { },
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
            onProgressChanged = { _, _ -> },
            onDeleteClicked = { },
            onUnlinkClicked = { },
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
            onProgressChanged = { _, _ -> },
            onDeleteClicked = { },
            onUnlinkClicked = { },
            isEditMode = true,
            sliderIncrement = 10,
            modifier = Modifier.fillMaxWidth()
        )
    }
}