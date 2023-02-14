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
import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.ICalObject.Companion.TZ_ALLDAY
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.database.views.VIEW_NAME_ICAL4LIST
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.SyncUtil
import at.techbee.jtx.util.getPackageInfoCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


open class ListViewModel(application: Application, val module: Module) : AndroidViewModel(application) {

    private val _application = application
    private var database: ICalDatabaseDao = ICalDatabase.getInstance(application).iCalDatabaseDao
    private val settings = PreferenceManager.getDefaultSharedPreferences(application)

    val prefs: SharedPreferences = when (module) {
        Module.JOURNAL -> application.getSharedPreferences(PREFS_LIST_JOURNALS, Context.MODE_PRIVATE)
        Module.NOTE -> application.getSharedPreferences(PREFS_LIST_NOTES, Context.MODE_PRIVATE)
        Module.TODO -> application.getSharedPreferences(PREFS_LIST_TODOS, Context.MODE_PRIVATE)
    }

    val listSettings = ListSettings.fromPrefs(prefs)
    val settingsStateHolder = SettingsStateHolder(_application)


    private var listQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>()
    var iCal4List: LiveData<List<ICal4List>> = Transformations.switchMap(listQuery) {
        database.getIcal4List(it)
    }

    private var allSubtasksQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>()
    var allSubtasks: LiveData<List<ICal4ListRel>> = Transformations.switchMap(allSubtasksQuery) {
        database.getSubEntries(it)
    }

    private var allSubnotesQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>()
    var allSubnotes: LiveData<List<ICal4ListRel>> = Transformations.switchMap(allSubnotesQuery) {
        database.getSubEntries(it)
    }

    private var selectFromAllListQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>()
    var selectFromAllList: LiveData<List<ICal4List>> = Transformations.switchMap(selectFromAllListQuery) {
        database.getIcal4List(it)
    }

    private val allAttachmentsList: LiveData<List<Attachment>> = database.getAllAttachments()
    val allAttachmentsMap = Transformations.map(allAttachmentsList) { list ->
        return@map list.groupBy { it.icalObjectId }
    }

    val allCategories = database.getAllCategoriesAsText()
    val allResources = database.getAllResourcesAsText()   // filter FragmentDialog
    val allWriteableCollections = database.getAllWriteableCollections()
    val allCollections = database.getAllCollections(module = module.name)


    var sqlConstraintException = mutableStateOf(false)
    val scrollOnceId = MutableLiveData<Long?>(null)
    var goToEdit = MutableLiveData<Long?>(null)
    var toastMessage = mutableStateOf<String?>(null)

    val selectedEntries = mutableStateListOf<Long>()
    val multiselectEnabled = mutableStateOf(false)

