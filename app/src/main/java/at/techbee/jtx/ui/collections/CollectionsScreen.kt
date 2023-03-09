/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.collections

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.ui.GlobalStateHolder
import at.techbee.jtx.ui.reusable.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.reusable.appbars.JtxTopAppBar
import at.techbee.jtx.ui.reusable.appbars.OverflowMenu
import at.techbee.jtx.ui.reusable.dialogs.CollectionsAddOrEditDialog
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.SyncUtil
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    navController: NavHostController,
    globalStateHolder: GlobalStateHolder,
    collectionsViewModel: CollectionsViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val isDAVx5available = SyncUtil.isDAVx5Available(context)
    val snackbarHostState = remember { SnackbarHostState() }

    /* EXPORT FUNCTIONALITIES */
    val collectionsICS = collectionsViewModel.collectionsICS.observeAsState()
    val resultExportFilepath = remember { mutableStateOf<Uri?>(null) }
    val launcherExportAll = rememberLauncherForActivityResult(CreateDocument("application/zip")) {
        resultExportFilepath.value = it
    }
    val launcherExportSingle = rememberLauncherForActivityResult(CreateDocument("text/calendar")) {
        resultExportFilepath.value = it
    }
    if (resultExportFilepath.value == null && collectionsICS.value != null && collectionsICS.value!!.size > 1) {
        launcherExportAll.launch(
            "jtxBoard_${
                DateTimeUtils.convertLongToYYYYMMDDString(
                    System.currentTimeMillis(),
                    TimeZone.getDefault().id
                )
            }.zip"
        )
    } else if (resultExportFilepath.value == null && collectionsICS.value != null && collectionsICS.value!!.size == 1) {
        launcherExportSingle.launch(
            "${collectionsICS.value!!.first().first}_${
                DateTimeUtils.convertLongToYYYYMMDDString(
                    System.currentTimeMillis(),
                    null
                )
            }.ics"
        )
    } else if (resultExportFilepath.value != null && !collectionsICS.value.isNullOrEmpty() && collectionsICS.value!!.size > 1) {
        collectionsViewModel.exportICSasZIP(
            resultExportFilepath = resultExportFilepath.value,
            context = context
        )
        resultExportFilepath.value = null
    } else if (resultExportFilepath.value != null && !collectionsICS.value.isNullOrEmpty() && collectionsICS.value!!.size == 1) {
        collectionsViewModel.exportICS(
            resultExportFilepath = resultExportFilepath.value,
            context = context
        )
        resultExportFilepath.value = null
    }

    /* IMPORT FUNCTIONALITIES */
    val resultImportFilepath = remember { mutableStateOf<Uri?>(null) }
    val launcherImport = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        resultImportFilepath.value = it
    }
    val importCollection = remember { mutableStateOf<CollectionsView?>(null) }
    // import from file uri
    if (resultImportFilepath.value != null && importCollection.value != null) {
        context.contentResolver?.openInputStream(resultImportFilepath.value!!)?.use {
            val icsString = it.readBytes().decodeToString()
            collectionsViewModel.insertICSFromReader(
                importCollection.value!!.toICalCollection(),
                icsString
            )
            importCollection.value = null
            resultImportFilepath.value = null
            it.close()
        }
    }
    // import from intent
    if (importCollection.value != null && globalStateHolder.icalString2Import.value != null) {
        collectionsViewModel.insertICSFromReader(
            importCollection.value!!.toICalCollection(),
            globalStateHolder.icalString2Import.value!!
        )
        importCollection.value = null
        globalStateHolder.icalString2Import.value = null
    }

    val snackbarMessage = stringResource(id = R.string.collections_snackbar_select_collection_for_ics_import)
    LaunchedEffect(key1 = importCollection, key2 = globalStateHolder.icalString2Import) {
        if (importCollection.value == null && globalStateHolder.icalString2Import.value != null) {
            snackbarHostState.showSnackbar(
                snackbarMessage,
                duration = SnackbarDuration.Indefinite
            )
        }
    }

    // show result
    val insertResult by collectionsViewModel.resultInsertedFromICS.observeAsState()
    insertResult?.let {
        Toast.makeText(
            context,
            stringResource(R.string.collections_snackbar_x_items_added, it.first, it.second),
            Toast.LENGTH_LONG
        ).show()
        collectionsViewModel.resultInsertedFromICS.value = null
        snackbarHostState.currentSnackbarData?.dismiss()
    }


    var showCollectionsAddDialog by remember { mutableStateOf(false) }
    if (showCollectionsAddDialog)
        CollectionsAddOrEditDialog(
            current = ICalCollection.createLocalCollection(context),
            onCollectionChanged = { collection -> collectionsViewModel.saveCollection(collection) },
            onDismiss = { showCollectionsAddDialog = false }
        )

    Scaffold(
        topBar = {
            JtxTopAppBar(
                drawerState = drawerState,
                title = stringResource(id = R.string.navigation_drawer_collections),
                actions = {

                    val menuExpanded = remember { mutableStateOf(false) }

                    OverflowMenu(menuExpanded = menuExpanded) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.menu_collections_add_local)) },
                            onClick = {
                                showCollectionsAddDialog = true
                                menuExpanded.value = false
                            },
                            leadingIcon = { Icon(Icons.Outlined.LocalLibrary, null) },
                        )
                        if (isDAVx5available) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.menu_collections_add_remote)) },
                                onClick = {
                                    SyncUtil.openDAVx5AccountsActivity(context)
                                    menuExpanded.value = false
                                },
                                leadingIcon = { Icon(Icons.Outlined.Backup, null) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.menu_collections_export_all)) },
                            onClick = {
                                collectionsViewModel.collections.value?.let {
                                    collectionsViewModel.requestICSForExport(
                                        it
                                    )
                                }
                                menuExpanded.value = false
                            },
                            leadingIcon = { Icon(Icons.Outlined.FileDownload, null) }
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            JtxNavigationDrawer(
                drawerState = drawerState,
                mainContent = {
                    CollectionsScreenContent(
                        collectionsLive = collectionsViewModel.collections,
                        isProcessing = collectionsViewModel.isProcessing,
                        onCollectionChanged = { collection ->
                            collectionsViewModel.saveCollection(
                                collection
                            )
                        },
                        onCollectionDeleted = { collection ->
                            collectionsViewModel.deleteCollection(
                                collection
                            )
                        },
                        onEntriesMoved = { old, new ->
                            collectionsViewModel.moveCollectionItems(
                                old.collectionId,
                                new.collectionId
                            )
                        },
                        onImportFromICS = { collection ->
                            importCollection.value = collection
                            launcherImport.launch(arrayOf("text/calendar"))
                        },
                        onExportAsICS = { collection ->
                            collectionsViewModel.requestICSForExport(
                                listOf(collection)
                            )
                        },
                        onCollectionClicked = { collection ->
                            if (globalStateHolder.icalString2Import.value?.isNotEmpty() == true && !collection.readonly)
                                importCollection.value = collection
                        },
                        onDeleteAccount = { account -> collectionsViewModel.removeAccount(account) }
                    )

                },
                navController = navController,
                paddingValues = paddingValues
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
    )
}



@Preview(showBackground = true)
@Composable
fun CollectionsScreen_Preview() {
    MaterialTheme {
        CollectionsScreen(
            navController = rememberNavController(),
            globalStateHolder = GlobalStateHolder(LocalContext.current),
            collectionsViewModel = CollectionsViewModel(LocalContext.current.applicationContext as Application)
        )
    }
}



