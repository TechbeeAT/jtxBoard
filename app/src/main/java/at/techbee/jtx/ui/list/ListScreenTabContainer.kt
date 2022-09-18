/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.screens


import android.app.Application
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.MainActivity2.Companion.BUILD_FLAVOR_GOOGLEPLAY
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.ListViewModelJournals
import at.techbee.jtx.ui.ListViewModelNotes
import at.techbee.jtx.ui.ListViewModelTodos
import at.techbee.jtx.ui.ViewMode
import at.techbee.jtx.ui.reusable.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.reusable.appbars.JtxTopAppBar
import at.techbee.jtx.ui.reusable.destinations.ListTabDestination
import at.techbee.jtx.ui.reusable.dialogs.DeleteVisibleDialog
import at.techbee.jtx.ui.reusable.dialogs.QuickAddDialog
import at.techbee.jtx.ui.reusable.elements.RadiobuttonWithText
import at.techbee.jtx.ui.GlobalStateHolder
import at.techbee.jtx.util.SyncUtil


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreenTabContainer(
    navController: NavHostController,
    globalStateHolder: GlobalStateHolder
) {

    val context = LocalContext.current
    val screens =
        listOf(ListTabDestination.Journals, ListTabDestination.Notes, ListTabDestination.Tasks)
    val destinationSaver = Saver<ListTabDestination, Int>(
        save = { it.tabIndex },
        restore = { tabIndex ->
            screens.find { it.tabIndex == tabIndex } ?: ListTabDestination.Journals
        }
    )
    var selectedTab by rememberSaveable(stateSaver = destinationSaver) {
        mutableStateOf(
            ListTabDestination.Journals
        )
    }
    var topBarMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteAllVisibleDialog by remember { mutableStateOf(false) }


    val icalListViewModelJournals: ListViewModelJournals = viewModel()
    val icalListViewModelNotes: ListViewModelNotes = viewModel()
    val icalListViewModelTodos: ListViewModelTodos = viewModel()

    fun getActiveViewModel() =
        when (selectedTab.module) {
            Module.JOURNAL -> icalListViewModelJournals
            Module.NOTE -> icalListViewModelNotes
            Module.TODO -> icalListViewModelTodos
        }

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    if (showDeleteAllVisibleDialog) {
        DeleteVisibleDialog(
            numEntriesToDelete = getActiveViewModel().iCal4List.value?.size ?: 0,
            onConfirm = { getActiveViewModel().deleteVisible() },
            onDismiss = { showDeleteAllVisibleDialog = false }
        )
    }

    if (globalStateHolder.icalFromIntentString.value != null || globalStateHolder.icalFromIntentAttachment.value != null) {
        val allCollections = getActiveViewModel().allCollections.observeAsState(emptyList())
        QuickAddDialog(
            presetModule = globalStateHolder.icalFromIntentModule.value,
            presetText = globalStateHolder.icalFromIntentString.value ?: "",
            presetAttachment = globalStateHolder.icalFromIntentAttachment.value,
            allCollections = allCollections.value,
            onEntrySaved = { newICalObject, categories, attachment, editAfterSaving ->
                getActiveViewModel().insertQuickItem(newICalObject, categories, attachment)
                /*  //TODO
            if(AdManager.getInstance()?.isAdFlavor() == true && BillingManager.getInstance()?.isProPurchased?.value == false)
                AdManager.getInstance()?.showInterstitialAd(requireActivity())     // don't forget to show an ad if applicable ;-)
             */
                if (editAfterSaving)
                    TODO("Not implemented")
            },
            onDismiss = {
                globalStateHolder.icalFromIntentString.value = null
                globalStateHolder.icalFromIntentAttachment.value = null
            }
        )
    }


    Scaffold(
        topBar = {
            JtxTopAppBar(
                drawerState = drawerState,
                title = stringResource(id = R.string.navigation_drawer_board),
                subtitle = when(selectedTab.module) {
                    Module.JOURNAL -> stringResource(id = R.string.toolbar_text_jtx_board_journals_overview)
                    Module.NOTE -> stringResource(id = R.string.toolbar_text_jtx_board_notes_overview)
                    Module.TODO -> stringResource(id = R.string.toolbar_text_jtx_board_tasks_overview)
                },
                actions = {
                    IconButton(onClick = { topBarMenuExpanded = true }) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = stringResource(id = R.string.more)
                        )
                    }

                    DropdownMenu(
                        expanded = topBarMenuExpanded,
                        onDismissRequest = { topBarMenuExpanded = false }
                    ) {

                        if(SyncUtil.isDAVx5CompatibleWithJTX(context.applicationContext as Application)) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(id = R.string.sync_now)
                                    )
                                },
                                leadingIcon = { Icon(Icons.Outlined.Sync, null) },
                                onClick = {
                                    SyncUtil.syncAllAccounts(context)
                                    topBarMenuExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(id = R.string.menu_list_delete_visible)
                                )
                            },
                            leadingIcon = { Icon(Icons.Outlined.DeleteOutline, null) },
                            onClick = {
                                showDeleteAllVisibleDialog = true
                                topBarMenuExpanded = false
                            }
                        )
                        Divider()
                        ViewMode.values().forEach { viewMode ->
                            RadiobuttonWithText(
                                text = stringResource(id = viewMode.stringResource),
                                isSelected = getActiveViewModel().listSettings.viewMode.value == viewMode,
                                onClick = {
                                    if ((BuildConfig.FLAVOR == BUILD_FLAVOR_GOOGLEPLAY && BillingManager.getInstance().isProPurchased.value == false)) {
                                        Toast.makeText(context, R.string.buypro_snackbar_please_purchase_pro, Toast.LENGTH_LONG).show()
                                    } else {
                                        getActiveViewModel().listSettings.viewMode.value = viewMode
                                        getActiveViewModel().listSettings.save()
                                    }
                                })
                        }

                        // TODO TBC
                    }
                }
            )
        },
        content = { paddingValues ->
                JtxNavigationDrawer(
                    drawerState,
                    mainContent = {
                        Column {
                            TabRow(selectedTabIndex = selectedTab.tabIndex) {
                                screens.forEach { screen ->
                                    Tab(selected = selectedTab == screen,
                                        onClick = {
                                            selectedTab = screen
                                        },
                                        text = { Text(stringResource(id = screen.titleResource)) })
                                }
                            }

                            Box {
                                Crossfade(targetState = selectedTab) { tab ->
                                    when (tab) {
                                        ListTabDestination.Journals -> {
                                            ListScreen(
                                                listViewModel = icalListViewModelJournals,
                                                navController = navController
                                            )
                                        }
                                        ListTabDestination.Notes -> {
                                            ListScreen(
                                                listViewModel = icalListViewModelNotes,
                                                navController = navController
                                            )
                                        }
                                        ListTabDestination.Tasks -> {
                                            ListScreen(
                                                listViewModel = icalListViewModelTodos,
                                                navController = navController
                                            )
                                        }
                                    }
                                }

                                if(globalStateHolder.isSyncInProgress.value) {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    },
                    navController = navController,
                    paddingValues = paddingValues
                )
        }
    )
}
