package com.example.android.vjournalcalendar.ui


import android.app.Application
import android.text.Editable
import android.util.Log
import androidx.lifecycle.*
import com.example.android.vjournalcalendar.convertCategoriesCSVtoList
import com.example.android.vjournalcalendar.convertCategoriesListtoCSVString
import com.example.android.vjournalcalendar.database.VJournalDatabaseDao
import com.example.android.vjournalcalendar.database.vJournalItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class VJournalFilterViewModel(      val database: VJournalDatabaseDao,
                                    application: Application) : AndroidViewModel(application) {

    private val allCategoriesRaw = database.getAllCategories()
    val allOrganizers = database.getAllOrganizers()

    val allCategories = Transformations.map(allCategoriesRaw) { allCategoriesRaw ->
        val allDistinct: MutableList<String> = mutableListOf()
        allCategoriesRaw.forEach {
            allDistinct.addAll(convertCategoriesCSVtoList(it))
        }
        return@map allDistinct.distinct().sorted()
    }


    init {

        viewModelScope.launch() {

        }
    }
}

