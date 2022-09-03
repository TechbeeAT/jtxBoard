/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.ui.compose.elements.HeadlineWithIcon
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()


    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            HeadlineWithIcon(icon = Icons.Outlined.WorkOutline, iconDesc = headline, text = headline)

            AnimatedVisibility(resources.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    resources.asReversed().forEach { resource ->
                        InputChip(
                            onClick = { },
                            label = { Text(resource.text?:"") },
                            trailingIcon = {
                                if (isEditMode)
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

            AnimatedVisibility(newResource.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {

                    if(resources.none { existing -> existing.text == newResource }) {
                        InputChip(
                            onClick = {
                                resources = resources.plus(Resource(text = newResource))
                                onResourcesUpdated(resources)
                                //newResource.value = ""
                            },
                            label = { Text(newResource) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.NewLabel,
                                    stringResource(id = R.string.add)
                                )
                            },
                            selected = false,
                            modifier = Modifier.onPlaced {
                                coroutineScope.launch {
                                    bringIntoViewRequester.bringIntoView()
                                }
                            }
                        )
                    }

                    allResources.filter { all -> all.lowercase().contains(newResource.lowercase()) && resources.none { existing -> existing.text?.lowercase() == all.lowercase() }}
                        .forEach { resource ->
                            InputChip(
                                onClick = {
                                    resources = resources.plus(Resource(text = resource))
                                    onResourcesUpdated(resources)
                                    //newResource.value = ""
                                },
                                label = { Text(resource) },
                                leadingIcon = {
                                        Icon(
                                            Icons.Outlined.NewLabel,
                                            stringResource(id = R.string.add)
                                        )
                                },
                                selected = false
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
                                IconButton(onClick = { newResource = "" }) {
                                    Icon(
                                        Icons.Outlined.Close,
                                        stringResource(id = R.string.delete)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(bringIntoViewRequester),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if(newResource.isNotEmpty() && resources.none { existing -> existing.text == newResource } ) {
                                resources = resources.plus(Resource(text = newResource))
                            }
                            newResource = ""
                        })
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