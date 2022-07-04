package at.techbee.jtx.ui.compose.appbars

import android.os.Parcel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.ListSettings
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.compose.elements.LabelledCheckbox
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
    onGoToDateSelected: (Long) -> Unit,
) {

    var menuExpanded by remember { mutableStateOf(false) }
    val iCal4List by iCal4ListLive.observeAsState()
    val context = LocalContext.current

    BottomAppBar(
        icons = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = stringResource(id = R.string.more)
                )
            }
            IconButton(onClick = { onFilterIconClicked() }) {
                Icon(
                    Icons.Outlined.FilterList,
                    contentDescription = stringResource(id = R.string.filter)
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

            if (module == Module.JOURNAL) {
                IconButton(onClick = {
                    // Create a custom date validator to only enable dates that are in the list
                    val customDateValidator = object : CalendarConstraints.DateValidator {
                        override fun describeContents(): Int {
                            return 0
                        }

                        override fun writeToParcel(dest: Parcel?, flags: Int) {}
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
            }
        },
        floatingActionButton = {
            // TODO(b/228588827): Replace with Secondary FAB when available.
            FloatingActionButton(
                onClick = { /* TODO */ },
                elevation = BottomAppBarDefaults.floatingActionButtonElevation()
            ) {
                Icon(Icons.Filled.Add, "New Entry")   // TODO: move to strings
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun ListBottomAppBar_Preview_Journal() {
    MaterialTheme {

        ListBottomAppBar(
            module = Module.JOURNAL,
            iCal4ListLive = MutableLiveData(emptyList()),
            listSettings = ListSettings(),
            onAddNewEntry = { },
            onAddNewQuickEntry = { },
            onListSettingsChanged = { },
            onFilterIconClicked = { },
            onGoToDateSelected = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListBottomAppBar_Preview_Todo() {
    MaterialTheme {

        ListBottomAppBar(
            module = Module.TODO,
            iCal4ListLive = MutableLiveData(emptyList()),
            listSettings = ListSettings(),
            onAddNewEntry = { },
            onAddNewQuickEntry = { },
            onListSettingsChanged = { },
            onFilterIconClicked = { },
            onGoToDateSelected = { }
        )
    }
}
