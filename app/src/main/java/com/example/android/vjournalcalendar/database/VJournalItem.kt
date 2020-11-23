package com.example.android.vjournalcalendar.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vjournalitems")
data class vJournalItem(

        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,

        var description: String = "",
        //var dtstamp: Long,
        //var dtstart: Long,
        //var organizer: String,
        //var uid: Long,
        //var attach: String,
        //var categories: String,
        //var class: String,
        var comment: String = ""
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