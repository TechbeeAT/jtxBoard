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
import at.techbee.jtx.AUTHORITY_FILEPROVIDER
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalDatabaseDao
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.properties.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

//TODO
/*
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class IcalEditViewModelTest {


    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ICalDatabaseDao
    private lateinit var context: Context
    private lateinit var application: Application
    //private lateinit var icalEditViewModel: IcalEditViewModel

    private var collection1id: Long? = null    //ATTENTION! The first inserted Collection is considered as LOCAL!
    private var collection2id: Long? = null
    private var collection3id: Long? = null


    private var sampleCategory1 = Category(text = "Techbee")
    private var sampleCategory2 = Category(text = "jtxBoard")
    //private var sampleCategory3 = Category(text = "DAVx5")

    private var sampleAttachment1 = Attachment(uri = "content://$AUTHORITY_FILEPROVIDER/jtx_files/1631560872968.aac")
    private var sampleAttachment2 = Attachment(uri = "content://$AUTHORITY_FILEPROVIDER/jtx_files/1631560872969.aac")

    private var sampleAttendee1 = Attendee(caladdress = "info@techbee.at")
    private var sampleAttendee2 = Attendee(caladdress = "contact@techbee.at")
    //private var sampleAttendee3 = Attendee(caladdress = "patrick@techbee.at")

    private var sampleComment1 = Comment(text = "Comment1")
    private var sampleComment2 = Comment(text = "Comment2")
    //private var sampleComment3 = Comment(text = "Comment3")

    private var sampleResource1 = Resource(text = "Resource1")
    private var sampleResource2 = Resource(text = "Resource2")
    //private var sampleResource3 = Resource(text = "Resource3")

    private var sampleSubtask1 = ICalObject.createTask(summary = "Subtask1")
    private var sampleSubtask2 = ICalObject.createTask(summary = "Subtask2")
    private var sampleSubtask3 = ICalObject.createTask(summary = "Subtask3")


    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ICalDatabase.switchToInMemory(context)
        database = ICalDatabase.getInstance(context).iCalDatabaseDao
        collection1id = database.insertCollectionSync(ICalCollection(displayName = "testcollection_local", accountName = ICalCollection.LOCAL_ACCOUNT_TYPE, accountType = ICalCollection.LOCAL_ACCOUNT_TYPE))
        collection2id = database.insertCollectionSync(ICalCollection(displayName = "testcollection_remote", accountName = "remote", accountType = "remote"))
        collection3id = database.insertCollectionSync(ICalCollection(displayName = "testcollection_remote2", accountName = "remote2", accountType = "remote2"))
        application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        //icalEditViewModel = IcalEditViewModel(application, ICalEntity())

    }

    @After
    fun tearDown() = ICalDatabase.getInMemoryDB(context).close()



    @Test
    fun updateVisibility_TabGeneral() {

        // TODO: Make more tests like that when the tab assignments are final
        icalEditViewModel = IcalEditViewModel(application, ICalEntity())
        icalEditViewModel.activeTab.postValue(TAB_GENERAL)
        icalEditViewModel.updateVisibility()

        assertFalse(icalEditViewModel.dateVisible.value!!)
        assertFalse(icalEditViewModel.timeVisible.value!!)
        assertFalse(icalEditViewModel.addTimeVisible.value!!)
        assertFalse(icalEditViewModel.timezoneVisible.value!!)
        assertFalse(icalEditViewModel.progressVisible.value!!)
        assertFalse(icalEditViewModel.priorityVisible.value!!)
        assertFalse(icalEditViewModel.duedateVisible.value!!)
        assertFalse(icalEditViewModel.duetimeVisible.value!!)
        assertFalse(icalEditViewModel.completeddateVisible.value!!)
        assertFalse(icalEditViewModel.completedtimeVisible.value!!)
        assertFalse(icalEditViewModel.starteddateVisible.value!!)
        assertFalse(icalEditViewModel.startedtimeVisible.value!!)
    }

    @Test
    fun savingClicked_update_newEntry() = runBlockingTest {

        val updatedEntry = ICalEntity().apply {
            property.module = Module.JOURNAL.name
            property.component = Component.VJOURNAL.name
            property.summary = "New Entry"
            property.description = "New Entry Description"
            property.collectionId = collection1id!!
        }
        icalEditViewModel.iCalObjectUpdated.value = updatedEntry.property
        icalEditViewModel.categoryUpdated.add(sampleCategory1)
        icalEditViewModel.attachmentUpdated.add(sampleAttachment1)
        icalEditViewModel.attendeeUpdated.add(sampleAttendee1)
        icalEditViewModel.commentUpdated.add(sampleComment1)
        icalEditViewModel.resourceUpdated.add(sampleResource1)
        icalEditViewModel.subtaskUpdated.add(sampleSubtask1)

        icalEditViewModel.update()
        Thread.sleep(100)

        val retrievedEntry = database.get(icalEditViewModel.returnIcalObjectId.getOrAwaitValue()!!).getOrAwaitValue()

        assertEquals(updatedEntry.property.module, retrievedEntry?.property?.module)
        assertEquals(updatedEntry.property.component, retrievedEntry?.property?.component)
        assertEquals(updatedEntry.property.summary, retrievedEntry?.property?.summary)
        assertEquals(updatedEntry.property.description, retrievedEntry?.property?.description)
        assertEquals(updatedEntry.property.collectionId, retrievedEntry?.property?.collectionId)

        assertEquals(sampleCategory1.text, retrievedEntry?.categories?.get(0)!!.text)
        assertEquals(sampleAttachment1.uri, retrievedEntry.attachments?.get(0)!!.uri)
        assertEquals(sampleAttendee1.caladdress, retrievedEntry.attendees?.get(0)!!.caladdress)
        assertEquals(sampleComment1.text, retrievedEntry.comments?.get(0)!!.text)
        assertEquals(sampleResource1.text, retrievedEntry.resources?.get(0)!!.text)
        assertEquals(1, retrievedEntry.relatedto?.size)
    }

    @Test
    fun savingClicked_update_updateEntry() = runBlockingTest{

        //first make a new entry and save it
        val updatedEntry = ICalEntity().apply {
            property.module = Module.JOURNAL.name
            property.component = Component.VJOURNAL.name
            property.summary = "New Entry"
            property.description = "New Entry Description"
            property.collectionId = collection1id!!
        }
        icalEditViewModel.iCalObjectUpdated.value = updatedEntry.property
        icalEditViewModel.categoryUpdated.add(sampleCategory1)
        icalEditViewModel.attachmentUpdated.add(sampleAttachment1)
        icalEditViewModel.attendeeUpdated.add(sampleAttendee1)
        icalEditViewModel.commentUpdated.add(sampleComment1)
        icalEditViewModel.resourceUpdated.add(sampleResource1)
        icalEditViewModel.subtaskUpdated.add(sampleSubtask1)

        icalEditViewModel.update()
        Thread.sleep(100)

        val retrievedEntry = database.get(icalEditViewModel.returnIcalObjectId.value!!).getOrAwaitValue(100)


        // we create a new instance of the view model and update the values (incl. some deletes)
        icalEditViewModel = IcalEditViewModel(application, retrievedEntry!!)
        Thread.sleep(100)

        val updatedEntry2 = ICalEntity().apply {
            property.module = Module.JOURNAL.name
            property.component = Component.VJOURNAL.name
            property.summary = "New Entry edited"
            property.description = "New Entry Description edited"
            property.collectionId = collection1id!!
        }
        icalEditViewModel.iCalObjectUpdated.value = updatedEntry2.property
        icalEditViewModel.categoryUpdated.add(sampleCategory2)
        icalEditViewModel.attachmentUpdated.add(sampleAttachment2)
        icalEditViewModel.attendeeUpdated.add(sampleAttendee2)
        icalEditViewModel.commentUpdated.add(sampleComment2)
        icalEditViewModel.resourceUpdated.add(sampleResource2)
        icalEditViewModel.subtaskUpdated.add(sampleSubtask2)
        icalEditViewModel.subtaskUpdated.add(sampleSubtask3)

        icalEditViewModel.categoryUpdated.remove(sampleCategory1)
        icalEditViewModel.attachmentUpdated.remove(sampleAttachment1)
        icalEditViewModel.attendeeUpdated.remove(sampleAttendee1)
        icalEditViewModel.resourceUpdated.remove(sampleResource1)
        icalEditViewModel.commentUpdated.remove(sampleComment1)
        icalEditViewModel.subtaskDeleted.add(sampleSubtask1)

        icalEditViewModel.update()
        Thread.sleep(100)

        val retrievedEntry2 = database.getSync(icalEditViewModel.returnIcalObjectId.value!!)


        assertEquals(updatedEntry2.property.module, retrievedEntry2?.property?.module)
        assertEquals(updatedEntry2.property.component, retrievedEntry2?.property?.component)
        assertEquals(updatedEntry2.property.summary, retrievedEntry2?.property?.summary)
        assertEquals(updatedEntry2.property.description, retrievedEntry2?.property?.description)
        assertEquals(updatedEntry2.property.collectionId, retrievedEntry2?.property?.collectionId)

        assertEquals(sampleCategory2.text, retrievedEntry2?.categories?.get(0)!!.text)
        assertEquals(sampleAttachment2.uri, retrievedEntry2.attachments?.get(0)!!.uri)
        assertEquals(sampleAttendee2.caladdress, retrievedEntry2.attendees?.get(0)!!.caladdress)
        assertEquals(sampleComment2.text, retrievedEntry2.comments?.get(0)!!.text)
        assertEquals(sampleResource2.text, retrievedEntry2.resources?.get(0)!!.text)
        assertEquals(2, retrievedEntry2.relatedto?.size)
    }

    @Test
    fun deleteClicked_delete() = runBlockingTest {

        //first make a new entry and save it
        val updatedEntry = ICalEntity().apply {
            property.module = Module.JOURNAL.name
            property.component = Component.VJOURNAL.name
            property.summary = "New Entry"
            property.description = "New Entry Description"
            property.collectionId = collection1id!!
        }
        icalEditViewModel.iCalObjectUpdated.value = updatedEntry.property
        icalEditViewModel.categoryUpdated.add(sampleCategory1)
        icalEditViewModel.attachmentUpdated.add(sampleAttachment1)
        icalEditViewModel.attendeeUpdated.add(sampleAttendee1)
        icalEditViewModel.commentUpdated.add(sampleComment1)
        icalEditViewModel.resourceUpdated.add(sampleResource1)
        icalEditViewModel.subtaskUpdated.add(sampleSubtask1)

        icalEditViewModel.update()
        Thread.sleep(100)

        val retrievedEntry = database.get(icalEditViewModel.returnIcalObjectId.getOrAwaitValue()!!).getOrAwaitValue()


        // we create a new instance of the view model and delete the entry
        icalEditViewModel = IcalEditViewModel(application, retrievedEntry!!)
        Thread.sleep(200)
        icalEditViewModel.iCalObjectUpdated.getOrAwaitValue(5)
        icalEditViewModel.isChild.getOrAwaitValue(5)

        icalEditViewModel.delete()
        Thread.sleep(200)

        val shouldBeNull = database.get(retrievedEntry.property.id).getOrAwaitValue()
        Thread.sleep(100)

        assertNull(shouldBeNull)
    }


    @Test
    fun deleteClicked_delete_with_Children() = runBlockingTest {

        //first make a new entry and save it
        val parent = ICalEntity().apply {
            property.module = Module.TODO.name
            property.component = Component.VTODO.name
            property.summary = "New Entry"
            property.description = "New Entry Description"
            property.collectionId = collection1id!!
        }
        val child1 = ICalObject.createTask("Subtask 1 Layer 1")
        val child2 = ICalObject.createTask("Subtask 2 Layer 1")

        icalEditViewModel.iCalObjectUpdated.value = parent.property
        icalEditViewModel.subtaskUpdated.add(child1)
        icalEditViewModel.subtaskUpdated.add(child2)

        icalEditViewModel.update()
        Thread.sleep(100)

        val retrievedParent = database.get(icalEditViewModel.returnIcalObjectId.getOrAwaitValue()!!).getOrAwaitValue()

        // now take a Subtask and make another two subtasks (by loading the subtask in the fragment and doing the same)
        val firstChildId = retrievedParent?.relatedto?.get(0)?.linkedICalObjectId!!
        val firstChildEntry = database.get(firstChildId)

        val icalEditViewModelSubtask = IcalEditViewModel(application, firstChildEntry.getOrAwaitValue()!!)
        Thread.sleep(200)
        icalEditViewModelSubtask.iCalObjectUpdated.getOrAwaitValue(5)

        val child1x1 = ICalObject.createTask("Subtask 1 Layer 2")
        val child1x2 = ICalObject.createTask("Subtask 1 Layer 2")

        icalEditViewModelSubtask.subtaskUpdated.add(child1x1)
        icalEditViewModelSubtask.subtaskUpdated.add(child1x2)

        icalEditViewModelSubtask.update()
        Thread.sleep(100)

        val retrievedEntrySubtask = database.get(icalEditViewModelSubtask.returnIcalObjectId.getOrAwaitValue()!!).getOrAwaitValue()

        //just make sure now that the retrieved Entry is not null
        assertNotNull(retrievedEntrySubtask)

        val parentId = retrievedParent.property.id
        val child1Id = retrievedParent.relatedto?.get(0)!!.linkedICalObjectId
        val child2Id = retrievedParent.relatedto?.get(1)!!.linkedICalObjectId
        val child1x1Id = retrievedEntrySubtask?.relatedto?.get(0)!!.linkedICalObjectId
        val child1x2Id = retrievedEntrySubtask.relatedto?.get(1)!!.linkedICalObjectId

        //Delete the parent through the view model, all subtasks must also be deleted now
        val icalEditViewModelParent4Delete = IcalEditViewModel(application, retrievedParent)
        Thread.sleep(200)
        icalEditViewModelParent4Delete.isChild.getOrAwaitValue()
        icalEditViewModelParent4Delete.isChild.observeForever {  }
        icalEditViewModelParent4Delete.iCalObjectUpdated.getOrAwaitValue(5)
        icalEditViewModelParent4Delete.delete()
        Thread.sleep(200)

        assertNull(database.get(parentId).getOrAwaitValue())
        assertNull(database.get(child1Id!!).getOrAwaitValue())
        assertNull(database.get(child2Id!!).getOrAwaitValue())
        assertNull(database.get(child1x1Id!!).getOrAwaitValue())
        assertNull(database.get(child1x2Id!!).getOrAwaitValue())
    }


    @Test
    fun deleteClicked_delete_mark_deleted_with_Children() = runBlockingTest {

        //first make a new entry and save it
        val parent = ICalEntity().apply {
            property.module = Module.TODO.name
            property.component = Component.VTODO.name
            property.summary = "New Entry"
            property.description = "New Entry Description"
            property.collectionId = collection2id!!
        }
        val child1 = ICalObject.createTask("Subtask 1 Layer 1")
        val child2 = ICalObject.createTask("Subtask 2 Layer 1")

        icalEditViewModel.iCalObjectUpdated.value = parent.property
        icalEditViewModel.subtaskUpdated.add(child1)
        icalEditViewModel.subtaskUpdated.add(child2)

        icalEditViewModel.update()
        Thread.sleep(100)

        val retrievedParent = database.get(icalEditViewModel.returnIcalObjectId.getOrAwaitValue()!!).getOrAwaitValue()

        // now take a Subtask and make another two subtasks (by loading the subtask in the fragment and doing the same)
        val firstChildId = retrievedParent?.relatedto?.get(0)?.linkedICalObjectId!!
        val firstChildEntry = database.get(firstChildId)

        val icalEditViewModelSubtask = IcalEditViewModel(application, firstChildEntry.getOrAwaitValue()!!)
        Thread.sleep(200)
        icalEditViewModelSubtask.iCalObjectUpdated.getOrAwaitValue(5)

        val child1x1 = ICalObject.createTask("Subtask 1 Layer 2")
        val child1x2 = ICalObject.createTask("Subtask 1 Layer 2")

        icalEditViewModelSubtask.subtaskUpdated.add(child1x1)
        icalEditViewModelSubtask.subtaskUpdated.add(child1x2)

        icalEditViewModelSubtask.update()
        Thread.sleep(100)

        val retrievedEntrySubtask = database.get(icalEditViewModelSubtask.returnIcalObjectId.getOrAwaitValue()!!).getOrAwaitValue()

        //just make sure now that the retrieved Entry is not null
        assertNotNull(retrievedEntrySubtask)

        val parentId = retrievedParent.property.id
        val child1Id = retrievedParent.relatedto?.get(0)!!.linkedICalObjectId
        val child2Id = retrievedParent.relatedto?.get(1)!!.linkedICalObjectId
        val child1x1Id = retrievedEntrySubtask?.relatedto?.get(0)!!.linkedICalObjectId
        val child1x2Id = retrievedEntrySubtask.relatedto?.get(1)!!.linkedICalObjectId

        //Delete the parent through the view model, all subtasks must also be deleted now
        val icalEditViewModelParent4Delete = IcalEditViewModel(application, retrievedParent)
        Thread.sleep(200)
        icalEditViewModelParent4Delete.isChild.getOrAwaitValue()
        icalEditViewModelParent4Delete.isChild.observeForever {  }
        icalEditViewModelParent4Delete.iCalObjectUpdated.getOrAwaitValue(5)
        icalEditViewModelParent4Delete.delete()
        Thread.sleep(200)

        assertTrue(database.get(parentId).getOrAwaitValue()?.property?.deleted!!)
        assertTrue(database.get(child1Id!!).getOrAwaitValue()?.property?.deleted!!)
        assertTrue(database.get(child2Id!!).getOrAwaitValue()?.property?.deleted!!)
        assertTrue(database.get(child1x1Id!!).getOrAwaitValue()?.property?.deleted!!)
        assertTrue(database.get(child1x2Id!!).getOrAwaitValue()?.property?.deleted!!)
    }

    @Test
    fun updateCollection_from_Local_to_Remote() = runBlockingTest {

        //first make a new entry and save it
        val parent = ICalEntity().apply {
            property.module = Module.TODO.name
            property.component = Component.VTODO.name
            property.summary = "New Entry"
            property.description = "New Entry Description"
            property.collectionId = collection1id!!
        }
        val child1 = ICalObject.createTask("Subtask 1 Layer 1")
        child1.collectionId = collection1id!!
        val child2 = ICalObject.createTask("Subtask 2 Layer 1")
        child2.collectionId = collection1id!!


        icalEditViewModel.iCalObjectUpdated.value = parent.property
        icalEditViewModel.subtaskUpdated.add(child1)
        icalEditViewModel.subtaskUpdated.add(child2)

        icalEditViewModel.update()
        Thread.sleep(100)

        val retrievedParent = database.get(icalEditViewModel.returnIcalObjectId.getOrAwaitValue()!!).getOrAwaitValue()

        // now take a Subtask and make another two subtasks (by loading the subtask in the fragment and doing the same)
        val firstChildId = retrievedParent?.relatedto?.get(0)?.linkedICalObjectId!!
        val firstChildEntry = database.get(firstChildId)

        val icalEditViewModelSubtask = IcalEditViewModel(application, firstChildEntry.getOrAwaitValue()!!)
        Thread.sleep(200)
        icalEditViewModelSubtask.iCalObjectUpdated.getOrAwaitValue(5)

        val child1x1 = ICalObject.createTask("Subtask 1 Layer 2")
        child1x1.collectionId = collection1id!!
        val child1x2 = ICalObject.createTask("Subtask 1 Layer 2")
        child1x2.collectionId = collection1id!!


        icalEditViewModelSubtask.subtaskUpdated.add(child1x1)
        icalEditViewModelSubtask.subtaskUpdated.add(child1x2)

        icalEditViewModelSubtask.update()
        Thread.sleep(100)

        val retrievedEntrySubtask = database.get(icalEditViewModelSubtask.returnIcalObjectId.getOrAwaitValue()!!).getOrAwaitValue()
        Thread.sleep(100)

        //just make sure now that the retrieved Entry is not null
        assertNotNull(retrievedEntrySubtask)

        val parentId = retrievedParent.property.id
        val child1Id = retrievedParent.relatedto?.get(0)!!.linkedICalObjectId
        val child2Id = retrievedParent.relatedto?.get(1)!!.linkedICalObjectId
        val child1x1Id = retrievedEntrySubtask?.relatedto?.get(0)!!.linkedICalObjectId
        val child1x2Id = retrievedEntrySubtask.relatedto?.get(1)!!.linkedICalObjectId

        //Update the collection of the parent through the view model, all subtasks must also be updated now
        val icalEditViewModelParent4UpdateCollection = IcalEditViewModel(application, retrievedParent)
        Thread.sleep(200)
        icalEditViewModelParent4UpdateCollection.iCalObjectUpdated.getOrAwaitValue(5)
        icalEditViewModelParent4UpdateCollection.iCalObjectUpdated.value?.collectionId = 3L
        icalEditViewModelParent4UpdateCollection.allRelatedto.getOrAwaitValue()
        Thread.sleep(200)

        icalEditViewModelParent4UpdateCollection.update()
        Thread.sleep(200)

        // all items that were inserted before (and linked with the parent that was edited) must be deleted (as they are local)
        assertNull(database.getSync(parentId)?.property?.id)
        assertNull(database.getSync(child1Id!!)?.property?.id)
        assertNull(database.getSync(child2Id!!)?.property?.id)
        assertNull(database.getSync(child1x1Id!!)?.property?.id)
        assertNull(database.getSync(child1x2Id!!)?.property?.id)

        // now load all items again by taking the returnVJournalItemId
        val parentInNewCollection = database.getSync(icalEditViewModelParent4UpdateCollection.returnIcalObjectId.getOrAwaitValue())
        val child1InNewCollection = database.getSync(parentInNewCollection?.relatedto?.get(0)!!.linkedICalObjectId!!)
        val child2InNewCollection = database.getSync(parentInNewCollection.relatedto?.get(1)!!.linkedICalObjectId!!)
        val child1x1InNewCollection = database.getSync(child1InNewCollection?.relatedto?.get(0)!!.linkedICalObjectId!!)
        val child1x2InNewCollection = database.getSync(child1InNewCollection.relatedto?.get(1)!!.linkedICalObjectId!!)

        assertEquals(3L, parentInNewCollection.property.collectionId)
        assertEquals(3L, child1InNewCollection.property.collectionId)
        assertEquals(3L, child2InNewCollection?.property?.collectionId)
        assertEquals(3L, child1x1InNewCollection?.property?.collectionId)
        assertEquals(3L, child1x2InNewCollection?.property?.collectionId)
    }


    @Test
    fun updateCollection_from_Remote_to_Remote() = runBlockingTest {

        //first make a new entry and save it
        val parent = ICalEntity().apply {
            property.module = Module.TODO.name
            property.component = Component.VTODO.name
            property.summary = "New Entry"
            property.description = "New Entry Description"
            property.collectionId = collection2id!!
        }
        val child1 = ICalObject.createTask("Subtask 1 Layer 1")
        val child2 = ICalObject.createTask("Subtask 2 Layer 1")

        icalEditViewModel.iCalObjectUpdated.value = parent.property
        icalEditViewModel.subtaskUpdated.add(child1)
        icalEditViewModel.subtaskUpdated.add(child2)

        icalEditViewModel.update()
        Thread.sleep(200)
        val retrievedParent = database.getSync(icalEditViewModel.returnIcalObjectId.getOrAwaitValue()!!)

        // now take a Subtask and make another two subtasks (by loading the subtask in the fragment and doing the same)
        val firstChildId = retrievedParent?.relatedto?.get(0)?.linkedICalObjectId!!
        val firstChildEntry = database.getSync(firstChildId)

        val icalEditViewModelSubtask = IcalEditViewModel(application, firstChildEntry!!)
        Thread.sleep(200)
        icalEditViewModelSubtask.iCalObjectUpdated.getOrAwaitValue(5)

        val child1x1 = ICalObject.createTask("Subtask 1 Layer 2")
        val child1x2 = ICalObject.createTask("Subtask 1 Layer 2")

        icalEditViewModelSubtask.subtaskUpdated.add(child1x1)
        icalEditViewModelSubtask.subtaskUpdated.add(child1x2)

        icalEditViewModelSubtask.update()
        Thread.sleep(100)

        val retrievedEntrySubtask = database.getSync(icalEditViewModelSubtask.returnIcalObjectId.getOrAwaitValue()!!)

        //just make sure now that the retrieved Entry is not null
        assertNotNull(retrievedEntrySubtask)

        val parentId = retrievedParent.property.id
        val child1Id = retrievedParent.relatedto?.get(0)!!.linkedICalObjectId
        val child2Id = retrievedParent.relatedto?.get(1)!!.linkedICalObjectId
        val child1x1Id = retrievedEntrySubtask?.relatedto?.get(0)!!.linkedICalObjectId
        val child1x2Id = retrievedEntrySubtask.relatedto?.get(1)!!.linkedICalObjectId

        //Update the collection of the parent through the view model, all subtasks must also be updated now
        val icalEditViewModelParent4UpdateCollection = IcalEditViewModel(application, retrievedParent)
        Thread.sleep(200)
        icalEditViewModelParent4UpdateCollection.iCalObjectUpdated.getOrAwaitValue(5)
        icalEditViewModelParent4UpdateCollection.iCalObjectUpdated.value?.collectionId = 3
        icalEditViewModelParent4UpdateCollection.allRelatedto.getOrAwaitValue()
        Thread.sleep(200)

        icalEditViewModelParent4UpdateCollection.update()
        Thread.sleep(200)

        // all items that were inserted before (and linked with the parent that was edited) must be marked as deleted
        assertTrue(database.getSync(parentId)!!.property.deleted)
        assertTrue(database.getSync(child1Id!!)?.property?.deleted!!)
        assertTrue(database.getSync(child2Id!!)?.property?.deleted!!)
        assertTrue(database.getSync(child1x1Id!!)?.property?.deleted!!)
        assertTrue(database.getSync(child1x2Id!!)?.property?.deleted!!)


        // now load all (newly inserted) items by taking the returnVJournalItemId
        val parentInNewCollection = database.getSync(icalEditViewModelParent4UpdateCollection.returnIcalObjectId.getOrAwaitValue())
        val child1InNewCollection = database.getSync(parentInNewCollection?.relatedto?.get(0)!!.linkedICalObjectId!!)
        val child2InNewCollection = database.getSync(parentInNewCollection.relatedto?.get(1)!!.linkedICalObjectId!!)
        val child1x1InNewCollection = database.getSync(child1InNewCollection?.relatedto?.get(0)!!.linkedICalObjectId!!)
        val child1x2InNewCollection = database.getSync(child1InNewCollection.relatedto?.get(1)!!.linkedICalObjectId!!)

        assertEquals(3L, parentInNewCollection.property.collectionId)
        assertEquals(3L, child1InNewCollection.property.collectionId)
        assertEquals(3L, child2InNewCollection?.property?.collectionId)
        assertEquals(3L, child1x1InNewCollection?.property?.collectionId)
        assertEquals(3L, child1x2InNewCollection?.property?.collectionId)
    }
}

 */