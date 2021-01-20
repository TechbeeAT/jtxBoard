package at.bitfire.notesx5.ui

import android.app.Application
import android.text.Editable
import android.util.Log
import androidx.lifecycle.*
import at.bitfire.notesx5.database.properties.Category
import at.bitfire.notesx5.database.ICalObject
import at.bitfire.notesx5.database.ICalDatabaseDao
import at.bitfire.notesx5.database.properties.Attendee
import at.bitfire.notesx5.database.properties.Comment
import at.bitfire.notesx5.database.properties.Relatedto
import at.bitfire.notesx5.database.relations.ICalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class VJournalEditViewModel(private val iCalEntity2edit: ICalEntity,
                            val database: ICalDatabaseDao,
                            application: Application) : AndroidViewModel(application) {

    val iCalEntity = iCalEntity2edit

    lateinit var allCategories: LiveData<List<String>>
    lateinit var allCollections: LiveData<List<String>>

    lateinit var relatedTodos: LiveData<List<ICalObject?>>

    var returnVJournalItemId: MutableLiveData<Long> = MutableLiveData<Long>().apply { postValue(0L) }
    var savingClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }
    var deleteClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }

    var iCalObjectUpdated: MutableLiveData<ICalObject> = MutableLiveData<ICalObject>().apply { postValue(iCalEntity.vJournal)}

    var categoryUpdated: MutableList<Category> = mutableListOf(Category())
    var commentUpdated: MutableList<Comment> = mutableListOf(Comment())
    var attendeeUpdated: MutableList<Attendee> = mutableListOf(Attendee())
    var subtaskUpdated: MutableList<ICalObject> = mutableListOf()


    var categoryDeleted: MutableList<Category> = mutableListOf(Category())
    var commentDeleted: MutableList<Comment> = mutableListOf(Comment())
    var attendeeDeleted: MutableList<Attendee> = mutableListOf(Attendee())
    var subtaskDeleted: MutableList<ICalObject> = mutableListOf()


    var possibleTimezones: MutableList<String> = mutableListOf("").also { it.addAll(TimeZone.getAvailableIDs().toList()) }

    var dateVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var timeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var alldayVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var timezoneVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var statusVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var classificationVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var urlVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var contactVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var categoriesVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var attendeesVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var commentsVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var progressVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var priorityVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var subtasksVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>()


    var showAll: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    var allDay: MutableLiveData<Boolean> = MutableLiveData<Boolean>(iCalEntity.vJournal.dtstartTimezone == "ALLDAY")




    val urlError = MutableLiveData<String>()
    val attendeesError = MutableLiveData<String>()



    init {

        updateVisibility()

        viewModelScope.launch() {

            relatedTodos =  database.getRelatedTodos(iCalEntity.vJournal.id)

            allCategories = database.getAllCategories()
            allCollections = database.getAllCollections()



        }
    }

    fun updateVisibility() {

        dateVisible.postValue(iCalEntity.vJournal.component == "JOURNAL")
        timeVisible.postValue(iCalEntity.vJournal.component == "JOURNAL" &&  iCalObjectUpdated.value?.dtstartTimezone != "ALLDAY") // simplified IF: Show time only if component == JOURNAL and Timezone is NOT ALLDAY
        alldayVisible.postValue(iCalEntity.vJournal.component == "JOURNAL")
        timezoneVisible.postValue(iCalEntity.vJournal.component == "JOURNAL" &&  iCalObjectUpdated.value?.dtstartTimezone != "ALLDAY") // simplified IF: Show time only if component == JOURNAL and Timezone is NOT ALLDAY
        statusVisible.postValue(iCalEntity.vJournal.component == "JOURNAL" || iCalEntity.vJournal.component == "TODO" || showAll.value == true)
        classificationVisible.postValue(iCalEntity.vJournal.component == "JOURNAL" || showAll.value == true)
        urlVisible.postValue((iCalEntity.vJournal.component == "JOURNAL" && showAll.value == true) || showAll.value == true)
        contactVisible.postValue((iCalEntity.vJournal.component == "JOURNAL" && showAll.value == true) || showAll.value == true)
        categoriesVisible.postValue(iCalEntity.vJournal.component == "JOURNAL" || showAll.value == true)
        attendeesVisible.postValue(iCalEntity.vJournal.component == "JOURNAL" || showAll.value == true)
        commentsVisible.postValue((iCalEntity.vJournal.component == "JOURNAL" && showAll.value == true) || showAll.value == true)
        progressVisible.postValue(iCalEntity.vJournal.component == "TODO")
        priorityVisible.postValue(iCalEntity.vJournal.component == "TODO")
        subtasksVisible.postValue(iCalEntity.vJournal.component == "TODO")

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

        commentUpdated.removeAll(commentDeleted)    // make sure to not accidentially upsert a comment that was deleted
        categoryUpdated.removeAll(categoryDeleted)  // make sure to not accidentially upsert a category that was deleted
        attendeeUpdated.removeAll(attendeeDeleted)  // make sure to not accidentially upsert a attendee that was deleted
        subtaskUpdated.removeAll(subtaskDeleted)


        deleteOldCategories()
        deleteOldComments()
        deleteOldAttendees()
        deleteOldSubtasks()


        viewModelScope.launch() {

            insertedOrUpdatedItemId = insertOrUpdateVJournal()
            insertNewCategories(insertedOrUpdatedItemId)
            insertNewComments(insertedOrUpdatedItemId)
            insertNewAttendees(insertedOrUpdatedItemId)
            insertNewSubtasks(insertedOrUpdatedItemId)
            //insertNewSubtaskRelation(insertedOrUpdatedItemId)
            returnVJournalItemId.value = insertedOrUpdatedItemId
        }

    }

    private suspend fun insertOrUpdateVJournal(): Long {
        if (iCalObjectUpdated.value!!.id == 0L) {

            //Log.println(Log.INFO, "VJournalItemViewModel", "creating a new one")
            return database.insertJournal(iCalObjectUpdated.value!!)
            //Log.println(Log.INFO, "vJournalItemViewModel", vJournalItemUpdate.id.toString())
        } else {
            iCalObjectUpdated.value!!.sequence++
            database.update(iCalObjectUpdated.value!!)
            return iCalObjectUpdated.value!!.id
        }
    }

    private suspend fun insertNewCategories(insertedOrUpdatedItemId: Long) {

        categoryUpdated.forEach { newCategory ->
            newCategory.icalObjectId = insertedOrUpdatedItemId                    //Update the foreign key for newly added comments
            viewModelScope.launch() {
                database.insertCategory(newCategory)
            }
            Log.println(Log.INFO, "CategoryUpdated", "${newCategory.text} added")
        }
    }



    private fun deleteOldCategories() {
        categoryDeleted.forEach { cat2del ->
                viewModelScope.launch(Dispatchers.IO) {
                    database.deleteCategory(cat2del)
                }
                Log.println(Log.INFO, "Category", "${cat2del.text} deleted")
            }
    }

    private suspend fun insertNewComments(insertedOrUpdatedItemId: Long) {

        commentUpdated.forEach { newComment ->
             newComment.icalObjectId = insertedOrUpdatedItemId                    //Update the foreign key for newly added comments
             viewModelScope.launch() {
                 database.insertComment(newComment)
             }
            Log.println(Log.INFO, "CommentUpdated", "${newComment.text} added")
        }
    }


    private fun deleteOldComments() {
        commentDeleted.forEach { com2del ->
            viewModelScope.launch(Dispatchers.IO) {
                database.deleteComment(com2del)
            }
            Log.println(Log.INFO, "Comment", "${com2del.text} deleted")
        }
    }



    private suspend fun insertNewAttendees(insertedOrUpdatedItemId: Long) {

        attendeeUpdated.forEach { newAttendee ->
            newAttendee.icalObjectId = insertedOrUpdatedItemId                    //Update the foreign key for newly added comments
            viewModelScope.launch() {
                database.insertAttendee(newAttendee)
            }
            Log.println(Log.INFO, "Attendee", "${newAttendee.caladdress} added")
        }
    }




    private fun deleteOldAttendees() {
        attendeeDeleted.forEach { att2del ->
            viewModelScope.launch(Dispatchers.IO) {
                database.deleteAttendee(att2del)
            }
            Log.println(Log.INFO, "Comment", "${att2del.caladdress} deleted")
        }
    }



    private suspend fun insertNewSubtasks(insertedOrUpdatedItemId: Long) {

        subtaskUpdated.forEach { subtask ->
            // attention, linkedICalObjectId is actually set in DAO!
            subtask.id = database.upsertSubtask(subtask)
            Log.println(Log.INFO, "Subtask", "${subtask.id} ${subtask.summary} added")


            // Only insert if the relation doesn't exist already, otherwise there's nothing to do
            if(iCalEntity.relatedto?.find { it.icalObjectId == insertedOrUpdatedItemId && it.linkedICalObjectId == subtask.id} == null )
                database.upsertRelatedto(Relatedto(icalObjectId = insertedOrUpdatedItemId, linkedICalObjectId = subtask.id, reltypeparam = "CHILD", text = subtask.uid))

        }
    }





    private fun deleteOldSubtasks() {

        if(iCalObjectUpdated.value?.id == null)      // This should not be possible to have no Id at this point, just to be sure!
            return

        subtaskDeleted.forEach { subtask2del ->
            viewModelScope.launch(Dispatchers.IO) {
                database.delete(subtask2del)
                database.deleteRelatedto(iCalObjectUpdated.value!!.id, subtask2del.id)
            }
            Log.println(Log.INFO, "Subtask", "${subtask2del.summary} deleted")
        }
    }


    fun delete() {
        viewModelScope.launch(Dispatchers.IO) {
            database.delete(iCalEntity.vJournal)
        }
    }

    fun clearUrlError(s: Editable) {
        urlError.value = null
    }

    fun clearAttendeesError(s: Editable) {
        attendeesError.value = null
    }

}

