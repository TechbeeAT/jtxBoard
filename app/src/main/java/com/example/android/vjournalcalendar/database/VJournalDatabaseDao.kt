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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Defines methods for using the SleepNight class with Room.
 */
@Dao
interface VJournalDatabaseDao {

    @Query("SELECT * FROM vjournalitems LIMIT 1")
    suspend fun getTestVJournalFromDatabase(): vJournalItem?

    @Query("SELECT * from vjournalitems WHERE id = :key")
    suspend fun get(key: Long): vJournalItem?

    @Query("SELECT * FROM vjournalitems ORDER BY id DESC")
    suspend fun getAllVJournalItems(): List<vJournalItem>

    @Insert
    suspend fun insert(vJournalItem: vJournalItem): Long

    @Update
    suspend fun update(vJournalItem: vJournalItem)

}

