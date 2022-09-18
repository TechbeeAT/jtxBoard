/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.appbars

import android.content.Context
import android.os.Parcel
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.ListSettings
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.ListViewModel
import at.techbee.jtx.ui.reusable.elements.LabelledCheckbox
import at.techbee.jtx.util.DateTimeUtils
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun ListBottomAppBar(
    module: Module,
    iCal4ListLive: LiveData<List<ICal4List>>,
    listSettings: ListSettings,
    onAddNewQuickEntry: () -> Unit,
    onAddNewEntry: () -> Unit,
    onListSettingsChanged: () -> Unit,
    onFilterIconClicked: () -> Unit,
    onClearFilterClicked: () -> Unit,
    onGoToDateSelected: (Long) -> Unit,
    onSearchTextClicked: () -> Unit
) {

    var menuExpanded by remember { mutableStateOf(false) }
    val iCal4List by iCal4ListLive.observeAsState()
    val context = LocalContext.current

    val isFilterActive = listSettings.searchCategories.value.isNotEmpty()
                //|| searchOrganizers.value.isNotEmpty()
                || (module == Module.JOURNAL && listSettings.searchStatusJournal.value.isNotEmpty())
                || (module == Module.NOTE && listSettings.searchStatusJournal.value.isNotEmpty())
                || (module == Module.TODO && listSettings.searchStatusTodo.value.isNotEmpty())
                || listSettings.searchClassification.value.isNotEmpty() || listSettings.searchCollection.value.isNotEmpty()
                || listSettings.searchAccount.value.isNotEmpty()


    BottomAppBar(
        actions = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = stringResource(id = R.string.more)
                )
            }
            IconButton(onClick = { onFilterIconClicked() }) {
                Icon(
                    Icons.Outlined.FilterList,
                    contentDescription = stringResource(id = R.string.filter),
                    tint = if (isFilterActive) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
            IconButton(onClick = { onAddNewQuickEntry() }) {
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
                    )
                )
            }
            IconButton(onClick = { onSearchTextClicked() }) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = stringResource(id = R.string.search),
                    tint = if (listSettings.searchText.value.isNotBlank()) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }

            if (module == Module.JOURNAL) {
                IconButton(onClick = {
                    // Create a custom date validator to only enable dates that are in the list
                    val customDateValidator = object : CalendarConstraints.DateValidator {
                        override fun describeContents(): Int { return 0 }
                        override fun writeToParcel(p0: Parcel, p1: Int) { }
                        override fun isValid(date: Long): Boolean {
                            iCal4List?.forEach {
                                val zonedDtstart = ZonedDateTime.ofInstant(
                                    Instant.ofEpochMilli(it.dtstart ?: 0L),
                                    DateTimeUtils.requireTzId(it.dtstartTimezone)
                                )
                                val zonedSelection = ZonedDateTime.ofInstant(
                                    Instant.ofEpochMilli(date),
                                    ZoneId.systemDefault()
                                )

                                if (zonedDtstart.dayOfMonth == zonedSelection.dayOfMonth && zonedDtstart.monthValue == zonedSelection.monthValue && zonedDtstart.year == zonedSelection.year)
                                    return true
                            }
                            return false
                        }
                    }

                    // Build constraints.
                    val constraintsBuilder =
                        CalendarConstraints.Builder().apply {
                            var dates = iCal4List?.map { it.dtstart ?: System.currentTimeMillis() }
                                ?.toList()
                            if (dates.isNullOrEmpty())
                                dates = listOf(System.currentTimeMillis())
                            setStart(dates.minOf { it })
                            setEnd(dates.maxOf { it })
                            setValidator(customDateValidator)
                        }

                    val datePicker =
                        MaterialDatePicker.Builder.datePicker()
                            .setTitleText(R.string.edit_datepicker_dialog_select_date)
                            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                            .setCalendarConstraints(constraintsBuilder.build())
                            .build()

                    datePicker.addOnPositiveButtonClickListener {
                        // Respond to positive button click.
                        val zonedSelection = ZonedDateTime.ofInstant(
                            Instant.ofEpochMilli(it),
                            ZoneId.systemDefault()
                        )

                        // find the item with the same date
                        val matchedItem = iCal4List?.find { item ->
                            val zonedMatch = ZonedDateTime.ofInstant(
                                Instant.ofEpochMilli(item.dtstart ?: 0L),
                                DateTimeUtils.requireTzId(item.dtstartTimezone)
                            )
                            zonedSelection.dayOfMonth == zonedMatch.dayOfMonth && zonedSelection.monthValue == zonedMatch.monthValue && zonedSelection.year == zonedMatch.year
                        }
                        if (matchedItem != null)
                            onGoToDateSelected(matchedItem.id)
                    }

                    datePicker.show(
                        (context as MainActivity2).supportFragmentManager,
                        "menu_list_gotodate"
                    )
                }) {
                    Icon(
                        Icons.Outlined.DateRange,
                        contentDescription = stringResource(id = R.string.menu_list_gotodate)
                    )
                }

            }


            // overflow menu
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.menu_list_todo_hide_completed),
                            isChecked = listSettings.isExcludeDone.value,
                            onCheckedChanged = { checked ->
                                listSettings.isExcludeDone.value = checked
                                onListSettingsChanged()
                            })
                    },
                    onClick = {
                        listSettings.isExcludeDone.value = !listSettings.isExcludeDone.value
                        onListSettingsChanged()
                    }
                )

                if (module == Module.TODO) {
                    DropdownMenuItem(
                        text = {
                            LabelledCheckbox(
                                text = stringResource(id = R.string.list_due_overdue),
                                isChecked = listSettings.isFilterOverdue.value,
                                onCheckedChanged = { checked ->
                                    listSettings.isFilterOverdue.value = checked
                                    onListSettingsChanged()
                                })
                        },
                        onClick = {
                            listSettings.isFilterOverdue.value = !listSettings.isFilterOverdue.value
                            onListSettingsChanged()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            LabelledCheckbox(
                                text = stringResource(id = R.string.list_due_today),
                                isChecked = listSettings.isFilterDueToday.value,
                                onCheckedChanged = { checked ->
                                    listSettings.isFilterDueToday.value = checked
                                    onListSettingsChanged()
                                })
                        },
                        onClick = {
                            listSettings.isFilterDueToday.value =
                                !listSettings.isFilterDueToday.value
                            onListSettingsChanged()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            LabelledCheckbox(
                                text = stringResource(id = R.string.list_due_tomorrow),
                                isChecked = listSettings.isFilterDueTomorrow.value,
                                onCheckedChanged = { checked ->
                                    listSettings.isFilterDueTomorrow.value = checked
                                    onListSettingsChanged()
                                })
                        },
                        onClick = {
                            listSettings.isFilterDueTomorrow.value =
                                !listSettings.isFilterDueTomorrow.value
                            onListSettingsChanged()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            LabelledCheckbox(
                                text = stringResource(id = R.string.list_due_future),
                                isChecked = listSettings.isFilterDueFuture.value,
                                onCheckedChanged = { checked ->
                                    listSettings.isFilterDueFuture.value = checked
                                    onListSettingsChanged()
                                })
                        },
                        onClick = {
                            listSettings.isFilterDueFuture.value =
                                !listSettings.isFilterDueFuture.value
                            onListSettingsChanged()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            LabelledCheckbox(
                                text = stringResource(id = R.string.list_no_dates_set),
                                isChecked = listSettings.isFilterNoDatesSet.value,
                                onCheckedChanged = { checked ->
                                    listSettings.isFilterNoDatesSet.value = checked
                                    onListSettingsChanged()
                                })
                        },
                        onClick = {
                            listSettings.isFilterNoDatesSet.value =
                                !listSettings.isFilterNoDatesSet.value
                            onListSettingsChanged()
                        }
                    )
                }
                if(isFilterActive) {
                    Divider()
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.menu_list_clearfilter))  },
                        leadingIcon = { Icon(Icons.Outlined.FilterListOff, null, modifier = Modifier.padding(8.dp)) },
                        onClick = { onClearFilterClicked() }
                    )
                }
            }
        },
        floatingActionButton = {
            // TODO(b/228588827): Replace with Secondary FAB when available.
            FloatingActionButton(
                onClick = {
                          onAddNewEntry()
                          /* TODO */
                          },
            ) {
                when (module) {
                    Module.JOURNAL -> Icon(Icons.Outlined.EventNote, stringResource(R.string.toolbar_text_add_journal))
                    Module.NOTE -> Icon(Icons.Outlined.NoteAdd, stringResource(R.string.toolbar_text_add_note))
                    Module.TODO -> Icon(Icons.Outlined.AddTask, stringResource(R.string.toolbar_text_add_task))

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

        val listSettings = ListSettings(prefs)
        listSettings.searchText.value = "whatever"

        ListBottomAppBar(
            module = Module.JOURNAL,
            iCal4ListLive = MutableLiveData(emptyList()),
            listSettings = listSettings,
            onAddNewEntry = { },
            onAddNewQuickEntry = { },
            onListSettingsChanged = { },
            onFilterIconClicked = { },
            onGoToDateSelected = { },
            onSearchTextClicked = { },
            onClearFilterClicked = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListBottomAppBar_Preview_Note() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(ListViewModel.PREFS_LIST_NOTES, Context.MODE_PRIVATE)

        val listSettings = ListSettings(prefs)
        listSettings.searchText.value = "whatever"

        ListBottomAppBar(
            module = Module.NOTE,
            iCal4ListLive = MutableLiveData(emptyList()),
            listSettings = listSettings,
            onAddNewEntry = { },
            onAddNewQuickEntry = { },
            onListSettingsChanged = { },
            onFilterIconClicked = { },
            onGoToDateSelected = { },
            onSearchTextClicked = { },
            onClearFilterClicked = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListBottomAppBar_Preview_Todo() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(ListViewModel.PREFS_LIST_TODOS, Context.MODE_PRIVATE)

        val listSettings = ListSettings(prefs)
        listSettings.searchText.value = "whatever"

        ListBottomAppBar(
            module = Module.TODO,
            iCal4ListLive = MutableLiveData(emptyList()),
            listSettings = listSettings,
            onAddNewEntry = { },
            onAddNewQuickEntry = { },
            onListSettingsChanged = { },
            onFilterIconClicked = { },
            onGoToDateSelected = { },
            onSearchTextClicked = { },
            onClearFilterClicked = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ListBottomAppBar_Preview_Todo_filterActive() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(ListViewModel.PREFS_LIST_TODOS, Context.MODE_PRIVATE)

        val listSettings = ListSettings(prefs)
        listSettings.searchCategories.value = listOf("Whatever")

        ListBottomAppBar(
            module = Module.TODO,
            iCal4ListLive = MutableLiveData(emptyList()),
            listSettings = listSettings,
            onAddNewEntry = { },
            onAddNewQuickEntry = { },
            onListSettingsChanged = { },
            onFilterIconClicked = { },
            onGoToDateSelected = { },
            onSearchTextClicked = { },
            onClearFilterClicked = { }
        )
    }
}
