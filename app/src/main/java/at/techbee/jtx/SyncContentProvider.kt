/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx

import android.accounts.Account
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import androidx.core.content.FileProvider
import androidx.sqlite.db.SimpleSQLiteQuery
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.*
import java.io.File
import java.io.IOException
import android.webkit.MimeTypeMap
import java.lang.NumberFormatException


private const val CODE_ICALOBJECTS_DIR = 1
private const val CODE_ATTENDEES_DIR = 2
private const val CODE_CATEGORIES_DIR = 3
private const val CODE_COMMENTS_DIR = 4
private const val CODE_CONTACTS_DIR = 5
private const val CODE_ORGANIZER_DIR = 6
private const val CODE_RELATEDTO_DIR = 7
private const val CODE_RESOURCE_DIR = 8
private const val CODE_COLLECTION_DIR = 9
private const val CODE_ATTACHMENT_DIR = 10
private const val CODE_ALARM_DIR = 11
private const val CODE_UNKNOWN_DIR = 12



private const val CODE_ICALOBJECT_ITEM = 101
private const val CODE_ATTENDEE_ITEM = 102
private const val CODE_CATEGORY_ITEM = 103
private const val CODE_COMMENT_ITEM = 104
private const val CODE_CONTACT_ITEM = 105
private const val CODE_ORGANIZER_ITEM = 106
private const val CODE_RELATEDTO_ITEM = 107
private const val CODE_RESOURCE_ITEM = 108
private const val CODE_COLLECTION_ITEM = 109
private const val CODE_ATTACHMENT_ITEM = 110
private const val CODE_ALARM_ITEM = 111
private const val CODE_UNKNOWN_ITEM = 112




const val SYNC_PROVIDER_AUTHORITY = "at.techbee.jtx.provider"

const val CALLER_IS_SYNCADAPTER = "caller_is_syncadapter"
const val ACCOUNT_NAME = "account_name"
const val ACCOUNT_TYPE = "account_type"

/** The URI for the Icalobject table.  */



class SyncContentProvider : ContentProvider() {


    private lateinit var database: ICalDatabaseDao
    //private lateinit var context: Context


    private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        /* see https://github.com/android/architecture-components-samples/blob/4c606ccd83fded22a52d0d994b0cb043dafc6dd7/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/provider/SampleContentProvider.java
         * for a good sample and some explanations
         */
        addURI(SYNC_PROVIDER_AUTHORITY, "icalobject", CODE_ICALOBJECTS_DIR)
        addURI(SYNC_PROVIDER_AUTHORITY, "attendee", CODE_ATTENDEES_DIR)
        addURI(SYNC_PROVIDER_AUTHORITY, "category", CODE_CATEGORIES_DIR)
        addURI(SYNC_PROVIDER_AUTHORITY, "comment", CODE_COMMENTS_DIR)
        addURI(SYNC_PROVIDER_AUTHORITY, "contact", CODE_CONTACTS_DIR)
        addURI(SYNC_PROVIDER_AUTHORITY, "organizer", CODE_ORGANIZER_DIR)
        addURI(SYNC_PROVIDER_AUTHORITY, "relatedto", CODE_RELATEDTO_DIR)
        addURI(SYNC_PROVIDER_AUTHORITY, "resource", CODE_RESOURCE_DIR)
        addURI(SYNC_PROVIDER_AUTHORITY, "collection", CODE_COLLECTION_DIR)
        addURI(SYNC_PROVIDER_AUTHORITY, "attachment", CODE_ATTACHMENT_DIR)
        addURI(SYNC_PROVIDER_AUTHORITY, "alarm", CODE_ALARM_DIR)
        addURI(SYNC_PROVIDER_AUTHORITY, "unknown", CODE_UNKNOWN_DIR)


