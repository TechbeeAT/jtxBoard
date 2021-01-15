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


class VJournalEditViewModel(private val vJournalItemId: Long,
                            val database: ICalDatabaseDao,
                            application: Application) : AndroidViewModel(application) {

    lateinit var vJournalItem: LiveData<ICalEntity?>
    lateinit var allCategories: LiveData<List<String>>

    lateinit var allCollections: LiveData<List<String>>



    var returnVJournalItemId: MutableLiveData<Long> = MutableLiveData<Long>().apply { postValue(0L) }
    var savingClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }
    var deleteClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }

    var vJournalUpdated: MutableLiveData<ICalObject> = MutableLiveData<ICalObject>()

    var categoryUpdated: MutableList<Category> = mutableListOf(Category())
    var commentUpdated: MutableList<Comment> = mutableListOf(Comment())
    var attendeeUpdated: MutableList<Attendee> = mutableListOf(Attendee())

    var categoryDeleted: MutableList<Category> = mutableListOf(Category())
    var commentDeleted: MutableList<Comment> = mutableListOf(Comment())
    var attendeeDeleted: MutableList<Attendee> = mutableListOf(Attendee())


    var possibleTimezones: MutableList<String> = mutableListOf("").also { it.addAll(TimeZone.getAvailableIDs().toList()) }

    var dateVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }
    var timeVisible: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }


    val urlError = MutableLiveData<String>()
    val attendeesError = MutableLiveData<String>()



    init {

        viewModelScope.launch() {

            // insert a new value to initialize the vJournalItem or load the existing one from the DB
            vJournalItem = if (vJournalItemId == 0L)
                MutableLiveData<ICalEntity>().apply {
                    postValue(ICalEntity(ICalObject(), null, null, null, null, null))
                }
            else {
                database.get(vJournalItemId)
            }


            allCategories = database.getAllCategories()
            allCollections = database.getAllCollections()


/*
            dateVisible = Transformations.map(vJournalUpdated) { item ->
                if (item?.component == "JOURNAL")   // show for JOURNAL only
                    return@map true

                return@map false
            }

            timeVisible = Transformations.map(vJournalUpdated) { item ->
                if (item?.component == "JOURNAL" && item.dtstartTimezone != "ALLDAY")   // show for JOURNAL but only if the timezone is NOT set to ALLDAY
                    return@map true

                return@map false
            }



 */
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

        vJournalUpdated.value!!.lastModified = System.currentTimeMillis()
        vJournalUpdated.value!!.dtstamp = System.currentTimeMillis()

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
        if (vJournalUpdated.value!!.id == 0L) {

            //Log.println(Log.INFO, "VJournalItemViewModel", "creating a new one")
            return database.insertJournal(vJournalUpdated.value!!)
            //Log.println(Log.INFO, "vJournalItemViewModel", vJournalItemUpdate.id.toString())
        } else {
            vJournalUpdated.value!!.sequence++
            database.update(vJournalUpdated.value!!)
            return vJournalUpdated.value!!.id
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
            database.delete(vJournalItem.value!!.vJournal)
        }
    }

    fun clearUrlError(s: Editable) {
        urlError.value = null
    }

    fun clearAttendeesError(s: Editable) {
        attendeesError.value = null
    }

    fun updateDateTimeVisibility() {
        dateVisible.value = vJournalUpdated.value?.component == "JOURNAL"    // simplified IF: Show date only if component == JOURNAL
        timeVisible.value = vJournalUpdated.value?.component == "JOURNAL" &&  vJournalUpdated.value?.dtstartTimezone != "ALLDAY" // simplified IF: Show time only if component == JOURNAL and Timezone is NOT ALLDAY
    }
}

