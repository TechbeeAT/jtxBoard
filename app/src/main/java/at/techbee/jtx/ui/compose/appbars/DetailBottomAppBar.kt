package at.techbee.jtx.ui.compose.appbars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.ui.compose.elements.LabelledCheckbox
import at.techbee.jtx.ui.compose.elements.LabelledSwitch
import at.techbee.jtx.ui.compose.elements.SwitchSetting

@Composable
fun DetailBottomAppBar(
    module: Module,
    isEditMode: MutableState<Boolean>,
    isReadOnly: MutableState<Boolean>,
    enableCategories: MutableState<Boolean>,
    enableAttendees: MutableState<Boolean>,
    enableResources: MutableState<Boolean>,
    enableContact: MutableState<Boolean>,
    enableLocation: MutableState<Boolean>,
    enableUrl: MutableState<Boolean>,
    enableSubtasks: MutableState<Boolean>,
    enableSubnotes: MutableState<Boolean>,
    enableAttachments: MutableState<Boolean>,
    enableRecurrence: MutableState<Boolean>,
    enableAlarms: MutableState<Boolean>,
    enableComments: MutableState<Boolean>,
    //iCal4ListLive: LiveData<List<ICal4List>>,
    //listSettings: ListSettings,
    onDeleteClicked: () -> Unit,
    //onAddNewQuickEntry: () -> Unit,
    //onAddNewEntry: () -> Unit,
    //onListSettingsChanged: () -> Unit,
    //onFilterIconClicked: () -> Unit,
    //onClearFilterClicked: () -> Unit,
    //onGoToDateSelected: (Long) -> Unit,
    //onSearchTextClicked: () -> Unit
) {

    var settingsMenuExpanded by remember { mutableStateOf(false) }
    //val iCal4List by iCal4ListLive.observeAsState()
    val context = LocalContext.current

    BottomAppBar(
        icons = {
            AnimatedVisibility(isEditMode.value) {
                IconButton(onClick = { settingsMenuExpanded = true }) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = stringResource(id = R.string.preferences)
                    )
                }
            }

            AnimatedVisibility(!isEditMode.value) {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(id = R.string.menu_view_copy_item),
                    )
                }
            }

            AnimatedVisibility(!isEditMode.value) {
                IconButton(onClick = { onDeleteClicked() }) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = stringResource(id = R.string.delete),
                    )
                }
            }

            //Box(modifier = Modifier.size(width = 48.dp, height = 1.dp))

            Icon(
                Icons.Outlined.CloudSync,
                contentDescription = stringResource(id = R.string.upload_pending),
                modifier = Modifier.alpha(0.2f)
            )



            // overflow menu
            DropdownMenu(
                expanded = settingsMenuExpanded,
                onDismissRequest = { settingsMenuExpanded = false }
            ) {

                LabelledCheckbox(
                    text = stringResource(id = R.string.categories),
                    isChecked = enableCategories.value,
                    onCheckedChanged = {
                        /* TODO */
                        enableCategories.value = !enableCategories.value
                    })
                LabelledCheckbox(
                    text = stringResource(id = R.string.attendees),
                    isChecked = enableAttendees.value,
                    onCheckedChanged = {
                        /* TODO */
                        enableAttendees.value = !enableAttendees.value
                    })
                LabelledCheckbox(
                    text = stringResource(id = R.string.resources),
                    isChecked = enableResources.value,
                    onCheckedChanged = {
                        /* TODO */
                        enableResources.value = !enableResources.value
                    })
                LabelledCheckbox(
                    text = stringResource(id = R.string.contact),
                    isChecked = enableContact.value,
                    onCheckedChanged = {
                        /* TODO */
                        enableContact.value = !enableContact.value
                    })
                LabelledCheckbox(
                    text = stringResource(id = R.string.location),
                    isChecked = enableLocation.value,
                    onCheckedChanged = {
                        /* TODO */
                        enableLocation.value = !enableLocation.value
                    })
                LabelledCheckbox(
                    text = stringResource(id = R.string.url),
                    isChecked = enableUrl.value,
                    onCheckedChanged = {
                        /* TODO */
                        enableUrl.value = !enableUrl.value
                    })
                LabelledCheckbox(
                    text = stringResource(id = R.string.subtasks),
                    isChecked = enableSubtasks.value,
                    onCheckedChanged = {
                        /* TODO */
                        enableSubtasks.value = !enableSubtasks.value
                    })
                LabelledCheckbox(
                    text = stringResource(id = R.string.view_feedback_linked_notes),
                    isChecked = enableSubnotes.value,
                    onCheckedChanged = {
                        /* TODO */
                        enableSubnotes.value = !enableSubnotes.value
                    })
                LabelledCheckbox(
                    text = stringResource(id = R.string.attachments),
                    isChecked = enableAttachments.value,
                    onCheckedChanged = {
                        /* TODO */
                        enableAttachments.value = !enableAttachments.value
                    })
                LabelledCheckbox(
                    text = stringResource(id = R.string.recurrence),
                    isChecked = enableRecurrence.value,
                    onCheckedChanged = {
                        /* TODO */
                        enableRecurrence.value = !enableRecurrence.value
                    })
                LabelledCheckbox(
                    text = stringResource(id = R.string.alarms),
                    isChecked = enableAlarms.value,
                    onCheckedChanged = {
                        /* TODO */
                        enableAlarms.value = !enableAlarms.value
                    })
                LabelledCheckbox(
                    text = stringResource(id = R.string.comments),
                    isChecked = enableComments.value,
                    onCheckedChanged = {
                        /* TODO */
                        enableComments.value = !enableComments.value
                    })

            }
        },
        floatingActionButton = {
            // TODO(b/228588827): Replace with Secondary FAB when available.
            FloatingActionButton(
                onClick = {
                    if(!isReadOnly.value)
                        isEditMode.value = !isEditMode.value
                          /* TODO */
                          },
            ) {
                Crossfade(targetState = isEditMode.value) { isEditMode ->
                    if(isEditMode) {
                        Icon(Icons.Filled.Visibility, stringResource(id = R.string.save))
                    } else {
                        if(isReadOnly.value)
                            Icon(Icons.Filled.EditOff, stringResource(id = R.string.readyonly))
                        else
                            Icon(Icons.Filled.Edit, stringResource(id = R.string.edit))
                    }
                }

            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun DetailBottomAppBar_Preview_View() {
    MaterialTheme {

        DetailBottomAppBar(
            module = Module.JOURNAL,
            isEditMode = remember { mutableStateOf(false) },
            isReadOnly = remember { mutableStateOf(false) },
            enableCategories = remember { mutableStateOf(true) },
            enableAttendees = remember { mutableStateOf(false) },
            enableResources = remember { mutableStateOf(false) },
            enableContact = remember { mutableStateOf(false) },
            enableLocation = remember { mutableStateOf(false) },
            enableUrl = remember { mutableStateOf(false) },
            enableSubtasks = remember { mutableStateOf(true) },
            enableSubnotes = remember { mutableStateOf(true) },
            enableAttachments = remember { mutableStateOf(true) },
            enableRecurrence = remember { mutableStateOf(false) },
            enableAlarms = remember { mutableStateOf(false) },
            enableComments = remember { mutableStateOf(false) },
            onDeleteClicked = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailBottomAppBar_Preview_edit() {
    MaterialTheme {

        DetailBottomAppBar(
            module = Module.JOURNAL,
            isEditMode = remember { mutableStateOf(true) },
            isReadOnly = remember { mutableStateOf(false) },
            enableCategories = remember { mutableStateOf(true) },
            enableAttendees = remember { mutableStateOf(false) },
            enableResources = remember { mutableStateOf(false) },
            enableContact = remember { mutableStateOf(false) },
            enableLocation = remember { mutableStateOf(false) },
            enableUrl = remember { mutableStateOf(false) },
            enableSubtasks = remember { mutableStateOf(true) },
            enableSubnotes = remember { mutableStateOf(true) },
            enableAttachments = remember { mutableStateOf(true) },
            enableRecurrence = remember { mutableStateOf(false) },
            enableAlarms = remember { mutableStateOf(false) },
            enableComments = remember { mutableStateOf(false) },
            onDeleteClicked = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailBottomAppBar_Preview_View_readonly() {
    MaterialTheme {

        DetailBottomAppBar(
            module = Module.JOURNAL,
            isEditMode = remember { mutableStateOf(false) },
            isReadOnly = remember { mutableStateOf(true) },
            enableCategories = remember { mutableStateOf(true) },
            enableAttendees = remember { mutableStateOf(false) },
            enableResources = remember { mutableStateOf(false) },
            enableContact = remember { mutableStateOf(false) },
            enableLocation = remember { mutableStateOf(false) },
            enableUrl = remember { mutableStateOf(false) },
            enableSubtasks = remember { mutableStateOf(true) },
            enableSubnotes = remember { mutableStateOf(true) },
            enableAttachments = remember { mutableStateOf(true) },
            enableRecurrence = remember { mutableStateOf(false) },
            enableAlarms = remember { mutableStateOf(false) },
            enableComments = remember { mutableStateOf(false) },
            onDeleteClicked = { }
        )
    }
}
