package at.bitfire.notesx5.ui


import android.app.Application
import androidx.lifecycle.*
import at.bitfire.notesx5.database.ICalDatabaseDao
import kotlinx.coroutines.launch


class IcalFilterViewModel(val database: ICalDatabaseDao,
                          application: Application) : AndroidViewModel(application) {

    val allCollections = database.getAllCollections()
    val allCategories = database.getAllCategories()


    init {

        viewModelScope.launch() {

        }
    }
}

