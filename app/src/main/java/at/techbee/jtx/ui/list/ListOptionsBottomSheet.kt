/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredListSetting
import at.techbee.jtx.database.locals.StoredListSettingData
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ListOptionsBottomSheet(
    module: Module,
    initialTab: ListOptionsBottomSheetTabs,
    listSettings: ListSettings,
    allCollections: List<ICalCollection>,
    allCategories: List<String>,
    allResources: List<String>,
    storedStatuses: List<ExtendedStatus>,
    storedListSettings: List<StoredListSetting>,
    storedCategories: List<StoredCategory>,
    onListSettingsChanged: () -> Unit,
    onSaveStoredListSetting: (StoredListSetting) -> Unit,
    onDeleteStoredListSetting: (StoredListSetting) -> Unit,
    modifier: Modifier = Modifier
) {

    val scope = rememberCoroutineScope()

    val listOptionTabs = if(listSettings.viewMode.value == ViewMode.KANBAN)
        listOf(ListOptionsBottomSheetTabs.FILTER, ListOptionsBottomSheetTabs.GROUP_SORT, ListOptionsBottomSheetTabs.KANBAN_SETTINGS )
    else
        listOf(ListOptionsBottomSheetTabs.FILTER, ListOptionsBottomSheetTabs.GROUP_SORT)

    val pagerState = rememberPagerState(initialPage = listOptionTabs.indexOf(initialTab), pageCount = { listOptionTabs.size })

    Column(
        modifier = modifier
    ) {

        SecondaryTabRow(selectedTabIndex = pagerState.currentPage) {
            listOptionTabs.forEach { tab ->
                Tab(
                    selected = pagerState.currentPage == listOptionTabs.indexOf(tab),
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(listOptionTabs.indexOf(tab))
                        }
                    },
                    content = {
                        Text(
                            stringResource(id = when(tab) {
                                ListOptionsBottomSheetTabs.FILTER -> R.string.filter
                                ListOptionsBottomSheetTabs.GROUP_SORT -> R.string.filter_group_sort
                                ListOptionsBottomSheetTabs.KANBAN_SETTINGS -> R.string.kanban_settings
                            } ),
                            textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier.height(50.dp)
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            verticalAlignment = Alignment.Top
        ) { page ->
            when (listOptionTabs[page]) {
                ListOptionsBottomSheetTabs.FILTER -> {
                    ListOptionsFilter(
                        module = module,
                        listSettings = listSettings,
                        allCollections = allCollections,
                        allCategories = allCategories,
                        allResources = allResources,
                        extendedStatuses = storedStatuses,
                        storedListSettings = storedListSettings,
                        onListSettingsChanged = onListSettingsChanged,
                        onSaveStoredListSetting = onSaveStoredListSetting,
                        onDeleteStoredListSetting = onDeleteStoredListSetting,
                        modifier = modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                    )
                }
                ListOptionsBottomSheetTabs.GROUP_SORT -> {
                    ListOptionsGroupSort(
                        module = module,
                        listSettings = listSettings,
                        onListSettingsChanged = onListSettingsChanged,
                        modifier = modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                    )
                }
                ListOptionsBottomSheetTabs.KANBAN_SETTINGS -> {
                    ListOptionsKanban(
                        module = module,
                        listSettings = listSettings,
                        extendedStatuses = storedStatuses,
                        storedCategories = storedCategories,
                        onListSettingsChanged = onListSettingsChanged,
                        modifier = modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListOptionsBottomSheet_Preview_TODO() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(
            ListViewModel.PREFS_LIST_JOURNALS,
            Context.MODE_PRIVATE
        )

        val listSettings = ListSettings.fromPrefs(prefs)

        ListOptionsBottomSheet(
            module = Module.TODO,
            initialTab = ListOptionsBottomSheetTabs.FILTER,
            listSettings = listSettings,
            allCollections =
                listOf(
                    ICalCollection(
                        collectionId = 1L,
                        displayName = "Collection 1",
                        accountName = "Account 1"
                    ),
                    ICalCollection(
                        collectionId = 2L,
                        displayName = "Collection 2",
                        accountName = "Account 2"
                    )
            ),
            allCategories = listOf("Category1", "#MyHashTag", "Whatever"),
            allResources = listOf("Resource1", "Whatever"),
            storedStatuses = listOf(ExtendedStatus("individual", Module.JOURNAL, Status.FINAL, null)),
            storedCategories = listOf(StoredCategory("cat1", Color.Green.toArgb())),
            storedListSettings = listOf(StoredListSetting(module = Module.JOURNAL, name = "test", storedListSettingData = StoredListSettingData())),
            onListSettingsChanged = { },
            onSaveStoredListSetting = { },
            onDeleteStoredListSetting = { }

        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListOptionsBottomSheet_Preview_JOURNAL() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(
            ListViewModel.PREFS_LIST_JOURNALS,
            Context.MODE_PRIVATE
        )

        val listSettings = ListSettings.fromPrefs(prefs)

        ListOptionsBottomSheet(
            module = Module.JOURNAL,
            initialTab = ListOptionsBottomSheetTabs.FILTER,
            listSettings = listSettings,
            allCollections =
                listOf(
                    ICalCollection(
                        collectionId = 1L,
                        displayName = "Collection 1",
                        accountName = "Account 1"
                    ),
                    ICalCollection(
                        collectionId = 2L,
                        displayName = "Collection 2",
                        accountName = "Account 2"
                    )
            ),
            allCategories = listOf("Category1", "#MyHashTag", "Whatever"),
            allResources = listOf("Resource1", "Whatever"),
            storedStatuses = listOf(ExtendedStatus("individual", Module.JOURNAL, Status.FINAL, null)),
            storedCategories = listOf(StoredCategory("cat1", Color.Green.toArgb())),
            storedListSettings = listOf(StoredListSetting(module = Module.JOURNAL, name = "test", storedListSettingData = StoredListSettingData())),
            onListSettingsChanged = { },
            onSaveStoredListSetting = { },
            onDeleteStoredListSetting = { }
        )
    }
}

enum class ListOptionsBottomSheetTabs { FILTER, GROUP_SORT, KANBAN_SETTINGS }