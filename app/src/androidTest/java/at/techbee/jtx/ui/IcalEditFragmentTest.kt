package at.techbee.jtx.ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
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

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.READ_CONTACTS
    )!!


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
    private lateinit var sampleNoteEntity: ICalEntity
    private lateinit var sampleTodoEntity: ICalEntity

    private var recurDay: String = ""
    private var recurWeek: String = ""
    private var recurMonth: String = ""
    private var recurYear: String = ""


        @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ICalDatabase.switchToInMemory(context)
        database = ICalDatabase.getInstance(context).iCalDatabaseDao     // should be in-memory db now
        application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

        val recurOptions = context.resources.getStringArray(R.array.edit_recur_day_week_month_year)
        recurDay = recurOptions[0]
        recurWeek = recurOptions[1]
        recurMonth = recurOptions[2]
        recurYear = recurOptions[3]

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
            sampleNoteEntity = database.getSync(sampleNote.id)!!
            sampleTodoEntity = database.getSync(sampleTodo.id)!!


        }
    }


    @After
    fun closeDb() {
        ICalDatabase.getInMemoryDB(context).close()
    }


    @Test
    fun journal_everything_is_prefilled_tab_general()  {


        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity )
        }
        launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)
        onView(withText(sampleJournalEntity.property.summary)).check(matches(isDisplayed()))
        onView(withText(sampleJournalEntity.property.description)).check(matches(isDisplayed()))
        onView(withText(convertLongToDayString(sampleJournalEntity.property.dtstart))).check(matches(isDisplayed()))
        onView(withText(convertLongToMonthString(sampleJournalEntity.property.dtstart))).check(matches(isDisplayed()))
        onView(withText(convertLongToYearString(sampleJournalEntity.property.dtstart))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.edit_dtstart_time), withText(convertLongToTimeString(sampleJournal.dtstart)))).check(matches(isDisplayed()))
        onView(withText(R.string.journal_status_final)).check(matches(isDisplayed()))
        onView(withText(R.string.classification_public)).check(matches(isDisplayed()))

        onView(withId(R.id.edit_progress_label)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_progress_checkbox)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_progress_slider)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_progress_percent)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_task_dates_fragment)).check(matches(not(isDisplayed())))

        onView(withText(sampleJournalEntity.categories?.get(0)?.text)).check(matches(isDisplayed()))
        onView(withText(sampleJournalEntity.categories?.get(1)?.text)).check(matches(isDisplayed()))
        onView(withText(R.string.classification_public)).check(matches(isDisplayed()))

    }


    @Test
    fun journal_everything_is_prefilled_tab_contact_attendees_resources()  {


        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity )
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        //switch to tab contact, attendees, resources
        scenario.onFragment {
            it.binding.icalEditTabs.selectTab(it.binding.icalEditTabs.getTabAt(IcalEditViewModel.TAB_PEOPLE_RES))
        }
        onView(withText(sampleJournalEntity.property.contact)).check(matches(isDisplayed()))
        onView(withText(sampleJournalEntity.attendees?.get(0)?.caladdress)).check(matches(isDisplayed()))
        onView(withText(sampleJournalEntity.attendees?.get(1)?.caladdress)).check(matches(isDisplayed()))
        onView(withText(sampleJournalEntity.resources?.get(0)?.text)).check(matches(isDisplayed()))
    }


    @Test
    fun journal_everything_is_prefilled_tab_url_loc_comments()  {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity )
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        //switch to tab url, location, comments
        scenario.onFragment {
            it.binding.icalEditTabs.selectTab(it.binding.icalEditTabs.getTabAt(IcalEditViewModel.TAB_LOC_COMMENTS))
        }
        onView(withText(sampleJournalEntity.property.url)).check(matches(isDisplayed()))
        onView(withText(sampleJournalEntity.property.location)).check(matches(isDisplayed()))
        onView(withText(sampleJournalEntity.comments!![0].text)).check(matches(withText(sampleComment1.text)))
        onView(withText(sampleJournalEntity.comments!![1].text)).check(matches(withText(sampleComment2.text)))
    }




    @Test
    fun journal_everything_is_prefilled_tab_attachments()  {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity )
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        //switch to tab attachments
        scenario.onFragment {
            it.binding.icalEditTabs.selectTab(it.binding.icalEditTabs.getTabAt(IcalEditViewModel.TAB_ATTACHMENTS))
        }

        onView(withId(R.id.edit_attachment_item_textview)).check(matches(withText(sampleJournalEntity.attachments!![0].filename)))
    }


    @Test
    fun journal_everything_is_prefilled_tab_subtasks()  {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity )
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        //switch to tab subtasks
        scenario.onFragment {
            it.binding.icalEditTabs.selectTab(it.binding.icalEditTabs.getTabAt(IcalEditViewModel.TAB_SUBTASKS))
        }
        onView(withId(R.id.edit_subtask_textview)).check(matches(withText(sampleSubtask.summary)))
    }



    @Test
    fun note_basic_check() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleNoteEntity)
        }
        launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        onView(withText(sampleNoteEntity.property.summary)).check(matches(isDisplayed()))
        onView(withText(sampleNoteEntity.property.description)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_dtstart_day)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_dtstart_month)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_dtstart_year)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_dtstart_time)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_all_day_switch)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_timezone_spinner)).check(matches(not(isDisplayed())))

        onView(withId(R.id.edit_progress_label)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_progress_checkbox)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_progress_slider)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_progress_percent)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_task_dates_fragment)).check(matches(not(isDisplayed())))


        onView(withText(R.string.journal_status_final)).check(matches(isDisplayed()))
        onView(withText(R.string.classification_public)).check(matches(isDisplayed()))
    }

    @Test
    fun todo_basic_check1() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleTodoEntity)
        }
        launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        onView(withText(sampleTodoEntity.property.summary)).check(matches(isDisplayed()))
        onView(withText(sampleTodoEntity.property.description)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_dtstart_day)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_dtstart_month)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_dtstart_year)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_dtstart_time)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_all_day_switch)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_timezone_spinner)).check(matches(not(isDisplayed())))

    }


    @Test
    fun todo_basic_check2() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleTodoEntity)
        }
        launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        onView(withId(R.id.edit_progress_label)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_progress_checkbox)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_progress_slider)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_progress_percent)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_task_dates_fragment)).check(matches(isDisplayed()))

        onView(withText(R.string.todo_status_needsaction)).check(matches(isDisplayed()))
        onView(withText(R.string.classification_public)).check(matches(isDisplayed()))
        val priorities = context.resources.getStringArray(R.array.priority)
        onView(withText(priorities[0])).check(matches(isDisplayed()))

    }


    @Test
    fun todo_basic_check3() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleTodoEntity)
        }
        launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        onView(withId(R.id.edit_task_addStartedAndDueTime_switch)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_started_date)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_started_time)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_startedtimezone_icon)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_startedtimezone_spinner)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_due_date)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_due_time)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_duetimezone_icon)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_duetimezone_spinner)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_completed_date)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_completed_time)).check(matches(isDisplayed()))
    }



    @Test
    fun todo_add_time_toggle() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleTodoEntity)
        }
        launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        onView(withId(R.id.edit_task_addStartedAndDueTime_switch)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_started_date)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_started_time)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_startedtimezone_icon)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_startedtimezone_spinner)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_due_date)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_due_time)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_duetimezone_icon)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_duetimezone_spinner)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_completed_date)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_completed_time)).check(matches(isDisplayed()))

        onView(withId(R.id.edit_task_addStartedAndDueTime_switch)).perform(scrollTo(), click())
        // after click (to activate) all the time elements must be visible

        onView(withId(R.id.edit_started_date)).perform(scrollTo())
        onView(withId(R.id.edit_started_date)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_started_time)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_startedtimezone_spinner)).perform(scrollTo())
