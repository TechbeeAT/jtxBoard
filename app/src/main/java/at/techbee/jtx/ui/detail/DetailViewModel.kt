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
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.sqlite.db.SimpleSQLiteQuery
import at.techbee.jtx.AUTHORITY_FILEPROVIDER
import at.techbee.jtx.NotificationPublisher
import at.techbee.jtx.R
import at.techbee.jtx.database.Component
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
    private var database: ICalDatabaseDao = ICalDatabase.getInstance(application).iCalDatabaseDao

    var icalEntity: LiveData<ICalEntity?> = MutableLiveData(ICalEntity(ICalObject(), null, null, null, null, null))
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

    var allCategories = database.getAllCategoriesAsText()
    var allResources = database.getAllResourcesAsText()
    val storedCategories = database.getStoredCategories()
    val storedResources = database.getStoredResources()
    val storedStatuses = database.getStoredStatuses()
    var allWriteableCollections = database.getAllWriteableCollections()

    private var selectFromAllListQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>()
    var selectFromAllList: LiveData<List<ICal4ListRel>> = selectFromAllListQuery.switchMap {database.getIcal4ListRel(it) }

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
        _isAuthenticated = isAuthenticated
        viewModelScope.launch {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.LOADING }
            icalEntity = database.get(icalObjectId)

            relatedParents = icalEntity.switchMap {
                it?.relatedto?.map { relatedto ->  relatedto.text }?.let { uids ->
                    database.getICal4ListByUIDs(uids)
                }
            }
            relatedSubtasks = icalEntity.switchMap {
                it?.property?.uid?.let { parentUid ->
                    database.getIcal4List(
                        ICal4List.getQueryForAllSubentriesForParentUID(
                            parentUid = parentUid,
                            component = Component.VTODO,
                            hideBiometricProtected = if(_isAuthenticated) emptyList() else  ListSettings.getProtectedClassificationsFromSettings(_application),
                            orderBy = detailSettings.listSettings?.subtasksOrderBy?.value ?: OrderBy.CREATED,
                            sortOrder = detailSettings.listSettings?.subtasksSortOrder?.value ?: SortOrder.ASC
                        )
                    )
                }
            }
            relatedSubnotes = icalEntity.switchMap {
                it?.property?.uid?.let { parentUid ->
                    database.getIcal4List(
                        ICal4List.getQueryForAllSubentriesForParentUID(
                            parentUid = parentUid,
                            component = Component.VJOURNAL,
                            hideBiometricProtected = if(_isAuthenticated) emptyList() else  ListSettings.getProtectedClassificationsFromSettings(_application),
                            orderBy = detailSettings.listSettings?.subnotesOrderBy?.value ?: OrderBy.CREATED,
                            sortOrder = detailSettings.listSettings?.subnotesSortOrder?.value ?: SortOrder.ASC
                        )
                    )
                }
            }
            seriesElement = icalEntity.switchMap { database.getSeriesICalObjectIdByUID(it?.property?.uid) }
            seriesInstances = icalEntity.switchMap { database.getSeriesInstancesICalObjectsByUID(it?.property?.uid) }
            isChild = database.isChild(icalObjectId)

            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.UNCHANGED }
        }

        viewModelScope.launch(Dispatchers.IO) {
            originalEntry = database.getSync(icalObjectId)  // store original entry for revert option
        }
    }

    fun updateSelectFromAllListQuery(searchText: String) {
        selectFromAllListQuery.postValue(ICal4List.constructQuery(
            modules = listOf(Module.JOURNAL, Module.NOTE, Module.TODO),
            searchText = searchText,
            hideBiometricProtected = if(_isAuthenticated) emptyList() else  ListSettings.getProtectedClassificationsFromSettings(_application)
        ))
    }


    fun updateProgress(id: Long, newPercent: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }
            val item = database.getICalObjectById(id) ?: return@launch
            try {
                item.setUpdatedProgress(newPercent, settingsStateHolder.settingKeepStatusProgressCompletedInSync.value)
                database.update(item)
                item.makeSeriesDirty(database)
                if(settingsStateHolder.settingLinkProgressToSubtasks.value) {
                    ICalObject.findTopParent(id, database)?.let {
                        ICalObject.updateProgressOfParents(it.id, database, settingsStateHolder.settingKeepStatusProgressCompletedInSync.value)
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
            val icalObject = database.getICalObjectById(icalObjectId) ?: return@launch
            icalObject.summary = newSummary
            icalObject.makeDirty()
            icalObject.makeSeriesDirty(database)
            try {
                database.update(icalObject)
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

    fun unlinkFromSeries(instances: List<ICalObject>, series: ICalObject?, deleteAfterUnlink: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }

            instances.forEach { instance ->
                val children = database.getRelatedChildren(instance.id)
                val updatedEntry = ICalObject.unlinkFromSeries(instance, database)
                children.forEach forEachChild@ { child ->
                    val childEntity = database.getSync(child.id) ?: return@forEachChild
                    createCopy(childEntity, child.getModuleFromString(), updatedEntry.uid)
                }
                instance.makeSeriesDirty(database)
            }

            if(deleteAfterUnlink) {
                series?.id?.let {
                    deleteById(it)
                    withContext (Dispatchers.Main) { changeState.value = DetailChangeState.DELETED }
                }
            } else {
                withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVED }
            }
            onChangeDone()

        }
    }

    /**
     * Adds a new relatedTo to the passed entries relating to the current ICalObject as a PARENT
     * @param newSubEntries that should get a link to the current entry
     */
    fun linkNewSubentries(newSubEntries: List<ICal4List>) {
        viewModelScope.launch(Dispatchers.IO) {
            newSubEntries.forEach { newSubEntry ->
                if(icalEntity.value?.property?.uid == null)
                    return@forEach

                if(newSubEntry.uid == icalEntity.value?.property?.uid)
                    return@forEach

                val existing = icalEntity.value?.property?.uid?.let { database.findRelatedTo(newSubEntry.id, it, Reltype.PARENT.name) != null } ?: return@forEach
                if(!existing) {
                    database.insertRelatedto(
                        Relatedto(
                            icalObjectId = newSubEntry.id,
                            text = icalEntity.value?.property?.uid!!,
                            reltype = Reltype.PARENT.name
                        )
                    )
                    database.getICalObjectById(newSubEntry.id)?.let {
                        it.makeDirty()
                        database.update(it)
                    }
                    onChangeDone()
                }
            }
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
            val newId = ICalObject.updateCollectionWithChildren(icalObject.id, null, newCollectionId, database, _application)
            newId?.let { load(it, _isAuthenticated) }
            // once the newId is there, the local entries can be deleted (or marked as deleted)
            ICalObject.deleteItemWithChildren(icalObject.id, database)        // make sure to delete the old item (or marked as deleted - this is already handled in the function)
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
                save(it, mutableCategories, mutableComments, mutableAttendees, mutableResources, mutableAttachments, mutableAlarms)
            }
            onChangeDone()
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
        withContext (Dispatchers.Main) { changeState.value = DetailChangeState.LOADING }

        try {
            if (icalEntity.value?.categories != categories || icalEntity.value?.property?.id != icalObject.id) {
                icalObject.makeDirty()
                database.deleteCategories(icalObject.id)
                categories.forEach { changedCategory ->
                    changedCategory.icalObjectId = icalObject.id
                    database.insertCategory(changedCategory)
                }
            }

            if (icalEntity.value?.comments != comments || icalEntity.value?.property?.id != icalObject.id) {
                icalObject.makeDirty()
                database.deleteComments(icalObject.id)
                comments.forEach { changedComment ->
                    changedComment.icalObjectId = icalObject.id
                    database.insertComment(changedComment)
                }
            }

            if (icalEntity.value?.attendees != attendees || icalEntity.value?.property?.id != icalObject.id) {
                icalObject.makeDirty()
                database.deleteAttendees(icalObject.id)
                attendees.forEach { changedAttendee ->
                    changedAttendee.icalObjectId = icalObject.id
                    database.insertAttendee(changedAttendee)
                }
            }

            if (icalEntity.value?.resources != resources || icalEntity.value?.property?.id != icalObject.id) {
                icalObject.makeDirty()
                database.deleteResources(icalObject.id)
                resources.forEach { changedResource ->
                    changedResource.icalObjectId = icalObject.id
                    database.insertResource(changedResource)
                }
            }

            if (icalEntity.value?.attachments != attachments || icalEntity.value?.property?.id != icalObject.id) {
                icalObject.makeDirty()
                database.deleteAttachments(icalObject.id)
                attachments.forEach { changedAttachment ->
                    changedAttachment.icalObjectId = icalObject.id
                    database.insertAttachment(changedAttachment)
                }
                Attachment.scheduleCleanupJob(getApplication())
            }

            if (icalEntity.value?.alarms != alarms || icalEntity.value?.property?.id != icalObject.id) {
                icalObject.makeDirty()
                database.deleteAlarms(icalObject.id)
                alarms.forEach { changedAlarm ->
                    changedAlarm.icalObjectId = icalObject.id
                    changedAlarm.alarmId = database.insertAlarm(changedAlarm)
                }
            }

            icalObject.makeDirty()
            database.update(icalObject)
            icalObject.makeSeriesDirty(database)
            icalObject.recreateRecurring(_application)
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
                    eTag = icalEntity.value?.property?.eTag
                    sequence = (icalEntity.value?.property?.sequence?:0)+1
                },
                categories = originalEntry?.categories ?: emptyList(),
                comments = originalEntry?.comments ?: emptyList(),

                attendees = originalEntry?.attendees ?: emptyList(),
                resources = originalEntry?.resources ?: emptyList(),
                attachments = originalEntry?.attachments ?: emptyList(),
                alarms = originalEntry?.alarms ?: emptyList()
            )
            navigateToId.value = icalEntity.value?.property?.id
            onChangeDone()
        }
    }


    fun addSubEntry(subEntry: ICalObject, attachment: Attachment?) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.LOADING }
            subEntry.collectionId = icalEntity.value?.property?.collectionId!!

            try {
                val subEntryId = database.insertICalObject(subEntry)
                attachment?.let {
                    it.icalObjectId = subEntryId
                    database.insertAttachment(it)
                }
                database.insertRelatedto(
                    Relatedto(
                        icalObjectId = subEntryId,
                        reltype = Reltype.PARENT.name,
                        text = icalEntity.value?.property?.uid!!
                    )
                )
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
        viewModelScope.launch(Dispatchers.IO) {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.DELETING }
            try {
                if(icalEntity.value?.property?.recurid != null) {
                    ICalObject.unlinkFromSeries(icalEntity.value?.property!!, database)
                    //toastMessage.value = _application.getString(R.string.toast_item_is_now_recu_exception)
                }
                icalEntity.value?.property?.id?.let { id ->
                    ICalObject.deleteItemWithChildren(id, database)
                    SyncUtil.notifyContentObservers(getApplication())
                    withContext (Dispatchers.Main) { changeState.value = DetailChangeState.DELETED }
                    toastMessage.value = _application.getString(R.string.details_toast_entry_deleted)
                }
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", "Corrupted ID: ${icalEntity.value?.property?.id}")
                Log.d("SQLConstraint", e.stackTraceToString())
                withContext (Dispatchers.Main) { changeState.value = DetailChangeState.SQLERROR }
            }
            onChangeDone()
        }
    }

    /**
     * Delete function for subtasks and subnotes
     * @param [icalObjectId] of the subtask/subnote to be deleted
     */
    fun deleteById(icalObjectId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.LOADING }
            try {
                ICalObject.deleteItemWithChildren(icalObjectId, database)
                withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVED }
                toastMessage.value = _application.getString(R.string.details_toast_entry_deleted)
                SyncUtil.notifyContentObservers(getApplication())
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", "Corrupted ID: $icalObjectId")
                Log.d("SQLConstraint", e.stackTraceToString())
                withContext (Dispatchers.Main) { changeState.value = DetailChangeState.SQLERROR }
            }
            onChangeDone()
        }
    }

    fun unlinkFromParent(icalObjectId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.LOADING }
            database.deleteRelatedto(icalObjectId, icalEntity.value?.property?.uid?:"")
            database.getICalObjectByIdSync(icalObjectId)?.let {
                it.makeDirty()
                database.update(it)
            }
            onChangeDone()
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVED }
        }
    }

    fun createCopy(newModule: Module) {
        icalEntity.value?.let { createCopy(it, newModule) }
    }

    private fun createCopy(icalEntityToCopy: ICalEntity, newModule: Module, newParentUID: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.LOADING }
            val newEntity = icalEntityToCopy.getIcalEntityCopy(newModule)
            try {
                val newId = database.insertICalObject(newEntity.property)
                newEntity.alarms?.forEach { alarm ->
                    database.insertAlarm(
                        alarm.copy(
                            alarmId = 0L,
                            icalObjectId = newId
                        )
                    )
                }
                newEntity.attachments?.forEach { attachment ->
                    database.insertAttachment(
                        attachment.copy(
                            icalObjectId = newId,
                            attachmentId = 0L
                        )
                    )
                }
                newEntity.attendees?.forEach { attendee ->
                    database.insertAttendee(attendee.copy(icalObjectId = newId, attendeeId = 0L))
                }
                newEntity.categories?.forEach { category ->
                    database.insertCategory(category.copy(icalObjectId = newId, categoryId = 0L))
                }
                newEntity.comments?.forEach { comment ->
                    database.insertComment(comment.copy(icalObjectId = newId, commentId = 0L))
                }
                newEntity.resources?.forEach { resource ->
                    database.insertResource(resource.copy(icalObjectId = newId, resourceId = 0L))
                }
                newEntity.unknown?.forEach { unknown ->
                    database.insertUnknownSync(unknown.copy(icalObjectId = newId, unknownId = 0L))
                }
                newEntity.organizer?.let { organizer ->
                    database.insertOrganizer(organizer.copy(icalObjectId = newId, organizerId = 0L))
                }

                newEntity.relatedto?.forEach { relatedto ->
                    if (relatedto.reltype == Reltype.PARENT.name && newParentUID != null) {
                        database.insertRelatedto(
                            relatedto.copy(
                                relatedtoId = 0L,
                                icalObjectId = newId,
                                text = newParentUID
                            )
                        )
                    }
                }

                val children = database.getRelatedChildren(icalEntityToCopy.property.id)
                children.forEach { child ->
                    database.getSync(child.id)?.let {
                        createCopy(
                            icalEntityToCopy = it,
                            newModule = it.property.getModuleFromString(),
                            newParentUID = newEntity.property.uid
                        )
                    }
                }

                onChangeDone()

                if(newParentUID == null)   // we navigate only to the parent (not to the children that are invoked recursively)
                    navigateToId.value = newId

                withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVED }
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", e.stackTraceToString())
                withContext (Dispatchers.Main) { changeState.value = DetailChangeState.SQLERROR }
            }
        }
    }

    /**
     * Creates a share intent to share the current entry as .ics file
     */
    fun shareAsICS(context: Context) {
        viewModelScope.launch(Dispatchers.IO)  {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }
            val iCalEntity = icalEntity.value ?: return@launch
            val icsContentUri = createContentUri(iCalEntity) ?: return@launch

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
            val iCalEntity = icalEntity.value ?: return@launch
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
        val children = database.getRelatedChildren(parent)
        list.addAll(children.map { it.id })
        children.forEach { addChildrenOf(it.id, list) }
    }

    /**
     * Notifies the contentObservers
     * schedules the notifications
     * updates the widget
     */
    private suspend fun onChangeDone() {
        SyncUtil.notifyContentObservers(getApplication())
        NotificationPublisher.scheduleNextNotifications(getApplication())
        GeofenceClient(_application).setGeofences()
        ListWidget().updateAll(getApplication())
    }

    enum class DetailChangeState { LOADING, UNCHANGED, CHANGEUNSAVED, SAVINGREQUESTED, CHANGESAVING, CHANGESAVED, DELETING, DELETED, SQLERROR }
}
