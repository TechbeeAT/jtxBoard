/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.sync

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.ui.reusable.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.reusable.appbars.JtxTopAppBar
import at.techbee.jtx.ui.reusable.destinations.NavigationDrawerDestination
import at.techbee.jtx.ui.theme.Typography
import at.techbee.jtx.util.SyncApp
import at.techbee.jtx.util.SyncUtil


@Composable
fun SyncScreen(
    isSyncInProgress: State<Boolean>,
    navController: NavHostController
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val remoteCollections by ICalDatabase.getInstance(context).iCalDatabaseDao().getAllRemoteCollectionsLive().observeAsState(emptyList())
    val availableSyncApps = if (LocalInspectionMode.current) emptyList() else SyncUtil.availableSyncApps(context)

    Scaffold(
        topBar = {
            JtxTopAppBar(
                drawerState = drawerState,
                title = stringResource(id = R.string.navigation_drawer_synchronization),
                actions = {
                    if (availableSyncApps.isNotEmpty()) {
                        IconButton(onClick = {
                            SyncUtil.syncAccounts(remoteCollections.map { it.getAccount() }.toSet())
                            SyncUtil.showSyncRequestedToast(context)
                        }) {
                            Icon(Icons.Outlined.Sync, stringResource(id = R.string.sync_now))
                        }
                    }
                }
            )
        },
        content = { paddingValues ->
            JtxNavigationDrawer(
                drawerState = drawerState,
                mainContent = {
                    SyncScreenContent(
                        remoteCollections = remoteCollections,
                        availableSyncApps = availableSyncApps,
                        isSyncInProgress = isSyncInProgress,
                        goToCollections = { navController.navigate(NavigationDrawerDestination.COLLECTIONS.name) })
                },
                navController = navController,
                paddingValues = paddingValues
            )
        }
    )
}


@Composable
fun SyncScreenContent(
    remoteCollections: List<ICalCollection>,
    availableSyncApps: List<SyncApp>,
    isSyncInProgress: State<Boolean>,
    goToCollections: () -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    Box {

        AnimatedVisibility(visible = isSyncInProgress.value) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SyncApp.entries.forEach { syncApp ->

                ElevatedCard(modifier = Modifier.fillMaxWidth()) {

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, 
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                    ) {

                        Text(
                            text = stringResource(id = R.string.sync_with_sync_app_heading, syncApp.appName),
                            style = Typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = syncApp.logoRes),
                                contentDescription = null,
                                modifier = Modifier.size(75.dp)
                            )
                            Text(
                                text = stringResource(syncApp.infoText),
                                style = Typography.bodyLarge,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Sync App is available
                        if (availableSyncApps.contains(syncApp)) {
                            /*
                            Text(
                                text = stringResource(id = R.string.sync_congratulations),
                                style = Typography.titleMedium,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                             */
                            Text(
                                text = stringResource(id = R.string.sync_sync_app_installed_but_no_collections_found, syncApp.appName),
                                style = Typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            if (remoteCollections.none { it.accountType == syncApp.accountType } && syncApp != SyncApp.KSYNC) {
                                Button(
                                    onClick = { SyncUtil.openSyncAppLoginActivity(syncApp, context) },
                                ) {
                                    Text(stringResource(R.string.sync_button_add_account_in_sync_app, syncApp.appName))
                                }
                            } else {
                                Button(
                                    onClick = { goToCollections() },
                                ) {
                                    Text(stringResource(id = R.string.sync_button_go_to_collections))
                                }
                            }
                            TextButton(
                                content = {
                                    Text(
                                        text = stringResource(id = R.string.sync_setup_instructions),
                                        style = Typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                },
                                onClick = {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(syncApp.setupURL)
                                        )
                                    )
                                }
                            )
                        } else {       // Sync App is NOT available
                            Text(
                                text = stringResource(id = R.string.sync_sync_app_not_found, syncApp.appName),
                                style = Typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            /*
                            Text(
                                text = stringResource(id = R.string.sync_check_out_sync_app, syncApp.appName),
                                style = Typography.titleMedium,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                            )
                             */
                            Button(
                                onClick = { SyncUtil.openSyncAppInStore(syncApp, context) },
                            ) {
                                Text(stringResource(R.string.sync_download_sync_app, syncApp.appName))
                            }

                            TextButton(
                                content = {
                                    Text(
                                        text = stringResource(id = R.string.sync_more_about_sync_app, syncApp.appName),
                                        style = Typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                onClick = {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(syncApp.websiteURL)
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SyncScreen_Preview_no_DAVX5() {
    MaterialTheme {
        SyncScreen(
            isSyncInProgress = remember { mutableStateOf(false) },
            navController = rememberNavController(),
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SyncScreenContent_Preview_no_DAVX5() {
    MaterialTheme {
        SyncScreenContent(
            availableSyncApps = emptyList(),
            isSyncInProgress = remember { mutableStateOf(false) },
            remoteCollections = emptyList(),
            goToCollections = { },
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SyncScreenContent_Preview_DAVx5_no_collections() {
    MaterialTheme {
        SyncScreenContent(
            availableSyncApps = SyncApp.entries,
            isSyncInProgress = remember { mutableStateOf(false) },
            remoteCollections = emptyList(),
            goToCollections = { },
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SyncScreenContent_Preview_DAVx5_with_collections() {
    MaterialTheme {
        SyncScreenContent(
            availableSyncApps = SyncApp.entries,
            isSyncInProgress = remember { mutableStateOf(true) },
            remoteCollections =
            listOf(
                ICalCollection().apply { this.collectionId = 1 },
                ICalCollection().apply { this.collectionId = 2 }
            ),
            goToCollections = { },
        )
    }
}