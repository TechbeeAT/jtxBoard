/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import androidx.sqlite.db.SimpleSQLiteQuery
import at.techbee.jtx.AUTHORITY_FILEPROVIDER
import at.techbee.jtx.NotificationPublisher
import at.techbee.jtx.R
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalDatabaseDao
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Attendee
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.properties.Comment
import at.techbee.jtx.database.properties.Relatedto
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.GeofenceClient
import at.techbee.jtx.ui.list.ListSettings
import at.techbee.jtx.ui.list.OrderBy
import at.techbee.jtx.ui.list.SortOrder
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.Ical4androidUtil
import at.techbee.jtx.util.SyncUtil
import at.techbee.jtx.widgets.ListWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _application = application
    private val database = ICalDatabase.getInstance(application)
    private var databaseDao: ICalDatabaseDao = database.iCalDatabaseDao()

    private var mainICalObjectId: Long? = null

    var icalObject: LiveData<ICalObject?> = MutableLiveData(null)
    private var relatedTo: LiveData<List<Relatedto>> = MutableLiveData(emptyList())
    var collection: LiveData<ICalCollection> = MutableLiveData(null)
    var relatedSubnotes: LiveData<List<ICal4List>> = MutableLiveData(emptyList())
    var relatedSubtasks: LiveData<List<ICal4List>> = MutableLiveData(emptyList())
    var relatedParents: LiveData<List<ICal4List>> = MutableLiveData(emptyList())
    var seriesElement: LiveData<ICalObject?> = MutableLiveData(null)
    var seriesInstances: LiveData<List<ICalObject>> = MutableLiveData(emptyList())
    var isChild: LiveData<Boolean> = MutableLiveData(false)
    private var originalEntry: ICalEntity? = null

    var mutableICalObject by mutableStateOf<ICalObject?>(null)
    val mutableCategories = mutableStateListOf<Category>()
    val mutableResources = mutableStateListOf<Resource>()
    val mutableAttendees = mutableStateListOf<Attendee>()
    val mutableComments = mutableStateListOf<Comment>()
    val mutableAttachments = mutableStateListOf<Attachment>()
    val mutableAlarms = mutableStateListOf<Alarm>()

    var allCategories = databaseDao.getAllCategoriesAsText()
    var allResources = databaseDao.getAllResourcesAsText()
    val storedCategories = databaseDao.getStoredCategories()
    val storedResources = databaseDao.getStoredResources()
    val storedStatuses = databaseDao.getStoredStatuses()
    var allWriteableCollections = databaseDao.getAllWriteableCollections()

    private var selectFromAllListQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>()
    var selectFromAllList: LiveData<List<ICal4ListRel>> = selectFromAllListQuery.switchMap {databaseDao.getIcal4ListRel(it) }

    var navigateToId = mutableStateOf<Long?>(null)
    var changeState = mutableStateOf(DetailChangeState.LOADING)
    var toastMessage = mutableStateOf<String?>(null)
    val detailSettings: DetailSettings = DetailSettings()
    val settingsStateHolder = SettingsStateHolder(_application)

    val mediaPlayer = MediaPlayer()

    private var _isAuthenticated = false

    companion object {
        const val PREFS_DETAIL_JOURNALS = "prefsDetailJournals"
        const val PREFS_DETAIL_NOTES = "prefsDetailNotes"
        const val PREFS_DETAIL_TODOS = "prefsDetailTodos"
    }

    fun load(icalObjectId: Long, isAuthenticated: Boolean) {
        mainICalObjectId = icalObjectId
        _isAuthenticated = isAuthenticated
        viewModelScope.launch {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.LOADING }

            withContext(Dispatchers.IO) {
                originalEntry = databaseDao.getSync(icalObjectId)
                mutableICalObject = originalEntry?.property
                mutableCategories.clear()
                mutableCategories.addAll(originalEntry?.categories ?: emptyList())
                mutableResources.clear()
                mutableResources.addAll(originalEntry?.resources ?: emptyList())
                mutableAttendees.clear()
                mutableAttendees.addAll(originalEntry?.attendees ?: emptyList())
                mutableComments.clear()
                mutableComments.addAll(originalEntry?.comments ?: emptyList())
                mutableAttachments.clear()
                mutableAttachments.addAll(originalEntry?.attachments ?: emptyList())
                mutableAlarms.clear()
                mutableAlarms.addAll(originalEntry?.alarms ?: emptyList())
            }

            //icalEntity = databaseDao.get(icalObjectId)
            icalObject = databaseDao.getICalObject(icalObjectId)
            relatedTo = databaseDao.getRelatedTo(icalObjectId)

            collection = icalObject.switchMap {
                it?.let { cur -> databaseDao.getCollection(cur.collectionId)  }

            }

            relatedParents = relatedTo.switchMap {
                it.map { relatedto ->  relatedto.text }.let { uids ->
                    databaseDao.getICal4ListByUIDs(uids)
                }
            }
            relatedSubtasks = icalObject.switchMap {
                it?.uid?.let { parentUid ->
                    databaseDao.getIcal4List(
                        ICal4List.getQueryForAllSubentriesForParentUID(
                            parentUid = parentUid,
                            component = Component.VTODO,
                            hideBiometricProtected = if(_isAuthenticated) emptyList() else  ListSettings.getProtectedClassificationsFromSettings(_application),
                            orderBy = detailSettings.listSettings?.subtasksOrderBy?.value ?: OrderBy.CREATED,
                            sortOrder = detailSettings.listSettings?.subtasksSortOrder?.value ?: SortOrder.DESC
                        )
                    )
                }
            }
            relatedSubnotes = icalObject.switchMap {
                it?.uid?.let { parentUid ->
                    databaseDao.getIcal4List(
                        ICal4List.getQueryForAllSubentriesForParentUID(
                            parentUid = parentUid,
                            component = Component.VJOURNAL,
                            hideBiometricProtected = if(_isAuthenticated) emptyList() else  ListSettings.getProtectedClassificationsFromSettings(_application),
                            orderBy = detailSettings.listSettings?.subnotesOrderBy?.value ?: OrderBy.CREATED,
                            sortOrder = detailSettings.listSettings?.subnotesSortOrder?.value ?: SortOrder.DESC
                        )
                    )
                }
            }
            seriesElement = icalObject.switchMap { databaseDao.getSeriesICalObjectIdByUID(it?.uid) }
            seriesInstances = icalObject.switchMap { databaseDao.getSeriesInstancesICalObjectsByUID(it?.uid) }
            isChild = databaseDao.isChild(icalObjectId)

            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.UNCHANGED }
        }

        //remove notification (if not sticky)
        if(!settingsStateHolder.settingStickyAlarms.value)
            NotificationManagerCompat.from(_application).cancel(icalObjectId.toInt())
    }

    fun updateSelectFromAllListQuery(searchText: String, modules: List<Module>, sameCollection: Boolean, sameAccount: Boolean) {
        selectFromAllListQuery.postValue(ICal4List.constructQuery(
            modules = modules,
            searchText = searchText,
            searchCollection = if(sameCollection) collection.value?.displayName?.let { listOf(it) }?: emptyList() else emptyList(),
            searchAccount = if(sameAccount) collection.value?.accountName?.let { listOf(it) }?: emptyList() else emptyList(),
            orderBy = OrderBy.LAST_MODIFIED,
            sortOrder = SortOrder.DESC,
            hideBiometricProtected = if(_isAuthenticated) emptyList() else  ListSettings.getProtectedClassificationsFromSettings(_application)
        ))
    }


    fun updateProgress(id: Long, newPercent: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }
            val item = databaseDao.getICalObjectById(id) ?: return@launch
            try {
                item.setUpdatedProgress(newPercent, settingsStateHolder.settingKeepStatusProgressCompletedInSync.value)
                databaseDao.update(item)
                item.makeSeriesDirty(databaseDao)
                if(settingsStateHolder.settingLinkProgressToSubtasks.value) {
                    ICalObject.findTopParent(id, databaseDao)?.let {
                        ICalObject.updateProgressOfParents(it.id, databaseDao, settingsStateHolder.settingKeepStatusProgressCompletedInSync.value)
                    }
                }
                onChangeDone()
                withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVED }
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", "Corrupted ID: $id")
                Log.d("SQLConstraint", e.stackTraceToString())
                withContext (Dispatchers.Main) { changeState.value = DetailChangeState.SQLERROR }
            }
        }
    }

    fun updateSummary(icalObjectId: Long, newSummary: String) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }
            val icalObject = databaseDao.getICalObjectById(icalObjectId) ?: return@launch
            icalObject.summary = newSummary
            icalObject.makeDirty()
            icalObject.makeSeriesDirty(databaseDao)
            try {
                databaseDao.update(icalObject)
                withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVED }
                SyncUtil.notifyContentObservers(getApplication())
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", "Corrupted ID: $icalObjectId")
                Log.d("SQLConstraint", e.stackTraceToString())
                withContext (Dispatchers.Main) { changeState.value = DetailChangeState.SQLERROR }
            }
            onChangeDone()
        }
    }

    fun updateSortOrder(list: List<ICal4List>) {

        viewModelScope.launch(Dispatchers.IO) {
            database.withTransaction {
                list.forEachIndexed { index, iCal4List ->
                    val iCalObject =
                        databaseDao.getICalObjectById(iCal4List.id) ?: return@forEachIndexed
                    iCalObject.sortIndex = index
                    iCalObject.makeDirty()
                    databaseDao.update(iCalObject)
                }
            }
        onChangeDone()
        }
    }

    fun unlinkFromSeries(instances: List<ICalObject>, series: ICalObject?, deleteAfterUnlink: Boolean) {
        viewModelScope.launch {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }

            withContext(Dispatchers.IO) {
                database.withTransaction {

                    instances.forEach { instance ->
                        val children = databaseDao.getRelatedChildren(instance.id)
                        val updatedEntry = ICalObject.unlinkFromSeries(instance, databaseDao)
                        children.forEach forEachChild@{ child ->
                            val childEntity = databaseDao.getSync(child.id) ?: return@forEachChild
                            createCopy(childEntity, child.getModuleFromString(), updatedEntry.uid)
                        }
                        instance.makeSeriesDirty(databaseDao)
                    }
                }

                if (deleteAfterUnlink) {
                    series?.id?.let { deleteById(it, false) }
                    withContext(Dispatchers.Main) {
                        changeState.value = DetailChangeState.DELETED_BACK_TO_LIST
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        changeState.value = DetailChangeState.CHANGESAVED
                    }
                }
            }
            withContext(Dispatchers.IO) {
                onChangeDone()
            }
        }
    }

    /**
     * Adds a new relatedTo to the passed entries relating to the current ICalObject as a PARENT
     * @param newSubEntries that should get a link to the current entry
     */
    fun linkNewSubentries(newSubEntries: List<ICal4List>) {
        viewModelScope.launch(Dispatchers.IO) {
            database.withTransaction {

                newSubEntries.forEach { newSubEntry ->
                    if (icalObject.value?.uid == null)
                        return@forEach

                    if (newSubEntry.uid == icalObject.value?.uid)
                        return@forEach

                    val existing = icalObject.value?.uid?.let {
                        databaseDao.findRelatedTo(
                            newSubEntry.id,
                            it,
                            Reltype.PARENT.name
                        ) != null
                    } ?: return@forEach
                    if (!existing) {
                        databaseDao.insertRelatedto(
                            Relatedto(
                                icalObjectId = newSubEntry.id,
                                text = icalObject.value?.uid!!,
                                reltype = Reltype.PARENT.name
                            )
                        )
                        databaseDao.getICalObjectById(newSubEntry.id)?.let {
                            it.makeDirty()
                            databaseDao.update(it)
                        }
                    }
                }
            }
            onChangeDone()
        }
    }

    /**
     * Adds a new relatedTo to the passed entries relating to the current ICalObject as a CHILD
     * @param newParents that should get a link to the current entry
     */
    fun linkNewParents(newParents: List<ICal4List>) {
        viewModelScope.launch(Dispatchers.IO) {
            newParents.forEach { newParent ->
                if(newParent.uid == null)
                    return@forEach

                if(newParent.uid == icalObject.value?.uid)
                    return@forEach

                val existing = newParent.uid?.let { databaseDao.findRelatedTo(icalObject.value?.id!!, it, Reltype.PARENT.name) != null } ?: return@forEach
                if(!existing) {
                    databaseDao.insertRelatedto(
                        Relatedto(
                            icalObjectId = mainICalObjectId!!,
                            text = newParent.uid,
                            reltype = Reltype.PARENT.name
                        )
                    )
                    databaseDao.getICalObjectById(mainICalObjectId!!)?.let {
                        it.makeDirty()
                        databaseDao.update(it)
                    }
                }
            }
            onChangeDone()
        }
    }

    fun moveToNewCollection(newCollectionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            mutableICalObject?.let {
                save(it, mutableCategories, mutableComments, mutableAttendees, mutableResources, mutableAttachments, mutableAlarms)
                move(it, newCollectionId)
            }
            onChangeDone()
        }
    }

    fun convertTo(module: Module) {
        viewModelScope.launch(Dispatchers.IO) {
            mutableICalObject?.let {
                it.module = module.name
                if (module == Module.JOURNAL || module == Module.NOTE) {
                    it.component = Component.VJOURNAL.name
                    if (module == Module.JOURNAL && it.dtstart == null) {
                        it.dtstart = DateTimeUtils.getTodayAsLong()
                        it.dtstartTimezone = ICalObject.TZ_ALLDAY
                    }
                    if(module == Module.NOTE) {
                        it.dtstart = null
                        it.dtstartTimezone = null
                        it.rrule = null
                        it.rdate = null
                        it.exdate = null
                        mutableAlarms.clear()
                    }
                    mutableResources.clear()
                    it.due = null
                    it.dueTimezone = null
                    it.completed = null
                    it.completedTimezone = null
                    it.duration = null
                    it.priority = null
                    it.percent = null

                    if(Status.valuesFor(Module.JOURNAL).none { status -> status.status == it.status })
                        it.status = Status.FINAL.status

                } else if (module == Module.TODO) {
                    it.component = Component.VTODO.name
                    if(Status.valuesFor(Module.TODO).none { status -> status.status == it.status })
                        it.status = Status.NEEDS_ACTION.status
                }
                it.makeDirty()
                save(it, mutableCategories, mutableComments, mutableAttendees, mutableResources, mutableAttachments, mutableAlarms)
            }
            onChangeDone()
        }
    }


    private suspend fun move(icalObject: ICalObject, newCollectionId: Long) {
        withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }

        try {
            val newId = ICalObject.updateCollectionWithChildren(icalObject.id, null, newCollectionId, databaseDao, _application)
            newId?.let { load(it, _isAuthenticated) }
            // once the newId is there, the local entries can be deleted (or marked as deleted)
            ICalObject.deleteItemWithChildren(icalObject.id, databaseDao)        // make sure to delete the old item (or marked as deleted - this is already handled in the function)
            if (icalObject.rrule != null)
                icalObject.recreateRecurring(_application)
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVED }
        } catch (e: SQLiteConstraintException) {
            Log.d("SQLConstraint", "Corrupted ID: ${icalObject.id}")
            Log.d("SQLConstraint", e.stackTraceToString())
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.SQLERROR }
        }
        onChangeDone()
    }



    fun saveEntry() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableICalObject?.let {
                // make sure the eTag, flags, scheduleTag and fileName gets updated in the background if the sync is triggered, so that another sync won't overwrite the changes!
                icalObject.value?.eTag.let { currentETag -> it.eTag = currentETag }
                icalObject.value?.flags.let { currentFlags -> it.flags = currentFlags }
                icalObject.value?.scheduleTag.let { currentScheduleTag -> it.scheduleTag = currentScheduleTag }
                icalObject.value?.fileName.let { currentFileName -> it.fileName = currentFileName }

                save(it, mutableCategories, mutableComments, mutableAttendees, mutableResources, mutableAttachments, mutableAlarms)
            }
        }
    }


    private suspend fun save(icalObject: ICalObject,
             categories: List<Category>,
             comments: List<Comment>,
             attendees: List<Attendee>,
             resources: List<Resource>,
             attachments: List<Attachment>,
             alarms: List<Alarm>
    ) {
        withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }

        try {
            database.withTransaction {

                if (databaseDao.getCategoriesSync(icalObject.id) != categories || mainICalObjectId != icalObject.id) {
                    icalObject.makeDirty()
                    databaseDao.deleteCategories(icalObject.id)
                    categories.forEach { changedCategory ->
                        changedCategory.icalObjectId = icalObject.id
                        databaseDao.insertCategory(changedCategory)
                    }
                }

                if (databaseDao.getCommentsSync(icalObject.id) != comments || mainICalObjectId != icalObject.id) {
                    icalObject.makeDirty()
                    databaseDao.deleteComments(icalObject.id)
                    comments.forEach { changedComment ->
                        changedComment.icalObjectId = icalObject.id
                        databaseDao.insertComment(changedComment)
                    }
                }

                if (databaseDao.getAttendeesSync(icalObject.id) != attendees || mainICalObjectId != icalObject.id) {
                    icalObject.makeDirty()
                    databaseDao.deleteAttendees(icalObject.id)
                    attendees.forEach { changedAttendee ->
                        changedAttendee.icalObjectId = icalObject.id
                        databaseDao.insertAttendee(changedAttendee)
                    }
                }

                if (databaseDao.getResourcesSync(icalObject.id) != resources || mainICalObjectId != icalObject.id) {
                    icalObject.makeDirty()
                    databaseDao.deleteResources(icalObject.id)
                    resources.forEach { changedResource ->
                        changedResource.icalObjectId = icalObject.id
                        databaseDao.insertResource(changedResource)
                    }
                }

                if (databaseDao.getAttachmentsSync(icalObject.id) != attachments || mainICalObjectId != icalObject.id) {
                    icalObject.makeDirty()
                    databaseDao.deleteAttachments(icalObject.id)
                    attachments.forEach { changedAttachment ->
                        changedAttachment.icalObjectId = icalObject.id
                        databaseDao.insertAttachment(changedAttachment)
                    }
                    Attachment.scheduleCleanupJob(getApplication())
                }

                if (databaseDao.getAlarmsSync(icalObject.id) != alarms || mainICalObjectId != icalObject.id) {
                    icalObject.makeDirty()
                    databaseDao.deleteAlarms(icalObject.id)
                    alarms.forEach { changedAlarm ->
                        changedAlarm.icalObjectId = icalObject.id
                        changedAlarm.alarmId = databaseDao.insertAlarm(changedAlarm)
                    }
                }

                icalObject.makeDirty()
                databaseDao.update(icalObject)
                icalObject.makeSeriesDirty(databaseDao)
                icalObject.recreateRecurring(_application)
            }
            onChangeDone()
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVED }
        } catch (e: SQLiteConstraintException) {
            Log.d("SQLConstraint", "Corrupted ID: ${icalObject.id}")
            Log.d("SQLConstraint", e.stackTraceToString())
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.SQLERROR }
        }
    }

    /**
     * reverts the current entry back to the original values that were stored in originalEntry
     */
    fun revert() {
        val originalICalObject = originalEntry?.property?: return
        viewModelScope.launch(Dispatchers.IO) {
            save(
                icalObject = originalICalObject.apply {
                    eTag = icalObject.value?.eTag
                    sequence = (icalObject.value?.sequence?:0)+1
                },
                categories = originalEntry?.categories ?: emptyList(),
                comments = originalEntry?.comments ?: emptyList(),
                attendees = originalEntry?.attendees ?: emptyList(),
                resources = originalEntry?.resources ?: emptyList(),
                attachments = originalEntry?.attachments ?: emptyList(),
                alarms = originalEntry?.alarms ?: emptyList()
            )
            navigateToId.value = icalObject.value?.id
            onChangeDone()
        }
    }


    fun addSubEntry(subEntry: ICalObject, attachment: Attachment?) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.LOADING }
            subEntry.collectionId = icalObject.value?.collectionId!!

            try {
                database.withTransaction {
                    val subEntryId = databaseDao.insertICalObject(subEntry)
                    attachment?.let {
                        it.icalObjectId = subEntryId
                        databaseDao.insertAttachment(it)
                    }
                    databaseDao.insertRelatedto(
                        Relatedto(
                            icalObjectId = subEntryId,
                            reltype = Reltype.PARENT.name,
                            text = icalObject.value?.uid!!
                        )
                    )
                }
                withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVED }
                onChangeDone()
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", e.stackTraceToString())
                withContext (Dispatchers.Main) { changeState.value = DetailChangeState.SQLERROR }
            }
        }
    }


    /**
     * Deletes the current entry with its children
     */
    fun delete() {
        viewModelScope.launch {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.DELETING }
            try {
                withContext(Dispatchers.IO) {
                    database.withTransaction {

                        if (icalObject.value?.recurid != null) {
                            ICalObject.unlinkFromSeries(icalObject.value!!, databaseDao)
                        }
                        icalObject.value?.id?.let { id ->
                            ICalObject.deleteItemWithChildren(id, databaseDao)
                        }
                    }
                }
                withContext (Dispatchers.Main) {
                    changeState.value = DetailChangeState.DELETED
                    toastMessage.value = _application.getString(R.string.details_toast_entry_deleted)
                }
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", "Corrupted ID: $mainICalObjectId")
                Log.d("SQLConstraint", e.stackTraceToString())
                withContext (Dispatchers.Main) { changeState.value = DetailChangeState.SQLERROR }
            } finally {
                withContext(Dispatchers.IO) {
                    onChangeDone()
                }
            }
        }
    }

    /**
     * Delete function for subtasks and subnotes
     * @param [icalObjectId] of the subtask/subnote to be deleted
     */
    fun deleteById(icalObjectId: Long, showToast: Boolean) {
        viewModelScope.launch {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.LOADING }
            try {
                withContext(Dispatchers.IO) {
                    database.withTransaction {
                        ICalObject.deleteItemWithChildren(icalObjectId, databaseDao)
                    }
                }
                withContext (Dispatchers.Main) {
                    changeState.value = DetailChangeState.CHANGESAVED
                    if(showToast)
                        toastMessage.value = _application.getString(R.string.details_toast_entry_deleted)
                }
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", "Corrupted ID: $icalObjectId")
                Log.d("SQLConstraint", e.stackTraceToString())
                withContext (Dispatchers.Main) { changeState.value = DetailChangeState.SQLERROR }
            } finally {
                withContext(Dispatchers.IO) {
                    onChangeDone()
                }
            }
        }
    }

    fun unlinkFromParent(icalObjectId: Long, parentUID: String?) {
        if(parentUID == null)
            return

        viewModelScope.launch(Dispatchers.IO) {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.LOADING }
            databaseDao.deleteRelatedto(icalObjectId, parentUID)
            databaseDao.getICalObjectByIdSync(icalObjectId)?.let {
                it.makeDirty()
                databaseDao.update(it)
            }
            onChangeDone()
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVED }
        }
    }

    fun createCopy(newModule: Module) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.getSync(mainICalObjectId!!)?.let {
                createCopy(it, newModule)
            }
        }
    }

    private fun createCopy(icalEntityToCopy: ICalEntity, newModule: Module, newParentUID: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.LOADING }
            val newEntity = icalEntityToCopy.getIcalEntityCopy(newModule)
            database.withTransaction {
                try {
                    val newId = databaseDao.insertICalObject(newEntity.property)
                    newEntity.alarms?.forEach { alarm ->
                        databaseDao.insertAlarm(
                            alarm.copy(
                                alarmId = 0L,
                                icalObjectId = newId
                            )
                        )
                    }
                    newEntity.attachments?.forEach { attachment ->
                        databaseDao.insertAttachment(
                            attachment.copy(
                                icalObjectId = newId,
                                attachmentId = 0L
                            )
                        )
                    }
                    newEntity.attendees?.forEach { attendee ->
                        databaseDao.insertAttendee(
                            attendee.copy(
                                icalObjectId = newId,
                                attendeeId = 0L
                            )
                        )
                    }
                    newEntity.categories?.forEach { category ->
                        databaseDao.insertCategory(
                            category.copy(
                                icalObjectId = newId,
                                categoryId = 0L
                            )
                        )
                    }
                    newEntity.comments?.forEach { comment ->
                        databaseDao.insertComment(comment.copy(icalObjectId = newId, commentId = 0L))
                    }
                    newEntity.resources?.forEach { resource ->
                        databaseDao.insertResource(
                            resource.copy(
                                icalObjectId = newId,
                                resourceId = 0L
                            )
                        )
                    }
                    newEntity.unknown?.forEach { unknown ->
                        databaseDao.insertUnknownSync(
                            unknown.copy(
                                icalObjectId = newId,
                                unknownId = 0L
                            )
                        )
                    }
                    newEntity.organizer?.let { organizer ->
                        databaseDao.insertOrganizer(
                            organizer.copy(
                                icalObjectId = newId,
                                organizerId = 0L
                            )
                        )
                    }

                    newEntity.relatedto?.forEach { relatedto ->
                        if (relatedto.reltype == Reltype.PARENT.name && newParentUID != null) {
                            databaseDao.insertRelatedto(
                                relatedto.copy(
                                    relatedtoId = 0L,
                                    icalObjectId = newId,
                                    text = newParentUID
                                )
                            )
                        }
                    }

                    val children = databaseDao.getRelatedChildren(icalEntityToCopy.property.id)
                    children.forEach { child ->
                        databaseDao.getSync(child.id)?.let {
                            createCopy(
                                icalEntityToCopy = it,
                                newModule = it.property.getModuleFromString(),
                                newParentUID = newEntity.property.uid
                            )
                        }
                    }

                    if (newParentUID == null)   // we navigate only to the parent (not to the children that are invoked recursively)
                        navigateToId.value = newId

                    withContext(Dispatchers.Main) {
                        changeState.value = DetailChangeState.CHANGESAVED
                    }
                } catch (e: SQLiteConstraintException) {
                    Log.d("SQLConstraint", e.stackTraceToString())
                    withContext(Dispatchers.Main) { changeState.value = DetailChangeState.SQLERROR }
                } finally {
                    onChangeDone()
                }
            }
        }
    }

    /**
     * Creates a share intent to share the current entry as .ics file
     */
    fun shareAsICS(context: Context) {
        viewModelScope.launch(Dispatchers.IO)  {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }
            val icsContentUri = databaseDao.getSync(mainICalObjectId!!)?.let {
                createContentUri(it)
            } ?: return@launch

            try {
                ShareCompat.IntentBuilder(context)
                    .setType("text/calendar")
                    .addStream(icsContentUri)
                    .startChooser()
            } catch (e: ActivityNotFoundException) {
                Log.i("ActivityNotFound", "No activity found to open file.")
                toastMessage.value = "No app found to open this file."
            } finally {
                withContext (Dispatchers.Main) { changeState.value = DetailChangeState.UNCHANGED }
            }
        }
    }

    /**
     * Creates a share intent to share the current entry as email.
     * The intent sets the subject (summary), attendees (receipients),
     * text (the contents as a text representation) and attachments
     */
    fun shareAsText(context: Context) {
        viewModelScope.launch(Dispatchers.IO)  {
            val iCalEntity = databaseDao.getSync(mainICalObjectId!!) ?: return@launch
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }

            val shareText = iCalEntity.getShareText(context)
            val subject = iCalEntity.property.summary
            val attendees = iCalEntity.attendees
                ?.map { it.caladdress.removePrefix("mailto:") }
                ?.toTypedArray()
            val attachmentUris = iCalEntity.attachments.orEmpty()
                .asSequence()
                .mapNotNull { attachment -> attachment.uri }
                .filter { attachmentUri ->
                    // A share intent may only contain content: URIs in EXTRA_STREAM
                    attachmentUri.startsWith("content:")
                }
                .map { attachmentUri -> Uri.parse(attachmentUri) }
                .toList()

            val icsContentUri = createContentUri(iCalEntity)

            val shareIntentBuilder = ShareCompat.IntentBuilder(context).apply {
                setText(shareText)

                if (subject != null) {
                    setSubject(subject)
                }

                if (attendees != null) {
                    setEmailTo(attendees)
                }

                attachmentUris.forEach { uri ->
                    addStream(uri)
                }

                if (icsContentUri != null) {
                    addStream(icsContentUri)
                }

                if (attachmentUris.isNotEmpty()) {
                    setType("*/*")
                } else if (icsContentUri != null) {
                    setType("text/calendar")
                } else {
                    setType("text/plain")
                }
            }

            try {
                shareIntentBuilder.startChooser()
            } catch (e: ActivityNotFoundException) {
                Log.i("ActivityNotFound", "No activity found to send this entry.")
                toastMessage.value = _application.getString(R.string.error_no_app_found_to_open_entry)
            } finally {
                withContext (Dispatchers.Main) { changeState.value = DetailChangeState.UNCHANGED }
            }
        }
    }

    /**
     * @param [iCalEntity] for which the content should be put in a file that is returned as Uri
     * @return the uri for the file with the [iCalEntity] content in as .ics format or null
     */
    private suspend fun createContentUri(iCalEntity: ICalEntity): Uri? {
        val icsFile = writeIcsFile(iCalEntity) ?: return null

        val context = getApplication<Application>()
        return FileProvider.getUriForFile(context, AUTHORITY_FILEPROVIDER, icsFile)
    }

    /**
     * @param [iCalEntity] for which the content should be put in a file
     * @return a file with the [iCalEntity] content in as .ics format or null
     */
    private suspend fun writeIcsFile(iCalEntity: ICalEntity): File? {
        val context = getApplication<Application>()
        val account = iCalEntity.ICalCollection?.getAccount() ?: return null
        val collectionId = iCalEntity.property.collectionId
        val parentId = iCalEntity.property.id
        val parentWithChildrenIds = mutableListOf(parentId)
        addChildrenOf(parentId, parentWithChildrenIds)

        return try {
            val outputFile = File(context.externalCacheDir, "ics_file.ics")

            withContext(Dispatchers.IO) {
                FileOutputStream(outputFile).use { outputStream ->
                    Ical4androidUtil.writeICSFormatFromProviderToOS(
                        account,
                        context,
                        collectionId,
                        parentWithChildrenIds,
                        outputStream
                    )
                }
            }

            outputFile
        } catch (e: Exception) {
            Log.i("fileprovider", "Failed to attach ICS File")
            toastMessage.value = "Failed to attach ICS File."

            null
        }
    }

    /**
     * Adds the children of an entry to the list recursively
     * @param parent whose children ICalObjectIds should be added to
     * @param list
     */
    private suspend fun addChildrenOf(parent: Long, list: MutableList<Long>) {
        val children = databaseDao.getRelatedChildren(parent)
        list.addAll(children.map { it.id })
        children.forEach { addChildrenOf(it.id, list) }
    }

    /**
     * Notifies the contentObservers
     * schedules the notifications
     * sets geofences
     * updates the widget
     */
    private suspend fun onChangeDone() {
        SyncUtil.notifyContentObservers(getApplication())
        NotificationPublisher.scheduleNextNotifications(getApplication())
        GeofenceClient(_application).setGeofences()
        ListWidget().updateAll(getApplication())
    }

    enum class DetailChangeState { LOADING, UNCHANGED, CHANGEUNSAVED, SAVINGREQUESTED, CHANGESAVING, CHANGESAVED, DELETING, DELETED, DELETED_BACK_TO_LIST, SQLERROR }
}
