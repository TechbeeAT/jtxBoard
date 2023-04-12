/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.database.*
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredResource
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.theme.jtxCardCornerShape
import kotlin.math.abs
import kotlin.math.roundToInt


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListScreenKanban(
    module: Module,
    list: List<ICal4ListRel>,
    subtasksLive: LiveData<List<ICal4ListRel>>,
    storedCategoriesLive: LiveData<List<StoredCategory>>,
    storedResourcesLive: LiveData<List<StoredResource>>,
    selectedEntries: SnapshotStateList<Long>,
    scrollOnceId: MutableLiveData<Long?>,
    settingLinkProgressToSubtasks: Boolean,
    player: MediaPlayer?,
    onProgressChanged: (itemId: Long, newPercent: Int, scrollOnce: Boolean) -> Unit,
    onStatusChanged: (itemid: Long, status: Status, scrollOnce: Boolean) -> Unit,
    onClick: (itemId: Long, list: List<ICal4List>) -> Unit,
    onLongClick: (itemId: Long, list: List<ICal4List>) -> Unit
) {

    val context = LocalContext.current
    val scrollId by scrollOnceId.observeAsState(null)
    val statusColumns = Status.valuesFor(module).filter { it != Status.CANCELLED && it != Status.NO_STATUS }
    val subtasks by subtasksLive.observeAsState(emptyList())
    val storedCategories by storedCategoriesLive.observeAsState(emptyList())
    val storedResources by storedResourcesLive.observeAsState(emptyList())


    Row(modifier = Modifier.fillMaxWidth()) {

        statusColumns.forEach { status ->

            val listState = rememberLazyListState()
            val listFilteredByStatus = list.filter { it.iCal4List.status == status.status      // below we handle also empty status for todos
                    || (it.iCal4List.status == null && it.iCal4List.component == Component.VTODO.name && (it.iCal4List.percent == null || it.iCal4List.percent == 0) && status == Status.NEEDS_ACTION)
                    || (it.iCal4List.status == null && it.iCal4List.component == Component.VTODO.name && it.iCal4List.percent in 1..99 && status == Status.IN_PROCESS)
                    || (it.iCal4List.status == null && it.iCal4List.component == Component.VTODO.name && it.iCal4List.percent == 100 && status == Status.COMPLETED)
                    || (it.iCal4List.status == null && it.iCal4List.component == Component.VJOURNAL.name && status == Status.FINAL)
            }
            if(scrollId != null) {
                LaunchedEffect(list) {
                    val index = listFilteredByStatus.indexOfFirst { iCal4ListRelObject -> iCal4ListRelObject.iCal4List.id == scrollId }
                    if(index > -1) {
                        listState.animateScrollToItem(index)
                        scrollOnceId.postValue(null)
                    }
                }
            }

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F)
            ) {

                stickyHeader {
                    Text(
                        text = stringResource(id = status.stringResource),
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center
                    )
                }

                items(
                    items = listFilteredByStatus,
                    key = { item -> item.iCal4List.id }
                )
                { iCal4ListRelObject ->

                    val currentSubtasks = subtasks.filter { iCal4ListRel -> iCal4ListRel.relatedto.any { relatedto -> relatedto.reltype == Reltype.PARENT.name && relatedto.text == iCal4ListRel.iCal4List.uid } }.map { it.iCal4List }

                    var offsetX by remember { mutableStateOf(0f) }  // see https://developer.android.com/jetpack/compose/gestures
                    val maxOffset = 50f

                    ListCardKanban(
                        iCal4ListRelObject.iCal4List,
                        categories = iCal4ListRelObject.categories,
                        resources = iCal4ListRelObject.resources,
                        storedCategories = storedCategories,
                        storedResources = storedResources,
                        selected = selectedEntries.contains(iCal4ListRelObject.iCal4List.id),
                        player = player,
                        modifier = Modifier
                            .animateItemPlacement()
                            .clip(jtxCardCornerShape)
                            .combinedClickable(
                                onClick = { onClick(iCal4ListRelObject.iCal4List.id, list.map { it.iCal4List }) },
                                onLongClick = {
                                    if (!iCal4ListRelObject.iCal4List.isReadOnly)
                                        onLongClick(iCal4ListRelObject.iCal4List.id, list.map { it.iCal4List })
                                }
                            )
                            .height(150.dp)
                            .fillMaxWidth()
                            .offset { IntOffset(offsetX.roundToInt(), 0) }
                            .draggable(
                                orientation = Orientation.Horizontal,
                                state = rememberDraggableState { delta ->
                                    if (iCal4ListRelObject.iCal4List.isReadOnly)   // no drag state for read only objects!
                                        return@rememberDraggableState
                                    if (settingLinkProgressToSubtasks && currentSubtasks.isNotEmpty())
                                        return@rememberDraggableState  // no drag is status depends on subtasks
                                    if (abs(offsetX) <= maxOffset)     // once maxOffset is reached, we don't update anymore
                                        offsetX += delta
                                },
                                onDragStopped = {
                                    if (abs(offsetX) > maxOffset / 2) {
                                        if (iCal4ListRelObject.iCal4List.component == Component.VTODO.name) {
                                            when {
                                                (iCal4ListRelObject.iCal4List.percent ?: 0) == 0 && offsetX > 0f -> onProgressChanged(
                                                    iCal4ListRelObject.iCal4List.id,
                                                    1,
                                                    true
                                                )   // positive change, from Needs Action to In Process
                                                (iCal4ListRelObject.iCal4List.percent ?: 0) in 1..99 && offsetX > 0f -> onProgressChanged(
                                                    iCal4ListRelObject.iCal4List.id,
                                                    100,
                                                    true
                                                )   // positive change, from In Process to Completed
                                                (iCal4ListRelObject.iCal4List.percent ?: 0) == 100 && offsetX < 0f -> onProgressChanged(
                                                    iCal4ListRelObject.iCal4List.id,
                                                    99,
                                                    true
                                                )   // negative change, from Completed to In Process
                                                (iCal4ListRelObject.iCal4List.percent ?: 0) in 1..99 && offsetX < 0f -> onProgressChanged(
                                                    iCal4ListRelObject.iCal4List.id,
                                                    0,
                                                    true
                                                )   // negative change, from In Process to Needs Action
                                            }
                                        } else {   // VJOURNAL
                                            when {
                                                (iCal4ListRelObject.iCal4List.status == Status.DRAFT.status && offsetX > 0f) -> onStatusChanged(iCal4ListRelObject.iCal4List.id, Status.FINAL, true)
                                                ((iCal4ListRelObject.iCal4List.status == Status.FINAL.status || iCal4ListRelObject.iCal4List.status == Status.NO_STATUS.status) && offsetX < 0f) -> onStatusChanged(iCal4ListRelObject.iCal4List.id, Status.DRAFT, true)
                                            }
                                        }
                                        // make a short vibration
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            val vibratorManager = context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                                            val vibrator = vibratorManager.defaultVibrator
                                            val vibrationEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                                            vibrator.vibrate(vibrationEffect)
                                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            @Suppress("DEPRECATION")
                                            val vibrator = context.getSystemService(VIBRATOR_SERVICE) as Vibrator
                                            val vibrationEffect = VibrationEffect.createOneShot(150, 10)
                                            vibrator.vibrate(vibrationEffect)
                                        }
                                    }
                                    offsetX = 0f
                                }
                            ),
                        )
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun ListScreenKanban_TODO() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            id = 1L
            component = Component.VTODO.name
            module = Module.TODO.name
            percent = 89
            status = Status.IN_PROCESS.status
            classification = Classification.PUBLIC.name
            dtstart = null
            due = null
            numAttachments = 0
            numSubnotes = 0
            numSubtasks = 0
            summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        }
        val icalobject2 = ICal4List.getSample().apply {
            id = 2L
            component = Component.VTODO.name
            module = Module.TODO.name
            percent = 89
            status = Status.NEEDS_ACTION.status
            classification = Classification.CONFIDENTIAL.name
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis()
            summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            colorItem = Color.Blue.toArgb()
        }
        ListScreenKanban(
            module = Module.TODO,
            list = listOf(
                ICal4ListRel(icalobject, emptyList(), emptyList(), emptyList()),
                ICal4ListRel(icalobject2, emptyList(), emptyList(), emptyList())
            ),
            subtasksLive = MutableLiveData(emptyList()),
            storedCategoriesLive = MutableLiveData(emptyList()),
            storedResourcesLive = MutableLiveData(emptyList()),
            selectedEntries = remember { mutableStateListOf() },
            scrollOnceId = MutableLiveData(null),
            settingLinkProgressToSubtasks = false,
            player = null,
            onProgressChanged = { _, _, _ -> },
            onStatusChanged = {_, _, _ -> },
            onClick = { _, _ -> },
            onLongClick = { _, _ -> }
        )
    }
}



