package at.techbee.jtx.ui.compose.appbars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.ListSettings
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.ui.compose.elements.LabelledCheckbox
import at.techbee.jtx.ui.theme.JtxBoardTheme

@Composable
fun ListBottomAppBar(
    module: Module,
    listSettingsLive: MutableLiveData<ListSettings>,
    onAddNewEntry: (ICalEntity) -> Unit,
    onListSettingsChanged: (ListSettings) -> Unit,
    onFilterIconClicked: () -> Unit
) {

    var menuExpanded by remember { mutableStateOf(false) }
    val listSettings by listSettingsLive.observeAsState()


    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false }
    ) {
        DropdownMenuItem(
            text = { LabelledCheckbox(
                text = stringResource(id = R.string.menu_list_todo_hide_completed),
                isChecked = listSettings?.isExcludeDone?.value == true,
                onCheckedChanged = { checked ->
                    listSettings?.isExcludeDone?.value = checked
                    listSettings?.let { onListSettingsChanged(it) }
                } )},
            onClick = {
                listSettings?.isExcludeDone?.value?.let { listSettings?.isExcludeDone?.value = !it }
                listSettings?.let { onListSettingsChanged(it) }
            }
        )
        if(module == Module.TODO) {
            DropdownMenuItem(
                text = { LabelledCheckbox(
                    text = stringResource(id = R.string.list_due_overdue),
                    isChecked = listSettings?.isFilterOverdue?.value == true,
                    onCheckedChanged = { checked ->
                        listSettings?.isFilterOverdue?.value = checked
                        listSettings?.let { onListSettingsChanged(it) }
                    } )},
                onClick = {
                    listSettings?.isFilterOverdue?.value?.let { listSettings?.isFilterOverdue?.value = !it }
                    listSettings?.let { onListSettingsChanged(it) }
                }
            )
            DropdownMenuItem(
                text = { LabelledCheckbox(
                    text = stringResource(id = R.string.list_due_today),
                    isChecked = listSettings?.isFilterDueToday?.value == true,
                    onCheckedChanged = { checked ->
                        listSettings?.isFilterDueToday?.value = checked
                        listSettings?.let { onListSettingsChanged(it) }
                    } )},
                onClick = {
                    listSettings?.isFilterDueToday?.value?.let { listSettings?.isFilterDueToday?.value = !it }
                    listSettings?.let { onListSettingsChanged(it) }
                }
            )
            DropdownMenuItem(
                text = { LabelledCheckbox(
                    text = stringResource(id = R.string.list_due_tomorrow),
                    isChecked = listSettings?.isFilterDueTomorrow?.value == true,
                    onCheckedChanged = { checked ->
                        listSettings?.isFilterDueTomorrow?.value = checked
                        listSettings?.let { onListSettingsChanged(it) }
                    } )},
                onClick = {
                    listSettings?.isFilterDueTomorrow?.value?.let { listSettings?.isFilterDueTomorrow?.value = !it }
                    listSettings?.let { onListSettingsChanged(it) }
                }
            )
            DropdownMenuItem(
                text = { LabelledCheckbox(
                    text = stringResource(id = R.string.list_due_future),
                    isChecked = listSettings?.isFilterDueFuture?.value == true,
                    onCheckedChanged = { checked ->
                        listSettings?.isFilterDueFuture?.value = checked
                        listSettings?.let { onListSettingsChanged(it) }
                    } )},
                onClick = {
                    listSettings?.isFilterDueFuture?.value?.let { listSettings?.isFilterDueFuture?.value = !it }
                    listSettings?.let { onListSettingsChanged(it) }
                }
            )
            DropdownMenuItem(
                text = { LabelledCheckbox(
                    text = stringResource(id = R.string.list_no_dates_set),
                    isChecked = listSettings?.isFilterNoDatesSet?.value == true,
                    onCheckedChanged = { checked ->
                        listSettings?.isFilterNoDatesSet?.value = checked
                        listSettings?.let { onListSettingsChanged(it) }
                    } )},
                onClick = {
                    listSettings?.isFilterNoDatesSet?.let { listSettings?.isFilterNoDatesSet != it }
                    listSettings?.let { onListSettingsChanged(it) }
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
            IconButton(onClick = { /* doSomething() */ }) {
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
                onClick = {
                    val newEntry = when(module) {
                        Module.JOURNAL -> ICalEntity(ICalObject.createJournal())
                        Module.NOTE -> ICalEntity(ICalObject.createNote())
                        Module.TODO -> ICalEntity(ICalObject.createTodo())
                    }
                    onAddNewEntry(newEntry)
                },
                elevation = BottomAppBarDefaults.floatingActionButtonElevation()
            ) {
                Icon(Icons.Filled.Add, "Localized description")
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun ListBottomAppBar_Preview_Journal() {
    JtxBoardTheme {

        val listSettingsLive = MutableLiveData(ListSettings())

        ListBottomAppBar(
                module = Module.JOURNAL,
            listSettingsLive = listSettingsLive,
            onAddNewEntry = { },
            onListSettingsChanged = { newListSettings -> listSettingsLive.value = newListSettings },
            onFilterIconClicked = { }
            )
        }
}

@Preview(showBackground = true)
@Composable
fun ListBottomAppBar_Preview_Todo() {
    JtxBoardTheme {

        val listSettingsLive = MutableLiveData(ListSettings())

        ListBottomAppBar(
            module = Module.TODO,
            listSettingsLive = listSettingsLive,
            onAddNewEntry = { },
            onListSettingsChanged = { newListSettings -> listSettingsLive.value = newListSettings },
            onFilterIconClicked = { }
        )
    }
}
