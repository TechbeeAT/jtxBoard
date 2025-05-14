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
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.GeofenceClient
import at.techbee.jtx.ui.list.ListSettings
import at.techbee.jtx.ui.list.OrderBy
import at.techbee.jtx.ui.list.SortOrder
import at.techbee.jtx.ui.settings.DropdownSettingOption
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
    private var databaseDao: ICalDatabaseDao = ICalDatabase.getInstance(application).iCalDatabaseDao()

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

    private var selectFromAllListQuery: MutableLiveData<SimpleSQLiteQuery> =
        MutableLiveData<SimpleSQLiteQuery>()
    var selectFromAllList: LiveData<List<ICal4ListRel>> =
        selectFromAllListQuery.switchMap { databaseDao.getIcal4ListRel(it) }

    var navigateToId = mutableStateOf<Long?>(null)
    var changeState = mutableStateOf(DetailChangeState.LOADING)
    var toastMessage = mutableStateOf<String?>(null)
    val detailSettings: DetailSettings = DetailSettings()
    val settingsStateHolder = SettingsStateHolder(_application)

    val mediaPlayer = MediaPlayer()

    private var _isAuthenticated = false
    private var immediateAlarmTriggeredOnce = false

    companion object {
        const val PREFS_DETAIL_JOURNALS = "prefsDetailJournals"
        const val PREFS_DETAIL_NOTES = "prefsDetailNotes"
        const val PREFS_DETAIL_TODOS = "prefsDetailTodos"
    }

    fun load(icalObjectId: Long, isAuthenticated: Boolean) {
        mainICalObjectId = icalObjectId
        _isAuthenticated = isAuthenticated
        immediateAlarmTriggeredOnce = false
        viewModelScope.launch {
            withContext(Dispatchers.Main) { changeState.value = DetailChangeState.LOADING }

            mutableCategories.clear()
            mutableResources.clear()
            mutableAttendees.clear()
            mutableComments.clear()
            mutableAttachments.clear()
            mutableAlarms.clear()

            withContext(Dispatchers.IO) {
                originalEntry = databaseDao.getSync(icalObjectId)
                mutableICalObject = originalEntry?.property
                mutableCategories.addAll(databaseDao.getCategoriesSync(icalObjectId))
                mutableResources.addAll(databaseDao.getResourcesSync(icalObjectId))
                mutableAttendees.addAll(databaseDao.getAttendeesSync(icalObjectId))
                mutableComments.addAll(databaseDao.getCommentsSync(icalObjectId))
                mutableAttachments.addAll(databaseDao.getAttachmentsSync(icalObjectId))
                mutableAlarms.addAll(databaseDao.getAlarmsSync(icalObjectId))
            }

            //icalEntity = databaseDao.get(icalObjectId)
            icalObject = databaseDao.getICalObject(icalObjectId)
            relatedTo = databaseDao.getRelatedTo(icalObjectId)

            collection = icalObject.switchMap {
                it?.let { cur -> databaseDao.getCollection(cur.collectionId) }

            }

            relatedParents = relatedTo.switchMap {
                it.map { relatedto -> relatedto.text }.let { uids ->
                    databaseDao.getICal4ListByUIDs(uids)
                }
            }
            relatedSubtasks = icalObject.switchMap {
                it?.uid?.let { parentUid ->
                    databaseDao.getIcal4List(
                        ICal4List.getQueryForAllSubentriesForParentUID(
                            parentUid = parentUid,
                            component = Component.VTODO,
                            hideBiometricProtected = if (_isAuthenticated) emptyList() else ListSettings.getProtectedClassificationsFromSettings(
                                _application
                            ),
                            orderBy = detailSettings.listSettings?.subtasksOrderBy?.value
                                ?: OrderBy.CREATED,
                            sortOrder = detailSettings.listSettings?.subtasksSortOrder?.value
                                ?: SortOrder.DESC
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
                            hideBiometricProtected = if (_isAuthenticated) emptyList() else ListSettings.getProtectedClassificationsFromSettings(
                                _application
                            ),
                            orderBy = detailSettings.listSettings?.subnotesOrderBy?.value
                                ?: OrderBy.CREATED,
                            sortOrder = detailSettings.listSettings?.subnotesSortOrder?.value
                                ?: SortOrder.DESC
                        )
                    )
                }
            }
            seriesElement = icalObject.switchMap { databaseDao.getSeriesICalObjectIdByUID(it?.uid) }
            seriesInstances =
                icalObject.switchMap { databaseDao.getSeriesInstancesICalObjectsByUID(it?.uid) }
            isChild = databaseDao.isChild(icalObjectId)

            withContext(Dispatchers.Main) { changeState.value = DetailChangeState.UNCHANGED }
        }
    }

    fun updateSelectFromAllListQuery(
        searchText: String,
        modules: List<Module>,
        sameCollection: Boolean,
        sameAccount: Boolean
    ) {
        selectFromAllListQuery.postValue(
            ICal4List.constructQuery(
                modules = modules,
                searchText = searchText,
                searchCollection = if (sameCollection) collection.value?.displayName?.let {
                    listOf(
                        it
                    )
                } ?: emptyList() else emptyList(),
                searchAccount = if (sameAccount) collection.value?.accountName?.let { listOf(it) }
                    ?: emptyList() else emptyList(),
                flatView = true,
                orderBy = OrderBy.LAST_MODIFIED,
                sortOrder = SortOrder.DESC,
                hideBiometricProtected = if (_isAuthenticated) emptyList() else ListSettings.getProtectedClassificationsFromSettings(
                    _application
                )
            )
        )
    }


    fun updateProgress(id: Long, newPercent: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }
            databaseDao.updateProgress(
                id = id,
                newPercent = newPercent,
                settingKeepStatusProgressCompletedInSync = settingsStateHolder.settingKeepStatusProgressCompletedInSync.value,
                settingLinkProgressToSubtasks = settingsStateHolder.settingLinkProgressToSubtasks.value
            )
            if(newPercent == 100) {
                NotificationManagerCompat.from(_application).cancel(id.toInt())
                databaseDao.setAlarmNotification(id, false)
            }
            onChangeDone()
            withContext(Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVED }
        }
    }

    fun updateSummary(icalObjectId: Long, newSummary: String) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }
            val icalObject = databaseDao.getICalObjectById(icalObjectId) ?: return@launch
            icalObject.summary = newSummary
            icalObject.makeDirty()
            databaseDao.makeSeriesDirty(icalObject.uid)
            try {
                databaseDao.update(icalObject)
                withContext(Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVED }
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", "Corrupted ID: $icalObjectId")
                Log.d("SQLConstraint", e.stackTraceToString())
                withContext(Dispatchers.Main) { changeState.value = DetailChangeState.SQLERROR }
            }
            onChangeDone()
        }
    }

    fun updateSortOrder(list: List<ICal4List>) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.updateSortOrder(list.map { it.id })
            onChangeDone()
        }
    }

    fun unlinkFromSeries(
        instances: List<ICalObject>,
        series: ICalObject?,
        deleteAfterUnlink: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }
            databaseDao.unlinkFromSeries(instances, series, deleteAfterUnlink)

            withContext(Dispatchers.Main) {
                if (deleteAfterUnlink) {
                    changeState.value = DetailChangeState.DELETED_BACK_TO_LIST
                } else {
                    changeState.value = DetailChangeState.CHANGESAVED
                }
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

            databaseDao.linkChildren(
                parentId = mainICalObjectId!!,
                childrenIds = newSubEntries.map { it.id }
            )
            onChangeDone()
        }
    }

    /**
     * Adds a new relatedTo to the passed entries relating to the current ICalObject as a CHILD
     * @param newParents that should get a link to the current entry
     */
    fun linkNewParents(newParents: List<ICal4List>) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDao.linkParents(
                parentIds = newParents.map { it.id },
                childId = mainICalObjectId!!
            )
            onChangeDone()
        }
    }

    fun moveToNewCollection(newCollectionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }

            mutableICalObject?.let {
                // make sure the eTag, flags, scheduleTag and fileName gets updated in the background if the sync is triggered, so that another sync won't overwrite the changes!
                icalObject.value?.eTag.let { currentETag -> it.eTag = currentETag }
                icalObject.value?.flags.let { currentFlags -> it.flags = currentFlags }
                icalObject.value?.scheduleTag.let { currentScheduleTag ->
                    it.scheduleTag = currentScheduleTag
                }
                icalObject.value?.fileName.let { currentFileName ->
                    it.fileName = currentFileName
                }

                saveSuspend(false)
                onChangeDone()
                val newId = databaseDao.moveToCollection(it.id, newCollectionId)
                if(newId == null) {
                    withContext(Dispatchers.Main) {
                        changeState.value = DetailChangeState.SQLERROR
                    }
                    return@launch
                }
                load(newId, _isAuthenticated)
            }
            withContext(Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVED }

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
                    if (module == Module.NOTE) {
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

                    if (Status.valuesFor(Module.JOURNAL)
                            .none { status -> status.status == it.status }
                    )
                        it.status = Status.FINAL.status

                } else if (module == Module.TODO) {
                    it.component = Component.VTODO.name
                    if (Status.valuesFor(Module.TODO)
                            .none { status -> status.status == it.status }
                    )
                        it.status = Status.NEEDS_ACTION.status
                }
                databaseDao.saveAll(
                    it,
                    mutableCategories,
                    mutableComments,
                    mutableAttendees,
                    mutableResources,
                    mutableAttachments,
                    mutableAlarms,
                    mutableICalObject!!.id != mainICalObjectId

                )
            }
            onChangeDone()
        }
    }


    fun saveEntry(triggerImmediateAlarm: Boolean) {
        mutableICalObject?.let {
            // make sure the eTag, flags, scheduleTag and fileName gets updated in the background if the sync is triggered, so that another sync won't overwrite the changes!
            icalObject.value?.eTag.let { currentETag -> it.eTag = currentETag }
            icalObject.value?.flags.let { currentFlags -> it.flags = currentFlags }
            icalObject.value?.scheduleTag.let { currentScheduleTag ->
                it.scheduleTag = currentScheduleTag
            }
            icalObject.value?.fileName.let { currentFileName ->
                it.fileName = currentFileName
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }
            saveSuspend(triggerImmediateAlarm)
            onChangeDone()
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVED }
        }
    }

    private suspend fun saveSuspend(triggerImmediateAlarm: Boolean) {
        mutableICalObject?.let {
            databaseDao.saveAll(
                it,
                mutableCategories,
                mutableComments,
                mutableAttendees,
                mutableResources,
                mutableAttachments,
                mutableAlarms,
                mutableICalObject!!.id != mainICalObjectId
            )

            if(
                (settingsStateHolder.settingAutoAlarm.value == DropdownSettingOption.AUTO_ALARM_ALWAYS_ON_DUE && (mutableICalObject?.due?:0L) > System.currentTimeMillis())
                || (settingsStateHolder.settingAutoAlarm.value == DropdownSettingOption.AUTO_ALARM_ON_DUE && (mutableICalObject?.due?:0L) > System.currentTimeMillis())
                || (settingsStateHolder.settingAutoAlarm.value == DropdownSettingOption.AUTO_ALARM_ON_START && (mutableICalObject?.dtstart?:0L) > System.currentTimeMillis())
                || mutableAlarms.any { alarm -> (alarm.triggerTime?:0L) > System.currentTimeMillis() }
            ) {
                NotificationManagerCompat.from(_application).cancel(it.id.toInt())
                databaseDao.setAlarmNotification(it.id, false)
            }

            val triggerInPastButNotDone =
                mutableAlarms.any { alarm -> (alarm.triggerTime?:0L) <= System.currentTimeMillis() }
                        && mutableAlarms.none  { alarm -> (alarm.triggerTime?:0L) > System.currentTimeMillis() }
                        && it.percent != 100
                        && it.status != Status.COMPLETED.status

            if(!immediateAlarmTriggeredOnce && (triggerImmediateAlarm || triggerInPastButNotDone)) {
                NotificationPublisher.triggerImmediateAlarm(it, _application)
                immediateAlarmTriggeredOnce = true
            }
        }
    }



    /**
     * reverts the current entry back to the original values that were stored in originalEntry
     */
    fun revert() {
        val originalICalObject = originalEntry?.property ?: return
        viewModelScope.launch(Dispatchers.IO) {
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }
            databaseDao.saveAll(
                icalObject = originalICalObject.apply {
                    eTag = icalObject.value?.eTag
                    sequence = (icalObject.value?.sequence ?: 0) + 1
                },
                categories = originalEntry?.categories ?: emptyList(),
                comments = originalEntry?.comments ?: emptyList(),
                attendees = originalEntry?.attendees ?: emptyList(),
                resources = originalEntry?.resources ?: emptyList(),
                attachments = originalEntry?.attachments ?: emptyList(),
                alarms = originalEntry?.alarms ?: emptyList(),
                enforceUpdateAll = mutableICalObject!!.id != mainICalObjectId

            )
            withContext (Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVED }
            navigateToId.value = icalObject.value?.id
            onChangeDone()
        }
    }

    fun addSubEntries(subEntries: List<ICalObject>, parentUID: String, collectionId: Long) {
        subEntries.forEach { addSubEntry(it, null, parentUID, collectionId) }
    }

    fun addSubEntry(subEntry: ICalObject, attachment: Attachment?, parentUID: String, collectionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }
            subEntry.collectionId = collectionId

            databaseDao.addSubEntry(
                parentUID = parentUID,
                subEntry = subEntry,
                attachment = attachment
            )

            withContext(Dispatchers.Main) {
                    changeState.value = DetailChangeState.CHANGESAVED
            }
            onChangeDone()
        }
    }


    /**
     * Deletes the current entry with its children
     */
    fun delete() = deleteById(icalObjectId = mainICalObjectId!!, mainICalObjectDeleted = true)

    /**
     * Delete function for subtasks and subnotes
     * @param [icalObjectId] of the subtask/subnote to be deleted
     */
    fun deleteById(icalObjectId: Long, mainICalObjectDeleted: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { changeState.value = DetailChangeState.LOADING }
            databaseDao.deleteICalObject(icalObjectId)
            withContext(Dispatchers.Main) {
                changeState.value = DetailChangeState.CHANGESAVED
                toastMessage.value = _application.getString(R.string.details_toast_entry_deleted)
                if(mainICalObjectDeleted)
                    changeState.value = DetailChangeState.DELETED
            }
            onChangeDone()
        }
    }

    fun unlinkFromParent(icalObjectId: Long, parentUID: String?) {
        if (parentUID == null)
            return

        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { changeState.value = DetailChangeState.LOADING }
            databaseDao.unlinkFromParent(icalObjectId, parentUID)
            onChangeDone()
            withContext(Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVED }
        }
    }

    fun createCopy(newModule: Module) {
        val parentUID = relatedParents.value?.firstOrNull()?.uid

        viewModelScope.launch(Dispatchers.IO) {

            withContext(Dispatchers.Main) { changeState.value = DetailChangeState.LOADING }

            val newId = databaseDao.createCopy(
                iCalObjectIdToCopy = mainICalObjectId!!,
                module = newModule,
                parentUID = parentUID
            )
            withContext(Dispatchers.Main) {
                changeState.value = DetailChangeState.CHANGESAVED
            }
            onChangeDone()
            newId?.let { navigateToId.value = it }
        }
    }


    /**
     * Creates a share intent to share the current entry as .ics file
     */
    fun shareAsICS(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }
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
                withContext(Dispatchers.Main) {
                    changeState.value = DetailChangeState.UNCHANGED
                }
            }
        }
    }

    /**
     * Creates a share intent to share the current entry as email.
     * The intent sets the subject (summary), attendees (receipients),
     * text (the contents as a text representation) and attachments
     */
    fun shareAsText(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val iCalEntity = databaseDao.getSync(mainICalObjectId!!) ?: return@launch
            withContext(Dispatchers.Main) { changeState.value = DetailChangeState.CHANGESAVING }

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
                toastMessage.value =
                    _application.getString(R.string.error_no_app_found_to_open_entry)
            } finally {
                withContext(Dispatchers.Main) {
                    changeState.value = DetailChangeState.UNCHANGED
                }
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
