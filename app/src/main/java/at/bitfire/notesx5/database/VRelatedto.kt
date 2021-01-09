package at.bitfire.notesx5.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "vrelatedto",
        foreignKeys = [ForeignKey(entity = VJournal::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("journalLinkId"),
                onDelete = ForeignKey.CASCADE)])
data class VRelatedto (

        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,
        var journalLinkId: Long = 0L,
        var relatedto: String = "",
        var reltypeparam: String = "",
        var otherparam: String = ""
)

