
package at.bitfire.notesx5.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import at.bitfire.notesx5.database.ICalDatabaseDao

class VJournalEditViewModelFactory (
        private val vJournalItemId: Long,
        private val dataSource: ICalDatabaseDao,
        private val application: Application) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VJournalEditViewModel::class.java)) {
                return VJournalEditViewModel(vJournalItemId, dataSource, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
}



