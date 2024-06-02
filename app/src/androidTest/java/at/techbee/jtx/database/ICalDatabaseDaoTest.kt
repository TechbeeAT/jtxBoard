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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        database = ICalDatabase.getInstance(context).iCalDatabaseDao()
        database.insertCollectionSync(
            ICalCollection(
                collectionId = 1L,
                displayName = "testcollection automated tests"
            )
        )
        database.insertCollectionSync(
            ICalCollection(
                collectionId = 2L,
                accountType = "remote",
                accountName = "remote",
                displayName = "testcollection automated tests"
            )
        )
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

        val retrievedEntry = database.getSync(preparedEntry.id)
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

        val retrievedEntry = database.getSync(preparedICalObject.id)
        assertEquals(retrievedEntry, preparedIcalEntity)
    }



    @Test
    fun recreateRecurring_journal() = runTest {

        val item = ICalObject.createJournal().apply {
            // from  fun getInstancesFromRrule_Journal_WEEKLY_withExceptions()
            this.collectionId = 1L
            this.dtstart = 1622541600000L
            this.dtstartTimezone = "Europe/Vienna"
            this.rrule = "FREQ=DAILY;COUNT=8;INTERVAL=2;BYDAY=TU,FR,SA,SU"
        }

        val id = database.insertICalObject(item)
        val savedItem = database.getICalObjectByIdSync(id) ?: throw AssertionError("Inserted ICalObject  not found")
        database.recreateRecurring(savedItem)

        val recurList = database.getRecurInstances(uid = savedItem.uid)
        assertEquals(8, recurList.size)

        database.deleteUnchangedRecurringInstances(savedItem.uid)
        val recurListEmpty = database.getRecurInstances(savedItem.uid)
        assertEquals(0, recurListEmpty.size)
    }

    @Test
    fun recreateRecurring_todo() = runTest {

        val item = ICalObject.createTodo().apply {
            // from  fun getInstancesFromRrule_Journal_WEEKLY_withExceptions()
            this.dtstart = 1663718400000L
            this.dtstartTimezone = "Europe/Vienna"
            this.due = 1663804800000L
            this.dueTimezone = "Europe/Vienna"
            this.rrule = "FREQ=WEEKLY;COUNT=6;INTERVAL=2;BYDAY=WE,SU,MO"
            this.exdate = "1663718400000"
            this.rdate = "1664496000000,1664575200000"
        }

        val id = database.insertICalObject(item)
        val savedItem = database.getICalObjectByIdSync(id) ?: throw AssertionError("Inserted ICalObject  not found")
        database.recreateRecurring(savedItem)

        val recurList = database.getRecurInstances(savedItem.uid)
        assertEquals(7, recurList.size)

        database.deleteUnchangedRecurringInstances(savedItem.uid)
        val recurListEmpty = database.getRecurInstances(savedItem.uid)
        assertEquals(0, recurListEmpty.size)
    }


    @Test
    fun unlink_from_series() = runTest {

        val item = ICalObject.createTodo().apply {
            // from  fun getInstancesFromRrule_Journal_WEEKLY_withExceptions()
            this.dtstart = 1663718400000L
            this.due = 1663804800000L
            this.rrule = "FREQ=DAILY;COUNT=5"
            this.uid = "series"
        }

        val id = database.insertICalObject(item)
        val savedItem = database.getICalObjectByIdSync(id) ?: throw AssertionError("Inserted ICalObject  not found")
        database.recreateRecurring(savedItem)

        val recurList = database.getRecurInstances(savedItem.uid)
        assertEquals(5, recurList.size)

        assertEquals(1663718400000L, recurList[0].dtstart)
        val firstInstance = database.getRecurInstance("series", ICalObject.getAsRecurId(recurList[0].dtstart!!, recurList[0].dtstartTimezone)) ?: throw AssertionError("firstInstance not found")
        database.unlinkFromSeries(listOf(firstInstance), savedItem, false)

        val firstInstanceAfter = database.getICalObjectByIdSync(firstInstance.id) ?: throw AssertionError("firstInstance not found")
        Assert.assertNotEquals("series", firstInstanceAfter.uid)
        Assert.assertNull(firstInstanceAfter.recurid)
    }



    @Test
    fun updateProgressOfParents_keepInSync() = runBlocking {
        val parentId = database.insertICalObject(ICalObject.createTodo().apply { uid = "parent" })
        val child1Id = database.insertICalObject(ICalObject.createTodo().apply { uid = "child1" })
        val child2Id = database.insertICalObject(ICalObject.createTodo().apply { uid = "child2" })

        database.getICalObjectByIdSync(child1Id)?.let {
            it.setUpdatedProgress(25, true)
            database.update(it)
        }
        database.getICalObjectByIdSync(child2Id)?.let {
            it.setUpdatedProgress(75, true)
            database.update(it)
        }
        database.insertRelatedtoSync(Relatedto(icalObjectId = child1Id, reltype =  Reltype.PARENT.name, text = "parent"))
        database.insertRelatedtoSync(Relatedto(icalObjectId = child2Id, reltype =  Reltype.PARENT.name, text = "parent"))

        //ICalObject.updateProgressOfParents(parentId, database, true)
        database.updateProgress(child1Id, 25, true, true)

        val parent = database.getICalObjectByIdSync(parentId)
        assertEquals(50, parent?.percent)
        assertEquals(Status.IN_PROCESS.status, parent?.status)
    }


    @Test
    fun updateProgressOfParents_NOT_keepInSync() = runBlocking {
        val parentId = database.insertICalObject(ICalObject.createTodo().apply { uid = "parent"; percent = 11 })
        val child1Id = database.insertICalObject(ICalObject.createTodo().apply { uid = "child1" })
        val child2Id = database.insertICalObject(ICalObject.createTodo().apply { uid = "child2" })

        database.getICalObjectByIdSync(child1Id)?.let {
            it.setUpdatedProgress(25, true)
            database.update(it)
        }
        database.getICalObjectByIdSync(child2Id)?.let {
            it.setUpdatedProgress(75, true)
            database.update(it)
        }
        database.insertRelatedtoSync(Relatedto(icalObjectId = child1Id, reltype =  Reltype.PARENT.name, text = "parent"))
        database.insertRelatedtoSync(Relatedto(icalObjectId = child2Id, reltype =  Reltype.PARENT.name, text = "parent"))

        database.updateProgress(child1Id, 25, false, false)
        //ICalObject.updateProgressOfParents(parentId, database, false)
        val parent = database.getICalObjectByIdSync(parentId)
        assertEquals(11, parent?.percent)
        assertEquals(Status.NO_STATUS.status, parent?.status)
    }


    @Test
    fun findTopParent_Test() = runBlocking {
        val parentId = database.insertICalObject(ICalObject.createTodo().apply { uid = "parent" })
        val child1Id = database.insertICalObject(ICalObject.createTodo().apply { uid = "child1" })
        val child1of1Id = database.insertICalObject(ICalObject.createTodo().apply { uid = "child1of1Id" })
        val child2of1Id = database.insertICalObject(ICalObject.createTodo().apply { uid = "child2of1Id" })
        val child1of2of1Id = database.insertICalObject(ICalObject.createTodo().apply { uid = "child1of2of1Id" })
        val child2Id = database.insertICalObject(ICalObject.createTodo().apply { uid = "child2" })
        database.insertRelatedtoSync(Relatedto(icalObjectId = child1Id, reltype =  Reltype.PARENT.name, text = "parent"))
        database.insertRelatedtoSync(Relatedto(icalObjectId = child2Id, reltype =  Reltype.PARENT.name, text = "parent"))
        database.insertRelatedtoSync(Relatedto(icalObjectId = child1of1Id, reltype =  Reltype.PARENT.name, text = "child1"))
        database.insertRelatedtoSync(Relatedto(icalObjectId = child2of1Id, reltype =  Reltype.PARENT.name, text = "child1"))
        database.insertRelatedtoSync(Relatedto(icalObjectId = child1of2of1Id, reltype =  Reltype.PARENT.name, text = "child2of1Id"))

        val actualParent = database.getICalObjectById(parentId) ?: throw AssertionError("entry not found")
        val retrievedParent = database.findTopParent(parentId) ?: throw AssertionError("retrieved entry null")
        assertEquals(actualParent, retrievedParent)
    }

    @Test
    fun findTopParent_Test_entry_links_itself() = runBlocking {
        val parentId = database.insertICalObject(ICalObject.createTodo().apply { uid = "parent" })
        database.insertRelatedtoSync(Relatedto(icalObjectId = parentId, reltype =  Reltype.PARENT.name, text = "parent"))
        Assert.assertNull(database.findTopParent(parentId))
    }

    @Test
    fun findTopParent_Test_multiple_parents() = runBlocking {
        database.insertICalObject(ICalObject.createTodo().apply { uid = "parent" })
        database.insertICalObject(ICalObject.createTodo().apply { uid = "parent2" })
        val child1Id = database.insertICalObject(ICalObject.createTodo().apply { uid = "child1" })
        database.insertRelatedtoSync(Relatedto(icalObjectId = child1Id, reltype =  Reltype.PARENT.name, text = "parent"))
        database.insertRelatedtoSync(Relatedto(icalObjectId = child1Id, reltype =  Reltype.PARENT.name, text = "parent2"))
        Assert.assertNull(database.findTopParent(child1Id))
    }



    @Test
    fun deleteItemWithChildren_LocalCollection() = runTest {
        val parent = ICalObject.createJournal().apply { this.collectionId = 1L }
        withContext(Dispatchers.IO) {
            val idParent = database.insertICalObject(parent)
            val idChild1 = database.insertICalObject(
                ICalObject.createJournal().apply { this.collectionId = 1L })
            val idChild2 = database.insertICalObject(
                ICalObject.createJournal().apply { this.collectionId = 1L })
            val idChild3 = database.insertICalObject(
                ICalObject.createJournal().apply { this.collectionId = 1L })

            database.insertRelatedto(Relatedto().apply {
                icalObjectId = idChild1
                text = parent.uid
                reltype = Reltype.PARENT.name
            })
            database.insertRelatedto(Relatedto().apply {
                icalObjectId = idChild2
                text = parent.uid
                reltype = Reltype.PARENT.name
            })
            database.insertRelatedto(Relatedto().apply {
                icalObjectId = idChild3
                text = parent.uid
                reltype = Reltype.PARENT.name
            })

            //make sure everything was correctly inserted
            assertEquals(3, database.getAllRelatedtoSync().size)

            database.deleteICalObject(idParent)

            assertEquals(0, database.getAllRelatedtoSync().size)
            assertEquals(null, database.getSync(idParent))
            assertEquals(null, database.getSync(idChild1))
            assertEquals(null, database.getSync(idChild2))
            assertEquals(null, database.getSync(idChild3))
        }
    }


    @Test
    fun deleteItemWithChildren_LocalCollection_multiple_parents() = runTest {
        val parent1 = ICalObject.createJournal().apply { this.collectionId = 1L; this.uid = "parent1" }
        val parent2 = ICalObject.createJournal().apply { this.collectionId = 1L; this.uid = "parent2" }
            val idParent1 = database.insertICalObject(parent1)
            database.insertICalObject(parent2)
            val idChild = database.insertICalObject(
                ICalObject.createJournal().apply { this.collectionId = 1L })

            database.insertRelatedto(Relatedto().apply {
                icalObjectId = idChild
                text = parent1.uid
                reltype = Reltype.PARENT.name
            })
            database.insertRelatedto(Relatedto().apply {
                icalObjectId = idChild
                text = parent2.uid
                reltype = Reltype.PARENT.name
            })
            assertEquals(2, database.getAllRelatedtoSync().size)

            database.deleteICalObject(idParent1)

            assertEquals(1, database.getAllRelatedtoSync().size)
            Assert.assertNull(database.getSync(idParent1))
            assertNotNull(database.getSync(idChild))

    }

    @Test
    fun deleteItemWithChildren_RemoteCollection() = runTest {
        val parent = ICalObject.createJournal().apply { this.collectionId = 2L }
            val idParent = database.insertICalObject(parent)
            val idChild1 = database.insertICalObject(
                ICalObject.createJournal().apply { this.collectionId = 2L })
            val idChild2 = database.insertICalObject(
                ICalObject.createJournal().apply { this.collectionId = 2L })
            val idChild3 = database.insertICalObject(
                ICalObject.createJournal().apply { this.collectionId = 2L })

            database.insertRelatedto(Relatedto().apply {
                icalObjectId = idChild1
                text = parent.uid
                reltype = Reltype.PARENT.name
            })
            database.insertRelatedto(Relatedto().apply {
                icalObjectId = idChild2
                text = parent.uid
                reltype = Reltype.PARENT.name
            })
            database.insertRelatedto(Relatedto().apply {
                icalObjectId = idChild3
                text = parent.uid
                reltype = Reltype.PARENT.name
            })
            //make sure everything was correctly inserted
            assertEquals(3, database.getAllRelatedtoSync().size)

            database.deleteICalObject(idParent)

            Assert.assertTrue(database.getSync(idParent)?.property?.deleted!!)
            Assert.assertTrue(database.getSync(idChild1)?.property?.deleted!!)
            Assert.assertTrue(database.getSync(idChild2)?.property?.deleted!!)
            Assert.assertTrue(database.getSync(idChild3)?.property?.deleted!!)

    }


    @Test
    fun deleteItemWithChildren_RecurringInstance_Local() = runTest {
        //Local or remote should not make a difference, the recurring instance must be deleted anyway
        withContext(Dispatchers.IO) {
            val idParent = database.insertICalObject(ICalObject.createJournal().apply {
                this.collectionId = 1L
                this.recurid = "recurid"
            })
            // a recurring instance cannot have children
            //make sure everything was correctly inserted

            assertNotNull(database.getSync(idParent)?.property)
            database.deleteICalObject(idParent)
            Assert.assertNull(database.getSync(idParent)?.property)
        }
    }

    @Test
    fun deleteItemWithChildren_RecurringInstance_Remote() = runTest {
        //Local or remote should not make a difference, the recurring instance must be deleted anyway
            val idParent = database.insertICalObject(ICalObject.createJournal().apply {
                this.collectionId = 2L
                this.recurid = "recurid"
            })
            // a recurring instance cannot have children
            //make sure everything was correctly inserted

            assertNotNull(database.getSync(idParent)?.property)
            database.deleteICalObject(idParent)
            Assert.assertNull(database.getSync(idParent)?.property)
    }

    @Test
    fun updateCollectionWithChildren_test() = runTest {

            val parent = ICalObject.createJournal().apply { this.collectionId = 1L }
                val idParent = database.insertICalObject(parent)
                val idChild1 = database.insertICalObject(
                    ICalObject.createJournal().apply { this.collectionId = 1L })
                val idChild2 = database.insertICalObject(
                    ICalObject.createJournal().apply { this.collectionId = 1L })
                val idChild3 = database.insertICalObject(
                    ICalObject.createJournal().apply { this.collectionId = 1L })

                database.insertRelatedto(Relatedto().apply {
                    icalObjectId = idChild1
                    text = parent.uid
                    reltype = Reltype.PARENT.name
                })
                database.insertRelatedto(Relatedto().apply {
                    icalObjectId = idChild2
                    text = parent.uid
                    reltype = Reltype.PARENT.name
                })
                database.insertRelatedto(Relatedto().apply {
                    icalObjectId = idChild3
                    text = parent.uid
                    reltype = Reltype.PARENT.name
                })

                //make sure everything was correctly inserted
                assertEquals(3, database.getAllRelatedtoSync().size)

                val newParentId = database.moveToCollection(idParent, 2L)
                    ?: throw AssertionError("newParentId not returned")
                Thread.sleep(500)

                val newParent = database.getICalObjectById(newParentId)
                Thread.sleep(100)
                assertEquals(2L, newParent?.collectionId)

                val children = database.getRelatedChildren(newParent?.id ?: 0L)
                assertEquals(3, children.size)
                assertEquals(2L, database.getICalObjectById(children[0].id)?.collectionId)
                assertEquals(2L, database.getICalObjectById(children[1].id)?.collectionId)


    }

}