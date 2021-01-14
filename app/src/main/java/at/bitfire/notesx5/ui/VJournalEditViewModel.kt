package at.bitfire.notesx5.ui

import android.app.Application
import android.text.Editable
import android.util.Log
import androidx.lifecycle.*
import at.bitfire.notesx5.database.properties.Category
import at.bitfire.notesx5.database.ICalObject
import at.bitfire.notesx5.database.ICalDatabaseDao
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

    lateinit var dateVisible: LiveData<Boolean>
    lateinit var timeVisible: LiveData<Boolean>

    var returnVJournalItemId: MutableLiveData<Long> = MutableLiveData<Long>().apply { postValue(0L) }
    var savingClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }
    var deleteClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }

    var vJournalUpdated: MutableLiveData<ICalObject> = MutableLiveData<ICalObject>()
    var categoryUpdated: MutableList<Category> = mutableListOf(Category())
    var commentUpdated: MutableList<Comment> = mutableListOf(Comment())

    var categoryDeleted: MutableList<Category> = mutableListOf(Category())
    var commentDeleted: MutableList<Comment> = mutableListOf(Comment())

    val urlError = MutableLiveData<String>()


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



            dateVisible = Transformations.map(vJournalItem) { item ->
                return@map item?.vJournal?.component == "JOURNAL"           // true if component == JOURNAL
            }

            timeVisible = Transformations.map(vJournalItem) { item ->
                if (item?.vJournal?.dtstart == 0L || item?.vJournal?.component != "JOURNAL")
                    return@map false

                val minuteFormatter = SimpleDateFormat("mm")
                val hourFormatter = SimpleDateFormat("HH")

                if (minuteFormatter.format(Date(item.vJournal.dtstart)).toString() == "00" && hourFormatter.format(Date(item.vJournal.dtstart)).toString() == "00")
                    return@map false

                return@map true
            }
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

        deleteOldCategories()
        deleteOldComments()

        viewModelScope.launch() {

            insertedOrUpdatedItemId = insertOrUpdateVJournal()
            insertNewCategories(insertedOrUpdatedItemId)
            insertNewComments(insertedOrUpdatedItemId)
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

    private fun deleteOldComments() {
        commentDeleted.forEach { com2del ->
            viewModelScope.launch(Dispatchers.IO) {
                database.deleteComment(com2del)
            }
            Log.println(Log.INFO, "Comment", "${com2del.text} deleted")
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


    fun delete() {
        viewModelScope.launch(Dispatchers.IO) {
            database.delete(vJournalItem.value!!.vJournal)
        }
    }

    fun clearUrlError(s: Editable) {
        urlError.value = null
    }
}

