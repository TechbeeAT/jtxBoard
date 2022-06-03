/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.views

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.util.DateTimeUtils
import java.time.Instant
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

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
            "main_icalobject.$COLUMN_LOCATION, " +
            "main_icalobject.$COLUMN_URL, " +
            "main_icalobject.$COLUMN_CONTACT, " +
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
            "collection.$COLUMN_COLLECTION_COLOR as colorCollection, " +
            "main_icalobject.$COLUMN_COLOR as colorItem, " +
            "main_icalobject.$COLUMN_ICALOBJECT_COLLECTIONID, " +
            "collection.$COLUMN_COLLECTION_ACCOUNT_NAME, " +
            "collection.$COLUMN_COLLECTION_DISPLAYNAME, " +
            "main_icalobject.$COLUMN_DELETED, " +
            "CASE WHEN main_icalobject.$COLUMN_DIRTY = 1 AND collection.$COLUMN_COLLECTION_ACCOUNT_TYPE != 'LOCAL' THEN 1 else 0 END as uploadPending, " +
            "CASE WHEN main_icalobject.$COLUMN_RRULE IS NULL THEN 0 ELSE 1 END as isRecurringOriginal, " +
            "CASE WHEN main_icalobject.$COLUMN_RECURID IS NULL THEN 0 ELSE 1 END as isRecurringInstance, " +
            "main_icalobject.$COLUMN_RECUR_ISLINKEDINSTANCE, " +
            "CASE WHEN main_icalobject.$COLUMN_ID IN (SELECT sub_rel.$COLUMN_RELATEDTO_LINKEDICALOBJECT_ID FROM $TABLE_NAME_RELATEDTO sub_rel INNER JOIN $TABLE_NAME_ICALOBJECT sub_ical on sub_rel.$COLUMN_RELATEDTO_ICALOBJECT_ID = sub_ical.$COLUMN_ID AND sub_ical.$COLUMN_MODULE = 'JOURNAL' AND sub_rel.$COLUMN_RELATEDTO_RELTYPE = 'CHILD') THEN 1 ELSE 0 END as isChildOfJournal, " +
            "CASE WHEN main_icalobject.$COLUMN_ID IN (SELECT sub_rel.$COLUMN_RELATEDTO_LINKEDICALOBJECT_ID FROM $TABLE_NAME_RELATEDTO sub_rel INNER JOIN $TABLE_NAME_ICALOBJECT sub_ical on sub_rel.$COLUMN_RELATEDTO_ICALOBJECT_ID = sub_ical.$COLUMN_ID AND sub_ical.$COLUMN_MODULE = 'NOTE' AND sub_rel.$COLUMN_RELATEDTO_RELTYPE = 'CHILD') THEN 1 ELSE 0 END as isChildOfNote, " +
            "CASE WHEN main_icalobject.$COLUMN_ID IN (SELECT sub_rel.$COLUMN_RELATEDTO_LINKEDICALOBJECT_ID FROM $TABLE_NAME_RELATEDTO sub_rel INNER JOIN $TABLE_NAME_ICALOBJECT sub_ical on sub_rel.$COLUMN_RELATEDTO_ICALOBJECT_ID = sub_ical.$COLUMN_ID AND sub_ical.$COLUMN_MODULE = 'TODO' AND sub_rel.$COLUMN_RELATEDTO_RELTYPE = 'CHILD') THEN 1 ELSE 0 END as isChildOfTodo, " +
            "(SELECT group_concat($TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_TEXT, \', \') FROM $TABLE_NAME_CATEGORY WHERE main_icalobject.$COLUMN_ID = $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ICALOBJECT_ID GROUP BY $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ICALOBJECT_ID) as categories, " +
            "(SELECT count(*) FROM $TABLE_NAME_ICALOBJECT sub_icalobject INNER JOIN $TABLE_NAME_RELATEDTO sub_relatedto ON sub_icalobject.$COLUMN_ID = sub_relatedto.$COLUMN_RELATEDTO_LINKEDICALOBJECT_ID AND sub_icalobject.$COLUMN_COMPONENT = \'VTODO\' AND sub_relatedto.$COLUMN_RELATEDTO_ICALOBJECT_ID = main_icalobject.$COLUMN_ID AND sub_relatedto.$COLUMN_RELATEDTO_RELTYPE = 'CHILD') as numSubtasks, " +
            "(SELECT count(*) FROM $TABLE_NAME_ICALOBJECT sub_icalobject INNER JOIN $TABLE_NAME_RELATEDTO sub_relatedto ON sub_icalobject.$COLUMN_ID = sub_relatedto.$COLUMN_RELATEDTO_LINKEDICALOBJECT_ID AND sub_icalobject.$COLUMN_COMPONENT = \'VJOURNAL\' AND sub_relatedto.$COLUMN_RELATEDTO_ICALOBJECT_ID = main_icalobject.$COLUMN_ID AND sub_relatedto.$COLUMN_RELATEDTO_RELTYPE = 'CHILD') as numSubnotes, " +
            "(SELECT count(*) FROM $TABLE_NAME_ATTACHMENT WHERE $COLUMN_ATTACHMENT_ICALOBJECT_ID = main_icalobject.$COLUMN_ID  ) as numAttachments, " +
            "(SELECT count(*) FROM $TABLE_NAME_ATTENDEE WHERE $COLUMN_ATTENDEE_ICALOBJECT_ID = main_icalobject.$COLUMN_ID  ) as numAttendees, " +
            "(SELECT count(*) FROM $TABLE_NAME_COMMENT WHERE $COLUMN_COMMENT_ICALOBJECT_ID = main_icalobject.$COLUMN_ID  ) as numComments, " +
            "(SELECT count(*) FROM $TABLE_NAME_RELATEDTO WHERE $COLUMN_RELATEDTO_ICALOBJECT_ID = main_icalobject.$COLUMN_ID  ) as numRelatedTodos, " +
            "(SELECT count(*) FROM $TABLE_NAME_RESOURCE WHERE $COLUMN_RESOURCE_ICALOBJECT_ID = main_icalobject.$COLUMN_ID  ) as numResources, " +
            "(SELECT $COLUMN_ATTACHMENT_URI FROM $TABLE_NAME_ATTACHMENT WHERE $COLUMN_ATTACHMENT_ICALOBJECT_ID = main_icalobject.$COLUMN_ID AND $COLUMN_ATTACHMENT_FMTTYPE LIKE 'audio/%' LIMIT 1 ) as audioAttachment, " +

            "collection.$COLUMN_COLLECTION_READONLY as isReadOnly " +
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
    @ColumnInfo(name = COLUMN_LOCATION) var location: String?,
    @ColumnInfo(name = COLUMN_URL) var url: String?,
    @ColumnInfo(name = COLUMN_CONTACT) var contact: String?,

    @ColumnInfo(name = COLUMN_DTSTART) var dtstart: Long?,
    @ColumnInfo(name = COLUMN_DTSTART_TIMEZONE) var dtstartTimezone: String?,

    @ColumnInfo(name = COLUMN_DTEND) var dtend: Long?,
    @ColumnInfo(name = COLUMN_DTEND_TIMEZONE) var dtendTimezone: String?,

    @ColumnInfo(name = COLUMN_STATUS) var status: String?,
    @ColumnInfo(name = COLUMN_CLASSIFICATION) var classification: String?,

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

    @ColumnInfo var colorCollection: Int?,
    @ColumnInfo var colorItem: Int?,


    @ColumnInfo(index = true, name = COLUMN_ICALOBJECT_COLLECTIONID) var collectionId: Long?,
    @ColumnInfo(name = COLUMN_COLLECTION_ACCOUNT_NAME) var accountName: String?,
    @ColumnInfo(name = COLUMN_COLLECTION_DISPLAYNAME) var collectionDisplayName: String?,

    @ColumnInfo(name = COLUMN_DELETED) var deleted: Boolean,
    @ColumnInfo var uploadPending: Boolean,

    @ColumnInfo var isRecurringOriginal: Boolean,
    @ColumnInfo var isRecurringInstance: Boolean,
    @ColumnInfo(name = COLUMN_RECUR_ISLINKEDINSTANCE) var isLinkedRecurringInstance: Boolean,

    @ColumnInfo var isChildOfJournal: Boolean,
    @ColumnInfo var isChildOfNote: Boolean,
    @ColumnInfo var isChildOfTodo: Boolean,

    @ColumnInfo var categories: String?,
    @ColumnInfo var numSubtasks: Int,
    @ColumnInfo var numSubnotes: Int,
    @ColumnInfo var numAttachments: Int,
    @ColumnInfo var numAttendees: Int,
    @ColumnInfo var numComments: Int,
    @ColumnInfo var numRelatedTodos: Int,
    @ColumnInfo var numResources: Int,
    @ColumnInfo var audioAttachment: String?,
    @ColumnInfo var isReadOnly: Boolean
)
{

    companion object {
        fun getSample() =
            ICal4List(
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
                numSubnotes = 2,
                numAttachments = 4,
                numAttendees = 5,
                numComments = 6,
                numRelatedTodos = 7,
                numResources = 8,
                null,
                isReadOnly = true
            )
    }




    fun getDtstartTextInfo(context: Context): String? {

        if(dtstart == null)
            return null

        val zonedStart = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(dtstart!!),
            DateTimeUtils.requireTzId(dtstartTimezone)
        ).toInstant().toEpochMilli()
        val millisLeft = if(dtstartTimezone == ICalObject.TZ_ALLDAY) zonedStart - DateTimeUtils.getTodayAsLong() else zonedStart - System.currentTimeMillis()

        val daysLeft = TimeUnit.MILLISECONDS.toDays(millisLeft)     // cannot be negative, would stop at 0!
        val hoursLeft = TimeUnit.MILLISECONDS.toHours(millisLeft)     // cannot be negative, would stop at 0!

        return when {
            millisLeft < 0L -> context.getString(R.string.list_start_past)
            millisLeft >= 0L && daysLeft == 0L && dtstartTimezone == ICalObject.TZ_ALLDAY -> context.getString(
                R.string.list_start_today)
            millisLeft >= 0L && daysLeft == 1L && dtstartTimezone == ICalObject.TZ_ALLDAY -> context.getString(
                R.string.list_start_tomorrow)
            millisLeft >= 0L && daysLeft <= 1L && dtstartTimezone != ICalObject.TZ_ALLDAY -> context.getString(
                R.string.list_start_inXhours, hoursLeft)
            millisLeft >= 0L && daysLeft >= 2L -> context.getString(R.string.list_start_inXdays, daysLeft)
            else -> null      //should not be possible
        }
    }

    fun getDueTextInfo(context: Context): String? {

        if(due == null)
            return null

        val zonedDue = ZonedDateTime.ofInstant(Instant.ofEpochMilli(due!!), DateTimeUtils.requireTzId(dueTimezone)).toInstant().toEpochMilli()
        val millisLeft = if(dueTimezone == ICalObject.TZ_ALLDAY) zonedDue - DateTimeUtils.getTodayAsLong() else zonedDue - System.currentTimeMillis()

        val daysLeft = TimeUnit.MILLISECONDS.toDays(millisLeft)     // cannot be negative, would stop at 0!
        val hoursLeft = TimeUnit.MILLISECONDS.toHours(millisLeft)     // cannot be negative, would stop at 0!

        return when {
            millisLeft < 0L -> context.getString(R.string.list_due_overdue)
            millisLeft >= 0L && daysLeft == 0L && dueTimezone == ICalObject.TZ_ALLDAY -> context.getString(R.string.list_due_today)
            millisLeft >= 0L && daysLeft == 1L && dueTimezone == ICalObject.TZ_ALLDAY -> context.getString(R.string.list_due_tomorrow)
            millisLeft >= 0L && daysLeft <= 1L && dueTimezone != ICalObject.TZ_ALLDAY -> context.getString(R.string.list_due_inXhours, hoursLeft)
            millisLeft >= 0L && daysLeft >= 2L -> context.getString(R.string.list_due_inXdays, daysLeft)
            else -> null      //should not be possible
        }
    }
}