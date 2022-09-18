/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.screens


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
import at.techbee.jtx.ui.ListViewModel
import at.techbee.jtx.ui.ViewMode
import at.techbee.jtx.ui.reusable.appbars.ListBottomAppBar
import at.techbee.jtx.ui.reusable.bottomsheets.FilterBottomSheet
import at.techbee.jtx.ui.reusable.bottomsheets.SearchTextBottomSheet
import at.techbee.jtx.ui.reusable.destinations.DetailDestination
import at.techbee.jtx.ui.reusable.dialogs.QuickAddDialog
import at.techbee.jtx.ui.settings.SettingsStateHolder
import kotlinx.coroutines.launch


@OptIn(
    ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun ListScreen(
    listViewModel: ListViewModel,
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
    val allCollections = listViewModel.allCollections.observeAsState(emptyList())
    if (showQuickAddDialog) {
        QuickAddDialog(
            presetModule = listViewModel.module,
            allCollections = allCollections.value,
            onEntrySaved = { newICalObject, categories, attachment, editAfterSaving ->
                listViewModel.insertQuickItem(newICalObject, categories, attachment)
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
                module = listViewModel.module,
                iCal4ListLive = listViewModel.iCal4List,
                onAddNewEntry = {
                    coroutineScope.launch {
                        val db = ICalDatabase.getInstance(context).iCalDatabaseDao
                        val newICalObject = when(listViewModel.module) {
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
                listSettings = listViewModel.listSettings,
                onListSettingsChanged = { listViewModel.updateSearch(saveListSettings = true) },
                onFilterIconClicked = {
                    coroutineScope.launch {
                        filterBottomSheetState.show()
                    }
                },
                onClearFilterClicked = {
                    listViewModel.clearFilter()
                },
                onGoToDateSelected = { id -> listViewModel.scrollOnceId.postValue(id) },
                onSearchTextClicked = {
                    coroutineScope.launch {
                        searchTextBottomSheetState.show()
                        focusRequesterSearchText.requestFocus()
                    }
                }
            )
        }, content = {  paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                when (listViewModel.listSettings.viewMode.value) {
                    ViewMode.LIST -> {
                        ListScreenList(
                            listLive = listViewModel.iCal4List,
                            subtasksLive = listViewModel.allSubtasksMap,
                            subnotesLive = listViewModel.allSubnotesMap,
                            attachmentsLive = listViewModel.allAttachmentsMap,
                            scrollOnceId = listViewModel.scrollOnceId,
                            listSettings = listViewModel.listSettings,
                            isSubtasksExpandedDefault = settingsStateHolder.settingAutoExpandSubtasks,
                            isSubnotesExpandedDefault = settingsStateHolder.settingAutoExpandSubnotes,
                            isAttachmentsExpandedDefault = settingsStateHolder.settingAutoExpandAttachments,
                            settingShowProgressMaintasks = settingsStateHolder.settingShowProgressForMainTasks,
                            settingShowProgressSubtasks = settingsStateHolder.settingShowProgressForSubTasks,
                            settingProgressIncrement = settingsStateHolder.settingStepForProgress,
                            goToView = { itemId -> navController.navigate(DetailDestination.Detail.getRoute(itemId, false)) },
                            goToEdit = { itemId -> navController.navigate(DetailDestination.Detail.getRoute(itemId, true)) },
                            onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance ->
                                listViewModel.updateProgress(
                                    itemId,
                                    newPercent,
                                    isLinkedRecurringInstance
                                )
                            },
                            onExpandedChanged = { itemId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isAttachmentsExpanded: Boolean ->
                                listViewModel.updateExpanded(
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
                            listLive = listViewModel.iCal4List,
                            scrollOnceId = listViewModel.scrollOnceId,
                            onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance ->
                                listViewModel.updateProgress(
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
                            listLive = listViewModel.iCal4List,
                            subtasksLive = listViewModel.allSubtasksMap,
                            scrollOnceId = listViewModel.scrollOnceId,
                            listSettings = listViewModel.listSettings,
                            onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance ->
                                listViewModel.updateProgress(
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
                            module = listViewModel.module,
                            listLive = listViewModel.iCal4List,
                            scrollOnceId = listViewModel.scrollOnceId,
                            onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance, scrollOnce ->
                                listViewModel.updateProgress(
                                    itemId,
                                    newPercent,
                                    isLinkedRecurringInstance,
                                    scrollOnce
                                )
                            },
                            onStatusChanged = { itemId, newStatus, isLinkedRecurringInstance, scrollOnce ->
                                listViewModel.updateStatusJournal(
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
                module = listViewModel.module,
                listSettings = listViewModel.listSettings,
                allCollectionsLive = listViewModel.allCollections,
                allCategoriesLive = listViewModel.allCategories,
                onListSettingsChanged = { listViewModel.updateSearch(saveListSettings = true) }
            )
        }
    ) {}

    ModalBottomSheetLayout(
        sheetState = searchTextBottomSheetState,
        sheetContent = {
            SearchTextBottomSheet(
                initialSeachText = listViewModel.listSettings.searchText.value,
                onSearchTextChanged = { newSearchText ->
                    listViewModel.listSettings.searchText.value = newSearchText
                    listViewModel.updateSearch(saveListSettings = false)
                },
                focusRequester = focusRequesterSearchText
            )
        }
    ) {}

}


