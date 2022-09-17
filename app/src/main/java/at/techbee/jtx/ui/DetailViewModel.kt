/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import at.techbee.jtx.AUTHORITY_FILEPROVIDER
import at.techbee.jtx.DetailSettings
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.util.Ical4androidUtil
import at.techbee.jtx.util.SyncUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException


class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _application = application
    private var database: ICalDatabaseDao = ICalDatabase.getInstance(application).iCalDatabaseDao

    lateinit var icalEntity: LiveData<ICalEntity?>
    lateinit var relatedSubnotes: LiveData<List<ICal4List>>
    lateinit var relatedSubtasks: LiveData<List<ICal4List>>
    lateinit var recurInstances: LiveData<List<ICalObject?>>
    lateinit var isChild: LiveData<Boolean>

    lateinit var allCategories: LiveData<List<String>>
    lateinit var allResources: LiveData<List<String>>
    lateinit var allCollections: LiveData<List<ICalCollection>>

    var entryDeleted = mutableStateOf(false)
    var navigateToId = mutableStateOf<Long?>(null)
    lateinit var detailSettings: DetailSettings

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
            allCollections = database.getAllCollections()

            relatedSubnotes = MutableLiveData(emptyList())
            relatedSubtasks = MutableLiveData(emptyList())
            isChild = MutableLiveData(false)

            recurInstances = Transformations.switchMap(icalEntity) {
                it?.property?.id?.let { originalId -> database.getRecurInstances(originalId) }
            }
        }
    }

    fun load(icalObjectId: Long) {
        viewModelScope.launch {
            icalEntity = database.get(icalObjectId)

            relatedSubnotes = Transformations.switchMap(icalEntity) {
                it?.property?.uid?.let { parentUid -> database.getAllSubnotesOf(parentUid) }
            }

            relatedSubtasks = Transformations.switchMap(icalEntity) {
                it?.property?.uid?.let { parentUid ->
                    database.getAllSubtasksOf(parentUid)
                }
            }
            isChild = database.isChild(icalObjectId)


            val prefs: SharedPreferences = when (icalEntity.value?.property?.getModuleFromString()) {
                Module.JOURNAL -> _application.getSharedPreferences(PREFS_DETAIL_JOURNALS, Context.MODE_PRIVATE)
                Module.NOTE -> _application.getSharedPreferences(PREFS_DETAIL_NOTES, Context.MODE_PRIVATE)
                Module.TODO -> _application.getSharedPreferences(PREFS_DETAIL_TODOS, Context.MODE_PRIVATE)
                else -> _application.getSharedPreferences(PREFS_DETAIL_JOURNALS, Context.MODE_PRIVATE)
            }
            detailSettings = DetailSettings(prefs)
        }
    }



    fun updateProgress(id: Long, newPercent: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = database.getICalObjectById(id) ?: return@launch
            if(icalEntity.value?.property?.isRecurLinkedInstance == true) {
                ICalObject.makeRecurringException(icalEntity.value?.property!!, database)
                Toast.makeText(getApplication(), R.string.toast_item_is_now_recu_exception, Toast.LENGTH_SHORT).show()
            }
            item.setUpdatedProgress(newPercent)
            database.update(item)
            SyncUtil.notifyContentObservers(getApplication())
        }
    }

    fun updateSummary(icalObjectId: Long, newSummary: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val icalObject = database.getICalObjectById(icalObjectId) ?: return@launch
            icalObject.summary = newSummary
            icalObject.makeDirty()
            database.update(icalObject)
            SyncUtil.notifyContentObservers(getApplication())
        }
    }

    fun moveToNewCollection(icalObject: ICalObject, newCollectionId: Long) {

        viewModelScope.launch(Dispatchers.IO) {
            val newId = ICalObject.updateCollectionWithChildren(icalObject.id, null, newCollectionId, getApplication())
            // once the newId is there, the local entries can be deleted (or marked as deleted)
            ICalObject.deleteItemWithChildren(icalObject.id, database)        // make sure to delete the old item (or marked as deleted - this is already handled in the function)
            if (icalObject.rrule != null)
                icalObject.recreateRecurring(database, getApplication())
            navigateToId.value = newId
        }
    }


    fun save(iCalObject: ICalObject,
             categories: List<Category>,
             comments: List<Comment>,
             attendees: List<Attendee>,
             resources: List<Resource>,
             attachments: List<Attachment>,
             alarms: List<Alarm>
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            if(icalEntity.value?.categories != categories) {
                iCalObject.makeDirty()
                database.deleteCategories(iCalObject.id)
                categories.forEach { changedCategory ->
                    changedCategory.icalObjectId = iCalObject.id
                    database.insertCategory(changedCategory)
                }
            }

            if(icalEntity.value?.comments != comments) {
                iCalObject.makeDirty()
                database.deleteComments(iCalObject.id)
                comments.forEach { changedComment ->
                    changedComment.icalObjectId = iCalObject.id
                    database.insertComment(changedComment)
                }
            }

            if(icalEntity.value?.attendees != attendees) {
                iCalObject.makeDirty()
                database.deleteAttendees(iCalObject.id)
                attendees.forEach { changedAttendee ->
                    changedAttendee.icalObjectId = iCalObject.id
                    database.insertAttendee(changedAttendee)
                }
            }

            if(icalEntity.value?.resources != resources) {
                iCalObject.makeDirty()
                database.deleteResources(iCalObject.id)
                resources.forEach { changedResource ->
                    changedResource.icalObjectId = iCalObject.id
                    database.insertResource(changedResource)
                }
            }

            if(icalEntity.value?.attachments != attachments) {
                iCalObject.makeDirty()
                database.deleteAttachments(iCalObject.id)
                attachments.forEach { changedAttachment ->
                    changedAttachment.icalObjectId = iCalObject.id
                    database.insertAttachment(changedAttachment)
                }
                Attachment.scheduleCleanupJob(getApplication())
            }

            if(icalEntity.value?.alarms != alarms) {
                iCalObject.makeDirty()
                database.deleteAlarms(iCalObject.id)
                alarms.forEach { changedAlarm ->
                    changedAlarm.icalObjectId = iCalObject.id
                    changedAlarm.alarmId = database.insertAlarm(changedAlarm)
                    val triggerTime = when {
                        changedAlarm.triggerTime != null -> changedAlarm.triggerTime
                        changedAlarm.triggerRelativeDuration != null && changedAlarm.triggerRelativeTo == AlarmRelativeTo.END.name -> changedAlarm.getDatetimeFromTriggerDuration(
                            iCalObject.due, iCalObject.dueTimezone)
                        changedAlarm.triggerRelativeDuration != null -> changedAlarm.getDatetimeFromTriggerDuration(iCalObject.dtstart, iCalObject.dtstartTimezone)
                        else -> null
                    }
                    triggerTime?.let {
                        changedAlarm.scheduleNotification(_application, triggerTime = it, isReadOnly = icalEntity.value?.ICalCollection?.readonly?: true, icalEntity.value?.property?.summary, icalEntity.value?.property?.description)
                    }
                }
            }

            if(icalEntity.value?.property != iCalObject) {
                iCalObject.makeDirty()
                database.update(iCalObject)

                if(icalEntity.value?.property?.isRecurLinkedInstance == true) {
                    ICalObject.makeRecurringException(icalEntity.value?.property!!, database)
                    Toast.makeText(getApplication(), R.string.toast_item_is_now_recu_exception, Toast.LENGTH_SHORT).show()
                }
            }
            SyncUtil.notifyContentObservers(getApplication())
        }
    }


    fun addSubEntry(subEntry: ICalObject, attachment: Attachment?) {
        viewModelScope.launch(Dispatchers.IO) {
            subEntry.collectionId = icalEntity.value?.property?.collectionId!!
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
                Toast.makeText(getApplication(), R.string.toast_item_is_now_recu_exception, Toast.LENGTH_SHORT).show()
            }
            SyncUtil.notifyContentObservers(getApplication())
        }
    }


    fun delete() {
        viewModelScope.launch(Dispatchers.IO) {
            if(icalEntity.value?.property?.isRecurLinkedInstance == true) {
                ICalObject.makeRecurringException(icalEntity.value?.property!!, database)
                //Toast.makeText(getApplication(), R.string.toast_item_is_now_recu_exception, Toast.LENGTH_SHORT).show()
            }
            icalEntity.value?.property?.id?.let { id ->
                ICalObject.deleteItemWithChildren(id, database)
                SyncUtil.notifyContentObservers(getApplication())
                entryDeleted.value = true
            }
        }
    }

    /**
     * Delete function for subtasks and subnotes
     * @param [icalObjectId] of the subtask/subnote to be deleted
     */
    fun deleteById(icalObjectId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            /* TODO
            if(icalEntity.value?.property?.isRecurLinkedInstance == true) {
                ICalObject.makeRecurringException(icalEntity.value?.property!!, database)
                Toast.makeText(getApplication(), R.string.toast_item_is_now_recu_exception, Toast.LENGTH_SHORT).show()
            }
             */
            icalEntity.value?.property?.id?.let { id ->
                ICalObject.deleteItemWithChildren(icalObjectId, database)
                SyncUtil.notifyContentObservers(getApplication())
            }
        }
    }

    fun createCopy(newModule: Module) {
        icalEntity.value?.let { createCopy(it, newModule) }
    }

    private fun createCopy(icalEntityToCopy: ICalEntity, newModule: Module, newParentUID: String? = null) {
        val newEntity = icalEntityToCopy.getIcalEntityCopy(newModule)

        viewModelScope.launch(Dispatchers.IO) {
            val newId = database.insertICalObject(newEntity.property)
            newEntity.alarms?.forEach { alarm ->
                database.insertAlarm(alarm.copy(alarmId = 0L, icalObjectId = newId ))   // TODO: Schedule Alarm!
            }
            newEntity.attachments?.forEach { attachment ->
                database.insertAttachment(attachment.copy(icalObjectId = newId, attachmentId = 0L))
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
                database.insertResource(resource.copy(icalObjectId = newId,  resourceId = 0L))
            }
            newEntity.unknown?.forEach { unknown ->
                database.insertUnknownSync(unknown.copy(icalObjectId = newId, unknownId = 0L))
            }
            newEntity.organizer?.let { organizer ->
                database.insertOrganizer(organizer.copy(icalObjectId = newId, organizerId = 0L))
            }

            newEntity.relatedto?.forEach { relatedto ->
                if (relatedto.reltype == Reltype.PARENT.name && newParentUID != null) {
                    database.insertRelatedto(relatedto.copy(relatedtoId = 0L, icalObjectId = newId, text = newParentUID))
                }
            }

            val children = database.getRelatedChildren(icalEntityToCopy.property.id)
            children.forEach { child ->
                database.getSync(child)?.let { createCopy(icalEntityToCopy = it, newModule = it.property.getModuleFromString(), newParentUID = newEntity.property.uid) }
            }

            if(newParentUID == null)   // we navigate only to the parent (not to the children that are invoked recursively)
                navigateToId.value = newId
        }
    }

    fun shareAsICS(context: Context) {

        viewModelScope.launch(Dispatchers.IO)  {
            val account = icalEntity.value?.ICalCollection?.getAccount() ?: return@launch
            val collectionId = icalEntity.value?.property?.collectionId ?: return@launch
            val iCalObjectId = icalEntity.value?.property?.id ?: return@launch
            val ics = Ical4androidUtil.getICSFormatFromProvider(account, getApplication(), collectionId, iCalObjectId) ?: return@launch

            val icsShareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/calendar"
            }

            try {
                val icsFileName = "${context.externalCacheDir}/ics_file.ics"
                val icsFile = File(icsFileName).apply {
                    this.writeBytes(ics.toByteArray())
                    createNewFile()
                }
                val uri =
                    FileProvider.getUriForFile(context, AUTHORITY_FILEPROVIDER, icsFile)
                icsShareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                context.startActivity(Intent(icsShareIntent))
            } catch (e: ActivityNotFoundException) {
                Log.i("ActivityNotFound", "No activity found to open file.")
                Toast.makeText(context, "No app found to open this file.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.i("fileprovider", "Failed to attach ICS File")
                Toast.makeText(context, "Failed to attach ICS File.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun shareAsText(context: Context) {

        viewModelScope.launch(Dispatchers.IO)  {
            val account = icalEntity.value?.ICalCollection?.getAccount() ?: return@launch
            val collectionId = icalEntity.value?.property?.collectionId ?: return@launch
            val iCalObjectId = icalEntity.value?.property?.id ?: return@launch

            val shareText = icalEntity.value?.getShareText(context) ?: ""

            val attendees: MutableList<String> = mutableListOf()
            icalEntity.value?.attendees?.forEach { attendees.add(it.caladdress.removePrefix("mailto:")) }

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND_MULTIPLE
                type = "text/plain"
                icalEntity.value?.property?.summary?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_EMAIL, attendees.toTypedArray())
            }
            val files = ArrayList<Uri>()

            icalEntity.value?.attachments?.forEach {
                try {
                    files.add(Uri.parse(it.uri))
                } catch (e: NullPointerException) {
                    Log.i("Attachment", "Attachment Uri could not be parsed")
                } catch (e: FileNotFoundException) {
                    Log.i("Attachment", "Attachment-File could not be accessed.")
                }
            }

            try {
                val os = ByteArrayOutputStream()
                Ical4androidUtil.writeICSFormatFromProviderToOS(account, getApplication(), collectionId, iCalObjectId, os)

                val icsFileName = "${context.externalCacheDir}/ics_file.ics"
                val icsFile = File(icsFileName).apply {
                    this.writeBytes(os.toByteArray())
                    createNewFile()
                }
                val uri = FileProvider.getUriForFile(context, AUTHORITY_FILEPROVIDER, icsFile)
                files.add(uri)
            } catch (e: Exception) {
                Log.i("fileprovider", "Failed to attach ICS File")
                Toast.makeText(context, "Failed to attach ICS File.", Toast.LENGTH_SHORT).show()
            }

            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
            //Log.d("shareIntent", shareText)

            try {
                context.startActivity(Intent(shareIntent))
            } catch (e: ActivityNotFoundException) {
                Log.i("ActivityNotFound", "No activity found to send this entry.")
                Toast.makeText(context, R.string.error_no_app_found_to_open_entry, Toast.LENGTH_SHORT).show()
            }
        }





    }
}
