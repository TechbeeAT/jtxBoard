/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database

import android.database.Cursor
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import at.techbee.jtx.database.locals.*
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.*
import at.techbee.jtx.ui.detail.LocationLatLng
import at.techbee.jtx.ui.presets.XStatusStatusPair



@Dao
interface ICalDatabaseDao {

/*
SELECTs (global selects without parameter)
 */

    /**
     * Retrieve an list of all DISTINCT Category names ([Category.text]) as a LiveData-List
     * @return a list of [Category.text] as LiveData<List<String>>
     */
    @Transaction
    @Query("SELECT DISTINCT $COLUMN_CATEGORY_TEXT FROM $TABLE_NAME_CATEGORY WHERE $COLUMN_CATEGORY_ICALOBJECT_ID IN (SELECT $COLUMN_ID FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_DELETED = 0) ORDER BY $COLUMN_CATEGORY_ID DESC")
    fun getAllCategoriesAsText(): LiveData<List<String>>

    /**
     * Retrieve an list of all DISTINCT Category names ([Category.text]) as a LiveData-List
     * @return a list of [Category.text] as LiveData<List<String>>
     */
    @Transaction
    @Query("SELECT DISTINCT $COLUMN_RESOURCE_TEXT FROM $TABLE_NAME_RESOURCE WHERE $COLUMN_CATEGORY_ICALOBJECT_ID IN (SELECT $COLUMN_ID FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_DELETED = 0) ORDER BY $COLUMN_RESOURCE_ID DESC")
    fun getAllResourcesAsText(): LiveData<List<String>>


    /**
     * Retrieve an list of all Attachment Uris
     *
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
    @Query("SELECT $TABLE_NAME_ICALOBJECT.* FROM $TABLE_NAME_ICALOBJECT INNER JOIN $TABLE_NAME_COLLECTION ON $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ID = $TABLE_NAME_ICALOBJECT.$COLUMN_ICALOBJECT_COLLECTIONID AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_READONLY = 0 WHERE $COLUMN_COMPONENT = 'VTODO' AND ($COLUMN_STATUS = 'COMPLETED' OR $COLUMN_PERCENT = 100)")
    fun getDoneTasks(): List<ICalObject>


    /**
     * Retrieve an [ICalCollection] by Id synchronously (non-suspend)
     *
     * @param id The id of the [ICalCollection] in the DB
     * @return the [ICalCollection] with the passed id or null if not found
     */
    @Query("SELECT * FROM $TABLE_NAME_COLLECTION WHERE $COLUMN_COLLECTION_ID = :id")
    fun getCollectionByIdSync(id: Long): ICalCollection?




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
    fun swapCategories(icalObjectId: Long, oldCategory: String, newCategory: String)




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
    @Query("SELECT $TABLE_NAME_ALARM.* " +
            "FROM $TABLE_NAME_ALARM " +
            "INNER JOIN $TABLE_NAME_ICALOBJECT ON $TABLE_NAME_ALARM.$COLUMN_ALARM_ICALOBJECT_ID = $TABLE_NAME_ICALOBJECT.$COLUMN_ID " +
            "WHERE $COLUMN_DELETED = 0 " +
            "AND $COLUMN_RRULE IS NULL " +
            "AND $COLUMN_ALARM_TRIGGER_TIME > :minDate " +
            "AND ($COLUMN_PERCENT IS NULL OR $COLUMN_PERCENT < 100) " +
            "AND ($COLUMN_STATUS IS NULL OR $COLUMN_STATUS != 'COMPLETED')" +
            "ORDER BY $COLUMN_ALARM_TRIGGER_TIME ASC LIMIT :limit")
    fun getNextAlarms(limit: Int, minDate: Long = System.currentTimeMillis()): List<Alarm>

    /**
     * Gets ICalObjects with lat/long and geofence radius
     * @param limit: The number of [ICalObject]s that should be returned
     * @return a list of ICalObjects
     */
    @Query("SELECT $TABLE_NAME_ICALOBJECT.* " +
            "FROM $TABLE_NAME_ICALOBJECT " +
            "WHERE $COLUMN_DELETED = 0 " +
            "AND $COLUMN_RRULE IS NULL " +
            "AND $COLUMN_GEO_LAT IS NOT NULL " +
            "AND $COLUMN_GEO_LONG IS NOT NULL " +
            "AND $COLUMN_GEOFENCE_RADIUS IS NOT NULL " +
            "LIMIT :limit")
    fun getICalObjectsWithGeofence(limit: Int): List<ICalObject>

