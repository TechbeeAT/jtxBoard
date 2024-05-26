/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.locals.StoredResource
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.ui.theme.getContrastSurfaceColorFor


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditResourcesDialog(
    initialResources: List<Resource>,
    allResources: List<String>,
    storedResources: List<StoredResource>,
    onResourcesUpdated: (List<Resource>) -> Unit,
    onDismiss: () -> Unit
) {

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }


    val currentResources = remember { mutableStateListOf<Resource>().apply { addAll(initialResources) }}
    var newResource by rememberSaveable { mutableStateOf("") }

    val mergedResources = mutableListOf<StoredResource>()
    mergedResources.addAll(storedResources)
    allResources.forEach { resource -> if(mergedResources.none { it.resource == resource }) mergedResources.add(StoredResource(resource, null)) }

    fun addResource() {
        if (newResource.isNotEmpty() && currentResources.none { existing -> existing.text == newResource }) {
            val caseSensitiveResource =
                allResources.firstOrNull { it == newResource }
                    ?: allResources.firstOrNull { it.lowercase() == newResource.lowercase() }
                    ?: newResource
            currentResources.add(Resource(text = caseSensitiveResource))
        }
        newResource = ""
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.WorkOutline, null)
                Text(stringResource(id = R.string.resources))
            }
        },
        text = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            AnimatedVisibility(currentResources.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    currentResources.forEach { resource ->
                            InputChip(
                                onClick = { },
                                label = { Text(resource.text?:"") },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            currentResources.remove(resource)
                                        },
                                        content = {
                                            Icon(
                                                Icons.Outlined.Close,
                                                stringResource(id = R.string.delete)
                                            )
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                colors = StoredResource.getColorForResource(resource.text, storedResources)?.let { InputChipDefaults.inputChipColors(
                                    containerColor = it,
                                    labelColor = MaterialTheme.colorScheme.getContrastSurfaceColorFor(it)
                                ) }?: InputChipDefaults.inputChipColors(),
                                selected = false
                            )

                    }
                }
            }

            val resourcesToSelectFiltered = mergedResources.filter { all ->
                all.resource.lowercase().contains(newResource.lowercase())
                        && currentResources.none { existing -> existing.text?.lowercase() == all.resource.lowercase() }
            }
            AnimatedVisibility(resourcesToSelectFiltered.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    resourcesToSelectFiltered.filterIndexed { index, _ -> index < 10 }.forEach { resource ->
                        InputChip(
                            onClick = {
                                currentResources.add(Resource(text = resource.resource))
                                newResource = ""
                            },
                            label = { Text(resource.resource) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.WorkOutline,
                                    stringResource(id = R.string.add)
                                )
                            },
                            selected = false,
                            colors = resource.color?.let { InputChipDefaults.inputChipColors(
                                containerColor = Color(it),
                                labelColor = MaterialTheme.colorScheme.getContrastSurfaceColorFor(Color(it))
                            ) }?: InputChipDefaults.inputChipColors(),
                            modifier = Modifier.alpha(0.4f)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = newResource,
                trailingIcon = {
                    AnimatedVisibility(newResource.isNotEmpty()) {
                        IconButton(onClick = {
                            addResource()
                        }) {
                            Icon(
                                Icons.Outlined.WorkOutline,
                                stringResource(id = R.string.add)
                            )
                        }
                    }
                },
                singleLine = true,
                label = { Text(stringResource(id = R.string.resource)) },
                onValueChange = { newResourceName -> newResource = newResourceName },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                isError = newResource.isNotEmpty(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    addResource()
                })
            )
        }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if(newResource.isNotEmpty())
                        addResource()
                    onResourcesUpdated(currentResources)
                    onDismiss()
                },
            ) {
                Text(stringResource(id = R.string.save))
            }

        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.cancel))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun EditResourcesDialog_Preview() {
    MaterialTheme {
        EditResourcesDialog(
            initialResources = listOf(Resource(text = "asdf")),
            allResources = listOf("res1", "res2", "Whatever"),
            storedResources = listOf(StoredResource("res2", Color.Green.toArgb())),
            onResourcesUpdated = { }
        ) {

        }
    }
}

