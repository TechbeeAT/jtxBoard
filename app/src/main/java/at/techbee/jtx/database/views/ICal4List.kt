/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.views

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.*

const val VIEW_NAME_ICAL4LIST = "ical4list"

/**
 * This data class defines a view that is used by the IcalListViewModel.
 * It provides only necessary columns that are actually used by the View Model.
 * Additionally it provides the categories as a string concatenated field.
 * Additionally it provides the number of subtasks of an item.
 * Additionally it excludes remote items that are marked as deleted.
 */
@DatabaseView(
    viewName = VIEW_NAME_ICAL4LIST,
    value = "SELECT DISTINCT " +
            "main_icalobject.$COLUMN_ID, " +
            "main_icalobject.$COLUMN_MODULE, " +
            "main_icalobject.$COLUMN_COMPONENT, " +
            "main_icalobject.$COLUMN_SUMMARY, " +
            "main_icalobject.$COLUMN_DESCRIPTION, " +
            "main_icalobject.$COLUMN_DTSTART, " +
            "main_icalobject.$COLUMN_DTSTART_TIMEZONE, " +
            "main_icalobject.$COLUMN_DTEND, " +
            "main_icalobject.$COLUMN_DTEND_TIMEZONE, " +
            "main_icalobject.$COLUMN_STATUS, " +
            "main_icalobject.$COLUMN_CLASSIFICATION, " +
            "main_icalobject.$COLUMN_PERCENT, " +
            "main_icalobject.$COLUMN_PRIORITY, " +
            "main_icalobject.$COLUMN_DUE, " +
            "main_icalobject.$COLUMN_DUE_TIMEZONE, " +
            "main_icalobject.$COLUMN_COMPLETED, " +
            "main_icalobject.$COLUMN_COMPLETED_TIMEZONE, " +
            "main_icalobject.$COLUMN_DURATION, " +
            "main_icalobject.$COLUMN_CREATED, " +
            "main_icalobject.$COLUMN_DTSTAMP, " +
            "main_icalobject.$COLUMN_LAST_MODIFIED, " +
            "main_icalobject.$COLUMN_SEQUENCE, " +
            "CASE WHEN main_icalobject.$COLUMN_COLOR IS NOT NULL THEN main_icalobject.$COLUMN_COLOR ELSE collection.$COLUMN_COLLECTION_COLOR END as color, " +             // take the color of the collection if there is no color given in the item. This is just for displaying in the list view.
            "main_icalobject.$COLUMN_ICALOBJECT_COLLECTIONID, " +
            "main_icalobject.$COLUMN_DELETED, " +
            "CASE WHEN main_icalobject.$COLUMN_RRULE IS NULL THEN 0 ELSE 1 END as isRecurringOriginal, " +
            "CASE WHEN main_icalobject.$COLUMN_RECURID IS NULL THEN 0 ELSE 1 END as isRecurringInstance, " +
            "main_icalobject.$COLUMN_RECUR_ISLINKEDINSTANCE, " +
            "CASE WHEN main_icalobject.$COLUMN_ID IN (SELECT sub_rel.$COLUMN_RELATEDTO_LINKEDICALOBJECT_ID FROM $TABLE_NAME_RELATEDTO sub_rel INNER JOIN $TABLE_NAME_ICALOBJECT sub_ical on sub_rel.$COLUMN_RELATEDTO_ICALOBJECT_ID = sub_ical.$COLUMN_ID AND sub_ical.$COLUMN_COMPONENT = 'VJOURNAL') THEN 1 ELSE 0 END as isChildOfVJOURNAL, " +
            "CASE WHEN main_icalobject.$COLUMN_ID IN (SELECT sub_rel.$COLUMN_RELATEDTO_LINKEDICALOBJECT_ID FROM $TABLE_NAME_RELATEDTO sub_rel INNER JOIN $TABLE_NAME_ICALOBJECT sub_ical on sub_rel.$COLUMN_RELATEDTO_ICALOBJECT_ID = sub_ical.$COLUMN_ID AND sub_ical.$COLUMN_COMPONENT = 'VTODO') THEN 1 ELSE 0 END as isChildOfVTODO, " +
            "(SELECT group_concat($TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_TEXT, \", \") FROM $TABLE_NAME_CATEGORY WHERE main_icalobject.$COLUMN_ID = $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ICALOBJECT_ID GROUP BY $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ICALOBJECT_ID) as categories, " +
            "(SELECT count(*) FROM $TABLE_NAME_ICALOBJECT sub_icalobject INNER JOIN $TABLE_NAME_RELATEDTO sub_relatedto ON sub_icalobject.$COLUMN_ID = sub_relatedto.$COLUMN_RELATEDTO_ICALOBJECT_ID AND sub_icalobject.$COLUMN_COMPONENT = \'TODO\' AND sub_icalobject.$COLUMN_ID = main_icalobject.$COLUMN_ID ) as numSubtasks, " +
            "(SELECT count(*) FROM $TABLE_NAME_ATTACHMENT WHERE $COLUMN_ATTACHMENT_ICALOBJECT_ID = main_icalobject.$COLUMN_ID  ) as numAttachments, " +
            "(SELECT count(*) FROM $TABLE_NAME_ATTENDEE WHERE $COLUMN_ATTENDEE_ICALOBJECT_ID = main_icalobject.$COLUMN_ID  ) as numAttendees, " +
            "(SELECT count(*) FROM $TABLE_NAME_COMMENT WHERE $COLUMN_COMMENT_ICALOBJECT_ID = main_icalobject.$COLUMN_ID  ) as numComments, " +
            "(SELECT count(*) FROM $TABLE_NAME_RELATEDTO WHERE $COLUMN_RELATEDTO_ICALOBJECT_ID = main_icalobject.$COLUMN_ID  ) as numRelatedtos, " +
            "(SELECT count(*) FROM $TABLE_NAME_RESOURCE WHERE $COLUMN_RESOURCE_ICALOBJECT_ID = main_icalobject.$COLUMN_ID  ) as numResources " +
            "FROM $TABLE_NAME_ICALOBJECT main_icalobject " +
            //"LEFT JOIN $TABLE_NAME_CATEGORY ON main_icalobject.$COLUMN_ID = $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ICALOBJECT_ID " +
            "INNER JOIN $TABLE_NAME_COLLECTION collection ON main_icalobject.$COLUMN_ICALOBJECT_COLLECTIONID = collection.$COLUMN_COLLECTION_ID " +
            "WHERE main_icalobject.$COLUMN_DELETED = 0"
)           // locally deleted entries are already excluded in the view!
data class ICal4List(

    @ColumnInfo(index = true, name = COLUMN_ID) var id: Long,
    @ColumnInfo(name = COLUMN_MODULE) var module: String,
    @ColumnInfo(name = COLUMN_COMPONENT) var component: String,
    @ColumnInfo(name = COLUMN_SUMMARY) var summary: String?,
    @ColumnInfo(name = COLUMN_DESCRIPTION) var description: String?,
    @ColumnInfo(name = COLUMN_DTSTART) var dtstart: Long?,
    @ColumnInfo(name = COLUMN_DTSTART_TIMEZONE) var dtstartTimezone: String?,

    @ColumnInfo(name = COLUMN_DTEND) var dtend: Long?,
    @ColumnInfo(name = COLUMN_DTEND_TIMEZONE) var dtendTimezone: String?,

    @ColumnInfo(name = COLUMN_STATUS) var status: String,
    @ColumnInfo(name = COLUMN_CLASSIFICATION) var classification: String,

    @ColumnInfo(name = COLUMN_PERCENT) var percent: Int?,
    @ColumnInfo(name = COLUMN_PRIORITY) var priority: Int?,

    @ColumnInfo(name = COLUMN_DUE) var due: Long?,
    @ColumnInfo(name = COLUMN_DUE_TIMEZONE) var dueTimezone: String?,
    @ColumnInfo(name = COLUMN_COMPLETED) var completed: Long?,
    @ColumnInfo(name = COLUMN_COMPLETED_TIMEZONE) var completedTimezone: String?,
    @ColumnInfo(name = COLUMN_DURATION) var duration: String?,

    @ColumnInfo(name = COLUMN_CREATED) var created: Long,
    @ColumnInfo(name = COLUMN_DTSTAMP) var dtstamp: Long,
    @ColumnInfo(name = COLUMN_LAST_MODIFIED) var lastModified: Long,
    @ColumnInfo(name = COLUMN_SEQUENCE) var sequence: Long,

    @ColumnInfo(name = COLUMN_COLOR) var color: Int?,

    @ColumnInfo(index = true, name = COLUMN_ICALOBJECT_COLLECTIONID) var collectionId: Long?,

    @ColumnInfo(name = COLUMN_DELETED) var deleted: Boolean,

    @ColumnInfo var isRecurringOriginal: Boolean,
    @ColumnInfo var isRecurringInstance: Boolean,
    @ColumnInfo(name = COLUMN_RECUR_ISLINKEDINSTANCE) var isLinkedRecurringInstance: Boolean,

    @ColumnInfo var isChildOfVJOURNAL: Boolean,
    @ColumnInfo var isChildOfVTODO: Boolean,

    @ColumnInfo var categories: String?,
    @ColumnInfo var numSubtasks: Int,
    @ColumnInfo var numAttachments: Int,
    @ColumnInfo var numAttendees: Int,
    @ColumnInfo var numComments: Int,
    @ColumnInfo var numRelatedtos: Int,
    @ColumnInfo var numResources: Int

)

