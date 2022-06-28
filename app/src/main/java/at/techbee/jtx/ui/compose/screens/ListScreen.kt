package at.techbee.jtx.ui.compose.screens


import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.navigation.NavController
import at.techbee.jtx.ui.IcalListFragmentDirections
import at.techbee.jtx.ui.IcalListViewModel
import at.techbee.jtx.ui.ViewMode
import at.techbee.jtx.ui.compose.appbars.ListBottomAppBar
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    icalListViewModel: IcalListViewModel,
    navController: NavController
) {

    val filterBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            ListBottomAppBar(
                module = icalListViewModel.module,
                onAddNewEntry = { newEntry ->  /* findNavController().navigate(IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(newEntry)) */   /* TODO */ },
                listSettings = icalListViewModel.listSettings,
                onListSettingsChanged = { icalListViewModel.updateSearch(saveListSettings = true) },
                onFilterIconClicked = {
                    coroutineScope.launch {
                        filterBottomSheetState.show()
                    }
                }
            )
        }) {


        when (icalListViewModel.listSettings.viewMode.value) {
            ViewMode.LIST -> {
                ListScreenList(
                    listLive = icalListViewModel.iCal4List,
                    subtasksLive = icalListViewModel.allSubtasksMap,
                    subnotesLive = icalListViewModel.allSubnotesMap,
                    attachmentsLive = icalListViewModel.allAttachmentsMap,
                    scrollOnceId = icalListViewModel.scrollOnceId,
                    listSettings = icalListViewModel.listSettings,
                    goToView = { itemId ->
                        navController.navigate(
                            IcalListFragmentDirections
                                .actionIcalListFragmentToIcalViewFragment()
                                .setItem2show(itemId)
                        )
                    },
                    goToEdit = { itemId ->
                        icalListViewModel.postDirectEditEntity(
                            itemId
                        )
                    },
                    onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance ->
                        icalListViewModel.updateProgress(
                            itemId,
                            newPercent,
                            isLinkedRecurringInstance
                        )
                    },
                    onExpandedChanged = { itemId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isAttachmentsExpanded: Boolean ->
                        icalListViewModel.updateExpanded(
                            itemId,
                            isSubtasksExpanded,
                            isSubnotesExpanded,
                            isAttachmentsExpanded
                        )
                    },
                )
            }
            ViewMode.GRID -> {
                ListScreenGrid(
                    listLive = icalListViewModel.iCal4List,
                    scrollOnceId = icalListViewModel.scrollOnceId,
                    onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance ->
                        icalListViewModel.updateProgress(
                            itemId,
                            newPercent,
                            isLinkedRecurringInstance
                        )
                    },
                    goToView = { itemId ->
                        navController.navigate(
                            IcalListFragmentDirections
                                .actionIcalListFragmentToIcalViewFragment()
                                .setItem2show(itemId)
                        )
                    },
                    goToEdit = { itemId ->
                        icalListViewModel.postDirectEditEntity(
                            itemId
                        )
                    }
                )
            }
            ViewMode.COMPACT -> {
                ListScreenCompact(
                    listLive = icalListViewModel.iCal4List,
                    subtasksLive = icalListViewModel.allSubtasksMap,
                    scrollOnceId = icalListViewModel.scrollOnceId,
                    listSettings = icalListViewModel.listSettings,
                    onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance ->
                        icalListViewModel.updateProgress(
                            itemId,
                            newPercent,
                            isLinkedRecurringInstance
                        )
                    },
                    goToView = { itemId ->
                        navController.navigate(
                            IcalListFragmentDirections
                                .actionIcalListFragmentToIcalViewFragment()
                                .setItem2show(itemId)
                        )
                    },
                    goToEdit = { itemId ->
                        icalListViewModel.postDirectEditEntity(
                            itemId
                        )
                    }
                )
            }
            ViewMode.KANBAN -> {
                ListScreenKanban(
                    module = icalListViewModel.module,
                    listLive = icalListViewModel.iCal4List,
                    scrollOnceId = icalListViewModel.scrollOnceId,
                    onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance, scrollOnce ->
                        icalListViewModel.updateProgress(
                            itemId,
                            newPercent,
                            isLinkedRecurringInstance,
                            scrollOnce
                        )
                    },
                    onStatusChanged = { itemId, newStatus, isLinkedRecurringInstance, scrollOnce ->
                        icalListViewModel.updateStatusJournal(
                            itemId,
                            newStatus,
                            isLinkedRecurringInstance,
                            scrollOnce
                        )
                    },
                    goToView = { itemId ->
                        navController.navigate(
                            IcalListFragmentDirections
                                .actionIcalListFragmentToIcalViewFragment()
                                .setItem2show(itemId)
                        )
                    },
                    goToEdit = { itemId ->
                        icalListViewModel.postDirectEditEntity(
                            itemId
                        )
                    }
                )
            }
        }

        ModalBottomSheetLayout(
            sheetState = filterBottomSheetState,
            sheetContent = {
                FilterScreen(
                    module = icalListViewModel.module,
                    listSettings = icalListViewModel.listSettings,
                    allCollectionsLive = icalListViewModel.allCollections,
                    allCategoriesLive = icalListViewModel.allCategories,
                    onListSettingsChanged = { icalListViewModel.updateSearch(saveListSettings = true) }
                )
            }
        ) {}
    }
}


