package at.techbee.jtx.ui.compose.screens


import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import at.techbee.jtx.ui.IcalListFragmentDirections
import at.techbee.jtx.ui.IcalListViewModel
import at.techbee.jtx.ui.compose.appbars.ListBottomAppBar
import at.techbee.jtx.ui.compose.destinations.ListTabDestination
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ListTabContainer(
    icalListViewModelJournal: IcalListViewModel,
    icalListViewModelNote: IcalListViewModel,
    icalListViewModelTodo: IcalListViewModel,
    navController: NavController
) {

    var selectedDestination by remember { mutableStateOf<ListTabDestination>(ListTabDestination.Journals) }
    val screens = listOf(ListTabDestination.Journals, ListTabDestination.Notes, ListTabDestination.Tasks)
    var activeViewModel by remember { mutableStateOf(icalListViewModelJournal) }
    val filterBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = { ListBottomAppBar(
            module = selectedDestination.module,
            onAddNewEntry = { newEntry -> navController.navigate(IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(newEntry)) },
            listSettingsLive = activeViewModel.listSettings,
            onListSettingsChanged = { newListSettings ->
                activeViewModel.listSettings.postValue(newListSettings)
                },
            onFilterIconClicked = {
                coroutineScope.launch {
                    filterBottomSheetState.show()
                }
            }
        ) },
        content = {
            Column {
                TabRow(selectedTabIndex = selectedDestination.tabIndex) {
                    screens.forEach { screen ->
                        Tab(selected = selectedDestination == screen,
                            onClick = {
                                selectedDestination = screen
                                activeViewModel = when(selectedDestination) {
                                    ListTabDestination.Journals -> icalListViewModelJournal
                                    ListTabDestination.Notes -> icalListViewModelNote
                                    ListTabDestination.Tasks -> icalListViewModelTodo
                                }
                            },
                            text = { Text(stringResource(id = screen.titleResource)) })
                    }
                }

                Crossfade(targetState = selectedDestination) {
                    when (it) {
                        ListTabDestination.Journals -> ListScreen(
                            icalListViewModel = activeViewModel,
                            navController = navController
                        )
                        ListTabDestination.Notes -> ListScreen(
                            icalListViewModel = activeViewModel,
                            navController = navController
                        )
                        ListTabDestination.Tasks -> ListScreen(
                            icalListViewModel = activeViewModel,
                            navController = navController
                        )
                    }
                }
            }



            ModalBottomSheetLayout(
                sheetState = filterBottomSheetState,
                sheetContent = {
                    FilterScreen(
                        module = selectedDestination.module,
                        listSettingsLive = activeViewModel.listSettings,
                        allCollectionsLive = activeViewModel.allCollections,
                        allCategoriesLive = activeViewModel.allCategories,
                        onListSettingsChanged = { newListSettings ->
                            activeViewModel.listSettings.postValue(newListSettings)
                        }
                    )
                }
            ) {}

        }
    )
}
