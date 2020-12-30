package at.bitfire.notesx5.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "vcomments")
data class VComment (

    @PrimaryKey(autoGenerate = true)
    var commentId: Long = 0L,
    var journalLinkId: Long = 0L,
    var comment: String = "",
    var altrepparam: String = "",
    var languageparam: String = ""
)



