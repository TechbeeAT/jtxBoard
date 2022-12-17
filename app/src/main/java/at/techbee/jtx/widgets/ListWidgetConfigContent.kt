/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.ui.list.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun ListWidgetConfigContent(
    initialConfig: ListWidgetConfig,
    isPurchased: Boolean,
    onFinish: (ListWidgetConfig) -> Unit,
    onCancel: () -> Unit
) {

    val context = LocalContext.current
    val database = ICalDatabase.getInstance(context).iCalDatabaseDao

    val selectedModule = remember { mutableStateOf(initialConfig.module) }
    val listSettings = ListSettings.fromListWidgetConfig(initialConfig)

    val tabIndexGeneral = 0
    val tabIndexFilter = 1
    val tabIndexGroupSort = 2

    val pagerState = rememberPagerState(initialPage = tabIndexGeneral)
    val scope = rememberCoroutineScope()

    val buyProToast = Toast.makeText(context, R.string.widget_list_configuration_pro_info, Toast.LENGTH_LONG)


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.widget_list_configuration),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onCancel() }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = stringResource(id = R.string.cancel)
                        )
                    }
                },
                actions = { }
            )
        },
        content = { paddingValues ->

            Column(
                modifier = Modifier.padding(paddingValues).fillMaxWidth(),
                verticalArrangement = Arrangement.Top
            ) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    //containerColor = MaterialTheme.colorScheme.primaryContainer,
                    //contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = false,
                        onClick = {
                            scope.launch {
                                pagerState.scrollToPage(tabIndexGeneral)
                            }
                        },
                        content = { Text(stringResource(id = R.string.general)) },
                        modifier = Modifier.height(50.dp)
                    )
                    Tab(
                        selected = false,
                        onClick = {
                            if (isPurchased) {
                                scope.launch {
                                    pagerState.scrollToPage(tabIndexFilter)
                                }
                            } else {
                                buyProToast.show()
                            }
                        },
                        content = { Text(stringResource(id = R.string.filter)) },
                        modifier = Modifier.height(50.dp)
                    )
                    Tab(
                        selected = false,
                        onClick = {
                            if (isPurchased) {
                                scope.launch {
                                    pagerState.scrollToPage(tabIndexGroupSort)
                                }
                            } else {
                                buyProToast.show()
                            }
                        },
                        content = { Text(stringResource(id = R.string.filter_group_sort)) },
                        modifier = Modifier.height(50.dp)
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    count = if(isPurchased) 3 else 1,
                    modifier = Modifier.weight(1f).padding(8.dp).verticalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.Top
                ) { page ->
                    when (page) {
                        tabIndexGeneral -> {
                            ListWidgetConfigGeneral(
                                selectedModule = selectedModule,
                                listSettings = listSettings
                            )
                        }
                        tabIndexFilter -> {
                            ListOptionsFilter(
                                module = selectedModule.value,
                                listSettings = listSettings,
                                allCollectionsLive = database.getAllCollections(module = selectedModule.value.name),
                                allCategoriesLive = database.getAllCategoriesAsText(),
                                onListSettingsChanged = { /* nothing to do, only relevant for states for filter bottom sheet, not for widget config */ },
                                isWidgetConfig = true
                            )
                        }
                        tabIndexGroupSort -> {
                            ListOptionsGroupSort(
                                module = selectedModule.value,
                                listSettings = listSettings,
                                onListSettingsChanged = { /* nothing to do, only relevant for states for filter bottom sheet, not for widget config */ }
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    if(isPurchased) {
                        TextButton(onClick = { listSettings.reset() }) {
                            Text(stringResource(id = R.string.reset))
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            onFinish(
                                ListWidgetConfig().apply {
                                    module = selectedModule.value
                                    searchCategories = listSettings.searchCategories.value
                                    searchStatusTodo = listSettings.searchStatusTodo.value
                                    searchStatusJournal = listSettings.searchStatusJournal.value
                                    searchClassification = listSettings.searchClassification.value
                                    searchCollection = listSettings.searchCollection.value
                                    searchAccount = listSettings.searchAccount.value
                                    orderBy = listSettings.orderBy.value
                                    sortOrder = listSettings.sortOrder.value
                                    orderBy2 = listSettings.orderBy2.value
                                    sortOrder2 = listSettings.sortOrder2.value
                                    groupBy = listSettings.groupBy.value
                                    flatView = listSettings.flatView.value
                                    checkboxPositionEnd = listSettings.checkboxPositionEnd.value
                                    showOneRecurEntryInFuture = listSettings.showOneRecurEntryInFuture.value
                                    widgetAlpha = listSettings.widgetAlpha.value
                                    widgetAlphaEntries = listSettings.widgetAlphaEntries.value
                                    showDescription = listSettings.showDescription.value
                                    showSubtasks = listSettings.showSubtasks.value
                                    showSubnotes = listSettings.showSubnotes.value

                                    isExcludeDone = listSettings.isExcludeDone.value
                                    isFilterOverdue = listSettings.isFilterOverdue.value
                                    isFilterDueToday = listSettings.isFilterDueToday.value
                                    isFilterDueTomorrow = listSettings.isFilterDueTomorrow.value
                                    isFilterDueFuture = listSettings.isFilterDueFuture.value
                                    isFilterStartInPast = listSettings.isFilterStartInPast.value
                                    isFilterStartToday = listSettings.isFilterStartToday.value
                                    isFilterStartTomorrow = listSettings.isFilterStartTomorrow.value
                                    isFilterStartFuture = listSettings.isFilterStartFuture.value
                                    isFilterNoDatesSet = listSettings.isFilterNoDatesSet.value
                                    isFilterNoStatusSet = listSettings.isFilterNoStatusSet.value
                                    isFilterNoClassificationSet = listSettings.isFilterNoClassificationSet.value
                                }
                            )
                        }
                    ) { Icon(Icons.Outlined.Done, stringResource(id = R.string.save))  }
                },
            )
        }
    )
}


