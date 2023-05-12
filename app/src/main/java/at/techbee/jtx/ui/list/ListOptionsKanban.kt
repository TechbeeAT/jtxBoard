/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ViewColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.ui.reusable.elements.FilterSection


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListOptionsKanban(
    module: Module,
    listSettings: ListSettings,
    extendedStatusesLive: LiveData<List<ExtendedStatus>>,
    storedCategoriesLive: LiveData<List<StoredCategory>>,
    onListSettingsChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedStatuses by extendedStatusesLive.observeAsState(initial = emptyList())
    val storedCategories by storedCategoriesLive.observeAsState(initial = emptyList())

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        FilterSection(
            icon = Icons.Outlined.ViewColumn,
            headline = stringResource(id = R.string.kanban_columns),
            subtitle = stringResource(R.string.kanban_columns_based_on_standard_status),
            onResetSelection = { },
            onInvertSelection = { },
            showMenu = false
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Status.valuesFor(module).forEach { status ->

                    FilterChip(
                        selected = listSettings.kanbanColumnsStatus.contains(status.status),
                        enabled = listSettings.kanbanColumnsCategory.isEmpty() && listSettings.kanbanColumnsXStatus.isEmpty(),
                        onClick = {
                            if (listSettings.kanbanColumnsStatus.contains(status.status))
                                listSettings.kanbanColumnsStatus.remove(status.status)
                            else
                                listSettings.kanbanColumnsStatus.add(status.status)
                            onListSettingsChanged()
                        },
                        label = { Text(stringResource(id = status.stringResource)) },
                        leadingIcon = {
                            if (listSettings.kanbanColumnsStatus.contains(status.status))
                                Text(text = "(${listSettings.kanbanColumnsStatus.indexOf(status.status) + 1})")
                        }
                    )
                }
            }
        }

        if(extendedStatuses.any { it.module == module }) {
            FilterSection(
                icon = Icons.Outlined.ViewColumn,
                headline = stringResource(id = R.string.kanban_columns),
                subtitle = stringResource(R.string.kanban_columns_based_on_extended_status),
                onResetSelection = { },
                onInvertSelection = { },
                showMenu = false
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    extendedStatuses
                        .filter { it.module == module }
                        .forEach { storedStatus ->
                            FilterChip(
                                selected = listSettings.kanbanColumnsXStatus.contains(storedStatus.xstatus),
                                enabled = listSettings.kanbanColumnsStatus.isEmpty() && listSettings.kanbanColumnsCategory.isEmpty(),
                                onClick = {
                                    if (listSettings.kanbanColumnsXStatus.contains(storedStatus.xstatus))
                                        listSettings.kanbanColumnsXStatus.remove(storedStatus.xstatus)
                                    else
                                        listSettings.kanbanColumnsXStatus.add(storedStatus.xstatus)
                                    onListSettingsChanged()
                                },
                                label = { Text(storedStatus.xstatus) },
                                leadingIcon = {
                                    if (listSettings.kanbanColumnsXStatus.contains(storedStatus.xstatus))
                                        Text(text = "(${listSettings.kanbanColumnsXStatus.indexOf(storedStatus.xstatus) + 1})")
                                }
                            )
                        }
                }
            }
        }

        if(storedCategories.isNotEmpty()) {
            FilterSection(
                icon = Icons.Outlined.ViewColumn,
                headline = stringResource(id = R.string.kanban_columns),
                subtitle = stringResource(R.string.kanban_columns_based_on_first_category),
                onResetSelection = { },
                onInvertSelection = { },
                showMenu = false
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    storedCategories.forEach { storedCategory ->
                            FilterChip(
                                selected = listSettings.kanbanColumnsCategory.contains(storedCategory.category),
                                enabled = listSettings.kanbanColumnsStatus.isEmpty() && listSettings.kanbanColumnsXStatus.isEmpty(),
                                onClick = {
                                    if (listSettings.kanbanColumnsCategory.contains(storedCategory.category))
                                        listSettings.kanbanColumnsCategory.remove(storedCategory.category)
                                    else
                                        listSettings.kanbanColumnsCategory.add(storedCategory.category)
                                    onListSettingsChanged()
                                },
                                label = { Text(storedCategory.category) },
                                leadingIcon = {
                                    if (listSettings.kanbanColumnsCategory.contains(storedCategory.category))
                                        Text(text = "(${listSettings.kanbanColumnsCategory.indexOf(storedCategory.category) + 1})")
                                }
                            )
                        }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ListOptionsKanban_Preview() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(
            ListViewModel.PREFS_LIST_JOURNALS,
            Context.MODE_PRIVATE
        )

        val listSettings = ListSettings.fromPrefs(prefs)
        listSettings.kanbanColumnsStatus.add(Status.NEEDS_ACTION.status)

        ListOptionsKanban(
            module = Module.TODO,
            listSettings = listSettings,
            extendedStatusesLive = MutableLiveData(listOf(ExtendedStatus("individual", Module.TODO, Status.FINAL, null))),
            storedCategoriesLive = MutableLiveData(listOf(StoredCategory("cat1", Color.Green.toArgb()))),
            onListSettingsChanged = { },
        )
    }
}