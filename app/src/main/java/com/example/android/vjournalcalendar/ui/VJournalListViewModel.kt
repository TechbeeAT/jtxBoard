package com.example.android.vjournalcalendar.ui

import android.app.Application
import androidx.lifecycle.*

import com.example.android.vjournalcalendar.database.VJournalDatabaseDao
import com.example.android.vjournalcalendar.database.vJournalItem
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


/**
 * ViewModel for SleepTrackerFragment.
 */
class VJournalListViewModel(
        val database: VJournalDatabaseDao,
        application: Application) : AndroidViewModel(application) {

        var vjournalList: LiveData<List<vJournalItem>> = database.getAllVJournalItems()
        var vjournaListCount: Int = vjournalList.value?.count() ?: 0


    init {

        viewModelScope.launch {
            insertTestData()
        }
    }



    suspend fun insertTestData() {
        database.insert(vJournalItem(0L, "desc1", System.currentTimeMillis(), "comment"))
        database.insert(vJournalItem(0L, "desc2", System.currentTimeMillis(), "comment"))
        database.insert(vJournalItem(0L, "desc3", System.currentTimeMillis(), "comment"))
        database.insert(vJournalItem(0L, "desc4", System.currentTimeMillis(), "comment"))
        database.insert(vJournalItem(0L, "desc5", System.currentTimeMillis(), "comment"))
        database.insert(vJournalItem(0L, "desc6", System.currentTimeMillis(), "comment"))
        database.insert(vJournalItem(0L, "desc7", System.currentTimeMillis(), "comment"))
        database.insert(vJournalItem(0L, "desc8", System.currentTimeMillis(), "comment"))
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