//        onView(withId(R.id.edit_startedtimezone_icon)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_startedtimezone_spinner)).check(matches(isDisplayed()))

        onView(withId(R.id.edit_due_date)).perform(scrollTo())
        onView(withId(R.id.edit_due_date)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_due_time)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_duetimezone_spinner)).perform(scrollTo())
//        onView(withId(R.id.edit_duetimezone_icon)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_duetimezone_spinner)).check(matches(isDisplayed()))

        onView(withId(R.id.edit_completed_date)).perform(scrollTo())
        onView(withId(R.id.edit_completed_date)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_completed_time)).check(matches(isDisplayed()))
    }


    @Test
    fun todo_check_datepickers_timepickers() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleTodoEntity)
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        // after click (to activate) all the time elements must be visible
        onView(withId(R.id.edit_task_addStartedAndDueTime_switch)).perform(scrollTo(), click())

        onView(withId(R.id.edit_started_date)).perform(scrollTo(), click())
        onView(withText(R.string.edit_datepicker_dialog_select_date)).check(matches(isDisplayed()))
        onView(withId(R.id.confirm_button)).perform(click())

        onView(withId(R.id.edit_started_time)).perform(scrollTo(), click())
        onView(withText(R.string.edit_datepicker_dialog_select_time)).check(matches(isDisplayed()))
        onView(withId(R.id.material_timepicker_ok_button)).perform(click())

        onView(withId(R.id.edit_due_date)).perform(scrollTo(), click())
        onView(withText(R.string.edit_datepicker_dialog_select_date)).check(matches(isDisplayed()))
        onView(withId(R.id.confirm_button)).perform(click())

        onView(withId(R.id.edit_due_time)).perform(scrollTo(), click())
        onView(withText(R.string.edit_datepicker_dialog_select_time)).check(matches(isDisplayed()))
        onView(withId(R.id.material_timepicker_ok_button)).perform(click())

        onView(withId(R.id.edit_completed_date)).perform(scrollTo(), click())
        onView(withText(R.string.edit_datepicker_dialog_select_date)).check(matches(isDisplayed()))
        onView(withId(R.id.confirm_button)).perform(click())

        onView(withId(R.id.edit_completed_time)).perform(scrollTo(), click())
        onView(withText(R.string.edit_datepicker_dialog_select_time)).check(matches(isDisplayed()))
        onView(withId(R.id.material_timepicker_ok_button)).perform(click())

        scenario.onFragment {
            assertEquals(true, it.binding.editTaskDatesFragment.editStartedDateEdittext.text?.isNotEmpty())
            assertEquals(true, it.binding.editTaskDatesFragment.editStartedTimeEdittext.text?.isNotEmpty())
            assertEquals(true, it.binding.editTaskDatesFragment.editDueDateEdittext.text?.isNotEmpty())
            assertEquals(true, it.binding.editTaskDatesFragment.editDueTimeEdittext.text?.isNotEmpty())
            assertEquals(true, it.binding.editTaskDatesFragment.editCompletedDateEdittext.text?.isNotEmpty())
            assertEquals(true, it.binding.editTaskDatesFragment.editCompletedTimeEdittext.text?.isNotEmpty())
        }
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
    fun journal_check_datepicker() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity)
        }
        launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        onView(allOf(withText(R.string.edit_all_day), withId(R.id.edit_all_day_switch))).perform(click())

        onView(withId(R.id.edit_dtstart_day)).perform(click())
        onView(withText(R.string.edit_datepicker_dialog_select_date)).check(matches(isDisplayed()))

        onView(withId(R.id.confirm_button)).perform(click())
        onView(withText(R.string.edit_datepicker_dialog_select_time)).check(doesNotExist())
    }

    @Test
    fun journal_check_datepicker_timepicker() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity)
        }
        launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        onView(withId(R.id.edit_dtstart_day)).perform(click())
        onView(withText(R.string.edit_datepicker_dialog_select_date)).check(matches(isDisplayed()))

        onView(withId(R.id.confirm_button)).perform(click())
        onView(withText(R.string.edit_datepicker_dialog_select_time)).check(matches(isDisplayed()))
        onView(withId(R.id.material_timepicker_ok_button)).perform(click())

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
            it.binding.icalEditTabs.selectTab(it.binding.icalEditTabs.getTabAt(IcalEditViewModel.TAB_PEOPLE_RES))
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
            it.binding.icalEditTabs.selectTab(it.binding.icalEditTabs.getTabAt(IcalEditViewModel.TAB_PEOPLE_RES))
        }

        val newResource = "lots of coffee"

        onView(withId(R.id.edit_resources_add_autocomplete)).perform(typeText(newResource))
        onView(withId(R.id.edit_resources_add_autocomplete)).perform(pressImeActionButton())

        scenario.onFragment {
            assertTrue(it.binding.editResourcesAddAutocomplete.text.isNullOrBlank())    // make sure that the edittext got cleared
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
            it.binding.icalEditTabs.selectTab(it.binding.icalEditTabs.getTabAt(IcalEditViewModel.TAB_LOC_COMMENTS))
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
            it.binding.icalEditTabs.selectTab(it.binding.icalEditTabs.getTabAt(IcalEditViewModel.TAB_SUBTASKS))
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

    @Test
    fun journal_add_attachment_link() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity)
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        val attachmentLink = "https://jtx.techbee.at/test.pdf"

        //switch tab
        scenario.onFragment {
            it.binding.icalEditTabs.selectTab(it.binding.icalEditTabs.getTabAt(IcalEditViewModel.TAB_ATTACHMENTS))
        }

        onView(allOf(withId(R.id.button_attachment_add_link), withText(R.string.edit_add_link_button_text))).perform(scrollTo(), click())
        onView(withId(R.id.edit_attachment_add_dialog_edittext)).perform(typeText(attachmentLink))
        onView(withText(R.string.save)).perform(click())
        onView(withText(attachmentLink)).check(matches(isDisplayed()))

        scenario.onFragment { fragment ->
            assertNotNull(
                fragment.icalEditViewModel.attachmentUpdated.find {
                    it.uri == attachmentLink
                }
            )
        }
    }

    @Test
    fun journal_check_recurrence_nothing_displayed() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity)
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        //switch tab
        scenario.onFragment {
            it.binding.icalEditTabs.selectTab(it.binding.icalEditTabs.getTabAt(IcalEditViewModel.TAB_RECURRING))
        }

        onView(withText(R.string.edit_recurrence_header)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_switch)).check(matches(isDisplayed()))

        onView(withId(R.id.edit_recur_every_x)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_every_x_numberPicker)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_days_months_spinner)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_on_weekday)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_weekly_on_chipgroup_weekdays)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_on_the_x_day_of_month)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_on_the_x_day_of_month_numberPicker)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_x_day_of_the_month)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_until_x_occurences_picker)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_until_occurences)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_last_occurence)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_last_occurence_item)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_all_occurences)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_all_occurences_items)).check(matches(not(isDisplayed())))

    }


    @Test
    fun journal_check_recurrence_default_day_recur() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity)
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        //switch tab
        scenario.onFragment {
            it.binding.icalEditTabs.selectTab(it.binding.icalEditTabs.getTabAt(IcalEditViewModel.TAB_RECURRING))
        }

        onView(withId(R.id.edit_recur_switch)).perform(click())

        // spinner is set to day, check visibilities
        onView(withId(R.id.edit_recur_every_x)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_every_x_numberPicker)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_days_months_spinner)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_on_weekday)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_weekly_on_chipgroup_weekdays)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_on_the_x_day_of_month)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_on_the_x_day_of_month_numberPicker)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_x_day_of_the_month)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_until_x_occurences_picker)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_until_occurences)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_last_occurence)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_last_occurence_item)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_all_occurences)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_all_occurences_items)).check(matches(isDisplayed()))

    }


    @Test
    fun journal_check_recurrence_week_recur() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity)
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        //switch tab
        scenario.onFragment {
            it.binding.icalEditTabs.selectTab(it.binding.icalEditTabs.getTabAt(IcalEditViewModel.TAB_RECURRING))
        }

        // switch the spinner to Week
        onView(withId(R.id.edit_recur_days_months_spinner)).perform(click())
        onView(withText(recurWeek)).perform(click())

        // spinner is set to week, check visibilities
        onView(withId(R.id.edit_recur_every_x)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_every_x_numberPicker)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_days_months_spinner)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_on_weekday)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_weekly_on_chipgroup_weekdays)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_on_the_x_day_of_month)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_on_the_x_day_of_month_numberPicker)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_x_day_of_the_month)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_until_x_occurences_picker)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_until_occurences)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_last_occurence)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_last_occurence_item)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_all_occurences)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_all_occurences_items)).check(matches(isDisplayed()))
    }



    @Test
    fun journal_check_recurrence_month_recur() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity)
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        //switch tab
        scenario.onFragment {
            it.binding.icalEditTabs.selectTab(it.binding.icalEditTabs.getTabAt(IcalEditViewModel.TAB_RECURRING))
        }

        // switch the spinner to month
        onView(withId(R.id.edit_recur_days_months_spinner)).perform(click())
        onView(withText(recurMonth)).perform(click())

        // spinner is set to month, check visibilities
        onView(withId(R.id.edit_recur_every_x)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_every_x_numberPicker)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_days_months_spinner)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_on_weekday)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_weekly_on_chipgroup_weekdays)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_on_the_x_day_of_month)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_on_the_x_day_of_month_numberPicker)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_x_day_of_the_month)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_until_x_occurences_picker)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_until_occurences)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_last_occurence)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_last_occurence_item)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_all_occurences)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_all_occurences_items)).check(matches(isDisplayed()))
    }



    @Test
    fun journal_check_recurrence_year_recur() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity)
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        //switch tab
        scenario.onFragment {
            it.binding.icalEditTabs.selectTab(it.binding.icalEditTabs.getTabAt(IcalEditViewModel.TAB_RECURRING))
        }

        // switch the spinner to year
        onView(withId(R.id.edit_recur_days_months_spinner)).perform(click())
        onView(withText(recurYear)).perform(click())

        // spinner is set to year, check visibilities
        onView(withId(R.id.edit_recur_every_x)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_every_x_numberPicker)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_days_months_spinner)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_on_weekday)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_weekly_on_chipgroup_weekdays)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_on_the_x_day_of_month)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_on_the_x_day_of_month_numberPicker)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_x_day_of_the_month)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_until_x_occurences_picker)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_until_occurences)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_last_occurence)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_last_occurence_item)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_all_occurences)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_recur_all_occurences_items)).check(matches(isDisplayed()))

    }

    @Test
    fun journal_check_recurrence_day_recur() {

        val fragmentArgs = Bundle().apply {
            putParcelable("icalentity", sampleJournalEntity)
        }
        val scenario = launchFragmentInContainer<IcalEditFragment>(fragmentArgs, R.style.AppTheme, Lifecycle.State.RESUMED)

        //switch tab
        scenario.onFragment {
            it.binding.icalEditTabs.selectTab(it.binding.icalEditTabs.getTabAt(IcalEditViewModel.TAB_RECURRING))
        }

        // switch the spinner to year
        onView(withId(R.id.edit_recur_days_months_spinner)).perform(click())
        onView(withText(recurYear)).perform(click())

        // turn switch off again and check if empty like initially
        onView(withId(R.id.edit_recur_switch)).perform(click())

        onView(withId(R.id.edit_recur_every_x)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_every_x_numberPicker)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_days_months_spinner)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_on_weekday)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_weekly_on_chipgroup_weekdays)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_on_the_x_day_of_month)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_on_the_x_day_of_month_numberPicker)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_x_day_of_the_month)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_until_x_occurences_picker)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_until_occurences)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_last_occurence)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_last_occurence_item)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_all_occurences)).check(matches(not(isDisplayed())))
        onView(withId(R.id.edit_recur_all_occurences_items)).check(matches(not(isDisplayed())))
    }



    // TODO continue with more checks on recurrence
}