    init {
        // only ad the welcomeEntries on first install and exclude all installs that didn't have this preference before (installed before 1641596400000L = 2022/01/08
        val firstInstall = application.packageManager
            ?.getPackageInfoCompat(application.packageName, 0)
            ?.firstInstallTime ?: System.currentTimeMillis()

        if(settings.getBoolean(PREFS_ISFIRSTRUN, true)) {    // never add welcome entries in instrumented tests
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
    fun updateSearch(saveListSettings: Boolean = false, isAuthenticated: Boolean) {
        val query = ICal4List.constructQuery(
            modules = listOf(module),
            searchCategories = listSettings.searchCategories.value,
            searchResources = listSettings.searchResources.value,
            searchStatus = listSettings.searchStatus.value,
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
            isFilterNoCategorySet = listSettings.isFilterNoCategorySet.value,
            isFilterNoResourceSet = listSettings.isFilterNoResourceSet.value,
            searchText = listSettings.searchText.value,
            flatView = listSettings.flatView.value,
            searchSettingShowOneRecurEntryInFuture = listSettings.showOneRecurEntryInFuture.value,
            hideBiometricProtected = if(isAuthenticated) emptyList() else  ListSettings.getProtectedClassificationsFromSettings(_application)
        )
        listQuery.postValue(query)

        allSubtasksQuery.postValue(ICal4List.getQueryForAllSubEntries(Component.VTODO, listSettings.subtasksOrderBy.value, listSettings.subtasksSortOrder.value))
        allSubnotesQuery.postValue(ICal4List.getQueryForAllSubEntries(Component.VJOURNAL, listSettings.subnotesOrderBy.value, listSettings.subnotesSortOrder.value))
        if(saveListSettings)
            listSettings.saveToPrefs(prefs)
    }

    fun updateSelectFromAllListQuery(searchText: String, isAuthenticated: Boolean) {
        selectFromAllListQuery.postValue(ICal4List.constructQuery(
            modules = listOf(Module.JOURNAL, Module.NOTE, Module.TODO),
            searchText = searchText,
            hideBiometricProtected = if(isAuthenticated) emptyList() else  ListSettings.getProtectedClassificationsFromSettings(_application)
        ))
    }

    
    fun updateProgress(itemId: Long, newPercent: Int, scrollOnce: Boolean = false) {

        viewModelScope.launch(Dispatchers.IO) {
            val currentItem = database.getICalObjectById(itemId) ?: return@launch
            currentItem.setUpdatedProgress(newPercent, settingsStateHolder.settingKeepStatusProgressCompletedInSync.value)
            database.update(currentItem)
            currentItem.makeSeriesDirty(database)

            if(settingsStateHolder.settingLinkProgressToSubtasks.value) {
                ICalObject.findTopParent(currentItem.id, database)?.let {
                    ICalObject.updateProgressOfParents(it.id, database, settingsStateHolder.settingKeepStatusProgressCompletedInSync.value)
                }
            }

            SyncUtil.notifyContentObservers(getApplication())
            if(scrollOnce)
                scrollOnceId.postValue(itemId)
        }
    }

    fun updateStatus(itemId: Long, newStatus: Status, scrollOnce: Boolean = false) {

        viewModelScope.launch(Dispatchers.IO) {
            val currentItem = database.getICalObjectById(itemId) ?: return@launch
            currentItem.status = newStatus.status
            if(settingsStateHolder.settingKeepStatusProgressCompletedInSync.value) {
                when(newStatus) {
                    Status.NEEDS_ACTION -> currentItem.setUpdatedProgress(0, true)
                    Status.IN_PROCESS -> currentItem.setUpdatedProgress(if(currentItem.percent !in 1..99) 1 else currentItem.percent, true)
                    Status.COMPLETED -> currentItem.setUpdatedProgress(100, true)
                    else -> { }
                }
            }
            currentItem.makeDirty()
            database.update(currentItem)
            currentItem.makeSeriesDirty(database)
            SyncUtil.notifyContentObservers(getApplication())
            if(scrollOnce)
                scrollOnceId.postValue(itemId)
        }
    }

    /*
    Deletes selected entries
     */
    fun deleteSelected() {
        viewModelScope.launch(Dispatchers.IO) {
            val selectedICal4List = database.getIcal4ListSync(
                SupportSQLiteQueryBuilder
                    .builder(VIEW_NAME_ICAL4LIST)
                    .selection("$COLUMN_ID IN (${selectedEntries.joinToString(separator = ",", transform = { "?"})})", selectedEntries.toTypedArray())
                    .create()
            )
            selectedICal4List.forEach { entry ->
                if(entry.isReadOnly)
                    return@forEach
                if(entry.recurid != null)
                    database.getICalObjectByIdSync(entry.id)?.let { ICalObject.unlinkFromSeries(it, database) }
                ICalObject.deleteItemWithChildren(entry.id, database)
                selectedEntries.clear()
            }
        }
    }

    /**
     * Adds new categories to the selected entries (if they don't exist already)
     * @param addedCategories that should be added
     * @param removedCategories that should be deleted
     */
    fun updateCategoriesOfSelected(addedCategories: List<String>, removedCategories: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {

            if(removedCategories.isNotEmpty())
                database.deleteCategoriesForICalObjects(removedCategories, selectedEntries)

            addedCategories.forEach { category ->
                selectedEntries.forEach { selected ->
                    if(database.getCategoryForICalObjectByName(selected, category) == null)
                        database.insertCategory(Category(icalObjectId = selected, text = category))
                }
            }
            makeSelectedDirty()
        }
    }

    fun moveSelectedToNewCollection(newCollection: ICalCollection) {
        viewModelScope.launch(Dispatchers.IO) {
            val newEntries = mutableListOf<Long>()

            selectedEntries.forEach { iCalObjectId ->
                try {
                    val newId = ICalObject.updateCollectionWithChildren(iCalObjectId, null, newCollection.collectionId, database, getApplication()) ?: return@forEach
                    newEntries.add(newId)
                    // once the newId is there, the local entries can be deleted (or marked as deleted)
                    ICalObject.deleteItemWithChildren(iCalObjectId, database)        // make sure to delete the old item (or marked as deleted - this is already handled in the function)
                    val newICalObject = database.getICalObjectByIdSync(newId)
                    if (newICalObject?.rrule != null)
                        newICalObject.recreateRecurring(getApplication())
                } catch (e: SQLiteConstraintException) {
                    Log.w("SQLConstraint", "Corrupted ID: $iCalObjectId")
                    Log.w("SQLConstraint", e.stackTraceToString())
                    //sqlConstraintException.value = true
                }
            }
            selectedEntries.clear()
            selectedEntries.addAll(newEntries)
        }
    }

    /**
     * Adds new resources to the selected entries (if they don't exist already)
     * @param addedResources that should be added
     * @param removedResources that should be deleted
     */
    fun updateResourcesToSelected(addedResources: List<String>, removedResources: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {

            if(removedResources.isNotEmpty())
                database.deleteResourcesForICalObjects(removedResources, selectedEntries)

            addedResources.forEach { resource ->
                selectedEntries.forEach { selected ->
                    if(database.getResourceForICalObjectByName(selected, resource) == null)
                        database.insertResource(Resource(icalObjectId = selected, text = resource))
                }
            }
            makeSelectedDirty()
        }
    }

    /**
     * Adds a new relatedTo to the selected entries
     * @param addedParent that should be added as a relation
     */
    fun addNewParentToSelected(addedParent: ICal4List) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedEntries.forEach { selected ->
                val existing = addedParent.uid?.let { database.findRelatedTo(selected, it, Reltype.PARENT.name) != null } ?: return@forEach
                if(!existing)
                    database.insertRelatedto(
                        Relatedto(
                            icalObjectId = selected,
                            text = addedParent.uid,
                            reltype = Reltype.PARENT.name
                        )
                    )
            }
            makeSelectedDirty()
        }
    }

