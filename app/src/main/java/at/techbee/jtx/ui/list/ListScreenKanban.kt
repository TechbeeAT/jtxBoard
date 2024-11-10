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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredCategory
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
    storedCategories: List<StoredCategory>,
    storedStatuses: List<ExtendedStatus>,
    selectedEntries: SnapshotStateList<Long>,
    kanbanColumnsStatus: SnapshotStateList<String?>,
    kanbanColumnsXStatus: SnapshotStateList<String>,
    kanbanColumnsCategory: SnapshotStateList<String>,
    scrollOnceId: MutableLiveData<Long?>,
    settingLinkProgressToSubtasks: Boolean,
    markdownEnabled: Boolean,
    player: MediaPlayer?,
    onStatusChanged: (itemid: Long, status: Status, scrollOnce: Boolean) -> Unit,
    onXStatusChanged: (itemid: Long, status: ExtendedStatus, scrollOnce: Boolean) -> Unit,
    onSwapCategories: (itemid: Long, old: String, new: String) -> Unit,
    onClick: (itemId: Long, list: List<ICal4List>, isReadOnly: Boolean) -> Unit,
    onLongClick: (itemId: Long, list: List<ICal4List>) -> Unit
) {

    val context = LocalContext.current
    val scrollId by scrollOnceId.observeAsState(null)
    val groupedList = list.groupBy {
        when {
            kanbanColumnsXStatus.isNotEmpty() -> it.iCal4List.xstatus
            kanbanColumnsCategory.isNotEmpty() -> it.categories.firstOrNull()?.text
            else -> {   // this covers also kanbanColumnsStatus.isNotEmpty() and the fallback for default kanbanColumns based on the status
                if (it.iCal4List.status.isNullOrEmpty()) {
                    when {
                        it.iCal4List.component == Component.VTODO.name && it.iCal4List.percent == 100 -> stringResource(id = Status.COMPLETED.stringResource)
                        it.iCal4List.component == Component.VTODO.name && it.iCal4List.percent in 1..99 -> stringResource(id = Status.IN_PROCESS.stringResource)
                        //it.iCal4List.component == Component.VTODO.name -> stringResource(id = Status.NEEDS_ACTION.stringResource)
                        else -> stringResource(Status.NO_STATUS.stringResource) // fallback, shouldn't happen
                    }
                } else {
                    Status.getStatusFromString(it.iCal4List.status)?.let { status -> stringResource(id = status.stringResource) }?:it.iCal4List.status
                }
            }
        }
    }
    val subtasks by subtasksLive.observeAsState(emptyList())

    val columns = when {
        kanbanColumnsStatus.isNotEmpty() -> kanbanColumnsStatus.map { Status.getStatusFromString(it)?.let { status -> context.getString(status.stringResource) } ?: context.getString(Status.NO_STATUS.stringResource)}
        kanbanColumnsXStatus.isNotEmpty() -> kanbanColumnsXStatus
        kanbanColumnsCategory.isNotEmpty() -> kanbanColumnsCategory
        else -> {   // default columns as fallback
            when(module) {
                Module.JOURNAL -> listOf(stringResource(id = Status.NO_STATUS.stringResource), stringResource(id = Status.DRAFT.stringResource), stringResource(id = Status.FINAL.stringResource))
                Module.NOTE -> listOf(stringResource(id = Status.NO_STATUS.stringResource), stringResource(id = Status.DRAFT.stringResource), stringResource(id = Status.FINAL.stringResource))
                Module.TODO -> listOf(stringResource(id = Status.NO_STATUS.stringResource), stringResource(id = Status.NEEDS_ACTION.stringResource), stringResource(id = Status.IN_PROCESS.stringResource), stringResource(id = Status.COMPLETED.stringResource))
            }
        }
    }


    Row(modifier = Modifier.fillMaxWidth()) {

        columns.forEachIndexed { index, column ->

            val listState = rememberLazyListState()

            if (scrollId != null) {
                LaunchedEffect(list) {
                    val itemIndex = groupedList[column]?.indexOfFirst { iCal4ListRelObject -> iCal4ListRelObject.iCal4List.id == scrollId } ?: -1
                    if (itemIndex > -1) {
                        listState.scrollToItem(itemIndex)
                        scrollOnceId.postValue(null)
                    }
                }
            }

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().weight(1F)
            ) {

                stickyHeader {
                    Text(
                        text = column,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center
                    )
                }

                items(
                    items = groupedList[column]?: emptyList(),
                    key = { item -> item.iCal4List.id }
                )
                { iCal4ListRelObject ->

                    val currentSubtasks =
                        subtasks.filter { iCal4ListRel -> iCal4ListRel.relatedto.any { relatedto -> relatedto.reltype == Reltype.PARENT.name && relatedto.text == iCal4ListRel.iCal4List.uid } }
                            .map { it.iCal4List }

                    var offsetX by remember { mutableFloatStateOf(0f) }  // see https://developer.android.com/jetpack/compose/gestures
                    val maxOffset = 50f

                    ListCardKanban(
                        iCal4ListRelObject.iCal4List,
                        storedCategories = storedCategories,
                        storedStatuses = storedStatuses,
                        selected = selectedEntries.contains(iCal4ListRelObject.iCal4List.id),
                        markdownEnabled = markdownEnabled,
                        player = player,
                        modifier = Modifier
                            .clip(jtxCardCornerShape)
                            .combinedClickable(
                                onClick = { onClick(iCal4ListRelObject.iCal4List.id, list.map { it.iCal4List }, iCal4ListRelObject.iCal4List.isReadOnly) },
                                onLongClick = {
                                    if (!iCal4ListRelObject.iCal4List.isReadOnly)
                                        onLongClick(iCal4ListRelObject.iCal4List.id, list.map { it.iCal4List })
                                }
                            )
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
                                    if (abs(offsetX) > maxOffset / 2 && !iCal4ListRelObject.iCal4List.isReadOnly) {

                                        val draggedToColumn = when {
                                            offsetX < 0f && index > 0 -> index - 1
                                            offsetX > 0F && index < columns.lastIndex -> index + 1
                                            else -> {
                                                offsetX = 0f
                                                return@draggable
                                            }
                                        }

                                        when {
                                            kanbanColumnsXStatus.isNotEmpty() -> storedStatuses
                                                .find { xstatus -> xstatus.module == module && xstatus.xstatus == columns[draggedToColumn] }
                                                ?.let { xstatus ->
                                                    onXStatusChanged(iCal4ListRelObject.iCal4List.id, xstatus, true)
                                                }

                                            kanbanColumnsCategory.isNotEmpty() -> onSwapCategories(iCal4ListRelObject.iCal4List.id, column, columns[draggedToColumn])

                                            else -> Status.entries
                                                .find { status -> context.getString(status.stringResource) == columns[draggedToColumn] }
                                                ?.let { status ->
                                                    onStatusChanged(iCal4ListRelObject.iCal4List.id, status, true)
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
            summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
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
            summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            colorItem = Color.Blue.toArgb()
        }
        ListScreenKanban(
            module = Module.TODO,
            list = listOf(
                ICal4ListRel(icalobject, emptyList(), emptyList(), emptyList()),
                ICal4ListRel(icalobject2, emptyList(), emptyList(), emptyList())
            ),
            storedCategories = emptyList(),
            storedStatuses = emptyList(),
            subtasksLive = MutableLiveData(emptyList()),
            selectedEntries = remember { mutableStateListOf() },
            kanbanColumnsStatus = remember { mutableStateListOf() },
            kanbanColumnsXStatus = remember { mutableStateListOf() },
            kanbanColumnsCategory = remember { mutableStateListOf() },
            scrollOnceId = MutableLiveData(null),
            settingLinkProgressToSubtasks = false,
            markdownEnabled = false,
            player = null,
            onStatusChanged = { _, _, _ -> },
            onXStatusChanged = { _, _, _ -> },
            onSwapCategories = { _, _, _ -> },
            onClick = { _, _, _ -> },
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
            summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        }
        val icalobject2 = ICal4List.getSample().apply {
            id = 2L
            component = Component.VJOURNAL.name
            module = Module.JOURNAL.name
            percent = 89
            status = "individual"
            classification = Classification.CONFIDENTIAL.name
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis()
            summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            colorItem = Color.Blue.toArgb()
        }
        ListScreenKanban(
            module = Module.JOURNAL,
            list = listOf(
                ICal4ListRel(icalobject, emptyList(), emptyList(), emptyList()),
                ICal4ListRel(icalobject2, emptyList(), emptyList(), emptyList())
            ),
            subtasksLive = MutableLiveData(emptyList()),
            storedCategories = emptyList(),
            storedStatuses = emptyList(),
            selectedEntries = remember { mutableStateListOf() },
            kanbanColumnsStatus = remember { mutableStateListOf(Status.FINAL.status?: Status.FINAL.name)  },
            kanbanColumnsXStatus = remember { mutableStateListOf() },
            kanbanColumnsCategory = remember { mutableStateListOf() },
            scrollOnceId = MutableLiveData(null),
            settingLinkProgressToSubtasks = false,
            markdownEnabled = false,
            player = null,
            onStatusChanged = { _, _, _ -> },
            onXStatusChanged = { _, _, _ -> },
            onSwapCategories = { _, _, _ -> },
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> }
        )
    }
}

