/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import android.media.MediaPlayer
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LabelOff
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.WorkOff
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
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
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredResource
import at.techbee.jtx.database.locals.StoredStatus
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.list.ListCardGrid


enum class UpdateEntriesDialogMode(@StringRes val stringResource: Int) {
    CATEGORIES(R.string.categories),
    RESOURCES(R.string.resources),
    STATUS(R.string.status),
    CLASSIFICATION(R.string.classification),
    PRIORITY(R.string.priority),
    COLLECTION(R.string.collection),
    LINK_TO_PARENT(R.string.link_to_parent)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UpdateEntriesDialog(
    module: Module,
    allCategoriesLive: LiveData<List<String>>,
    allResourcesLive: LiveData<List<String>>,
    allCollectionsLive: LiveData<List<ICalCollection>>,
    selectFromAllListLive: LiveData<List<ICal4ListRel>>,
    storedCategoriesLive: LiveData<List<StoredCategory>>,
    storedResourcesLive: LiveData<List<StoredResource>>,
    storedStatusesLive: LiveData<List<StoredStatus>>,
    player: MediaPlayer?,
    onSelectFromAllListSearchTextUpdated: (String) -> Unit,
    //currentCategories: List<String>,
    //currentResources: List<String>
    //current: ICalCollection,
    //onCollectionChanged: (ICalCollection) -> Unit,
    onCategoriesChanged: (addedCategories: List<String>, removedCategories: List<String>) -> Unit,
    onResourcesChanged: (addedResources: List<String>, removedResources: List<String>) -> Unit,
    onStatusChanged: (String) -> Unit,
    onClassificationChanged: (Classification) -> Unit,
    onPriorityChanged: (Int?) -> Unit,
    onCollectionChanged: (ICalCollection) -> Unit,
    onParentAdded: (parent: ICal4List) -> Unit,
    onDismiss: () -> Unit
) {

    val allCategories by allCategoriesLive.observeAsState(emptyList())
    val allResources by allResourcesLive.observeAsState(emptyList())
    val allCollections by allCollectionsLive.observeAsState(emptyList())
    val selectFromAllList by selectFromAllListLive.observeAsState(emptyList())
    val storedCategories by storedCategoriesLive.observeAsState(emptyList())
    val storedResources by storedResourcesLive.observeAsState(emptyList())
    val storedStatuses by storedStatusesLive.observeAsState(emptyList())

    val addedCategories = remember { mutableStateListOf<String>() }
    val removedCategories = remember { mutableStateListOf<String>() }
    val addedResources = remember { mutableStateListOf<String>() }
    val removedResources = remember { mutableStateListOf<String>() }
    var newStatus by remember { mutableStateOf<String?>(null) }
    var newClassification by remember { mutableStateOf<Classification?>(null) }
    var newPriority by remember { mutableStateOf<Int?>(null) }
    var newCollection by remember { mutableStateOf<ICalCollection?>(null) }
    var selectFromAllListSearchText by remember { mutableStateOf("") }
    var selectFromAllListSelectedEntry by remember { mutableStateOf<ICal4List?>(null) }

    var updateEntriesDialogMode by remember { mutableStateOf(UpdateEntriesDialogMode.CATEGORIES) }
    var selectFromAllListMaxEntriesShown by remember { mutableStateOf(10) }


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

                Divider(modifier = Modifier.padding(8.dp))

                AnimatedVisibility(visible = updateEntriesDialogMode == UpdateEntriesDialogMode.CATEGORIES) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth()
                    )
                    {

                        Status.valuesFor(module).forEach { status ->
                            InputChip(
                                onClick = { newStatus = status.status?:status.name },
                                label = { Text(stringResource(id = status.stringResource)) },
                                selected = newStatus == status.status || newStatus ==status.name,
                            )
                        }
                        storedStatuses
                            .filter { Status.valuesFor(module).none { default -> stringResource(id = default.stringResource) == it.status } }
                            .filter { it.module == module.name }
                            .forEach { storedStatus ->
                                InputChip(
                                    onClick = { newStatus = storedStatus.status },
                                    label = { Text(storedStatus.status) },
                                    selected = newStatus == storedStatus.status,
                                )
                            }
                    }
                }

                AnimatedVisibility(visible = updateEntriesDialogMode == UpdateEntriesDialogMode.CLASSIFICATION) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
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

                AnimatedVisibility(visible = updateEntriesDialogMode == UpdateEntriesDialogMode.COLLECTION) {
                    Text(
                        text = stringResource(id = R.string.list_update_entries_dialog_attention_collection_update),
                        style = MaterialTheme.typography.labelMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                AnimatedVisibility(visible = updateEntriesDialogMode == UpdateEntriesDialogMode.LINK_TO_PARENT) {
                    Text(
                        text = stringResource(R.string.list_update_entries_dialog_new_parent_info),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                AnimatedVisibility(visible = updateEntriesDialogMode == UpdateEntriesDialogMode.LINK_TO_PARENT) {
                    OutlinedTextField(
                        value = selectFromAllListSearchText,
                        onValueChange = {
                            selectFromAllListSearchText = it
                            onSelectFromAllListSearchTextUpdated(it)
                        },
                        label = { Text(stringResource(R.string.search)) },
                        trailingIcon = {
                            AnimatedVisibility(selectFromAllListSearchText.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        selectFromAllListSearchText = ""
                                        onSelectFromAllListSearchTextUpdated("")
                                    }
                                ) {
                                    Icon(Icons.Outlined.Close, stringResource(R.string.delete))
                                }
                            }
                        }
                    )
                }

                AnimatedVisibility(selectFromAllList.isNotEmpty() && updateEntriesDialogMode == UpdateEntriesDialogMode.LINK_TO_PARENT) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        selectFromAllList.forEachIndexed { index, entry ->
                            if(index > selectFromAllListMaxEntriesShown)
                                return@forEachIndexed
                            ListCardGrid(
                                iCalObject = entry.iCal4List,
                                categories = entry.categories,
                                resources = entry.resources,
                                storedCategories = storedCategories,
                                storedResources = storedResources,
                                storedStatuses = storedStatuses,
                                selected = entry.iCal4List == selectFromAllListSelectedEntry,
                                progressUpdateDisabled = true,
                                player = player,
                                onProgressChanged = {_, _ -> },
                                modifier = Modifier.clickable {
                                    selectFromAllListSelectedEntry = if(entry.iCal4List == selectFromAllListSelectedEntry)
                                        null
                                    else
                                        entry.iCal4List
                                }
                            )
                        }
                    }
                }

                AnimatedVisibility(selectFromAllList.isNotEmpty() && selectFromAllListMaxEntriesShown < selectFromAllList.size) {
                    TextButton(onClick = { selectFromAllListMaxEntriesShown += 10 }) {
                        Text(stringResource(R.string.more))
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
                        UpdateEntriesDialogMode.STATUS -> newStatus?.let { onStatusChanged(it) }
                        UpdateEntriesDialogMode.CLASSIFICATION -> newClassification?.let { onClassificationChanged(it) }
                        UpdateEntriesDialogMode.PRIORITY -> onPriorityChanged(if(newPriority == 0) null else newPriority)
                        UpdateEntriesDialogMode.COLLECTION -> newCollection?.let { onCollectionChanged(it) }
                        UpdateEntriesDialogMode.LINK_TO_PARENT -> selectFromAllListSelectedEntry?.let { onParentAdded(it) }
                    }
                    onDismiss()
                },
                enabled = when(updateEntriesDialogMode) {
                    UpdateEntriesDialogMode.CATEGORIES -> addedCategories.isNotEmpty() || removedCategories.isNotEmpty()
                    UpdateEntriesDialogMode.RESOURCES -> addedResources.isNotEmpty() || removedResources.isNotEmpty()
                    UpdateEntriesDialogMode.STATUS -> newStatus != null
                    UpdateEntriesDialogMode.CLASSIFICATION -> newClassification != null
                    UpdateEntriesDialogMode.PRIORITY -> true
                    UpdateEntriesDialogMode.COLLECTION -> newCollection != null
                    UpdateEntriesDialogMode.LINK_TO_PARENT -> selectFromAllListSelectedEntry != null
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
            selectFromAllListLive = MutableLiveData(listOf()),
            storedCategoriesLive = MutableLiveData(listOf(StoredCategory("cat1", Color.Green.toArgb()))),
            storedResourcesLive = MutableLiveData(listOf(StoredResource("1234", Color.Green.toArgb()))),
            storedStatusesLive = MutableLiveData(listOf(StoredStatus("individual", Module.JOURNAL.name, Color.Green.toArgb()))),
            player = null,
            onSelectFromAllListSearchTextUpdated = { },
            onCategoriesChanged = { _, _ -> },
            onResourcesChanged = { _, _ -> },
            onStatusChanged = {},
            onClassificationChanged = {},
            onPriorityChanged = {},
            onCollectionChanged = {},
            onParentAdded = {},
            onDismiss = { }
        )
    }
}
