package at.bitfire.notesx5.ui

import android.app.Application
import android.text.Editable
import android.util.Log
import androidx.lifecycle.*
import at.bitfire.notesx5.database.properties.Category
import at.bitfire.notesx5.database.VJournal
import at.bitfire.notesx5.database.VJournalDatabaseDao
import at.bitfire.notesx5.database.properties.Organizer
import at.bitfire.notesx5.database.relations.VJournalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class VJournalEditViewModel(private val vJournalItemId: Long,
                            val database: VJournalDatabaseDao,
                            application: Application) : AndroidViewModel(application) {

    lateinit var vJournalItem: LiveData<VJournalEntity?>
    lateinit var allCategories: LiveData<List<String>>

    lateinit var allCollections: LiveData<List<String>>

    lateinit var dateVisible: LiveData<Boolean>
    lateinit var timeVisible: LiveData<Boolean>

    var returnVJournalItemId: MutableLiveData<Long> = MutableLiveData<Long>().apply { postValue(0L) }
    var savingClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }
    var deleteClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }

    var vJournalUpdated: MutableLiveData<VJournal> = MutableLiveData<VJournal>().apply { postValue(VJournal()) }
    var categoryUpdated: MutableList<Category> = mutableListOf(Category())
    var organizerUpdated: MutableLiveData<Organizer> = MutableLiveData<Organizer>().apply { postValue(Organizer()) }


    val urlError = MutableLiveData<String>()


    init {

        viewModelScope.launch() {

            // insert a new value to initialize the vJournalItem or load the existing one from the DB
            vJournalItem = if (vJournalItemId == 0L)
                MutableLiveData<VJournalEntity>().apply {
                    postValue(VJournalEntity(VJournal(), null, null, null, null, null))
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

        deleteOldCategories()

        viewModelScope.launch() {

            insertedOrUpdatedItemId = insertOrUpdateVJournal()
            insertNewCategories(insertedOrUpdatedItemId)
            upsertOrganizer(insertedOrUpdatedItemId)
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

        Log.println(Log.INFO, "vCategoryUpdated", "Size of Array: ${categoryUpdated.size}")
        if (categoryUpdated != vJournalItem.value!!.category) {   // make effort of updating only if the categories changed
            Log.println(Log.INFO, "vCategoryUpdated", "Categories are not the same and need to be updated")

            categoryUpdated.forEach { newVCategory ->
                Log.println(Log.INFO, "vCategoryUpdated", "Checking #${newVCategory.categoryId} with value ${newVCategory.text} and journalLinkId ${newVCategory.journalLinkId}")

                if (newVCategory.categoryId == 0L && newVCategory.text.isNotBlank()) {                                     //Insert only categories that don't have an ID yet (= new ones)
                    newVCategory.journalLinkId = insertedOrUpdatedItemId                    //Update the foreign key for newly added categories
                    viewModelScope.launch() {
                        database.insertCategory(newVCategory)
                    }
                    Log.println(Log.INFO, "vCategoryUpdated", "${newVCategory.text} added")
                }
            }
        }
    }

    private suspend fun upsertOrganizer(insertedOrUpdatedItemId: Long) {
        organizerUpdated.value!!.journalLinkId = insertedOrUpdatedItemId
        database.insertOrganizer(organizerUpdated.value!!)
    }


    fun deleteOldCategories() {
        // if the old category cannot be found in the new list, then delete it!
        vJournalItem.value!!.category?.forEach { oldVCategory ->
            if (!categoryUpdated.contains(oldVCategory)) {
                viewModelScope.launch(Dispatchers.IO) {
                    database.deleteCategory(oldVCategory)
                }
                Log.println(Log.INFO, "Category", "${oldVCategory.text} deleted")
            }
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



