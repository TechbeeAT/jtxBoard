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
import at.bitfire.notesx5.database.relations.ICalEntityWithCategory


/**
 * Defines methods for using the SleepNight class with Room.
 */
@Dao
interface ICalDatabaseDao {


    @Transaction
    @Query("SELECT DISTINCT text FROM category ORDER BY text ASC")
    fun getAllCategories(): LiveData<List<String>>

    @Transaction
    @Query("SELECT DISTINCT caladdress FROM organizer ORDER BY caladdress ASC")
    fun getAllOrganizers(): LiveData<List<String>>

    @Transaction
    @Query("SELECT DISTINCT collection FROM icalobject ORDER BY collection ASC")
    fun getAllCollections(): LiveData<List<String>>

/*
INSERTs
 */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(vJournalItem: ICalObject): Long

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: ICalObject): Long




/*
DELETEs
 */

    @Delete
    fun deleteCategory(category: Category)

    @Delete
    fun deleteComment(comment: Comment)

    @Delete
    fun deleteRelatedto(rel: Relatedto)

    @Delete
    fun deleteAttendee(attendee: Attendee)

    @Query("DELETE FROM relatedto WHERE relatedto.icalObjectId = :parentId and relatedto.linkedICalObjectId = :childId")
    fun deleteRelatedto(parentId: Long, childId: Long)

    @Transaction
    @Query("DELETE FROM icalobject WHERE icalobject._id in (SELECT linkedICalObjectId FROM relatedto WHERE relatedto.icalObjectId = :parentKey)")
    fun deleteRelatedChildren(parentKey: Long)




    @Update
    suspend fun update(icalObject: ICalObject)



    // TODO Take care to delete also child elements!
    @Delete
    fun delete(icalObject: ICalObject)


    @Transaction
    @RawQuery
    fun getIcalEntity(query: SupportSQLiteQuery): LiveData<List<ICalEntity>>

    @Transaction
    @RawQuery
    fun getIcalObjectWithCategory(query: SupportSQLiteQuery): LiveData<List<ICalEntityWithCategory>>


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

    @Transaction
    @Query("SELECT icalobject.* from icalobject INNER JOIN relatedto ON icalobject._id = relatedto.linkedICalObjectId WHERE icalobject.component = 'TODO'")
    fun getAllSubtasks(): LiveData<List<ICalObject?>>

    // Determines the number of Subtasks. This is especially used for Sub-Sub-Tasks to show the number of Sub-Sub-Tasks on a Sub-Task
    @Transaction
    @Query("SELECT icalobject._id as icalobjectId, count(*) as count from relatedto INNER JOIN icalobject ON icalobject._id = relatedto.icalObjectId WHERE icalobject.component = 'TODO' GROUP BY icalobjectId")
    fun getSubtasksCount(): LiveData<List<SubtaskCount>>


    /*
    Queries for the content provider returning a Cursor
     */
    @Transaction
    @RawQuery
    fun getCursor(query: SupportSQLiteQuery): Cursor?



    /*
    @Query("SELECT * from icalobject")
    fun getAllIcalObjects(): Cursor?

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


