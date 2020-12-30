package at.bitfire.notesx5.ui

import android.app.Application
import androidx.lifecycle.*
import at.bitfire.notesx5.database.*

import kotlinx.coroutines.launch


/**
 * ViewModel for SleepTrackerFragment.
 */
class VJournalListViewModel(
        val database: VJournalDatabaseDao,
        application: Application) : AndroidViewModel(application) {

        val SEARCH_COMPONENT = 0
        val SEARCH_GLOBAL = 1
        val SEARCH_CATEGORIES = 2
        val SEARCH_ORGANIZER = 3
        val SEARCH_STATUS = 4
        val SEARCH_CLASSIFICATION = 5


        //var vJournalFocusItem: MutableLiveData<vJournalItem> = MutableLiveData<vJournalItem>().apply { vJournalItem()  }
        var focusItemId: MutableLiveData<Long> = MutableLiveData(0L)

        var filterArray = MutableLiveData<Array<Array<String>>>().apply {
            //this.value = arrayOf("JOURNAL", "%", "%","%","%","%")
            this.value = arrayOf(arrayOf("JOURNAL"), arrayOf("%"), arrayOf("%"), arrayOf("%"), arrayOf("%"), arrayOf("%"))
        }


    var vJournalList: LiveData<List<VJournalWithEverything>> = database.getVJournalItemWithEverything()

    /*
        var vJournalList: LiveData<List<VJournalItem>> = Transformations.switchMap(filterArray) { filter ->
            database.getVJournalItems(filter[SEARCH_COMPONENT], filter[SEARCH_GLOBAL][0], filter[SEARCH_CATEGORIES], filter[SEARCH_ORGANIZER], filter[SEARCH_STATUS], filter[SEARCH_CLASSIFICATION])
        }

     */

    /*
        var vJournalListFiltered: LiveData<List<VJournalItem>> = Transformations.map(vJournalList) {
            return@map it
        }
*/
        // var vJournalListWithEverything = database.getVJournalItemWithEverything()



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

        var newEntry = database.insert(VJournal(component="JOURNAL", summary=rfcSummary, description=rfcDesc))
        database.insertAttendee(VAttendee(attendee="test@test.de", journalLinkId = newEntry))
        database.insertCategory(VCategory(categories = "cat", journalLinkId = newEntry))
        database.insertCategory(VCategory(categories = "cat", journalLinkId = newEntry))

        database.insertComment(VComment(comment = "comment", journalLinkId = newEntry))
        database.insertOrganizer(VOrganizer(organizer = "organizer", journalLinkId = newEntry))
        database.insertRelatedto(VRelatedto(relatedto = "related to", journalLinkId = newEntry))


        //database.insert(vJournalItem(component="JOURNAL", summary=jSummary, description=jDesc, organizer="LOCAL", categories="Appointment, Education"))

        //database.insert(vJournalItem(component="NOTE", dtstart=0L, summary=noteSummary, description=noteDesc, organizer="LOCAL", categories="JourFixe, BestProject"))
        //database.insert(vJournalItem(component="NOTE", dtstart=0L, summary=noteSummary2, description=noteDesc2, organizer="LOCAL", categories="Shopping"))


    }



    fun setFocusItem(vJournalItemId: Long) {
        focusItemId.value = vJournalItemId
    }

    fun getFocusItemPosition(): Int {

        val focusItem = vJournalList.value?.find {
            focusItemId.value == it.vJournalItem.id
        }

        return if(vJournalList.value != null && focusItem != null)
            vJournalList.value!!.indexOf(focusItem)
        else
            -1
    }

    fun resetFocusItem() {
        focusItemId.value = 0L
    }

    fun setFilter(field: Int, searchString: Array<String>) {
        filterArray.value?.set(field, searchString)
        filterArray.postValue(filterArray.value)      // Post the filterArray to notify observers for Transformation Switchmap
        //Log.println(Log.INFO, "array SearchGlobal", filterArray.value?.get(SEARCH_GLOBAL).toString())

    }

}
