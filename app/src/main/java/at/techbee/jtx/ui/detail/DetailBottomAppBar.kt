/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.DetailSettings
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalCollection.Factory.DAVX5_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.reusable.elements.LabelledCheckbox
import at.techbee.jtx.util.SyncUtil

@Composable
fun DetailBottomAppBar(
    icalObject: ICalObject?,
    collection: ICalCollection?,
    isEditMode: MutableState<Boolean>,
    contentsChanged: MutableState<Boolean?>,
    detailSettings: DetailSettings,
    onDeleteClicked: () -> Unit,
    onCopyRequested: (Module) -> Unit,
    //onListSettingsChanged: () -> Unit
) {

    if (icalObject == null || collection == null)
        return

    val context = LocalContext.current
    var settingsMenuExpanded by remember { mutableStateOf(false) }
    var copyOptionsExpanded by remember { mutableStateOf(false) }
    val isProPurchased = BillingManager.getInstance().isProPurchased.observeAsState()

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

        val listener = if (isPreview)
            null
        else {
            ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE) {
                isSyncInProgress = SyncUtil.isJtxSyncRunning(context)
            }
        }
        onDispose {
            if (!isPreview)
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

            AnimatedVisibility(!isEditMode.value && !collection.readonly) {
                IconButton(onClick = { copyOptionsExpanded = true }) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(id = R.string.menu_view_copy_item),
                    )
                    DropdownMenu(
                        expanded = copyOptionsExpanded,
                        onDismissRequest = { copyOptionsExpanded = false }
                    ) {
                        if (collection.supportsVJOURNAL) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.menu_view_copy_as_journal)) },
                                onClick = {
                                    onCopyRequested(Module.JOURNAL)
                                    copyOptionsExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.menu_view_copy_as_note)) },
                                onClick = {
                                    onCopyRequested(Module.NOTE)
                                    copyOptionsExpanded = false
                                }
                            )
                        }
                        if (collection.supportsVTODO) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.menu_view_copy_as_todo)) },
                                onClick = {
                                    onCopyRequested(Module.TODO)
                                    copyOptionsExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(!isEditMode.value && !collection.readonly) {
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
                        if (changed == false)
                            Icon(
                                painterResource(id = R.drawable.ic_save_check_outline),
                                contentDescription = stringResource(id = R.string.saving),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.alpha(0.3f)
                            )
                        else if (changed == true)
                            Icon(
                                Icons.Outlined.DriveFileRenameOutline,
                                contentDescription = stringResource(id = R.string.saving),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.alpha(0.3f)
                            )
                    }
                }
            }


            // overflow menu
            DropdownMenu(
                expanded = settingsMenuExpanded,
                onDismissRequest = {
                    detailSettings.save()
                    settingsMenuExpanded = false
                }
            ) {
                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.categories),
                            isChecked = detailSettings.enableCategories.value,
                            onCheckedChanged = { detailSettings.enableCategories.value = !detailSettings.enableCategories.value }
                        )
                    },
                    onClick = { detailSettings.enableCategories.value = !detailSettings.enableCategories.value }
                )
                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.attendees),
                            isChecked = detailSettings.enableAttendees.value,
                            onCheckedChanged = {
                                detailSettings.enableAttendees.value =
                                    !detailSettings.enableAttendees.value
                            })
                    },
                    onClick = {
                        detailSettings.enableAttendees.value = !detailSettings.enableAttendees.value
                    }
                )
                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.resources),
                            isChecked = detailSettings.enableResources.value,
                            onCheckedChanged = { detailSettings.enableResources.value = !detailSettings.enableResources.value }
                        )
                    },
                    onClick = { detailSettings.enableResources.value = !detailSettings.enableResources.value }
                )
                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.contact),
                            isChecked = detailSettings.enableContact.value,
                            onCheckedChanged = { detailSettings.enableContact.value = !detailSettings.enableContact.value }
                        )
                    },
                    onClick = { detailSettings.enableContact.value = !detailSettings.enableContact.value }
                )
                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.location),
                            isChecked = detailSettings.enableLocation.value,
                            onCheckedChanged = { detailSettings.enableLocation.value = !detailSettings.enableLocation.value }
                        )
                    },
                    onClick = { detailSettings.enableLocation.value = !detailSettings.enableLocation.value }
                )
                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.url),
                            isChecked = detailSettings.enableUrl.value,
                            onCheckedChanged = {
                                detailSettings.enableUrl.value = !detailSettings.enableUrl.value
                            }
                        )
                    },
                    onClick = {
                        detailSettings.enableUrl.value = !detailSettings.enableUrl.value
                    }
                )
                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.subtasks),
                            isChecked = detailSettings.enableSubtasks.value,
                            onCheckedChanged = { detailSettings.enableSubtasks.value = !detailSettings.enableSubtasks.value }
                        )
                    },
                    onClick = { detailSettings.enableSubtasks.value = !detailSettings.enableSubtasks.value }
                )
                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.view_feedback_linked_notes),
                            isChecked = detailSettings.enableSubnotes.value,
                            onCheckedChanged = { detailSettings.enableSubnotes.value = !detailSettings.enableSubnotes.value }
                        )
                    },
                    onClick = { detailSettings.enableSubnotes.value = !detailSettings.enableSubnotes.value }
                )
                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.attachments),
                            isChecked = detailSettings.enableAttachments.value,
                            onCheckedChanged = { detailSettings.enableAttachments.value = !detailSettings.enableAttachments.value }
                        )
                    },
                    onClick = { detailSettings.enableAttachments.value = !detailSettings.enableAttachments.value }
                )
                if (icalObject.module != Module.NOTE.name) {   //Never show the recurring tab for Notes
                    DropdownMenuItem(
                        text = {
                            LabelledCheckbox(
                                text = stringResource(id = R.string.recurrence),
                                isChecked = detailSettings.enableRecurrence.value,
                                onCheckedChanged = { detailSettings.enableRecurrence.value = !detailSettings.enableRecurrence.value }
                            )
                        },
                        onClick = {
                            detailSettings.enableRecurrence.value =
                                !detailSettings.enableRecurrence.value
                        }
                    )
                }
                if (icalObject.module == Module.TODO.name) {    //Never show the recurring tab for Journals and Notes, only for Todos
                    DropdownMenuItem(
                        text = {
                            LabelledCheckbox(
                                text = stringResource(id = R.string.alarms),
                                isChecked = detailSettings.enableAlarms.value,
                                onCheckedChanged = { detailSettings.enableAlarms.value = !detailSettings.enableAlarms.value }
                            )
                        },
                        onClick = { detailSettings.enableAlarms.value = !detailSettings.enableAlarms.value }
                    )
                }
                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.comments),
                            isChecked = detailSettings.enableComments.value,
                            onCheckedChanged = { detailSettings.enableComments.value = !detailSettings.enableComments.value }
                        )
                    },
                    onClick = { detailSettings.enableComments.value = !detailSettings.enableComments.value }
                )
            }


        },
        floatingActionButton = {
            // TODO(b/228588827): Replace with Secondary FAB when available.
            FloatingActionButton(
                onClick = {
                    if (!collection.readonly && collection.accountType != LOCAL_ACCOUNT_TYPE && isProPurchased.value == false)
                        Toast.makeText(
                            context,
                            context.getText(R.string.buypro_snackbar_remote_entries_blocked),
                            Toast.LENGTH_LONG
                        ).show()
                    else if (!collection.readonly)
                        isEditMode.value = !isEditMode.value
                },
                containerColor = if (collection.readonly) MaterialTheme.colorScheme.surface
                else if (collection.accountType != LOCAL_ACCOUNT_TYPE && isProPurchased.value == false) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.primaryContainer
            ) {
                Crossfade(targetState = isEditMode.value) { isEditMode ->
                    if (isEditMode) {
                        Icon(painterResource(id = R.drawable.ic_save_move_outline), stringResource(id = R.string.save))
                    } else {
                        if (collection.readonly)
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

        val prefs: SharedPreferences = LocalContext.current.getSharedPreferences(
            DetailViewModel.PREFS_DETAIL_NOTES,
            Context.MODE_PRIVATE
        )
        val detailSettings = DetailSettings(prefs)

        DetailBottomAppBar(
            icalObject = ICalObject.createNote().apply { dirty = true },
            collection = collection,
            isEditMode = remember { mutableStateOf(false) },
            contentsChanged = remember { mutableStateOf(true) },
            detailSettings = detailSettings,
            onDeleteClicked = { },
            onCopyRequested = { }
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

        val prefs: SharedPreferences = LocalContext.current.getSharedPreferences(
            DetailViewModel.PREFS_DETAIL_NOTES,
            Context.MODE_PRIVATE
        )
        val detailSettings = DetailSettings(prefs)

        DetailBottomAppBar(
            icalObject = ICalObject.createNote().apply { dirty = true },
            collection = collection,
            isEditMode = remember { mutableStateOf(true) },
            contentsChanged = remember { mutableStateOf(false) },
            detailSettings = detailSettings,
            onDeleteClicked = { },
            onCopyRequested = { }
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

        val prefs: SharedPreferences = LocalContext.current.getSharedPreferences(
            DetailViewModel.PREFS_DETAIL_NOTES,
            Context.MODE_PRIVATE
        )
        val detailSettings = DetailSettings(prefs)

        DetailBottomAppBar(
            icalObject = ICalObject.createNote().apply { dirty = false },
            collection = collection,
            isEditMode = remember { mutableStateOf(false) },
            contentsChanged = remember { mutableStateOf(null) },
            detailSettings = detailSettings,
            onDeleteClicked = { },
            onCopyRequested = { }
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

        val prefs: SharedPreferences = LocalContext.current.getSharedPreferences(
            DetailViewModel.PREFS_DETAIL_NOTES,
            Context.MODE_PRIVATE
        )
        val detailSettings = DetailSettings(prefs)

        BillingManager.getInstance().initialise(LocalContext.current.applicationContext)

        DetailBottomAppBar(
            icalObject = ICalObject.createNote().apply { dirty = true },
            collection = collection,
            isEditMode = remember { mutableStateOf(false) },
            contentsChanged = remember { mutableStateOf(null) },
            detailSettings = detailSettings,
            onDeleteClicked = { },
            onCopyRequested = { }
        )
    }
}
