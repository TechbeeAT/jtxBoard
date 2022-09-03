package at.techbee.jtx.ui.compose.appbars

import android.content.ContentResolver
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalCollection.Factory.DAVX5_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.ui.compose.elements.LabelledCheckbox
import at.techbee.jtx.util.SyncUtil

@Composable
fun DetailBottomAppBar(
    icalObject: ICalObject?,
    collection: ICalCollection?,
    isEditMode: MutableState<Boolean>,
    contentsChanged: MutableState<Boolean?>,
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
    //onListSettingsChanged: () -> Unit
) {

    if(icalObject == null || collection == null)
        return

    val context = LocalContext.current
    var settingsMenuExpanded by remember { mutableStateOf(false) }
    //val iCal4List by iCal4ListLive.observeAsState()

    val syncIconAnimation = rememberInfiniteTransition()
    val angle by syncIconAnimation.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
            }
        )
    )

    val isPreview = LocalInspectionMode.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isSyncInProgress by remember { mutableStateOf(false) }
    DisposableEffect(lifecycleOwner) {

        val listener = if(isPreview)
            null
        else {
            ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE) {
                isSyncInProgress = SyncUtil.isJtxSyncRunning(context)
            }
        }
        onDispose {
            if(!isPreview)
                ContentResolver.removeStatusChangeListener(listener)
        }
    }


    BottomAppBar(
        actions = {
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

            AnimatedVisibility(collection.accountType != LOCAL_ACCOUNT_TYPE && (isSyncInProgress || icalObject.dirty)) {
                IconButton(
                    onClick = {
                        if (!isSyncInProgress)
                            collection.getAccount().let { SyncUtil.syncAccount(it) }
                    },
                    enabled = icalObject.dirty && !isSyncInProgress
                ) {
                    Crossfade(isSyncInProgress) { synchronizing ->
                        if (synchronizing) {
                            Icon(
                                Icons.Outlined.Sync,
                                contentDescription = stringResource(id = R.string.sync_in_progress),
                                modifier = Modifier
                                    .graphicsLayer {
                                        rotationZ = angle
                                    }
                            )
                        } else {
                            Icon(
                                Icons.Outlined.CloudSync,
                                contentDescription = stringResource(id = R.string.upload_pending),
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(contentsChanged.value != null) {
                IconButton(
                    onClick = { },
                    enabled = false
                ) {
                    Crossfade(contentsChanged.value) { changed ->
                        if(changed == false)
                            Icon(
                                Icons.Outlined.Save,
                                contentDescription = stringResource(id = R.string.saving)
                            )
                        else if(changed == true)
                            Icon(
                                Icons.Outlined.DriveFileRenameOutline,
                                contentDescription = stringResource(id = R.string.saving)
                            )
                    }
                }
            }


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
                    if(!collection.readonly)
                        isEditMode.value = !isEditMode.value
                          },
            ) {
                Crossfade(targetState = isEditMode.value) { isEditMode ->
                    if(isEditMode) {
                        Icon(Icons.Filled.Visibility, stringResource(id = R.string.save))
                    } else {
                        if(collection.readonly)
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

        val collection = ICalCollection().apply {
            this.readonly = false
            this.accountType = DAVX5_ACCOUNT_TYPE
        }

        DetailBottomAppBar(
            icalObject = ICalObject.createNote().apply { dirty = true },
            collection = collection,
            isEditMode = remember { mutableStateOf(false) },
            contentsChanged = remember { mutableStateOf(true) },
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

        val collection = ICalCollection().apply {
            this.readonly = false
            this.accountType = DAVX5_ACCOUNT_TYPE
        }


        DetailBottomAppBar(
            icalObject = ICalObject.createNote().apply { dirty = true },
            collection = collection,
            isEditMode = remember { mutableStateOf(true) },
            contentsChanged = remember { mutableStateOf(false) },
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

        val collection = ICalCollection().apply {
            this.readonly = true
            this.accountType = DAVX5_ACCOUNT_TYPE
        }

        DetailBottomAppBar(
            icalObject = ICalObject.createNote().apply { dirty = false },
            collection = collection,
            isEditMode = remember { mutableStateOf(false) },
            contentsChanged = remember { mutableStateOf(null) },
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
fun DetailBottomAppBar_Preview_View_local() {
    MaterialTheme {

        val collection = ICalCollection().apply {
            this.readonly = false
            this.accountType = LOCAL_ACCOUNT_TYPE
        }

        DetailBottomAppBar(
            icalObject = ICalObject.createNote().apply { dirty = true },
            collection = collection,
            isEditMode = remember { mutableStateOf(false) },
            contentsChanged = remember { mutableStateOf(null) },
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
