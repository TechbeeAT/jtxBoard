/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.*
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.util.DateTimeUtils.addLongToCSVString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class IcalEditViewModel(
    application: Application,
    val iCalEntity: ICalEntity
) : AndroidViewModel(application) {

    companion object {
        const val TAB_GENERAL = 0
        const val TAB_PEOPLE_RES = 1
        const val TAB_LOC_COMMENTS = 2
        const val TAB_ATTACHMENTS = 3
        const val TAB_SUBTASKS = 4
        const val TAB_ALARMS = 5
        const val TAB_RECURRING = 6
    }

    private var database: ICalDatabaseDao = ICalDatabase.getInstance(application).iCalDatabaseDao

    lateinit var allCategories: LiveData<List<String>>
    lateinit var allResources: LiveData<List<String>>
    lateinit var allCollections: LiveData<List<ICalCollection>>
    lateinit var isChild: LiveData<Boolean>
    lateinit var relatedSubtasks: LiveData<List<ICalObject>>

    var recurrenceList = mutableListOf<Long>()

    var returnIcalObjectId: MutableLiveData<Long> =
        MutableLiveData<Long>().apply { postValue(0L) }
    var savingClicked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(false)
    var deleteClicked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(false)
    var entryDeleted: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(false)
    var collectionNotFoundError: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(false)

    var iCalObjectUpdated: MutableLiveData<ICalObject> =
        MutableLiveData<ICalObject>().apply { postValue(iCalEntity.property) }
    var selectedCollectionId: Long? = null

    var categoryUpdated: MutableList<Category> = mutableListOf()
    var commentUpdated: MutableList<Comment> = mutableListOf()
    var attachmentUpdated: MutableList<Attachment> = mutableListOf()
    var attendeeUpdated: MutableList<Attendee> = mutableListOf()
    var resourceUpdated: MutableList<Resource> = mutableListOf()
    var alarmUpdated: MutableList<Alarm> = mutableListOf()
    var subtaskUpdated: MutableList<ICalObject> = mutableListOf()
    var subtaskDeleted: MutableList<ICalObject> = mutableListOf()

    var activeTab: MutableLiveData<Int> = MutableLiveData<Int>(TAB_GENERAL)
    var tabGeneralVisible = Transformations.map(activeTab) { it == TAB_GENERAL }
    var tabCARVisible = Transformations.map(activeTab) { it == TAB_PEOPLE_RES }
    var tabULCVisible = Transformations.map(activeTab) { it == TAB_LOC_COMMENTS }
    var tabAttachmentsVisible = Transformations.map(activeTab) { it == TAB_ATTACHMENTS }
    var tabSubtasksVisible = Transformations.map(activeTab) { it == TAB_SUBTASKS }
    var tabAlarmsVisible = Transformations.map(activeTab) { it == TAB_ALARMS }
    var tabRecurVisible = Transformations.map(activeTab) { it == TAB_RECURRING }


    var dateVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var timeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var addTimeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var timezoneVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var progressVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var priorityVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var duedateVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var duetimeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var completeddateVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var completedtimeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var starteddateVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var startedtimeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var addStartedAndDueTimeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var recurrenceGeneralVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var recurrenceWeekdaysVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var recurrenceDayOfMonthVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var recurrenceCountVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var recurrenceUntilVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var recurrenceExceptionsVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var recurrenceAdditionsVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()



    var addTimeChecked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(iCalEntity.property.dueTimezone != ICalObject.TZ_ALLDAY && iCalEntity.property.dtstartTimezone != ICalObject.TZ_ALLDAY && iCalEntity.property.completedTimezone != ICalObject.TZ_ALLDAY)
    var addTimezoneJournalChecked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(iCalEntity.property.component == Component.VJOURNAL.name && ((iCalEntity.property.dtstartTimezone != ICalObject.TZ_ALLDAY && iCalEntity.property.dtstartTimezone?.isNotEmpty() == true)))
    var addTimezoneTodoChecked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(iCalEntity.property.component == Component.VTODO.name && ((iCalEntity.property.dueTimezone != ICalObject.TZ_ALLDAY && iCalEntity.property.dueTimezone?.isNotEmpty() == true) || (iCalEntity.property.dtstartTimezone != ICalObject.TZ_ALLDAY && iCalEntity.property.dtstartTimezone?.isNotEmpty() == true) || (iCalEntity.property.completedTimezone != ICalObject.TZ_ALLDAY && iCalEntity.property.completedTimezone?.isNotEmpty() == true)))



    private var selectedTab = TAB_GENERAL


    init {

        updateVisibility()

        viewModelScope.launch {

            relatedSubtasks = database.getAllSubtasksAsICalObjectOf(iCalEntity.property.uid)

            allCategories = database.getAllCategoriesAsText()
            allResources = database.getAllResourcesAsText()
            allCollections = when (iCalEntity.property.component) {
                Component.VTODO.name -> database.getAllWriteableVTODOCollections()
                Component.VJOURNAL.name -> database.getAllWriteableVJOURNALCollections()
                else -> database.getAllCollections() // should not happen!
            }

            isChild = database.isChild(iCalEntity.property.id)

        }
    }

    fun updateVisibility() {

        dateVisible.postValue(iCalEntity.property.module == Module.JOURNAL.name && (selectedTab == TAB_GENERAL))
        timeVisible.postValue((iCalObjectUpdated.value?.dtstart != null && iCalObjectUpdated.value?.dtstartTimezone != ICalObject.TZ_ALLDAY) || (iCalObjectUpdated.value?.due != null && iCalObjectUpdated.value?.dueTimezone != ICalObject.TZ_ALLDAY) || (iCalObjectUpdated.value?.completed != null && iCalObjectUpdated.value?.completedTimezone != ICalObject.TZ_ALLDAY) )
        addTimeVisible.postValue(iCalEntity.property.module == Module.JOURNAL.name && (selectedTab == TAB_GENERAL))
        timezoneVisible.postValue(iCalEntity.property.module == Module.JOURNAL.name && (selectedTab == TAB_GENERAL) && iCalObjectUpdated.value?.dtstartTimezone != ICalObject.TZ_ALLDAY ) // simplified IF: Show time only if.module == JOURNAL and Timezone is NOT ALLDAY
        progressVisible.postValue(iCalEntity.property.module == Module.TODO.name && (selectedTab == TAB_GENERAL))
        priorityVisible.postValue(iCalEntity.property.module == Module.TODO.name && (selectedTab == TAB_GENERAL))
        duedateVisible.postValue(iCalEntity.property.module == Module.TODO.name)
        duetimeVisible.postValue(iCalEntity.property.module == Module.TODO.name && iCalEntity.property.dueTimezone != ICalObject.TZ_ALLDAY)
        completeddateVisible.postValue(iCalEntity.property.module == Module.TODO.name)
        completedtimeVisible.postValue(iCalEntity.property.module == Module.TODO.name && iCalEntity.property.completedTimezone != ICalObject.TZ_ALLDAY)
        starteddateVisible.postValue( iCalEntity.property.module == Module.TODO.name)
        startedtimeVisible.postValue(iCalEntity.property.module == Module.TODO.name && iCalEntity.property.dtstartTimezone != ICalObject.TZ_ALLDAY)
        addStartedAndDueTimeVisible.postValue((selectedTab == TAB_GENERAL) && iCalEntity.property.module == Module.TODO.name)
        recurrenceExceptionsVisible.postValue(iCalEntity.property.exdate?.isNotEmpty() == true)
        recurrenceAdditionsVisible.postValue(iCalEntity.property.rdate?.isNotEmpty() == true)
    }

    fun savingClicked() {
        savingClicked.value = true
    }

    fun deleteClicked() {
        deleteClicked.value = true
    }

    fun update() {
        var insertedOrUpdatedItemId: Long

        //TODO: check if the item got a new sequence in the meantime!

        iCalObjectUpdated.value!!.lastModified = System.currentTimeMillis()
        iCalObjectUpdated.value!!.dtstamp = System.currentTimeMillis()
        iCalObjectUpdated.value!!.sequence++

        if(iCalObjectUpdated.value!!.duration != null)
            iCalObjectUpdated.value!!.duration = null     // we make sure that the unsupported duration is set to null, the user was warned before

        iCalObjectUpdated.value!!.dirty = true

        if(iCalObjectUpdated.value!!.module == Module.TODO.name)
            iCalObjectUpdated.value!!.setUpdatedProgress(iCalObjectUpdated.value!!.percent)

        viewModelScope.launch(Dispatchers.IO) {
            // the case that an item gets deleted at the same time the user was already editing this item, is currently not handled.
            // On save the user would not get an error, he would return to the overview with the deleted item missing
            try {
                // we insert or update - if the collection was changed, we still update the current entry and move the item to the new collection at the end
                insertedOrUpdatedItemId = if(iCalObjectUpdated.value!!.id == 0L)
                        database.insertICalObject(iCalObjectUpdated.value!!)
                    else {
                        database.update(iCalObjectUpdated.value!!)
                        iCalObjectUpdated.value!!.id
                    }
                iCalObjectUpdated.value!!.id = insertedOrUpdatedItemId
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", e.toString())
                Log.d("SQLConstraint", iCalObjectUpdated.value.toString())
                collectionNotFoundError.postValue(true)
                savingClicked.postValue(false)
                collectionNotFoundError.postValue(false)
                return@launch
            }

            // delete the list attributes, then insert again the once that are still in the list (or were added)
            database.deleteCategories(insertedOrUpdatedItemId)
            database.deleteComments(insertedOrUpdatedItemId)
            database.deleteAttachments(insertedOrUpdatedItemId)
            database.deleteAttendees(insertedOrUpdatedItemId)
            database.deleteResources(insertedOrUpdatedItemId)
            database.deleteAlarms(insertedOrUpdatedItemId)

            subtaskDeleted.forEach { subtask2del ->
                ICalObject.deleteItemWithChildren(subtask2del.id, database)
                Log.println(Log.INFO, "Subtask", "${subtask2del.summary} deleted")
            }

            // now insert or update the item and take care of all attributes
            // insert new Categories
            categoryUpdated.forEach { newCategory ->
                newCategory.icalObjectId = insertedOrUpdatedItemId
                database.insertCategory(newCategory)
            }
            commentUpdated.forEach { newComment ->
                newComment.icalObjectId =
                    insertedOrUpdatedItemId                    //Update the foreign key for newly added comments
                database.insertComment(newComment)
            }
            attachmentUpdated.forEach { newAttachment ->
                newAttachment.icalObjectId =
                    insertedOrUpdatedItemId                    //Update the foreign key for newly added attachments
                database.insertAttachment(newAttachment)
            }
            attendeeUpdated.forEach { newAttendee ->
                newAttendee.icalObjectId = insertedOrUpdatedItemId
                database.insertAttendee(newAttendee)
            }
            resourceUpdated.forEach { newResource ->
                newResource.icalObjectId = insertedOrUpdatedItemId
                database.insertResource(newResource)
            }
            alarmUpdated.forEach { newAlarm ->
                newAlarm.icalObjectId = insertedOrUpdatedItemId
                if(newAlarm.action.isNullOrEmpty())
                    newAlarm.action = AlarmAction.DISPLAY.name

                // VALARM with action DISPLAY must have a description!
                iCalObjectUpdated.value?.summary?.let { newAlarm.summary = it  }
                iCalObjectUpdated.value?.description?.let { newAlarm.description = it }
                if(newAlarm.description.isNullOrEmpty())
                    newAlarm.description = newAlarm.summary ?: ""          // If no description was set, we try to set it to the summary, if also the summary is null, an empty string is set
                newAlarm.alarmId = database.insertAlarm(newAlarm)

                // take care of notifications
                val triggerTime = when {
                    newAlarm.triggerTime != null -> newAlarm.triggerTime
                    newAlarm.triggerRelativeDuration != null && newAlarm.triggerRelativeTo == AlarmRelativeTo.END.name -> newAlarm.getDatetimeFromTriggerDuration(
                        iCalObjectUpdated.value?.due, iCalObjectUpdated.value?.dueTimezone)
                    newAlarm.triggerRelativeDuration != null -> newAlarm.getDatetimeFromTriggerDuration(iCalObjectUpdated.value?.dtstart, iCalObjectUpdated.value?.dtstartTimezone)
                    else -> null
                }
                triggerTime?.let { newAlarm.scheduleNotification(getApplication(), it, false) }
            }

            // if a collection was selected that doesn't support VTODO, we do not update/insert any subtasks
            // deleting a subtask in the DB is not necessary before, as the insertion should never have been possible for VTODOs
            val currentCollection = allCollections.value?.find { col -> col.collectionId == iCalObjectUpdated.value?.collectionId }
            if(currentCollection?.supportsVTODO == false)
                subtaskUpdated.clear()

            subtaskUpdated.forEach { subtask ->
                subtask.setUpdatedProgress(subtask.percent?:0)
                subtask.collectionId = iCalObjectUpdated.value!!.collectionId
                subtask.id = database.insertICalObject(subtask)
                Log.println(Log.INFO, "Subtask", "${subtask.id} ${subtask.summary} added")

                // upsert relation from the Child to the Parent
                // check if the relation is there, if not we insert
                val parentRelation = database.findRelatedTo(subtask.id, iCalObjectUpdated.value!!.uid, Reltype.PARENT.name)
                if(parentRelation == null) {
                    database.insertRelatedto(
                        Relatedto(
                            icalObjectId = subtask.id,
                            reltype = Reltype.PARENT.name,
                            text = iCalObjectUpdated.value!!.uid
                        )
                    )
                }
            }


            if (iCalObjectUpdated.value?.recurOriginalIcalObjectId != null && iCalObjectUpdated.value?.isRecurLinkedInstance == false) {
                val newExceptionList = addLongToCSVString(
                    database.getRecurExceptions(iCalObjectUpdated.value?.recurOriginalIcalObjectId!!),
                    iCalObjectUpdated.value!!.dtstart
                )
                database.setRecurExceptions(
                    iCalObjectUpdated.value?.recurOriginalIcalObjectId!!,
                    newExceptionList,
                    System.currentTimeMillis()
                )
            }

            if(iCalEntity.ICalCollection?.collectionId != selectedCollectionId
                && selectedCollectionId != null
                && iCalEntity.property.id != 0L) {
                val newId = ICalObject.updateCollectionWithChildren(iCalEntity.property.id, null, selectedCollectionId!!, getApplication())

                // once the newId is there, the local entries can be deleted (or marked as deleted)
                ICalObject.deleteItemWithChildren(iCalEntity.property.id, database)        // make sure to delete the old item (or marked as deleted - this is already handled in the function)
                insertedOrUpdatedItemId = newId
            }

            iCalObjectUpdated.value!!.id = insertedOrUpdatedItemId
            if (recurrenceList.size > 0 || iCalObjectUpdated.value!!.id != 0L)    // recreateRecurring if the recurrenceList is not empty, but also when it is an update, as the recurrence might have been deactivated and it is necessary to delete instances
                iCalObjectUpdated.value?.recreateRecurring(database, getApplication())


            returnIcalObjectId.postValue(insertedOrUpdatedItemId)
        }
    }

    fun delete() {
        viewModelScope.launch(Dispatchers.IO) {
            ICalObject.deleteItemWithChildren(iCalObjectUpdated.value!!.id, database)
            entryDeleted.postValue(true)
        }
    }
}

