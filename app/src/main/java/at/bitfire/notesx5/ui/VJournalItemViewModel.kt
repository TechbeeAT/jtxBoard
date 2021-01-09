package at.bitfire.notesx5.ui

import android.app.Application
import androidx.lifecycle.*
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.relations.VJournalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class VJournalItemViewModel(    private val vJournalItemId: Long,
                                val database: VJournalDatabaseDao,
                                application: Application) : AndroidViewModel(application) {

    lateinit var vJournal: LiveData<VJournalEntity?>
    lateinit var vCategory: LiveData<List<VCategory>>

    lateinit var dateVisible: LiveData<Boolean>
    lateinit var timeVisible: LiveData<Boolean>
    lateinit var dtstartFormatted: LiveData<String>
    lateinit var createdFormatted: LiveData<String>
    lateinit var lastModifiedFormatted: LiveData<String>

    lateinit var urlVisible: LiveData<Boolean>
    lateinit var attendeesVisible: LiveData<Boolean>
    lateinit var organizerVisible: LiveData<Boolean>
    lateinit var contactVisible: LiveData<Boolean>
    lateinit var relatedtoVisible: LiveData<Boolean>

    var editingClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }


    init {

        viewModelScope.launch {

            // insert a new value to initialize the vJournalItem or load the existing one from the DB
            vJournal = if (vJournalItemId == 0L)
                MutableLiveData<VJournalEntity?>().apply {
                    postValue(VJournalEntity(VJournal(), null, null, null, null, null)) }
            else
                database.get(vJournalItemId)

            vCategory = Transformations.map(vJournal) {
                it?.vCategory
            }

            setupDates()

            urlVisible = Transformations.map(vJournal) { item ->
                return@map !item?.vJournal?.url.isNullOrBlank()      // true if url is NOT null or empty
            }
            attendeesVisible = Transformations.map(vJournal) { item ->
                return@map !item?.vAttendee.isNullOrEmpty()      // true if attendees is NOT null or empty
            }
            organizerVisible = Transformations.map(vJournal) { item ->
                return@map !(item?.vOrganizer == null)      // true if organizer is NOT null or empty
            }
            contactVisible = Transformations.map(vJournal) { item ->
                return@map !item?.vJournal?.contact.isNullOrBlank()      // true if contact is NOT null or empty
            }
            relatedtoVisible = Transformations.map(vJournal) { item ->
                return@map !item?.vRelatedto.isNullOrEmpty()      // true if relatedto is NOT null or empty
            }

        }
    }

    fun editingClicked() {
        editingClicked.value = true
    }



    private fun setupDates() {


        dateVisible = Transformations.map(vJournal) { item ->
            return@map item?.vJournal?.component == "JOURNAL"           // true if component == JOURNAL
        }

        timeVisible = Transformations.map(vJournal) { item ->
            if (item?.vJournal?.dtstart == 0L || item?.vJournal?.component != "JOURNAL" )
                return@map false

            val minuteFormatter = SimpleDateFormat("mm")
            val hourFormatter = SimpleDateFormat("HH")

            if (minuteFormatter.format(Date(item.vJournal.dtstart)).toString() == "00" && hourFormatter.format(Date(item.vJournal.dtstart)).toString() == "00")
                return@map false

            return@map true
        }



        dtstartFormatted = Transformations.map(vJournal) { item ->
            val formattedDate = DateFormat.getDateInstance(DateFormat.LONG).format(Date(item!!.vJournal.dtstart))
            val formattedTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(item.vJournal.dtstart))
            return@map "$formattedDate $formattedTime"
        }

        createdFormatted = Transformations.map(vJournal) { item ->
            item!!.vJournal.let { Date(it.created).toString() }
        }

        lastModifiedFormatted = Transformations.map(vJournal) { item ->
            item!!.vJournal.let { Date(it.lastModified).toString() }
        }

    }

    fun upsertComment(comment: VComment) {
        viewModelScope.launch() {
            database.insertComment(comment)
        }
    }

    fun deleteComment(comment: VComment) {
        viewModelScope.launch(Dispatchers.IO) {
            database.deleteComment(comment)
        }
    }

    fun updateUrl (url: String) {
        val updatedVJournal = vJournal.value!!.vJournal.copy()
        updatedVJournal.url = url
        viewModelScope.launch(Dispatchers.IO) {
            database.update(updatedVJournal)
        }
    }

    fun updateContact (contact: String) {
        val updatedVJournal = vJournal.value!!.vJournal.copy()
        updatedVJournal.contact = contact
        viewModelScope.launch(Dispatchers.IO) {
            database.update(updatedVJournal)
        }
    }
}



