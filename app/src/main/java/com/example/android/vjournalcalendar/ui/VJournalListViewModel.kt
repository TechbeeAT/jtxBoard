package com.example.android.vjournalcalendar.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.android.vjournalcalendar.R

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

        val SEARCH_COMPONENT = 0
        val SEARCH_GLOBAL = 1
        val SEARCH_CATEGORIES = 2
        val SEARCH_ORGANIZER = 3
        val SEARCH_STATUS = 4
        val SEARCH_CLASSIFICATION = 5


        //var vJournalFocusItem: MutableLiveData<vJournalItem> = MutableLiveData<vJournalItem>().apply { vJournalItem()  }
        var focusItemId: MutableLiveData<Long> = MutableLiveData(0L)

        var filterArray = MutableLiveData<Array<String>>().apply {
            this.value = arrayOf("JOURNAL", "%", "%","%","%","%")
        }


        var vJournalList: LiveData<List<vJournalItem>> = Transformations.switchMap(filterArray) { filter ->
            database.getVJournalItems(filter[SEARCH_COMPONENT], filter[SEARCH_GLOBAL], filter[SEARCH_CATEGORIES], filter[SEARCH_ORGANIZER], filter[SEARCH_STATUS], filter[SEARCH_CLASSIFICATION])
        }

        val allCategories: LiveData<List<String>> = database.getAllCategories()


    init {

        viewModelScope.launch {
            insertTestData()

        }

    }



    private suspend fun insertTestData() {

        val lipsumSummary = "Lorem ipsum dolor sit amet"
        val lipsumDescription = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."


        //database.insert(vJournalItem(0L, lipsumSummary, lipsumDescription, System.currentTimeMillis(), "Organizer",  "#category1, #category2", "FINAL","PUBLIC", "", "uid", System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), 0))
        //database.insert(vJournalItem(0L, lipsumSummary, lipsumDescription, System.currentTimeMillis(), "Organizer",  "#category1, #category2", "FINAL","PUBLIC", "", "uid", System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), 0))
        database.insert(vJournalItem(summary=lipsumSummary, description=lipsumDescription, organizer="Organizer", categories="JourFixe, BestProject"))
        database.insert(vJournalItem(component="NOTE", dtstart=0L, summary=lipsumSummary, description=lipsumDescription, organizer="Organizer", categories="JourFixe, BestProject"))

    }



    fun setFocusItem(vJournalItemId: Long) {
        focusItemId.value = vJournalItemId
    }

    fun getFocusItemPosition(): Int {

        val focusItem = vJournalList.value?.find {
            focusItemId.value == it.id
        }

        return if(vJournalList.value != null && focusItem != null)
            vJournalList.value!!.indexOf(focusItem)
        else
            -1
    }

    fun resetFocusItem() {
        focusItemId.value = 0L
    }

    fun setFilter(field: Int, searchString: String) {
        filterArray.value?.set(field, searchString)
        filterArray.postValue(filterArray.value)      // Post the filterArray to notify observers for Transformation Switchmap
        //Log.println(Log.INFO, "array SearchGlobal", filterArray.value?.get(SEARCH_GLOBAL).toString())

    }

}
