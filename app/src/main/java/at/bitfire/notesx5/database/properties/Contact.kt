package at.bitfire.notesx5.database.properties


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import at.bitfire.notesx5.database.ICalObject
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "contact",
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("icalObjectId"),
                onDelete = ForeignKey.CASCADE)],
        indices = [Index(value = ["contactId"]),
                Index(value = ["icalObjectId"])])
data class Contact (

        @PrimaryKey(autoGenerate = true)
        var contactId: Long = 0L,
        var icalObjectId: Long = 0L,
        var text: String? = null,
        var languageparam: String? = null,
        var otherparam: String? = null
): Parcelable

