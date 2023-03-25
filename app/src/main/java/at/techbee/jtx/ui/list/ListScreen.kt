/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list


import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.reusable.destinations.DetailDestination
import at.techbee.jtx.ui.settings.SettingsStateHolder


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListScreen(
    listViewModel: ListViewModel,
    navController: NavController,
    filterBottomSheetState: ModalBottomSheetState,
    isAuthenticated: Boolean
) {
    val context = LocalContext.current
    val settingsStateHolder = SettingsStateHolder(context)

    listViewModel.toastMessage.value?.let {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        listViewModel.toastMessage.value = null
    }

    val list = listViewModel.iCal4List.observeAsState(emptyList())

    // first apply a proper sort order, then group
    val sortedList = when (listViewModel.listSettings.groupBy.value) {
        GroupBy.STATUS -> list.value.sortedBy {
            if (listViewModel.module == Module.TODO && it.percent != 100)
                try {
                    Status.valueOf(it.status ?: Status.NO_STATUS.name).ordinal
                } catch (e: java.lang.IllegalArgumentException) {
                    -1
                }
            else
                try {
                    Status.valueOf(it.status ?: Status.FINAL.name).ordinal
                } catch (e: java.lang.IllegalArgumentException) {
                    -1
                }
        }.let { if (listViewModel.listSettings.sortOrder.value == SortOrder.DESC) it.asReversed() else it }
        GroupBy.CLASSIFICATION -> list.value.sortedBy {
            try {
                Classification.valueOf(it.classification ?: Classification.PUBLIC.name).ordinal
            } catch (e: java.lang.IllegalArgumentException) {
                -1
            }
        }.let { if (listViewModel.listSettings.sortOrder.value == SortOrder.DESC) it.asReversed() else it }
        else -> list.value
    }

    val groupedList = sortedList.groupBy {
        when (listViewModel.listSettings.groupBy.value) {
            GroupBy.STATUS -> Status.values().find { status ->  status.status == it.status }?.stringResource?.let { stringRes -> stringResource(id = stringRes)}?: it.status?:""
            GroupBy.CLASSIFICATION -> Classification.values().find { classif ->  classif.classification == it.classification }?.stringResource?.let { stringResource(id = it)}?: it.classification?:""
            GroupBy.PRIORITY -> {
                when (it.priority) {
                    null -> stringArrayResource(id = R.array.priority)[0]
                    in 0..9 -> stringArrayResource(id = R.array.priority)[it.priority!!]
                    else -> it.priority.toString()
                }
            }
            GroupBy.DATE -> ICalObject.getDtstartTextInfo(module = Module.JOURNAL, dtstart = it.dtstart, dtstartTimezone = it.dtstartTimezone, daysOnly = true, context = LocalContext.current)
            GroupBy.START -> ICalObject.getDtstartTextInfo(module = Module.TODO, dtstart = it.dtstart, dtstartTimezone = it.dtstartTimezone, daysOnly = true, context = LocalContext.current)
            GroupBy.DUE -> ICalObject.getDueTextInfo(status = it.status, due = it.due, dueTimezone = it.dueTimezone, percent = it.percent, daysOnly = true, context = LocalContext.current)
            else -> {
                it.module
            }
        }
    }

    fun processOnClick(itemId: Long, ical4list: List<ICal4List>) {
        if (listViewModel.multiselectEnabled.value)
            if (listViewModel.selectedEntries.contains(itemId)) listViewModel.selectedEntries.remove(itemId) else listViewModel.selectedEntries.add(itemId)
        else
            navController.navigate(DetailDestination.Detail.getRoute(itemId, ical4list.map { it.id }, false))
    }

    fun processOnLongClick(itemId: Long, ical4list: List<ICal4List>) {
        if (!listViewModel.multiselectEnabled.value && BillingManager.getInstance().isProPurchased.value == true)
            navController.navigate(DetailDestination.Detail.getRoute(itemId, ical4list.map { it.id }, true))
    }

    fun processOnProgressChanged(itemId: Long, newPercent: Int, scrollOnce: Boolean = false) {
        listViewModel.updateProgress(
            itemId,
            newPercent,
            scrollOnce
        )
    }

    Column {
        when (listViewModel.listSettings.viewMode.value) {
            ViewMode.LIST -> {
                ListScreenList(
                    groupedList = groupedList,
                    subtasksLive = listViewModel.allSubtasks,
                    subnotesLive = listViewModel.allSubnotes,
                    selectedEntries = listViewModel.selectedEntries,
                    attachmentsLive = listViewModel.allAttachmentsMap,
                    scrollOnceId = listViewModel.scrollOnceId,
                    listSettings = listViewModel.listSettings,
                    isSubtasksExpandedDefault = settingsStateHolder.settingAutoExpandSubtasks,
                    isSubnotesExpandedDefault = settingsStateHolder.settingAutoExpandSubnotes,
                    isAttachmentsExpandedDefault = settingsStateHolder.settingAutoExpandAttachments,
                    settingShowProgressMaintasks = settingsStateHolder.settingShowProgressForMainTasks,
                    settingShowProgressSubtasks = settingsStateHolder.settingShowProgressForSubTasks,
                    settingProgressIncrement = settingsStateHolder.settingStepForProgress,
                    settingLinkProgressToSubtasks = settingsStateHolder.settingLinkProgressToSubtasks.value,
                    onClick = { itemId, ical4list -> processOnClick(itemId, ical4list) },
                    onLongClick = { itemId, ical4list -> processOnLongClick(itemId, ical4list) },
                    onProgressChanged = { itemId, newPercent ->
                        processOnProgressChanged(itemId, newPercent)
                    },
                    onExpandedChanged = { itemId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isAttachmentsExpanded: Boolean ->
                        listViewModel.updateExpanded(
                            itemId,
                            isSubtasksExpanded,
                            isSubnotesExpanded,
                            isAttachmentsExpanded
                        )
                    },
                )
            }
            ViewMode.GRID -> {
                ListScreenGrid(
                    list = list,
                    subtasksLive = listViewModel.allSubtasks,
                    selectedEntries = listViewModel.selectedEntries,
                    scrollOnceId = listViewModel.scrollOnceId,
                    settingLinkProgressToSubtasks = settingsStateHolder.settingLinkProgressToSubtasks.value,
                    onClick = { itemId, ical4list -> processOnClick(itemId, ical4list) },
                    onLongClick = { itemId, ical4list -> processOnLongClick(itemId, ical4list) },
                    onProgressChanged = { itemId, newPercent ->
                        processOnProgressChanged(itemId, newPercent)
                    }
                )
            }
            ViewMode.COMPACT -> {
                ListScreenCompact(
                    groupedList = groupedList,
                    subtasksLive = listViewModel.allSubtasks,
                    selectedEntries = listViewModel.selectedEntries,
                    scrollOnceId = listViewModel.scrollOnceId,
                    listSettings = listViewModel.listSettings,
                    settingLinkProgressToSubtasks = settingsStateHolder.settingLinkProgressToSubtasks.value,
                    onClick = { itemId, ical4list -> processOnClick(itemId, ical4list) },
                    onLongClick = { itemId, ical4list -> processOnLongClick(itemId, ical4list) },
                    onProgressChanged = { itemId, newPercent ->
                        processOnProgressChanged(itemId, newPercent)
                    }
                )
            }
            ViewMode.KANBAN -> {
                ListScreenKanban(
                    module = listViewModel.module,
                    list = list,
                    subtasksLive = listViewModel.allSubtasks,
                    selectedEntries = listViewModel.selectedEntries,
                    scrollOnceId = listViewModel.scrollOnceId,
                    settingLinkProgressToSubtasks = settingsStateHolder.settingLinkProgressToSubtasks.value,
                    onClick = { itemId, ical4list -> processOnClick(itemId, ical4list) },
                    onLongClick = { itemId, ical4list -> processOnLongClick(itemId, ical4list) },
                    onProgressChanged = { itemId, newPercent, scrollOnce ->
                        processOnProgressChanged(itemId, newPercent, scrollOnce)
                    },
                    onStatusChanged = { itemId, newStatus, scrollOnce ->
                        listViewModel.updateStatus(
                            itemId,
                            newStatus,
                            scrollOnce
                        )
                    }
                )
            }
        }
    }

    ModalBottomSheetLayout(
        sheetState = filterBottomSheetState,
        sheetContent = {
            ListOptionsBottomSheet(
                module = listViewModel.module,
                listSettings = listViewModel.listSettings,
                allCollectionsLive = listViewModel.allCollections,
                allCategoriesLive = listViewModel.allCategories,
                allResourcesLive = listViewModel.allResources,
                onListSettingsChanged = { listViewModel.updateSearch(saveListSettings = true, isAuthenticated = isAuthenticated) }
            )
        },
        sheetBackgroundColor = MaterialTheme.colorScheme.surface,
        sheetContentColor = MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.surface)
    ) { }
}
