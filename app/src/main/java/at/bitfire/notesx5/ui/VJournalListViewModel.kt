package at.bitfire.notesx5.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import at.bitfire.notesx5.database.*

import kotlinx.coroutines.launch


/**
 * ViewModel for SleepTrackerFragment.
 */
class VJournalListViewModel(
        val database: VJournalDatabaseDao,
        application: Application) : AndroidViewModel(application) {


    var searchComponent = MutableLiveData<List<String>>(listOf("JOURNAL"))
    var searchGlobal = MutableLiveData<List<String>>(listOf("%"))
    var searchCategories = MutableLiveData<List<String>>()
    var searchOrganizer = MutableLiveData<List<String>>()
    var searchStatus = MutableLiveData<List<String>>()
    var searchClassification = MutableLiveData<List<String>>()

    private val search = MediatorLiveData<List<String>>()


    //var vJournalFocusItem: MutableLiveData<vJournalItem> = MutableLiveData<vJournalItem>().apply { vJournalItem()  }
    var focusItemId: MutableLiveData<Long> = MutableLiveData(0L)


    var vJournalList: LiveData<List<VJournalWithEverything>> = Transformations.switchMap(search) {

        // search only globally and filter the right component if no further filter criteria was passed
        if (searchCategories.value.isNullOrEmpty() || searchOrganizer.value.isNullOrEmpty() || searchStatus.value.isNullOrEmpty() || searchStatus.value.isNullOrEmpty() )
            database.getVJournalItemWithEverything(searchComponent.value!!, "%${searchGlobal.value?.joinToString(separator = "%")}%")

        // apply all filter criteria. ALL filter criteria must be passed!
        else if (!searchCategories.value.isNullOrEmpty() && !searchOrganizer.value.isNullOrEmpty() && !searchStatus.value.isNullOrEmpty() && !searchStatus.value.isNullOrEmpty())
            database.getVJournalItemWithEverything(searchComponent.value!!, "%")

        else
            database.getVJournalItemWithEverything(listOf("JOURNAL"), "%")     // Hardcoded fallback
    }


    init {

        search.addSource(searchComponent) { component -> search.value = component }
        search.addSource(searchGlobal) { global -> search.value = global }
        search.addSource(searchCategories) { categories -> search.value = categories }
        search.addSource(searchOrganizer) { organizer -> search.value = organizer }
        search.addSource(searchStatus) { status -> search.value = status }
        search.addSource(searchClassification) { classification -> search.value = classification }

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

        val newEntry = database.insert(VJournal(component = "JOURNAL", summary = rfcSummary, description = rfcDesc))
        database.insertAttendee(VAttendee(attendee = "test@test.de", journalLinkId = newEntry))
        database.insertCategory(VCategory(categories = "cat", journalLinkId = newEntry))
        database.insertCategory(VCategory(categories = "cat", journalLinkId = newEntry))

        database.insertComment(VComment(comment = "comment", journalLinkId = newEntry))
        database.insertOrganizer(VOrganizer(organizer = "organizer", journalLinkId = newEntry))
        database.insertRelatedto(VRelatedto(relatedto = "related to", journalLinkId = newEntry))


        //database.insert(vJournalItem(component="JOURNAL", summary=jSummary, description=jDesc, organizer="LOCAL", categories="Appointment, Education"))

        //database.insert(vJournalItem(component="NOTE", dtstart=0L, summary=noteSummary, description=noteDesc, organizer="LOCAL", categories="JourFixe, BestProject"))
        //database.insert(vJournalItem(component="NOTE", dtstart=0L, summary=noteSummary2, description=noteDesc2, organizer="LOCAL", categories="Shopping"))

        val newEntry2 = database.insert(VJournal(component = "NOTE", summary = noteSummary, description = noteDesc))
        database.insertAttendee(VAttendee(attendee = "test@test.de", journalLinkId = newEntry2))
        database.insertCategory(VCategory(categories = "cat", journalLinkId = newEntry2))
        database.insertCategory(VCategory(categories = "cat", journalLinkId = newEntry2))

        database.insertComment(VComment(comment = "comment", journalLinkId = newEntry2))
        database.insertOrganizer(VOrganizer(organizer = "organizer", journalLinkId = newEntry2))
        database.insertRelatedto(VRelatedto(relatedto = "related to", journalLinkId = newEntry2))

    }


    fun setFocusItem(vJournalItemId: Long) {
        focusItemId.value = vJournalItemId
    }

    fun getFocusItemPosition(): Int {

        val focusItem = vJournalList.value?.find {
            focusItemId.value == it.vJournalItem.id
        }

        return if (vJournalList.value != null && focusItem != null)
            vJournalList.value!!.indexOf(focusItem)
        else
            -1
    }

    fun resetFocusItem() {
        focusItemId.value = 0L
    }


}
