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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.locals.StoredListSettingData
import at.techbee.jtx.database.locals.StoredResource
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.ui.theme.getContrastSurfaceColorFor


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailsCardResources(
    resources: SnapshotStateList<Resource>,
    isEditMode: Boolean,
    allResourcesLive: LiveData<List<String>>,
    storedResources: List<StoredResource>,
    onResourcesUpdated: () -> Unit,
    onGoToFilteredList: (StoredListSettingData) -> Unit,
    modifier: Modifier = Modifier
) {

    val allResources by allResourcesLive.observeAsState(emptyList())

    val headline = stringResource(id = R.string.resources)
    var newResource by rememberSaveable { mutableStateOf("") }

    val mergedResources = mutableListOf<StoredResource>()
    mergedResources.addAll(storedResources)
    allResources.forEach { resource -> if(mergedResources.none { it.resource == resource }) mergedResources.add(StoredResource(resource, null)) }

    fun addResource() {
        if (newResource.isNotEmpty() && resources.none { existing -> existing.text == newResource }) {
            val caseSensitiveResource =
                allResources.firstOrNull { it == newResource }
                    ?: allResources.firstOrNull { it.lowercase() == newResource.lowercase() }
                    ?: newResource
            resources.add(Resource(text = caseSensitiveResource))
            onResourcesUpdated()
        }
        newResource = ""
    }

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            HeadlineWithIcon(icon = Icons.Outlined.WorkOutline, iconDesc = headline, text = headline)

            AnimatedVisibility(resources.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    resources.forEach { resource ->
                        if(!isEditMode) {
                            ElevatedAssistChip(
                                onClick = { onGoToFilteredList(StoredListSettingData(searchResources = listOf(resource.text?:""))) },
                                label = { Text(resource.text ?: "") },
                                colors = StoredResource.getColorForResource(resource.text?:"", storedResources)?.let { AssistChipDefaults.elevatedAssistChipColors(
                                    containerColor = it,
                                    labelColor = MaterialTheme.colorScheme.getContrastSurfaceColorFor(it)
                                ) }?: AssistChipDefaults.elevatedAssistChipColors(),
                            )
                        } else {
                            InputChip(
                                onClick = { },
                                label = { Text(resource.text ?: "") },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            resources.remove(resource)
                                            onResourcesUpdated()
                                        },
                                        content = { Icon(Icons.Outlined.Close, stringResource(id = R.string.delete)) },
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                colors = StoredResource.getColorForResource(resource.text?:"", storedResources)?.let { InputChipDefaults.inputChipColors(
                                    containerColor = it,
                                    labelColor = MaterialTheme.colorScheme.getContrastSurfaceColorFor(it)
                                ) }?: InputChipDefaults.inputChipColors(),
                                selected = false
                            )
                        }
                    }
                }
            }

            val resourcesToSelectFiltered = mergedResources.filter { all ->
                all.resource.lowercase().contains(newResource.lowercase())
                        && resources.none { existing -> existing.text?.lowercase() == all.resource.lowercase() }
            }

            AnimatedVisibility(resourcesToSelectFiltered.isNotEmpty() && isEditMode) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(resourcesToSelectFiltered) { resource ->
                        InputChip(
                            onClick = {
                                resources.add(Resource(text = resource.resource))
                                onResourcesUpdated()
                                newResource = ""
                            },
                            label = { Text(resource.resource) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.NewLabel,
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


            Crossfade(isEditMode, label = "newResourceIsEditMode") {
                if (it) {

                    OutlinedTextField(
                        value = newResource,
                        leadingIcon = { Icon(Icons.Outlined.WorkOutline, headline) },
                        trailingIcon = {
                            if (newResource.isNotEmpty()) {
                                IconButton(onClick = {
                                    addResource()
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
                        isError = newResource.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            addResource()
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
            resources = remember { mutableStateListOf(Resource(text = "asdf")) },
            isEditMode = false,
            allResourcesLive = MutableLiveData(listOf("projector", "overhead-thingy", "Whatever")),
            storedResources = listOf(StoredResource("projector", Color.Green.toArgb())),
            onResourcesUpdated = { },
            onGoToFilteredList = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardResources_Preview_edit() {
    MaterialTheme {
        DetailsCardResources(
            resources = remember { mutableStateListOf(Resource(text = "asdf")) },
            isEditMode = true,
            allResourcesLive = MutableLiveData(listOf("projector", "overhead-thingy", "Whatever")),
            storedResources = listOf(StoredResource("projector", Color.Green.toArgb())),
            onResourcesUpdated = { },
            onGoToFilteredList = { }
        )
    }
}