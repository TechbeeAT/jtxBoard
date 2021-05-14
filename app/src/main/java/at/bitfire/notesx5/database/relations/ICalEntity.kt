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

        @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Resource::class)
        var resource: List<Resource>? = null,

        @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Attachment::class)
        var attachment: List<Attachment>? = null,


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

                var content = property.getICalStringHead()
                content += property.getICalStringBody()

                attendee?.forEach {  content += it.getICalString() }

                if(category?.isNotEmpty() == true) {
                        content += "CATEGORIES:"
                        category?.forEach { content+= "${it.text}," }
                        content += "\r\n"
                }

                comment?.forEach { content += it.getICalString() }
                //contact?.forEach { content += it.getICalString() }

                if(organizer != null)
                        content += organizer?.getICalString()
                relatedto?.forEach { content += it.getICalString() }

                if(resource?.isNotEmpty() == true) {
                        content += "RESOURCES:"
                        resource?.forEach { content+= "${it.text}," }
                        content += "\r\n"
                }

                attachment?.forEach {  content += it.getICalString() }

                content += property.getICalStringEnd()

                // replace(...) replaces every 75 characters with itself PLUS a line break and a space
                // this is needed according to the ICAL specification
                // see https://stackoverflow.com/questions/537174/putting-char-into-a-java-string-for-each-n-characters for technique
                // see https://icalendar.org/iCalendar-RFC-5545/3-1-content-lines.html for content line restrictions
                content = content.replace("(.{74})".toRegex(), "$1\r\n ")

                return content

        }
}