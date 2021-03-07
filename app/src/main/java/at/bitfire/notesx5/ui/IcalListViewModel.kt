package at.bitfire.notesx5.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.sqlite.db.SimpleSQLiteQuery
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.*
import at.bitfire.notesx5.database.relations.ICal4ListWithRelatedto
import at.bitfire.notesx5.database.views.ICal4List
import at.bitfire.notesx5.database.views.VIEW_NAME_ICAL4LIST

import kotlinx.coroutines.launch


class IcalListViewModel(
        val database: ICalDatabaseDao,
        application: Application) : AndroidViewModel(application) {


    var searchComponent: String = "JOURNAL"
    var searchText: String = ""
    var searchCategories: MutableList<String> = mutableListOf()
    var searchOrganizer: MutableList<String> = mutableListOf()
    var searchStatusJournal: MutableList<StatusJournal> = mutableListOf()
    var searchStatusTodo: MutableList<StatusTodo> = mutableListOf()
    var searchClassification: MutableList<Classification> = mutableListOf()
    var searchCollection: MutableList<String> = mutableListOf()


    private var listQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>().apply { postValue(constructQuery()) }
    var iCal4List: LiveData<List<ICal4ListWithRelatedto>> = Transformations.switchMap(listQuery) {
        database.getIcalObjectWithRelatedto(it)
    }


    // TODO maybe retrieve all subtasks only when subtasks are needed!
    val allSubtasks: LiveData<List<ICal4List?>> = database.getAllSubtasks()

    var focusItemId: MutableLiveData<Long> = MutableLiveData(0L)


    init {

        viewModelScope.launch {

            allSubtasks.apply { database.getAllSubtasks() }

            for (i in 1..2) {
                insertTestData()

            }
        }
    }


    private suspend fun insertTestData() {

/*
        val lipsumSummary = "Lorem ipsum dolor sit amet"
        val lipsumDescription = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."
 */
        val rfcSummary = "Staff meeting minutes"
        val rfcDesc = "1. Staff meeting: Participants include Joe, Lisa, and Bob. Aurora project plans were reviewed. There is currently no budget reserves for this project. Lisa will escalate to management. Next meeting on Tuesday.\n\n" +
                "2. Telephone Conference: ABC Corp. sales representative called to discuss new printer. Promised to get us a demo by Friday.\n\n" +
                "3. Henry Miller (Handsoff Insurance): Car was totaled by tree. Is looking into a loaner car. 555-2323 (tel)."

        /*
        val jSummary = "Project JF"
        val jDesc = "1. Steering PPT presented and discussed\n\n" +
                "2. Testphase will be initiated on Thursday\n\n" +
                "3. Management presentation in progress"


         */
        val noteSummary = "Notes for the next JF"
        val noteDesc = "Get a proper pen\nOffer free coffee for everyone"
/*
        val noteSummary2 = "Shopping list"
        val noteDesc2 = "Present for Tom\nProper Pen\nCoffee"


 */

        //database.insert(vJournalItem(0L, lipsumSummary, lipsumDescription, System.currentTimeMillis(), "Organizer",  "#category1, #category2", "FINAL","PUBLIC", "", "uid", System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), 0))
        //database.insert(vJournalItem(0L, lipsumSummary, lipsumDescription, System.currentTimeMillis(), "Organizer",  "#category1, #category2", "FINAL","PUBLIC", "", "uid", System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), 0))
        //database.insert(vJournalItem(summary=lipsumSummary, description=lipsumDescription, organizer="Organizer", categories="JourFixe, BestProject"))

        //onConflict strategy = IGNORE!
        database.upsertCollection(ICalCollection(collectionId = 1L, url = "https://localhost", displayName = "Local Collection"))

        val newEntry = database.insertICalObject(ICalObject(collectionId = 1L, component = Component.JOURNAL.name, summary = rfcSummary, description = rfcDesc, dtstart = System.currentTimeMillis()))
        database.insertAttendee(Attendee(caladdress = "test@test.de", icalObjectId = newEntry))
        database.insertCategory(Category(text = "cat", icalObjectId = newEntry))
        database.insertCategory(Category(text = "cat", icalObjectId = newEntry))

        database.insertComment(Comment(text = "comment", icalObjectId = newEntry))
        database.insertOrganizer(Organizer(caladdress = "organizer", icalObjectId = newEntry))
        //database.insertRelatedto(Relatedto(text = "related to", icalObjectId = newEntry))


        //database.insert(vJournalItem(component="JOURNAL", summary=jSummary, description=jDesc, organizer="LOCAL", categories="Appointment, Education"))

        //database.insert(vJournalItem(component="NOTE", dtstart=0L, summary=noteSummary, description=noteDesc, organizer="LOCAL", categories="JourFixe, BestProject"))
        //database.insert(vJournalItem(component="NOTE", dtstart=0L, summary=noteSummary2, description=noteDesc2, organizer="LOCAL", categories="Shopping"))

        val newEntry2 = database.insertICalObject(ICalObject(collectionId = 1L, component = Component.NOTE.name, summary = noteSummary, description = noteDesc))
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

        val focusItem = iCal4List.value?.find {
            focusItemId.value == it.property.id
        }

        return if (iCal4List.value != null && focusItem != null)
            iCal4List.value!!.indexOf(focusItem)
        else
            -1
    }

    fun resetFocusItem() {
        focusItemId.value = 0L
    }

    private fun constructQuery(): SimpleSQLiteQuery {

        val args = arrayListOf<String>()

// Beginning of query string
        var queryString = "SELECT DISTINCT $VIEW_NAME_ICAL4LIST.* FROM $VIEW_NAME_ICAL4LIST " +
                "LEFT JOIN $TABLE_NAME_CATEGORY ON $VIEW_NAME_ICAL4LIST.$COLUMN_ID = $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ICALOBJECT_ID " +
                "LEFT JOIN $TABLE_NAME_COLLECTION ON $VIEW_NAME_ICAL4LIST.$COLUMN_ICALOBJECT_COLLECTIONID = $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ID "  // +
        //     "LEFT JOIN vattendees ON icalobject._id = vattendees.icalObjectId " +
        //     "LEFT JOIN vcomments ON icalobject._id = vcomments.icalObjectId " +
        //     "LEFT JOIN vorganizer ON icalobject._id = vorganizer.icalObjectId " +
        //     "LEFT JOIN vRelatedto ON icalobject._id = vRelatedto.icalObjectId "

        // First query parameter Component must always be present!
        queryString += "WHERE $COLUMN_COMPONENT = ? "
        args.add(searchComponent)

        // Query for the given text search from the action bar
        if (searchText.isNotEmpty() && searchText.length >= 2) {
            queryString += "AND ($COLUMN_SUMMARY LIKE ? OR $COLUMN_DESCRIPTION LIKE ?) "
            args.add(searchText)
            args.add(searchText)
        }

        // Query for the passed filter criteria from VJournalFilterFragment
        if (searchCategories.size > 0) {
            queryString += "AND $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_TEXT IN ("
            searchCategories.forEach {
                queryString += "?,"
                args.add(it)
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Query for the passed filter criteria from FilterFragment
        if (searchStatusJournal.size > 0 && (searchComponent == Component.JOURNAL.name || searchComponent == Component.JOURNAL.name)) {
            queryString += "AND $COLUMN_STATUS IN ("
            searchStatusJournal.forEach {
                queryString += "?,"
                args.add(it.toString())
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Query for the passed filter criteria from FilterFragment
        if (searchStatusTodo.size > 0 && searchComponent == Component.TODO.name) {
            queryString += "AND $COLUMN_STATUS IN ("
            searchStatusTodo.forEach {
                queryString += "?,"
                args.add(it.toString())
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Query for the passed filter criteria from FilterFragment
        if (searchClassification.size > 0) {
            queryString += "AND $COLUMN_CLASSIFICATION IN ("
            searchClassification.forEach {
                queryString += "?,"
                args.add(it.toString())
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }


        // Query for the passed filter criteria from FilterFragment
        if (searchCollection.size > 0) {
            queryString += "AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_DISPLAYNAME IN ("
            searchCollection.forEach {
                queryString += "?,"
                args.add(it)
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Exclude items that are Child items by checking if they appear in the linkedICalObjectId of relatedto!
        queryString += "AND $VIEW_NAME_ICAL4LIST.$COLUMN_ID NOT IN (SELECT $COLUMN_RELATEDTO_LINKEDICALOBJECT_ID FROM $TABLE_NAME_RELATEDTO) "

        queryString += "ORDER BY $COLUMN_DTSTART DESC, $COLUMN_CREATED DESC "

        Log.println(Log.INFO, "queryString", queryString)
        Log.println(Log.INFO, "queryStringArgs", args.joinToString(separator = ", "))


        return SimpleSQLiteQuery(queryString, args.toArray())
    }

    fun updateSearch() {
        listQuery.postValue(constructQuery())
    }

    fun clearFilter() {
        searchCategories.clear()
        searchOrganizer.clear()
        searchStatusJournal.clear()
        searchStatusTodo.clear()
        searchClassification.clear()
        searchCollection.clear()
        updateSearch()
    }


    fun updateProgress(itemId: Long, newPercent: Int) {

        val newStatus = when (newPercent) {
            100 -> StatusTodo.COMPLETED.param
            in 1..99 -> StatusTodo.INPROCESS.param
            0 -> StatusTodo.NEEDSACTION.param
            else -> StatusTodo.NEEDSACTION.param      // should never happen!
        }

        viewModelScope.launch() {
               database.updateProgress(itemId, newPercent, newStatus, System.currentTimeMillis())
        }
    }
}
