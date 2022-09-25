/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager.PackageInfoFlags
import android.database.sqlite.SQLiteConstraintException
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import androidx.sqlite.db.SimpleSQLiteQuery
import at.techbee.jtx.ListSettings
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.ICalObject.Companion.TZ_ALLDAY
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.database.views.VIEW_NAME_ICAL4LIST
import at.techbee.jtx.ui.settings.SwitchSetting
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.SyncUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


open class ListViewModel(application: Application, val module: Module) : AndroidViewModel(application) {

    private val _application = application
    private var database: ICalDatabaseDao = ICalDatabase.getInstance(application).iCalDatabaseDao
    private val settings = PreferenceManager.getDefaultSharedPreferences(application)

    private val prefs: SharedPreferences = when (module) {
        Module.JOURNAL -> application.getSharedPreferences(PREFS_LIST_JOURNALS, Context.MODE_PRIVATE)
        Module.NOTE -> application.getSharedPreferences(PREFS_LIST_NOTES, Context.MODE_PRIVATE)
        Module.TODO -> application.getSharedPreferences(PREFS_LIST_TODOS, Context.MODE_PRIVATE)
    }

    val listSettings = ListSettings(prefs)


    private var listQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>()
    var iCal4List: LiveData<List<ICal4List>> = Transformations.switchMap(listQuery) {
        database.getIcal4List(it)
    }

    private val allSubtasksList: LiveData<List<ICal4List>> = database.getAllSubtasks()
    val allSubtasksMap = Transformations.map(allSubtasksList) { list ->
        return@map list.groupBy { it.vtodoUidOfParent }
    }

    private val allSubnotesList: LiveData<List<ICal4List>> = database.getAllSubnotes()
    val allSubnotesMap = Transformations.map(allSubnotesList) { list ->
        return@map list.groupBy { it.vjournalUidOfParent }
    }

    private val allAttachmentsList: LiveData<List<Attachment>> = database.getAllAttachments()
    val allAttachmentsMap = Transformations.map(allAttachmentsList) { list ->
        return@map list.groupBy { it.icalObjectId }
    }

    val allCategories = database.getAllCategoriesAsText()   // filter FragmentDialog
    val allCollections = database.getAllCollections()

    var sqlConstraintException = mutableStateOf(false)
    val scrollOnceId = MutableLiveData<Long?>(null)
    var goToEdit = MutableLiveData<Long?>(null)
    var toastMessage = mutableStateOf<String?>(null)


    private val searchSettingShowAllSubtasksInTasklist: Boolean
        get() = settings?.getBoolean(SwitchSetting.SETTING_SHOW_SUBTASKS_IN_TASKLIST.key, false) ?: false
    private val searchSettingShowAllSubnotesInNoteslist: Boolean
        get() =  settings?.getBoolean(SwitchSetting.SETTING_SHOW_SUBNOTES_IN_NOTESLIST.key, false) ?: false
    private val searchSettingShowAllSubjournalsinJournallist: Boolean
        get() = settings?.getBoolean(SwitchSetting.SETTING_SHOW_SUBJOURNALS_IN_JOURNALLIST.key, false) ?: false



    init {
        updateSearch()

        // only ad the welcomeEntries on first install and exclude all installs that didn't have this preference before (installed before 1641596400000L = 2022/01/08
        val firstInstall = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            application.packageManager?.getPackageInfo(application.packageName, PackageInfoFlags.of(0))?.firstInstallTime ?: System.currentTimeMillis()
        else
            application.packageManager?.getPackageInfo(application.packageName, 0)?.firstInstallTime ?: System.currentTimeMillis()

        if(settings.getBoolean(PREFS_ISFIRSTRUN, true)) {
            if (firstInstall > 1641596400000L)
                addWelcomeEntries(application)
            settings.edit().putBoolean(PREFS_ISFIRSTRUN, false).apply()
        }
    }

    companion object {

        const val PREFS_LIST_JOURNALS = "prefsListJournals"
        const val PREFS_LIST_NOTES = "prefsListNotes"
        const val PREFS_LIST_TODOS = "prefsListTodos"

        const val PREFS_ISFIRSTRUN = "isFirstRun"
    }

