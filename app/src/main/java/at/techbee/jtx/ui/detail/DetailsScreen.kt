/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.screens

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.ui.detail.DetailViewModel
import at.techbee.jtx.ui.reusable.appbars.DetailBottomAppBar
import at.techbee.jtx.ui.reusable.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.reusable.appbars.JtxTopAppBar
import at.techbee.jtx.ui.reusable.appbars.OverflowMenu
import at.techbee.jtx.ui.reusable.destinations.NavigationDrawerDestination
import at.techbee.jtx.ui.reusable.dialogs.DeleteEntryDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    navController: NavHostController,
    detailViewModel: DetailViewModel,
    editImmediately: Boolean = false,
    onLastUsedCollectionChanged: (Long) -> Unit,
    onRequestReview: () -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current

    val isEditMode = rememberSaveable { mutableStateOf(editImmediately) }
    val contentsChanged = remember { mutableStateOf<Boolean?>(null) }

    val showDeleteDialog = rememberSaveable { mutableStateOf(false) }

    val icalEntity = detailViewModel.icalEntity.observeAsState()
    val subtasks = detailViewModel.relatedSubtasks.observeAsState(emptyList())
    val subnotes = detailViewModel.relatedSubnotes.observeAsState(emptyList())
    val isChild = detailViewModel.isChild.observeAsState(false)
    val allCategories = detailViewModel.allCategories.observeAsState(emptyList())
    val allResources = detailViewModel.allResources.observeAsState(emptyList())
    val allCollections = detailViewModel.allCollections.observeAsState(emptyList())


    if (detailViewModel.entryDeleted.value) {
        Toast.makeText(context, context.getString(R.string.details_toast_entry_deleted), Toast.LENGTH_SHORT).show()
        Attachment.scheduleCleanupJob(context)
        navController.navigate(NavigationDrawerDestination.BOARD.name) {
            launchSingleTop = true
        }
        onRequestReview()
        detailViewModel.entryDeleted.value = false
    }

    detailViewModel.navigateToId.value?.let {
        detailViewModel.navigateToId.value = null
        navController.navigate("details/$it?isEditMode=true")
    }

    if(showDeleteDialog.value) {
        DeleteEntryDialog(
            icalObject = detailViewModel.icalEntity.value?.property!!,
            onConfirm = { detailViewModel.delete() },
            onDismiss = { showDeleteDialog.value = false }
        )
    }


    Scaffold(
        topBar = {
            JtxTopAppBar(
                drawerState = drawerState,
                title = stringResource(id = R.string.details),
                subtitle = detailViewModel.icalEntity.value?.property?.summary,
                actions = {

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
            )
        },
        content = { paddingValues ->
            JtxNavigationDrawer(
                drawerState = drawerState,
                mainContent = {

                    DetailScreenContent(
                        iCalEntity = icalEntity,
                        isEditMode = isEditMode,
                        contentsChanged = contentsChanged,
                        subtasks = subtasks,
                        subnotes = subnotes,
                        isChild = isChild.value,
                        allCollections = allCollections.value,
                        allCategories = allCategories.value,
                        allResources = allResources.value,
                        detailSettings = detailViewModel.detailSettings,
                        saveIcalObject = { changedICalObject, changedCategories, changedComments, changedAttendees, changedResources, changedAttachments, changedAlarms ->
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
                        onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance ->
                            detailViewModel.updateProgress(itemId, newPercent)
                        },
                        onMoveToNewCollection = { icalObject, newCollection ->
                            navController.popBackStack()
                            detailViewModel.moveToNewCollection(icalObject, newCollection.collectionId)
                                                },
                        onSubEntryAdded = { icalObject, attachment -> detailViewModel.addSubEntry(icalObject, attachment) },
                        onSubEntryDeleted = { icalObjectId -> detailViewModel.deleteById(icalObjectId) },
                        onSubEntryUpdated = { icalObjectId, newText -> detailViewModel.updateSummary(icalObjectId, newText) },
                        player = detailViewModel.mediaPlayer,
                        goToView = { icalObjectId -> navController.navigate("details/$icalObjectId?isEditMode=false") },
                        goToEdit = { icalObjectId -> navController.navigate("details/$icalObjectId?isEditMode=true") }
                    )
                },
                navController = navController,
                paddingValues = paddingValues
            )
        },
        bottomBar = {
            DetailBottomAppBar(
                icalObject = icalEntity.value?.property,
                collection = icalEntity.value?.ICalCollection,
                isEditMode = isEditMode,
                contentsChanged = contentsChanged,
                detailSettings = detailViewModel.detailSettings,
                onDeleteClicked = { showDeleteDialog.value = true },
                onCopyRequested = { newModule -> detailViewModel.createCopy(newModule) }
            )
        }
    )
}
