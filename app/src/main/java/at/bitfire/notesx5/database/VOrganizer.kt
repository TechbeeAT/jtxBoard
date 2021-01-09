package at.bitfire.notesx5.database


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey



@Entity(tableName = "vorganizer",
        foreignKeys = [ForeignKey(entity = VJournal::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("journalLinkId"),
                onDelete = ForeignKey.CASCADE)])
data class VOrganizer (

        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,
        var journalLinkId: Long = 0L,
        var caladdress: String = "",
        var cnparam: String = "",
        var dirparam: String = "",
        var sentbyparam: String = "",
        var languageparam: String = "",
        var otherparam: String = "",
)
