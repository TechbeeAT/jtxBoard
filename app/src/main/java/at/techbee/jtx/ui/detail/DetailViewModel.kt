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
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import at.techbee.jtx.AUTHORITY_FILEPROVIDER
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.list.OrderBy
import at.techbee.jtx.ui.list.SortOrder
import at.techbee.jtx.util.Ical4androidUtil
import at.techbee.jtx.util.SyncUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _application = application
    private var database: ICalDatabaseDao = ICalDatabase.getInstance(application).iCalDatabaseDao

    lateinit var icalEntity: LiveData<ICalEntity?>
    lateinit var relatedSubnotes: LiveData<List<ICal4List>>
    lateinit var relatedSubtasks: LiveData<List<ICal4List>>
    //lateinit var recurInstances: LiveData<List<ICalObject?>>
    lateinit var isChild: LiveData<Boolean>
    private var originalEntry: ICalEntity? = null

    lateinit var allCategories: LiveData<List<String>>
    lateinit var allResources: LiveData<List<String>>
    lateinit var allWriteableCollections: LiveData<List<ICalCollection>>

    var entryDeleted = mutableStateOf(false)
    var sqlConstraintException = mutableStateOf(false)
    var navigateToId = mutableStateOf<Long?>(null)
    var changeState = mutableStateOf(DetailChangeState.UNCHANGED)
    var toastMessage = mutableStateOf<String?>(null)
    val detailSettings: DetailSettings = DetailSettings()

    val mediaPlayer = MediaPlayer()

    companion object {
        const val PREFS_DETAIL_JOURNALS = "prefsDetailJournals"
        const val PREFS_DETAIL_NOTES = "prefsDetailNotes"
        const val PREFS_DETAIL_TODOS = "prefsDetailTodos"
    }

    init {

        viewModelScope.launch {

            // insert a new value to initialize the item or load the existing one from the DB
            icalEntity = MutableLiveData<ICalEntity?>().apply {
                    postValue(ICalEntity(ICalObject(), null, null, null, null, null))
                }

            allCategories = database.getAllCategoriesAsText()
            allResources = database.getAllResourcesAsText()
            allWriteableCollections = database.getAllWriteableCollections()

            relatedSubnotes = MutableLiveData(emptyList())
            relatedSubtasks = MutableLiveData(emptyList())
            isChild = MutableLiveData(false)

            /*
            recurInstances = Transformations.switchMap(icalEntity) {
                it?.property?.id?.let { originalId -> database.getRecurInstances(originalId) }
            }
             */
        }
    }

    fun load(icalObjectId: Long) {
        viewModelScope.launch {
            icalEntity = database.get(icalObjectId)

            relatedSubnotes = Transformations.switchMap(icalEntity) {
                it?.property?.uid?.let { parentUid ->
                    database.getIcal4List(ICal4List.getQueryForAllSubnotesForParentUID(parentUid, detailSettings.listSettings?.subnotesOrderBy?.value ?: OrderBy.CREATED, detailSettings.listSettings?.subnotesSortOrder?.value ?: SortOrder.ASC ))
                }
            }

            relatedSubtasks = Transformations.switchMap(icalEntity) {
                it?.property?.uid?.let { parentUid ->
                    database.getIcal4List(ICal4List.getQueryForAllSubtasksForParentUID(parentUid, detailSettings.listSettings?.subtasksOrderBy?.value ?: OrderBy.CREATED, detailSettings.listSettings?.subtasksSortOrder?.value ?: SortOrder.ASC ))
                }
            }
            isChild = database.isChild(icalObjectId)
        }

        viewModelScope.launch(Dispatchers.IO) {
            originalEntry = database.getSync(icalObjectId)  // store original entry for revert option
        }
    }



    fun updateProgress(id: Long, newPercent: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            changeState.value = DetailChangeState.CHANGESAVING
            val item = database.getICalObjectById(id) ?: return@launch
            try {
                if(icalEntity.value?.property?.isRecurLinkedInstance == true) {
                    ICalObject.makeRecurringException(icalEntity.value?.property!!, database)
                    toastMessage.value = _application.getString(R.string.toast_item_is_now_recu_exception)
                }
                item.setUpdatedProgress(newPercent)
                database.update(item)
                SyncUtil.notifyContentObservers(getApplication())
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", "Corrupted ID: $id")
                Log.d("SQLConstraint", e.stackTraceToString())
                sqlConstraintException.value = true
            } finally {
                changeState.value = DetailChangeState.CHANGESAVED
            }
        }
    }

    fun updateSummary(icalObjectId: Long, newSummary: String) {
        viewModelScope.launch(Dispatchers.IO) {
            changeState.value = DetailChangeState.CHANGESAVING
            val icalObject = database.getICalObjectById(icalObjectId) ?: return@launch
            icalObject.summary = newSummary
            icalObject.makeDirty()
            try {
                database.update(icalObject)
                SyncUtil.notifyContentObservers(getApplication())
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", "Corrupted ID: $icalObjectId")
                Log.d("SQLConstraint", e.stackTraceToString())
                sqlConstraintException.value = true
            } finally {
                changeState.value = DetailChangeState.CHANGESAVED
            }
        }
    }

    fun moveToNewCollection(icalObject: ICalObject, newCollectionId: Long) {

        viewModelScope.launch(Dispatchers.IO) {
            while(changeState.value != DetailChangeState.CHANGESAVED)
                delay(50)

            changeState.value = DetailChangeState.CHANGESAVING
            try {
                val newId = ICalObject.updateCollectionWithChildren(icalObject.id, null, newCollectionId, database, getApplication())
                // once the newId is there, the local entries can be deleted (or marked as deleted)
                ICalObject.deleteItemWithChildren(icalObject.id, database)        // make sure to delete the old item (or marked as deleted - this is already handled in the function)
                if (icalObject.rrule != null)
                    icalObject.recreateRecurring(getApplication())
                changeState.value = DetailChangeState.CHANGESAVED
                navigateToId.value = newId
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", "Corrupted ID: ${icalObject.id}")
                Log.d("SQLConstraint", e.stackTraceToString())
                sqlConstraintException.value = true
            } finally {
                changeState.value = DetailChangeState.CHANGESAVED
            }
        }
    }


    fun save(icalObject: ICalObject,
             categories: List<Category>,
             comments: List<Comment>,
             attendees: List<Attendee>,
             resources: List<Resource>,
             attachments: List<Attachment>,
             alarms: List<Alarm>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            changeState.value = DetailChangeState.CHANGESAVING

            try {
                if (icalEntity.value?.categories != categories) {
                    icalObject.makeDirty()
                    database.deleteCategories(icalObject.id)
                    categories.forEach { changedCategory ->
                        changedCategory.icalObjectId = icalObject.id
                        database.insertCategory(changedCategory)
                    }
                }

                if (icalEntity.value?.comments != comments) {
                    icalObject.makeDirty()
                    database.deleteComments(icalObject.id)
                    comments.forEach { changedComment ->
                        changedComment.icalObjectId = icalObject.id
                        database.insertComment(changedComment)
                    }
                }

                if (icalEntity.value?.attendees != attendees) {
                    icalObject.makeDirty()
                    database.deleteAttendees(icalObject.id)
                    attendees.forEach { changedAttendee ->
                        changedAttendee.icalObjectId = icalObject.id
                        database.insertAttendee(changedAttendee)
                    }
                }

                if (icalEntity.value?.resources != resources) {
                    icalObject.makeDirty()
                    database.deleteResources(icalObject.id)
                    resources.forEach { changedResource ->
                        changedResource.icalObjectId = icalObject.id
                        database.insertResource(changedResource)
                    }
                }

                if (icalEntity.value?.attachments != attachments) {
                    icalObject.makeDirty()
                    database.deleteAttachments(icalObject.id)
                    attachments.forEach { changedAttachment ->
                        changedAttachment.icalObjectId = icalObject.id
                        database.insertAttachment(changedAttachment)
                    }
                    Attachment.scheduleCleanupJob(getApplication())
                }

                if (icalEntity.value?.alarms != alarms) {
                    icalObject.makeDirty()
                    database.deleteAlarms(icalObject.id)
                    alarms.forEach { changedAlarm ->
                        changedAlarm.icalObjectId = icalObject.id
                        changedAlarm.alarmId = database.insertAlarm(changedAlarm)
                    }
                }

                // removed check as it was causing problems when loading the entry from the widget, somehow it is not reliable enough...
                // if (icalEntity.value?.property?.equals(icalObject) == false) {
                icalObject.makeDirty()
                database.update(icalObject)

                if (icalEntity.value?.property?.isRecurLinkedInstance == true) {
                    ICalObject.makeRecurringException(icalEntity.value?.property!!, database)
                    toastMessage.value = _application.getString(R.string.toast_item_is_now_recu_exception)
                }
                icalObject.recreateRecurring(getApplication())
                //}

                Alarm.scheduleNextNotifications(getApplication())
                SyncUtil.notifyContentObservers(getApplication())
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", "Corrupted ID: ${icalObject.id}")
                Log.d("SQLConstraint", e.stackTraceToString())
                sqlConstraintException.value = true
            } finally {
                changeState.value = DetailChangeState.CHANGESAVED
            }
        }
    }

    /**
     * reverts the current entry back to the original values that were stored in originalEntry
     */
    fun revert() {

        val originalICalObject = originalEntry?.property?: return
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
    }


    fun addSubEntry(subEntry: ICalObject, attachment: Attachment?) {
        viewModelScope.launch(Dispatchers.IO) {
            changeState.value = DetailChangeState.CHANGESAVING
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
                if(icalEntity.value?.property?.isRecurLinkedInstance == true) {
                    ICalObject.makeRecurringException(icalEntity.value?.property!!, database)
                    toastMessage.value = _application.getString(R.string.toast_item_is_now_recu_exception)
                }
                SyncUtil.notifyContentObservers(getApplication())
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", e.stackTraceToString())
                sqlConstraintException.value = true
            } finally {
                changeState.value = DetailChangeState.CHANGESAVED
            }
        }
    }


    /**
     * Deletes the current entry with its children
     */
    fun delete() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if(icalEntity.value?.property?.isRecurLinkedInstance == true) {
                    ICalObject.makeRecurringException(icalEntity.value?.property!!, database)
                    toastMessage.value = _application.getString(R.string.toast_item_is_now_recu_exception)
                }
                icalEntity.value?.property?.id?.let { id ->
                    ICalObject.deleteItemWithChildren(id, database)
                    SyncUtil.notifyContentObservers(getApplication())
                    entryDeleted.value = true
                }
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", "Corrupted ID: ${icalEntity.value?.property?.id}")
                Log.d("SQLConstraint", e.stackTraceToString())
                sqlConstraintException.value = true
            }
        }
    }

    /**
     * Delete function for subtasks and subnotes
     * @param [icalObjectId] of the subtask/subnote to be deleted
     */
    fun deleteById(icalObjectId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                ICalObject.deleteItemWithChildren(icalObjectId, database)
                SyncUtil.notifyContentObservers(getApplication())
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", "Corrupted ID: $icalObjectId")
                Log.d("SQLConstraint", e.stackTraceToString())
                sqlConstraintException.value = true
            }
        }
    }

    fun createCopy(newModule: Module) {
        icalEntity.value?.let { createCopy(it, newModule) }
    }

    private fun createCopy(icalEntityToCopy: ICalEntity, newModule: Module, newParentUID: String? = null) {
        val newEntity = icalEntityToCopy.getIcalEntityCopy(newModule)

        viewModelScope.launch(Dispatchers.IO) {
            changeState.value = DetailChangeState.CHANGESAVING
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
                    database.getSync(child)?.let {
                        createCopy(
                            icalEntityToCopy = it,
                            newModule = it.property.getModuleFromString(),
                            newParentUID = newEntity.property.uid
                        )
                    }
                }

                Alarm.scheduleNextNotifications(getApplication())

                if(newParentUID == null)   // we navigate only to the parent (not to the children that are invoked recursively)
                    navigateToId.value = newId

            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", e.stackTraceToString())
                sqlConstraintException.value = true
            } finally {
                changeState.value = DetailChangeState.CHANGESAVED
            }
        }
    }

    /**
     * Creates a share intent to share the current entry as .ics file
     */
    fun shareAsICS(context: Context) {

        viewModelScope.launch(Dispatchers.IO)  {
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
            }
        }
    }

    /**
     * @param [iCalEntity] for which the content should be put in a file that is returned as Uri
     * @return the uri for the file with the [iCalEntity] content in as .ics format or null
     */
    private fun createContentUri(iCalEntity: ICalEntity): Uri? {
        val icsFile = writeIcsFile(iCalEntity) ?: return null

        val context = getApplication<Application>()
        return FileProvider.getUriForFile(context, AUTHORITY_FILEPROVIDER, icsFile)
    }

    /**
     * @param [iCalEntity] for which the content should be put in a file
     * @return a file with the [iCalEntity] content in as .ics format or null
     */
    private fun writeIcsFile(iCalEntity: ICalEntity): File? {
        val context = getApplication<Application>()
        val account = iCalEntity.ICalCollection?.getAccount() ?: return null
        val collectionId = iCalEntity.property.collectionId
        val iCalObjectId = iCalEntity.property.id

        return try {
            val outputFile = File(context.externalCacheDir, "ics_file.ics")

            FileOutputStream(outputFile).use { outputStream ->
                Ical4androidUtil.writeICSFormatFromProviderToOS(
                    account,
                    context,
                    collectionId,
                    iCalObjectId,
                    outputStream
                )
            }

            outputFile
        } catch (e: Exception) {
            Log.i("fileprovider", "Failed to attach ICS File")
            toastMessage.value = "Failed to attach ICS File."

            null
        }
    }
    
    enum class DetailChangeState { UNCHANGED, CHANGEUNSAVED, CHANGESAVING, CHANGESAVED }
}
