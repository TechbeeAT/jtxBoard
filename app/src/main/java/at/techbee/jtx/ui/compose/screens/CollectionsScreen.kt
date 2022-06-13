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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.ui.compose.cards.CollectionCard
import at.techbee.jtx.ui.theme.JtxBoardTheme


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CollectionsScreen(
    collectionsLive: LiveData<List<CollectionsView>>,
    onCollectionChanged: (ICalCollection) -> Unit,
    onCollectionDeleted: (ICalCollection) -> Unit,
    onEntriesMoved: (old: ICalCollection, new: ICalCollection) -> Unit,
    onImportFromICS: (CollectionsView) -> Unit,
    onExportAsICS: (CollectionsView) -> Unit,
    onCollectionClicked: (CollectionsView) -> Unit
) {

    val list by collectionsLive.observeAsState(emptyList())
    val listState = rememberLazyListState()

    val grouped = list.groupBy { it.accountName?:it.accountType?:"Account" }


    Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 16.dp)) {

        Text(stringResource(id = R.string.collections_info), textAlign = TextAlign.Center)

        LazyColumn(
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            state = listState,
        ) {

            grouped.forEach { (account, collectionsInAccount) ->
                stickyHeader {
                    Text(
                        account,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, start = 8.dp, end = 16.dp, bottom = 8.dp)
                    )
                }

                items(
                    items = collectionsInAccount,
                    key = { collection -> collection.collectionId }
                ) { collection ->

                    CollectionCard(
                        collection = collection,
                        allCollections = list,
                        onCollectionChanged = onCollectionChanged,
                        onCollectionDeleted = onCollectionDeleted,
                        onEntriesMoved = onEntriesMoved,
                        onImportFromICS = onImportFromICS,
                        onExportAsICS = onExportAsICS,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .animateItemPlacement()
                            .combinedClickable(
                                onClick = { onCollectionClicked(collection) })
                    )
                }

                /*
                ICalObjectListCard(
                    iCalObject,
                    currentSubtasks,
                    currentSubnotes,
                    navController,
                    isSubtasksExpandedDefault = settings.getBoolean(SettingsFragment.EXPAND_SUBTASKS_DEFAULT, false),
                    isSubnotesExpandedDefault = settings.getBoolean(SettingsFragment.EXPAND_SUBNOTES_DEFAULT, false),
                    isAttachmentsExpandedDefault = settings.getBoolean(SettingsFragment.EXPAND_ATTACHMENTS_DEFAULT, false),
                    settingShowProgressMaintasks = settings.getBoolean(SettingsFragment.SHOW_PROGRESS_FOR_MAINTASKS_IN_LIST, false),
                    settingShowProgressSubtasks = settings.getBoolean(SettingsFragment.SHOW_PROGRESS_FOR_SUBTASKS_IN_LIST, true),
                    onEditRequest = { id -> model.postDirectEditEntity(id) },
                    onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance -> model.updateProgress(itemId, newPercent, isLinkedRecurringInstance)  },
                    onExpandedChanged = { itemId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isAttachmentsExpanded: Boolean -> model.updateExpanded(itemId, isSubtasksExpanded, isSubnotesExpanded, isAttachmentsExpanded)},
                    player = mediaPlayer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
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

                 */
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CollectionsScreen_Preview() {
    JtxBoardTheme {

        val collection1 = CollectionsView(
            collectionId = 1L,
            color = Color.Cyan.toArgb(),
            displayName = "Test",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )

        val collection2 = CollectionsView(
            collectionId = 2L,
            displayName = "Test Number 2",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL",
            numJournals = 5,
            numNotes = 19,
            numTodos = 8989,
            supportsVJOURNAL = true,
            supportsVTODO = true
        )

        val collection3 = CollectionsView(
            collectionId = 3L,
            displayName = "Test",
            description = "Here comes the desc",
            accountName = "Another account",
            accountType = "at.bitfire.davx5"
        )
        CollectionsScreen(
            MutableLiveData(listOf(collection1, collection2, collection3)),
            onCollectionChanged = { },
            onCollectionDeleted = { },
            onEntriesMoved = { _, _ -> },
            onImportFromICS = { },
            onExportAsICS = { },
            onCollectionClicked = { }
        )
    }
}
