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
import androidx.biometric.BiometricPrompt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import at.techbee.jtx.ui.settings.DropdownSettingOption
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.SyncUtil


@Composable
fun CollectionsScreen(
    navController: NavHostController,
    globalStateHolder: GlobalStateHolder,
    settingsStateHolder: SettingsStateHolder,
    collectionsViewModel: CollectionsViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val availableSyncApps = SyncUtil.availableSyncApps(context)
    val snackbarHostState = remember { SnackbarHostState() }
    val collections by collectionsViewModel.collections.observeAsState(emptyList())
    val toastText = collectionsViewModel.toastText.observeAsState()

    /* EXPORT FUNCTIONALITIES */
    val launcherExportAll = rememberLauncherForActivityResult(CreateDocument("application/zip")) {
        it?.let { uri ->
            collectionsViewModel.writeToFile(uri)
        }
    }
    val launcherExportSingle = rememberLauncherForActivityResult(CreateDocument("text/calendar")) {
        it?.let { uri ->
            collectionsViewModel.writeToFile(uri)
        }
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

    LaunchedEffect(toastText.value) {
        toastText.value?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            collectionsViewModel.toastText.postValue(null)
        }
    }


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
                    collectionsViewModel.insertTxt(
                        text = it.readBytes().decodeToString(),
                        module = importModule!!,
                        collection = importCollection!!.toICalCollection(),
                        defaultJournalDateSettingOption = settingsStateHolder.settingDefaultJournalsDate.value,
                        defaultStartDateSettingOption = settingsStateHolder.settingDefaultStartDate.value,
                        defaultStartTime = settingsStateHolder.settingDefaultStartTime.value,
                        defaultDueDateSettingOption = settingsStateHolder.settingDefaultDueDate.value,
                        defaultDueTime = settingsStateHolder.settingDefaultDueTime.value
                    )
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

    val biometricPromptInfo: BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(context.getString(R.string.collections_biometric_protected_entries_locked_title))
        .setSubtitle(context.getString(R.string.collections_biometric_protected_entries_locked_subtitle))
        .setNegativeButtonText(context.getString(R.string.cancel))
        .build()

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
                        availableSyncApps.forEach { syncApp ->
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.menu_collections_add_remote_to_sync_app, syncApp.appName)) },
                                onClick = {
                                    SyncUtil.openSyncAppAccountsActivity(syncApp, context)
                                    menuExpanded.value = false
                                },
                                leadingIcon = { Icon(Icons.Outlined.Backup, null) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.menu_collections_export_all)) },
                            onClick = {
                                if(settingsStateHolder.settingProtectBiometric.value == DropdownSettingOption.PROTECT_BIOMETRIC_OFF || globalStateHolder.isAuthenticated.value) {
                                    collectionsViewModel.collectionsToExport.value = collections
                                    launcherExportAll.launch("jtxBoard_${DateTimeUtils.timestampAsFilenameAppendix()}.zip")
                                } else {
                                    globalStateHolder.biometricPrompt?.authenticate(biometricPromptInfo)
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
                        collections = collections,
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
                            if(settingsStateHolder.settingProtectBiometric.value == DropdownSettingOption.PROTECT_BIOMETRIC_OFF || globalStateHolder.isAuthenticated.value) {
                                collectionsViewModel.collectionsToExport.value = listOf(collection)
                                launcherExportSingle.launch("${collection.displayName ?: collection.collectionId.toString()}_${DateTimeUtils.timestampAsFilenameAppendix()}.ics")
                            } else {
                                globalStateHolder.biometricPrompt?.authenticate(biometricPromptInfo)
                            }
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
            settingsStateHolder = SettingsStateHolder(LocalContext.current),
            collectionsViewModel = CollectionsViewModel(LocalContext.current.applicationContext as Application)
        )
    }
}



