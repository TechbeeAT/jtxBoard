package at.bitfire.notesx5.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "vcategories",
        foreignKeys = [ForeignKey(entity = VJournal::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("journalLinkId"),
                onDelete = ForeignKey.CASCADE)])
data class VCategory (

        @PrimaryKey(autoGenerate = true)
        var categoryId: Long = 0L,
        var journalLinkId: Long = 0L,
        var categories: String = "",
        var languageparam: String = "",
        var otherparam: String = "",
)

