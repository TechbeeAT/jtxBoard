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
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.list.OrderBy
import at.techbee.jtx.ui.reusable.appbars.OverflowMenu
import at.techbee.jtx.ui.reusable.destinations.DetailDestination
import at.techbee.jtx.ui.reusable.destinations.FilteredListDestination
import at.techbee.jtx.ui.reusable.destinations.NavigationDrawerDestination
import at.techbee.jtx.ui.reusable.dialogs.DeleteEntryDialog
import at.techbee.jtx.ui.reusable.dialogs.ErrorOnUpdateDialog
import at.techbee.jtx.ui.reusable.dialogs.LinkExistingEntryDialog
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
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showRevertDialog by rememberSaveable { mutableStateOf(false) }
    var showUnsavedChangesDialog by rememberSaveable { mutableStateOf(false) }
    var showLinkEntryDialog by rememberSaveable { mutableStateOf(false) }
    var linkEntryDialogModule by rememberSaveable { mutableStateOf(listOf<Module>())}
    var linkEntryDialogReltype by rememberSaveable { mutableStateOf<Reltype?>(null)}
    var navigateUp by remember { mutableStateOf(false) }
    val markdownState = remember { mutableStateOf(MarkdownState.DISABLED) }
    val scrollToSection = remember { mutableStateOf<DetailsScreenSection?>(null) }

    val icalEntity = detailViewModel.icalEntity.observeAsState(null)

    val seriesElement = detailViewModel.seriesElement.observeAsState(null)
    val storedCategories by detailViewModel.storedCategories.observeAsState(emptyList())
    val storedResources by detailViewModel.storedResources.observeAsState(emptyList())
    val storedStatuses by detailViewModel.storedStatuses.observeAsState(emptyList())

    val isProPurchased = BillingManager.getInstance().isProPurchased.observeAsState(true)
    val isProActionAvailable by remember(isProPurchased, icalEntity) { derivedStateOf { isProPurchased.value || icalEntity.value?.ICalCollection?.accountType == ICalCollection.LOCAL_ACCOUNT_TYPE } }

    icalEntity.value?.property?.getModuleFromString()?.let {
        detailViewModel.detailSettings.load(it, context)
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

    LaunchedEffect(linkEntryDialogModule, linkEntryDialogReltype) {
        showLinkEntryDialog = linkEntryDialogModule.isNotEmpty() && linkEntryDialogReltype != null
    }
    if(showLinkEntryDialog) {
        LinkExistingEntryDialog(
            excludeCurrentId = detailViewModel.icalEntity.value?.property?.id,
            preselectedLinkEntryModules = linkEntryDialogModule,
            preselectedLinkEntryReltype = linkEntryDialogReltype ?: Reltype.CHILD,
            allEntriesLive = detailViewModel.selectFromAllList,
            storedCategories = storedCategories,
            storedResources = storedResources,
            extendedStatuses = storedStatuses,
            settingIsAccessibilityMode = detailViewModel.settingsStateHolder.settingAccessibilityMode.value,
            player = detailViewModel.mediaPlayer,
            onAllEntriesSearchTextUpdated = { searchText, modules, sameCollection, sameAccount -> detailViewModel.updateSelectFromAllListQuery(searchText, modules, sameCollection, sameAccount) },
            onEntriesToLinkConfirmed = { selected, reltype ->
                when(reltype) {
                    Reltype.CHILD -> detailViewModel.linkNewSubentries(selected)
                    Reltype.PARENT -> detailViewModel.linkNewParents(selected)
                    Reltype.SIBLING -> Unit
                }
            },
            onDismiss = {
                linkEntryDialogModule = emptyList()
                linkEntryDialogReltype = null
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
                onAddSubnote = { subnoteText ->
                    detailViewModel.addSubEntry(ICalObject.createNote(subnoteText), null)
                    scrollToSection.value = DetailsScreenSection.SUBNOTES
                               },
                onAddSubtask = { subtaskText ->
                    detailViewModel.addSubEntry(ICalObject.createTask(subtaskText), null)
                    scrollToSection.value = DetailsScreenSection.SUBTASKS
                               },
                actions = {
                    val menuExpanded = remember { mutableStateOf(false) }

                    OverflowMenu(menuExpanded = menuExpanded) {

                        Text(stringResource(R.string.details_app_bar_behaviour), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp))
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.detail_top_app_bar_quick_add_subtask),
                                    color = if (detailViewModel.settingsStateHolder.detailTopAppBarMode.value == DetailTopAppBarMode.ADD_SUBTASK) MaterialTheme.colorScheme.primary else Color.Unspecified
                                )
                            },
                            onClick = {
                                detailViewModel.settingsStateHolder.setDetailTopAppBarMode(DetailTopAppBarMode.ADD_SUBTASK)
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
                                    text = stringResource(id = R.string.detail_top_app_bar_quick_add_subnote),
                                    color = if (detailViewModel.settingsStateHolder.detailTopAppBarMode.value == DetailTopAppBarMode.ADD_SUBNOTE) MaterialTheme.colorScheme.primary else Color.Unspecified
                                )
                            },
                            onClick = {
                                detailViewModel.settingsStateHolder.setDetailTopAppBarMode(DetailTopAppBarMode.ADD_SUBNOTE)
                                menuExpanded.value = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.NoteAdd,
                                    contentDescription = null,
                                    tint = if (detailViewModel.settingsStateHolder.detailTopAppBarMode.value == DetailTopAppBarMode.ADD_SUBNOTE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.link_entry)) },
                            onClick = {
                                linkEntryDialogModule = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
                                linkEntryDialogReltype = Reltype.CHILD
                                menuExpanded.value = false
                            },
                            leadingIcon = { Icon(painterResource(id = R.drawable.ic_link_variant_plus), null) }
                        )

                        HorizontalDivider()

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

                        HorizontalDivider()

                        CheckboxWithText(
                            text = stringResource(id = R.string.menu_view_markdown_formatting),
                            onCheckedChange = {
                                detailViewModel.detailSettings.detailSetting[DetailSettingsOption.ENABLE_MARKDOWN] = it
                                detailViewModel.detailSettings.save()
                            },
                            isSelected = detailViewModel.detailSettings.detailSetting[DetailSettingsOption.ENABLE_MARKDOWN] ?: true,
                        )

                        HorizontalDivider()

                        if(icalEntity.value?.ICalCollection?.readonly == false && icalEntity.value?.ICalCollection?.supportsVJOURNAL == true) {
                            if (icalEntity.value?.property?.module != Module.JOURNAL.name) {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(id = R.string.menu_view_convert_to_journal)) },
                                    onClick = { detailViewModel.convertTo(Module.JOURNAL) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.EventNote,
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
                                            imageVector = Icons.AutoMirrored.Outlined.Note,
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
                observedICalEntity = icalEntity,
                iCalObject = detailViewModel.mutableICalObject,
                categories = detailViewModel.mutableCategories,
                resources = detailViewModel.mutableResources,
                attendees = detailViewModel.mutableAttendees,
                comments = detailViewModel.mutableComments,
                attachments = detailViewModel.mutableAttachments,
                alarms = detailViewModel.mutableAlarms,
                isEditMode = isEditMode,
                changeState = detailViewModel.changeState,
                parentsLive = detailViewModel.relatedParents,
                subtasksLive = detailViewModel.relatedSubtasks,
                subnotesLive = detailViewModel.relatedSubnotes,
                isChildLive = detailViewModel.isChild,
                allWriteableCollectionsLive = detailViewModel.allWriteableCollections,
                allCategoriesLive = detailViewModel.allCategories,
                allResourcesLive = detailViewModel.allResources,
                storedCategories = storedCategories,
                storedResources = storedResources,
                extendedStatuses = storedStatuses,
                detailSettings = detailViewModel.detailSettings,
                icalObjectIdList = icalObjectIdList,
                seriesInstancesLive = detailViewModel.seriesInstances,
                seriesElement = seriesElement.value,
                sliderIncrement = detailViewModel.settingsStateHolder.settingStepForProgress.value.getProgressStepKeyAsInt(),
                showProgressForMainTasks = detailViewModel.settingsStateHolder.settingShowProgressForMainTasks.value,
                showProgressForSubTasks = detailViewModel.settingsStateHolder.settingShowProgressForSubTasks.value,
                keepStatusProgressCompletedInSync = detailViewModel.settingsStateHolder.settingKeepStatusProgressCompletedInSync.value,
                linkProgressToSubtasks = detailViewModel.settingsStateHolder.settingLinkProgressToSubtasks.value,
                isSubtaskDragAndDropEnabled = detailViewModel.detailSettings.listSettings?.subtasksOrderBy?.value == OrderBy.DRAG_AND_DROP,
                isSubnoteDragAndDropEnabled = detailViewModel.detailSettings.listSettings?.subnotesOrderBy?.value == OrderBy.DRAG_AND_DROP,
                markdownState = markdownState,
                scrollToSectionState = scrollToSection,
                saveEntry = {
                    detailViewModel.saveEntry()
                    onLastUsedCollectionChanged(detailViewModel.mutableICalObject!!.getModuleFromString(), detailViewModel.mutableICalObject!!.collectionId)
                },
                onProgressChanged = { itemId, newPercent -> detailViewModel.updateProgress(itemId, newPercent) },
                onMoveToNewCollection = { newCollection -> detailViewModel.moveToNewCollection(newCollection.collectionId) },
                onSubEntryAdded = { icalObject, attachment ->
                    detailViewModel.addSubEntry(icalObject, attachment)
                    when(icalObject.getModuleFromString()) {
                        Module.JOURNAL -> scrollToSection.value = DetailsScreenSection.SUBNOTES
                        Module.NOTE -> scrollToSection.value = DetailsScreenSection.SUBNOTES
                        Module.TODO -> scrollToSection.value = DetailsScreenSection.SUBTASKS
                    }
                                  },
                onSubEntryDeleted = { icalObjectId -> detailViewModel.deleteById(icalObjectId) },
                onSubEntryUpdated = { icalObjectId, newText -> detailViewModel.updateSummary(icalObjectId, newText) },
                onUnlinkSubEntry = { icalObjectId, parentUID -> detailViewModel.unlinkFromParent(icalObjectId, parentUID) },
                player = detailViewModel.mediaPlayer,
                goToDetail = { itemId, editMode, list, popBackStack ->
                    if(popBackStack)
                        navController.popBackStack()
                    navController.navigate(DetailDestination.Detail.getRoute(itemId, list, editMode)) },
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
                onShowLinkExistingDialog = { modules, reltype ->
                    linkEntryDialogModule = modules
                    linkEntryDialogReltype = reltype
                },
                onUpdateSortOrder = { detailViewModel.updateSortOrder(it) },
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
