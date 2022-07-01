/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.testing.withFragment
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import at.techbee.jtx.*
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.util.DateTimeUtils.convertLongToDayString
import at.techbee.jtx.util.DateTimeUtils.convertLongToMonthString
import at.techbee.jtx.util.DateTimeUtils.convertLongToTimeString
import at.techbee.jtx.util.DateTimeUtils.convertLongToYearString
import com.google.android.material.slider.Slider
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@LargeTest
class IcalViewFragmentTest {


    private lateinit var database: ICalDatabaseDao
    private lateinit var context: Context
    private lateinit var application: Application

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    @get: Rule
    var testRule = activityScenarioRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.RECORD_AUDIO
    )!!

    private val sampleDate = 1632636027826L     //  Sun Sep 26 2021 06:00:27
    private val sampleCollection = ICalCollection(collectionId = 1L, displayName = "testcollection automated tests")
    private val sampleJour = ICalObject.createJournal().apply {
        collectionId = sampleCollection.collectionId
        summary = "Journal4Test"
        description = "Description4JournalTest"
        dtstart = sampleDate
        contact = "info@techbee.at"
    }
    private val sampleNote = ICalObject.createNote("Note4Test").apply {
        collectionId = sampleCollection.collectionId
        description = "Description4NoteTest"
        dtstart = sampleDate
    }
    private val sampleTodo = ICalObject.createTodo().apply {
        collectionId = sampleCollection.collectionId
        summary = "Todo4Test"
        description = "Description4TodoTest"
        dtstart = sampleDate
    }

    private val sampleSubtask = ICalObject.createTodo().apply {
        collectionId = sampleCollection.collectionId
        summary = "Subtask"
        description = "Subtask Description"
        dtstart = sampleDate
    }

    private val sampleAttendee = Attendee(icalObjectId = sampleJour.id, caladdress = "contact@techbee.at")
    private val sampleAttachment = Attachment(icalObjectId = sampleJour.id, uri = "https://techbee.at", filename = "test.pdf")
    private val sampleComment = Comment(icalObjectId = sampleJour.id, text = "my comment")
    private val sampleCategory1 = Category(text = "cat1")
    private val sampleCategory2 = Category(text = "cat2")
    private val sampleOrganizer = Organizer(caladdress = "orga")
    private val sampleResource = Resource(text = "Projector")


    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ICalDatabase.switchToInMemory(context)
        database = ICalDatabase.getInstance(context).iCalDatabaseDao     // should be in-memory db now
        application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application


        //insert sample entries
        testScope.launch {
            database.insertCollectionSync(sampleCollection)
            sampleJour.id = database.insertICalObjectSync(sampleJour)
            sampleNote.id = database.insertICalObjectSync(sampleNote)
            sampleTodo.id = database.insertICalObjectSync(sampleTodo)

            sampleAttendee.icalObjectId = sampleJour.id
            sampleAttachment.icalObjectId = sampleJour.id
            sampleComment.icalObjectId = sampleJour.id
            sampleCategory1.icalObjectId = sampleJour.id
            sampleCategory2.icalObjectId = sampleJour.id
            sampleOrganizer.icalObjectId = sampleJour.id
            sampleResource.icalObjectId = sampleJour.id

            database.insertAttendeeSync(sampleAttendee)
            database.insertAttachmentSync(sampleAttachment)
            database.insertCommentSync(sampleComment)
            database.insertCategorySync(sampleCategory1)
            database.insertCategorySync(sampleCategory2)
            database.insertOrganizerSync(sampleOrganizer)
            database.insertResourceSync(sampleResource)

            sampleSubtask.id = database.insertICalObjectSync(sampleSubtask)
            database.insertRelatedtoSync(Relatedto(icalObjectId = sampleTodo.id, linkedICalObjectId = sampleSubtask.id, reltype = Reltype.CHILD.name, text = sampleSubtask.uid))
        }
    }


    @After
    fun closeDb() {
        ICalDatabase.getInMemoryDB(context).close()
    }


    @Test
    fun journal_everything_is_displayed_part1()  {

        val fragmentArgs = Bundle()
        fragmentArgs.putLong("item2show", sampleJour.id)
        //val scenario =
        launchFragmentInContainer<IcalViewFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        val collectionString = sampleCollection.displayName + " (" + sampleCollection.accountName + ")"
        onView(allOf(withId(R.id.view_collection), withText(collectionString))).check(matches(isDisplayed()))
        //onView(withText(sampleCollection.displayName)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.view_summary),withText(sampleJour.summary))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.view_description),withText(sampleJour.description))).check(matches(isDisplayed()))

        val expectedDay = convertLongToDayString(sampleJour.dtstart, sampleJour.dtstartTimezone)
        val expectedMonth = convertLongToMonthString(sampleJour.dtstart, sampleJour.dtstartTimezone)
        val expectedYear = convertLongToYearString(sampleJour.dtstart, sampleJour.dtstartTimezone)
        val expectedTime = convertLongToTimeString(sampleJour.dtstart, sampleJour.dtstartTimezone)

        onView(withId(R.id.view_journal_dtstart_day)).check(matches(withText(expectedDay)))
        onView(withId(R.id.view_journal_dtstart_month)).check(matches(withText(expectedMonth)))
        onView(withId(R.id.view_journal_dtstart_year)).check(matches(withText(expectedYear)))
        onView(withId(R.id.view_journal_dtstart_time)).check(matches(withText(expectedTime)))

        val expectedCreated = application.resources.getString(R.string.view_created_text, Date(sampleJour.created))
        val expectedLastModified = application.resources.getString(R.string.view_last_modified_text, Date(sampleJour.lastModified))

        onView(withId(R.id.view_created)).check(matches(withText(expectedCreated)))
        onView(withId(R.id.view_lastModified)).check(matches(withText(expectedLastModified)))
    }


    @Test
    fun journal_everything_is_displayed_part2()  {

        val fragmentArgs = Bundle()
        fragmentArgs.putLong("item2show", sampleJour.id)
        //val scenario =
        launchFragmentInContainer<IcalViewFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        onView(withId(R.id.view_add_audio_note)).check(matches(withText(R.string.view_add_audio_note)))
        onView(withId(R.id.view_add_note_edittext)).check(matches(isDisplayed()))
        onView(withText(R.string.view_feedback_linked_notes)).check(matches(withText(R.string.view_feedback_linked_notes)))
        onView(withId(R.id.view_classification_chip)).check(matches(isDisplayed()))
        onView(withId(R.id.view_status_chip)).check(matches(isDisplayed()))
    }

    @Test
    fun journal_everything_is_displayed_part3()  {

        val fragmentArgs = Bundle()
        fragmentArgs.putLong("item2show", sampleJour.id)
        launchFragmentInContainer<IcalViewFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        onView(withId(R.id.view_attendee_header)).check(matches(withText(R.string.attendees)))
        //onView(withText(sampleAttendee.caladdress)).check(matches(isDisplayed()))

        onView(withText(R.string.comments)).check(matches(isDisplayed()))
        onView(withId(R.id.view_comment_textview)).check(matches(withText(sampleComment.text)))

        onView(withText(R.string.attachments)).check(matches(isDisplayed()))
        onView(withId(R.id.view_attachment_textview)).check(matches(withText(sampleAttachment.filename)))

        onView(withId(R.id.view_organizer_header)).check(matches(withText(R.string.organizer)))
        onView(withId(R.id.view_organizer)).check(matches(withText(sampleOrganizer.caladdress)))

        onView(withId(R.id.view_contact_header)).check(matches(withText(R.string.contact)))
        onView(withId(R.id.view_contact)).check(matches(withText(sampleJour.contact)))
    }

    @Test
    fun note_is_displayed()  {

        val fragmentArgs = Bundle()
        fragmentArgs.putLong("item2show", sampleNote.id)
        //val scenario =
        launchFragmentInContainer<IcalViewFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)
        onView(withText(sampleNote.summary)).check(matches(isDisplayed()))
        onView(withText(sampleNote.description)).check(matches(isDisplayed()))

        onView(withId(R.id.view_status_chip)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.view_classification_chip)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun task_is_displayed()  {

        val fragmentArgs = Bundle()
        fragmentArgs.putLong("item2show", sampleTodo.id)
        val scenario = launchFragmentInContainer<IcalViewFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)
        onView(withText(sampleTodo.summary)).check(matches(isDisplayed()))
        onView(withText(sampleTodo.description)).check(matches(isDisplayed()))
        onView(withId(R.id.view_progress_checkbox)).check(matches(isDisplayed()))
        onView(withId(R.id.view_progress_label)).check(matches(isDisplayed()))
        onView(withId(R.id.view_progress_percent)).check(matches(isDisplayed()))
        onView(withId(R.id.view_progress_slider)).check(matches(isDisplayed()))
        onView(withText(R.string.todo_status_needsaction)).check(matches(isDisplayed()))
        onView(withText(R.string.classification_public)).check(matches(isDisplayed()))

        val priorities = context.resources.getStringArray(R.array.priority)
        onView(withText(priorities[0])).check(matches(isDisplayed()))


        onView(withText(sampleSubtask.summary)).check(matches(isDisplayed()))
        onView(withId(R.id.view_subtask_progress_checkbox)).check(matches(isDisplayed()))
        onView(withId(R.id.view_subtask_progress_percent)).check(matches(withText("0 %")))

        onView(withId(R.id.view_subtask_progress_slider)).check(matches(isDisplayed()))
        scenario.withFragment {
            val slider = this.activity?.findViewById<Slider>(R.id.view_subtask_progress_slider)
            assertEquals(0F, slider?.value)
        }
    }

    @Test
    fun task_update_progress_to_in_process()  {

        val fragmentArgs = Bundle()
        fragmentArgs.putLong("item2show", sampleTodo.id)
        val scenario = launchFragmentInContainer<IcalViewFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)
        onView(withText(sampleTodo.summary)).check(matches(isDisplayed()))
        onView(withText(sampleTodo.description)).check(matches(isDisplayed()))
        onView(withId(R.id.view_progress_checkbox)).check(matches(isDisplayed()))
        onView(withId(R.id.view_progress_label)).check(matches(isDisplayed()))
        onView(withId(R.id.view_progress_percent)).check(matches(isDisplayed()))
        onView(withId(R.id.view_progress_slider)).check(matches(isDisplayed()))
        onView(withText(R.string.todo_status_needsaction)).check(matches(isDisplayed()))

        scenario.withFragment {
            this.binding.viewProgressSlider.value = 50F
            //the update would be done by the touch listener, this is a bit problematic in espresso, no proper solution was found yet, so we call the method of the listener manually:
            this.icalViewViewModel.updateProgress(icalViewViewModel.icalEntity.value!!.property.id, binding.viewProgressSlider.value.toInt())

            // wait for update to be done or until timeout is reached
            val timeout = 2000
            var timer = 0
            while (icalViewViewModel.icalEntity.value!!.property.percent!! != 50 || timer < timeout) {
                timer += 50
                Thread.sleep(50)
            }
        }

        onView(withId(R.id.view_progress_percent)).check(matches(withText("50 %")))
        onView(withText(R.string.todo_status_inprocess)).check(matches(isDisplayed()))
    }


    @Test
    fun task_update_progress_to_completed()  {

        val fragmentArgs = Bundle()
        fragmentArgs.putLong("item2show", sampleTodo.id)
        val scenario = launchFragmentInContainer<IcalViewFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)
        onView(withText(sampleTodo.summary)).check(matches(isDisplayed()))
        onView(withText(sampleTodo.description)).check(matches(isDisplayed()))
        onView(withId(R.id.view_progress_checkbox)).check(matches(isDisplayed()))
        onView(withId(R.id.view_progress_label)).check(matches(isDisplayed()))
        onView(withId(R.id.view_progress_percent)).check(matches(isDisplayed()))
        onView(withId(R.id.view_progress_slider)).check(matches(isDisplayed()))
        onView(withText(R.string.todo_status_needsaction)).check(matches(isDisplayed()))

        scenario.withFragment {
            this.binding.viewProgressSlider.value = 100F
            //the update would be done by the touch listener, this is a bit problematic in espresso, no proper solution was found yet, so we call the method of the listener manually:
            this.icalViewViewModel.updateProgress(icalViewViewModel.icalEntity.value!!.property.id, binding.viewProgressSlider.value.toInt())

            // wait for update to be done or until timeout is reached
            val timeout = 2000
            var timer = 0
            while (icalViewViewModel.icalEntity.value!!.property.percent!! != 100 || timer < timeout) {
                timer += 50
                Thread.sleep(50)
            }
        }

        onView(withId(R.id.view_progress_percent)).check(matches(withText("100 %")))
        onView(withText(R.string.todo_status_completed)).check(matches(isDisplayed()))
    }

