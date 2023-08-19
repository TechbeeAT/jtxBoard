/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListActiveFiltersRow(
    listSettings: ListSettings, 
    storedCategories: List<StoredCategory>,
    storedResources: List<StoredResource>,
    storedListSettings: List<StoredListSetting>,
    module: Module,
    modifier: Modifier = Modifier
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.active_filters),
            style = MaterialTheme.typography.labelMedium
        )

        val activeStoredListSetting = storedListSettings.firstOrNull { storedListSetting -> storedListSetting.module == module && storedListSetting.storedListSettingData == StoredListSettingData.fromListSettings(listSettings)}
        if(activeStoredListSetting != null) {
            ListBadge(
                text = activeStoredListSetting.name,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        } else {

            listSettings.searchCategories.forEach { category ->
                ListBadge(
                    icon = Icons.Outlined.Label,
                    iconDesc = stringResource(R.string.category),
                    text = category,
                    containerColor = StoredCategory.getColorForCategory(category, storedCategories) ?: MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            AnimatedVisibility(listSettings.isFilterNoCategorySet.value) {
                ListBadge(
                    text = stringResource(id = R.string.filter_no_category),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            listSettings.searchResources.forEach { resource ->
                ListBadge(
                    icon = Icons.Outlined.WorkOutline,
                    iconDesc = stringResource(R.string.resources),
                    text = resource,
                    containerColor = StoredResource.getColorForResource(resource, storedResources) ?: MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            AnimatedVisibility(listSettings.isFilterNoResourceSet.value) {
                ListBadge(
                    text = stringResource(id = R.string.filter_no_resource),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            listSettings.searchAccount.forEach { account ->
                ListBadge(
                    icon = Icons.Outlined.AccountBalance,
                    iconDesc = stringResource(R.string.account),
                    text = account,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            listSettings.searchCollection.forEach { collection ->
                ListBadge(
                    icon = Icons.Outlined.FolderOpen,
                    iconDesc = stringResource(R.string.collection),
                    text = collection,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            listSettings.searchStatus.forEach { status ->
                ListBadge(
                    icon = Icons.Outlined.PublishedWithChanges,
                    iconDesc = stringResource(R.string.status),
                    text = stringResource(status.stringResource),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            listSettings.searchClassification.forEach { classification ->
                ListBadge(
                    icon = Icons.Outlined.PrivacyTip,
                    iconDesc = stringResource(R.string.classification),
                    text = stringResource(classification.stringResource),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            AnimatedVisibility(listSettings.isExcludeDone.value) {
                ListBadge(
                    text = stringResource(R.string.list_hide_completed_tasks),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            AnimatedVisibility(listSettings.isFilterStartInPast.value) {
                ListBadge(
                    text = stringResource(id = if (module == Module.TODO) R.string.list_start_date_in_past else R.string.list_date_start_in_past),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            AnimatedVisibility(listSettings.isFilterStartToday.value) {
                ListBadge(
                    text = stringResource(id = if (module == Module.TODO) R.string.list_start_date_today else R.string.list_date_today),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            AnimatedVisibility(listSettings.isFilterStartTomorrow.value) {
                ListBadge(
                    text = stringResource(id = if (module == Module.TODO) R.string.list_start_date_tomorrow else R.string.list_date_tomorrow),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            AnimatedVisibility(listSettings.isFilterStartWithin7Days.value) {
                ListBadge(
                    text = stringResource(id = if (module == Module.TODO) R.string.list_start_date_within_7_days else R.string.list_date_within_7_days),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            AnimatedVisibility(listSettings.isFilterStartFuture.value) {
                ListBadge(
                    text = stringResource(id = if (module == Module.TODO) R.string.list_start_date_future else R.string.list_date_future),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            AnimatedVisibility(listSettings.isFilterOverdue.value) {
                ListBadge(
                    text = stringResource(id = R.string.list_due_overdue),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            AnimatedVisibility(listSettings.isFilterDueToday.value) {
                ListBadge(
                    text = stringResource(id = R.string.list_due_today),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            AnimatedVisibility(listSettings.isFilterDueTomorrow.value) {
                ListBadge(
                    text = stringResource(id = R.string.list_due_tomorrow),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            AnimatedVisibility(listSettings.isFilterDueWithin7Days.value) {
                ListBadge(
                    text = stringResource(id = R.string.list_due_within_7_days),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            AnimatedVisibility(listSettings.isFilterDueFuture.value) {
                ListBadge(
                    text = stringResource(id = R.string.list_due_future),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            AnimatedVisibility(listSettings.isFilterNoDatesSet.value) {
                ListBadge(
                    text = stringResource(id = R.string.list_no_dates_set),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            AnimatedVisibility(listSettings.isFilterNoStartDateSet.value) {
                ListBadge(
                    text = stringResource(id = R.string.list_without_start_date),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            AnimatedVisibility(listSettings.isFilterNoDueDateSet.value) {
                ListBadge(
                    text = stringResource(id = R.string.list_without_due_date),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            AnimatedVisibility(listSettings.isFilterNoCompletedDateSet.value) {
                ListBadge(
                    text = stringResource(id = R.string.list_without_completed_date),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
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
            storedListSettings = emptyList()
        )
    }
}

