/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import at.techbee.jtx.database.relations.ICal4ListWithRelatedto
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.IcalListFragmentDirections
import at.techbee.jtx.ui.IcalListViewModel


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListScreenGrid(
    listLive: LiveData<List<ICal4ListWithRelatedto>>,
    scrollOnceId: MutableLiveData<Long?>,
    navController: NavController,
    model: IcalListViewModel) {

    val list by listLive.observeAsState(emptyList())

    val scrollId by scrollOnceId.observeAsState(null)
    val gridState = rememberLazyGridState()

    if(scrollId != null) {
        LaunchedEffect(list) {
            val index = list.indexOfFirst { iCalObject -> iCalObject.property.id == scrollId }
            if(index > -1) {
                gridState.animateScrollToItem(index)
                scrollOnceId.postValue(null)
            }
        }
    }

    LazyVerticalGrid(
        //columns = GridCells.Adaptive(150.dp),
        columns = GridCells.Adaptive(150.dp),
        //modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp),
        state = gridState,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = list,
            key = { item -> item.property.id }
        )
        { iCalObject ->

            ListCardSmall(
                iCalObject,
                modifier = Modifier
                    .fillMaxWidth()
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
                    .height(150.dp),
                onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance -> model.updateProgress(itemId, newPercent, isLinkedRecurringInstance)  },
                )
        }
    }
}

