package at.bitfire.notesx5.database.properties

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import at.bitfire.notesx5.database.ICalObject

@Entity(tableName = "category",
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("icalLinkId"),
                onDelete = ForeignKey.CASCADE)],
        indices = [Index(value = ["categoryId", "icalLinkId"]),
                Index(value = ["categoryId"]),
                Index(value = ["text"])])

data class Category (

        @PrimaryKey(autoGenerate = true)
        var categoryId: Long = 0L,
        var icalLinkId: Long = 0L,
        var text: String = "",
        var languageparam: String? = null,
        var otherparam: String? = null
)

