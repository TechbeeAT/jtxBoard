/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.content.ContentResolver
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.TextFormat
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.util.SyncApp
import at.techbee.jtx.util.SyncUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailBottomAppBar(
    icalObject: ICalObject?,
    seriesElement: ICalObject?,
    collection: ICalCollection?,
    isEditMode: MutableState<Boolean>,
    markdownState: MutableState<MarkdownState>,
    isProActionAvailable: Boolean,
    changeState: MutableState<DetailViewModel.DetailChangeState>,
    detailsBottomSheetState: SheetState,
    onDeleteClicked: () -> Unit,
    onCopyRequested: (Module) -> Unit,
    onRevertClicked: () -> Unit,
) {

    if (icalObject == null || collection == null)
        return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var copyOptionsExpanded by remember { mutableStateOf(false) }

    val syncIconAnimation = rememberInfiniteTransition(label = "syncIconAnimation")
    val angle by syncIconAnimation.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
            }
        ), label = "syncIconAnimationAngle"
    )

    val isPreview = LocalInspectionMode.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isSyncInProgress by remember { mutableStateOf(false) }
    DisposableEffect(lifecycleOwner) {

        val listener = if (isPreview)
            null
        else {
            ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE) {
                isSyncInProgress = SyncUtil.isJtxSyncRunningFor(setOf(collection.getAccount()))
            }
        }
        onDispose {
            if (!isPreview)
                ContentResolver.removeStatusChangeListener(listener)
        }
    }


    BottomAppBar(
        actions = {
            AnimatedVisibility(markdownState.value == MarkdownState.CLOSED) {
                IconButton(onClick = { markdownState.value = MarkdownState.OBSERVING }) {
                    Icon(
                        Icons.Outlined.TextFormat,
                        contentDescription = stringResource(id = R.string.menu_view_markdown_formatting)
                    )
                }
            }

            AnimatedVisibility(
                isEditMode.value
                        && (markdownState.value == MarkdownState.DISABLED || markdownState.value == MarkdownState.CLOSED)
            ) {
                IconButton(onClick = {
                    scope.launch {
                        if (detailsBottomSheetState.isVisible)
                            detailsBottomSheetState.hide()
                        else
                            detailsBottomSheetState.show()
                    }
                }
                ) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = stringResource(id = R.string.preferences)
                    )
                }
            }

            AnimatedVisibility(
                !isEditMode.value
                        && !collection.readonly
                        && isProActionAvailable
            ) {
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

            if(
                !collection.readonly
                && isProActionAvailable
                && (markdownState.value == MarkdownState.DISABLED || markdownState.value == MarkdownState.CLOSED)
            ) {
                IconButton(onClick = { onDeleteClicked() }) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = stringResource(id = R.string.delete),
                    )
                }
            }

            AnimatedVisibility(
                isEditMode.value
                        && changeState.value != DetailViewModel.DetailChangeState.UNCHANGED
                        && (markdownState.value == MarkdownState.DISABLED || markdownState.value == MarkdownState.CLOSED)
            ) {
                IconButton(onClick = { onRevertClicked() }) {
                    Icon(
                        painterResource(id = R.drawable.ic_revert),
                        contentDescription = stringResource(id = R.string.revert)
                    )
                }
            }

            AnimatedVisibility(
                collection.accountType != LOCAL_ACCOUNT_TYPE
                    && (isSyncInProgress || seriesElement?.dirty ?: icalObject.dirty)
                    && (markdownState.value == MarkdownState.DISABLED || markdownState.value == MarkdownState.CLOSED)
            ) {
                IconButton(
                    onClick = {
                        if (!isSyncInProgress) {
                            collection.getAccount().let { SyncUtil.syncAccounts(setOf(it)) }
                            SyncUtil.showSyncRequestedToast(context)
                        }
                    },
                    enabled = seriesElement?.dirty ?: icalObject.dirty && !isSyncInProgress
                ) {
                    Crossfade(isSyncInProgress, label = "isSyncInProgress") { synchronizing ->
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
                    || changeState.value == DetailViewModel.DetailChangeState.CHANGESAVED)
                    && (markdownState.value == MarkdownState.DISABLED || markdownState.value == MarkdownState.CLOSED)) {
                IconButton(
                    onClick = { },
                    enabled = false
                ) {
                    Crossfade(changeState.value, label = "saving_change_state") { state ->
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
                                IconButton(onClick = { /* no action, icon button just to keep the same style */  }) {
                                    CircularProgressIndicator(modifier = Modifier
                                        .alpha(0.3f)
                                        .size(24.dp))
                                }
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
            AnimatedVisibility(isEditMode.value && markdownState.value != MarkdownState.DISABLED && markdownState.value != MarkdownState.CLOSED) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { markdownState.value = MarkdownState.CLOSED }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
                    }
                    VerticalDivider(modifier = Modifier.height(40.dp))
                }
            }
            AnimatedVisibility(isEditMode.value && markdownState.value != MarkdownState.DISABLED && markdownState.value != MarkdownState.CLOSED) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { markdownState.value = MarkdownState.BOLD }) {
                        Icon(Icons.Outlined.FormatBold, stringResource(R.string.markdown_bold))
                    }
                    IconButton(onClick = { markdownState.value = MarkdownState.ITALIC  }) {
                        Icon(Icons.Outlined.FormatItalic, stringResource(R.string.markdown_italic))
                    }
                    /*
                    IconButton(onClick = { markdownState.value = MarkdownState.UNDERLINED  }) {
                        Icon(Icons.Outlined.FormatUnderlined, stringResource(R.string.markdown_underlined))
                    }
                     */
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
                        Icon(Icons.AutoMirrored.Outlined.List, stringResource(R.string.markdown_unordered_list))
                    }
                    IconButton(onClick = { markdownState.value = MarkdownState.CODE  }) {
                        Icon(Icons.Outlined.Code, stringResource(R.string.markdown_code))
                    }
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
                Crossfade(targetState = isEditMode.value, label = "fab_icon_content") { isEditMode ->
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


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DetailBottomAppBar_Preview_View() {
    MaterialTheme {

        val collection = ICalCollection().apply {
            this.readonly = false
            this.accountType = SyncApp.DAVX5.accountType
        }

        DetailBottomAppBar(
            icalObject = ICalObject.createNote().apply { dirty = true },
            seriesElement = null,
            collection = collection,
            isEditMode = remember { mutableStateOf(false) },
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            isProActionAvailable = true,
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGEUNSAVED) },
            detailsBottomSheetState = rememberModalBottomSheetState(),
            onDeleteClicked = { },
            onCopyRequested = { },
            onRevertClicked = { }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DetailBottomAppBar_Preview_edit() {
    MaterialTheme {

        val collection = ICalCollection().apply {
            this.readonly = false
            this.accountType = SyncApp.DAVX5.accountType
        }

        DetailBottomAppBar(
            icalObject = ICalObject.createNote().apply { dirty = true },
            seriesElement = null,
            collection = collection,
            isEditMode = remember { mutableStateOf(true) },
            isProActionAvailable = true,
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVING) },
            detailsBottomSheetState = rememberModalBottomSheetState(),
            onDeleteClicked = { },
            onCopyRequested = { },
            onRevertClicked = { }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DetailBottomAppBar_Preview_edit_markdown() {
    MaterialTheme {

        val collection = ICalCollection().apply {
            this.readonly = false
            this.accountType = SyncApp.DAVX5.accountType
        }

        DetailBottomAppBar(
            icalObject = ICalObject.createNote().apply { dirty = true },
            seriesElement = null,
            collection = collection,
            isEditMode = remember { mutableStateOf(true) },
            isProActionAvailable = true,
            markdownState = remember { mutableStateOf(MarkdownState.OBSERVING) },
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVING) },
            detailsBottomSheetState = rememberModalBottomSheetState(),
            onDeleteClicked = { },
            onCopyRequested = { },
            onRevertClicked = { }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DetailBottomAppBar_Preview_View_readonly() {
    MaterialTheme {

        val collection = ICalCollection().apply {
            this.readonly = true
            this.accountType = SyncApp.DAVX5.accountType
        }

        DetailBottomAppBar(
            icalObject = ICalObject.createNote().apply { dirty = false },
            seriesElement = null,
            collection = collection,
            isEditMode = remember { mutableStateOf(false) },
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            isProActionAvailable = true,
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVED) },
            detailsBottomSheetState = rememberModalBottomSheetState(),
            onDeleteClicked = { },
            onCopyRequested = { },
            onRevertClicked = { }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DetailBottomAppBar_Preview_View_proOnly() {
    MaterialTheme {

        val collection = ICalCollection().apply {
            this.readonly = false
            this.accountType = SyncApp.DAVX5.accountType
        }

        DetailBottomAppBar(
            icalObject = ICalObject.createNote().apply { dirty = false },
            seriesElement = null,
            collection = collection,
            isEditMode = remember { mutableStateOf(false) },
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            isProActionAvailable = false,
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVED) },
            detailsBottomSheetState = rememberModalBottomSheetState(),
            onDeleteClicked = { },
            onCopyRequested = { },
            onRevertClicked = { }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DetailBottomAppBar_Preview_View_local() {
    MaterialTheme {

        val collection = ICalCollection().apply {
            this.readonly = false
            this.accountType = LOCAL_ACCOUNT_TYPE
        }

        BillingManager.getInstance().initialise(LocalContext.current.applicationContext)

        DetailBottomAppBar(
            icalObject = ICalObject.createNote().apply { dirty = true },
            seriesElement = null,
            collection = collection,
            isEditMode = remember { mutableStateOf(false) },
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            isProActionAvailable = true,
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVING) },
            detailsBottomSheetState = rememberModalBottomSheetState(),
            onDeleteClicked = { },
            onCopyRequested = { },
            onRevertClicked = { }
        )
    }
}
