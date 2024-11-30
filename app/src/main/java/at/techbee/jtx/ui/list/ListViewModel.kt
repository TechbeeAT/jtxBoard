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
import android.media.MediaPlayer
import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import androidx.sqlite.db.SimpleSQLiteQuery
import at.techbee.jtx.NotificationPublisher
import at.techbee.jtx.R
import at.techbee.jtx.database.COLUMN_CLASSIFICATION
import at.techbee.jtx.database.COLUMN_COLLECTION_ACCOUNT_NAME
import at.techbee.jtx.database.COLUMN_COLLECTION_DISPLAYNAME
import at.techbee.jtx.database.COLUMN_COMPLETED
import at.techbee.jtx.database.COLUMN_CREATED
import at.techbee.jtx.database.COLUMN_DTSTART
import at.techbee.jtx.database.COLUMN_DUE
import at.techbee.jtx.database.COLUMN_LAST_MODIFIED
import at.techbee.jtx.database.COLUMN_PERCENT
import at.techbee.jtx.database.COLUMN_PRIORITY
import at.techbee.jtx.database.COLUMN_SORT_INDEX
import at.techbee.jtx.database.COLUMN_STATUS
import at.techbee.jtx.database.COLUMN_SUMMARY
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.ICalObject.Companion.TZ_ALLDAY
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredListSetting
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.settings.DropdownSettingOption
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.SyncUtil
import at.techbee.jtx.util.getPackageInfoCompat
import at.techbee.jtx.widgets.ListWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


open class ListViewModel(application: Application, val module: Module) : AndroidViewModel(application) {

