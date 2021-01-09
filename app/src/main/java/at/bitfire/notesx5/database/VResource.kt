package at.bitfire.notesx5.database


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "vresource",
        foreignKeys = [ForeignKey(entity = VJournal::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("journalLinkId"),
                onDelete = ForeignKey.CASCADE)])
data class VResource (

        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,
        var journalLinkId: Long = 0L,
        var text: String = "",
        var reltypeparam: String = "",
        var otherparam: String = ""
)



