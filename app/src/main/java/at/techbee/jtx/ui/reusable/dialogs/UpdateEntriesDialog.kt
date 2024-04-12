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
import androidx.compose.material.icons.automirrored.outlined.LabelOff
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.WorkOff
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableIntStateOf
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
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.list.ListCardGrid

const val MAX_ITEMS = 5

enum class UpdateEntriesDialogMode(@StringRes val stringResource: Int) {
    CATEGORIES(R.string.categories),
    RESOURCES(R.string.resources),
    STATUS(R.string.status),
    CLASSIFICATION(R.string.classification),
    PRIORITY(R.string.priority),
    COLLECTION(R.string.collection),
    LINK_TO_PARENT(R.string.link_to_parent);

    companion object {
        fun valuesFor(module: Module) = when(module) {
            Module.JOURNAL, Module.NOTE -> listOf(CATEGORIES, RESOURCES, STATUS, CLASSIFICATION, COLLECTION, LINK_TO_PARENT)
            Module.TODO -> listOf(CATEGORIES, RESOURCES, STATUS, CLASSIFICATION, PRIORITY, COLLECTION, LINK_TO_PARENT)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UpdateEntriesDialog(
    module: Module,
    allCategoriesLive: LiveData<List<String>>,
    allResourcesLive: LiveData<List<String>>,
    allCollectionsLive: LiveData<List<ICalCollection>>,
    selectFromAllListLive: LiveData<List<ICal4ListRel>>,
    extendedStatusesLive: LiveData<List<ExtendedStatus>>,
    player: MediaPlayer?,
    onSelectFromAllListSearchTextUpdated: (String) -> Unit,
    onCategoriesChanged: (addedCategories: List<String>, removedCategories: List<String>) -> Unit,
    onResourcesChanged: (addedResources: List<String>, removedResources: List<String>) -> Unit,
    onStatusChanged: (Status) -> Unit,
    onXStatusChanged: (ExtendedStatus) -> Unit,
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
    val storedStatuses by extendedStatusesLive.observeAsState(emptyList())

    val addedCategories = remember { mutableStateListOf<String>() }
    val removedCategories = remember { mutableStateListOf<String>() }
    val addedResources = remember { mutableStateListOf<String>() }
    val removedResources = remember { mutableStateListOf<String>() }
    var newStatus by remember { mutableStateOf<Status?>(null) }
    var newXStatus by remember { mutableStateOf<ExtendedStatus?>(null) }
    var newClassification by remember { mutableStateOf<Classification?>(null) }
    var newPriority by remember { mutableStateOf<Int?>(null) }
    var newCollection by remember { mutableStateOf<ICalCollection?>(null) }
    var selectFromAllListSearchText by remember { mutableStateOf("") }
    var selectFromAllListSelectedEntry by remember { mutableStateOf<ICal4List?>(null) }

    var updateEntriesDialogMode by remember { mutableStateOf(UpdateEntriesDialogMode.CATEGORIES) }
    var selectFromAllListMaxEntriesShown by remember { mutableIntStateOf(10) }


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
                    var menuExpanded by remember { mutableStateOf(false) }

                    AssistChip(
                        onClick = { menuExpanded = true },
                        label = {
                            Text(stringResource(id = updateEntriesDialogMode.stringResource))

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {

                                UpdateEntriesDialogMode.valuesFor(module).forEach {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(id = it.stringResource)) },
                                        onClick = {
                                            if (updateEntriesDialogMode != it) {
                                                addedCategories.clear()
                                                removedCategories.clear()
                                                addedResources.clear()
                                                removedResources.clear()
                                                updateEntriesDialogMode = it
                                            }
                                            menuExpanded = false
                                        }
                                    )
                                }
                            }
                        },
                        trailingIcon = { Icon(Icons.Outlined.ArrowDropDown, null) }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                AnimatedVisibility(visible = updateEntriesDialogMode == UpdateEntriesDialogMode.CATEGORIES) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth()
                    )
                    {
                        var maxItems by remember { mutableIntStateOf(MAX_ITEMS) }
                        allCategories.forEachIndexed { index, category ->
                            if(index > maxItems-1)
                                return@forEachIndexed

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
                                        Icon(Icons.AutoMirrored.Outlined.LabelOff, stringResource(id = R.string.delete), tint = MaterialTheme.colorScheme.error)
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

                        if(allCategories.size > maxItems) {
                            TextButton(onClick = { maxItems = Int.MAX_VALUE }) {
                                Text(stringResource(R.string.filter_options_more_entries, allCategories.size-maxItems))
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = updateEntriesDialogMode == UpdateEntriesDialogMode.RESOURCES) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth()
                    )
                    {
                        var maxItems by remember { mutableIntStateOf(MAX_ITEMS) }

                        allResources.forEachIndexed { index,resource ->
                            if(index > maxItems-1)
                                return@forEachIndexed

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


                        if(allResources.size > maxItems) {
                            TextButton(onClick = { maxItems = Int.MAX_VALUE }) {
                                Text(stringResource(R.string.filter_options_more_entries, allResources.size-maxItems))
                            }
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
                                onClick = {
                                    newStatus = status
                                    newXStatus = null
                                          },
                                label = { Text(stringResource(id = status.stringResource)) },
                                selected = newStatus == status,
                            )
                        }
                        storedStatuses
                            .filter { Status.valuesFor(module).none { default -> stringResource(id = default.stringResource) == it.xstatus } }
                            .filter { it.module == module }
                            .forEach { storedStatus ->
                                InputChip(
                                    onClick = {
                                        newStatus = null
                                        newXStatus = storedStatus
                                              },
                                    label = { Text(storedStatus.xstatus) },
                                    selected = newXStatus == storedStatus,
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

                        Classification.entries.forEach { classification ->
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
                        var maxItems by remember { mutableIntStateOf(MAX_ITEMS) }

                        allCollections.forEachIndexed { index, collection ->
                            if(index > maxItems-1)
                                return@forEachIndexed

                            InputChip(
                                onClick = { newCollection = collection },
                                label = { Text(collection.displayName ?: collection.collectionId.toString()) },
                                selected = newCollection == collection,
                            )
                        }
                        if(allCollections.size > maxItems) {
                            TextButton(onClick = { maxItems = Int.MAX_VALUE }) {
                                Text(stringResource(R.string.filter_options_more_entries, allCollections.size-maxItems))
                            }
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
                                selected = entry.iCal4List == selectFromAllListSelectedEntry,
                                progressUpdateDisabled = true,
                                markdownEnabled = false,
                                player = player,
                                onProgressChanged = {_, _ -> },
                                storedCategories = emptyList(),
                                storedStatuses = emptyList(),
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
                        UpdateEntriesDialogMode.STATUS -> if(newXStatus != null) onXStatusChanged(newXStatus!!) else newStatus?.let { onStatusChanged(it) }
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
                    UpdateEntriesDialogMode.STATUS -> newStatus != null || newXStatus != null
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
            extendedStatusesLive = MutableLiveData(listOf(ExtendedStatus("individual", Module.JOURNAL, Status.NO_STATUS, Color.Green.toArgb()))),
            player = null,
            onSelectFromAllListSearchTextUpdated = { },
            onCategoriesChanged = { _, _ -> },
            onResourcesChanged = { _, _ -> },
            onStatusChanged = {},
            onXStatusChanged = {},
            onClassificationChanged = {},
            onPriorityChanged = {},
            onCollectionChanged = {},
            onParentAdded = {},
            onDismiss = { }
        )
    }
}