    private val _application = application
    private val databaseDao = ICalDatabase.getInstance(_application).iCalDatabaseDao()
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
        databaseDao.getIcal4ListRel(it)
    }

    private var allSubtasksQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>()
    var allSubtasks: LiveData<List<ICal4ListRel>> = allSubtasksQuery.switchMap { databaseDao.getIcal4ListRel(it) }

    private var allSubnotesQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>()
    var allSubnotes: LiveData<List<ICal4ListRel>> = allSubnotesQuery.switchMap { databaseDao.getIcal4ListRel(it) }

    var allParents: LiveData<List<ICal4ListRel>> = databaseDao.getAllParents()

    private var selectFromAllListQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>()
    var selectFromAllList: LiveData<List<ICal4ListRel>> = selectFromAllListQuery.switchMap { databaseDao.getIcal4ListRel(it) }

    private val allAttachmentsList: LiveData<List<Attachment>> = databaseDao.getAllAttachments()
    val allAttachmentsMap = allAttachmentsList.map { list ->
        return@map list.groupBy { it.icalObjectId }
    }

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
            searchCategoriesAnyAllNone = listSettings.searchCategoriesAnyAllNone.value,
            searchResources = listSettings.searchResources,
            searchResourcesAnyAllNone = listSettings.searchResourcesAnyAllNone.value,
            searchStatus = listSettings.searchStatus,
            searchXStatus = listSettings.searchXStatus,
            searchClassification = listSettings.searchClassification,
            searchPriority = listSettings.searchPriority,
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
            isFilterDueWithin7Days = listSettings.isFilterDueWithin7Days.value,
            isFilterDueFuture = listSettings.isFilterDueFuture.value,
            isFilterStartInPast = listSettings.isFilterStartInPast.value,
            isFilterStartToday = listSettings.isFilterStartToday.value,
            isFilterStartTomorrow = listSettings.isFilterStartTomorrow.value,
            isFilterStartWithin7Days = listSettings.isFilterStartWithin7Days.value,
            isFilterStartFuture = listSettings.isFilterStartFuture.value,
            isFilterNoDatesSet = listSettings.isFilterNoDatesSet.value,
            isFilterNoStartDateSet = listSettings.isFilterNoStartDateSet.value,
            isFilterNoDueDateSet = listSettings.isFilterNoDueDateSet.value,
            isFilterNoCompletedDateSet = listSettings.isFilterNoCompletedDateSet.value,
            filterStartRangeStart = listSettings.filterStartRangeStart.value,
            filterStartRangeEnd = listSettings.filterStartRangeEnd.value,
            filterDueRangeStart = listSettings.filterDueRangeStart.value,
            filterDueRangeEnd = listSettings.filterDueRangeEnd.value,
            filterCompletedRangeStart = listSettings.filterCompletedRangeStart.value,
            filterCompletedRangeEnd = listSettings.filterCompletedRangeEnd.value,
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
                sortOrder = listSettings.subtasksSortOrder.value,
                searchText = if(listSettings.showOnlySearchMatchingSubentries.value) listSettings.searchText.value else null
            )
        )
        allSubnotesQuery.postValue(
            ICal4List.getQueryForAllSubEntries(
                component = Component.VJOURNAL,
                hideBiometricProtected = if(isAuthenticated) emptyList() else ListSettings.getProtectedClassificationsFromSettings(_application),
                orderBy = listSettings.subnotesOrderBy.value,
                sortOrder = listSettings.subnotesSortOrder.value,
                searchText = if(listSettings.showOnlySearchMatchingSubentries.value) listSettings.searchText.value else null
            )
        )
        if(saveListSettings)
            saveListSettings()
    }

    fun saveListSettings() = listSettings.saveToPrefs(prefs)

    fun updateSelectFromAllListQuery(searchText: String, isAuthenticated: Boolean) {
        selectFromAllListQuery.postValue(ICal4List.constructQuery(
            modules = listOf(Module.JOURNAL, Module.NOTE, Module.TODO),
            searchText = searchText,
            flatView = true,
            orderBy = OrderBy.LAST_MODIFIED,
            sortOrder = SortOrder.DESC,
            hideBiometricProtected = if(isAuthenticated) emptyList() else  ListSettings.getProtectedClassificationsFromSettings(_application)
        ))
    }


    fun updateProgress(itemId: Long, newPercent: Int, scrollOnce: Boolean = false) {

        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.updateProgress(
                id = itemId,
                newPercent = newPercent,
                settingKeepStatusProgressCompletedInSync = settingsStateHolder.settingKeepStatusProgressCompletedInSync.value,
                settingLinkProgressToSubtasks = settingsStateHolder.settingLinkProgressToSubtasks.value
            )
            if(newPercent == 100) {
                NotificationManagerCompat.from(_application).cancel(itemId.toInt())
                databaseDao.setAlarmNotification(itemId, false)
            }

            onChangeDone(updateNotifications = true)
            if(scrollOnce)
                scrollOnceId.postValue(itemId)
        }
    }

    fun updateStatus(itemId: Long, newStatus: Status, scrollOnce: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.updateStatus(itemId, newStatus, null, settingsStateHolder.settingKeepStatusProgressCompletedInSync.value)
            onChangeDone(updateNotifications = true)
            if(scrollOnce)
                scrollOnceId.postValue(itemId)
        }
    }

    fun updateXStatus(itemId: Long, newXStatus: ExtendedStatus, scrollOnce: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.updateStatus(itemId, newXStatus.rfcStatus, newXStatus, settingsStateHolder.settingKeepStatusProgressCompletedInSync.value)
            onChangeDone(updateNotifications = false)
            if(scrollOnce)
                scrollOnceId.postValue(itemId)
        }
    }

    fun swapCategories(iCalObjectId: Long, oldCategory: String, newCategory: String) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.swapCategories(iCalObjectId, oldCategory, newCategory)
            onChangeDone(updateNotifications = false)
            scrollOnceId.postValue(iCalObjectId)
        }
    }

    /*
    Deletes selected entries
     */
    fun deleteSelected() {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.deleteICalObjects(selectedEntries)
            selectedEntries.clear()
            onChangeDone(updateNotifications = true)
        }
    }

    /**
     * Adds new categories to the selected entries (if they don't exist already)
     * @param addedCategories that should be added
     * @param removedCategories that should be deleted
     */
    fun updateCategoriesOfSelected(addedCategories: List<String>, removedCategories: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.updateCategories(selectedEntries, addedCategories, removedCategories)
            onChangeDone(updateNotifications = false)
        }
    }

    fun moveSelectedToNewCollection(newCollection: ICalCollection) {
        viewModelScope.launch(Dispatchers.IO) {
            val newEntries = databaseDao.moveToCollection(
                iCalObjectIds = selectedEntries,
                newCollectionId = newCollection.collectionId
            )
            selectedEntries.clear()
            selectedEntries.addAll(newEntries)
            onChangeDone(updateNotifications = true)
        }
    }

    /**
     * Adds new resources to the selected entries (if they don't exist already)
     * @param addedResources that should be added
     * @param removedResources that should be deleted
     */
    fun updateResourcesToSelected(addedResources: List<String>, removedResources: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.updateResources(selectedEntries, addedResources, removedResources)
            onChangeDone(updateNotifications = false)
        }
    }

    /**
     * Adds a new relatedTo to the selected entries
     * @param addedParent that should be added as a relation
     */
    fun addNewParentToSelected(addedParent: ICal4List) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.linkChildren(addedParent.id, selectedEntries)
            onChangeDone(updateNotifications = false)
        }
    }


    /**
     * Updates the status of the selected entries
     * @param newStatus to be set
     */
    fun updateStatusOfSelected(newStatus: Status) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.updateStatus(selectedEntries, newStatus, null,  settingsStateHolder.settingKeepStatusProgressCompletedInSync.value)
            onChangeDone(updateNotifications = false)
        }
    }

    /**
     * Updates the status of the selected entries
     * @param newXStatus to be set
     */
    fun updateXStatusOfSelected(newXStatus: ExtendedStatus) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.updateStatus(selectedEntries, newXStatus.rfcStatus, newXStatus, settingsStateHolder.settingKeepStatusProgressCompletedInSync.value)
            onChangeDone(updateNotifications = false)
        }
    }

    /**
     * Updates the classification of the selected entries
     * @param newClassification to be set
     */
    fun updateClassificationOfSelected(newClassification: Classification) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.updateClassification(selectedEntries, newClassification)
            onChangeDone(updateNotifications = false)
        }
    }

    /**
     * Updates the priority of the selected entries
     * @param newPriority to be set
     */
    fun updatePriorityOfSelected(newPriority: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.updatePriority(selectedEntries, newPriority)
            onChangeDone(updateNotifications = false)
        }
    }

    /**
     * Inserts a new icalobject with categories
     * @param icalObject to be inserted
     * @param categories the list of categories that should be linked to the icalObject
     */
    fun insertQuickItem(icalObject: ICalObject, categories: List<Category>, attachments: List<Attachment>, alarm: Alarm?, editAfterSaving: Boolean) {

        viewModelScope.launch(Dispatchers.IO) {
            val newId = databaseDao.insertQuickItem(icalObject, categories, attachments, alarm)

            if(newId == null) {
                sqlConstraintException.value = true
                return@launch
            }

            scrollOnceId.postValue(newId)

            // trigger alarm immediately if setting is active
            if (icalObject.getModuleFromString() == Module.TODO
                && SettingsStateHolder(_application).settingAutoAlarm.value == DropdownSettingOption.AUTO_ALARM_ALWAYS_ON_SAVE)
                NotificationPublisher.triggerImmediateAlarm(icalObject, _application)

            if (editAfterSaving)
                goToEdit.postValue(newId)

            onChangeDone(updateNotifications = true)
        }
    }

    fun saveStoredListSetting(storedListSetting: StoredListSetting) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.upsertStoredListSetting(storedListSetting)
        }
    }

    fun deleteStoredListSetting(storedListSetting: StoredListSetting) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.deleteStoredListSetting(storedListSetting)
        }
    }

    /**
     * Updates the expanded status of subtasks, subnotes and attachments in the DB
     */
    fun updateExpanded(icalObjectId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isParentsExpanded: Boolean, isAttachmentsExpanded: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.updateExpanded(icalObjectId, isSubtasksExpanded, isSubnotesExpanded, isParentsExpanded, isAttachmentsExpanded)
        }
    }

    /**
     * Deletes all tasks that are marked as done.
     * Subtasks are deleted if their parent is marked as done independent of their status.
     */
    fun deleteDone() {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.getDoneTasks().let {
                databaseDao.deleteICalObjects(it)
                toastMessage.value = _application.getString(R.string.toast_done_tasks_deleted, it.size)
            }
            onChangeDone(updateNotifications = true)
        }
    }

    /**
     * Retrieves the remote collections from the database
     * and triggers sync for their accounts
     */
    fun syncAccounts() {
        viewModelScope.launch(Dispatchers.IO) {
            val collections = databaseDao.getAllRemoteCollections()
            SyncUtil.syncAccounts(collections.map { Account(it.accountName, it.accountType) }.toSet())
        }
        SyncUtil.showSyncRequestedToast(_application)
    }


    /**
     * Updates the sort order of entries in the database
     * @param list of entries, the index of the entry in the list corresponds to the sort order
     */
    fun updateSortOrder(list: List<ICal4List>) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.updateSortOrder(list.map { it.id })
            onChangeDone(updateNotifications = false)
        }
    }

    /**
     * Notifies the contentObservers
     * schedules the notifications
     * updates the widget
     */
    private suspend fun onChangeDone(updateNotifications: Boolean) {
        SyncUtil.notifyContentObservers(getApplication())
        ListWidget().updateAll(getApplication())
        if(updateNotifications)
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
            databaseDao.insertQuickItem(welcomeJournal, listOf(Category(text = context.getString(R.string.list_welcome_category))), emptyList(), null)
            databaseDao.insertQuickItem(welcomeNote, listOf(Category(text = context.getString(R.string.list_welcome_category))), emptyList(), null)
            databaseDao.insertQuickItem(welcomeTodo, listOf(Category(text = context.getString(R.string.list_welcome_category))), emptyList(), null)
        }
    }
}

