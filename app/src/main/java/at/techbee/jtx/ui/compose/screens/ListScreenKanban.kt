/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.database.*
import at.techbee.jtx.database.relations.ICal4ListWithRelatedto
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.compose.cards.ListCardKanban
import at.techbee.jtx.ui.theme.JtxBoardTheme
import kotlin.math.abs
import kotlin.math.roundToInt


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListScreenKanban(
    module: Module,
    listLive: LiveData<List<ICal4ListWithRelatedto>>,
    scrollOnceId: MutableLiveData<Long?>,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean, scrollOnce: Boolean) -> Unit,
    onStatusChanged: (itemid: Long, status: StatusJournal, isLinkedRecurringInstance: Boolean, scrollOnce: Boolean) -> Unit,
    goToView: (itemId: Long) -> Unit,
    goToEdit: (itemId: Long) -> Unit
) {

    val list by listLive.observeAsState(emptyList())
    val scrollId by scrollOnceId.observeAsState(null)
    val statusColumns = if(module == Module.TODO) setOf(StatusTodo.`NEEDS-ACTION`.name, StatusTodo.`IN-PROCESS`.name, StatusTodo.COMPLETED.name) else setOf(StatusJournal.DRAFT.name, StatusJournal.FINAL.name)

    Row(modifier = Modifier.fillMaxWidth()) {

        statusColumns.forEach { status ->

            val listState = rememberLazyListState()
            val listFilteredByStatus = list.filter { it.property.status == status      // below we handle also empty status for todos
                    || (it.property.status == null && it.property.component == Component.VTODO.name && (it.property.percent == null || it.property.percent == 0) && status == StatusTodo.`NEEDS-ACTION`.name)
                    || (it.property.status == null && it.property.component == Component.VTODO.name && it.property.percent in 1..99 && status == StatusTodo.`IN-PROCESS`.name)
                    || (it.property.status == null && it.property.component == Component.VTODO.name && it.property.percent == 100 && status == StatusTodo.COMPLETED.name)
                    || (it.property.status == null && it.property.component == Component.VJOURNAL.name && status == StatusJournal.FINAL.name)
            }
            if(scrollId != null) {
                LaunchedEffect(list) {
                    val index = listFilteredByStatus.indexOfFirst { iCalObject -> iCalObject.property.id == scrollId }
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

                item {
                    Text(
                        if(module == Module.TODO)
                            StatusTodo.getStringResource(LocalContext.current, status) ?: ""
                        else
                            StatusJournal.getStringResource(LocalContext.current, status) ?: "",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                items(
                    items = listFilteredByStatus,
                    key = { item -> item.property.id }
                )
                { iCalObject ->

                    var offsetX by remember { mutableStateOf(0f) }  // see https://developer.android.com/jetpack/compose/gestures
                    val maxOffset = 50f

                    ListCardKanban(
                        iCalObject,
                        modifier = Modifier
                            .animateItemPlacement()
                            .combinedClickable(
                                onClick = { goToView(iCalObject.property.id) },
                                onLongClick = {
                                    if (!iCalObject.property.isReadOnly && BillingManager.getInstance()?.isProPurchased?.value == true)
                                        goToEdit(iCalObject.property.id)
                                }
                            )
                            .height(150.dp)
                            .offset { IntOffset(offsetX.roundToInt(), 0) }
                            .draggable(
                                orientation = Orientation.Horizontal,
                                state = rememberDraggableState { delta ->
                                    if(iCalObject.property.isReadOnly)   // no drag state for read only objects!
                                        return@rememberDraggableState
                                    if(abs(offsetX) <= maxOffset)     // once maxOffset is reached, we don't update anymore
                                        offsetX += delta
                                },
                                onDragStopped = {
                                    if(abs(offsetX) > maxOffset / 2) {
                                        if(iCalObject.property.component == Component.VTODO.name) {
                                            when {
                                                (iCalObject.property.percent ?: 0) == 0 && offsetX > 0f -> onProgressChanged(iCalObject.property.id, 1, iCalObject.property.isLinkedRecurringInstance, true)   // positive change, from Needs Action to In Process
                                                (iCalObject.property.percent ?: 0) in 1..99 && offsetX > 0f -> onProgressChanged(iCalObject.property.id, 100, iCalObject.property.isLinkedRecurringInstance, true)   // positive change, from In Process to Completed
                                                (iCalObject.property.percent ?: 0) == 100 && offsetX < 0f -> onProgressChanged(iCalObject.property.id, 99, iCalObject.property.isLinkedRecurringInstance, true)   // negative change, from Completed to In Process
                                                (iCalObject.property.percent ?: 0) in 1..99 && offsetX < 0f -> onProgressChanged(iCalObject.property.id, 0, iCalObject.property.isLinkedRecurringInstance, true)   // negative change, from In Process to Needs Action
                                            }
                                        } else {   // VJOURNAL
                                            when {
                                                (iCalObject.property.status == StatusJournal.DRAFT.name && offsetX > 0f) -> onStatusChanged(iCalObject.property.id, StatusJournal.FINAL, iCalObject.property.isLinkedRecurringInstance, true)
                                                (iCalObject.property.status == StatusJournal.FINAL.name && offsetX < 0f) -> onStatusChanged(iCalObject.property.id, StatusJournal.DRAFT, iCalObject.property.isLinkedRecurringInstance, true)
                                            }
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
    JtxBoardTheme {

        val icalobject = ICal4ListWithRelatedto.getSample().apply {
            property.id = 1L
            property.component = Component.VTODO.name
            property.module = Module.TODO.name
            property.percent = 89
            property.status = StatusTodo.`IN-PROCESS`.name
            property.classification = Classification.PUBLIC.name
            property.dtstart = null
            property.due = null
            property.numAttachments = 0
            property.numSubnotes = 0
            property.numSubtasks = 0
            property.summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        }
        val icalobject2 = ICal4ListWithRelatedto.getSample().apply {
            property.id = 2L
            property.component = Component.VTODO.name
            property.module = Module.TODO.name
            property.percent = 89
            property.status = StatusTodo.`NEEDS-ACTION`.name
            property.classification = Classification.CONFIDENTIAL.name
            property.dtstart = System.currentTimeMillis()
            property.due = System.currentTimeMillis()
            property.summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            property.colorItem = Color.Blue.toArgb()
        }
        ListScreenKanban(
            module = Module.TODO,
            listLive = MutableLiveData(listOf(icalobject, icalobject2)),
            scrollOnceId = MutableLiveData(null),
            onProgressChanged = { _, _, _, _ -> },
            onStatusChanged = {_, _, _, _ -> },
            goToView = { },
            goToEdit = { }
        )
    }
}



@Preview(showBackground = true)
@Composable
fun ListScreenKanban_JOURNAL() {
    JtxBoardTheme {

        val icalobject = ICal4ListWithRelatedto.getSample().apply {
            property.id = 1L
            property.component = Component.VJOURNAL.name
            property.module = Module.JOURNAL.name
            property.percent = 89
            property.status = StatusJournal.FINAL.name
            property.classification = Classification.PUBLIC.name
            property.dtstart = null
            property.due = null
            property.numAttachments = 0
            property.numSubnotes = 0
            property.numSubtasks = 0
            property.summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        }
        val icalobject2 = ICal4ListWithRelatedto.getSample().apply {
            property.id = 2L
            property.component = Component.VJOURNAL.name
            property.module = Module.JOURNAL.name
            property.percent = 89
            property.status = StatusJournal.DRAFT.name
            property.classification = Classification.CONFIDENTIAL.name
            property.dtstart = System.currentTimeMillis()
            property.due = System.currentTimeMillis()
            property.summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            property.colorItem = Color.Blue.toArgb()
        }
        ListScreenKanban(
            module = Module.JOURNAL,
            listLive = MutableLiveData(listOf(icalobject, icalobject2)),
            scrollOnceId = MutableLiveData(null),
            onProgressChanged = { _, _, _, _ -> },
            onStatusChanged = {_, _, _, _ -> },
            goToView = { },
            goToEdit = { }
        )
    }
}

