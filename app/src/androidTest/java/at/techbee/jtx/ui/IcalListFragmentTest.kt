package at.techbee.jtx.ui

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import at.techbee.jtx.*
import at.techbee.jtx.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@LargeTest
class IcalListFragmentTest {


    private lateinit var database: ICalDatabaseDao
    private lateinit var context: Context
    private lateinit var application: Application

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    private val sampleDate = 1632636027826L     //  Sun Sep 26 2021 06:00:27
    private val sampleCollection = ICalCollection(collectionId = 1L, displayName = "testcollection automated tests")
    private val sampleJournal = ICalObject(collectionId = 1L, module = Module.JOURNAL.name, component = Component.VJOURNAL.name, summary = "Journal4Test", description = "Description4JournalTest", dtstart = sampleDate, status = StatusJournal.FINAL.name, classification = Classification.PUBLIC.name)
    private val sampleNote = ICalObject(collectionId = 1L, module = Module.NOTE.name, component = Component.VJOURNAL.name, summary = "Note4Test", description = "Description4NoteTest", dtstart = sampleDate)
    private val sampleTodo = ICalObject(collectionId = 1L, module = Module.TODO.name, component = Component.VTODO.name, summary = "Todo4Test", description = "Description4TodoTest", dtstart = sampleDate)

    /*
    database.insertAttendee(Attendee(caladdress = "${UUID.randomUUID()}@test.de", icalObjectId = newEntry))
    database.insertCategory(Category(text = UUID.randomUUID().toString(), icalObjectId = newEntry))
    database.insertCategory(Category(text = "cat", icalObjectId = newEntry))

    database.insertComment(Comment(text = "comment", icalObjectId = newEntry))
    database.insertOrganizer(Organizer(caladdress = "organizer", icalObjectId = newEntry))
     */


    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ICalDatabase.switchToInMemory(context)
        database = ICalDatabase.getInstance(context).iCalDatabaseDao     // should be in-memory db now
        application =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application


        //insert sample entries
        testScope.launch(TestCoroutineDispatcher()) {
            database.insertCollectionSync(sampleCollection)
            database.insertICalObjectSync(sampleJournal)
            database.insertICalObjectSync(sampleNote)
            database.insertICalObjectSync(sampleTodo)

            //ICalDatabase.getInstance(context).populateTestData()
        }
    }


    @After
    fun closeDb() {
        ICalDatabase.getInMemoryDB(context).close()
    }


    @Test
    fun journal_is_displayed()  {

        //val fragmentArgs = Bundle()
        val scenario = launchFragmentInContainer<IcalListFragment>(Bundle(), R.style.AppTheme, Lifecycle.State.RESUMED)
        onView(withText(R.string.list_tabitem_journals)).perform(click())
        onView(withText(sampleJournal.summary)).check(matches(isDisplayed()))
        onView(withText(sampleJournal.description)).check(matches(isDisplayed()))
        onView(withText(convertLongToDayString(sampleJournal.dtstart))).check(matches(isDisplayed()))
        onView(withText(convertLongToMonthString(sampleJournal.dtstart))).check(matches(isDisplayed()))
        onView(withText(convertLongToYearString(sampleJournal.dtstart))).check(matches(isDisplayed()))
        onView(withText(convertLongToTimeString(sampleJournal.dtstart))).check(matches(isDisplayed()))
        onView(withId(R.id.list_item_status)).check(matches(withText(R.string.journal_status_final)))
        onView(withId(R.id.list_item_classification)).check(matches(withText(R.string.classification_public)))
    }

    @Test
    fun note_is_displayed()  {

        //val fragmentArgs = Bundle()
        val scenario = launchFragmentInContainer<IcalListFragment>(Bundle(), R.style.AppTheme, Lifecycle.State.RESUMED)
        onView(withText(R.string.list_tabitem_notes)).perform(click())
        onView(withText(sampleNote.summary)).check(matches(isDisplayed()))
        onView(withText(sampleNote.description)).check(matches(isDisplayed()))
    }

    @Test
    fun task_is_displayed()  {

        //val fragmentArgs = Bundle()
        val scenario = launchFragmentInContainer<IcalListFragment>(Bundle(), R.style.AppTheme, Lifecycle.State.RESUMED)
        onView(withText(R.string.list_tabitem_todos)).perform(click())
        onView(withText(sampleTodo.summary)).check(matches(isDisplayed()))
        onView(withText(sampleTodo.description)).check(matches(isDisplayed()))
    }
}