open class ListViewModelJournals(application: Application) : ListViewModel(application, Module.JOURNAL)
open class ListViewModelNotes(application: Application) : ListViewModel(application, Module.NOTE)
open class ListViewModelTodos(application: Application) : ListViewModel(application, Module.TODO)



enum class OrderBy(@StringRes val stringResource: Int) {
    START_VTODO(R.string.started),
    START_VJOURNAL(R.string.date),
    DUE(R.string.due),
    COMPLETED(R.string.completed),
    CREATED(R.string.filter_created),
    LAST_MODIFIED(R.string.filter_last_modified),
    SUMMARY(R.string.summary),
    PRIORITY(R.string.priority),
    CLASSIFICATION(R.string.classification),
    STATUS(R.string.status),
    PROGRESS(R.string.progress),
    ACCOUNT(R.string.account),
    COLLECTION(R.string.collection),
    DRAG_AND_DROP(R.string.order_by_drag_and_drop),
    CATEGORIES(R.string.categories),
    RESOURCES(R.string.resources);

    fun getQueryAppendix(sortOrder: SortOrder): String {
        return when(this) {
            START_VTODO -> "$COLUMN_COMPLETED IS NOT NULL OR ($COLUMN_PERCENT IS NOT NULL AND $COLUMN_PERCENT = 100) OR $COLUMN_DTSTART IS NULL, $COLUMN_DTSTART ${sortOrder.name} "
            START_VJOURNAL -> "$COLUMN_DTSTART IS NULL, $COLUMN_DTSTART ${sortOrder.name} "
            DUE -> "$COLUMN_COMPLETED IS NOT NULL OR ($COLUMN_PERCENT IS NOT NULL AND $COLUMN_PERCENT = 100), $COLUMN_DUE IS NULL, $COLUMN_DUE ${sortOrder.name} "
            COMPLETED -> "IFNULL($COLUMN_COMPLETED, 0) ${sortOrder.name} "
            CREATED -> "$COLUMN_COMPLETED IS NOT NULL OR ($COLUMN_PERCENT IS NOT NULL AND $COLUMN_PERCENT = 100), $COLUMN_CREATED ${sortOrder.name} "
            LAST_MODIFIED -> "$COLUMN_LAST_MODIFIED ${sortOrder.name} "
            SUMMARY -> "UPPER($COLUMN_SUMMARY) ${sortOrder.name} "
            PRIORITY -> "CASE WHEN $COLUMN_PRIORITY IS NULL THEN 1 WHEN $COLUMN_PRIORITY = 0 THEN 1 ELSE 0 END, $COLUMN_PRIORITY ${sortOrder.name} "
            CLASSIFICATION -> "$COLUMN_CLASSIFICATION IS NULL, $COLUMN_CLASSIFICATION ${sortOrder.name} "
            STATUS -> "$COLUMN_STATUS IS NULL, $COLUMN_STATUS ${sortOrder.name} "
            PROGRESS -> "$COLUMN_PERCENT ${sortOrder.name} "
            ACCOUNT -> "$COLUMN_COLLECTION_ACCOUNT_NAME ${sortOrder.name} "
            COLLECTION -> "$COLUMN_COLLECTION_DISPLAYNAME ${sortOrder.name} "
            DRAG_AND_DROP -> "$COLUMN_SORT_INDEX "
            CATEGORIES -> "categories ${sortOrder.name} "
            RESOURCES -> "resources ${sortOrder.name} "
        }
    }

