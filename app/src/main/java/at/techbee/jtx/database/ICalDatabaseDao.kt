/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database

import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import at.techbee.jtx.database.locals.COLUMN_STORED_LIST_SETTING_ID
import at.techbee.jtx.database.locals.COLUMN_STORED_LIST_SETTING_MODULE
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredListSetting
import at.techbee.jtx.database.locals.StoredResource
import at.techbee.jtx.database.locals.TABLE_NAME_EXTENDED_STATUS
import at.techbee.jtx.database.locals.TABLE_NAME_STORED_CATEGORIES
import at.techbee.jtx.database.locals.TABLE_NAME_STORED_LIST_SETTINGS
import at.techbee.jtx.database.locals.TABLE_NAME_STORED_RESOURCES
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.AlarmRelativeTo
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Attendee
import at.techbee.jtx.database.properties.COLUMN_ALARM_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_ALARM_TRIGGER_TIME
import at.techbee.jtx.database.properties.COLUMN_ATTACHMENT_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_ATTACHMENT_ID
import at.techbee.jtx.database.properties.COLUMN_ATTACHMENT_URI
import at.techbee.jtx.database.properties.COLUMN_ATTENDEE_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_CATEGORY_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_CATEGORY_TEXT
import at.techbee.jtx.database.properties.COLUMN_COMMENT_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_RELATEDTO_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_RELATEDTO_RELTYPE
import at.techbee.jtx.database.properties.COLUMN_RELATEDTO_TEXT
import at.techbee.jtx.database.properties.COLUMN_RESOURCE_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_RESOURCE_TEXT
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.properties.Comment
import at.techbee.jtx.database.properties.Organizer
import at.techbee.jtx.database.properties.Relatedto
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.database.properties.TABLE_NAME_ALARM
import at.techbee.jtx.database.properties.TABLE_NAME_ATTACHMENT
import at.techbee.jtx.database.properties.TABLE_NAME_ATTENDEE
import at.techbee.jtx.database.properties.TABLE_NAME_CATEGORY
import at.techbee.jtx.database.properties.TABLE_NAME_COMMENT
import at.techbee.jtx.database.properties.TABLE_NAME_RELATEDTO
import at.techbee.jtx.database.properties.TABLE_NAME_RESOURCE
import at.techbee.jtx.database.properties.Unknown
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.database.views.VIEW_NAME_COLLECTIONS_VIEW
import at.techbee.jtx.database.views.VIEW_NAME_ICAL4LIST
import at.techbee.jtx.ui.detail.LocationLatLng
import at.techbee.jtx.ui.presets.XStatusStatusPair
import at.techbee.jtx.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration


@Dao
interface ICalDatabaseDao {

    /*
    SELECTs (global selects without parameter)
     */

    /**
     * Retrieve an list of all DISTINCT Category names ([Category.text]) as a LiveData-List
     * @return a list of [Category.text] as LiveData<List<String>>
     */
    @Query("SELECT DISTINCT $COLUMN_CATEGORY_TEXT FROM $TABLE_NAME_CATEGORY WHERE $COLUMN_CATEGORY_ICALOBJECT_ID IN (SELECT $COLUMN_ID FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_DELETED = 0) GROUP BY $COLUMN_CATEGORY_TEXT ORDER BY count(*) DESC, $COLUMN_CATEGORY_TEXT ASC")
    fun getAllCategoriesAsText(): LiveData<List<String>>

    /**
     * Retrieve an list of all DISTINCT Category names ([Category.text]) as a LiveData-List
     * @return a list of [Category.text] as LiveData<List<String>>
     */
    @Query("SELECT DISTINCT $COLUMN_RESOURCE_TEXT FROM $TABLE_NAME_RESOURCE WHERE $COLUMN_RESOURCE_ICALOBJECT_ID IN (SELECT $COLUMN_ID FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_DELETED = 0) GROUP BY $COLUMN_RESOURCE_TEXT ORDER BY count(*) DESC, $COLUMN_RESOURCE_TEXT ASC")
    fun getAllResourcesAsText(): LiveData<List<String>>

    /**
     * Retrieve an list of all DISTINCT Colors for ICalObjects as Int in a LiveData object
     */
    @Query("SELECT DISTINCT $COLUMN_COLOR FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_COLOR IS NOT NULL")
    fun getAllColors(): LiveData<List<Int>>



    /**
     * Retrieve an list of all Attachment Uris
     * @return a list of [Attachment.uri] as List<String>
     */
    @Query("SELECT $COLUMN_ATTACHMENT_URI FROM $TABLE_NAME_ATTACHMENT")
    suspend fun getAllAttachmentUris(): List<String>

    /**
     * Retrieve an Attachment with a specific [id]
     *
     * @return the [Attachment] with this [id]
     */
    @Query("SELECT * FROM $TABLE_NAME_ATTACHMENT WHERE $COLUMN_ATTACHMENT_ID = :id")
    fun getAttachmentById(id: Long): Attachment?

    /**
     * Retrieve an list of all Collections ([Collection]) as a LiveData-List
     *
     * @return a list of [Collection] as LiveData<List<ICalCollection>>
     */
    @Query("SELECT * FROM $TABLE_NAME_COLLECTION WHERE $COLUMN_COLLECTION_READONLY = 0 AND ($COLUMN_COLLECTION_SUPPORTSVJOURNAL = 1 OR $COLUMN_COLLECTION_SUPPORTSVTODO = 1) ORDER BY $COLUMN_COLLECTION_ACCOUNT_NAME ASC")
    fun getAllWriteableCollections(): LiveData<List<ICalCollection>>

    /**
     * Retrieve an list of all Collections ([Collection]) that have entries for a given module as a LiveData-List
     * @param module (Module.name) for which there are existing entries for a collection
     * @return a list of [Collection] as LiveData<List<ICalCollection>>
     */
    @Transaction
    @Query("SELECT $TABLE_NAME_COLLECTION.* FROM $TABLE_NAME_COLLECTION WHERE $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ID IN (SELECT $TABLE_NAME_ICALOBJECT.$COLUMN_ICALOBJECT_COLLECTIONID FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_MODULE = :module) ORDER BY $COLUMN_COLLECTION_ACCOUNT_NAME ASC")
    fun getAllCollections(module: String): LiveData<List<ICalCollection>>


    /**
     * Retrieve an list of all Collections ([CollectionsView]) as a LiveData-List
     *
     * @return a list of [CollectionsView] as LiveData<List<CollectionsView>>
     */
    @Query("SELECT * FROM $VIEW_NAME_COLLECTIONS_VIEW ORDER BY $COLUMN_COLLECTION_ACCOUNT_TYPE = 'LOCAL' DESC, $COLUMN_COLLECTION_ACCOUNT_NAME ASC")
    fun getAllCollectionsView(): LiveData<List<CollectionsView>>


    /**
     * Retrieve a list of ICalObjectIds that can be moved to a new collection
     * This process excludes child-entries that should be handled by the move-method
     */
    @Query("SELECT $COLUMN_ID FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_ICALOBJECT_COLLECTIONID = :collectionId AND $COLUMN_ID NOT IN (SELECT $COLUMN_RELATEDTO_ICALOBJECT_ID FROM $TABLE_NAME_RELATEDTO WHERE $COLUMN_RELATEDTO_RELTYPE = 'PARENT')")
    suspend fun getICalObjectIdsToMove(collectionId: Long): List<Long>


    /**
     * Retrieve an list of all remote collections ([ICalCollection])
     * @return a list of [ICalCollection] as LiveData
     */
    @Query("SELECT * FROM $TABLE_NAME_COLLECTION WHERE $COLUMN_COLLECTION_ACCOUNT_TYPE NOT IN (\'LOCAL\')")
    fun getAllRemoteCollectionsLive(): LiveData<List<ICalCollection>>

    /**
     * Retrieve an list of all remote collections ([ICalCollection])
     * @return a list of [ICalCollection]
     */
    @Query("SELECT * FROM $TABLE_NAME_COLLECTION WHERE $COLUMN_COLLECTION_ACCOUNT_TYPE NOT IN (\'LOCAL\')")
    fun getAllRemoteCollections(): List<ICalCollection>


    /**
     * Retrieve an list of all [ICal4List] their UIDs
     * @param uids of the entries
     * @return list of [ICal4List]
     */
    @Query("SELECT * FROM $VIEW_NAME_ICAL4LIST WHERE $COLUMN_UID IN (:uids)")
    fun getICal4ListByUIDs(uids: List<String?>): LiveData<List<ICal4List>>

