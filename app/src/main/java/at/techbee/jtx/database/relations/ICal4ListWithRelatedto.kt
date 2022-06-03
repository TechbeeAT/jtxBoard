/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.relations


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
{

        companion object {
                fun getSample() =
                        ICal4ListWithRelatedto(
                                property = ICal4List(
                                id = 1L,
                                module = Module.JOURNAL.name,
                                component = Component.VJOURNAL.name,
                                "My Summary",
                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur tellus risus, tristique ac elit vitae, mollis feugiat quam. Duis aliquet arcu at purus porttitor ultricies. Vivamus sagittis feugiat ex eu efficitur. Aliquam nec cursus ante, a varius nisi. In a malesuada urna, in rhoncus est. Maecenas auctor molestie quam, quis lobortis tortor sollicitudin sagittis. Curabitur sit amet est varius urna mattis interdum.\n" +
                                        "\n" +
                                        "Phasellus id quam vel enim semper ullamcorper in ac velit. Aliquam eleifend dignissim lacinia. Donec elementum ex et dui iaculis, eget vehicula leo bibendum. Nam turpis erat, luctus ut vehicula quis, congue non ex. In eget risus consequat, luctus ipsum nec, venenatis elit. In in tellus vel mauris rhoncus bibendum. Pellentesque sit amet quam elementum, pharetra nisl id, vehicula turpis. ",
                                        null,
                                        null,
                                        null,
                                System.currentTimeMillis(),
                                null,
                                null,
                                null,
                                status = StatusJournal.DRAFT.name,
                                classification = Classification.CONFIDENTIAL.name,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                System.currentTimeMillis(),
                                System.currentTimeMillis(),
                                System.currentTimeMillis(),
                                0,
                                Color.Magenta.toArgb(),
                                Color.Cyan.toArgb(),
                                1L,
                                "myAccount",
                                "myCollection",
                                deleted = false,
                                uploadPending = true,
                                isRecurringOriginal = true,
                                isRecurringInstance = true,
                                isLinkedRecurringInstance = true,
                                isChildOfJournal = false,
                                isChildOfNote = false,
                                isChildOfTodo = false,
                                categories = "Category1, Whatever",
                                numSubtasks = 3,
                                numSubnotes = 5,
                                numAttachments = 4,
                                numAttendees = 5,
                                numComments = 6,
                                numRelatedTodos = 7,
                                numResources = 8,
                                isReadOnly = false
                                ),
                                null,
                                listOf(Attachment.getSample(), Attachment.getSample())
                )
        }
}