    companion object {
        fun getValuesFor(module: Module): Array<OrderBy> =
            when(module) {
                Module.JOURNAL -> arrayOf(START_VJOURNAL, CREATED, LAST_MODIFIED, SUMMARY, STATUS, CLASSIFICATION, DRAG_AND_DROP)
                Module.NOTE -> arrayOf(CREATED, LAST_MODIFIED, SUMMARY, STATUS, CLASSIFICATION, DRAG_AND_DROP)
                Module.TODO -> arrayOf(START_VTODO, DUE, COMPLETED, CREATED, LAST_MODIFIED, SUMMARY, PRIORITY, PROGRESS, STATUS, CLASSIFICATION, DRAG_AND_DROP)
            }
    }
}

enum class SortOrder(@StringRes val stringResource: Int) {
    ASC(R.string.filter_asc),
    DESC(R.string.filter_desc)
}

enum class GroupBy(@StringRes val stringResource: Int) {
    CATEGORY(R.string.category),
    RESOURCE(R.string.resource),
    PRIORITY(R.string.priority),
    STATUS(R.string.status),
    CLASSIFICATION(R.string.classification),
    DATE(R.string.group_by_date_day),
    DATE_WEEK(R.string.group_by_date_week),
    DATE_MONTH(R.string.group_by_date_month),
    START(R.string.group_by_started_day),
    START_WEEK(R.string.group_by_started_week),
    START_MONTH(R.string.group_by_started_month),
    DUE(R.string.group_by_due_day),
    DUE_WEEK(R.string.group_by_due_week),
    DUE_MONTH(R.string.group_by_due_month),
    ACCOUNT(R.string.account),
    COLLECTION(R.string.collection);

