/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list


import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.StatusJournal
import at.techbee.jtx.database.StatusTodo
import at.techbee.jtx.widgets.ListWidgetConfig


class ListSettings {

    var searchCategories: MutableState<List<String>> = mutableStateOf(emptyList())
    var searchResources: MutableState<List<String>> = mutableStateOf(emptyList())
    //var searchOrganizers: MutableState<List<String>> = mutableStateOf(emptyList())
    var searchStatusTodo: MutableState<List<StatusTodo>> = mutableStateOf(emptyList())
    var searchStatusJournal: MutableState<List<StatusJournal>> = mutableStateOf(emptyList())
    var searchClassification: MutableState<List<Classification>> = mutableStateOf(emptyList())
    var searchCollection: MutableState<List<String>> = mutableStateOf(emptyList())
    var searchAccount: MutableState<List<String>> = mutableStateOf(emptyList())
    var orderBy: MutableState<OrderBy> = mutableStateOf(OrderBy.CREATED)
    var sortOrder: MutableState<SortOrder> = mutableStateOf(SortOrder.ASC)
    var orderBy2: MutableState<OrderBy> = mutableStateOf(OrderBy.SUMMARY)
    var sortOrder2: MutableState<SortOrder> = mutableStateOf(SortOrder.ASC)
    var groupBy: MutableState<GroupBy?> = mutableStateOf(null)
    var isExcludeDone: MutableState<Boolean> = mutableStateOf(false)
    var isFilterOverdue: MutableState<Boolean> = mutableStateOf(false)
    var isFilterDueToday: MutableState<Boolean> = mutableStateOf(false)
    var isFilterDueTomorrow: MutableState<Boolean> = mutableStateOf(false)
    var isFilterDueFuture: MutableState<Boolean> = mutableStateOf(false)
    var isFilterStartInPast: MutableState<Boolean> = mutableStateOf(false)
    var isFilterStartToday: MutableState<Boolean> = mutableStateOf(false)
    var isFilterStartTomorrow: MutableState<Boolean> = mutableStateOf(false)
    var isFilterStartFuture: MutableState<Boolean> = mutableStateOf(false)
    var isFilterNoDatesSet: MutableState<Boolean> = mutableStateOf(false)
    var isFilterNoStatusSet: MutableState<Boolean> = mutableStateOf(false)
    var isFilterNoClassificationSet: MutableState<Boolean> = mutableStateOf(false)
    var isFilterNoCategorySet: MutableState<Boolean> = mutableStateOf(false)
    var isFilterNoResourceSet: MutableState<Boolean> = mutableStateOf(false)
    var searchText: MutableState<String?> = mutableStateOf(null)        // search text is not saved!
    var viewMode: MutableState<ViewMode> = mutableStateOf(ViewMode.LIST)
    var flatView: MutableState<Boolean> = mutableStateOf(false)
    var showOneRecurEntryInFuture: MutableState<Boolean> = mutableStateOf(false)

    var checkboxPositionEnd: MutableState<Boolean> = mutableStateOf(false)  // widget only
    var widgetAlpha: MutableState<Float> = mutableStateOf(1F)  // widget only
    var widgetAlphaEntries: MutableState<Float> = mutableStateOf(1F)  // widget only
    var showDescription: MutableState<Boolean> = mutableStateOf(true)  // widget only
    var showSubtasks: MutableState<Boolean> = mutableStateOf(true)  // widget only
    var showSubnotes: MutableState<Boolean> = mutableStateOf(true)  // widget only


