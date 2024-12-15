/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list


import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Status
import at.techbee.jtx.ui.settings.DropdownSettingOption
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.widgets.ListWidgetConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class CheckboxPosition { START, END, OFF }

class ListSettings {

    var searchCategories = mutableStateListOf<String>()
    var searchCategoriesAnyAllNone = mutableStateOf(AnyAllNone.ANY)
    var searchResources = mutableStateListOf<String>()
    var searchResourcesAnyAllNone = mutableStateOf(AnyAllNone.ANY)
    //var searchOrganizers: MutableState<List<String>> = mutableStateOf(emptyList())
    var searchStatus = mutableStateListOf<Status>()
    var searchXStatus = mutableStateListOf<String>()
    var searchClassification = mutableStateListOf<Classification>()
    var searchPriority = mutableStateListOf<Int?>()
    var searchCollection = mutableStateListOf<String>()
    var searchAccount = mutableStateListOf<String>()
    var orderBy: MutableState<OrderBy> = mutableStateOf(OrderBy.DRAG_AND_DROP)
    var sortOrder: MutableState<SortOrder> = mutableStateOf(SortOrder.ASC)
    var orderBy2: MutableState<OrderBy> = mutableStateOf(OrderBy.SUMMARY)
    var sortOrder2: MutableState<SortOrder> = mutableStateOf(SortOrder.ASC)
    var groupBy: MutableState<GroupBy?> = mutableStateOf(null)
    var showOnlySearchMatchingSubentries: MutableState<Boolean> = mutableStateOf(false)
    var subtasksOrderBy: MutableState<OrderBy> = mutableStateOf(OrderBy.DRAG_AND_DROP)
    var subtasksSortOrder: MutableState<SortOrder> = mutableStateOf(SortOrder.ASC)
    var subnotesOrderBy: MutableState<OrderBy> = mutableStateOf(OrderBy.DRAG_AND_DROP)
    var subnotesSortOrder: MutableState<SortOrder> = mutableStateOf(SortOrder.ASC)
    var isExcludeDone: MutableState<Boolean> = mutableStateOf(false)
    var isFilterOverdue: MutableState<Boolean> = mutableStateOf(false)
    var isFilterDueToday: MutableState<Boolean> = mutableStateOf(false)
    var isFilterDueTomorrow: MutableState<Boolean> = mutableStateOf(false)
    var isFilterDueWithin7Days: MutableState<Boolean> = mutableStateOf(false)
    var isFilterDueFuture: MutableState<Boolean> = mutableStateOf(false)
    var isFilterStartInPast: MutableState<Boolean> = mutableStateOf(false)
    var isFilterStartToday: MutableState<Boolean> = mutableStateOf(false)
    var isFilterStartTomorrow: MutableState<Boolean> = mutableStateOf(false)
    var isFilterStartWithin7Days: MutableState<Boolean> = mutableStateOf(false)
    var isFilterStartFuture: MutableState<Boolean> = mutableStateOf(false)
    var isFilterNoDatesSet: MutableState<Boolean> = mutableStateOf(false)
    var isFilterNoStartDateSet: MutableState<Boolean> = mutableStateOf(false)
    var isFilterNoDueDateSet: MutableState<Boolean> = mutableStateOf(false)
    var isFilterNoCompletedDateSet: MutableState<Boolean> = mutableStateOf(false)
    var isFilterNoCategorySet: MutableState<Boolean> = mutableStateOf(false)
    var isFilterNoResourceSet: MutableState<Boolean> = mutableStateOf(false)

    var filterStartRangeStart: MutableState<Long?> = mutableStateOf(null)
    var filterStartRangeEnd: MutableState<Long?> = mutableStateOf(null)
    var filterDueRangeStart: MutableState<Long?> = mutableStateOf(null)
    var filterDueRangeEnd: MutableState<Long?> = mutableStateOf(null)
    var filterCompletedRangeStart: MutableState<Long?> = mutableStateOf(null)
    var filterCompletedRangeEnd: MutableState<Long?> = mutableStateOf(null)

    var searchText: MutableState<String?> = mutableStateOf(null)        // search text is not saved!
    var newEntryText: MutableState<String> = mutableStateOf("")    // newEntryText is not saved!
    var viewMode: MutableState<ViewMode> = mutableStateOf(ViewMode.LIST)
    var flatView: MutableState<Boolean> = mutableStateOf(false)
    var showOneRecurEntryInFuture: MutableState<Boolean> = mutableStateOf(false)
    var markdownEnabled: MutableState<Boolean> = mutableStateOf(false)

