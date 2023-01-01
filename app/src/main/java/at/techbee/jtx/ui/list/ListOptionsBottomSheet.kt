/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.Module
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch


@OptIn(ExperimentalPagerApi::class)
@Composable
fun ListOptionsBottomSheet(
    module: Module,
    listSettings: ListSettings,
    allCollectionsLive: LiveData<List<ICalCollection>>,
    allCategoriesLive: LiveData<List<String>>,
    allResourcesLive: LiveData<List<String>>,
    onListSettingsChanged: () -> Unit,
    modifier: Modifier = Modifier
) {

    val scope = rememberCoroutineScope()

    val tabIndexFilter = 0
    val tabIndexGroupSort = 1

    val pagerState = rememberPagerState(initialPage = tabIndexFilter)

    Column(
        modifier = modifier
    ) {

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Tab(
                selected = false,
                onClick = {
                    scope.launch {
                        pagerState.scrollToPage(tabIndexFilter)
                    }
                },
                content = { Text(stringResource(id = R.string.filter)) },
                modifier = Modifier.height(50.dp)
            )
            Tab(
                selected = false,
                onClick = {
                    scope.launch {
                        pagerState.scrollToPage(tabIndexGroupSort)
                    }
                },
                content = { Text(stringResource(id = R.string.filter_group_sort)) },
                modifier = Modifier.height(50.dp)
            )
        }

        HorizontalPager(
            state = pagerState,
            count = 2
        ) { page ->
            when (page) {
                tabIndexFilter -> {
                    ListOptionsFilter(
                        module = module,
                        listSettings = listSettings,
                        allCollectionsLive = allCollectionsLive,
                        allCategoriesLive = allCategoriesLive,
                        allResourcesLive = allResourcesLive,
                        onListSettingsChanged = onListSettingsChanged,
                        modifier = modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                    )
                }
                tabIndexGroupSort -> {
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
            listSettings = listSettings,
            allCollectionsLive = MutableLiveData(
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
                )
            ),
            allCategoriesLive = MutableLiveData(listOf("Category1", "#MyHashTag", "Whatever")),
            allResourcesLive = MutableLiveData(listOf("Resource1", "Whatever")),
            onListSettingsChanged = { }

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
            listSettings = listSettings,
            allCollectionsLive = MutableLiveData(
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
                )
            ),
            allCategoriesLive = MutableLiveData(listOf("Category1", "#MyHashTag", "Whatever")),
            allResourcesLive = MutableLiveData(listOf("Resource1", "Whatever")),
            onListSettingsChanged = { }
        )
    }
}
