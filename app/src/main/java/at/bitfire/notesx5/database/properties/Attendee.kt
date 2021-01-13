package at.bitfire.notesx5.database.properties

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import at.bitfire.notesx5.database.ICalObject

@Entity(tableName = "attendee",
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("icalLinkId"),
                onDelete = ForeignKey.CASCADE)])
data class Attendee (

        @PrimaryKey(autoGenerate = true)         // TODO Doublecheck ALL types here, crosscheck with RFC 5545
        var attendeeId: Long = 0L,
        var icalLinkId: Long = 0L,
        var caladdress: String = "",
        var cutypeparam: String = "INDIVIDUAL",
        var memberparam: String? = null,
        var roleparam: String? = null,
        var partstatparam: String? = null,
        var rsvpparam: String? = null,
        var deltoparam: String? = null,
        var delfromparam: String? = null,
        var sentbyparam: String? = null,
        var cnparam: String? = null,
        var dirparam: String? = null,
        var languageparam: String? = null,
        var otherparam: String? = null

)