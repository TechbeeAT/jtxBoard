/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.notesx5.ui

import android.app.Application
import androidx.lifecycle.*
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.*
import at.bitfire.notesx5.database.relations.ICalEntity
import at.bitfire.notesx5.database.views.ICal4ViewNote
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*


class IcalViewViewModel(private val icalItemId: Long,
                        val database: ICalDatabaseDao,
                        application: Application) : AndroidViewModel(application) {

    lateinit var icalEntity: LiveData<ICalEntity?>
    lateinit var categories: LiveData<List<Category>>
    lateinit var attendees: LiveData<List<Attendee>>
    lateinit var relatedNotes: LiveData<List<ICal4ViewNote?>>
    lateinit var relatedSubtasks: LiveData<List<ICalObject?>>

    lateinit var dtstartFormatted: LiveData<String>
    lateinit var createdFormatted: LiveData<String>
    lateinit var lastModifiedFormatted: LiveData<String>
    lateinit var completedFormatted: LiveData<String>
    lateinit var startedFormatted: LiveData<String>

    lateinit var dateVisible: LiveData<Boolean>
    lateinit var timeVisible: LiveData<Boolean>
    lateinit var urlVisible: LiveData<Boolean>
    lateinit var attendeesVisible: LiveData<Boolean>
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


    lateinit var subtasksCountList: LiveData<List<SubtaskCount>>


    var editingClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }


    init {

        viewModelScope.launch {

            // insert a new value to initialize the vJournalItem or load the existing one from the DB
            icalEntity = if (icalItemId == 0L)
                MutableLiveData<ICalEntity?>().apply {
                    postValue(ICalEntity(ICalObject(), null, null, null, null, null))
                }
            else
                database.get(icalItemId)

            subtasksCountList = database.getSubtasksCount()


            categories = Transformations.map(icalEntity) {
                it?.category
            }

            attendees = Transformations.map(icalEntity) {
                it?.attendee
            }


            relatedNotes = Transformations.switchMap(icalEntity) {
                it?.property?.id?.let { parentId -> database.getRelatedNotes(parentId) }
            }

            relatedSubtasks = Transformations.switchMap(icalEntity) {
                it?.property?.id?.let { parentId -> database.getRelatedTodos(parentId) }
            }


            dateVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == Component.VJOURNAL.name && item.property.dtstart != null           // true if component == JOURNAL
            }

            timeVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == Component.VJOURNAL.name && item.property.dtstart != null && item.property.dtstartTimezone != "ALLDAY"           // true if component == JOURNAL and it is not an All Day Event
            }

            dtstartFormatted = Transformations.map(icalEntity) { item ->
                if (item!!.property.dtstart != null) {
                    val formattedDate = DateFormat.getDateInstance(DateFormat.LONG).format(Date(item.property.dtstart!!))
                    val formattedTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(item.property.dtstart!!))
                    return@map "$formattedDate $formattedTime"
                } else
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



            urlVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.property?.url.isNullOrBlank()      // true if url is NOT null or empty
            }
            attendeesVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.attendee.isNullOrEmpty()      // true if attendees is NOT null or empty
            }
            organizerVisible = Transformations.map(icalEntity) { item ->
                return@map item?.organizer != null      // true if organizer is NOT null or empty
            }
            contactVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.property?.contact.isNullOrBlank()      // true if contact is NOT null or empty
            }
           relatedtoVisible = Transformations.map(icalEntity) { item ->
                //return@map !item?.relatedto.isNullOrEmpty()      // true if relatedto is NOT null or empty
               return@map false    // currently not in use, therefore always false
            }
            subtasksVisible = Transformations.map(relatedSubtasks) { subtasks ->
                return@map subtasks?.isNotEmpty()      // true if relatedto is NOT null or empty
            }
            commentsVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.comment.isNullOrEmpty()      // true if comment is NOT null or empty
            }
            attachmentsVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.attachment.isNullOrEmpty()      // true if attachment is NOT null or empty
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
            val newNoteId = database.insertICalObject(newNote)
            database.insertRelatedto(Relatedto(icalObjectId = icalEntity.value!!.property.id, linkedICalObjectId = newNoteId, reltype = Reltype.CHILD.name, text = newNote.uid))
        }
    }

    fun insertRelatedAudioNote(attachment: Attachment) {

        viewModelScope.launch {
            val newNote = ICalObject.createNote("Audio Comment")
            val newNoteId = database.insertICalObject(newNote)
            database.insertRelatedto(Relatedto(icalObjectId = icalEntity.value!!.property.id, linkedICalObjectId = newNoteId, reltype = Reltype.CHILD.name, text = newNote.uid))
            attachment.icalObjectId = newNoteId
            database.insertAttachment(attachment)
        }

    }



    fun updateProgress(item: ICalObject, newPercent: Int) {

        viewModelScope.launch {
            database.update(item.setUpdatedProgress(newPercent))
        }
    }
}