@Preview(showBackground = true)
@Composable
fun WidgetConfigContent_Preview() {
    MaterialTheme {
        ListWidgetConfigContent(
            initialConfig = ListWidgetConfig(module = Module.TODO),
            isPurchased = true,
            onFinish = { },
            onCancel = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WidgetConfigContent_Preview_not_purchased() {
    MaterialTheme {
        ListWidgetConfigContent(
            initialConfig = ListWidgetConfig(),
            isPurchased = false,
            onFinish = { },
            onCancel = { }
        )
    }
}



@kotlinx.serialization.Serializable
data class ListWidgetConfig(
    var module: Module = Module.NOTE,
    var searchCategories: List<String> = emptyList(),
    var searchStatusTodo: List<StatusTodo> = emptyList(),
    var searchStatusJournal: List<StatusJournal> = emptyList(),
    var searchClassification: List<Classification> = emptyList(),
    var searchCollection: List<String> = emptyList(),
    var searchAccount: List<String> = emptyList(),
    var orderBy: OrderBy = OrderBy.CREATED,
    var sortOrder: SortOrder = SortOrder.ASC,
    var orderBy2: OrderBy = OrderBy.SUMMARY,
    var sortOrder2: SortOrder = SortOrder.ASC,
    var groupBy: GroupBy? = null,
    var isExcludeDone: Boolean = false,
    var isFilterOverdue: Boolean = false,
    var isFilterDueToday: Boolean = false,
    var isFilterDueTomorrow: Boolean = false,
    var isFilterDueFuture: Boolean = false,
    var isFilterStartInPast: Boolean = false,
    var isFilterStartToday: Boolean = false,
    var isFilterStartTomorrow: Boolean = false,
    var isFilterStartFuture: Boolean = false,
    var isFilterNoDatesSet: Boolean = false,
    var isFilterNoStatusSet: Boolean = false,
    var isFilterNoClassificationSet: Boolean = false,
    var searchText: String? = null,        // search text is not saved!
    var viewMode: ViewMode = ViewMode.LIST,
    var flatView: Boolean = false,
    var showOneRecurEntryInFuture: Boolean = false,
    var checkboxPositionEnd: Boolean = false,
    var widgetAlpha: Float = 1F,
    var widgetAlphaEntries: Float = 1F,
    var showDescription: Boolean = true,
    var showSubtasks: Boolean = true,
    var showSubnotes: Boolean = true
)