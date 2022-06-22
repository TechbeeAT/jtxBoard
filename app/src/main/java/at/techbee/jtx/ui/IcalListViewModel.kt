/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import androidx.sqlite.db.SimpleSQLiteQuery
import at.techbee.jtx.ListSettings
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.ICalObject.Factory.TZ_ALLDAY
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICal4ListWithRelatedto
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.database.views.VIEW_NAME_ICAL4LIST
import at.techbee.jtx.ui.SettingsFragment.Companion.SETTINGS_SHOW_SUBJOURNALS_IN_LIST
import at.techbee.jtx.ui.SettingsFragment.Companion.SETTINGS_SHOW_SUBNOTES_IN_LIST
import at.techbee.jtx.ui.SettingsFragment.Companion.SETTINGS_SHOW_SUBTASKS_IN_LIST
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.SyncUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


open class IcalListViewModel(application: Application, val module: Module) : AndroidViewModel(application) {

    private var database: ICalDatabaseDao = ICalDatabase.getInstance(application).iCalDatabaseDao


    private val settings = PreferenceManager.getDefaultSharedPreferences(application)

    private val prefs: SharedPreferences = when (module) {
        Module.JOURNAL -> application.getSharedPreferences(PREFS_LIST_JOURNALS, Context.MODE_PRIVATE)
        Module.NOTE -> application.getSharedPreferences(PREFS_LIST_NOTES, Context.MODE_PRIVATE)
        Module.TODO -> application.getSharedPreferences(PREFS_LIST_TODOS, Context.MODE_PRIVATE)
    }

    val listSettings = MutableLiveData(ListSettings().apply { load(prefs) })

    private var listQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>()
    var iCal4List: LiveData<List<ICal4ListWithRelatedto>> = Transformations.switchMap(listQuery) {
        database.getIcalObjectWithRelatedto(it)
    }

        // TODO maybe retrieve all subtasks only when subtasks are needed!
    val allSubtasks: LiveData<List<ICal4List>> = database.getAllSubtasks()
    val allSubnotes: LiveData<List<ICal4List>> = database.getAllSubnotes()

    val allCategories = database.getAllCategories()   // filter FragmentDialog
    val allCollections = database.getAllCollections()

    val quickInsertedEntity = MutableLiveData<ICalEntity?>(null)
    val directEditEntity = MutableLiveData<ICalEntity?>(null)
    val scrollOnceId = MutableLiveData<Long?>(null)

    val isSynchronizing = MutableLiveData(false)

    var searchText: String = ""


    private val searchSettingShowAllSubtasksInTasklist: Boolean
        get() = settings?.getBoolean(SETTINGS_SHOW_SUBTASKS_IN_LIST, false) ?: false
    private val searchSettingShowAllSubnotesInNoteslist: Boolean
        get() =  settings?.getBoolean(SETTINGS_SHOW_SUBNOTES_IN_LIST, false) ?: false
    private val searchSettingShowAllSubjournalsinJournallist: Boolean
        get() = settings?.getBoolean(SETTINGS_SHOW_SUBJOURNALS_IN_LIST, false) ?: false



    init {
        updateSearch()
    }

    companion object {

        const val PREFS_LIST_JOURNALS = "prefsListJournals"
        const val PREFS_LIST_NOTES = "prefsListNotes"
        const val PREFS_LIST_TODOS = "prefsListTodos"
    }

