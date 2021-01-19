package at.bitfire.notesx5.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.sqlite.db.SimpleSQLiteQuery
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.*
import at.bitfire.notesx5.database.relations.ICalEntityWithCategory

import kotlinx.coroutines.launch



class VJournalListViewModel(
        val database: ICalDatabaseDao,
        application: Application) : AndroidViewModel(application) {


    var searchComponent = "JOURNAL"
    var searchText: String = ""
    var searchCategories: MutableList<String> = mutableListOf()
    var searchOrganizer: MutableList<String> = mutableListOf()
    var searchStatus: MutableList<Int> = mutableListOf()
    var searchClassification: MutableList<Int> = mutableListOf()
    var searchCollection: MutableList<String> = mutableListOf()


    private var listQuery:  MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>().apply { postValue(constructQuery()) }
    var vJournalList: LiveData<List<ICalEntityWithCategory>> = Transformations.switchMap(listQuery) {
        database.getVJournalWithCategory(it)
        }

    //var vJournalFocusItem: MutableLiveData<vJournalItem> = MutableLiveData<vJournalItem>().apply { vJournalItem()  }
    var focusItemId: MutableLiveData<Long> = MutableLiveData(0L)



    init {

        viewModelScope.launch {
            insertTestData()

        }

    }


    private suspend fun insertTestData() {

        val lipsumSummary = "Lorem ipsum dolor sit amet"
        val lipsumDescription = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."

        val rfcSummary = "Staff meeting minutes"
        val rfcDesc = "1. Staff meeting: Participants include Joe, Lisa, and Bob. Aurora project plans were reviewed. There is currently no budget reserves for this project. Lisa will escalate to management. Next meeting on Tuesday.\n\n" +
                "2. Telephone Conference: ABC Corp. sales representative called to discuss new printer. Promised to get us a demo by Friday.\n\n" +
                "3. Henry Miller (Handsoff Insurance): Car was totaled by tree. Is looking into a loaner car. 555-2323 (tel)."

        val jSummary = "Project JF"
        val jDesc = "1. Steering PPT presented and discussed\n\n" +
                "2. Testphase will be initiated on Thursday\n\n" +
                "3. Management presentation in progress"

        val noteSummary = "Notes for the next JF"
        val noteDesc = "Get a proper pen\nOffer free coffee for everyone"

        val noteSummary2 = "Shopping list"
        val noteDesc2 = "Present for Tom\nProper Pen\nCoffee"


        //database.insert(vJournalItem(0L, lipsumSummary, lipsumDescription, System.currentTimeMillis(), "Organizer",  "#category1, #category2", "FINAL","PUBLIC", "", "uid", System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), 0))
        //database.insert(vJournalItem(0L, lipsumSummary, lipsumDescription, System.currentTimeMillis(), "Organizer",  "#category1, #category2", "FINAL","PUBLIC", "", "uid", System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), 0))
        //database.insert(vJournalItem(summary=lipsumSummary, description=lipsumDescription, organizer="Organizer", categories="JourFixe, BestProject"))

        val newEntry = database.insertJournal(ICalObject(component = "JOURNAL", summary = rfcSummary, description = rfcDesc, dtstart = System.currentTimeMillis()))
        database.insertAttendee(Attendee(caladdress = "test@test.de", icalObjectId = newEntry))
        database.insertCategory(Category(text = "cat", icalObjectId = newEntry))
        database.insertCategory(Category(text = "cat", icalObjectId = newEntry))

        database.insertComment(Comment(text = "comment", icalObjectId = newEntry))
        database.insertOrganizer(Organizer(caladdress = "organizer", icalObjectId = newEntry))
        //database.insertRelatedto(Relatedto(text = "related to", icalObjectId = newEntry))


        //database.insert(vJournalItem(component="JOURNAL", summary=jSummary, description=jDesc, organizer="LOCAL", categories="Appointment, Education"))

        //database.insert(vJournalItem(component="NOTE", dtstart=0L, summary=noteSummary, description=noteDesc, organizer="LOCAL", categories="JourFixe, BestProject"))
        //database.insert(vJournalItem(component="NOTE", dtstart=0L, summary=noteSummary2, description=noteDesc2, organizer="LOCAL", categories="Shopping"))

        val newEntry2 = database.insertJournal(ICalObject(component = "NOTE", summary = noteSummary, description = noteDesc))
        database.insertAttendee(Attendee(caladdress = "test@test.de", icalObjectId = newEntry2))
        database.insertCategory(Category(text = "cat", icalObjectId = newEntry2))
        database.insertCategory(Category(text = "cat", icalObjectId = newEntry2))

        database.insertComment(Comment(text = "comment", icalObjectId = newEntry2))
        database.insertOrganizer(Organizer(caladdress = "organizer", icalObjectId = newEntry2))
       // database.insertRelatedto(Relatedto(text = "related to", icalObjectId = newEntry2))

    }


    fun setFocusItem(vJournalItemId: Long) {
        focusItemId.value = vJournalItemId
    }

    fun getFocusItemPosition(): Int {

        val focusItem = vJournalList.value?.find {
            focusItemId.value == it.vJournal.id
        }

        return if (vJournalList.value != null && focusItem != null)
            vJournalList.value!!.indexOf(focusItem)
        else
            -1
    }

    fun resetFocusItem() {
        focusItemId.value = 0L
    }

    fun constructQuery(): SimpleSQLiteQuery  {

        val args = arrayListOf<String>()

// Beginning of query string
        var queryString = "SELECT DISTINCT icalobject.* FROM icalobject " +
        "LEFT JOIN category ON icalobject.id = category.icalObjectId "  // +
        //     "LEFT JOIN vattendees ON icalobject.id = vattendees.icalObjectId " +
        //     "LEFT JOIN vcomments ON icalobject.id = vcomments.icalObjectId " +
        //     "LEFT JOIN vorganizer ON icalobject.id = vorganizer.icalObjectId " +
        //     "LEFT JOIN vRelatedto ON icalobject.id = vRelatedto.icalObjectId "

        // First query parameter Component must always be present!
        queryString += "WHERE component = ? "
        args.add(searchComponent)

        // Query for the given text search from the action bar
        if (searchText.isNotEmpty() && searchText.length >= 2) {
            queryString += "AND (summary LIKE ? OR description LIKE ?) "
            args.add(searchText)
            args.add(searchText)
        }

        // Query for the passed filter criteria from VJournalFilterFragment
        if (searchCategories.size > 0) {
            queryString += "AND vcategories.text IN ("
            searchCategories.forEach {
                queryString += "?,"
                args.add(it)
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Query for the passed filter criteria from VJournalFilterFragment
        if (searchStatus.size > 0) {
            queryString += "AND status IN ("
            searchStatus.forEach {
                queryString += "?,"
                args.add(it.toString())
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Query for the passed filter criteria from VJournalFilterFragment
        if (searchClassification.size > 0) {
            queryString += "AND classification IN ("
            searchClassification.forEach {
                queryString += "?,"
                args.add(it.toString())
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Query for the passed filter criteria from VJournalFilterFragment
        if (searchCollection.size > 0) {
            queryString += "AND collection IN ("
            searchCollection.forEach {
                queryString += "?,"
                args.add(it)
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Exclude items that are Child items by checking if they appear in the linkedICalObjectId of relatedto!
        queryString += "AND icalobject.id NOT IN (SELECT linkedICalObjectId FROM relatedto) "

        queryString += "ORDER BY dtstart DESC, created DESC "

        Log.println(Log.INFO, "queryString", queryString)
        Log.println(Log.INFO, "queryStringArgs", args.joinToString(separator=", "))


        return SimpleSQLiteQuery(queryString, args.toArray())
    }

    fun updateSearch() {
        listQuery.postValue(constructQuery())
    }

    fun clearFilter() {
       searchCategories.clear()
       searchOrganizer.clear()
       searchStatus.clear()
       searchClassification.clear()
       searchCollection.clear()
       updateSearch()
    }
}
