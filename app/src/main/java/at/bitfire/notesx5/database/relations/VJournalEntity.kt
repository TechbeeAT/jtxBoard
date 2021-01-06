package at.bitfire.notesx5.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import at.bitfire.notesx5.database.*

data class VJournalEntity (
        @Embedded
        var vJournal: VJournal = VJournal(),


        @Relation(parentColumn = "id", entityColumn = "journalLinkId")
        var vComment: List<VComment>?,

        @Relation(parentColumn = "id", entityColumn = "journalLinkId", entity = VCategory::class)
        var vCategory: List<VCategory>? = null,

        @Relation(parentColumn = "id", entityColumn = "journalLinkId")
        var vAttendee: List<VAttendee>?,

        @Relation(parentColumn = "id", entityColumn = "journalLinkId")
        var vOrganizer: List<VOrganizer>?,

        @Relation(parentColumn = "id", entityColumn = "journalLinkId")
        var vRelatedto: List<VRelatedto>?


)