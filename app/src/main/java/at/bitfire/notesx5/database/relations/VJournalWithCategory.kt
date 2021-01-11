package at.bitfire.notesx5.database.relations


import androidx.room.Embedded
import androidx.room.Relation
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.Category

data class VJournalWithCategory (
        @Embedded
        var vJournal: VJournal = VJournal(),

        @Relation(parentColumn = "id", entityColumn = "journalLinkId", entity = Category::class)
        var category: List<Category>? = null
)