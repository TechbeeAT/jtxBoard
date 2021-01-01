package at.bitfire.notesx5.ui

import android.app.Application
import android.text.Editable
import androidx.lifecycle.*
import at.bitfire.notesx5.convertCategoriesListtoCSVString
import at.bitfire.notesx5.database.VJournal
import at.bitfire.notesx5.database.VJournalDatabaseDao
import at.bitfire.notesx5.database.VJournalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class VJournalItemEditViewModel(private val vJournalItemId: Long,
                                val database: VJournalDatabaseDao,
                                application: Application) : AndroidViewModel(application) {

    lateinit var vJournalItem: LiveData<VJournalEntity?>
    lateinit var allCategories: LiveData<List<String>>

    //lateinit var allOrganizers: LiveData<List<String>>
    lateinit var allCollections: LiveData<List<String>>

    lateinit var dateVisible: LiveData<Boolean>
    lateinit var timeVisible: LiveData<Boolean>

    var returnVJournalItemId: MutableLiveData<Long> = MutableLiveData<Long>().apply { postValue(0L) }
    var savingClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }
    var deleteClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }

    lateinit var vJournalItemUpdated: MutableLiveData<VJournalEntity>



    /*
    var summaryChanged: String = ""
    var descriptionChanged: String = ""
    var statusChanged: Int = -1
    var classificationChanged: Int = -1
    var organizerChanged: String = ""
    var collectionChanged: String = ""

    var urlChanged: String = ""
    var attendeeChanged: String = ""
    var contactChanged: String = ""
    var relatesChanged: String = ""
*/

    var dtstartChangedYear: Int = -1
    var dtstartChangedMonth: Int = -1
    var dtstartChangedDay: Int = -1
    var dtstartChangedHour: Int = -1
    var dtstartChangedMinute: Int = -1

    var categoriesListChanged: MutableList<String> = mutableListOf()

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

            vJournalItemUpdated =  Transformations.map(vJournalItem) {
                it
            } as MutableLiveData<VJournalEntity>


            allCategories = database.getAllCategories()
            allCollections = database.getAllCollections()



            dateVisible = Transformations.map(vJournalItem) { item ->
                return@map item?.vJournalItem?.component == "JOURNAL"           // true if component == JOURNAL
            }

            timeVisible = Transformations.map(vJournalItem) { item ->
                if (item?.vJournalItem?.dtstart == 0L || item?.vJournalItem?.component != "JOURNAL")
                    return@map false

                val minuteFormatter = SimpleDateFormat("mm")
                val hourFormatter = SimpleDateFormat("HH")

                if (minuteFormatter.format(Date(item.vJournalItem.dtstart)).toString() == "00" && hourFormatter.format(Date(item.vJournalItem.dtstart)).toString() == "00")
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
        viewModelScope.launch() {
            //TODO: check if the item got a new sequence in the meantime!

            vJournalItemUpdated.value!!.vJournalItem.lastModified = System.currentTimeMillis()
            vJournalItemUpdated.value!!.vJournalItem.dtstamp = System.currentTimeMillis()


            /*
            var c: Calendar = Calendar.getInstance()
            c.timeInMillis = vJournalItemUpdated.value!!.vJournalItem.dtstart
            Log.println(Log.INFO, "VJournalItemEditViewMod", "Value before: ${c.timeInMillis}")
            if (dtstartChangedYear >= 0)
                c.set(Calendar.YEAR, dtstartChangedYear)
            if (dtstartChangedMonth >= 0)
                c.set(Calendar.MONTH, dtstartChangedMonth)
            if (dtstartChangedYear >= 0)
                c.set(Calendar.DAY_OF_MONTH, dtstartChangedDay)
            if (dtstartChangedHour >= 0)
                c.set(Calendar.HOUR_OF_DAY, dtstartChangedHour)
            if (dtstartChangedMinute >= 0)
                c.set(Calendar.MINUTE, dtstartChangedMinute)
            Log.println(Log.INFO, "VJournalItemEditViewMod", "Value after: ${c.timeInMillis}")

            vJournalItemUpdated.value!!.vJournalItem.dtstart = c.timeInMillis


             */

            categoriesListChanged = categoriesListChanged.sorted().toMutableList()
            vJournalItemUpdated.value!!.vJournalItem.categories = convertCategoriesListtoCSVString(categoriesListChanged)


            if (vJournalItemUpdated.value!!.vJournalItem.id == 0L) {
                //Log.println(Log.INFO, "VJournalItemViewModel", "creating a new one")
                returnVJournalItemId.value = database.insert(vJournalItemUpdated.value!!.vJournalItem)
                //Log.println(Log.INFO, "vJournalItemViewModel", vJournalItemUpdate.id.toString())
            } else {
                returnVJournalItemId.value = vJournalItemUpdated.value!!.vJournalItem.id
                vJournalItemUpdated.value!!.vJournalItem.sequence++
                database.update(vJournalItemUpdated.value!!.vJournalItem)
            }
        }
    }

    fun delete() {
        viewModelScope.launch(Dispatchers.IO) {
            database.delete(vJournalItem.value!!.vJournalItem)
        }
    }

    fun clearUrlError(s: Editable) {
        urlError.value = null
    }

}



