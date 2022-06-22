package at.techbee.jtx.ui.compose.screens


import androidx.compose.runtime.*
import androidx.navigation.NavController
import at.techbee.jtx.ui.IcalListFragmentDirections
import at.techbee.jtx.ui.IcalListViewModel
import at.techbee.jtx.ui.ViewMode


@Composable
fun ListScreen(
    icalListViewModel: IcalListViewModel,
    navController: NavController
) {

    when (icalListViewModel.listSettings.viewMode.value) {
        ViewMode.LIST -> {
            ListScreenList(
                listLive = icalListViewModel.iCal4List,
                subtasksLive = icalListViewModel.allSubtasks,
                subnotesLive = icalListViewModel.allSubnotes,
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
                onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance -> icalListViewModel.updateProgress(itemId, newPercent, isLinkedRecurringInstance)  },
                onExpandedChanged = { itemId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isAttachmentsExpanded: Boolean -> icalListViewModel.updateExpanded(itemId, isSubtasksExpanded, isSubnotesExpanded, isAttachmentsExpanded)},
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
                subtasksLive = icalListViewModel.allSubtasks,
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
                    icalListViewModel.updateStatusJournal(itemId, newStatus, isLinkedRecurringInstance, scrollOnce)
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
}


