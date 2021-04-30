/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

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

        @Relation(parentColumn = COLUMN_ICALOBJECT_COLLECTIONID, entityColumn = COLUMN_COLLECTION_ID, entity = at.bitfire.notesx5.database.ICalCollection::class)
        var ICalCollection: ICalCollection? = null

        /*
        @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Contact::class)
        var contact: Contact? = null,

        @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Resource::class)
        var resource: List<Resource>? = null

         */

): Parcelable
{
        fun getICalString(): String  {

                var content = "BEGIN:${this.property.component}\n"
                content+= "UID:${this.property.uid}\n"
                content+= "DTSTAMP:${this.property.dtstamp}\n"
                content+= "DUE;VALUE=DATE:${this.property.due}\n"
                content+= "SUMMARY:${this.property.summary}\n"
                content+= "CLASS:${this.property.classification}\n"
                content+= "STATUS:${this.property.status}"
                content+= "END:${this.property.component}\n"

                return content
        }

}