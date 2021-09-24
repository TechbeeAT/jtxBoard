/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.app.Application
import android.text.Editable
import android.util.Log
import androidx.lifecycle.*
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.*


class IcalEditViewModel(
    val iCalEntity: ICalEntity,
    val database: ICalDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    companion object {
        const val TAB_GENERAL = 0
        const val TAB_MORE = 1
        const val TAB_COMMENTS = 2
        const val TAB_ATTACHMENTS = 3
        const val TAB_SUBTASKS = 4
        const val TAB_ALARMS = 5
        const val TAB_RECURRING = 6

        const val RECURRENCE_MODE_DAY = 0
        const val RECURRENCE_MODE_WEEK = 1
        const val RECURRENCE_MODE_MONTH = 2
        const val RECURRENCE_MODE_YEAR = 3

    }

    lateinit var allCategories: LiveData<List<String>>
    lateinit var allCollections: LiveData<List<ICalCollection>>
    lateinit var allRelatedto: LiveData<List<Relatedto>>

    lateinit var relatedSubtasks: LiveData<List<ICalObject?>>

    var returnVJournalItemId: MutableLiveData<Long> =
        MutableLiveData<Long>().apply { postValue(0L) }
    var savingClicked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { postValue(false) }
    var deleteClicked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { postValue(false) }

    var iCalObjectUpdated: MutableLiveData<ICalObject> =
        MutableLiveData<ICalObject>().apply { postValue(iCalEntity.property) }

    var categoryUpdated: MutableList<Category> = mutableListOf(Category())
    var commentUpdated: MutableList<Comment> = mutableListOf(Comment())
    var attachmentUpdated: MutableList<Attachment> = mutableListOf(Attachment())
    var attendeeUpdated: MutableList<Attendee> = mutableListOf(Attendee())
    var resourceUpdated: MutableList<Resource> = mutableListOf(Resource())
    var subtaskUpdated: MutableList<ICalObject> = mutableListOf()

    var categoryDeleted: MutableList<Category> = mutableListOf(Category())
    var commentDeleted: MutableList<Comment> = mutableListOf(Comment())
    var attachmentDeleted: MutableList<Attachment> = mutableListOf(Attachment())
    var attendeeDeleted: MutableList<Attendee> = mutableListOf(Attendee())
    var resourceDeleted: MutableList<Resource> = mutableListOf(Resource())
    var subtaskDeleted: MutableList<ICalObject> = mutableListOf()


    var possibleTimezones: MutableList<String> =
        mutableListOf("").also { it.addAll(TimeZone.getAvailableIDs().toList()) }

    var collectionVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var summaryVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var descriptionVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var dateVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var timeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var alldayVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var timezoneVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var statusVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var classificationVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var urlVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var locationVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var contactVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var categoriesVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var attendeesVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var resourcesVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var commentsVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var attachmentsVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var takePhotoVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var progressVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var priorityVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var subtasksVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var duedateVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var duetimeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var completeddateVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var completedtimeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var starteddateVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var startedtimeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var recurrenceVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var recurrenceGeneralVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var recurrenceWeekdaysVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var recurrenceDayOfMonthVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()







    var duedateFormated: LiveData<String> = Transformations.map(iCalObjectUpdated) {
        if (it.due != null)
            return@map DateFormat.getDateInstance().format(it.due)
        else
            return@map null
    }

    var duetimeFormated: LiveData<String> = Transformations.map(iCalObjectUpdated) {
        if (it.due != null)
            return@map DateFormat.getTimeInstance(DateFormat.SHORT).format(it.due)
        else
            return@map null
    }

    var completeddateFormated: LiveData<String> = Transformations.map(iCalObjectUpdated) {
        if (it.completed != null)
            return@map DateFormat.getDateInstance().format(it.completed)
        else
            return@map null
    }

    var completedtimeFormated: LiveData<String> = Transformations.map(iCalObjectUpdated) {
        if (it.completed != null)
            return@map DateFormat.getTimeInstance(DateFormat.SHORT).format(it.completed)
        else
            return@map null
    }

    var starteddateFormated: LiveData<String> = Transformations.map(iCalObjectUpdated) {
        if (it.dtstart != null)
            return@map DateFormat.getDateInstance().format(it.dtstart)
        else
            return@map null
    }

    var startedtimeFormated: LiveData<String> = Transformations.map(iCalObjectUpdated) {
        if (it.dtstart != null)
            return@map DateFormat.getTimeInstance(DateFormat.SHORT).format(it.dtstart)
        else
            return@map null
    }


    var allDayChecked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(iCalEntity.property.dtstartTimezone == "ALLDAY")
    var addDueTimeChecked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(iCalEntity.property.component == Component.VTODO.name && iCalEntity.property.dueTimezone != "ALLDAY")
    var addCompletedTimeChecked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(iCalEntity.property.component == Component.VTODO.name && iCalEntity.property.completedTimezone != "ALLDAY")
    var addStartedTimeChecked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(iCalEntity.property.component == Component.VTODO.name && iCalEntity.property.dtstartTimezone != "ALLDAY")
    var recurrenceChecked: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(false)
    //todo make test, pre-fill
    var recurrenceMode: MutableLiveData<Int> = MutableLiveData<Int>(RECURRENCE_MODE_DAY)


    val urlError = MutableLiveData<String?>()
    val attendeesError = MutableLiveData<String>()

    var isLandscape = false
    var selectedTab = TAB_GENERAL


    init {

        updateVisibility()

        viewModelScope.launch {

            relatedSubtasks = database.getRelatedTodos(iCalEntity.property.id)

            allCategories = database.getAllCategories()
            allCollections = when (iCalEntity.property.component) {
                Component.VTODO.name -> database.getAllVTODOCollections()
                Component.VJOURNAL.name -> database.getAllVJOURNALCollections()
                else -> database.getAllCollections() // should not happen!
            }

            allRelatedto = database.getAllRelatedto()

        }
    }

    fun updateVisibility() {

        collectionVisible.postValue(selectedTab == TAB_GENERAL || isLandscape)
        summaryVisible.postValue(selectedTab == TAB_GENERAL || isLandscape)
        descriptionVisible.postValue(selectedTab == TAB_GENERAL || isLandscape)
        dateVisible.postValue(iCalEntity.property.module == Module.JOURNAL.name && (selectedTab == TAB_GENERAL || isLandscape))
        timeVisible.postValue(iCalEntity.property.module == Module.JOURNAL.name && (selectedTab == TAB_GENERAL || isLandscape) && iCalObjectUpdated.value?.dtstartTimezone != "ALLDAY" ) // simplified IF: Show time only if.module == JOURNAL and Timezone is NOT ALLDAY
        alldayVisible.postValue(iCalEntity.property.module == Module.JOURNAL.name && (selectedTab == TAB_GENERAL || isLandscape))
        timezoneVisible.postValue(iCalEntity.property.module == Module.JOURNAL.name && (selectedTab == TAB_GENERAL || isLandscape) && iCalObjectUpdated.value?.dtstartTimezone != "ALLDAY" ) // simplified IF: Show time only if.module == JOURNAL and Timezone is NOT ALLDAY
        statusVisible.postValue(selectedTab == TAB_GENERAL || isLandscape)
        classificationVisible.postValue(selectedTab == TAB_GENERAL || isLandscape)
        urlVisible.postValue(selectedTab == TAB_MORE || isLandscape)
        locationVisible.postValue(selectedTab == TAB_MORE || isLandscape)
        categoriesVisible.postValue(selectedTab == TAB_GENERAL || isLandscape)
        contactVisible.postValue(selectedTab == TAB_MORE || isLandscape)
        attendeesVisible.postValue(selectedTab == TAB_MORE || isLandscape)
        resourcesVisible.postValue(selectedTab == TAB_MORE || isLandscape)
        commentsVisible.postValue(selectedTab == TAB_COMMENTS || isLandscape)
        attachmentsVisible.postValue(selectedTab == TAB_ATTACHMENTS || isLandscape)
        takePhotoVisible.postValue(selectedTab == TAB_ATTACHMENTS || isLandscape)
        progressVisible.postValue(iCalEntity.property.module == Module.TODO.name && (selectedTab == TAB_GENERAL || isLandscape))
        priorityVisible.postValue(iCalEntity.property.module == Module.TODO.name && (selectedTab == TAB_GENERAL || isLandscape))
        subtasksVisible.postValue(selectedTab == TAB_SUBTASKS || isLandscape)
        duedateVisible.postValue((selectedTab == TAB_GENERAL || isLandscape) && iCalEntity.property.module == Module.TODO.name)
        duetimeVisible.postValue((selectedTab == TAB_GENERAL || isLandscape) && iCalEntity.property.module == Module.TODO.name && iCalEntity.property.dueTimezone != "ALLDAY")
        completeddateVisible.postValue((selectedTab == TAB_GENERAL || isLandscape) && iCalEntity.property.module == Module.TODO.name)
        completedtimeVisible.postValue((selectedTab == TAB_GENERAL || isLandscape) && iCalEntity.property.module == Module.TODO.name && iCalEntity.property.completedTimezone != "ALLDAY")
        starteddateVisible.postValue((selectedTab == TAB_GENERAL || isLandscape) && iCalEntity.property.module == Module.TODO.name)
        startedtimeVisible.postValue((selectedTab == TAB_GENERAL || isLandscape) && iCalEntity.property.module == Module.TODO.name && iCalEntity.property.dtstartTimezone != "ALLDAY")
        recurrenceVisible.postValue((selectedTab == TAB_RECURRING || isLandscape))
        recurrenceGeneralVisible.postValue((selectedTab == TAB_RECURRING && recurrenceChecked.value?:false))
        recurrenceWeekdaysVisible.postValue((selectedTab == TAB_RECURRING && recurrenceChecked.value?:false && recurrenceMode.value == RECURRENCE_MODE_WEEK))
        recurrenceDayOfMonthVisible.postValue((selectedTab == TAB_RECURRING && recurrenceChecked.value?:false && recurrenceMode.value == RECURRENCE_MODE_MONTH))
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

        if (iCalObjectUpdated.value!!.collectionId != 1L)
            iCalObjectUpdated.value!!.dirty = true

        commentUpdated.removeAll(commentDeleted)    // make sure to not accidentially upsert a comment that was deleted
        categoryUpdated.removeAll(categoryDeleted)  // make sure to not accidentially upsert a category that was deleted
        attachmentUpdated.removeAll(attachmentDeleted)  // make sure to not accidentially upsert a category that was deleted
        attendeeUpdated.removeAll(attendeeDeleted)  // make sure to not accidentially upsert a attendee that was deleted
        subtaskUpdated.removeAll(subtaskDeleted)
        resourceUpdated.removeAll(resourceDeleted)

        // make sure to delete all rows of items, that were deleted for this ICalObject
        viewModelScope.launch(Dispatchers.IO) {

            categoryDeleted.forEach { cat2del ->
                database.deleteCategory(cat2del)
                Log.println(Log.INFO, "Category", "${cat2del.text} deleted")
            }
            commentDeleted.forEach { com2del ->
                database.deleteComment(com2del)
                Log.println(Log.INFO, "Comment", "${com2del.text} deleted")
            }
            attachmentDeleted.forEach { att2del ->
                database.deleteAttachment(att2del)
                Log.println(Log.INFO, "Attachment", "Attachment deleted")
            }
            attendeeDeleted.forEach { att2del ->
                database.deleteAttendee(att2del)
                Log.println(Log.INFO, "Attendee", "${att2del.caladdress} deleted")
            }
            resourceDeleted.forEach { res ->
                database.deleteResource(res)
                Log.println(Log.INFO, "Resource", "{${res.text} deleted")
            }
            subtaskDeleted.forEach { subtask2del ->
                viewModelScope.launch(Dispatchers.IO) {
                    database.deleteRelatedChildren(subtask2del.id)       // Also Child-Elements of Child-Elements need to be deleted!
                    database.delete(subtask2del)
                    database.deleteRelatedto(iCalObjectUpdated.value!!.id, subtask2del.id)
                }
                Log.println(Log.INFO, "Subtask", "${subtask2del.summary} deleted")
            }
        }
        //deleteOldResources()


        viewModelScope.launch {

            insertedOrUpdatedItemId = insertOrUpdateICalObject()
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
            subtaskUpdated.forEach { subtask ->
                subtask.sequence++
                subtask.lastModified = System.currentTimeMillis()
                subtask.dirty = true
                subtask.collectionId = iCalObjectUpdated.value?.collectionId!!
                subtask.id = database.insertSubtask(subtask)
                Log.println(Log.INFO, "Subtask", "${subtask.id} ${subtask.summary} added")


                // Only insert if the relation doesn't exist already, otherwise there's nothing to do
                if (iCalEntity.relatedto?.find { it.icalObjectId == insertedOrUpdatedItemId && it.linkedICalObjectId == subtask.id } == null)
                    database.insertRelatedto(
                        Relatedto(
                            icalObjectId = insertedOrUpdatedItemId,
                            linkedICalObjectId = subtask.id,
                            reltype = "CHILD",
                            text = subtask.uid
                        )
                    )

            }
            returnVJournalItemId.value = insertedOrUpdatedItemId
        }

    }

    private suspend fun insertOrUpdateICalObject(): Long {
        return when {
            iCalObjectUpdated.value!!.id == 0L -> {

                //Log.println(Log.INFO, "VJournalItemViewModel", "creating a new one")
                database.insertICalObject(iCalObjectUpdated.value!!)
                //Log.println(Log.INFO, "vJournalItemViewModel", vJournalItemUpdate.id.toString())
            }
            iCalEntity.ICalCollection!!.collectionId != iCalObjectUpdated.value!!.collectionId -> {

                val newId = updateCollectionWithChildren(iCalObjectUpdated.value!!.id, null)
                iCalObjectUpdated.value!!.id = newId
                database.update(iCalObjectUpdated.value!!)
                return newId
                // TODO mark the main element as deleted or delete it, make sure all children are deleted/marked as deleted

            }
            else -> {
                database.update(iCalObjectUpdated.value!!)
                iCalObjectUpdated.value!!.id
            }
        }
    }


    fun delete() {
        deleteItemWithChildren(iCalObjectUpdated.value!!.id)
    }

    /**
     * this function takes a parent [id], the function recursively calls itself and deletes all items and linked children (for local collections)
     * or updates the linked children and marks them as deleted.
     */
    private fun deleteItemWithChildren(id: Long) {

        when {
            iCalObjectUpdated.value!!.id == 0L -> return // do nothing, the item was never saved in DB
            iCalEntity.ICalCollection?.collectionId == 1L -> {        // call the function again to recursively delete all children, then delete the item
                val children =
                    allRelatedto.value?.filter { it.icalObjectId == id && it.reltype == Reltype.CHILD.name }
                children?.forEach {
                    it.linkedICalObjectId?.let { linkedICalObjectId ->
                        deleteItemWithChildren(linkedICalObjectId)
                    }
                }

                viewModelScope.launch(Dispatchers.IO) {
                    database.deleteICalObjectsbyId(id)
                }
            }
            else -> {                                                 // call the function again to recursively delete all children, then mark the item as deleted
                val children =
                    allRelatedto.value?.filter { it.icalObjectId == id && it.reltype == Reltype.CHILD.name }
                children?.forEach {
                    it.linkedICalObjectId?.let { linkedICalObjectId ->
                        deleteItemWithChildren(linkedICalObjectId)
                    }
                }

                viewModelScope.launch {
                    database.updateDeleted(listOf(id), System.currentTimeMillis())
                }
            }
        }
    }

    /**
     * @param [id] the id of the item for which the collection needs to be updated
     * @param [parentId] is needed for the recursive call in order to provide it for the movItemToNewCollection(...) function. For the initial call this would be null as the function should initially always be called from the top parent.
     *
     * this function takes care of
     * 1. moving the item to a new collection (by copying and deleting the current item)
     * 2. determining the children of this item and calling itself recusively to to the same again for each child.
     *
     * @return The new id of the item in the new collection
     */
    private suspend fun updateCollectionWithChildren(id: Long, parentId: Long?): Long {

        val newParentId = moveItemToNewCollection(id, parentId)

        // then determine the children and recursively call the function again. The possible child becomes the new parent and is added to the list until there are no more children.
        val children =
            allRelatedto.value?.filter { it.icalObjectId == id && it.reltype == Reltype.CHILD.name }
        children?.forEach {
            it.linkedICalObjectId?.let { linkedICalObjectId ->
                updateCollectionWithChildren(
                    linkedICalObjectId,
                    newParentId
                )
            }
        }
        deleteItemWithChildren(id)                                         // make sure to delete the old item (or marked as deleted - this is already handled in the function)
        return newParentId
    }

    /**
     * @param [id] is the id of the original item that should be moved to another collection. On the recursive call this is the id of the original child.
     * @param [newParentId] is the id of the parent that was already copied into the new collection. This is needed in order to re-create the relation between the parent and the child.
     *
     * This function creates a copy of an item with all it's children in the new collection and then
     * deletes (or marks as deleted) the original item.
     *
     * @return the new id of the item that was inserted (that becomes the newParentId)
     *
     */
    private suspend fun moveItemToNewCollection(id: Long, newParentId: Long?): Long =
        withContext(Dispatchers.IO) {
            val item = database.getSync(id)
            if (item != null) {
                item.property.id = 0L
                item.property.collectionId = iCalObjectUpdated.value!!.collectionId
                item.property.sequence = 0
                item.property.dirty = true
                item.property.lastModified = System.currentTimeMillis()
                item.property.created = System.currentTimeMillis()
                item.property.dtstamp = System.currentTimeMillis()
                item.property.uid = ICalObject.generateNewUID()
                val newId = database.insertICalObject(item.property)

                item.attendees?.forEach {
                    it.icalObjectId = newId
                    database.insertAttendee(it)
                }

                item.resources?.forEach {
                    it.icalObjectId = newId
                    database.insertResource(it)
                }

                item.categories?.forEach {
                    it.icalObjectId = newId
                    database.insertCategory(it)
                }

                item.comments?.forEach {
                    it.icalObjectId = newId
                    database.insertComment(it)
                }

                if (item.organizer != null) {
                    item.organizer?.icalObjectId = newId
                    database.insertOrganizer(item.organizer!!)
                }

                item.attachments?.forEach {
                    it.icalObjectId = newId
                    database.insertAttachment(it)
                }

                // relations need to be rebuilt from the child to the parent
                if (newParentId != null) {
                    val rel = Relatedto()
                    rel.icalObjectId = newParentId
                    rel.linkedICalObjectId = newId
                    rel.reltype = Reltype.CHILD.name
                    rel.text = item.property.uid
                    database.insertRelatedto(rel)
                }


                return@withContext newId
            }
            return@withContext 0L
        }


    fun clearUrlError(s: Editable) {
        urlError.value = null
    }
}

/*
fun clearAttendeeError(s: Editable) {
    attendeesError.value = null
}

 */
