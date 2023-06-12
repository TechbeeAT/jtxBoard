/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.media.MediaPlayer
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredResource
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.theme.jtxCardCornerShape


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun ListScreenGrid(
    list: List<ICal4ListRel>,
    subtasksLive: LiveData<List<ICal4ListRel>>,
    storedCategoriesLive: LiveData<List<StoredCategory>>,
    storedResourcesLive: LiveData<List<StoredResource>>,
    storedStatusesLive: LiveData<List<ExtendedStatus>>,
    selectedEntries: SnapshotStateList<Long>,
    scrollOnceId: MutableLiveData<Long?>,
    settingLinkProgressToSubtasks: Boolean,
    isPullRefreshEnabled: Boolean,
    player: MediaPlayer?,
    onProgressChanged: (itemId: Long, newPercent: Int) -> Unit,
    onClick: (itemId: Long, list: List<ICal4List>, isReadOnly: Boolean) -> Unit,
    onLongClick: (itemId: Long, list: List<ICal4List>) -> Unit,
    onSyncRequested: () -> Unit
) {

    val subtasks by subtasksLive.observeAsState(emptyList())
    val scrollId by scrollOnceId.observeAsState(null)
    val storedCategories by storedCategoriesLive.observeAsState(emptyList())
    val storedResources by storedResourcesLive.observeAsState(emptyList())
    val storedStatuses by storedStatusesLive.observeAsState(emptyList())
    val gridState = rememberLazyStaggeredGridState()

    if (scrollId != null) {
        LaunchedEffect(list) {
            val index = list.indexOfFirst { iCal4ListRelObject -> iCal4ListRelObject.iCal4List.id == scrollId }
            if (index > -1) {
                gridState.animateScrollToItem(index)
                scrollOnceId.postValue(null)
            }
        }
    }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = false,
        onRefresh = { onSyncRequested() }
    )

    Box(
        contentAlignment = Alignment.TopCenter
    ) {

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(8.dp),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = if(isPullRefreshEnabled) Modifier.pullRefresh(pullRefreshState) else Modifier,
        ) {
            items(
                items = list,
                key = { item -> item.iCal4List.id }
            )
            { iCal4ListRelObject ->

                val currentSubtasks =
                    subtasks.filter { iCal4ListRel -> iCal4ListRel.relatedto.any { relatedto -> relatedto.reltype == Reltype.PARENT.name && relatedto.text == iCal4ListRelObject.iCal4List.uid } }
                        .map { it.iCal4List }

                ListCardGrid(
                    iCal4ListRelObject.iCal4List,
                    categories = iCal4ListRelObject.categories,
                    resources = iCal4ListRelObject.resources,
                    storedCategories = storedCategories,
                    storedResources = storedResources,
                    storedStatuses = storedStatuses,
                    selected = selectedEntries.contains(iCal4ListRelObject.iCal4List.id),
                    progressUpdateDisabled = settingLinkProgressToSubtasks && currentSubtasks.isNotEmpty(),
                    player = player,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(jtxCardCornerShape)
                        //.animateItemPlacement()
                        .combinedClickable(
                            onClick = { onClick(iCal4ListRelObject.iCal4List.id, list.map { it.iCal4List }, iCal4ListRelObject.iCal4List.isReadOnly) },
                            onLongClick = {
                                if (!iCal4ListRelObject.iCal4List.isReadOnly)
                                    onLongClick(iCal4ListRelObject.iCal4List.id, list.map { it.iCal4List })
                            }
                        ),
                    onProgressChanged = onProgressChanged,
                )
            }
        }

        PullRefreshIndicator(
            refreshing = false,
            state = pullRefreshState
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ListScreenGrid_TODO() {
    MaterialTheme {

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
        ListScreenGrid(
            list = listOf(
                ICal4ListRel(icalobject, emptyList(), emptyList(), emptyList()),
                ICal4ListRel(icalobject2, emptyList(), emptyList(), emptyList())
            ),
            subtasksLive = MutableLiveData(emptyList()),
            storedCategoriesLive = MutableLiveData(emptyList()),
            storedResourcesLive = MutableLiveData(emptyList()),
            storedStatusesLive = MutableLiveData(emptyList()),
            selectedEntries = remember { mutableStateListOf() },
            scrollOnceId = MutableLiveData(null),
            settingLinkProgressToSubtasks = false,
            isPullRefreshEnabled = true,
            player = null,
            onProgressChanged = { _, _ -> },
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            onSyncRequested = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ListScreenGrid_JOURNAL() {
    MaterialTheme {

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
        ListScreenGrid(
            list = listOf(
                ICal4ListRel(icalobject, emptyList(), emptyList(), emptyList()),
                ICal4ListRel(icalobject2, emptyList(), emptyList(), emptyList())
            ),
            subtasksLive = MutableLiveData(emptyList()),
            storedCategoriesLive = MutableLiveData(emptyList()),
            storedResourcesLive = MutableLiveData(emptyList()),
            storedStatusesLive = MutableLiveData(emptyList()),
            selectedEntries = remember { mutableStateListOf() },
            scrollOnceId = MutableLiveData(null),
            settingLinkProgressToSubtasks = false,
            isPullRefreshEnabled = true,
            player = null,
            onProgressChanged = { _, _ -> },
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            onSyncRequested = { }
        )
    }
}

