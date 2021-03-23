/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.bitfire.notesx5.database

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import at.bitfire.notesx5.database.properties.*
import at.bitfire.notesx5.database.relations.ICalEntity
import at.bitfire.notesx5.database.relations.ICal4ListWithRelatedto
import at.bitfire.notesx5.database.views.ICal4List
import at.bitfire.notesx5.database.views.VIEW_NAME_ICAL4LIST


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
     *
     * @return a list of [Category.text] as LiveData<List<String>>
     */
    @Transaction
    @Query("SELECT DISTINCT text FROM category ORDER BY text ASC")
    fun getAllCategories(): LiveData<List<String>>

    /**
     * Retrieve an list of all DISTINCT Organizer caladdresses ([Organizer.caladdress]) as a LiveData-List
     *
     * @return a list of [Organizer.caladdress] as LiveData<List<String>>
     */
    @Transaction
    @Query("SELECT DISTINCT caladdress FROM organizer ORDER BY caladdress ASC")
    fun getAllOrganizers(): LiveData<List<String>>

    /**
     * Retrieve an list of all DISTINCT Collections ([Collection]) as a LiveData-List
     *
     * @return a list of [Collection] as LiveData<List<String>>
     */
    @Transaction
    @Query("SELECT * FROM collection ORDER BY displayname ASC")
    fun getAllCollections(): LiveData<List<ICalCollection>>

    /**
     * Retrieve an list of [ICalObject] that are child-elements of another [ICalObject]
     * by checking if the [ICalObject.id] is listed as a [Relatedto.linkedICalObjectId].
     *
     * @return a list of [ICalObject] as LiveData<List<[ICalObject]>>
     */
    @Transaction
    @Query("SELECT $VIEW_NAME_ICAL4LIST.* from $VIEW_NAME_ICAL4LIST INNER JOIN $TABLE_NAME_RELATEDTO ON $VIEW_NAME_ICAL4LIST.$COLUMN_ID = $TABLE_NAME_RELATEDTO.$COLUMN_RELATEDTO_LINKEDICALOBJECT_ID WHERE $VIEW_NAME_ICAL4LIST.$COLUMN_COMPONENT = 'TODO'")
    fun getAllSubtasks(): LiveData<List<ICal4List?>>

    /**
     * Retrieve an list the number of Subtasks of an [ICalObject] as a [SubtaskCount].
     * This is especially used for Sub-Sub-Tasks to show the number of Sub-Sub-Tasks on a Sub-Task.
     *
     * @return a list of [ICalObject] as LiveData<List<[ICalObject]>>
     */
    @Transaction
    @Query("SELECT icalobject._id as icalobjectId, count(*) as count from relatedto INNER JOIN icalobject ON icalobject._id = relatedto.icalObjectId WHERE icalobject.component = 'TODO' GROUP BY icalobjectId")
    fun getSubtasksCount(): LiveData<List<SubtaskCount>>

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



/*
SELECTs (SELECTs by Id, snychronous for Content Povider)
 */
    /**
     * Retrieve an [ICalObject] by Id synchronously (non-suspend)
     *
     * @param id The id of the [ICalObject] in the DB
     * @return the [ICalObject] with the passed id or null if not found
     */
    @Query("SELECT * FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_ID = :id")
    fun getICalObjectByIdSync(id: Long): ICalObject?

    /**
     * Retrieve an [Attendee] by Id synchronously (non-suspend)
     *
     * @param id The id of the [Attendee] in the DB
     * @return the [Attendee] with the passed id or null if not found
     */
    @Query("SELECT * FROM $TABLE_NAME_ATTENDEE WHERE $COLUMN_ATTENDEE_ID = :id")
    fun getAttendeeByIdSync(id: Long): Attendee?

    /**
     * Retrieve an [Category] by Id synchronously (non-suspend)
     *
     * @param id The id of the [Category] in the DB
     * @return the [Category] with the passed id or null if not found
     */
    @Query("SELECT * FROM $TABLE_NAME_CATEGORY WHERE $COLUMN_CATEGORY_ID = :id")
    fun getCategoryByIdSync(id: Long): Category?

    /**
     * Retrieve an [Comment] by Id synchronously (non-suspend)
     *
     * @param id The id of the [Comment] in the DB
     * @return the [Comment] with the passed id or null if not found
     */
    @Query("SELECT * FROM $TABLE_NAME_COMMENT WHERE $COLUMN_COMMENT_ID = :id")
    fun getCommentByIdSync(id: Long): Comment?

    /**
     * Retrieve an [Contact] by Id synchronously (non-suspend)
     *
     * @param id The id of the [Contact] in the DB
     * @return the [Contact] with the passed id or null if not found
     */
    @Query("SELECT * FROM $TABLE_NAME_CONTACT WHERE $COLUMN_CONTACT_ID = :id")
    fun getContactByIdSync(id: Long): Contact?

    /**
     * Retrieve an [Organizer] by Id synchronously (non-suspend)
     *
     * @param id The id of the [Organizer] in the DB
     * @return the [Organizer] with the passed id or null if not found
     */
    @Query("SELECT * FROM $TABLE_NAME_ORGANIZER WHERE $COLUMN_ORGANIZER_ID = :id")
    fun getOrganizerByIdSync(id: Long): Organizer?

    /**
     * Retrieve an [Relatedto] by Id synchronously (non-suspend)
     *
     * @param id The id of the [Relatedto] in the DB
     * @return the [Relatedto] with the passed id or null if not found
     */
    @Query("SELECT * FROM $TABLE_NAME_RELATEDTO WHERE $COLUMN_RELATEDTO_ID = :id")
    fun getRelatedtoByIdSync(id: Long): Relatedto?

    /**
     * Retrieve an [Resource] by Id synchronously (non-suspend)
     *
     * @param id The id of the [Resource] in the DB
     * @return the [Resource] with the passed id or null if not found
     */
    @Query("SELECT * FROM $TABLE_NAME_RESOURCE WHERE $COLUMN_RESOURCE_ID = :id")
    fun getResourceByIdSync(id: Long): Resource?

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
    suspend fun insertRelatedto(relatedto: Relatedto): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun upsertCollection(ICalCollection: ICalCollection): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: ICalObject): Long


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
    fun insertContactSync(contact: Contact): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertResourceSync(resource: Resource): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCollectionSync(ICalCollection: ICalCollection): Long


    /*
UPDATEs (Synchronously)
 */
