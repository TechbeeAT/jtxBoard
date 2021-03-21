package at.bitfire.notesx5

import android.accounts.Account
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import at.bitfire.notesx5.NotesX5Contract.asSyncAdapter
import at.bitfire.notesx5.database.ICalDatabase
import at.bitfire.notesx5.database.TABLE_NAME_ICALOBJECT
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@SmallTest
class SyncContentProviderTest {

    private var mContentResolver: ContentResolver? = null

    var testAccount: Account = Account("test", "test")


    //private val queryParams = "$CALLER_IS_SYNCADAPTER=true&$ACCOUNT_NAME=test&$ACCOUNT_TYPE=test"
    //private val URI_COLLECTION = Uri.parse("content://${NotesX5Contract.AUTHORITY}/$TABLE_NAME_COLLECTION?$queryParams")


    private val URI_ICALOBJECT = NotesX5Contract.X5ICalObject.CONTENT_URI.asSyncAdapter(testAccount)
    private val URI_ATTENDEES = NotesX5Contract.X5Attendee.CONTENT_URI.asSyncAdapter(testAccount)
    private val URI_CATEGORIES = NotesX5Contract.X5Category.CONTENT_URI.asSyncAdapter(testAccount)
    private val URI_COMMENTS = NotesX5Contract.X5Comment.CONTENT_URI.asSyncAdapter(testAccount)
    private val URI_CONTACTS = NotesX5Contract.X5Contact.CONTENT_URI.asSyncAdapter(testAccount)
    private val URI_ORGANIZER = NotesX5Contract.X5Organizer.CONTENT_URI.asSyncAdapter(testAccount)
    private val URI_RELATEDTO = NotesX5Contract.X5Relatedto.CONTENT_URI.asSyncAdapter(testAccount)
    private val URI_RESOURCE = NotesX5Contract.X5Resource.CONTENT_URI.asSyncAdapter(testAccount)
    private val URI_COLLECTION = NotesX5Contract.X5Collection.CONTENT_URI.asSyncAdapter(testAccount)




    @Before
    fun setUp() {
        //val context: Context = ApplicationProvider.getApplicationContext()
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        ICalDatabase.switchToInMemory(context)
        mContentResolver = context.contentResolver
    }

    /*
    @Test
    fun icalObject_initiallyEmpty() {
        val cursor: Cursor? = mContentResolver?.query(URI_ICALOBJECT, arrayOf<String>(COLUMN_ID), null, null, null)
        assertThat(cursor, notNullValue())
        assertThat(cursor?.count, `is`(0))
        Log.println(Log.INFO, "icalObject_initiallyEmpty", "Assert successful, DB is empty (Cursor count: ${cursor?.count})")
        cursor?.close()
    }

     */

    // TODO COLUMN_* ersetzen durch Contract!!!!

    @Test
    fun icalObject_insert_find_delete()  {

        val contentValues = ContentValues()
        contentValues.put(NotesX5Contract.X5ICalObject.SUMMARY, "note2delete")
        val newUri = mContentResolver?.insert(URI_ICALOBJECT, contentValues)


        val cursor: Cursor? = mContentResolver?.query(newUri!!, arrayOf(NotesX5Contract.X5ICalObject.ID, NotesX5Contract.X5ICalObject.SUMMARY), null, null, null)
        assertNotNull(cursor)
        assertEquals(cursor?.count, 1)
//        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, the new id is ${cursor?.getString(0)}")

        val countDel: Int? = mContentResolver?.delete(newUri!!, null, null)
        assertNotNull(countDel)
        assertEquals(countDel,1)
        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        cursor?.close()
    }

