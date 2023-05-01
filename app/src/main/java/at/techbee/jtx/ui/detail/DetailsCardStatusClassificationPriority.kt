/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentLate
import androidx.compose.material.icons.outlined.GppMaybe
import androidx.compose.material.icons.outlined.PublishedWithChanges
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.StoredStatus


@Composable
fun DetailsCardStatusClassificationPriority(
    icalObject: ICalObject,
    isEditMode: Boolean,
    enableStatus: Boolean,
    enableClassification: Boolean,
    enablePriority: Boolean,
    allowStatusChange: Boolean,
    storedStatuses: List<StoredStatus>,
    onStatusChanged: (String?) -> Unit,
    onClassificationChanged: (String?) -> Unit,
    onPriorityChanged: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {

    // we don't show the block in view mode if all three values are null
    if(!isEditMode && icalObject.status == null && icalObject.classification == null && icalObject.priority == null)
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

            if(!isEditMode && !icalObject.status.isNullOrEmpty()) {
                ElevatedAssistChip(
                    label = {
                        Text(Status.values().find { it.status == icalObject.status }?.stringResource?.let { stringResource(id = it) }?: icalObject.status ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis)
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
            } else if(isEditMode && (enableStatus || !icalObject.status.isNullOrEmpty())) {
                AssistChip(
                    enabled = allowStatusChange,
                    label = {
                        Text(Status.values().find { it.status == icalObject.status }?.stringResource?.let { stringResource(id = it) }?: icalObject.status ?: "")

                        DropdownMenu(
                            expanded = statusMenuExpanded,
                            onDismissRequest = { statusMenuExpanded = false }
                        ) {

                            Status.valuesFor(icalObject.getModuleFromString()).forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = status.stringResource)) },
                                    onClick = {
                                        icalObject.status = status.status
                                        statusMenuExpanded = false
                                        onStatusChanged(status.status)
                                    }
                                )
                            }
                            storedStatuses
                                .filter { Status.valuesFor(icalObject.getModuleFromString()).none { default -> stringResource(id = default.stringResource) == it.status } }
                                .filter { it.module == icalObject.module }
                                .forEach { storedStatus ->
                                    DropdownMenuItem(
                                        text = { Text(storedStatus.status) },
                                        onClick = {
                                            icalObject.status = storedStatus.status
                                            statusMenuExpanded = false
                                            onStatusChanged(storedStatus.status)
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


            if(!isEditMode && !icalObject.classification.isNullOrEmpty()) {
                ElevatedAssistChip(
                    label = {
                        Text( Classification.values().find { it.classification == icalObject.classification}?.stringResource?.let { stringResource(id = it)}?: icalObject.classification ?:"", maxLines = 1, overflow = TextOverflow.Ellipsis)
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
            } else if(isEditMode && (enableClassification || !icalObject.classification.isNullOrEmpty())) {
                AssistChip(
                    label = {
                        Text(
                            Classification.values().find { it.classification == icalObject.classification }?.stringResource?.let { stringResource(id = it) }?: icalObject.classification ?: "",
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
                                        icalObject.classification = clazzification.classification
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

                if(!isEditMode && icalObject.priority in 1..9) {
                    ElevatedAssistChip(
                        label = {
                            Text(
                                if (icalObject.priority in priorityStrings.indices)
                                    stringArrayResource(id = R.array.priority)[icalObject.priority?:0]
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
                } else if(isEditMode && (enablePriority || icalObject.priority in 1..9)) {
                    AssistChip(
                        label = {
                            Text(
                                if (icalObject.priority in priorityStrings.indices)
                                    stringArrayResource(id = R.array.priority)[icalObject.priority?:0]
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
                                            icalObject.priority = index
                                            priorityMenuExpanded = false
                                            onPriorityChanged(icalObject.priority)
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
            allowStatusChange = true,
            storedStatuses = emptyList(),
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
            allowStatusChange = true,
            storedStatuses = emptyList(),
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
            allowStatusChange = false,
            storedStatuses = emptyList(),
            onStatusChanged = { },
            onClassificationChanged = { },
            onPriorityChanged = { }
        )
    }
}