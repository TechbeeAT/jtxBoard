/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.screens

import android.media.MediaPlayer
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.database.relations.ICal4ListWithRelatedto
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.SettingsFragment
import at.techbee.jtx.ui.compose.cards.ICalObjectListCard
import at.techbee.jtx.ui.theme.JtxBoardTheme


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListScreenList(
    listLive: LiveData<List<ICal4ListWithRelatedto>>,
    subtasksLive: LiveData<List<ICal4List>>,
    subnotesLive: LiveData<List<ICal4List>>,
    scrollOnceId: MutableLiveData<Long?>,
    isExcludeDone: MutableLiveData<Boolean>,
    goToView: (itemId: Long) -> Unit,
    goToEdit: (itemId: Long) -> Unit,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit,
    onExpandedChanged: (itemId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isAttachmentsExpanded: Boolean) -> Unit
) {

    val list by listLive.observeAsState(emptyList())
    val subtasks by subtasksLive.observeAsState(emptyList())
    val subnotes by subnotesLive.observeAsState(emptyList())

    val scrollId by scrollOnceId.observeAsState(null)
    val listState = rememberLazyListState()

    val mediaPlayer = MediaPlayer()

    //load settings
    val settings = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)

    val excludeDone by isExcludeDone.observeAsState(false)


    if(scrollId != null) {
        LaunchedEffect(list) {
            val index = list.indexOfFirst { iCalObject -> iCalObject.property.id == scrollId }
            if(index > -1) {
                listState.animateScrollToItem(index)
                scrollOnceId.postValue(null)
            }
        }
    }


    LazyColumn(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp),
        state = listState,
    ) {
        items(
            items = list,
            key = { item -> item.property.id }
        ) { iCalObject ->

            var currentSubtasks = subtasks.filter { subtask ->
                iCalObject.relatedto?.any { relatedto ->
                    relatedto.linkedICalObjectId == subtask.id && relatedto.reltype == Reltype.CHILD.name } == true
            }
            if(excludeDone)   // exclude done if applicable
                currentSubtasks = currentSubtasks.filter { subtask -> subtask.percent != 100 }
            /*
            if(model.searchStatusTodo.isNotEmpty()) // exclude filtered if applicable
                currentSubtasks = currentSubtasks.filter { subtask -> model.searchStatusTodo.contains(StatusTodo.getFromString(subtask.status)) }
             */

            val currentSubnotes = subnotes.filter { subnote ->
                iCalObject.relatedto?.any { relatedto ->
                    relatedto.linkedICalObjectId == subnote.id && relatedto.reltype == Reltype.CHILD.name } == true
            }

            ICalObjectListCard(
                iCalObject,
                currentSubtasks,
                currentSubnotes,
                isSubtasksExpandedDefault = settings.getBoolean(SettingsFragment.EXPAND_SUBTASKS_DEFAULT, false),
                isSubnotesExpandedDefault = settings.getBoolean(SettingsFragment.EXPAND_SUBNOTES_DEFAULT, false),
                isAttachmentsExpandedDefault = settings.getBoolean(SettingsFragment.EXPAND_ATTACHMENTS_DEFAULT, false),
                settingShowProgressMaintasks = settings.getBoolean(SettingsFragment.SHOW_PROGRESS_FOR_MAINTASKS_IN_LIST, false),
                settingShowProgressSubtasks = settings.getBoolean(SettingsFragment.SHOW_PROGRESS_FOR_SUBTASKS_IN_LIST, true),
                goToView = goToView,
                goToEdit = goToEdit,
                onProgressChanged = onProgressChanged,
                onExpandedChanged = onExpandedChanged,
                player = mediaPlayer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .animateItemPlacement()
                    .combinedClickable(
                        onClick = { goToView(iCalObject.property.id) },
                        onLongClick = {
                            if (!iCalObject.property.isReadOnly && BillingManager.getInstance()?.isProPurchased?.value == true)
                                goToEdit(iCalObject.property.id)
                        }
                    )
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun ListScreenList_TODO() {
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
            property.status = StatusTodo.`IN-PROCESS`.name
            property.classification = Classification.CONFIDENTIAL.name
            property.dtstart = System.currentTimeMillis()
            property.due = System.currentTimeMillis()
            property.summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            property.colorItem = Color.Blue.toArgb()
        }
        ListScreenList(
            listLive = MutableLiveData(listOf(icalobject, icalobject2)),
            subtasksLive = MutableLiveData(emptyList()),
            scrollOnceId = MutableLiveData(null),
            onProgressChanged = { _, _, _ -> },
            goToView = { },
            goToEdit = { },
            isExcludeDone = MutableLiveData(false),
            subnotesLive = MutableLiveData(emptyList()),
            onExpandedChanged = { _, _, _, _ -> }
        )
    }
}



@Preview(showBackground = true)
@Composable
fun ListScreenList_JOURNAL() {
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
            property.status = StatusTodo.`IN-PROCESS`.name
            property.classification = Classification.CONFIDENTIAL.name
            property.dtstart = System.currentTimeMillis()
            property.due = System.currentTimeMillis()
            property.summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            property.colorItem = Color.Blue.toArgb()
        }
        ListScreenList(
            listLive = MutableLiveData(listOf(icalobject, icalobject2)),
            subtasksLive = MutableLiveData(emptyList()),
            scrollOnceId = MutableLiveData(null),
            onProgressChanged = { _, _, _ -> },
            goToView = { },
            goToEdit = { },
            isExcludeDone = MutableLiveData(false),
            subnotesLive = MutableLiveData(emptyList()),
            onExpandedChanged = { _, _, _, _ -> }
        )
    }
}