/*
    @Test
    fun journal_add_note() {

        val fragmentArgs = Bundle()
        fragmentArgs.putLong("item2show", sampleNote.id)
        launchFragmentInContainer<IcalViewFragment>(
            fragmentArgs,
            R.style.AppTheme,
            Lifecycle.State.RESUMED
        )

        val noteText = "TestText"

        //onView(allOf(withId(R.id.view_add_note), withText(R.string.view_add_note))).check(matches(isDisplayed()))
        onView(withId(R.id.view_add_note)).perform(scrollTo(), click())
        onView (withId(R.id.view_view_addnote_dialog_edittext)).perform(typeText(noteText))
        onView (withText(R.string.save)).perform(click())
        onView (withText(noteText)).check(matches(isDisplayed()))
    }

 */

    @Test
    fun journal_add_audio_note() {

        val fragmentArgs = Bundle()
        fragmentArgs.putLong("item2show", sampleNote.id)
        launchFragmentInContainer<IcalViewFragment>(
            fragmentArgs,
            R.style.AppTheme,
            Lifecycle.State.RESUMED
        )

        onView(
            allOf(
                withId(R.id.view_add_audio_note),
                withText(R.string.view_add_audio_note)
            )
        ).check(matches(isDisplayed()))
        onView(withId(R.id.view_add_audio_note)).perform(scrollTo(), click())

        onView(withId(R.id.view_audio_dialog_startrecording_fab)).check(matches(isDisplayed()))
        onView(withId(R.id.view_audio_dialog_startplaying_fab)).check(matches(isDisplayed()))
        onView(withId(R.id.view_audio_dialog_progressbar)).check(matches(isDisplayed()))

        onView(withId(R.id.view_audio_dialog_startrecording_fab)).check(matches(isEnabled()))
        onView(withId(R.id.view_audio_dialog_startplaying_fab)).check(matches(not(isEnabled())))   // initially the playback button must be disabled

        onView(withId(R.id.view_audio_dialog_startrecording_fab)).perform(click())  // start recording
        Thread.sleep(100)
        onView(withId(R.id.view_audio_dialog_startrecording_fab)).perform(click())  // stop recording

        onView(withId(R.id.view_audio_dialog_startplaying_fab)).check(matches(isEnabled()))  // fab should be enabled now
        //onView(withId(R.id.view_audio_dialog_startplaying_fab)).perform(click())  // start playback
        //Thread.sleep(200)
        // TODO: Find a way to check if the slider value got updated correctly on playback

        onView (withText(R.string.save)).perform(click())
        onView (withId(R.id.view_comment_playbutton)).check(matches(isDisplayed()))
    }
}