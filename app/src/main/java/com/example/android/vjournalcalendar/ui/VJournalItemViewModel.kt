package com.example.android.vjournalcalendar.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.example.android.vjournalcalendar.database.VJournalDatabaseDao
import com.example.android.vjournalcalendar.database.vJournalItem
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class VJournalItemViewModel(    private val vJournalItemId: Long,
                                val database: VJournalDatabaseDao,
                                application: Application) : AndroidViewModel(application) {

    lateinit var vJournalItem: LiveData<vJournalItem?>

    lateinit var dateVisible: LiveData<Boolean>
    lateinit var timeVisible: LiveData<Boolean>
    lateinit var dtstartFormatted: LiveData<String>
    lateinit var createdFormatted: LiveData<String>
    lateinit var lastModifiedFormatted: LiveData<String>

    var editingClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }


    init {

        viewModelScope.launch {

            // insert a new value to initialize the vJournalItem or load the existing one from the DB
            vJournalItem = if (vJournalItemId == 0L)
                MutableLiveData<vJournalItem?>().apply {
                    postValue(vJournalItem()) }
            else
                database.get(vJournalItemId)

            setupDates()

        }
    }

    fun editingClicked() {
        editingClicked.value = true
    }



    private fun setupDates() {


        dateVisible = Transformations.map(vJournalItem) { item ->
            return@map item?.component == "JOURNAL"           // true if component == JOURNAL
        }

        timeVisible = Transformations.map(vJournalItem) { item ->
            if (item?.dtstart == 0L || item?.component != "JOURNAL" )
                return@map false

            val minute_formatter = SimpleDateFormat("mm")
            val hour_formatter = SimpleDateFormat("HH")

            if (minute_formatter.format(Date(item!!.dtstart)).toString() == "00" && hour_formatter.format(Date(item.dtstart)).toString() == "00")
                return@map false

            return@map true
        }



        dtstartFormatted = Transformations.map(vJournalItem) { _ ->
            val formattedDate = DateFormat.getDateInstance(DateFormat.LONG).format(Date(vJournalItem.value!!.dtstart))
            val formattedTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(vJournalItem.value!!.dtstart))
            return@map "$formattedDate $formattedTime"
        }

        createdFormatted = Transformations.map(vJournalItem) { _ ->
            vJournalItem.value?.let { Date(it.created).toString() }
        }

        lastModifiedFormatted = Transformations.map(vJournalItem) { _ ->
            vJournalItem.value?.let { Date(it.lastModified).toString() }
        }

    }
}