    private fun constructQuery(): SimpleSQLiteQuery {

        val args = arrayListOf<String>()

// Beginning of query string
        var queryString = "SELECT DISTINCT $VIEW_NAME_ICAL4LIST.* FROM $VIEW_NAME_ICAL4LIST "
        if(listSettings.value?.searchCategories?.value?.isNotEmpty() == true)
            queryString += "LEFT JOIN $TABLE_NAME_CATEGORY ON $VIEW_NAME_ICAL4LIST.$COLUMN_ID = $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ICALOBJECT_ID "
//        if(listSettings.value.searchOrganizer.isNotEmpty())
//            queryString += "LEFT JOIN $TABLE_NAME_ORGANIZER ON $VIEW_NAME_ICAL4LIST.$COLUMN_ID = $TABLE_NAME_ORGANIZER.$COLUMN_ORGANIZER_ICALOBJECT_ID "
        if(listSettings.value?.searchCollection?.value?.isNotEmpty() == true || listSettings.value?.searchAccount?.value?.isNotEmpty() == true)
            queryString += "LEFT JOIN $TABLE_NAME_COLLECTION ON $VIEW_NAME_ICAL4LIST.$COLUMN_ICALOBJECT_COLLECTIONID = $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ID "  // +
        //     "LEFT JOIN vattendees ON icalobject._id = vattendees.icalObjectId " +
        //     "LEFT JOIN vorganizer ON icalobject._id = vorganizer.icalObjectId " +
        //     "LEFT JOIN vRelatedto ON icalobject._id = vRelatedto.icalObjectId "

        // First query parameter Component must always be present!
        queryString += "WHERE $COLUMN_MODULE = ? "
        args.add(module.name)

        // Query for the given text search from the action bar
        if (searchText.isNotEmpty() && searchText.length >= 2) {
            queryString += "AND ($VIEW_NAME_ICAL4LIST.$COLUMN_SUMMARY LIKE ? OR $VIEW_NAME_ICAL4LIST.$COLUMN_DESCRIPTION LIKE ?) "
            args.add(searchText)
            args.add(searchText)
        }

        // Query for the passed filter criteria from VJournalFilterFragment
        if (listSettings.value?.searchCategories?.value?.isNotEmpty() == true) {
            queryString += "AND $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_TEXT IN ("
            listSettings.value?.searchCategories?.value?.forEach {
                queryString += "?,"
                args.add(it)
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        /*
        if (listSettings.value.searchOrganizer.size > 0) {
            queryString += "AND $TABLE_NAME_ORGANIZER.$COLUMN_ORGANIZER_CALADDRESS IN ("
            listSettings.value.searchOrganizer.forEach {
                queryString += "?,"
                args.add(it)
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }
         */

        // Query for the passed filter criteria from FilterFragment
        if (listSettings.value?.searchStatusJournal?.value?.isNotEmpty() == true && (module == Module.JOURNAL || module == Module.NOTE)) {
            queryString += "AND $COLUMN_STATUS IN ("
            listSettings.value?.searchStatusJournal?.value?.forEach {
                queryString += "?,"
                args.add(it.toString())
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Query for the passed filter criteria from FilterFragment
        if (listSettings.value?.searchStatusTodo?.value?.isNotEmpty() == true && module == Module.TODO) {
            queryString += "AND $COLUMN_STATUS IN ("
            listSettings.value?.searchStatusTodo?.value?.forEach {
                queryString += "?,"
                args.add(it.toString())
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        if (listSettings.value?.isExcludeDone?.value == true)
            queryString += "AND $COLUMN_PERCENT IS NOT 100 "

        val dueQuery = mutableListOf<String>()
        if (listSettings.value?.isFilterOverdue?.value == true)
            dueQuery.add("$COLUMN_DUE < ${System.currentTimeMillis()}")
        if (listSettings.value?.isFilterDueToday?.value == true)
            dueQuery.add("$COLUMN_DUE BETWEEN ${DateTimeUtils.getTodayAsLong()} AND ${DateTimeUtils.getTodayAsLong()+ TimeUnit.DAYS.toMillis(1)-1}")
        if (listSettings.value?.isFilterDueTomorrow?.value == true)
            dueQuery.add("$COLUMN_DUE BETWEEN ${DateTimeUtils.getTodayAsLong()+ TimeUnit.DAYS.toMillis(1)} AND ${DateTimeUtils.getTodayAsLong() + TimeUnit.DAYS.toMillis(2)-1}")
        if (listSettings.value?.isFilterDueFuture?.value == true)
            dueQuery.add("$COLUMN_DUE > ${System.currentTimeMillis()}")
        if(dueQuery.isNotEmpty())
            queryString += " AND (${dueQuery.joinToString(separator = " OR ")}) "

        if(listSettings.value?.isFilterNoDatesSet?.value == true)
            queryString += "AND $COLUMN_DTSTART IS NULL AND $COLUMN_DUE IS NULL AND $COLUMN_COMPLETED IS NULL "

        // Query for the passed filter criteria from FilterFragment
        if (listSettings.value?.searchClassification?.value?.isNotEmpty() == true) {
            queryString += "AND $COLUMN_CLASSIFICATION IN ("
            listSettings.value?.searchClassification?.value?.forEach {
                queryString += "?,"
                args.add(it.toString())
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }


        // Query for the passed filter criteria from FilterFragment
        if (listSettings.value?.searchCollection?.value?.isNotEmpty() == true) {
            queryString += "AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_DISPLAYNAME IN ("
            listSettings.value?.searchCollection?.value?.forEach {
                queryString += "?,"
                args.add(it)
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Query for the passed filter criteria from FilterFragment
        if (listSettings.value?.searchAccount?.value?.isNotEmpty() == true) {
            queryString += "AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_NAME IN ("
            listSettings.value?.searchAccount?.value?.forEach {
                queryString += "?,"
                args.add(it)
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Exclude items that are Child items by checking if they appear in the linkedICalObjectId of relatedto!
        //queryString += "AND $VIEW_NAME_ICAL4LIST.$COLUMN_ID NOT IN (SELECT $COLUMN_RELATEDTO_LINKEDICALOBJECT_ID FROM $TABLE_NAME_RELATEDTO) "
        when (module) {
            Module.TODO -> {
                // we exclude all Children of Tasks from the List, as they never should appear as main tasks (they will later be added as subtasks in the observer)
                queryString += "AND $VIEW_NAME_ICAL4LIST.isChildOfTodo = 0 "

                // if the user did NOT set the option to see all tasks that are subtasks of Notes and Journals, then we exclude them here as well
                if (!searchSettingShowAllSubtasksInTasklist)
                    queryString += "AND $VIEW_NAME_ICAL4LIST.isChildOfJournal = 0 AND $VIEW_NAME_ICAL4LIST.isChildOfNote = 0 "

            }
            Module.NOTE -> {
                queryString += "AND $VIEW_NAME_ICAL4LIST.isChildOfNote = 0 "

                if (!searchSettingShowAllSubnotesInNoteslist)
                    queryString += "AND $VIEW_NAME_ICAL4LIST.isChildOfJournal = 0 AND $VIEW_NAME_ICAL4LIST.isChildOfTodo = 0 "

            }
            Module.JOURNAL -> {
                queryString += "AND $VIEW_NAME_ICAL4LIST.isChildOfJournal = 0 "

                if (!searchSettingShowAllSubjournalsinJournallist)
                    queryString += "AND $VIEW_NAME_ICAL4LIST.isChildOfNote = 0 AND $VIEW_NAME_ICAL4LIST.isChildOfTodo = 0 "
            }
        }

        queryString += listSettings.value?.orderBy?.value?.queryAppendix ?: ""
        listSettings.value?.sortOrder?.let { queryString += it.value.queryAppendix }

        //Log.println(Log.INFO, "queryString", queryString)
        //Log.println(Log.INFO, "queryStringArgs", args.joinToString(separator = ", "))

        return SimpleSQLiteQuery(queryString, args.toArray())
    }

    /**
     * updates the search by constructing a new query and by posting the
     * new query in the listQuery variable. This can trigger an
     * observer in the fragment.
     */
    fun updateSearch() {
        listQuery.postValue(constructQuery())
    }


    /**
     * Clears all search criteria (except for module) and updates the search
     */
    fun clearFilter() {
        listSettings.value?.reset()
        listSettings.value?.save(prefs)
        updateSearch()
    }


    fun updateProgress(itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean, scrollOnce: Boolean = false) {

        viewModelScope.launch(Dispatchers.IO) {
            val currentItem = database.getICalObjectById(itemId) ?: return@launch
            ICalObject.makeRecurringException(currentItem, database)
            val item = database.getSync(itemId)?.property  ?: return@launch
            item.setUpdatedProgress(newPercent)
            database.update(item)
            SyncUtil.notifyContentObservers(getApplication())
            if(scrollOnce)
                scrollOnceId.postValue(itemId)
        }
        if(isLinkedRecurringInstance)
            Toast.makeText(getApplication(), R.string.toast_item_is_now_recu_exception, Toast.LENGTH_LONG).show()
    }

    fun updateStatusJournal(itemId: Long, newStatusJournal: StatusJournal, isLinkedRecurringInstance: Boolean, scrollOnce: Boolean = false) {

        viewModelScope.launch(Dispatchers.IO) {
            val currentItem = database.getICalObjectById(itemId) ?: return@launch
            ICalObject.makeRecurringException(currentItem, database)
            val item = database.getSync(itemId)?.property ?: return@launch
            item.status = newStatusJournal.name
            database.update(item)
            SyncUtil.notifyContentObservers(getApplication())
            if(scrollOnce)
                scrollOnceId.postValue(itemId)
        }
        if(isLinkedRecurringInstance)
            Toast.makeText(getApplication(), R.string.toast_item_is_now_recu_exception, Toast.LENGTH_LONG).show()

    }


    fun delete(itemIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            itemIds.forEach { id ->
                ICalObject.deleteItemWithChildren(id, database)
            }
        }
    }

    /**
     * Inserts a new icalobject with categories
     * @param icalObject to be inserted
     * @param categories the list of categories that should be linked to the icalObject
     */
    fun insertQuickItem(icalObject: ICalObject, categories: List<Category>) {

        viewModelScope.launch(Dispatchers.IO) {
            val newId = database.insertICalObject(icalObject)

            categories.forEach {
                it.icalObjectId = newId
                database.insertCategory(it)
            }
            scrollOnceId.postValue(newId)
            quickInsertedEntity.postValue(database.getSync(newId))
        }
    }

    /**
     * Updates the expanded status of subtasks, subnotes and attachments in the DB
     */
    fun updateExpanded(icalObjectId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isAttachmentsExpanded: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            database.updateExpanded(icalObjectId, isSubtasksExpanded, isSubnotesExpanded, isAttachmentsExpanded)
        }
    }


    /**
     * This function takes an icalObjectId, retrives the icalObject and posts it  in the directEditEntity LiveData Object.
     * This can be observed and will forward the user to the edit fragment.
     * [icalObjectId] that should be opened in the edit view
     */
    fun postDirectEditEntity(icalObjectId: Long) {

        viewModelScope.launch(Dispatchers.IO) {
            directEditEntity.postValue(database.getSync(icalObjectId))
        }
    }

    /**
     * This function adds some welcome entries, this should only be used on the first install.
     * @param [context] to resolve localized string resources
     */
    fun addWelcomeEntries(context: Context) {

        val welcomeJournal = ICalObject.createJournal().apply {
            this.dtstart = DateTimeUtils.getTodayAsLong()
            this.dtstartTimezone = TZ_ALLDAY
            this.summary = context.getString(R.string.list_welcome_entry_journal_summary)
            this.description = context.getString(R.string.list_welcome_entry_journal_description)
        }

        val welcomeNote = ICalObject.createNote().apply {
            this.summary = context.getString(R.string.list_welcome_entry_note_summary)
            this.description = context.getString(R.string.list_welcome_entry_note_description)
        }

        val welcomeTodo = ICalObject.createTodo().apply {
            this.dtstart = DateTimeUtils.getTodayAsLong()
            this.dtstartTimezone = TZ_ALLDAY
            this.due = DateTimeUtils.getTodayAsLong() + 604800000  // = + one week in millis
            this.dueTimezone = TZ_ALLDAY
            this.summary = context.getString(R.string.list_welcome_entry_todo_summary)
            this.description = context.getString(R.string.list_welcome_entry_todo_description)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val wj = database.insertICalObject(welcomeJournal)
            val wn = database.insertICalObject(welcomeNote)
            val wt = database.insertICalObject(welcomeTodo)

            database.insertCategory(Category().apply {
                this.icalObjectId = wj
                this.text = context.getString(R.string.list_welcome_category)
            })
            database.insertCategory(Category().apply {
                this.icalObjectId = wn
                this.text = context.getString(R.string.list_welcome_category)
            })
            database.insertCategory(Category().apply {
                this.icalObjectId = wt
                this.text = context.getString(R.string.list_welcome_category)
            })
        }
    }
}


enum class OrderBy(val stringResource: Int, val queryAppendix: String) {
    START(R.string.started, "ORDER BY $COLUMN_DTSTART IS NULL, $COLUMN_DTSTART "),
    DUE(R.string.due, "ORDER BY $COLUMN_DUE IS NULL, $COLUMN_DUE "),
    COMPLETED(R.string.completed, "ORDER BY $COLUMN_COMPLETED IS NULL, $COLUMN_COMPLETED "),
    CREATED(R.string.filter_created, "ORDER BY $COLUMN_CREATED "),
    LAST_MODIFIED(R.string.filter_last_modified, "ORDER BY $COLUMN_LAST_MODIFIED "),
    SUMMARY(R.string.summary, "ORDER BY $COLUMN_SUMMARY "),
    PRIORITY(R.string.priority, "ORDER BY $COLUMN_PRIORITY IS NULL, $COLUMN_PRIORITY ");

    companion object {
        fun getValuesFor(module: Module): Array<OrderBy> =
            if(module == Module.JOURNAL || module == Module.NOTE)
                arrayOf(START, CREATED, LAST_MODIFIED, SUMMARY)
            else
                values()
    }
}

enum class SortOrder(val stringResource: Int, val queryAppendix: String) {
    ASC(R.string.filter_asc, "ASC "),
    DESC(R.string.filter_desc, "DESC ")
}

enum class ViewMode(val stringResource: Int) {
    LIST(R.string.menu_list_viewmode_list),
    GRID(R.string.menu_list_viewmode_grid),
    COMPACT(R.string.menu_list_viewmode_compact),
    KANBAN(R.string.menu_list_viewmode_kanban)
}

