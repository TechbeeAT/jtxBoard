/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalDatabase
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class
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
    val database = ICalDatabase.getInstance(context).iCalDatabaseDao()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val locationPermissionState = if (!LocalInspectionMode.current) rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    ) else null
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
            },
        pageCount = { enabledTabs.size }
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
    val allWriteableCollections = database.getAllWriteableCollections().observeAsState(emptyList())
    val isProPurchased = BillingManager.getInstance().isProPurchased.observeAsState(true)
    val allUsableCollections by remember(allWriteableCollections) {
        derivedStateOf {
            allWriteableCollections.value.filter { collection ->
                (enabledTabs.any { it.module == Module.JOURNAL || it.module == Module.NOTE} && collection.supportsVJOURNAL)
                        || (enabledTabs.any { it.module == Module.TODO} && collection.supportsVTODO)
            }
        }
    }

    val iCal4ListRel by listViewModel.iCal4ListRel.observeAsState(initial = emptyList())

    var timeout by remember { mutableStateOf(false) }
    LaunchedEffect(timeout, allWriteableCollections.value) {
        if (!timeout) {
            delay((1).seconds)
            timeout = true
        }
    }

    var topBarMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteSelectedDialog by rememberSaveable { mutableStateOf(false) }
    var showUpdateEntriesDialog by rememberSaveable { mutableStateOf(false) }
    var showCollectionSelectorDialog by rememberSaveable { mutableStateOf(false) }

    var isRefreshing by remember { mutableStateOf(false) }
    val isPullRefreshEnabled = remember {
        SyncUtil.availableSyncApps(context).any { SyncUtil.isSyncAppCompatible(it, context) } && settingsStateHolder.settingSyncOnPullRefresh.value
    }

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

    LaunchedEffect(storedListSettingData) {
        if (storedListSettingData != null) {
            storedListSettingData.applyToListSettings(icalListViewModelJournals.listSettings)
            storedListSettingData.applyToListSettings(icalListViewModelNotes.listSettings)
            storedListSettingData.applyToListSettings(icalListViewModelTodos.listSettings)
            getActiveViewModel().updateSearch(saveListSettings = false, isAuthenticated = globalStateHolder.isAuthenticated.value)
        }
    }


    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val filterSheetState = rememberModalBottomSheetState()
    var filterSheetInitialTab by remember { mutableStateOf(ListOptionsBottomSheetTabs.FILTER)}

    var showSearch by remember { mutableStateOf(false) }
    val showQuickAdd = rememberSaveable { mutableStateOf(false) }
    var quickAddBackupText by rememberSaveable { mutableStateOf("") }

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
            allCategories = database.getAllCategoriesAsText().observeAsState(emptyList()).value,
            allResources = database.getAllResourcesAsText().observeAsState(emptyList()).value,
            allCollections = database.getAllWriteableCollections().observeAsState(emptyList()).value,
            selectFromAllListLive = getActiveViewModel().selectFromAllList,
            storedStatuses = database.getStoredStatuses().observeAsState(emptyList()).value,
            player = getActiveViewModel().mediaPlayer,
            onSelectFromAllListSearchTextUpdated = { getActiveViewModel().updateSelectFromAllListQuery(searchText = it, isAuthenticated = globalStateHolder.isAuthenticated.value) },
            onCategoriesChanged = { addedCategories, deletedCategories -> getActiveViewModel().updateCategoriesOfSelected(addedCategories, deletedCategories) },
            onResourcesChanged = { addedResources, deletedResources -> getActiveViewModel().updateResourcesToSelected(addedResources, deletedResources) },
            onStatusChanged = { newStatus -> getActiveViewModel().updateStatusOfSelected(newStatus) },
            onXStatusChanged = { newXStatus -> getActiveViewModel().updateXStatusOfSelected(newXStatus) },
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
            allCollections = database.getAllCollections(module = getActiveViewModel().module.name).observeAsState(emptyList()).value,
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
        categories: List<String>,
        collectionId: Long,
        attachments: List<Attachment>,
        setLocation: Boolean,
        editAfterSaving: Boolean
    ) {

        val newICalObject = ICalObject.fromText(
            module,
            collectionId,
            text,
            settingsStateHolder.settingDefaultJournalsDate.value,
            settingsStateHolder.settingDefaultStartDate.value,
            settingsStateHolder.settingDefaultStartTime.value,
            settingsStateHolder.settingDefaultStartTimezone.value,
            settingsStateHolder.settingDefaultDueDate.value,
            settingsStateHolder.settingDefaultDueTime.value,
            settingsStateHolder.settingDefaultDueTimezone.value,
        )
        val mergedCategories = mutableListOf<Category>()
        categories.forEach {
            mergedCategories.add(Category(text = it))
        }
        mergedCategories.addAll(Category.extractHashtagsFromText(text))

        if(setLocation && locationPermissionState?.permissions?.any { it.status.isGranted } == true) {
            // Get the location manager, avoiding using fusedLocationClient here to not use proprietary libraries
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val bestProvider = locationManager.getProviders(true).firstOrNull() ?: return
            val locListener = LocationListener { }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.requestLocationUpdates(bestProvider, 0, 0f, locListener)
                locationManager.getLastKnownLocation(bestProvider)?.let { lastKnownLocation ->
                    newICalObject.geoLat = lastKnownLocation.latitude
                    newICalObject.geoLong = lastKnownLocation.longitude
                }
            }
        } else if(setLocation && locationPermissionState?.permissions?.none { it.status.isGranted } == true) {
            Toast.makeText(context, R.string.location_permission_denied, Toast.LENGTH_SHORT).show()
        }

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
            mergedCategories,
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
                allCollections = database.getAllCollections(module = listViewModel.module.name).observeAsState(emptyList()).value,
                allCategories = database.getAllCategoriesAsText().observeAsState(emptyList()).value,
                allResources = database.getAllResourcesAsText().observeAsState(emptyList()).value,
                storedStatuses = database.getStoredStatuses().observeAsState(emptyList()).value,
                storedCategories = database.getStoredCategories().observeAsState(emptyList()).value,
                storedListSettings = database.getStoredListSettings(listOf(listViewModel.module.name)).observeAsState(emptyList()).value,
                onListSettingsChanged = {
                    listViewModel.updateSearch(
                        saveListSettings = true,
                        isAuthenticated = globalStateHolder.isAuthenticated.value
                    )
                },
                onSaveStoredListSetting = { storedListSetting -> listViewModel.saveStoredListSetting(storedListSetting) },
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
                        categories = emptyList(),
                        collectionId = listViewModel.listSettings.topAppBarCollectionId.value,
                        attachments = emptyList(),
                        setLocation = when(listViewModel.module) {
                            Module.JOURNAL -> settingsStateHolder.settingSetDefaultCurrentLocationJournals.value
                            Module.NOTE -> settingsStateHolder.settingSetDefaultCurrentLocationNotes.value
                            Module.TODO -> settingsStateHolder.settingSetDefaultCurrentLocationTasks.value
                        },
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
                        HorizontalDivider()

                        AnimatedVisibility(getActiveViewModel().listSettings.topAppBarMode.value == ListTopAppBarMode.SEARCH
                                && (getActiveViewModel().listSettings.viewMode.value == ViewMode.LIST || getActiveViewModel().listSettings.viewMode.value == ViewMode.COMPACT)
                        ) {
                            CheckboxWithText(
                                text = stringResource(R.string.list_show_only_search_matching_subentries),
                                isSelected = getActiveViewModel().listSettings.showOnlySearchMatchingSubentries.value,
                                onCheckedChange = {
                                    getActiveViewModel().listSettings.showOnlySearchMatchingSubentries.value = it
                                    getActiveViewModel().updateSearch(saveListSettings = true, isAuthenticated = globalStateHolder.isAuthenticated.value)
                                }
                            )
                        }
                        AnimatedVisibility(getActiveViewModel().listSettings.topAppBarMode.value == ListTopAppBarMode.SEARCH) {
                            HorizontalDivider()
                        }



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
                            HorizontalDivider()
                        }
                        ViewMode.entries.forEach { viewMode ->

                            if(getActiveViewModel().module == Module.NOTE && viewMode == ViewMode.WEEK)
                                return@forEach

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
                                    if(viewMode == ViewMode.KANBAN) {
                                        if(isProPurchased.value) {
                                            getActiveViewModel().listSettings.viewMode.value = viewMode
                                            filterSheetInitialTab = ListOptionsBottomSheetTabs.KANBAN_SETTINGS
                                            scope.launch { filterSheetState.show() }
                                        } else {
                                            Toast.makeText(context, R.string.buypro_snackbar_please_purchase_pro, Toast.LENGTH_LONG).show()
                                        }
                                        topBarMenuExpanded = false
                                    }
                                }
                            )
                        }
                        HorizontalDivider()
                        CheckboxWithText(
                            text = stringResource(R.string.menu_list_flat_view),
                            subtext = stringResource(R.string.menu_list_flat_view_sub),
                            isSelected = getActiveViewModel().listSettings.flatView.value,
                            onCheckedChange = {
                                getActiveViewModel().listSettings.flatView.value = it
                                getActiveViewModel().updateSearch(saveListSettings = true, isAuthenticated = globalStateHolder.isAuthenticated.value)
                            }
                        )
                        HorizontalDivider()
                        CheckboxWithText(
                            text = stringResource(R.string.menu_list_limit_recur_entries),
                            subtext = stringResource(R.string.menu_list_limit_recur_entries_sub),
                            isSelected = getActiveViewModel().listSettings.showOneRecurEntryInFuture.value,
                            onCheckedChange = {
                                getActiveViewModel().listSettings.showOneRecurEntryInFuture.value = it
                                getActiveViewModel().updateSearch(saveListSettings = true, isAuthenticated = globalStateHolder.isAuthenticated.value)
                            }
                        )
                        HorizontalDivider()
                        CheckboxWithText(
                            text = stringResource(R.string.menu_view_markdown_formatting),
                            isSelected = getActiveViewModel().listSettings.markdownEnabled.value,
                            onCheckedChange = {
                                getActiveViewModel().listSettings.markdownEnabled.value = it
                                getActiveViewModel().updateSearch(saveListSettings = true, isAuthenticated = globalStateHolder.isAuthenticated.value)
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
                    iCal4ListRel = iCal4ListRel,
                    isSyncInProgress = globalStateHolder.isSyncInProgress.value,
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

                        addNewEntry(
                            module = listViewModel.module,
                            text = listViewModel.listSettings.newEntryText.value.ifEmpty { null },
                            categories = emptyList(),
                            collectionId = proposedCollectionId,
                            attachments = emptyList(),
                            setLocation = when(listViewModel.module) {
                                Module.JOURNAL -> settingsStateHolder.settingSetDefaultCurrentLocationJournals.value
                                Module.NOTE -> settingsStateHolder.settingSetDefaultCurrentLocationNotes.value
                                Module.TODO -> settingsStateHolder.settingSetDefaultCurrentLocationTasks.value
                            },
                            editAfterSaving = true
                        )
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
                    onDeleteDone = { listViewModel.deleteDone() },
                    onListSettingsChanged = {
                        listViewModel.updateSearch(
                            saveListSettings = true,
                            isAuthenticated = globalStateHolder.isAuthenticated.value
                        )
                    }
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
                        Column(modifier = Modifier.fillMaxSize()) {

                            if(enabledTabs.size > 1) {
                                PrimaryTabRow(
                                    selectedTabIndex = pagerState.currentPage
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


                            ListActiveFiltersRow(
                                listSettings = listViewModel.listSettings,
                                module = listViewModel.module,
                                storedCategories = database.getStoredCategories().observeAsState(emptyList()).value,
                                storedResources = database.getStoredResources().observeAsState(emptyList()).value,
                                storedListSettings = database.getStoredListSettings(listOf(listViewModel.module.name)).observeAsState(emptyList()).value,
                                numShownEntries = iCal4ListRel.size,
                                numAllEntries = database.getICal4ListCount(module = listViewModel.module.name).observeAsState(0).value,
                                isFilterActive = listViewModel.listSettings.isFilterActive(),
                                isAccessibilityMode = settingsStateHolder.settingAccessibilityMode.value,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )

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
                                    presetText = globalStateHolder.icalFromIntentString.value ?: quickAddBackupText,    // only relevant when coming from intent
                                    presetCategories = globalStateHolder.icalFromIntentCategories,   // only relevant when coming from intent
                                    presetAttachment = globalStateHolder.icalFromIntentAttachment.value,    // only relevant when coming from intent
                                    allWriteableCollections = allUsableCollections,
                                    presetCollectionId = globalStateHolder.icalFromIntentCollection.value?.let {fromIntent ->
                                        allUsableCollections.find { fromIntent == it.displayName }?.collectionId
                                    } ?: listViewModel.listSettings.getLastUsedCollectionId(listViewModel.prefs),
                                    player = listViewModel.mediaPlayer,
                                    onSaveEntry = { module, text, categories, attachments, collectionId,  editAfterSaving ->

                                        listViewModel.listSettings.saveLastUsedCollectionId(listViewModel.prefs, collectionId)
                                        settingsStateHolder.lastUsedModule.value = module
                                        settingsStateHolder.lastUsedModule = settingsStateHolder.lastUsedModule

                                        val setLocation = when(module) {
                                            Module.JOURNAL -> settingsStateHolder.settingSetDefaultCurrentLocationJournals.value
                                            Module.NOTE -> settingsStateHolder.settingSetDefaultCurrentLocationNotes.value
                                            Module.TODO -> settingsStateHolder.settingSetDefaultCurrentLocationTasks.value
                                        }

                                        addNewEntry(module, text, categories, collectionId, attachments, setLocation, editAfterSaving)

                                        globalStateHolder.icalFromIntentString.value = null  // origin was state from import
                                        globalStateHolder.icalFromIntentAttachment.value = null  // origin was state from import
                                        globalStateHolder.icalFromIntentModule.value = null
                                        globalStateHolder.icalFromIntentCollection.value = null

                                        scope.launch {
                                            val index = enabledTabs.indexOf(enabledTabs.find { tab -> tab.module == module })
                                            if(index >=0)
                                                pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    onDismiss = { text ->
                                        showQuickAdd.value = false  // origin was button
                                        quickAddBackupText = text
                                        globalStateHolder.icalFromIntentString.value = null  // origin was state from import
                                        globalStateHolder.icalFromIntentAttachment.value = null  // origin was state from import
                                        globalStateHolder.icalFromIntentModule.value = null
                                        globalStateHolder.icalFromIntentCollection.value = null
                                        globalStateHolder.icalFromIntentCategories.clear()
                                    },
                                    keepDialogOpen = { showQuickAdd.value = true } // necessary when origin was intent and save&new is clicked!
                                )
                            }

                            HorizontalPager(
                                state = pagerState,
                                userScrollEnabled = !filterSheetState.isVisible,
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->

                                // TODO: Remove this stupid condition once PullToRefreshBox allows a disabled state!
                                if (isPullRefreshEnabled) {
                                    PullToRefreshBox(
                                        state = rememberPullToRefreshState(),
                                        onRefresh = {
                                            //TODO: Get rid of this stupid workaround with isRefreshing and delay once the new version properly makes the animation disappear!!!
                                            isRefreshing = true
                                            listViewModel.syncAccounts()
                                            scope.launch {
                                                delay(100)
                                                isRefreshing = false
                                            }
                                        },
                                        isRefreshing = isRefreshing,
                                        contentAlignment = Alignment.TopCenter,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        ListScreen(
                                            listViewModel = when (enabledTabs[page].module) {
                                                Module.JOURNAL -> icalListViewModelJournals
                                                Module.NOTE -> icalListViewModelNotes
                                                Module.TODO -> icalListViewModelTodos
                                            },
                                            navController = navController
                                        )
                                    }
                                } else {
                                    ListScreen(
                                        listViewModel = when (enabledTabs[page].module) {
                                            Module.JOURNAL -> icalListViewModelJournals
                                            Module.NOTE -> icalListViewModelNotes
                                            Module.TODO -> icalListViewModelTodos
                                        },
                                        navController = navController
                                    )
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
