package com.example.android.vjournalcalendar.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "vjournalitems")

data class vJournalItem(

        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,

        var description: String = "",
        var dtstamp: Long = 0L,
        var dtstart: Long = 0L,
        var organizer: String,
        var uid: String,
        //var attach: String,

        //var categories: ArrayList<String> = arrayListOf(),
        //var class: String,

        //var comment: ArrayList<String> = arrayListOf()
/*        val contact: String,
        var created: Long?,
        var exdate: Long?,
        var lastModified: Long?,
        var rdate: Long?,
        var recurrenceId: Long?,
        var relatedTo: Long?,
        var rrule: String?,
        var sequence: Long?,
        var status: String?,
        var summary: String?,
        var url: String?,
        var ianaProperty: String,
        var xProperty: String,

        var vAlarm: String?,
        var vTimezone: Long?,
        var ianaComponent: String?,
        var xComponent: String?
*/


)