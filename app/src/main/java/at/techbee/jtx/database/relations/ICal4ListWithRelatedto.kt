/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.relations


import androidx.room.Embedded
import androidx.room.Relation
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.COLUMN_ATTACHMENT_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_RELATEDTO_ICALOBJECT_ID
import at.techbee.jtx.database.properties.Relatedto
import at.techbee.jtx.database.views.ICal4List


data class ICal4ListWithRelatedto (
        @Embedded
        var property: ICal4List,

        @Relation(parentColumn = COLUMN_ID, entityColumn = COLUMN_RELATEDTO_ICALOBJECT_ID, entity = Relatedto::class)
        var relatedto: List<Relatedto>? = null,

        @Relation(parentColumn = COLUMN_ID, entityColumn = COLUMN_ATTACHMENT_ICALOBJECT_ID, entity = Attachment::class)
        var attachment: List<Attachment>? = null

)


