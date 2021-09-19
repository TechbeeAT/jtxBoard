package at.techbee.jtx.ui

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import at.techbee.jtx.database.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class IcalListViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ICalDatabaseDao
    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var icalListViewModel: IcalListViewModel

    @Before
    fun setup()   {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = ICalDatabase.getInMemoryDB(context).iCalDatabaseDao
        database.insertCollectionSync(ICalCollection(collectionId = 1L, displayName = "testcollection automated tests"))
        application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        icalListViewModel = IcalListViewModel(database, application)

    }

    @After
    fun closeDb() {
        ICalDatabase.getInMemoryDB(context).close()
    }


    @Test
    fun setFocusItem() {
        // covered by getFocusItemPosition()
    }

    @Test
    fun getFocusItemPosition() = runBlockingTest {

        icalListViewModel.iCal4List.observeForever {  }

        val id1 = database.insertICalObject(ICalObject.createTask("Test1"))
        database.insertICalObject(ICalObject.createTask("Test2"))
        database.insertICalObject(ICalObject.createTask("Test3"))

        // default search module is for Journals, we change it and update the search
        icalListViewModel.searchModule = Module.TODO.name
        icalListViewModel.updateSearch()

        icalListViewModel.setFocusItem(id1)

        //sorting is descending, the first inserted item will be at the last position (=2)
        assertEquals(2, icalListViewModel.getFocusItemPosition())

    }

    @Test
    fun resetFocusItem() {
        icalListViewModel.resetFocusItem()
        assertNull(icalListViewModel.focusItemId.value)
    }

    @Test
    fun updateSearch() {
    }

    @Test
    fun clearFilter() {
        icalListViewModel.clearFilter()
        assertEquals(0, icalListViewModel.searchCategories.size)
        assertEquals(0, icalListViewModel.searchOrganizer.size)
        assertEquals(0, icalListViewModel.searchStatusJournal.size)
        assertEquals(0, icalListViewModel.searchStatusTodo.size)
        assertEquals(0, icalListViewModel.searchClassification.size)
        assertEquals(0, icalListViewModel.searchCollection.size)
    }

    @Test
    fun updateProgress() = runBlockingTest {

        val id = database.insertICalObject(ICalObject.createTask("Test"))
        icalListViewModel.updateProgress(id, 50)
        Thread.sleep(100)
        val icalobject = database.getICalObjectById(id)

        assertEquals(50, icalobject?.percent)
    }
}