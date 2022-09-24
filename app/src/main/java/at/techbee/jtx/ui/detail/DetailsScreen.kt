/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.ui.reusable.appbars.DetailsTopAppBar
import at.techbee.jtx.ui.reusable.appbars.OverflowMenu
import at.techbee.jtx.ui.reusable.destinations.DetailDestination
import at.techbee.jtx.ui.reusable.dialogs.DeleteEntryDialog
import at.techbee.jtx.ui.reusable.dialogs.ErrorOnUpdateDialog
import at.techbee.jtx.ui.reusable.dialogs.RevertChangesDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    navController: NavHostController,
    detailViewModel: DetailViewModel,
    editImmediately: Boolean = false,
    autosave: Boolean,
    onLastUsedCollectionChanged: (Long) -> Unit,
    onRequestReview: () -> Unit,
) {
    //val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current

    val isEditMode = rememberSaveable { mutableStateOf(editImmediately) }
    var goBackRequestedByTopBar by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRevertDialog by remember { mutableStateOf(false) }
    var navigateUp by remember { mutableStateOf(false) }


    val icalEntity = detailViewModel.icalEntity.observeAsState()
    val subtasks = detailViewModel.relatedSubtasks.observeAsState(emptyList())
    val subnotes = detailViewModel.relatedSubnotes.observeAsState(emptyList())
    val isChild = detailViewModel.isChild.observeAsState(false)
    val allCategories = detailViewModel.allCategories.observeAsState(emptyList())
    val allResources = detailViewModel.allResources.observeAsState(emptyList())
    val allCollections = detailViewModel.allCollections.observeAsState(emptyList())

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
        navController.navigate(DetailDestination.Detail.getRoute(it, true))
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
                subtitle = detailViewModel.icalEntity.value?.property?.summary,
                goBack = {
                    goBackRequestedByTopBar = true
                },     // goBackRequestedByTopBar is handled in DetailScreenContent.kt
                actions = {

                    if (!isEditMode.value) {
                        val menuExpanded = remember { mutableStateOf(false) }
                        OverflowMenu(menuExpanded = menuExpanded) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.menu_view_share_text)) },
                                onClick = {
                                    detailViewModel.shareAsText(context)
                                    menuExpanded.value = false
                                },
                                leadingIcon = { Icon(Icons.Outlined.Share, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.menu_view_share_ics)) },
                                onClick = {
                                    detailViewModel.shareAsICS(context)
                                    menuExpanded.value = false
                                },
                                leadingIcon = { Icon(Icons.Outlined.Description, null) }
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
                allCollections = allCollections.value,
                allCategories = allCategories.value,
                allResources = allResources.value,
                detailSettings = detailViewModel.detailSettings,
                autosave = autosave,
                goBackRequested = goBackRequestedByTopBar,
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
                    onLastUsedCollectionChanged(changedICalObject.collectionId)
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
                goToView = { icalObjectId -> navController.navigate(DetailDestination.Detail.getRoute(icalObjectId, false)) },
                goToEdit = { icalObjectId -> navController.navigate(DetailDestination.Detail.getRoute(icalObjectId, true)) },
                goBack = { navigateUp = true },
                modifier = Modifier.padding(paddingValues)
            )
        },
        bottomBar = {
            DetailBottomAppBar(
                icalObject = icalEntity.value?.property,
                collection = icalEntity.value?.ICalCollection,
                isEditMode = isEditMode,
                changeState = detailViewModel.changeState,
                detailSettings = detailViewModel.detailSettings,
                onDeleteClicked = { showDeleteDialog = true },
                onCopyRequested = { newModule -> detailViewModel.createCopy(newModule) },
                onRevertClicked = { showRevertDialog = true }
            )
        }
    )
}
