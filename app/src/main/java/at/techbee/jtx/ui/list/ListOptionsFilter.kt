/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import at.techbee.jtx.database.locals.StoredListSetting
import at.techbee.jtx.database.locals.StoredListSettingData
import at.techbee.jtx.database.locals.StoredStatus
import at.techbee.jtx.ui.reusable.dialogs.SaveListSettingsPresetDialog
import at.techbee.jtx.ui.reusable.elements.FilterSection


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOptionsFilter(
    module: Module,
    listSettings: ListSettings,
    allCollectionsLive: LiveData<List<ICalCollection>>,
    allCategoriesLive: LiveData<List<String>>,
    allResourcesLive: LiveData<List<String>>,
    storedListSettingLive: LiveData<List<StoredListSetting>>,
    storedStatusesLive: LiveData<List<StoredStatus>>,
    onListSettingsChanged: () -> Unit,
    onSaveStoredListSetting: (String, StoredListSettingData) -> Unit,
    onDeleteStoredListSetting: (StoredListSetting) -> Unit,
    modifier: Modifier = Modifier,
    isWidgetConfig: Boolean = false
) {
    val allCollectionsState = allCollectionsLive.observeAsState(emptyList())
    val allCategories by allCategoriesLive.observeAsState(emptyList())
    val allResources by allResourcesLive.observeAsState(emptyList())
    val allCollections by remember { derivedStateOf { allCollectionsState.value.map { it.displayName ?: "" }.sortedBy { it.lowercase() } } }
    val allAccounts by remember { derivedStateOf { allCollectionsState.value.map { it.accountName ?: "" }.distinct().sortedBy { it.lowercase() } } }
    val storedListSettings by storedListSettingLive.observeAsState(emptyList())
    val storedStatuses by storedStatusesLive.observeAsState(initial = emptyList())
    var showSaveListSettingsPresetDialog by remember { mutableStateOf(false) }

    if(showSaveListSettingsPresetDialog) {
        SaveListSettingsPresetDialog(
            onConfirm = { name ->  onSaveStoredListSetting(name, StoredListSettingData.fromListSettings(listSettings)) },
            onDismiss = { showSaveListSettingsPresetDialog = false }
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        AnimatedVisibility(storedListSettings.isNotEmpty()) {
            FilterSection(
                icon = Icons.Outlined.DashboardCustomize,
                headline = stringResource(id = R.string.filter_presets),
                onResetSelection = { },
                onInvertSelection = { },
                showMenu = false
            ) {
                storedListSettings.forEach { storedListSetting ->
                    var expanded by remember { mutableStateOf(false) }

                    FilterChip(
                        onClick = {
                            storedListSetting.storedListSettingData.applyToListSettings(listSettings)
                            onListSettingsChanged()
                                  },
                        label = { Text(storedListSetting.name) },
                        selected = false,
                        trailingIcon = {

                            if(!isWidgetConfig) {
                                Icon(
                                    Icons.Outlined.ChevronRight,
                                    contentDescription = stringResource(id = R.string.more),
                                    modifier = Modifier.clickable { expanded = true }
                                )

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(Icons.Outlined.Check, null) },
                                        text = { Text(stringResource(id = R.string.apply)) },
                                        onClick = {
                                            storedListSetting.storedListSettingData.applyToListSettings(listSettings)
                                            onListSettingsChanged()
                                            expanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(Icons.Outlined.Close, null) },
                                        text = { Text(stringResource(id = R.string.delete)) },
                                        onClick = {
                                            onDeleteStoredListSetting(storedListSetting)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
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
                    listSettings.isFilterNoStartDateSet.value = false
                    listSettings.isFilterNoDueDateSet.value = false
                    listSettings.isFilterNoCompletedDateSet.value = false
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
                    listSettings.isFilterNoStartDateSet.value = !listSettings.isFilterNoStartDateSet.value
                    listSettings.isFilterNoDueDateSet.value = !listSettings.isFilterNoDueDateSet.value
                    listSettings.isFilterNoCompletedDateSet.value = !listSettings.isFilterNoCompletedDateSet.value
                }
                onListSettingsChanged()
            })
        {
            FilterChip(
                selected = listSettings.isExcludeDone.value,
                onClick = {
                    listSettings.isExcludeDone.value = !listSettings.isExcludeDone.value
                    onListSettingsChanged()
                },
                label = { Text(stringResource(id = R.string.list_hide_completed_tasks)) }
            )

            if (module == Module.TODO || module == Module.JOURNAL) {
                FilterChip(
                    selected = listSettings.isFilterStartInPast.value,
                    onClick = {
                        listSettings.isFilterStartInPast.value = !listSettings.isFilterStartInPast.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = if (module == Module.TODO) R.string.list_start_date_in_past else R.string.list_date_start_in_past)) }
                )
                FilterChip(
                    selected = listSettings.isFilterStartToday.value,
                    onClick = {
                        listSettings.isFilterStartToday.value =
                            !listSettings.isFilterStartToday.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = if (module == Module.TODO) R.string.list_start_date_today else R.string.list_date_today)) }
                )
                FilterChip(
                    selected = listSettings.isFilterStartTomorrow.value,
                    onClick = {
                        listSettings.isFilterStartTomorrow.value =
                            !listSettings.isFilterStartTomorrow.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = if (module == Module.TODO) R.string.list_start_date_tomorrow else R.string.list_date_tomorrow)) }
                )
                FilterChip(
                    selected = listSettings.isFilterStartFuture.value,
                    onClick = {
                        listSettings.isFilterStartFuture.value =
                            !listSettings.isFilterStartFuture.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = if (module == Module.TODO) R.string.list_start_date_future else R.string.list_date_future)) }
                )
            }

            if (module == Module.TODO) {
                FilterChip(
                    selected = listSettings.isFilterOverdue.value,
                    onClick = {
                        listSettings.isFilterOverdue.value = !listSettings.isFilterOverdue.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = R.string.list_due_overdue)) }
                )
                FilterChip(
                    selected = listSettings.isFilterDueToday.value,
                    onClick = {
                        listSettings.isFilterDueToday.value =
                            !listSettings.isFilterDueToday.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = R.string.list_due_today)) }
                )
                FilterChip(
                    selected = listSettings.isFilterDueTomorrow.value,
                    onClick = {
                        listSettings.isFilterDueTomorrow.value =
                            !listSettings.isFilterDueTomorrow.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = R.string.list_due_tomorrow)) }
                )
                FilterChip(
                    selected = listSettings.isFilterDueFuture.value,
                    onClick = {
                        listSettings.isFilterDueFuture.value =
                            !listSettings.isFilterDueFuture.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = R.string.list_due_future)) }
                )
                FilterChip(
                    selected = listSettings.isFilterNoStartDateSet.value,
                    onClick = {
                        listSettings.isFilterNoStartDateSet.value =
                            !listSettings.isFilterNoStartDateSet.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = R.string.list_without_start_date)) }
                )
                FilterChip(
                    selected = listSettings.isFilterNoDueDateSet.value,
                    onClick = {
                        listSettings.isFilterNoDueDateSet.value =
                            !listSettings.isFilterNoDueDateSet.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = R.string.list_without_due_date)) }
                )
                FilterChip(
                    selected = listSettings.isFilterNoCompletedDateSet.value,
                    onClick = {
                        listSettings.isFilterNoCompletedDateSet.value =
                            !listSettings.isFilterNoCompletedDateSet.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = R.string.list_without_completed_date)) }
                )
                FilterChip(
                    selected = listSettings.isFilterNoDatesSet.value,
                    onClick = {
                        listSettings.isFilterNoDatesSet.value =
                            !listSettings.isFilterNoDatesSet.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = R.string.list_no_dates_set)) }
                )
            }
        }

        ////// CATEGORIES
        FilterSection(
            icon = Icons.Outlined.Label,
            headline = stringResource(id = R.string.category),
            onResetSelection = {
                listSettings.isFilterNoCategorySet.value = false
                listSettings.searchCategories.clear()
                onListSettingsChanged()
            },
            onInvertSelection = {
                listSettings.isFilterNoCategorySet.value = !listSettings.isFilterNoCategorySet.value
                val missing = allCategories.filter { category -> !listSettings.searchCategories.contains(category) }
                listSettings.searchCategories.clear()
                listSettings.searchCategories.addAll(missing)
                onListSettingsChanged()
            })
        {
            FilterChip(
                selected = listSettings.isFilterNoCategorySet.value,
                onClick = {
                    listSettings.isFilterNoCategorySet.value = !listSettings.isFilterNoCategorySet.value
                    onListSettingsChanged()
                },
                label = { Text(stringResource(id = R.string.filter_no_category)) }
            )

            allCategories.forEach { category ->
                FilterChip(
                    selected = listSettings.searchCategories.contains(category),
                    onClick = {
                        if (listSettings.searchCategories.contains(category))
                            listSettings.searchCategories.remove(category)
                        else
                            listSettings.searchCategories.add(category)
                        onListSettingsChanged()
                    },
                    label = { Text(category) }
                )
            }

            ////// ACCOUNTS
            FilterSection(
                icon = Icons.Outlined.AccountBalance,
                headline = stringResource(id = R.string.account),
                onResetSelection = {
                    listSettings.searchAccount.clear()
                    onListSettingsChanged()
                },
                onInvertSelection = {
                    val missing = allAccounts.toMutableList().apply { removeAll(listSettings.searchAccount) }
                    listSettings.searchAccount.clear()
                    listSettings.searchAccount.addAll(missing)
                    onListSettingsChanged()
                })
            {
                allAccounts.forEach { account ->
                    FilterChip(
                        selected = listSettings.searchAccount.contains(account),
                        onClick = {
                            if (listSettings.searchAccount.contains(account))
                                listSettings.searchAccount.remove(account)
                            else
                                listSettings.searchAccount.add(account)
                            onListSettingsChanged()
                        },
                        label = { Text(account) }
                    )
                }
            }

            ////// COLLECTIONS
            FilterSection(
                icon = Icons.Outlined.FolderOpen,
                headline = stringResource(id = R.string.collection),
                onResetSelection = {
                    listSettings.searchCollection.clear()
                    onListSettingsChanged()
                },
                onInvertSelection = {
                    val missing = allCollections.toMutableList().apply { removeAll(listSettings.searchCollection) }
                    listSettings.searchCollection.clear()
                    listSettings.searchCollection.addAll(missing)
                    onListSettingsChanged()
                })
            {
                allCollections.forEach { collection ->
                    FilterChip(
                        selected = listSettings.searchCollection.contains(collection),
                        onClick = {
                            if (listSettings.searchCollection.contains(collection))
                                listSettings.searchCollection.remove(collection)
                            else
                                listSettings.searchCollection.add(collection)
                            onListSettingsChanged()
                        },
                        label = { Text(collection) }
                    )
                }
            }


            FilterSection(
                icon = Icons.Outlined.PublishedWithChanges,
                headline = stringResource(id = R.string.status),
                onResetSelection = {
                    listSettings.searchStatus.clear()
                    onListSettingsChanged()
                },
                onInvertSelection = {
                    val missing = Status.valuesFor(module).filter { status -> !listSettings.searchStatus.contains(status)}
                    listSettings.searchStatus.clear()
                    listSettings.searchStatus.addAll(missing)
                    onListSettingsChanged()
                })
            {
                Status.valuesFor(module).forEach { status ->
                    FilterChip(
                        selected = listSettings.searchStatus.contains(status),
                        onClick = {
                                if (listSettings.searchStatus.contains(status))
                                    listSettings.searchStatus.remove(status)
                                else
                                    listSettings.searchStatus.add(status)
                            onListSettingsChanged()
                        },
                        label = { Text(stringResource(id = status.stringResource)) }
                    )
                }
            }


            ////// CLASSIFICATION
            FilterSection(
                icon = Icons.Outlined.PrivacyTip,
                headline = stringResource(id = R.string.classification),
                onResetSelection = {
                    listSettings.searchClassification.clear()
                    onListSettingsChanged()
                },
                onInvertSelection = {
                    val missing = Classification.values().filter { classification -> !listSettings.searchClassification.contains(classification) }
                    listSettings.searchClassification.clear()
                    listSettings.searchClassification.addAll(missing)
                    onListSettingsChanged()
                })
            {
                Classification.values().forEach { classification ->
                    FilterChip(
                        selected = listSettings.searchClassification.contains(classification),
                        onClick = {
                            if (listSettings.searchClassification.contains(classification))
                                    listSettings.searchClassification.remove(classification)
                                else
                                    listSettings.searchClassification.add(classification)
                            onListSettingsChanged()
                        },
                        label = { Text(stringResource(id = classification.stringResource)) }
                    )
                }
            }


            ////// RESOURCES
            if (module == Module.TODO) {
                FilterSection(
                    icon = Icons.Outlined.Label,
                    headline = stringResource(id = R.string.resources),
                    onResetSelection = {
                        listSettings.isFilterNoResourceSet.value = false
                        listSettings.searchResources.clear()
                        onListSettingsChanged()
                    },
                    onInvertSelection = {
                        listSettings.isFilterNoResourceSet.value = !listSettings.isFilterNoResourceSet.value
                        val missing = allResources.filter { resource -> !listSettings.searchResources.contains(resource) }
                        listSettings.searchResources.clear()
                        listSettings.searchResources.addAll(missing)
                        onListSettingsChanged()
                    })
                {
                    FilterChip(
                        selected = listSettings.isFilterNoResourceSet.value,
                        onClick = {
                            listSettings.isFilterNoResourceSet.value = !listSettings.isFilterNoResourceSet.value
                            onListSettingsChanged()
                        },
                        label = { Text(stringResource(id = R.string.filter_no_resource)) }
                    )

                    allResources.forEach { resource ->
                        FilterChip(
                            selected = listSettings.searchResources.contains(resource),
                            onClick = {
                                if (listSettings.searchResources.contains(resource))
                                    listSettings.searchResources.remove(resource)
                                else
                                    listSettings.searchResources.add(resource)
                                onListSettingsChanged()
                            },
                            label = { Text(resource) }
                        )
                    }
                }
            }


            if (!isWidgetConfig) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                    horizontalAlignment = Alignment.CenterHorizontally,
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

                    Button(onClick = { showSaveListSettingsPresetDialog = true}) {
                        Text(stringResource(R.string.filter_save_as_preset))
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
            storedStatusesLive = MutableLiveData(listOf(StoredStatus("individual", Module.JOURNAL, Status.FINAL, null))),
            storedListSettingLive = MutableLiveData(listOf(StoredListSetting(module = Module.JOURNAL, name = "test", storedListSettingData = StoredListSettingData()))),
            onListSettingsChanged = { },
            onSaveStoredListSetting = { _, _ -> },
            onDeleteStoredListSetting = { }
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
            storedListSettingLive = MutableLiveData(listOf(StoredListSetting(module = Module.JOURNAL, name = "test", storedListSettingData = StoredListSettingData()))),
            storedStatusesLive = MutableLiveData(listOf(StoredStatus("individual", Module.JOURNAL, Status.FINAL, null))),
            onListSettingsChanged = { },
            onSaveStoredListSetting = { _, _ -> },
            onDeleteStoredListSetting = { }
        )
    }
}
