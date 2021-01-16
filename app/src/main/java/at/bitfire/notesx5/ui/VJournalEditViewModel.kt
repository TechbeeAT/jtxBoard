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
import at.bitfire.notesx5.database.relations.ICalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class VJournalEditViewModel(private val iCalEntity2edit: ICalEntity,
                            val database: ICalDatabaseDao,
                            application: Application) : AndroidViewModel(application) {

    val iCalEntity = iCalEntity2edit

    lateinit var allCategories: LiveData<List<String>>
    lateinit var allCollections: LiveData<List<String>>

    var returnVJournalItemId: MutableLiveData<Long> = MutableLiveData<Long>().apply { postValue(0L) }
    var savingClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }
    var deleteClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }

    var iCalObjectUpdated: MutableLiveData<ICalObject> = MutableLiveData<ICalObject>().apply { postValue(iCalEntity.vJournal)}

    var categoryUpdated: MutableList<Category> = mutableListOf(Category())
    var commentUpdated: MutableList<Comment> = mutableListOf(Comment())
    var attendeeUpdated: MutableList<Attendee> = mutableListOf(Attendee())

    var categoryDeleted: MutableList<Category> = mutableListOf(Category())
    var commentDeleted: MutableList<Comment> = mutableListOf(Comment())
    var attendeeDeleted: MutableList<Attendee> = mutableListOf(Attendee())


    var possibleTimezones: MutableList<String> = mutableListOf("").also { it.addAll(TimeZone.getAvailableIDs().toList()) }

    var dateVisible: LiveData<Boolean> = Transformations.map(iCalObjectUpdated) { it?.component == "JOURNAL" }
    var timeVisible: LiveData<Boolean> = Transformations.map(iCalObjectUpdated) { it?.component == "JOURNAL" &&  it.dtstartTimezone != "ALLDAY"} // simplified IF: Show time only if component == JOURNAL and Timezone is NOT ALLDAY

    val urlError = MutableLiveData<String>()
    val attendeesError = MutableLiveData<String>()


    init {

        viewModelScope.launch() {

            allCategories = database.getAllCategories()
            allCollections = database.getAllCollections()

        }
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


        deleteOldCategories()
        deleteOldComments()
        deleteOldAttendees()


        viewModelScope.launch() {

            insertedOrUpdatedItemId = insertOrUpdateVJournal()
            insertNewCategories(insertedOrUpdatedItemId)
            insertNewComments(insertedOrUpdatedItemId)
            insertNewAttendees(insertedOrUpdatedItemId)
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

    /*
    fun updateDateTimeVisibility() {
        dateVisible.value = iCalObjectUpdated.value?.component == "JOURNAL"    // simplified IF: Show date only if component == JOURNAL
        timeVisible.value = iCalObjectUpdated.value?.component == "JOURNAL" &&  iCalObjectUpdated.value?.dtstartTimezone != "ALLDAY" // simplified IF: Show time only if component == JOURNAL and Timezone is NOT ALLDAY
    }

     */
}

