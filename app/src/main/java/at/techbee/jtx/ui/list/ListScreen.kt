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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.reusable.destinations.DetailDestination
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.util.SyncUtil


@Composable
fun ListScreen(
    listViewModel: ListViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val settingsStateHolder = SettingsStateHolder(context)
    val isPullRefreshEnabled = SyncUtil.availableSyncApps(context).any { SyncUtil.isSyncAppCompatible(it, context) } && settingsStateHolder.settingSyncOnPullRefresh.value

    listViewModel.toastMessage.value?.let {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        listViewModel.toastMessage.value = null
    }

    val list by listViewModel.iCal4ListRel.observeAsState(emptyList())
    val groupedList = ICal4ListRel.getGroupedList(
        initialList = list,
        groupBy = listViewModel.listSettings.groupBy.value,
        sortOrder = listViewModel.listSettings.sortOrder.value,
        module = listViewModel.module,
        context = context
    )

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
                    storedStatusesLive = listViewModel.extendedStatuses,
                    isSubtasksExpandedDefault = settingsStateHolder.settingAutoExpandSubtasks,
                    isSubnotesExpandedDefault = settingsStateHolder.settingAutoExpandSubnotes,
                    isAttachmentsExpandedDefault = settingsStateHolder.settingAutoExpandAttachments,
                    settingShowProgressMaintasks = settingsStateHolder.settingShowProgressForMainTasks,
                    settingShowProgressSubtasks = settingsStateHolder.settingShowProgressForSubTasks,
                    settingProgressIncrement = settingsStateHolder.settingStepForProgress,
                    settingLinkProgressToSubtasks = settingsStateHolder.settingLinkProgressToSubtasks.value,
                    isPullRefreshEnabled = isPullRefreshEnabled,
                    markdownEnabled = listViewModel.listSettings.markdownEnabled.value,
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
                    onSyncRequested = { listViewModel.syncAccounts() },
                    onSaveListSettings = { listViewModel.saveListSettings() }
                )
            }
            ViewMode.GRID -> {
                ListScreenGrid(
                    list = list,
                    subtasksLive = listViewModel.allSubtasks,
                    storedCategoriesLive = listViewModel.storedCategories,
                    storedResourcesLive = listViewModel.storedResources,
                    storedStatusesLive = listViewModel.extendedStatuses,
                    selectedEntries = listViewModel.selectedEntries,
                    scrollOnceId = listViewModel.scrollOnceId,
                    settingLinkProgressToSubtasks = settingsStateHolder.settingLinkProgressToSubtasks.value,
                    isPullRefreshEnabled = isPullRefreshEnabled,
                    markdownEnabled = listViewModel.listSettings.markdownEnabled.value,
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
                    extendedStatusesLive = listViewModel.extendedStatuses,
                    selectedEntries = listViewModel.selectedEntries,
                    scrollOnceId = listViewModel.scrollOnceId,
                    listSettings = listViewModel.listSettings,
                    settingLinkProgressToSubtasks = settingsStateHolder.settingLinkProgressToSubtasks.value,
                    isPullRefreshEnabled = isPullRefreshEnabled,
                    markdownEnabled = listViewModel.listSettings.markdownEnabled.value,
                    player = listViewModel.mediaPlayer,
                    onClick = { itemId, ical4list, isReadOnly -> processOnClick(itemId, ical4list, isReadOnly) },
                    onLongClick = { itemId, ical4list -> processOnLongClick(itemId, ical4list) },
                    onProgressChanged = { itemId, newPercent -> processOnProgressChanged(itemId, newPercent) },
                    onSyncRequested = { listViewModel.syncAccounts() },
                    onSaveListSettings = { listViewModel.saveListSettings() }
                )
            }
            ViewMode.KANBAN -> {
                ListScreenKanban(
                    module = listViewModel.module,
                    list = list,
                    subtasksLive = listViewModel.allSubtasks,
                    storedCategoriesLive = listViewModel.storedCategories,
                    storedResourcesLive = listViewModel.storedResources,
                    extendedStatusesLive = listViewModel.extendedStatuses,
                    selectedEntries = listViewModel.selectedEntries,
                    kanbanColumnsStatus = listViewModel.listSettings.kanbanColumnsStatus,
                    kanbanColumnsXStatus = listViewModel.listSettings.kanbanColumnsXStatus,
                    kanbanColumnsCategory = listViewModel.listSettings.kanbanColumnsCategory,
                    scrollOnceId = listViewModel.scrollOnceId,
                    settingLinkProgressToSubtasks = settingsStateHolder.settingLinkProgressToSubtasks.value,
                    isPullRefreshEnabled = isPullRefreshEnabled,
                    markdownEnabled = listViewModel.listSettings.markdownEnabled.value,
                    player = listViewModel.mediaPlayer,
                    onClick = { itemId, ical4list, isReadOnly -> processOnClick(itemId, ical4list, isReadOnly) },
                    onLongClick = { itemId, ical4list -> processOnLongClick(itemId, ical4list) },
                    onStatusChanged = { itemId, newStatus, scrollOnce -> listViewModel.updateStatus(itemId, newStatus, scrollOnce) },
                    onXStatusChanged = { itemId, newXStatus, scrollOnce -> listViewModel.updateXStatus(itemId, newXStatus, scrollOnce) },
                    onSwapCategories = { itemId, oldCategory, newCategory -> listViewModel.swapCategories(itemId, oldCategory, newCategory) },
                    onSyncRequested = { listViewModel.syncAccounts() }
                )
            }
        }
    }
}
