/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsCardResources(
    initialResources: List<Resource>,
    isEditMode: Boolean,
    allResources: List<String>,
    onResourcesUpdated: (List<Resource>) -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.resources)
    var resources by remember { mutableStateOf(initialResources) }
    var newResource by remember { mutableStateOf("") }


    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            HeadlineWithIcon(icon = Icons.Outlined.WorkOutline, iconDesc = headline, text = headline)

            AnimatedVisibility(resources.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    items(resources.asReversed()) { resource ->
                        if(!isEditMode) {
                            ElevatedAssistChip(
                                onClick = { },
                                label = { Text(resource.text ?: "") }
                            )
                        } else {
                            InputChip(
                                onClick = { },
                                label = { Text(resource.text ?: "") },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            resources = resources.filter { it != resource }
                                            onResourcesUpdated(resources)
                                        },
                                        content = { Icon(Icons.Outlined.Close, stringResource(id = R.string.delete)) },
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                selected = false
                            )
                        }
                    }
                }
            }

            val resourcesToSelect = allResources.filter { all -> all.lowercase().contains(newResource.lowercase()) && resources.none { existing -> existing.text?.lowercase() == all.lowercase() }}

            AnimatedVisibility(resourcesToSelect.isNotEmpty() && isEditMode) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(resourcesToSelect) { resource ->
                        InputChip(
                            onClick = {
                                resources = resources.plus(Resource(text = resource))
                                onResourcesUpdated(resources)
                                newResource = ""
                            },
                            label = { Text(resource) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.NewLabel,
                                    stringResource(id = R.string.add)
                                )
                            },
                            selected = false,
                            modifier = Modifier.alpha(0.4f)
                        )
                    }
                }
            }


            Crossfade(isEditMode) {
                if (it) {

                    OutlinedTextField(
                        value = newResource,
                        leadingIcon = { Icon(Icons.Outlined.WorkOutline, headline) },
                        trailingIcon = {
                            if (newResource.isNotEmpty()) {
                                IconButton(onClick = {
                                    resources = resources.plus(Resource(text = newResource))
                                    onResourcesUpdated(resources)
                                    newResource = ""
                                }) {
                                    Icon(
                                        Icons.Outlined.NewLabel,
                                        stringResource(id = R.string.add)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        label = { Text(headline) },
                        onValueChange = { newResourceName ->
                            newResource = newResourceName
                        },
                        colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
                        isError = newResource.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if(newResource.isNotEmpty() && resources.none { existing -> existing.text == newResource } ) {
                                resources = resources.plus(Resource(text = newResource))
                            }
                            newResource = ""
                        }),
                        textStyle = TextStyle(textDirection = TextDirection.Content)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardResources_Preview() {
    MaterialTheme {
        DetailsCardResources(
            initialResources = listOf(Resource(text = "asdf")),
            isEditMode = false,
            allResources = listOf("projector", "overhead-thingy", "Whatever"),
            onResourcesUpdated = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardResources_Preview_edit() {
    MaterialTheme {
        DetailsCardResources(
            initialResources = listOf(Resource(text = "asdf")),
            isEditMode = true,
            allResources = listOf("projector", "overhead-thingy", "Whatever"),
            onResourcesUpdated = { }
        )
    }
}