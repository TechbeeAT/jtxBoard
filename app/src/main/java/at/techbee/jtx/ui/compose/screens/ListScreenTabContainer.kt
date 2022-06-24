package at.techbee.jtx.ui.compose.screens


import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreenTabContainer(navController: NavHostController) {


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

    Scaffold(
        topBar = { JtxTopAppBar(
            drawerState = drawerState, 
            title = stringResource(id = R.string.navigation_drawer_board)
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


