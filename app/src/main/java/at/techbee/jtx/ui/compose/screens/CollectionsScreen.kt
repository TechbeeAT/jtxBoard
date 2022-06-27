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
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.ui.CollectionsViewModel
import at.techbee.jtx.ui.compose.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.compose.appbars.JtxTopAppBar
import at.techbee.jtx.ui.compose.appbars.OverflowMenu
import at.techbee.jtx.ui.compose.cards.CollectionCard
import at.techbee.jtx.ui.compose.dialogs.CollectionsAddOrEditDialog
import at.techbee.jtx.ui.compose.stateholder.GlobalStateHolder
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.SyncUtil
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    navController: NavHostController,
    collectionsViewModel: CollectionsViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val isDAVx5available = SyncUtil.isDAVx5CompatibleWithJTX(context.applicationContext as Application)

    /* EXPORT FUNCTIONALITIES */
    val resultExportFilepath = remember { mutableStateOf<Uri?>(null) }
    val launcherExportAll = rememberLauncherForActivityResult(CreateDocument("text/calendar")) {
        resultExportFilepath.value = it
    }
    val collectionsICS = collectionsViewModel.collectionsICS.observeAsState()
    if(resultExportFilepath.value == null && collectionsICS.value != null && collectionsICS.value!!.size > 1) {
        launcherExportAll.launch("jtxBoard_${DateTimeUtils.convertLongToYYYYMMDDString(System.currentTimeMillis(), TimeZone.getDefault().id)}.zip")
    } else if(resultExportFilepath.value == null && collectionsICS.value != null && collectionsICS.value!!.size == 1) {
        launcherExportAll.launch("${collectionsICS.value!!.first().first}_${DateTimeUtils.convertLongToYYYYMMDDString(System.currentTimeMillis(),null)}.ics")
    }
    else if(resultExportFilepath.value != null && !collectionsICS.value.isNullOrEmpty() && collectionsICS.value!!.size > 1) {
        collectionsViewModel.exportICSasZIP(resultExportFilepath = resultExportFilepath.value, context = context)
        resultExportFilepath.value = null
    } else if(resultExportFilepath.value != null && !collectionsICS.value.isNullOrEmpty() && collectionsICS.value!!.size == 1) {
        collectionsViewModel.exportICS(resultExportFilepath = resultExportFilepath.value, context = context)
        resultExportFilepath.value = null
    }


    var showCollectionsAddDialog by remember { mutableStateOf(false) }
    if (showCollectionsAddDialog)
        CollectionsAddOrEditDialog(
            current = ICalCollection.createLocalCollection(context),
            onCollectionChanged = { collection -> collectionsViewModel.saveCollection(collection) },
            onDismiss = { showCollectionsAddDialog = false }
        )

    Scaffold(
        topBar = { JtxTopAppBar(
            drawerState = drawerState,
            title = stringResource(id = R.string.navigation_drawer_collections),
            actions = {

                val menuExpanded = remember { mutableStateOf(false) }

                OverflowMenu(menuExpanded = menuExpanded) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.menu_collections_add_local))  },
                        onClick = {
                            showCollectionsAddDialog = true
                            menuExpanded.value = false
                                  },
                        leadingIcon = { Icon(Icons.Outlined.LocalLibrary, null) }
                    )
                    if(isDAVx5available) {
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
                        text = { Text(text = stringResource(id = R.string.menu_collections_export_all))  },
                        onClick = {
                            collectionsViewModel.collections.value?.let { collectionsViewModel.requestICSForExport(it)}
                            menuExpanded.value = false
                                  },
                        leadingIcon = { Icon(Icons.Outlined.FileDownload, null) }
                    )
                }
            }
        ) },
        content = {
            Column {
                JtxNavigationDrawer(
                    drawerState = drawerState,
                    mainContent = { CollectionsScreenContent(
                        collectionsLive = collectionsViewModel.collections,
                        isProcessing = collectionsViewModel.isProcessing,
                        onCollectionChanged = { collection -> collectionsViewModel.saveCollection(collection) },
                        onCollectionDeleted = { collection -> collectionsViewModel.deleteCollection(collection) },
                        onEntriesMoved = { old, new -> collectionsViewModel.moveCollectionItems(old.collectionId, new.collectionId) },
                        onImportFromICS = { collection -> /* importFromICS(collection) */ /*TODO*/ },
                        onExportAsICS = { collection -> collectionsViewModel.requestICSForExport(listOf(collection)) },
                        onCollectionClicked = { collection ->
                            /*
                            iCalString2Import?.let {
                                collectionsViewModel.isProcessing.postValue(true)
                                collectionsViewModel.insertICSFromReader(collection.toICalCollection(), it)
                                iCalString2Import = null
                                iCalImportSnackbar?.dismiss()
                            }
                             */  /*TODO*/
                        },
                        onDeleteAccount = { account -> collectionsViewModel.removeAccount(account) },
                    )

                    },
                    navController = navController
                )
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CollectionsScreenContent(
    collectionsLive: LiveData<List<CollectionsView>>,
    isProcessing: LiveData<Boolean>,
    onCollectionChanged: (ICalCollection) -> Unit,
    onCollectionDeleted: (ICalCollection) -> Unit,
    onEntriesMoved: (old: ICalCollection, new: ICalCollection) -> Unit,
    onImportFromICS: (CollectionsView) -> Unit,
    onExportAsICS: (CollectionsView) -> Unit,
    onCollectionClicked: (CollectionsView) -> Unit,
    onDeleteAccount: (Account) -> Unit
) {

    val list by collectionsLive.observeAsState(emptyList())
    val grouped = list.groupBy { Account(it.accountName, it.accountType) }
    val showProgressIndicator by isProcessing.observeAsState(false)

    val foundAccounts = AccountManager.get(LocalContext.current).getAccountsByType(ICalCollection.DAVX5_ACCOUNT_TYPE)


    Box {
        AnimatedVisibility(
            visible = showProgressIndicator,
            content = {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        )

        Column(
            modifier = Modifier
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                stringResource(id = R.string.collections_info),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            grouped.forEach { (account, collectionsInAccount) ->
                CollectionsAccountHeader(
                    account = account,
                    isFoundInAccountmanager = foundAccounts.contains(account) || account.type == LOCAL_ACCOUNT_TYPE,
                    onDeleteAccount = onDeleteAccount,
                    modifier = Modifier.padding(
                        top = 16.dp,
                        start = 8.dp,
                        end = 16.dp,
                        bottom = 8.dp
                    )
                )

                collectionsInAccount.forEach { collection ->

                    CollectionCard(
                        collection = collection,
                        allCollections = list,
                        onCollectionChanged = onCollectionChanged,
                        onCollectionDeleted = onCollectionDeleted,
                        onEntriesMoved = onEntriesMoved,
                        onImportFromICS = onImportFromICS,
                        onExportAsICS = onExportAsICS,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                            .combinedClickable(
                                onClick = { onCollectionClicked(collection) })
                    )
                }
            }
        }

        /*
        // Alternative with LazyColumn caused weird scroll behaviour, observe if a better solution can be found!
        // https://stackoverflow.com/questions/72604009/jetpack-compose-lazycolumn-items-scroll-over-stickyheader-and-does-not-scroll-to/72604421#72604421

        LazyColumn(
            modifier = Modifier.padding(8.dp)
        ) {

            item {
                Text(
                    stringResource(id = R.string.collections_info),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            grouped.forEach { (account, collectionsInAccount) ->
                stickyHeader {
                    Text(
                        account,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            top = 16.dp,
                            start = 8.dp,
                            end = 16.dp,
                            bottom = 8.dp
                        )
                    )
                }

                items(
                    items = collectionsInAccount,
                    key = { collection -> collection.collectionId }
                ) { collection ->

                    CollectionCard(
                        collection = collection,
                        allCollections = list,
                        onCollectionChanged = onCollectionChanged,
                        onCollectionDeleted = onCollectionDeleted,
                        onEntriesMoved = onEntriesMoved,
                        onImportFromICS = onImportFromICS,
                        onExportAsICS = onExportAsICS,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .animateItemPlacement()
                            .combinedClickable(
                                onClick = { onCollectionClicked(collection) })
                    )
                }
            }
        }
        */

    }
}


@Preview(showBackground = true)
@Composable
fun CollectionsScreenContent_Preview() {
    JtxBoardTheme {

        val collection1 = CollectionsView(
            collectionId = 1L,
            color = Color.Cyan.toArgb(),
            displayName = "Test",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )

        val collection2 = CollectionsView(
            collectionId = 2L,
            displayName = "Test Number 2",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL",
            numJournals = 5,
            numNotes = 19,
            numTodos = 8989,
            supportsVJOURNAL = true,
            supportsVTODO = true
        )

        val collection3 = CollectionsView(
            collectionId = 3L,
            displayName = "Test",
            description = "Here comes the desc",
            accountName = "Another account",
            accountType = "at.bitfire.davx5"
        )
        CollectionsScreenContent(
            collectionsLive = MutableLiveData(listOf(collection1, collection2, collection3)),
            isProcessing = MutableLiveData(true),
            onCollectionChanged = { },
            onCollectionDeleted = { },
            onEntriesMoved = { _, _ -> },
            onImportFromICS = { },
            onExportAsICS = { },
            onCollectionClicked = { },
            onDeleteAccount = { }
        )
    }
}


@Composable
fun CollectionsAccountHeader(
    account: Account,
    isFoundInAccountmanager: Boolean,
    onDeleteAccount: (Account) -> Unit,
    modifier: Modifier = Modifier
) {

    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    if (showDeleteAccountDialog)
        AccountDeleteDialog(
            account = account,
            onDeleteAccount = onDeleteAccount,
            onDismiss = { showDeleteAccountDialog = false }
        )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Column(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)) {
            Text(
                account.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            if (!isFoundInAccountmanager) {
                Text(
                    stringResource(id = R.string.collections_account_not_found_info),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        if (!isFoundInAccountmanager)
            IconButton(onClick = { showDeleteAccountDialog = true }) {
                Icon(Icons.Outlined.Delete, contentDescription = stringResource(id = R.string.delete))
            }
    }
}


@Preview(showBackground = true)
@Composable
fun CollectionsAccountHeader_Preview() {
    JtxBoardTheme {

        CollectionsAccountHeader(
            Account("Test Account Name", "at.bitfire.davdroid"),
            isFoundInAccountmanager = true,
            onDeleteAccount = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CollectionsAccountHeader_Preview2() {
    JtxBoardTheme {

        CollectionsAccountHeader(
            Account("Test Account Name", "at.bitfire.davdroid"),
            false,
            onDeleteAccount = { }
        )
    }
}



@Composable
fun AccountDeleteDialog(
    account: Account,
    onDeleteAccount: (Account) -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.collections_account_delete_dialog_title, account.name)) },
        text = { Text(stringResource(R.string.collections_account_delete_dialog_message)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDeleteAccount(account)
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.delete))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text( stringResource(id = R.string.cancel))
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun AccountDeleteDialog_preview() {
    JtxBoardTheme {

        AccountDeleteDialog(
            Account("Test Account Name", "at.bitfire.davdroid"),
            onDeleteAccount = { },
            onDismiss = { }
        )
    }
}


