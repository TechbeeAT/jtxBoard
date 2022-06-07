/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.preference.PreferenceManager
import at.techbee.jtx.database.StatusTodo
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.database.relations.ICal4ListWithRelatedto
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.IcalListFragmentDirections
import at.techbee.jtx.ui.IcalListViewModel
import at.techbee.jtx.ui.SettingsFragment


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListScreen(
    listLive: LiveData<List<ICal4ListWithRelatedto>>,
    subtasksLive: LiveData<List<ICal4List>>,
    subnotesLive: LiveData<List<ICal4List>>,
    scrollOnceId: MutableLiveData<Long?>,
    navController: NavController,
    model: IcalListViewModel) {

    val list by listLive.observeAsState(emptyList())
    val subtasks by subtasksLive.observeAsState(emptyList())
    val subnotes by subnotesLive.observeAsState(emptyList())

    val scrollId by scrollOnceId.observeAsState(null)
    val listState = rememberLazyListState()

    val mediaPlayer = MediaPlayer()

    //load settings
    val settings = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)

    val excludeDone by model.isExcludeDone.observeAsState(false)


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
            if(model.searchStatusTodo.isNotEmpty()) // exclude filtered if applicable
                currentSubtasks = currentSubtasks.filter { subtask -> model.searchStatusTodo.contains(StatusTodo.getFromString(subtask.status)) }

            val currentSubnotes = subnotes.filter { subnote ->
                iCalObject.relatedto?.any { relatedto ->
                    relatedto.linkedICalObjectId == subnote.id && relatedto.reltype == Reltype.CHILD.name } == true
            }

            ICalObjectListCard(
                iCalObject,
                currentSubtasks,
                currentSubnotes,
                navController,
                isScrolling = listState.isScrollInProgress,
                settingExpandSubtasks = settings.getBoolean(SettingsFragment.AUTO_EXPAND_SUBTASKS, false),
                settingExpandSubnotes = settings.getBoolean(SettingsFragment.AUTO_EXPAND_SUBNOTES, false),
                settingExpandAttachments = settings.getBoolean(SettingsFragment.AUTO_EXPAND_ATTACHMENTS, false),
                settingShowProgressMaintasks = settings.getBoolean(SettingsFragment.SHOW_PROGRESS_FOR_MAINTASKS_IN_LIST, false),
                settingShowProgressSubtasks = settings.getBoolean(SettingsFragment.SHOW_PROGRESS_FOR_SUBTASKS_IN_LIST, true),
                onEditRequest = { id -> model.postDirectEditEntity(id) },
                onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance -> model.updateProgress(itemId, newPercent, isLinkedRecurringInstance)  },
                player = mediaPlayer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if(iCalObject.property.id == list.last().property.id) 400.dp else 8.dp)
                    .animateItemPlacement()
                    .combinedClickable(
                        onClick = {
                            navController.navigate(
                                IcalListFragmentDirections
                                    .actionIcalListFragmentToIcalViewFragment()
                                    .setItem2show(iCalObject.property.id)
                            )
                        },
                        onLongClick = {
                            if (!iCalObject.property.isReadOnly && BillingManager.getInstance()?.isProPurchased?.value == true)
                                model.postDirectEditEntity(iCalObject.property.id)
                        }
                    )
            )
        }
    }
}

