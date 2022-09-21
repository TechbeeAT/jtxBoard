/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.app.Application
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Attendee
import at.techbee.jtx.database.properties.Comment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineExceptionHandler
import kotlinx.coroutines.test.createTestCoroutineScope
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@LargeTest
class IcalListFragmentTest {


    private lateinit var database: ICalDatabaseDao
    private lateinit var context: Context
    private lateinit var application: Application

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope =
        createTestCoroutineScope(TestCoroutineDispatcher() + TestCoroutineExceptionHandler() + testDispatcher)

    private val sampleDate = 1632636027826L     //  Sun Sep 26 2021 06:00:27
    private val sampleCollection = ICalCollection(collectionId = 1L, displayName = "testcollection automated tests")
    private val sampleJournal = ICalObject(collectionId = 1L, module = Module.JOURNAL.name, component = Component.VJOURNAL.name, summary = "Journal4Test", description = "Description4JournalTest", dtstart = sampleDate, status = StatusJournal.FINAL.name, classification = Classification.PUBLIC.name)
    private val sampleNote = ICalObject(collectionId = 1L, module = Module.NOTE.name, component = Component.VJOURNAL.name, summary = "Note4Test", description = "Description4NoteTest", dtstart = sampleDate)
    private val sampleTodo = ICalObject(collectionId = 1L, module = Module.TODO.name, component = Component.VTODO.name, summary = "Todo4Test", description = "Description4TodoTest", dtstart = sampleDate)


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
            val journalId = database.insertICalObjectSync(sampleJournal)
            val noteId = database.insertICalObjectSync(sampleNote)
            val taskId = database.insertICalObjectSync(sampleTodo)

            //Attendees
            database.insertAttendee(Attendee(icalObjectId = journalId, caladdress = "contact@techbee.at"))
            database.insertAttendee(Attendee(icalObjectId = noteId, caladdress = "contact@techbee.at"))
            database.insertAttendee(Attendee(icalObjectId = taskId, caladdress = "contact@techbee.at"))

            //Attachments
            database.insertAttachment(Attachment(icalObjectId = journalId, uri = "https://techbee.at"))
            database.insertAttachment(Attachment(icalObjectId = journalId, uri = "https://jtx.techbee.at"))
            database.insertAttachment(Attachment(icalObjectId = noteId, uri = "https://techbee.at"))
            database.insertAttachment(Attachment(icalObjectId = taskId, uri = "https://techbee.at"))

            //Comments
            database.insertComment(Comment(icalObjectId = journalId, text = "my comment"))
            database.insertComment(Comment(icalObjectId = journalId, text = "my comment2"))
            database.insertComment(Comment(icalObjectId = journalId, text = "my comment3"))
            database.insertComment(Comment(icalObjectId = noteId, text = "my comment"))
            database.insertComment(Comment(icalObjectId = taskId, text = "my comment"))

        }
    }


    @After
    fun closeDb() {
        ICalDatabase.getInMemoryDB(context).close()
    }

/*
    @Test
    fun journal_is_displayed()  {

        launchFragmentInContainer<IcalListFragment>(Bundle(), R.style.AppTheme, Lifecycle.State.RESUMED)
        onView(withText(R.string.list_tabitem_journals)).perform(click())
        onView(withText(sampleJournal.summary)).check(matches(isDisplayed()))
        onView(withText(sampleJournal.description)).check(matches(isDisplayed()))
        onView(withText(convertLongToDayString(sampleJournal.dtstart, sampleJournal.dtstartTimezone))).check(matches(isDisplayed()))
        onView(withText(convertLongToMonthString(sampleJournal.dtstart, sampleJournal.dtstartTimezone))).check(matches(isDisplayed()))
        onView(withText(convertLongToYearString(sampleJournal.dtstart, sampleJournal.dtstartTimezone))).check(matches(isDisplayed()))
        onView(withText(convertLongToTimeString(sampleJournal.dtstart, sampleJournal.dtstartTimezone))).check(matches(isDisplayed()))
    }

    @Test
    fun note_is_displayed()  {

        launchFragmentInContainer<IcalListFragment>(Bundle(), R.style.AppTheme, Lifecycle.State.RESUMED)
        onView(withText(R.string.list_tabitem_notes)).perform(click())
        onView(withText(sampleNote.summary)).check(matches(isDisplayed()))
        onView(withText(sampleNote.description)).check(matches(isDisplayed()))

        onView(withText(convertLongToDayString(sampleJournal.dtstart, sampleJournal.dtstartTimezone))).check(doesNotExist())
        onView(withText(convertLongToMonthString(sampleJournal.dtstart, sampleJournal.dtstartTimezone))).check(doesNotExist())
        onView(withText(convertLongToYearString(sampleJournal.dtstart, sampleJournal.dtstartTimezone))).check(doesNotExist())
        onView(withText(convertLongToTimeString(sampleJournal.dtstart, sampleJournal.dtstartTimezone))).check(doesNotExist())

    }

    @Test
    fun task_is_displayed()  {

        launchFragmentInContainer<IcalListFragment>(Bundle(), R.style.AppTheme, Lifecycle.State.RESUMED)
        onView(withText(R.string.list_tabitem_todos)).perform(click())
        onView(withText(sampleTodo.summary)).check(matches(isDisplayed()))
        onView(withText(sampleTodo.description)).check(matches(isDisplayed()))
    }

 */
}