    var topAppBarCollectionId: MutableState<Long> = mutableLongStateOf(0L)   // list view only
    var topAppBarMode: MutableState<ListTopAppBarMode> = mutableStateOf(ListTopAppBarMode.SEARCH)   // list view only
    var kanbanColumnsStatus = mutableStateListOf<String?>()
    var kanbanColumnsXStatus = mutableStateListOf<String>()
    var kanbanColumnsCategory = mutableStateListOf<String>()
    var collapsedGroups = mutableStateListOf<String>()


    var widgetHeader: MutableState<String> = mutableStateOf("") //widgetOnly
    var checkboxPosition: MutableState<CheckboxPosition> = mutableStateOf(CheckboxPosition.START)  // widget only
    @Deprecated("alpha is now in widgetColor") var widgetAlpha: MutableState<Float> = mutableFloatStateOf(1F)  // widget only
    @Deprecated("alpha is now in widgetColorEntries") var widgetAlphaEntries: MutableState<Float> = mutableFloatStateOf(1F)  // widget only
    var widgetColor: MutableState<Int?> = mutableStateOf(null)  // widget only
    var widgetColorEntries: MutableState<Int?> = mutableStateOf(null)  // widget only
    var showDescription: MutableState<Boolean> = mutableStateOf(true)  // widget only
    var showSubtasks: MutableState<Boolean> = mutableStateOf(true)  // widget only
    var showSubnotes: MutableState<Boolean> = mutableStateOf(true)  // widget only
    var defaultCategories = mutableStateListOf<String>() // widget only