    /**
     * Gets the next due [ICalObject]s after a certain date or after now.
     * Elements that define a series are excluded.
     * Sorting is ascending by trigger time.
     * @param limit: The number of [ICalObject]s that should be returned
     * @param minDate: The due date from which the [ICalObject]s should be fetched (default: System.currentTimeMillis())
     * @return a list of the next due icalobjects
     */
    @Query("SELECT $TABLE_NAME_ICALOBJECT.* " +
            "FROM $TABLE_NAME_ICALOBJECT " +
            "WHERE $COLUMN_DELETED = 0 " +
            "AND $COLUMN_DUE > :minDate " +
            "AND $COLUMN_RRULE IS NULL " +
            "AND ($COLUMN_PERCENT IS NULL OR $COLUMN_PERCENT < 100) " +
            "AND ($COLUMN_STATUS IS NULL OR $COLUMN_STATUS != 'COMPLETED')" +
            "ORDER BY $COLUMN_DUE ASC LIMIT :limit")
    fun getNextDueEntries(limit: Int, minDate: Long = System.currentTimeMillis()): List<ICalObject>

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateCollection(collection: ICalCollection)

    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET $COLUMN_DELETED = 1, $COLUMN_LAST_MODIFIED = :lastModified, $COLUMN_SEQUENCE = $COLUMN_SEQUENCE + 1, $COLUMN_DIRTY = 1 WHERE $COLUMN_ID in (:id)")
    suspend fun updateToDeleted(id: Long, lastModified: Long)

    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET $COLUMN_LAST_MODIFIED = :lastModified, $COLUMN_SEQUENCE = $COLUMN_SEQUENCE + 1, $COLUMN_DIRTY = 1 WHERE $COLUMN_ID = :id")
    suspend fun updateSetDirty(id: Long, lastModified: Long)

    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET $COLUMN_SUBTASKS_EXPANDED = :isSubtasksExpanded, $COLUMN_SUBNOTES_EXPANDED = :isSubnotesExpanded, $COLUMN_ATTACHMENTS_EXPANDED = :isAttachmentsExpanded, $COLUMN_PARENTS_EXPANDED = :isParentsExpanded WHERE $COLUMN_ID = :id")
    suspend fun updateExpanded(id: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isParentsExpanded: Boolean, isAttachmentsExpanded: Boolean)

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

    @RawQuery(observedEntities = [ICal4List::class])
    fun getIcal4List(query: SupportSQLiteQuery): LiveData<List<ICal4List>>


    @Transaction
    @RawQuery(observedEntities = [ICal4List::class])
    fun getIcal4ListRel(query: SupportSQLiteQuery): LiveData<List<ICal4ListRel>>

    @Transaction
    @RawQuery(observedEntities = [ICal4List::class])
    fun getIcal4ListSync(query: SupportSQLiteQuery): List<ICal4List>

    @Transaction
    @RawQuery(observedEntities = [ICal4ListRel::class])
    fun getSubEntries(query: SupportSQLiteQuery): LiveData<List<ICal4ListRel>>

    @Transaction
    @RawQuery(observedEntities = [ICal4ListRel::class])
    fun getSubEntriesSync(query: SupportSQLiteQuery): List<ICal4ListRel>

    @Transaction
    @Query("SELECT * from icalobject WHERE _id = :key")
    fun get(key: Long): LiveData<ICalEntity?>


    @Transaction
    @Query("SELECT * from icalobject WHERE _id = :key")
    fun getSync(key: Long): ICalEntity?


    @Query("SELECT * from alarm WHERE _id = :key")
    fun getAlarmSync(key: Long): Alarm?

    @Query("SELECT * from $TABLE_NAME_ALARM WHERE $COLUMN_ALARM_ICALOBJECT_ID = :icalobjectId")
    fun getAlarmsSync(icalobjectId: Long): List<Alarm>


    @Query("SELECT * from $TABLE_NAME_CATEGORY WHERE $COLUMN_CATEGORY_ICALOBJECT_ID = :iCalObjectId AND $COLUMN_CATEGORY_TEXT = :category")
    fun getCategoryForICalObjectByName(iCalObjectId: Long, category: String): Category?

    @Query("SELECT * from $TABLE_NAME_RESOURCE WHERE $COLUMN_RESOURCE_ICALOBJECT_ID = :iCalObjectId AND $COLUMN_RESOURCE_TEXT = :resource")
    fun getResourceForICalObjectByName(iCalObjectId: Long, resource: String): Resource?


    /** This query returns all ids of child elements of the given [parentKey]  */
    @Query("SELECT $TABLE_NAME_ICALOBJECT.* FROM $TABLE_NAME_ICALOBJECT WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID IN (SELECT rel.$COLUMN_RELATEDTO_ICALOBJECT_ID FROM $TABLE_NAME_RELATEDTO rel INNER JOIN $TABLE_NAME_ICALOBJECT ical ON rel.$COLUMN_RELATEDTO_TEXT = ical.$COLUMN_UID AND ical.$COLUMN_ID = :parentKey AND $COLUMN_RELATEDTO_RELTYPE = 'PARENT')" )
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
    fun setRecurExceptions(originalId: Long, exceptions: String?, lastUpdated: Long = System.currentTimeMillis())

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

}
