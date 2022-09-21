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
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import at.techbee.jtx.database.*
import at.techbee.jtx.ui.list.ListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class ListViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ICalDatabaseDao
    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var listViewModel: ListViewModel

    @Before
    fun setup()   {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ICalDatabase.switchToInMemory(context)
        application = context.applicationContext as Application
        //icalListViewModel = IcalListViewModel(application)
        database = ICalDatabase.getInstance(context).iCalDatabaseDao
        database.insertCollectionSync(ICalCollection(collectionId = 1L, displayName = "testcollection automated tests"))
    }

    @After
    fun closeDb() {
        ICalDatabase.getInstance(context).close()
    }


    @Test
    fun setFocusItem() {
        // covered by getFocusItemPosition()
    }

    /*

    @Test
    fun updateSearch_filter_Module_Journal() = runTest {

        icalListViewModel.iCal4ListJournals.observeForever {  }
        database.insertICalObject(ICalObject.createJournal())
        database.insertICalObject(ICalObject.createJournal())

        icalListViewModel.searchModule = Module.JOURNAL.name
        icalListViewModel.updateSearch()
        assertEquals(2, icalListViewModel.iCal4ListJournals.getOrAwaitValue(100).size)
    }

    @Test
    fun updateSearch_filter_Module_Note() = runTest {

        icalListViewModel.iCal4ListNotes.observeForever {  }
        database.insertICalObject(ICalObject.createNote("Note1"))
        icalListViewModel.searchModule = Module.NOTE.name
        icalListViewModel.updateSearch()
        assertEquals(1, icalListViewModel.iCal4ListNotes.value?.size)
    }


    @Test
    fun updateSearch_filter_Module_Todo() = runTest {

        icalListViewModel.iCal4ListTodos.observeForever {  }
        database.insertICalObject(ICalObject.createTask("Task1"))
        database.insertICalObject(ICalObject.createTask("Task2"))
        database.insertICalObject(ICalObject.createTask("Task3"))
        icalListViewModel.searchModule = Module.TODO.name
        icalListViewModel.updateSearch()
        assertEquals(3, icalListViewModel.iCal4ListTodos.value?.size)
    }

    @Test
    fun updateSearch_filter_Text() = runTest {

        icalListViewModel.iCal4ListTodos.observeForever {  }
        database.insertICalObject(ICalObject.createTask("Task1_abc_Text"))
        database.insertICalObject(ICalObject.createTask("Task2_asdf_Text"))
        database.insertICalObject(ICalObject.createTask("Task3_abc"))

        icalListViewModel.searchModule = Module.TODO.name
        icalListViewModel.searchText = "%abc%"
        icalListViewModel.updateSearch()
        assertEquals(2, icalListViewModel.iCal4ListTodos.value?.size)
    }

    @Test
    fun updateSearch_filter_Categories() = runTest {

        icalListViewModel.iCal4ListTodos.observeForever {  }
        icalListViewModel.iCal4ListNotes.observeForever {  }
        icalListViewModel.iCal4ListJournals.observeForever {  }


        val id1 = database.insertICalObject(ICalObject.createTask("Task1"))
        database.insertCategory(Category(icalObjectId = id1, text = "Test1"))
        val id2 = database.insertICalObject(ICalObject.createTask("Task2"))
        database.insertCategory(Category(icalObjectId = id2, text = "Test1"))
        database.insertCategory(Category(icalObjectId = id2, text = "Whatever"))
        database.insertCategory(Category(icalObjectId = id2, text = "No matter"))
        val id3 = database.insertICalObject(ICalObject.createTask("Task3"))
        database.insertCategory(Category(icalObjectId = id3, text = "Whatever"))
        database.insertCategory(Category(icalObjectId = id3, text = "No matter"))
        database.insertICalObject(ICalObject.createTask("Task4"))  // val id4 = ...
        // no categories for id4

        icalListViewModel.searchModule = Module.TODO.name
        icalListViewModel.searchCategories.add("Test1")
        icalListViewModel.updateSearch()
        assertEquals(2, icalListViewModel.iCal4ListTodos.value?.size)

        icalListViewModel.searchCategories.add("Whatever")
        icalListViewModel.updateSearch()
        assertEquals(3, icalListViewModel.iCal4ListTodos.value?.size)
    }



    @Test
    fun updateSearch_filter_Collections() = runTest {

        icalListViewModel.iCal4ListNotes.observeForever {  }

        val col1 = database.insertCollectionSync(ICalCollection(displayName = "ABC"))
        val col2 = database.insertCollectionSync(ICalCollection(displayName = "XYZ"))

        database.insertICalObject(ICalObject(collectionId = col1))
        database.insertICalObject(ICalObject(collectionId = col1))
        database.insertICalObject(ICalObject(collectionId = col2))

        icalListViewModel.searchModule = Module.NOTE.name
        icalListViewModel.searchCollection.add("ABC")
        icalListViewModel.updateSearch()
        assertEquals(2, icalListViewModel.iCal4ListNotes.value?.size)

        icalListViewModel.searchModule = Module.NOTE.name
        icalListViewModel.searchCollection.add("XYZ")
        icalListViewModel.updateSearch()
        assertEquals(3, icalListViewModel.iCal4ListNotes.value?.size)

        icalListViewModel.searchModule = Module.NOTE.name
        icalListViewModel.searchCollection.clear()
        icalListViewModel.searchCollection.add("XYZ")
        icalListViewModel.updateSearch()
        assertEquals(1, icalListViewModel.iCal4ListNotes.value?.size)
    }

    @Test
    fun updateSearch_filter_Organizer() = runTest {

        icalListViewModel.iCal4ListTodos.observeForever {  }

        val id1 = database.insertICalObject(ICalObject.createTask("Task1"))
        database.insertOrganizer(Organizer(icalObjectId = id1, caladdress = "Organizer1@techbee.at"))
        val id2 = database.insertICalObject(ICalObject.createTask("Task2"))
        database.insertOrganizer(Organizer(icalObjectId = id2, caladdress = "Organizer1@techbee.at"))
        database.insertOrganizer(Organizer(icalObjectId = id2, caladdress = "rezinagrO@techbee.at"))
        val id3 = database.insertICalObject(ICalObject.createTask("Task3"))
        database.insertOrganizer(Organizer(icalObjectId = id3, caladdress = "rezinagrO@techbee.at"))
        database.insertICalObject(ICalObject.createTask("Task4"))  // val id4 =
        // no organizer for id4

        icalListViewModel.searchModule = Module.TODO.name
        icalListViewModel.searchOrganizer.add("Organizer1@techbee.at")
        icalListViewModel.updateSearch()
        assertEquals(2, icalListViewModel.iCal4ListTodos.value?.size)

        icalListViewModel.searchOrganizer.add("rezinagrO@techbee.at")
        icalListViewModel.updateSearch()
        assertEquals(3, icalListViewModel.iCal4ListTodos.value?.size)
    }

    @Test
    fun updateSearch_filter_StatusJournal() = runTest {

        icalListViewModel.iCal4ListNotes.observeForever {  }

        database.insertICalObject(ICalObject(summary="Note1", module = Module.NOTE.name, component = Component.VJOURNAL.name, status = StatusJournal.CANCELLED.name))
        database.insertICalObject(ICalObject(summary="Note2", module = Module.NOTE.name, component = Component.VJOURNAL.name, status = StatusJournal.DRAFT.name))
        database.insertICalObject(ICalObject(summary="Note3", module = Module.NOTE.name, component = Component.VJOURNAL.name, status = StatusJournal.FINAL.name))
        database.insertICalObject(ICalObject(summary="Note4", module = Module.NOTE.name, component = Component.VJOURNAL.name, status = StatusJournal.CANCELLED.name))

        icalListViewModel.searchModule = Module.NOTE.name
        icalListViewModel.searchStatusJournal.add(StatusJournal.DRAFT)
        icalListViewModel.updateSearch()
        assertEquals(1, icalListViewModel.iCal4ListNotes.value?.size)

        icalListViewModel.searchStatusJournal.add(StatusJournal.CANCELLED)
        icalListViewModel.updateSearch()
        assertEquals(3, icalListViewModel.iCal4ListNotes.value?.size)

        icalListViewModel.searchStatusJournal.add(StatusJournal.FINAL)
        icalListViewModel.updateSearch()
        assertEquals(4, icalListViewModel.iCal4ListNotes.value?.size)
    }

    @Test
    fun updateSearch_filter_StatusTodo() = runTest {

        icalListViewModel.iCal4ListTodos.observeForever {  }

        database.insertICalObject(ICalObject(summary="Task1", module = Module.TODO.name, component = Component.VTODO.name, status = StatusTodo.CANCELLED.name))
        database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name,  status = StatusTodo.`NEEDS-ACTION`.name))
        database.insertICalObject(ICalObject(summary="Task2", module = Module.TODO.name, component = Component.VTODO.name,  status = StatusTodo.`IN-PROCESS`.name))
        database.insertICalObject(ICalObject(summary="Task3", module = Module.TODO.name, component = Component.VTODO.name,  status = StatusTodo.`IN-PROCESS`.name))
        database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name,  status = StatusTodo.COMPLETED.name))

        icalListViewModel.searchModule = Module.TODO.name
        icalListViewModel.searchStatusTodo.add(StatusTodo.`NEEDS-ACTION`)
        icalListViewModel.updateSearch()
        assertEquals(1, icalListViewModel.iCal4ListTodos.value?.size)

        icalListViewModel.searchStatusTodo.add(StatusTodo.`IN-PROCESS`)
        icalListViewModel.updateSearch()
        assertEquals(3, icalListViewModel.iCal4ListTodos.value?.size)

        icalListViewModel.searchStatusTodo.add(StatusTodo.COMPLETED)
        icalListViewModel.updateSearch()
        assertEquals(4, icalListViewModel.iCal4ListTodos.value?.size)
    }

    @Test
    fun updateSearch_filter_Classification() = runTest {

        icalListViewModel.iCal4ListTodos.observeForever {  }

        database.insertICalObject(ICalObject(summary="Task1", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PUBLIC.name))
        database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name,  classification = Classification.PUBLIC.name))
        database.insertICalObject(ICalObject(summary="Task2", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PRIVATE.name))
        database.insertICalObject(ICalObject(summary="Task3", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PRIVATE.name))
        database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.CONFIDENTIAL.name))

        icalListViewModel.searchModule = Module.TODO.name
        icalListViewModel.searchClassification.add(Classification.PUBLIC)
        icalListViewModel.updateSearch()
        assertEquals(2, icalListViewModel.iCal4ListTodos.value?.size)

        icalListViewModel.searchClassification.add(Classification.PRIVATE)
        icalListViewModel.updateSearch()
        assertEquals(4, icalListViewModel.iCal4ListTodos.value?.size)

        icalListViewModel.searchClassification.add(Classification.CONFIDENTIAL)
        icalListViewModel.updateSearch()
        assertEquals(5, icalListViewModel.iCal4ListTodos.value?.size)
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
    fun updateProgress() = runTest {

        val id = database.insertICalObject(ICalObject.createTask("Test"))
        icalListViewModel.updateProgress(id, 50, false)
        Thread.sleep(100)
        val icalobject = database.getICalObjectById(id)

        assertEquals(50, icalobject?.percent)
    }

    @Test
    fun updateProgress_withUnlink() = runTest {

        val item = ICalObject.createTask("Test")
        item.isRecurLinkedInstance = true

        val id = database.insertICalObject(item)
        icalListViewModel.updateProgress(id, 50, false)
        Thread.sleep(100)
        val icalobject = database.getICalObjectById(id)

        assertEquals(50, icalobject?.percent)
        assertEquals(false, icalobject?.isRecurLinkedInstance)
    }



    @Test
    fun deleteVisible() = runTest {

        icalListViewModel.iCal4ListTodos.observeForever {  }

        val id1 = database.insertICalObject(ICalObject(summary="Task1", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PUBLIC.name))
        val id2 = database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name,  classification = Classification.PUBLIC.name))
        database.insertICalObject(ICalObject(summary="Task2", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PRIVATE.name))
        database.insertICalObject(ICalObject(summary="Task3", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PRIVATE.name))
        database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.CONFIDENTIAL.name))

        icalListViewModel.searchModule = Module.TODO.name
        icalListViewModel.searchClassification.add(Classification.PUBLIC)
        icalListViewModel.updateSearch()
        assertEquals(2, icalListViewModel.iCal4ListTodos.value?.size)

        icalListViewModel.delete(listOf(id1, id2))
        Thread.sleep(100)

        icalListViewModel.searchClassification.clear()
        icalListViewModel.updateSearch()
        assertEquals(3, icalListViewModel.iCal4ListTodos.value?.size)
    }

    @Test
    fun getAllCollections() {

        icalListViewModel.allCollections.observeForever {  }
        assertEquals(1, icalListViewModel.allCollections.value?.size)
    }

    @Test
    fun getAllCategories() = runTest {

        icalListViewModel.allCategories.observeForever {  }

        val id1 = database.insertICalObject(ICalObject.createTask("Task1"))
        database.insertCategory(Category(icalObjectId = id1, text = "Test1"))
        val id2 = database.insertICalObject(ICalObject.createTask("Task2"))
        database.insertCategory(Category(icalObjectId = id2, text = "Test1"))
        database.insertCategory(Category(icalObjectId = id2, text = "Whatever"))
        database.insertCategory(Category(icalObjectId = id2, text = "No matter"))
        val id3 = database.insertICalObject(ICalObject.createTask("Task3"))
        database.insertCategory(Category(icalObjectId = id3, text = "Whatever"))
        database.insertCategory(Category(icalObjectId = id3, text = "No matter"))
        database.insertICalObject(ICalObject.createTask("Task4"))   // val id4 =

        // only 3 should be returned as the query selects only DISTINCT values!
        assertEquals(3, icalListViewModel.allCategories.value?.size)
    }

     */
}