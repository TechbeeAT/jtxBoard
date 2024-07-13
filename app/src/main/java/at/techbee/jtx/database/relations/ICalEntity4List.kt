/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.relations

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import at.techbee.jtx.database.COLUMN_COLLECTION_ID
import at.techbee.jtx.database.COLUMN_ICALOBJECT_COLLECTIONID
import at.techbee.jtx.database.COLUMN_ID
import at.techbee.jtx.database.COLUMN_UID
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Attendee
import at.techbee.jtx.database.properties.COLUMN_ALARM_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_ALARM_TRIGGER_TIME
import at.techbee.jtx.database.properties.COLUMN_ATTACHMENT_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_ATTENDEE_CALADDRESS
import at.techbee.jtx.database.properties.COLUMN_ATTENDEE_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_CATEGORY_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_CATEGORY_TEXT
import at.techbee.jtx.database.properties.COLUMN_COMMENT_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_COMMENT_TEXT
import at.techbee.jtx.database.properties.COLUMN_RELATEDTO_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_RELATEDTO_TEXT
import at.techbee.jtx.database.properties.COLUMN_RESOURCE_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_RESOURCE_TEXT
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.properties.Comment
import at.techbee.jtx.database.properties.Relatedto
import at.techbee.jtx.database.properties.Resource
import kotlinx.parcelize.Parcelize


@Parcelize
data class ICalEntity4List(
    @Embedded
    var property: ICalObject = ICalObject(),

    @Relation(parentColumn = COLUMN_ID, entityColumn = COLUMN_COMMENT_ICALOBJECT_ID, entity = Comment::class, projection = [COLUMN_COMMENT_TEXT])
    var comments: List<String> = emptyList(),

    @Relation(parentColumn = COLUMN_ID, entityColumn = COLUMN_CATEGORY_ICALOBJECT_ID, entity = Category::class, projection = [COLUMN_CATEGORY_TEXT])
    var categories: List<String> = emptyList(),

        @Relation(parentColumn = COLUMN_ID, entityColumn = COLUMN_ATTENDEE_ICALOBJECT_ID, entity = Attendee::class, projection = [COLUMN_ATTENDEE_CALADDRESS])
        var attendees: List<String> = emptyList(),

        /*
        @Relation(parentColumn = COLUMN_ID, entityColumn = COLUMN_ORGANIZER_ICALOBJECT_ID, entity = Organizer::class)
        var organizer: String? = null,
         */

        @Relation(parentColumn = COLUMN_ID, entityColumn = COLUMN_RELATEDTO_ICALOBJECT_ID, entity = Relatedto::class, projection = [COLUMN_RELATEDTO_TEXT])
        var parents: List<String> = emptyList(),

          @Relation(parentColumn = COLUMN_UID, entityColumn = COLUMN_RELATEDTO_TEXT, entity = Relatedto::class, projection = [COLUMN_RELATEDTO_ICALOBJECT_ID])
          var children: List<Long> = emptyList(),

          @Relation(parentColumn = COLUMN_ID, entityColumn = COLUMN_RESOURCE_ICALOBJECT_ID, entity = Resource::class, projection = [COLUMN_RESOURCE_TEXT])
          var resources: List<String?> = emptyList(),

          @Relation(parentColumn = COLUMN_ID, entityColumn = COLUMN_ATTACHMENT_ICALOBJECT_ID, entity = Attachment::class)
          var attachments: List<Attachment> = emptyList(),

          @Relation(parentColumn = COLUMN_ID, entityColumn = COLUMN_ALARM_ICALOBJECT_ID, entity = Alarm::class, projection = [COLUMN_ALARM_TRIGGER_TIME])
          var alarms: List<Long> = emptyList(),


          @Relation(
              parentColumn = COLUMN_ICALOBJECT_COLLECTIONID,
              entityColumn = COLUMN_COLLECTION_ID,
              entity = ICalCollection::class
          )
          var iCalCollection: ICalCollection
    /*
             /*
             @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Contact::class)
             var contact: Contact? = null,
              */

          */

): Parcelable