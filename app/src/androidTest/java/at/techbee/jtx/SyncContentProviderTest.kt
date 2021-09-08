/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx

import android.accounts.Account
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import at.techbee.jtx.JtxContract.asSyncAdapter
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.TABLE_NAME_ICALOBJECT
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@SmallTest
class SyncContentProviderTest {

    private var mContentResolver: ContentResolver? = null

    private var testaccount = Account("testAccount", "testAccount")


    @Before
    fun setUp() {
        //val context: Context = ApplicationProvider.getApplicationContext()
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        ICalDatabase.switchToInMemory(context)
        mContentResolver = context.contentResolver
    }



    @Test
    fun icalObject_insert_find_delete()  {
        //prepare
        val newCollection = insertCollection(testaccount, null, null)
        val newCollectionId = newCollection?.lastPathSegment?.toLongOrNull()

        //insert
        val newICalObject = insertIcalObject(testaccount, "note2delete", newCollectionId!!)

        //find
        val cursor: Cursor? = mContentResolver?.query(newICalObject!!, arrayOf(JtxContract.JtxICalObject.ID, JtxContract.JtxICalObject.SUMMARY), null, null, null)
        assertNotNull(cursor)
        assertEquals(cursor?.count, 1)
        cursor?.close()

        //update
        val updatedContentValues = ContentValues()
        updatedContentValues.put(JtxContract.JtxICalObject.DESCRIPTION, "description was updated")
        val countUpdated = mContentResolver?.update(newICalObject!!, updatedContentValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "icalObject_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        //find
        val cursor2: Cursor? = mContentResolver?.query(newICalObject!!, arrayOf(JtxContract.JtxICalObject.ID, JtxContract.JtxICalObject.SUMMARY, JtxContract.JtxICalObject.DESCRIPTION), "${JtxContract.JtxICalObject.SUMMARY} = ?", arrayOf("note2delete"), null)
        assertEquals(cursor2?.count,1)             // inserted object was found
        cursor2?.close()

        //delete
        val countDel: Int? = mContentResolver?.delete(newICalObject!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        val cursor3: Cursor? = mContentResolver?.query(newICalObject!!, arrayOf(JtxContract.JtxICalObject.ID, JtxContract.JtxICalObject.SUMMARY, JtxContract.JtxICalObject.DESCRIPTION), "${JtxContract.JtxICalObject.SUMMARY} = ?", arrayOf("note2delete"), null)
        assertEquals(cursor3?.count,0)             // inserted object was found
        cursor3?.close()

        //cleanup
        mContentResolver?.delete(newCollection, null, null)

    }



    @Test
    fun attendee_insert_find_update_delete()  {

        //prepare
        val account = Account("journal4attendee", "journal4attendee")
        val newCollection = insertCollection(account, null, null)
        val newCollectionId = newCollection?.lastPathSegment?.toLongOrNull()
        val newICalObject = insertIcalObject(account, "journal4attendee", newCollectionId!!)
        val newICalObjectId = newICalObject?.lastPathSegment?.toLongOrNull()


        // INSERT a new Attendee
        val attendeeValues = ContentValues()
        attendeeValues.put(JtxContract.JtxAttendee.ICALOBJECT_ID, newICalObjectId)
        attendeeValues.put(JtxContract.JtxAttendee.CALADDRESS, "mailto:test@test.com")
        val uriAttendees = JtxContract.JtxAttendee.CONTENT_URI.asSyncAdapter(account)
        val newAttendee = mContentResolver?.insert(uriAttendees, attendeeValues)
        assertNotNull(newAttendee)

        //QUERY the Attendee
        val cursorIcalobject: Cursor? = mContentResolver?.query(newAttendee!!, arrayOf(JtxContract.JtxAttendee.ID), null, null, null)
        assertEquals(cursorIcalobject?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedAttendeeValues = ContentValues()
        updatedAttendeeValues.put(JtxContract.JtxAttendee.CALADDRESS, "mailto:test@test.net")
        val countUpdated = mContentResolver?.update(newAttendee!!, updatedAttendeeValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorAttendee: Cursor? = mContentResolver?.query(newAttendee!!, arrayOf(JtxContract.JtxAttendee.ID, JtxContract.JtxAttendee.CALADDRESS), "${JtxContract.JtxAttendee.CALADDRESS} = ?", arrayOf("mailto:test@test.net"), null)
        assertEquals(cursorAttendee?.count,1)             // inserted object was found
        cursorAttendee?.close()

        //delete
        val countDel: Int? = mContentResolver?.delete(newAttendee!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        val cursor3: Cursor? = mContentResolver?.query(newAttendee!!, arrayOf(JtxContract.JtxAttendee.ID, JtxContract.JtxAttendee.CALADDRESS), "${JtxContract.JtxAttendee.CALADDRESS} = ?", arrayOf("mailto:test@test.net"), null)
        assertEquals(0, cursor3?.count)             // inserted object was found
        cursor3?.close()

        // Cleanup
        val countDeleted = mContentResolver?.delete(newCollection, null, null)
        assertEquals(countDeleted, 1)

    }



    @Test
    fun category_insert_find_update_delete()  {

        //prepare
        val account = Account("journal4category", "journal4category")
        val newCollection = insertCollection(account, null, null)
        val newCollectionId = newCollection?.lastPathSegment?.toLongOrNull()
        val newICalObject = insertIcalObject(account, "journal4category", newCollectionId!!)
        val newICalObjectId = newICalObject?.lastPathSegment?.toLongOrNull()


        // INSERT
        val categoryValues = ContentValues()
        categoryValues.put(JtxContract.JtxCategory.ICALOBJECT_ID, newICalObjectId)
        categoryValues.put(JtxContract.JtxCategory.TEXT, "inserted category")
        val uriCategories = JtxContract.JtxCategory.CONTENT_URI.asSyncAdapter(account)
        val newCategory = mContentResolver?.insert(uriCategories, categoryValues)
        assertNotNull(newCategory)

        //QUERY
        val cursor1: Cursor? = mContentResolver?.query(newCategory!!, arrayOf(JtxContract.JtxCategory.ID), null, null, null)
        assertEquals(cursor1?.count, 1)             // inserted object was found

        // UPDATE
        val updatedCategoryValues = ContentValues()
        updatedCategoryValues.put(JtxContract.JtxCategory.TEXT, "updated category")
        val countUpdated = mContentResolver?.update(newCategory!!, updatedCategoryValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursor2: Cursor? = mContentResolver?.query(newCategory!!, arrayOf(JtxContract.JtxCategory.ID, JtxContract.JtxCategory.TEXT), "${JtxContract.JtxCategory.TEXT} = ?", arrayOf("updated category"), null)
        assertEquals(cursor2?.count,1)             // inserted object was found
        cursor2?.close()

        //delete
        val countDel: Int? = mContentResolver?.delete(newCategory!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        val cursor3: Cursor? = mContentResolver?.query(newCategory!!, arrayOf(JtxContract.JtxCategory.ID, JtxContract.JtxCategory.TEXT), "${JtxContract.JtxCategory.TEXT} = ?", arrayOf("updated category"), null)
        assertEquals(0, cursor3?.count)             // inserted object was found
        cursor3?.close()

        // Cleanup
        val countDeleted = mContentResolver?.delete(newCollection, null, null)
        assertEquals(countDeleted, 1)

    }




    @Test
    fun comment_insert_find_update_delete()  {

        //prepare
        val account = Account("journal4comment", "journal4comment")
        val newCollection = insertCollection(account, null, null)
        val newCollectionId = newCollection?.lastPathSegment?.toLongOrNull()
        val newICalObject = insertIcalObject(account, "journal4comment", newCollectionId!!)
        val newICalObjectId = newICalObject?.lastPathSegment?.toLongOrNull()


        // INSERT
        val commentValues = ContentValues()
        commentValues.put(JtxContract.JtxComment.ICALOBJECT_ID, newICalObjectId)
        commentValues.put(JtxContract.JtxComment.TEXT, "inserted comment")
        val uriComments = JtxContract.JtxComment.CONTENT_URI.asSyncAdapter(account)
        val newComment = mContentResolver?.insert(uriComments, commentValues)
        assertNotNull(newComment)

        //QUERY
        val cursor1: Cursor? = mContentResolver?.query(newComment!!, arrayOf(JtxContract.JtxComment.ID), null, null, null)
        assertEquals(cursor1?.count, 1)             // inserted object was found

        // UPDATE
        val updatedCommentValues = ContentValues()
        updatedCommentValues.put(JtxContract.JtxComment.TEXT, "updated comment")
        val countUpdated = mContentResolver?.update(newComment!!, updatedCommentValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursor2: Cursor? = mContentResolver?.query(newComment!!, arrayOf(JtxContract.JtxComment.ID, JtxContract.JtxComment.TEXT), "${JtxContract.JtxComment.TEXT} = ?", arrayOf("updated comment"), null)
        assertEquals(cursor2?.count,1)             // inserted object was found
        cursor2?.close()

        //delete
        val countDel: Int? = mContentResolver?.delete(newComment!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        val cursor3: Cursor? = mContentResolver?.query(newComment!!, arrayOf(JtxContract.JtxComment.ID, JtxContract.JtxComment.TEXT), "${JtxContract.JtxComment.TEXT} = ?", arrayOf("updated comment"), null)
        assertEquals(0, cursor3?.count)             // inserted object was found
        cursor3?.close()

        // Cleanup
        val countDeleted = mContentResolver?.delete(newCollection, null, null)
        assertEquals(countDeleted, 1)

    }




    @Test
    fun contact_insert_find_update_delete()  {


        //prepare
        val account = Account("journal4contact", "journal4contact")
        val newCollection = insertCollection(account, null, null)
        val newCollectionId = newCollection?.lastPathSegment?.toLongOrNull()
        val newICalObject = insertIcalObject(account, "journal4contact", newCollectionId!!)
        val newICalObjectId = newICalObject?.lastPathSegment?.toLongOrNull()


        // INSERT
        val contactValues = ContentValues()
        contactValues.put(JtxContract.JtxContact.ICALOBJECT_ID, newICalObjectId)
        contactValues.put(JtxContract.JtxContact.TEXT, "inserted contact")
        val uriContacts = JtxContract.JtxContact.CONTENT_URI.asSyncAdapter(account)
        val newContact = mContentResolver?.insert(uriContacts, contactValues)
        assertNotNull(newContact)

        //QUERY
        val cursor1: Cursor? = mContentResolver?.query(newContact!!, arrayOf(JtxContract.JtxContact.ID), null, null, null)
        assertEquals(cursor1?.count, 1)             // inserted object was found

        // UPDATE
        val updatedContactValues = ContentValues()
        updatedContactValues.put(JtxContract.JtxContact.TEXT, "updated contact")
        val countUpdated = mContentResolver?.update(newContact!!, updatedContactValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursor2: Cursor? = mContentResolver?.query(newContact!!, arrayOf(JtxContract.JtxContact.ID, JtxContract.JtxContact.TEXT), "${JtxContract.JtxContact.TEXT} = ?", arrayOf("updated contact"), null)
        assertEquals(cursor2?.count,1)             // inserted object was found
        cursor2?.close()

        //delete
        val countDel: Int? = mContentResolver?.delete(newContact!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        val cursor3: Cursor? = mContentResolver?.query(newContact!!, arrayOf(JtxContract.JtxContact.ID, JtxContract.JtxContact.TEXT), "${JtxContract.JtxContact.TEXT} = ?", arrayOf("updated contact"), null)
        assertEquals(0, cursor3?.count)             // inserted object was found
        cursor3?.close()

        // Cleanup
        val countDeleted = mContentResolver?.delete(newCollection, null, null)
        assertEquals(countDeleted, 1)

    }


    @Test
    fun organizer_insert_find_update_delete()  {

        //prepare
        val account = Account("journal4organizer", "journal4organizer")
        val newCollection = insertCollection(account, null, null)
        val newCollectionId = newCollection?.lastPathSegment?.toLongOrNull()
        val newICalObject = insertIcalObject(account, "journal4organizer", newCollectionId!!)
        val newICalObjectId = newICalObject?.lastPathSegment?.toLongOrNull()


        // INSERT
        val contactValues = ContentValues()
        contactValues.put(JtxContract.JtxOrganizer.ICALOBJECT_ID, newICalObjectId)
        contactValues.put(JtxContract.JtxOrganizer.CALADDRESS, "mailto:a@b.com")
        val uriOrganizer = JtxContract.JtxOrganizer.CONTENT_URI.asSyncAdapter(account)
        val newOrganizer = mContentResolver?.insert(uriOrganizer, contactValues)
        assertNotNull(newOrganizer)

        //QUERY
        val cursor1: Cursor? = mContentResolver?.query(newOrganizer!!, arrayOf(JtxContract.JtxOrganizer.ID), null, null, null)
        assertEquals(cursor1?.count, 1)             // inserted object was found

        // UPDATE
        val updatedOrganizerValues = ContentValues()
        updatedOrganizerValues.put(JtxContract.JtxOrganizer.CALADDRESS, "mailto:c@d.com")
        val countUpdated = mContentResolver?.update(newOrganizer!!, updatedOrganizerValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursor2: Cursor? = mContentResolver?.query(newOrganizer!!, arrayOf(JtxContract.JtxOrganizer.ID, JtxContract.JtxOrganizer.CALADDRESS), "${JtxContract.JtxOrganizer.CALADDRESS} = ?", arrayOf("mailto:c@d.com"), null)
        assertEquals(cursor2?.count,1)             // inserted object was found
        cursor2?.close()

        //delete
        val countDel: Int? = mContentResolver?.delete(newOrganizer!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        val cursor3: Cursor? = mContentResolver?.query(newOrganizer!!, arrayOf(JtxContract.JtxOrganizer.ID, JtxContract.JtxOrganizer.CALADDRESS), "${JtxContract.JtxOrganizer.CALADDRESS} = ?", arrayOf("mailto:c@d.com"), null)
        assertEquals(0, cursor3?.count)             // inserted object was found
        cursor3?.close()

        // Cleanup
        val countDeleted = mContentResolver?.delete(newCollection, null, null)
        assertEquals(countDeleted, 1)


    }



    @Test
    fun relatedto_insert_find_update_delete()  {

        //prepare
        val account = Account("journal4relatedto", "journal4relatedto")
        val newCollection = insertCollection(account, null, null)
        val newCollectionId = newCollection?.lastPathSegment?.toLongOrNull()
        val newICalObject = insertIcalObject(account, "journal4relatedto", newCollectionId!!)
        val newICalObjectId = newICalObject?.lastPathSegment?.toLongOrNull()


        // INSERT
        val relatedtoValues = ContentValues()
        relatedtoValues.put(JtxContract.JtxRelatedto.ICALOBJECT_ID, newICalObjectId)
        relatedtoValues.put(JtxContract.JtxRelatedto.LINKEDICALOBJECT_ID, newICalObjectId)
        relatedtoValues.put(JtxContract.JtxRelatedto.RELTYPE, "Child")
        val uriRelatedto = JtxContract.JtxRelatedto.CONTENT_URI.asSyncAdapter(account)
        val newRelatedto = mContentResolver?.insert(uriRelatedto, relatedtoValues)
        assertNotNull(newRelatedto)

        //QUERY
        val cursor1: Cursor? = mContentResolver?.query(newRelatedto!!, arrayOf(JtxContract.JtxRelatedto.ID), null, null, null)
        assertEquals(cursor1?.count, 1)             // inserted object was found

        // UPDATE
        val updatedRelatedtoValues = ContentValues()
        updatedRelatedtoValues.put(JtxContract.JtxRelatedto.RELTYPE, "Parent")
        val countUpdated = mContentResolver?.update(newRelatedto!!, updatedRelatedtoValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursor2: Cursor? = mContentResolver?.query(newRelatedto!!, arrayOf(JtxContract.JtxRelatedto.ID, JtxContract.JtxRelatedto.ICALOBJECT_ID), "${JtxContract.JtxRelatedto.ICALOBJECT_ID} = ?", arrayOf(newICalObjectId.toString()), null)
        assertEquals(cursor2?.count,1)             // inserted object was found
        cursor2?.close()

        //delete
        val countDel: Int? = mContentResolver?.delete(newRelatedto!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        val cursor3: Cursor? = mContentResolver?.query(newRelatedto!!, arrayOf(JtxContract.JtxRelatedto.ID, JtxContract.JtxRelatedto.ICALOBJECT_ID), "${JtxContract.JtxRelatedto.ICALOBJECT_ID} = ?", arrayOf(newICalObjectId.toString()), null)
        assertEquals(0, cursor3?.count)             // inserted object was found
        cursor3?.close()

        // Cleanup
        val countDeleted = mContentResolver?.delete(newCollection, null, null)
        assertEquals(countDeleted, 1)

   }



    @Test
    fun resource_insert_find_update_delete()  {
        //prepare
        val account = Account("journal4resource", "journal4resource")
        val newCollection = insertCollection(account, null, null)
        val newCollectionId = newCollection?.lastPathSegment?.toLongOrNull()
        val newICalObject = insertIcalObject(account, "journal4resource", newCollectionId!!)
        val newICalObjectId = newICalObject?.lastPathSegment?.toLongOrNull()


        // INSERT
        val commentValues = ContentValues()
        commentValues.put(JtxContract.JtxResource.ICALOBJECT_ID, newICalObjectId)
        commentValues.put(JtxContract.JtxResource.TEXT, "inserted resource")
        val uriResource = JtxContract.JtxResource.CONTENT_URI.asSyncAdapter(account)
        val newResource = mContentResolver?.insert(uriResource, commentValues)
        assertNotNull(newResource)

        //QUERY
        val cursor1: Cursor? = mContentResolver?.query(newResource!!, arrayOf(JtxContract.JtxResource.ID), null, null, null)
        assertEquals(cursor1?.count, 1)             // inserted object was found

        // UPDATE
        val updatedResourceValues = ContentValues()
        updatedResourceValues.put(JtxContract.JtxResource.TEXT, "updated resource")
        val countUpdated = mContentResolver?.update(newResource!!, updatedResourceValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursor2: Cursor? = mContentResolver?.query(newResource!!, arrayOf(JtxContract.JtxResource.ID, JtxContract.JtxResource.TEXT), "${JtxContract.JtxResource.TEXT} = ?", arrayOf("updated resource"), null)
        assertEquals(cursor2?.count,1)             // inserted object was found
        cursor2?.close()

        //delete
        val countDel: Int? = mContentResolver?.delete(newResource!!, null, null)
        assertNotNull(countDel)
        assertEquals(1,countDel)
        //Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        //find
        val cursor3: Cursor? = mContentResolver?.query(newResource!!, arrayOf(JtxContract.JtxResource.ID, JtxContract.JtxResource.TEXT), "${JtxContract.JtxResource.TEXT} = ?", arrayOf("updated resource"), null)
        assertEquals(0, cursor3?.count)             // inserted object was found
        cursor3?.close()

        // Cleanup
        val countDeleted = mContentResolver?.delete(newCollection, null, null)
        assertEquals(countDeleted, 1)
    }



    @Test
    fun collection_insert_find_update_delete()  {

        // INSERT a new Collection
        val newCollection = insertCollection(Account("testcollection", "testcollection"), "testcollection", "testcollection")
        val newCollectionId = newCollection?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newCollectionUriId", newCollection.toString())
        assertNotNull(newCollectionId)

        //QUERY the Collection
        val cursorNewCollection: Cursor? = mContentResolver?.query(newCollection!!, arrayOf(JtxContract.JtxCollection.ID), null, null, null)
        assertEquals(cursorNewCollection?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedCollectionValues = ContentValues()
        updatedCollectionValues.put(JtxContract.JtxCollection.DISPLAYNAME, "testcollection updated")
        val countUpdated = mContentResolver?.update(newCollection!!, updatedCollectionValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorUpdatedCollection: Cursor? = mContentResolver?.query(newCollection!!, arrayOf(JtxContract.JtxCollection.ID, JtxContract.JtxCollection.DISPLAYNAME), "${JtxContract.JtxCollection.DISPLAYNAME} = ?", arrayOf("testcollection updated"), null)
        assertEquals(cursorUpdatedCollection?.count,1)             // inserted object was found
        cursorUpdatedCollection?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newCollection!!, null, null)
        assertEquals(1, countDeleted)

    }

    @Test
    fun collection_query_all() {
        //prepare
        val account = Account("collection1", "collection1")
        val col1 = insertCollection(account, null, "Collection1")
        val col2 = insertCollection(account, null, "Collection2")

        val allCollections: Cursor? = mContentResolver?.query(JtxContract.JtxCollection.CONTENT_URI.asSyncAdapter(account), arrayOf(JtxContract.JtxCollection.ID), null, null, null)
        assertEquals(2, allCollections?.count)

        mContentResolver?.delete(col1!!, null, null)
        mContentResolver?.delete(col2!!, null, null)


    }




    @Test(expected = IllegalArgumentException::class)                    // needed to assert exceptions, see e.g. https://www.baeldung.com/junit-assert-exception
    fun query_invalid_url()  {
        val uriInvalid = Uri.parse("content://${JtxContract.AUTHORITY}/invalid")
        mContentResolver?.query(uriInvalid, arrayOf(JtxContract.JtxICalObject.ID), null, null, null)
    }

    @Test(expected = IllegalArgumentException::class)                    // needed to assert exceptions, see e.g. https://www.baeldung.com/junit-assert-exception
    fun query_valid_url_with_wrong_parameter()  {
        val uriWrong = Uri.parse("content://${JtxContract.AUTHORITY}/$TABLE_NAME_ICALOBJECT/asdf")
        mContentResolver?.query(uriWrong, arrayOf(JtxContract.JtxICalObject.ID), null, null, null)
    }


    @Test
    fun check_for_SQL_injection_through_contentValues()  {

        //prepare
        val account = Account("journal4injectionContentValues", "journal4injectionContentValues")
        val newCollection = insertCollection(account, null, null)
        val newCollectionId = newCollection?.lastPathSegment?.toLongOrNull()


        val contentValuesCurrupted = ContentValues()
        contentValuesCurrupted.put(JtxContract.JtxICalObject.SUMMARY, "note2corrupted\"; delete * from $TABLE_NAME_ICALOBJECT")
        contentValuesCurrupted.put(JtxContract.JtxICalObject.ICALOBJECT_COLLECTIONID, newCollectionId)
        val newUri2 = mContentResolver?.insert(JtxContract.JtxICalObject.CONTENT_URI.asSyncAdapter(account), contentValuesCurrupted)


        val cursor: Cursor? = mContentResolver?.query(newUri2!!, arrayOf(JtxContract.JtxICalObject.ID, JtxContract.JtxICalObject.SUMMARY), null, null, null)
        assertEquals(cursor?.count, 1)
//        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, the new id is ${cursor?.getString(0)}")

        cursor?.close()

        // Cleanup
        val countDeleted = mContentResolver?.delete(newCollection!!, null, null)
        assertEquals(countDeleted, 1)
    }


    @Test
    fun check_for_SQL_injection_through_query()  {

        // INSERT a new value, this one must remain
        //prepare
        val account = Account("journal4injectionQuery", "journal4injectionQuery")
        val newCollection = insertCollection(account, null, null)
        val newCollectionId = newCollection?.lastPathSegment?.toLongOrNull()
        val newICalObject = insertIcalObject(account, "journal4injectionQuery", newCollectionId!!)
        //val newICalObjectId = newICalObject?.lastPathSegment?.toLongOrNull()


        val cursor: Cursor? = mContentResolver?.query(newICalObject!!, arrayOf(JtxContract.JtxICalObject.ID, JtxContract.JtxICalObject.SUMMARY), "${JtxContract.JtxICalObject.SUMMARY} = ?); DELETE * FROM $TABLE_NAME_ICALOBJECT", arrayOf("note2check"), null)
        assertEquals(0, cursor?.count)
//        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, the new id is ${cursor?.getString(0)}")
        cursor?.close()

        val cursor2: Cursor? = mContentResolver?.query(JtxContract.JtxICalObject.CONTENT_URI.asSyncAdapter(account), arrayOf(JtxContract.JtxICalObject.ID), null, null, null)
        assertTrue(cursor2?.count!! > 0)     // there must be entries! Delete must not be executed!
        Log.println(Log.INFO, "icalObject_initiallyEmpty", "Assert successful, DB is empty (Cursor count: ${cursor?.count})")
        cursor?.close()

        // Cleanup
        val countDeleted = mContentResolver?.delete(newCollection, null, null)
        assertEquals(countDeleted, 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun check_for_invalid_URI_without_IS_SYNC_ADAPTER()  {

        val queryParamsInavalid = "$ACCOUNT_NAME=test&$ACCOUNT_TYPE=test"
        val uri = Uri.parse("content://${JtxContract.AUTHORITY}/$TABLE_NAME_ICALOBJECT?$queryParamsInavalid")

        val contentValues = ContentValues()
        contentValues.put(JtxContract.JtxICalObject.SUMMARY, "note2check")
        mContentResolver?.insert(uri, contentValues)
    }

    @Test(expected = IllegalArgumentException::class)
    fun check_for_invalid_URI_without_ACCOUNT_NAME()  {

        val queryParamsInavalid = "$CALLER_IS_SYNCADAPTER=false&$ACCOUNT_TYPE=test"
        val uri = Uri.parse("content://${JtxContract.AUTHORITY}/$TABLE_NAME_ICALOBJECT?$queryParamsInavalid")

        val contentValues = ContentValues()
        contentValues.put(JtxContract.JtxICalObject.SUMMARY, "note2check")
        mContentResolver?.insert(uri, contentValues)
    }


    @Test(expected = IllegalArgumentException::class)
    fun check_for_invalid_URI_without_ACCOUNT_TYPE()  {

        val queryParamsInavalid = "$CALLER_IS_SYNCADAPTER=false&$ACCOUNT_NAME=test&"
        val uri = Uri.parse("content://${JtxContract.AUTHORITY}/$TABLE_NAME_ICALOBJECT?$queryParamsInavalid")

        val contentValues = ContentValues()
        contentValues.put(JtxContract.JtxICalObject.SUMMARY, "note2check")
        mContentResolver?.insert(uri, contentValues)
    }





    private fun insertCollection(account: Account, url: String?, displayname: String?): Uri? {

        // INSERT a new Collection
        val collectionValues = ContentValues()
        collectionValues.put(JtxContract.JtxCollection.DISPLAYNAME, displayname)
        collectionValues.put(JtxContract.JtxCollection.URL, url)
        collectionValues.put(JtxContract.JtxCollection.ACCOUNT_NAME, account.name)
        collectionValues.put(JtxContract.JtxCollection.ACCOUNT_TYPE, account.type)

        val uriCollection = JtxContract.JtxCollection.CONTENT_URI.asSyncAdapter(account)

        return mContentResolver?.insert(uriCollection, collectionValues)
    }


    private fun insertIcalObject(account: Account, summary: String, collectionId: Long): Uri? {
        val contentValues = ContentValues()
        contentValues.put(JtxContract.JtxICalObject.SUMMARY, summary)
        contentValues.put(JtxContract.JtxICalObject.ICALOBJECT_COLLECTIONID, collectionId)
        val uriIcalobject = JtxContract.JtxICalObject.CONTENT_URI.asSyncAdapter(account)

        return mContentResolver?.insert(uriIcalobject, contentValues)

    }




    /*
    @Test
    fun delete() {
    }

    @Test
    fun getType() {
    }

    @Test
    fun insert() {
    }

    @Test
    fun onCreate() {
    }

    @Test
    fun query() {

    }

    @Test
    fun update() {
    }

     */
}