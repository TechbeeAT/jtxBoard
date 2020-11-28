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
        lateinit var vjournaListCount: LiveData<Int>




    init {

        viewModelScope.launch {
            vjournaListCount = database.getAllVJournalItemsCount()
            insertTestData()

        }
    }



    suspend fun insertTestData() {
        database.insert(vJournalItem(0L, "desc1", System.currentTimeMillis(), System.currentTimeMillis(),"organizer1", "uid1"))
        database.insert(vJournalItem(0L, "desc2", System.currentTimeMillis(), System.currentTimeMillis(),"organizer1", "uid1"))
        database.insert(vJournalItem(0L, "desc3", System.currentTimeMillis(), System.currentTimeMillis(),"organizer1", "uid1"))
        database.insert(vJournalItem(0L, "desc4", System.currentTimeMillis(), System.currentTimeMillis(),"organizer1", "uid1"))
        database.insert(vJournalItem(0L, "desc5", System.currentTimeMillis(), System.currentTimeMillis(),"organizer1", "uid1"))
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
