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
import at.techbee.jtx.database.COLUMN_ID
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.views.ICal4List


data class ICal4ListRel(
    @Embedded
    var iCal4List: ICal4List,

    @Relation(parentColumn = COLUMN_ID, entityColumn = COLUMN_RELATEDTO_ICALOBJECT_ID, entity = Relatedto::class)
    var relatedto: List<Relatedto>,

    @Relation(parentColumn = COLUMN_ID, entityColumn = COLUMN_CATEGORY_ICALOBJECT_ID, entity = Category::class)
    var categories: List<Category>,

    @Relation(parentColumn = COLUMN_ID, entityColumn = COLUMN_RESOURCE_ICALOBJECT_ID, entity = Resource::class)
    var resources: List<Resource>
    )