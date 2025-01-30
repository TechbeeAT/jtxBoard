/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AssignmentLate
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.PublishedWithChanges
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredListSetting
import at.techbee.jtx.database.locals.StoredListSettingData
import at.techbee.jtx.database.locals.StoredResource
import at.techbee.jtx.ui.reusable.elements.ListBadge
import at.techbee.jtx.util.DateTimeUtils


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListActiveFiltersRow(
    listSettings: ListSettings,
    isAccessibilityMode: Boolean,
    storedCategories: List<StoredCategory>,
    storedResources: List<StoredResource>,
    storedListSettings: List<StoredListSetting>,
    numShownEntries: Int,
    numAllEntries: Int?,
    isFilterActive: Boolean,
    module: Module,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        AnimatedVisibility(isFilterActive) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.fillMaxWidth(fraction = 0.8f)
            ) {
                Text(
                    text = stringResource(R.string.active_filters),
                    style = MaterialTheme.typography.labelMedium
                )

                val activeStoredListSetting = storedListSettings.firstOrNull { storedListSetting ->
                    storedListSetting.module == module && storedListSetting.storedListSettingData == StoredListSettingData.fromListSettings(
                        listSettings
                    )
                }
                if (activeStoredListSetting != null) {
                    ListBadge(
                        text = activeStoredListSetting.name,
                        modifier = Modifier.padding(vertical = 2.dp),
                        isAccessibilityMode = isAccessibilityMode
                    )
                } else {

                    listSettings.searchCategories.forEach { category ->
                        ListBadge(
                            icon = Icons.AutoMirrored.Outlined.Label,
                            iconDesc = stringResource(R.string.category),
                            text = category,
                            containerColor = StoredCategory.getColorForCategory(category, storedCategories)
                                ?: MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.isFilterNoCategorySet.value) {
                        ListBadge(
                            text = stringResource(id = R.string.filter_no_category),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    listSettings.searchResources.forEach { resource ->
                        ListBadge(
                            icon = Icons.Outlined.WorkOutline,
                            iconDesc = stringResource(R.string.resources),
                            text = resource,
                            containerColor = StoredResource.getColorForResource(resource, storedResources)
                                ?: MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.isFilterNoResourceSet.value) {
                        ListBadge(
                            text = stringResource(id = R.string.filter_no_resource),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    listSettings.searchAccount.forEach { account ->
                        ListBadge(
                            icon = Icons.Outlined.AccountBalance,
                            iconDesc = stringResource(R.string.account),
                            text = account,
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    listSettings.searchCollection.forEach { collection ->
                        ListBadge(
                            icon = Icons.Outlined.FolderOpen,
                            iconDesc = stringResource(R.string.collection),
                            text = collection,
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    listSettings.searchStatus.forEach { status ->
                        ListBadge(
                            icon = Icons.Outlined.PublishedWithChanges,
                            iconDesc = stringResource(R.string.status),
                            text = stringResource(status.stringResource),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    listSettings.searchClassification.forEach { classification ->
                        ListBadge(
                            icon = Icons.Outlined.PrivacyTip,
                            iconDesc = stringResource(R.string.classification),
                            text = stringResource(classification.stringResource),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    listSettings.searchPriority.forEach { priorityInt ->
                        if(priorityInt in 0..9) {
                            ListBadge(
                                icon = Icons.Outlined.AssignmentLate,
                                iconDesc = stringResource(R.string.priority),
                                text = stringArrayResource(id = R.array.priority)[priorityInt?:0],
                                modifier = Modifier.padding(vertical = 2.dp),
                                isAccessibilityMode = isAccessibilityMode
                            )
                        }
                    }
                    AnimatedVisibility(listSettings.isExcludeDone.value) {
                        ListBadge(
                            text = stringResource(R.string.list_hide_completed_tasks),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.isFilterStartInPast.value) {
                        ListBadge(
                            text = stringResource(id = if (module == Module.TODO) R.string.list_start_date_in_past else R.string.list_date_start_in_past),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.isFilterStartToday.value) {
                        ListBadge(
                            text = stringResource(id = if (module == Module.TODO) R.string.list_start_date_today else R.string.list_date_today),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.isFilterStartTomorrow.value) {
                        ListBadge(
                            text = stringResource(id = if (module == Module.TODO) R.string.list_start_date_tomorrow else R.string.list_date_tomorrow),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.isFilterStartWithin7Days.value) {
                        ListBadge(
                            text = stringResource(id = if (module == Module.TODO) R.string.list_start_date_within_7_days else R.string.list_date_within_7_days),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.isFilterStartFuture.value) {
                        ListBadge(
                            text = stringResource(id = if (module == Module.TODO) R.string.list_start_date_future else R.string.list_date_future),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.isFilterOverdue.value) {
                        ListBadge(
                            text = stringResource(id = R.string.list_due_overdue),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.isFilterDueToday.value) {
                        ListBadge(
                            text = stringResource(id = R.string.list_due_today),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.isFilterDueTomorrow.value) {
                        ListBadge(
                            text = stringResource(id = R.string.list_due_tomorrow),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.isFilterDueWithin7Days.value) {
                        ListBadge(
                            text = stringResource(id = R.string.list_due_within_7_days),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.isFilterDueFuture.value) {
                        ListBadge(
                            text = stringResource(id = R.string.list_due_future),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.isFilterNoDatesSet.value) {
                        ListBadge(
                            text = stringResource(id = R.string.list_no_dates_set),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.isFilterNoStartDateSet.value) {
                        ListBadge(
                            text = stringResource(id = R.string.list_without_start_date),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.isFilterNoDueDateSet.value) {
                        ListBadge(
                            text = stringResource(id = R.string.list_without_due_date),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.isFilterNoCompletedDateSet.value) {
                        ListBadge(
                            text = stringResource(id = R.string.list_without_completed_date),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.filterStartRangeStart.value != null && listSettings.filterStartRangeEnd.value != null) {
                        val dateFrom = listSettings.filterStartRangeStart.value?.let { DateTimeUtils.convertLongToShortDateString(it, null)}  ?: "…"
                        val dateTo = listSettings.filterStartRangeEnd.value?.let { DateTimeUtils.convertLongToShortDateString(it, null)}  ?: "…"
                        val text = if(module == Module.TODO)
                            stringResource(id = R.string.filter_started_from_to, dateFrom, dateTo)
                        else
                            stringResource(id = R.string.filter_date_from_to, dateFrom, dateTo)

                        ListBadge(
                            text = text,
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.filterDueRangeStart.value != null && listSettings.filterDueRangeEnd.value != null) {
                        val dateFrom = listSettings.filterDueRangeStart.value?.let { DateTimeUtils.convertLongToShortDateString(it, null)}  ?: "…"
                        val dateTo = listSettings.filterDueRangeEnd.value?.let { DateTimeUtils.convertLongToShortDateString(it, null)}  ?: "…"

                        ListBadge(
                            text = stringResource(id = R.string.filter_due_from_to, dateFrom, dateTo),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                    AnimatedVisibility(listSettings.filterCompletedRangeStart.value != null && listSettings.filterCompletedRangeEnd.value != null) {
                        val dateFrom = listSettings.filterCompletedRangeStart.value?.let { DateTimeUtils.convertLongToShortDateString(it, null)}  ?: "…"
                        val dateTo = listSettings.filterCompletedRangeEnd.value?.let { DateTimeUtils.convertLongToShortDateString(it, null)}  ?: "…"

                        ListBadge(
                            text = stringResource(id = R.string.filter_completed_from_to, dateFrom, dateTo),
                            modifier = Modifier.padding(vertical = 2.dp),
                            isAccessibilityMode = isAccessibilityMode
                        )
                    }
                }
            }
        }

        AnimatedVisibility(!isFilterActive) {
            Box(Modifier.size(1.dp).weight(1f))
        }

        ListBadge(
            text = "$numShownEntries/${numAllEntries?:0}",
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.padding(vertical = 2.dp),
            isAccessibilityMode = isAccessibilityMode
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ListActiveFiltersRow_Preview() {
    MaterialTheme {
        val listSettings = ListSettings()
        listSettings.isFilterNoResourceSet.value = true
        listSettings.searchCategories.add("Category1")

        ListActiveFiltersRow(
            module = Module.JOURNAL,
            listSettings = listSettings,
            storedCategories = emptyList(),
            storedResources = emptyList(),
            storedListSettings = emptyList(),
            numShownEntries = 15,
            numAllEntries = 255,
            isFilterActive = true,
            isAccessibilityMode = false
        )
    }
}

