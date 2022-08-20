/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.bottomsheets

import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.ListSettings
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.ui.IcalListViewModel
import at.techbee.jtx.ui.OrderBy
import at.techbee.jtx.ui.SortOrder
import at.techbee.jtx.ui.compose.elements.FilterSection
import at.techbee.jtx.ui.compose.elements.HeadlineWithIcon


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    module: Module,
    listSettings: ListSettings,
    allCollectionsLive: LiveData<List<ICalCollection>>,
    allCategoriesLive: LiveData<List<String>>,
    onListSettingsChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allCollections by allCollectionsLive.observeAsState()
    val allCategories by allCategoriesLive.observeAsState()
    val allAccounts = allCollections?.groupBy { it.accountName ?: "" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        ////// ACCOUNTS
        FilterSection(
            icon = Icons.Outlined.AccountBalance,
            headline = stringResource(id = R.string.account),
            onResetSelection = {
                listSettings.searchAccount.value = emptyList()
                onListSettingsChanged()
            },
            onInvertSelection = {
                listSettings.searchAccount.value = allAccounts?.keys?.toMutableList()
                    ?.apply { removeAll(listSettings.searchAccount.value) } ?: emptyList()
                onListSettingsChanged()
            })
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                allAccounts?.keys?.sortedBy { it.lowercase() }?.forEach { account ->
                    FilterChip(
                        selected = listSettings.searchAccount.value.contains(account),
                        onClick = {
                            listSettings.searchAccount.value =
                                if (listSettings.searchAccount.value.contains(account))
                                    listSettings.searchAccount.value.minus(account)
                                else
                                    listSettings.searchAccount.value.plus(account)
                            onListSettingsChanged()
                        },
                        label = { Text(account) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }


        ////// COLLECTIONS
        FilterSection(
            icon = Icons.Outlined.FolderOpen,
            headline = stringResource(id = R.string.collection),
            onResetSelection = {
                listSettings.searchCollection.value = emptyList()
                onListSettingsChanged()
                               },
            onInvertSelection = {
                val allCollectionsGrouped = allCollections?.groupBy { it.displayName ?: "" }
                listSettings.searchCollection.value = allCollectionsGrouped?.keys?.toMutableList()
                    ?.apply { removeAll(listSettings.searchCollection.value) } ?: emptyList()
                onListSettingsChanged()
            })
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {

                val allCollectionsGrouped = allCollections?.groupBy { it.displayName ?: "" }
                allCollectionsGrouped?.keys?.sortedBy { it.lowercase() }?.forEach { collection ->
                    FilterChip(
                        selected = listSettings.searchCollection.value.contains(collection),
                        onClick = {
                            listSettings.searchCollection.value =
                                if (listSettings.searchCollection.value.contains(collection))
                                    listSettings.searchCollection.value.minus(collection)
                                else
                                    listSettings.searchCollection.value.plus(collection)
                            onListSettingsChanged()
                        },
                        label = { Text(collection) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }


        FilterSection(
            icon = Icons.Outlined.PublishedWithChanges,
            headline = stringResource(id = R.string.status),
            onResetSelection = {
                if (module == Module.JOURNAL || module == Module.NOTE)
                    listSettings.searchStatusJournal.value = emptyList()
                else
                    listSettings.searchStatusTodo.value = emptyList()
                onListSettingsChanged()
                               },
            onInvertSelection = {
                if (module == Module.JOURNAL || module == Module.NOTE)
                    listSettings.searchStatusJournal.value = StatusJournal.values().filter { status -> !listSettings.searchStatusJournal.value.contains(status)  }
                else
                    listSettings.searchStatusTodo.value = StatusTodo.values().filter { status -> !listSettings.searchStatusTodo.value.contains(status)  }
                onListSettingsChanged()
            })
        {
            if (module == Module.JOURNAL || module == Module.NOTE) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    StatusJournal.values().forEach { status ->
                        FilterChip(
                            selected = listSettings.searchStatusJournal.value.contains(status),
                            onClick = {
                                listSettings.searchStatusJournal.value =
                                    if (listSettings.searchStatusJournal.value.contains(status))
                                        listSettings.searchStatusJournal.value.minus(status)
                                    else
                                        listSettings.searchStatusJournal.value.plus(status)
                                onListSettingsChanged()
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
                            selected = listSettings.searchStatusTodo.value.contains(status),
                            onClick = {
                                listSettings.searchStatusTodo.value =
                                    if (listSettings.searchStatusTodo.value.contains(status))
                                        listSettings.searchStatusTodo.value.minus(status)
                                    else
                                        listSettings.searchStatusTodo.value.plus(status)
                                onListSettingsChanged()
                            },
                            label = { Text(stringResource(id = status.stringResource)) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }
        }


        ////// CLASSIFICATION
        FilterSection(
            icon = Icons.Outlined.PrivacyTip,
            headline = stringResource(id = R.string.classification),
            onResetSelection = {
                listSettings.searchClassification.value = emptyList()
                onListSettingsChanged()
                               },
            onInvertSelection = {
                listSettings.searchClassification.value = Classification.values().filter { classification -> !listSettings.searchClassification.value.contains(classification)  }
                onListSettingsChanged()
            })
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Classification.values().forEach { classification ->
                    FilterChip(
                        selected = listSettings.searchClassification.value.contains(classification),
                        onClick = {
                            listSettings.searchClassification.value =
                                if (listSettings.searchClassification.value.contains(classification))
                                    listSettings.searchClassification.value.minus(classification)
                                else
                                    listSettings.searchClassification.value.plus(classification)
                            onListSettingsChanged()
                        },
                        label = { Text(stringResource(id = classification.stringResource)) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }



        ////// CATEGORIES
        FilterSection(
            icon = Icons.Outlined.Label,
            headline = stringResource(id = R.string.category),
            onResetSelection = {
                listSettings.searchCategories.value = emptyList()
                onListSettingsChanged()
                               },
            onInvertSelection = {
                listSettings.searchCategories.value = allCategories?.filter { category -> !listSettings.searchCategories.value.contains(category) } ?: emptyList()
                onListSettingsChanged()
            })
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                allCategories?.forEach { category ->
                    FilterChip(
                        selected = listSettings.searchCategories.value.contains(category),
                        onClick = {
                            listSettings.searchCategories.value =
                                if (listSettings.searchCategories.value.contains(category))
                                    listSettings.searchCategories.value.minus(category)
                                else
                                    listSettings.searchCategories.value.plus(category)
                            onListSettingsChanged()
                        },
                        label = { Text(category) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
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

        // SORT ORDER 1
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            OrderBy.getValuesFor(module).forEach { orderBy ->
                FilterChip(
                    selected = listSettings.orderBy.value == orderBy,
                    onClick = {
                        if (listSettings.orderBy.value != orderBy)
                            listSettings.orderBy.value = orderBy
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = orderBy.stringResource)) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            SortOrder.values().forEach { sortOrder ->
                FilterChip(
                    selected = listSettings.sortOrder.value == sortOrder,
                    onClick = {
                        if (listSettings.sortOrder.value != sortOrder)
                            listSettings.sortOrder.value = sortOrder
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = sortOrder.stringResource)) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }


        // SORT ORDER 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 8.dp)
        ) {
            OrderBy.getValuesFor(module).forEach { orderBy ->
                if(orderBy == listSettings.orderBy.value) // don't show criteria that was already selected
                    return@forEach

                FilterChip(
                    selected = listSettings.orderBy2.value == orderBy,
                    onClick = {
                        if (listSettings.orderBy2.value != orderBy)
                            listSettings.orderBy2.value = orderBy
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = orderBy.stringResource)) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            SortOrder.values().forEach { sortOrder ->
                FilterChip(
                    selected = listSettings.sortOrder2.value == sortOrder,
                    onClick = {
                        if (listSettings.sortOrder2.value != sortOrder)
                            listSettings.sortOrder2.value = sortOrder
                        onListSettingsChanged()
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
                .padding(top = 16.dp, bottom = 100.dp)
                .fillMaxWidth()
        ) {
            Button(onClick = {
                listSettings.reset()
                onListSettingsChanged()

            }) {
                Text(stringResource(id = R.string.reset))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FilterBottomSheet_Preview_TODO() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(
            IcalListViewModel.PREFS_LIST_JOURNALS,
            Context.MODE_PRIVATE
        )

        val listSettings = ListSettings(prefs)

        FilterBottomSheet(
            module = Module.TODO,
            listSettings = listSettings,
            allCollectionsLive = MutableLiveData(
                listOf(
                    ICalCollection(
                        collectionId = 1L,
                        displayName = "Collection 1",
                        accountName = "Account 1"
                    ),
                    ICalCollection(
                        collectionId = 2L,
                        displayName = "Collection 2",
                        accountName = "Account 2"
                    )
                )
            ),
            allCategoriesLive = MutableLiveData(listOf("Category1", "#MyHashTag", "Whatever")),
            onListSettingsChanged = { }

        )
    }
}

@Preview(showBackground = true)
@Composable
fun FilterBottomSheet_Preview_JOURNAL() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(
            IcalListViewModel.PREFS_LIST_JOURNALS,
            Context.MODE_PRIVATE
        )

        val listSettings = ListSettings(prefs)

        FilterBottomSheet(
            module = Module.JOURNAL,
            listSettings = listSettings,
            allCollectionsLive = MutableLiveData(
                listOf(
                    ICalCollection(
                        collectionId = 1L,
                        displayName = "Collection 1",
                        accountName = "Account 1"
                    ),
                    ICalCollection(
                        collectionId = 2L,
                        displayName = "Collection 2",
                        accountName = "Account 2"
                    )
                )
            ),
            allCategoriesLive = MutableLiveData(listOf("Category1", "#MyHashTag", "Whatever")),
            onListSettingsChanged = { }
        )
    }
}
