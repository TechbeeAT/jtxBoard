/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.accounts.Account
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteConstraintException
import android.media.MediaPlayer
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.compose
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import at.techbee.jtx.NotificationPublisher
import at.techbee.jtx.R
import at.techbee.jtx.database.COLUMN_CLASSIFICATION
import at.techbee.jtx.database.COLUMN_COLLECTION_ACCOUNT_NAME
import at.techbee.jtx.database.COLUMN_COLLECTION_DISPLAYNAME
import at.techbee.jtx.database.COLUMN_COMPLETED
import at.techbee.jtx.database.COLUMN_CREATED
import at.techbee.jtx.database.COLUMN_DTSTART
import at.techbee.jtx.database.COLUMN_DUE
import at.techbee.jtx.database.COLUMN_ID
import at.techbee.jtx.database.COLUMN_LAST_MODIFIED
import at.techbee.jtx.database.COLUMN_PERCENT
import at.techbee.jtx.database.COLUMN_PRIORITY
import at.techbee.jtx.database.COLUMN_STATUS
import at.techbee.jtx.database.COLUMN_SUMMARY
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalDatabaseDao
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.ICalObject.Companion.TZ_ALLDAY
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredListSetting
import at.techbee.jtx.database.locals.StoredListSettingData
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.properties.Relatedto
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.database.views.VIEW_NAME_ICAL4LIST
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.SyncUtil
import at.techbee.jtx.util.getPackageInfoCompat
import at.techbee.jtx.widgets.ListWidget
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
    val mediaPlayer = MediaPlayer()

    private var listQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>()
    var iCal4ListRel: LiveData<List<ICal4ListRel>> = listQuery.switchMap {
        database.getIcal4ListRel(it)
    }

    private var allSubtasksQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>()
    var allSubtasks: LiveData<List<ICal4ListRel>> = allSubtasksQuery.switchMap { database.getSubEntries(it) }

    private var allSubnotesQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>()
    var allSubnotes: LiveData<List<ICal4ListRel>> = allSubnotesQuery.switchMap { database.getSubEntries(it) }

    var allParents: LiveData<List<ICal4ListRel>> = database.getAllParents()

    private var selectFromAllListQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>()
    var selectFromAllList: LiveData<List<ICal4ListRel>> = selectFromAllListQuery.switchMap { database.getIcal4ListRel(it) }

    private val allAttachmentsList: LiveData<List<Attachment>> = database.getAllAttachments()
    val allAttachmentsMap = allAttachmentsList.map { list ->
        return@map list.groupBy { it.icalObjectId }
    }

    val allCategories = database.getAllCategoriesAsText()
    val allResources = database.getAllResourcesAsText()
    val allWriteableCollections = database.getAllWriteableCollections()
    val allCollections = database.getAllCollections(module = module.name)
    val storedListSettings = database.getStoredListSettings(module = module.name)
    val storedCategories = database.getStoredCategories()
    val storedResources = database.getStoredResources()
    val extendedStatuses = database.getStoredStatuses()

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
            searchCategories = listSettings.searchCategories,
            searchResources = listSettings.searchResources,
            searchStatus = listSettings.searchStatus,
            searchXStatus = listSettings.searchXStatus,
            searchClassification = listSettings.searchClassification,
            searchCollection = listSettings.searchCollection,
            searchAccount = listSettings.searchAccount,
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
            isFilterNoStartDateSet = listSettings.isFilterNoStartDateSet.value,
            isFilterNoDueDateSet = listSettings.isFilterNoDueDateSet.value,
            isFilterNoCompletedDateSet = listSettings.isFilterNoCompletedDateSet.value,
            isFilterNoCategorySet = listSettings.isFilterNoCategorySet.value,
            isFilterNoResourceSet = listSettings.isFilterNoResourceSet.value,
            searchText = listSettings.searchText.value,
            flatView = listSettings.flatView.value,
            searchSettingShowOneRecurEntryInFuture = listSettings.showOneRecurEntryInFuture.value,
            hideBiometricProtected = if(isAuthenticated) emptyList() else ListSettings.getProtectedClassificationsFromSettings(_application)
        )
        listQuery.postValue(query)

        allSubtasksQuery.postValue(
            ICal4List.getQueryForAllSubEntries(
                component = Component.VTODO,
                hideBiometricProtected = if(isAuthenticated) emptyList() else ListSettings.getProtectedClassificationsFromSettings(_application),
                orderBy = listSettings.subtasksOrderBy.value,
                sortOrder = listSettings.subtasksSortOrder.value
            )
        )
        allSubnotesQuery.postValue(
            ICal4List.getQueryForAllSubEntries(
                component = Component.VJOURNAL,
                hideBiometricProtected = if(isAuthenticated) emptyList() else ListSettings.getProtectedClassificationsFromSettings(_application),
                orderBy = listSettings.subnotesOrderBy.value,
                sortOrder = listSettings.subnotesSortOrder.value
            )
        )
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

            onChangeDone()
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
                    Status.IN_PROCESS -> currentItem.setUpdatedProgress(if(currentItem.percent !in 1..99) 1 else currentItem.percent, true)
                    Status.COMPLETED -> currentItem.setUpdatedProgress(100, true)
                    else -> { }
                }
            }
            currentItem.makeDirty()
            database.update(currentItem)
            currentItem.makeSeriesDirty(database)
            onChangeDone()
            if(scrollOnce)
                scrollOnceId.postValue(itemId)
        }
    }

    fun updateXStatus(itemId: Long, newXStatus: ExtendedStatus, scrollOnce: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentItem = database.getICalObjectById(itemId) ?: return@launch
            currentItem.xstatus = newXStatus.xstatus
            database.update(currentItem)
            updateStatus(itemId, newXStatus.rfcStatus, scrollOnce)
        }
    }

    fun swapCategories(iCalObjectId: Long, oldCategory: String, newCategory: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.swapCategories(iCalObjectId, oldCategory, newCategory)

            val currentItem = database.getICalObjectById(iCalObjectId) ?: return@launch
            currentItem.makeDirty()
            database.update(currentItem)
            currentItem.makeSeriesDirty(database)
            SyncUtil.notifyContentObservers(getApplication())
            NotificationPublisher.scheduleNextNotifications(getApplication())
            scrollOnceId.postValue(iCalObjectId)
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
                if(entry.iCal4List.isReadOnly)
                    return@forEach
                if(entry.iCal4List.recurid != null)
                    database.getICalObjectByIdSync(entry.iCal4List.id)?.let { ICalObject.unlinkFromSeries(it, database) }
                ICalObject.deleteItemWithChildren(entry.iCal4List.id, database)
                selectedEntries.clear()
            }
            onChangeDone()
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
            onChangeDone()
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
            onChangeDone()
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
            onChangeDone()
        }
    }

    /**
     * Adds a new relatedTo to the selected entries
     * @param addedParent that should be added as a relation
     */
    fun addNewParentToSelected(addedParent: ICal4List) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedEntries.forEach { selected ->

                val childUID = database.getICalObjectById(selected)?.uid ?: return@forEach
                if(addedParent.uid == childUID)
                    return@forEach

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
            onChangeDone()
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
    fun updateStatusOfSelected(newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedEntries.forEach { iCalObjectId ->
                database.getICalObjectByIdSync(iCalObjectId)?.let {
                    it.status = newStatus
                    when {
                        newStatus == Status.COMPLETED.status -> it.percent = 100
                        newStatus == Status.NEEDS_ACTION.status -> it.percent = 0
                        newStatus == Status.IN_PROCESS.status && it.percent !in 1..99 -> it.percent = 1
                    }
                    it.makeDirty()
                    database.update(it)
                    it.makeSeriesDirty(database)
                }
            }
            onChangeDone()
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
            onChangeDone()
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
            onChangeDone()
        }
    }

    /**
     * Inserts a new icalobject with categories
     * @param icalObject to be inserted
     * @param categories the list of categories that should be linked to the icalObject
     */
    fun insertQuickItem(icalObject: ICalObject, categories: List<Category>, attachments: List<Attachment>, alarm: Alarm?, editAfterSaving: Boolean) {

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newId = database.insertICalObject(icalObject)

                categories.forEach {
                    it.icalObjectId = newId
                    database.insertCategory(it)
                }

                attachments.forEach { attachment ->
                    attachment.icalObjectId = newId
                    database.insertAttachment(attachment)
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
            onChangeDone()
        }
    }

    fun saveStoredListSettingsData(name: String, config: StoredListSettingData) {
        viewModelScope.launch(Dispatchers.IO) {
            database.insertStoredListSetting(
                StoredListSetting(
                    module = module,
                    name = name,
                    storedListSettingData = config
                )
            )
        }
    }

    fun deleteStoredListSetting(storedListSetting: StoredListSetting) {
        viewModelScope.launch(Dispatchers.IO) {
            database.deleteStoredListSetting(storedListSetting)
        }
    }

    /**
     * Updates the expanded status of subtasks, subnotes and attachments in the DB
     */
    fun updateExpanded(icalObjectId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isParentsExpanded: Boolean, isAttachmentsExpanded: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            database.updateExpanded(icalObjectId, isSubtasksExpanded, isSubnotesExpanded, isParentsExpanded, isAttachmentsExpanded)
        }
    }

    /**
     * Deletes all tasks that are marked as done.
     * Subtasks are deleted if their parent is marked as done independent of their status.
     */
    fun deleteDone() {
        viewModelScope.launch(Dispatchers.IO) {
            val doneTasks = database.getDoneTasks()
            doneTasks.forEach { doneTask ->
                ICalObject.deleteItemWithChildren(doneTask.id, database)
            }
            toastMessage.value = _application.getString(R.string.toast_done_tasks_deleted, doneTasks.size)
            onChangeDone()
        }
    }

    /**
     * Retrieves the remote collections from the database
     * and triggers sync for their accounts
     */
    fun syncAccounts() {
        viewModelScope.launch(Dispatchers.IO) {
            val collections = database.getAllRemoteCollections()
            SyncUtil.syncAccounts(collections.map { Account(it.accountName, it.accountType) }.toSet())
        }
        SyncUtil.showSyncRequestedToast(_application)
    }

    /**
     * Notifies the contentObservers
     * schedules the notifications
     * updates the widget
     */
    private suspend fun onChangeDone() {
        SyncUtil.notifyContentObservers(getApplication())
        ListWidget().updateAll(getApplication())
        /*
        GlanceAppWidgetManager(_application).getGlanceIds(ListWidget::class.java).forEach { glanceId ->
            //ListWidget().provideGlance(_application, glanceId)
            //ListWidget().compose(_application, glanceId)
        }
         */
        NotificationPublisher.scheduleNextNotifications(getApplication())
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
    PROGRESS(R.string.progress, "$COLUMN_PERCENT "),
    ACCOUNT(R.string.account, "$COLUMN_COLLECTION_ACCOUNT_NAME "),
    COLLECTION(R.string.collection, "$COLUMN_COLLECTION_DISPLAYNAME ");

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
    DUE(R.string.due),
    ACCOUNT(R.string.account),
    COLLECTION(R.string.collection);

    companion object {
        fun getValuesFor(module: Module): Array<GroupBy> =
            when(module) {
                Module.JOURNAL -> arrayOf(
                    DATE,
                    STATUS,
                    CLASSIFICATION,
                    ACCOUNT,
                    COLLECTION
                )
                Module.NOTE -> arrayOf(
                    STATUS,
                    CLASSIFICATION,
                    ACCOUNT,
                    COLLECTION
                )
                Module.TODO -> arrayOf(
                    START,
                    DUE,
                    STATUS,
                    CLASSIFICATION,
                    PRIORITY,
                    ACCOUNT,
                    COLLECTION
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
