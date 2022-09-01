/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.screens

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
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.Module
import at.techbee.jtx.ui.DetailViewModel
import at.techbee.jtx.ui.compose.appbars.DetailBottomAppBar
import at.techbee.jtx.ui.compose.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.compose.appbars.JtxTopAppBar
import at.techbee.jtx.ui.compose.appbars.OverflowMenu
import at.techbee.jtx.ui.compose.destinations.NavigationDrawerDestination
import at.techbee.jtx.ui.compose.dialogs.DeleteEntryDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    navController: NavHostController,
    detailViewModel: DetailViewModel,
    editImmediately: Boolean = false,
    //globalStateHolder: GlobalStateHolder,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val enableCategories = rememberSaveable { mutableStateOf(true) }
    val enableAttendees = rememberSaveable { mutableStateOf(false) }
    val enableResources = rememberSaveable { mutableStateOf(false) }
    val enableContact = rememberSaveable { mutableStateOf(false) }
    val enableLocation = rememberSaveable { mutableStateOf(false) }
    val enableUrl = rememberSaveable { mutableStateOf(false) }
    val enableSubtasks = rememberSaveable { mutableStateOf(true) }
    val enableSubnotes = rememberSaveable { mutableStateOf(true) }
    val enableAttachments = rememberSaveable { mutableStateOf(true) }
    val enableRecurrence = rememberSaveable { mutableStateOf(false) }
    val enableAlarms = rememberSaveable { mutableStateOf(false) }
    val enableComments = rememberSaveable { mutableStateOf(false) }

    val isEditMode = rememberSaveable { mutableStateOf(editImmediately) }
    val isReadOnly = rememberSaveable { mutableStateOf(false) }

    val showDeleteDialog = rememberSaveable { mutableStateOf(false) }

    val icalEntity = detailViewModel.icalEntity.observeAsState()
    val subtasks = detailViewModel.relatedSubtasks.observeAsState(emptyList())
    val subnotes = detailViewModel.relatedSubnotes.observeAsState(emptyList())


    if (detailViewModel.entryDeleted.value)
        navController.navigate(NavigationDrawerDestination.BOARD.name) {
            launchSingleTop = true
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
                            onClick = { /* TODO */ },
                            leadingIcon = { Icon(Icons.Outlined.Share, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.menu_view_share_ics)) },
                            onClick = { /* TODO */ },
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
                        subtasks = subtasks,
                        subnotes = subnotes,
                        allCollections = listOf(
                            ICalCollection.createLocalCollection(
                                LocalContext.current
                            )
                        ),
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
                        },
                        onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance ->
                            detailViewModel.updateProgress(itemId, newPercent)
                        },
                        onSubEntryAdded = { icalObject -> detailViewModel.addSubEntry(icalObject) },
                        onSubEntryDeleted = { icalObjectId -> detailViewModel.deleteById(icalObjectId) },
                        onSubEntryUpdated = { icalObjectId, newText -> detailViewModel.updateSummary(icalObjectId, newText) },
                        player = detailViewModel.mediaPlayer
                    )
                },
                navController = navController,
                paddingValues = paddingValues
            )
        },
        bottomBar = {
            DetailBottomAppBar(
                module = when (detailViewModel.icalEntity.value?.property?.module) {
                    Module.JOURNAL.name -> Module.JOURNAL
                    Module.NOTE.name -> Module.NOTE
                    Module.TODO.name -> Module.TODO
                    else -> Module.JOURNAL
                },
                isEditMode = isEditMode,
                isReadOnly = isReadOnly,
                enableCategories = enableCategories,
                enableAttendees = enableAttendees,
                enableResources = enableResources,
                enableContact = enableContact,
                enableLocation = enableLocation,
                enableUrl = enableUrl,
                enableSubtasks = enableSubtasks,
                enableSubnotes = enableSubnotes,
                enableAttachments = enableAttachments,
                enableRecurrence = enableRecurrence,
                enableAlarms = enableAlarms,
                enableComments = enableComments,
                onDeleteClicked = { showDeleteDialog.value = true }
            )
        }
    )
}