/*
    @Update
    fun updateICalObjectSync(iCalObject: ICalObject): Int

    @Update
    fun updateAttendeeSync(attendee: Attendee): Int

    @Update
    fun updateCategorySync(category: Category): Int

    @Update
    fun updateCommentSync(comment: Comment): Int

    @Update
    fun updateOrganizerSync(organizer: Organizer): Int

    @Update
    fun updateRelatedtoSync(relatedto: Relatedto): Int

    @Update
    fun updateContactSync(contact: Contact): Int


    @Update
    fun updateCollectionSync(collection: ICalCollection): Int

    @Update
    fun updateResourceSync(resource: Resource): Int


 */

/*
DELETEs by Object
 */


    // TODO Take care to delete also child elements!
    /**
     * Delete an iCalObject by the object.
     *
     * @param icalObject The object of the icalObject that should be deleted.
     */
    @Delete
    fun delete(icalObject: ICalObject)



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
     * Delete a relatedto by parentId and childId.
     *
     * @param parentId The Id of the parent IcalObject.
     * @param childId The Id of the child IcalObject.
     */
    @Query("DELETE FROM relatedto WHERE relatedto.icalObjectId = :parentId and relatedto.linkedICalObjectId = :childId")
    fun deleteRelatedto(parentId: Long, childId: Long)


    /*
    DELETE by Id
     */

    /**
     * Delete an IcalObject by the ID.
     *
     * @param id The row ID.
     * @return A number of ICalObjects deleted. This should always be `1`.
     */
    @Query("DELETE FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_ID = :id")
    fun deleteICalObjectById(id: Long): Int

    /**
     * Delete an Attendee by the ID.
     *
     * @param id The row ID.
     * @return A number of Attendees deleted. This should always be `1`.
     */
    @Query("DELETE FROM $TABLE_NAME_ATTENDEE WHERE $COLUMN_ATTENDEE_ID = :id")
    fun deleteAttendeeById(id: Long): Int


    /**
     * Delete an Category by the ID.
     *
     * @param id The row ID.
     * @return A number of Categories deleted. This should always be `1`.
     */
    @Query("DELETE FROM $TABLE_NAME_CATEGORY WHERE $COLUMN_CATEGORY_ID = :id")
    fun deleteCategoryById(id: Long): Int


    /**
     * Delete an Comment by the ID.
     *
     * @param id The row ID.
     * @return A number of Comments deleted. This should always be `1`.
     */
    @Query("DELETE FROM $TABLE_NAME_COMMENT WHERE $COLUMN_COMMENT_ID = :id")
    fun deleteCommentById(id: Long): Int

    /**
     * Delete an Contact by the ID.
     *
     * @param id The row ID.
     * @return A number of Contacts deleted. This should always be `1`.
     */
    @Query("DELETE FROM $TABLE_NAME_CONTACT WHERE $COLUMN_CONTACT_ID = :id")
    fun deleteContactById(id: Long): Int

    /**
     * Delete an Organizer by the ID.
     *
     * @param id The row ID.
     * @return A number of Organizers deleted. This should always be `1`.
     */
    @Query("DELETE FROM $TABLE_NAME_ORGANIZER WHERE $COLUMN_ORGANIZER_ID = :id")
    fun deleteOrganizerById(id: Long): Int

    /**
     * Delete a Related-to by the ID.
     *
     * @param id The row ID.
     * @return A number of Related-to deleted. This should always be `1`.
     */
    @Query("DELETE FROM $TABLE_NAME_RELATEDTO WHERE $COLUMN_RELATEDTO_ID = :id")
    fun deleteRelatedtoById(id: Long): Int

    /**
     * Delete an Resource by the ID.
     *
     * @param id The row ID.
     * @return A number of Resource deleted. This should always be `1`.
     */
    @Query("DELETE FROM $TABLE_NAME_RESOURCE WHERE $COLUMN_RESOURCE_ID = :id")
    fun deleteResourceById(id: Long): Int


    /**
     * Delete an Collection by the ID. Due to the foreign constraint
     * this would also delete all ICalObjects (and its' related entities)
     * within the Collection
     *
     * @param id The row ID.
     * @return A number of Collections deleted. This should always be `1`.
     */
    @Query("DELETE FROM $TABLE_NAME_COLLECTION WHERE $COLUMN_COLLECTION_ID = :id")
    fun deleteCollectionById(id: Long): Int


    /**
     * Delete all children of a parent ICalObject by the parent ID.
     *
     * @param parentKey The row ID of the parent.
     */
    @Transaction
    @Query("DELETE FROM icalobject WHERE icalobject._id in (SELECT linkedICalObjectId FROM relatedto WHERE relatedto.icalObjectId = :parentKey)")
    fun deleteRelatedChildren(parentKey: Long)

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







    @Update
    suspend fun update(icalObject: ICalObject)

    @Transaction
    @Query("UPDATE $TABLE_NAME_ICALOBJECT SET $COLUMN_PERCENT = :progress, $COLUMN_STATUS = :status, $COLUMN_LAST_MODIFIED = :lastModified, $COLUMN_SEQUENCE = $COLUMN_SEQUENCE + 1, $COLUMN_DIRTY = 1 WHERE $COLUMN_ID = :id")
    suspend fun updateProgress(id: Long, progress: Int, status: String, lastModified: Long)


