/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.ui.reusable.elements.FilterSection
import com.google.accompanist.flowlayout.FlowRow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOptionsFilter(
    module: Module,
    listSettings: ListSettings,
    allCollectionsLive: LiveData<List<ICalCollection>>,
    allCategoriesLive: LiveData<List<String>>,
    allResourcesLive: LiveData<List<String>>,
    onListSettingsChanged: () -> Unit,
    modifier: Modifier = Modifier,
    isWidgetConfig: Boolean = false
) {
    val allCollectionsState = allCollectionsLive.observeAsState(emptyList())
    val allCategories by allCategoriesLive.observeAsState(emptyList())
    val allResources by allResourcesLive.observeAsState(emptyList())
    val allCollections by remember { derivedStateOf { allCollectionsState.value.map { it.displayName ?: "" }.sortedBy { it.lowercase() } } }
    val allAccounts by remember { derivedStateOf { allCollectionsState.value.map { it.accountName ?: "" }.distinct().sortedBy { it.lowercase() } } }


    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {


        ////// QuickFilters
        FilterSection(
            icon = Icons.Outlined.FilterAlt,
            headline = stringResource(id = R.string.filter_special),
            onResetSelection = {
                listSettings.isExcludeDone.value = false
                if (module == Module.TODO) {
                    listSettings.isFilterOverdue.value = false
                    listSettings.isFilterDueToday.value = false
                    listSettings.isFilterDueTomorrow.value = false
                    listSettings.isFilterDueFuture.value = false
                    listSettings.isFilterStartInPast.value = false
                    listSettings.isFilterStartToday.value = false
                    listSettings.isFilterStartTomorrow.value = false
                    listSettings.isFilterStartFuture.value = false
                    listSettings.isFilterNoDatesSet.value = false
                }
                onListSettingsChanged()
            },
            onInvertSelection = {
                listSettings.isExcludeDone.value = !listSettings.isExcludeDone.value
                if (module == Module.TODO) {
                    listSettings.isFilterOverdue.value = !listSettings.isFilterOverdue.value
                    listSettings.isFilterDueToday.value = !listSettings.isFilterDueToday.value
                    listSettings.isFilterDueTomorrow.value = !listSettings.isFilterDueTomorrow.value
                    listSettings.isFilterDueFuture.value = !listSettings.isFilterDueFuture.value
                    listSettings.isFilterStartInPast.value = !listSettings.isFilterStartInPast.value
                    listSettings.isFilterStartToday.value = !listSettings.isFilterStartToday.value
                    listSettings.isFilterStartTomorrow.value = !listSettings.isFilterStartTomorrow.value
                    listSettings.isFilterStartFuture.value = !listSettings.isFilterStartFuture.value
                    listSettings.isFilterNoDatesSet.value = !listSettings.isFilterNoDatesSet.value
                }
                onListSettingsChanged()
            })
        {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                FilterChip(
                    selected = listSettings.isExcludeDone.value,
                    onClick = {
                        listSettings.isExcludeDone.value = !listSettings.isExcludeDone.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = R.string.list_hide_completed_tasks)) },
                    modifier = Modifier.padding(end = 4.dp)
                )

                if (module == Module.TODO || module == Module.JOURNAL) {
                    FilterChip(
                        selected = listSettings.isFilterStartInPast.value,
                        onClick = {
                            listSettings.isFilterStartInPast.value = !listSettings.isFilterStartInPast.value
                            onListSettingsChanged()
                        },
                        label = { Text(stringResource(id = if (module == Module.TODO) R.string.list_start_date_in_past else R.string.list_date_start_in_past)) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    FilterChip(
                        selected = listSettings.isFilterStartToday.value,
                        onClick = {
                            listSettings.isFilterStartToday.value =
                                !listSettings.isFilterStartToday.value
                            onListSettingsChanged()
                        },
                        label = { Text(stringResource(id = if (module == Module.TODO) R.string.list_start_date_today else R.string.list_date_today)) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    FilterChip(
                        selected = listSettings.isFilterStartTomorrow.value,
                        onClick = {
                            listSettings.isFilterStartTomorrow.value =
                                !listSettings.isFilterStartTomorrow.value
                            onListSettingsChanged()
                        },
                        label = { Text(stringResource(id = if (module == Module.TODO) R.string.list_start_date_tomorrow else R.string.list_date_tomorrow)) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    FilterChip(
                        selected = listSettings.isFilterStartFuture.value,
                        onClick = {
                            listSettings.isFilterStartFuture.value =
                                !listSettings.isFilterStartFuture.value
                            onListSettingsChanged()
                        },
                        label = { Text(stringResource(id = if (module == Module.TODO) R.string.list_start_date_future else R.string.list_date_future)) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }

                if (module == Module.TODO) {
                    FilterChip(
                        selected = listSettings.isFilterOverdue.value,
                        onClick = {
                            listSettings.isFilterOverdue.value = !listSettings.isFilterOverdue.value
                            onListSettingsChanged()
                        },
                        label = { Text(stringResource(id = R.string.list_due_overdue)) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    FilterChip(
                        selected = listSettings.isFilterDueToday.value,
                        onClick = {
                            listSettings.isFilterDueToday.value =
                                !listSettings.isFilterDueToday.value
                            onListSettingsChanged()
                        },
                        label = { Text(stringResource(id = R.string.list_due_today)) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    FilterChip(
                        selected = listSettings.isFilterDueTomorrow.value,
                        onClick = {
                            listSettings.isFilterDueTomorrow.value =
                                !listSettings.isFilterDueTomorrow.value
                            onListSettingsChanged()
                        },
                        label = { Text(stringResource(id = R.string.list_due_tomorrow)) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    FilterChip(
                        selected = listSettings.isFilterDueFuture.value,
                        onClick = {
                            listSettings.isFilterDueFuture.value =
                                !listSettings.isFilterDueFuture.value
                            onListSettingsChanged()
                        },
                        label = { Text(stringResource(id = R.string.list_due_future)) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    FilterChip(
                        selected = listSettings.isFilterNoDatesSet.value,
                        onClick = {
                            listSettings.isFilterNoDatesSet.value =
                                !listSettings.isFilterNoDatesSet.value
                            onListSettingsChanged()
                        },
                        label = { Text(stringResource(id = R.string.list_no_dates_set)) },
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
                listSettings.isFilterNoCategorySet.value = false
                listSettings.searchCategories.value = emptyList()
                onListSettingsChanged()
            },
            onInvertSelection = {
                listSettings.isFilterNoCategorySet.value = !listSettings.isFilterNoCategorySet.value
                listSettings.searchCategories.value =
                    allCategories.filter { category ->
                        !listSettings.searchCategories.value.contains(category)
                    }
                onListSettingsChanged()
            })
        {
            FlowRow(modifier = Modifier.fillMaxWidth()) {

                FilterChip(
                    selected = listSettings.isFilterNoCategorySet.value,
                    onClick = {
                        listSettings.isFilterNoCategorySet.value = !listSettings.isFilterNoCategorySet.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = R.string.filter_no_category)) },
                    modifier = Modifier.padding(end = 4.dp)
                )

                allCategories.forEach { category ->
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


            ////// ACCOUNTS
            FilterSection(
                icon = Icons.Outlined.AccountBalance,
                headline = stringResource(id = R.string.account),
                onResetSelection = {
                    listSettings.searchAccount.value = emptyList()
                    onListSettingsChanged()
                },
                onInvertSelection = {
                    listSettings.searchAccount.value = allAccounts.toMutableList().apply { removeAll(listSettings.searchAccount.value) }
                    onListSettingsChanged()
                })
            {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    allAccounts.forEach { account ->
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
                    listSettings.searchCollection.value =
                        allCollections
                            .toMutableList()
                            .apply { removeAll(listSettings.searchCollection.value) }
                    onListSettingsChanged()
                })
            {
                FlowRow(modifier = Modifier.fillMaxWidth()) {
                    allCollections.forEach { collection ->
                        FilterChip(
                            selected = listSettings.searchCollection.value.contains(
                                collection
                            ),
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
                    listSettings.searchStatus.value = emptyList()
                    onListSettingsChanged()
                },
                onInvertSelection = {
                        listSettings.searchStatus.value = Status.valuesFor(module)
                            .filter { status -> !listSettings.searchStatus.value.contains(status) }
                    onListSettingsChanged()
                })
            {
                FlowRow(modifier = Modifier.fillMaxWidth()) {

                    Status.valuesFor(module).forEach { status ->
                        FilterChip(
                            selected = listSettings.searchStatus.value.contains(status),
                            onClick = {
                                listSettings.searchStatus.value =
                                    if (listSettings.searchStatus.value.contains(status))
                                        listSettings.searchStatus.value.minus(status)
                                    else
                                        listSettings.searchStatus.value.plus(status)
                                onListSettingsChanged()
                            },
                            label = { Text(stringResource(id = status.stringResource)) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
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
                    listSettings.searchClassification.value = Classification.values()
                        .filter { classification -> !listSettings.searchClassification.value.contains(classification) }
                    onListSettingsChanged()
                })
            {
                FlowRow(modifier = Modifier.fillMaxWidth()) {

                    Classification.values().forEach { classification ->
                        FilterChip(
                            selected = listSettings.searchClassification.value.contains(classification),
                            onClick = {
                                listSettings.searchClassification.value = if (listSettings.searchClassification.value.contains(classification))
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


            ////// RESOURCES
            if (module == Module.TODO) {
                FilterSection(
                    icon = Icons.Outlined.Label,
                    headline = stringResource(id = R.string.resources),
                    onResetSelection = {
                        listSettings.isFilterNoResourceSet.value = false
                        listSettings.searchResources.value = emptyList()
                        onListSettingsChanged()
                    },
                    onInvertSelection = {
                        listSettings.isFilterNoResourceSet.value = !listSettings.isFilterNoResourceSet.value
                        listSettings.searchResources.value =
                            allResources.filter { resource ->
                                !listSettings.searchResources.value.contains(resource)
                            }
                        onListSettingsChanged()
                    })
                {
                    FlowRow(modifier = Modifier.fillMaxWidth()) {

                        FilterChip(
                            selected = listSettings.isFilterNoResourceSet.value,
                            onClick = {
                                listSettings.isFilterNoResourceSet.value = !listSettings.isFilterNoResourceSet.value
                                onListSettingsChanged()
                            },
                            label = { Text(stringResource(id = R.string.filter_no_resource)) },
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        allResources.forEach { resource ->
                            FilterChip(
                                selected = listSettings.searchResources.value.contains(resource),
                                onClick = {
                                    listSettings.searchResources.value =
                                        if (listSettings.searchResources.value.contains(resource))
                                            listSettings.searchResources.value.minus(resource)
                                        else
                                            listSettings.searchResources.value.plus(resource)
                                    onListSettingsChanged()
                                },
                                label = { Text(resource) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            }


            if (!isWidgetConfig) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 16.dp)
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
    }
}


@Preview(showBackground = true)
@Composable
fun ListOptionsFilter_Preview_TODO() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(
            ListViewModel.PREFS_LIST_JOURNALS,
            Context.MODE_PRIVATE
        )

        val listSettings = ListSettings.fromPrefs(prefs)

        ListOptionsFilter(
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
            allResourcesLive = MutableLiveData(listOf("Resource1", "Whatever")),
            onListSettingsChanged = { }

        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListOptionsFilter_Preview_JOURNAL() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(
            ListViewModel.PREFS_LIST_JOURNALS,
            Context.MODE_PRIVATE
        )

        val listSettings = ListSettings.fromPrefs(prefs)

        ListOptionsFilter(
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
            allResourcesLive = MutableLiveData(listOf("Resource1", "Whatever")),
            onListSettingsChanged = { }
        )
    }
}