    companion object {
        private const val PREFS_COLLECTION = "prefsCollection"
        private const val PREFS_ACCOUNT = "prefsAccount"
        private const val PREFS_CATEGORIES = "prefsCategories"
        private const val PREFS_RESOURCES = "prefsResources"
        private const val PREFS_CLASSIFICATION = "prefsClassification"
        private const val PREFS_STATUS_JOURNAL = "prefsStatusJournal"
        private const val PREFS_STATUS_TODO = "prefsStatusTodo"
        private const val PREFS_EXCLUDE_DONE = "prefsExcludeDone"
        private const val PREFS_ORDERBY = "prefsOrderBy"
        private const val PREFS_SORTORDER = "prefsSortOrder"
        private const val PREFS_ORDERBY2 = "prefsOrderBy2"
        private const val PREFS_SORTORDER2 = "prefsSortOrder2"
        private const val PREFS_GROUPBY = "prefsGroupBy"
        private const val PREFS_FILTER_OVERDUE = "prefsFilterOverdue"
        private const val PREFS_FILTER_DUE_TODAY = "prefsFilterToday"
        private const val PREFS_FILTER_DUE_TOMORROW = "prefsFilterTomorrow"
        private const val PREFS_FILTER_DUE_FUTURE = "prefsFilterFuture"
        private const val PREFS_FILTER_NO_DATES_SET = "prefsFilterNoDatesSet"
        private const val PREFS_FILTER_START_IN_PAST = "prefsFilterStartOverdue"
        private const val PREFS_FILTER_START_TODAY = "prefsFilterStartToday"
        private const val PREFS_FILTER_START_TOMORROW = "prefsFilterStartTomorrow"
        private const val PREFS_FILTER_START_FUTURE = "prefsFilterStartFuture"
        private const val PREFS_FILTER_NO_STATUS_SET = "prefsFilterNoStatusSet"
        private const val PREFS_FILTER_NO_CLASSIFICATION_SET = "prefsFilterNoClassificationSet"
        private const val PREFS_VIEWMODE = "prefsViewmodeList"
        private const val PREFS_LAST_COLLECTION = "prefsLastUsedCollection"
        private const val PREFS_FLAT_VIEW = "prefsFlatView"
        private const val PREFS_SHOW_ONE_RECUR_ENTRY_IN_FUTURE = "prefsShowOneRecurEntryInFuture"
        private const val PREFS_FILTER_NO_CATEGORY_SET = "prefsFilterNoCategorySet"
        private const val PREFS_FILTER_NO_RESOURCE_SET = "prefsFilterNoResourceSet"
        //private const val PREFS_CHECKBOX_POSITION_END = "prefsCheckboxPosition"
        //private const val PREFS_WIDGET_ALPHA = "prefsWidgetAlpha"
        //private const val PREFS_WIDGET_ALPHA_ENTRIES = "prefsWidgetAlhpaEntries"




        fun fromPrefs(prefs: SharedPreferences) = ListSettings().apply {

            isExcludeDone.value = prefs.getBoolean(PREFS_EXCLUDE_DONE, false)
            isFilterOverdue.value = prefs.getBoolean(PREFS_FILTER_OVERDUE, false)
            isFilterDueToday.value = prefs.getBoolean(PREFS_FILTER_DUE_TODAY, false)
            isFilterDueTomorrow.value = prefs.getBoolean(PREFS_FILTER_DUE_TOMORROW, false)
            isFilterDueFuture.value = prefs.getBoolean(PREFS_FILTER_DUE_FUTURE, false)
            isFilterStartInPast.value = prefs.getBoolean(PREFS_FILTER_START_IN_PAST, false)
            isFilterStartToday.value = prefs.getBoolean(PREFS_FILTER_START_TODAY, false)
            isFilterStartTomorrow.value = prefs.getBoolean(PREFS_FILTER_START_TOMORROW, false)
            isFilterStartFuture.value = prefs.getBoolean(PREFS_FILTER_START_FUTURE, false)
            isFilterNoDatesSet.value = prefs.getBoolean(PREFS_FILTER_NO_DATES_SET, false)
            isFilterNoStatusSet.value = prefs.getBoolean(PREFS_FILTER_NO_STATUS_SET, false)
            isFilterNoClassificationSet.value = prefs.getBoolean(PREFS_FILTER_NO_CLASSIFICATION_SET, false)
            isFilterNoCategorySet.value = prefs.getBoolean(PREFS_FILTER_NO_CATEGORY_SET, false)
            isFilterNoResourceSet.value = prefs.getBoolean(PREFS_FILTER_NO_RESOURCE_SET, false)

            //searchOrganizers =
            searchCategories.value = prefs.getStringSet(PREFS_CATEGORIES, null)?.toList() ?: emptyList()
            searchResources.value = prefs.getStringSet(PREFS_RESOURCES, null)?.toList() ?: emptyList()
            searchStatusJournal.value = StatusJournal.getListFromStringList(prefs.getStringSet(PREFS_STATUS_JOURNAL, null))
            searchStatusTodo.value = StatusTodo.getListFromStringList(prefs.getStringSet(PREFS_STATUS_TODO, null))
            searchCollection.value = prefs.getStringSet(PREFS_COLLECTION, null)?.toList() ?: emptyList()
            searchAccount.value = prefs.getStringSet(PREFS_ACCOUNT, null)?.toMutableList() ?: mutableListOf()
            orderBy.value = prefs.getString(PREFS_ORDERBY, null)?.let { try { OrderBy.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: OrderBy.DUE
            sortOrder.value = prefs.getString(PREFS_SORTORDER, null)?.let { try { SortOrder.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: SortOrder.ASC
            orderBy2.value = prefs.getString(PREFS_ORDERBY2, null)?.let { try { OrderBy.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: OrderBy.DUE
            sortOrder2.value = prefs.getString(PREFS_SORTORDER2, null)?.let { try { SortOrder.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: SortOrder.ASC
            groupBy.value = prefs.getString(PREFS_GROUPBY, null)?.let { try { GroupBy.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } }

            viewMode.value = prefs.getString(PREFS_VIEWMODE, ViewMode.LIST.name)?.let { try { ViewMode.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: ViewMode.LIST
            flatView.value = prefs.getBoolean(PREFS_FLAT_VIEW, false)

            showOneRecurEntryInFuture.value = prefs.getBoolean(PREFS_SHOW_ONE_RECUR_ENTRY_IN_FUTURE, false)
        }

        fun fromListWidgetConfig(listWidgetConfig: ListWidgetConfig) = ListSettings().apply {
            isExcludeDone.value = listWidgetConfig.isExcludeDone
            isFilterOverdue.value = listWidgetConfig.isFilterOverdue
            isFilterDueToday.value = listWidgetConfig.isFilterDueToday
            isFilterDueTomorrow.value = listWidgetConfig.isFilterDueTomorrow
            isFilterDueFuture.value = listWidgetConfig.isFilterDueFuture
            isFilterStartInPast.value = listWidgetConfig.isFilterStartInPast
            isFilterStartToday.value = listWidgetConfig.isFilterStartToday
            isFilterStartTomorrow.value = listWidgetConfig.isFilterStartTomorrow
            isFilterStartFuture.value = listWidgetConfig.isFilterStartFuture
            isFilterNoDatesSet.value = listWidgetConfig.isFilterNoDatesSet
            isFilterNoStatusSet.value = listWidgetConfig.isFilterNoStatusSet
            isFilterNoClassificationSet.value = listWidgetConfig.isFilterNoClassificationSet
            isFilterNoCategorySet.value = listWidgetConfig.isFilterNoCategorySet
            isFilterNoResourceSet.value = listWidgetConfig.isFilterNoResourceSet

            searchCategories.value = listWidgetConfig.searchCategories
            searchResources.value = listWidgetConfig.searchResources
            searchStatusJournal.value = listWidgetConfig.searchStatusJournal
            searchStatusTodo.value = listWidgetConfig.searchStatusTodo
            searchClassification.value = listWidgetConfig.searchClassification
            searchCollection.value = listWidgetConfig.searchCollection
            searchAccount.value = listWidgetConfig.searchAccount
            orderBy.value = listWidgetConfig.orderBy
            sortOrder.value = listWidgetConfig.sortOrder
            orderBy2.value = listWidgetConfig.orderBy2
            sortOrder2.value = listWidgetConfig.sortOrder2
            groupBy.value = listWidgetConfig.groupBy

            flatView.value = listWidgetConfig.flatView
            viewMode.value = listWidgetConfig.viewMode
            showOneRecurEntryInFuture.value = listWidgetConfig.showOneRecurEntryInFuture
            checkboxPositionEnd.value = listWidgetConfig.checkboxPositionEnd
            showDescription.value = listWidgetConfig.showDescription
            showSubtasks.value = listWidgetConfig.showSubtasks
            showSubnotes.value = listWidgetConfig.showSubnotes
            widgetAlpha.value = listWidgetConfig.widgetAlpha
            widgetAlphaEntries.value = listWidgetConfig.widgetAlphaEntries
        }
    }

    fun saveToPrefs(prefs: SharedPreferences) {
        prefs.edit().apply {
            putBoolean(PREFS_FILTER_OVERDUE, isFilterOverdue.value)
            putBoolean(PREFS_FILTER_DUE_TODAY, isFilterDueToday.value)
            putBoolean(PREFS_FILTER_DUE_TOMORROW, isFilterDueTomorrow.value)
            putBoolean(PREFS_FILTER_DUE_FUTURE, isFilterDueFuture.value)
            putBoolean(PREFS_FILTER_START_IN_PAST, isFilterStartInPast.value)
            putBoolean(PREFS_FILTER_START_TODAY, isFilterStartToday.value)
            putBoolean(PREFS_FILTER_START_TOMORROW, isFilterStartTomorrow.value)
            putBoolean(PREFS_FILTER_START_FUTURE, isFilterStartFuture.value)
            putBoolean(PREFS_FILTER_NO_DATES_SET, isFilterNoDatesSet.value)
            putBoolean(PREFS_FILTER_NO_STATUS_SET, isFilterNoStatusSet.value)
            putBoolean(PREFS_FILTER_NO_CLASSIFICATION_SET, isFilterNoClassificationSet.value)
            putBoolean(PREFS_FILTER_NO_CATEGORY_SET, isFilterNoCategorySet.value)
            putBoolean(PREFS_FILTER_NO_RESOURCE_SET, isFilterNoResourceSet.value)

            putString(PREFS_ORDERBY, orderBy.value.name)
            putString(PREFS_SORTORDER, sortOrder.value.name)
            putString(PREFS_ORDERBY2, orderBy2.value.name)
            putString(PREFS_SORTORDER2, sortOrder2.value.name)
            groupBy.value?.name?.let { putString(PREFS_GROUPBY, it) } ?: remove(PREFS_GROUPBY)
            putBoolean(PREFS_EXCLUDE_DONE, isExcludeDone.value)

            putStringSet(PREFS_CATEGORIES, searchCategories.value.toSet())
            putStringSet(PREFS_RESOURCES, searchResources.value.toSet())
            putStringSet(PREFS_STATUS_JOURNAL, StatusJournal.getStringSetFromList(searchStatusJournal.value))
            putStringSet(PREFS_STATUS_TODO, StatusTodo.getStringSetFromList(searchStatusTodo.value))
            putStringSet(PREFS_CLASSIFICATION, Classification.getStringSetFromList(searchClassification.value))
            putStringSet(PREFS_COLLECTION, searchCollection.value.toSet())
            putStringSet(PREFS_ACCOUNT, searchAccount.value.toSet())

            putString(PREFS_VIEWMODE, viewMode.value.name)
            putBoolean(PREFS_FLAT_VIEW, flatView.value)

            putBoolean(PREFS_SHOW_ONE_RECUR_ENTRY_IN_FUTURE, showOneRecurEntryInFuture.value)

        }.apply()
    }

    fun reset() {
        searchCategories.value = emptyList()
        searchResources.value = emptyList()
        //searchOrganizer = emptyList()
        searchStatusJournal.value = emptyList()
        searchStatusTodo.value = emptyList()
        searchClassification.value = emptyList()
        searchCollection.value = emptyList()
        searchAccount.value = emptyList()
        isExcludeDone.value = false
        isFilterStartInPast.value = false
        isFilterStartToday.value = false
        isFilterStartTomorrow.value = false
        isFilterStartFuture.value = false
        isFilterOverdue.value = false
        isFilterDueToday.value = false
        isFilterDueTomorrow.value = false
        isFilterDueFuture.value = false
        isFilterNoDatesSet.value = false
        isFilterNoStatusSet.value = false
        isFilterNoClassificationSet.value = false
        isFilterNoCategorySet.value = false
        isFilterNoResourceSet.value = false
    }

    fun getLastUsedCollectionId(prefs: SharedPreferences) = prefs.getLong(PREFS_LAST_COLLECTION, 0L)
    fun saveLastUsedCollectionId(prefs: SharedPreferences, collectionId: Long) = prefs.edit()?.putLong(PREFS_LAST_COLLECTION, collectionId)?.apply()
}