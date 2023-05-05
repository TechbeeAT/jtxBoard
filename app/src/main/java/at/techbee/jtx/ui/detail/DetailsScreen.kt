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
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.ContextWrapper
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.reusable.appbars.OverflowMenu
import at.techbee.jtx.ui.reusable.destinations.DetailDestination
import at.techbee.jtx.ui.reusable.destinations.FilteredListDestination
import at.techbee.jtx.ui.reusable.destinations.NavigationDrawerDestination
import at.techbee.jtx.ui.reusable.dialogs.DeleteEntryDialog
import at.techbee.jtx.ui.reusable.dialogs.ErrorOnUpdateDialog
import at.techbee.jtx.ui.reusable.dialogs.RevertChangesDialog
import at.techbee.jtx.ui.reusable.dialogs.UnsavedChangesDialog
import at.techbee.jtx.ui.reusable.elements.CheckboxWithText
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    navController: NavHostController,
    detailViewModel: DetailViewModel,
    editImmediately: Boolean = false,
    returnToLauncher: Boolean = false,
    icalObjectIdList: List<Long>,
    onLastUsedCollectionChanged: (Module, Long) -> Unit,
    onRequestReview: () -> Unit,
) {
    val context = LocalContext.current
    fun Context.getActivity(): AppCompatActivity? = when (this) {
        is AppCompatActivity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> null
    }

    val detailsBottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val isEditMode = rememberSaveable { mutableStateOf(editImmediately) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRevertDialog by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by rememberSaveable { mutableStateOf(false) }
    var navigateUp by remember { mutableStateOf(false) }
    val markdownState = remember { mutableStateOf(MarkdownState.DISABLED) }

    val icalEntity = detailViewModel.icalEntity.observeAsState()
    val parents = detailViewModel.relatedParents.observeAsState(emptyList())
    val subtasks = detailViewModel.relatedSubtasks.observeAsState(emptyList())
    val subnotes = detailViewModel.relatedSubnotes.observeAsState(emptyList())
    val seriesElement = detailViewModel.seriesElement.observeAsState(null)
    val seriesInstances = detailViewModel.seriesInstances.observeAsState(emptyList())
    val isChild = detailViewModel.isChild.observeAsState(false)
    val allCategories = detailViewModel.allCategories.observeAsState(emptyList())
    val allResources = detailViewModel.allResources.observeAsState(emptyList())
    val storedCategories by detailViewModel.storedCategories.observeAsState(emptyList())
    val storedResources by detailViewModel.storedResources.observeAsState(emptyList())
    val storedStatuses by detailViewModel.storedStatuses.observeAsState(emptyList())
    val allWriteableCollections = detailViewModel.allWriteableCollections.observeAsState(emptyList())

    val isProPurchased = BillingManager.getInstance().isProPurchased.observeAsState(true)
    val isProActionAvailable by remember(isProPurchased, icalEntity) { derivedStateOf { isProPurchased.value || icalEntity.value?.ICalCollection?.accountType == ICalCollection.LOCAL_ACCOUNT_TYPE } }

    icalEntity.value?.property?.getModuleFromString()?.let {
        detailViewModel.detailSettings.load(it, context)
    }

    // load objects into states for editing
    var statesLoaded by remember { mutableStateOf(false) }  //don't load them again once done
    LaunchedEffect(detailViewModel.icalEntity.isInitialized) {
        if(detailViewModel.icalEntity.isInitialized && !statesLoaded) {
            detailViewModel.mutableICalObject = icalEntity.value?.property
            detailViewModel.mutableCategories.addAll(icalEntity.value?.categories ?: emptyList())
            detailViewModel.mutableResources.addAll(icalEntity.value?.resources ?: emptyList())
            detailViewModel.mutableAttendees.addAll(icalEntity.value?.attendees ?: emptyList())
            detailViewModel.mutableComments.addAll(icalEntity.value?.comments ?: emptyList())
            detailViewModel.mutableAttachments.addAll(icalEntity.value?.attachments ?: emptyList())
            detailViewModel.mutableAlarms.addAll(icalEntity.value?.alarms ?: emptyList())
            statesLoaded = true
        }
    }


    BackHandler {
        navigateUp = true
    }

    if (navigateUp) {
        if (returnToLauncher) {
            context.getActivity()?.finish()
            navigateUp = false
        }

        when(detailViewModel.changeState.value) {
            DetailViewModel.DetailChangeState.UNCHANGED -> {
                if (isEditMode.value
                    && detailViewModel.changeState.value == DetailViewModel.DetailChangeState.UNCHANGED
                    && icalEntity.value?.property?.sequence == 0L
                    && icalEntity.value?.property?.summary == null
                    && icalEntity.value?.property?.description == null
                    && icalEntity.value?.attachments?.isEmpty() == true
                ) {
                    showDeleteDialog = true
                } else if(!detailViewModel.mutableICalObject?.rrule.isNullOrEmpty())  {
                    navController.popBackStack(NavigationDrawerDestination.BOARD.name, false)
                    navigateUp = false
                } else {
                    navController.navigateUp()
                    navigateUp = false
                }
            }
            DetailViewModel.DetailChangeState.CHANGEUNSAVED -> { showUnsavedChangesDialog = true }
            DetailViewModel.DetailChangeState.LOADING -> { /* do nothing */ }
            DetailViewModel.DetailChangeState.SAVINGREQUESTED -> { /* do nothing, wait until saved */ }
            DetailViewModel.DetailChangeState.CHANGESAVING -> { /* do nothing, wait until saved */ }
            DetailViewModel.DetailChangeState.DELETING -> { /* do nothing, wait until deleted */ }
            DetailViewModel.DetailChangeState.DELETED -> { navController.navigateUp() }
            DetailViewModel.DetailChangeState.SQLERROR -> { navController.navigateUp() }
            DetailViewModel.DetailChangeState.CHANGESAVED -> {
                showUnsavedChangesDialog = false
                if(isEditMode.value)
                    isEditMode.value = false
                else
                    navController.navigateUp()
                onRequestReview()
                navigateUp = false
            }
        }
    }

    if (detailViewModel.changeState.value == DetailViewModel.DetailChangeState.DELETED) {
        Attachment.scheduleCleanupJob(context)
        onRequestReview()
        navigateUp = true
    }

    if(detailViewModel.changeState.value == DetailViewModel.DetailChangeState.SQLERROR) {
        ErrorOnUpdateDialog(onConfirm = { navigateUp = true })
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
        icalEntity.value?.property?.let {
            DeleteEntryDialog(
                icalObject = it,
                onConfirm = {
                    showDeleteDialog = false
                    detailViewModel.delete()
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }

    if (showRevertDialog) {
        RevertChangesDialog(
            onConfirm = { detailViewModel.revert() },
            onDismiss = { showRevertDialog = false }
        )
    }

    if(showUnsavedChangesDialog) {
        UnsavedChangesDialog(
            onSave = {
                showUnsavedChangesDialog = false
                detailViewModel.changeState.value = DetailViewModel.DetailChangeState.SAVINGREQUESTED
            },
            onDiscard = {
                showUnsavedChangesDialog = false
                detailViewModel.changeState.value = DetailViewModel.DetailChangeState.UNCHANGED
            }
        )
    }

    if(detailsBottomSheetState.currentValue != SheetValue.Hidden) {
        ModalBottomSheet(
            sheetState = detailsBottomSheetState,
            onDismissRequest = {
                scope.launch { detailsBottomSheetState.hide() }
            }) {
            DetailOptionsBottomSheet(
                module = try {
                    Module.valueOf(icalEntity.value?.property?.module ?: Module.NOTE.name)
                } catch (e: Exception) {
                    Module.NOTE
                },
                detailSettings = detailViewModel.detailSettings,
                onListSettingsChanged = { detailViewModel.detailSettings.save() },
                modifier = Modifier.padding(top = 0.dp, start = 8.dp, end = 8.dp, bottom = 32.dp)
            )
        }
    }

    Scaffold(
        topBar = {
            DetailsTopAppBar(
                readonly = icalEntity.value?.ICalCollection?.readonly ?: true,
                goBack = {
                    navigateUp = true
                },     // goBackRequestedByTopBar is handled in DetailScreenContent.kt
                detailTopAppBarMode = detailViewModel.settingsStateHolder.detailTopAppBarMode.value,
                onAddSubnote = { subnoteText -> detailViewModel.addSubEntry(ICalObject.createNote(subnoteText), null) },
                onAddSubtask = { subtaskText -> detailViewModel.addSubEntry(ICalObject.createTask(subtaskText), null) },
                actions = {
                    val menuExpanded = remember { mutableStateOf(false) }

                    OverflowMenu(menuExpanded = menuExpanded) {

                        Text(stringResource(R.string.details_app_bar_behaviour), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp))
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.edit_subtasks_add_helper),
                                    color = if (detailViewModel.settingsStateHolder.detailTopAppBarMode.value == DetailTopAppBarMode.ADD_SUBTASK) MaterialTheme.colorScheme.primary else Color.Unspecified
                                )
                            },
                            onClick = {
                                detailViewModel.settingsStateHolder.detailTopAppBarMode.value = DetailTopAppBarMode.ADD_SUBTASK
                                menuExpanded.value = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.AddTask,
                                    contentDescription = null,
                                    tint = if (detailViewModel.settingsStateHolder.detailTopAppBarMode.value == DetailTopAppBarMode.ADD_SUBTASK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.edit_subnote_add_helper),
                                    color = if (detailViewModel.settingsStateHolder.detailTopAppBarMode.value == DetailTopAppBarMode.ADD_SUBNOTE) MaterialTheme.colorScheme.primary else Color.Unspecified
                                )
                            },
                            onClick = {
                                detailViewModel.settingsStateHolder.detailTopAppBarMode.value = DetailTopAppBarMode.ADD_SUBNOTE
                                menuExpanded.value = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.NoteAdd,
                                    contentDescription = null,
                                    tint = if (detailViewModel.settingsStateHolder.detailTopAppBarMode.value == DetailTopAppBarMode.ADD_SUBNOTE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )

                        Divider()

                        if (!isEditMode.value) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.menu_view_share_mail)) },
                                onClick = {
                                    detailViewModel.shareAsText(context)
                                    menuExpanded.value = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Mail,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.menu_view_share_ics)) },
                                onClick = {
                                    detailViewModel.shareAsICS(context)
                                    menuExpanded.value = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
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
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentPaste,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            )
                        }


                        if (isEditMode.value) {
                            CheckboxWithText(
                                text = stringResource(id = R.string.menu_view_autosave),
                                onCheckedChange = {
                                    detailViewModel.detailSettings.detailSetting[DetailSettingsOption.ENABLE_AUTOSAVE] = it
                                    detailViewModel.detailSettings.save()
                                },
                                isSelected = detailViewModel.detailSettings.detailSetting[DetailSettingsOption.ENABLE_AUTOSAVE] ?: true,
                            )
                        }

                        Divider()

                        CheckboxWithText(
                            text = stringResource(id = R.string.menu_view_markdown_formatting),
                            onCheckedChange = {
                                detailViewModel.detailSettings.detailSetting[DetailSettingsOption.ENABLE_MARKDOWN] = it
                                detailViewModel.detailSettings.save()
                            },
                            isSelected = detailViewModel.detailSettings.detailSetting[DetailSettingsOption.ENABLE_MARKDOWN] ?: true,
                        )

                        Divider()

                        if(icalEntity.value?.ICalCollection?.readonly == false && icalEntity.value?.ICalCollection?.supportsVJOURNAL == true) {
                            if (icalEntity.value?.property?.module != Module.JOURNAL.name) {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(id = R.string.menu_view_convert_to_journal)) },
                                    onClick = { detailViewModel.convertTo(Module.JOURNAL) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.EventNote,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                )
                            }
                            if (icalEntity.value?.property?.module != Module.NOTE.name) {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(id = R.string.menu_view_convert_to_note)) },
                                    onClick = { detailViewModel.convertTo(Module.NOTE) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Note,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                )
                            }
                        }
                        if(icalEntity.value?.ICalCollection?.readonly == false && icalEntity.value?.ICalCollection?.supportsVTODO == true) {
                            if(icalEntity.value?.property?.module != Module.TODO.name) {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(id = R.string.menu_view_convert_to_task)) },
                                    onClick = { detailViewModel.convertTo(Module.TODO) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.TaskAlt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        content = { paddingValues ->

            DetailScreenContent(
                originalICalEntity = icalEntity,
                iCalObject = detailViewModel.mutableICalObject,
                categories = detailViewModel.mutableCategories,
                resources = detailViewModel.mutableResources,
                attendees = detailViewModel.mutableAttendees,
                comments = detailViewModel.mutableComments,
                attachments = detailViewModel.mutableAttachments,
                alarms = detailViewModel.mutableAlarms,
                isEditMode = isEditMode,
                changeState = detailViewModel.changeState,
                parents = parents,
                subtasks = subtasks,
                subnotes = subnotes,
                isChild = isChild.value,
                allWriteableCollections = allWriteableCollections.value,
                allCategories = allCategories.value,
                allResources = allResources.value,
                storedCategories = storedCategories,
                storedResources = storedResources,
                extendedStatuses = storedStatuses,
                selectFromAllListLive = detailViewModel.selectFromAllList,
                detailSettings = detailViewModel.detailSettings,
                icalObjectIdList = icalObjectIdList,
                seriesInstances = seriesInstances.value,
                seriesElement = seriesElement.value,
                sliderIncrement = detailViewModel.settingsStateHolder.settingStepForProgress.value.getProgressStepKeyAsInt(),
                showProgressForMainTasks = detailViewModel.settingsStateHolder.settingShowProgressForMainTasks.value,
                showProgressForSubTasks = detailViewModel.settingsStateHolder.settingShowProgressForSubTasks.value,
                keepStatusProgressCompletedInSync = detailViewModel.settingsStateHolder.settingKeepStatusProgressCompletedInSync.value,
                linkProgressToSubtasks = detailViewModel.settingsStateHolder.settingLinkProgressToSubtasks.value,
                setCurrentLocation = if(isEditMode.value
                        && detailViewModel.changeState.value == DetailViewModel.DetailChangeState.UNCHANGED
                        && icalEntity.value?.property?.sequence == 0L
                        && icalEntity.value?.property?.summary == null
                        && icalEntity.value?.property?.description == null
                        && icalEntity.value?.attachments?.isEmpty() == true) {
                            when(icalEntity.value?.property?.getModuleFromString()) {
                                Module.JOURNAL -> detailViewModel.settingsStateHolder.settingSetDefaultCurrentLocationJournals.value
                                Module.NOTE -> detailViewModel.settingsStateHolder.settingSetDefaultCurrentLocationNotes.value
                                Module.TODO -> detailViewModel.settingsStateHolder.settingSetDefaultCurrentLocationTasks.value
                                else -> false
                            }
                    } else false,
                markdownState = markdownState,
                saveEntry = {
                    detailViewModel.saveEntry()
                    onLastUsedCollectionChanged(detailViewModel.mutableICalObject!!.getModuleFromString(), detailViewModel.mutableICalObject!!.collectionId)
                },
                onProgressChanged = { itemId, newPercent -> detailViewModel.updateProgress(itemId, newPercent) },
                onMoveToNewCollection = { newCollection -> detailViewModel.moveToNewCollection(newCollection.collectionId) },
                onSubEntryAdded = { icalObject, attachment -> detailViewModel.addSubEntry(icalObject, attachment) },
                onSubEntryDeleted = { icalObjectId -> detailViewModel.deleteById(icalObjectId) },
                onSubEntryUpdated = { icalObjectId, newText -> detailViewModel.updateSummary(icalObjectId, newText) },
                onUnlinkSubEntry = { icalObjectId -> detailViewModel.unlinkFromParent(icalObjectId) },
                onLinkSubEntries = { newSubEntries -> detailViewModel.linkNewSubentries(newSubEntries) },
                onAllEntriesSearchTextUpdated = { searchText -> detailViewModel.updateSelectFromAllListQuery(searchText) },
                player = detailViewModel.mediaPlayer,
                goToDetail = { itemId, editMode, list -> navController.navigate(DetailDestination.Detail.getRoute(itemId, list, editMode)) },
                goBack = { navigateUp = true },
                goToFilteredList = {
                    navController.navigate(
                        FilteredListDestination.FilteredList.getRoute(
                            module = icalEntity.value?.property?.getModuleFromString() ?: Module.NOTE,
                            storedListSettingData = it
                        )
                    )
                },
                unlinkFromSeries = { instances, series, deleteAfterUnlink -> detailViewModel.unlinkFromSeries(instances, series, deleteAfterUnlink) },
                modifier = Modifier.padding(paddingValues)
            )
        },
        bottomBar = {
            DetailBottomAppBar(
                icalObject = icalEntity.value?.property,
                seriesElement = seriesElement.value,
                collection = icalEntity.value?.ICalCollection,
                isEditMode = isEditMode,
                markdownState = markdownState,
                isProActionAvailable = isProActionAvailable,
                changeState = detailViewModel.changeState,
                detailsBottomSheetState = detailsBottomSheetState,
                onDeleteClicked = { showDeleteDialog = true },
                onCopyRequested = { newModule -> detailViewModel.createCopy(newModule) },
                onRevertClicked = { showRevertDialog = true }
            )
        }
    )
}
