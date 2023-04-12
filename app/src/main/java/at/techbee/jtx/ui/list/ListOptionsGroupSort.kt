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
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.ViewHeadline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListOptionsGroupSort(
    module: Module,
    listSettings: ListSettings,
    onListSettingsChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        HeadlineWithIcon(
            icon = Icons.Outlined.ViewHeadline,
            iconDesc = stringResource(id = R.string.filter_group_by),
            text = stringResource(id = R.string.filter_group_by),
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = stringResource(id = R.string.filter_group_by_info),
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic
        )

        FlowRow(modifier = Modifier.fillMaxWidth()) {
            GroupBy.getValuesFor(module).forEach { groupBy ->
                FilterChip(
                    selected = listSettings.groupBy.value == groupBy,
                    onClick = {
                        if (listSettings.groupBy.value != groupBy)
                            listSettings.groupBy.value = groupBy
                        else
                            listSettings.groupBy.value = null

                        listSettings.orderBy.value = when(listSettings.groupBy.value) {
                            GroupBy.START -> OrderBy.START_VTODO
                            GroupBy.DATE -> OrderBy.START_VJOURNAL
                            GroupBy.CLASSIFICATION -> OrderBy.CLASSIFICATION
                            GroupBy.PRIORITY -> OrderBy.PRIORITY
                            GroupBy.STATUS -> OrderBy.STATUS
                            GroupBy.DUE -> OrderBy.DUE
                            GroupBy.ACCOUNT -> OrderBy.ACCOUNT
                            GroupBy.COLLECTION -> OrderBy.COLLECTION
                            else -> listSettings.orderBy.value
                        }
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = groupBy.stringResource)) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        HeadlineWithIcon(
            icon = Icons.Outlined.Sort,
            iconDesc = stringResource(id = R.string.filter_order_by),
            text = stringResource(id = R.string.filter_order_by),
            modifier = Modifier.padding(top = 8.dp)
        )

        // SORT ORDER 1
        FlowRow(modifier = Modifier.fillMaxWidth()) {
            OrderBy.getValuesFor(module).forEach { orderBy ->
                FilterChip(
                    selected = listSettings.orderBy.value == orderBy,
                    enabled = listSettings.groupBy.value == null,
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
        FlowRow(modifier = Modifier.fillMaxWidth()) {
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
        HeadlineWithIcon(
            icon = Icons.Outlined.Sort,
            iconDesc = stringResource(id = R.string.filter_second_order_by),
            text = stringResource(id = R.string.filter_second_order_by),
            modifier = Modifier.padding(top = 8.dp)
        )

        FlowRow(modifier = Modifier.fillMaxWidth()) {
            OrderBy.getValuesFor(module).forEach { orderBy ->
                FilterChip(
                    selected = listSettings.orderBy2.value == orderBy,
                    enabled = orderBy != listSettings.orderBy.value,
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
        FlowRow(modifier = Modifier.fillMaxWidth()) {
            SortOrder.values().forEach { sortOrder2 ->
                FilterChip(
                    selected = listSettings.sortOrder2.value == sortOrder2,
                    onClick = {
                        if (listSettings.sortOrder2.value != sortOrder2)
                            listSettings.sortOrder2.value = sortOrder2
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = sortOrder2.stringResource)) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // SORT ORDER FOR SUBTASKS
        HeadlineWithIcon(
            icon = Icons.Outlined.Sort,
            iconDesc = null,
            text = stringResource(id = R.string.filter_subtasks_order_by),
            modifier = Modifier.padding(top = 8.dp)
        )
        FlowRow(modifier = Modifier.fillMaxWidth()) {
            OrderBy.getValuesFor(Module.TODO).forEach { orderBy ->
                FilterChip(
                    selected = listSettings.subtasksOrderBy.value == orderBy,
                    onClick = {
                        if (listSettings.subtasksOrderBy.value != orderBy)
                            listSettings.subtasksOrderBy.value = orderBy
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = orderBy.stringResource)) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
        FlowRow(modifier = Modifier.fillMaxWidth()) {
            SortOrder.values().forEach { sortOrder ->
                FilterChip(
                    selected = listSettings.subtasksSortOrder.value == sortOrder,
                    onClick = {
                        if (listSettings.subtasksSortOrder.value != sortOrder)
                            listSettings.subtasksSortOrder.value = sortOrder
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = sortOrder.stringResource)) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }


        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // SORT ORDER FOR SUBNOTES
        HeadlineWithIcon(
            icon = Icons.Outlined.Sort,
            iconDesc = null,
            text = stringResource(id = R.string.filter_subnotes_order_by),
            modifier = Modifier.padding(top = 8.dp)
        )
        FlowRow(modifier = Modifier.fillMaxWidth()) {
            OrderBy.getValuesFor(Module.NOTE).forEach { orderBy ->
                FilterChip(
                    selected = listSettings.subnotesOrderBy.value == orderBy,
                    onClick = {
                        if (listSettings.subnotesOrderBy.value != orderBy)
                            listSettings.subnotesOrderBy.value = orderBy
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = orderBy.stringResource)) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
        FlowRow(modifier = Modifier.fillMaxWidth()) {
            SortOrder.values().forEach { sortOrder ->
                FilterChip(
                    selected = listSettings.subnotesSortOrder.value == sortOrder,
                    onClick = {
                        if (listSettings.subnotesSortOrder.value != sortOrder)
                            listSettings.subnotesSortOrder.value = sortOrder
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = sortOrder.stringResource)) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ListOptionsSortOrder_Preview_TODO() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(
            ListViewModel.PREFS_LIST_JOURNALS,
            Context.MODE_PRIVATE
        )
        val listSettings = ListSettings.fromPrefs(prefs)

        ListOptionsGroupSort(
            module = Module.TODO,
            listSettings = listSettings,
            onListSettingsChanged = { },
            modifier = Modifier.fillMaxWidth()

        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListOptionsSortOrder_Preview_JOURNAL() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(
            ListViewModel.PREFS_LIST_JOURNALS,
            Context.MODE_PRIVATE
        )
        val listSettings = ListSettings.fromPrefs(prefs).apply { groupBy.value = GroupBy.CLASSIFICATION }

        ListOptionsGroupSort(
            module = Module.JOURNAL,
            listSettings = listSettings,
            onListSettingsChanged = { },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
