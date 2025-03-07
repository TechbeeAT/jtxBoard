/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.views

import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.sqlite.db.SimpleSQLiteQuery
import at.techbee.jtx.database.COLUMN_ATTACHMENTS_EXPANDED
import at.techbee.jtx.database.COLUMN_CLASSIFICATION
import at.techbee.jtx.database.COLUMN_COLLECTION_ACCOUNT_NAME
import at.techbee.jtx.database.COLUMN_COLLECTION_ACCOUNT_TYPE
import at.techbee.jtx.database.COLUMN_COLLECTION_COLOR
import at.techbee.jtx.database.COLUMN_COLLECTION_DISPLAYNAME
import at.techbee.jtx.database.COLUMN_COLLECTION_ID
import at.techbee.jtx.database.COLUMN_COLLECTION_READONLY
import at.techbee.jtx.database.COLUMN_COLOR
import at.techbee.jtx.database.COLUMN_COMPLETED
import at.techbee.jtx.database.COLUMN_COMPLETED_TIMEZONE
import at.techbee.jtx.database.COLUMN_COMPONENT
import at.techbee.jtx.database.COLUMN_CONTACT
import at.techbee.jtx.database.COLUMN_CREATED
import at.techbee.jtx.database.COLUMN_DELETED
import at.techbee.jtx.database.COLUMN_DESCRIPTION
import at.techbee.jtx.database.COLUMN_DIRTY
import at.techbee.jtx.database.COLUMN_DTEND
import at.techbee.jtx.database.COLUMN_DTEND_TIMEZONE
import at.techbee.jtx.database.COLUMN_DTSTAMP
import at.techbee.jtx.database.COLUMN_DTSTART
import at.techbee.jtx.database.COLUMN_DTSTART_TIMEZONE
import at.techbee.jtx.database.COLUMN_DUE
import at.techbee.jtx.database.COLUMN_DUE_TIMEZONE
import at.techbee.jtx.database.COLUMN_DURATION
import at.techbee.jtx.database.COLUMN_EXTENDED_STATUS
import at.techbee.jtx.database.COLUMN_GEO_LAT
import at.techbee.jtx.database.COLUMN_GEO_LONG
import at.techbee.jtx.database.COLUMN_ICALOBJECT_COLLECTIONID
import at.techbee.jtx.database.COLUMN_ID
import at.techbee.jtx.database.COLUMN_LAST_MODIFIED
import at.techbee.jtx.database.COLUMN_LOCATION
import at.techbee.jtx.database.COLUMN_MODULE
import at.techbee.jtx.database.COLUMN_PARENTS_EXPANDED
import at.techbee.jtx.database.COLUMN_PERCENT
import at.techbee.jtx.database.COLUMN_PRIORITY
import at.techbee.jtx.database.COLUMN_RECURID
import at.techbee.jtx.database.COLUMN_RRULE
import at.techbee.jtx.database.COLUMN_SEQUENCE
import at.techbee.jtx.database.COLUMN_SORT_INDEX
import at.techbee.jtx.database.COLUMN_STATUS
import at.techbee.jtx.database.COLUMN_SUBNOTES_EXPANDED
import at.techbee.jtx.database.COLUMN_SUBTASKS_EXPANDED
import at.techbee.jtx.database.COLUMN_SUMMARY
import at.techbee.jtx.database.COLUMN_UID
import at.techbee.jtx.database.COLUMN_URL
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.TABLE_NAME_COLLECTION
import at.techbee.jtx.database.TABLE_NAME_ICALOBJECT
import at.techbee.jtx.database.properties.COLUMN_ALARM_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_ATTACHMENT_FMTTYPE
import at.techbee.jtx.database.properties.COLUMN_ATTACHMENT_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_ATTACHMENT_URI
import at.techbee.jtx.database.properties.COLUMN_ATTENDEE_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_CATEGORY_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_CATEGORY_TEXT
import at.techbee.jtx.database.properties.COLUMN_COMMENT_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_RELATEDTO_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_RELATEDTO_RELTYPE
import at.techbee.jtx.database.properties.COLUMN_RELATEDTO_TEXT
import at.techbee.jtx.database.properties.COLUMN_RESOURCE_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_RESOURCE_TEXT
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.database.properties.TABLE_NAME_ALARM
import at.techbee.jtx.database.properties.TABLE_NAME_ATTACHMENT
import at.techbee.jtx.database.properties.TABLE_NAME_ATTENDEE
import at.techbee.jtx.database.properties.TABLE_NAME_CATEGORY
import at.techbee.jtx.database.properties.TABLE_NAME_COMMENT
import at.techbee.jtx.database.properties.TABLE_NAME_RELATEDTO
import at.techbee.jtx.database.properties.TABLE_NAME_RESOURCE
import at.techbee.jtx.ui.list.AnyAllNone
import at.techbee.jtx.ui.list.OrderBy
import at.techbee.jtx.ui.list.SortOrder
import at.techbee.jtx.util.DateTimeUtils
import java.util.UUID
import kotlin.time.Duration.Companion.days

const val VIEW_NAME_ICAL4LIST = "ical4list"