    /**
     * Retrieves an [ICal4List]
     * @param iCalObjectId of the entry
     * @return [ICal4List]
     */
    @Query("SELECT * FROM $VIEW_NAME_ICAL4LIST WHERE $COLUMN_ID = :iCalObjectId")
    fun getICal4ListSync(iCalObjectId: Long): ICal4List?


    /**
     * Retrieve an list of all Relatedto ([Relatedto]) as a List
     *
     * @return a list of [Relatedto] as List<Relatedto>
     */
    @Query("SELECT * FROM $TABLE_NAME_RELATEDTO")
    fun getAllRelatedtoSync(): List<Relatedto>

    /**
     * Retrieve the UID of a specific ICalObjectID
     * @param uid ot find
     * @return ICalObject of the UID
     */
    @Query("SELECT * FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_UID = :uid")
    fun getICalObjectFor(uid: String): ICalObject?


    /**
     * Retrieve an list of [ICalObject] that are child-elements of another [ICalObject]
     * by checking if the [ICalObject.id] is listed as a [Relatedto.linkedICalObjectId].
     *
     * @return a list of [ICalObject] as LiveData<List<[ICalObject]>>
     */
    @Query("SELECT * FROM $TABLE_NAME_ATTACHMENT")
    fun getAllAttachments(): LiveData<List<Attachment>>


    /**
     * Retrieve an list of all  [Attachment]
     * @return a list of [Attachment] as LiveData<List<[Attachment]>>
     */
    @Query("SELECT CASE WHEN (EXISTS (SELECT * FROM $TABLE_NAME_RELATEDTO WHERE $COLUMN_RELATEDTO_ICALOBJECT_ID = :id AND $COLUMN_RELATEDTO_RELTYPE = 'PARENT')) THEN 1 ELSE 0 END")
    fun isChild(id: Long): LiveData<Boolean>


    /**
     * Retrieve the number of items in the table of [ICalObject] as Int.
     * Currently only used for Tests.
     *
     * @return Int with the total number of [ICalObject] in the table.
     */
    @Query("SELECT count(*) FROM $TABLE_NAME_ICALOBJECT")
    fun getCount(): Int

    /**
     * Retrieve the number of items in the table of [ICal4List] for a specific module as Int.
     * @param
     * @return Int with the total number of [ICal4List] in the table for the given module.
     */
    @Query("SELECT count(*) FROM $VIEW_NAME_ICAL4LIST WHERE $COLUMN_MODULE = :module AND $VIEW_NAME_ICAL4LIST.isChildOfTodo = 0 AND $VIEW_NAME_ICAL4LIST.isChildOfJournal = 0 AND $VIEW_NAME_ICAL4LIST.isChildOfNote = 0 ")
    fun getICal4ListCount(module: String): LiveData<Int?>

    /**
     * Retrieve an [ICalObject] by Id as LiveData
     *
     * @param id The id of the [ICalObject] in the DB
     * @return the [ICalObject] with the passed id or null if not found
     */
    @Query("SELECT * FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_ID = :id")
    fun getICalObject(id: Long): LiveData<ICalObject?>

    @Query("SELECT * FROM $TABLE_NAME_RELATEDTO WHERE $COLUMN_RELATEDTO_ICALOBJECT_ID = :iCalObjectId")
    fun getRelatedTo(iCalObjectId: Long): LiveData<List<Relatedto>>

    @Query("SELECT * FROM $TABLE_NAME_COLLECTION WHERE $COLUMN_COLLECTION_ID = :collectionId")
    fun getCollection(collectionId: Long): LiveData<ICalCollection>


    /**
     * Retrieve an [ICalObject] by Id asynchronously (suspend)
     *
     * @param id The id of the [ICalObject] in the DB
     * @return the [ICalObject] with the passed id or null if not found
     */
    @Query("SELECT * FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_ID = :id")
    suspend fun getICalObjectById(id: Long): ICalObject?

    /**
     * Retrieve an [ICalObject] by Id synchronously (suspend)
     *
     * @param id The id of the [ICalObject] in the DB
     * @return the [ICalObject] with the passed id or null if not found
     */
    @Query("SELECT * FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_ID = :id")
    fun getICalObjectByIdSync(id: Long): ICalObject?

    /**
     * Resolve the UID with the corresonding ICalObjectId as LiveData
     * Attention: This only returns the series elemen that has no recurid!
     * @param uid of the [ICalObject] in the DB
     * @return the [ICalObject.id] as LiveData
     */
    @Query("SELECT * FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_UID = :uid AND $COLUMN_RECURID IS NULL")
    fun getSeriesICalObjectIdByUID(uid: String?): LiveData<ICalObject?>

    /**
     * Resolve the UID with the corresonding ICalObjectId as LiveData
     * Attention: This only returns the series elemen that has no recurid!
     * @param uid of the [ICalObject] instances in the DB
     * @return the [ICalObject.id] as LiveData List
     */
    @Query("SELECT * FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_UID = :uid AND $COLUMN_RECURID IS NOT NULL ORDER BY $COLUMN_RECURID")
    fun getSeriesInstancesICalObjectsByUID(uid: String?): LiveData<List<ICalObject>>

    /**
     * Retrieve all tasks that are done (Status = Completed or Percent = 100)
     * @return list of [ICalObject]
     */
    @Query("SELECT $TABLE_NAME_ICALOBJECT.$COLUMN_ID FROM $TABLE_NAME_ICALOBJECT INNER JOIN $TABLE_NAME_COLLECTION ON $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ID = $TABLE_NAME_ICALOBJECT.$COLUMN_ICALOBJECT_COLLECTIONID AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_READONLY = 0 WHERE $COLUMN_COMPONENT = 'VTODO' AND ($COLUMN_STATUS = 'COMPLETED' OR $COLUMN_PERCENT = 100)")
    fun getDoneTasks(): List<Long>


    /**
     * Retrieve an [ICalCollection] by Id synchronously (non-suspend)
     *
     * @param id The id of the [ICalCollection] in the DB
     * @return the [ICalCollection] with the passed id or null if not found
     */
    @Query("SELECT * FROM $TABLE_NAME_COLLECTION WHERE $COLUMN_COLLECTION_ID = :id")
    fun getCollectionByIdSync(id: Long): ICalCollection?


    @Query("SELECT * FROM $TABLE_NAME_CATEGORY WHERE $COLUMN_CATEGORY_ICALOBJECT_ID = :iCalObjectId")
    fun getCategoriesSync(iCalObjectId: Long): List<Category>

    @Query("SELECT * FROM $TABLE_NAME_COMMENT WHERE $COLUMN_COMMENT_ICALOBJECT_ID = :iCalObjectId")
    fun getCommentsSync(iCalObjectId: Long): List<Comment>

    @Query("SELECT * FROM $TABLE_NAME_ATTENDEE WHERE $COLUMN_ATTENDEE_ICALOBJECT_ID = :iCalObjectId")
    fun getAttendeesSync(iCalObjectId: Long): List<Attendee>

    @Query("SELECT * FROM $TABLE_NAME_RESOURCE WHERE $COLUMN_RESOURCE_ICALOBJECT_ID = :iCalObjectId")
    fun getResourcesSync(iCalObjectId: Long): List<Resource>

    @Query("SELECT * FROM $TABLE_NAME_ATTACHMENT WHERE $COLUMN_ATTACHMENT_ICALOBJECT_ID = :iCalObjectId")
    fun getAttachmentsSync(iCalObjectId: Long): List<Attachment>

    @Query("SELECT * FROM $TABLE_NAME_ALARM WHERE $COLUMN_ALARM_ICALOBJECT_ID = :iCalObjectId")
    fun getAlarmsSync(iCalObjectId: Long): List<Alarm>

    /**
     * Returns a list of (distinct) ICalObjects that have an active alarm
     * (an alarm, that was already triggered but not removed)
     * The list contains only elements that haven't been completed (status, percent)
     */
    @Query("SELECT DISTINCT $TABLE_NAME_ICALOBJECT.* FROM $TABLE_NAME_ICALOBJECT " +
            "WHERE $COLUMN_IS_ALARM_NOTIFICATION_ACTIVE = 1 " +
            "AND ($COLUMN_PERCENT IS NULL OR $COLUMN_PERCENT < 100) " +
            "AND ($COLUMN_STATUS IS NULL OR $COLUMN_STATUS != 'COMPLETED') ")
    fun getICalObjectsWithActiveAlarms(): List<ICalObject>

/*
iCalObject.percent != 100
                            && iCalObject.status != Status.COMPLETED.status
 */
    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET $COLUMN_IS_ALARM_NOTIFICATION_ACTIVE = :active WHERE $COLUMN_ID = :iCalObjectId")
    fun setAlarmNotification(iCalObjectId: Long, active: Boolean)

