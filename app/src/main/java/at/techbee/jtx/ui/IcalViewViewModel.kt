/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.*
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.ICal4ViewNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*


class IcalViewViewModel(private val icalItemId: Long,
                        val database: ICalDatabaseDao,
                        application: Application) : AndroidViewModel(application) {

    lateinit var icalEntity: LiveData<ICalEntity?>
    lateinit var relatedNotes: LiveData<List<ICal4ViewNote?>>
    lateinit var relatedSubtasks: LiveData<List<ICalObject?>>
    lateinit var recurInstances: LiveData<List<ICalObject?>>

    lateinit var dtstartFormatted: LiveData<String>
    lateinit var dtstartTimezone: LiveData<String>
    lateinit var createdFormatted: LiveData<String>
    lateinit var lastModifiedFormatted: LiveData<String>
    lateinit var progressFormatted: LiveData<String>
    lateinit var organizerFormatted: LiveData<String>



    lateinit var progressIndicatorVisible: LiveData<Boolean>
    val showSyncProgressIndicator = MutableLiveData(false)

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
    lateinit var alarmsVisible: LiveData<Boolean>
    lateinit var attachmentsVisible: LiveData<Boolean>
    lateinit var relatedtoVisible: LiveData<Boolean>
    lateinit var progressVisible: LiveData<Boolean>
    lateinit var priorityVisible: LiveData<Boolean>
    lateinit var subtasksVisible: LiveData<Boolean>
    lateinit var completedVisible: LiveData<Boolean>
    lateinit var startedVisible: LiveData<Boolean>
    lateinit var dueVisible: LiveData<Boolean>
    lateinit var completedTimeVisible: LiveData<Boolean>
    lateinit var startedTimeVisible: LiveData<Boolean>
    lateinit var dueTimeVisible: LiveData<Boolean>
    lateinit var completedTimezoneVisible: LiveData<Boolean>
    lateinit var startedTimezoneVisible: LiveData<Boolean>
    lateinit var dueTimezoneVisible: LiveData<Boolean>
    lateinit var uploadPendingVisible: LiveData<Boolean>

    lateinit var recurrenceVisible: LiveData<Boolean>
    lateinit var recurrenceItemsVisible: LiveData<Boolean>
    lateinit var recurrenceLinkedVisible: LiveData<Boolean>
    lateinit var recurrenceGoToOriginalVisible: LiveData<Boolean>
    lateinit var recurrenceIsExceptionVisible: LiveData<Boolean>
    lateinit var recurrenceExceptionsVisible: LiveData<Boolean>
    lateinit var recurrenceAdditionsVisible: LiveData<Boolean>


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
                return@map item?.property?.component == Component.VJOURNAL.name && item.property.dtstart != null && item.property.dtstartTimezone != ICalObject.TZ_ALLDAY          // true if component == JOURNAL and it is not an All Day Event
            }

            timezoneVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == Component.VJOURNAL.name && item.property.dtstart != null && !(item.property.dtstartTimezone == ICalObject.TZ_ALLDAY || item.property.dtstartTimezone.isNullOrEmpty())           // true if component == JOURNAL and it is not an All Day Event
            }

            uploadPendingVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.dirty == true && item.ICalCollection?.accountType != LOCAL_ACCOUNT_TYPE
            }




            dtstartFormatted = Transformations.map(icalEntity) { item ->
                item?.property?.dtstart?.let {
                        val formattedDate = DateFormat.getDateInstance(DateFormat.LONG).format(Date(it))
                        val formattedTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(it))
                        return@map "$formattedDate $formattedTime"
                }
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
                item?.property?.let {
                    application.resources.getString(R.string.view_created_text, Date(it.created))
                }
            }

            lastModifiedFormatted = Transformations.map(icalEntity) { item ->
                item?.property?.let {
                    application.resources.getString(R.string.view_last_modified_text, Date(it.lastModified))
                }
            }

            progressFormatted = Transformations.map(icalEntity) { item ->
                item?.property?.percent.let { progress ->
                    String.format("%.0f%%", progress?.toFloat() ?: 0F)
                }
            }

            organizerFormatted = Transformations.map(icalEntity) { item ->
                item?.organizer?.caladdress?.removePrefix("mailto:")
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
            alarmsVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.alarms.isNullOrEmpty()      // true if alarms is NOT null or empty
            }
            attachmentsVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.attachments.isNullOrEmpty()      // true if attachment is NOT null or empty
            }
            progressVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == Component.VTODO.name     // true if percent (progress) is NOT null
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
            dueVisible = Transformations.map(icalEntity) { item ->
                return@map (item?.property?.due != null && item.property.component == Component.VTODO.name)
            }

            startedTimeVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == Component.VTODO.name && item.property.dtstart != null && item.property.dtstartTimezone != ICalObject.TZ_ALLDAY
            }
            startedTimezoneVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == Component.VTODO.name && item.property.dtstart != null && !(item.property.dtstartTimezone == ICalObject.TZ_ALLDAY || item.property.dtstartTimezone.isNullOrEmpty())
            }

            dueTimeVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == Component.VTODO.name && item.property.due != null && item.property.dueTimezone != ICalObject.TZ_ALLDAY          // true if component == JOURNAL and it is not an All Day Event
            }
            dueTimezoneVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == Component.VTODO.name && item.property.due != null && !(item.property.dueTimezone == ICalObject.TZ_ALLDAY || item.property.dueTimezone.isNullOrEmpty())           // true if component == JOURNAL and it is not an All Day Event
            }

            completedTimeVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == Component.VTODO.name && item.property.completed != null && item.property.completedTimezone != ICalObject.TZ_ALLDAY          // true if component == JOURNAL and it is not an All Day Event
            }
            completedTimezoneVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == Component.VTODO.name && item.property.completed != null && !(item.property.completedTimezone == ICalObject.TZ_ALLDAY || item.property.completedTimezone.isNullOrEmpty())           // true if component == JOURNAL and it is not an All Day Event
            }


            recurrenceVisible = Transformations.map(icalEntity) { item ->
                return@map (item?.property?.rrule != null || item?.property?.recurOriginalIcalObjectId != null)
            }
            recurrenceItemsVisible = Transformations.map(icalEntity) { item ->
                return@map (item?.property?.rrule != null)
            }
            recurrenceLinkedVisible = Transformations.map(icalEntity) { item ->
                return@map (item?.property?.recurOriginalIcalObjectId != null && item.property.isRecurLinkedInstance)
            }
            recurrenceGoToOriginalVisible = Transformations.map(icalEntity) { item ->
                return@map (item?.property?.recurOriginalIcalObjectId != null)
            }
            recurrenceIsExceptionVisible = Transformations.map(icalEntity) { item ->
                return@map (item?.property?.isRecurLinkedInstance == false && item.property.recurOriginalIcalObjectId != null)
            }
            recurrenceExceptionsVisible = Transformations.map(icalEntity) { item ->
                return@map (recurrenceVisible.value == true && item?.property?.exdate?.isNotEmpty() == true)
            }
            recurrenceAdditionsVisible = Transformations.map(icalEntity) { item ->
                return@map (recurrenceVisible.value == true && item?.property?.rdate?.isNotEmpty() == true)
            }
        }

        viewModelScope.launch {
            subtasksCountList = database.getSubtasksCount()
        }
    }

    fun editingClicked() {
        editingClicked.value = true
    }


    fun insertRelated(noteText: String?, attachment: Attachment?) {

        this.icalEntity.value?.property?.let {
            makeRecurringExceptionIfNecessary(it)
        }

        viewModelScope.launch {
            val newNote = ICalObject.createNote()
            if(noteText != null)
                newNote.summary = noteText
            newNote.collectionId = icalEntity.value?.ICalCollection?.collectionId ?: 1L
            val newNoteId = database.insertICalObject(newNote)

            // We insert both directions in the database
            database.insertRelatedto(Relatedto(icalObjectId = icalEntity.value!!.property.id, linkedICalObjectId = newNoteId, reltype = Reltype.CHILD.name, text = newNote.uid))
            database.insertRelatedto(Relatedto(linkedICalObjectId = icalEntity.value!!.property.id, icalObjectId = newNoteId, reltype = Reltype.PARENT.name, text = icalEntity.value!!.property.uid))

            if(attachment != null) {
                attachment.icalObjectId = newNoteId
                database.insertAttachment(attachment)
            }

            database.updateSetDirty(icalItemId, System.currentTimeMillis())
        }
    }


    fun updateProgress(item: ICalObject, newPercent: Int) {

        makeRecurringExceptionIfNecessary(item)
        viewModelScope.launch(Dispatchers.IO) {
            database.update(item.setUpdatedProgress(newPercent))
        }
    }

    private fun makeRecurringExceptionIfNecessary(item: ICalObject) {

        if(item.isRecurLinkedInstance) {
            viewModelScope.launch(Dispatchers.IO) {
                ICalObject.makeRecurringException(item, database)
            }
            Toast.makeText(getApplication(), R.string.toast_item_is_now_recu_exception, Toast.LENGTH_SHORT).show()
        }
    }

    fun delete(item: ICalObject) {

        viewModelScope.launch(Dispatchers.IO) {
            ICalObject.deleteItemWithChildren(item.id, database)
        }
    }
}
