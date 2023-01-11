/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsCardStatusClassificationPriority(
    icalObject: ICalObject,
    isEditMode: Boolean,
    enableStatus: Boolean,
    enableClassification: Boolean,
    enablePriority: Boolean,
    onStatusChanged: (String?) -> Unit,
    onClassificationChanged: (String?) -> Unit,
    onPriorityChanged: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {

    var currentStatus by rememberSaveable { mutableStateOf(icalObject.status) }
    var currentClassification by rememberSaveable { mutableStateOf( icalObject.classification) }
    var currentPriority by rememberSaveable { mutableStateOf(icalObject.priority) }

    // we don't show the block in view mode if all three values are null
    if(!isEditMode && currentStatus == null && currentClassification == null && currentPriority == null)
        return


    ElevatedCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var statusMenuExpanded by remember { mutableStateOf(false) }
            var classificationMenuExpanded by remember { mutableStateOf(false) }
            var priorityMenuExpanded by remember { mutableStateOf(false) }

            if(!isEditMode && !currentStatus.isNullOrEmpty()) {
                ElevatedAssistChip(
                    label = {
                        Text(Status.values().find { it.status == currentStatus }?.stringResource?.let { stringResource(id = it) }?: currentStatus?: "", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.PublishedWithChanges,
                            stringResource(id = R.string.status)
                        )
                    },
                    onClick = { },
                    modifier = Modifier.weight(0.33f)
                )
            } else if(isEditMode && (enableStatus || !currentStatus.isNullOrEmpty())) {
                AssistChip(
                    label = {
                        Text(Status.values().find { it.status == currentStatus }?.stringResource?.let { stringResource(id = it) }?: currentStatus?: "")

                        DropdownMenu(
                            expanded = statusMenuExpanded,
                            onDismissRequest = { statusMenuExpanded = false }
                        ) {

                            Status.valuesFor(icalObject.getModuleFromString()).forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = status.stringResource)) },
                                    onClick = {
                                        currentStatus = status.status
                                        statusMenuExpanded = false
                                        onStatusChanged(status.status)
                                        //icalObject.status = status
                                    }
                                )
                            }
                        }
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.PublishedWithChanges,
                            stringResource(id = R.string.status)
                        )
                    },
                    onClick = {  statusMenuExpanded = true },
                    modifier = Modifier.weight(0.33f)
                )
            }


            if(!isEditMode && !currentClassification.isNullOrEmpty()) {
                ElevatedAssistChip(
                    label = {
                        Text( Classification.values().find { it.classification == currentClassification}?.stringResource?.let { stringResource(id = it)}?: currentClassification?:"", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.GppMaybe,
                            stringResource(id = R.string.classification)
                        )
                    },
                    onClick = { },
                    modifier = Modifier.weight(0.33f)
                )
            } else if(isEditMode && (enableClassification || !currentClassification.isNullOrEmpty())) {
                AssistChip(
                    label = {
                        Text(
                            Classification.values().find { it.classification == currentClassification }?.stringResource?.let { stringResource(id = it) }?: currentClassification?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        DropdownMenu(
                            expanded = classificationMenuExpanded,
                            onDismissRequest = { classificationMenuExpanded = false }
                        ) {

                            Classification.values().forEach { clazzification ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = clazzification.stringResource)) },
                                    onClick = {
                                        currentClassification = clazzification.classification
                                        classificationMenuExpanded = false
                                        onClassificationChanged(clazzification.classification)
                                    }
                                )
                            }
                        }
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.GppMaybe,
                            stringResource(id = R.string.classification)
                        )
                    },
                    onClick = { classificationMenuExpanded = true },
                    modifier = Modifier.weight(0.33f)
                )
            }

            val priorityStrings = stringArrayResource(id = R.array.priority)
            if (icalObject.component == Component.VTODO.name) {

                if(!isEditMode && currentPriority in 1..9) {
                    ElevatedAssistChip(
                        label = {
                            Text(
                                if (currentPriority in priorityStrings.indices)
                                    stringArrayResource(id = R.array.priority)[currentPriority?:0]
                                else
                                    stringArrayResource(id = R.array.priority)[0],
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.AssignmentLate,
                                stringResource(id = R.string.priority)
                            )
                        },
                        onClick = { },
                        modifier = Modifier.weight(0.33f)
                    )
                } else if(isEditMode && (enablePriority || currentPriority in 1..9)) {
                    AssistChip(
                        label = {
                            Text(
                                if (currentPriority in priorityStrings.indices)
                                    stringArrayResource(id = R.array.priority)[currentPriority?:0]
                                else
                                    stringArrayResource(id = R.array.priority)[0],
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            DropdownMenu(
                                expanded = priorityMenuExpanded,
                                onDismissRequest = { priorityMenuExpanded = false }
                            ) {
                                stringArrayResource(id = R.array.priority).forEachIndexed { index, prio ->
                                    DropdownMenuItem(
                                        text = { Text(prio) },
                                        onClick = {
                                            currentPriority = index
                                            priorityMenuExpanded = false
                                            onPriorityChanged(currentPriority)
                                        }
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.AssignmentLate,
                                stringResource(id = R.string.priority)
                            )
                        },
                        onClick = { priorityMenuExpanded = true },
                        modifier = Modifier.weight(0.33f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardStatusClassificationPriority_Journal_Preview() {
    MaterialTheme {
        DetailsCardStatusClassificationPriority(
            icalObject = ICalObject.createJournal(),
            isEditMode = false,
            enableStatus = false,
            enableClassification = false,
            enablePriority = false,
            onStatusChanged = { },
            onClassificationChanged = { },
            onPriorityChanged = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardStatusClassificationPriority_Todo_Preview() {
    MaterialTheme {
        DetailsCardStatusClassificationPriority(
            icalObject = ICalObject.createTodo(),
            isEditMode = true,
            enableStatus = true,
            enableClassification = true,
            enablePriority = true,
            onStatusChanged = { },
            onClassificationChanged = { },
            onPriorityChanged = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardStatusClassificationPriority_Todo_Preview2() {
    MaterialTheme {
        DetailsCardStatusClassificationPriority(
            icalObject = ICalObject.createTodo(),
            isEditMode = true,
            enableStatus = true,
            enableClassification = false,
            enablePriority = false,
            onStatusChanged = { },
            onClassificationChanged = { },
            onPriorityChanged = { }
        )
    }
}