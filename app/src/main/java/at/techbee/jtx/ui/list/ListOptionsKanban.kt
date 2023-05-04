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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.StoredStatus
import at.techbee.jtx.ui.reusable.elements.FilterSection


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListOptionsKanban(
    module: Module,
    listSettings: ListSettings,
    storedStatusesLive: LiveData<List<StoredStatus>>,
    onListSettingsChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val storedStatuses by storedStatusesLive.observeAsState(initial = emptyList())

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        FilterSection(
            icon = Icons.Outlined.ViewColumn,
            headline = stringResource(id = R.string.kanban_columns),
            onResetSelection = { },
            onInvertSelection = { },
            showMenu = false
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Status.valuesFor(module).forEach { status ->
                    if (status == Status.NO_STATUS)   // skip "no status" is it will be treated like "needs action"/"final"
                        return@forEach

                    FilterChip(
                        selected = listSettings.kanbanColumns.contains(status.status ?: status.name),
                        onClick = {
                            if (listSettings.kanbanColumns.contains(status.status ?: status.name))
                                listSettings.kanbanColumns.remove(status.status ?: status.name)
                            else
                                listSettings.kanbanColumns.add(status.status ?: status.name)
                            onListSettingsChanged()
                        },
                        label = { Text(stringResource(id = status.stringResource)) },
                        leadingIcon = {
                            if (listSettings.kanbanColumns.contains(status.status ?: status.name))
                                Text(text = "(${listSettings.kanbanColumns.indexOf(status.status ?: status.name) + 1})")
                        }
                    )
                }
                storedStatuses
                    .filter { Status.valuesFor(module).none { default -> stringResource(id = default.stringResource) == it.status } }
                    .filter { it.module == module.name }
                    .forEach { storedStatus ->
                        FilterChip(
                            selected = listSettings.kanbanColumns.contains(storedStatus.status),
                            onClick = {
                                if (listSettings.kanbanColumns.contains(storedStatus.status))
                                    listSettings.kanbanColumns.remove(storedStatus.status)
                                else
                                    listSettings.kanbanColumns.add(storedStatus.status)
                                onListSettingsChanged()
                            },
                            label = { Text(storedStatus.status) },
                            leadingIcon = {
                                if (listSettings.kanbanColumns.contains(storedStatus.status))
                                    Text(text = "(${listSettings.kanbanColumns.indexOf(storedStatus.status) + 1})")
                            }
                        )
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
        listSettings.kanbanColumns.add(Status.NEEDS_ACTION.status ?: Status.NEEDS_ACTION.name)
        listSettings.kanbanColumns.add("individual")

        ListOptionsKanban(
            module = Module.TODO,
            listSettings = listSettings,
            storedStatusesLive = MutableLiveData(listOf(StoredStatus("individual", Module.TODO.name, null))),
            onListSettingsChanged = { },
        )
    }
}