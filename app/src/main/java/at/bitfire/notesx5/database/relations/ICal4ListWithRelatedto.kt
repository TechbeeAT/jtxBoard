package at.bitfire.notesx5.database.relations


import androidx.room.Embedded
import androidx.room.Relation
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.COLUMN_RELATEDTO_ICALOBJECT_ID
import at.bitfire.notesx5.database.properties.Relatedto
import at.bitfire.notesx5.database.views.ICal4List


data class ICal4ListWithRelatedto (
        @Embedded
        var property: ICal4List,

        @Relation(parentColumn = COLUMN_ID, entityColumn = COLUMN_RELATEDTO_ICALOBJECT_ID, entity = Relatedto::class)
        var relatedto: List<Relatedto>? = null,

)


