/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

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


