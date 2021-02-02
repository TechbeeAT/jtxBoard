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

    /*
    @Query("SELECT * from vjournalitems WHERE id = :key")
    fun get(key: Long): LiveData<VJournalItem?>

    @Query("SELECT * FROM vjournals ORDER BY dtstart DESC, created DESC")
    fun getVJournalItems(): LiveData<List<VJournal>>


    @Query("SELECT * FROM vjournals WHERE component LIKE :component AND (categories LIKE :search_global OR summary LIKE :search_global OR description LIKE :search_global OR organizer LIKE :search_global OR status LIKE :search_global)  AND categories LIKE :search_category AND organizer LIKE :search_organizer AND status LIKE :search_status AND classification LIKE :search_classification ORDER BY dtstart DESC, created DESC")
    fun getVJournalItems(component: Array<String>, search_global: String, search_category: Array<String>, search_organizer: Array<String>, search_status: Array<String>, search_classification: Array<String>): LiveData<List<VJournal>>

     */

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
    suspend fun upsertRelatedto(relatedto: Relatedto): Long

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
    @Query("DELETE FROM icalobject WHERE icalobject.id in (SELECT linkedICalObjectId FROM relatedto WHERE relatedto.icalObjectId = :parentKey)")
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
    @Query("SELECT * from icalobject WHERE id = :key")
    fun get(key: Long): LiveData<ICalEntity?>


    // This query makes a Join between icalobjects and the linked (child) elements (JOIN relatedto ON icalobject.id = relatedto.linkedICalObjectId ) and then filters for one specific parent element (WHERE relatedto.icalObjectId = :parentKey)
    @Transaction
    @Query("SELECT icalobject.* from icalobject INNER JOIN relatedto ON icalobject.id = relatedto.linkedICalObjectId WHERE relatedto.icalObjectId = :parentKey and icalobject.component = 'NOTE'")
    fun getRelatedNotes(parentKey: Long): LiveData<List<ICalObject?>>

    // This query makes a Join between icalobjects and the linked (child) elements (JOIN relatedto ON icalobject.id = relatedto.linkedICalObjectId ) and then filters for one specific parent element (WHERE relatedto.icalObjectId = :parentKey)
    @Transaction
    @Query("SELECT icalobject.* from icalobject INNER JOIN relatedto ON icalobject.id = relatedto.linkedICalObjectId WHERE relatedto.icalObjectId = :parentKey and icalobject.component = 'TODO'")
    fun getRelatedTodos(parentKey: Long): LiveData<List<ICalObject?>>

    @Transaction
    @Query("SELECT icalobject.* from icalobject INNER JOIN relatedto ON icalobject.id = relatedto.linkedICalObjectId WHERE icalobject.component = 'TODO'")
    fun getAllSubtasks(): LiveData<List<ICalObject?>>

    @Transaction
    @Query("SELECT icalobject.id as icalobjectId, count(*) as count from relatedto INNER JOIN icalobject ON icalobject.id = relatedto.icalObjectId WHERE icalobject.component = 'TODO' GROUP BY icalobjectId")
    fun getSubtasksCount(): LiveData<List<SubtaskCount>>


    @Query("SELECT * from icalobject")
    fun getCursorAllIcalObjects(): Cursor?

}


class SubtaskCount {
    var icalobjectId: Long = 0L
    var count: Int = 0
}


