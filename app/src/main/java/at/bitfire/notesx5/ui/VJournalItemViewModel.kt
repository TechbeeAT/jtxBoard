package at.bitfire.notesx5.ui

import android.app.Application
import androidx.lifecycle.*
import at.bitfire.notesx5.database.*
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class VJournalItemViewModel(    private val vJournalItemId: Long,
                                val database: VJournalDatabaseDao,
                                application: Application) : AndroidViewModel(application) {

    lateinit var vJournal: LiveData<VJournalWithEverything?>

    lateinit var dateVisible: LiveData<Boolean>
    lateinit var timeVisible: LiveData<Boolean>
    lateinit var dtstartFormatted: LiveData<String>
    lateinit var createdFormatted: LiveData<String>
    lateinit var lastModifiedFormatted: LiveData<String>

    var editingClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }


    init {

        viewModelScope.launch {

            // insert a new value to initialize the vJournalItem or load the existing one from the DB
            vJournal = if (vJournalItemId == 0L)
                MutableLiveData<VJournalWithEverything?>().apply {
                    postValue(VJournalWithEverything(VJournal(), null, null, null, null, null)) }
            else
                database.get(vJournalItemId)

            setupDates()

        }
    }

    fun editingClicked() {
        editingClicked.value = true
    }



    private fun setupDates() {


        dateVisible = Transformations.map(vJournal) { item ->
            return@map item?.vJournalItem?.component == "JOURNAL"           // true if component == JOURNAL
        }

        timeVisible = Transformations.map(vJournal) { item ->
            if (item?.vJournalItem?.dtstart == 0L || item?.vJournalItem?.component != "JOURNAL" )
                return@map false

            val minuteFormatter = SimpleDateFormat("mm")
            val hourFormatter = SimpleDateFormat("HH")

            if (minuteFormatter.format(Date(item.vJournalItem.dtstart)).toString() == "00" && hourFormatter.format(Date(item.vJournalItem.dtstart)).toString() == "00")
                return@map false

            return@map true
        }



        dtstartFormatted = Transformations.map(vJournal) { item ->
            val formattedDate = DateFormat.getDateInstance(DateFormat.LONG).format(Date(item!!.vJournalItem.dtstart))
            val formattedTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(item.vJournalItem.dtstart))
            return@map "$formattedDate $formattedTime"
        }

        createdFormatted = Transformations.map(vJournal) { item ->
            item!!.vJournalItem.let { Date(it.created).toString() }
        }

        lastModifiedFormatted = Transformations.map(vJournal) { item ->
            item!!.vJournalItem.let { Date(it.lastModified).toString() }
        }

    }
}



