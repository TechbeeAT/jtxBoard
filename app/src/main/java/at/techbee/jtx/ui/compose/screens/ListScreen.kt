package at.techbee.jtx.ui.compose.screens


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.navigation.NavController
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.ui.IcalListViewModel
import at.techbee.jtx.ui.ViewMode
import at.techbee.jtx.ui.compose.appbars.ListBottomAppBar
import at.techbee.jtx.ui.compose.bottomsheets.FilterBottomSheet
import at.techbee.jtx.ui.compose.bottomsheets.SearchTextBottomSheet
import at.techbee.jtx.ui.compose.destinations.DetailDestination
import at.techbee.jtx.ui.compose.dialogs.QuickAddDialog
import at.techbee.jtx.ui.compose.stateholder.SettingsStateHolder
import kotlinx.coroutines.launch


@OptIn(
    ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun ListScreen(
    icalListViewModel: IcalListViewModel,
    navController: NavController
) {

    val filterBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val searchTextBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val settingsStateHolder = SettingsStateHolder(context)
    val keyboardController = LocalSoftwareKeyboardController.current
    if (!searchTextBottomSheetState.isVisible)         // hide keyboard when bottom sheet is closed (ensure proper back pressed handling)
        keyboardController?.hide()
    val focusRequesterSearchText = remember { FocusRequester() }


    var showQuickAddDialog by remember { mutableStateOf(false) }
    val allCollections = icalListViewModel.allCollections.observeAsState(emptyList())
    if (showQuickAddDialog) {
        QuickAddDialog(
            presetModule = icalListViewModel.module,
            allCollections = allCollections.value,
            onEntrySaved = { newICalObject, categories, attachment, editAfterSaving ->
                icalListViewModel.insertQuickItem(newICalObject, categories, attachment)
                /*  //TODO
            if(AdManager.getInstance()?.isAdFlavor() == true && BillingManager.getInstance()?.isProPurchased?.value == false)
                AdManager.getInstance()?.showInterstitialAd(requireActivity())     // don't forget to show an ad if applicable ;-)
             */
                if (editAfterSaving)
                    TODO("Not implemented")
            },
            onDismiss = { showQuickAddDialog = false }
        )
    }



    Scaffold(
        bottomBar = {
            ListBottomAppBar(
                module = icalListViewModel.module,
                iCal4ListLive = icalListViewModel.iCal4List,
                onAddNewEntry = {
                    coroutineScope.launch {
                        val db = ICalDatabase.getInstance(context).iCalDatabaseDao
                        val newICalObject = when(icalListViewModel.module) {
                            Module.JOURNAL -> ICalObject.createJournal()
                            Module.NOTE -> ICalObject.createNote()
                            Module.TODO -> ICalObject.createTodo().apply {
                                this.setDefaultDueDateFromSettings(context)
                                this.setDefaultStartDateFromSettings(context)
                            }
                        }
                        newICalObject.dirty = false
                        val newIcalObjectId = db.insertICalObject(newICalObject)
                        navController.navigate("details/$newIcalObjectId?isEditMode=true")
                    }
                },
                onAddNewQuickEntry = { showQuickAddDialog = true },
                listSettings = icalListViewModel.listSettings,
                onListSettingsChanged = { icalListViewModel.updateSearch(saveListSettings = true) },
                onFilterIconClicked = {
                    coroutineScope.launch {
                        filterBottomSheetState.show()
                    }
                },
                onClearFilterClicked = {
                    icalListViewModel.clearFilter()
                },
                onGoToDateSelected = { id -> icalListViewModel.scrollOnceId.postValue(id) },
                onSearchTextClicked = {
                    coroutineScope.launch {
                        searchTextBottomSheetState.show()
                        focusRequesterSearchText.requestFocus()
                    }
                }
            )
        }, content = {  paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                when (icalListViewModel.listSettings.viewMode.value) {
                    ViewMode.LIST -> {
                        ListScreenList(
                            listLive = icalListViewModel.iCal4List,
                            subtasksLive = icalListViewModel.allSubtasksMap,
                            subnotesLive = icalListViewModel.allSubnotesMap,
                            attachmentsLive = icalListViewModel.allAttachmentsMap,
                            scrollOnceId = icalListViewModel.scrollOnceId,
                            listSettings = icalListViewModel.listSettings,
                            isSubtasksExpandedDefault = settingsStateHolder.settingAutoExpandSubtasks,
                            isSubnotesExpandedDefault = settingsStateHolder.settingAutoExpandSubnotes,
                            isAttachmentsExpandedDefault = settingsStateHolder.settingAutoExpandAttachments,
                            settingShowProgressMaintasks = settingsStateHolder.settingShowProgressForMainTasks,
                            settingShowProgressSubtasks = settingsStateHolder.settingShowProgressForSubTasks,
                            settingProgressIncrement = settingsStateHolder.settingStepForProgress,
                            goToView = { itemId -> navController.navigate(DetailDestination.Detail.getRoute(itemId, false)) },
                            goToEdit = { itemId -> navController.navigate(DetailDestination.Detail.getRoute(itemId, true)) },
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
                            goToView = { itemId -> navController.navigate(DetailDestination.Detail.getRoute(itemId, false)) },
                            goToEdit = { itemId -> navController.navigate(DetailDestination.Detail.getRoute(itemId, true)) },
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
                            goToView = { itemId -> navController.navigate(DetailDestination.Detail.getRoute(itemId, false)) },
                            goToEdit = { itemId -> navController.navigate(DetailDestination.Detail.getRoute(itemId, true)) },
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
                            goToView = { itemId -> navController.navigate(DetailDestination.Detail.getRoute(itemId, false)) },
                            goToEdit = { itemId -> navController.navigate(DetailDestination.Detail.getRoute(itemId, true)) },
                        )
                    }
                }
            }
        }
    )

    ModalBottomSheetLayout(
        sheetState = filterBottomSheetState,
        sheetContent = {
            FilterBottomSheet(
                module = icalListViewModel.module,
                listSettings = icalListViewModel.listSettings,
                allCollectionsLive = icalListViewModel.allCollections,
                allCategoriesLive = icalListViewModel.allCategories,
                onListSettingsChanged = { icalListViewModel.updateSearch(saveListSettings = true) }
            )
        }
    ) {}

    ModalBottomSheetLayout(
        sheetState = searchTextBottomSheetState,
        sheetContent = {
            SearchTextBottomSheet(
                initialSeachText = icalListViewModel.listSettings.searchText.value,
                onSearchTextChanged = { newSearchText ->
                    icalListViewModel.listSettings.searchText.value = newSearchText
                    icalListViewModel.updateSearch(saveListSettings = false)
                },
                focusRequester = focusRequesterSearchText
            )
        }
    ) {}

}


