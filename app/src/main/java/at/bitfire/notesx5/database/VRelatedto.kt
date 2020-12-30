package at.bitfire.notesx5.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "vRelatedto")
data class VRelatedto (

        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,
        var journalLinkId: Long = 0L,
        var relatedto: String = "",
        var reltypeparam: String = "",
        var otherparam: String = ""
)

