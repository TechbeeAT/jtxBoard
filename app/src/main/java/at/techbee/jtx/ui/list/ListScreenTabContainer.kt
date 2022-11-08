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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.MainActivity2.Companion.BUILD_FLAVOR_GOOGLEPLAY
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.AlarmRelativeTo
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.GlobalStateHolder
import at.techbee.jtx.ui.reusable.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.reusable.appbars.JtxTopAppBar
import at.techbee.jtx.ui.reusable.dialogs.DeleteVisibleDialog
import at.techbee.jtx.ui.reusable.dialogs.ErrorOnUpdateDialog
import at.techbee.jtx.ui.reusable.elements.CheckboxWithText
import at.techbee.jtx.ui.reusable.elements.RadiobuttonWithText
import at.techbee.jtx.ui.settings.DropdownSettingOption
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.util.SyncUtil
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes


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
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val screens = listOf(ListTabDestination.Journals, ListTabDestination.Notes, ListTabDestination.Tasks)
    val pagerState = rememberPagerState(
        initialPage = when(settingsStateHolder.lastUsedModule.value) {
            Module.JOURNAL -> ListTabDestination.Journals.tabIndex
            Module.NOTE -> ListTabDestination.Notes.tabIndex
            Module.TODO -> ListTabDestination.Tasks.tabIndex
        }
    )

    val icalListViewModelJournals: ListViewModelJournals = viewModel()
    val icalListViewModelNotes: ListViewModelNotes = viewModel()
    val icalListViewModelTodos: ListViewModelTodos = viewModel()

    val listViewModel = when(pagerState.currentPage) {
        ListTabDestination.Journals.tabIndex -> icalListViewModelJournals
        ListTabDestination.Notes.tabIndex -> icalListViewModelNotes
        ListTabDestination.Tasks.tabIndex -> icalListViewModelTodos
        else -> icalListViewModelJournals  // fallback, should not happen
    }
    val allWriteableCollections = listViewModel.allWriteableCollections.observeAsState(emptyList())

    var topBarMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteAllVisibleDialog by remember { mutableStateOf(false) }

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
    val showQuickAdd = remember { mutableStateOf(false) }

    if (showDeleteAllVisibleDialog) {
        DeleteVisibleDialog(
            numEntriesToDelete = getActiveViewModel().iCal4List.value?.filter { entry -> !entry.isReadOnly}?.size ?: 0,
            onConfirm = { getActiveViewModel().deleteVisible() },
            onDismiss = { showDeleteAllVisibleDialog = false }
        )
    }

    if(getActiveViewModel().sqlConstraintException.value) {
        ErrorOnUpdateDialog(onConfirm = { getActiveViewModel().sqlConstraintException.value = false })
    }

    // reset search when tab changes
    val lastUsedPage = remember { mutableStateOf<Int?>(null) }
    if(lastUsedPage.value != pagerState.currentPage) {
        showSearch = false
        keyboardController?.hide()
        listViewModel.listSettings.searchText.value = null  // null removes color indicator for active search
        listViewModel.updateSearch(saveListSettings = false)
        lastUsedPage.value = pagerState.currentPage
    }

    fun addNewEntry(newICalObject: ICalObject, categories: List<Category>, attachment: Attachment?, editAfterSaving: Boolean) {
        listViewModel.listSettings.saveLastUsedCollectionId(newICalObject.collectionId)
        settingsStateHolder.lastUsedModule.value = newICalObject.getModuleFromString()
        settingsStateHolder.lastUsedModule = settingsStateHolder.lastUsedModule

        globalStateHolder.icalFromIntentString.value = null  // origin was state from import
        globalStateHolder.icalFromIntentAttachment.value = null  // origin was state from import

        //handle autoAlarm
        val autoAlarm = if(settingsStateHolder.settingAutoAlarm.value == DropdownSettingOption.AUTO_ALARM_ON_DUE && newICalObject.due != null) {
            Alarm.createDisplayAlarm(
                dur = (0).minutes,
                alarmRelativeTo = AlarmRelativeTo.END,
                referenceDate = newICalObject.due!!,
                referenceTimezone = newICalObject.dueTimezone
            )
        } else if(settingsStateHolder.settingAutoAlarm.value == DropdownSettingOption.AUTO_ALARM_ON_START && newICalObject.dtstart != null) {
            Alarm.createDisplayAlarm(
                dur = (0).minutes,
                alarmRelativeTo = null,
                referenceDate = newICalObject.dtstart!!,
                referenceTimezone = newICalObject.dtstartTimezone
            )
        } else null

        getActiveViewModel().insertQuickItem(
            newICalObject,
            categories,
            attachment,
            autoAlarm,
            editAfterSaving
        )
    }


    Scaffold(
        topBar = {
            JtxTopAppBar(
                drawerState = drawerState,
                title = stringResource(id = R.string.app_name),
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
                            Divider()
                        }
                        if(getActiveViewModel().iCal4List.value?.isNotEmpty() == true) {
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
                        }
                        ViewMode.values().forEach { viewMode ->
                            RadiobuttonWithText(
                                text = stringResource(id = viewMode.stringResource),
                                isSelected = getActiveViewModel().listSettings.viewMode.value == viewMode,
                                onClick = {
                                    if ((BuildConfig.FLAVOR == BUILD_FLAVOR_GOOGLEPLAY && BillingManager.getInstance().isProPurchased.value == false)) {
                                        Toast.makeText(context, R.string.buypro_snackbar_please_purchase_pro, Toast.LENGTH_LONG).show()
                                    } else {
                                        getActiveViewModel().listSettings.viewMode.value = viewMode
                                        getActiveViewModel().listSettings.saveToPrefs()
                                    }
                                }
                            )
                        }
                        Divider()
                        CheckboxWithText(
                            text = stringResource(R.string.menu_list_flat_view),
                            subtext = stringResource(R.string.menu_list_flat_view_sub),
                            isSelected = getActiveViewModel().listSettings.flatView.value,
                            onCheckedChange = {
                                getActiveViewModel().listSettings.flatView.value = it
                                getActiveViewModel().updateSearch(saveListSettings = true)
                            }
                        )
                        Divider()
                        CheckboxWithText(
                            text = stringResource(R.string.menu_list_limit_recur_entries),
                            subtext = stringResource(R.string.menu_list_limit_recur_entries_sub),
                            isSelected = getActiveViewModel().listSettings.showOneRecurEntryInFuture.value,
                            onCheckedChange = {
                                getActiveViewModel().listSettings.showOneRecurEntryInFuture.value = it
                                getActiveViewModel().updateSearch(saveListSettings = true)
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {

            // show the bottom bar only if there is any collection available that supports the component/module
            if (allWriteableCollections.value.any { collection ->
                    (listViewModel.module == Module.JOURNAL && collection.supportsVJOURNAL)
                            || (listViewModel.module == Module.NOTE && collection.supportsVJOURNAL)
                            || (listViewModel.module == Module.TODO && collection.supportsVTODO)
                }) {
                ListBottomAppBar(
                    module = listViewModel.module,
                    iCal4ListLive = listViewModel.iCal4List,
                    allowNewEntries = allWriteableCollections.value.any { collection ->
                        ((listViewModel.module == Module.JOURNAL && collection.supportsVJOURNAL)
                                || (listViewModel.module == Module.NOTE && collection.supportsVJOURNAL)
                                || (listViewModel.module == Module.TODO && collection.supportsVTODO)
                             && !collection.readonly)
                    },
                    onAddNewEntry = {
                        val lastUsedCollectionId =
                            listViewModel.listSettings.getLastUsedCollectionId()
                        val proposedCollectionId =
                            if (allWriteableCollections.value.any { collection -> collection.collectionId == lastUsedCollectionId })
                                lastUsedCollectionId
                            else
                                allWriteableCollections.value.firstOrNull()?.collectionId
                                    ?: return@ListBottomAppBar
                        val newICalObject = when (listViewModel.module) {
                            Module.JOURNAL -> ICalObject.createJournal()
                                .apply { collectionId = proposedCollectionId }
                            Module.NOTE -> ICalObject.createNote()
                                .apply { collectionId = proposedCollectionId }
                            Module.TODO -> ICalObject.createTodo().apply {
                                this.setDefaultDueDateFromSettings(context)
                                this.setDefaultStartDateFromSettings(context)
                                collectionId = proposedCollectionId
                            }
                        }
                        addNewEntry(newICalObject, emptyList(), null, true)
                    },
                    showQuickEntry = showQuickAdd,
                    listSettings = listViewModel.listSettings,
                    onFilterIconClicked = {
                        scope.launch {
                            if (filterBottomSheetState.isVisible)
                                filterBottomSheetState.hide()
                            else
                                filterBottomSheetState.show()
                        }
                    },
                    onGoToDateSelected = { id -> listViewModel.scrollOnceId.postValue(id) },
                    onSearchTextClicked = {
                        scope.launch {
                            if (!showSearch) {
                                showSearch = true
                                listViewModel.listSettings.searchText.value = ""
                                keyboardController?.show()
                                //focusRequesterSearchText.requestFocus()
                            } else {
                                showSearch = false
                                keyboardController?.hide()
                                listViewModel.listSettings.searchText.value =
                                    null  // null removes color indicator for active search
                                listViewModel.updateSearch(saveListSettings = false)
                            }
                        }
                    }
                )
            } else {
                Snackbar(modifier = Modifier.padding(8.dp)) {
                    Text("Please add a local collection or synchronise a remote collection that supports this module.")
                }
            }
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
                                            settingsStateHolder.lastUsedModule.value =
                                                when (screen) {
                                                    ListTabDestination.Journals -> Module.JOURNAL
                                                    ListTabDestination.Notes -> Module.NOTE
                                                    ListTabDestination.Tasks -> Module.TODO
                                                }
                                            settingsStateHolder.lastUsedModule =
                                                settingsStateHolder.lastUsedModule  // in order to save
                                        },
                                        text = {
                                            Text(
                                                text = stringResource(id = screen.titleResource),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    )
                                }
                            }


                            AnimatedVisibility(showSearch) {
                                ListSearchTextField(
                                    initialSeachText = getActiveViewModel().listSettings.searchText.value,
                                    onSearchTextChanged = { newSearchText ->
                                        getActiveViewModel().listSettings.searchText.value =
                                            newSearchText
                                        getActiveViewModel().updateSearch(saveListSettings = false)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(1.dp)
                                        .padding(8.dp)
                                )
                            }

                            AnimatedVisibility(showQuickAdd.value || globalStateHolder.icalFromIntentString.value != null || globalStateHolder.icalFromIntentAttachment.value != null) {
                                // origin can be button click or an import through the intent
                                ListQuickAddElement(
                                    presetModule = if (showQuickAdd.value)
                                        getActiveViewModel().module    // coming from button
                                    else
                                        globalStateHolder.icalFromIntentModule.value,   // coming from intent
                                    presetText = globalStateHolder.icalFromIntentString.value
                                        ?: "",    // only relevant when coming from intent
                                    presetAttachment = globalStateHolder.icalFromIntentAttachment.value,    // only relevant when coming from intent
                                    allWriteableCollections = allWriteableCollections.value,
                                    presetCollectionId = listViewModel.listSettings.getLastUsedCollectionId(),
                                    onSaveEntry = { newICalObject, categories, attachment, editAfterSaving ->
                                        addNewEntry(newICalObject, categories, attachment, editAfterSaving)
                                        scope.launch {
                                            pagerState.scrollToPage(
                                                when (newICalObject.getModuleFromString()) {
                                                    Module.JOURNAL -> ListTabDestination.Journals.tabIndex
                                                    Module.NOTE -> ListTabDestination.Notes.tabIndex
                                                    Module.TODO -> ListTabDestination.Tasks.tabIndex
                                                }
                                            )
                                        }
                                    },
                                    onDismiss = {
                                        showQuickAdd.value = false  // origin was button
                                        globalStateHolder.icalFromIntentString.value =
                                            null  // origin was state from import
                                        globalStateHolder.icalFromIntentAttachment.value =
                                            null  // origin was state from import
                                    }
                                )
                            }

                            Box {
                                HorizontalPager(
                                    state = pagerState,
                                    count = 3,
                                    userScrollEnabled = !filterBottomSheetState.isVisible,
                                ) { page ->
                                    when (page) {
                                        ListTabDestination.Journals.tabIndex -> {
                                            ListScreen(
                                                listViewModel = icalListViewModelJournals,
                                                navController = navController,
                                                filterBottomSheetState = filterBottomSheetState,
                                            )
                                        }
                                        ListTabDestination.Notes.tabIndex -> {
                                            ListScreen(
                                                listViewModel = icalListViewModelNotes,
                                                navController = navController,
                                                filterBottomSheetState = filterBottomSheetState,
                                            )
                                        }
                                        ListTabDestination.Tasks.tabIndex -> {
                                            ListScreen(
                                                listViewModel = icalListViewModelTodos,
                                                navController = navController,
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
