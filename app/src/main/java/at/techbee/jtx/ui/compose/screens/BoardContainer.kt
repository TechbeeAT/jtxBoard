package at.techbee.jtx.ui.compose.screens


import android.app.Application
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import at.techbee.jtx.database.Module
import at.techbee.jtx.ui.IcalListFragmentDirections
import at.techbee.jtx.ui.IcalListViewModel
import at.techbee.jtx.ui.ViewMode
import at.techbee.jtx.ui.compose.appbars.ListBottomAppBar
import at.techbee.jtx.ui.compose.destinations.ListTabDestination
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BoardContainer() {

    val application = LocalContext.current.applicationContext as Application

    val modelJournals = IcalListViewModel(application, Module.JOURNAL)
    val modelNotes = IcalListViewModel(application, Module.NOTE)
    val modelTasks = IcalListViewModel(application, Module.TODO)

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
    val modelSaver = Saver<IcalListViewModel, String>(
        save = { it.module.name },
        restore = { module ->
            when (module) {
                ListTabDestination.Journals.module.name -> modelJournals
                ListTabDestination.Notes.module.name -> modelNotes
                ListTabDestination.Tasks.module.name -> modelTasks
                else -> modelJournals
            }
        }
    )
    var activeViewModel by rememberSaveable(stateSaver = modelSaver) { mutableStateOf(modelJournals) }
    val filterBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            ListBottomAppBar(
                module = selectedTab.module,
                onAddNewEntry = { newEntry ->  /* findNavController().navigate(IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(newEntry)) */   /* TODO */ },
                listSettings = activeViewModel.listSettings,
                onListSettingsChanged = { activeViewModel.updateSearch(saveListSettings = true) },
                onFilterIconClicked = {
                    coroutineScope.launch {
                        filterBottomSheetState.show()
                    }
                }
            )
        },
        content = {
            Column {
                TabRow(selectedTabIndex = selectedTab.tabIndex) {
                    screens.forEach { screen ->
                        Tab(selected = selectedTab == screen,
                            onClick = {
                                selectedTab = screen
                                activeViewModel = when (selectedTab) {
                                    ListTabDestination.Journals -> modelJournals
                                    ListTabDestination.Notes -> modelNotes
                                    ListTabDestination.Tasks -> modelTasks
                                }
                            },
                            text = { Text(stringResource(id = screen.titleResource)) })
                    }
                }

                Crossfade(targetState = selectedTab) {
                    when (it) {
                        ListTabDestination.Journals -> ListScreen(
                            icalListViewModel = activeViewModel,
                            navController = rememberNavController()    // TODO!!!!!
                        )
                        ListTabDestination.Notes -> ListScreen(
                            icalListViewModel = activeViewModel,
                            navController = rememberNavController()    // TODO!!!!!
                        )
                        ListTabDestination.Tasks -> ListScreen(
                            icalListViewModel = activeViewModel,
                            navController = rememberNavController()    // TODO!!!!!
                        )
                    }
                }
            }



            ModalBottomSheetLayout(
                sheetState = filterBottomSheetState,
                sheetContent = {
                    FilterScreen(
                        module = selectedTab.module,
                        listSettings = activeViewModel.listSettings,
                        allCollectionsLive = activeViewModel.allCollections,
                        allCategoriesLive = activeViewModel.allCategories,
                        onListSettingsChanged = { activeViewModel.updateSearch(saveListSettings = true) }
                    )
                }
            ) {}

        }
    )
}


