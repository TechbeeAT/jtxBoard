package at.bitfire.notesx5.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "vattendees",
        foreignKeys = [ForeignKey(entity = VJournal::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("journalLinkId"),
                onDelete = ForeignKey.CASCADE)])
data class VAttendee (

        @PrimaryKey(autoGenerate = true)         // TODO Doublecheck ALL types here, crosscheck with RFC 5545
        var attendeeId: Long = 0L,
        var journalLinkId: Long = 0L,
        var attendee: String = "",
        var cutypeparam: String = "INDIVIDUAL",
        var memberparam: String = "",
        var roleparam: String = "",
        var partstatparam: String = "",
        var rsvpparam: String = "",
        var deltoparam: String = "",
        var delfromparam: String = "",
        var sentbyparam: String = "",
        var cnparam: String = "",
        var dirparam: String = "",
        var languageparam: String = "",
        var otherparam: String = ""

)