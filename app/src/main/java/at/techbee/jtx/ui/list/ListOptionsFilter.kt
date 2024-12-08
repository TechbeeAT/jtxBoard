/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AssignmentLate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.DoneOutline
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.PublishedWithChanges
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredListSetting
import at.techbee.jtx.database.locals.StoredListSettingData
import at.techbee.jtx.ui.reusable.dialogs.DateRangePickerDialog
import at.techbee.jtx.ui.reusable.dialogs.DeleteFilterPresetDialog
import at.techbee.jtx.ui.reusable.dialogs.SaveListSettingsPresetDialog
import at.techbee.jtx.ui.reusable.elements.FilterSection
import at.techbee.jtx.util.DateTimeUtils

const val MAX_ITEMS_PER_SECTION = 5

@Composable
fun ListOptionsFilter(
    module: Module,
    listSettings: ListSettings,
    allCollections: List<ICalCollection>,
    allCategories: List<String>,
    allResources: List<String>,
    storedListSettings: List<StoredListSetting>,
    extendedStatuses: List<ExtendedStatus>,
    onListSettingsChanged: () -> Unit,
    onSaveStoredListSetting: (StoredListSetting) -> Unit,
    onDeleteStoredListSetting: (StoredListSetting) -> Unit,
    modifier: Modifier = Modifier,
    isWidgetConfig: Boolean = false
) {
   val allCollectionsNames = allCollections
       .map { it.displayName ?: "" }
       .sortedBy { it.lowercase() }
    val allAccounts = allCollections
            .map { it.accountName ?: "" }
            .distinct()
            .sortedBy { it.lowercase() }

    var showSaveListSettingsPresetDialog by rememberSaveable { mutableStateOf(false) }

    var showFilterDateRangeStartDialog by rememberSaveable { mutableStateOf(false) }
    var showFilterDateRangeDueDialog by rememberSaveable { mutableStateOf(false) }
    var showFilterDateRangeCompletedDialog by rememberSaveable { mutableStateOf(false) }

    if (showSaveListSettingsPresetDialog) {
        val currentListSettingData = StoredListSettingData.fromListSettings(listSettings)
        val currentListSetting =
            storedListSettings.firstOrNull { it.module == module && it.storedListSettingData == currentListSettingData }
                ?: StoredListSetting(
                    module = module,
                    name = "",
                    storedListSettingData = currentListSettingData
                )
        SaveListSettingsPresetDialog(
            currentSetting = currentListSetting,
            storedListSettings = storedListSettings,
            onConfirm = { newStoredListSetting -> onSaveStoredListSetting(newStoredListSetting) },
            onDismiss = { showSaveListSettingsPresetDialog = false }
        )
    }

    if (showFilterDateRangeStartDialog) {
        DateRangePickerDialog(
            dateRangeStart = listSettings.filterStartRangeStart.value,
            dateRangeEnd = listSettings.filterStartRangeEnd.value,
            onConfirm = { start, end ->
                listSettings.filterStartRangeStart.value = start
                listSettings.filterStartRangeEnd.value = end
                onListSettingsChanged()
            },
            onDismiss = {
                showFilterDateRangeStartDialog = false
            }
        )
    }
    if (showFilterDateRangeDueDialog) {
        DateRangePickerDialog(
            dateRangeStart = listSettings.filterDueRangeStart.value,
            dateRangeEnd = listSettings.filterDueRangeEnd.value,
            onConfirm = { start, end ->
                listSettings.filterDueRangeStart.value = start
                listSettings.filterDueRangeEnd.value = end
                onListSettingsChanged()
            },
            onDismiss = {
                showFilterDateRangeDueDialog = false
            }
        )
    }
    if (showFilterDateRangeCompletedDialog) {
        DateRangePickerDialog(
            dateRangeStart = listSettings.filterCompletedRangeStart.value,
            dateRangeEnd = listSettings.filterCompletedRangeEnd.value,
            onConfirm = { start, end ->
                listSettings.filterCompletedRangeStart.value = start
                listSettings.filterCompletedRangeEnd.value = end
                onListSettingsChanged()
            },
            onDismiss = {
                showFilterDateRangeCompletedDialog = false
            }
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        FilterSection(
            icon = Icons.Outlined.DashboardCustomize,
            headline = stringResource(id = R.string.filter_presets),
            onResetSelection = { },
            onInvertSelection = { },
            showDefaultMenu = false,
            customMenu = {
                if (!isWidgetConfig) {
                    var expanded by remember { mutableStateOf(false) }
                    Row {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(Icons.Outlined.MoreVert, stringResource(id = R.string.more))
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Outlined.Save, null) },
                                text = { Text(stringResource(id = R.string.filter_save_as_preset)) },
                                onClick = {
                                    showSaveListSettingsPresetDialog = true
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        ) {

            FilterChip(
                onClick = {
                    listSettings.reset()
                    onListSettingsChanged()
                },
                label = { Text(stringResource(id = R.string.filter_no_filter)) },
                selected = !listSettings.isFilterActive(),
            )
            var maxEntries by rememberSaveable { mutableIntStateOf(MAX_ITEMS_PER_SECTION) }

            storedListSettings.forEachIndexed { index, storedListSetting ->
                if (index > maxEntries - 1)
                    return@forEachIndexed

                var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

                if (showDeleteDialog) {
                    DeleteFilterPresetDialog(
                        storedListSetting = storedListSetting,
                        onConfirm = { onDeleteStoredListSetting(storedListSetting) },
                        onDismiss = { showDeleteDialog = false }
                    )
                }

                FilterChip(
                    onClick = {
                        storedListSetting.storedListSettingData.applyToListSettings(listSettings)
                        onListSettingsChanged()
                    },
                    label = { Text(storedListSetting.name) },
                    selected = storedListSetting.module == module && storedListSetting.storedListSettingData == StoredListSettingData.fromListSettings(
                        listSettings
                    ),
                    trailingIcon = {

                        if (!isWidgetConfig) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = stringResource(id = R.string.delete),
                                modifier = Modifier.clickable { showDeleteDialog = true }
                            )
                        }
                    }
                )
            }
            if (storedListSettings.size > maxEntries) {
                TextButton(onClick = { maxEntries = Int.MAX_VALUE }) {
                    Text(
                        stringResource(
                            R.string.filter_options_more_entries,
                            storedListSettings.size - maxEntries
                        )
                    )
                }
            }
            if (maxEntries == Int.MAX_VALUE) {
                TextButton(onClick = { maxEntries = MAX_ITEMS_PER_SECTION }) {
                    Text(stringResource(R.string.filter_options_less_entries))
                }
            }
        }

        ////// Special Filters
        FilterSection(
            icon = Icons.Outlined.FilterAlt,
            headline = stringResource(id = R.string.filter_special),
            onResetSelection = {
                listSettings.isExcludeDone.value = false
                listSettings.isFilterNoDatesSet.value = false
                onListSettingsChanged()
            },
            onInvertSelection = {
                listSettings.isExcludeDone.value = !listSettings.isExcludeDone.value
                onListSettingsChanged()
            })
        {
            FilterChip(
                selected = listSettings.isExcludeDone.value,
                onClick = {
                    listSettings.isExcludeDone.value = !listSettings.isExcludeDone.value
                    if (module == Module.TODO || module == Module.JOURNAL)
                        listSettings.isFilterNoDatesSet.value =
                            !listSettings.isFilterNoDatesSet.value
                    onListSettingsChanged()
                },
                label = { Text(stringResource(id = R.string.list_hide_completed_tasks)) }
            )

            if(module == Module.TODO || module == Module.JOURNAL)
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

        ////// Filter by DtStart
        if (module == Module.TODO || module == Module.JOURNAL) {

            FilterSection(
                icon = Icons.Outlined.CalendarMonth,
                headline = stringResource(id = if (module == Module.TODO) R.string.started else R.string.date),
                onResetSelection = {
                    listSettings.isFilterNoStartDateSet.value = false
                    listSettings.isFilterStartInPast.value = false
                    listSettings.isFilterStartToday.value = false
                    listSettings.isFilterStartTomorrow.value = false
                    listSettings.isFilterStartWithin7Days.value = false
                    listSettings.isFilterStartFuture.value = false
                    listSettings.filterStartRangeStart.value = null
                    listSettings.filterStartRangeEnd.value = null
                    onListSettingsChanged()
                },
                onInvertSelection = {
                    listSettings.isFilterNoStartDateSet.value =
                        !listSettings.isFilterNoStartDateSet.value
                    listSettings.isFilterStartInPast.value = !listSettings.isFilterStartInPast.value
                    listSettings.isFilterStartToday.value = !listSettings.isFilterStartToday.value
                    listSettings.isFilterStartTomorrow.value =
                        !listSettings.isFilterStartTomorrow.value
                    listSettings.isFilterStartWithin7Days.value =
                        !listSettings.isFilterStartWithin7Days.value
                    listSettings.isFilterStartFuture.value = !listSettings.isFilterStartFuture.value
                    listSettings.filterStartRangeStart.value = null
                    listSettings.filterStartRangeEnd.value = null
                    onListSettingsChanged()
                })
            {

                FilterChip(
                    selected = listSettings.isFilterStartInPast.value,
                    onClick = {
                        listSettings.isFilterStartInPast.value =
                            !listSettings.isFilterStartInPast.value
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
                    selected = listSettings.isFilterStartWithin7Days.value,
                    onClick = {
                        listSettings.isFilterStartWithin7Days.value =
                            !listSettings.isFilterStartWithin7Days.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = if (module == Module.TODO) R.string.list_start_date_within_7_days else R.string.list_date_within_7_days)) }
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
                    selected = listSettings.filterStartRangeStart.value != null || listSettings.filterStartRangeEnd.value != null,
                    onClick = {
                        showFilterDateRangeStartDialog = true
                    },
                    label = {
                        val dateFrom = listSettings.filterStartRangeStart.value?.let {
                            DateTimeUtils.convertLongToShortDateString(
                                it,
                                null
                            )
                        } ?: "…"
                        val dateTo = listSettings.filterStartRangeEnd.value?.let {
                            DateTimeUtils.convertLongToShortDateString(
                                it,
                                null
                            )
                        } ?: "…"
                        val text = if (module == Module.TODO)
                            stringResource(
                                id = R.string.filter_started_from_to,
                                dateFrom,
                                dateTo
                            )
                        else
                            stringResource(id = R.string.filter_date_from_to, dateFrom, dateTo)
                        Text(text)
                    }
                )
            }
        }


        ////// Filter by Due
        if (module == Module.TODO) {
            FilterSection(
                icon = Icons.Outlined.CalendarMonth,
                headline = stringResource(id = R.string.due),
                onResetSelection = {
                    listSettings.isFilterOverdue.value = false
                    listSettings.isFilterDueToday.value = false
                    listSettings.isFilterDueTomorrow.value = false
                    listSettings.isFilterDueWithin7Days.value = false
                    listSettings.isFilterDueFuture.value = false
                    listSettings.isFilterNoDueDateSet.value = false
                    listSettings.filterDueRangeStart.value = null
                    listSettings.filterDueRangeEnd.value = null
                    onListSettingsChanged()
                },
                onInvertSelection = {
                    listSettings.isFilterOverdue.value = !listSettings.isFilterOverdue.value
                    listSettings.isFilterDueToday.value = !listSettings.isFilterDueToday.value
                    listSettings.isFilterDueTomorrow.value =
                        !listSettings.isFilterDueTomorrow.value
                    listSettings.isFilterDueWithin7Days.value =
                        !listSettings.isFilterDueWithin7Days.value
                    listSettings.isFilterDueFuture.value = !listSettings.isFilterDueFuture.value
                    listSettings.isFilterNoDueDateSet.value =
                        !listSettings.isFilterNoDueDateSet.value
                    listSettings.filterDueRangeStart.value = null
                    listSettings.filterDueRangeEnd.value = null
                    onListSettingsChanged()
                })
            {
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
                    selected = listSettings.isFilterDueWithin7Days.value,
                    onClick = {
                        listSettings.isFilterDueWithin7Days.value =
                            !listSettings.isFilterDueWithin7Days.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = R.string.list_due_within_7_days)) }
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
                    selected = listSettings.isFilterNoDueDateSet.value,
                    onClick = {
                        listSettings.isFilterNoDueDateSet.value =
                            !listSettings.isFilterNoDueDateSet.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = R.string.list_without_due_date)) }
                )

                FilterChip(
                    selected = listSettings.filterDueRangeStart.value != null || listSettings.filterDueRangeEnd.value != null,
                    onClick = {
                        showFilterDateRangeDueDialog = true
                    },
                    label = {
                        val dueFrom = listSettings.filterDueRangeStart.value?.let {
                            DateTimeUtils.convertLongToShortDateString(
                                it,
                                null
                            )
                        } ?: "…"
                        val dueTo = listSettings.filterDueRangeEnd.value?.let {
                            DateTimeUtils.convertLongToShortDateString(
                                it,
                                null
                            )
                        } ?: "…"
                        val text =
                            stringResource(id = R.string.filter_due_from_to, dueFrom, dueTo)
                        Text(text)
                    }
                )
            }
        }

        ////// Filter by Completed
        if (module == Module.TODO) {
            FilterSection(
                icon = Icons.Outlined.DoneOutline,
                headline = stringResource(id = R.string.completed),
                onResetSelection = {
                    listSettings.isFilterNoCompletedDateSet.value = false
                    listSettings.filterCompletedRangeStart.value = null
                    listSettings.filterCompletedRangeEnd.value = null
                    onListSettingsChanged()
                },
                onInvertSelection = {
                    listSettings.isExcludeDone.value = !listSettings.isExcludeDone.value
                    listSettings.isFilterNoCompletedDateSet.value =
                        !listSettings.isFilterNoCompletedDateSet.value
                    listSettings.filterCompletedRangeStart.value = null
                    listSettings.filterCompletedRangeEnd.value = null
                    onListSettingsChanged()
                })
            {

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
                    selected = listSettings.filterCompletedRangeStart.value != null || listSettings.filterCompletedRangeEnd.value != null,
                    onClick = {
                        showFilterDateRangeCompletedDialog = true
                    },
                    label = {
                        val completedFrom = listSettings.filterCompletedRangeStart.value?.let {
                            DateTimeUtils.convertLongToShortDateString(
                                it,
                                null
                            )
                        } ?: "…"
                        val completedTo = listSettings.filterCompletedRangeEnd.value?.let {
                            DateTimeUtils.convertLongToShortDateString(
                                it,
                                null
                            )
                        } ?: "…"
                        val text = stringResource(
                            id = R.string.filter_completed_from_to,
                            completedFrom,
                            completedTo
                        )
                        Text(text)
                    }
                )
            }
        }

        ////// CATEGORIES
        FilterSection(
            icon = Icons.AutoMirrored.Outlined.Label,
            headline = stringResource(id = R.string.category),
            onResetSelection = {
                listSettings.isFilterNoCategorySet.value = false
                listSettings.searchCategories.clear()
                onListSettingsChanged()
            },
            onInvertSelection = {
                listSettings.isFilterNoCategorySet.value = !listSettings.isFilterNoCategorySet.value
                val missing = allCategories.filter { category ->
                    !listSettings.searchCategories.contains(category)
                }
                listSettings.searchCategories.clear()
                listSettings.searchCategories.addAll(missing)
                onListSettingsChanged()
            },
            anyAllNone = listSettings.searchCategoriesAnyAllNone.value,
            onAnyAllNoneChanged = {
                listSettings.searchCategoriesAnyAllNone.value = it
                onListSettingsChanged()
            }
        ) {
            FilterChip(
                selected = listSettings.isFilterNoCategorySet.value,
                onClick = {
                    listSettings.isFilterNoCategorySet.value =
                        !listSettings.isFilterNoCategorySet.value
                    onListSettingsChanged()
                },
                label = { Text(stringResource(id = R.string.filter_no_category)) }
            )

            var maxEntries by rememberSaveable { mutableIntStateOf(MAX_ITEMS_PER_SECTION) }
            val allCategoriesSorted =
                if (allCategories.size > maxEntries) allCategories else allCategories.sortedBy { it.lowercase() }
            allCategoriesSorted.forEachIndexed { index, category ->
                if (index > maxEntries - 1)
                    return@forEachIndexed

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

            if (allCategories.size > maxEntries) {
                TextButton(onClick = { maxEntries = Int.MAX_VALUE }) {
                    Text(
                        stringResource(
                            R.string.filter_options_more_entries,
                            allCategories.size - maxEntries
                        )
                    )
                }
            }
            if (maxEntries == Int.MAX_VALUE) {
                TextButton(onClick = { maxEntries = MAX_ITEMS_PER_SECTION }) {
                    Text(stringResource(R.string.filter_options_less_entries))
                }
            }
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
                val missing =
                    allAccounts.toMutableList().apply { removeAll(listSettings.searchAccount) }
                listSettings.searchAccount.clear()
                listSettings.searchAccount.addAll(missing)
                onListSettingsChanged()
            })
        {
            var maxEntries by rememberSaveable { mutableIntStateOf(MAX_ITEMS_PER_SECTION) }

            allAccounts.forEachIndexed { index, account ->
                if (index > maxEntries - 1)
                    return@forEachIndexed

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

            if (allAccounts.size > maxEntries) {
                TextButton(onClick = { maxEntries = Int.MAX_VALUE }) {
                    Text(
                        stringResource(
                            R.string.filter_options_more_entries,
                            allAccounts.size - maxEntries
                        )
                    )
                }
            }
            if (maxEntries == Int.MAX_VALUE) {
                TextButton(onClick = { maxEntries = MAX_ITEMS_PER_SECTION }) {
                    Text(stringResource(R.string.filter_options_less_entries))
                }
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
                val missing = allCollectionsNames.toMutableList()
                    .apply { removeAll(listSettings.searchCollection) }
                listSettings.searchCollection.clear()
                listSettings.searchCollection.addAll(missing)
                onListSettingsChanged()
            })
        {
            var maxEntries by rememberSaveable { mutableIntStateOf(MAX_ITEMS_PER_SECTION) }

            allCollectionsNames.forEachIndexed { index, collection ->
                if (index > maxEntries - 1)
                    return@forEachIndexed

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

            if (allCollectionsNames.size > maxEntries) {
                TextButton(onClick = { maxEntries = Int.MAX_VALUE }) {
                    Text(
                        stringResource(
                            R.string.filter_options_more_entries,
                            allCollectionsNames.size - maxEntries
                        )
                    )
                }
            }
            if (maxEntries == Int.MAX_VALUE) {
                TextButton(onClick = { maxEntries = MAX_ITEMS_PER_SECTION }) {
                    Text(stringResource(R.string.filter_options_less_entries))
                }
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
                val missing = Status.valuesFor(module)
                    .filter { status -> !listSettings.searchStatus.contains(status) }
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

        if (extendedStatuses.any { it.module == module }) {
            FilterSection(
                icon = Icons.Outlined.PublishedWithChanges,
                headline = stringResource(id = R.string.extended_status),
                onResetSelection = {
                    listSettings.searchXStatus.clear()
                    onListSettingsChanged()
                },
                onInvertSelection = {
                    val missing = extendedStatuses.filter { xstatus ->
                        !listSettings.searchXStatus.contains(xstatus.xstatus)
                    }
                    listSettings.searchXStatus.clear()
                    listSettings.searchXStatus.addAll(missing.map { it.xstatus })
                    onListSettingsChanged()
                })
            {
                var maxEntries by rememberSaveable { mutableIntStateOf(MAX_ITEMS_PER_SECTION) }

                extendedStatuses.filter { it.module == module }.forEachIndexed { index, xstatus ->
                    if (index > maxEntries - 1)
                        return@forEachIndexed

                    FilterChip(
                        selected = listSettings.searchXStatus.contains(xstatus.xstatus),
                        onClick = {
                            if (listSettings.searchXStatus.contains(xstatus.xstatus))
                                listSettings.searchXStatus.remove(xstatus.xstatus)
                            else
                                listSettings.searchXStatus.add(xstatus.xstatus)
                            onListSettingsChanged()
                        },
                        label = { Text(xstatus.xstatus) }
                    )
                }

                if (extendedStatuses.size > maxEntries) {
                    TextButton(onClick = { maxEntries = Int.MAX_VALUE }) {
                        Text(
                            stringResource(
                                R.string.filter_options_more_entries,
                                extendedStatuses.size - maxEntries
                            )
                        )
                    }
                }
                if (maxEntries == Int.MAX_VALUE) {
                    TextButton(onClick = { maxEntries = MAX_ITEMS_PER_SECTION }) {
                        Text(stringResource(R.string.filter_options_less_entries))
                    }
                }
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
                val missing = Classification.entries.filter { classification ->
                    !listSettings.searchClassification.contains(classification)
                }
                listSettings.searchClassification.clear()
                listSettings.searchClassification.addAll(missing)
                onListSettingsChanged()
            })
        {
            Classification.entries.forEach { classification ->
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


        ////// Priority
        val priorities = stringArrayResource(id = R.array.priority)
        if(module == Module.TODO) {
            FilterSection(
                icon = Icons.Outlined.AssignmentLate,
                headline = stringResource(id = R.string.priority),
                onResetSelection = {
                    listSettings.searchPriority.clear()
                    onListSettingsChanged()
                },
                onInvertSelection = {
                    priorities.forEachIndexed { index, _ ->
                        if (listSettings.searchPriority.contains(index))
                            listSettings.searchPriority.remove(index)
                        else
                            listSettings.searchPriority.add(index)
                    }
                    onListSettingsChanged()
                })
            {
                priorities.forEachIndexed { index, prio ->
                    FilterChip(
                        selected = listSettings.searchPriority.contains(index),
                        onClick = {
                            if (listSettings.searchPriority.contains(index))
                                listSettings.searchPriority.remove(index)
                            else
                                listSettings.searchPriority.add(index)
                            onListSettingsChanged()
                        },
                        label = { Text(prio) }
                    )
                }
            }
        }


        ////// RESOURCES
        if (module == Module.TODO) {
            FilterSection(
                icon = Icons.AutoMirrored.Outlined.Label,
                headline = stringResource(id = R.string.resources),
                onResetSelection = {
                    listSettings.isFilterNoResourceSet.value = false
                    listSettings.searchResources.clear()
                    onListSettingsChanged()
                },
                onInvertSelection = {
                    listSettings.isFilterNoResourceSet.value =
                        !listSettings.isFilterNoResourceSet.value
                    val missing = allResources.filter { resource ->
                        !listSettings.searchResources.contains(resource)
                    }
                    listSettings.searchResources.clear()
                    listSettings.searchResources.addAll(missing)
                    onListSettingsChanged()
                },
                anyAllNone = listSettings.searchResourcesAnyAllNone.value,
                onAnyAllNoneChanged = {
                    listSettings.searchResourcesAnyAllNone.value = it
                    onListSettingsChanged()
                }
            ) {
                FilterChip(
                    selected = listSettings.isFilterNoResourceSet.value,
                    onClick = {
                        listSettings.isFilterNoResourceSet.value =
                            !listSettings.isFilterNoResourceSet.value
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = R.string.filter_no_resource)) }
                )

                var maxEntries by rememberSaveable { mutableIntStateOf(MAX_ITEMS_PER_SECTION) }
                val allResourcesSorted =
                    if (allResources.size > maxEntries) allResources else allResources.sortedBy { it.lowercase() }
                allResourcesSorted.forEachIndexed { index, resource ->
                    if (index > maxEntries - 1)
                        return@forEachIndexed

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

                if (allResources.size > maxEntries) {
                    TextButton(onClick = { maxEntries = Int.MAX_VALUE }) {
                        Text(
                            stringResource(
                                R.string.filter_options_more_entries,
                                allResources.size - maxEntries
                            )
                        )
                    }
                }
                if (maxEntries == Int.MAX_VALUE) {
                    TextButton(onClick = { maxEntries = MAX_ITEMS_PER_SECTION }) {
                        Text(stringResource(R.string.filter_options_less_entries))
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
            allCollections = listOf(
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
            ),
            allCategories = listOf("Category1", "#MyHashTag", "Whatever"),
            allResources = listOf("Resource1", "Whatever"),
            extendedStatuses = listOf(
                    ExtendedStatus(
                        "individual",
                        Module.JOURNAL,
                        Status.FINAL,
                        null
                    )
            ),
            storedListSettings = listOf(
                    StoredListSetting(
                        module = Module.JOURNAL,
                        name = "test",
                        storedListSettingData = StoredListSettingData()
                    )
            ),
            onListSettingsChanged = { },
            onSaveStoredListSetting = { },
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
            allCollections =
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

            ),
            allCategories = listOf("Category1", "#MyHashTag", "Whatever"),
            allResources = listOf("Resource1", "Whatever"),
            storedListSettings = listOf(
                    StoredListSetting(
                        module = Module.JOURNAL,
                        name = "test",
                        storedListSettingData = StoredListSettingData()
                    )
            ),
            extendedStatuses = listOf(
                    ExtendedStatus(
                        "individual",
                        Module.JOURNAL,
                        Status.FINAL,
                        null
                    )
            ),
            onListSettingsChanged = { },
            onSaveStoredListSetting = { },
            onDeleteStoredListSetting = { }
        )
    }
}
