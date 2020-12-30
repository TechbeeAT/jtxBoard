package at.bitfire.notesx5.database


import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity(tableName = "vorganizer")
data class VOrganizer (

        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,
        var journalLinkId: Long = 0L,
        var organizer: String = "",
        var cnparam: String = "",
        var dirparam: String = "",
        var sentbyparam: String = "",
        var languageparam: String = "",
        var otherparam: String = "",
)
