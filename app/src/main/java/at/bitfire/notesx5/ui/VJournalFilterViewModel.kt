package at.bitfire.notesx5.ui


import android.app.Application
import androidx.lifecycle.*
import at.bitfire.notesx5.convertCategoriesCSVtoList
import at.bitfire.notesx5.database.VJournalDatabaseDao
import kotlinx.coroutines.launch
import java.util.*


class VJournalFilterViewModel(      val database: VJournalDatabaseDao,
                                    application: Application) : AndroidViewModel(application) {

    val allOrganizers = database.getAllOrganizers()
    val allCategories = database.getAllCategories()


    init {

        viewModelScope.launch() {

        }
    }
}

