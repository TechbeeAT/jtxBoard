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

package com.example.android.vjournalcalendar.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*

/**
 * Defines methods for using the SleepNight class with Room.
 */
@Dao
interface VJournalDatabaseDao {

    @Query("SELECT * from vjournalitems WHERE id = :key")
    fun get(key: Long): LiveData<vJournalItem?>


    @Query("SELECT * FROM vjournalitems ORDER BY dtstart DESC, created DESC")
    fun getVJournalItems(): LiveData<List<vJournalItem>>


    @Query("SELECT * FROM vjournalitems WHERE component = :component AND (categories LIKE :search_global OR summary LIKE :search_global OR description LIKE :search_global OR organizer LIKE :search_global OR status LIKE :search_global)  AND categories LIKE :search_category AND organizer LIKE :search_organizer AND status LIKE :search_status AND classification LIKE :search_classification ORDER BY dtstart DESC, created DESC")
    fun getVJournalItems(component: String, search_global: String, search_category: String, search_organizer: String, search_status: String, search_classification: String): LiveData<List<vJournalItem>>


    @Query("SELECT DISTINCT categories FROM vjournalitems ORDER BY categories ASC")
    fun getAllCategories(): LiveData<List<String>>

    @Query("SELECT DISTINCT organizer FROM vjournalitems ORDER BY organizer ASC")
    fun getAllOrganizers(): LiveData<List<String>>


    @Insert
    suspend fun insert(vJournalItem: vJournalItem): Long

    @Update
    suspend fun update(vJournalItem: vJournalItem)

    @Delete
    fun delete(vJournalItem: vJournalItem)

}

