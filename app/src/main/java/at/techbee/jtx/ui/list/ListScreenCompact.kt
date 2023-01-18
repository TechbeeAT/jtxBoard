/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import at.techbee.jtx.database.*
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.theme.jtxCardCornerShape


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListScreenCompact(
    groupedList: Map<String, List<ICal4List>>,
    subtasksLive: LiveData<Map<String?, List<ICal4List>>>,
    selectedEntries: SnapshotStateList<Long>,
    scrollOnceId: MutableLiveData<Long?>,
    listSettings: ListSettings,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit,
    onClick: (itemId: Long, list: List<ICal4List>) -> Unit,
    onLongClick: (itemId: Long, list: List<ICal4List>) -> Unit
) {

    val subtasks by subtasksLive.observeAsState(emptyMap())
    val scrollId by scrollOnceId.observeAsState(null)
    val listState = rememberLazyListState()

    val itemsCollapsed = remember { mutableStateListOf<String>() }


    LazyColumn(
        modifier = Modifier.padding(start = 2.dp, end = 2.dp),
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
                        Text(
                            text = groupName,
                            style = MaterialTheme.typography.titleLarge
                        )

                        TextButton(onClick = {
                            if (itemsCollapsed.contains(groupName))
                                itemsCollapsed.remove(groupName)
                            else
                                itemsCollapsed.add(groupName)
                        }) {
                            if (itemsCollapsed.contains(groupName))
                                Icon(Icons.Outlined.ArrowDropUp, stringResource(R.string.list_collapse))
                            else
                                Icon(Icons.Outlined.ArrowDropDown, stringResource(R.string.list_expand))
                        }
                    }
                }
            }

            if (groupedList.keys.size <= 1 || (groupedList.keys.size > 1 && !itemsCollapsed.contains(groupName))) {
                items(
                    items = group,
                    key = { item -> item.id }
                )
                { iCalObject ->

                    var currentSubtasks = subtasks[iCalObject.uid]
                    if (listSettings.isExcludeDone.value)   // exclude done if applicable

                        currentSubtasks = currentSubtasks?.filter { subtask -> subtask.percent != 100 }


                    if (scrollId != null) {
                        LaunchedEffect(group) {
                            val index = group.indexOfFirst { iCalObject -> iCalObject.id == scrollId }
                            if (index > -1) {
                                listState.animateScrollToItem(index)
                                scrollOnceId.postValue(null)
                            }
                        }
                    }

                    ListCardCompact(
                        iCalObject,
                        subtasks = currentSubtasks ?: emptyList(),
                        selected = selectedEntries.contains(iCalObject.id),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 4.dp)
                            .animateItemPlacement()
                            .clip(jtxCardCornerShape)
                            .combinedClickable(
                                onClick = { onClick(iCalObject.id, groupedList.flatMap { it.value }) },
                                onLongClick = {
                                    if (!iCalObject.isReadOnly)
                                        onLongClick(iCalObject.id, groupedList.flatMap { it.value })
                                }
                            ),
                        onProgressChanged = onProgressChanged,
                        onClick = onClick,
                        onLongClick = onLongClick
                    )

                    if (iCalObject != group.last())
                        Divider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            thickness = 1.dp,
                            modifier = Modifier.alpha(0.25f)
                        )
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
            groupedList = listOf(icalobject, icalobject2).groupBy { it.status ?: "" },
            subtasksLive = MutableLiveData(emptyMap()),
            scrollOnceId = MutableLiveData(null),
            selectedEntries = remember { mutableStateListOf() },
            listSettings = listSettings,
            onProgressChanged = { _, _, _ -> },
            onClick = { _, _ -> },
            onLongClick = { _, _ -> }
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
            groupedList = listOf(icalobject, icalobject2).groupBy { it.status ?: "" },
            subtasksLive = MutableLiveData(emptyMap()),
            selectedEntries = remember { mutableStateListOf() },
            scrollOnceId = MutableLiveData(null),
            listSettings = listSettings,
            onProgressChanged = { _, _, _ -> },
            onClick = { _, _ -> },
            onLongClick = { _, _ -> }
        )
    }
}
