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
import at.bitfire.notesx5.convertLongToICalDateTime
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.*
import kotlinx.parcelize.Parcelize

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

                var content = "BEGIN:VCALENDAR\n" +
                        "VERSION:2.0\n" +
                        "PRODID:-//bitfire.at//NOTESx5 v1.0//EN\n"
                content+= "BEGIN:${this.property.component}\n"
                content+= "UID:${this.property.uid}\n"
                content+= "DTSTAMP:${convertLongToICalDateTime(this.property.dtstamp, null)}\n"
                if(this.property.component == Component.VTODO.name && this.property.due != null)
                        content+= "DUE;VALUE=DATE:${convertLongToICalDateTime(this.property.due, this.property.dueTimezone)}\n"
                content+= "SUMMARY:${this.property.summary}\n"
                if (this.property.description?.isNotEmpty() == true) { content+= "DESCRIPTION:${this.property.description}\n"  }
                if (this.property.dtstart != null)  { content+= "DTSTART:${convertLongToICalDateTime(this.property.dtstart , this.property.dtstartTimezone)}\n" }
                if (this.property.dtend != null)    { content+= "DTEND:${convertLongToICalDateTime(this.property.dtend, this.property.dtendTimezone)}\n" }
                content+= "CLASS:${this.property.classification}\n"
                content+= "STATUS:${this.property.status}\n"
                if (this.property.url?.isNotEmpty() == true) { content+= "URL:${this.property.url}\n"}
                if (this.property.contact?.isNotEmpty() == true)  { content+= "CONTACT:${this.property.contact}\n"}
                if(this.property.geoLat != null && this.property.geoLong != null)
                        content+= "GEO:${this.property.geoLat};${this.property.geoLong}\n"
                if (this.property.location?.isNotEmpty() == true) { content+= "LOCATION:${this.property.location}\n"}
                if (this.property.percent != null) { content+= "PERCENT-COMPLETE:${this.property.percent}\n"}
                if (this.property.priority != null) { content+= "PRIORITY:${this.property.priority}\n"}
                if(this.property.component == Component.VTODO.name && this.property.completed != null)
                        content+= "COMPLETED:${convertLongToICalDateTime(this.property.completed, this.property.completedTimezone)}\n"
                if(this.property.component == Component.VTODO.name && this.property.duration?.isNotEmpty() == true)
                        content+= "DURATION:${this.property.duration}\n"
                content+= "CREATED:${convertLongToICalDateTime(this.property.lastModified, null)}\n"
                content+= "LAST-MODIFIED:${convertLongToICalDateTime(this.property.lastModified, null)}\n"
                content+= "SEQUENCE:${this.property.sequence}\n"
                if (this.property.color != null)  { content+= "COLOR:${this.property.color}\n" }
                //this.property.other.let {content+= "OTHER:$it\n"}
                content+= "END:${this.property.component}\n"

                return content

        }
}