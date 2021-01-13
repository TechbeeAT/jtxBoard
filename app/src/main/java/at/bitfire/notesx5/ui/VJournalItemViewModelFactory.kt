package at.bitfire.notesx5.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import at.bitfire.notesx5.database.ICalDatabaseDao


class VJournalItemViewModelFactory (
        private val vJournalItemId: Long,
        private val dataSource: ICalDatabaseDao,
        private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VJournalItemViewModel::class.java)) {
            return VJournalItemViewModel(vJournalItemId, dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

