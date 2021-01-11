package at.bitfire.notesx5.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.*

data class VJournalEntity (
        @Embedded
        var vJournal: VJournal = VJournal(),


        @Relation(parentColumn = "id", entityColumn = "journalLinkId")
        var comment: List<Comment>? = null,

        @Relation(parentColumn = "id", entityColumn = "journalLinkId", entity = Category::class)
        var category: List<Category>? = null,

        @Relation(parentColumn = "id", entityColumn = "journalLinkId")
        var attendee: List<Attendee>? = null,

        @Relation(parentColumn = "id", entityColumn = "journalLinkId")
        var organizer: Organizer? = null,

        @Relation(parentColumn = "id", entityColumn = "journalLinkId")
        var relatedto: List<Relatedto>? = null


)