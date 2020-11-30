package com.example.android.vjournalcalendar.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.android.vjournalcalendar.database.VJournalDatabaseDao
import com.example.android.vjournalcalendar.database.vJournalItem
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*


class VJournalItemViewModel(    private val _vJournalItemId: Long,
                                val database: VJournalDatabaseDao,
                                application: Application) : AndroidViewModel(application) {

    lateinit var vJournalItem: LiveData<vJournalItem?>
    lateinit var dtstart_formatted: LiveData<String>
    var vJournalItemId = _vJournalItemId

    init {

        viewModelScope.launch {

            // insert a new value to initialize the vJournalItem, this needs to be deleted if the user cancels
            if (vJournalItemId == 0L) {
                val vJournalItemMutable: MutableLiveData<vJournalItem?> = MutableLiveData<vJournalItem?>().apply {
                    //postValue(vJournalItem(0L, "<summary>", "<description>", System.currentTimeMillis(), System.currentTimeMillis(), "", "", null, null, null, null))
                    postValue(vJournalItem())
                }
                vJournalItem = vJournalItemMutable
            } else {
                vJournalItem = database.get(vJournalItemId)
            }



            dtstart_formatted = Transformations.map(vJournalItem)  { item ->
                var formattedDate = DateFormat.getDateInstance(DateFormat.LONG).format(vJournalItem.value?.let { Date(it?.dtstart) })
                var formattedTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(vJournalItem.value?.let { Date(it.dtstart) })
                var formattedDateTime = "$formattedDate $formattedTime"
                formattedDateTime
            }
        }
    }
}



