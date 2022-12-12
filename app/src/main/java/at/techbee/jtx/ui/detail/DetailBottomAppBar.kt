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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalCollection.Factory.DAVX5_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.detail.DetailSettings.Companion.ENABLE_ALARMS
import at.techbee.jtx.ui.detail.DetailSettings.Companion.ENABLE_ATTACHMENTS
import at.techbee.jtx.ui.detail.DetailSettings.Companion.ENABLE_ATTENDEES
import at.techbee.jtx.ui.detail.DetailSettings.Companion.ENABLE_CATEGORIES
import at.techbee.jtx.ui.detail.DetailSettings.Companion.ENABLE_COMMENTS
import at.techbee.jtx.ui.detail.DetailSettings.Companion.ENABLE_CONTACT
import at.techbee.jtx.ui.detail.DetailSettings.Companion.ENABLE_LOCATION
import at.techbee.jtx.ui.detail.DetailSettings.Companion.ENABLE_RECURRENCE
import at.techbee.jtx.ui.detail.DetailSettings.Companion.ENABLE_RESOURCES
import at.techbee.jtx.ui.detail.DetailSettings.Companion.ENABLE_SUBNOTES
import at.techbee.jtx.ui.detail.DetailSettings.Companion.ENABLE_SUBTASKS
import at.techbee.jtx.ui.detail.DetailSettings.Companion.ENABLE_URL
import at.techbee.jtx.ui.reusable.elements.LabelledCheckbox
import at.techbee.jtx.util.SyncUtil

