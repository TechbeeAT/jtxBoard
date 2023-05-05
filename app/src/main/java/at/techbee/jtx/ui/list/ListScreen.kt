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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import at.techbee.jtx.R
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.reusable.destinations.DetailDestination
import at.techbee.jtx.ui.settings.SettingsStateHolder


@Composable
fun ListScreen(
    listViewModel: ListViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val settingsStateHolder = SettingsStateHolder(context)

    listViewModel.toastMessage.value?.let {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        listViewModel.toastMessage.value = null
    }

    val list = listViewModel.iCal4ListRel.observeAsState(emptyList())

    // first apply a proper sort order, then group
    val sortedList = when (listViewModel.listSettings.groupBy.value) {
        GroupBy.STATUS -> list.value.sortedBy {
            if (listViewModel.module == Module.TODO && it.iCal4List.percent != 100)
                try {
                    Status.valueOf(it.iCal4List.status ?: Status.NO_STATUS.name).ordinal
                } catch (e: java.lang.IllegalArgumentException) {
                    -1
                }
            else
                try {
                    Status.valueOf(it.iCal4List.status ?: Status.FINAL.name).ordinal
                } catch (e: java.lang.IllegalArgumentException) {
                    -1
                }
        }.let { if (listViewModel.listSettings.sortOrder.value == SortOrder.DESC) it.asReversed() else it }
        GroupBy.CLASSIFICATION -> list.value.sortedBy {
            try {
                Classification.valueOf(it.iCal4List.classification ?: Classification.PUBLIC.name).ordinal
            } catch (e: java.lang.IllegalArgumentException) {
                -1
            }
        }.let { if (listViewModel.listSettings.sortOrder.value == SortOrder.DESC) it.asReversed() else it }
        else -> list.value
    }

    val groupedList = sortedList.groupBy {
        when (listViewModel.listSettings.groupBy.value) {
            GroupBy.STATUS -> Status.values().find { status ->  status.status == it.iCal4List.status }?.stringResource?.let { stringRes -> stringResource(id = stringRes)}?: it.iCal4List.status?:""
            GroupBy.CLASSIFICATION -> Classification.values().find { classif ->  classif.classification == it.iCal4List.classification }?.stringResource?.let { stringRes -> stringResource(id = stringRes)}?: it.iCal4List.classification?:""
            GroupBy.ACCOUNT -> it.iCal4List.accountName ?:""
            GroupBy.COLLECTION -> it.iCal4List.collectionDisplayName ?:""
            GroupBy.PRIORITY -> {
                when (it.iCal4List.priority) {
                    null -> stringArrayResource(id = R.array.priority)[0]
                    in 0..9 -> stringArrayResource(id = R.array.priority)[it.iCal4List.priority!!]
                    else -> it.iCal4List.priority.toString()
                }
            }
            GroupBy.DATE -> ICalObject.getDtstartTextInfo(module = Module.JOURNAL, dtstart = it.iCal4List.dtstart, dtstartTimezone = it.iCal4List.dtstartTimezone, daysOnly = true, context = LocalContext.current)
            GroupBy.START -> ICalObject.getDtstartTextInfo(module = Module.TODO, dtstart = it.iCal4List.dtstart, dtstartTimezone = it.iCal4List.dtstartTimezone, daysOnly = true, context = LocalContext.current)
            GroupBy.DUE -> ICalObject.getDueTextInfo(status = it.iCal4List.status, due = it.iCal4List.due, dueTimezone = it.iCal4List.dueTimezone, percent = it.iCal4List.percent, daysOnly = true, context = LocalContext.current)
            else -> {
                it.iCal4List.module
            }
        }
    }

    fun processOnClick(itemId: Long, ical4list: List<ICal4List>, isReadOnly: Boolean) {
        if (listViewModel.multiselectEnabled.value && isReadOnly)
            return
        else if (listViewModel.multiselectEnabled.value)
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

    Column(modifier = Modifier.fillMaxSize()) {
        when (listViewModel.listSettings.viewMode.value) {
            ViewMode.LIST -> {
                ListScreenList(
                    groupedList = groupedList,
                    subtasksLive = listViewModel.allSubtasks,
                    subnotesLive = listViewModel.allSubnotes,
                    parentsLive = listViewModel.allParents,
                    selectedEntries = listViewModel.selectedEntries,
                    attachmentsLive = listViewModel.allAttachmentsMap,
                    scrollOnceId = listViewModel.scrollOnceId,
                    listSettings = listViewModel.listSettings,
                    storedCategoriesLive = listViewModel.storedCategories,
                    storedResourcesLive = listViewModel.storedResources,
                    storedStatusesLive = listViewModel.storedStatuses,
                    isSubtasksExpandedDefault = settingsStateHolder.settingAutoExpandSubtasks,
                    isSubnotesExpandedDefault = settingsStateHolder.settingAutoExpandSubnotes,
                    isAttachmentsExpandedDefault = settingsStateHolder.settingAutoExpandAttachments,
                    settingShowProgressMaintasks = settingsStateHolder.settingShowProgressForMainTasks,
                    settingShowProgressSubtasks = settingsStateHolder.settingShowProgressForSubTasks,
                    settingProgressIncrement = settingsStateHolder.settingStepForProgress,
                    settingLinkProgressToSubtasks = settingsStateHolder.settingLinkProgressToSubtasks.value,
                    player = listViewModel.mediaPlayer,
                    onClick = { itemId, ical4list, isReadOnly -> processOnClick(itemId, ical4list, isReadOnly) },
                    onLongClick = { itemId, ical4list -> processOnLongClick(itemId, ical4list) },
                    onProgressChanged = { itemId, newPercent ->
                        processOnProgressChanged(itemId, newPercent)
                    },
                    onExpandedChanged = { itemId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isParentsExpanded: Boolean, isAttachmentsExpanded: Boolean ->
                        listViewModel.updateExpanded(
                            itemId,
                            isSubtasksExpanded,
                            isSubnotesExpanded,
                            isParentsExpanded,
                            isAttachmentsExpanded
                        )
                    },
                    onSyncRequested = { listViewModel.syncAccounts() }
                )
            }
            ViewMode.GRID -> {
                ListScreenGrid(
                    list = list.value,
                    subtasksLive = listViewModel.allSubtasks,
                    storedCategoriesLive = listViewModel.storedCategories,
                    storedResourcesLive = listViewModel.storedResources,
                    storedStatusesLive = listViewModel.storedStatuses,
                    selectedEntries = listViewModel.selectedEntries,
                    scrollOnceId = listViewModel.scrollOnceId,
                    settingLinkProgressToSubtasks = settingsStateHolder.settingLinkProgressToSubtasks.value,
                    player = listViewModel.mediaPlayer,
                    onClick = { itemId, ical4list, isReadOnly -> processOnClick(itemId, ical4list, isReadOnly) },
                    onLongClick = { itemId, ical4list -> processOnLongClick(itemId, ical4list) },
                    onProgressChanged = { itemId, newPercent ->
                        processOnProgressChanged(itemId, newPercent)
                    },
                    onSyncRequested = { listViewModel.syncAccounts() }
                )
            }
            ViewMode.COMPACT -> {
                ListScreenCompact(
                    groupedList = groupedList,
                    subtasksLive = listViewModel.allSubtasks,
                    storedCategoriesLive = listViewModel.storedCategories,
                    storedResourcesLive = listViewModel.storedResources,
                    extendedStatusesLive = listViewModel.storedStatuses,
                    selectedEntries = listViewModel.selectedEntries,
                    scrollOnceId = listViewModel.scrollOnceId,
                    listSettings = listViewModel.listSettings,
                    settingLinkProgressToSubtasks = settingsStateHolder.settingLinkProgressToSubtasks.value,
                    player = listViewModel.mediaPlayer,
                    onClick = { itemId, ical4list, isReadOnly -> processOnClick(itemId, ical4list, isReadOnly) },
                    onLongClick = { itemId, ical4list -> processOnLongClick(itemId, ical4list) },
                    onProgressChanged = { itemId, newPercent -> processOnProgressChanged(itemId, newPercent) },
                    onSyncRequested = { listViewModel.syncAccounts() }
                )
            }
            ViewMode.KANBAN -> {
                ListScreenKanban(
                    module = listViewModel.module,
                    list = list.value,
                    subtasksLive = listViewModel.allSubtasks,
                    storedCategoriesLive = listViewModel.storedCategories,
                    storedResourcesLive = listViewModel.storedResources,
                    storedStatusesLive = listViewModel.storedStatuses,
                    selectedEntries = listViewModel.selectedEntries,
                    kanbanColumns = listViewModel.listSettings.kanbanColumns,
                    scrollOnceId = listViewModel.scrollOnceId,
                    settingLinkProgressToSubtasks = settingsStateHolder.settingLinkProgressToSubtasks.value,
                    player = listViewModel.mediaPlayer,
                    onClick = { itemId, ical4list, isReadOnly -> processOnClick(itemId, ical4list, isReadOnly) },
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
                    },
                    onSyncRequested = { listViewModel.syncAccounts() }
                )
            }
        }
    }
}
