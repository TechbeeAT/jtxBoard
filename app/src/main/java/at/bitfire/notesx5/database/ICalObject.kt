package at.bitfire.notesx5.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "icalobject", indices = [Index(value = ["id", "summary", "description"])])
data class ICalObject(

        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,
        var component: String = "NOTE",          // JOURNAL or NOTE
        var collection: String = "LOCAL",
        var summary: String? = null,
        var description: String? = null,
        var dtstart: Long? = null,
        var dtstartTimezone: String? = null,

        var dtend: Long? = null,
        var dtendTimezone: String? = null,


        //var organizer: String = "",
        //var categories: String = "",
        var status: Int = 1,     // 0 = DRAFT, 1 = FINAL, 2 = CANCELLED, -1 = NOT SUPPORTED (value in statusX)
        var statusX: String? = null,
        var classification: Int = 0,    // 0 = PUBLIC, 1 = PRIVATE, 2 = CONFIDENTIAL, -1 = NOT SUPPORTED (value in classificationX)
        var classificationX: String? = null,

        //var attach: String,
        var url: String? = null,
        //var attendee: String = "",
        var contact: String? = null,
        //var related: String = "",
        var geoLat: Float? = null,
        var geoLong: Float? = null,
        var location: String? = null,

        var percent: Int? = null,    // VTODO only!
        var priority: Int? = null,   // VTODO and VEVENT

        var due: Long? = null,      // VTODO only!
        var completed: Long? = null, // VTODO only!


        var uid: String = "${System.currentTimeMillis()}-${UUID.randomUUID()}@at.bitfire.notesx5",                              //unique identifier, see https://tools.ietf.org/html/rfc5545#section-3.8.4.7

        /*
         The following properties specify change management information in  calendar components.
         https://tools.ietf.org/html/rfc5545#section-3.8.7
         */
        var created: Long = System.currentTimeMillis(),   // see https://tools.ietf.org/html/rfc5545#section-3.8.7.1
        var dtstamp: Long = System.currentTimeMillis(),   // see https://tools.ietf.org/html/rfc5545#section-3.8.7.2
        var lastModified: Long = System.currentTimeMillis(), // see https://tools.ietf.org/html/rfc5545#section-3.8.7.3
        var sequence: Long = 0,                             // increase on every change (+1), see https://tools.ietf.org/html/rfc5545#section-3.8.7.4

        //var exdate: Long? = System.currentTimeMillis(),   //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5.1
        //var rdate: Long? = System.currentTimeMillis()     //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5.2
        //var recurrenceId: String? = null,                          //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5
        //var rrule: String?,                               //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5.3


        //var ianaProperty: String,
        //var xProperty: String,

        //var vAlarm: String?,
        //var vTimezone: Long?,
        //var ianaComponent: String?,
        //var xComponent: String?

        val color: String

): Parcelable



