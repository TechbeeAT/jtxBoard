/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.ListSettings
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.ui.OrderBy
import at.techbee.jtx.ui.SortOrder
import at.techbee.jtx.ui.compose.elements.HeadlineWithIcon
import at.techbee.jtx.ui.theme.JtxBoardTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    module: Module,
    listSettingsLive: LiveData<ListSettings>,
    allCollectionsLive: LiveData<List<ICalCollection>>,
    allCategoriesLive: LiveData<List<String>>,
    onListSettingsChanged: (ListSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val allCollections by allCollectionsLive.observeAsState()
    val allCategories by allCategoriesLive.observeAsState()

    val listSettings by listSettingsLive.observeAsState()


    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        ////// ACCOUNTS
        HeadlineWithIcon(
            icon = Icons.Outlined.AccountBalance,
            iconDesc = stringResource(id = R.string.account),
            text = stringResource(id = R.string.account),
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
        ) {

            val allAccounts = allCollections?.groupBy { it.accountName ?: return@Row }
            allAccounts?.keys?.forEach { account ->
                FilterChip(
                    selected = listSettings?.searchAccount?.value?.contains(account) == true,
                    onClick = {
                        listSettings?.let { listSettings ->
                            listSettings.searchAccount.value = if (listSettings.searchAccount.value.contains(account))
                                listSettings.searchAccount.value.minus(account)
                            else
                                listSettings.searchAccount.value.plus(account)
                            onListSettingsChanged(listSettings)
                        }
                    },
                    label = { Text(account) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        ////// COLLECTIONS
        HeadlineWithIcon(
            icon = Icons.Outlined.FolderOpen,
            iconDesc = stringResource(id = R.string.collection),
            text = stringResource(id = R.string.collection),
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
        ) {

            val allCollectionsGrouped = allCollections?.groupBy { it.displayName ?: return@Row }
            allCollectionsGrouped?.keys?.forEach { collection ->
                FilterChip(
                    selected = listSettings?.searchCollection?.value?.contains(collection) == true,
                    onClick = {
                        listSettings?.let { listSettings ->
                            listSettings.searchCollection.value =
                                if (listSettings.searchCollection.value.contains(collection))
                                    listSettings.searchCollection.value.minus(collection)
                                else
                                    listSettings.searchCollection.value.plus(collection)
                            onListSettingsChanged(listSettings)
                        }
                    },
                    label = { Text(collection) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        // STATUS
        HeadlineWithIcon(
            icon = Icons.Outlined.PublishedWithChanges,
            iconDesc = stringResource(id = R.string.status),
            text = stringResource(id = R.string.status),
            modifier = Modifier.padding(top = 8.dp)
        )
        if(module == Module.JOURNAL || module == Module.NOTE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                StatusJournal.values().forEach { status ->
                    FilterChip(
                        selected = listSettings?.searchStatusJournal?.value?.contains(status) == true,
                        onClick = {
                            listSettings?.let { listSettings ->

                                listSettings.searchStatusJournal.value =
                                    if (listSettings.searchStatusJournal.value.contains(status))
                                        listSettings.searchStatusJournal.value.minus(status)
                                    else
                                        listSettings.searchStatusJournal.value.plus(status)
                                onListSettingsChanged(listSettings)
                            }
                        },
                        label = { Text(stringResource(id = status.stringResource)) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                StatusTodo.values().forEach { status ->
                    FilterChip(
                        selected = listSettings?.searchStatusTodo?.value?.contains(status) == true,
                        onClick = {
                            listSettings?.let { listSettings ->

                                listSettings.searchStatusTodo.value =
                                    if (listSettings.searchStatusTodo.value.contains(status))
                                        listSettings.searchStatusTodo.value.minus(status)
                                    else
                                        listSettings.searchStatusTodo.value.plus(status)
                                onListSettingsChanged(listSettings)
                            }
                        },
                        label = { Text(stringResource(id = status.stringResource)) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }

        ////// CLASSIFICATION
        HeadlineWithIcon(
            icon = Icons.Outlined.PrivacyTip,
            iconDesc = stringResource(id = R.string.classification),
            text = stringResource(id = R.string.classification),
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
        ) {
            Classification.values().forEach { classification ->
                FilterChip(
                    selected = listSettings?.searchClassification?.value?.contains(classification) == true,
                    onClick = {
                        listSettings?.let { listSettings ->
                            listSettings.searchClassification.value =
                                if (listSettings.searchClassification.value.contains(classification))
                                    listSettings.searchClassification.value.minus(classification)
                                else
                                    listSettings.searchClassification.value.plus(classification)
                            onListSettingsChanged(listSettings)
                        }
                    },
                    label = { Text(stringResource(id = classification.stringResource)) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        ////// CATEGORIES
        HeadlineWithIcon(
            icon = Icons.Outlined.Label,
            iconDesc = stringResource(id = R.string.category),
            text = stringResource(id = R.string.category),
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
        ) {
            allCategories?.forEach { category ->
                FilterChip(
                    selected = listSettings?.searchCategories?.value?.contains(category) == true,
                    onClick = {
                        listSettings?.let { listSettings ->

                            listSettings.searchCategories.value =
                                if (listSettings.searchCategories.value.contains(category))
                                    listSettings.searchCategories.value.minus(category)
                                else
                                    listSettings.searchCategories.value.plus(category)
                            onListSettingsChanged(listSettings)
                        }
                    },
                    label = { Text(category) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }


        Divider(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            thickness = 1.dp,
            modifier = Modifier
                .alpha(0.25f)
                .padding(top = 8.dp)
        )

        HeadlineWithIcon(
            icon = Icons.Outlined.Sort,
            iconDesc = stringResource(id = R.string.filter_order_by),
            text = stringResource(id = R.string.filter_order_by),
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
        ) {
            OrderBy.getValuesFor(module).forEach { orderBy ->
                FilterChip(
                    selected = listSettings?.orderBy?.value == orderBy,
                    onClick = {
                        if (listSettings?.orderBy?.value != orderBy)
                            listSettings?.orderBy?.value = orderBy
                        listSettings?.let { onListSettingsChanged(it) }
                    },
                    label = { Text(stringResource(id = orderBy.stringResource)) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
        ) {
            SortOrder.values().forEach { sortOrder ->
                FilterChip(
                    selected = listSettings?.sortOrder?.value == sortOrder,
                    onClick = {
                        if (listSettings?.sortOrder?.value != sortOrder)
                            listSettings?.sortOrder?.value = sortOrder
                        listSettings?.let { onListSettingsChanged(it) }
                    },
                    label = { Text(stringResource(id = sortOrder.stringResource)) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Button(onClick = {
                listSettings?.reset()
                listSettings?.let { onListSettingsChanged(it) }

            }) {
                Text(stringResource(id = R.string.reset))
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun FilterScreen_Preview_TODO() {
    JtxBoardTheme {

        val listSettings = MutableLiveData(ListSettings())

        FilterScreen(
            module = Module.TODO,
            listSettingsLive = listSettings,
            allCollectionsLive = MutableLiveData(listOf(
                ICalCollection(collectionId = 1L, displayName = "Collection 1", accountName = "Account 1"),
                ICalCollection(collectionId = 2L, displayName = "Collection 2", accountName = "Account 2")
            )),
            allCategoriesLive = MutableLiveData(listOf("Category1", "#MyHashTag", "Whatever")),
            onListSettingsChanged = { newListSettings -> listSettings.postValue(newListSettings) }

        )
    }
}

@Preview(showBackground = true)
@Composable
fun FilterScreen_Preview_JOURNAL() {
    JtxBoardTheme {

        val listSettings = MutableLiveData(ListSettings())

        FilterScreen(
            module = Module.JOURNAL,
            listSettingsLive = listSettings,
            allCollectionsLive = MutableLiveData(listOf(
                ICalCollection(collectionId = 1L, displayName = "Collection 1", accountName = "Account 1"),
                ICalCollection(collectionId = 2L, displayName = "Collection 2", accountName = "Account 2")
            )),
            allCategoriesLive = MutableLiveData(listOf("Category1", "#MyHashTag", "Whatever")),
            onListSettingsChanged = { newListSettings -> listSettings.postValue(newListSettings) }
        )
    }
}