/*
    @Transaction
    @RawQuery
    fun getIcalEntity(query: SupportSQLiteQuery): LiveData<List<ICalEntity>>


 */

    @Transaction
    @RawQuery(observedEntities = [ICal4List::class, Relatedto::class])
    fun getIcalObjectWithRelatedto(query: SupportSQLiteQuery): LiveData<List<ICal4ListWithRelatedto>>


    @Transaction
    @Query("SELECT * from icalobject WHERE _id = :key")
    fun get(key: Long): LiveData<ICalEntity?>


    // This query makes a Join between icalobjects and the linked (child) elements (JOIN relatedto ON icalobject.id = relatedto.linkedICalObjectId ) and then filters for one specific parent element (WHERE relatedto.icalObjectId = :parentKey)
    @Transaction
    @Query("SELECT icalobject.* from icalobject INNER JOIN relatedto ON icalobject._id = relatedto.linkedICalObjectId WHERE relatedto.icalObjectId = :parentKey and icalobject.component = 'NOTE'")
    fun getRelatedNotes(parentKey: Long): LiveData<List<ICalObject?>>

    // This query makes a Join between icalobjects and the linked (child) elements (JOIN relatedto ON icalobject.id = relatedto.linkedICalObjectId ) and then filters for one specific parent element (WHERE relatedto.icalObjectId = :parentKey)
    @Transaction
    @Query("SELECT icalobject.* from icalobject INNER JOIN relatedto ON icalobject._id = relatedto.linkedICalObjectId WHERE relatedto.icalObjectId = :parentKey and icalobject.component = 'TODO'")
    fun getRelatedTodos(parentKey: Long): LiveData<List<ICalObject?>>



    /*
    Queries for the content provider returning a Cursor
     */
    @Transaction
    @RawQuery
    fun getCursor(query: SupportSQLiteQuery): Cursor?

    @Transaction
    @RawQuery
    fun getICalObjectRaw(query: SupportSQLiteQuery): List<ICalObject>




    /*

    @Query("SELECT * from attendee WHERE attendee.icalObjectId = :icalobjectId")
    fun getAttendeesByIcalentity(icalobjectId: Long): Cursor?

    @Query("SELECT * from category WHERE category.icalObjectId = :icalobjectId")
    fun getCategoriesByIcalentity(icalobjectId: Long): Cursor?

    @Query("SELECT * from comment WHERE comment.icalObjectId = :icalobjectId")
    fun getCommentsByIcalentity(icalobjectId: Long): Cursor?

    @Query("SELECT * from contact WHERE contact.icalObjectId = :icalobjectId")
    fun getContactByIcalentity(icalobjectId: Long): Cursor?

    @Query("SELECT * from organizer WHERE organizer.icalObjectId = :icalobjectId")
    fun getOrganizersByIcalentity(icalobjectId: Long): Cursor?

    @Query("SELECT * from relatedto WHERE relatedto.icalObjectId = :icalobjectId")
    fun getRelatedtoByIcalentity(icalobjectId: Long): Cursor?

    @Query("SELECT * from resource WHERE resource.icalObjectId = :icalobjectId")
    fun getResourceByIcalentity(icalobjectId: Long): Cursor?


     */


}


class SubtaskCount {
    var icalobjectId: Long = 0L
    var count: Int = 0
}


