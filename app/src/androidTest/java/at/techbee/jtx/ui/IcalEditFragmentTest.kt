package at.techbee.jtx.ui

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import at.techbee.jtx.*
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICalEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@LargeTest
class IcalEditFragmentTest {


    private lateinit var database: ICalDatabaseDao
    private lateinit var context: Context
    private lateinit var application: Application

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    @get: Rule
    var testRule = activityScenarioRule<MainActivity>()

    private val sampleDate = 1632636027826L     //  Sun Sep 26 2021 06:00:27
    private val sampleCollection = ICalCollection(collectionId = 1L, displayName = "testcollection automated tests")
    private val sampleJournal = ICalObject.createJournal().apply {
        collectionId = sampleCollection.collectionId
        summary = "Journal4Test"
        description = "Description4JournalTest"
        contact = "patrick@techbee.at"
        dtstart = sampleDate
        dtstartTimezone = null
        location = "Vienna"
        url = "https://jtx.techbee.at"
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

    private val sampleAttendee1 = Attendee(icalObjectId = sampleJournal.id, caladdress = "attendee1@techbee.at")
    private val sampleAttendee2 = Attendee(icalObjectId = sampleJournal.id, caladdress = "attendee2@techbee.at")
    private val sampleAttachment = Attachment(icalObjectId = sampleJournal.id, uri = "https://techbee.at", filename = "test.pdf")
    private val sampleComment1 = Comment(icalObjectId = sampleJournal.id, text = "my comment1")
    private val sampleComment2 = Comment(icalObjectId = sampleJournal.id, text = "my comment2")
    private val sampleCategory1 = Category(text = "cat1")
    private val sampleCategory2 = Category(text = "cat2")
    private val sampleOrganizer = Organizer(caladdress = "orga")
    private val sampleResource = Resource(text = "Projector")

    private lateinit var sampleJournalEntity: ICalEntity


    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ICalDatabase.switchToInMemory(context)
        database = ICalDatabase.getInstance(context).iCalDatabaseDao     // should be in-memory db now
        application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application


        //insert sample entries
        testScope.launch(TestCoroutineDispatcher()) {
            database.insertCollectionSync(sampleCollection)
            sampleJournal.id = database.insertICalObjectSync(sampleJournal)
            sampleNote.id = database.insertICalObjectSync(sampleNote)
            sampleTodo.id = database.insertICalObjectSync(sampleTodo)

            sampleAttendee1.icalObjectId = sampleJournal.id
            sampleAttendee2.icalObjectId = sampleJournal.id
            sampleAttachment.icalObjectId = sampleJournal.id
            sampleComment1.icalObjectId = sampleJournal.id
            sampleComment2.icalObjectId = sampleJournal.id
            sampleCategory1.icalObjectId = sampleJournal.id
            sampleCategory2.icalObjectId = sampleJournal.id
            sampleOrganizer.icalObjectId = sampleJournal.id
            sampleResource.icalObjectId = sampleJournal.id

            database.insertAttendeeSync(sampleAttendee1)
            database.insertAttendeeSync(sampleAttendee2)
            database.insertAttachmentSync(sampleAttachment)
            database.insertCommentSync(sampleComment1)
            database.insertCommentSync(sampleComment2)
            database.insertCategorySync(sampleCategory1)
            database.insertCategorySync(sampleCategory2)
            database.insertOrganizerSync(sampleOrganizer)
            database.insertResourceSync(sampleResource)

            sampleSubtask.id = database.insertICalObjectSync(sampleSubtask)
            database.insertRelatedtoSync(Relatedto(icalObjectId = sampleJournal.id, linkedICalObjectId = sampleSubtask.id, reltype = Reltype.CHILD.name, text = sampleSubtask.uid))

            sampleJournalEntity = database.getSync(sampleJournal.id)!!

        }
    }


    @After
    fun closeDb() {
        ICalDatabase.getInMemoryDB(context).close()
    }


