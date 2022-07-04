package at.techbee.jtx.ui.compose.screens


import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.ui.IcalListViewModelJournals
import at.techbee.jtx.ui.IcalListViewModelNotes
import at.techbee.jtx.ui.IcalListViewModelTodos
import at.techbee.jtx.ui.compose.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.compose.appbars.JtxTopAppBar
import at.techbee.jtx.ui.compose.destinations.ListTabDestination
import at.techbee.jtx.ui.compose.dialogs.DeleteVisibleDialog
import at.techbee.jtx.util.SyncUtil


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreenTabContainer(
    navController: NavHostController
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


    val icalListViewModelJournals: IcalListViewModelJournals = viewModel()
    val icalListViewModelNotes: IcalListViewModelNotes = viewModel()
    val icalListViewModelTodos: IcalListViewModelTodos = viewModel()

    fun getActiveViewModel() =
        when(selectedTab.module) {
            Module.JOURNAL -> icalListViewModelJournals
            Module.NOTE -> icalListViewModelNotes
            Module.TODO -> icalListViewModelTodos
        }

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    if(showDeleteAllVisibleDialog) {
        DeleteVisibleDialog(
            numEntriesToDelete = getActiveViewModel().iCal4List.value?.size?: 0,
            onConfirm = { getActiveViewModel().deleteVisible() },
            onDismiss = { showDeleteAllVisibleDialog = false }
        )
    }


    Scaffold(
        topBar = { JtxTopAppBar(
            drawerState = drawerState, 
            title = stringResource(id = R.string.navigation_drawer_board),
            actions = {
                IconButton(onClick = { topBarMenuExpanded = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(id = R.string.more))
                }


                DropdownMenu(
                    expanded = topBarMenuExpanded,
                    onDismissRequest = { topBarMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(
                            stringResource(id = R.string.sync_now))
                        },
                        leadingIcon = { Icon(Icons.Outlined.Sync, null) },
                        onClick = {
                            SyncUtil.syncAllAccounts(context)
                            topBarMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(
                            stringResource(id = R.string.menu_list_delete_visible))
                        },
                        leadingIcon = { Icon(Icons.Outlined.DeleteOutline, null) },
                        onClick = {
                            showDeleteAllVisibleDialog = true
                            topBarMenuExpanded = false
                        }
                    )

                    // TODO TBC
                }
            }
        ) },
        content = {
            Column {
                JtxNavigationDrawer(drawerState,
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

                            Crossfade(targetState = selectedTab) {
                                ListScreen(
                                    icalListViewModel = getActiveViewModel() ,
                                    navController = navController
                                )
                            }
                        }
                    },
                    navController = navController
                )
            }
        }
    )
}


