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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.database.*
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.theme.jtxCardCornerShape
import kotlin.math.abs
import kotlin.math.roundToInt


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListScreenKanban(
    module: Module,
    list: State<List<ICal4List>>,
    scrollOnceId: MutableLiveData<Long?>,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean, scrollOnce: Boolean) -> Unit,
    onStatusChanged: (itemid: Long, status: StatusJournal, isLinkedRecurringInstance: Boolean, scrollOnce: Boolean) -> Unit,
    goToDetail: (itemId: Long, editMode: Boolean, list: List<ICal4List>) -> Unit
) {

    val context = LocalContext.current
    val scrollId by scrollOnceId.observeAsState(null)
    val statusColumns = if(module == Module.TODO) setOf(StatusTodo.`NEEDS-ACTION`.name, StatusTodo.`IN-PROCESS`.name, StatusTodo.COMPLETED.name) else setOf(StatusJournal.DRAFT.name, StatusJournal.FINAL.name)

    Row(modifier = Modifier.fillMaxWidth()) {

        statusColumns.forEach { status ->

            val listState = rememberLazyListState()
            val listFilteredByStatus = list.value.filter { it.status == status      // below we handle also empty status for todos
                    || (it.status == null && it.component == Component.VTODO.name && (it.percent == null || it.percent == 0) && status == StatusTodo.`NEEDS-ACTION`.name)
                    || (it.status == null && it.component == Component.VTODO.name && it.percent in 1..99 && status == StatusTodo.`IN-PROCESS`.name)
                    || (it.status == null && it.component == Component.VTODO.name && it.percent == 100 && status == StatusTodo.COMPLETED.name)
                    || (it.status == null && it.component == Component.VJOURNAL.name && status == StatusJournal.FINAL.name)
            }
            if(scrollId != null) {
                LaunchedEffect(list) {
                    val index = listFilteredByStatus.indexOfFirst { iCalObject -> iCalObject.id == scrollId }
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
                        if(module == Module.TODO)
                            StatusTodo.getStringResource(LocalContext.current, status)
                        else
                            StatusJournal.getStringResource(LocalContext.current, status),
                        modifier = Modifier.align(Alignment.CenterVertically).background(MaterialTheme.colorScheme.background).fillMaxWidth(),
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center
                    )
                }

                items(
                    items = listFilteredByStatus,
                    key = { item -> item.id }
                )
                { iCalObject ->

                    var offsetX by remember { mutableStateOf(0f) }  // see https://developer.android.com/jetpack/compose/gestures
                    val maxOffset = 50f

                    ListCardKanban(
                        iCalObject,
                        modifier = Modifier
                            .animateItemPlacement()
                            .clip(jtxCardCornerShape)
                            .combinedClickable(
                                onClick = { goToDetail(iCalObject.id, false, list.value) },
                                onLongClick = {
                                    if (!iCalObject.isReadOnly && BillingManager.getInstance().isProPurchased.value == true)
                                        goToDetail(iCalObject.id, true, list.value)
                                }
                            )
                            .height(150.dp)
                            .offset { IntOffset(offsetX.roundToInt(), 0) }
                            .draggable(
                                orientation = Orientation.Horizontal,
                                state = rememberDraggableState { delta ->
                                    if(iCalObject.isReadOnly)   // no drag state for read only objects!
                                        return@rememberDraggableState
                                    if(abs(offsetX) <= maxOffset)     // once maxOffset is reached, we don't update anymore
                                        offsetX += delta
                                },
                                onDragStopped = {
                                    if(abs(offsetX) > maxOffset / 2) {
                                        if(iCalObject.component == Component.VTODO.name) {
                                            when {
                                                (iCalObject.percent ?: 0) == 0 && offsetX > 0f -> onProgressChanged(iCalObject.id, 1, iCalObject.isLinkedRecurringInstance, true)   // positive change, from Needs Action to In Process
                                                (iCalObject.percent ?: 0) in 1..99 && offsetX > 0f -> onProgressChanged(iCalObject.id, 100, iCalObject.isLinkedRecurringInstance, true)   // positive change, from In Process to Completed
                                                (iCalObject.percent ?: 0) == 100 && offsetX < 0f -> onProgressChanged(iCalObject.id, 99, iCalObject.isLinkedRecurringInstance, true)   // negative change, from Completed to In Process
                                                (iCalObject.percent ?: 0) in 1..99 && offsetX < 0f -> onProgressChanged(iCalObject.id, 0, iCalObject.isLinkedRecurringInstance, true)   // negative change, from In Process to Needs Action
                                            }
                                        } else {   // VJOURNAL
                                            when {
                                                (iCalObject.status == StatusJournal.DRAFT.name && offsetX > 0f) -> onStatusChanged(iCalObject.id, StatusJournal.FINAL, iCalObject.isLinkedRecurringInstance, true)
                                                (iCalObject.status == StatusJournal.FINAL.name && offsetX < 0f) -> onStatusChanged(iCalObject.id, StatusJournal.DRAFT, iCalObject.isLinkedRecurringInstance, true)
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
            status = StatusTodo.`IN-PROCESS`.name
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
            status = StatusTodo.`NEEDS-ACTION`.name
            classification = Classification.CONFIDENTIAL.name
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis()
            summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            colorItem = Color.Blue.toArgb()
        }
        ListScreenKanban(
            module = Module.TODO,
            list = remember { mutableStateOf(listOf(icalobject, icalobject2)) },
            scrollOnceId = MutableLiveData(null),
            onProgressChanged = { _, _, _, _ -> },
            onStatusChanged = {_, _, _, _ -> },
            goToDetail = { _, _, _ -> }
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
            status = StatusJournal.FINAL.name
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
            status = StatusJournal.DRAFT.name
            classification = Classification.CONFIDENTIAL.name
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis()
            summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            colorItem = Color.Blue.toArgb()
        }
        ListScreenKanban(
            module = Module.JOURNAL,
            list = remember { mutableStateOf(listOf(icalobject, icalobject2)) },
            scrollOnceId = MutableLiveData(null),
            onProgressChanged = { _, _, _, _ -> },
            onStatusChanged = {_, _, _, _ -> },
            goToDetail = { _, _, _ -> }
        )
    }
}

