package at.bitfire.notesx5.database.properties

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import at.bitfire.notesx5.database.ICalObject

@Entity(tableName = "comment",
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("icalLinkId"),
                onDelete = ForeignKey.CASCADE)])
data class Comment (

    @PrimaryKey(autoGenerate = true)
    var commentId: Long = 0L,
    var icalLinkId: Long = 0L,
    var text: String = "",
    var altrepparam: String? = null,
    var languageparam: String? = null
)



