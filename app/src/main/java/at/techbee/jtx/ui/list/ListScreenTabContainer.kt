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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.MainActivity2.Companion.BUILD_FLAVOR_GOOGLEPLAY
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


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
            if(enabledTabs.any { tab -> tab.module == settingsStateHolder.lastUsedModule.value }) {
                when (settingsStateHolder.lastUsedModule.value) {
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
                (collection.accountType == LOCAL_ACCOUNT_TYPE || isProPurchased.value)        // filter remote collections if pro was not purchased
                        && (enabledTabs.any { it.module == Module.JOURNAL || it.module == Module.NOTE} && collection.supportsVJOURNAL
                            || enabledTabs.any { it.module == Module.TODO} && collection.supportsVTODO
                        )
            }
        }
    }

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
        navController.navigate(DetailDestination.Detail.getRoute(iCalObjectId = icalObjectId, icalObjectIdList = getActiveViewModel().iCal4List.value?.map { it.id } ?: emptyList(), isEditMode = true))
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val filterBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

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
            onCategoriesChanged = { addedCategories, deletedCategories -> getActiveViewModel().updateCategoriesOfSelected(addedCategories, deletedCategories) },
            onResourcesChanged = { addedResources, deletedResources -> getActiveViewModel().updateResourcesToSelected(addedResources, deletedResources) },
            onStatusChanged = { newStatus -> getActiveViewModel().updateStatusOfSelected(newStatus) },
            onClassificationChanged = { newClassification -> getActiveViewModel().updateClassificationOfSelected(newClassification) },
            onPriorityChanged = { newPriority -> getActiveViewModel().updatePriorityOfSelected(newPriority) },
            onCollectionChanged = { newCollection -> getActiveViewModel().moveSelectedToNewCollection(newCollection) },
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
        listViewModel.updateSearch(saveListSettings = false)
        lastUsedPage.value = pagerState.currentPage
    }

    fun addNewEntry(
        module: Module,
        text: String?,
        collectionId: Long,
        attachment: Attachment?,
        editAfterSaving: Boolean
    ) {

        val newICalObject = when (module) {
            Module.JOURNAL -> ICalObject.createJournal().apply { this.setDefaultJournalDateFromSettings(context) }
            Module.NOTE -> ICalObject.createNote()
            Module.TODO -> ICalObject.createTodo().apply {
                this.setDefaultDueDateFromSettings(context)
                this.setDefaultStartDateFromSettings(context)
            }
        }
        newICalObject.collectionId = collectionId
        newICalObject.parseSummaryAndDescription(text)
        newICalObject.parseURL(text)
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
            attachment,
            autoAlarm,
            editAfterSaving
        )
    }


    Scaffold(
        topBar = {
            ListTopAppBar(
                drawerState = drawerState,
                listTopAppBarMode = getActiveViewModel().listSettings.topAppBarMode.value,
                module = listViewModel.module,
                searchText = listViewModel.listSettings.searchText,
                onSearchTextUpdated = { listViewModel.updateSearch(saveListSettings = false) },
                onCreateNewEntry = { newEntryText ->
                    addNewEntry(
                        module = listViewModel.module,
                        text = newEntryText,
                        collectionId = listViewModel.listSettings.topAppBarCollectionId.value,
                        attachment = null,
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
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    showCollectionSelectorDialog = true
                                    topBarMenuExpanded = false
                                }) {
                                    Icon(Icons.Outlined.Folder, null)
                                }
                            }
                        )
                        Divider()


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
                        ViewMode.values().forEach { viewMode ->
                            RadiobuttonWithText(
                                text = stringResource(id = viewMode.stringResource),
                                isSelected = getActiveViewModel().listSettings.viewMode.value == viewMode,
                                onClick = {
                                    if ((BuildConfig.FLAVOR == BUILD_FLAVOR_GOOGLEPLAY && !isProPurchased.value)) {
                                        Toast.makeText(context, R.string.buypro_snackbar_please_purchase_pro, Toast.LENGTH_LONG).show()
                                    } else {
                                        getActiveViewModel().listSettings.viewMode.value = viewMode
                                        getActiveViewModel().updateSearch(saveListSettings = true)
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

                        addNewEntry(module = listViewModel.module, text = null, collectionId = proposedCollectionId, attachment = null, editAfterSaving = true)
                    },
                    showQuickEntry = showQuickAdd,
                    multiselectEnabled = listViewModel.multiselectEnabled,
                    selectedEntries = listViewModel.selectedEntries,
                    listSettings = listViewModel.listSettings,
                    onFilterIconClicked = {
                        scope.launch {
                            if (filterBottomSheetState.isVisible)
                                filterBottomSheetState.hide()
                            else
                                filterBottomSheetState.show()
                        }
                    },
                    onGoToDateSelected = { id -> getActiveViewModel().scrollOnceId.postValue(id) },
                    onDeleteSelectedClicked = { showDeleteSelectedDialog = true },
                    onUpdateSelectedClicked = { showUpdateEntriesDialog = true }
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
                                                    pagerState.scrollToPage(enabledTabs.indexOf(enabledTab))
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

                            AnimatedVisibility(
                                allUsableCollections.isNotEmpty() &&
                                        (showQuickAdd.value || globalStateHolder.icalFromIntentString.value != null || globalStateHolder.icalFromIntentAttachment.value != null))
                            {
                                // origin can be button click or an import through the intent
                                ListQuickAddElement(
                                    presetModule = if (showQuickAdd.value)
                                        getActiveViewModel().module    // coming from button
                                    else
                                        globalStateHolder.icalFromIntentModule.value,   // coming from intent
                                    enabledModules = enabledTabs.map { it.module },
                                    presetText = globalStateHolder.icalFromIntentString.value
                                        ?: "",    // only relevant when coming from intent
                                    presetAttachment = globalStateHolder.icalFromIntentAttachment.value,    // only relevant when coming from intent
                                    allWriteableCollections = allUsableCollections,
                                    presetCollectionId = listViewModel.listSettings.getLastUsedCollectionId(listViewModel.prefs),
                                    onSaveEntry = { module, text, attachment, collectionId,  editAfterSaving ->

                                        listViewModel.listSettings.saveLastUsedCollectionId(listViewModel.prefs, collectionId)
                                        settingsStateHolder.lastUsedModule.value = module
                                        settingsStateHolder.lastUsedModule = settingsStateHolder.lastUsedModule

                                        globalStateHolder.icalFromIntentString.value = null  // origin was state from import
                                        globalStateHolder.icalFromIntentAttachment.value = null  // origin was state from import

                                        addNewEntry(module, text, collectionId, attachment, editAfterSaving)
                                        scope.launch {
                                            val index = enabledTabs.indexOf(enabledTabs.find { tab -> tab.module == module })
                                            if(index >=0)
                                                pagerState.scrollToPage(index)
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
                                    count = enabledTabs.size,
                                    userScrollEnabled = !filterBottomSheetState.isVisible,
                                ) { page ->

                                    ListScreen(
                                        listViewModel = when (enabledTabs[page].module) {
                                            Module.JOURNAL -> icalListViewModelJournals
                                            Module.NOTE -> icalListViewModelNotes
                                            Module.TODO -> icalListViewModelTodos
                                        },
                                        navController = navController,
                                        filterBottomSheetState = filterBottomSheetState,
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
