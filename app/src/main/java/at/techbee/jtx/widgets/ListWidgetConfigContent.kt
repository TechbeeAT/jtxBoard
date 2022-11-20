/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.list.*
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListWidgetConfigContent(
    initialConfig: ListWidgetConfig,
    onFinish: (ListWidgetConfig) -> Unit,
    onCancel: () -> Unit
) {

    val context = LocalContext.current
    val database = ICalDatabase.getInstance(context).iCalDatabaseDao

    var selectedModule by remember { mutableStateOf(initialConfig.module) }
    val listSettings = ListSettings.fromListWidgetConfig(initialConfig)
    BillingManager.getInstance().initialise(context)
    val isPurchased = if(LocalInspectionMode.current)
        remember { mutableStateOf(true) }
    else
        BillingManager.getInstance().isProPurchased.observeAsState(false)


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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = stringResource(R.string.widget_list_configuration_beta_info),
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    mainAxisAlignment = FlowMainAxisAlignment.Center
                ) {

                    Module.values().forEach { module ->
                        FilterChip(
                            selected = module == selectedModule,
                            onClick = {
                                selectedModule = module
                                listSettings.reset()
                            },
                            label = { Text(stringResource(id = when(module) {
                                Module.JOURNAL -> R.string.list_tabitem_journals
                                Module.NOTE -> R.string.list_tabitem_notes
                                Module.TODO -> R.string.list_tabitem_todos
                            })) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }

                if(isPurchased.value) {

                    ListOptionsFilter(
                        module = selectedModule,
                        listSettings = listSettings,
                        allCollectionsLive = database.getAllCollections(module = selectedModule.name),
                        allCategoriesLive = database.getAllCategoriesAsText(),
                        onListSettingsChanged = { /* nothing to do, only relevant for states for filter bottom sheet, not for widget config */ },
                        isWidgetConfig = true
                    )

                    Divider()

                    ListOptionsSortOrder(
                        module = selectedModule,
                        listSettings = listSettings,
                        onListSettingsChanged = { /* nothing to do, only relevant for states for filter bottom sheet, not for widget config */ }
                    )

                    Divider()

                    HeadlineWithIcon(
                        icon = Icons.Outlined.Settings,
                        iconDesc = stringResource(id = R.string.widget_list_view_settings),
                        text = stringResource(id = R.string.widget_list_view_settings),
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                    )
                    FlowRow(modifier = Modifier.fillMaxWidth()) {

                        FilterChip(
                            selected = listSettings.flatView.value,
                            onClick = {
                                listSettings.flatView.value = !listSettings.flatView.value
                            },
                            label = { Text(stringResource(id = R.string.menu_list_flat_view)) },
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        if(selectedModule == Module.TODO) {
                            FilterChip(
                                selected = listSettings.checkboxPositionEnd.value,
                                onClick = {
                                    listSettings.checkboxPositionEnd.value = !listSettings.checkboxPositionEnd.value
                                },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if(!listSettings.checkboxPositionEnd.value)
                                            Icon(Icons.Outlined.CheckBox, "Start", modifier = Modifier.padding(end = 4.dp))
                                        Text(stringResource(id = R.string.widget_list_configuration_checkbox_position))
                                        if(listSettings.checkboxPositionEnd.value)
                                            Icon(Icons.Outlined.CheckBox, "End", modifier = Modifier.padding(start = 4.dp))
                                        }
                                    },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }


                } else {
                    Text(
                        text = stringResource(R.string.widget_list_configuration_pro_info),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    if(isPurchased.value) {
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
                                    module = selectedModule
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
            initialConfig = ListWidgetConfig(),
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
    var searchText: String? = null,        // search text is not saved!
    var viewMode: ViewMode = ViewMode.LIST,
    var flatView: Boolean = false,
    var checkboxPositionEnd: Boolean = false
)