    /*
    INSERTs (Asyncronously / Suspend)
     */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertICalObject(iCalObject: ICalObject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendee(attendee: Attendee): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganizer(organizer: Organizer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: Resource): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelatedto(relatedto: Relatedto): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: Attachment): Long


    /*
    INSERTs (Synchronously)
     */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertICalObjectSync(iCalObject: ICalObject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAttendeeSync(attendee: Attendee): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategorySync(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCommentSync(comment: Comment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrganizerSync(organizer: Organizer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRelatedtoSync(relatedto: Relatedto): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertResourceSync(resource: Resource): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAttachmentSync(attachment: Attachment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlarmSync(alarm: Alarm): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUnknownSync(unknown: Unknown): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCollectionSync(iCalCollection: ICalCollection): Long


    /*
    DELETEs by Object
     */


    /**
     * Delete an iCalObject by the object.
     * @param icalObject The object of the icalObject that should be deleted.
     */
    @Delete
    fun delete(icalObject: ICalObject)

    /**
     * Delete an iCalObject by the object.
     * @param id The iCalObjects to be deleted.
     */
    @Query("DELETE FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_ID = :id")
    fun deleteICalObjectsbyId(id: Long)


    /**
     * Delete an ICalCollection by the object.
     * @param [collection] to be deleted.
     */
    @Delete
    fun deleteICalCollection(collection: ICalCollection)

    /**
     * Delete all collections of an account.
     * @param [accountName] and [accountType] of the Account to be deleted.
     */
    @Query("DELETE FROM $TABLE_NAME_COLLECTION WHERE $COLUMN_COLLECTION_ACCOUNT_NAME = :accountName AND $COLUMN_COLLECTION_ACCOUNT_TYPE = :accountType")
    fun deleteAccount(accountName: String, accountType: String)


    /**
     * Delete all categories with a specific icalobjectid.
     * @param [icalobjectId] of the icalObject that should be deleted.
     */
    @Query("DELETE FROM $TABLE_NAME_CATEGORY WHERE $COLUMN_CATEGORY_ICALOBJECT_ID = :icalobjectId")
    fun deleteCategories(icalobjectId: Long)

    /**
     * Delete all comment with a specific icalobjectid.
     * @param [icalobjectId] of the icalObject that should be deleted.
     */
    @Query("DELETE FROM $TABLE_NAME_COMMENT WHERE $COLUMN_COMMENT_ICALOBJECT_ID = :icalobjectId")
    fun deleteComments(icalobjectId: Long)


    /**
     * Delete all attachments with a specific icalobjectid.
     * @param [icalobjectId] of the icalObject that should be deleted.
     */
    @Query("DELETE FROM $TABLE_NAME_ATTACHMENT WHERE $COLUMN_ATTACHMENT_ICALOBJECT_ID = :icalobjectId")
    fun deleteAttachments(icalobjectId: Long)

    /**
     * Delete all attendees with a specific icalobjectid.
     * @param [icalobjectId] of the icalObject that should be deleted.
     */
    @Query("DELETE FROM $TABLE_NAME_ATTENDEE WHERE $COLUMN_ATTENDEE_ICALOBJECT_ID = :icalobjectId")
    fun deleteAttendees(icalobjectId: Long)

    /**
     * Delete all resources with a specific icalobjectid.
     * @param [icalobjectId] of the icalObject that should be deleted.
     */
    @Query("DELETE FROM $TABLE_NAME_RESOURCE WHERE $COLUMN_RESOURCE_ICALOBJECT_ID = :icalobjectId")
    fun deleteResources(icalobjectId: Long)

    /**
     * Delete all alarms with a specific icalobjectid.
     * @param [icalobjectId] of the icalObject that should be deleted.
     */
    @Query("DELETE FROM $TABLE_NAME_ALARM WHERE $COLUMN_ALARM_ICALOBJECT_ID = :icalobjectId")
    fun deleteAlarms(icalobjectId: Long)


    /**
     * Delete a relatedto by the object.
     *
     * @param rel The object of the relatedto that should be deleted.
     */
    @Delete
    fun deleteRelatedto(rel: Relatedto)

    /**
     * Deletes the relatedto for the given [iCalObjectId] and [parentUID] (for Reltype = PARENT)
     */
    @Query("DELETE FROM $TABLE_NAME_RELATEDTO WHERE $COLUMN_RELATEDTO_ICALOBJECT_ID = :iCalObjectId AND $COLUMN_RELATEDTO_TEXT = :parentUID AND $COLUMN_RELATEDTO_RELTYPE = 'PARENT'")
    fun deleteRelatedto(iCalObjectId: Long, parentUID: String)

    /**
     * Deletes all ICalObjects. ONLY FOR TESTING!
     */
    @Query("DELETE FROM $TABLE_NAME_ICALOBJECT")
    @VisibleForTesting
    fun deleteAllICalObjects()


    /**
     * Deletes the given categories in [categories] for the given iCalObjectIds in [iCalObjectIds]
     */
    @Query("DELETE FROM $TABLE_NAME_CATEGORY WHERE $COLUMN_CATEGORY_TEXT IN (:categories) AND $COLUMN_CATEGORY_ICALOBJECT_ID IN (:iCalObjectIds)")
    fun deleteCategoriesForICalObjects(categories: List<String>, iCalObjectIds: List<Long>)

    /**
     * Deletes the given categories in [resources] for the given iCalObjectIds in [iCalObjectIds]
     */
    @Query("DELETE FROM $TABLE_NAME_RESOURCE WHERE $COLUMN_RESOURCE_TEXT IN (:resources) AND $COLUMN_RESOURCE_ICALOBJECT_ID IN (:iCalObjectIds)")
    fun deleteResourcesForICalObjects(resources: List<String>, iCalObjectIds: List<Long>)


    /**
     * Exchanges a category with another
     */
    @Query("UPDATE $TABLE_NAME_CATEGORY SET $COLUMN_CATEGORY_TEXT = :newCategory WHERE $COLUMN_CATEGORY_ICALOBJECT_ID = :icalObjectId AND $COLUMN_CATEGORY_TEXT = :oldCategory")
    fun swapCategoriesUpdate(icalObjectId: Long, oldCategory: String, newCategory: String)


    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun update(icalObject: ICalObject)

    @Update
    fun updateAttachment(attachment: Attachment)

    @Update
    fun updateAlarm(alarm: Alarm)

    /**
     * Gets the next [Alarm]s after a certain date or after now
     * Elements that define a series are excluded.
     * Sorting is ascending by trigger time.
     * @param limit: The number of [Alarm]s that should be returned
     * @param minDate: The date from which the [Alarm]s should be fetched (default: System.currentTimeMillis())
     * @return a list of the next alarms
     */
    @Query(
        "SELECT $TABLE_NAME_ALARM.* " +
                "FROM $TABLE_NAME_ALARM " +
                "INNER JOIN $TABLE_NAME_ICALOBJECT ON $TABLE_NAME_ALARM.$COLUMN_ALARM_ICALOBJECT_ID = $TABLE_NAME_ICALOBJECT.$COLUMN_ID " +
                "WHERE $COLUMN_DELETED = 0 " +
                "AND $COLUMN_RRULE IS NULL " +
                "AND $COLUMN_ALARM_TRIGGER_TIME > :minDate " +
                "AND ($COLUMN_PERCENT IS NULL OR $COLUMN_PERCENT < 100) " +
                "AND ($COLUMN_STATUS IS NULL OR $COLUMN_STATUS != 'COMPLETED')" +
                "ORDER BY $COLUMN_ALARM_TRIGGER_TIME ASC LIMIT :limit"
    )
    fun getNextAlarms(limit: Int = 10, minDate: Long = System.currentTimeMillis()): List<Alarm>

    /**
     * Gets ICalObjects with lat/long and geofence radius
     * @param limit: The number of [ICalObject]s that should be returned
     * @return a list of ICalObjects
     */
    @Query(
        "SELECT $TABLE_NAME_ICALOBJECT.* " +
                "FROM $TABLE_NAME_ICALOBJECT " +
                "WHERE $COLUMN_DELETED = 0 " +
                "AND $COLUMN_RRULE IS NULL " +
                "AND $COLUMN_GEO_LAT IS NOT NULL " +
                "AND $COLUMN_GEO_LONG IS NOT NULL " +
                "AND $COLUMN_GEOFENCE_RADIUS IS NOT NULL " +
                "LIMIT :limit"
    )
    fun getICalObjectsWithGeofence(limit: Int): List<ICalObject>

    /**
     * Gets the next due [ICalObject]s after a certain date or after now.
     * Elements that define a series are excluded.
     * Sorting is ascending by trigger time.
     * @param limit: The number of [ICalObject]s that should be returned
     * @param minDate: The due date from which the [ICalObject]s should be fetched (default: System.currentTimeMillis())
     * @return a list of the next due icalobjects
     */
    @Query(
        "SELECT $TABLE_NAME_ICALOBJECT.* " +
                "FROM $TABLE_NAME_ICALOBJECT " +
                "WHERE $COLUMN_DELETED = 0 " +
                "AND $COLUMN_DUE > :minDate " +
                "AND $COLUMN_RRULE IS NULL " +
                "AND ($COLUMN_PERCENT IS NULL OR $COLUMN_PERCENT < 100) " +
                "AND ($COLUMN_STATUS IS NULL OR $COLUMN_STATUS != 'COMPLETED')" +
                "ORDER BY $COLUMN_DUE ASC LIMIT :limit"
    )
    fun getNextDueEntries(limit: Int, minDate: Long = System.currentTimeMillis()): List<ICalObject>

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateCollection(collection: ICalCollection)

    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET $COLUMN_DELETED = 1, $COLUMN_LAST_MODIFIED = :lastModified, $COLUMN_SEQUENCE = $COLUMN_SEQUENCE + 1, $COLUMN_DIRTY = 1 WHERE $COLUMN_ID in (:id)")
    suspend fun updateToDeleted(id: Long, lastModified: Long = System.currentTimeMillis())

    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET $COLUMN_LAST_MODIFIED = :lastModified, $COLUMN_SEQUENCE = $COLUMN_SEQUENCE + 1, $COLUMN_DIRTY = 1 WHERE $COLUMN_ID = :id")
    suspend fun updateSetDirty(id: Long, lastModified: Long = System.currentTimeMillis())

    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET $COLUMN_SUBTASKS_EXPANDED = :isSubtasksExpanded, $COLUMN_SUBNOTES_EXPANDED = :isSubnotesExpanded, $COLUMN_ATTACHMENTS_EXPANDED = :isAttachmentsExpanded, $COLUMN_PARENTS_EXPANDED = :isParentsExpanded WHERE $COLUMN_ID = :id")
    suspend fun updateExpanded(
        id: Long,
        isSubtasksExpanded: Boolean,
        isSubnotesExpanded: Boolean,
        isParentsExpanded: Boolean,
        isAttachmentsExpanded: Boolean
    )

    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET $COLUMN_SORT_INDEX = :index WHERE $COLUMN_ID = :id")
    suspend fun updateOrder(id: Long, index: Int?)


    @Transaction
    @Query("SELECT * FROM $VIEW_NAME_ICAL4LIST WHERE $COLUMN_UID in (SELECT $COLUMN_RELATEDTO_TEXT FROM $TABLE_NAME_RELATEDTO INNER JOIN $TABLE_NAME_ICALOBJECT ON $TABLE_NAME_ICALOBJECT.$COLUMN_ID = $TABLE_NAME_RELATEDTO.$COLUMN_RELATEDTO_ICALOBJECT_ID AND $COLUMN_RELATEDTO_RELTYPE = 'PARENT' AND $COLUMN_DELETED = 0)")
    fun getAllParents(): LiveData<List<ICal4ListRel>>

    /**
     * Updates/deletes entities through a RawQuery.
     * This is especially used for the Content Provider
     *
     * @param query The UPDATE statement.
     * @return A number of Entities that were updated.
     */
    @Transaction
    @RawQuery
    fun executeRAW(query: SupportSQLiteQuery): Int

    @Transaction
    @RawQuery(observedEntities = [ICal4List::class])
    fun getIcal4List(query: SupportSQLiteQuery): LiveData<List<ICal4List>>


    @Transaction
    @RawQuery(observedEntities = [ICal4List::class, Relatedto::class, Category::class, Resource::class])
    fun getIcal4ListRel(query: SupportSQLiteQuery): LiveData<List<ICal4ListRel>>

    @Transaction
    @RawQuery(observedEntities = [ICal4List::class, Relatedto::class, Category::class, Resource::class])
    suspend fun getIcal4ListRelSync(query: SupportSQLiteQuery): List<ICal4ListRel>

    @Transaction
    @RawQuery(observedEntities = [ICal4List::class, Relatedto::class, Category::class, Resource::class])
    fun getIcal4ListFlow(query: SupportSQLiteQuery): Flow<List<ICal4ListRel>>

    @Transaction
    @RawQuery(observedEntities = [ICal4List::class, Relatedto::class, Category::class, Resource::class])
    fun getSubEntries(query: SupportSQLiteQuery): LiveData<List<ICal4ListRel>>

    @Transaction
    @RawQuery(observedEntities = [ICal4List::class, Relatedto::class, Category::class, Resource::class])
    fun getSubEntriesSync(query: SupportSQLiteQuery): List<ICal4ListRel>

    @Transaction
    @RawQuery(observedEntities = [ICal4List::class, Relatedto::class, Category::class, Resource::class])
    fun getSubEntriesFlow(query: SupportSQLiteQuery): Flow<List<ICal4ListRel>>

    @Transaction
    @Query("SELECT * from icalobject WHERE _id = :key")
    fun get(key: Long): LiveData<ICalEntity?>


    @Transaction
    @Query("SELECT * from icalobject WHERE _id = :key")
    fun getSync(key: Long): ICalEntity?


    @Query("SELECT * from $TABLE_NAME_ALARM WHERE _id = :key")
    fun getAlarm(key: Long): LiveData<Alarm?>

    @Query("SELECT * from $TABLE_NAME_ALARM WHERE _id = :key")
    fun getAlarmSync(key: Long): Alarm?

    @Query("SELECT * from $TABLE_NAME_CATEGORY WHERE $COLUMN_CATEGORY_ICALOBJECT_ID = :iCalObjectId AND $COLUMN_CATEGORY_TEXT = :category")
    fun getCategoryForICalObjectByName(iCalObjectId: Long, category: String): Category?

    @Query("SELECT * from $TABLE_NAME_RESOURCE WHERE $COLUMN_RESOURCE_ICALOBJECT_ID = :iCalObjectId AND $COLUMN_RESOURCE_TEXT = :resource")
    fun getResourceForICalObjectByName(iCalObjectId: Long, resource: String): Resource?


    /** This query returns all ids of child elements of the given [parentKey]  */
    @Query("SELECT $TABLE_NAME_ICALOBJECT.* FROM $TABLE_NAME_ICALOBJECT WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID IN (SELECT rel.$COLUMN_RELATEDTO_ICALOBJECT_ID FROM $TABLE_NAME_RELATEDTO rel INNER JOIN $TABLE_NAME_ICALOBJECT ical ON rel.$COLUMN_RELATEDTO_TEXT = ical.$COLUMN_UID AND ical.$COLUMN_ID = :parentKey AND $COLUMN_RELATEDTO_RELTYPE = 'PARENT')")
    suspend fun getRelatedChildren(parentKey: Long): List<ICalObject>

    @Query("SELECT * from $TABLE_NAME_RELATEDTO WHERE $COLUMN_RELATEDTO_ICALOBJECT_ID = :icalobjectid AND $COLUMN_RELATEDTO_TEXT = :linkedUID AND $COLUMN_RELATEDTO_RELTYPE = :reltype")
    fun findRelatedTo(icalobjectid: Long, linkedUID: String, reltype: String): Relatedto?


    // This query returns all IcalObjects that have a specific ICalObjectId in the field for the OriginalIcalObjectId (ie. all generated items for a recurring entry)
    @Query("SELECT * from $TABLE_NAME_ICALOBJECT WHERE $COLUMN_UID = :uid AND $COLUMN_RECURID IS NULL")
    fun getRecurSeriesElement(uid: String): ICalObject?


    // This query returns all IcalObjects that have a specific ICalObjectId in the field for the OriginalIcalObjectId (ie. all generated items for a recurring entry)
    @Query("SELECT * from $TABLE_NAME_ICALOBJECT WHERE $COLUMN_UID = :uid AND $COLUMN_RECURID IS NOT NULL")
    fun getRecurInstances(uid: String): List<ICalObject>

    @Query("SELECT $COLUMN_EXDATE from $TABLE_NAME_ICALOBJECT WHERE $COLUMN_ID = :originalId")
    fun getRecurExceptions(originalId: Long): String?

    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET $COLUMN_EXDATE = :exceptions, $COLUMN_DIRTY = 1, $COLUMN_LAST_MODIFIED = :lastUpdated, $COLUMN_SEQUENCE = $COLUMN_SEQUENCE + 1 WHERE $COLUMN_ID = :originalId")
    fun setRecurExceptions(
        originalId: Long,
        exceptions: String?,
        lastUpdated: Long = System.currentTimeMillis()
    )

    /**
     * Recurring instances are synchronized through the series definition. This method updates the UID
     * of series elements to be assigned to the right series definition again after being moved
     * to a new collection.
     * @param oldUID of the entries (that should be updated)
     * @param newUID that should be set
     * @param newCollectionId that should be set
     */
    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET $COLUMN_UID = :newUID, $COLUMN_ICALOBJECT_COLLECTIONID = :newCollectionId WHERE $COLUMN_UID = :oldUID AND $COLUMN_RECURID IS NOT NULL")
    fun updateRecurringInstanceUIDs(oldUID: String?, newUID: String, newCollectionId: Long)

    @Query("DELETE FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_RRULE IS NULL AND $COLUMN_RECURID IS NOT NULL AND $COLUMN_UID NOT IN (SELECT $COLUMN_UID FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_RRULE IS NOT NULL)")
    fun removeOrphans()

    @Query("SELECT * FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_ID = :id")
    fun getRecurringToPopulate(id: Long): ICalObject?


    /*
    Queries for the content provider returning a Cursor
     */
    @RawQuery
    fun getCursor(query: SupportSQLiteQuery): Cursor

    @Query("DELETE FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_RECURID IS NOT NULL AND $COLUMN_UID = :uid AND $COLUMN_SEQUENCE = 0")
    fun deleteUnchangedRecurringInstances(uid: String?)


    @Query("DELETE FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_RECURID IS NOT NULL AND $COLUMN_UID = :uid")
    fun deleteRecurringInstances(uid: String?)

    @Query("SELECT * FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_UID = :uid AND $COLUMN_RECURID = :recurid")
    fun getRecurInstance(uid: String?, recurid: String): ICalObject?


    /**
     *  StoredListSetting
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStoredListSetting(storedListSetting: StoredListSetting): Long

    @Transaction
    @Query("SELECT * FROM $TABLE_NAME_STORED_LIST_SETTINGS WHERE $COLUMN_STORED_LIST_SETTING_MODULE IN (:modules) ORDER BY $COLUMN_STORED_LIST_SETTING_ID DESC")
    fun getStoredListSettings(modules: List<String>): LiveData<List<StoredListSetting>>

    @Delete
    fun deleteStoredListSetting(storedListSetting: StoredListSetting)

    /**
     * StoredCategory
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStoredCategory(storedCategory: StoredCategory)

    @Query("SELECT * FROM $TABLE_NAME_STORED_CATEGORIES")
    fun getStoredCategories(): LiveData<List<StoredCategory>>


    @Delete
    fun deleteStoredCategory(storedCategory: StoredCategory)


    /**
     * StoredResource
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStoredResource(storedResource: StoredResource)

    @Query("SELECT * FROM $TABLE_NAME_STORED_RESOURCES")
    fun getStoredResources(): LiveData<List<StoredResource>>

    @Delete
    fun deleteStoredResource(storedResource: StoredResource)


    /**
     * StoredStatus
     */

    @Transaction
    @Query("SELECT * FROM $TABLE_NAME_EXTENDED_STATUS")
    fun getStoredStatuses(): LiveData<List<ExtendedStatus>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStoredStatus(storedStatus: ExtendedStatus)

    @Delete
    fun deleteStoredStatus(storedStatus: ExtendedStatus)

    /**
     * Gets all XStatuses for a specific module
     * @param [module] for which the XStatuses should be retrieved
     * @return a pair of XSTATUS and mapped STATUS
     */
    @Query("SELECT DISTINCT $COLUMN_EXTENDED_STATUS, $COLUMN_STATUS FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_MODULE = :module AND $COLUMN_EXTENDED_STATUS IS NOT NULL")
    fun getAllXStatusesFor(module: String): LiveData<List<XStatusStatusPair>>

    /**
     * Gets all location als LocationLatLng object
     */
    @Query("SELECT DISTINCT $COLUMN_LOCATION, $COLUMN_GEO_LAT, $COLUMN_GEO_LONG FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_LOCATION IS NOT NULL ORDER BY $COLUMN_LAST_MODIFIED DESC")
    fun getAllLocationsLatLng(): LiveData<List<LocationLatLng>>

    @Transaction
    suspend fun deleteICalObjects(iCalObjectIds: List<Long>) {
        iCalObjectIds.forEach {
            deleteICalObjectWithChildren(it)
        }
    }

    suspend fun deleteICalObject(iCalObjectId: Long) = deleteICalObjects(listOf(iCalObjectId))

    /**
     * this function takes a parent [id], the function recursively calls itself and deletes all items and linked children (for local collections)
     * or updates the linked children and marks them as deleted.
     */
    private suspend fun deleteICalObjectWithChildren(id: Long, parentUID: String? = null) {

        if (id == 0L)
            return // do nothing, the item was never saved in DB

        // if the item could not be found, just return (this can happen on mass deletion from the list view, when a recur-instance was passed to delete, but it was already deleted through the original entry
        val item = getSync(id) ?: return
        val children = getRelatedChildren(id)
        children.forEach { child ->
            deleteICalObjectWithChildren(
                child.id,
                item.property.uid
            )    // call the function again to recursively delete all children, then delete the item
        }

        if (item.property.rrule != null)
            deleteRecurringInstances(item.property.uid)  // recurring instances are always physically deleted

        // if the entry has multiple parents, we only delete the reference, but not the entry itself
        if ((item.relatedto?.filter { it.reltype == Reltype.PARENT.name }?.size ?: 0) > 1) {
            item.relatedto?.find { it.text == parentUID && it.reltype == Reltype.PARENT.name }
                ?.let {
                    deleteRelatedto(it)
                    item.property.makeDirty()
                    update(item.property)
                    return
                }
        }

        when {
            item.property.recurid != null -> {
                unlinkFromSeries(item.property)   // if the current item
                deleteICalObjectsbyId(id)
            }
            item.ICalCollection?.accountType == ICalCollection.LOCAL_ACCOUNT_TYPE -> deleteICalObjectsbyId(
                item.property.id
            ) // Elements in local collection are physically deleted
            else -> updateToDeleted(item.property.id)
        }
    }


    suspend fun moveToCollection(iCalObjectId: Long, newCollectionId: Long): Long? {
        return moveToCollection(listOf(iCalObjectId), newCollectionId).firstOrNull()
    }

    @Transaction
    suspend fun moveToCollection(iCalObjectIds: List<Long>, newCollectionId: Long): List<Long> {
        val newEntries = mutableListOf<Long>()

        iCalObjectIds.forEach { iCalObjectId ->
                try {
                    val newId = updateCollectionWithChildren(
                        iCalObjectId,
                        null,
                        newCollectionId
                    ) ?: return@forEach
                    newEntries.add(newId)
                    // once the newId is there, the local entries can be deleted (or marked as deleted)
                    deleteICalObjectWithChildren(iCalObjectId)
                    val newICalObject = getICalObjectByIdSync(newId)
                    if (newICalObject?.rrule != null)
                        recreateRecurring(newICalObject)
                } catch (e: SQLiteConstraintException) {
                    Log.w("SQLConstraint", "Corrupted ID: $iCalObjectId")
                    Log.w("SQLConstraint", e.stackTraceToString())
                }
            }
        return newEntries
    }


    /**
     * @param [id] the id of the item for which the collection needs to be updated
     * @param [parentId] is needed for the recursive call in order to provide it for the movItemToNewCollection(...) function. For the initial call this would be null as the function should initially always be called from the top parent.
     *
     * this function takes care of
     * 1. moving the item to a new collection (by copying and deleting the current item)
     * 2. determining the children of this item and calling itself recusively to to the same again for each child.
     *
     * @return The new id of the item in the new collection
     */
    private suspend fun updateCollectionWithChildren(
        id: Long,
        parentId: Long?,
        newCollectionId: Long
    ): Long? {

        val newParentId = moveItemToNewCollection(id, parentId, newCollectionId)

        // then determine the children and recursively call the function again. The possible child becomes the new parent and is added to the list until there are no more children.
        val children = getRelatedChildren(id)
        children.forEach { child ->
            updateCollectionWithChildren(child.id, newParentId, newCollectionId)
        }
        return newParentId
    }


    /**
     * @param [id] is the id of the original item that should be moved to another collection. On the recursive call this is the id of the original child.
     * @param [newParentId] is the id of the parent that was already copied into the new collection. This is needed in order to re-create the relation between the parent and the child.
     *
     * This function creates a copy of an item with all it's children in the new collection and then
     * deletes (or marks as deleted) the original item.
     *
     * @return the new id of the item that was inserted (that becomes the newParentId)
     *
     */
    private suspend fun moveItemToNewCollection(
        id: Long,
        newParentId: Long?,
        newCollectionId: Long
    ): Long? {

        val item = getSync(id)
        val oldUID = item?.property?.uid
        val newUID = ICalObject.generateNewUID()
        if (item != null) {

            if (item.property.recurid != null)  // recur instances are ignored, changed recur instances are updated below
                return null

            item.property.id = 0L
            item.property.collectionId = newCollectionId
            item.property.sequence = 0
            item.property.dirty = true
            item.property.lastModified = System.currentTimeMillis()
            item.property.created = System.currentTimeMillis()
            item.property.dtstamp = System.currentTimeMillis()
            item.property.uid = newUID
            item.property.flags = null
            item.property.scheduleTag = null
            item.property.eTag = null
            item.property.fileName = null

            val newId = insertICalObject(item.property)

            item.attendees?.forEach {
                it.icalObjectId = newId
                insertAttendee(it)
            }

            item.resources?.forEach {
                it.icalObjectId = newId
                insertResource(it)
            }

            item.categories?.forEach {
                it.icalObjectId = newId
                insertCategory(it)
            }

            item.comments?.forEach {
                it.icalObjectId = newId
                insertComment(it)
            }

            if (item.organizer?.caladdress != null) {
                item.organizer?.icalObjectId = newId
                insertOrganizer(item.organizer!!)
            }

            item.attachments?.forEach {
                it.icalObjectId = newId
                insertAttachment(it)
            }

            item.alarms?.forEach {
                it.icalObjectId = newId
                insertAlarm(it)
            }

            // relations need to be rebuilt from the new child to the parent
            if (newParentId != null) {
                val parent = getSync(newParentId)
                val relParent2Child = Relatedto()
                relParent2Child.icalObjectId = newId
                relParent2Child.reltype = Reltype.PARENT.name
                relParent2Child.text = parent?.property?.uid
                insertRelatedto(relParent2Child)
            }

            updateRecurringInstanceUIDs(oldUID, newUID, newCollectionId)
            //NotificationPublisher.scheduleNextNotifications(context)  TODO: Check if covered in View Models
            return newId
        }
        return null
    }


    suspend fun findTopParent(iCalObjectId: Long): ICalObject? {

        val allRelatedTo = getAllRelatedtoSync()
        var topParent = getICalObjectById(iCalObjectId)

        while (allRelatedTo.any { it.icalObjectId == topParent?.id && it.reltype == Reltype.PARENT.name }) {
            if (allRelatedTo.filter { it.icalObjectId == topParent?.id && it.reltype == Reltype.PARENT.name }.size > 1) {
                Log.w("findTopParent", "Entry has multiple parents, cannot return single parent.")
                return null
            }

            val parentUID =
                allRelatedTo.find { it.icalObjectId == topParent?.id && it.reltype == Reltype.PARENT.name }?.text
            parentUID?.let { uid -> getICalObjectFor(uid)?.let { topParent = it } }

            //make sure no endless loop occurs in the error case that an entry links to itself
            if (allRelatedTo.any { it.icalObjectId == topParent?.id && it.text == topParent?.uid }) {
                Log.w("findTopParent", "Entry links to itself, cannot return parent.")
                return null
            }
        }
        return topParent
    }


    @Transaction
    suspend fun updateProgress(id: Long, newPercent: Int?, settingKeepStatusProgressCompletedInSync: Boolean, settingLinkProgressToSubtasks: Boolean, uid: String? = null) {
        val fetchedUID = uid ?: getUid(id) ?: return

            //val item = getICalObjectById(id) ?: return
        try {

            updateSingleProgress(id, fetchedUID, newPercent, settingKeepStatusProgressCompletedInSync)

            if(settingLinkProgressToSubtasks) {
                findTopParent(id)?.let {
                    updateProgressOfParents(it.id, it.uid, settingKeepStatusProgressCompletedInSync)
                }
            }
        } catch (e: SQLiteConstraintException) {
            Log.d("SQLConstraint", "Corrupted ID: $id")
            Log.d("SQLConstraint", e.stackTraceToString())
        }
    }

    private suspend fun updateSingleProgress(id: Long, uid: String, newPercent: Int?, settingKeepStatusProgressCompletedInSync: Boolean) {
        if(settingKeepStatusProgressCompletedInSync) {
            updateProgressKeepSync(
                id = id,
                progress = if (newPercent == 0) null else newPercent,
                status = when (newPercent) {
                    100 -> Status.COMPLETED.status
                    in 1..99 -> Status.IN_PROCESS.status
                    else -> Status.NEEDS_ACTION.status
                },
                completed = if (newPercent == 100) System.currentTimeMillis() else null
            )
        } else {
            updateProgressNotSync(
                id = id,
                progress = if (newPercent == 0) null else newPercent
            )
        }
        makeSeriesDirty(uid)
    }

    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET " +
            "$COLUMN_PERCENT = :progress, " +
            "$COLUMN_SEQUENCE = $COLUMN_SEQUENCE + 1, " +
            "$COLUMN_LAST_MODIFIED = :lastModified, " +
            "$COLUMN_DIRTY = 1 " +
            "WHERE $COLUMN_ID = :id")
    suspend fun updateProgressNotSync(id: Long, progress: Int?, lastModified: Long = System.currentTimeMillis())

    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET " +
            "$COLUMN_PERCENT = :progress, " +
            "$COLUMN_SEQUENCE = $COLUMN_SEQUENCE + 1, " +
            "$COLUMN_LAST_MODIFIED = :lastModified, " +
            "$COLUMN_DIRTY = 1, " +
            "$COLUMN_STATUS = :status, " +
            "$COLUMN_COMPLETED = :completed " +
            "WHERE $COLUMN_ID = :id")
    suspend fun updateProgressKeepSync(id: Long, progress: Int?, status: String?, completed: Long?, lastModified: Long = System.currentTimeMillis())



    private suspend fun updateProgressOfParents(
        parentId: Long,
        parentUid: String,
        keepInSync: Boolean
    ) {

        val children =
            getRelatedChildren(parentId).filter { it.module == Module.TODO.name }
        if (children.isNotEmpty()) {
            children.forEach { child ->
                updateProgressOfParents(child.id, child.uid, keepInSync)
            }
            val newProgress = children.map { it.percent ?: 0 }.average().toInt()
            updateSingleProgress(parentId, parentUid, newProgress, keepInSync)

            val parent = getICalObjectByIdSync(parentId)
            parent?.setUpdatedProgress(newProgress, keepInSync)
            parent?.let { update(it) }
        }
    }


    @Transaction
    suspend fun updateStatus(iCalObjectIds: List<Long>, newStatus: Status, newXStatus: ExtendedStatus?, settingKeepStatusProgressCompletedInSync: Boolean) {
        iCalObjectIds.forEach { iCalObjectId ->
            val currentItem = getICalObjectById(iCalObjectId) ?: return@forEach
            currentItem.status = newStatus.status
            currentItem.xstatus = newXStatus?.xstatus
            if(settingKeepStatusProgressCompletedInSync) {
                when(newStatus) {
                    Status.IN_PROCESS -> currentItem.setUpdatedProgress(if(currentItem.percent !in 1..99) 1 else currentItem.percent, true)
                    Status.COMPLETED -> currentItem.setUpdatedProgress(100, true)
                    else -> { }
                }
            }
            currentItem.makeDirty()
            update(currentItem)
            makeSeriesDirty(currentItem.uid)
        }
    }

    suspend fun updateStatus(iCalObjectId: Long, newStatus: Status, newXStatus: ExtendedStatus?, settingKeepStatusProgressCompletedInSync: Boolean) =
        updateStatus(listOf(iCalObjectId), newStatus, newXStatus, settingKeepStatusProgressCompletedInSync)


    @Transaction
    suspend fun swapCategories(iCalObjectId: Long, oldCategory: String, newCategory: String) {
        swapCategoriesUpdate(iCalObjectId, oldCategory, newCategory)
        val currentItem = getICalObjectById(iCalObjectId) ?: return
        currentItem.makeDirty()
        update(currentItem)
        makeSeriesDirty(currentItem.uid)
    }


    /**
     * updates the item and makes it dirty
     * this automatically takes the series definition
     * and ignores series instances
     */
    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET " +
            "$COLUMN_SEQUENCE = $COLUMN_SEQUENCE + 1, " +
            "$COLUMN_LAST_MODIFIED = :lastModified, " +
            "$COLUMN_DIRTY = 1 " +
            "WHERE $COLUMN_UID = :uid AND $COLUMN_RECURID IS NULL")
    suspend fun makeSeriesDirty(uid: String, lastModified: Long = System.currentTimeMillis())


    @Transaction
    fun recreateRecurring(iCalObject: ICalObject) {

        if (iCalObject.recurid?.isNotEmpty() == true) {
            getRecurSeriesElement(iCalObject.uid)?.let {
                recreateRecurring(it)
            }
            return
        }

        deleteUnchangedRecurringInstances(iCalObject.uid)
        // delete also exceptions (as recurring instances might still exist):
        val exceptions = DateTimeUtils.getLongListfromCSVString(iCalObject.exdate)
        exceptions.forEach { exceptionDate ->
            getRecurInstance(
                iCalObject.uid,
                ICalObject.getAsRecurId(exceptionDate, iCalObject.dtstartTimezone)
            )?.let {
                delete(it)
            }
        }

        if (iCalObject.dtstart == null || iCalObject.rrule.isNullOrEmpty())
            return

        val original = getSync(iCalObject.id) ?: return
        val timeToDue =
            if (original.property.component == Component.VTODO.name && original.property.due != null)
                original.property.due!! - original.property.dtstart!!
            else
                0L

        iCalObject.getInstancesFromRrule().forEach { recurrenceDate ->
            val instance = original.copy()

            instance.property.dtstart = recurrenceDate
            instance.property.recurid =
                ICalObject.getAsRecurId(recurrenceDate, instance.property.dtstartTimezone)
            instance.property.recuridTimezone = instance.property.dtstartTimezone

            if (getRecurInstance(
                    uid = iCalObject.uid,
                    recurid = instance.property.recurid!!
                ) != null
            )
                return@forEach   // skip the entry if there is an existing linked entry that was changed (and therefore not deleted before)

            instance.property.id = 0L
            //instance.property.uid = generateNewUID()
            instance.property.dtstamp = System.currentTimeMillis()
            instance.property.created = System.currentTimeMillis()
            instance.property.lastModified = System.currentTimeMillis()
            instance.property.rrule = null
            instance.property.rdate = null
            instance.property.exdate = null
            instance.property.sequence = 0
            instance.property.fileName = null
            instance.property.eTag = null
            instance.property.scheduleTag = null
            instance.property.dirty = false


            if (instance.property.component == Component.VTODO.name && original.property.due != null)
                instance.property.due = recurrenceDate + timeToDue

            val instanceId = insertICalObjectSync(instance.property)

            instance.categories?.forEach {
                it.categoryId = 0L
                it.icalObjectId = instanceId
                insertCategorySync(it)
            }
            instance.comments?.forEach {
                it.commentId = 0L
                it.icalObjectId = instanceId
                insertCommentSync(it)
            }
            instance.attachments?.forEach {
                it.attachmentId = 0L
                it.icalObjectId = instanceId
                insertAttachmentSync(it)
            }
            instance.organizer.apply {
                this?.organizerId = 0L
                this?.icalObjectId = instanceId
                this?.let { insertOrganizerSync(it) }
            }
            instance.attendees?.forEach {
                it.attendeeId = 0L
                it.icalObjectId = instanceId
                insertAttendeeSync(it)
            }
            instance.resources?.forEach {
                it.resourceId = 0L
                it.icalObjectId = instanceId
                insertResourceSync(it)
            }
            instance.relatedto?.forEach {
                it.relatedtoId = 0L
                it.icalObjectId = instanceId
                insertRelatedtoSync(it)
            }
            instance.alarms?.forEach {
                if (it.triggerRelativeDuration != null) {    // only relative alarms are considered
                    it.alarmId = 0L
                    it.icalObjectId = instanceId

                    try {
                        val dur = Duration.parse(it.triggerRelativeDuration!!)
                        if (it.triggerRelativeTo == AlarmRelativeTo.END.name) {
                            it.triggerTime = instance.property.due!! + dur.inWholeMilliseconds
                            it.triggerTimezone = instance.property.dueTimezone
                        } else {
                            it.triggerTime = instance.property.dtstart!! + dur.inWholeMilliseconds
                            it.triggerTimezone = instance.property.dtstartTimezone
                        }
                        insertAlarmSync(it)
                    } catch (e: IllegalArgumentException) {
                        Log.w(
                            "DurationParsing",
                            "Duration could not be parsed for instance, skipping this alarm."
                        )
                    }
                }
            }
        }
    }

    @Transaction
    suspend fun updateSortOrder(sortedList: List<Long>) {
        sortedList.forEachIndexed { index, iCalObjectId ->
                val iCalObject = getICalObjectById(iCalObjectId) ?: return@forEachIndexed
                iCalObject.sortIndex = index
                iCalObject.makeDirty()
                update(iCalObject)
            }
    }

    @Transaction
    suspend fun unlinkFromSeries(
        instances: List<ICalObject>,
        series: ICalObject?,
        deleteAfterUnlink: Boolean
    ) {

        instances.forEach { instance ->
            val children = getRelatedChildren(instance.id)
            val updatedEntry = unlinkFromSeries(instance)
            children.forEach forEachChild@{ child ->
                createCopyWithChildren(child.id, child.getModuleFromString(), updatedEntry.uid)
            }
        }


        if (deleteAfterUnlink)
            series?.id?.let { deleteICalObject(it) }
        else
            series?.let { makeSeriesDirty(it.uid) }
    }


    private suspend fun unlinkFromSeries(item: ICalObject): ICalObject {
        getRecurSeriesElement(item.uid)?.let { series ->
            val newExceptionList = DateTimeUtils.addLongToCSVString(
                getRecurExceptions(series.id),
                item.dtstart
            )
            setRecurExceptions(
                series.id,
                newExceptionList
            )
        }
        item.uid = ICalObject.generateNewUID()
        item.recurid = null
        item.makeDirty()
        update(item)
        return item
    }

    @Transaction
    suspend fun createCopy(
        iCalObjectIdToCopy: Long,
        module: Module,
        parentUID: String?
    ) = createCopyWithChildren(iCalObjectIdToCopy, module, parentUID)


    private suspend fun createCopyWithChildren(
        iCalObjectIdToCopy: Long,
        module: Module,
        parentUID: String?
    ): Long? {

        val icalEntityToCopy = getSync(iCalObjectIdToCopy) ?: return  null

        val newEntity = icalEntityToCopy.getIcalEntityCopy(module)
        try {
            val newId = insertICalObject(newEntity.property)
            newEntity.alarms?.forEach { alarm ->
                insertAlarm(
                    alarm.copy(
                        alarmId = 0L,
                        icalObjectId = newId
                    )
                )
            }
            newEntity.attachments?.forEach { attachment ->
                insertAttachment(
                    attachment.copy(
                        icalObjectId = newId,
                        attachmentId = 0L
                    )
                )
            }
            newEntity.attendees?.forEach { attendee ->
                insertAttendee(
                    attendee.copy(
                        icalObjectId = newId,
                        attendeeId = 0L
                    )
                )
            }
            newEntity.categories?.forEach { category ->
                insertCategory(
                    category.copy(
                        icalObjectId = newId,
                        categoryId = 0L
                    )
                )
            }
            newEntity.comments?.forEach { comment ->
                insertComment(
                    comment.copy(
                        icalObjectId = newId,
                        commentId = 0L
                    )
                )
            }
            newEntity.resources?.forEach { resource ->
                insertResource(
                    resource.copy(
                        icalObjectId = newId,
                        resourceId = 0L
                    )
                )
            }
            newEntity.unknown?.forEach { unknown ->
                insertUnknownSync(
                    unknown.copy(
                        icalObjectId = newId,
                        unknownId = 0L
                    )
                )
            }
            newEntity.organizer?.let { organizer ->
                insertOrganizer(
                    organizer.copy(
                        icalObjectId = newId,
                        organizerId = 0L
                    )
                )
            }

            newEntity.relatedto?.forEach { relatedto ->
                if (relatedto.reltype == Reltype.PARENT.name && parentUID != null) {
                    insertRelatedto(
                        relatedto.copy(
                            relatedtoId = 0L,
                            icalObjectId = newId,
                            text = parentUID
                        )
                    )
                }
            }

            val children = getRelatedChildren(icalEntityToCopy.property.id)
            children.forEach { child ->
                createCopyWithChildren(
                        iCalObjectIdToCopy = child.id,
                        module = child.getModuleFromString(),
                        parentUID = newEntity.property.uid
                    )

            }

            return if (parentUID == null)   // we navigate only to the parent (not to the children that are invoked recursively)
                newId
            else
                null
        } catch (e: SQLiteConstraintException) {
            Log.d("SQLConstraint", e.stackTraceToString())
            return null
        }
    }


    /**
     * Adds new related to from all given parentIds to all given childrenIds
     */

    private suspend fun linkEntries(parentIds: List<Long>, childrenIds: List<Long>) {

        parentIds.forEach forEachParent@ { parentId ->

            val parent = getICalObjectById(parentId) ?: return@forEachParent

            childrenIds.forEach forEachChild@ { childId ->

                val child = getICalObjectById(childId) ?: return@forEachChild

                if (child.uid == parent.uid)
                    return@forEachChild

                val existing = findRelatedTo(
                    child.id,
                    parent.uid,
                    Reltype.PARENT.name
                ) != null

                if (existing)
                    return@forEachChild
                else {
                    insertRelatedto(
                        Relatedto(
                            icalObjectId = child.id,
                            text = parent.uid,
                            reltype = Reltype.PARENT.name
                        )
                    )
                    child.makeDirty()
                    update(child)
                }
            }
        }
    }

    @Transaction
    suspend fun linkChildren(parentId: Long, childrenIds: List<Long>) = linkEntries(listOf(parentId), childrenIds)
    @Transaction
    suspend fun linkParents(childId: Long, parentIds: List<Long>) = linkEntries(parentIds, listOf(childId))



    suspend fun addSubEntry(parentUID: String, subEntry: ICalObject, attachment: Attachment?):Boolean {
        try {
            val subEntryId = insertICalObject(subEntry)
            attachment?.let {
                it.icalObjectId = subEntryId
                insertAttachment(it)
            }
            insertRelatedto(
                Relatedto(
                    icalObjectId = subEntryId,
                    reltype = Reltype.PARENT.name,
                    text = parentUID
                )
            )
        } catch (e: SQLiteConstraintException) {
            Log.d("SQLConstraint", e.stackTraceToString())
            return false
        }
    return true
    }

    @Transaction
    suspend fun unlinkFromParent(icalObjectId: Long, parentUID: String) {

        deleteRelatedto(icalObjectId, parentUID)
        getICalObjectByIdSync(icalObjectId)?.let {
                it.makeDirty()
                update(it)
        }
    }

    @Transaction
    suspend fun saveAll(
        icalObject: ICalObject,
        categories: List<Category>,
        comments: List<Comment>,
        attendees: List<Attendee>,
        resources: List<Resource>,
        attachments: List<Attachment>,
        alarms: List<Alarm>,
        enforceUpdateAll: Boolean
    ) {

        try {

                if (getCategoriesSync(icalObject.id) != categories || enforceUpdateAll) {
                    deleteCategories(icalObject.id)
                    categories.forEach { changedCategory ->
                        changedCategory.icalObjectId = icalObject.id
                        insertCategory(changedCategory)
                    }
                }

                if (getCommentsSync(icalObject.id) != comments || enforceUpdateAll) {
                    deleteComments(icalObject.id)
                    comments.forEach { changedComment ->
                        changedComment.icalObjectId = icalObject.id
                        insertComment(changedComment)
                    }
                }

                if (getAttendeesSync(icalObject.id) != attendees || enforceUpdateAll) {
                    deleteAttendees(icalObject.id)
                    attendees.forEach { changedAttendee ->
                        changedAttendee.icalObjectId = icalObject.id
                        insertAttendee(changedAttendee)
                    }
                }

                if (getResourcesSync(icalObject.id) != resources || enforceUpdateAll) {
                    deleteResources(icalObject.id)
                    resources.forEach { changedResource ->
                        changedResource.icalObjectId = icalObject.id
                        insertResource(changedResource)
                    }
                }

                if (getAttachmentsSync(icalObject.id) != attachments || enforceUpdateAll) {
                    deleteAttachments(icalObject.id)
                    attachments.forEach { changedAttachment ->
                        changedAttachment.icalObjectId = icalObject.id
                        insertAttachment(changedAttachment)
                    }
                }

                if (getAlarmsSync(icalObject.id) != alarms || enforceUpdateAll) {
                    deleteAlarms(icalObject.id)
                    alarms.forEach { changedAlarm ->
                        changedAlarm.icalObjectId = icalObject.id
                        changedAlarm.alarmId = insertAlarm(changedAlarm)
                    }
                }

                icalObject.makeDirty()
                update(icalObject)
                makeSeriesDirty(icalObject.uid)
                recreateRecurring(icalObject)
            } catch (e: SQLiteConstraintException) {
            Log.d("SQLConstraint", "Corrupted ID: ${icalObject.id}")
            Log.d("SQLConstraint", e.stackTraceToString())
        }
    }


    /**
     * Adds new categories to the selected entries (if they don't exist already)
     * @param iCalObjectIds for which the categories should be added/removed
     * @param addedCategories that should be added
     * @param removedCategories that should be deleted
     */
    @Transaction
    suspend fun updateCategories(iCalObjectIds: List<Long>, addedCategories: List<String>, removedCategories: List<String>) {

        if (removedCategories.isNotEmpty())
            deleteCategoriesForICalObjects(removedCategories, iCalObjectIds)

        addedCategories.forEach { category ->
            iCalObjectIds.forEach { selected ->
                if (getCategoryForICalObjectByName(selected, category) == null)
                    insertCategory(
                        Category(
                            icalObjectId = selected,
                            text = category
                        )
                    )
            }
        }

        iCalObjectIds.forEach { iCalObjectId ->
            getICalObjectById(iCalObjectId)?.let {
                it.makeDirty()
                update(it)
                makeSeriesDirty(it.uid)
            }
        }
    }

    /**
     * Adds new resources to the selected entries (if they don't exist already)
     * @param iCalObjectIds for which the resources should be added/removed
     * @param addedResources that should be added
     * @param removedResources that should be deleted
     */
    @Transaction
    suspend fun updateResources(iCalObjectIds: List<Long>, addedResources: List<String>, removedResources: List<String>) {

        if (removedResources.isNotEmpty())
            deleteResourcesForICalObjects(removedResources, iCalObjectIds)

        addedResources.forEach { resource ->
            iCalObjectIds.forEach { selected ->
                if (getResourceForICalObjectByName(selected, resource) == null)
                    insertResource(
                        Resource(
                            icalObjectId = selected,
                            text = resource
                        )
                    )
            }
        }

        iCalObjectIds.forEach {iCalObjectId ->
            getICalObjectById(iCalObjectId)?.let {
                it.makeDirty()
                update(it)
                makeSeriesDirty(it.uid)
            }
        }
    }

    /**
     * Updates the classification of the selected entries
     * @param iCalObjectIds for which the classification should be updated
     * @param newClassification to be set
     */
    @Transaction
    suspend fun updateClassification(iCalObjectIds: List<Long>, newClassification: Classification) {

        iCalObjectIds.forEach { iCalObjectId ->
            getICalObjectByIdSync(iCalObjectId)?.let {
                it.classification = newClassification.classification
                it.makeDirty()
                update(it)
                makeSeriesDirty(it.uid)
            }
        }
    }

    /**
     * Updates the priority of the selected entries
     * @param iCalObjectIds for which the priority should be updated
     * @param newPriority to be set
     */
    suspend fun updatePriority(iCalObjectIds: List<Long>, newPriority: Int?) {

        iCalObjectIds.forEach { iCalObjectId ->
            getICalObjectByIdSync(iCalObjectId)?.let {
                it.priority = newPriority
                it.makeDirty()
                update(it)
                makeSeriesDirty(it.uid)
            }
        }
    }


    /**
     * Inserts a new icalobject with categories
     * @param icalObject to be inserted
     * @param categories the list of categories that should be linked to the icalObject
     * @param attachments to be added
     * @param alarm to be added
     */
    @Transaction
    suspend fun insertQuickItem(icalObject: ICalObject, categories: List<Category>, attachments: List<Attachment>, alarm: Alarm?): Long? {

        try {
            val newId = insertICalObject(icalObject)
            icalObject.id = newId

            categories.forEach {
                it.icalObjectId = newId
                insertCategory(it)
            }

            attachments.forEach { attachment ->
                attachment.icalObjectId = newId
                insertAttachment(attachment)
            }

            alarm?.let {
                it.icalObjectId = newId
                insertAlarm(it)
            }
            return newId

        } catch (e: SQLiteConstraintException) {
            Log.d("SQLConstraint", "Corrupted ID: ${icalObject.id}")
            Log.d("SQLConstraint", e.stackTraceToString())
            return null
        }
    }

    /**
     * Retrieve the UID of an iCalObjectId
     * @return the uid or null if not found
     */
    @Query("SELECT $COLUMN_UID FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_ID = :iCalObjectId")
    fun getUid(iCalObjectId: Long): String?

}
