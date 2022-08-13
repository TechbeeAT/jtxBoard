/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    navController: NavHostController,
    detailViewModel: DetailViewModel,
    editImmediately: Boolean = false,
    //globalStateHolder: GlobalStateHolder,
    //collectionsViewModel: CollectionsViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    //val context = LocalContext.current

    //val scope = rememberCoroutineScope()

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

    val icalEntity = detailViewModel.icalEntity.observeAsState()


    Scaffold(
        topBar = { JtxTopAppBar(
            drawerState = drawerState,
            title = "TODO",
            actions = {

                val menuExpanded = remember { mutableStateOf(false) }

                OverflowMenu(menuExpanded = menuExpanded) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.menu_view_share_text))  },
                        onClick = { /* TODO */ },
                        leadingIcon = { Icon(Icons.Outlined.Share, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.menu_view_share_ics))  },
                        onClick = { /* TODO */ },
                        leadingIcon = { Icon(Icons.Outlined.Description, null) }
                    )
                }
            }
        ) },
        content = {
            Column(modifier = Modifier.padding(it)) {
                JtxNavigationDrawer(
                    drawerState = drawerState,
                    mainContent = {

                                  // for testing only!
/*
                        val entity = ICalEntity().apply {
                            this.property = ICalObject.createJournal("MySummary")
                            //this.property.dtstart = System.currentTimeMillis()
                        }
                        entity.property.description = "Hello World, this \nis my description."
                        entity.categories = listOf(
                            Category(1,1,"MyCategory1", null, null),
                            Category(2,1,"My Dog likes Cats", null, null),
                            Category(3,1,"This is a very long category", null, null),
                        )

 */

                        DetailScreenContent(
                            iCalEntity = icalEntity,
                            isEditMode = isEditMode,
                            subtasks = emptyList(),
                            subnotes = emptyList(),
                            //attachments = emptyList(),
                            allCollections = listOf(
                                ICalCollection.createLocalCollection(
                                    LocalContext.current
                                )
                            ),
                            //player = null,
                            onProgressChanged = { _, _, _ -> },
                            onExpandedChanged = { _, _, _, _ -> }
                        )

                        /*
                        CollectionsScreenContent(
                            collectionsLive = collectionsViewModel.collections,
                            isProcessing = collectionsViewModel.isProcessing,
                            onCollectionChanged = { collection -> collectionsViewModel.saveCollection(collection) },
                            onCollectionDeleted = { collection -> collectionsViewModel.deleteCollection(collection) },
                            onEntriesMoved = { old, new -> collectionsViewModel.moveCollectionItems(old.collectionId, new.collectionId) },
                            onImportFromICS = { collection ->
                                importCollection.value = collection
                                launcherImport.launch(arrayOf("text/calendar"))
                                              },
                            onExportAsICS = { collection -> collectionsViewModel.requestICSForExport(listOf(collection)) },
                            onCollectionClicked = { collection ->
                                if(globalStateHolder.icalString2Import.value?.isNotEmpty() == true)
                                    importCollection.value = collection
                            },
                            onDeleteAccount = { account -> collectionsViewModel.removeAccount(account) }
                        )

                         */
                    },
                    navController = navController
                )
            }
        },
        bottomBar = {
            DetailBottomAppBar(
                module = Module.JOURNAL,   // TODO
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
                enableComments = enableComments
            )
        }
    )
}
