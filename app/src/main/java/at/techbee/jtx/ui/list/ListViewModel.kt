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
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import androidx.sqlite.db.SimpleSQLiteQuery
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.ICalObject.Companion.TZ_ALLDAY
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.SyncUtil
import at.techbee.jtx.util.getPackageInfoCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


open class ListViewModel(application: Application, val module: Module) : AndroidViewModel(application) {

    private val _application = application
    private var database: ICalDatabaseDao = ICalDatabase.getInstance(application).iCalDatabaseDao
    private val settings = PreferenceManager.getDefaultSharedPreferences(application)

    private val prefs: SharedPreferences = when (module) {
        Module.JOURNAL -> application.getSharedPreferences(PREFS_LIST_JOURNALS, Context.MODE_PRIVATE)
        Module.NOTE -> application.getSharedPreferences(PREFS_LIST_NOTES, Context.MODE_PRIVATE)
        Module.TODO -> application.getSharedPreferences(PREFS_LIST_TODOS, Context.MODE_PRIVATE)
    }

    val listSettings = ListSettings.fromPrefs(prefs)


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
    val allWriteableCollections = database.getAllWriteableCollections()
    val allCollections = database.getAllCollections(module = module.name)


    var sqlConstraintException = mutableStateOf(false)
    val scrollOnceId = MutableLiveData<Long?>(null)
    var goToEdit = MutableLiveData<Long?>(null)
    var toastMessage = mutableStateOf<String?>(null)


    init {
        updateSearch()

        // only ad the welcomeEntries on first install and exclude all installs that didn't have this preference before (installed before 1641596400000L = 2022/01/08
        val firstInstall = application.packageManager
            ?.getPackageInfoCompat(application.packageName, 0)
            ?.firstInstallTime ?: System.currentTimeMillis()

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


    /**
     * updates the search by constructing a new query and by posting the
     * new query in the listQuery variable. This can trigger an
     * observer in the fragment.
     */
    fun updateSearch(saveListSettings: Boolean = false) {
        val query = ICal4List.constructQuery(
            module = module,
            searchCategories = listSettings.searchCategories.value,
            searchStatusTodo =  listSettings.searchStatusTodo.value,
            searchStatusJournal = listSettings.searchStatusJournal.value,
            searchClassification = listSettings.searchClassification.value,
            searchCollection = listSettings.searchCollection.value,
            searchAccount = listSettings.searchAccount.value,
            orderBy = listSettings.orderBy.value,
            sortOrder = listSettings.sortOrder.value,
            orderBy2 = listSettings.orderBy2.value,
            sortOrder2 = listSettings.sortOrder2.value,
            isExcludeDone = listSettings.isExcludeDone.value,
            isFilterOverdue = listSettings.isFilterOverdue.value,
            isFilterDueToday = listSettings.isFilterDueToday.value,
            isFilterDueTomorrow = listSettings.isFilterDueTomorrow.value,
            isFilterDueFuture = listSettings.isFilterDueFuture.value,
            isFilterStartInPast = listSettings.isFilterStartInPast.value,
            isFilterStartToday = listSettings.isFilterStartToday.value,
            isFilterStartTomorrow = listSettings.isFilterStartTomorrow.value,
            isFilterStartFuture = listSettings.isFilterStartFuture.value,
            isFilterNoDatesSet = listSettings.isFilterNoDatesSet.value,
            searchText = listSettings.searchText.value,
            flatView = listSettings.flatView.value,
            searchSettingShowOneRecurEntryInFuture = listSettings.showOneRecurEntryInFuture.value
        )
        listQuery.postValue(query)
        if(saveListSettings)
            listSettings.saveToPrefs()
    }


    /**
     * Clears all search criteria (except for module) and updates the search
     */
    fun clearFilter() {
        listSettings.reset()
        listSettings.saveToPrefs()
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
    fun insertQuickItem(icalObject: ICalObject, categories: List<Category>, attachment: Attachment?, alarm: Alarm?, editAfterSaving: Boolean) {

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

                alarm?.let {
                    it.icalObjectId = newId
                    database.insertAlarm(it)
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
    PRIORITY(R.string.priority, "$COLUMN_PRIORITY IS NULL, $COLUMN_PRIORITY "),
    PROGRESS(R.string.progress, "$COLUMN_PERCENT ");

    companion object {
        fun getValuesFor(module: Module): Array<OrderBy> =
            when(module) {
                Module.JOURNAL -> arrayOf(START_VJOURNAL, CREATED, LAST_MODIFIED, SUMMARY)
                Module.NOTE -> arrayOf(CREATED, LAST_MODIFIED, SUMMARY)
                Module.TODO -> arrayOf(START_VTODO, DUE, COMPLETED, CREATED, LAST_MODIFIED, SUMMARY, PRIORITY, PROGRESS)
            }
    }
}

enum class SortOrder(val stringResource: Int, val queryAppendix: String) {
    ASC(R.string.filter_asc, "ASC"),
    DESC(R.string.filter_desc, "DESC")
}

enum class GroupBy(val stringResource: Int) {
    PRIORITY(R.string.priority),
    STATUS(R.string.status),
    CLASSIFICATION(R.string.classification),
    DATE(R.string.date),
    START(R.string.started),
    DUE(R.string.due);

    companion object {
        fun getValuesFor(module: Module): Array<GroupBy> =
            when(module) {
                Module.JOURNAL -> arrayOf(
                    DATE,
                    STATUS,
                    CLASSIFICATION
                )
                Module.NOTE -> arrayOf(
                    STATUS,
                    CLASSIFICATION
                )
                Module.TODO -> arrayOf(
                    START,
                    DUE,
                    STATUS,
                    CLASSIFICATION,
                    PRIORITY
                )
            }
    }
}

enum class ViewMode(val stringResource: Int) {
    LIST(R.string.menu_list_viewmode_list),
    GRID(R.string.menu_list_viewmode_grid),
    COMPACT(R.string.menu_list_viewmode_compact),
    KANBAN(R.string.menu_list_viewmode_kanban)
}