    @Test
    fun icalObject_insert_find_update()  {

        // INSERT a new value
        val contentValues = ContentValues()
        contentValues.put(NotesX5Contract.X5ICalObject.SUMMARY, "note2update")
        val newUri = mContentResolver?.insert(URI_ICALOBJECT, contentValues)

        // QUERY the new value
        val cursor: Cursor? = mContentResolver?.query(newUri!!, arrayOf(NotesX5Contract.X5ICalObject.ID, NotesX5Contract.X5ICalObject.SUMMARY), null, null, null)
        assertNotNull(cursor)
        assertEquals(cursor?.count, 1)             // inserted object was found


//        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, the new id is ${cursor?.getString(0)}")

        // UPDATE the new value
        val updatedContentValues = ContentValues()
        updatedContentValues.put(NotesX5Contract.X5ICalObject.DESCRIPTION, "description was updated")
        val countUpdated = mContentResolver?.update(newUri!!, updatedContentValues, null, null)
        assertEquals(countUpdated, 1)
        Log.println(Log.INFO, "icalObject_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursor2: Cursor? = mContentResolver?.query(newUri!!, arrayOf(NotesX5Contract.X5ICalObject.ID, NotesX5Contract.X5ICalObject.SUMMARY, NotesX5Contract.X5ICalObject.DESCRIPTION), "${NotesX5Contract.X5ICalObject.SUMMARY} = ?", arrayOf("note2update"), null)
        assertEquals(cursor2?.count,1)             // inserted object was found
        cursor2?.close()

        // DELETE the updated value
        val countDeleted = mContentResolver?.delete(newUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        val cursor3: Cursor? = mContentResolver?.query(newUri!!, arrayOf(NotesX5Contract.X5ICalObject.ID, NotesX5Contract.X5ICalObject.SUMMARY, NotesX5Contract.X5ICalObject.DESCRIPTION), "${NotesX5Contract.X5ICalObject.SUMMARY} = ?", arrayOf("note2update"), null)
        assertEquals(cursor3?.count,0)             // inserted object was found
        cursor3?.close()

    }



    @Test
    fun attendee_insert_find_update_delete()  {

        // INSERT a new ICalObject
        val contentValues = ContentValues()
        contentValues.put(NotesX5Contract.X5ICalObject.SUMMARY, "journal4attendee")
        contentValues.put(NotesX5Contract.X5ICalObject.DTSTART, System.currentTimeMillis())
        contentValues.put(NotesX5Contract.X5ICalObject.COMPONENT, "JOURNAL")
        val newUri = mContentResolver?.insert(URI_ICALOBJECT, contentValues)
        val newICalObjectId = newUri?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newICalObjectId", newICalObjectId.toString())
        assertNotNull(newICalObjectId)

        // QUERY the new value is skipped, instead we insert a new attendee
        // INSERT a new Attendee
        val attendeeValues = ContentValues()
        attendeeValues.put(NotesX5Contract.X5Attendee.ICALOBJECT_ID, newICalObjectId)
        attendeeValues.put(NotesX5Contract.X5Attendee.CALADDRESS, "mailto:test@test.com")
        val newAttendeeUri = mContentResolver?.insert(URI_ATTENDEES, attendeeValues)
        assertNotNull(newAttendeeUri)

        //QUERY the Attendee
        val cursorIcalobject: Cursor? = mContentResolver?.query(newAttendeeUri!!, arrayOf(NotesX5Contract.X5Attendee.ID), null, null, null)
        assertEquals(cursorIcalobject?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedAttendeeValues = ContentValues()
        updatedAttendeeValues.put(NotesX5Contract.X5Attendee.CALADDRESS, "mailto:test@test.net")
        val countUpdated = mContentResolver?.update(newAttendeeUri!!, updatedAttendeeValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorAttendee: Cursor? = mContentResolver?.query(newAttendeeUri!!, arrayOf(NotesX5Contract.X5Attendee.ID, NotesX5Contract.X5Attendee.CALADDRESS), "${NotesX5Contract.X5Attendee.CALADDRESS} = ?", arrayOf("mailto:test@test.net"), null)
        assertEquals(cursorAttendee?.count,1)             // inserted object was found
        cursorAttendee?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        val cursorDeletedIcalobject: Cursor? = mContentResolver?.query(newUri!!, arrayOf(NotesX5Contract.X5ICalObject.ID, NotesX5Contract.X5ICalObject.SUMMARY, NotesX5Contract.X5ICalObject.DESCRIPTION), "${NotesX5Contract.X5ICalObject.SUMMARY} = ?", arrayOf("note2update"), null)
        assertEquals(cursorDeletedIcalobject?.count,0)             // inserted object was found
        cursorDeletedIcalobject?.close()
        val cursorDeletedAttendee: Cursor? = mContentResolver?.query(newAttendeeUri!!, arrayOf(NotesX5Contract.X5Attendee.ID, NotesX5Contract.X5Attendee.CALADDRESS), "${NotesX5Contract.X5Attendee.CALADDRESS} = ?", arrayOf("mailto:test@test.net"), null)
        assertEquals(cursorDeletedAttendee?.count,0)             // inserted object was found
        cursorAttendee?.close()
    }



    @Test
    fun category_insert_find_update_delete()  {

        // INSERT a new ICalObject
        val icalobjectValues = ContentValues()
        icalobjectValues.put(NotesX5Contract.X5ICalObject.SUMMARY, "journal4category")
        icalobjectValues.put(NotesX5Contract.X5ICalObject.DTSTART, System.currentTimeMillis())
        icalobjectValues.put(NotesX5Contract.X5ICalObject.COMPONENT, "JOURNAL")
        val newIcalUri = mContentResolver?.insert(URI_ICALOBJECT, icalobjectValues)
        val newICalObjectId = newIcalUri?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newICalObjectId", newICalObjectId.toString())
        assertNotNull(newICalObjectId)

        // QUERY the new value is skipped, instead we insert a new category
        // INSERT a new Category
        val categoryValues = ContentValues()
        categoryValues.put(NotesX5Contract.X5Category.ICALOBJECT_ID, newICalObjectId)
        categoryValues.put(NotesX5Contract.X5Category.TEXT, "category1")
        val newCategoryUri = mContentResolver?.insert(URI_CATEGORIES, categoryValues)
        assertNotNull(newCategoryUri)

        //QUERY the Category
        val cursorNewCategory: Cursor? = mContentResolver?.query(newCategoryUri!!, arrayOf(NotesX5Contract.X5Category.ID), null, null, null)
        assertEquals(cursorNewCategory?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedCategoryValues = ContentValues()
        updatedCategoryValues.put(NotesX5Contract.X5Category.TEXT, "category2")
        val countUpdated = mContentResolver?.update(newCategoryUri!!, updatedCategoryValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorUpdatedCategory: Cursor? = mContentResolver?.query(newCategoryUri!!, arrayOf(NotesX5Contract.X5Category.ID, NotesX5Contract.X5Category.TEXT), "${NotesX5Contract.X5Category.TEXT} = ?", arrayOf("category2"), null)
        assertEquals(cursorUpdatedCategory?.count,1)             // inserted object was found
        cursorUpdatedCategory?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newIcalUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        val cursorDeletedIcalobject: Cursor? = mContentResolver?.query(newIcalUri!!, arrayOf(NotesX5Contract.X5ICalObject.ID, NotesX5Contract.X5ICalObject.SUMMARY, NotesX5Contract.X5ICalObject.DESCRIPTION), "${NotesX5Contract.X5ICalObject.SUMMARY} = ?", arrayOf("journal4category"), null)
        assertEquals(cursorDeletedIcalobject?.count,0)             // inserted object was found
        cursorDeletedIcalobject?.close()
        val cursorDeletedCategory: Cursor? = mContentResolver?.query(newCategoryUri!!, arrayOf(NotesX5Contract.X5Category.ID, NotesX5Contract.X5Category.TEXT), "${NotesX5Contract.X5Category.TEXT} = ?", arrayOf("category2"), null)
        assertEquals(cursorDeletedCategory?.count,0)             // inserted object was found
        cursorUpdatedCategory?.close()
    }




    @Test
    fun comment_insert_find_update_delete()  {

        // INSERT a new ICalObject
        val icalobjectValues = ContentValues()
        icalobjectValues.put(NotesX5Contract.X5ICalObject.SUMMARY, "journal4comment")
        icalobjectValues.put(NotesX5Contract.X5ICalObject.DTSTART, System.currentTimeMillis())
        icalobjectValues.put(NotesX5Contract.X5ICalObject.COMPONENT, "JOURNAL")
        val newIcalUri = mContentResolver?.insert(URI_ICALOBJECT, icalobjectValues)
        val newICalObjectId = newIcalUri?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newICalObjectId", newICalObjectId.toString())
        assertNotNull(newICalObjectId)

        // QUERY the new value is skipped, instead we insert a new category
        // INSERT a new Category
        val commentValues = ContentValues()
        commentValues.put(NotesX5Contract.X5Comment.ICALOBJECT_ID, newICalObjectId)
        commentValues.put(NotesX5Contract.X5Comment.TEXT, "comment1")
        val newCommentUri = mContentResolver?.insert(URI_COMMENTS, commentValues)
        assertNotNull(newCommentUri)

        //QUERY the Category
        val cursorNewComment: Cursor? = mContentResolver?.query(newCommentUri!!, arrayOf(NotesX5Contract.X5Comment.ID), null, null, null)
        assertEquals(cursorNewComment?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedCommentValues = ContentValues()
        updatedCommentValues.put(NotesX5Contract.X5Comment.TEXT, "comment2")
        val countUpdated = mContentResolver?.update(newCommentUri!!, updatedCommentValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorUpdatedComment: Cursor? = mContentResolver?.query(newCommentUri!!, arrayOf(NotesX5Contract.X5Comment.ID, NotesX5Contract.X5Comment.TEXT), "${NotesX5Contract.X5Comment.TEXT} = ?", arrayOf("comment2"), null)
        assertEquals(cursorUpdatedComment?.count,1)             // inserted object was found
        cursorUpdatedComment?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newIcalUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        val cursorDeletedIcalobject: Cursor? = mContentResolver?.query(newIcalUri!!, arrayOf(NotesX5Contract.X5ICalObject.ID, NotesX5Contract.X5ICalObject.SUMMARY, NotesX5Contract.X5ICalObject.DESCRIPTION), "${NotesX5Contract.X5ICalObject.SUMMARY} = ?", arrayOf("journal4comment"), null)
        assertEquals(cursorDeletedIcalobject?.count,0)             // inserted object was found
        cursorDeletedIcalobject?.close()
        val cursorDeletedComment: Cursor? = mContentResolver?.query(newCommentUri!!, arrayOf(NotesX5Contract.X5Comment.ID, NotesX5Contract.X5Comment.TEXT), "${NotesX5Contract.X5Comment.TEXT} = ?", arrayOf("comment2"), null)
        assertEquals(cursorDeletedComment?.count,0)             // inserted object was found
        cursorUpdatedComment?.close()

    }




    @Test
    fun contact_insert_find_update_delete()  {

        // INSERT a new ICalObject
        val icalobjectValues = ContentValues()
        icalobjectValues.put(NotesX5Contract.X5ICalObject.SUMMARY, "journal4contact")
        icalobjectValues.put(NotesX5Contract.X5ICalObject.DTSTART, System.currentTimeMillis())
        icalobjectValues.put(NotesX5Contract.X5ICalObject.COMPONENT, "JOURNAL")
        val newIcalUri = mContentResolver?.insert(URI_ICALOBJECT, icalobjectValues)
        val newICalObjectId = newIcalUri?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newICalObjectId", newICalObjectId.toString())
        assertNotNull(newICalObjectId)

        // QUERY the new value is skipped, instead we insert a new contact
        // INSERT a new Contact
        val contactValues = ContentValues()
        contactValues.put(NotesX5Contract.X5Contact.ICALOBJECT_ID, newICalObjectId)
        contactValues.put(NotesX5Contract.X5Contact.TEXT, "contact1")
        val newContactUri = mContentResolver?.insert(URI_CONTACTS, contactValues)
        assertNotNull(newContactUri)

        //QUERY the Contact
        val cursorNewContact: Cursor? = mContentResolver?.query(newContactUri!!, arrayOf(NotesX5Contract.X5Contact.ID), null, null, null)
        assertEquals(cursorNewContact?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedContactValues = ContentValues()
        updatedContactValues.put(NotesX5Contract.X5Contact.TEXT, "contact2")
        val countUpdated = mContentResolver?.update(newContactUri!!, updatedContactValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorUpdatedContact: Cursor? = mContentResolver?.query(newContactUri!!, arrayOf(NotesX5Contract.X5Contact.ID, NotesX5Contract.X5Contact.TEXT), "${NotesX5Contract.X5Contact.TEXT} = ?", arrayOf("contact2"), null)
        assertEquals(cursorUpdatedContact?.count,1)             // inserted object was found
        cursorUpdatedContact?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newIcalUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        val cursorDeletedIcalobject: Cursor? = mContentResolver?.query(newIcalUri!!, arrayOf(NotesX5Contract.X5ICalObject.ID, NotesX5Contract.X5ICalObject.SUMMARY, NotesX5Contract.X5ICalObject.DESCRIPTION), "${NotesX5Contract.X5ICalObject.SUMMARY} = ?", arrayOf("journal4comment"), null)
        assertEquals(cursorDeletedIcalobject?.count,0)             // inserted object was found
        cursorDeletedIcalobject?.close()
        val cursorDeletedContact: Cursor? = mContentResolver?.query(newContactUri!!, arrayOf(NotesX5Contract.X5Contact.ID, NotesX5Contract.X5Contact.TEXT), "${NotesX5Contract.X5Contact.TEXT} = ?", arrayOf("contact2"), null)
        assertEquals(cursorDeletedContact?.count,0)             // inserted object was found
        cursorUpdatedContact?.close()
    }


    @Test
    fun organizer_insert_find_update_delete()  {

        // INSERT a new ICalObject
        val icalobjectValues = ContentValues()
        icalobjectValues.put(NotesX5Contract.X5ICalObject.SUMMARY, "journal4organizer")
        icalobjectValues.put(NotesX5Contract.X5ICalObject.DTSTART, System.currentTimeMillis())
        icalobjectValues.put(NotesX5Contract.X5ICalObject.COMPONENT, "JOURNAL")
        val newIcalUri = mContentResolver?.insert(URI_ICALOBJECT, icalobjectValues)
        val newICalObjectId = newIcalUri?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newICalObjectId", newICalObjectId.toString())
        assertNotNull(newICalObjectId)

        // QUERY the new value is skipped, instead we insert a new contact
        // INSERT a new Contact
        val organizerValues = ContentValues()
        organizerValues.put(NotesX5Contract.X5Organizer.ICALOBJECT_ID, newICalObjectId)
        organizerValues.put(NotesX5Contract.X5Organizer.CALADDRESS, "mailto:test@test.com")
        val newOrganizerUri = mContentResolver?.insert(URI_ORGANIZER, organizerValues)
        assertNotNull(newOrganizerUri)

        //QUERY the Contact
        val cursorNewOrganizer: Cursor? = mContentResolver?.query(newOrganizerUri!!, arrayOf(NotesX5Contract.X5Organizer.ID), null, null, null)
        assertEquals(cursorNewOrganizer?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedOrganizerValues = ContentValues()
        updatedOrganizerValues.put(NotesX5Contract.X5Organizer.CALADDRESS, "mailto:test@test.net")
        val countUpdated = mContentResolver?.update(newOrganizerUri!!, updatedOrganizerValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorUpdatedOrganizer: Cursor? = mContentResolver?.query(newOrganizerUri!!, arrayOf(NotesX5Contract.X5Organizer.ID, NotesX5Contract.X5Organizer.CALADDRESS), "${NotesX5Contract.X5Organizer.CALADDRESS} = ?", arrayOf("mailto:test@test.net"), null)
        assertEquals(cursorUpdatedOrganizer?.count,1)             // inserted object was found
        cursorUpdatedOrganizer?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newIcalUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        val cursorDeletedIcalobject: Cursor? = mContentResolver?.query(newIcalUri!!, arrayOf(NotesX5Contract.X5ICalObject.ID, NotesX5Contract.X5ICalObject.SUMMARY, NotesX5Contract.X5ICalObject.DESCRIPTION), "${NotesX5Contract.X5ICalObject.SUMMARY} = ?", arrayOf("journal4comment"), null)
        assertEquals(cursorDeletedIcalobject?.count,0)             // inserted object was found
        cursorDeletedIcalobject?.close()
        val cursorDeletedOrganizer: Cursor? = mContentResolver?.query(newOrganizerUri!!, arrayOf(NotesX5Contract.X5Organizer.ID, NotesX5Contract.X5Organizer.CALADDRESS), "${NotesX5Contract.X5Organizer.CALADDRESS} = ?", arrayOf("mailto:test@test.net"), null)
        assertEquals(cursorDeletedOrganizer?.count,0)             // inserted object was found
        cursorUpdatedOrganizer?.close()
    }



    @Test
    fun relatedto_insert_find_update_delete()  {

        // INSERT a new ICalObject
        val icalobjectValues = ContentValues()
        icalobjectValues.put(NotesX5Contract.X5ICalObject.SUMMARY, "journal4relatedto")
        icalobjectValues.put(NotesX5Contract.X5ICalObject.DTSTART, System.currentTimeMillis())
        icalobjectValues.put(NotesX5Contract.X5ICalObject.COMPONENT, "JOURNAL")
        val newIcalUri = mContentResolver?.insert(URI_ICALOBJECT, icalobjectValues)
        val newICalObjectId = newIcalUri?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newICalObjectId", newICalObjectId.toString())
        assertNotNull(newICalObjectId)

        // QUERY the new value is skipped, instead we insert a new contact
        // INSERT a new Contact
        val relatedtoValues = ContentValues()
        relatedtoValues.put(NotesX5Contract.X5Relatedto.ICALOBJECT_ID, newICalObjectId)
        relatedtoValues.put(NotesX5Contract.X5Relatedto.LINKEDICALOBJECT_ID, newICalObjectId)
        relatedtoValues.put(NotesX5Contract.X5Relatedto.RELTYPE, "Child")
        val newRelatedtoUri = mContentResolver?.insert(URI_RELATEDTO, relatedtoValues)
        assertNotNull(newRelatedtoUri)

        //QUERY the Relatedto
        val cursorNewRelatedto: Cursor? = mContentResolver?.query(newRelatedtoUri!!, arrayOf(NotesX5Contract.X5Relatedto.ID), null, null, null)
        assertEquals(cursorNewRelatedto?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedRelatedtoValues = ContentValues()
        updatedRelatedtoValues.put(NotesX5Contract.X5Relatedto.RELTYPE, "Parent")
        val countUpdated = mContentResolver?.update(newRelatedtoUri!!, updatedRelatedtoValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorUpdatedRelatedto: Cursor? = mContentResolver?.query(newRelatedtoUri!!, arrayOf(NotesX5Contract.X5Relatedto.ID, NotesX5Contract.X5Relatedto.ICALOBJECT_ID), "${NotesX5Contract.X5Relatedto.ICALOBJECT_ID} = ?", arrayOf(newICalObjectId.toString()), null)
        assertEquals(cursorUpdatedRelatedto?.count,1)             // inserted object was found
        cursorUpdatedRelatedto?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newIcalUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        val cursorDeletedIcalobject: Cursor? = mContentResolver?.query(newIcalUri!!, arrayOf(NotesX5Contract.X5ICalObject.ID, NotesX5Contract.X5ICalObject.SUMMARY, NotesX5Contract.X5ICalObject.DESCRIPTION), "${NotesX5Contract.X5ICalObject.SUMMARY} = ?", arrayOf("journal4relatedto"), null)
        assertEquals(cursorDeletedIcalobject?.count,0)             // inserted object was found
        cursorDeletedIcalobject?.close()
        val cursorDeletedRelatedto: Cursor? = mContentResolver?.query(newRelatedtoUri!!, arrayOf(NotesX5Contract.X5Relatedto.ID, NotesX5Contract.X5Relatedto.ICALOBJECT_ID), "${NotesX5Contract.X5Relatedto.ICALOBJECT_ID} = ?", arrayOf(newICalObjectId.toString()), null)
        assertEquals(cursorDeletedRelatedto?.count,0)             // inserted object was found
        cursorUpdatedRelatedto?.close()
    }



    @Test
    fun resource_insert_find_update_delete()  {

        // INSERT a new ICalObject
        val icalobjectValues = ContentValues()
        icalobjectValues.put(NotesX5Contract.X5ICalObject.SUMMARY, "journal4resource")
        icalobjectValues.put(NotesX5Contract.X5ICalObject.DTSTART, System.currentTimeMillis())
        icalobjectValues.put(NotesX5Contract.X5ICalObject.COMPONENT, "JOURNAL")
        val newIcalUri = mContentResolver?.insert(URI_ICALOBJECT, icalobjectValues)
        val newICalObjectId = newIcalUri?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newICalObjectId", newICalObjectId.toString())
        assertNotNull(newICalObjectId)

        // QUERY the new value is skipped, instead we insert a new contact
        // INSERT a new Contact
        val resourceValues = ContentValues()
        resourceValues.put(NotesX5Contract.X5Resource.ICALOBJECT_ID, newICalObjectId)
        resourceValues.put(NotesX5Contract.X5Resource.TEXT, "projector")
        val newResourceUri = mContentResolver?.insert(URI_RESOURCE, resourceValues)
        assertNotNull(newResourceUri)

        //QUERY the Resource
        val cursorNewResource: Cursor? = mContentResolver?.query(newResourceUri!!, arrayOf(NotesX5Contract.X5Resource.ID), null, null, null)
        assertEquals(cursorNewResource?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedResourceValues = ContentValues()
        updatedResourceValues.put(NotesX5Contract.X5Resource.TEXT, "microphone")
        val countUpdated = mContentResolver?.update(newResourceUri!!, updatedResourceValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorUpdatedResource: Cursor? = mContentResolver?.query(newResourceUri!!, arrayOf(NotesX5Contract.X5Resource.ID, NotesX5Contract.X5Resource.TEXT), "${NotesX5Contract.X5Resource.TEXT} = ?", arrayOf("microphone"), null)
        assertEquals(cursorUpdatedResource?.count,1)             // inserted object was found
        cursorUpdatedResource?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newIcalUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        val cursorDeletedIcalobject: Cursor? = mContentResolver?.query(newIcalUri!!, arrayOf(NotesX5Contract.X5ICalObject.ID, NotesX5Contract.X5ICalObject.SUMMARY, NotesX5Contract.X5ICalObject.DESCRIPTION), "${NotesX5Contract.X5ICalObject.SUMMARY} = ?", arrayOf("journal4comment"), null)
        assertEquals(cursorDeletedIcalobject?.count,0)             // inserted object was found
        cursorDeletedIcalobject?.close()
        val cursorDeletedResource: Cursor? = mContentResolver?.query(newResourceUri!!, arrayOf(NotesX5Contract.X5Resource.ID, NotesX5Contract.X5Resource.TEXT), "${NotesX5Contract.X5Resource.TEXT} = ?", arrayOf("microphone"), null)
        assertEquals(cursorDeletedResource?.count,0)             // inserted object was found
        cursorUpdatedResource?.close()
    }



    @Test
    fun collection_insert_find_update_delete()  {

        // INSERT a new Collection
        val collectionValues = ContentValues()
        collectionValues.put(NotesX5Contract.X5Collection.DISPLAYNAME, "testcollection")
        collectionValues.put(NotesX5Contract.X5Collection.URL, "https://testcollection")

        val newCollection = mContentResolver?.insert(URI_COLLECTION, collectionValues)
        val newCollectionId = newCollection?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newCollectionId", newCollectionId.toString())
        assertNotNull(newCollectionId)

        //QUERY the Collection
        val cursorNewCollection: Cursor? = mContentResolver?.query(newCollection!!, arrayOf(NotesX5Contract.X5Collection.ID), null, null, null)
        assertEquals(cursorNewCollection?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedCollectionValues = ContentValues()
        updatedCollectionValues.put(NotesX5Contract.X5Collection.DISPLAYNAME, "testcollection updated")
        val countUpdated = mContentResolver?.update(newCollection!!, updatedCollectionValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorUpdatedCollection: Cursor? = mContentResolver?.query(newCollection!!, arrayOf(NotesX5Contract.X5Collection.ID, NotesX5Contract.X5Collection.DISPLAYNAME), "${NotesX5Contract.X5Collection.DISPLAYNAME} = ?", arrayOf("testcollection updated"), null)
        assertEquals(cursorUpdatedCollection?.count,1)             // inserted object was found
        cursorUpdatedCollection?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newCollection!!, null, null)
        assertEquals(countDeleted, 1)
    }









    @Test(expected = IllegalArgumentException::class)                    // needed to assert exceptions, see e.g. https://www.baeldung.com/junit-assert-exception
    fun query_invalid_url()  {
        val uriInvalid = Uri.parse("content://${NotesX5Contract.AUTHORITY}/invalid")
        mContentResolver?.query(uriInvalid, arrayOf(NotesX5Contract.X5ICalObject.ID), null, null, null)
    }

    @Test(expected = IllegalArgumentException::class)                    // needed to assert exceptions, see e.g. https://www.baeldung.com/junit-assert-exception
    fun query_valid_url_with_wrong_parameter()  {
        val uriWrong = Uri.parse("content://${NotesX5Contract.AUTHORITY}/$TABLE_NAME_ICALOBJECT/asdf")
        mContentResolver?.query(uriWrong, arrayOf(NotesX5Contract.X5ICalObject.ID), null, null, null)
    }


    @Test
    fun check_for_SQL_injection_through_contentValues()  {

        // INSERT a new value, this one must remain
        val contentValues = ContentValues()
        contentValues.put(NotesX5Contract.X5ICalObject.SUMMARY, "note2update")
        mContentResolver?.insert(URI_ICALOBJECT, contentValues)


        val contentValuesCurrupted = ContentValues()
        contentValuesCurrupted.put(NotesX5Contract.X5ICalObject.SUMMARY, "note2corrupted\"; delete * from $TABLE_NAME_ICALOBJECT")
        val newUri2 = mContentResolver?.insert(URI_ICALOBJECT, contentValuesCurrupted)


        val cursor: Cursor? = mContentResolver?.query(newUri2!!, arrayOf(NotesX5Contract.X5ICalObject.ID, NotesX5Contract.X5ICalObject.SUMMARY), null, null, null)
        assertEquals(cursor?.count, 1)
//        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, the new id is ${cursor?.getString(0)}")

        cursor?.close()
    }


    @Test
    fun check_for_SQL_injection_through_query()  {

        // INSERT a new value, this one must remain
        val contentValues = ContentValues()
        contentValues.put(NotesX5Contract.X5ICalObject.SUMMARY, "note2check")
        val newUri = mContentResolver?.insert(URI_ICALOBJECT, contentValues)

        val cursor: Cursor? = mContentResolver?.query(newUri!!, arrayOf(NotesX5Contract.X5ICalObject.ID, NotesX5Contract.X5ICalObject.SUMMARY), "${NotesX5Contract.X5ICalObject.SUMMARY} = ?); DELETE * FROM $TABLE_NAME_ICALOBJECT", arrayOf("note2check"), null)
        assertEquals(cursor?.count,1)
//        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, the new id is ${cursor?.getString(0)}")
        cursor?.close()

        val cursor2: Cursor? = mContentResolver?.query(URI_ICALOBJECT, arrayOf(NotesX5Contract.X5ICalObject.ID), null, null, null)
        assertTrue(cursor2?.count!! > 0)     // there must be entries! Delete must not be executed!
        Log.println(Log.INFO, "icalObject_initiallyEmpty", "Assert successful, DB is empty (Cursor count: ${cursor?.count})")
        cursor?.close()
    }

    @Test(expected = IllegalArgumentException::class)
    fun check_for_invalid_URI_without_IS_SYNC_ADAPTER()  {

        val queryParamsInavalid = "$ACCOUNT_NAME=test&$ACCOUNT_TYPE=test"
        val uri = Uri.parse("content://${NotesX5Contract.AUTHORITY}/$TABLE_NAME_ICALOBJECT?$queryParamsInavalid")

        val contentValues = ContentValues()
        contentValues.put(NotesX5Contract.X5ICalObject.SUMMARY, "note2check")
        val newUri = mContentResolver?.insert(uri, contentValues)
    }

    @Test(expected = IllegalArgumentException::class)
    fun check_for_invalid_URI_without_ACCOUNT_NAME()  {

        val queryParamsInavalid = "$CALLER_IS_SYNCADAPTER=false&$ACCOUNT_TYPE=test"
        val uri = Uri.parse("content://${NotesX5Contract.AUTHORITY}/$TABLE_NAME_ICALOBJECT?$queryParamsInavalid")

        val contentValues = ContentValues()
        contentValues.put(NotesX5Contract.X5ICalObject.SUMMARY, "note2check")
        val newUri = mContentResolver?.insert(uri, contentValues)
    }


    @Test(expected = IllegalArgumentException::class)
    fun check_for_invalid_URI_without_ACCOUNT_TYPE()  {

        val queryParamsInavalid = "$CALLER_IS_SYNCADAPTER=false&$ACCOUNT_NAME=test&"
        val uri = Uri.parse("content://${NotesX5Contract.AUTHORITY}/$TABLE_NAME_ICALOBJECT?$queryParamsInavalid")

        val contentValues = ContentValues()
        contentValues.put(NotesX5Contract.X5ICalObject.SUMMARY, "note2check")
        val newUri = mContentResolver?.insert(uri, contentValues)
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