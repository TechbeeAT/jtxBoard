/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.app.Application
import androidx.lifecycle.*
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.ICal4ViewNote
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*


class IcalViewViewModel(private val icalItemId: Long,
                        val database: ICalDatabaseDao,
                        application: Application) : AndroidViewModel(application) {

    lateinit var icalEntity: LiveData<ICalEntity?>
    lateinit var categories: LiveData<List<Category>>
    lateinit var resources: LiveData<List<Resource>>
    lateinit var attendees: LiveData<List<Attendee>>
    lateinit var relatedNotes: LiveData<List<ICal4ViewNote?>>
    lateinit var relatedSubtasks: LiveData<List<ICalObject?>>
    lateinit var recurInstances: LiveData<List<ICalObject?>>

    lateinit var dtstartFormatted: LiveData<String>
    lateinit var dtstartTimezone: LiveData<String>
    lateinit var createdFormatted: LiveData<String>
    lateinit var lastModifiedFormatted: LiveData<String>
    lateinit var completedFormatted: LiveData<String>
    lateinit var startedFormatted: LiveData<String>


    lateinit var progressIndicatorVisible: LiveData<Boolean>

    lateinit var dateVisible: LiveData<Boolean>
    lateinit var timeVisible: LiveData<Boolean>
    lateinit var timezoneVisible: LiveData<Boolean>
    lateinit var urlVisible: LiveData<Boolean>
    lateinit var locationVisible: LiveData<Boolean>
    lateinit var attendeesVisible: LiveData<Boolean>
    lateinit var resourcesVisible: LiveData<Boolean>
    lateinit var organizerVisible: LiveData<Boolean>
    lateinit var contactVisible: LiveData<Boolean>
    lateinit var commentsVisible: LiveData<Boolean>
    lateinit var attachmentsVisible: LiveData<Boolean>
    lateinit var relatedtoVisible: LiveData<Boolean>
    lateinit var progressVisible: LiveData<Boolean>
    lateinit var priorityVisible: LiveData<Boolean>
    lateinit var subtasksVisible: LiveData<Boolean>
    lateinit var completedVisible: LiveData<Boolean>
    lateinit var startedVisible: LiveData<Boolean>

    lateinit var recurrenceVisible: LiveData<Boolean>
    lateinit var recurrenceItemsVisible: LiveData<Boolean>
    lateinit var recurrenceGoToOriginalVisible: LiveData<Boolean>


    lateinit var collectionText: LiveData<String?>



    lateinit var subtasksCountList: LiveData<List<SubtaskCount>>


    var editingClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }


    init {

        viewModelScope.launch {

            // insert a new value to initialize the item or load the existing one from the DB
            icalEntity = if (icalItemId == 0L)
                MutableLiveData<ICalEntity?>().apply {
                    postValue(ICalEntity(ICalObject(), null, null, null, null, null))
                }
            else
                database.get(icalItemId)

            subtasksCountList = database.getSubtasksCount()

            categories = Transformations.map(icalEntity) {
                it?.categories
            }

            resources = Transformations.map(icalEntity) {
                it?.resources
            }

            attendees = Transformations.map(icalEntity) {
                it?.attendees
            }


            relatedNotes = Transformations.switchMap(icalEntity) {
                it?.property?.id?.let { parentId -> database.getRelatedNotes(parentId) }
            }

            relatedSubtasks = Transformations.switchMap(icalEntity) {
                it?.property?.id?.let { parentId -> database.getRelatedTodos(parentId) }
            }

            recurInstances = Transformations.switchMap(icalEntity) {
                it?.property?.id?.let { originalId -> database.getRecurInstances(originalId) }
            }

            progressIndicatorVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property == null     // show progress indicator as long as item.property is null
            }
            dateVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == Component.VJOURNAL.name && item.property.dtstart != null           // true if component == JOURNAL
            }

            timeVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == Component.VJOURNAL.name && item.property.dtstart != null && item.property.dtstartTimezone != "ALLDAY"           // true if component == JOURNAL and it is not an All Day Event
            }

            timezoneVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == Component.VJOURNAL.name && item.property.dtstart != null && !(item.property.dtstartTimezone == "ALLDAY" || item.property.dtstartTimezone.isNullOrEmpty())           // true if component == JOURNAL and it is not an All Day Event
            }

            dtstartFormatted = Transformations.map(icalEntity) { item ->
                if (item!!.property.dtstart != null) {
                    val formattedDate = DateFormat.getDateInstance(DateFormat.LONG).format(Date(item.property.dtstart!!))
                    val formattedTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(item.property.dtstart!!))
                    return@map "$formattedDate $formattedTime"
                } else
                    return@map ""
            }

            dtstartTimezone = Transformations.map(icalEntity) { item ->
                item?.property?.dtstartTimezone?.let {
                    val tz = TimeZone.getTimeZone(it)
                    if (tz != null)
                        return@map tz.getDisplayName(true, TimeZone.SHORT)
                }
                return@map ""
            }

            createdFormatted = Transformations.map(icalEntity) { item ->
                item!!.property.let { Date(it.created).toString() }
            }

            lastModifiedFormatted = Transformations.map(icalEntity) { item ->
                item!!.property.let { Date(it.lastModified).toString() }
            }

            completedFormatted = Transformations.map(icalEntity) { item ->
                if (item?.property?.completed != null) {
                    val formattedDate = DateFormat.getDateInstance(DateFormat.LONG).format(Date(item.property.completed!!))
                    val formattedTime = if (item.property.completedTimezone != "ALLDAY")
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(item.property.completed!!))
                    else ""
                    return@map "Completed: $formattedDate $formattedTime"
                } else
                    return@map ""
            }

            startedFormatted = Transformations.map(icalEntity) { item ->
                if (item?.property?.dtstart != null) {
                    val formattedDate = DateFormat.getDateInstance(DateFormat.LONG).format(Date(item.property.dtstart!!))
                    val formattedTime = if (item.property.dtstartTimezone != "ALLDAY")
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(item.property.dtstart!!))
                    else ""
                    return@map "Started: $formattedDate $formattedTime"
                } else
                    return@map ""
            }

            collectionText = Transformations.map(icalEntity) { item ->
                if (item?.ICalCollection?.accountName?.isNotEmpty() == true)
                    item.ICalCollection?.displayName + " (" + item.ICalCollection?.accountName + ")"
                else
                    item?.ICalCollection?.displayName ?: "-"
            }



            urlVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.property?.url.isNullOrBlank()      // true if url is NOT null or empty
            }
            locationVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.property?.location.isNullOrBlank()      // true if url is NOT null or empty
            }
            attendeesVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.attendees.isNullOrEmpty()      // true if attendees is NOT null or empty
            }
            resourcesVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.resources.isNullOrEmpty()      // true if attendees is NOT null or empty
            }
            organizerVisible = Transformations.map(icalEntity) { item ->
                return@map item?.organizer != null      // true if organizer is NOT null or empty
            }
            contactVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.property?.contact.isNullOrBlank()      // true if contact is NOT null or empty
            }
           relatedtoVisible = Transformations.map(icalEntity) {
                //return@map !item?.relatedto.isNullOrEmpty()      // true if relatedto is NOT null or empty
               return@map false    // currently not in use, therefore always false
            }
            subtasksVisible = Transformations.map(relatedSubtasks) { subtasks ->
                return@map subtasks?.isNotEmpty()      // true if relatedto is NOT null or empty
            }
            commentsVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.comments.isNullOrEmpty()      // true if comment is NOT null or empty
            }
            attachmentsVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.attachments.isNullOrEmpty()      // true if attachment is NOT null or empty
            }
            progressVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.percent != null && item.property.component == Component.VTODO.name     // true if percent (progress) is NOT null
            }
            priorityVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.priority != null      // true if priority is NOT null
            }
            completedVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.completed != null
            }
            startedVisible = Transformations.map(icalEntity) { item ->
                return@map (item?.property?.dtstart != null && item.property.component == Component.VTODO.name)
            }

            recurrenceVisible = Transformations.map(icalEntity) { item ->
                return@map (item?.property?.rrule != null || item?.property?.recurOriginalIcalObjectId != null)
            }
            recurrenceItemsVisible = Transformations.map(icalEntity) { item ->
                return@map (item?.property?.rrule != null)
            }
            recurrenceGoToOriginalVisible = Transformations.map(icalEntity) { item ->
                return@map (item?.property?.recurOriginalIcalObjectId != null)
            }
        }

        viewModelScope.launch {
            subtasksCountList = database.getSubtasksCount()
        }

    }

    fun editingClicked() {
        editingClicked.value = true
    }


    fun insertRelatedNote(noteText: String) {
        viewModelScope.launch {
            val newNote = ICalObject.createNote(noteText)
            newNote.collectionId = icalEntity.value?.ICalCollection?.collectionId ?: 1L
            val newNoteId = database.insertICalObject(newNote)

            database.insertRelatedto(Relatedto(icalObjectId = icalEntity.value!!.property.id, linkedICalObjectId = newNoteId, reltype = Reltype.CHILD.name, text = newNote.uid))

            database.updateSetDirty(icalItemId, System.currentTimeMillis())
        }
    }

    fun insertRelatedAudioNote(attachment: Attachment) {

        viewModelScope.launch {
            val newNote = ICalObject.createNote("Audio Comment")
            newNote.collectionId = icalEntity.value?.ICalCollection?.collectionId ?: 1L
            val newNoteId = database.insertICalObject(newNote)

            database.insertRelatedto(Relatedto(icalObjectId = icalEntity.value!!.property.id, linkedICalObjectId = newNoteId, reltype = Reltype.CHILD.name, text = newNote.uid))
            attachment.icalObjectId = newNoteId
            database.insertAttachment(attachment)

            database.updateSetDirty(icalItemId, System.currentTimeMillis())
        }

    }



    fun updateProgress(item: ICalObject, newPercent: Int) {

        viewModelScope.launch {
            database.update(item.setUpdatedProgress(newPercent))
        }
    }
}
