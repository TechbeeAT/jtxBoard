package at.bitfire.notesx5

import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@SmallTest
class SyncContentProviderTest {

    private var mContentResolver: ContentResolver? = null

    //private val queryParams = "$CALLER_IS_SYNCADAPTER=true&$ACCOUNT_NAME=test&$ACCOUNT_TYPE=test"
    private val queryParams = "$CALLER_IS_SYNCADAPTER=true&$ACCOUNT_NAME=test&$ACCOUNT_TYPE=test"

    private val URI_ICALOBJECT = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_ICALOBJECT?$queryParams")
    private val URI_ATTENDEES = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_ATTENDEE?$queryParams")
    private val URI_CATEGORIES = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_CATEGORY?$queryParams")
    private val URI_COMMENTS = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_COMMENT?$queryParams")
    private val URI_CONTACTS = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_CONTACT?$queryParams")
    private val URI_ORGANIZER = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_ORGANIZER?$queryParams")
    private val URI_RELATEDTO = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_RELATEDTO?$queryParams")
    private val URI_RESOURCE = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_RESOURCE?$queryParams")
    private val URI_COLLECTION = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_COLLECTION?$queryParams")


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


    @Test
    fun icalObject_insert_find_delete()  {

        val contentValues = ContentValues()
        contentValues.put(COLUMN_SUMMARY, "note2delete")
        val newUri = mContentResolver?.insert(URI_ICALOBJECT, contentValues)


        val cursor: Cursor? = mContentResolver?.query(newUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY), null, null, null)
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
        contentValues.put(COLUMN_SUMMARY, "note2update")
        val newUri = mContentResolver?.insert(URI_ICALOBJECT, contentValues)

        // QUERY the new value
        val cursor: Cursor? = mContentResolver?.query(newUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY), null, null, null)
        assertNotNull(cursor)
        assertEquals(cursor?.count, 1)             // inserted object was found


