package at.bitfire.notesx5.database.relations


import androidx.room.Embedded
import androidx.room.Relation
import at.bitfire.notesx5.database.*

data class VJournalWithCategory (
        @Embedded
        var vJournal: VJournal = VJournal(),

        @Relation(parentColumn = "id", entityColumn = "journalLinkId", entity = VCategory::class)
        var vCategory: List<VCategory>? = null
)