/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.ui.reusable.appbars.OverflowMenu
import at.techbee.jtx.ui.reusable.destinations.DetailDestination
import at.techbee.jtx.ui.reusable.dialogs.DeleteEntryDialog
import at.techbee.jtx.ui.reusable.dialogs.ErrorOnUpdateDialog
import at.techbee.jtx.ui.reusable.dialogs.RevertChangesDialog
import at.techbee.jtx.ui.reusable.elements.CheckboxWithText
import at.techbee.jtx.ui.settings.SettingsStateHolder


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    navController: NavHostController,
    detailViewModel: DetailViewModel,
    editImmediately: Boolean = false,
    icalObjectIdList: List<Long>,
    onLastUsedCollectionChanged: (Module, Long) -> Unit,
    onRequestReview: () -> Unit,
) {
    //val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val settingsStateHolder = SettingsStateHolder(context)

    val isEditMode = rememberSaveable { mutableStateOf(editImmediately) }
    val goBackRequestedByTopBar = remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRevertDialog by remember { mutableStateOf(false) }
    var navigateUp by remember { mutableStateOf(false) }
    val markdownState = remember { mutableStateOf(MarkdownState.DISABLED) }


    val icalEntity = detailViewModel.icalEntity.observeAsState()
    val subtasks = detailViewModel.relatedSubtasks.observeAsState(emptyList())
    val subnotes = detailViewModel.relatedSubnotes.observeAsState(emptyList())
    val isChild = detailViewModel.isChild.observeAsState(false)
    val allCategories = detailViewModel.allCategories.observeAsState(emptyList())
    val allResources = detailViewModel.allResources.observeAsState(emptyList())
    val allWriteableCollections = detailViewModel.allWriteableCollections.observeAsState(emptyList())

    if (navigateUp && detailViewModel.changeState.value != DetailViewModel.DetailChangeState.CHANGESAVING) {
        onRequestReview()
        navController.navigateUp()
    }

    if (detailViewModel.entryDeleted.value) {
        Toast.makeText(
            context,
            context.getString(R.string.details_toast_entry_deleted),
            Toast.LENGTH_SHORT
        ).show()
        Attachment.scheduleCleanupJob(context)
        onRequestReview()
        detailViewModel.entryDeleted.value = false
        navigateUp = true
    }

    detailViewModel.toastMessage.value?.let {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        detailViewModel.toastMessage.value = null
    }

    detailViewModel.navigateToId.value?.let {
        detailViewModel.navigateToId.value = null
        navController.navigate(DetailDestination.Detail.getRoute(it, icalObjectIdList, true))
    }

    if (showDeleteDialog) {
        DeleteEntryDialog(
            icalObject = detailViewModel.icalEntity.value?.property!!,
            onConfirm = { detailViewModel.delete() },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showRevertDialog) {
        RevertChangesDialog(
            onConfirm = { detailViewModel.revert() },
            onDismiss = { showRevertDialog = false }
        )
    }

    if (detailViewModel.sqlConstraintException.value) {
        ErrorOnUpdateDialog(onConfirm = { navigateUp = true })
    }


    Scaffold(
        topBar = {
            DetailsTopAppBar(
                title = stringResource(id = R.string.details),
                goBack = {
                    goBackRequestedByTopBar.value = true
                },     // goBackRequestedByTopBar is handled in DetailScreenContent.kt
                actions = {
                    if (!isEditMode.value) {
                        val menuExpanded = remember { mutableStateOf(false) }
                        OverflowMenu(menuExpanded = menuExpanded) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.menu_view_share_mail)) },
                                onClick = {
                                    detailViewModel.shareAsText(context)
                                    menuExpanded.value = false
                                },
                                leadingIcon = { Icon(Icons.Outlined.Mail, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.menu_view_share_ics)) },
                                onClick = {
                                    detailViewModel.shareAsICS(context)
                                    menuExpanded.value = false
                                },
                                leadingIcon = { Icon(Icons.Outlined.Description, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.menu_view_copy_to_clipboard)) },
                                onClick = {
                                    val text = icalEntity.value?.getShareText(context) ?: ""
                                    val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboardManager.setPrimaryClip(ClipData.newPlainText("", text))
                                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)            // Only show a toast for Android 12 and lower.
                                        Toast.makeText(context, context.getText(R.string.menu_view_copy_to_clipboard_copied), Toast.LENGTH_SHORT).show()
                                    menuExpanded.value = false
                                },
                                leadingIcon = { Icon(Icons.Outlined.ContentPaste, null) }
                            )

                            Divider()

                            CheckboxWithText(
                                text = stringResource(id = R.string.menu_view_markdown_formatting),
                                onCheckedChange = {
                                    detailViewModel.detailSettings.switchSetting[DetailSettings.ENABLE_MARKDOWN] = it
                                    detailViewModel.detailSettings.save()
                                },
                                isSelected = detailViewModel.detailSettings.switchSetting[DetailSettings.ENABLE_MARKDOWN] ?: true,
                            )
                        }
                    } else {
                        val menuExpanded = remember { mutableStateOf(false) }
                        OverflowMenu(menuExpanded = menuExpanded) {
                            CheckboxWithText(
                                text = stringResource(id = R.string.menu_view_autosave),
                                onCheckedChange = {
                                    detailViewModel.detailSettings.switchSetting[DetailSettings.ENABLE_AUTOSAVE] = it
                                    detailViewModel.detailSettings.save()
                                },
                                isSelected = detailViewModel.detailSettings.switchSetting[DetailSettings.ENABLE_AUTOSAVE] ?: true,
                            )
                        }
                    }
                }
            )
        },
        content = { paddingValues ->

            DetailScreenContent(
                iCalEntity = icalEntity,
                isEditMode = isEditMode,
                changeState = detailViewModel.changeState,
                subtasks = subtasks,
                subnotes = subnotes,
                isChild = isChild.value,
                allWriteableCollections = allWriteableCollections.value,
                allCategories = allCategories.value,
                allResources = allResources.value,
                detailSettings = detailViewModel.detailSettings,
                icalObjectIdList = icalObjectIdList,
                sliderIncrement = settingsStateHolder.settingStepForProgress.value.getProgressStepKeyAsInt(),
                goBackRequested = goBackRequestedByTopBar,
                markdownState = markdownState,
                saveICalObject = { changedICalObject, changedCategories, changedComments, changedAttendees, changedResources, changedAttachments, changedAlarms ->
                    if (changedICalObject.isRecurLinkedInstance)
                        changedICalObject.isRecurLinkedInstance = false

                    detailViewModel.save(
                        changedICalObject,
                        changedCategories,
                        changedComments,
                        changedAttendees,
                        changedResources,
                        changedAttachments,
                        changedAlarms
                    )
                    onLastUsedCollectionChanged(detailViewModel.icalEntity.value?.property?.getModuleFromString() ?: Module.NOTE, changedICalObject.collectionId)
                },
                deleteICalObject = { showDeleteDialog = true },
                onProgressChanged = { itemId, newPercent, _ ->
                    detailViewModel.updateProgress(itemId, newPercent)
                },
                onMoveToNewCollection = { icalObject, newCollection ->
                    navController.popBackStack()
                    detailViewModel.moveToNewCollection(icalObject, newCollection.collectionId)
                },
                onSubEntryAdded = { icalObject, attachment ->
                    detailViewModel.addSubEntry(
                        icalObject,
                        attachment
                    )
                },
                onSubEntryDeleted = { icalObjectId -> detailViewModel.deleteById(icalObjectId) },
                onSubEntryUpdated = { icalObjectId, newText ->
                    detailViewModel.updateSummary(
                        icalObjectId,
                        newText
                    )
                },
                player = detailViewModel.mediaPlayer,
                goToDetail = { itemId, editMode, list -> navController.navigate(DetailDestination.Detail.getRoute(itemId, list, editMode)) },
                goBack = { navigateUp = true },
                modifier = Modifier.padding(paddingValues)
            )
        },
        bottomBar = {
            DetailBottomAppBar(
                icalObject = icalEntity.value?.property,
                collection = icalEntity.value?.ICalCollection,
                isEditMode = isEditMode,
                markdownState = markdownState,
                changeState = detailViewModel.changeState,
                detailSettings = detailViewModel.detailSettings,
                onDeleteClicked = { showDeleteDialog = true },
                onCopyRequested = { newModule -> detailViewModel.createCopy(newModule) },
                onRevertClicked = { showRevertDialog = true }
            )
        }
    )
}
