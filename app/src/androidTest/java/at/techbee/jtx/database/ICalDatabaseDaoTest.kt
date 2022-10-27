/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class ICalDatabaseDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ICalDatabaseDao
    private lateinit var context: Context
    //private lateinit var db: ICalDatabase

    @Before
    fun createDb() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ICalDatabase.switchToInMemory(context)
        database = ICalDatabase.getInstance(context).iCalDatabaseDao
        database.insertCollectionSync(ICalCollection(collectionId = 1L, displayName = "testcollection automated tests"))
    }



    @Test
    fun insertAndCount() = runTest {
        assertEquals(database.getCount(), 0)
        database.insertICalObject(ICalObject.createJournal())
        assertEquals(database.getCount(), 1)

        //assertEquals(vJournalItem, retrievedItem)
    }

    @Test
    fun insert_and_retrieve_ICalObject() = runTest {
        val preparedEntry = ICalObject.createNote("myTestJournal")
        preparedEntry.id = database.insertICalObject(preparedEntry)

        val retrievedEntry = database.get(preparedEntry.id).getOrAwaitValue()
        assertEquals(retrievedEntry?.property, preparedEntry)
    }


    @Test
    fun insert_and_retrieve_ICalEntityObject() = runTest {
        val preparedICalObject = ICalObject(dtstart = System.currentTimeMillis(), summary="myTestJournal")
        preparedICalObject.id = database.insertICalObject(preparedICalObject)

        val preparedAttendee = Attendee(icalObjectId = preparedICalObject.id, caladdress = "mailto:test@test.net")
        val preparedAttachment = Attachment(icalObjectId = preparedICalObject.id, uri = "https://localhost/")
        val preparedCategory = Category(icalObjectId = preparedICalObject.id, text = "category")
        val preparedComment = Comment(icalObjectId = preparedICalObject.id, text = "comment")
        //val preparedContact = Contact(icalObjectId = preparedEntry.id, text = "contact")
        val preparedOrganizer = Organizer(icalObjectId = preparedICalObject.id, caladdress = "mailto:test@test.net")
        val preparedResource = Resource(icalObjectId = preparedICalObject.id, text = "resource")

        val preparedSubNote = ICalObject.createNote("Subnote")
        preparedSubNote.id = database.insertICalObject(preparedSubNote)
        val preparedRelatedto = Relatedto(icalObjectId = preparedICalObject.id, reltype = "parent")

        preparedAttendee.attendeeId = database.insertAttendee(preparedAttendee)
        preparedAttachment.attachmentId = database.insertAttachment(preparedAttachment)
        preparedCategory.categoryId = database.insertCategory(preparedCategory)
        preparedComment.commentId = database.insertComment(preparedComment)
        //database.insertContact(preparedContact)
        preparedOrganizer.organizerId = database.insertOrganizer(preparedOrganizer)
        preparedResource.resourceId = database.insertResource(preparedResource)
        preparedRelatedto.relatedtoId = database.insertRelatedto(preparedRelatedto)

        val preparedIcalEntity = ICalEntity(preparedICalObject, listOf(preparedComment), listOf(preparedCategory), listOf(preparedAttendee), preparedOrganizer, listOf(preparedRelatedto), listOf(preparedResource), listOf(preparedAttachment), listOf(), listOf())
        preparedIcalEntity.ICalCollection = database.getCollectionByIdSync(1L)

        val retrievedEntry = database.get(preparedICalObject.id).getOrAwaitValue()
        assertEquals(retrievedEntry, preparedIcalEntity)
    }
}