package com.example.android.vjournalcalendar.ui

import android.app.Application
import androidx.lifecycle.*

import com.example.android.vjournalcalendar.database.VJournalDatabaseDao
import com.example.android.vjournalcalendar.database.vJournalItem
import kotlinx.coroutines.launch


/**
 * ViewModel for SleepTrackerFragment.
 */
class VJournalListViewModel(
        val database: VJournalDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    var vjournal_item = MutableLiveData<vJournalItem?>()


    init {
        initializeVJournalList()
        vjournal_item.value = vJournalItem(1, "desc", "comm")


    }



    private fun initializeVJournalList() {
        viewModelScope.launch {
            var vJournalItemTest = vJournalItem(1, "TestDescription", "TestComment")
            database.insert(vJournalItemTest)
            //vjournal_item = database.getTestVJournalFromDatabase()

        }
    }



    /**
     *  Handling the case of the stopped app or forgotten recording,
     *  the start and end times will be the same.j
     *
     *  If the start time and end time are not the same, then we do not have an unfinished
     *  recording.
     */

    /*

    private suspend fun getTonightFromDatabase(): SleepNight? {
        var night = database.getTonight()
        if (night?.endTimeMilli != night?.startTimeMilli) {
            night = null
        }
        return night
    }

    private suspend fun clear() {
        database.clear()
    }

    private suspend fun update(night: SleepNight) {
        database.update(night)
    }

    private suspend fun insert(night: SleepNight) {
        database.insert(night)
    }
*/


}
