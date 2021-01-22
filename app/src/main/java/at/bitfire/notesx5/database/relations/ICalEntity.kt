package at.bitfire.notesx5.database.relations

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.Transaction
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ICalEntity (
        @Embedded
        var property: ICalObject = ICalObject(),


        @Relation(parentColumn = "id", entityColumn = "icalObjectId")
        var comment: List<Comment>? = null,

        @Relation(parentColumn = "id", entityColumn = "icalObjectId", entity = Category::class)
        var category: List<Category>? = null,

        @Relation(parentColumn = "id", entityColumn = "icalObjectId")
        var attendee: List<Attendee>? = null,

        @Relation(parentColumn = "id", entityColumn = "icalObjectId")
        var organizer: Organizer? = null,

        @Relation(parentColumn = "id", entityColumn = "icalObjectId")
        var relatedto: List<Relatedto>? = null


): Parcelable