const val CONCAT_DELIMITER = "|||"

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
            "main_icalobject.$COLUMN_GEO_LAT, " +
            "main_icalobject.$COLUMN_GEO_LONG, " +
            "main_icalobject.$COLUMN_URL, " +
            "main_icalobject.$COLUMN_CONTACT, " +
            "main_icalobject.$COLUMN_DTSTART, " +
            "main_icalobject.$COLUMN_DTSTART_TIMEZONE, " +
            "main_icalobject.$COLUMN_DTEND, " +
            "main_icalobject.$COLUMN_DTEND_TIMEZONE, " +
            "main_icalobject.$COLUMN_STATUS, " +
            "main_icalobject.$COLUMN_EXTENDED_STATUS, " +
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
            "main_icalobject.$COLUMN_UID, " +
            "main_icalobject.$COLUMN_RRULE, " +
            "main_icalobject.$COLUMN_RECURID, " +
            "collection.$COLUMN_COLLECTION_COLOR as colorCollection, " +
            "main_icalobject.$COLUMN_COLOR as colorItem, " +
            "main_icalobject.$COLUMN_ICALOBJECT_COLLECTIONID, " +
            "collection.$COLUMN_COLLECTION_ACCOUNT_NAME, " +
            "collection.$COLUMN_COLLECTION_DISPLAYNAME, " +
            "main_icalobject.$COLUMN_DELETED, " +
            "CASE WHEN collection.$COLUMN_COLLECTION_ACCOUNT_TYPE = '$LOCAL_ACCOUNT_TYPE' THEN 0 WHEN main_icalobject.$COLUMN_RECURID IS NOT NULL THEN (SELECT series.$COLUMN_DIRTY FROM $TABLE_NAME_ICALOBJECT series WHERE series.$COLUMN_RECURID IS NULL AND series.$COLUMN_UID = main_icalobject.$COLUMN_UID) ELSE main_icalobject.$COLUMN_DIRTY END as uploadPending, " +
            "CASE WHEN main_icalobject.$COLUMN_ID IN (SELECT sub_rel.$COLUMN_RELATEDTO_ICALOBJECT_ID FROM $TABLE_NAME_RELATEDTO sub_rel INNER JOIN $TABLE_NAME_ICALOBJECT sub_ical on sub_rel.$COLUMN_RELATEDTO_TEXT = sub_ical.$COLUMN_UID AND sub_ical.$COLUMN_MODULE = 'JOURNAL' AND sub_rel.$COLUMN_RELATEDTO_RELTYPE = 'PARENT') THEN 1 ELSE 0 END as isChildOfJournal, " +
            "CASE WHEN main_icalobject.$COLUMN_ID IN (SELECT sub_rel.$COLUMN_RELATEDTO_ICALOBJECT_ID FROM $TABLE_NAME_RELATEDTO sub_rel INNER JOIN $TABLE_NAME_ICALOBJECT sub_ical on sub_rel.$COLUMN_RELATEDTO_TEXT = sub_ical.$COLUMN_UID AND sub_ical.$COLUMN_MODULE = 'NOTE' AND sub_rel.$COLUMN_RELATEDTO_RELTYPE = 'PARENT') THEN 1 ELSE 0 END as isChildOfNote, " +
            "CASE WHEN main_icalobject.$COLUMN_ID IN (SELECT sub_rel.$COLUMN_RELATEDTO_ICALOBJECT_ID FROM $TABLE_NAME_RELATEDTO sub_rel INNER JOIN $TABLE_NAME_ICALOBJECT sub_ical on sub_rel.$COLUMN_RELATEDTO_TEXT = sub_ical.$COLUMN_UID AND sub_ical.$COLUMN_MODULE = 'TODO' AND sub_rel.$COLUMN_RELATEDTO_RELTYPE = 'PARENT') THEN 1 ELSE 0 END as isChildOfTodo, " +
            "(SELECT group_concat(sub.$COLUMN_CATEGORY_TEXT, \'"+ CONCAT_DELIMITER + "\') FROM (SELECT * FROM $TABLE_NAME_CATEGORY ORDER BY $COLUMN_CATEGORY_TEXT) as sub WHERE main_icalobject.$COLUMN_ID = sub.$COLUMN_CATEGORY_ICALOBJECT_ID) as categories, " +
            "(SELECT group_concat(sub.$COLUMN_RESOURCE_TEXT, \'"+ CONCAT_DELIMITER + "\') FROM (SELECT * FROM $TABLE_NAME_RESOURCE ORDER BY $COLUMN_RESOURCE_TEXT) as sub WHERE main_icalobject.$COLUMN_ID = sub.$COLUMN_RESOURCE_ICALOBJECT_ID) as resources, " +
            "(SELECT count(*) FROM $TABLE_NAME_ICALOBJECT sub_icalobject INNER JOIN $TABLE_NAME_RELATEDTO sub_relatedto ON sub_icalobject.$COLUMN_ID = sub_relatedto.$COLUMN_RELATEDTO_ICALOBJECT_ID AND sub_icalobject.$COLUMN_COMPONENT = 'VTODO' AND sub_relatedto.$COLUMN_RELATEDTO_TEXT = main_icalobject.$COLUMN_UID AND sub_relatedto.$COLUMN_RELATEDTO_RELTYPE = 'PARENT' AND sub_icalobject.$COLUMN_DELETED = 0 AND sub_icalobject.$COLUMN_RRULE IS NULL) as numSubtasks, " +
            "(SELECT count(*) FROM $TABLE_NAME_ICALOBJECT sub_icalobject INNER JOIN $TABLE_NAME_RELATEDTO sub_relatedto ON sub_icalobject.$COLUMN_ID = sub_relatedto.$COLUMN_RELATEDTO_ICALOBJECT_ID AND sub_icalobject.$COLUMN_COMPONENT = 'VJOURNAL' AND sub_relatedto.$COLUMN_RELATEDTO_TEXT = main_icalobject.$COLUMN_UID AND sub_relatedto.$COLUMN_RELATEDTO_RELTYPE = 'PARENT' AND sub_icalobject.$COLUMN_DELETED = 0 AND sub_icalobject.$COLUMN_RRULE IS NULL) as numSubnotes, " +
            "(SELECT count(*) FROM $TABLE_NAME_ATTACHMENT WHERE $COLUMN_ATTACHMENT_ICALOBJECT_ID = main_icalobject.$COLUMN_ID  ) as numAttachments, " +
            "(SELECT count(*) FROM $TABLE_NAME_ATTENDEE WHERE $COLUMN_ATTENDEE_ICALOBJECT_ID = main_icalobject.$COLUMN_ID  ) as numAttendees, " +
            "(SELECT count(*) FROM $TABLE_NAME_COMMENT WHERE $COLUMN_COMMENT_ICALOBJECT_ID = main_icalobject.$COLUMN_ID  ) as numComments, " +
            "(SELECT count(*) FROM $TABLE_NAME_RELATEDTO WHERE $COLUMN_RELATEDTO_ICALOBJECT_ID = main_icalobject.$COLUMN_ID  ) as numRelatedTodos, " +
            "(SELECT count(*) FROM $TABLE_NAME_RESOURCE WHERE $COLUMN_RESOURCE_ICALOBJECT_ID = main_icalobject.$COLUMN_ID  ) as numResources, " +
            "(SELECT count(*) FROM $TABLE_NAME_ALARM WHERE $COLUMN_ALARM_ICALOBJECT_ID = main_icalobject.$COLUMN_ID  ) as numAlarms, " +
            "(SELECT $COLUMN_ATTACHMENT_URI FROM $TABLE_NAME_ATTACHMENT WHERE $COLUMN_ATTACHMENT_ICALOBJECT_ID = main_icalobject.$COLUMN_ID AND ($COLUMN_ATTACHMENT_FMTTYPE LIKE 'audio/%' OR $COLUMN_ATTACHMENT_FMTTYPE LIKE 'video/%') LIMIT 1 ) as audioAttachment, " +
            "collection.$COLUMN_COLLECTION_READONLY as isReadOnly, " +
            "main_icalobject.$COLUMN_SUBTASKS_EXPANDED, " +
            "main_icalobject.$COLUMN_SUBNOTES_EXPANDED, " +
            "main_icalobject.$COLUMN_PARENTS_EXPANDED, " +
            "main_icalobject.$COLUMN_ATTACHMENTS_EXPANDED, " +
            "main_icalobject.$COLUMN_SORT_INDEX " +
            "FROM $TABLE_NAME_ICALOBJECT main_icalobject " +
            //"LEFT JOIN $TABLE_NAME_CATEGORY ON main_icalobject.$COLUMN_ID = $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ICALOBJECT_ID " +
            "INNER JOIN $TABLE_NAME_COLLECTION collection ON main_icalobject.$COLUMN_ICALOBJECT_COLLECTIONID = collection.$COLUMN_COLLECTION_ID " +
            "WHERE main_icalobject.$COLUMN_DELETED = 0 AND (main_icalobject.$COLUMN_RRULE IS NULL OR (main_icalobject.$COLUMN_DTSTART IS NULL AND main_icalobject.$COLUMN_RRULE IS NOT NULL))"
)           // locally deleted entries are already excluded in the view, original recur entries are also excluded