@Preview(showBackground = true)
@Composable
fun ListScreenKanban_JOURNAL() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            id = 1L
            component = Component.VJOURNAL.name
            module = Module.JOURNAL.name
            percent = 89
            status = Status.FINAL.status
            classification = Classification.PUBLIC.name
            dtstart = null
            due = null
            numAttachments = 0
            numSubnotes = 0
            numSubtasks = 0
            summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        }
        val icalobject2 = ICal4List.getSample().apply {
            id = 2L
            component = Component.VJOURNAL.name
            module = Module.JOURNAL.name
            percent = 89
            status = Status.DRAFT.status
            classification = Classification.CONFIDENTIAL.name
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis()
            summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            colorItem = Color.Blue.toArgb()
        }
        ListScreenKanban(
            module = Module.JOURNAL,
            list = listOf(
                ICal4ListRel(icalobject, emptyList(), emptyList(), emptyList()),
                ICal4ListRel(icalobject2, emptyList(), emptyList(), emptyList())
            ),
            subtasksLive = MutableLiveData(emptyList()),
            storedCategoriesLive = MutableLiveData(emptyList()),
            storedResourcesLive = MutableLiveData(emptyList()),
            selectedEntries = remember { mutableStateListOf() },
            scrollOnceId = MutableLiveData(null),
            settingLinkProgressToSubtasks = false,
            player = null,
            onProgressChanged = { _, _, _ -> },
            onStatusChanged = {_, _, _ -> },
            onClick = { _, _ -> },
            onLongClick = { _, _ -> }
        )
    }
}

