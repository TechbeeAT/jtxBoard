/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LabelOff
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.WorkOff
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow


enum class UpdateEntriesDialogMode(val stringResource: Int) {
    CATEGORIES(R.string.categories),
    RESOURCES(R.string.resources)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateEntriesDialog(
    allCategoriesLive: LiveData<List<String>>,
    allResourcesLive: LiveData<List<String>>,
    //currentCategories: List<String>,
    //currentResources: List<String>
    //current: ICalCollection,
    //onCollectionChanged: (ICalCollection) -> Unit,
    onCategoriesChanged: (addedCategories: List<String>, removedCategories: List<String>) -> Unit,
    onResourcesChanged: (addedResources: List<String>, removedResources: List<String>) -> Unit,
    onDismiss: () -> Unit
) {

    val allCategories by allCategoriesLive.observeAsState(emptyList())
    val allResources by allResourcesLive.observeAsState(emptyList())

    val addedCategories = remember { mutableStateListOf<String>() }
    val removedCategories = remember { mutableStateListOf<String>() }
    val addedResources = remember { mutableStateListOf<String>() }
    val removedResources = remember { mutableStateListOf<String>() }

    var updateEntriesDialogMode by remember { mutableStateOf(UpdateEntriesDialogMode.CATEGORIES) }


    AlertDialog(
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
                    UpdateEntriesDialogMode.values().forEach {
                        FilterChip(
                            selected = updateEntriesDialogMode == it,
                            onClick = {
                                updateEntriesDialogMode = it
                            },
                            label =  { Text(stringResource(id = it.stringResource)) }
                        )
                    }
                }

                AnimatedVisibility(visible = updateEntriesDialogMode == UpdateEntriesDialogMode.CATEGORIES) {
                    FlowRow(
                        //horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        mainAxisSpacing = 8.dp,
                        mainAxisAlignment = FlowMainAxisAlignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    {
                        allCategories.forEach {category ->
                            InputChip(
                                onClick = {
                                    when {
                                        addedCategories.contains(category) -> {
                                            addedCategories.remove(category)
                                            removedCategories.add(category)
                                        }
                                        removedCategories.contains(category) -> removedCategories.remove(category)
                                        else -> addedCategories.add(category)
                                    }
                                },
                                label = { Text(category) },
                                leadingIcon = {
                                    if(removedCategories.contains(category))
                                        Icon(Icons.Outlined.LabelOff, stringResource(id = R.string.delete), tint = MaterialTheme.colorScheme.error)
                                    else
                                        Icon(Icons.Outlined.NewLabel, stringResource(id = R.string.add), tint = if(addedCategories.contains(category)) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                                },
                                selected = false,
                                modifier = Modifier
                                    .alpha(if(addedCategories.contains(category) || removedCategories.contains(category)) 1f else 0.4f)
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = updateEntriesDialogMode == UpdateEntriesDialogMode.RESOURCES) {
                    FlowRow(
                        //horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        mainAxisSpacing = 8.dp,
                        mainAxisAlignment = FlowMainAxisAlignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    {
                        allResources.forEach { resource ->
                            InputChip(
                                onClick = {
                                    when {
                                        addedResources.contains(resource) -> {
                                            addedResources.remove(resource)
                                            removedResources.add(resource)
                                        }
                                        removedResources.contains(resource) -> removedResources.remove(resource)
                                        else -> addedResources.add(resource)
                                    }
                                },
                                label = { Text(resource) },
                                leadingIcon = {
                                    if (removedResources.contains(resource))
                                        Icon(Icons.Outlined.WorkOff, stringResource(id = R.string.delete), tint = MaterialTheme.colorScheme.error)
                                    else
                                        Icon(Icons.Outlined.WorkOutline, stringResource(id = R.string.add), tint = if(addedResources.contains(resource)) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                                },
                                selected = false,
                                modifier = Modifier.alpha(if(addedResources.contains(resource) || removedResources.contains(resource)) 1f else 0.4f)
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
                        UpdateEntriesDialogMode.CATEGORIES -> onCategoriesChanged(addedCategories, removedCategories)
                        UpdateEntriesDialogMode.RESOURCES -> onResourcesChanged(addedResources, removedResources)
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
            onCategoriesChanged = { _, _ ->  },
            onResourcesChanged = {  _, _ -> },
            onDismiss = { }
        )
    }
}
