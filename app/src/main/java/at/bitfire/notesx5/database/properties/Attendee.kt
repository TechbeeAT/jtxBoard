package at.bitfire.notesx5.database.properties

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import at.bitfire.notesx5.database.ICalObject
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "attendee",
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("icalObjectId"),
                onDelete = ForeignKey.CASCADE)],
        indices = [Index(value = ["attendeeId"]),
                Index(value = ["icalObjectId"])]
)
data class Attendee (

        @PrimaryKey(autoGenerate = true)         // TODO Doublecheck ALL types here, crosscheck with RFC 5545
        var attendeeId: Long = 0L,
        var icalObjectId: Long = 0L,
        var caladdress: String = "",
        var cutypeparam: String = "INDIVIDUAL",
        var memberparam: String? = null,
        var roleparam: Int? = 1,
        var roleparamX: String? = null,
        var partstatparam: String? = null,
        var rsvpparam: String? = null,
        var deltoparam: String? = null,
        var delfromparam: String? = null,
        var sentbyparam: String? = null,
        var cnparam: String? = null,
        var dirparam: String? = null,
        var languageparam: String? = null,
        var otherparam: String? = null

): Parcelable