//        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, the new id is ${cursor?.getString(0)}")

        // UPDATE the new value
        val updatedContentValues = ContentValues()
        updatedContentValues.put(COLUMN_DESCRIPTION, "description was updated")
        val countUpdated = mContentResolver?.update(newUri!!, updatedContentValues, null, null)
        assertEquals(countUpdated, 1)
        Log.println(Log.INFO, "icalObject_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        //val cursor2: Cursor? = mContentResolver?.query(newUri!!, arrayOf<String>(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        val cursor2: Cursor? = mContentResolver?.query(newUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        assertEquals(cursor2?.count,1)             // inserted object was found
        cursor2?.close()

        // DELETE the updated value
        val countDeleted = mContentResolver?.delete(newUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        //val cursor2: Cursor? = mContentResolver?.query(newUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        val cursor3: Cursor? = mContentResolver?.query(newUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        assertEquals(cursor3?.count,0)             // inserted object was found
        cursor3?.close()

    }



    @Test
    fun attendee_insert_find_update_delete()  {

        // INSERT a new ICalObject
        val contentValues = ContentValues()
        contentValues.put(COLUMN_SUMMARY, "journal4attendee")
        contentValues.put(COLUMN_DTSTART, System.currentTimeMillis())
        contentValues.put(COLUMN_COMPONENT, "JOURNAL")
        val newUri = mContentResolver?.insert(URI_ICALOBJECT, contentValues)
        val newICalObjectId = newUri?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newICalObjectId", newICalObjectId.toString())
        assertNotNull(newICalObjectId)

        // QUERY the new value is skipped, instead we insert a new attendee
        // INSERT a new Attendee
        val attendeeValues = ContentValues()
        attendeeValues.put(COLUMN_ATTENDEE_ICALOBJECT_ID, newICalObjectId)
        attendeeValues.put(COLUMN_ATTENDEE_CALADDRESS, "mailto:test@test.com")
        val newAttendeeUri = mContentResolver?.insert(URI_ATTENDEES, attendeeValues)
        assertNotNull(newAttendeeUri)

        //QUERY the Attendee
        val cursorIcalobject: Cursor? = mContentResolver?.query(newAttendeeUri!!, arrayOf(COLUMN_ATTENDEE_ID), null, null, null)
        assertEquals(cursorIcalobject?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedAttendeeValues = ContentValues()
        updatedAttendeeValues.put(COLUMN_ATTENDEE_CALADDRESS, "mailto:test@test.net")
        val countUpdated = mContentResolver?.update(newAttendeeUri!!, updatedAttendeeValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorAttendee: Cursor? = mContentResolver?.query(newAttendeeUri!!, arrayOf(COLUMN_ATTENDEE_ID, COLUMN_ATTENDEE_CALADDRESS), "$COLUMN_ATTENDEE_CALADDRESS = ?", arrayOf("mailto:test@test.net"), null)
        assertEquals(cursorAttendee?.count,1)             // inserted object was found
        cursorAttendee?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        //val cursor2: Cursor? = mContentResolver?.query(newUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        val cursorDeletedIcalobject: Cursor? = mContentResolver?.query(newUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        assertEquals(cursorDeletedIcalobject?.count,0)             // inserted object was found
        cursorDeletedIcalobject?.close()
        val cursorDeletedAttendee: Cursor? = mContentResolver?.query(newAttendeeUri!!, arrayOf(COLUMN_ATTENDEE_ID, COLUMN_ATTENDEE_CALADDRESS), "$COLUMN_ATTENDEE_CALADDRESS = ?", arrayOf("mailto:test@test.net"), null)
        assertEquals(cursorDeletedAttendee?.count,0)             // inserted object was found
        cursorAttendee?.close()
    }



    @Test
    fun category_insert_find_update_delete()  {

        // INSERT a new ICalObject
        val icalobjectValues = ContentValues()
        icalobjectValues.put(COLUMN_SUMMARY, "journal4category")
        icalobjectValues.put(COLUMN_DTSTART, System.currentTimeMillis())
        icalobjectValues.put(COLUMN_COMPONENT, "JOURNAL")
        val newIcalUri = mContentResolver?.insert(URI_ICALOBJECT, icalobjectValues)
        val newICalObjectId = newIcalUri?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newICalObjectId", newICalObjectId.toString())
        assertNotNull(newICalObjectId)

        // QUERY the new value is skipped, instead we insert a new category
        // INSERT a new Category
        val categoryValues = ContentValues()
        categoryValues.put(COLUMN_CATEGORY_ICALOBJECT_ID, newICalObjectId)
        categoryValues.put(COLUMN_CATEGORY_TEXT, "category1")
        val newCategoryUri = mContentResolver?.insert(URI_CATEGORIES, categoryValues)
        assertNotNull(newCategoryUri)

        //QUERY the Category
        val cursorNewCategory: Cursor? = mContentResolver?.query(newCategoryUri!!, arrayOf(COLUMN_CATEGORY_ID), null, null, null)
        assertEquals(cursorNewCategory?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedCategoryValues = ContentValues()
        updatedCategoryValues.put(COLUMN_CATEGORY_TEXT, "category2")
        val countUpdated = mContentResolver?.update(newCategoryUri!!, updatedCategoryValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorUpdatedCategory: Cursor? = mContentResolver?.query(newCategoryUri!!, arrayOf(COLUMN_CATEGORY_ID, COLUMN_CATEGORY_TEXT), "$COLUMN_CATEGORY_TEXT = ?", arrayOf("category2"), null)
        assertEquals(cursorUpdatedCategory?.count,1)             // inserted object was found
        cursorUpdatedCategory?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newIcalUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        //val cursor2: Cursor? = mContentResolver?.query(newUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        val cursorDeletedIcalobject: Cursor? = mContentResolver?.query(newIcalUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("journal4category"), null)
        assertEquals(cursorDeletedIcalobject?.count,0)             // inserted object was found
        cursorDeletedIcalobject?.close()
        val cursorDeletedCategory: Cursor? = mContentResolver?.query(newCategoryUri!!, arrayOf(COLUMN_CATEGORY_ID, COLUMN_CATEGORY_TEXT), "$COLUMN_CATEGORY_TEXT = ?", arrayOf("category2"), null)
        assertEquals(cursorDeletedCategory?.count,0)             // inserted object was found
        cursorUpdatedCategory?.close()
    }




    @Test
    fun comment_insert_find_update_delete()  {

        // INSERT a new ICalObject
        val icalobjectValues = ContentValues()
        icalobjectValues.put(COLUMN_SUMMARY, "journal4comment")
        icalobjectValues.put(COLUMN_DTSTART, System.currentTimeMillis())
        icalobjectValues.put(COLUMN_COMPONENT, "JOURNAL")
        val newIcalUri = mContentResolver?.insert(URI_ICALOBJECT, icalobjectValues)
        val newICalObjectId = newIcalUri?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newICalObjectId", newICalObjectId.toString())
        assertNotNull(newICalObjectId)

        // QUERY the new value is skipped, instead we insert a new category
        // INSERT a new Category
        val commentValues = ContentValues()
        commentValues.put(COLUMN_COMMENT_ICALOBJECT_ID, newICalObjectId)
        commentValues.put(COLUMN_COMMENT_TEXT, "comment1")
        val newCommentUri = mContentResolver?.insert(URI_COMMENTS, commentValues)
        assertNotNull(newCommentUri)

        //QUERY the Category
        val cursorNewComment: Cursor? = mContentResolver?.query(newCommentUri!!, arrayOf(COLUMN_COMMENT_ID), null, null, null)
        assertEquals(cursorNewComment?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedCommentValues = ContentValues()
        updatedCommentValues.put(COLUMN_COMMENT_TEXT, "comment2")
        val countUpdated = mContentResolver?.update(newCommentUri!!, updatedCommentValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorUpdatedComment: Cursor? = mContentResolver?.query(newCommentUri!!, arrayOf(COLUMN_COMMENT_ID, COLUMN_COMMENT_TEXT), "$COLUMN_COMMENT_TEXT = ?", arrayOf("comment2"), null)
        assertEquals(cursorUpdatedComment?.count,1)             // inserted object was found
        cursorUpdatedComment?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newIcalUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        //val cursor2: Cursor? = mContentResolver?.query(newUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        val cursorDeletedIcalobject: Cursor? = mContentResolver?.query(newIcalUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("journal4comment"), null)
        assertEquals(cursorDeletedIcalobject?.count,0)             // inserted object was found
        cursorDeletedIcalobject?.close()
        val cursorDeletedComment: Cursor? = mContentResolver?.query(newCommentUri!!, arrayOf(COLUMN_COMMENT_ID, COLUMN_COMMENT_TEXT), "$COLUMN_COMMENT_TEXT = ?", arrayOf("comment2"), null)
        assertEquals(cursorDeletedComment?.count,0)             // inserted object was found
        cursorUpdatedComment?.close()

    }




    @Test
    fun contact_insert_find_update_delete()  {

        // INSERT a new ICalObject
        val icalobjectValues = ContentValues()
        icalobjectValues.put(COLUMN_SUMMARY, "journal4contact")
        icalobjectValues.put(COLUMN_DTSTART, System.currentTimeMillis())
        icalobjectValues.put(COLUMN_COMPONENT, "JOURNAL")
        val newIcalUri = mContentResolver?.insert(URI_ICALOBJECT, icalobjectValues)
        val newICalObjectId = newIcalUri?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newICalObjectId", newICalObjectId.toString())
        assertNotNull(newICalObjectId)

        // QUERY the new value is skipped, instead we insert a new contact
        // INSERT a new Contact
        val contactValues = ContentValues()
        contactValues.put(COLUMN_CONTACT_ICALOBJECT_ID, newICalObjectId)
        contactValues.put(COLUMN_CONTACT_TEXT, "contact1")
        val newContactUri = mContentResolver?.insert(URI_CONTACTS, contactValues)
        assertNotNull(newContactUri)

        //QUERY the Contact
        val cursorNewContact: Cursor? = mContentResolver?.query(newContactUri!!, arrayOf(COLUMN_CONTACT_ID), null, null, null)
        assertEquals(cursorNewContact?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedContactValues = ContentValues()
        updatedContactValues.put(COLUMN_COMMENT_TEXT, "contact2")
        val countUpdated = mContentResolver?.update(newContactUri!!, updatedContactValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorUpdatedContact: Cursor? = mContentResolver?.query(newContactUri!!, arrayOf(COLUMN_CONTACT_ID, COLUMN_CONTACT_TEXT), "$COLUMN_CONTACT_TEXT = ?", arrayOf("contact2"), null)
        assertEquals(cursorUpdatedContact?.count,1)             // inserted object was found
        cursorUpdatedContact?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newIcalUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        //val cursor2: Cursor? = mContentResolver?.query(newUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        val cursorDeletedIcalobject: Cursor? = mContentResolver?.query(newIcalUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("journal4comment"), null)
        assertEquals(cursorDeletedIcalobject?.count,0)             // inserted object was found
        cursorDeletedIcalobject?.close()
        val cursorDeletedContact: Cursor? = mContentResolver?.query(newContactUri!!, arrayOf(COLUMN_CONTACT_ID, COLUMN_CONTACT_TEXT), "$COLUMN_CONTACT_TEXT = ?", arrayOf("contact2"), null)
        assertEquals(cursorDeletedContact?.count,0)             // inserted object was found
        cursorUpdatedContact?.close()
    }


    @Test
    fun organizer_insert_find_update_delete()  {

        // INSERT a new ICalObject
        val icalobjectValues = ContentValues()
        icalobjectValues.put(COLUMN_SUMMARY, "journal4organizer")
        icalobjectValues.put(COLUMN_DTSTART, System.currentTimeMillis())
        icalobjectValues.put(COLUMN_COMPONENT, "JOURNAL")
        val newIcalUri = mContentResolver?.insert(URI_ICALOBJECT, icalobjectValues)
        val newICalObjectId = newIcalUri?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newICalObjectId", newICalObjectId.toString())
        assertNotNull(newICalObjectId)

        // QUERY the new value is skipped, instead we insert a new contact
        // INSERT a new Contact
        val organizerValues = ContentValues()
        organizerValues.put(COLUMN_ORGANIZER_ICALOBJECT_ID, newICalObjectId)
        organizerValues.put(COLUMN_ORGANIZER_CALADDRESS, "mailto:test@test.com")
        val newOrganizerUri = mContentResolver?.insert(URI_ORGANIZER, organizerValues)
        assertNotNull(newOrganizerUri)

        //QUERY the Contact
        val cursorNewOrganizer: Cursor? = mContentResolver?.query(newOrganizerUri!!, arrayOf(COLUMN_ORGANIZER_ID), null, null, null)
        assertEquals(cursorNewOrganizer?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedOrganizerValues = ContentValues()
        updatedOrganizerValues.put(COLUMN_ORGANIZER_CALADDRESS, "mailto:test@test.net")
        val countUpdated = mContentResolver?.update(newOrganizerUri!!, updatedOrganizerValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorUpdatedOrganizer: Cursor? = mContentResolver?.query(newOrganizerUri!!, arrayOf(COLUMN_ORGANIZER_ID, COLUMN_ORGANIZER_CALADDRESS), "$COLUMN_ORGANIZER_CALADDRESS = ?", arrayOf("mailto:test@test.net"), null)
        assertEquals(cursorUpdatedOrganizer?.count,1)             // inserted object was found
        cursorUpdatedOrganizer?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newIcalUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        //val cursor2: Cursor? = mContentResolver?.query(newUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        val cursorDeletedIcalobject: Cursor? = mContentResolver?.query(newIcalUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("journal4comment"), null)
        assertEquals(cursorDeletedIcalobject?.count,0)             // inserted object was found
        cursorDeletedIcalobject?.close()
        val cursorDeletedOrganizer: Cursor? = mContentResolver?.query(newOrganizerUri!!, arrayOf(COLUMN_ORGANIZER_ID, COLUMN_ORGANIZER_CALADDRESS), "$COLUMN_ORGANIZER_CALADDRESS = ?", arrayOf("mailto:test@test.net"), null)
        assertEquals(cursorDeletedOrganizer?.count,0)             // inserted object was found
        cursorUpdatedOrganizer?.close()
    }



    @Test
    fun relatedto_insert_find_update_delete()  {

        // INSERT a new ICalObject
        val icalobjectValues = ContentValues()
        icalobjectValues.put(COLUMN_SUMMARY, "journal4relatedto")
        icalobjectValues.put(COLUMN_DTSTART, System.currentTimeMillis())
        icalobjectValues.put(COLUMN_COMPONENT, "JOURNAL")
        val newIcalUri = mContentResolver?.insert(URI_ICALOBJECT, icalobjectValues)
        val newICalObjectId = newIcalUri?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newICalObjectId", newICalObjectId.toString())
        assertNotNull(newICalObjectId)

        // QUERY the new value is skipped, instead we insert a new contact
        // INSERT a new Contact
        val relatedtoValues = ContentValues()
        relatedtoValues.put(COLUMN_RELATEDTO_ICALOBJECT_ID, newICalObjectId)
        relatedtoValues.put(COLUMN_RELATEDTO_LINKEDICALOBJECT_ID, newICalObjectId)
        relatedtoValues.put(COLUMN_RELATEDTO_RELTYPEPARAM, "Child")
        val newRelatedtoUri = mContentResolver?.insert(URI_RELATEDTO, relatedtoValues)
        assertNotNull(newRelatedtoUri)

        //QUERY the Relatedto
        val cursorNewRelatedto: Cursor? = mContentResolver?.query(newRelatedtoUri!!, arrayOf(COLUMN_RELATEDTO_ID), null, null, null)
        assertEquals(cursorNewRelatedto?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedRelatedtoValues = ContentValues()
        updatedRelatedtoValues.put(COLUMN_RELATEDTO_RELTYPEPARAM, "Parent")
        val countUpdated = mContentResolver?.update(newRelatedtoUri!!, updatedRelatedtoValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorUpdatedRelatedto: Cursor? = mContentResolver?.query(newRelatedtoUri!!, arrayOf(COLUMN_RELATEDTO_ID, COLUMN_RELATEDTO_ICALOBJECT_ID), "$COLUMN_RELATEDTO_ICALOBJECT_ID = ?", arrayOf(newICalObjectId.toString()), null)
        assertEquals(cursorUpdatedRelatedto?.count,1)             // inserted object was found
        cursorUpdatedRelatedto?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newIcalUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        //val cursor2: Cursor? = mContentResolver?.query(newUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        val cursorDeletedIcalobject: Cursor? = mContentResolver?.query(newIcalUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("journal4relatedto"), null)
        assertEquals(cursorDeletedIcalobject?.count,0)             // inserted object was found
        cursorDeletedIcalobject?.close()
        val cursorDeletedRelatedto: Cursor? = mContentResolver?.query(newRelatedtoUri!!, arrayOf(COLUMN_RELATEDTO_ID, COLUMN_RELATEDTO_ICALOBJECT_ID), "$COLUMN_RELATEDTO_ICALOBJECT_ID = ?", arrayOf(newICalObjectId.toString()), null)
        assertEquals(cursorDeletedRelatedto?.count,0)             // inserted object was found
        cursorUpdatedRelatedto?.close()
    }



    @Test
    fun resource_insert_find_update_delete()  {

        // INSERT a new ICalObject
        val icalobjectValues = ContentValues()
        icalobjectValues.put(COLUMN_SUMMARY, "journal4resource")
        icalobjectValues.put(COLUMN_DTSTART, System.currentTimeMillis())
        icalobjectValues.put(COLUMN_COMPONENT, "JOURNAL")
        val newIcalUri = mContentResolver?.insert(URI_ICALOBJECT, icalobjectValues)
        val newICalObjectId = newIcalUri?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newICalObjectId", newICalObjectId.toString())
        assertNotNull(newICalObjectId)

        // QUERY the new value is skipped, instead we insert a new contact
        // INSERT a new Contact
        val resourceValues = ContentValues()
        resourceValues.put(COLUMN_RESOURCE_ICALOBJECT_ID, newICalObjectId)
        resourceValues.put(COLUMN_RESOURCE_TEXT, "projector")
        val newResourceUri = mContentResolver?.insert(URI_RESOURCE, resourceValues)
        assertNotNull(newResourceUri)

        //QUERY the Resource
        val cursorNewResource: Cursor? = mContentResolver?.query(newResourceUri!!, arrayOf(COLUMN_RESOURCE_ID), null, null, null)
        assertEquals(cursorNewResource?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedResourceValues = ContentValues()
        updatedResourceValues.put(COLUMN_RESOURCE_TEXT, "microphone")
        val countUpdated = mContentResolver?.update(newResourceUri!!, updatedResourceValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorUpdatedResource: Cursor? = mContentResolver?.query(newResourceUri!!, arrayOf(COLUMN_RESOURCE_ID, COLUMN_RESOURCE_TEXT), "$COLUMN_RESOURCE_TEXT = ?", arrayOf("microphone"), null)
        assertEquals(cursorUpdatedResource?.count,1)             // inserted object was found
        cursorUpdatedResource?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newIcalUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        //val cursor2: Cursor? = mContentResolver?.query(newUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        val cursorDeletedIcalobject: Cursor? = mContentResolver?.query(newIcalUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("journal4comment"), null)
        assertEquals(cursorDeletedIcalobject?.count,0)             // inserted object was found
        cursorDeletedIcalobject?.close()
        val cursorDeletedResource: Cursor? = mContentResolver?.query(newResourceUri!!, arrayOf(COLUMN_RESOURCE_ID, COLUMN_RESOURCE_TEXT), "$COLUMN_RESOURCE_TEXT = ?", arrayOf("microphone"), null)
        assertEquals(cursorDeletedResource?.count,0)             // inserted object was found
        cursorUpdatedResource?.close()
    }



    @Test
    fun collection_insert_find_update_delete()  {

        // INSERT a new Collection
        val collectionValues = ContentValues()
        collectionValues.put(COLUMN_COLLECTION_DISPLAYNAME, "testcollection")
        collectionValues.put(COLUMN_COLLECTION_URL, "https://testcollection")

        val newCollection = mContentResolver?.insert(URI_COLLECTION, collectionValues)
        val newCollectionId = newCollection?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newCollectionId", newCollectionId.toString())
        assertNotNull(newCollectionId)

        //QUERY the Collection
        val cursorNewCollection: Cursor? = mContentResolver?.query(newCollection!!, arrayOf(COLUMN_COLLECTION_ID), null, null, null)
        assertEquals(cursorNewCollection?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedCollectionValues = ContentValues()
        updatedCollectionValues.put(COLUMN_COLLECTION_DISPLAYNAME, "testcollection updated")
        val countUpdated = mContentResolver?.update(newCollection!!, updatedCollectionValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorUpdatedCollection: Cursor? = mContentResolver?.query(newCollection!!, arrayOf(COLUMN_COLLECTION_ID, COLUMN_COLLECTION_DISPLAYNAME), "$COLUMN_COLLECTION_DISPLAYNAME = ?", arrayOf("testcollection updated"), null)
        assertEquals(cursorUpdatedCollection?.count,1)             // inserted object was found
        cursorUpdatedCollection?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newCollection!!, null, null)
        assertEquals(countDeleted, 1)
    }









    @Test(expected = IllegalArgumentException::class)                    // needed to assert exceptions, see e.g. https://www.baeldung.com/junit-assert-exception
    fun query_invalid_url()  {
        val uriInvalid = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/invalid")
        mContentResolver?.query(uriInvalid, arrayOf(COLUMN_ID), null, null, null)
    }

    @Test(expected = IllegalArgumentException::class)                    // needed to assert exceptions, see e.g. https://www.baeldung.com/junit-assert-exception
    fun query_valid_url_with_wrong_parameter()  {
        val uriWrong = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_ICALOBJECT/asdf")
        mContentResolver?.query(uriWrong, arrayOf(COLUMN_ID), null, null, null)
    }


    @Test
    fun check_for_SQL_injection_through_contentValues()  {

        // INSERT a new value, this one must remain
        val contentValues = ContentValues()
        contentValues.put(COLUMN_SUMMARY, "note2update")
        mContentResolver?.insert(URI_ICALOBJECT, contentValues)


        val contentValuesCurrupted = ContentValues()
        contentValuesCurrupted.put(COLUMN_SUMMARY, "note2corrupted\"; delete * from $TABLE_NAME_ICALOBJECT")
        val newUri2 = mContentResolver?.insert(URI_ICALOBJECT, contentValuesCurrupted)


        val cursor: Cursor? = mContentResolver?.query(newUri2!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY), null, null, null)
        assertEquals(cursor?.count, 1)
//        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, the new id is ${cursor?.getString(0)}")

        cursor?.close()
    }


    @Test
    fun check_for_SQL_injection_through_query()  {

        // INSERT a new value, this one must remain
        val contentValues = ContentValues()
        contentValues.put(COLUMN_SUMMARY, "note2check")
        val newUri = mContentResolver?.insert(URI_ICALOBJECT, contentValues)

        val cursor: Cursor? = mContentResolver?.query(newUri!!, arrayOf(COLUMN_ID, COLUMN_SUMMARY), "$COLUMN_SUMMARY = ?); DELETE * FROM $TABLE_NAME_ICALOBJECT", arrayOf("note2check"), null)
        assertEquals(cursor?.count,1)
//        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, the new id is ${cursor?.getString(0)}")
        cursor?.close()

        val cursor2: Cursor? = mContentResolver?.query(URI_ICALOBJECT, arrayOf(COLUMN_ID), null, null, null)
        assertTrue(cursor2?.count!! > 0)     // there must be entries! Delete must not be executed!
        Log.println(Log.INFO, "icalObject_initiallyEmpty", "Assert successful, DB is empty (Cursor count: ${cursor?.count})")
        cursor?.close()
    }

    @Test(expected = IllegalArgumentException::class)
    fun check_for_invalid_URI_without_IS_SYNC_ADAPTER()  {

        val queryParamsInavalid = "$ACCOUNT_NAME=test&$ACCOUNT_TYPE=test"
        val uri = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_ICALOBJECT?$queryParamsInavalid")

        val contentValues = ContentValues()
        contentValues.put(COLUMN_SUMMARY, "note2check")
        val newUri = mContentResolver?.insert(uri, contentValues)
    }

    @Test(expected = IllegalArgumentException::class)
    fun check_for_invalid_URI_without_ACCOUNT_NAME()  {

        val queryParamsInavalid = "$CALLER_IS_SYNCADAPTER=false&$ACCOUNT_TYPE=test"
        val uri = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_ICALOBJECT?$queryParamsInavalid")

        val contentValues = ContentValues()
        contentValues.put(COLUMN_SUMMARY, "note2check")
        val newUri = mContentResolver?.insert(uri, contentValues)
    }


    @Test(expected = IllegalArgumentException::class)
    fun check_for_invalid_URI_without_ACCOUNT_TYPE()  {

        val queryParamsInavalid = "$CALLER_IS_SYNCADAPTER=false&$ACCOUNT_NAME=test&"
        val uri = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_ICALOBJECT?$queryParamsInavalid")

        val contentValues = ContentValues()
        contentValues.put(COLUMN_SUMMARY, "note2check")
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