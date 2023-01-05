/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R


enum class UpdateEntriesDialogMode { CATEGORIES, RESOURCES }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun UpdateEntriesDialog(
    allCategoriesLive: LiveData<List<String>>,
    allResourcesLive: LiveData<List<String>>,
    //currentCategories: List<String>,
    //currentResources: List<String>
    //current: ICalCollection,
    //onCollectionChanged: (ICalCollection) -> Unit,
    onCategoriesAdded: (List<String>) -> Unit,
    onResourcesAdded: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {

    val allCategories by allCategoriesLive.observeAsState(emptyList())
    val allResources by allResourcesLive.observeAsState(emptyList())

    val addedCategories = remember { mutableStateListOf<String>() }
    //val removedCategories = remember { mutableStateListOf<String>() }
    val addedResources = remember { mutableStateListOf<String>() }
    //val removedResources = remember { mutableStateListOf<String>() }

    var updateEntriesDialogMode by remember { mutableStateOf(UpdateEntriesDialogMode.CATEGORIES) }


    AlertDialog(
        //properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.categories))  },
        text = {
            Column(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    FilterChip(
                        selected = updateEntriesDialogMode == UpdateEntriesDialogMode.CATEGORIES,
                        onClick = { updateEntriesDialogMode = UpdateEntriesDialogMode.CATEGORIES },
                        label =  { Text(stringResource(id = R.string.categories)) }
                    )
                    FilterChip(
                        selected = updateEntriesDialogMode == UpdateEntriesDialogMode.RESOURCES,
                        onClick = { updateEntriesDialogMode = UpdateEntriesDialogMode.RESOURCES },
                        label =  { Text(stringResource(id = R.string.resources)) }
                    )
                }

                AnimatedVisibility(visible = updateEntriesDialogMode == UpdateEntriesDialogMode.CATEGORIES) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth()
                    )
                    {
                        items(allCategories) {category ->
                            InputChip(
                                onClick = {
                                    if(addedCategories.contains(category))
                                        addedCategories.remove(category)
                                    else
                                        addedCategories.add(category)
                                },
                                label = { Text(category) },
                                leadingIcon = { Icon(Icons.Outlined.NewLabel, stringResource(id = R.string.add)) },
                                selected = false,
                                modifier = Modifier
                                    .alpha(if(addedCategories.contains(category)) 1f else 0.4f)
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = updateEntriesDialogMode == UpdateEntriesDialogMode.RESOURCES) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth()
                    )
                    {
                        items(allResources.sortedBy { addedResources.contains(it) }) { resource ->
                            InputChip(
                                onClick = {
                                    if(addedResources.contains(resource))
                                        addedResources.remove(resource)
                                    else
                                        addedResources.add(resource)
                                },
                                label = { Text(resource) },
                                leadingIcon = { Icon(Icons.Outlined.WorkOutline, stringResource(id = R.string.add)) },
                                selected = false,
                                modifier = Modifier.alpha(if(addedResources.contains(resource)) 1f else 0.4f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when(updateEntriesDialogMode) {
                        UpdateEntriesDialogMode.CATEGORIES -> onCategoriesAdded(addedCategories)
                        UpdateEntriesDialogMode.RESOURCES -> onResourcesAdded(addedResources)
                    }
                    onDismiss()
                }
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
        }
    )
}

@Preview(showBackground = true)
@Composable
fun UpdateEntriesDialog_Preview() {
    MaterialTheme {

        UpdateEntriesDialog(
            allCategoriesLive = MutableLiveData(listOf("cat1", "Hello")),
            allResourcesLive = MutableLiveData(listOf("1234", "aaa")),
            onCategoriesAdded = { },
            onResourcesAdded = { },
            onDismiss = { }
        )
    }
}
