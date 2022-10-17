package at.techbee.jtx

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.StatusJournal
import at.techbee.jtx.database.StatusTodo
import at.techbee.jtx.ui.list.OrderBy
import at.techbee.jtx.ui.list.SortOrder
import at.techbee.jtx.ui.list.ViewMode


data class ListSettings(
    val prefs: SharedPreferences
) {

    var searchCategories: MutableState<List<String>> = mutableStateOf(emptyList())
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
    var isExcludeDone: MutableState<Boolean> = mutableStateOf(false)
    var isFilterOverdue: MutableState<Boolean> = mutableStateOf(false)
    var isFilterDueToday: MutableState<Boolean> = mutableStateOf(false)
    var isFilterDueTomorrow: MutableState<Boolean> = mutableStateOf(false)
    var isFilterDueFuture: MutableState<Boolean> = mutableStateOf(false)
    var isFilterNoDatesSet: MutableState<Boolean> = mutableStateOf(false)
    var searchText: MutableState<String?> = mutableStateOf(null)        // search text is not saved!
    var viewMode: MutableState<ViewMode> = mutableStateOf(ViewMode.LIST)

    init {
        load()
    }


    companion object {
        private const val PREFS_COLLECTION = "prefsCollection"
        private const val PREFS_ACCOUNT = "prefsAccount"
        private const val PREFS_CATEGORIES = "prefsCategories"
        private const val PREFS_CLASSIFICATION = "prefsClassification"
        private const val PREFS_STATUS_JOURNAL = "prefsStatusJournal"
        private const val PREFS_STATUS_TODO = "prefsStatusTodo"
        private const val PREFS_EXCLUDE_DONE = "prefsExcludeDone"
        private const val PREFS_ORDERBY = "prefsOrderBy"
        private const val PREFS_SORTORDER = "prefsSortOrder"
        private const val PREFS_ORDERBY2 = "prefsOrderBy2"
        private const val PREFS_SORTORDER2 = "prefsSortOrder2"
        private const val PREFS_FILTER_OVERDUE = "prefsFilterOverdue"
        private const val PREFS_FILTER_DUE_TODAY = "prefsFilterToday"
        private const val PREFS_FILTER_DUE_TOMORROW = "prefsFilterTomorrow"
        private const val PREFS_FILTER_DUE_FUTURE = "prefsFilterFuture"
        private const val PREFS_FILTER_NO_DATES_SET = "prefsFilterNoDatesSet"
        private const val PREFS_VIEWMODE = "prefsViewmodeList"
    }


    private fun load() {

        isExcludeDone.value = prefs.getBoolean(PREFS_EXCLUDE_DONE, false)
        isFilterOverdue.value = prefs.getBoolean(PREFS_FILTER_OVERDUE, false)
        isFilterDueToday.value = prefs.getBoolean(PREFS_FILTER_DUE_TODAY, false)
        isFilterDueTomorrow.value = prefs.getBoolean(PREFS_FILTER_DUE_TOMORROW, false)
        isFilterDueFuture.value = prefs.getBoolean(PREFS_FILTER_DUE_FUTURE, false)
        isFilterNoDatesSet.value = prefs.getBoolean(PREFS_FILTER_NO_DATES_SET, false)

        //searchOrganizers =
        searchCategories.value = prefs.getStringSet(PREFS_CATEGORIES, null)?.toList() ?: emptyList()
        searchStatusJournal.value = StatusJournal.getListFromStringList(prefs.getStringSet(PREFS_STATUS_JOURNAL, null))
        searchStatusTodo.value = StatusTodo.getListFromStringList(prefs.getStringSet(PREFS_STATUS_TODO, null))
        searchCollection.value = prefs.getStringSet(PREFS_COLLECTION, null)?.toList() ?: emptyList()
        searchAccount.value = prefs.getStringSet(PREFS_ACCOUNT, null)?.toMutableList() ?: mutableListOf()
        orderBy.value = prefs.getString(PREFS_ORDERBY, null)?.let { try { OrderBy.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: OrderBy.DUE
        sortOrder.value = prefs.getString(PREFS_SORTORDER, null)?.let { try { SortOrder.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: SortOrder.ASC
        orderBy2.value = prefs.getString(PREFS_ORDERBY2, null)?.let { try { OrderBy.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: OrderBy.DUE
        sortOrder2.value = prefs.getString(PREFS_SORTORDER2, null)?.let { try { SortOrder.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: SortOrder.ASC

        viewMode.value = prefs.getString(PREFS_VIEWMODE, ViewMode.LIST.name)?.let { try { ViewMode.valueOf(it) } catch(e: java.lang.IllegalArgumentException) { null } } ?: ViewMode.LIST
    }

    fun save() {

        prefs.edit().apply {
            putBoolean(PREFS_FILTER_OVERDUE, isFilterOverdue.value)
            putBoolean(PREFS_FILTER_DUE_TODAY, isFilterDueToday.value)
            putBoolean(PREFS_FILTER_DUE_TOMORROW, isFilterDueTomorrow.value)
            putBoolean(PREFS_FILTER_DUE_FUTURE, isFilterDueFuture.value)
            putBoolean(PREFS_FILTER_NO_DATES_SET, isFilterNoDatesSet.value)
            putString(PREFS_ORDERBY, orderBy.value.name)
            putString(PREFS_SORTORDER, sortOrder.value.name)
            putString(PREFS_ORDERBY2, orderBy2.value.name)
            putString(PREFS_SORTORDER2, sortOrder2.value.name)
            putBoolean(PREFS_EXCLUDE_DONE, isExcludeDone.value)

            putStringSet(PREFS_CATEGORIES, searchCategories.value.toSet())
            putStringSet(PREFS_STATUS_JOURNAL, StatusJournal.getStringSetFromList(searchStatusJournal.value))
            putStringSet(PREFS_STATUS_TODO, StatusTodo.getStringSetFromList(searchStatusTodo.value))
            putStringSet(PREFS_CLASSIFICATION, Classification.getStringSetFromList(searchClassification.value))
            putStringSet(PREFS_COLLECTION, searchCollection.value.toSet())
            putStringSet(PREFS_ACCOUNT, searchAccount.value.toSet())

            putString(PREFS_VIEWMODE, viewMode.value.name)

        }.apply()
    }

    fun reset() {
        searchCategories.value = emptyList()
        //searchOrganizer = emptyList()
        searchStatusJournal.value = emptyList()
        searchStatusTodo.value = emptyList()
        searchClassification.value = emptyList()
        searchCollection.value = emptyList()
        searchAccount.value = emptyList()
        isExcludeDone.value = false
        isFilterOverdue.value = false
        isFilterDueToday.value = false
        isFilterDueTomorrow.value = false
        isFilterDueFuture.value = false
        isFilterNoDatesSet.value = false
    }

}