    /**
     * Updates the currently selected entries to make them dirty
     */
    private suspend fun makeSelectedDirty() {
        selectedEntries.forEach { selected ->
            database.getICalObjectByIdSync(selected)?.let {
                database.update(it.apply { makeDirty() })
                it.makeSeriesDirty(database)
            }
        }
    }

    /**
     * Updates the status of the selected entries
     * @param newStatus to be set
     */
    fun updateStatusOfSelected(newStatus: Status) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedEntries.forEach { iCalObjectId ->
                database.getICalObjectByIdSync(iCalObjectId)?.let {
                    it.status = newStatus.status
                    when {
                        newStatus == Status.COMPLETED -> it.percent = 100
                        newStatus == Status.NEEDS_ACTION -> it.percent = 0
                        newStatus == Status.IN_PROCESS && it.percent !in 1..99 -> it.percent = 1
                    }
                    it.makeDirty()
                    database.update(it)
                    it.makeSeriesDirty(database)
                }
            }
        }
    }

    /**
     * Updates the classification of the selected entries
     * @param newClassification to be set
     */
    fun updateClassificationOfSelected(newClassification: Classification) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedEntries.forEach { iCalObjectId ->
                database.getICalObjectByIdSync(iCalObjectId)?.let {
                    it.classification = newClassification.classification
                    it.makeDirty()
                    database.update(it)
                    it.makeSeriesDirty(database)
                }
            }
        }
    }

    /**
     * Updates the priority of the selected entries
     * @param newPriority to be set
     */
    fun updatePriorityOfSelected(newPriority: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedEntries.forEach { iCalObjectId ->
                database.getICalObjectByIdSync(iCalObjectId)?.let {
                    it.priority = newPriority
                    it.makeDirty()
                    database.update(it)
                    it.makeSeriesDirty(database)
                }
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



enum class OrderBy(@StringRes val stringResource: Int, val queryAppendix: String) {
    START_VTODO(R.string.started, "$COLUMN_DTSTART IS NULL, $COLUMN_DTSTART "),
    START_VJOURNAL(R.string.date, "$COLUMN_DTSTART IS NULL, $COLUMN_DTSTART "),
    DUE(R.string.due, "$COLUMN_DUE IS NULL, $COLUMN_DUE "),
    COMPLETED(R.string.completed, "$COLUMN_COMPLETED IS NULL, $COLUMN_COMPLETED "),
    CREATED(R.string.filter_created, "$COLUMN_CREATED "),
    LAST_MODIFIED(R.string.filter_last_modified, "$COLUMN_LAST_MODIFIED "),
    SUMMARY(R.string.summary, "UPPER($COLUMN_SUMMARY) "),
    PRIORITY(R.string.priority, "$COLUMN_PRIORITY IS NULL, $COLUMN_PRIORITY "),
    CLASSIFICATION(R.string.classification, "$COLUMN_CLASSIFICATION IS NULL, $COLUMN_CLASSIFICATION "),
    STATUS(R.string.status, "$COLUMN_STATUS IS NULL, $COLUMN_STATUS "),
    PROGRESS(R.string.progress, "$COLUMN_PERCENT ");

    companion object {
        fun getValuesFor(module: Module): Array<OrderBy> =
            when(module) {
                Module.JOURNAL -> arrayOf(START_VJOURNAL, CREATED, LAST_MODIFIED, SUMMARY, STATUS, CLASSIFICATION)
                Module.NOTE -> arrayOf(CREATED, LAST_MODIFIED, SUMMARY, STATUS, CLASSIFICATION)
                Module.TODO -> arrayOf(START_VTODO, DUE, COMPLETED, CREATED, LAST_MODIFIED, SUMMARY, PRIORITY, PROGRESS, STATUS, CLASSIFICATION)
            }
    }
}

enum class SortOrder(@StringRes val stringResource: Int, val queryAppendix: String) {
    ASC(R.string.filter_asc, "ASC"),
    DESC(R.string.filter_desc, "DESC")
}

enum class GroupBy(@StringRes val stringResource: Int) {
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

enum class ViewMode(@StringRes val stringResource: Int) {
    LIST(R.string.menu_list_viewmode_list),
    GRID(R.string.menu_list_viewmode_grid),
    COMPACT(R.string.menu_list_viewmode_compact),
    KANBAN(R.string.menu_list_viewmode_kanban)
}
