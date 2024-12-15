/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.ui.list.AnyAllNone
import at.techbee.jtx.ui.list.CheckboxPosition
import at.techbee.jtx.ui.list.GroupBy
import at.techbee.jtx.ui.list.ListOptionsFilter
import at.techbee.jtx.ui.list.ListOptionsGroupSort
import at.techbee.jtx.ui.list.ListSettings
import at.techbee.jtx.ui.list.OrderBy
import at.techbee.jtx.ui.list.SortOrder
import at.techbee.jtx.ui.list.ViewMode
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListWidgetConfigContent(
    initialConfig: ListWidgetConfig,
    isPurchased: Boolean,
    onFinish: (ListWidgetConfig) -> Unit,
    onCancel: () -> Unit
) {

    val context = LocalContext.current
    val database = ICalDatabase.getInstance(context).iCalDatabaseDao()

    val selectedModule = remember { mutableStateOf(initialConfig.module) }
    val listSettings = ListSettings.fromListWidgetConfig(initialConfig)

    val tabIndexGeneral = 0
    val tabIndexFilter = 1
    val tabIndexGroupSort = 2

    val pagerState = rememberPagerState(initialPage = tabIndexGeneral, pageCount = { if (isPurchased) 3 else 1 })
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
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(id = R.string.cancel)
                        )
                    }
                },
                actions = { }
            )
        },
        content = { paddingValues ->

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Top
            ) {
                PrimaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    //containerColor = MaterialTheme.colorScheme.primaryContainer,
                    //contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = false,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(tabIndexGeneral)
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
                                    pagerState.animateScrollToPage(tabIndexFilter)
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
                                    pagerState.animateScrollToPage(tabIndexGroupSort)
                                }
                            } else {
                                buyProToast.show()
                            }
                        },
                        content = {
                            Text(
                                stringResource(id = R.string.filter_group_sort),
                                textAlign = TextAlign.Center
                            )
                        },
                        modifier = Modifier.height(50.dp)
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.Top
                ) { page ->
                    when (page) {
                        tabIndexGeneral -> {
                            ListWidgetConfigGeneral(
                                selectedModule = selectedModule,
                                allCategoriesLive = database.getAllCategoriesAsText(),
                                listSettings = listSettings
                            )
                        }
                        tabIndexFilter -> {
                            ListOptionsFilter(
                                module = selectedModule.value,
                                listSettings = listSettings,
                                allCollections = database.getAllCollections(module = selectedModule.value.name).observeAsState(emptyList()).value,
                                allCategories = database.getAllCategoriesAsText().observeAsState(emptyList()).value,
                                allResources = database.getAllResourcesAsText().observeAsState(emptyList()).value,
                                extendedStatuses = database.getStoredStatuses().observeAsState(emptyList()).value,
                                storedListSettings = database.getStoredListSettings(modules = listOf(selectedModule.value.name)).observeAsState(emptyList()).value,
                                onListSettingsChanged = { /* nothing to do, only relevant for states for filter bottom sheet, not for widget config */ },
                                isWidgetConfig = true,
                                onSaveStoredListSetting = { /* no saving option in list widget config*/ },
                                onDeleteStoredListSetting = { /* no option to save/delete list widget config */ }
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
                                    searchCategories = listSettings.searchCategories
                                    searchCategoriesAnyAllNone = listSettings.searchCategoriesAnyAllNone.value
                                    searchResources = listSettings.searchResources
                                    searchResourcesAnyAllNone = listSettings.searchResourcesAnyAllNone.value
                                    searchStatus = listSettings.searchStatus
                                    searchXStatus = listSettings.searchXStatus
                                    searchClassification = listSettings.searchClassification
                                    searchPriority = listSettings.searchPriority
                                    searchCollection = listSettings.searchCollection
                                    searchAccount = listSettings.searchAccount
                                    orderBy = listSettings.orderBy.value
                                    sortOrder = listSettings.sortOrder.value
                                    orderBy2 = listSettings.orderBy2.value
                                    sortOrder2 = listSettings.sortOrder2.value
                                    groupBy = listSettings.groupBy.value
                                    subtasksOrderBy = listSettings.subtasksOrderBy.value
                                    subtasksSortOrder = listSettings.subtasksSortOrder.value
                                    subnotesOrderBy = listSettings.subnotesOrderBy.value
                                    subnotesSortOrder = listSettings.subnotesSortOrder.value
                                    flatView = listSettings.flatView.value
                                    checkboxPositionEnd = false  // reset to false for legacy handling
                                    checkboxPosition = listSettings.checkboxPosition.value
                                    showOneRecurEntryInFuture = listSettings.showOneRecurEntryInFuture.value
                                    widgetAlpha = listSettings.widgetAlpha.value
                                    widgetAlphaEntries = listSettings.widgetAlphaEntries.value
                                    widgetColor = listSettings.widgetColor.value
                                    widgetColorEntries = listSettings.widgetColorEntries.value
                                    showDescription = listSettings.showDescription.value
                                    showSubtasks = listSettings.showSubtasks.value
                                    showSubnotes = listSettings.showSubnotes.value
                                    widgetHeader = listSettings.widgetHeader.value
                                    defaultCategories = listSettings.defaultCategories

                                    isExcludeDone = listSettings.isExcludeDone.value
                                    isFilterOverdue = listSettings.isFilterOverdue.value
                                    isFilterDueToday = listSettings.isFilterDueToday.value
                                    isFilterDueTomorrow = listSettings.isFilterDueTomorrow.value
                                    isFilterDueWithin7Days = listSettings.isFilterDueWithin7Days.value
                                    isFilterDueFuture = listSettings.isFilterDueFuture.value
                                    isFilterStartInPast = listSettings.isFilterStartInPast.value
                                    isFilterStartToday = listSettings.isFilterStartToday.value
                                    isFilterStartTomorrow = listSettings.isFilterStartTomorrow.value
                                    isFilterStartWithin7Days = listSettings.isFilterStartWithin7Days.value
                                    isFilterStartFuture = listSettings.isFilterStartFuture.value
                                    isFilterNoDatesSet = listSettings.isFilterNoDatesSet.value
                                    isFilterNoStartDateSet = listSettings.isFilterNoStartDateSet.value
                                    isFilterNoDueDateSet = listSettings.isFilterNoDueDateSet.value
                                    isFilterNoCompletedDateSet = listSettings.isFilterNoCompletedDateSet.value

                                    filterStartRangeStart = listSettings.filterStartRangeStart.value
                                    filterStartRangeEnd = listSettings.filterStartRangeEnd.value
                                    filterDueRangeStart = listSettings.filterDueRangeStart.value
                                    filterDueRangeEnd = listSettings.filterDueRangeEnd.value
                                    filterCompletedRangeStart = listSettings.filterCompletedRangeStart.value
                                    filterCompletedRangeEnd = listSettings.filterCompletedRangeEnd.value

                                    isFilterNoCategorySet = listSettings.isFilterNoCategorySet.value
                                    isFilterNoResourceSet = listSettings.isFilterNoResourceSet.value
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
    var searchCategoriesAnyAllNone: AnyAllNone = AnyAllNone.ANY,
    var searchResources: List<String> = emptyList(),
    var searchResourcesAnyAllNone: AnyAllNone = AnyAllNone.ANY,
    var searchStatus: List<Status> = emptyList(),
    var searchXStatus: List<String> = emptyList(),
    var searchClassification: List<Classification> = emptyList(),
    var searchPriority: List<Int?> = emptyList(),
    var searchCollection: List<String> = emptyList(),
    var searchAccount: List<String> = emptyList(),
    var orderBy: OrderBy = OrderBy.CREATED,
    var sortOrder: SortOrder = SortOrder.ASC,
    var orderBy2: OrderBy = OrderBy.SUMMARY,
    var sortOrder2: SortOrder = SortOrder.ASC,
    var groupBy: GroupBy? = null,
    var subtasksOrderBy: OrderBy = OrderBy.CREATED,
    var subtasksSortOrder: SortOrder = SortOrder.ASC,
    var subnotesOrderBy: OrderBy = OrderBy.CREATED,
    var subnotesSortOrder: SortOrder = SortOrder.ASC,
    var isExcludeDone: Boolean = false,
    var isFilterOverdue: Boolean = false,
    var isFilterDueToday: Boolean = false,
    var isFilterDueTomorrow: Boolean = false,
    var isFilterDueWithin7Days: Boolean = false,
    var isFilterDueFuture: Boolean = false,
    var isFilterStartInPast: Boolean = false,
    var isFilterStartToday: Boolean = false,
    var isFilterStartTomorrow: Boolean = false,
    var isFilterStartWithin7Days: Boolean = false,
    var isFilterStartFuture: Boolean = false,
    var isFilterNoDatesSet: Boolean = false,
    var isFilterNoStartDateSet: Boolean = false,
    var isFilterNoDueDateSet: Boolean = false,
    var isFilterNoCompletedDateSet: Boolean = false,

    var filterStartRangeStart: Long? = null,
    var filterStartRangeEnd: Long? = null,
    var filterDueRangeStart: Long? = null,
    var filterDueRangeEnd: Long? = null,
    var filterCompletedRangeStart: Long? = null,
    var filterCompletedRangeEnd: Long? = null,

    var isFilterNoCategorySet: Boolean = false,
    var isFilterNoResourceSet: Boolean = false,
    var searchText: String? = null,        // search text is not saved!
    var viewMode: ViewMode = ViewMode.LIST,
    var flatView: Boolean = false,
    var showOneRecurEntryInFuture: Boolean = false,
    @Deprecated("Use Checkbox Position instead") var checkboxPositionEnd: Boolean = false,
    var checkboxPosition: CheckboxPosition = CheckboxPosition.START,
    @Deprecated("Now in widgetColor") var widgetAlpha: Float = 1F,
    @Deprecated("Now in widgetColorEntries") var widgetAlphaEntries: Float = 1F,
    var showDescription: Boolean = true,
    var showSubtasks: Boolean = true,
    var showSubnotes: Boolean = true,
    var widgetHeader: String = "",
    var widgetColor: Int? = null,
    var widgetColorEntries: Int? = null,

    var defaultCategories: List<String> = emptyList()
)