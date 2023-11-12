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
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.FilterListOff
import androidx.compose.material.icons.outlined.LibraryAddCheck
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.ui.reusable.appbars.OverflowMenu
import at.techbee.jtx.ui.reusable.dialogs.DatePickerDialog
import at.techbee.jtx.ui.reusable.dialogs.DeleteDoneDialog
import at.techbee.jtx.ui.reusable.dialogs.SyncAppIncompatibleDialog
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.SyncApp
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun ListBottomAppBar(
    module: Module,
    iCal4ListRel: List<ICal4ListRel>,
    listSettings: ListSettings,
    showQuickEntry: MutableState<Boolean>,
    multiselectEnabled: MutableState<Boolean>,
    selectedEntries: SnapshotStateList<Long>,
    allowNewEntries: Boolean,
    isBiometricsEnabled: Boolean,
    isBiometricsUnlocked: Boolean,
    incompatibleSyncApps: List<SyncApp>,
    onAddNewEntry: () -> Unit,
    onFilterIconClicked: () -> Unit,
    onGoToDateSelected: (Long) -> Unit,
    onDeleteSelectedClicked: () -> Unit,
    onUpdateSelectedClicked: () -> Unit,
    onToggleBiometricAuthentication: () -> Unit,
    onDeleteDone: () -> Unit
) {

    var showGoToDatePicker by rememberSaveable { mutableStateOf(false) }
    var showSyncAppIncompatibleDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDoneDialog by rememberSaveable { mutableStateOf(false) }
    val showMoreActionsMenu = rememberSaveable { mutableStateOf(false) }

    if(showGoToDatePicker) {
        DatePickerDialog(
            datetime = DateTimeUtils.getTodayAsLong(),
            timezone = ZoneId.systemDefault().id,
            allowNull = false,
            onConfirm = { selectedDate, _ ->
                val selectedZoned = selectedDate?.let {ZonedDateTime.ofInstant(Instant.ofEpochMilli(selectedDate), ZoneId.systemDefault()) } ?: return@DatePickerDialog

                var match = iCal4ListRel.firstOrNull {
                    val dtstartZoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(it.iCal4List.dtstart!!), DateTimeUtils.requireTzId(it.iCal4List.dtstartTimezone))
                    if(listSettings.sortOrder.value == SortOrder.ASC)
                        dtstartZoned.year >= selectedZoned.year && dtstartZoned.monthValue >= selectedZoned.monthValue && dtstartZoned.dayOfMonth >= selectedZoned.dayOfMonth
                    else
                        dtstartZoned.year <= selectedZoned.year && dtstartZoned.monthValue <= selectedZoned.monthValue && dtstartZoned.dayOfMonth <= selectedZoned.dayOfMonth
                }

                if(match == null && listSettings.sortOrder.value == SortOrder.ASC) // no match found, sort order ascending, so the date must be after the last one!
                    match = iCal4ListRel.lastOrNull()
                else if(match == null && listSettings.sortOrder.value == SortOrder.DESC) // no match found, sort order descending, so the date must be before the last one!
                    match = iCal4ListRel.lastOrNull()

                match?.let { closest -> onGoToDateSelected(closest.iCal4List.id) }
            },
            onDismiss = { showGoToDatePicker = false },
            dateOnly = true,
            minDate = iCal4ListRel.minByOrNull { it.iCal4List.dtstart ?: Long.MAX_VALUE }?.iCal4List?.dtstart?.let { Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC"))},
            //maxDate = iCal4List.maxByOrNull { it.iCal4List.dtstart ?: Long.MIN_VALUE }?.iCal4List?.dtstart?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())},
        )
    }

    if(showSyncAppIncompatibleDialog) {
        SyncAppIncompatibleDialog(
            incompatibleSyncApps = incompatibleSyncApps,
            onDismiss = { showSyncAppIncompatibleDialog = false }
        )
    }

    if(showDeleteDoneDialog) {
        DeleteDoneDialog(
            onConfirm = { onDeleteDone() },
            onDismiss = { showDeleteDoneDialog = false }
        )
    }

    BottomAppBar(
        actions = {

            AnimatedVisibility(!multiselectEnabled.value) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { multiselectEnabled.value = true }) {
                        Icon(
                            Icons.Outlined.LibraryAddCheck,
                            contentDescription = "select multiple"
                        )
                    }
                    IconButton(onClick = { onFilterIconClicked() }) {
                        Icon(
                            Icons.Outlined.FilterList,
                            contentDescription = stringResource(id = R.string.filter),
                            tint = if (listSettings.isFilterActive()) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    AnimatedVisibility(allowNewEntries) {
                        IconButton(onClick = { showQuickEntry.value = !showQuickEntry.value }) {
                            Icon(
                                painterResource(
                                    id = R.drawable.ic_add_quick
                                ),
                                contentDescription = stringResource(
                                    id = when (module) {
                                        Module.JOURNAL -> R.string.menu_list_quick_journal
                                        Module.NOTE -> R.string.menu_list_quick_note
                                        Module.TODO -> R.string.menu_list_quick_todo
                                    }
                                ),
                                tint = if (showQuickEntry.value) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }

                    AnimatedVisibility(visible = module == Module.JOURNAL && listSettings.groupBy.value == null && listSettings.orderBy.value == OrderBy.START_VJOURNAL) {
                        IconButton(onClick = { showGoToDatePicker = true }) {
                            Icon(
                                Icons.Outlined.DateRange,
                                contentDescription = stringResource(id = R.string.menu_list_gotodate)
                            )
                        }
                    }


                    AnimatedVisibility(isBiometricsEnabled) {
                        IconButton(onClick = { onToggleBiometricAuthentication() }) {
                            Crossfade(isBiometricsUnlocked, label = "isBiometricsUnlocked") {
                                if(it) {
                                    Icon(
                                        painterResource(id = R.drawable.ic_shield_lock_open),
                                        contentDescription = stringResource(id = R.string.list_biometric_protected_entries_unlocked),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        painterResource(id = R.drawable.ic_shield_lock),
                                        contentDescription = stringResource(id = R.string.list_biometric_protected_entries_locked),
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(incompatibleSyncApps.isNotEmpty()) {
                        IconButton(onClick = { showSyncAppIncompatibleDialog = true }) {
                            Icon(
                                Icons.Outlined.SyncProblem,
                                contentDescription = stringResource(id = R.string.dialog_sync_app_outdated_title),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    AnimatedVisibility(module == Module.TODO || listSettings.isFilterActive()) {
                        OverflowMenu(menuExpanded = showMoreActionsMenu) {

                            if(module == Module.TODO) {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(R.string.list_delete_done)) },
                                    onClick = {
                                        showDeleteDoneDialog = true
                                        showMoreActionsMenu.value = false
                                    },
                                    leadingIcon = { Icon(Icons.Outlined.DeleteSweep, null) },
                                )
                            }

                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.clear_filters)) },
                                onClick = {
                                    listSettings.reset()
                                    showMoreActionsMenu.value = false
                                },
                                leadingIcon = { Icon(Icons.Outlined.FilterListOff, null) }
                            )
                        }
                    }
                }
            }


            AnimatedVisibility(multiselectEnabled.value) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        selectedEntries.clear()
                        multiselectEnabled.value = false
                    }) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = stringResource(id = R.string.cancel)
                        )
                    }
                    VerticalDivider(modifier = Modifier.height(40.dp))

                    IconButton(
                        onClick = { onDeleteSelectedClicked() },
                        enabled = selectedEntries.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = stringResource(id = R.string.delete),
                        )
                    }
                    IconButton(
                        onClick = { onUpdateSelectedClicked() },
                        enabled = selectedEntries.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = stringResource(id = R.string.more),
                        )
                    }

                    Text(
                        text = stringResource(R.string.x_selected, selectedEntries.size, iCal4ListRel.size),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    TextButton(onClick = {
                        when(selectedEntries.size) {
                            0 -> selectedEntries.addAll(iCal4ListRel.map { it.iCal4List.id })
                            iCal4ListRel.size -> selectedEntries.clear()
                            else -> {
                                selectedEntries.clear()
                                selectedEntries.addAll(iCal4ListRel.map { it.iCal4List.id })
                            }
                        }
                    }) {
                        Crossfade(selectedEntries.size, label = "selectall_selectnone") {
                            when (it) {
                                0 -> Text(stringResource(R.string.select_all))
                                iCal4ListRel.size -> Text(stringResource(R.string.select_none))
                                else -> Text(stringResource(R.string.select_all))
                            }
                        }
                    }

                }
            }
        },
        floatingActionButton = {
            // TODO(b/228588827): Replace with Secondary FAB when available.
            AnimatedVisibility(allowNewEntries && !multiselectEnabled.value) {
                FloatingActionButton(
                    onClick = { onAddNewEntry() },
                ) {
                    Crossfade(module, label = "fab_content_list") {
                        when (it) {
                            Module.JOURNAL -> Icon(Icons.AutoMirrored.Outlined.EventNote, stringResource(R.string.toolbar_text_add_journal))
                            Module.NOTE -> Icon(Icons.AutoMirrored.Outlined.NoteAdd, stringResource(R.string.toolbar_text_add_note))
                            Module.TODO -> Icon(Icons.Outlined.AddTask, stringResource(R.string.toolbar_text_add_task))
                        }
                    }
                }
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun ListBottomAppBar_Preview_Journal() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(ListViewModel.PREFS_LIST_JOURNALS, Context.MODE_PRIVATE)
        val listSettings = ListSettings.fromPrefs(prefs)

        ListBottomAppBar(
            module = Module.JOURNAL,
            iCal4ListRel = emptyList(),
            listSettings = listSettings,
            allowNewEntries = true,
            isBiometricsEnabled = false,
            isBiometricsUnlocked = false,
            incompatibleSyncApps = emptyList(),
            multiselectEnabled = remember { mutableStateOf(false) },
            selectedEntries = remember { mutableStateListOf() },
            onAddNewEntry = { },
            showQuickEntry = remember { mutableStateOf(true) },
            onFilterIconClicked = { },
            onGoToDateSelected = { },
            onDeleteSelectedClicked = { },
            onUpdateSelectedClicked = { },
            onToggleBiometricAuthentication = { },
            onDeleteDone = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListBottomAppBar_Preview_Note() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(ListViewModel.PREFS_LIST_NOTES, Context.MODE_PRIVATE)
        val listSettings = ListSettings.fromPrefs(prefs)

        ListBottomAppBar(
            module = Module.NOTE,
            iCal4ListRel = emptyList(),
            listSettings = listSettings,
            allowNewEntries = false,
            isBiometricsEnabled = false,
            isBiometricsUnlocked = false,
            incompatibleSyncApps = listOf(SyncApp.DAVX5),
            multiselectEnabled = remember { mutableStateOf(true) },
            selectedEntries = remember { mutableStateListOf() },
            onAddNewEntry = { },
            showQuickEntry = remember { mutableStateOf(false) },
            onFilterIconClicked = { },
            onGoToDateSelected = { },
            onDeleteSelectedClicked = { },
            onUpdateSelectedClicked = { },
            onToggleBiometricAuthentication = { },
            onDeleteDone = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListBottomAppBar_Preview_Todo() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(ListViewModel.PREFS_LIST_TODOS, Context.MODE_PRIVATE)
        val listSettings = ListSettings.fromPrefs(prefs)

        ListBottomAppBar(
            module = Module.TODO,
            iCal4ListRel = emptyList(),
            listSettings = listSettings,
            allowNewEntries = true,
            incompatibleSyncApps = listOf(SyncApp.DAVX5),
            isBiometricsEnabled = true,
            isBiometricsUnlocked = false,
            multiselectEnabled = remember { mutableStateOf(false) },
            selectedEntries = remember { mutableStateListOf() },
            onAddNewEntry = { },
            showQuickEntry = remember { mutableStateOf(true) },
            onFilterIconClicked = { },
            onGoToDateSelected = { },
            onDeleteSelectedClicked = { },
            onUpdateSelectedClicked = { },
            onToggleBiometricAuthentication = { },
            onDeleteDone = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ListBottomAppBar_Preview_Todo_filterActive() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(ListViewModel.PREFS_LIST_TODOS, Context.MODE_PRIVATE)
        val listSettings = ListSettings.fromPrefs(prefs)

        ListBottomAppBar(
            module = Module.TODO,
            iCal4ListRel = emptyList(),
            listSettings = listSettings,
            allowNewEntries = true,
            isBiometricsEnabled = true,
            isBiometricsUnlocked = true,
            incompatibleSyncApps = listOf(SyncApp.DAVX5),
            multiselectEnabled = remember { mutableStateOf(false) },
            selectedEntries = remember { mutableStateListOf() },
            onAddNewEntry = { },
            showQuickEntry = remember { mutableStateOf(true) },
            onFilterIconClicked = { },
            onGoToDateSelected = { },
            onDeleteSelectedClicked = { },
            onUpdateSelectedClicked = { },
            onToggleBiometricAuthentication = { },
            onDeleteDone = { }
        )
    }
}