    companion object {
        fun getValuesFor(module: Module): Array<GroupBy> =
            when(module) {
                Module.JOURNAL -> arrayOf(
                    CATEGORY,
                    DATE,
                    DATE_WEEK,
                    DATE_MONTH,
                    STATUS,
                    CLASSIFICATION,
                    ACCOUNT,
                    COLLECTION
                )
                Module.NOTE -> arrayOf(
                    CATEGORY,
                    STATUS,
                    CLASSIFICATION,
                    ACCOUNT,
                    COLLECTION
                )
                Module.TODO -> arrayOf(
                    CATEGORY,
                    RESOURCE,
                    START,
                    START_WEEK,
                    START_MONTH,
                    DUE,
                    DUE_WEEK,
                    DUE_MONTH,
                    STATUS,
                    CLASSIFICATION,
                    PRIORITY,
                    ACCOUNT,
                    COLLECTION
                )
            }
    }
}

enum class AnyAllNone(@StringRes val stringResource: Int) {
    ANY(R.string.filter_any),
    ALL(R.string.filter_all),
    NONE(R.string.filter_none)
}

enum class ViewMode(@StringRes val stringResource: Int) {
    LIST(R.string.menu_list_viewmode_list),
    GRID(R.string.menu_list_viewmode_grid),
    COMPACT(R.string.menu_list_viewmode_compact),
    KANBAN(R.string.menu_list_viewmode_kanban),
    WEEK(R.string.menu_list_viewmode_week)
}
