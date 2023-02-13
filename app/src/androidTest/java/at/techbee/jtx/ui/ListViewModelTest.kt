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
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.getOrAwaitValue
import at.techbee.jtx.ui.list.ListViewModel
import at.techbee.jtx.ui.list.ListViewModelJournals
import at.techbee.jtx.ui.list.ListViewModelNotes
import at.techbee.jtx.ui.list.ListViewModelTodos
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
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

    private lateinit var listViewModelJournals: ListViewModelJournals
    private lateinit var listViewModelNotes: ListViewModelNotes
    private lateinit var listViewModelTodos: ListViewModelTodos

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ICalDatabase.switchToInMemory(context)
        application = context.applicationContext as Application
        database = ICalDatabase.getInstance(context).iCalDatabaseDao
        database.insertCollectionSync(ICalCollection(collectionId = 1L, displayName = "testcollection automated tests", readonly = false, supportsVJOURNAL = true, supportsVTODO = true))
        database.insertCollectionSync(ICalCollection(collectionId = 2L, displayName = "testcollection readonly", readonly = true, supportsVJOURNAL = true, supportsVTODO = true))

        listViewModelJournals = ListViewModelJournals(application)
        listViewModelNotes = ListViewModelNotes(application)
        listViewModelTodos = ListViewModelTodos(application)

        database.deleteAllICalObjects()    // make sure welcome entries get deleted
    }

    @After
    fun closeDb()  {
        database.deleteAllICalObjects()
        ICalDatabase.getInstance(context).close()
    }


    @Test
    fun updateSearch_filter_Module_Journal() = runTest {

        listViewModel = listViewModelJournals
        listViewModel.iCal4List.observeForever {  }
        database.insertICalObject(ICalObject.createJournal())
        database.insertICalObject(ICalObject.createJournal())
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(2, listViewModel.iCal4List.value?.size)
    }


    @Test
    fun updateSearch_filter_Module_Note() = runTest {

        listViewModel = listViewModelNotes
        listViewModel.iCal4List.observeForever {  }
        database.insertICalObject(ICalObject.createNote("Note1"))
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(1, listViewModel.iCal4List.value?.size)
    }


    @Test
    fun updateSearch_filter_Module_Todo() = runTest {

        listViewModel = listViewModelTodos
        listViewModel.iCal4List.observeForever {  }
        database.insertICalObject(ICalObject.createTask("Task1"))
        database.insertICalObject(ICalObject.createTask("Task2"))
        database.insertICalObject(ICalObject.createTask("Task3"))
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(3, listViewModel.iCal4List.value?.size)
    }

    @Test
    fun updateSearch_filter_Text() = runTest {

        listViewModel = listViewModelTodos
        listViewModel.iCal4List.observeForever {  }
        database.insertICalObject(ICalObject.createTask("Task1_abc_Text"))
        database.insertICalObject(ICalObject.createTask("Task2_asdf_Text"))
        database.insertICalObject(ICalObject.createTask("Task3_abc"))
        listViewModel.listSettings.searchText.value = "%abc%"
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(2, listViewModel.iCal4List.getOrAwaitValue().size)
    }

    @Test
    fun updateSearch_filter_Categories() = runTest {

        listViewModel = listViewModelTodos
        listViewModel.iCal4List.observeForever {  }

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

        listViewModel.listSettings.searchCategories.value = listViewModel.listSettings.searchCategories.value.plus("Test1")
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(2, listViewModel.iCal4List.value?.size)

        listViewModel.listSettings.searchCategories.value = listViewModel.listSettings.searchCategories.value.plus("Whatever")
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(3, listViewModel.iCal4List.value?.size)
    }



    @Test
    fun updateSearch_filter_Collections() = runTest {

        listViewModel = listViewModelNotes
        listViewModel.iCal4List.observeForever {  }

        val col1 = database.insertCollectionSync(ICalCollection(displayName = "ABC"))
        val col2 = database.insertCollectionSync(ICalCollection(displayName = "XYZ"))

        database.insertICalObject(ICalObject(collectionId = col1))
        database.insertICalObject(ICalObject(collectionId = col1))
        database.insertICalObject(ICalObject(collectionId = col2))

        listViewModel.listSettings.searchCollection.value = listViewModel.listSettings.searchCollection.value.plus("ABC")
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(2, listViewModel.iCal4List.value?.size)

        listViewModel.listSettings.searchCollection.value = listViewModel.listSettings.searchCollection.value.plus("XYZ")
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(3, listViewModel.iCal4List.value?.size)

        listViewModel.listSettings.searchCollection.value = listViewModel.listSettings.searchCollection.value.minus("ABC")
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(1, listViewModel.iCal4List.value?.size)
    }

    @Test
    fun updateSearch_filter_StatusJournal() = runTest {

        listViewModel = listViewModelNotes
        listViewModel.iCal4List.observeForever {  }

        database.insertICalObject(ICalObject(summary="Note1", module = Module.NOTE.name, component = Component.VJOURNAL.name, status = Status.CANCELLED.status))
        database.insertICalObject(ICalObject(summary="Note2", module = Module.NOTE.name, component = Component.VJOURNAL.name, status = Status.DRAFT.status))
        database.insertICalObject(ICalObject(summary="Note3", module = Module.NOTE.name, component = Component.VJOURNAL.name, status = Status.FINAL.status))
        database.insertICalObject(ICalObject(summary="Note4", module = Module.NOTE.name, component = Component.VJOURNAL.name, status = Status.CANCELLED.status))

        listViewModel.listSettings.searchStatus.value = listViewModel.listSettings.searchStatus.value.plus(Status.DRAFT)
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(1, listViewModel.iCal4List.value?.size)

        listViewModel.listSettings.searchStatus.value = listViewModel.listSettings.searchStatus.value.plus(Status.CANCELLED)
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(3, listViewModel.iCal4List.value?.size)

        listViewModel.listSettings.searchStatus.value = listViewModel.listSettings.searchStatus.value.plus(Status.FINAL)
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(4, listViewModel.iCal4List.value?.size)
    }

    @Test
    fun updateSearch_filter_StatusTodo() = runTest {

        listViewModel = listViewModelTodos
        listViewModel.iCal4List.observeForever {  }

        database.insertICalObject(ICalObject(summary="Task1", module = Module.TODO.name, component = Component.VTODO.name, status = Status.CANCELLED.status))
        database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name,  status = Status.NEEDS_ACTION.status))
        database.insertICalObject(ICalObject(summary="Task2", module = Module.TODO.name, component = Component.VTODO.name,  status = Status.IN_PROCESS.status))
        database.insertICalObject(ICalObject(summary="Task3", module = Module.TODO.name, component = Component.VTODO.name,  status = Status.IN_PROCESS.status))
        database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name,  status = Status.COMPLETED.status))

        listViewModel.listSettings.searchStatus.value = listViewModel.listSettings.searchStatus.value.plus(Status.NEEDS_ACTION)
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(1, listViewModel.iCal4List.value?.size)

        listViewModel.listSettings.searchStatus.value = listViewModel.listSettings.searchStatus.value.plus(Status.IN_PROCESS)
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(3, listViewModel.iCal4List.value?.size)

        listViewModel.listSettings.searchStatus.value = listViewModel.listSettings.searchStatus.value.plus(Status.COMPLETED)
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(4, listViewModel.iCal4List.value?.size)
    }

    @Test
    fun updateSearch_filter_Classification() = runTest {

        listViewModel = listViewModelTodos
        listViewModel.iCal4List.observeForever {  }

        database.insertICalObject(ICalObject(summary="Task1", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PUBLIC.name))
        database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name,  classification = Classification.PUBLIC.name))
        database.insertICalObject(ICalObject(summary="Task2", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PRIVATE.name))
        database.insertICalObject(ICalObject(summary="Task3", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PRIVATE.name))
        database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.CONFIDENTIAL.name))

        listViewModel.listSettings.searchClassification.value = listViewModel.listSettings.searchClassification.value.plus(Classification.PUBLIC)
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(2, listViewModel.iCal4List.value?.size)

        listViewModel.listSettings.searchClassification.value = listViewModel.listSettings.searchClassification.value.plus(Classification.PRIVATE)
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(4, listViewModel.iCal4List.value?.size)

        listViewModel.listSettings.searchClassification.value = listViewModel.listSettings.searchClassification.value.plus(Classification.CONFIDENTIAL)
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(5, listViewModel.iCal4List.value?.size)
    }

    @Test
    fun updateProgress() = runTest {
        listViewModel = listViewModelTodos
        val id = database.insertICalObject(ICalObject.createTask("Test").apply { percent = 0 })

        withContext(Dispatchers.IO) {
            listViewModel.updateProgress(id, 50)
            Thread.sleep(100)
            val icalobject = database.getICalObjectById(id)
            assertEquals(50, icalobject?.percent)
        }
    }


    @Test
    fun deleteVisible() = runTest {

        listViewModel = listViewModelTodos
        listViewModel.iCal4List.observeForever {  }

        database.insertICalObject(ICalObject(summary="Task1", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PUBLIC.name))
        database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name,  classification = Classification.PUBLIC.name))
        database.insertICalObject(ICalObject(summary="Task2", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PRIVATE.name))
        database.insertICalObject(ICalObject(summary="Task3", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PRIVATE.name))
        database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.CONFIDENTIAL.name))

        listViewModel.listSettings.searchClassification.value = listViewModel.listSettings.searchClassification.value.plus(Classification.PUBLIC)
        listViewModel.updateSearch(isAuthenticated = false)
        assertEquals(2, listViewModel.iCal4List.value?.size)
        listViewModel.selectedEntries.addAll(listViewModel.iCal4List.value!!.map { it.id })

        withContext(Dispatchers.IO) {
            listViewModel.deleteSelected()
            Thread.sleep(100)
            listViewModel.listSettings.searchClassification.value = emptyList()
            listViewModel.updateSearch(isAuthenticated = false)
            assertEquals(3, listViewModel.iCal4List.value?.size)
        }

    }

    @Test
    fun getAllCollections() {
        listViewModel = listViewModelTodos
        listViewModel.allWriteableCollections.observeForever {  }
        assertEquals(1, listViewModel.allWriteableCollections.value?.size)
    }

    @Test
    fun getAllCategories() = runTest {
        listViewModel = listViewModelTodos
        listViewModel.allCategories.observeForever {  }

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
        assertEquals(3, listViewModel.allCategories.value?.size)
    }
}