package at.bitfire.notesx5.database.properties

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import at.bitfire.notesx5.database.VJournal

@Entity(tableName = "comment",
        foreignKeys = [ForeignKey(entity = VJournal::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("journalLinkId"),
                onDelete = ForeignKey.CASCADE)])
data class Comment (

    @PrimaryKey(autoGenerate = true)
    var commentId: Long = 0L,
    var journalLinkId: Long = 0L,
    var text: String = "",
    var altrepparam: String = "",
    var languageparam: String = ""
)



