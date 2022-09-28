/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list


import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.MainActivity2.Companion.BUILD_FLAVOR_GOOGLEPLAY
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.GlobalStateHolder
import at.techbee.jtx.ui.reusable.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.reusable.appbars.JtxTopAppBar
import at.techbee.jtx.ui.reusable.dialogs.DeleteVisibleDialog
import at.techbee.jtx.ui.reusable.dialogs.ErrorOnUpdateDialog
import at.techbee.jtx.ui.reusable.dialogs.QuickAddDialog
import at.techbee.jtx.ui.reusable.elements.RadiobuttonWithText
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.util.SyncUtil
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch


@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalPagerApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
)
@Composable
fun ListScreenTabContainer(
    navController: NavHostController,
    globalStateHolder: GlobalStateHolder,
    settingsStateHolder: SettingsStateHolder
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val screens = listOf(ListTabDestination.Journals, ListTabDestination.Notes, ListTabDestination.Tasks)
    val pagerState = rememberPagerState(
        initialPage = when(settingsStateHolder.lastUsedModule.value) {
            Module.JOURNAL -> ListTabDestination.Journals.tabIndex
            Module.NOTE -> ListTabDestination.Notes.tabIndex
            Module.TODO -> ListTabDestination.Tasks.tabIndex
        }
    )

    var topBarMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteAllVisibleDialog by remember { mutableStateOf(false) }
    var showQuickAddDialog by remember { mutableStateOf(false) }

    val icalListViewModelJournals: ListViewModelJournals = viewModel()
    val icalListViewModelNotes: ListViewModelNotes = viewModel()
    val icalListViewModelTodos: ListViewModelTodos = viewModel()

    fun getActiveViewModel() =
        when (pagerState.currentPage) {
            ListTabDestination.Journals.tabIndex -> icalListViewModelJournals
            ListTabDestination.Notes.tabIndex -> icalListViewModelNotes
            ListTabDestination.Tasks.tabIndex  -> icalListViewModelTodos
            else -> icalListViewModelJournals
        }

    val goToEdit = getActiveViewModel().goToEdit.observeAsState()
    goToEdit.value?.let { icalObjectId ->
        getActiveViewModel().goToEdit.value = null
        navController.navigate("details/$icalObjectId?isEditMode=true")
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val filterBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    var showSearch by remember { mutableStateOf(false) }

    if (showDeleteAllVisibleDialog) {
        DeleteVisibleDialog(
            numEntriesToDelete = getActiveViewModel().iCal4List.value?.size ?: 0,
            onConfirm = { getActiveViewModel().deleteVisible() },
            onDismiss = { showDeleteAllVisibleDialog = false }
        )
    }

    // origin can be button click or an import through the intent
    if (showQuickAddDialog || globalStateHolder.icalFromIntentString.value != null || globalStateHolder.icalFromIntentAttachment.value != null) {
        val allCollections = getActiveViewModel().allCollections.observeAsState(emptyList())
        QuickAddDialog(
            presetModule = if(showQuickAddDialog)
                getActiveViewModel().module    // coming from button
            else
                globalStateHolder.icalFromIntentModule.value,   // coming from intent
            presetText = globalStateHolder.icalFromIntentString.value ?: "",    // only relevant when coming from intent
            presetAttachment = globalStateHolder.icalFromIntentAttachment.value,    // only relevant when coming from intent
            allCollections = allCollections.value,
            presetCollectionId = settingsStateHolder.lastUsedCollection.value,
            presetSaveAndEdit = settingsStateHolder.saveAndEdit.value,
            onSaveEntry = { newICalObject, categories, attachment, editAfterSaving ->
                settingsStateHolder.lastUsedCollection.value = newICalObject.collectionId
                settingsStateHolder.lastUsedCollection = settingsStateHolder.lastUsedCollection
                settingsStateHolder.lastUsedModule.value = newICalObject.getModuleFromString()
                settingsStateHolder.lastUsedModule = settingsStateHolder.lastUsedModule
                scope.launch {
                    pagerState.scrollToPage(
                        when(newICalObject.getModuleFromString()) {
                            Module.JOURNAL -> ListTabDestination.Journals.tabIndex
                            Module.NOTE -> ListTabDestination.Notes.tabIndex
                            Module.TODO -> ListTabDestination.Tasks.tabIndex
                        }
                    )
                    getActiveViewModel().insertQuickItem(newICalObject, categories, attachment, editAfterSaving)
                }
            },
            onDismiss = {
                showQuickAddDialog = false  // origin was button
                globalStateHolder.icalFromIntentString.value = null  // origin was state from import
                globalStateHolder.icalFromIntentAttachment.value = null  // origin was state from import
                        },
            onSaveAndEditChanged = {
                settingsStateHolder.saveAndEdit.value = it
                settingsStateHolder.saveAndEdit = settingsStateHolder.saveAndEdit
            }
        )
    }

    if(getActiveViewModel().sqlConstraintException.value) {
        ErrorOnUpdateDialog(onConfirm = { getActiveViewModel().sqlConstraintException.value = false })
    }


    Scaffold(
        topBar = {
            JtxTopAppBar(
                drawerState = drawerState,
                title = stringResource(id = R.string.navigation_drawer_board),
                subtitle = when(pagerState.currentPage) {
                    ListTabDestination.Journals.tabIndex -> stringResource(id = R.string.toolbar_text_jtx_board_journals_overview)
                    ListTabDestination.Notes.tabIndex -> stringResource(id = R.string.toolbar_text_jtx_board_notes_overview)
                    ListTabDestination.Tasks.tabIndex -> stringResource(id = R.string.toolbar_text_jtx_board_tasks_overview)
                    else -> stringResource(id = R.string.toolbar_text_jtx_board_journals_overview)
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
                    }
                }
            )
        },
        bottomBar = {
            val coroutineScope = rememberCoroutineScope()
            val listViewModel = when(pagerState.currentPage) {
                ListTabDestination.Journals.tabIndex -> icalListViewModelJournals
                ListTabDestination.Notes.tabIndex -> icalListViewModelNotes
                ListTabDestination.Tasks.tabIndex -> icalListViewModelTodos
                else -> return@Scaffold
            }
            val keyboardController = LocalSoftwareKeyboardController.current
            val allCollections = listViewModel.allCollections.observeAsState(emptyList())

            ListBottomAppBar(
                module = listViewModel.module,
                iCal4ListLive = listViewModel.iCal4List,
                onAddNewEntry = {
                    coroutineScope.launch {
                        val lastUsedCollectionId = settingsStateHolder.lastUsedCollection.value
                        val proposedCollectionId = if(allCollections.value.any {collection -> collection.collectionId == lastUsedCollectionId })
                            lastUsedCollectionId
                        else
                            allCollections.value.firstOrNull()?.collectionId ?: return@launch
                        val db = ICalDatabase.getInstance(context).iCalDatabaseDao
                        val newICalObject = when(listViewModel.module) {
                            Module.JOURNAL -> ICalObject.createJournal().apply { collectionId = proposedCollectionId }
                            Module.NOTE -> ICalObject.createNote().apply { collectionId = proposedCollectionId }
                            Module.TODO -> ICalObject.createTodo().apply {
                                this.setDefaultDueDateFromSettings(context)
                                this.setDefaultStartDateFromSettings(context)
                                collectionId = proposedCollectionId
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
                        if(!showSearch) {
                            showSearch = true
                            listViewModel.listSettings.searchText.value = ""
                            keyboardController?.show()
                            //focusRequesterSearchText.requestFocus()
                        } else {
                            showSearch = false
                            keyboardController?.hide()
                            listViewModel.listSettings.searchText.value = null  // null removes color indicator for active search
                            listViewModel.updateSearch(saveListSettings = false)
                        }
                    }
                }
            )
        },
        content = { paddingValues ->
                JtxNavigationDrawer(
                    drawerState,
                    mainContent = {
                        Column {
                            TabRow(
                                selectedTabIndex = pagerState.currentPage    // adding the indicator might make a smooth movement of the tabIndicator, but Accompanist does not support all components (TODO: Check again in future) https://www.geeksforgeeks.org/tab-layout-in-android-using-jetpack-compose/
                            ) {
                                screens.forEach { screen ->
                                    Tab(selected = pagerState.currentPage == screen.tabIndex,
                                        onClick = {
                                            scope.launch {
                                                pagerState.scrollToPage(screen.tabIndex)
                                            }
                                            settingsStateHolder.lastUsedModule.value = when(screen) {
                                                ListTabDestination.Journals -> Module.JOURNAL
                                                ListTabDestination.Notes -> Module.NOTE
                                                ListTabDestination.Tasks -> Module.TODO
                                            }
                                            settingsStateHolder.lastUsedModule = settingsStateHolder.lastUsedModule  // in order to save
                                        },
                                        text = { Text(stringResource(id = screen.titleResource)) })
                                }
                            }

                            Box {
                                HorizontalPager(state = pagerState, count = 3) { page ->
                                    when (page) {
                                        ListTabDestination.Journals.tabIndex -> {
                                            ListScreen(
                                                listViewModel = icalListViewModelJournals,
                                                navController = navController,
                                                showSearch = showSearch,
                                                filterBottomSheetState = filterBottomSheetState,
                                            )
                                        }
                                        ListTabDestination.Notes.tabIndex -> {
                                            ListScreen(
                                                listViewModel = icalListViewModelNotes,
                                                navController = navController,
                                                showSearch = showSearch,
                                                filterBottomSheetState = filterBottomSheetState,
                                            )
                                        }
                                        ListTabDestination.Tasks.tabIndex -> {
                                            ListScreen(
                                                listViewModel = icalListViewModelTodos,
                                                navController = navController,
                                                showSearch = showSearch,
                                                filterBottomSheetState = filterBottomSheetState,
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
