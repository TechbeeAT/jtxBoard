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

package com.example.android.vjournalcalendar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.vjournalcalendar.database.vJournalItem
import com.example.android.vjournalcalendar.ui.VJournalListAdapter


/**
 * This is the toy app for lesson 6 of the
 * Android App Development in Kotlin course on Udacity(https://www.udacity.com/course/???).
 *
 * The SleepQualityTracker app is a demo app that helps you collect information about your sleep.
 * - Start time, end time, quality, and time slept
 *
 * This app demonstrates the following views and techniques:
 * - Room database, DAO, and Coroutines
 *
 * It also uses and builds on the following techniques from previous lessons:
 * - Transformation map
 * - Data Binding in XML files
 * - ViewModel Factory
 * - Using Backing Properties to protect MutableLiveData
 * - Observable state LiveData variables to trigger navigation
 */

/**
 * This main activity is just a container for our fragments,
 * where the real action is.
 */
class MainActivity : AppCompatActivity() {

    private var recyclerView: RecyclerView? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var vJournalList: MutableList<vJournalItem>? = ArrayList()
    private var vJournalListAdapter: VJournalListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)


        recyclerView = findViewById(R.id.vjournal_list_items_recycler_view)
        linearLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView?.layoutManager = linearLayoutManager

        recyclerView?.setHasFixedSize(true)

        vJournalList?.add(vJournalItem(1, "desc1", System.currentTimeMillis(),"comm"))
        vJournalList?.add(vJournalItem(2, "desc2", System.currentTimeMillis(),"comm"))
        vJournalList?.add(vJournalItem(3, "desc3", System.currentTimeMillis(),"comm"))
        vJournalList?.add(vJournalItem(4, "desc4", System.currentTimeMillis(),"comm"))

        vJournalListAdapter = vJournalList?.let { VJournalListAdapter(applicationContext, it) }
        recyclerView?.adapter = vJournalListAdapter




    }
}
