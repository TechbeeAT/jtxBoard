package at.bitfire.notesx5.database.relations


import androidx.room.Embedded
import androidx.room.Relation
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.Category

data class ICalEntityWithCategory (
        @Embedded
        var vJournal: ICalObject = ICalObject(),

        @Relation(parentColumn = "id", entityColumn = "icalObjectId", entity = Category::class)
        var category: List<Category>? = null
)