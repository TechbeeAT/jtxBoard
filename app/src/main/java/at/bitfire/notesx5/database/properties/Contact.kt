package at.bitfire.notesx5.database.properties


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import at.bitfire.notesx5.database.VJournal

@Entity(tableName = "contact",
        foreignKeys = [ForeignKey(entity = VJournal::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("journalLinkId"),
                onDelete = ForeignKey.CASCADE)])
data class Contact (

        @PrimaryKey(autoGenerate = true)
        var contactId: Long = 0L,
        var journalLinkId: Long = 0L,
        var text: String = "",
        var languageparam: String = "",
        var otherparam: String = "",
)