    @Test
    fun journal_everything_is_prefilled()  {


        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity )
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)
        onView(withText(sampleJournalEntity.property.summary)).check(matches(isDisplayed()))
        onView(withText(sampleJournalEntity.property.description)).check(matches(isDisplayed()))
        onView(withText(convertLongToDayString(sampleJournalEntity.property.dtstart))).check(matches(isDisplayed()))
        onView(withText(convertLongToMonthString(sampleJournalEntity.property.dtstart))).check(matches(isDisplayed()))
        onView(withText(convertLongToYearString(sampleJournalEntity.property.dtstart))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.edit_dtstart_time), withText(convertLongToTimeString(sampleJournal.dtstart)))).check(matches(isDisplayed()))
        onView(withText(R.string.journal_status_final)).check(matches(isDisplayed()))
        onView(withText(R.string.classification_public)).check(matches(isDisplayed()))

        onView(withText(sampleJournalEntity.categories?.get(0)?.text)).check(matches(isDisplayed()))
        onView(withText(sampleJournalEntity.categories?.get(1)?.text)).check(matches(isDisplayed()))
        onView(withText(R.string.classification_public)).check(matches(isDisplayed()))

        //switch to tab contact, attendees, resources
        scenario.onFragment {
            it.binding.icalEditTabs?.selectTab(it.binding.icalEditTabs?.getTabAt(IcalEditViewModel.TAB_PEOPLE_RES))
        }
        onView(withText(sampleJournalEntity.property.contact)).check(matches(isDisplayed()))
        onView(withText(sampleJournalEntity.attendees?.get(0)?.caladdress)).check(matches(isDisplayed()))
        onView(withText(sampleJournalEntity.attendees?.get(1)?.caladdress)).check(matches(isDisplayed()))
        onView(withText(sampleJournalEntity.resources?.get(0)?.text)).check(matches(isDisplayed()))


        //switch to tab url, location, comments
        scenario.onFragment {
            it.binding.icalEditTabs?.selectTab(it.binding.icalEditTabs?.getTabAt(IcalEditViewModel.TAB_LOC_COMMENTS))
        }
        onView(withText(sampleJournalEntity.property.url)).check(matches(isDisplayed()))
        onView(withText(sampleJournalEntity.property.location)).check(matches(isDisplayed()))
        onView(withText(sampleJournalEntity.comments?.get(0)?.text)).check(matches(isDisplayed()))
        onView(withText(sampleJournalEntity.comments?.get(1)?.text)).check(matches(isDisplayed()))

        //switch to tab attachments
        scenario.onFragment {
            it.binding.icalEditTabs?.selectTab(it.binding.icalEditTabs?.getTabAt(IcalEditViewModel.TAB_ATTACHMENTS))
        }
        onView(withText(sampleJournalEntity.attachments?.get(0)?.filename)).check(matches(isDisplayed()))

        //switch to tab attachments
        scenario.onFragment {
            it.binding.icalEditTabs?.selectTab(it.binding.icalEditTabs?.getTabAt(IcalEditViewModel.TAB_ATTACHMENTS))
        }
        onView(withText(sampleJournalEntity.attachments?.get(0)?.filename)).check(matches(isDisplayed()))

        //switch to tab subtasks
        scenario.onFragment {
            it.binding.icalEditTabs?.selectTab(it.binding.icalEditTabs?.getTabAt(IcalEditViewModel.TAB_SUBTASKS))
        }
        onView(withText(sampleSubtask.summary)).check(matches(isDisplayed()))
    }

    @Test
    fun journal_check_allday() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity)
        }
        launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        onView(withId(R.id.edit_dtstart_time)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_timezone_spinner)).check(matches(isDisplayed()))

        onView(allOf(withText(R.string.edit_all_day), withId(R.id.edit_all_day_switch))).perform(click())

        onView(withId(R.id.edit_dtstart_time)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_timezone_spinner)).check(matches(not(isDisplayed())))
    }

    @Test
    fun journal_add_category() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity)
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        val newCategory = "espressoCategory"

        onView(withId(R.id.edit_categories_add_autocomplete)).perform(typeText(newCategory))
        onView(withId(R.id.edit_categories_add_autocomplete)).perform(pressImeActionButton())

        scenario.onFragment {
            assertTrue(it.binding.editCategoriesAddAutocomplete.text.isNullOrBlank())    // make sure that the edittext got cleared
            assertTrue(it.icalEditViewModel.categoryUpdated.contains(Category(text = newCategory)))
        }

        onView(withText(newCategory)).check(matches(isDisplayed()))           // new category should be visible as a chip
    }

    @Test
    fun journal_add_attendee() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity)
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        //switch tab
        scenario.onFragment {
            it.binding.icalEditTabs?.selectTab(it.binding.icalEditTabs?.getTabAt(IcalEditViewModel.TAB_PEOPLE_RES))
        }

        val newAttendee = "espressoattendee@techbee.at"

        onView(withId(R.id.edit_attendees_add_autocomplete)).perform(typeText(newAttendee))
        onView(withId(R.id.edit_attendees_add_autocomplete)).perform(pressImeActionButton())

        scenario.onFragment {
            assertTrue(it.binding.editAttendeesAddAutocomplete.text.isNullOrBlank())    // make sure that the edittext got cleared
            assertTrue(it.icalEditViewModel.attendeeUpdated.contains(Attendee(caladdress = newAttendee)))
        }

        onView(withText(newAttendee)).check(matches(isDisplayed()))           // new attendee should be visible as a chip
    }

    @Test
    fun journal_add_resource() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity)
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        //switch tab
        scenario.onFragment {
            it.binding.icalEditTabs?.selectTab(it.binding.icalEditTabs?.getTabAt(IcalEditViewModel.TAB_PEOPLE_RES))
        }

        val newResource = "lots of coffee"

        onView(withId(R.id.edit_resources_add_autocomplete)).perform(typeText(newResource))
        onView(withId(R.id.edit_resources_add_autocomplete)).perform(pressImeActionButton())

        scenario.onFragment {
            assertTrue(it.binding.editResourcesAddAutocomplete?.text.isNullOrBlank())    // make sure that the edittext got cleared
            assertTrue(it.icalEditViewModel.resourceUpdated.contains(Resource(text = newResource)))
        }

        onView(withText(newResource)).check(matches(isDisplayed()))           // new resource should be visible as a chip
    }



    @Test
    fun journal_add_comment() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity)
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        //switch tab
        scenario.onFragment {
            it.binding.icalEditTabs?.selectTab(it.binding.icalEditTabs?.getTabAt(IcalEditViewModel.TAB_LOC_COMMENTS))
        }

        val newComment = "maybe that was one espresso too much :-P"

        onView(withId(R.id.edit_comment_add_edittext)).perform(typeText(newComment))
        onView(withId(R.id.edit_comment_add_edittext)).perform(pressImeActionButton())

        scenario.onFragment {
            assertTrue(it.binding.editCommentAddEdittext.text.isNullOrBlank())    // make sure that the edittext got cleared
            assertTrue(it.icalEditViewModel.commentUpdated.contains(Comment(text = newComment)))
        }

        onView(withText(newComment)).check(matches(isDisplayed()))           // new resource should be visible as a chip
    }


    @Test
    fun journal_add_subtask() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity)
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        //switch tab
        scenario.onFragment {
            it.binding.icalEditTabs?.selectTab(it.binding.icalEditTabs?.getTabAt(IcalEditViewModel.TAB_SUBTASKS))
        }

        val newSubtask = "get decaf coffee^^"

        onView(withId(R.id.edit_subtasks_add_edittext)).perform(typeText(newSubtask))
        onView(withId(R.id.edit_subtasks_add_edittext)).perform(pressImeActionButton())

        scenario.onFragment {
            assertTrue(it.binding.editSubtasksAddEdittext.text.isNullOrBlank())    // make sure that the edittext got cleared
            assertNotNull(it.icalEditViewModel.subtaskUpdated.find { subtask ->
                subtask.summary == newSubtask
            })
        }

        onView(withText(newSubtask)).check(matches(isDisplayed()))           // new resource should be visible as a chip
    }

    //TODO: Add Test for Recurrence
    //TODO: Add Test for Attachment
    //TODO: Add Test for Note (if fields are correctly hidden)
    //TODO: Add Test for Task (if fields are correctly displayed)


    /*
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
            this.icalViewViewModel.updateProgress(icalViewViewModel.icalEntity.value!!.property, binding.viewProgressSlider.value.toInt())

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
            this.icalViewViewModel.updateProgress(icalViewViewModel.icalEntity.value!!.property, binding.viewProgressSlider.value.toInt())

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


    @Test
    fun journal_add_note() {

        val fragmentArgs = Bundle()
        fragmentArgs.putLong("item2show", sampleJournal.id)
        launchFragmentInContainer<IcalViewFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        val noteText = "TestText"

        onView(allOf(withId(R.id.view_add_note), withText(R.string.view_add_note))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.view_add_audio_note), withText(R.string.view_add_audio_note))).check(matches(isDisplayed()))
        onView(withId(R.id.view_add_note)).perform(scrollTo(), click())
        onView (withId(R.id.view_view_addnote_dialog_edittext)).perform(typeText(noteText))
        onView (withText(R.string.save)).perform(click())
        onView (withText(noteText)).check(matches(isDisplayed()))
    }

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

     */
}