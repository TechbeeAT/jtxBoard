package at.bitfire.notesx5.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.*

data class ICalEntity (
        @Embedded
        var vJournal: ICalObject = ICalObject(),


        @Relation(parentColumn = "id", entityColumn = "icalLinkId")
        var comment: List<Comment>? = null,

        @Relation(parentColumn = "id", entityColumn = "icalLinkId", entity = Category::class)
        var category: List<Category>? = null,

        @Relation(parentColumn = "id", entityColumn = "icalLinkId")
        var attendee: List<Attendee>? = null,

        @Relation(parentColumn = "id", entityColumn = "icalLinkId")
        var organizer: Organizer? = null,

        @Relation(parentColumn = "id", entityColumn = "icalLinkId")
        var relatedto: List<Relatedto>? = null


)