package com.example.android.vjournalcalendar.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.lang.reflect.Constructor
import java.text.DateFormat
import java.text.DateFormat.getDateInstance
import java.text.DateFormat.getTimeInstance
import java.util.*

@Entity(tableName = "vjournalitems")
data class vJournalItem(

        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,
        var summary: String? = "",
        var description: String = "",
        var dtstart: Long = System.currentTimeMillis(),
        var organizer: String = "",
        var uid: String = "",                              //unique identifier, see https://tools.ietf.org/html/rfc5545#section-3.8.4.7
        //var attach: String,

        var categories: String = "", //make it an array or another structure?
        //var class: String,

        //var comment: ArrayList<String> = arrayListOf()
        //  val contact: String,

        /*
         The following properties specify change management information in  calendar components.
         https://tools.ietf.org/html/rfc5545#section-3.8.7
         */
        var created: Long = System.currentTimeMillis(),   // see https://tools.ietf.org/html/rfc5545#section-3.8.7.1
        var dtstamp: Long = System.currentTimeMillis(),   // see https://tools.ietf.org/html/rfc5545#section-3.8.7.2
        var lastModified: Long = System.currentTimeMillis(), // see https://tools.ietf.org/html/rfc5545#section-3.8.7.3
        var sequence: Long = 0                               // increase on every change (+1), see https://tools.ietf.org/html/rfc5545#section-3.8.7.4

        //var exdate: Long? = System.currentTimeMillis(),   //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5.1
        //var rdate: Long? = System.currentTimeMillis()     //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5.2
        //var recurrenceId: Long?,                          //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5
        //var rrule: String?,                               //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5.3

        //var relatedTo: Long?,
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