@kotlinx.serialization.Serializable
data class ICal4List(

    @ColumnInfo(index = true, name = COLUMN_ID) var id: Long,
    @ColumnInfo(name = COLUMN_MODULE) var module: String,
    @ColumnInfo(name = COLUMN_COMPONENT) var component: String,
    @ColumnInfo(name = COLUMN_SUMMARY) var summary: String?,
    @ColumnInfo(name = COLUMN_DESCRIPTION) var description: String?,
    @ColumnInfo(name = COLUMN_LOCATION) var location: String?,
    @ColumnInfo(name = COLUMN_GEO_LAT) var geoLat: Double?,
    @ColumnInfo(name = COLUMN_GEO_LONG) var geoLong: Double?,
    @ColumnInfo(name = COLUMN_URL) var url: String?,
    @ColumnInfo(name = COLUMN_CONTACT) var contact: String?,

    @ColumnInfo(name = COLUMN_DTSTART) var dtstart: Long?,
    @ColumnInfo(name = COLUMN_DTSTART_TIMEZONE) var dtstartTimezone: String?,

    @ColumnInfo(name = COLUMN_DTEND) var dtend: Long?,
    @ColumnInfo(name = COLUMN_DTEND_TIMEZONE) var dtendTimezone: String?,

    @ColumnInfo(name = COLUMN_STATUS) var status: String?,
    @ColumnInfo(name = COLUMN_EXTENDED_STATUS) var xstatus: String?,
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
    @ColumnInfo(index = true, name = COLUMN_UID) var uid: String?,
    @ColumnInfo(name = COLUMN_RRULE) var rrule: String?,
    @ColumnInfo(name = COLUMN_RECURID) var recurid: String?,

    @ColumnInfo var colorCollection: Int?,
    @ColumnInfo var colorItem: Int?,


    @ColumnInfo(name = COLUMN_ICALOBJECT_COLLECTIONID) var collectionId: Long?,
    @ColumnInfo(name = COLUMN_COLLECTION_ACCOUNT_NAME) var accountName: String?,
    @ColumnInfo(name = COLUMN_COLLECTION_DISPLAYNAME) var collectionDisplayName: String?,

    @ColumnInfo(name = COLUMN_DELETED) var deleted: Boolean,
    @ColumnInfo var uploadPending: Boolean,

    @ColumnInfo var isChildOfJournal: Boolean,
    @ColumnInfo var isChildOfNote: Boolean,
    @ColumnInfo var isChildOfTodo: Boolean,

    @ColumnInfo var categories: String?,
    @ColumnInfo var resources: String?,
    @ColumnInfo var numSubtasks: Int,
    @ColumnInfo var numSubnotes: Int,
    @ColumnInfo var numAttachments: Int,
    @ColumnInfo var numAttendees: Int,
    @ColumnInfo var numComments: Int,
    @ColumnInfo var numRelatedTodos: Int,
    @ColumnInfo var numResources: Int,
    @ColumnInfo var numAlarms: Int,
    @ColumnInfo var audioAttachment: String?,
    @ColumnInfo var isReadOnly: Boolean,

    @ColumnInfo(name = COLUMN_SUBTASKS_EXPANDED) var isSubtasksExpanded: Boolean? = null,
    @ColumnInfo(name = COLUMN_SUBNOTES_EXPANDED) var isSubnotesExpanded: Boolean? = null,
    @ColumnInfo(name = COLUMN_PARENTS_EXPANDED) var isParentsExpanded: Boolean? = null,
    @ColumnInfo(name = COLUMN_ATTACHMENTS_EXPANDED) var isAttachmentsExpanded: Boolean? = null,
    @ColumnInfo(name = COLUMN_SORT_INDEX) var sortIndex: Int? = null,
) {

    companion object {
        fun getSample() =
            ICal4List(
                id = 1L,
                module = Module.JOURNAL.name,
                component = Component.VJOURNAL.name,
                summary = "My Summary",
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur tellus risus, tristique ac elit vitae, mollis feugiat quam. Duis aliquet arcu at purus porttitor ultricies. Vivamus sagittis feugiat ex eu efficitur. Aliquam nec cursus ante, a varius nisi. In a malesuada urna, in rhoncus est. Maecenas auctor molestie quam, quis lobortis tortor sollicitudin sagittis. Curabitur sit amet est varius urna mattis interdum.\n" +
                        "\n" +
                        "Phasellus id quam vel enim semper ullamcorper in ac velit. Aliquam eleifend dignissim lacinia. Donec elementum ex et dui iaculis, eget vehicula leo bibendum. Nam turpis erat, luctus ut vehicula quis, congue non ex. In eget risus consequat, luctus ipsum nec, venenatis elit. In in tellus vel mauris rhoncus bibendum. Pellentesque sit amet quam elementum, pharetra nisl id, vehicula turpis. ",
                location = "Vienna",
                geoLat = 123.456,
                geoLong = 321.654,
                url = "https://jtx.techbee.at",
                contact = null,
                dtstart = System.currentTimeMillis(),
                dtstartTimezone = null,
                dtend = null,
                dtendTimezone = null,
                status = Status.DRAFT.status,
                xstatus = null,
                classification = Classification.CONFIDENTIAL.classification,
                percent = null,
                priority = null,
                due = null,
                dueTimezone = null,
                completed = null,
                completedTimezone = null,
                duration = null,
                created = System.currentTimeMillis(),
                dtstamp = System.currentTimeMillis(),
                lastModified = System.currentTimeMillis(),
                sequence = 0,
                uid = UUID.randomUUID().toString(),
                rrule = null,
                recurid = null,
                colorCollection = Color.Magenta.toArgb(),
                colorItem = Color.Cyan.toArgb(),
                collectionId = 1L,
                accountName = "myAccount",
                collectionDisplayName = "myCollection",
                deleted = false,
                uploadPending = true,
                isChildOfJournal = false,
                isChildOfNote = false,
                isChildOfTodo = false,
                categories = "Category1, Whatever",
                resources = "Resource1, Resource2",
                numSubtasks = 3,
                numSubnotes = 2,
                numAttachments = 4,
                numAttendees = 5,
                numComments = 6,
                numRelatedTodos = 7,
                numResources = 8,
                numAlarms = 2,
                audioAttachment = null,
                isReadOnly = true
            )

        fun constructQuery(
            modules: List<Module>,
            searchCategories: List<String> = emptyList(),
            searchCategoriesAnyAllNone: AnyAllNone = AnyAllNone.ANY,
            searchResources: List<String> = emptyList(),
            searchResourcesAnyAllNone: AnyAllNone = AnyAllNone.ANY,
            searchStatus: List<Status> = emptyList(),
            searchXStatus: List<String> = emptyList(),
            searchClassification: List<Classification> = emptyList(),
            searchPriority: List<Int?> = emptyList(),
            searchCollection: List<String> = emptyList(),
            searchAccount: List<String> = emptyList(),
            orderBy: OrderBy = OrderBy.CREATED,
            sortOrder: SortOrder = SortOrder.ASC,
            orderBy2: OrderBy = OrderBy.SUMMARY,
            sortOrder2: SortOrder = SortOrder.ASC,
            isExcludeDone: Boolean = false,
            isFilterOverdue: Boolean = false,
            isFilterDueToday: Boolean = false,
            isFilterDueTomorrow: Boolean = false,
            isFilterDueWithin7Days: Boolean = false,
            isFilterDueFuture: Boolean = false,
            isFilterStartInPast: Boolean = false,
            isFilterStartToday: Boolean = false,
            isFilterStartTomorrow: Boolean = false,
            isFilterStartWithin7Days: Boolean = false,
            isFilterStartFuture: Boolean = false,
            isFilterNoDatesSet: Boolean = false,
            isFilterNoStartDateSet: Boolean = false,
            isFilterNoDueDateSet: Boolean = false,
            isFilterNoCompletedDateSet: Boolean = false,
            filterStartRangeStart: Long? = null,
            filterStartRangeEnd: Long? = null,
            filterDueRangeStart: Long? = null,
            filterDueRangeEnd: Long? = null,
            filterCompletedRangeStart: Long? = null,
            filterCompletedRangeEnd: Long? = null,
            isFilterNoCategorySet: Boolean = false,
            isFilterNoResourceSet: Boolean = false,
            searchText: String? = null,
            flatView: Boolean = false,
            searchSettingShowOneRecurEntryInFuture: Boolean = false,
            hideBiometricProtected: List<Classification>,
            limit: Int? = null
        ): SimpleSQLiteQuery {

            val args = arrayListOf<String>()

            // Beginning of query string
            var queryString = "SELECT DISTINCT $VIEW_NAME_ICAL4LIST.* FROM $VIEW_NAME_ICAL4LIST "
            if (searchResources.isNotEmpty())
                queryString += "LEFT JOIN $TABLE_NAME_RESOURCE ON $VIEW_NAME_ICAL4LIST.$COLUMN_ID = $TABLE_NAME_RESOURCE.$COLUMN_RESOURCE_ICALOBJECT_ID "
            if (searchCollection.isNotEmpty() || searchAccount.isNotEmpty())
                queryString += "LEFT JOIN $TABLE_NAME_COLLECTION ON $VIEW_NAME_ICAL4LIST.$COLUMN_ICALOBJECT_COLLECTIONID = $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ID "  // +

            // First query parameter Module must always be present!
            queryString += "WHERE $COLUMN_MODULE IN (${modules.joinToString(separator = ",") { "?" }}) "
            args.addAll(modules.map { it.name })

            //TEXT
            searchText?.let { text ->
                if (text.length >= 2) {
                    queryString += "AND ("

                    queryString += "$VIEW_NAME_ICAL4LIST.$COLUMN_SUMMARY LIKE ? OR $VIEW_NAME_ICAL4LIST.$COLUMN_DESCRIPTION LIKE ? "
                    args.add("%$text%")
                    args.add("%$text%")

                    // Search in Subtasks and Subnotes
                    queryString += "OR $VIEW_NAME_ICAL4LIST.$COLUMN_UID IN (SELECT $COLUMN_RELATEDTO_TEXT FROM $TABLE_NAME_RELATEDTO INNER JOIN $VIEW_NAME_ICAL4LIST ON $TABLE_NAME_RELATEDTO.$COLUMN_RELATEDTO_ICALOBJECT_ID = $VIEW_NAME_ICAL4LIST.$COLUMN_ID WHERE (isChildOfJournal = 1 OR isChildOfNote = 1 OR isChildOfTodo = 1) AND ($VIEW_NAME_ICAL4LIST.$COLUMN_SUMMARY LIKE ? OR $VIEW_NAME_ICAL4LIST.$COLUMN_DESCRIPTION LIKE ?) ) "
                    args.add("%$text%")
                    args.add("%$text%")

                    queryString += ") "
                }
            }

            //CATEGORIES
            if (searchCategories.isNotEmpty() || isFilterNoCategorySet) {
                queryString += "AND ("
                if (searchCategories.isNotEmpty()) {
                    queryString += "("
                    searchCategories.forEachIndexed { index, _ ->
                        queryString += when(searchCategoriesAnyAllNone) {
                            AnyAllNone.ANY -> "$VIEW_NAME_ICAL4LIST.$COLUMN_ID IN (SELECT sub.$COLUMN_CATEGORY_ICALOBJECT_ID FROM $TABLE_NAME_CATEGORY sub WHERE sub.$COLUMN_CATEGORY_ICALOBJECT_ID = $VIEW_NAME_ICAL4LIST.$COLUMN_ID AND sub.$COLUMN_CATEGORY_TEXT = ?) "
                            AnyAllNone.ALL -> "$VIEW_NAME_ICAL4LIST.$COLUMN_ID IN (SELECT sub.$COLUMN_CATEGORY_ICALOBJECT_ID FROM $TABLE_NAME_CATEGORY sub WHERE sub.$COLUMN_CATEGORY_ICALOBJECT_ID = $VIEW_NAME_ICAL4LIST.$COLUMN_ID AND sub.$COLUMN_CATEGORY_TEXT = ?) "
                            AnyAllNone.NONE -> "$VIEW_NAME_ICAL4LIST.$COLUMN_ID NOT IN (SELECT sub.$COLUMN_CATEGORY_ICALOBJECT_ID FROM $TABLE_NAME_CATEGORY sub WHERE sub.$COLUMN_CATEGORY_ICALOBJECT_ID = $VIEW_NAME_ICAL4LIST.$COLUMN_ID AND sub.$COLUMN_CATEGORY_TEXT = ?) "
                        }
                        if(index != searchCategories.lastIndex) {
                            queryString += when(searchCategoriesAnyAllNone) {
                                AnyAllNone.ANY -> "OR "
                                AnyAllNone.ALL, AnyAllNone.NONE -> "AND "
                            }
                        }
                    }
                    queryString += ") "
                    args.addAll(searchCategories)

                    if(isFilterNoCategorySet)
                        queryString += "OR "
                }
                if (isFilterNoCategorySet)
                    queryString += "$VIEW_NAME_ICAL4LIST.$COLUMN_ID NOT IN (SELECT $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ICALOBJECT_ID FROM $TABLE_NAME_CATEGORY) "
                queryString += ") "
            }

            //RESOURCES
            if (searchResources.isNotEmpty() || isFilterNoResourceSet) {
                queryString += "AND ("
                if (searchResources.isNotEmpty()) {
                    queryString += "("
                    searchResources.forEachIndexed { index, _ ->
                        queryString += when(searchResourcesAnyAllNone) {
                            AnyAllNone.ANY -> "$VIEW_NAME_ICAL4LIST.$COLUMN_ID IN (SELECT sub.$COLUMN_RESOURCE_ICALOBJECT_ID FROM $TABLE_NAME_RESOURCE sub WHERE sub.$COLUMN_RESOURCE_ICALOBJECT_ID = $VIEW_NAME_ICAL4LIST.$COLUMN_ID AND sub.$COLUMN_RESOURCE_TEXT = ?) "
                            AnyAllNone.ALL -> "$VIEW_NAME_ICAL4LIST.$COLUMN_ID IN (SELECT sub.$COLUMN_RESOURCE_ICALOBJECT_ID FROM $TABLE_NAME_RESOURCE sub WHERE sub.$COLUMN_RESOURCE_ICALOBJECT_ID = $VIEW_NAME_ICAL4LIST.$COLUMN_ID AND sub.$COLUMN_RESOURCE_TEXT = ?) "
                            AnyAllNone.NONE -> "$VIEW_NAME_ICAL4LIST.$COLUMN_ID NOT IN (SELECT sub.$COLUMN_RESOURCE_ICALOBJECT_ID FROM $TABLE_NAME_RESOURCE sub WHERE sub.$COLUMN_RESOURCE_ICALOBJECT_ID = $VIEW_NAME_ICAL4LIST.$COLUMN_ID AND sub.$COLUMN_RESOURCE_TEXT = ?) "
                        }
                        if(index != searchResources.lastIndex) {
                            queryString += when(searchResourcesAnyAllNone) {
                                AnyAllNone.ANY -> "OR "
                                AnyAllNone.ALL, AnyAllNone.NONE -> "AND "
                            }
                        }
                    }
                    queryString += ") "
                    args.addAll(searchResources)

                    if(isFilterNoResourceSet)
                        queryString += "OR "
                }
                if (isFilterNoResourceSet)
                    queryString += "$VIEW_NAME_ICAL4LIST.$COLUMN_ID NOT IN (SELECT $TABLE_NAME_RESOURCE.$COLUMN_RESOURCE_ICALOBJECT_ID FROM $TABLE_NAME_RESOURCE) "
                queryString += ") "
            }

            if (searchStatus.isNotEmpty()) {
                queryString += "AND ("
                queryString += searchStatus.joinToString(separator = "OR ", transform = { "$COLUMN_STATUS = ? " })
                args.addAll(searchStatus.map { it.status ?:"" })

                if (searchStatus.contains(Status.NO_STATUS))
                    queryString += "OR $COLUMN_STATUS IS NULL"
                queryString += ") "
            }

            if (searchXStatus.isNotEmpty()) {
                queryString += "AND ("
                queryString += searchXStatus.joinToString(separator = "OR ", transform = { "$COLUMN_EXTENDED_STATUS = ? " })
                args.addAll(searchXStatus.map { it })
                queryString += ") "
            }

            if (isExcludeDone)
                queryString += "AND $COLUMN_PERCENT IS NOT 100 AND ($COLUMN_STATUS IS NULL OR $COLUMN_STATUS NOT IN ('${Status.COMPLETED.status}', '${Status.CANCELLED.status}')) "

            val dateQuery = mutableListOf<String>()
            if (isFilterStartInPast)
                dateQuery.add("$COLUMN_DTSTART < ${System.currentTimeMillis()}")
            if (isFilterStartToday)
                dateQuery.add("$COLUMN_DTSTART BETWEEN ${DateTimeUtils.getTodayAsLong()} AND ${DateTimeUtils.getTodayAsLong() + (1).days.inWholeMilliseconds - 1}")
            if (isFilterStartTomorrow)
                dateQuery.add("$COLUMN_DTSTART BETWEEN ${DateTimeUtils.getTodayAsLong() + (1).days.inWholeMilliseconds} AND ${DateTimeUtils.getTodayAsLong() + (2).days.inWholeMilliseconds - 1}")
            if (isFilterStartWithin7Days)
                dateQuery.add("$COLUMN_DTSTART BETWEEN ${DateTimeUtils.getTodayAsLong()} AND ${DateTimeUtils.getTodayAsLong() + (8).days.inWholeMilliseconds - 1}")
            if (isFilterStartFuture)
                dateQuery.add("$COLUMN_DTSTART > ${System.currentTimeMillis()}")
            if (isFilterOverdue)
                dateQuery.add("$COLUMN_DUE < ${System.currentTimeMillis()}")
            if (isFilterDueToday)
                dateQuery.add("$COLUMN_DUE BETWEEN ${DateTimeUtils.getTodayAsLong()} AND ${DateTimeUtils.getTodayAsLong() + (1).days.inWholeMilliseconds - 1}")
            if (isFilterDueTomorrow)
                dateQuery.add("$COLUMN_DUE BETWEEN ${DateTimeUtils.getTodayAsLong() + (1).days.inWholeMilliseconds} AND ${DateTimeUtils.getTodayAsLong() + (2).days.inWholeMilliseconds - 1}")
            if (isFilterDueWithin7Days)
                dateQuery.add("$COLUMN_DUE BETWEEN ${DateTimeUtils.getTodayAsLong()} AND ${DateTimeUtils.getTodayAsLong() + (8).days.inWholeMilliseconds - 1}")
            if (isFilterDueFuture)
                dateQuery.add("$COLUMN_DUE > ${System.currentTimeMillis()}")
            if (isFilterNoDatesSet)
                dateQuery.add("$COLUMN_DTSTART IS NULL AND $COLUMN_DUE IS NULL AND $COLUMN_COMPLETED IS NULL ")
            if (isFilterNoStartDateSet)
                dateQuery.add("$COLUMN_DTSTART IS NULL ")
            if (isFilterNoDueDateSet)
                dateQuery.add("$COLUMN_DUE IS NULL ")
            if (isFilterNoCompletedDateSet)
                dateQuery.add("$COLUMN_COMPLETED IS NULL ")

            if (dateQuery.isNotEmpty())
                queryString += " AND (${dateQuery.joinToString(separator = " OR ")}) "

            // DATE RANGE
            if(filterStartRangeStart != null || filterStartRangeEnd != null)
                queryString += " AND ($COLUMN_DTSTART BETWEEN ${filterStartRangeStart?:Long.MIN_VALUE} AND ${filterStartRangeEnd?.let { it + (1).days.inWholeMilliseconds-1 }?:Long.MAX_VALUE})"
            if(filterDueRangeStart != null || filterDueRangeEnd != null)
                queryString += " AND ($COLUMN_DUE BETWEEN ${filterDueRangeStart?:Long.MIN_VALUE} AND ${filterDueRangeEnd?.let { it + (1).days.inWholeMilliseconds-1 }?:Long.MAX_VALUE})"
            if(filterCompletedRangeStart != null || filterCompletedRangeEnd != null)
                queryString += " AND ($COLUMN_DTSTART BETWEEN ${filterCompletedRangeStart?:Long.MIN_VALUE} AND ${filterCompletedRangeEnd?.let { it + (1).days.inWholeMilliseconds-1 }?:Long.MAX_VALUE})"

            //CLASSIFICATION
            if (searchClassification.isNotEmpty()) {
                queryString += "AND ("
                queryString += searchClassification.joinToString(separator = "OR ", transform = { "$COLUMN_CLASSIFICATION = ? " })
                args.addAll(searchClassification.map { it.classification ?: ""})

                if(searchClassification.contains(Classification.NO_CLASSIFICATION))
                    queryString += "OR $COLUMN_CLASSIFICATION IS NULL"
                queryString += ") "
            }

            //PRIORITY
            if (searchPriority.isNotEmpty()) {
                queryString += "AND ("
                queryString += searchPriority.joinToString(separator = "OR ", transform = { "$COLUMN_PRIORITY = ? " })
                args.addAll(searchPriority.map { it?.toString() ?: "0"})

                if(searchPriority.contains(null) || searchPriority.contains(0))
                    queryString += "OR $COLUMN_PRIORITY IS NULL"
                queryString += ") "
            }

            //Hide biometric protected
            hideBiometricProtected.forEach {
                if(it == Classification.NO_CLASSIFICATION) {
                    queryString += "AND $COLUMN_CLASSIFICATION IS NOT NULL "
                } else {
                    queryString += "AND $COLUMN_CLASSIFICATION IS NOT ? "
                    args.add(it.classification?:"")
                }
            }

            //COLLECTION
            if (searchCollection.isNotEmpty()) {
                queryString += searchCollection.joinToString (
                    prefix = "AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_DISPLAYNAME IN (",
                    separator = ", ",
                    transform = { "?" },
                    postfix = ") "
                )
                args.addAll(searchCollection)
            }

            //ACCOUNT
            if (searchAccount.isNotEmpty()) {
                queryString += searchAccount.joinToString (
                    prefix = "AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ACCOUNT_NAME IN (",
                    separator = ", ",
                    transform = { "?" },
                    postfix = ") "
                )
                args.addAll(searchAccount)
            }

            // Exclude items that are Child items by checking if they appear in the linkedICalObjectId of relatedto!
            //queryString += "AND $VIEW_NAME_ICAL4LIST.$COLUMN_ID NOT IN (SELECT $COLUMN_RELATEDTO_LINKEDICALOBJECT_ID FROM $TABLE_NAME_RELATEDTO) "
            if (!flatView)
                queryString += "AND $VIEW_NAME_ICAL4LIST.isChildOfTodo = 0 AND $VIEW_NAME_ICAL4LIST.isChildOfJournal = 0 AND $VIEW_NAME_ICAL4LIST.isChildOfNote = 0 "

            if (searchSettingShowOneRecurEntryInFuture) {
                queryString += "AND ($VIEW_NAME_ICAL4LIST.$COLUMN_RECURID IS NULL " +
                        "OR $VIEW_NAME_ICAL4LIST.$COLUMN_DTSTART <= " +
                        "(SELECT MIN(recurList.$COLUMN_DTSTART) FROM $TABLE_NAME_ICALOBJECT as recurList WHERE recurList.$COLUMN_UID = $VIEW_NAME_ICAL4LIST.$COLUMN_UID AND recurList.$COLUMN_RECURID IS NOT NULL AND recurList.$COLUMN_DTSTART >= ${DateTimeUtils.getTodayAsLong()} )) "
            }

            queryString += "ORDER BY "
            queryString += orderBy.getQueryAppendix(sortOrder)

            queryString += ", "
            queryString += orderBy2.getQueryAppendix(sortOrder2)

            limit?.let { queryString += "LIMIT $it" }

            Log.println(Log.INFO, "queryString", queryString)
            //Log.println(Log.INFO, "queryStringArgs", args.joinToString(separator = ", "))
            return SimpleSQLiteQuery(queryString, args.toArray())
        }

        /**
         * Returns all sub-entries
         * @param component: Use Component.VTODO to get all subtasks, use Component.VJOURNAL to get all subnotes/subjournals
         * @param orderBy
         * @param sortOrder
         */
        fun getQueryForAllSubEntries(component: Component,
                                     hideBiometricProtected: List<Classification>,
                                     searchText: String?,
                                     orderBy: OrderBy,
                                     sortOrder: SortOrder
        ): SimpleSQLiteQuery {

            val queryArgs = mutableListOf<String>()
            var queryString = "SELECT DISTINCT $VIEW_NAME_ICAL4LIST.* " +
                    "from $VIEW_NAME_ICAL4LIST " +
                    "INNER JOIN $TABLE_NAME_RELATEDTO ON $VIEW_NAME_ICAL4LIST.$COLUMN_ID = $TABLE_NAME_RELATEDTO.$COLUMN_RELATEDTO_ICALOBJECT_ID " +
                    "WHERE $VIEW_NAME_ICAL4LIST.$COLUMN_COMPONENT = '$component' AND $TABLE_NAME_RELATEDTO.$COLUMN_RELATEDTO_RELTYPE = '${Reltype.PARENT.name}' "

            if(hideBiometricProtected.isNotEmpty()) {
                queryString += if(hideBiometricProtected.contains(Classification.NO_CLASSIFICATION)) {
                    "AND ($COLUMN_CLASSIFICATION IS NOT NULL AND $COLUMN_CLASSIFICATION NOT IN (${hideBiometricProtected.joinToString(separator = ",", transform = { "'${it.classification ?:""}'" })})) "
                } else {
                    "AND ($COLUMN_CLASSIFICATION IS NULL OR $COLUMN_CLASSIFICATION NOT IN (${hideBiometricProtected.joinToString(separator = ",", transform = { "'${it.classification ?:""}'" })})) "
                }
            }

            if ((searchText?.length?:0) >= 2) {
                queryString += "AND ($VIEW_NAME_ICAL4LIST.$COLUMN_SUMMARY LIKE ? OR $VIEW_NAME_ICAL4LIST.$COLUMN_DESCRIPTION LIKE ? )"
                queryArgs.add("%${searchText!!}%")
                queryArgs.add("%${searchText}%")
            }

            queryString += "ORDER BY ${orderBy.getQueryAppendix(sortOrder)}"

            return SimpleSQLiteQuery(queryString, queryArgs.toTypedArray())
        }

        fun getQueryForAllSubentriesForParentUID(
            parentUid: String,
            hideBiometricProtected: List<Classification>,
            component: Component,
            orderBy: OrderBy,
            sortOrder: SortOrder
        ): SimpleSQLiteQuery = SimpleSQLiteQuery("SELECT $VIEW_NAME_ICAL4LIST.* " +
                "from $VIEW_NAME_ICAL4LIST " +
                "INNER JOIN $TABLE_NAME_RELATEDTO ON ${TABLE_NAME_RELATEDTO}.${COLUMN_RELATEDTO_ICALOBJECT_ID} = ${VIEW_NAME_ICAL4LIST}.${COLUMN_ID} " +
                "WHERE $TABLE_NAME_RELATEDTO.$COLUMN_RELATEDTO_TEXT = '$parentUid' " +
                "AND $COLUMN_RELATEDTO_RELTYPE = 'PARENT' " +
                "AND $COLUMN_COMPONENT = '${component.name}' " +
                if(hideBiometricProtected.isNotEmpty()) {
                    if(hideBiometricProtected.contains(Classification.NO_CLASSIFICATION)) {
                        "AND ($COLUMN_CLASSIFICATION IS NOT NULL AND $COLUMN_CLASSIFICATION NOT IN (${hideBiometricProtected.joinToString(separator = ",", transform = { "'${it.classification ?:""}'" })})) "
                    } else {
                        "AND ($COLUMN_CLASSIFICATION IS NULL OR $COLUMN_CLASSIFICATION NOT IN (${hideBiometricProtected.joinToString(separator = ",", transform = { "'${it.classification ?:""}'" })})) "
                    }
                } else
                    ""
                + "ORDER BY ${orderBy.getQueryAppendix(sortOrder)}")

    }


    /**
     * @return the audioAttachment as Uri or null
     */
    fun getAudioAttachmentAsUri(): Uri? {
        return try {
            if(audioAttachment?.startsWith("content://") == true)
                Uri.parse(audioAttachment)
            else
                null
        } catch (e: Exception) {
            null
        }
    }

    /***
     * @return Module of the current entry
     */
    fun getModule() = when(this.module) {
        Module.TODO.name -> Module.TODO
        Module.JOURNAL.name -> Module.JOURNAL
        else -> Module.NOTE
    }

    fun getSplitCategories() = categories?.split(CONCAT_DELIMITER) ?: emptyList()
    //fun getSplitResources() = resources?.split(CONCAT_DELIMITER) ?: emptyList()

}