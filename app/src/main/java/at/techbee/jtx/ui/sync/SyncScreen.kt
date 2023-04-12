/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.sync

import android.accounts.Account
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import at.techbee.jtx.ui.theme.jtxCardCornerShape
import at.techbee.jtx.util.SyncUtil


@Composable
fun SyncScreen(
    isSyncInProgress: State<Boolean>,
    navController: NavHostController
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val remoteCollections by ICalDatabase.getInstance(context).iCalDatabaseDao.getAllRemoteCollectionsLive().observeAsState(emptyList())
    val isDAVx5available = if (LocalInspectionMode.current) true else SyncUtil.isDAVx5Available(context)

    Scaffold(
        topBar = {
            JtxTopAppBar(
                drawerState = drawerState,
                title = stringResource(id = R.string.navigation_drawer_sync),
                actions = {
                    if (isDAVx5available) {
                        IconButton(onClick = { SyncUtil.syncAccounts(remoteCollections.map { Account(it.accountName, it.accountType) }.toSet()) }) {
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
                        isDAVx5available = isDAVx5available,
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
    isDAVx5available: Boolean,
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Image(
                painter = painterResource(id = R.drawable.ic_davx5_icon_green_bg_without_shadow),
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .padding(top = 24.dp, bottom = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.sync_with_davx5_heading),
                style = Typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = R.string.sync_basic_info),
                modifier = Modifier.padding(top = 16.dp),
                style = Typography.bodyLarge,
                textAlign = TextAlign.Center
            )


            if (isDAVx5available) {
                Text(
                    text = stringResource(id = R.string.sync_congratulations),
                    style = Typography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )

                if (remoteCollections.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.sync_davx5_installed_but_no_collections_found),
                        modifier = Modifier.padding(top = 16.dp),
                        style = Typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    TextButton(
                        content = {
                            Text(
                                text = stringResource(id = R.string.link_jtx_sync),
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        },
                        modifier = Modifier.padding(top = 8.dp),
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(context.getString(R.string.link_jtx_sync))
                                )
                            )
                        }
                    )
                } else {     // collections found
                    Text(
                        text = stringResource(id = R.string.sync_davx5_installed_with_collections_found),
                        modifier = Modifier.padding(top = 16.dp),
                        style = Typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    TextButton(
                        content = {
                            Text(
                                text = stringResource(id = R.string.link_jtx_sync),
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        },
                        modifier = Modifier.padding(top = 8.dp),
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(context.getString(R.string.link_jtx_sync))
                                )
                            )
                        }
                    )
                    Button(
                        onClick = { goToCollections() },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(stringResource(id = R.string.sync_button_go_to_collections))
                    }

                }

                Button(
                    onClick = { SyncUtil.openDAVx5LoginActivity(context) },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(stringResource(id = R.string.sync_button_add_account_in_davx5))
                }
            } else {       // DAVx5 was not found
                Text(
                    text = stringResource(id = R.string.sync_check_out_davx5),
                    style = Typography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = stringResource(id = R.string.sync_davx5_not_found),
                    modifier = Modifier.padding(top = 16.dp),
                    style = Typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                TextButton(
                    content = {
                        Text(
                            text = stringResource(id = R.string.link_jtx_sync),
                            style = Typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(context.getString(R.string.link_jtx_sync))
                            )
                        )
                    }
                )
                Text(
                    text = stringResource(id = R.string.sync_davx5_get_davx5_on),
                    modifier = Modifier.padding(top = 16.dp),
                    style = Typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = { SyncUtil.openDAVx5inPlayStore(context) }) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_play),
                        contentDescription = "Google Play",
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .size(200.dp, 70.dp)
                            .clip(jtxCardCornerShape)
                    )
                }

                Text(
                    text = stringResource(id = R.string.sync_furhter_info_davx5),
                    modifier = Modifier.padding(top = 16.dp),
                    style = Typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                TextButton(
                    content = {
                        Text(
                            text = stringResource(id = R.string.link_davx5),
                            style = Typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(context.getString(R.string.link_davx5))
                            )
                        )
                    }
                )

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
            isDAVx5available = false,
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
            isDAVx5available = true,
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
            isDAVx5available = true,
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