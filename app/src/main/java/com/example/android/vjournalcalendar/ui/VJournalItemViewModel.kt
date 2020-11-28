package com.example.android.vjournalcalendar.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.vjournalcalendar.database.VJournalDatabaseDao
import com.example.android.vjournalcalendar.database.vJournalItem
import kotlinx.coroutines.launch


class VJournalItemViewModel(


        val database: VJournalDatabaseDao, application: Application) : AndroidViewModel(application) {

    lateinit var vJournalItem: vJournalItem

    init {

        viewModelScope.launch {
            var vjournalItem: vJournalItem? = database.get(1)

        }

    }


}