    private fun constructQuery(): SimpleSQLiteQuery {

        val args = arrayListOf<String>()

// Beginning of query string
        var queryString = "SELECT DISTINCT $VIEW_NAME_ICAL4LIST.* FROM $VIEW_NAME_ICAL4LIST "
        if(listSettings.searchCategories.value.isNotEmpty())
            queryString += "LEFT JOIN $TABLE_NAME_CATEGORY ON $VIEW_NAME_ICAL4LIST.$COLUMN_ID = $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ICALOBJECT_ID "
//        if(listSettings.value.searchOrganizer.isNotEmpty())
//            queryString += "LEFT JOIN $TABLE_NAME_ORGANIZER ON $VIEW_NAME_ICAL4LIST.$COLUMN_ID = $TABLE_NAME_ORGANIZER.$COLUMN_ORGANIZER_ICALOBJECT_ID "
        if(listSettings.searchCollection.value.isNotEmpty() || listSettings.searchAccount.value.isNotEmpty())
            queryString += "LEFT JOIN $TABLE_NAME_COLLECTION ON $VIEW_NAME_ICAL4LIST.$COLUMN_ICALOBJECT_COLLECTIONID = $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ID "  // +
        //     "LEFT JOIN vattendees ON icalobject._id = vattendees.icalObjectId " +
        //     "LEFT JOIN vorganizer ON icalobject._id = vorganizer.icalObjectId " +
        //     "LEFT JOIN vRelatedto ON icalobject._id = vRelatedto.icalObjectId "

        // First query parameter Component must always be present!
        queryString += "WHERE $COLUMN_MODULE = ? "
        args.add(module.name)

        // Query for the given text search from the action bar
        listSettings.searchText.value?.let { text ->
            if (text.length >= 2) {
                queryString += "AND ($VIEW_NAME_ICAL4LIST.$COLUMN_SUMMARY LIKE ? OR $VIEW_NAME_ICAL4LIST.$COLUMN_DESCRIPTION LIKE ?) "
                args.add("%" + text + "%")
                args.add("%" + text + "%")
            }
        }

        // Query for the passed filter criteria from VJournalFilterFragment
        if (listSettings.searchCategories.value.isNotEmpty()) {
            queryString += "AND $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_TEXT IN ("
            listSettings.searchCategories.value.forEach {
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
        if (listSettings.searchStatusJournal.value.isNotEmpty() && (module == Module.JOURNAL || module == Module.NOTE)) {
            queryString += "AND $COLUMN_STATUS IN ("
            listSettings.searchStatusJournal.value.forEach {
                queryString += "?,"
                args.add(it.toString())
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Query for the passed filter criteria from FilterFragment
        if (listSettings.searchStatusTodo.value.isNotEmpty() && module == Module.TODO) {
            queryString += "AND $COLUMN_STATUS IN ("
            listSettings.searchStatusTodo.value.forEach {
                queryString += "?,"
                args.add(it.toString())
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        if (listSettings.isExcludeDone.value)
            queryString += "AND $COLUMN_PERCENT IS NOT 100 "

        val dueQuery = mutableListOf<String>()
        if (listSettings.isFilterOverdue.value)
            dueQuery.add("$COLUMN_DUE < ${System.currentTimeMillis()}")
        if (listSettings.isFilterDueToday.value)
            dueQuery.add("$COLUMN_DUE BETWEEN ${DateTimeUtils.getTodayAsLong()} AND ${DateTimeUtils.getTodayAsLong()+ TimeUnit.DAYS.toMillis(1)-1}")
        if (listSettings.isFilterDueTomorrow.value)
            dueQuery.add("$COLUMN_DUE BETWEEN ${DateTimeUtils.getTodayAsLong()+ TimeUnit.DAYS.toMillis(1)} AND ${DateTimeUtils.getTodayAsLong() + TimeUnit.DAYS.toMillis(2)-1}")
        if (listSettings.isFilterDueFuture.value)
            dueQuery.add("$COLUMN_DUE > ${System.currentTimeMillis()}")
        if(dueQuery.isNotEmpty())
            queryString += " AND (${dueQuery.joinToString(separator = " OR ")}) "

        if(listSettings.isFilterNoDatesSet.value)
            queryString += "AND $COLUMN_DTSTART IS NULL AND $COLUMN_DUE IS NULL AND $COLUMN_COMPLETED IS NULL "

        // Query for the passed filter criteria from FilterFragment
        if (listSettings.searchClassification.value.isNotEmpty()) {
            queryString += "AND $COLUMN_CLASSIFICATION IN ("
            listSettings.searchClassification.value.forEach {
                queryString += "?,"
                args.add(it.toString())
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }


        // Query for the passed filter criteria from FilterFragment
        if (listSettings.searchCollection.value.isNotEmpty()) {
            queryString += "AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_DISPLAYNAME IN ("
            listSettings.searchCollection.value.forEach {
                queryString += "?,"
                args.add(it)
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Query for the passed filter criteria from FilterFragment
        if (listSettings.searchAccount.value.isNotEmpty()) {
            queryString += "AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_NAME IN ("
            listSettings.searchAccount.value.forEach {
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

        queryString += "ORDER BY "
        queryString += listSettings.orderBy.value.queryAppendix
        listSettings.sortOrder.let { queryString += it.value.queryAppendix }

        queryString += ", "
        queryString += listSettings.orderBy2.value.queryAppendix
        listSettings.sortOrder2.let { queryString += it.value.queryAppendix }

        //Log.println(Log.INFO, "queryString", queryString)
        //Log.println(Log.INFO, "queryStringArgs", args.joinToString(separator = ", "))

        return SimpleSQLiteQuery(queryString, args.toArray())
    }

    /**
     * updates the search by constructing a new query and by posting the
     * new query in the listQuery variable. This can trigger an
     * observer in the fragment.
     */
    fun updateSearch(saveListSettings: Boolean = false) {
        listQuery.postValue(constructQuery())
        if(saveListSettings)
            listSettings.save()
    }


    /**
     * Clears all search criteria (except for module) and updates the search
     */
    fun clearFilter() {
        listSettings.reset()
        listSettings.save()
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
            toastMessage.value = _application.getString(R.string.toast_item_is_now_recu_exception)
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
            toastMessage.value = _application.getString(R.string.toast_item_is_now_recu_exception)

    }

    /*
    Deletes all entries that are currently visible (present in iCal4List)
     */
    fun deleteVisible() {
        viewModelScope.launch(Dispatchers.IO) {
            iCal4List.value?.forEach { entry ->
                if(entry.isReadOnly || entry.isLinkedRecurringInstance)
                    return@forEach
                else
                    ICalObject.deleteItemWithChildren(entry.id, database)

            }
        }
    }

    /**
     * Inserts a new icalobject with categories
     * @param icalObject to be inserted
     * @param categories the list of categories that should be linked to the icalObject
     */
    fun insertQuickItem(icalObject: ICalObject, categories: List<Category>, attachment: Attachment?, editAfterSaving: Boolean) {

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newId = database.insertICalObject(icalObject)

                categories.forEach {
                    it.icalObjectId = newId
                    database.insertCategory(it)
                }

                attachment?.let {
                    it.icalObjectId = newId
                    database.insertAttachment(it)
                }

                scrollOnceId.postValue(newId)
                if (editAfterSaving)
                    goToEdit.postValue(newId)
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", "Corrupted ID: ${icalObject.id}")
                Log.d("SQLConstraint", e.stackTraceToString())
                sqlConstraintException.value = true
            }
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
     * This function adds some welcome entries, this should only be used on the first install.
     * @param [context] to resolve localized string resources
     */
    private fun addWelcomeEntries(context: Context) {

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

open class ListViewModelJournals(application: Application) : ListViewModel(application, Module.JOURNAL)
open class ListViewModelNotes(application: Application) : ListViewModel(application, Module.NOTE)
open class ListViewModelTodos(application: Application) : ListViewModel(application, Module.TODO)



enum class OrderBy(val stringResource: Int, val queryAppendix: String) {
    START_VTODO(R.string.started, "$COLUMN_DTSTART IS NULL, $COLUMN_DTSTART "),
    START_VJOURNAL(R.string.date, "$COLUMN_DTSTART IS NULL, $COLUMN_DTSTART "),
    DUE(R.string.due, "$COLUMN_DUE IS NULL, $COLUMN_DUE "),
    COMPLETED(R.string.completed, "$COLUMN_COMPLETED IS NULL, $COLUMN_COMPLETED "),
    CREATED(R.string.filter_created, "$COLUMN_CREATED "),
    LAST_MODIFIED(R.string.filter_last_modified, "$COLUMN_LAST_MODIFIED "),
    SUMMARY(R.string.summary, "UPPER($COLUMN_SUMMARY) "),
    PRIORITY(R.string.priority, "$COLUMN_PRIORITY IS NULL, $COLUMN_PRIORITY ");

    companion object {
        fun getValuesFor(module: Module): Array<OrderBy> =
            when(module) {
                Module.JOURNAL -> arrayOf(START_VJOURNAL, CREATED, LAST_MODIFIED, SUMMARY)
                Module.NOTE -> arrayOf(CREATED, LAST_MODIFIED, SUMMARY)
                Module.TODO -> arrayOf(START_VTODO, DUE, COMPLETED, CREATED, LAST_MODIFIED, SUMMARY, PRIORITY)
            }
    }
}

enum class SortOrder(val stringResource: Int, val queryAppendix: String) {
    ASC(R.string.filter_asc, "ASC"),
    DESC(R.string.filter_desc, "DESC")
}

enum class ViewMode(val stringResource: Int) {
    LIST(R.string.menu_list_viewmode_list),
    GRID(R.string.menu_list_viewmode_grid),
    COMPACT(R.string.menu_list_viewmode_compact),
    KANBAN(R.string.menu_list_viewmode_kanban)
}

