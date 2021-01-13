package at.bitfire.notesx5.database.properties


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import at.bitfire.notesx5.database.ICalObject


@Entity(tableName = "organizer",
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("icalLinkId"),
                onDelete = ForeignKey.CASCADE)])
data class Organizer (

        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,
        var icalLinkId: Long = 0L,
        var caladdress: String = "",
        var cnparam: String? = null,
        var dirparam: String? = null,
        var sentbyparam: String? = null,
        var languageparam: String? = null,
        var otherparam: String? = null
)
