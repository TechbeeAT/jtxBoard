package com.example.android.vjournalcalendar.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.example.android.vjournalcalendar.database.VJournalDatabaseDao
import com.example.android.vjournalcalendar.database.vJournalItem
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*


class VJournalItemViewModel(    private val vJournalItemId: Long,
                                val database: VJournalDatabaseDao,
                                application: Application) : AndroidViewModel(application) {

    lateinit var vJournalItem: LiveData<vJournalItem?>
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

            setFormattedDates()

        }
    }

    fun editingClicked() {
        editingClicked.value = true
    }



    private fun setFormattedDates() {
        dtstartFormatted = Transformations.map(vJournalItem)  { _ ->
            var formattedDate = DateFormat.getDateInstance(DateFormat.LONG).format(vJournalItem.value?.let { Date(it?.dtstart) })
            var formattedTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(vJournalItem.value?.let { Date(it.dtstart) })
            var formattedDateTime = "$formattedDate $formattedTime"
            formattedDateTime
        }

        createdFormatted = Transformations.map(vJournalItem)  { _ ->
            vJournalItem.value?.let { Date(it.created).toString() }
        }

        lastModifiedFormatted = Transformations.map(vJournalItem)  { _ ->
            vJournalItem.value?.let { Date(it.lastModified).toString() }
        }
    }


}



