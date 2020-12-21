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


        var vJournalFocusItem: MutableLiveData<vJournalItem> = MutableLiveData<vJournalItem>().apply { vJournalItem()  }

        var filterArray = MutableLiveData<Array<String>>().apply {
            this.value = arrayOf("JOURNAL", "%", "%","%","%","%")
        }


        var vJournalList: LiveData<List<vJournalItem>> = Transformations.switchMap(filterArray) { filter ->
            /*if (filter[SEARCH_GLOBAL].isNullOrBlank() || filter[SEARCH_GLOBAL] == "%")
                database.getVJournalItems()
            else */
            //database.getVJournalItems("%${filter[SEARCH_GLOBAL].replace(" ", "%")}%")
            database.getVJournalItems(filter[SEARCH_COMPONENT], filter[SEARCH_GLOBAL])
            // Note: The tranformation could not handle multiple method calls in order to separate searches for
        }

        val allCategories: LiveData<List<String>> = database.getAllCategories()


    init {

        viewModelScope.launch {
            insertTestData()


/*
            val statusArray = application.applicationContext.resources.getStringArray(R.array.vjournal_status)
            when (vJournalItemViewModel.vJournalItem.value!!.status) {
                "DRAFT" -> binding.statusChip.text = statusArray[0]
                "FINAL" -> binding.statusChip.text = statusArray[1]
                "CANCELLED" -> binding.statusChip.text = statusArray[2]
                else -> binding.statusChip.text = vJournalItemViewModel.vJournalItem.value!!.status
            }

            val classificationArray = resources.getStringArray(R.array.vjournal_classification)
            when (vJournalItemViewModel.vJournalItem.value!!.classification) {
                "PUBLIC" -> binding.classificationChip.text = classificationArray[0]
                "PRIVATE" -> binding.classificationChip.text = classificationArray[1]
                "CONFIDENTIAL" -> binding.classificationChip.text = classificationArray[2]
                else -> binding.classificationChip.text = vJournalItemViewModel.vJournalItem.value!!.classification
            }

 */
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

        vJournalFocusItem.value = vJournalList.value?.find { focusItem ->
            focusItem.id == vJournalItemId
        }!!

    }

    fun getFocusItemPosition(): Int? {
        return vJournalList.value?.indexOf(vJournalFocusItem.value)
    }

    fun setFilter(field: Int, searchString: String) {
        filterArray.value?.set(field, searchString)
        filterArray.postValue(filterArray.value)      // Post the filterArray to notify observers for Transformation Switchmap
        //Log.println(Log.INFO, "array SearchGlobal", filterArray.value?.get(SEARCH_GLOBAL).toString())

    }

}
