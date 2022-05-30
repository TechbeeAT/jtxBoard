/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.database.relations.ICal4ListWithRelatedto
import at.techbee.jtx.database.views.ICal4List


@Composable
fun ListScreen(listLive: LiveData<List<ICal4ListWithRelatedto>>, subtasksLive: LiveData<List<ICal4List>>, navController: NavController) {

    val list by listLive.observeAsState(emptyList())
    val subtasks by subtasksLive.observeAsState(emptyList())

    LazyColumn(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp)
    ) {
        items(
            items = list,
            key = { item -> item.property.id }
        ) { iCalObject ->

            val currentSubtasks = subtasks.filter { subtask ->
                iCalObject.relatedto?.any { relatedto ->
                    relatedto.linkedICalObjectId == subtask.id && relatedto.reltype == Reltype.CHILD.name
                } == true
            }


            ICalObjectListCard(iCalObject, currentSubtasks, navController)
        }
    }
}

