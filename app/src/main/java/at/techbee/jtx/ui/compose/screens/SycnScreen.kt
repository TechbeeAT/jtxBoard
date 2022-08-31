/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.screens

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.ui.compose.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.compose.appbars.JtxTopAppBar
import at.techbee.jtx.ui.compose.destinations.NavigationDrawerDestination
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.ui.theme.Typography
import at.techbee.jtx.util.SyncUtil


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    remoteCollectionsLive: LiveData<List<ICalCollection>>,
    isSyncInProgress: State<Boolean>,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val isDAVx5available =
        SyncUtil.isDAVx5CompatibleWithJTX(context.applicationContext as Application)

    Scaffold(
        topBar = {
            JtxTopAppBar(
                drawerState = drawerState,
                title = stringResource(id = R.string.navigation_drawer_sync),
                actions = {
                    if (isDAVx5available) {
                        IconButton(onClick = { SyncUtil.syncAllAccounts(context) }) {
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
                        remoteCollectionsLive = remoteCollectionsLive,
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SyncScreenContent(
    remoteCollectionsLive: LiveData<List<ICalCollection>>,
    isDAVx5available: Boolean,
    isSyncInProgress: State<Boolean>,
    goToCollections: () -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val remoteCollections by remoteCollectionsLive.observeAsState(emptyList())

    Box {

        AnimatedVisibility(visible = isSyncInProgress.value) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .scrollable(scrollState, orientation = Orientation.Vertical)
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
                                fontWeight = FontWeight.Bold
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
                                fontWeight = FontWeight.Bold
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
                            fontWeight = FontWeight.Bold
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
                Image(
                    painter = painterResource(id = R.drawable.ic_google_play),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .size(200.dp, 70.dp)
                        .combinedClickable(
                            enabled = true,
                            onClickLabel = "Google Play",
                            onClick = { SyncUtil.openDAVx5inPlayStore(context) }
                        )
                )
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
    JtxBoardTheme {
        SyncScreen(
            remoteCollectionsLive = MutableLiveData(emptyList()),
            isSyncInProgress = remember { mutableStateOf(false) },
            navController = rememberNavController(),
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SyncScreenContent_Preview_no_DAVX5() {
    JtxBoardTheme {
        SyncScreenContent(
            isDAVx5available = false,
            isSyncInProgress = remember { mutableStateOf(false) },
            remoteCollectionsLive = MutableLiveData(emptyList()),
            goToCollections = { },
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SyncScreenContent_Preview_DAVx5_no_collections() {
    JtxBoardTheme {
        SyncScreenContent(
            isDAVx5available = true,
            isSyncInProgress = remember { mutableStateOf(false) },
            remoteCollectionsLive = MutableLiveData(
                emptyList()
            ),
            goToCollections = { },
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SyncScreenContent_Preview_DAVx5_with_collections() {
    JtxBoardTheme {
        SyncScreenContent(
            isDAVx5available = true,
            isSyncInProgress = remember { mutableStateOf(true) },
            remoteCollectionsLive = MutableLiveData(
                listOf(
                    ICalCollection().apply { this.collectionId = 1 },
                    ICalCollection().apply { this.collectionId = 2 }
                )
            ),
            goToCollections = { },
        )
    }
}