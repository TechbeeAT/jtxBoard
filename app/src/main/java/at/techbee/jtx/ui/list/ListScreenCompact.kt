/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material.icons.outlined.VerticalAlignTop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.reusable.elements.DragHandleLazy
import at.techbee.jtx.ui.theme.jtxCardCornerShape
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.util.UUID


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListScreenCompact(
    groupedList: Map<String, List<ICal4ListRel>>,
    subtasksLive: LiveData<List<ICal4ListRel>>,
    storedCategories: List<StoredCategory>,
    storedStatuses: List<ExtendedStatus>,
    selectedEntries: SnapshotStateList<Long>,
    scrollOnceId: MutableLiveData<Long?>,
    listSettings: ListSettings,
    settingLinkProgressToSubtasks: Boolean,
    player: MediaPlayer?,
    isListDragAndDropEnabled: Boolean,
    isSubtaskDragAndDropEnabled: Boolean,
    onProgressChanged: (itemId: Long, newPercent: Int) -> Unit,
    onClick: (itemId: Long, list: List<ICal4List>, isReadOnly: Boolean) -> Unit,
    onLongClick: (itemId: Long, list: List<ICal4List>) -> Unit,
    onSaveListSettings: () -> Unit,
    onUpdateSortOrder: (List<ICal4List>) -> Unit
) {

    val context = LocalContext.current
    val subtasks by subtasksLive.observeAsState(emptyList())
    val scrollId by scrollOnceId.observeAsState(null)
    val listState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(listState) { from, to ->
        val reordered = groupedList.flatMap { it.value }.map { it.iCal4List }.toMutableList().apply {
            val fromIndex = indexOfFirst { it.id == from.key }
            val toIndex = indexOfFirst { it.id == to.key }
            add(toIndex, removeAt(fromIndex))
        }
        ICalDatabase.getInstance(context).iCalDatabaseDao().updateSortOrder(reordered.map { it.id })
    }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .padding(start = 2.dp, end = 2.dp)
            .fillMaxSize(),
        state = listState,
    ) {

        groupedList.forEach { (groupName, group) ->

            if (groupedList.keys.size > 1) {
                stickyHeader {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)

                    ) {
                        TextButton(onClick = {
                            if (listSettings.collapsedGroups.contains(groupName))
                                listSettings.collapsedGroups.remove(groupName)
                            else
                                listSettings.collapsedGroups.add(groupName)
                            onSaveListSettings()
                        }) {
                            Text(
                                text = groupName,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            if (listSettings.collapsedGroups.contains(groupName))
                                Icon(Icons.Outlined.ArrowDropUp, stringResource(R.string.list_collapse))
                            else
                                Icon(Icons.Outlined.ArrowDropDown, stringResource(R.string.list_expand))
                        }
                    }
                }
            }

            if (groupedList.keys.size <= 1 || (groupedList.keys.size > 1 && !listSettings.collapsedGroups.contains(groupName))) {
                items(
                    items = group.toList(),
                    key = { item ->
                        if(listSettings.groupBy.value == GroupBy.CATEGORY || listSettings.groupBy.value == GroupBy.RESOURCE)
                            item.iCal4List.id.toString() + UUID.randomUUID()
                        else
                            item.iCal4List.id
                    }
                )
                { iCal4ListRelObject ->

                    var currentSubtasks =
                        subtasks.filter { iCal4ListRel -> iCal4ListRel.relatedto.any { relatedto -> relatedto.reltype == Reltype.PARENT.name && relatedto.text == iCal4ListRelObject.iCal4List.uid } }
                            .map { it.iCal4List }
                    if (listSettings.isExcludeDone.value)   // exclude done if applicable
                        currentSubtasks = currentSubtasks.filter { subtask -> subtask.percent != 100 && subtask.status != Status.COMPLETED.status }


                    if (scrollId != null) {
                        LaunchedEffect(group) {
                            val index = group.indexOfFirst { iCalObject -> iCalObject.iCal4List.id == scrollId }
                            if (index > -1) {
                                listState.scrollToItem(index)
                                scrollOnceId.postValue(null)
                            }
                        }
                    }

                    ReorderableItem(
                        reorderableLazyListState,
                        key = iCal4ListRelObject.iCal4List.id
                    ) { _ ->
                        ListCardCompact(
                            iCal4ListRelObject.iCal4List,
                            storedCategories = storedCategories,
                            storedStatuses = storedStatuses,
                            subtasks = currentSubtasks,
                            progressUpdateDisabled = settingLinkProgressToSubtasks && currentSubtasks.isNotEmpty(),
                            selected = selectedEntries,
                            player = player,
                            isSubtaskDragAndDropEnabled = isSubtaskDragAndDropEnabled,
                            dragHandle = {
                                if(isListDragAndDropEnabled)
                                    DragHandleLazy(this)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 4.dp)
                                .clip(jtxCardCornerShape)
                                .combinedClickable(
                                    onClick = {
                                        onClick(
                                            iCal4ListRelObject.iCal4List.id,
                                            groupedList
                                                .flatMap { it.value }
                                                .map { it.iCal4List },
                                            iCal4ListRelObject.iCal4List.isReadOnly,
                                        )
                                    },
                                    onLongClick = {
                                        if (!iCal4ListRelObject.iCal4List.isReadOnly)
                                            onLongClick(
                                                iCal4ListRelObject.iCal4List.id,
                                                groupedList
                                                    .flatMap { it.value }
                                                    .map { it.iCal4List })
                                    }
                                ),
                            onProgressChanged = onProgressChanged,
                            onClick = onClick,
                            onLongClick = onLongClick,
                            onUpdateSortOrder = onUpdateSortOrder
                        )
                    }

                    if (iCal4ListRelObject != group.last())
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.alpha(0.25f)
                        )
                }
            }
        }
    }

    Crossfade(listState.canScrollBackward, label = "showScrollUp") {
        if(it) {
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.fillMaxSize()
            ) {
                Button(
                    onClick = {
                        scope.launch { listState.scrollToItem(0) }
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    modifier = Modifier
                        .padding(8.dp)
                        .alpha(0.33f)
                ) {
                    Icon(Icons.Outlined.VerticalAlignTop, stringResource(R.string.list_scroll_to_top))
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ListScreenCompact_TODO() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(ListViewModel.PREFS_LIST_TODOS, Context.MODE_PRIVATE)

        val listSettings = ListSettings.fromPrefs(prefs)

        val icalobject = ICal4List.getSample().apply {
            id = 1L
            component = Component.VTODO.name
            module = Module.TODO.name
            percent = 89
            status = Status.IN_PROCESS.status
            classification = Classification.PUBLIC.classification
            dtstart = null
            due = null
            numAttachments = 0
            numSubnotes = 0
            numSubtasks = 0
            summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        }
        val icalobject2 = ICal4List.getSample().apply {
            id = 2L
            component = Component.VTODO.name
            module = Module.TODO.name
            percent = 89
            status = Status.IN_PROCESS.status
            classification = Classification.CONFIDENTIAL.classification
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis()
            summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            colorItem = Color.Blue.toArgb()
        }
        ListScreenCompact(
            groupedList = listOf(
                ICal4ListRel(icalobject, emptyList(), emptyList(), emptyList()),
                ICal4ListRel(icalobject2, emptyList(), emptyList(), emptyList())
            )
                .groupBy { it.iCal4List.status ?: "" },
            subtasksLive = MutableLiveData(emptyList()),
            storedCategories = emptyList(),
            storedStatuses = emptyList(),
            scrollOnceId = MutableLiveData(null),
            selectedEntries = remember { mutableStateListOf() },
            listSettings = listSettings,
            settingLinkProgressToSubtasks = false,
            player = null,
            isListDragAndDropEnabled = true,
            isSubtaskDragAndDropEnabled = true,
            onProgressChanged = { _, _ -> },
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            onSaveListSettings = { },
            onUpdateSortOrder = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ListScreenCompact_JOURNAL() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(ListViewModel.PREFS_LIST_JOURNALS, Context.MODE_PRIVATE)

        val listSettings = ListSettings.fromPrefs(prefs)


        val icalobject = ICal4List.getSample().apply {
            id = 1L
            component = Component.VJOURNAL.name
            module = Module.JOURNAL.name
            percent = 89
            status = Status.FINAL.status
            classification = Classification.PUBLIC.classification
            dtstart = null
            due = null
            numAttachments = 0
            numSubnotes = 0
            numSubtasks = 0
            summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        }
        val icalobject2 = ICal4List.getSample().apply {
            id = 2L
            component = Component.VJOURNAL.name
            module = Module.JOURNAL.name
            percent = 89
            status = Status.IN_PROCESS.status
            classification = Classification.CONFIDENTIAL.classification
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis()
            summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            colorItem = Color.Blue.toArgb()
        }
        ListScreenCompact(
            groupedList = listOf(
                ICal4ListRel(icalobject, emptyList(), emptyList(), emptyList()),
                ICal4ListRel(icalobject2, emptyList(), emptyList(), emptyList())
            )
                .groupBy { it.iCal4List.status ?: "" },
            subtasksLive = MutableLiveData(emptyList()),
            storedCategories = emptyList(),
            storedStatuses = emptyList(),
            selectedEntries = remember { mutableStateListOf() },
            scrollOnceId = MutableLiveData(null),
            listSettings = listSettings,
            settingLinkProgressToSubtasks = false,
            player = null,
            isListDragAndDropEnabled = true,
            isSubtaskDragAndDropEnabled = true,
            onProgressChanged = { _, _ -> },
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            onSaveListSettings = { },
            onUpdateSortOrder = { }
        )
    }
}
