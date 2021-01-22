
package at.bitfire.notesx5.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import at.bitfire.notesx5.database.ICalDatabaseDao
import at.bitfire.notesx5.database.relations.ICalEntity

class IcalEditViewModelFactory (
        private val iCalEntity: ICalEntity,
        private val dataSource: ICalDatabaseDao,
        private val application: Application) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(IcalEditViewModel::class.java)) {
                return IcalEditViewModel(iCalEntity, dataSource, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
}



