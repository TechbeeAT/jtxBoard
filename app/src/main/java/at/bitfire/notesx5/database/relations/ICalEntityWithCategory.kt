package at.bitfire.notesx5.database.relations


import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.Category
import at.bitfire.notesx5.database.properties.Relatedto
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ICalEntityWithCategory (
        @Embedded
        var property: ICalObject = ICalObject(),

        @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Category::class)
        var category: List<Category>? = null,

        @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Relatedto::class)
        var relatedto: List<Relatedto>? = null,

): Parcelable