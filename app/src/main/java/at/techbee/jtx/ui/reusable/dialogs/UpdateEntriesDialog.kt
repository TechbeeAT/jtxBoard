/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LabelOff
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.WorkOff
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow


enum class UpdateEntriesDialogMode(@StringRes val stringResource: Int) {
    CATEGORIES(R.string.categories),
    RESOURCES(R.string.resources),
    STATUS(R.string.status),
    CLASSIFICATION(R.string.classification),
    PRIORITY(R.string.priority),
    COLLECTION(R.string.collection)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun UpdateEntriesDialog(
    module: Module,
    allCategoriesLive: LiveData<List<String>>,
    allResourcesLive: LiveData<List<String>>,
    allCollectionsLive: LiveData<List<ICalCollection>>,
    //currentCategories: List<String>,
    //currentResources: List<String>
    //current: ICalCollection,
    //onCollectionChanged: (ICalCollection) -> Unit,
    onCategoriesChanged: (addedCategories: List<String>, removedCategories: List<String>) -> Unit,
    onResourcesChanged: (addedResources: List<String>, removedResources: List<String>) -> Unit,
    onStatusChanged: (Status) -> Unit,
    onClassificationChanged: (Classification) -> Unit,
    onPriorityChanged: (Int?) -> Unit,
    onCollectionChanged: (ICalCollection) -> Unit,
    onDismiss: () -> Unit
) {

    val allCategories by allCategoriesLive.observeAsState(emptyList())
    val allResources by allResourcesLive.observeAsState(emptyList())
    val allCollections by allCollectionsLive.observeAsState(emptyList())

    val addedCategories = remember { mutableStateListOf<String>() }
    val removedCategories = remember { mutableStateListOf<String>() }
    val addedResources = remember { mutableStateListOf<String>() }
    val removedResources = remember { mutableStateListOf<String>() }
    var newStatus by remember { mutableStateOf(Status.NO_STATUS) }
    var newClassification by remember { mutableStateOf(Classification.NO_CLASSIFICATION) }
    var newPriority by remember { mutableStateOf<Int?>(null) }
    var newCollection by remember { mutableStateOf<ICalCollection?>(null) }

    var updateEntriesDialogMode by remember { mutableStateOf(UpdateEntriesDialogMode.CATEGORIES) }


    AlertDialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(stringResource(R.string.list_update_entries_dialog_title)) },
        text = {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
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
                        if(module != Module.TODO && it == UpdateEntriesDialogMode.PRIORITY)
                            return@forEach

                        FilterChip(
                            selected = updateEntriesDialogMode == it,
                            onClick = {
                                if (updateEntriesDialogMode != it) {
                                    addedCategories.clear()
                                    removedCategories.clear()
                                    addedResources.clear()
                                    removedResources.clear()
                                    updateEntriesDialogMode = it
                                }
                            },
                            label = { Text(stringResource(id = it.stringResource)) }
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
                        allCategories.forEach { category ->
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
                                    if (removedCategories.contains(category))
                                        Icon(Icons.Outlined.LabelOff, stringResource(id = R.string.delete), tint = MaterialTheme.colorScheme.error)
                                    else
                                        Icon(
                                            Icons.Outlined.NewLabel,
                                            stringResource(id = R.string.add),
                                            tint = if (addedCategories.contains(category)) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                        )
                                },
                                selected = false,
                                modifier = Modifier
                                    .alpha(if (addedCategories.contains(category) || removedCategories.contains(category)) 1f else 0.4f)
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = updateEntriesDialogMode == UpdateEntriesDialogMode.RESOURCES) {
                    FlowRow(
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
                                        Icon(
                                            Icons.Outlined.WorkOutline,
                                            stringResource(id = R.string.add),
                                            tint = if (addedResources.contains(resource)) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                        )
                                },
                                selected = false,
                                modifier = Modifier.alpha(if (addedResources.contains(resource) || removedResources.contains(resource)) 1f else 0.4f)
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = updateEntriesDialogMode == UpdateEntriesDialogMode.STATUS) {
                    FlowRow(
                        mainAxisSpacing = 8.dp,
                        mainAxisAlignment = FlowMainAxisAlignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    {

                        Status.valuesFor(module).forEach { status ->
                            InputChip(
                                onClick = { newStatus = status },
                                label = { Text(stringResource(id = status.stringResource)) },
                                selected = status == newStatus,
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = updateEntriesDialogMode == UpdateEntriesDialogMode.CLASSIFICATION) {
                    FlowRow(
                        mainAxisSpacing = 8.dp,
                        mainAxisAlignment = FlowMainAxisAlignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    {

                        Classification.values().forEach { classification ->
                            InputChip(
                                onClick = { newClassification = classification },
                                label = { Text(stringResource(id = classification.stringResource)) },
                                selected = classification == newClassification,
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = updateEntriesDialogMode == UpdateEntriesDialogMode.PRIORITY) {
                    FlowRow(
                        mainAxisSpacing = 8.dp,
                        mainAxisAlignment = FlowMainAxisAlignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    {

                        stringArrayResource(id = R.array.priority).forEachIndexed { index, prio ->
                            InputChip(
                                onClick = { newPriority = index },
                                label = { Text(prio) },
                                selected = newPriority == index,
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = updateEntriesDialogMode == UpdateEntriesDialogMode.COLLECTION) {
                    FlowRow(
                        mainAxisSpacing = 8.dp,
                        mainAxisAlignment = FlowMainAxisAlignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    {
                        allCollections.forEach { collection ->
                            InputChip(
                                onClick = { newCollection = collection },
                                label = { Text(collection.displayName ?: collection.collectionId.toString()) },
                                selected = newCollection == collection,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (updateEntriesDialogMode) {
                        UpdateEntriesDialogMode.CATEGORIES -> onCategoriesChanged(addedCategories, removedCategories)
                        UpdateEntriesDialogMode.RESOURCES -> onResourcesChanged(addedResources, removedResources)
                        UpdateEntriesDialogMode.STATUS -> onStatusChanged(newStatus)
                        UpdateEntriesDialogMode.CLASSIFICATION -> onClassificationChanged(newClassification)
                        UpdateEntriesDialogMode.PRIORITY -> onPriorityChanged(if(newPriority == 0) null else newPriority)
                        UpdateEntriesDialogMode.COLLECTION -> newCollection?.let { onCollectionChanged(it) }
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
            module = Module.JOURNAL,
            allCategoriesLive = MutableLiveData(listOf("cat1", "Hello")),
            allResourcesLive = MutableLiveData(listOf("1234", "aaa")),
            allCollectionsLive = MutableLiveData(listOf(ICalCollection())),
            onCategoriesChanged = { _, _ -> },
            onResourcesChanged = { _, _ -> },
            onStatusChanged = {},
            onClassificationChanged = {},
            onPriorityChanged = {},
            onCollectionChanged = {},
            onDismiss = { }
        )
    }
}
