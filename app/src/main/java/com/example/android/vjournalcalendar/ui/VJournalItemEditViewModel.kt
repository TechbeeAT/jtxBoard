package com.example.android.vjournalcalendar.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.android.vjournalcalendar.convertCategoriesListtoCSVString
import com.example.android.vjournalcalendar.database.VJournalDatabaseDao
import com.example.android.vjournalcalendar.database.vJournalItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class VJournalItemEditViewModel(    private val vJournalItemId: Long,
                                val database: VJournalDatabaseDao,
                                application: Application) : AndroidViewModel(application) {

    lateinit var vJournalItem: LiveData<vJournalItem?>
    lateinit var allCategories: LiveData<List<String>>

    var returnVJournalItemId: MutableLiveData<Long> = MutableLiveData<Long>().apply { postValue(0L) }
    var savingClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }
    var deleteClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }


    var summaryChanged: String = ""
    var descriptionChanged: String = ""
    var statusChanged: String = ""
    var classificationChanged: String = ""

    var urlChanged: String = ""
    var attendeeChanged: String = ""
    var contactChanged: String = ""
    var relatesChanged: String = ""


    var dtstartChangedYear: Int = 0
    var dtstartChangedMonth: Int = 0
    var dtstartChangedDay: Int = 0
    var dtstartChangedHour: Int = 0
    var dtstartChangedMinute: Int = 0

    var categoriesListChanged: MutableList<String> = mutableListOf()


    init {

        viewModelScope.launch() {

            // insert a new value to initialize the vJournalItem or load the existing one from the DB
            vJournalItem = if (vJournalItemId == 0L)
                MutableLiveData<vJournalItem?>().apply {
                    postValue(vJournalItem()) }
            else
                database.get(vJournalItemId)

            allCategories = database.getAllCategories()

        }
    }



    fun savingClicked() {
        savingClicked.value = true
    }

    fun deleteClicked() {
        deleteClicked.value = true
    }

    fun update() {
        viewModelScope.launch() {
            var vJournalItemUpdate = vJournalItem.value!!.copy()
            vJournalItemUpdate.summary = summaryChanged
            vJournalItemUpdate.description = descriptionChanged
            vJournalItemUpdate.url = urlChanged
            vJournalItemUpdate.attendee = attendeeChanged
            vJournalItemUpdate.contact = contactChanged
            vJournalItemUpdate.related = relatesChanged

            if(!statusChanged.isNullOrEmpty()) vJournalItemUpdate.status = statusChanged
            if(!classificationChanged.isNullOrEmpty()) vJournalItemUpdate.classification = classificationChanged

            vJournalItemUpdate.lastModified = System.currentTimeMillis()
            vJournalItemUpdate.dtstamp = System.currentTimeMillis()

            var c: Calendar = Calendar.getInstance()
            c.timeInMillis = vJournalItemUpdate.dtstart
            Log.println(Log.INFO, "VJournalItemEditViewMod", "Value before: ${c.timeInMillis}")
            if (dtstartChangedYear != 0)
                c.set(Calendar.YEAR, dtstartChangedYear)
            if (dtstartChangedMonth != 0)
                c.set(Calendar.MONTH, dtstartChangedMonth)
            if (dtstartChangedYear != 0)
                c.set(Calendar.DAY_OF_MONTH, dtstartChangedDay)
            if (dtstartChangedHour != 0)
                c.set(Calendar.HOUR_OF_DAY, dtstartChangedHour)
            if (dtstartChangedMinute != 0)
                c.set(Calendar.MINUTE, dtstartChangedMinute)
            Log.println(Log.INFO, "VJournalItemEditViewMod", "Value after: ${c.timeInMillis}")

            vJournalItemUpdate.dtstart = c.timeInMillis

            vJournalItemUpdate.categories = convertCategoriesListtoCSVString(categoriesListChanged)


            if (vJournalItemUpdate.id == 0L) {
                //Log.println(Log.INFO, "VJournalItemViewModel", "creating a new one")
                returnVJournalItemId.value = database.insert(vJournalItemUpdate)
                //Log.println(Log.INFO, "vJournalItemViewModel", vJournalItemUpdate.id.toString())
            }
            else {
                returnVJournalItemId.value = vJournalItemUpdate.id
                vJournalItemUpdate.sequence++
                database.update(vJournalItemUpdate)
            }
        }
    }

    fun delete () {
        viewModelScope.launch(Dispatchers.IO) {
            database.delete(vJournalItem.value!!)
        }
    }

}



