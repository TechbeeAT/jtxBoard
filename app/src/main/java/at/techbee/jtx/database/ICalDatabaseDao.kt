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
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.*


/**
 * Defines methods for using the SleepNight class with Room.
 */
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
    @Transaction
    @Query("SELECT $COLUMN_ATTACHMENT_URI FROM $TABLE_NAME_ATTACHMENT")
    suspend fun getAllAttachmentUris(): List<String>

    /**
     * Retrieve an Attachment with a specific [id]
     *
     * @return the [Attachment] with this [id]
     */
    @Transaction
    @Query("SELECT * FROM $TABLE_NAME_ATTACHMENT WHERE $COLUMN_ATTACHMENT_ID = :id")
    fun getAttachmentById(id: Long): Attachment?



    /**
     * Retrieve an list of all DISTINCT Organizer caladdresses ([Organizer.caladdress]) as a LiveData-List
     *
     * @return a list of [Organizer.caladdress] as LiveData<List<String>>
     */
    @Transaction
    @Query("SELECT DISTINCT caladdress FROM organizer ORDER BY caladdress ASC")
    fun getAllOrganizers(): LiveData<List<String>>

    /**
     * Retrieve an list of all Collections ([Collection]) as a LiveData-List
     *
     * @return a list of [Collection] as LiveData<List<ICalCollection>>
     */
    @Transaction
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
    @Transaction
    @Query("SELECT * FROM $VIEW_NAME_COLLECTIONS_VIEW ORDER BY $COLUMN_COLLECTION_ACCOUNT_TYPE = 'LOCAL' DESC, $COLUMN_COLLECTION_ACCOUNT_NAME ASC")
    fun getAllCollectionsView(): LiveData<List<CollectionsView>>


    /**
     * Retrieve a list of ICalObjectIds that are parents within a given Collection
     */
    @Transaction
    @Query("SELECT $COLUMN_ID FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_ICALOBJECT_COLLECTIONID = :collectionId AND $COLUMN_UID IN (SELECT $COLUMN_RELATEDTO_TEXT FROM $TABLE_NAME_RELATEDTO WHERE $COLUMN_RELATEDTO_RELTYPE = 'PARENT')")
    suspend fun getICalObjectIdsWithinCollection(collectionId: Long): List<Long>


    /**
     * Retrieve an list of all remote collections ([ICalCollection])
     *
     * @return a list of [ICalCollection] as LiveData<List<ICalCollection>>
     */
    @Transaction
    @Query("SELECT * FROM $TABLE_NAME_COLLECTION WHERE $COLUMN_COLLECTION_ACCOUNT_TYPE NOT IN (\'LOCAL\')")
    fun getAllRemoteCollections(): LiveData<List<ICalCollection>>



    /**
     * Retrieve an list of all Relatedto ([Relatedto]) as a List
     *
     * @return a list of [Relatedto] as List<Relatedto>
     */
    @Transaction
    @Query("SELECT * FROM $TABLE_NAME_RELATEDTO")
    fun getAllRelatedto(): LiveData<List<Relatedto>>

    /**
     * Retrieve an list of all Relatedto ([Relatedto]) as a List
     *
     * @return a list of [Relatedto] as List<Relatedto>
     */
    @Transaction
    @Query("SELECT * FROM $TABLE_NAME_RELATEDTO")
    fun getAllRelatedtoSync(): List<Relatedto>

    /**
     * Retrieve the UID of a specific ICalObjectID
     * @param uid ot find
     * @return ICalObject of the UID
     */
    @Transaction
    @Query("SELECT * FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_UID = :uid")
    fun getICalObjectFor(uid: String): ICalObject?



    /**
     * Retrieve an list of [ICalObject] that are child-elements of another [ICalObject]
     * by checking if the [ICalObject.id] is listed as a [Relatedto.linkedICalObjectId].
     *
     * @return a list of [ICalObject] as LiveData<List<[ICalObject]>>
     */
    @Transaction
    @Query("SELECT * FROM $TABLE_NAME_ATTACHMENT")
    fun getAllAttachments(): LiveData<List<Attachment>>


    /**
     * Retrieve an list of all  [Attachment]
     * @return a list of [Attachment] as LiveData<List<[Attachment]>>
     */
    @Transaction
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
    @Query("SELECT $COLUMN_ID FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_UID = :uid AND $COLUMN_RECURID IS NULL")
    fun getSeriesICalObjectIdByUID(uid: String?): LiveData<Long?>


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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun upsertCollection(ICalCollection: ICalCollection): Long


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
    fun insertCollectionSync(ICalCollection: ICalCollection): Long



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
     * Delete an ICalCollection by the id.
     * @param id The ICalCollection to be deleted.
     */
    @Query("DELETE FROM $TABLE_NAME_COLLECTION WHERE $COLUMN_COLLECTION_ID = :id")
    fun deleteICalCollectionbyId(id: Long)

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
     * Delete all relatedto with a specific icalobjectid.
     * @param [icalobjectId] of the icalObject that should be deleted.
     */
    @Query("DELETE FROM $TABLE_NAME_RELATEDTO WHERE $COLUMN_RELATEDTO_ICALOBJECT_ID = :icalobjectId")
    fun deleteRelatedtos(icalobjectId: Long)

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
     * Delete a category by the object.
     *
     * @param category The object of the category that should be deleted.
     */
    @Delete
    fun deleteCategory(category: Category)

    /**
     * Delete a comment by the object.
     *
     * @param comment The object of the comment that should be deleted.
     */
    @Delete
    fun deleteComment(comment: Comment)

    /**
     * Delete an attachment by the object.
     *
     * @param attachment The object of the attachment that should be deleted.
     */
    @Delete
    fun deleteAttachment(attachment: Attachment)

    /**
     * Delete a relatedto by the object.
     *
     * @param rel The object of the relatedto that should be deleted.
     */
    @Delete
    fun deleteRelatedto(rel: Relatedto)

    /**
     * Delete an attendee by the object.
     *
     * @param attendee The object of the attendee that should be deleted.
     */
    @Delete
    fun deleteAttendee(attendee: Attendee)

    /**
     * Delete a resource by the object.
     *
     * @param resource The object of the resource that should be deleted.
     */
    @Delete
    fun deleteResource(resource: Resource)

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
     * Delete entities through a RawQuery.
     * This is especially used for the Content Provider
     *
     * @param query The DELETE statement.
     * @return A number of Entities deleted.
     */
    @Transaction
    @RawQuery
    fun deleteRAW(query: SupportSQLiteQuery): Int

    /**
     * Updates entities through a RawQuery.
     * This is especially used for the Content Provider
     *
     * @param query The UPDATE statement.
     * @return A number of Entities that were updated.
     */
    @Transaction
    @RawQuery
    fun updateRAW(query: SupportSQLiteQuery): Int


    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun update(icalObject: ICalObject)

    @Update
    fun updateAttachment(attachment: Attachment)

    @Update
    fun updateAlarm(alarm: Alarm)

    /**
     * Gets the next [Alarm]s after a certain date or after now
     * Elements that define a series are excluded.
     * @param limit: The number of [Alarm]s that should be returned
     * @param minDate: The date from which the [Alarm]s should be fetched (default: System.currentTimeMillis())
     * @return a list of the next alarms
     */
    @Transaction
    @Query("SELECT $TABLE_NAME_ALARM.* FROM $TABLE_NAME_ALARM INNER JOIN $TABLE_NAME_ICALOBJECT ON $TABLE_NAME_ALARM.$COLUMN_ALARM_ICALOBJECT_ID = $TABLE_NAME_ICALOBJECT.$COLUMN_ID WHERE $COLUMN_DELETED = 0 AND $COLUMN_RRULE IS NULL AND $COLUMN_ALARM_TRIGGER_TIME > :minDate ORDER BY $COLUMN_ALARM_TRIGGER_TIME ASC LIMIT :limit")
    fun getNextAlarms(limit: Int, minDate: Long = System.currentTimeMillis()): List<Alarm>

    /**
     * Gets the next due [ICalObject]s after a certain date or after now.
     * Elements that define a series are excluded.
     * @param limit: The number of [ICalObject]s that should be returned
     * @param minDate: The due date from which the [ICalObject]s should be fetched (default: System.currentTimeMillis())
     * @return a list of the next due icalobjects
     */
    @Transaction
    @Query("SELECT $TABLE_NAME_ICALOBJECT.* FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_DELETED = 0 AND $COLUMN_DUE > :minDate AND $COLUMN_RRULE IS NULL ORDER BY $COLUMN_DUE ASC LIMIT :limit")
    fun getNextDueEntries(limit: Int, minDate: Long = System.currentTimeMillis()): List<ICalObject>

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateCollection(collection: ICalCollection)

    @Transaction
    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET $COLUMN_DELETED = 1, $COLUMN_LAST_MODIFIED = :lastModified, $COLUMN_SEQUENCE = $COLUMN_SEQUENCE + 1, $COLUMN_DIRTY = 1 WHERE $COLUMN_ID in (:id)")
    suspend fun updateToDeleted(id: Long, lastModified: Long)

    @Transaction
    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET $COLUMN_ICALOBJECT_COLLECTIONID = :collectionId, $COLUMN_LAST_MODIFIED = :lastModified, $COLUMN_SEQUENCE = $COLUMN_SEQUENCE + 1, $COLUMN_DIRTY = 1 WHERE $COLUMN_ID in (:ids)")
    suspend fun updateCollection(ids: List<Long>, collectionId: Long, lastModified: Long)

    @Transaction
    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET $COLUMN_LAST_MODIFIED = :lastModified, $COLUMN_SEQUENCE = $COLUMN_SEQUENCE + 1, $COLUMN_DIRTY = 1 WHERE $COLUMN_ID = :id")
    suspend fun updateSetDirty(id: Long, lastModified: Long)

    @Transaction
    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET $COLUMN_SUBTASKS_EXPANDED = :isSubtasksExpanded, $COLUMN_SUBNOTES_EXPANDED = :isSubnotesExpanded, $COLUMN_ATTACHMENTS_EXPANDED = :isAttachmentsExpanded WHERE $COLUMN_ID = :id")
    suspend fun updateExpanded(id: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isAttachmentsExpanded: Boolean)

    @Transaction
    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET $COLUMN_SORT_INDEX = :index WHERE $COLUMN_ID = :id")
    suspend fun updateOrder(id: Long, index: Int?)



    @Transaction
    @RawQuery(observedEntities = [ICal4List::class])
    fun getIcal4List(query: SupportSQLiteQuery): LiveData<List<ICal4List>>

    @Transaction
    @RawQuery(observedEntities = [ICal4List::class])
    fun getIcal4ListSync(query: SupportSQLiteQuery): List<ICal4List>

    @Transaction
    @RawQuery(observedEntities = [ICal4List::class])
    fun getSubEntries(query: SupportSQLiteQuery): LiveData<List<ICal4List>>

    @Transaction
    @RawQuery(observedEntities = [ICal4List::class])
    fun getSubEntriesSync(query: SupportSQLiteQuery): List<ICal4List>

    @Transaction
    @Query("SELECT * from icalobject WHERE _id = :key")
    fun get(key: Long): LiveData<ICalEntity?>


    @Transaction
    @Query("SELECT * from icalobject WHERE _id = :key")
    fun getSync(key: Long): ICalEntity?


    @Transaction
    @Query("SELECT * from alarm WHERE _id = :key")
    fun getAlarmSync(key: Long): Alarm?

    @Transaction
    @Query("SELECT * from alarm WHERE icalObjectId = :icalobjectId")
    fun getAlarmsSync(icalobjectId: Long): List<Alarm>?


    @Transaction
    @Query("SELECT * from $TABLE_NAME_CATEGORY WHERE $COLUMN_CATEGORY_ICALOBJECT_ID = :iCalObjectId AND $COLUMN_CATEGORY_TEXT = :category")
    fun getCategoryForICalObjectByName(iCalObjectId: Long, category: String): Category?

    @Transaction
    @Query("SELECT * from $TABLE_NAME_RESOURCE WHERE $COLUMN_RESOURCE_ICALOBJECT_ID = :iCalObjectId AND $COLUMN_RESOURCE_TEXT = :resource")
    fun getResourceForICalObjectByName(iCalObjectId: Long, resource: String): Resource?


    /** This query returns all ids of child elements of the given [parentKey]  */
    @Transaction
    @Query("SELECT $TABLE_NAME_ICALOBJECT.* FROM $TABLE_NAME_ICALOBJECT WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID IN (SELECT rel.$COLUMN_RELATEDTO_ICALOBJECT_ID FROM $TABLE_NAME_RELATEDTO rel INNER JOIN $TABLE_NAME_ICALOBJECT ical ON rel.$COLUMN_RELATEDTO_TEXT = ical.$COLUMN_UID AND ical.$COLUMN_ID = :parentKey AND $COLUMN_RELATEDTO_RELTYPE = 'PARENT')" )
    suspend fun getRelatedChildren(parentKey: Long): List<ICalObject>

    /** This query returns the average progress of the given ICalObjectIds  */
    @Transaction
    @Query("SELECT AVG(IFNULL($COLUMN_PERCENT, 0)) FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_ID IN (:iCalObjectIds)" )
    suspend fun getAverageProgressOf(iCalObjectIds: List<Long>): Int?

    @Transaction
    @Query("SELECT * from $TABLE_NAME_RELATEDTO WHERE $COLUMN_RELATEDTO_ICALOBJECT_ID = :icalobjectid AND $COLUMN_RELATEDTO_TEXT = :linkedUID AND $COLUMN_RELATEDTO_RELTYPE = :reltype")
    fun findRelatedTo(icalobjectid: Long, linkedUID: String, reltype: String): Relatedto?


    // This query returns all IcalObjects that have a specific ICalObjectId in the field for the OriginalIcalObjectId (ie. all generated items for a recurring entry)
    @Transaction
    @Query("SELECT * from $TABLE_NAME_ICALOBJECT WHERE $COLUMN_UID = :uid AND $COLUMN_RECURID IS NULL")
    fun getRecurSeriesElement(uid: String): ICalObject?


    // This query returns all IcalObjects that have a specific ICalObjectId in the field for the OriginalIcalObjectId (ie. all generated items for a recurring entry)
    @Transaction
    @Query("SELECT * from $TABLE_NAME_ICALOBJECT WHERE $COLUMN_UID = :uid AND $COLUMN_RECURID IS NOT NULL")
    fun getRecurInstances(uid: String): List<ICalObject?>

    @Transaction
    @Query("SELECT $COLUMN_EXDATE from $TABLE_NAME_ICALOBJECT WHERE $COLUMN_ID = :originalId")
    fun getRecurExceptions(originalId: Long): String?

    @Transaction
    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET $COLUMN_EXDATE = :exceptions, $COLUMN_DIRTY = 1, $COLUMN_LAST_MODIFIED = :lastUpdated, $COLUMN_SEQUENCE = $COLUMN_SEQUENCE + 1 WHERE $COLUMN_ID = :originalId")
    fun setRecurExceptions(originalId: Long, exceptions: String?, lastUpdated: Long = System.currentTimeMillis())


    @Transaction
    @Query("DELETE FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_RRULE IS NULL AND $COLUMN_RECURID IS NOT NULL AND $COLUMN_UID NOT IN (SELECT $COLUMN_UID FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_RRULE IS NOT NULL)")
    fun removeOrphans()

    @Transaction
    @Query("SELECT * FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_ID = :id")
    fun getRecurringToPopulate(id: Long): ICalObject?



    /*
    Queries for the content provider returning a Cursor
     */
    @Transaction
    @RawQuery
    fun getCursor(query: SupportSQLiteQuery): Cursor?

    @Transaction
    @RawQuery
    fun getICalObjectRaw(query: SupportSQLiteQuery): List<ICalObject>


    @Transaction
    @Query("DELETE FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_RECURID IS NOT NULL AND $COLUMN_UID = :uid AND $COLUMN_SEQUENCE = 0")
    fun deleteUnchangedRecurringInstances(uid: String?)


    @Transaction
    @Query("DELETE FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_RECURID IS NOT NULL AND $COLUMN_UID = :uid")
    fun deleteRecurringInstances(uid: String?)

    @Transaction
    @Query("SELECT * FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_UID = :uid AND $COLUMN_DTSTART = :dtstart AND $COLUMN_RECURID IS NOT NULL")
    fun getRecurInstance(uid: String?, dtstart: Long): ICalObject?
}
