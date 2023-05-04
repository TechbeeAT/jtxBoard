/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list


import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.locals.StoredListSettingData
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.AlarmRelativeTo
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.GlobalStateHolder
import at.techbee.jtx.ui.reusable.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.reusable.destinations.DetailDestination
import at.techbee.jtx.ui.reusable.dialogs.CollectionSelectorDialog
import at.techbee.jtx.ui.reusable.dialogs.DeleteSelectedDialog
import at.techbee.jtx.ui.reusable.dialogs.ErrorOnUpdateDialog
import at.techbee.jtx.ui.reusable.dialogs.UpdateEntriesDialog
import at.techbee.jtx.ui.reusable.elements.CheckboxWithText
import at.techbee.jtx.ui.reusable.elements.RadiobuttonWithText
import at.techbee.jtx.ui.settings.DropdownSettingOption
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.util.SyncUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun ListScreenTabContainer(
    navController: NavHostController,
    globalStateHolder: GlobalStateHolder,
    settingsStateHolder: SettingsStateHolder,
    initialModule: Module,
    storedListSettingData: StoredListSettingData? = null
) {

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val enabledTabs = mutableListOf<ListTabDestination>().apply {
        if(settingsStateHolder.settingEnableJournals.value)
            add(ListTabDestination.Journals)
        if(settingsStateHolder.settingEnableNotes.value)
            add(ListTabDestination.Notes)
        if(settingsStateHolder.settingEnableTasks.value)
            add(ListTabDestination.Tasks)
    }.toList()
    val pagerState = rememberPagerState(
        initialPage =
            if(globalStateHolder.icalFromIntentModule.value != null
                && enabledTabs.any { tab -> tab.module == globalStateHolder.icalFromIntentModule.value }) {
                when (globalStateHolder.icalFromIntentModule.value!!) {
                    Module.JOURNAL -> enabledTabs.indexOf(ListTabDestination.Journals)
                    Module.NOTE -> enabledTabs.indexOf(ListTabDestination.Notes)
                    Module.TODO -> enabledTabs.indexOf(ListTabDestination.Tasks)
                }
            }
            else if(enabledTabs.any { tab -> tab.module == initialModule }) {
                when (initialModule) {
                    Module.JOURNAL -> enabledTabs.indexOf(ListTabDestination.Journals)
                    Module.NOTE -> enabledTabs.indexOf(ListTabDestination.Notes)
                    Module.TODO -> enabledTabs.indexOf(ListTabDestination.Tasks)
                }
            } else {
                0
            }
    )

    val icalListViewModelJournals: ListViewModelJournals = viewModel()
    val icalListViewModelNotes: ListViewModelNotes = viewModel()
    val icalListViewModelTodos: ListViewModelTodos = viewModel()

    val listViewModel = when(pagerState.currentPage) {
        enabledTabs.indexOf(ListTabDestination.Journals) -> icalListViewModelJournals
        enabledTabs.indexOf(ListTabDestination.Notes) -> icalListViewModelNotes
        enabledTabs.indexOf(ListTabDestination.Tasks) -> icalListViewModelTodos
        else -> icalListViewModelJournals  // fallback, should not happen
    }
    val allWriteableCollections = listViewModel.allWriteableCollections.observeAsState(emptyList())
    val isProPurchased = BillingManager.getInstance().isProPurchased.observeAsState(true)
    val allUsableCollections by remember(allWriteableCollections) {
        derivedStateOf {
            allWriteableCollections.value.filter { collection ->
                (enabledTabs.any { it.module == Module.JOURNAL || it.module == Module.NOTE} && collection.supportsVJOURNAL)
                        || (enabledTabs.any { it.module == Module.TODO} && collection.supportsVTODO)
            }
        }
    }
    val storedCategories by listViewModel.storedCategories.observeAsState(emptyList())
    val storedResources by listViewModel.storedResources.observeAsState(emptyList())

    var timeout by remember { mutableStateOf(false) }
    LaunchedEffect(timeout, allWriteableCollections.value) {
        if (!timeout) {
            delay((1).seconds)
            timeout = true
        }
    }

    var topBarMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }
    var showUpdateEntriesDialog by remember { mutableStateOf(false) }
    var showCollectionSelectorDialog by remember { mutableStateOf(false) }


    fun getActiveViewModel() = when (pagerState.currentPage) {
            enabledTabs.indexOf(ListTabDestination.Journals) -> icalListViewModelJournals
            enabledTabs.indexOf(ListTabDestination.Notes) -> icalListViewModelNotes
            enabledTabs.indexOf(ListTabDestination.Tasks) -> icalListViewModelTodos
            else -> icalListViewModelJournals  // fallback, should not happen
        }

    val goToEdit = getActiveViewModel().goToEdit.observeAsState()
    goToEdit.value?.let { icalObjectId ->
        getActiveViewModel().goToEdit.value = null
        navController.navigate(DetailDestination.Detail.getRoute(iCalObjectId = icalObjectId, icalObjectIdList = getActiveViewModel().iCal4ListRel.value?.map { it.iCal4List.id } ?: emptyList(), isEditMode = true))
    }

    if(storedListSettingData != null) {
        storedListSettingData.applyToListSettings(icalListViewModelJournals.listSettings)
        storedListSettingData.applyToListSettings(icalListViewModelNotes.listSettings)
        storedListSettingData.applyToListSettings(icalListViewModelTodos.listSettings)
        getActiveViewModel().updateSearch(saveListSettings = false, isAuthenticated = globalStateHolder.isAuthenticated.value)
    }


    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val filterSheetState = rememberModalBottomSheetState()
    var filterSheetInitialTab by remember { mutableStateOf(ListOptionsBottomSheetTabs.FILTER)}

    var showSearch by remember { mutableStateOf(false) }
    val showQuickAdd = remember { mutableStateOf(false) }

    if (showDeleteSelectedDialog) {
        DeleteSelectedDialog(
            numEntriesToDelete = getActiveViewModel().selectedEntries.size,
            onConfirm = { getActiveViewModel().deleteSelected() },
            onDismiss = { showDeleteSelectedDialog = false }
        )
    }

    if (showUpdateEntriesDialog) {
        UpdateEntriesDialog(
            module = getActiveViewModel().module,
            allCategoriesLive = getActiveViewModel().allCategories,
            allResourcesLive = getActiveViewModel().allResources,
            allCollectionsLive = getActiveViewModel().allWriteableCollections,
            selectFromAllListLive = getActiveViewModel().selectFromAllList,
            storedCategoriesLive = getActiveViewModel().storedCategories,
            storedResourcesLive = getActiveViewModel().storedResources,
            storedStatusesLive = getActiveViewModel().storedStatuses,
            player = getActiveViewModel().mediaPlayer,
            onSelectFromAllListSearchTextUpdated = { getActiveViewModel().updateSelectFromAllListQuery(searchText = it, isAuthenticated = globalStateHolder.isAuthenticated.value) },
            onCategoriesChanged = { addedCategories, deletedCategories -> getActiveViewModel().updateCategoriesOfSelected(addedCategories, deletedCategories) },
            onResourcesChanged = { addedResources, deletedResources -> getActiveViewModel().updateResourcesToSelected(addedResources, deletedResources) },
            onStatusChanged = { newStatus -> getActiveViewModel().updateStatusOfSelected(newStatus) },
            onClassificationChanged = { newClassification -> getActiveViewModel().updateClassificationOfSelected(newClassification) },
            onPriorityChanged = { newPriority -> getActiveViewModel().updatePriorityOfSelected(newPriority) },
            onCollectionChanged = { newCollection -> getActiveViewModel().moveSelectedToNewCollection(newCollection) },
            onParentAdded = { addedParent -> getActiveViewModel().addNewParentToSelected(addedParent) },
            onDismiss = { showUpdateEntriesDialog = false }
        )
    }

    if (showCollectionSelectorDialog) {
        CollectionSelectorDialog(
            module = getActiveViewModel().module,
            presetCollectionId = getActiveViewModel().listSettings.topAppBarCollectionId.value,
            allCollectionsLive = getActiveViewModel().allCollections,
            onCollectionConfirmed = { selectedCollection ->
                getActiveViewModel().listSettings.topAppBarMode.value = ListTopAppBarMode.ADD_ENTRY
                getActiveViewModel().listSettings.topAppBarCollectionId.value = selectedCollection.collectionId
                getActiveViewModel().listSettings.saveToPrefs(getActiveViewModel().prefs)
            },
            onDismiss = { showCollectionSelectorDialog = false }
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
        listViewModel.updateSearch(saveListSettings = false, isAuthenticated = globalStateHolder.isAuthenticated.value)
        lastUsedPage.value = pagerState.currentPage
    }

    var lastIsAuthenticated by remember { mutableStateOf(false) }
    if(lastIsAuthenticated != globalStateHolder.isAuthenticated.value) {
        lastIsAuthenticated = globalStateHolder.isAuthenticated.value
        getActiveViewModel().updateSearch(false, globalStateHolder.isAuthenticated.value)
    }

    fun addNewEntry(
        module: Module,
        text: String?,
        collectionId: Long,
        attachments: List<Attachment>,
        editAfterSaving: Boolean
    ) {

        val newICalObject = ICalObject.fromText(module, collectionId, text, context)
        val categories = Category.extractHashtagsFromText(text)

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
            attachments,
            autoAlarm,
            editAfterSaving
        )
    }

    if(filterSheetState.currentValue != SheetValue.Hidden) {
        ModalBottomSheet(sheetState = filterSheetState, onDismissRequest = { }) {
            ListOptionsBottomSheet(
                module = listViewModel.module,
                initialTab = filterSheetInitialTab,
                listSettings = listViewModel.listSettings,
                allCollectionsLive = listViewModel.allCollections,
                allCategoriesLive = listViewModel.allCategories,
                allResourcesLive = listViewModel.allResources,
                storedStatusesLive = listViewModel.storedStatuses,
                storedListSettingLive = listViewModel.storedListSettings,
                onListSettingsChanged = {
                    listViewModel.updateSearch(
                        saveListSettings = true,
                        isAuthenticated = globalStateHolder.isAuthenticated.value
                    )
                },
                onSaveStoredListSetting = { name, storedListSettingData -> listViewModel.saveStoredListSettingsData(name, storedListSettingData) },
                onDeleteStoredListSetting = { storedListSetting -> listViewModel.deleteStoredListSetting(storedListSetting) }
            )
        }
    }

    Scaffold(
        topBar = {
            ListTopAppBar(
                drawerState = drawerState,
                listTopAppBarMode = getActiveViewModel().listSettings.topAppBarMode.value,
                module = listViewModel.module,
                searchText = listViewModel.listSettings.searchText,
                newEntryText = listViewModel.listSettings.newEntryText,
                onSearchTextUpdated = { listViewModel.updateSearch(saveListSettings = false, isAuthenticated = globalStateHolder.isAuthenticated.value) },
                onCreateNewEntry = { newEntryText ->
                    addNewEntry(
                        module = listViewModel.module,
                        text = newEntryText,
                        collectionId = listViewModel.listSettings.topAppBarCollectionId.value,
                        attachments = emptyList(),
                        editAfterSaving = false
                    )
                },
                actions = {
                    IconButton(
                        onClick = { topBarMenuExpanded = true },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = stringResource(id = R.string.more)
                        )
                    }

                    DropdownMenu(
                        expanded = topBarMenuExpanded,
                        onDismissRequest = { topBarMenuExpanded = false }
                    ) {
                        Text(stringResource(R.string.details_app_bar_behaviour), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp))
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.search),
                                    color = if(getActiveViewModel().listSettings.topAppBarMode.value == ListTopAppBarMode.SEARCH) MaterialTheme.colorScheme.primary else Color.Unspecified
                                )},
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = null,
                                    tint = if(getActiveViewModel().listSettings.topAppBarMode.value == ListTopAppBarMode.SEARCH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                getActiveViewModel().listSettings.topAppBarMode.value = ListTopAppBarMode.SEARCH
                                getActiveViewModel().listSettings.saveToPrefs(getActiveViewModel().prefs)
                                getActiveViewModel().listSettings.newEntryText.value = ""
                                topBarMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                when (getActiveViewModel().module) {
                                    Module.JOURNAL -> Text(text = stringResource(id = R.string.toolbar_text_add_journal), color = if(getActiveViewModel().listSettings.topAppBarMode.value == ListTopAppBarMode.ADD_ENTRY) MaterialTheme.colorScheme.primary else Color.Unspecified)
                                    Module.NOTE -> Text(text = stringResource(id = R.string.toolbar_text_add_note), color = if(getActiveViewModel().listSettings.topAppBarMode.value == ListTopAppBarMode.ADD_ENTRY) MaterialTheme.colorScheme.primary else Color.Unspecified)
                                    Module.TODO -> Text(text = stringResource(id = R.string.toolbar_text_add_task), color = if(getActiveViewModel().listSettings.topAppBarMode.value == ListTopAppBarMode.ADD_ENTRY) MaterialTheme.colorScheme.primary else Color.Unspecified)
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    tint = if(getActiveViewModel().listSettings.topAppBarMode.value == ListTopAppBarMode.ADD_ENTRY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                          },
                            onClick = {
                                if(listViewModel.listSettings.topAppBarCollectionId.value == 0L
                                    || allWriteableCollections.value.none { collection -> collection.collectionId == listViewModel.listSettings.topAppBarCollectionId.value }) {
                                    showCollectionSelectorDialog = true
                                    topBarMenuExpanded = false
                                } else {
                                    getActiveViewModel().listSettings.topAppBarMode.value = ListTopAppBarMode.ADD_ENTRY
                                    getActiveViewModel().listSettings.saveToPrefs(getActiveViewModel().prefs)
                                    topBarMenuExpanded = false
                                }
                                getActiveViewModel().listSettings.searchText.value = null
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    showCollectionSelectorDialog = true
                                    topBarMenuExpanded = false
                                }) {
                                    Icon(Icons.Outlined.Folder, stringResource(id = R.string.collection))
                                }
                            }
                        )
                        Divider()


                        if(SyncUtil.availableSyncApps(context).any { SyncUtil.isSyncAppCompatible(it, context) }) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(id = R.string.sync_now)
                                    )
                                },
                                leadingIcon = { Icon(Icons.Outlined.Sync, null) },
                                onClick = {
                                    getActiveViewModel().syncAccounts()
                                    topBarMenuExpanded = false
                                }
                            )
                            Divider()
                        }
                        ViewMode.values().forEach { viewMode ->
                            RadiobuttonWithText(
                                text = stringResource(id = viewMode.stringResource),
                                isSelected = getActiveViewModel().listSettings.viewMode.value == viewMode,
                                hasSettings = viewMode == ViewMode.KANBAN,
                                onClick = {
                                    if ((!isProPurchased.value)) {
                                        Toast.makeText(context, R.string.buypro_snackbar_please_purchase_pro, Toast.LENGTH_LONG).show()
                                    } else {
                                        getActiveViewModel().listSettings.viewMode.value = viewMode
                                        getActiveViewModel().updateSearch(saveListSettings = true, isAuthenticated = globalStateHolder.isAuthenticated.value)
                                    }
                                    topBarMenuExpanded = false
                                },
                                onSettingsClicked = {
                                    if(viewMode ==ViewMode.KANBAN) {
                                        filterSheetInitialTab = ListOptionsBottomSheetTabs.KANBAN_SETTINGS
                                        scope.launch { filterSheetState.show() }
                                        topBarMenuExpanded = false
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
                                getActiveViewModel().updateSearch(saveListSettings = true, isAuthenticated = globalStateHolder.isAuthenticated.value)
                            }
                        )
                        Divider()
                        CheckboxWithText(
                            text = stringResource(R.string.menu_list_limit_recur_entries),
                            subtext = stringResource(R.string.menu_list_limit_recur_entries_sub),
                            isSelected = getActiveViewModel().listSettings.showOneRecurEntryInFuture.value,
                            onCheckedChange = {
                                getActiveViewModel().listSettings.showOneRecurEntryInFuture.value = it
                                getActiveViewModel().updateSearch(saveListSettings = true, isAuthenticated = globalStateHolder.isAuthenticated.value)
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            if(storedListSettingData != null)    // no bottom bar if there are preset filters
                return@Scaffold

            // show the bottom bar only if there is any collection available that supports the component/module
            if (allWriteableCollections.value.any { collection ->
                    (listViewModel.module == Module.JOURNAL && collection.supportsVJOURNAL)
                            || (listViewModel.module == Module.NOTE && collection.supportsVJOURNAL)
                            || (listViewModel.module == Module.TODO && collection.supportsVTODO)
                }) {
                ListBottomAppBar(
                    module = listViewModel.module,
                    iCal4ListRelLive = listViewModel.iCal4ListRel,
                    allowNewEntries = allUsableCollections.any { collection ->
                        ((listViewModel.module == Module.JOURNAL && collection.supportsVJOURNAL)
                                || (listViewModel.module == Module.NOTE && collection.supportsVJOURNAL)
                                || (listViewModel.module == Module.TODO && collection.supportsVTODO)
                             && !collection.readonly)
                    },
                    onAddNewEntry = {
                        val lastUsedCollectionId = listViewModel.listSettings.getLastUsedCollectionId(listViewModel.prefs)
                        val proposedCollectionId = allUsableCollections.find { collection -> collection.collectionId == lastUsedCollectionId }?.collectionId
                            ?: allUsableCollections.firstOrNull()?.collectionId
                            ?: return@ListBottomAppBar

                        listViewModel.listSettings.saveLastUsedCollectionId(listViewModel.prefs, proposedCollectionId)
                        settingsStateHolder.lastUsedModule.value = listViewModel.module
                        settingsStateHolder.lastUsedModule = settingsStateHolder.lastUsedModule

                        addNewEntry(module = listViewModel.module, text = listViewModel.listSettings.newEntryText.value.ifEmpty { null }, collectionId = proposedCollectionId, attachments = emptyList(), editAfterSaving = true)
                        listViewModel.listSettings.newEntryText.value = ""
                    },
                    showQuickEntry = showQuickAdd,
                    incompatibleSyncApps = SyncUtil.availableSyncApps(context).filter { !SyncUtil.isSyncAppCompatible(it, context) },
                    multiselectEnabled = listViewModel.multiselectEnabled,
                    selectedEntries = listViewModel.selectedEntries,
                    listSettings = listViewModel.listSettings,
                    isBiometricsEnabled = settingsStateHolder.settingProtectBiometric.value != DropdownSettingOption.PROTECT_BIOMETRIC_OFF,
                    isBiometricsUnlocked = globalStateHolder.isAuthenticated.value,
                    onFilterIconClicked = {
                        scope.launch {
                            if (filterSheetState.isVisible) {
                                filterSheetState.hide()
                            } else {
                                filterSheetInitialTab = ListOptionsBottomSheetTabs.FILTER
                                filterSheetState.show()
                            }
                        }
                    },
                    onGoToDateSelected = { id -> getActiveViewModel().scrollOnceId.postValue(id) },
                    onDeleteSelectedClicked = { showDeleteSelectedDialog = true },
                    onUpdateSelectedClicked = { showUpdateEntriesDialog = true },
                    onToggleBiometricAuthentication = {
                        if(globalStateHolder.isAuthenticated.value) {
                            globalStateHolder.isAuthenticated.value = false
                        } else {
                            val promptInfo: BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
                                .setTitle(context.getString(R.string.settings_protect_biometric))
                                .setSubtitle(context.getString(R.string.settings_protect_biometric_info_on_unlock))
                                .setNegativeButtonText(context.getString(R.string.cancel))
                                .build()
                            globalStateHolder.biometricPrompt?.authenticate(promptInfo)
                        }
                        listViewModel.updateSearch(saveListSettings = false, isAuthenticated = globalStateHolder.isAuthenticated.value)
                    },
                    onDeleteDone = { listViewModel.deleteDone() }
                )
            } else if(timeout) {
                BottomAppBar {
                    Text(
                        text = stringResource(R.string.list_snackbar_no_collection),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        content = { paddingValues ->
                JtxNavigationDrawer(
                    drawerState,
                    mainContent = {
                        Column {

                            if(enabledTabs.size > 1) {
                                TabRow(
                                    selectedTabIndex = pagerState.currentPage    // adding the indicator might make a smooth movement of the tabIndicator, but Accompanist does not support all components (TODO: Check again in future) https://www.geeksforgeeks.org/tab-layout-in-android-using-jetpack-compose/
                                ) {
                                    enabledTabs.forEach { enabledTab ->
                                        Tab(
                                            selected = pagerState.currentPage == enabledTabs.indexOf(enabledTab),
                                            onClick = {
                                                scope.launch {
                                                    pagerState.animateScrollToPage(enabledTabs.indexOf(enabledTab))
                                                }
                                                settingsStateHolder.lastUsedModule.value = enabledTab.module
                                                settingsStateHolder.lastUsedModule = settingsStateHolder.lastUsedModule  // in order to save
                                            },
                                            text = {
                                                Text(
                                                    text = stringResource(id = enabledTab.titleResource),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(listViewModel.listSettings.isFilterActive()) {
                                ListActiveFiltersRow(
                                    listSettings = listViewModel.listSettings,
                                    module = listViewModel.module,
                                    storedCategories = storedCategories,
                                    storedResources = storedResources,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            AnimatedVisibility(
                                allUsableCollections.isNotEmpty() &&
                                        (showQuickAdd.value || globalStateHolder.icalFromIntentString.value != null || globalStateHolder.icalFromIntentAttachment.value != null))
                            {
                                // origin can be button click or an import through the intent
                                ListQuickAddElement(
                                    presetModule = if (showQuickAdd.value)
                                        getActiveViewModel().module    // coming from button
                                    else
                                        globalStateHolder.icalFromIntentModule.value ?: getActiveViewModel().module,   // coming from intent
                                    enabledModules = enabledTabs.map { it.module },
                                    presetText = globalStateHolder.icalFromIntentString.value
                                        ?: "",    // only relevant when coming from intent
                                    presetAttachment = globalStateHolder.icalFromIntentAttachment.value,    // only relevant when coming from intent
                                    allWriteableCollections = allUsableCollections,
                                    presetCollectionId = globalStateHolder.icalFromIntentCollection.value?.let {fromIntent ->
                                        allUsableCollections.find { fromIntent == it.displayName }?.collectionId
                                    } ?: listViewModel.listSettings.getLastUsedCollectionId(listViewModel.prefs),
                                    player = listViewModel.mediaPlayer,
                                    onSaveEntry = { module, text, attachments, collectionId,  editAfterSaving ->

                                        listViewModel.listSettings.saveLastUsedCollectionId(listViewModel.prefs, collectionId)
                                        settingsStateHolder.lastUsedModule.value = module
                                        settingsStateHolder.lastUsedModule = settingsStateHolder.lastUsedModule

                                        globalStateHolder.icalFromIntentString.value = null  // origin was state from import
                                        globalStateHolder.icalFromIntentAttachment.value = null  // origin was state from import
                                        globalStateHolder.icalFromIntentModule.value = null
                                        globalStateHolder.icalFromIntentCollection.value = null

                                        addNewEntry(module, text, collectionId, attachments, editAfterSaving)
                                        scope.launch {
                                            val index = enabledTabs.indexOf(enabledTabs.find { tab -> tab.module == module })
                                            if(index >=0)
                                                pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    onDismiss = {
                                        showQuickAdd.value = false  // origin was button
                                        globalStateHolder.icalFromIntentString.value = null  // origin was state from import
                                        globalStateHolder.icalFromIntentAttachment.value = null  // origin was state from import
                                        globalStateHolder.icalFromIntentModule.value = null
                                        globalStateHolder.icalFromIntentCollection.value = null
                                    },
                                    keepDialogOpen = { showQuickAdd.value = true } // necessary when origin was intent and save&new is clicked!
                                )
                            }

                            Box {
                                HorizontalPager(
                                    state = pagerState,
                                    pageCount = enabledTabs.size,
                                    userScrollEnabled = !filterSheetState.isVisible,
                                    verticalAlignment = Alignment.Top
                                ) { page ->

                                    ListScreen(
                                        listViewModel = when (enabledTabs[page].module) {
                                            Module.JOURNAL -> icalListViewModelJournals
                                            Module.NOTE -> icalListViewModelNotes
                                            Module.TODO -> icalListViewModelTodos
                                        },
                                        navController = navController
                                    )
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