@Composable
fun DetailBottomAppBar(
    icalObject: ICalObject?,
    collection: ICalCollection?,
    isEditMode: MutableState<Boolean>,
    markdownState: MutableState<MarkdownState>,
    isProActionAvailable: Boolean,
    changeState: MutableState<DetailViewModel.DetailChangeState>,
    detailSettings: DetailSettings,
    onDeleteClicked: () -> Unit,
    onCopyRequested: (Module) -> Unit,
    onRevertClicked: () -> Unit,
) {

    if (icalObject == null || collection == null)
        return

    val context = LocalContext.current
    var settingsMenuExpanded by remember { mutableStateOf(false) }
    var copyOptionsExpanded by remember { mutableStateOf(false) }

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
            AnimatedVisibility(isEditMode.value && markdownState.value == MarkdownState.DISABLED) {
                IconButton(onClick = { settingsMenuExpanded = true }) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = stringResource(id = R.string.preferences)
                    )
                }
            }

            AnimatedVisibility(!isEditMode.value && !collection.readonly && isProActionAvailable) {
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

            if(!collection.readonly && isProActionAvailable && markdownState.value == MarkdownState.DISABLED) {
                IconButton(onClick = { onDeleteClicked() }) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = stringResource(id = R.string.delete),
                    )
                }
            }

            AnimatedVisibility(isEditMode.value && changeState.value != DetailViewModel.DetailChangeState.UNCHANGED && markdownState.value == MarkdownState.DISABLED) {
                IconButton(onClick = { onRevertClicked() }) {
                    Icon(
                        painterResource(id = R.drawable.ic_revert),
                        contentDescription = stringResource(id = R.string.revert)
                    )
                }
            }


            AnimatedVisibility(collection.accountType != LOCAL_ACCOUNT_TYPE && (isSyncInProgress || icalObject.dirty) && markdownState.value == MarkdownState.DISABLED) {
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
                                    .alpha(0.3f),
                                tint = MaterialTheme.colorScheme.primary,
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

            AnimatedVisibility((changeState.value == DetailViewModel.DetailChangeState.CHANGEUNSAVED
                    || changeState.value == DetailViewModel.DetailChangeState.CHANGESAVING
                    || changeState.value == DetailViewModel.DetailChangeState.CHANGESAVED)  && markdownState.value == MarkdownState.DISABLED) {
                IconButton(
                    onClick = { },
                    enabled = false
                ) {
                    Crossfade(changeState.value) { state ->
                        when(state) {
                            DetailViewModel.DetailChangeState.CHANGEUNSAVED -> {
                                Icon(
                                    Icons.Outlined.DriveFileRenameOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.alpha(0.3f)
                                )
                            }
                            DetailViewModel.DetailChangeState.CHANGESAVING -> {
                                Icon(
                                    painterResource(id = R.drawable.ic_saving),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.alpha(0.3f)
                                )
                            }
                            DetailViewModel.DetailChangeState.CHANGESAVED -> {
                                Icon(
                                    painterResource(id = R.drawable.ic_save_check_outline),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.alpha(0.3f)
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }

            // Icons for Markdown formatting
            AnimatedVisibility(isEditMode.value && markdownState.value != MarkdownState.DISABLED) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                ) {
                    IconButton(onClick = { markdownState.value = MarkdownState.BOLD }) {
                        Icon(Icons.Outlined.TextFormat, stringResource(R.string.markdown_bold))
                    }
                    IconButton(onClick = { markdownState.value = MarkdownState.ITALIC  }) {
                        Icon(Icons.Outlined.FormatItalic, stringResource(R.string.markdown_italic))
                    }
                    IconButton(onClick = { markdownState.value = MarkdownState.UNDERLINED  }) {
                        Icon(Icons.Outlined.FormatUnderlined, stringResource(R.string.markdown_underlined))
                    }
                    IconButton(onClick = { markdownState.value = MarkdownState.STRIKETHROUGH  }) {
                        Icon(Icons.Outlined.FormatStrikethrough, stringResource(R.string.markdown_strikethrough))
                    }
                    IconButton(onClick = { markdownState.value = MarkdownState.H1  }) {
                        Icon(painterResource(id = R.drawable.ic_h1), stringResource(R.string.markdown_heading1))
                    }
                    IconButton(onClick = { markdownState.value = MarkdownState.H2  }) {
                        Icon(painterResource(id = R.drawable.ic_h2), stringResource(R.string.markdown_heading2))
                    }
                    IconButton(onClick = { markdownState.value = MarkdownState.H3  }) {
                        Icon(painterResource(id = R.drawable.ic_h3), stringResource(R.string.markdown_heading3))
                    }
                    IconButton(onClick = { markdownState.value = MarkdownState.HR  }) {
                        Icon(Icons.Outlined.HorizontalRule, stringResource(R.string.markdown_horizontal_ruler))
                    }
                    IconButton(onClick = { markdownState.value = MarkdownState.UNORDEREDLIST  }) {
                        Icon(Icons.Outlined.List, stringResource(R.string.markdown_unordered_list))
                    }

                    //ATTENTION! Copy of FAB!!!
                    // WORKAROUND, otherwise the FAB completely disappears
                    /*
                    FloatingActionButton(
                        onClick = {
                            if (!isProActionAvailable)
                                Toast.makeText(
                                    context,
                                    context.getText(R.string.buypro_snackbar_remote_entries_blocked),
                                    Toast.LENGTH_LONG
                                ).show()
                            else if (!collection.readonly)
                                isEditMode.value = !isEditMode.value
                        },
                        containerColor = if (collection.readonly || !isProActionAvailable) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Crossfade(targetState = isEditMode.value) { isEditMode ->
                            if (isEditMode) {
                                Icon(painterResource(id = R.drawable.ic_save_move_outline), stringResource(id = R.string.save))
                            } else {
                                if (collection.readonly || !isProActionAvailable)
                                    Icon(Icons.Filled.EditOff, stringResource(id = R.string.readyonly))
                                else
                                    Icon(Icons.Filled.Edit, stringResource(id = R.string.edit))
                            }
                        }
                    }
                     */
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
                            isChecked = detailSettings.switchSetting[ENABLE_CATEGORIES] ?: true,
                            onCheckedChanged = { detailSettings.switchSetting[ENABLE_CATEGORIES] = it }
                        )
                    },
                    onClick = { detailSettings.switchSetting[ENABLE_CATEGORIES] = detailSettings.switchSetting[ENABLE_CATEGORIES]?.not() ?: true }
                )

                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.subtasks),
                            isChecked = detailSettings.switchSetting[ENABLE_SUBTASKS] ?: true,
                            onCheckedChanged = { detailSettings.switchSetting[ENABLE_SUBTASKS] = it }
                        )
                    },
                    onClick = { detailSettings.switchSetting[ENABLE_SUBTASKS] = detailSettings.switchSetting[ENABLE_SUBTASKS]?.not() ?: true }
                )

                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.view_feedback_linked_notes),
                            isChecked = detailSettings.switchSetting[ENABLE_SUBNOTES] ?: false,
                            onCheckedChanged = { detailSettings.switchSetting[ENABLE_SUBNOTES] = it }
                        )
                    },
                    onClick = { detailSettings.switchSetting[ENABLE_SUBNOTES] = detailSettings.switchSetting[ENABLE_SUBNOTES]?.not() ?: false }
                )

                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.resources),
                            isChecked = detailSettings.switchSetting[ENABLE_RESOURCES] ?: false,
                            onCheckedChanged = { detailSettings.switchSetting[ENABLE_RESOURCES] = it }
                        )
                    },
                    onClick = { detailSettings.switchSetting[ENABLE_RESOURCES] = detailSettings.switchSetting[ENABLE_RESOURCES]?.not() ?: false }
                )

                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.attendees),
                            isChecked = detailSettings.switchSetting[ENABLE_ATTENDEES] ?: false,
                            onCheckedChanged = { detailSettings.switchSetting[ENABLE_ATTENDEES]= it }
                        )
                    },
                    onClick = { detailSettings.switchSetting[ENABLE_ATTENDEES] = detailSettings.switchSetting[ENABLE_ATTENDEES]?.not() ?: false }
                )

                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.contact),
                            isChecked = detailSettings.switchSetting[ENABLE_CONTACT] ?: false,
                            onCheckedChanged = { detailSettings.switchSetting[ENABLE_CONTACT] = it }
                        )
                    },
                    onClick = { detailSettings.switchSetting[ENABLE_CONTACT] = detailSettings.switchSetting[ENABLE_CONTACT]?.not() ?: false }
                )

                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.url),
                            isChecked = detailSettings.switchSetting[ENABLE_URL] ?: false,
                            onCheckedChanged = { detailSettings.switchSetting[ENABLE_URL] = it }
                        )
                    },
                    onClick = { detailSettings.switchSetting[ENABLE_URL] = detailSettings.switchSetting[ENABLE_URL]?.not() ?: false }

                )

                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.location),
                            isChecked = detailSettings.switchSetting[ENABLE_LOCATION] ?: false,
                            onCheckedChanged = { detailSettings.switchSetting[ENABLE_LOCATION] = it }
                        )
                    },
                    onClick = { detailSettings.switchSetting[ENABLE_LOCATION] = detailSettings.switchSetting[ENABLE_LOCATION]?.not() ?: false }
                )

                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.comments),
                            isChecked = detailSettings.switchSetting[ENABLE_COMMENTS] ?: false,
                            onCheckedChanged = { detailSettings.switchSetting[ENABLE_COMMENTS] = it }
                        )
                    },
                    onClick = { detailSettings.switchSetting[ENABLE_COMMENTS] = detailSettings.switchSetting[ENABLE_COMMENTS]?.not() ?: false }
                )

                DropdownMenuItem(
                    text = {
                        LabelledCheckbox(
                            text = stringResource(id = R.string.attachments),
                            isChecked = detailSettings.switchSetting[ENABLE_ATTACHMENTS] ?: false,
                            onCheckedChanged = { detailSettings.switchSetting[ENABLE_ATTACHMENTS] = it }
                        )
                    },
                    onClick = { detailSettings.switchSetting[ENABLE_ATTACHMENTS] = detailSettings.switchSetting[ENABLE_ATTACHMENTS]?.not() ?: false }
                )

                if (icalObject.module == Module.TODO.name) {    //Never show the recurring tab for Journals and Notes, only for Todos
                    DropdownMenuItem(
                        text = {
                            LabelledCheckbox(
                                text = stringResource(id = R.string.alarms),
                                isChecked = detailSettings.switchSetting[ENABLE_ALARMS] ?: false,
                                onCheckedChanged = { detailSettings.switchSetting[ENABLE_ALARMS] = it }
                            )
                        },
                        onClick = { detailSettings.switchSetting[ENABLE_ALARMS] = detailSettings.switchSetting[ENABLE_ALARMS]?.not() ?: false }
                    )
                }

                if (icalObject.module != Module.NOTE.name) {   //Never show the recurring tab for Notes
                    DropdownMenuItem(
                        text = {
                            LabelledCheckbox(
                                text = stringResource(id = R.string.recurrence),
                                isChecked = detailSettings.switchSetting[ENABLE_RECURRENCE] ?: false,
                                onCheckedChanged = { detailSettings.switchSetting[ENABLE_RECURRENCE] = it }
                            )
                        },
                        onClick = { detailSettings.switchSetting[ENABLE_RECURRENCE] = detailSettings.switchSetting[ENABLE_RECURRENCE]?.not() ?: false }

                    )
                }
            }
        },
        floatingActionButton = {
            // TODO(b/228588827): Replace with Secondary FAB when available.
            FloatingActionButton(
                onClick = {
                    if (!isProActionAvailable)
                        Toast.makeText(
                            context,
                            context.getText(R.string.buypro_snackbar_remote_entries_blocked),
                            Toast.LENGTH_LONG
                        ).show()
                    else if (!collection.readonly)
                        isEditMode.value = !isEditMode.value
                },
                containerColor = if (collection.readonly || !isProActionAvailable) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer
            ) {
                Crossfade(targetState = isEditMode.value) { isEditMode ->
                    if (isEditMode) {
                        Icon(painterResource(id = R.drawable.ic_save_move_outline), stringResource(id = R.string.save))
                    } else {
                        if (collection.readonly || !isProActionAvailable)
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
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            isProActionAvailable = true,
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGEUNSAVED) },
            detailSettings = detailSettings,
            onDeleteClicked = { },
            onCopyRequested = { },
            onRevertClicked = { }
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
            isProActionAvailable = true,
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVING) },
            detailSettings = detailSettings,
            onDeleteClicked = { },
            onCopyRequested = { },
            onRevertClicked = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailBottomAppBar_Preview_edit_markdown() {
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
            isProActionAvailable = true,
            markdownState = remember { mutableStateOf(MarkdownState.OBSERVING) },
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVING) },
            detailSettings = detailSettings,
            onDeleteClicked = { },
            onCopyRequested = { },
            onRevertClicked = { }
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
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            isProActionAvailable = true,
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVED) },
            detailSettings = detailSettings,
            onDeleteClicked = { },
            onCopyRequested = { },
            onRevertClicked = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailBottomAppBar_Preview_View_proOnly() {
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
            icalObject = ICalObject.createNote().apply { dirty = false },
            collection = collection,
            isEditMode = remember { mutableStateOf(false) },
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            isProActionAvailable = false,
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVED) },
            detailSettings = detailSettings,
            onDeleteClicked = { },
            onCopyRequested = { },
            onRevertClicked = { }
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
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            isProActionAvailable = true,
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVING) },
            detailSettings = detailSettings,
            onDeleteClicked = { },
            onCopyRequested = { },
            onRevertClicked = { }
        )
    }
}
