package at.bitfire.notesx5.database

import androidx.room.Embedded
import androidx.room.Relation

data class VJournalWithEverything (
        @Embedded
        var vJournalItem: VJournal = VJournal(),

        @Relation(parentColumn = "id", entityColumn = "journalLinkId")
        var vComment: List<VComment>?,

        @Relation(parentColumn = "id", entityColumn = "journalLinkId")
        var vCategory: List<VCategory>?,

        @Relation(parentColumn = "id", entityColumn = "journalLinkId")
        var vAttendee: List<VAttendee>?,

        @Relation(parentColumn = "id", entityColumn = "journalLinkId")
        var vOrganizer: List<VOrganizer>?,

        @Relation(parentColumn = "id", entityColumn = "journalLinkId")
        var vRelatedto: List<VRelatedto>?


)