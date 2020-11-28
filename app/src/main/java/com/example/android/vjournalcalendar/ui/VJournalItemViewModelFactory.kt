package com.example.android.vjournalcalendar.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.vjournalcalendar.database.VJournalDatabaseDao


class VJournalItemViewModelFactory (
        private val dataSource: VJournalDatabaseDao,
        private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VJournalItemViewModel::class.java)) {
            return VJournalItemViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