        addURI(SYNC_PROVIDER_AUTHORITY, "icalobject/#", CODE_ICALOBJECT_ITEM)
        addURI(SYNC_PROVIDER_AUTHORITY, "attendee/#", CODE_ATTENDEE_ITEM)
        addURI(SYNC_PROVIDER_AUTHORITY, "category/#", CODE_CATEGORY_ITEM)
        addURI(SYNC_PROVIDER_AUTHORITY, "comment/#", CODE_COMMENT_ITEM)
        addURI(SYNC_PROVIDER_AUTHORITY, "contact/#", CODE_CONTACT_ITEM)
        addURI(SYNC_PROVIDER_AUTHORITY, "organizer/#", CODE_ORGANIZER_ITEM)
        addURI(SYNC_PROVIDER_AUTHORITY, "relatedto/#", CODE_RELATEDTO_ITEM)
        addURI(SYNC_PROVIDER_AUTHORITY, "resource/#", CODE_RESOURCE_ITEM)
        addURI(SYNC_PROVIDER_AUTHORITY, "collection/#", CODE_COLLECTION_ITEM)
        addURI(SYNC_PROVIDER_AUTHORITY, "attachment/#", CODE_ATTACHMENT_ITEM)
        addURI(SYNC_PROVIDER_AUTHORITY, "alarm/#", CODE_ALARM_ITEM)
        addURI(SYNC_PROVIDER_AUTHORITY, "unknown/#", CODE_UNKNOWN_ITEM)
    }



    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {

        if (context == null)
            return 0

        isSyncAdapter(uri)
        val account = getAccountFromUri(uri)
        val count: Int
        val args = arrayListOf(account.name, account.type)
        if (uri.pathSegments.size >= 2)
            args.add(uri.pathSegments[1].toLong().toString())      // add first argument (must be Long! String is expected, toLong would make other values null

        val subquery = "SELECT $TABLE_NAME_ICALOBJECT.$COLUMN_ID " +
                "FROM $TABLE_NAME_ICALOBJECT " +
                "INNER JOIN $TABLE_NAME_COLLECTION ON $TABLE_NAME_ICALOBJECT.$COLUMN_ICALOBJECT_COLLECTIONID = $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ID " +
                "AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_NAME = ? " +
                "AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_TYPE = ? " +
                "AND $TABLE_NAME_ICALOBJECT.$COLUMN_RECUR_ISLINKEDINSTANCE = 0"

        var queryString = "DELETE FROM "

        // The tables must be joined with the collections table in order to make sure that only accounts are affected that were passed in the URI!
        when (sUriMatcher.match(uri)) {
            CODE_ICALOBJECTS_DIR -> queryString += "$TABLE_NAME_ICALOBJECT WHERE $COLUMN_ID IN ($subquery) "
            CODE_ATTENDEES_DIR -> queryString += "$TABLE_NAME_ATTENDEE WHERE $COLUMN_ATTENDEE_ICALOBJECT_ID IN ($subquery) "
            CODE_CATEGORIES_DIR -> queryString += "$TABLE_NAME_CATEGORY WHERE $COLUMN_CATEGORY_ICALOBJECT_ID IN ($subquery) "
            CODE_COMMENTS_DIR -> queryString += "$TABLE_NAME_COMMENT WHERE $COLUMN_COMMENT_ICALOBJECT_ID IN ($subquery) "
            CODE_CONTACTS_DIR -> queryString += "$TABLE_NAME_CONTACT WHERE $COLUMN_CONTACT_ICALOBJECT_ID IN ($subquery) "
            CODE_ORGANIZER_DIR -> queryString += "$TABLE_NAME_ORGANIZER WHERE $COLUMN_ORGANIZER_ICALOBJECT_ID IN ($subquery) "
            CODE_RELATEDTO_DIR -> queryString += "$TABLE_NAME_RELATEDTO WHERE $COLUMN_RELATEDTO_ICALOBJECT_ID IN ($subquery) "
            CODE_RESOURCE_DIR -> queryString += "$TABLE_NAME_RESOURCE WHERE $COLUMN_RESOURCE_ICALOBJECT_ID IN ($subquery) "
            CODE_COLLECTION_DIR -> queryString += "$TABLE_NAME_COLLECTION WHERE $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_NAME = ? AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_TYPE = ? "
            CODE_ATTACHMENT_DIR -> queryString += "$TABLE_NAME_ATTACHMENT WHERE $COLUMN_ATTACHMENT_ICALOBJECT_ID IN ($subquery) "
            CODE_ALARM_DIR -> queryString += "$TABLE_NAME_ALARM WHERE $COLUMN_ALARM_ICALOBJECT_ID IN ($subquery) "
            CODE_UNKNOWN_DIR -> queryString += "$TABLE_NAME_UNKNOWN WHERE $COLUMN_UNKNOWN_ICALOBJECT_ID IN ($subquery) "


            CODE_ICALOBJECT_ITEM -> queryString += "$TABLE_NAME_ICALOBJECT WHERE $COLUMN_ID IN ($subquery) AND $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ? "
            CODE_ATTENDEE_ITEM -> queryString += "$TABLE_NAME_ATTENDEE WHERE $COLUMN_ATTENDEE_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_ATTENDEE.$COLUMN_ATTENDEE_ID = ? "
            CODE_CATEGORY_ITEM -> queryString += "$TABLE_NAME_CATEGORY WHERE $COLUMN_CATEGORY_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ID = ?"
            CODE_COMMENT_ITEM -> queryString += "$TABLE_NAME_COMMENT WHERE $COLUMN_COMMENT_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_COMMENT.$COLUMN_COMMENT_ID = ? "
            CODE_CONTACT_ITEM -> queryString += "$TABLE_NAME_CONTACT WHERE $COLUMN_CONTACT_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_CONTACT.$COLUMN_CONTACT_ID = ? "
            CODE_ORGANIZER_ITEM -> queryString += "$TABLE_NAME_ORGANIZER WHERE $COLUMN_ORGANIZER_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_ORGANIZER.$COLUMN_ORGANIZER_ID = ? "
            CODE_RELATEDTO_ITEM -> queryString += "$TABLE_NAME_RELATEDTO WHERE $COLUMN_RELATEDTO_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_RELATEDTO.$COLUMN_RELATEDTO_ID = ? "
            CODE_RESOURCE_ITEM -> queryString += "$TABLE_NAME_RESOURCE WHERE $COLUMN_RESOURCE_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_RESOURCE.$COLUMN_RESOURCE_ID = ? "
            CODE_COLLECTION_ITEM -> queryString += "$TABLE_NAME_COLLECTION WHERE $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_NAME = ? AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_TYPE = ? AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ID = ? "
            CODE_ATTACHMENT_ITEM -> queryString += "$TABLE_NAME_ATTACHMENT WHERE $COLUMN_ATTACHMENT_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_ATTACHMENT.$COLUMN_ATTACHMENT_ID = ? "
            CODE_ALARM_ITEM -> queryString += "$TABLE_NAME_ALARM WHERE $COLUMN_ALARM_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_ALARM.$COLUMN_ALARM_ID = ? "
            CODE_UNKNOWN_ITEM -> queryString += "$TABLE_NAME_UNKNOWN WHERE $COLUMN_UNKNOWN_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_UNKNOWN.$COLUMN_UNKNOWN_ID = ? "

            else -> throw java.lang.IllegalArgumentException("Unknown URI: $uri")
        }

        if (selection != null) {
            queryString += "AND ($selection)"
            selectionArgs?.forEach { args.add(it) }          // add all selection args to the args array, no further validation needed here
        }

        // this block updates the count variable. The raw query doesn't return the count of the deleted rows, so we determine it before
        val countQueryString = queryString.replace("DELETE FROM ", "SELECT count(*) FROM ")
        val countQuery = SimpleSQLiteQuery(countQueryString, args.toArray())
        count = database.deleteRAW(countQuery)

        val deleteQuery = SimpleSQLiteQuery(queryString, args.toArray())
        Log.println(Log.INFO, "SyncContentProvider", "Delete Query prepared: $queryString")
        Log.println(Log.INFO, "SyncContentProvider", "Delete Query args prepared: ${args.joinToString(separator = ", ")}")

        database.deleteRAW(deleteQuery)
        context!!.contentResolver.notifyChange(uri, null)

        Attachment.scheduleCleanupJob(context!!)    // cleanup possible old Attachments

        if(sUriMatcher.match(uri) == CODE_ICALOBJECTS_DIR || sUriMatcher.match(uri) == CODE_ICALOBJECT_ITEM || sUriMatcher.match(uri) == CODE_COLLECTION_ITEM || sUriMatcher.match(uri) == CODE_COLLECTION_DIR)
            database.removeOrphans()    // remove orpahns (recurring instances of a deleted original item)


        return count
    }

    override fun getType(uri: Uri): String? {
        TODO("Implement this to handle requests for the MIME type of the data" +
                "at the given URI")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {

        isSyncAdapter(uri)
        // TODO: Make sure that only the items within the collection of the given account are considered
        getAccountFromUri(uri)     // here this is used just for validation

        val id: Long?

        when (sUriMatcher.match(uri)) {
            CODE_ICALOBJECTS_DIR -> id = ICalObject.fromContentValues(values)?.let { database.insertICalObjectSync(it) }
            CODE_ATTENDEES_DIR -> id = Attendee.fromContentValues(values)?.let { database.insertAttendeeSync(it) }
            CODE_CATEGORIES_DIR -> id = Category.fromContentValues(values)?.let { database.insertCategorySync(it) }
            CODE_COMMENTS_DIR -> id = Comment.fromContentValues(values)?.let { database.insertCommentSync(it) }
            CODE_CONTACTS_DIR -> id = Contact.fromContentValues(values)?.let { database.insertContactSync(it) }
            CODE_ORGANIZER_DIR -> id = Organizer.fromContentValues(values)?.let { database.insertOrganizerSync(it) }
            CODE_RELATEDTO_DIR -> id = Relatedto.fromContentValues(values)?.let { database.insertRelatedtoSync(it) }
            CODE_RESOURCE_DIR -> id = Resource.fromContentValues(values)?.let { database.insertResourceSync(it) }
            CODE_COLLECTION_DIR -> id = ICalCollection.fromContentValues(values)?.let { database.insertCollectionSync(it) }
            CODE_ATTACHMENT_DIR -> id = Attachment.fromContentValues(values)?.let { database.insertAttachmentSync(it) }
            CODE_ALARM_DIR -> id = Alarm.fromContentValues(values)?.let { database.insertAlarmSync(it) }
            CODE_UNKNOWN_DIR -> id = Unknown.fromContentValues(values)?.let { database.insertUnknownSync(it) }

            CODE_ICALOBJECT_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_ATTENDEE_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_CATEGORY_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_COMMENT_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_CONTACT_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_ORGANIZER_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_RELATEDTO_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_RESOURCE_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_COLLECTION_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_ATTACHMENT_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_ALARM_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_UNKNOWN_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")

            else -> throw java.lang.IllegalArgumentException("Unknown URI: $uri")
        }

        if (context == null)
            return null

        if(id == null)
            return null

        context!!.contentResolver.notifyChange(uri, null)
        Log.println(Log.INFO, "newContentUri", ContentUris.withAppendedId(uri, id).toString())

        if(sUriMatcher.match(uri) == CODE_ATTACHMENT_DIR)
            storeBinaryAttachmentInDir(id)

        if(sUriMatcher.match(uri) == CODE_ICALOBJECTS_DIR && (values?.containsKey(COLUMN_RRULE) == true || values?.containsKey(COLUMN_RDATE) == true || values?.containsKey(COLUMN_EXDATE) == true))
            rebuildRecurring(id)

        return ContentUris.withAppendedId(uri, id)
    }

    override fun onCreate(): Boolean {

        if(context?.applicationContext == null)
            return false

        database = ICalDatabase.getInstance(context!!.applicationContext).iCalDatabaseDao
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {

        if (context == null)
            return null

        isSyncAdapter(uri)
        val account = getAccountFromUri(uri)
        val args = arrayListOf(account.name, account.type)
        if (uri.pathSegments.size >= 2)
            args.add(uri.pathSegments[1].toLong().toString())      // add first argument (must be Long! String is expected, toLong would make other values null

        val subquery = "SELECT $TABLE_NAME_ICALOBJECT.$COLUMN_ID " +
                "FROM $TABLE_NAME_ICALOBJECT " +
                "INNER JOIN $TABLE_NAME_COLLECTION ON $TABLE_NAME_ICALOBJECT.$COLUMN_ICALOBJECT_COLLECTIONID = $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ID " +
                "AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_NAME = ? " +
                "AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_TYPE = ? " +
                "AND $TABLE_NAME_ICALOBJECT.$COLUMN_RECUR_ISLINKEDINSTANCE = 0"

        var queryString = "SELECT "
        queryString += if (projection.isNullOrEmpty())
            "*"
        else
            projection.joinToString(separator = ", ")

        queryString += " FROM "

        when (sUriMatcher.match(uri)) {
            CODE_ICALOBJECTS_DIR -> queryString += "$TABLE_NAME_ICALOBJECT WHERE $COLUMN_ID IN ($subquery) "
            CODE_ATTENDEES_DIR -> queryString += "$TABLE_NAME_ATTENDEE WHERE $COLUMN_ATTENDEE_ICALOBJECT_ID IN ($subquery) "
            CODE_CATEGORIES_DIR -> queryString += "$TABLE_NAME_CATEGORY WHERE $COLUMN_CATEGORY_ICALOBJECT_ID IN ($subquery) "
            CODE_COMMENTS_DIR -> queryString += "$TABLE_NAME_COMMENT WHERE $COLUMN_COMMENT_ICALOBJECT_ID IN ($subquery) "
            CODE_CONTACTS_DIR -> queryString += "$TABLE_NAME_CONTACT WHERE $COLUMN_CONTACT_ICALOBJECT_ID IN ($subquery) "
            CODE_ORGANIZER_DIR -> queryString += "$TABLE_NAME_ORGANIZER WHERE $COLUMN_ORGANIZER_ICALOBJECT_ID IN ($subquery) "
            CODE_RELATEDTO_DIR -> queryString += "$TABLE_NAME_RELATEDTO WHERE $COLUMN_RELATEDTO_ICALOBJECT_ID IN ($subquery) "
            CODE_RESOURCE_DIR -> queryString += "$TABLE_NAME_RESOURCE WHERE $COLUMN_RESOURCE_ICALOBJECT_ID IN ($subquery) "
            CODE_ATTACHMENT_DIR -> queryString += "$TABLE_NAME_ATTACHMENT WHERE $COLUMN_ATTACHMENT_ICALOBJECT_ID IN ($subquery) "
            CODE_ALARM_DIR -> queryString += "$TABLE_NAME_ALARM WHERE $COLUMN_ALARM_ICALOBJECT_ID IN ($subquery) "
            CODE_UNKNOWN_DIR -> queryString += "$TABLE_NAME_UNKNOWN WHERE $COLUMN_UNKNOWN_ICALOBJECT_ID IN ($subquery) "
            CODE_COLLECTION_DIR -> queryString += "$TABLE_NAME_COLLECTION WHERE $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_NAME = ? AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_TYPE = ? "

            CODE_ICALOBJECT_ITEM -> queryString += "$TABLE_NAME_ICALOBJECT WHERE $COLUMN_ID IN ($subquery) AND $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ? "
            CODE_ATTENDEE_ITEM -> queryString += "$TABLE_NAME_ATTENDEE WHERE $COLUMN_ATTENDEE_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_ATTENDEE.$COLUMN_ATTENDEE_ID = ? "
            CODE_CATEGORY_ITEM -> queryString += "$TABLE_NAME_CATEGORY WHERE $COLUMN_CATEGORY_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ID = ? "
            CODE_COMMENT_ITEM -> queryString += "$TABLE_NAME_COMMENT WHERE $COLUMN_COMMENT_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_COMMENT.$COLUMN_COMMENT_ID = ? "
            CODE_CONTACT_ITEM -> queryString += "$TABLE_NAME_CONTACT WHERE $COLUMN_CONTACT_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_CONTACT.$COLUMN_CONTACT_ID = ? "
            CODE_ORGANIZER_ITEM -> queryString += "$TABLE_NAME_ORGANIZER WHERE $COLUMN_ORGANIZER_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_ORGANIZER.$COLUMN_ORGANIZER_ID = ? "
            CODE_RELATEDTO_ITEM -> queryString += "$TABLE_NAME_RELATEDTO WHERE $COLUMN_RELATEDTO_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_RELATEDTO.$COLUMN_RELATEDTO_ID = ? "
            CODE_RESOURCE_ITEM -> queryString += "$TABLE_NAME_RESOURCE WHERE $COLUMN_RESOURCE_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_RESOURCE.$COLUMN_RESOURCE_ID = ? "
            CODE_ATTACHMENT_ITEM -> queryString += "$TABLE_NAME_ATTACHMENT WHERE $COLUMN_ATTACHMENT_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_ATTACHMENT.$COLUMN_ATTACHMENT_ID = ? "
            CODE_ALARM_ITEM -> queryString += "$TABLE_NAME_ALARM WHERE $COLUMN_ALARM_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_ALARM.$COLUMN_ALARM_ID = ? "
            CODE_UNKNOWN_ITEM -> queryString += "$TABLE_NAME_UNKNOWN WHERE $COLUMN_UNKNOWN_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_UNKNOWN.$COLUMN_UNKNOWN_ID = ? "
            CODE_COLLECTION_ITEM -> queryString += "$TABLE_NAME_COLLECTION WHERE $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_NAME = ? AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_TYPE = ? AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ID = ? "

            else -> throw java.lang.IllegalArgumentException("Unknown URI: $uri")
        }

        if (selection != null)
            queryString += " AND ($selection)"

        selectionArgs?.forEach { args.add(it) }          // add all selection args to the args array, no further validation needed here

        if (!sortOrder.isNullOrBlank())
            queryString += " ORDER BY $sortOrder"

        val query = SimpleSQLiteQuery(queryString, args.toArray())

        Log.println(Log.INFO, "SyncContentProvider", "Query prepared: $queryString")
        Log.println(Log.INFO, "SyncContentProvider", "Query args prepared: ${args.joinToString(separator = ", ")}")

        val result = database.getCursor(query)

        // if the request was for an Attachment, then allow the calling application to access the file by grantUriPermission
        if(sUriMatcher.match(uri) == CODE_ATTACHMENT_DIR || CODE_ATTACHMENT_DIR == CODE_ATTACHMENT_ITEM) {
            while(result?.moveToNext() == true) {
                val uriColumnIndex = result.getColumnIndex(COLUMN_ATTACHMENT_URI)
                val attachmentUriString = result.getString(uriColumnIndex)
                val attachmentUri = Uri.parse(attachmentUriString)

                //TODO: grantUriPermission could enable DAVx5 to access the files, further investigation is needed!
                context!!.grantUriPermission("at.bitfire.davdroid", attachmentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        return result
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {


        if (context == null)
            return 0

        if(values == null || values.size() == 0)
            throw java.lang.IllegalArgumentException("Cannot update without values.")

        isSyncAdapter(uri)
        val account = getAccountFromUri(uri)

        // construct UPDATE <tablename>
        var queryString = "UPDATE "
        when (sUriMatcher.match(uri)) {
            CODE_ICALOBJECTS_DIR -> queryString += "$TABLE_NAME_ICALOBJECT "
            CODE_ATTENDEES_DIR -> queryString += "$TABLE_NAME_ATTENDEE "
            CODE_CATEGORIES_DIR -> queryString += "$TABLE_NAME_CATEGORY "
            CODE_COMMENTS_DIR -> queryString += "$TABLE_NAME_COMMENT "
            CODE_CONTACTS_DIR -> queryString += "$TABLE_NAME_CONTACT "
            CODE_ORGANIZER_DIR -> queryString += "$TABLE_NAME_ORGANIZER "
            CODE_RELATEDTO_DIR -> queryString += "$TABLE_NAME_RELATEDTO "
            CODE_RESOURCE_DIR -> queryString += "$TABLE_NAME_RESOURCE "
            CODE_COLLECTION_DIR -> queryString += "$TABLE_NAME_COLLECTION "
            CODE_ATTACHMENT_DIR -> queryString += "$TABLE_NAME_ATTACHMENT "
            CODE_ALARM_DIR -> queryString += "$TABLE_NAME_ALARM "
            CODE_UNKNOWN_DIR -> queryString += "$TABLE_NAME_UNKNOWN "


            CODE_ICALOBJECT_ITEM -> queryString += "$TABLE_NAME_ICALOBJECT "
            CODE_ATTENDEE_ITEM -> queryString += "$TABLE_NAME_ATTENDEE "
            CODE_CATEGORY_ITEM -> queryString += "$TABLE_NAME_CATEGORY "
            CODE_COMMENT_ITEM -> queryString += "$TABLE_NAME_COMMENT "
            CODE_CONTACT_ITEM -> queryString += "$TABLE_NAME_CONTACT "
            CODE_ORGANIZER_ITEM -> queryString += "$TABLE_NAME_ORGANIZER "
            CODE_RELATEDTO_ITEM -> queryString += "$TABLE_NAME_RELATEDTO "
            CODE_RESOURCE_ITEM -> queryString += "$TABLE_NAME_RESOURCE "
            CODE_COLLECTION_ITEM -> queryString += "$TABLE_NAME_COLLECTION "
            CODE_ATTACHMENT_ITEM -> queryString += "$TABLE_NAME_ATTACHMENT "
            CODE_ALARM_ITEM -> queryString += "$TABLE_NAME_ALARM "
            CODE_UNKNOWN_ITEM -> queryString += "$TABLE_NAME_UNKNOWN "


            else -> throw java.lang.IllegalArgumentException("Unknown URI: $uri")
        }

        //construct SET <key> = <value>
        queryString += "SET "
        val args: ArrayList<Any> = arrayListOf()
        values.valueSet().forEach {
            queryString += "${it.key} = ?, "
            args.add(it.value)
        }
        queryString = queryString.removeSuffix(", ")

        args.add(account.name)
        args.add(account.type)
        if (uri.pathSegments.size >= 2)
            args.add(uri.pathSegments[1].toLong().toString())      // add first argument (must be Long! String is expected, toLong would make other values null

        //construct WHERE icalobjectId in <subquery> and further conditions
        queryString += " WHERE "
        val subquery = "SELECT $TABLE_NAME_ICALOBJECT.$COLUMN_ID " +
                "FROM $TABLE_NAME_ICALOBJECT " +
                "INNER JOIN $TABLE_NAME_COLLECTION ON $TABLE_NAME_ICALOBJECT.$COLUMN_ICALOBJECT_COLLECTIONID = $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ID " +
                "AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_NAME = ? " +
                "AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_TYPE = ? " +
                "AND $TABLE_NAME_ICALOBJECT.$COLUMN_RECUR_ISLINKEDINSTANCE = 0"


        when (sUriMatcher.match(uri)) {
            CODE_ICALOBJECTS_DIR -> queryString += "$COLUMN_ID IN ($subquery) "
            CODE_ATTENDEES_DIR -> queryString += "$COLUMN_ATTENDEE_ICALOBJECT_ID IN ($subquery) "
            CODE_CATEGORIES_DIR -> queryString += "$COLUMN_CATEGORY_ICALOBJECT_ID IN ($subquery) "
            CODE_COMMENTS_DIR -> queryString += "$COLUMN_COMMENT_ICALOBJECT_ID IN ($subquery) "
            CODE_CONTACTS_DIR -> queryString += "$COLUMN_CONTACT_ICALOBJECT_ID IN ($subquery) "
            CODE_ORGANIZER_DIR -> queryString += "$COLUMN_ORGANIZER_ICALOBJECT_ID IN ($subquery) "
            CODE_RELATEDTO_DIR -> queryString += "$COLUMN_RELATEDTO_ICALOBJECT_ID IN ($subquery) "
            CODE_RESOURCE_DIR -> queryString += "$COLUMN_RESOURCE_ICALOBJECT_ID IN ($subquery) "
            CODE_COLLECTION_DIR -> queryString += "$TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_NAME = ? AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_TYPE = ? "
            CODE_ATTACHMENT_DIR -> queryString += "$COLUMN_ATTACHMENT_ICALOBJECT_ID IN ($subquery) "
            CODE_ALARM_DIR -> queryString += "$COLUMN_ALARM_ICALOBJECT_ID IN ($subquery) "
            CODE_UNKNOWN_DIR -> queryString += "$COLUMN_UNKNOWN_ICALOBJECT_ID IN ($subquery) "


            CODE_ICALOBJECT_ITEM -> queryString += "$COLUMN_ID IN ($subquery) AND $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ? "
            CODE_ATTENDEE_ITEM -> queryString += "$COLUMN_ATTENDEE_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_ATTENDEE.$COLUMN_ATTENDEE_ID = ? "
            CODE_CATEGORY_ITEM -> queryString += "$COLUMN_CATEGORY_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ID = ? "
            CODE_COMMENT_ITEM -> queryString += "$COLUMN_COMMENT_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_COMMENT.$COLUMN_COMMENT_ID = ? "
            CODE_CONTACT_ITEM -> queryString += "$COLUMN_CONTACT_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_CONTACT.$COLUMN_CONTACT_ID = ? "
            CODE_ORGANIZER_ITEM -> queryString += "$COLUMN_ORGANIZER_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_ORGANIZER.$COLUMN_ORGANIZER_ID = ? "
            CODE_RELATEDTO_ITEM -> queryString += "$COLUMN_RELATEDTO_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_RELATEDTO.$COLUMN_RELATEDTO_ID = ? "
            CODE_RESOURCE_ITEM -> queryString += "$COLUMN_RESOURCE_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_RESOURCE.$COLUMN_RESOURCE_ID = ? "
            CODE_COLLECTION_ITEM -> queryString += "$TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_NAME = ? AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_TYPE = ? AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ID = ? "
            CODE_ATTACHMENT_ITEM -> queryString += "$COLUMN_ATTACHMENT_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_ATTACHMENT.$COLUMN_ATTACHMENT_ID = ? "
            CODE_ALARM_ITEM -> queryString += "$COLUMN_ALARM_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_ALARM.$COLUMN_ALARM_ID = ? "
            CODE_UNKNOWN_ITEM -> queryString += "$COLUMN_UNKNOWN_ICALOBJECT_ID IN ($subquery) AND $TABLE_NAME_UNKNOWN.$COLUMN_UNKNOWN_ID = ? "

            else -> throw java.lang.IllegalArgumentException("Unknown URI: $uri")
        }

        if (selection != null) {
            queryString += "AND ($selection)"
            selectionArgs?.forEach { args.add(it) }          // add all selection args to the args array, no further validation needed here
        }

        Log.println(Log.INFO, "SyncContentProvider", "Update-Query prepared: $queryString")
        Log.println(Log.INFO, "SyncContentProvider", "Update-Query args prepared: ${args.joinToString(separator = ", ")}")

        val updateQuery = SimpleSQLiteQuery(queryString, args.toArray())

        // TODO: find a solution to efficiently return the actual count of updated rows (the return value of the RAW-query doesn't work)
        //val count = database.updateRAW(updateQuery)
        database.updateRAW(updateQuery)
        context!!.contentResolver.notifyChange(uri, null)

        // updates on recurring instances through bulk updates should not occur, only updates on single items will update the recurring instances
        if(sUriMatcher.match(uri) == CODE_ICALOBJECT_ITEM && (values.containsKey(COLUMN_RRULE) == true || values.containsKey(COLUMN_RDATE) == true || values.containsKey(COLUMN_EXDATE) == true))
        {
            try {
                val id: Long = uri.pathSegments[1].toLong()
                rebuildRecurring(id)
            } catch (e: NumberFormatException) {
                Log.d("pathSegments[1]", "Could not convert path segment to Long. \n$e")

            }
        }

        return 1
    }


    private fun isSyncAdapter(uri: Uri): Boolean {

        val isSyncAdapter = uri.getBooleanQueryParameter(CALLER_IS_SYNCADAPTER, false)
        if(isSyncAdapter)
            return true
        else
            throw java.lang.IllegalArgumentException("Currently only Syncadapters are supported. Uri: ($uri)")
    }

    private fun getAccountFromUri(uri: Uri): Account {
        val accountName = uri.getQueryParameter(ACCOUNT_NAME) ?: throw java.lang.IllegalArgumentException("Query parameter $ACCOUNT_NAME missing. Uri: ($uri)")
        if (accountName == "LOCAL")
            throw java.lang.IllegalArgumentException("Local collections cannot be used. Uri: ($uri)")

        val accountType = uri.getQueryParameter(ACCOUNT_TYPE) ?: throw java.lang.IllegalArgumentException("Query parameter $ACCOUNT_TYPE missing. Uri: ($uri)")
        if (accountType == "LOCAL")
            throw java.lang.IllegalArgumentException("Local collections cannot be used. Uri: ($uri)")

        return Account(accountName, accountType)
    }

    private fun storeBinaryAttachmentInDir(id: Long) {

        val attachment = database.getAttachmentById(id)
        if(attachment?.binary == null)
            return

        val bytearray = Base64.decode(attachment.binary, Base64.DEFAULT)
        val fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(attachment.fmttype)


        try {
            val storageDir = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile("jtx_", ".$fileExtension", storageDir)
            file.writeBytes(bytearray)
            //Log.d("externalFilesPath", file.absolutePath)

            val attachmentUri = FileProvider.getUriForFile(context!!, AUTHORITY_FILEPROVIDER, file)
            val contentResolver = context!!.contentResolver

            if(attachment.fmttype == null)
                attachment.fmttype = contentResolver.getType(attachmentUri)

            attachment.binary = null
            attachment.uri = attachmentUri.toString()
            attachment.extension = ".$fileExtension"
            attachment.filename = file.name
            attachment.filesize = file.length()

            database.updateAttachment(attachment)

        } catch (e: IOException) {
            Log.e("SyncContentProvider", "Failed to access storage\n$e")
        }
    }


    private fun rebuildRecurring(id: Long) {

        // rebuild recurring
        database.getRecurringToPopulate(id)?.recreateRecurring(database)

    }

}

