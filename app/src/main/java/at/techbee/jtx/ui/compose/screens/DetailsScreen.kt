/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.screens

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.ui.CollectionsViewModel
import at.techbee.jtx.ui.compose.appbars.DetailBottomAppBar
import at.techbee.jtx.ui.compose.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.compose.appbars.JtxTopAppBar
import at.techbee.jtx.ui.compose.appbars.OverflowMenu
import at.techbee.jtx.ui.compose.cards.CollectionCard
import at.techbee.jtx.ui.compose.dialogs.CollectionsAddOrEditDialog
import at.techbee.jtx.ui.compose.stateholder.GlobalStateHolder
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.SyncUtil
import kotlinx.coroutines.launch
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    navController: NavHostController,
    //globalStateHolder: GlobalStateHolder,
    //collectionsViewModel: CollectionsViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

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
            Column {
                JtxNavigationDrawer(
                    drawerState = drawerState,
                    mainContent = {

                                  // for testing only!

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

                        DetailScreenContent(
                            iCalEntity = mutableStateOf(entity),
                            subtasks = emptyList(),
                            subnotes = emptyList(),
                            attachments = emptyList(),
                            allCollections = listOf(ICalCollection.createLocalCollection(LocalContext.current)),
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
                isEditMode = mutableStateOf(false),   // TODO
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
