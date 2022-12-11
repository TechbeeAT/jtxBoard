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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.reusable.dialogs.DatePickerDialog
import at.techbee.jtx.util.DateTimeUtils
import java.util.*

@Composable
fun ListBottomAppBar(
    module: Module,
    iCal4ListLive: LiveData<List<ICal4List>>,
    listSettings: ListSettings,
    showQuickEntry: MutableState<Boolean>,
    allowNewEntries: Boolean,
    onAddNewEntry: () -> Unit,
    onFilterIconClicked: () -> Unit,
    onGoToDateSelected: (Long) -> Unit,
    onSearchTextClicked: () -> Unit
) {

    var showGoToDatePicker by remember { mutableStateOf(false) }
    val iCal4List by iCal4ListLive.observeAsState()

    val isFilterActive = listSettings.searchCategories.value.isNotEmpty()
                //|| searchOrganizers.value.isNotEmpty()
            || (module == Module.JOURNAL && listSettings.searchStatusJournal.value.isNotEmpty())
            || (module == Module.NOTE && listSettings.searchStatusJournal.value.isNotEmpty())
            || (module == Module.TODO && listSettings.searchStatusTodo.value.isNotEmpty())
            || listSettings.searchClassification.value.isNotEmpty() || listSettings.searchCollection.value.isNotEmpty()
            || listSettings.searchAccount.value.isNotEmpty()
            || listSettings.isExcludeDone.value
            || listSettings.isFilterStartInPast.value
            || listSettings.isFilterStartToday.value
            || listSettings.isFilterStartTomorrow.value
            || listSettings.isFilterStartFuture.value
            || (module == Module.TODO && listSettings.isFilterOverdue.value)
            || (module == Module.TODO && listSettings.isFilterDueToday.value)
            || (module == Module.TODO && listSettings.isFilterDueTomorrow.value)
            || (module == Module.TODO && listSettings.isFilterDueFuture.value)
            || (module == Module.TODO && listSettings.isFilterNoDatesSet.value)
            || (module == Module.TODO && listSettings.isFilterNoStatusSet.value)
            || (module == Module.TODO && listSettings.isFilterNoClassificationSet.value)


    if(showGoToDatePicker) {
        var dates = iCal4List?.map { it.dtstart ?: System.currentTimeMillis() }?.toList()
        if (dates.isNullOrEmpty())
            dates = listOf(System.currentTimeMillis())

        // finds the closes number in a list of long
        fun List<Long>.findClosest(input: Long) = fold(null) { acc: Long?, num ->
            val closest = if (num <= input && (acc == null || num > acc)) num else acc
            if (closest == input) return@findClosest closest else return@fold closest
        }

        DatePickerDialog(
            datetime = DateTimeUtils.getTodayAsLong(),
            timezone = TimeZone.getDefault().id,
            allowNull = false,
            onConfirm = { selectedDate, _ ->
                selectedDate?.let { selected ->
                    val closestDate = dates.findClosest(selected)
                    iCal4List?.find { it.dtstart == closestDate }?.let { foundEntry ->
                        onGoToDateSelected(foundEntry.id)
                    }
                }
            },
            onDismiss = { showGoToDatePicker = false },
            dateOnly = true,
            minDate = dates.minOf { it },
            maxDate =  dates.maxOf { it }
        )
    }

    BottomAppBar(
        actions = {
            IconButton(onClick = { onFilterIconClicked() }) {
                Icon(
                    Icons.Outlined.FilterList,
                    contentDescription = stringResource(id = R.string.filter),
                    tint = if (isFilterActive) MaterialTheme.colorScheme.primary else LocalContentColor.current
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
            IconButton(onClick = { onSearchTextClicked() }) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = stringResource(id = R.string.search),
                    tint = if (listSettings.searchText.value != null) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }

            AnimatedVisibility(visible = module == Module.JOURNAL) {
                IconButton(onClick = { showGoToDatePicker = true }) {
                    Icon(
                        Icons.Outlined.DateRange,
                        contentDescription = stringResource(id = R.string.menu_list_gotodate)
                    )
                }
            }
        },
        floatingActionButton = {
            // TODO(b/228588827): Replace with Secondary FAB when available.
            AnimatedVisibility(allowNewEntries) {
                FloatingActionButton(
                    onClick = { onAddNewEntry() },
                ) {
                    Crossfade(module) {
                        when (it) {
                            Module.JOURNAL -> Icon(Icons.Outlined.EventNote, stringResource(R.string.toolbar_text_add_journal))
                            Module.NOTE -> Icon(Icons.Outlined.NoteAdd, stringResource(R.string.toolbar_text_add_note))
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
        listSettings.searchText.value = "whatever"

        ListBottomAppBar(
            module = Module.JOURNAL,
            iCal4ListLive = MutableLiveData(emptyList()),
            listSettings = listSettings,
            allowNewEntries = true,
            onAddNewEntry = { },
            showQuickEntry = remember { mutableStateOf(true) },
            onFilterIconClicked = { },
            onGoToDateSelected = { },
            onSearchTextClicked = { },
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
        listSettings.searchText.value = "whatever"

        ListBottomAppBar(
            module = Module.NOTE,
            iCal4ListLive = MutableLiveData(emptyList()),
            listSettings = listSettings,
            allowNewEntries = true,
            onAddNewEntry = { },
            showQuickEntry = remember { mutableStateOf(true) },
            onFilterIconClicked = { },
            onGoToDateSelected = { },
            onSearchTextClicked = { },
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
        listSettings.searchText.value = "whatever"

        ListBottomAppBar(
            module = Module.TODO,
            iCal4ListLive = MutableLiveData(emptyList()),
            listSettings = listSettings,
            allowNewEntries = true,
            onAddNewEntry = { },
            showQuickEntry = remember { mutableStateOf(true) },
            onFilterIconClicked = { },
            onGoToDateSelected = { },
            onSearchTextClicked = { },
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
        listSettings.searchCategories.value = listOf("Whatever")

        ListBottomAppBar(
            module = Module.TODO,
            iCal4ListLive = MutableLiveData(emptyList()),
            listSettings = listSettings,
            allowNewEntries = true,
            onAddNewEntry = { },
            showQuickEntry = remember { mutableStateOf(true) },
            onFilterIconClicked = { },
            onGoToDateSelected = { },
            onSearchTextClicked = { },
        )
    }
}
