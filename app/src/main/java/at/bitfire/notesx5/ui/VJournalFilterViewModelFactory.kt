package at.bitfire.notesx5.ui


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import at.bitfire.notesx5.database.VJournalDatabaseDao

class VJournalFilterViewModelFactory (
        private val dataSource: VJournalDatabaseDao,
        private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VJournalFilterViewModel::class.java)) {
            return VJournalFilterViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