    companion object {
        private const val PREFS_COLLECTION = "prefsCollection"
        private const val PREFS_ACCOUNT = "prefsAccount"
        private const val PREFS_CATEGORIES = "prefsCategories"
        private const val PREFS_CATEGORIES_ANYALLNONE = "prefsCategoriesAnyAllNone"
        private const val PREFS_RESOURCES = "prefsResources"
        private const val PREFS_RESOURCES_ANYALLNONE = "prefsResourcesAnyAllNone"
        private const val PREFS_CLASSIFICATION = "prefsClassification"
        private const val PREFS_PRIORITY = "prefsPriority"
        private const val PREFS_STATUS = "prefsStatus"
        private const val PREFS_EXTENDED_STATUS = "prefsXStatus"
        private const val PREFS_EXCLUDE_DONE = "prefsExcludeDone"
        private const val PREFS_ORDERBY = "prefsOrderBy"
        private const val PREFS_SORTORDER = "prefsSortOrder"
        private const val PREFS_ORDERBY2 = "prefsOrderBy2"
        private const val PREFS_SORTORDER2 = "prefsSortOrder2"
        private const val PREFS_GROUPBY = "prefsGroupBy"
        private const val PREFS_SUBTASKS_ORDERBY = "prefsSubtasksOrderBy"
        private const val PREFS_SUBTASKS_SORTORDER = "prefsSubtasksSortOrder"
        private const val PREFS_SUBNOTES_ORDERBY = "prefsSubnotesOrderBy"
        private const val PREFS_SUBNOTES_SORTORDER = "prefsSubnotesSortOrder"
        private const val PREFS_SHOW_ONLY_SEARCH_MATCHING_SUBENTRIES = "prefsShowOnlySearchMatchingSubentries"
        private const val PREFS_FILTER_OVERDUE = "prefsFilterOverdue"
        private const val PREFS_FILTER_DUE_TODAY = "prefsFilterToday"
        private const val PREFS_FILTER_DUE_TOMORROW = "prefsFilterTomorrow"
        private const val PREFS_FILTER_DUE_WITHIN_7_DAYS = "prefsFilterDueWithin7Days"
        private const val PREFS_FILTER_DUE_FUTURE = "prefsFilterFuture"
        private const val PREFS_FILTER_NO_DATES_SET = "prefsFilterNoDatesSet"
        private const val PREFS_FILTER_NO_START_DATE_SET = "prefsFilterNoStartDateSet"
        private const val PREFS_FILTER_NO_DUE_DATE_SET = "prefsFilterNoDueDateSet"
        private const val PREFS_FILTER_NO_COMPLETED_DATE_SET = "prefsFilterNoCompletedDateSet"
        private const val PREFS_FILTER_START_IN_PAST = "prefsFilterStartOverdue"
        private const val PREFS_FILTER_START_TODAY = "prefsFilterStartToday"
        private const val PREFS_FILTER_START_TOMORROW = "prefsFilterStartTomorrow"
        private const val PREFS_FILTER_START_WITHIN_7_DAYS = "prefsFilterStartWithin7Days"
        private const val PREFS_FILTER_START_FUTURE = "prefsFilterStartFuture"

        private const val PREFS_FILTER_START_RANGE_START = "prefsFilterStartRangeStart"
        private const val PREFS_FILTER_START_RANGE_END = "prefsFilterStartRangeEnd"
        private const val PREFS_FILTER_DUE_RANGE_START = "prefsFilterDueRangeStart"
        private const val PREFS_FILTER_DUE_RANGE_END = "prefsFilterDueRangeEnd"
        private const val PREFS_FILTER_COMPLETED_RANGE_START = "prefsFilterCompletedRangeStart"
        private const val PREFS_FILTER_COMPLETED_RANGE_END = "prefsFilterCompletedRangeEnd"

        private const val PREFS_VIEWMODE = "prefsViewmodeList"
        private const val PREFS_LAST_COLLECTION = "prefsLastUsedCollection"
        private const val PREFS_FLAT_VIEW = "prefsFlatView"
        private const val PREFS_SHOW_ONE_RECUR_ENTRY_IN_FUTURE = "prefsShowOneRecurEntryInFuture"
        private const val PREFS_MARKDOWN_ENABLED = "prefsMarkdownEnabled"
        private const val PREFS_FILTER_NO_CATEGORY_SET = "prefsFilterNoCategorySet"
        private const val PREFS_FILTER_NO_RESOURCE_SET = "prefsFilterNoResourceSet"

        private const val PREFS_TOPAPPBAR_MODE = "topAppBarMode"
        private const val PREFS_TOPAPPBAR_COLLECTION_ID = "topAppBarCollectionId"

        @Deprecated("Kept for legacy handling only, remove in future") private const val PREFS_KANBAN_COLUMNS_STATUS = "kanbanColumnsStatus"
        @Deprecated("Kept for legacy handling only, remove in future") private const val PREFS_KANBAN_COLUMNS_EXTENDED_STATUS = "kanbanColumnsXStatus"
        @Deprecated("Kept for legacy handling only, remove in future") private const val PREFS_KANBAN_COLUMNS_CATEGORY = "kanbanColumnsCategory"
        private const val PREFS_KANBAN_COLUMNS_STATUS2 = "kanbanColumnsStatus2"
        private const val PREFS_KANBAN_COLUMNS_EXTENDED_STATUS2 = "kanbanColumnsXStatus2"
        private const val PREFS_KANBAN_COLUMNS_CATEGORY2 = "kanbanColumnsCategory2"
        private const val PREFS_COLLAPSED_GROUPS = "collapsedGroups"

        //private const val PREFS_CHECKBOX_POSITION_END = "prefsCheckboxPosition"
        //private const val PREFS_WIDGET_ALPHA = "prefsWidgetAlpha"
        //private const val PREFS_WIDGET_ALPHA_ENTRIES = "prefsWidgetAlhpaEntries"

        fun getProtectedClassificationsFromSettings(context: Context) =
            when(SettingsStateHolder(context).settingProtectBiometric.value) {
                DropdownSettingOption.PROTECT_BIOMETRIC_ALL -> Classification.entries
                DropdownSettingOption.PROTECT_BIOMETRIC_CONFIDENTIAL -> listOf(Classification.CONFIDENTIAL)
                DropdownSettingOption.PROTECT_BIOMETRIC_PRIVATE_CONFIDENTIAL -> listOf(Classification.PRIVATE, Classification.CONFIDENTIAL)
                else -> emptyList()
        }

        fun fromPrefs(prefs: SharedPreferences) = ListSettings().apply {

            isExcludeDone.value = prefs.getBoolean(PREFS_EXCLUDE_DONE, false)
            isFilterOverdue.value = prefs.getBoolean(PREFS_FILTER_OVERDUE, false)
            isFilterDueToday.value = prefs.getBoolean(PREFS_FILTER_DUE_TODAY, false)
            isFilterDueTomorrow.value = prefs.getBoolean(PREFS_FILTER_DUE_TOMORROW, false)
            isFilterDueWithin7Days.value = prefs.getBoolean(PREFS_FILTER_DUE_WITHIN_7_DAYS, false)
            isFilterDueFuture.value = prefs.getBoolean(PREFS_FILTER_DUE_FUTURE, false)
            isFilterStartInPast.value = prefs.getBoolean(PREFS_FILTER_START_IN_PAST, false)
            isFilterStartToday.value = prefs.getBoolean(PREFS_FILTER_START_TODAY, false)
            isFilterStartTomorrow.value = prefs.getBoolean(PREFS_FILTER_START_TOMORROW, false)
            isFilterStartWithin7Days.value = prefs.getBoolean(PREFS_FILTER_START_WITHIN_7_DAYS, false)
            isFilterStartFuture.value = prefs.getBoolean(PREFS_FILTER_START_FUTURE, false)
            isFilterNoDatesSet.value = prefs.getBoolean(PREFS_FILTER_NO_DATES_SET, false)
            isFilterNoStartDateSet.value = prefs.getBoolean(PREFS_FILTER_NO_START_DATE_SET, false)
            isFilterNoDueDateSet.value = prefs.getBoolean(PREFS_FILTER_NO_DUE_DATE_SET, false)
            isFilterNoCompletedDateSet.value = prefs.getBoolean(PREFS_FILTER_NO_COMPLETED_DATE_SET, false)
            isFilterNoCategorySet.value = prefs.getBoolean(PREFS_FILTER_NO_CATEGORY_SET, false)
            isFilterNoResourceSet.value = prefs.getBoolean(PREFS_FILTER_NO_RESOURCE_SET, false)

            filterStartRangeStart.value = prefs.getLong(PREFS_FILTER_START_RANGE_START, 0L).takeIf { it != 0L }
            filterStartRangeEnd.value = prefs.getLong(PREFS_FILTER_START_RANGE_END, 0L).takeIf { it != 0L }
            filterDueRangeStart.value = prefs.getLong(PREFS_FILTER_DUE_RANGE_START, 0L).takeIf { it != 0L }
            filterDueRangeEnd.value = prefs.getLong(PREFS_FILTER_DUE_RANGE_END, 0L).takeIf { it != 0L }
            filterCompletedRangeStart.value = prefs.getLong(PREFS_FILTER_COMPLETED_RANGE_START, 0L).takeIf { it != 0L }
            filterCompletedRangeEnd.value = prefs.getLong(PREFS_FILTER_COMPLETED_RANGE_END, 0L).takeIf { it != 0L }

            //searchOrganizers =
            searchCategories.addAll(prefs.getStringSet(PREFS_CATEGORIES, emptySet())?.toList() ?: emptyList())
            searchCategoriesAnyAllNone.value = prefs.getString(PREFS_CATEGORIES_ANYALLNONE, null)?.let { try { AnyAllNone.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: AnyAllNone.ANY
            searchResources.addAll(prefs.getStringSet(PREFS_RESOURCES, emptySet())?.toList() ?: emptyList())
            searchResourcesAnyAllNone.value = prefs.getString(PREFS_RESOURCES_ANYALLNONE, null)?.let { try { AnyAllNone.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: AnyAllNone.ANY
            searchStatus.addAll(Status.getListFromStringList(prefs.getStringSet(PREFS_STATUS, null)))
            searchXStatus.addAll(prefs.getStringSet(PREFS_EXTENDED_STATUS, emptySet())?.toList() ?: emptyList())
            searchClassification.addAll(Classification.getListFromStringList(prefs.getStringSet(PREFS_CLASSIFICATION, null)))
            searchPriority.addAll(prefs.getStringSet(PREFS_PRIORITY, null)?.map { it.toIntOrNull() } ?: emptyList())
            searchCollection.addAll(prefs.getStringSet(PREFS_COLLECTION, emptySet())?.toList() ?: emptyList())
            searchAccount.addAll(prefs.getStringSet(PREFS_ACCOUNT, emptySet())?.toList() ?: emptyList())

            orderBy.value = prefs.getString(PREFS_ORDERBY, null)?.let { try { OrderBy.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: OrderBy.DRAG_AND_DROP
            sortOrder.value = prefs.getString(PREFS_SORTORDER, null)?.let { try { SortOrder.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: SortOrder.ASC
            orderBy2.value = prefs.getString(PREFS_ORDERBY2, null)?.let { try { OrderBy.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: OrderBy.SUMMARY
            sortOrder2.value = prefs.getString(PREFS_SORTORDER2, null)?.let { try { SortOrder.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: SortOrder.ASC
            groupBy.value = prefs.getString(PREFS_GROUPBY, null)?.let { try { GroupBy.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } }
            showOnlySearchMatchingSubentries.value = prefs.getBoolean(PREFS_SHOW_ONLY_SEARCH_MATCHING_SUBENTRIES, false)

            subtasksOrderBy.value = prefs.getString(PREFS_SUBTASKS_ORDERBY, null)?.let { try { OrderBy.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: OrderBy.DRAG_AND_DROP
            subtasksSortOrder.value = prefs.getString(PREFS_SUBTASKS_SORTORDER, null)?.let { try { SortOrder.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: SortOrder.ASC
            subnotesOrderBy.value = prefs.getString(PREFS_SUBNOTES_ORDERBY, null)?.let { try { OrderBy.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: OrderBy.DRAG_AND_DROP
            subnotesSortOrder.value = prefs.getString(PREFS_SUBNOTES_SORTORDER, null)?.let { try { SortOrder.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: SortOrder.ASC

            viewMode.value = prefs.getString(PREFS_VIEWMODE, ViewMode.LIST.name)?.let { try { ViewMode.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: ViewMode.LIST
            flatView.value = prefs.getBoolean(PREFS_FLAT_VIEW, false)
            markdownEnabled.value = prefs.getBoolean(PREFS_MARKDOWN_ENABLED, false)

            // Legacy handling
            kanbanColumnsStatus.addAll(prefs.getStringSet(PREFS_KANBAN_COLUMNS_STATUS, emptySet())?.toList()?: emptyList())
            kanbanColumnsXStatus.addAll(prefs.getStringSet(PREFS_KANBAN_COLUMNS_EXTENDED_STATUS, emptySet())?.toList()?: emptyList())
            kanbanColumnsCategory.addAll(prefs.getStringSet(PREFS_KANBAN_COLUMNS_CATEGORY, emptySet())?.toList()?: emptyList())

            kanbanColumnsStatus.addAll(prefs.getString(PREFS_KANBAN_COLUMNS_STATUS2, null)?.let { Json.decodeFromString(it) }?: emptyList())
            kanbanColumnsXStatus.addAll(prefs.getString(PREFS_KANBAN_COLUMNS_EXTENDED_STATUS2, null)?.let { Json.decodeFromString(it) }?: emptyList())
            kanbanColumnsCategory.addAll(prefs.getString(PREFS_KANBAN_COLUMNS_CATEGORY2, null)?.let { Json.decodeFromString(it) }?: emptyList())
            collapsedGroups.addAll(prefs.getString(PREFS_COLLAPSED_GROUPS, null)?.let { Json.decodeFromString(it) }?: emptyList())

            showOneRecurEntryInFuture.value = prefs.getBoolean(PREFS_SHOW_ONE_RECUR_ENTRY_IN_FUTURE, false)

            topAppBarCollectionId.value = prefs.getLong(PREFS_TOPAPPBAR_COLLECTION_ID, 0L)
            topAppBarMode.value = try { ListTopAppBarMode.valueOf(prefs.getString(PREFS_TOPAPPBAR_MODE, null)?:ListTopAppBarMode.SEARCH.name) } catch (e: IllegalArgumentException) { ListTopAppBarMode.SEARCH }

        }

        fun fromListWidgetConfig(listWidgetConfig: ListWidgetConfig) = ListSettings().apply {
            isExcludeDone.value = listWidgetConfig.isExcludeDone
            isFilterOverdue.value = listWidgetConfig.isFilterOverdue
            isFilterDueToday.value = listWidgetConfig.isFilterDueToday
            isFilterDueTomorrow.value = listWidgetConfig.isFilterDueTomorrow
            isFilterDueWithin7Days.value = listWidgetConfig.isFilterDueWithin7Days
            isFilterDueFuture.value = listWidgetConfig.isFilterDueFuture
            isFilterStartInPast.value = listWidgetConfig.isFilterStartInPast
            isFilterStartToday.value = listWidgetConfig.isFilterStartToday
            isFilterStartTomorrow.value = listWidgetConfig.isFilterStartTomorrow
            isFilterStartWithin7Days.value = listWidgetConfig.isFilterStartWithin7Days
            isFilterStartFuture.value = listWidgetConfig.isFilterStartFuture
            isFilterNoDatesSet.value = listWidgetConfig.isFilterNoDatesSet
            isFilterNoStartDateSet.value = listWidgetConfig.isFilterNoStartDateSet
            isFilterNoDueDateSet.value = listWidgetConfig.isFilterNoDueDateSet
            isFilterNoCompletedDateSet.value = listWidgetConfig.isFilterNoCompletedDateSet
            isFilterNoCategorySet.value = listWidgetConfig.isFilterNoCategorySet
            isFilterNoResourceSet.value = listWidgetConfig.isFilterNoResourceSet

            filterStartRangeStart.value = listWidgetConfig.filterStartRangeStart
            filterStartRangeEnd.value = listWidgetConfig.filterStartRangeEnd
            filterDueRangeStart.value = listWidgetConfig.filterDueRangeStart
            filterDueRangeEnd.value = listWidgetConfig.filterDueRangeEnd
            filterCompletedRangeStart.value = listWidgetConfig.filterCompletedRangeStart
            filterCompletedRangeEnd.value = listWidgetConfig.filterCompletedRangeEnd

            searchCategories.addAll(listWidgetConfig.searchCategories)
            searchCategoriesAnyAllNone.value = listWidgetConfig.searchCategoriesAnyAllNone
            searchResources.addAll(listWidgetConfig.searchResources)
            searchResourcesAnyAllNone.value = listWidgetConfig.searchResourcesAnyAllNone
            searchStatus.addAll(listWidgetConfig.searchStatus)
            searchXStatus.addAll(listWidgetConfig.searchXStatus)
            searchClassification.addAll(listWidgetConfig.searchClassification)
            searchPriority.addAll(listWidgetConfig.searchPriority)
            searchCollection.addAll(listWidgetConfig.searchCollection)
            searchAccount.addAll(listWidgetConfig.searchAccount)

            orderBy.value = listWidgetConfig.orderBy
            sortOrder.value = listWidgetConfig.sortOrder
            orderBy2.value = listWidgetConfig.orderBy2
            sortOrder2.value = listWidgetConfig.sortOrder2
            groupBy.value = listWidgetConfig.groupBy

            subtasksOrderBy.value = listWidgetConfig.subtasksOrderBy
            subtasksSortOrder.value = listWidgetConfig.subtasksSortOrder
            subnotesOrderBy.value = listWidgetConfig.subnotesOrderBy
            subnotesSortOrder.value = listWidgetConfig.subnotesSortOrder

            flatView.value = listWidgetConfig.flatView
            viewMode.value = listWidgetConfig.viewMode
            showOneRecurEntryInFuture.value = listWidgetConfig.showOneRecurEntryInFuture
            checkboxPosition.value = if(listWidgetConfig.checkboxPositionEnd) CheckboxPosition.END else listWidgetConfig.checkboxPosition
            showDescription.value = listWidgetConfig.showDescription
            showSubtasks.value = listWidgetConfig.showSubtasks
            showSubnotes.value = listWidgetConfig.showSubnotes
            widgetAlpha.value = listWidgetConfig.widgetAlpha
            widgetAlphaEntries.value = listWidgetConfig.widgetAlphaEntries
            widgetHeader.value = listWidgetConfig.widgetHeader
            widgetColor.value = listWidgetConfig.widgetColor
            widgetColorEntries.value = listWidgetConfig.widgetColorEntries
            defaultCategories.clear()
            defaultCategories.addAll(listWidgetConfig.defaultCategories)
        }
    }

    fun saveToPrefs(prefs: SharedPreferences) {
        prefs.edit().apply {
            putBoolean(PREFS_FILTER_OVERDUE, isFilterOverdue.value)
            putBoolean(PREFS_FILTER_DUE_TODAY, isFilterDueToday.value)
            putBoolean(PREFS_FILTER_DUE_TOMORROW, isFilterDueTomorrow.value)
            putBoolean(PREFS_FILTER_DUE_WITHIN_7_DAYS, isFilterDueWithin7Days.value)
            putBoolean(PREFS_FILTER_DUE_FUTURE, isFilterDueFuture.value)
            putBoolean(PREFS_FILTER_START_IN_PAST, isFilterStartInPast.value)
            putBoolean(PREFS_FILTER_START_TODAY, isFilterStartToday.value)
            putBoolean(PREFS_FILTER_START_TOMORROW, isFilterStartTomorrow.value)
            putBoolean(PREFS_FILTER_START_WITHIN_7_DAYS, isFilterStartWithin7Days.value)
            putBoolean(PREFS_FILTER_START_FUTURE, isFilterStartFuture.value)
            putBoolean(PREFS_FILTER_NO_DATES_SET, isFilterNoDatesSet.value)
            putBoolean(PREFS_FILTER_NO_START_DATE_SET, isFilterNoStartDateSet.value)
            putBoolean(PREFS_FILTER_NO_DUE_DATE_SET, isFilterNoDueDateSet.value)
            putBoolean(PREFS_FILTER_NO_COMPLETED_DATE_SET, isFilterNoCompletedDateSet.value)
            putBoolean(PREFS_FILTER_NO_CATEGORY_SET, isFilterNoCategorySet.value)
            putBoolean(PREFS_FILTER_NO_RESOURCE_SET, isFilterNoResourceSet.value)

            filterStartRangeStart.value?.let {
                putLong(PREFS_FILTER_START_RANGE_START, it)  }
                ?: remove(PREFS_FILTER_START_RANGE_START)
            filterStartRangeEnd.value?.let {
                putLong(PREFS_FILTER_START_RANGE_END, it)  }
                ?: remove(PREFS_FILTER_START_RANGE_END)
            filterDueRangeStart.value?.let {
                putLong(PREFS_FILTER_DUE_RANGE_START, it)  }
                ?: remove(PREFS_FILTER_DUE_RANGE_START)
            filterDueRangeEnd.value?.let {
                putLong(PREFS_FILTER_DUE_RANGE_END, it)  }
                ?: remove(PREFS_FILTER_DUE_RANGE_END)
            filterCompletedRangeStart.value?.let {
                putLong(PREFS_FILTER_COMPLETED_RANGE_START, it)  }
                ?: remove(PREFS_FILTER_COMPLETED_RANGE_START)
            filterCompletedRangeEnd.value?.let {
                putLong(PREFS_FILTER_COMPLETED_RANGE_END, it)  }
                ?: remove(PREFS_FILTER_COMPLETED_RANGE_END)

            putString(PREFS_ORDERBY, orderBy.value.name)
            putString(PREFS_SORTORDER, sortOrder.value.name)
            putString(PREFS_ORDERBY2, orderBy2.value.name)
            putString(PREFS_SORTORDER2, sortOrder2.value.name)
            groupBy.value?.name?.let { putString(PREFS_GROUPBY, it) } ?: remove(PREFS_GROUPBY)
            putBoolean(PREFS_SHOW_ONLY_SEARCH_MATCHING_SUBENTRIES, showOnlySearchMatchingSubentries.value)

            putString(PREFS_SUBTASKS_ORDERBY, subtasksOrderBy.value.name)
            putString(PREFS_SUBTASKS_SORTORDER, subtasksSortOrder.value.name)
            putString(PREFS_SUBNOTES_ORDERBY, subnotesOrderBy.value.name)
            putString(PREFS_SUBNOTES_SORTORDER, subnotesSortOrder.value.name)

            putBoolean(PREFS_EXCLUDE_DONE, isExcludeDone.value)

            putStringSet(PREFS_CATEGORIES, searchCategories.toSet())
            putString(PREFS_CATEGORIES_ANYALLNONE, searchCategoriesAnyAllNone.value.name)
            putStringSet(PREFS_RESOURCES, searchResources.toSet())
            putString(PREFS_RESOURCES_ANYALLNONE, searchResourcesAnyAllNone.value.name)
            putStringSet(PREFS_STATUS, Status.getStringSetFromList(searchStatus))
            putStringSet(PREFS_EXTENDED_STATUS, searchXStatus.toSet())
            putStringSet(PREFS_CLASSIFICATION, Classification.getStringSetFromList(searchClassification))
            putStringSet(PREFS_PRIORITY, searchPriority.map { it.toString() }.toSet())
            putStringSet(PREFS_COLLECTION, searchCollection.toSet())
            putStringSet(PREFS_ACCOUNT, searchAccount.toSet())

            putString(PREFS_VIEWMODE, viewMode.value.name)
            putBoolean(PREFS_FLAT_VIEW, flatView.value)
            putBoolean(PREFS_MARKDOWN_ENABLED, markdownEnabled.value)
            remove(PREFS_KANBAN_COLUMNS_STATUS)   // remove legacy config
            remove(PREFS_KANBAN_COLUMNS_EXTENDED_STATUS) // remove legacy config
            remove(PREFS_KANBAN_COLUMNS_CATEGORY)  // remove legacy config
            putString(PREFS_KANBAN_COLUMNS_STATUS2, Json.encodeToString(kanbanColumnsStatus.toList()))
            putString(PREFS_KANBAN_COLUMNS_EXTENDED_STATUS2, Json.encodeToString(kanbanColumnsXStatus.toList()))
            putString(PREFS_KANBAN_COLUMNS_CATEGORY2, Json.encodeToString(kanbanColumnsCategory.toList()))
            putString(PREFS_COLLAPSED_GROUPS, Json.encodeToString(collapsedGroups.toList()))

            putBoolean(PREFS_SHOW_ONE_RECUR_ENTRY_IN_FUTURE, showOneRecurEntryInFuture.value)

            putLong(PREFS_TOPAPPBAR_COLLECTION_ID, topAppBarCollectionId.value)
            putString(PREFS_TOPAPPBAR_MODE, topAppBarMode.value.name)
        }.apply()
    }

    fun reset() {
        searchCategories.clear()
        searchCategoriesAnyAllNone.value = AnyAllNone.ANY
        searchResources.clear()
        searchResourcesAnyAllNone.value = AnyAllNone.ANY
        //searchOrganizer = emptyList()
        searchStatus.clear()
        searchXStatus.clear()
        searchClassification.clear()
        searchPriority.clear()
        searchCollection.clear()
        searchAccount.clear()
        isExcludeDone.value = false
        isFilterStartInPast.value = false
        isFilterStartToday.value = false
        isFilterStartTomorrow.value = false
        isFilterStartWithin7Days.value = false
        isFilterStartFuture.value = false
        isFilterOverdue.value = false
        isFilterDueToday.value = false
        isFilterDueTomorrow.value = false
        isFilterDueWithin7Days.value = false
        isFilterDueFuture.value = false
        isFilterNoDatesSet.value = false
        isFilterNoStartDateSet.value = false
        isFilterNoDueDateSet.value = false
        isFilterNoCompletedDateSet.value = false
        isFilterNoCategorySet.value = false
        isFilterNoResourceSet.value = false

        filterStartRangeStart.value = null
        filterStartRangeEnd.value = null
        filterDueRangeStart.value = null
        filterDueRangeEnd.value = null
        filterCompletedRangeStart.value = null
        filterCompletedRangeEnd.value = null
    }

    fun getLastUsedCollectionId(prefs: SharedPreferences) = prefs.getLong(PREFS_LAST_COLLECTION, 0L)
    fun saveLastUsedCollectionId(prefs: SharedPreferences, collectionId: Long) = prefs.edit()?.putLong(PREFS_LAST_COLLECTION, collectionId)?.apply()

    fun isFilterActive() =
        searchCategories.isNotEmpty()
                || searchResources.isNotEmpty()
                //|| searchOrganizers.value.isNotEmpty()
                || searchStatus.isNotEmpty()
                || searchXStatus.isNotEmpty()
                || searchClassification.isNotEmpty()
                || searchPriority.isNotEmpty()
                || searchCollection.isNotEmpty()
                || searchAccount.isNotEmpty()
                || isExcludeDone.value
                || isFilterStartInPast.value
                || isFilterStartToday.value
                || isFilterStartTomorrow.value
                || isFilterStartWithin7Days.value
                || isFilterStartFuture.value
                || isFilterOverdue.value
                || isFilterDueToday.value
                || isFilterDueTomorrow.value
                || isFilterDueWithin7Days.value
                || isFilterDueFuture.value
                || isFilterNoDatesSet.value
                || isFilterNoStartDateSet.value
                || isFilterNoDueDateSet.value
                || isFilterNoCompletedDateSet.value
                || isFilterNoCategorySet.value
                || isFilterNoResourceSet.value
                || filterStartRangeStart.value != null
                || filterStartRangeEnd.value != null
                || filterDueRangeStart.value != null
                || filterDueRangeEnd.value != null
                || filterCompletedRangeStart.value != null
                || filterCompletedRangeEnd.value != null
}