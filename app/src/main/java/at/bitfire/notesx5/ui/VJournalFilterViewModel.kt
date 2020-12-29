package at.bitfire.notesx5.ui


import android.app.Application
import androidx.lifecycle.*
import at.bitfire.notesx5.convertCategoriesCSVtoList
import at.bitfire.notesx5.database.VJournalDatabaseDao
import kotlinx.coroutines.launch
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

