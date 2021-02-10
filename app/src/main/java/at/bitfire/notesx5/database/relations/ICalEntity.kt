package at.bitfire.notesx5.database.relations

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ICalEntity (
        @Embedded
        var property: ICalObject = ICalObject(),


        @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Comment::class)
        var comment: List<Comment>? = null,

        @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Category::class)
        var category: List<Category>? = null,

        @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Attendee::class)
        var attendee: List<Attendee>? = null,

        @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Organizer::class)
        var organizer: Organizer? = null,

        @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Relatedto::class)
        var relatedto: List<Relatedto>? = null,

        /*
        @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Contact::class)
        var contact: Contact? = null,

        @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Resource::class)
        var resource: List<Resource>? = null

         */

): Parcelable