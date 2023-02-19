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
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.ui.GlobalStateHolder
import at.techbee.jtx.ui.reusable.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.reusable.appbars.JtxTopAppBar
import at.techbee.jtx.ui.reusable.appbars.OverflowMenu
import at.techbee.jtx.ui.reusable.dialogs.CollectionsAddOrEditDialog
import at.techbee.jtx.ui.reusable.dialogs.SelectModuleForTxtImportDialog
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
    val isDAVx5available =
        SyncUtil.isDAVx5CompatibleWithJTX(context.applicationContext as Application)

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
    var resultImportICSFilepaths by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val launcherImportICS = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
        resultImportICSFilepaths = it
    }
    var resultImportTxtFilepaths by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val launcherImportTxt = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
        resultImportTxtFilepaths = it
    }
    var importCollection by remember { mutableStateOf<CollectionsView?>(null) }
    var importModule by remember { mutableStateOf<Module?>(null) }


    LaunchedEffect(resultImportICSFilepaths, importCollection) {
        // import from file uri
        if (resultImportICSFilepaths.isNotEmpty() && importCollection != null) {
            resultImportICSFilepaths.forEach { filepath ->
                context.contentResolver?.openInputStream(filepath)?.use {
                    val icsString = it.readBytes().decodeToString()
                    collectionsViewModel.insertICSFromReader(
                        importCollection!!.toICalCollection(),
                        icsString
                    )

                    it.close()
                }
            }
            importCollection = null
            resultImportICSFilepaths = emptyList()
        }
    }

    var showSelectModuleForTxtImportDialog by remember { mutableStateOf(false) }
    if(showSelectModuleForTxtImportDialog && importCollection != null && resultImportTxtFilepaths.isNotEmpty()) {
        SelectModuleForTxtImportDialog(
            files = resultImportTxtFilepaths,
            onModuleSelected = { module -> importModule = module },
            onDismiss = { showSelectModuleForTxtImportDialog = false }
        )
    }

    LaunchedEffect(resultImportTxtFilepaths, importCollection, importModule) {
        // import from file uri
        if(importModule == null) {
            showSelectModuleForTxtImportDialog = true
            return@LaunchedEffect
        }

        if (resultImportTxtFilepaths.isNotEmpty() && importCollection != null) {
            resultImportTxtFilepaths.forEach { filepath ->
                context.contentResolver?.openInputStream(filepath)?.use {
                    collectionsViewModel.insertTxt(text = it.readBytes().decodeToString(), module = importModule!!, collection = importCollection!!.toICalCollection())
                    it.close()
                }
            }
            Toast.makeText(
                context,
                context.getString(R.string.collections_toast_x_items_added, resultImportTxtFilepaths.size),
                Toast.LENGTH_LONG
            ).show()
            importCollection = null
            importModule = null
            resultImportTxtFilepaths = emptyList()
        }
    }

    LaunchedEffect(resultImportICSFilepaths, globalStateHolder.icalString2Import.value) {
        // import from intent
        if (importCollection != null && globalStateHolder.icalString2Import.value != null) {
            collectionsViewModel.insertICSFromReader(
                importCollection!!.toICalCollection(),
                globalStateHolder.icalString2Import.value!!
            )
            importCollection = null
            globalStateHolder.icalString2Import.value = null
        }
    }

    val snackbarMessage = stringResource(id = R.string.collections_snackbar_select_collection_for_ics_import)
    LaunchedEffect(importCollection, globalStateHolder.icalString2Import) {
        if (importCollection == null && globalStateHolder.icalString2Import.value != null) {
            snackbarHostState.showSnackbar(
                snackbarMessage,
                duration = SnackbarDuration.Indefinite
            )
        }
    }

    // show result
    val insertResult by collectionsViewModel.resultInsertedFromICS.observeAsState()
    LaunchedEffect(insertResult) {
        insertResult?.let {
            Toast.makeText(
                context,
                context.getString(R.string.collections_snackbar_x_items_added, it.first, it.second),
                Toast.LENGTH_LONG
            ).show()
            snackbarHostState.currentSnackbarData?.dismiss()
            collectionsViewModel.resultInsertedFromICS.value = null
        }
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
                            importCollection = collection
                            launcherImportICS.launch(arrayOf("text/calendar"))
                        },
                        onImportFromTxt = { collection ->
                            importCollection = collection
                            launcherImportTxt.launch(arrayOf("text/plain", "text/markdown"))
                        },
                        onExportAsICS = { collection ->
                            collectionsViewModel.requestICSForExport(
                                listOf(collection)
                            )
                        },
                        onCollectionClicked = { collection ->
                            if (globalStateHolder.icalString2Import.value?.isNotEmpty() == true && !collection.readonly)
                                importCollection = collection
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



