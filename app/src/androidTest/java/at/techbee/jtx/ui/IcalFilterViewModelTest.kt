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
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalDatabaseDao
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.properties.Category
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class IcalFilterViewModelTest {


    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ICalDatabaseDao
    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var icalFilterViewModel: IcalFilterViewModel


    @Before
    fun setUp() {

        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = ICalDatabase.getInMemoryDB(context).iCalDatabaseDao
        database.insertCollectionSync(ICalCollection(displayName = "testcollection1"))
        database.insertCollectionSync(ICalCollection(displayName = "testcollection2"))
        application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        icalFilterViewModel = IcalFilterViewModel(database, application)

    }

    @After
    fun tearDown() {

        ICalDatabase.getInMemoryDB(context).close()

    }

    @Test
    fun getAllCollections() {

        icalFilterViewModel.allCollections.observeForever {  }
        assertEquals(2, icalFilterViewModel.allCollections.value?.size)
    }

    @Test
    fun getAllCategories() = runBlockingTest {

        icalFilterViewModel.allCategories.observeForever {  }

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
        assertEquals(3, icalFilterViewModel.allCategories.value?.size)

    }
}