package at.techbee.jtx.ui.compose.appbars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.ListSettings
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.ui.compose.elements.LabelledCheckbox

@Composable
fun ListBottomAppBar(
    module: Module,
    listSettings: ListSettings,
    onAddNewQuickEntry: () -> Unit,
    onAddNewEntry: () -> Unit,
    onListSettingsChanged: () -> Unit,
    onFilterIconClicked: () -> Unit
) {

    var menuExpanded by remember { mutableStateOf(false) }


    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false }
    ) {
        DropdownMenuItem(
            text = { LabelledCheckbox(
                text = stringResource(id = R.string.menu_list_todo_hide_completed),
                isChecked = listSettings.isExcludeDone.value,
                onCheckedChanged = { checked ->
                    listSettings.isExcludeDone.value = checked
                    onListSettingsChanged()
                } )},
            onClick = {
                listSettings.isExcludeDone.value = !listSettings.isExcludeDone.value
                onListSettingsChanged()
            }
        )
        if(module == Module.TODO) {
            DropdownMenuItem(
                text = { LabelledCheckbox(
                    text = stringResource(id = R.string.list_due_overdue),
                    isChecked = listSettings.isFilterOverdue.value,
                    onCheckedChanged = { checked ->
                        listSettings.isFilterOverdue.value = checked
                        onListSettingsChanged()
                    } )},
                onClick = {
                    listSettings.isFilterOverdue.value = !listSettings.isFilterOverdue.value
                    onListSettingsChanged()
                }
            )
            DropdownMenuItem(
                text = { LabelledCheckbox(
                    text = stringResource(id = R.string.list_due_today),
                    isChecked = listSettings.isFilterDueToday.value,
                    onCheckedChanged = { checked ->
                        listSettings.isFilterDueToday.value = checked
                        onListSettingsChanged()
                    } )},
                onClick = {
                    listSettings.isFilterDueToday.value = !listSettings.isFilterDueToday.value
                    onListSettingsChanged()
                }
            )
            DropdownMenuItem(
                text = { LabelledCheckbox(
                    text = stringResource(id = R.string.list_due_tomorrow),
                    isChecked = listSettings.isFilterDueTomorrow.value,
                    onCheckedChanged = { checked ->
                        listSettings.isFilterDueTomorrow.value = checked
                        onListSettingsChanged()
                    } )},
                onClick = {
                    listSettings.isFilterDueTomorrow.value = !listSettings.isFilterDueTomorrow.value
                    onListSettingsChanged()
                }
            )
            DropdownMenuItem(
                text = { LabelledCheckbox(
                    text = stringResource(id = R.string.list_due_future),
                    isChecked = listSettings.isFilterDueFuture.value,
                    onCheckedChanged = { checked ->
                        listSettings.isFilterDueFuture.value = checked
                        onListSettingsChanged()
                    } )},
                onClick = {
                    listSettings.isFilterDueFuture.value = !listSettings.isFilterDueFuture.value
                    onListSettingsChanged()
                }
            )
            DropdownMenuItem(
                text = { LabelledCheckbox(
                    text = stringResource(id = R.string.list_no_dates_set),
                    isChecked = listSettings.isFilterNoDatesSet.value,
                    onCheckedChanged = { checked ->
                        listSettings.isFilterNoDatesSet.value = checked
                        onListSettingsChanged()
                    } )},
                onClick = {
                    listSettings.isFilterNoDatesSet.value = !listSettings.isFilterNoDatesSet.value
                    onListSettingsChanged()
                }
            )
        }
    }

    BottomAppBar(
        icons = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(id = R.string.more))
            }
            IconButton(onClick = { onFilterIconClicked() }) {
                Icon(Icons.Outlined.FilterList, contentDescription = stringResource(id = R.string.filter))
            }
            IconButton(onClick = { onAddNewQuickEntry() }) {
                Icon(painterResource(
                    id = R.drawable.ic_add_quick),
                    contentDescription = stringResource(id = when(module) {
                        Module.JOURNAL -> R.string.menu_list_quick_journal
                        Module.NOTE -> R.string.menu_list_quick_note
                        Module.TODO -> R.string.menu_list_quick_todo
                    })
                )
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
            listSettings = ListSettings(),
            onAddNewEntry = { },
            onAddNewQuickEntry = { },
            onListSettingsChanged = {  },
            onFilterIconClicked = { }
            )
        }
}

@Preview(showBackground = true)
@Composable
fun ListBottomAppBar_Preview_Todo() {
    MaterialTheme {

        ListBottomAppBar(
            module = Module.TODO,
            listSettings = ListSettings(),
            onAddNewEntry = { },
            onAddNewQuickEntry = { },
            onListSettingsChanged = {  },
            onFilterIconClicked = { }
        )
    }
}
