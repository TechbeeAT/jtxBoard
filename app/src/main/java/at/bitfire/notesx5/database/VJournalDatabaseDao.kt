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

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Defines methods for using the SleepNight class with Room.
 */
@Dao
interface VJournalDatabaseDao {

    /*
    @Query("SELECT * from vjournalitems WHERE id = :key")
    fun get(key: Long): LiveData<VJournalItem?>


     */

    @Query("SELECT * FROM vjournals ORDER BY dtstart DESC, created DESC")
    fun getVJournalItems(): LiveData<List<VJournal>>


    @Query("SELECT * FROM vjournals WHERE component LIKE :component AND (categories LIKE :search_global OR summary LIKE :search_global OR description LIKE :search_global OR organizer LIKE :search_global OR status LIKE :search_global)  AND categories LIKE :search_category AND organizer LIKE :search_organizer AND status LIKE :search_status AND classification LIKE :search_classification ORDER BY dtstart DESC, created DESC")
    fun getVJournalItems(component: Array<String>, search_global: String, search_category: Array<String>, search_organizer: Array<String>, search_status: Array<String>, search_classification: Array<String>): LiveData<List<VJournal>>


    @Query("SELECT DISTINCT categories FROM vjournals ORDER BY categories ASC")
    fun getAllCategories(): LiveData<List<String>>

    @Query("SELECT DISTINCT organizer FROM vjournals ORDER BY organizer ASC")
    fun getAllOrganizers(): LiveData<List<String>>

    @Query("SELECT DISTINCT collection FROM vjournals ORDER BY collection ASC")
    fun getAllCollections(): LiveData<List<String>>



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vJournalItem: VJournal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendee(vAttendee: VAttendee): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(vCategory: VCategory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(vComment: VComment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganizer(vOrganizer: VOrganizer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelatedto(vRelatedto: VRelatedto): Long



    /*
   @Insert(onConflict = OnConflictStrategy.REPLACE)
   suspend fun insert(vJournalItem: vJournalItem, VAttendee: List<VAttendee>, vCategory: List<vCategory>, vComment: List<vComment>, vOrganizer: vOrganizer, vRelatedto: List<vRelatedto>): Long


    */

    @Update
    suspend fun update(vJournal: VJournal)



        // TODO Take care to delete also child elements!
    @Delete
    fun delete(vJournal: VJournal)


    //@Transaction
    @Query ("SELECT * FROM vjournals")
    fun getVJournalItemWithEverything(): LiveData<List<VJournalWithEverything>>

    @Query("SELECT * from vjournals WHERE id = :key")
    fun get(key: Long): LiveData<VJournalWithEverything?>


}

