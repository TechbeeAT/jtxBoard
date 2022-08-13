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
    resources: MutableState<List<Resource>>,
    isEditMode: MutableState<Boolean>,
    allResources: List<Resource>,
    onResourcesUpdated: () -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.resources)
    val newResource = remember { mutableStateOf("") }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()


    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            HeadlineWithIcon(icon = Icons.Outlined.WorkOutline, iconDesc = headline, text = headline)

            AnimatedVisibility(resources.value.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    resources.value.asReversed().forEach { resource ->
                        InputChip(
                            onClick = { },
                            label = { Text(resource.text?:"") },
                            trailingIcon = {
                                if (isEditMode.value)
                                    IconButton(
                                        onClick = { resources.value = resources.value.filter { it != resource } },
                                        content = { Icon(Icons.Outlined.Close, stringResource(id = R.string.delete)) },
                                        modifier = Modifier.size(24.dp)
                                    )
                            },
                            selected = false
                        )
                    }
                }
            }

            AnimatedVisibility(newResource.value.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {

                    if(resources.value.none { existing -> existing.text == newResource.value }) {
                        InputChip(
                            onClick = {
                                resources.value = resources.value.plus(Resource(text = newResource.value))
                                //newResource.value = ""
                            },
                            label = { Text(newResource.value) },
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

                    allResources.filter { all -> all.text?.lowercase()?.contains(newResource.value.lowercase()) == true && resources.value.none { existing -> existing.text?.lowercase() == all.text?.lowercase() }}
                        .forEach { resource ->
                            InputChip(
                                onClick = {
                                    resources.value = resources.value.plus(Resource(text = resource.text))
                                    //newResource.value = ""
                                },
                                label = { Text(resource.text?: "") },
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
                if (it.value) {

                    OutlinedTextField(
                        value = newResource.value,
                        leadingIcon = { Icon(Icons.Outlined.WorkOutline, headline) },
                        trailingIcon = {
                            if (newResource.value.isNotEmpty()) {
                                IconButton(onClick = { newResource.value = "" }) {
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
                            newResource.value = newResourceName
                            /* TODO */
                        },
                        colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(bringIntoViewRequester),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if(newResource.value.isNotEmpty() && resources.value.none { existing -> existing.text == newResource.value } )
                                resources.value = resources.value.plus(Resource(text = newResource.value))
                            newResource.value = ""
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
            resources = remember { mutableStateOf(listOf(Resource(text = "asdf"))) },
            isEditMode = remember { mutableStateOf(false) },
            allResources = listOf(Resource(text = "projector"), Resource(text = "overhead-thingy"), Resource(text = "Whatever")),
            onResourcesUpdated = { /*TODO*/ }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardResources_Preview_edit() {
    MaterialTheme {
        DetailsCardResources(
            resources = remember { mutableStateOf(listOf(Resource(text = "asdf"))) },
            isEditMode = remember { mutableStateOf(true) },
            allResources = listOf(Resource(text = "projector"), Resource(text = "overhead-thingy"), Resource(text = "Whatever")),
            onResourcesUpdated = { /*TODO*/ }
        )
    }
}