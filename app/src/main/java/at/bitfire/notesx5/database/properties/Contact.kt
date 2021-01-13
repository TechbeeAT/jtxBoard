package at.bitfire.notesx5.database.properties


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import at.bitfire.notesx5.database.ICalObject

@Entity(tableName = "contact",
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("icalLinkId"),
                onDelete = ForeignKey.CASCADE)])
data class Contact (

        @PrimaryKey(autoGenerate = true)
        var contactId: Long = 0L,
        var icalLinkId: Long = 0L,
        var text: String? = null,
        var languageparam: String? = null,
        var otherparam: String? = null
)

