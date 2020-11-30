package com.example.android.vjournalcalendar.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.lang.reflect.Constructor

@Entity(tableName = "vjournalitems")
data class vJournalItem(

        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,
        var summary: String? = "",
        var description: String = "",
        var dtstamp: Long = System.currentTimeMillis(),
        var dtstart: Long = System.currentTimeMillis(),
        var organizer: String = "",
        var uid: String = "",
        //var attach: String,

        //var categories: ArrayList<String> = arrayListOf(),
        //var class: String,

        //var comment: ArrayList<String> = arrayListOf()
        //  val contact: String,
        var created: Long? = System.currentTimeMillis(),
        var exdate: Long? = System.currentTimeMillis(),
        var lastModified: Long? = System.currentTimeMillis(),
        var rdate: Long? = System.currentTimeMillis()
        //var recurrenceId: Long?,
        //var relatedTo: Long?,
        //var rrule: String?,
        //var sequence: Long?,
        //var status: String?,

        //var url: String?,
        //var ianaProperty: String,
        //var xProperty: String,

        //var vAlarm: String?,
        //var vTimezone: Long?,
        //var ianaComponent: String?,
        //var xComponent: String?
        //
)