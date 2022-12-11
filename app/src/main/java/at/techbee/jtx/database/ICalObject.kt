/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.provider.BaseColumns
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.graphics.Color
import androidx.core.util.PatternsCompat
import androidx.preference.PreferenceManager
import androidx.room.*
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.AlarmRelativeTo
import at.techbee.jtx.database.properties.Relatedto
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.ui.settings.DropdownSetting
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.DateTimeUtils.addLongToCSVString
import at.techbee.jtx.util.DateTimeUtils.convertLongToFullDateTimeString
import at.techbee.jtx.util.DateTimeUtils.getLongListfromCSVString
import at.techbee.jtx.util.DateTimeUtils.requireTzId
import at.techbee.jtx.util.UiUtil.asDayOfWeek
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import net.fortuna.ical4j.model.*
import net.fortuna.ical4j.model.Date
import net.fortuna.ical4j.model.property.DtStart
import java.text.ParseException
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.TimeZone
import kotlin.time.Duration


/** The name of the the table for IcalObjects.
 * This is a general purpose table containing general columns
 * for Journals, Notes and Todos */
const val TABLE_NAME_ICALOBJECT = "icalobject"

/** The name of the ID column.
 * This is the unique identifier of an ICalObject
 * Type: [Long]*/
const val COLUMN_ID = BaseColumns._ID

/** The column for the module.
 * This is an internal differentiation for JOURNAL, NOTE and TODOs
 * provided in the enum [Module]
 * Type: [String]
 */
const val COLUMN_MODULE = "module"

/* The names of all the other columns  */
/** The column for the component based on the values
 * provided in the enum [Component]
 * Type: [String]
 */
const val COLUMN_COMPONENT = "component"

/**
 * Purpose:  This column/property defines a short summary or subject for the calendar component.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.1.12]
 * Type: [String]
 */
const val COLUMN_SUMMARY = "summary"

/**
 * Purpose:  This column/property provides a more complete description of the calendar component than that provided by the "SUMMARY" property.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.1.5]
 * Type: [String]
 */
const val COLUMN_DESCRIPTION = "description"

/**
 * Purpose:  This column/property specifies when the calendar component begins.
 * The corresponding timezone is stored in [COLUMN_DTSTART_TIMEZONE].
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.2.4]
 * Type: [Long]
 */
const val COLUMN_DTSTART = "dtstart"

/**
 * Purpose:  This column/property specifies the timezone of when the calendar component begins.
 * The corresponding datetime is stored in [COLUMN_DTSTART].
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.2.4]
 * Type: [String]
 */
const val COLUMN_DTSTART_TIMEZONE = "dtstarttimezone"

/**
 * Purpose:  This column/property specifies when the calendar component ends.
 * The corresponding timezone is stored in [COLUMN_DTEND_TIMEZONE].
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.2.4]
 * Type: [Long]
 */
const val COLUMN_DTEND = "dtend"

/**
 * Purpose:  This column/property specifies the timezone of when the calendar component ends.
 * The corresponding datetime is stored in [COLUMN_DTEND].
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.2.2]
 * Type: [String]
 */
const val COLUMN_DTEND_TIMEZONE = "dtendtimezone"

/**
 * Purpose:  This property defines the overall status or confirmation for the calendar component.
 * The possible values of a status are defined in [StatusTodo] for To-Dos and in [StatusJournal] for Notes and Journals
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.1.11]
 * Type: [String]
 */
const val COLUMN_STATUS = "status"

/**
 * Purpose:  This property defines the access classification for a calendar component.
 * The possible values of a status are defined in the enum [Classification].
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.1.11]
 * Type: [String]
 */
const val COLUMN_CLASSIFICATION = "classification"

/**
 * Purpose:  This property defines a Uniform Resource Locator (URL) associated with the iCalendar object.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.4.6]
 * Type: [String]
 */
const val COLUMN_URL = "url"

/**
 * Purpose:  This property is used to represent contact information or alternately a reference
 * to contact information associated with the calendar component.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.4.2]
 * Type: [String]
 */
const val COLUMN_CONTACT = "contact"

/**
 * Purpose:  This property specifies information related to the global position for the activity specified by a calendar component.
 * This property is split in the fields [COLUMN_GEO_LAT] for the latitude
 * and [COLUMN_GEO_LONG] for the longitude coordinates.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.1.6]
 * Type: [Double]
 */
const val COLUMN_GEO_LAT = "geolat"

/**
 * Purpose:  This property specifies information related to the global position for the activity specified by a calendar component.
 * This property is split in the fields [COLUMN_GEO_LAT] for the latitude
 * and [COLUMN_GEO_LONG] for the longitude coordinates.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.1.6]
 * Type: [Double]
 */
const val COLUMN_GEO_LONG = "geolong"

/**
 * Purpose:  This property defines the intended venue for the activity defined by a calendar component.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.1.7]
 * Type: [String]
 */
const val COLUMN_LOCATION = "location"

/**
 * Purpose:  This property defines the alternative representation of the intended venue for the activity defined by a calendar component.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.1.7]
 * Type: [String]
 */
const val COLUMN_LOCATION_ALTREP = "locationaltrep"

/**
 * Purpose:  This property is used by an assignee or delegatee of a to-do to convey the percent completion of a to-do to the "Organizer".
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.1.8]
 * Type: [Int]
 */
const val COLUMN_PERCENT = "percent"

/**
 * Purpose:  This property defines the relative priority for a calendar component.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.1.9]
 * Type: [Int]
 */
const val COLUMN_PRIORITY = "priority"

/**
 * Purpose:  This property defines the date and time that a to-do is expected to be completed.
 * The corresponding timezone is stored in [COLUMN_DUE_TIMEZONE].
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.2.3]
 * Type: [Long]
 */
const val COLUMN_DUE = "due"

/**
 * Purpose:  This column/property specifies the timezone of when a to-do is expected to be completed.
 * The corresponding datetime is stored in [COLUMN_DUE].
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.2.2]
 * Type: [String]
 */
const val COLUMN_DUE_TIMEZONE = "duetimezone"

/**
 * Purpose:  This property defines the date and time that a to-do was actually completed.
 * The corresponding timezone is stored in [COLUMN_COMPLETED_TIMEZONE].
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.2.1]
 * Type: [Long]
 */
const val COLUMN_COMPLETED = "completed"

/**
 * Purpose:  This column/property specifies the timezone of when a to-do was actually completed.
 * The corresponding datetime is stored in [COLUMN_DUE].
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.2.1]
 * Type: [String]
 */
const val COLUMN_COMPLETED_TIMEZONE = "completedtimezone"

/**
 * Purpose:  This property specifies a positive duration of time.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.2.5]
 * Type: [String]
 */
const val COLUMN_DURATION = "duration"

/**
 * Purpose:  This property defines the persistent, globally unique identifier for the calendar component.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.4.7]
 * Type: [String]
 */
const val COLUMN_UID = "uid"

/**
 * Purpose:  This property specifies the date and time that the calendar information
 * was created by the calendar user agent in the calendar store.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.7.1]
 * Type: [Long]
 */
const val COLUMN_CREATED = "created"

/**
 * Purpose:  In the case of an iCalendar object that specifies a
 * "METHOD" property, this property specifies the date and time that
 * the instance of the iCalendar object was created.  In the case of
 * an iCalendar object that doesn't specify a "METHOD" property, this
 * property specifies the date and time that the information
 * associated with the calendar component was last revised in the
 * calendar store.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.7.2]
 * Type: [Long]
 */
const val COLUMN_DTSTAMP = "dtstamp"

/**
 * Purpose:  This property specifies the date and time that the information associated
 * with the calendar component was last revised in the calendar store.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.7.3]
 * Type: [Long]
 */
const val COLUMN_LAST_MODIFIED = "lastmodified"

/**
 * Purpose:  This property defines the revision sequence number of the calendar component within a sequence of revisions.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.7.4]
 * Type: [Int]
 */
const val COLUMN_SEQUENCE = "sequence"


/**
 * Purpose:  This property defines a rule or repeating pattern for recurring events,
 * to-dos, journal entries, or time zone definitions.
 * Type: [String]
 */
const val COLUMN_RRULE = "rrule"

/**
 * Purpose:  This property defines the list of DATE-TIME values for
 * recurring events, to-dos, journal entries, or time zone definitions.
 * Type: [String], contains a list of comma-separated date values as Long
 */
const val COLUMN_RDATE = "rdate"

/**
 * Purpose:  This property defines the list of DATE-TIME exceptions for
 * recurring events, to-dos, journal entries, or time zone definitions.
 * Type: [String], contains a list of comma-separated date values as Long
 */
const val COLUMN_EXDATE = "exdate"

/**
 * Purpose:  This property is used in conjunction with the "UID" and
 * "SEQUENCE" properties to identify a specific instance of a
 * recurring "VEVENT", "VTODO", or "VJOURNAL" calendar component.
 * The property value is the original value of the "DTSTART" property
 * of the recurrence instance.
 */
const val COLUMN_RECURID = "recurid"

/**
 * Purpose:  This property is used to return status code information
related to the processing of an associated iCalendar object.  The
value type for this property is TEXT.
 */
const val COLUMN_RSTATUS = "rstatus"

/**
 * Stores the reference to the original event from which the recurring event was derived.
 * This value is NULL for the orignal event.
 * Type: [Long]
 */
const val COLUMN_RECUR_ORIGINALICALOBJECTID = "recur_original_icalobjectid"

/**
 * Marks recurring instances that have not been changed. Those must be excluded from the sync as they are still instances of the original item.
 * Type: [Boolean]
 */
const val COLUMN_RECUR_ISLINKEDINSTANCE = "recur_islinkedinstance"


/**
 * Purpose:  This property specifies a color used for displaying the calendar, event, todo, or journal data.
 * See [https://tools.ietf.org/html/rfc7986#section-5.9]
 * Type: [String]
 */
const val COLUMN_COLOR = "color"



/**
 * Purpose:  This column is the foreign key to the [TABLE_NAME_COLLECTION].
 * Type: [Long]
 */
const val COLUMN_ICALOBJECT_COLLECTIONID = "collectionId"

/**
 * Purpose:  This column defines if a local collection was changed that is supposed to be synchronised.
 * Type: [Boolean]
 */
const val COLUMN_DIRTY = "dirty"

/**
 * Purpose:  This column defines if a collection that is supposed to be synchonized was locally marked as deleted.
 * Type: [Boolean]
 */
const val COLUMN_DELETED = "deleted"


/**
 * Purpose:  filename of the synched entry (*.ics), only relevant for synched entries through sync-adapter
 * Type: [String]
 */
const val COLUMN_FILENAME = "filename"

/**
 * Purpose:  eTag for SyncAdapter, only relevant for synched entries through sync-adapter
 * Type: [String]
 */
const val COLUMN_ETAG = "etag"

/**
 * Purpose:  scheduleTag for SyncAdapter, only relevant for synched entries through sync-adapter
 * Type: [String]
 */
const val COLUMN_SCHEDULETAG = "scheduletag"

/**
 * Purpose:  flags for SyncAdapter, only relevant for synched entries through sync-adapter
 * Type: [Int]
 */
const val COLUMN_FLAGS = "flags"

/**
 * Purpose:  defines if the subtasks for this entry are expanded
 * Type: [Boolean?]
 */
const val COLUMN_SUBTASKS_EXPANDED = "subtasksExpanded"

/**
 * Purpose:  defines if the subnotes for this entry are expanded
 * Type: [Boolean?]
 */
const val COLUMN_SUBNOTES_EXPANDED = "subnotesExpanded"

/**
 * Purpose:  defines if the attachments for this entry are expanded
 * Type: [Boolean?]
 */
const val COLUMN_ATTACHMENTS_EXPANDED = "attachmentsExpanded"

/**
 * Purpose:  defines if the order index especially for subtasks and subnotes
 * Type: [Int?]
 */
const val COLUMN_SORT_INDEX = "sortIndex"

@Parcelize
@Entity(
    tableName = TABLE_NAME_ICALOBJECT,
    indices = [Index(value = ["_id", "summary", "description"])],
    foreignKeys = [ForeignKey(
        entity = ICalCollection::class,
        parentColumns = arrayOf(COLUMN_COLLECTION_ID),
        childColumns = arrayOf(COLUMN_ICALOBJECT_COLLECTIONID),
        onDelete = ForeignKey.CASCADE
    )]
)
data class ICalObject(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_ID)
    var id: Long = 0L,

    @ColumnInfo(name = COLUMN_MODULE) var module: String = Module.NOTE.name,
    @ColumnInfo(name = COLUMN_COMPONENT) var component: String = Component.VJOURNAL.name,
    @ColumnInfo(name = COLUMN_SUMMARY) var summary: String? = null,
    @ColumnInfo(name = COLUMN_DESCRIPTION) var description: String? = null,
    @ColumnInfo(name = COLUMN_DTSTART) var dtstart: Long? = null,
    @ColumnInfo(name = COLUMN_DTSTART_TIMEZONE) var dtstartTimezone: String? = null,

    @ColumnInfo(name = COLUMN_DTEND) var dtend: Long? = null,
    @ColumnInfo(name = COLUMN_DTEND_TIMEZONE) var dtendTimezone: String? = null,

    @ColumnInfo(name = COLUMN_STATUS) var status: String? = null,
    @ColumnInfo(name = COLUMN_CLASSIFICATION) var classification: String? = null,

    @ColumnInfo(name = COLUMN_URL) var url: String? = null,
    @ColumnInfo(name = COLUMN_CONTACT) var contact: String? = null,
    @ColumnInfo(name = COLUMN_GEO_LAT) var geoLat: Double? = null,
    @ColumnInfo(name = COLUMN_GEO_LONG) var geoLong: Double? = null,
    @ColumnInfo(name = COLUMN_LOCATION) var location: String? = null,
    @ColumnInfo(name = COLUMN_LOCATION_ALTREP) var locationAltrep: String? = null,

    @ColumnInfo(name = COLUMN_PERCENT) var percent: Int? = null,    // VTODO only!
    @ColumnInfo(name = COLUMN_PRIORITY) var priority: Int? = null,   // VTODO and VEVENT

    @ColumnInfo(name = COLUMN_DUE) var due: Long? = null,      // VTODO only!
    @ColumnInfo(name = COLUMN_DUE_TIMEZONE) var dueTimezone: String? = null, //VTODO only!
    @ColumnInfo(name = COLUMN_COMPLETED) var completed: Long? = null, // VTODO only!
    @ColumnInfo(name = COLUMN_COMPLETED_TIMEZONE) var completedTimezone: String? = null, //VTODO only!
    @ColumnInfo(name = COLUMN_DURATION) var duration: String? = null, //VTODO only!


    @ColumnInfo(name = COLUMN_UID) var uid: String = generateNewUID(),                              //unique identifier, see https://tools.ietf.org/html/rfc5545#section-3.8.4.7

    /*
     The following properties specify change management information in  calendar components.
     https://tools.ietf.org/html/rfc5545#section-3.8.7
     */
    @ColumnInfo(name = COLUMN_CREATED) var created: Long = System.currentTimeMillis(),   // see https://tools.ietf.org/html/rfc5545#section-3.8.7.1
    @ColumnInfo(name = COLUMN_DTSTAMP) var dtstamp: Long = System.currentTimeMillis(),   // see https://tools.ietf.org/html/rfc5545#section-3.8.7.2
    @ColumnInfo(name = COLUMN_LAST_MODIFIED) var lastModified: Long = System.currentTimeMillis(), // see https://tools.ietf.org/html/rfc5545#section-3.8.7.3
    @ColumnInfo(name = COLUMN_SEQUENCE) var sequence: Long = 0,                             // increase on every change (+1), see https://tools.ietf.org/html/rfc5545#section-3.8.7.4

    @ColumnInfo(name = COLUMN_RRULE)  var rrule: String? = null,    //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5.3
    @ColumnInfo(name = COLUMN_EXDATE) var exdate: String? = null,   //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5.1
    @ColumnInfo(name = COLUMN_RDATE)  var rdate: String? = null,     //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5.2
    @ColumnInfo(name = COLUMN_RECURID) var recurid: String? = null,                          //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5
    @ColumnInfo(name = COLUMN_RECUR_ORIGINALICALOBJECTID) var recurOriginalIcalObjectId: Long? = null,
    @ColumnInfo(name = COLUMN_RECUR_ISLINKEDINSTANCE) var isRecurLinkedInstance: Boolean = false,

    @ColumnInfo(name = COLUMN_RSTATUS) var rstatus: String? = null,

    @ColumnInfo(name = COLUMN_COLOR)
    @ColorInt
    var color: Int? = null,

    @ColumnInfo(index = true, name = COLUMN_ICALOBJECT_COLLECTIONID) var collectionId: Long = 1L,

    @ColumnInfo(name = COLUMN_DIRTY) var dirty: Boolean = false,
    @ColumnInfo(name = COLUMN_DELETED) var deleted: Boolean = false,

    @ColumnInfo(name = COLUMN_FILENAME) var fileName: String? = null,
    @ColumnInfo(name = COLUMN_ETAG) var eTag: String? = null,
    @ColumnInfo(name = COLUMN_SCHEDULETAG) var scheduleTag: String? = null,
    @ColumnInfo(name = COLUMN_FLAGS) var flags: Int? = null,

    @ColumnInfo(name = COLUMN_SUBTASKS_EXPANDED) var isSubtasksExpanded: Boolean? = null,
    @ColumnInfo(name = COLUMN_SUBNOTES_EXPANDED) var isSubnotesExpanded: Boolean? = null,
    @ColumnInfo(name = COLUMN_ATTACHMENTS_EXPANDED) var isAttachmentsExpanded: Boolean? = null,
    @ColumnInfo(name = COLUMN_SORT_INDEX) var sortIndex: Int? = null

) : Parcelable {


    companion object {

        const val TZ_ALLDAY = "ALLDAY"
        const val DEFAULT_MAX_RECUR_INSTANCES = 100
        val defaultColors = arrayListOf(
            Color.Transparent,
            Color.Red,
            Color.Green,
            Color.Blue,
            Color.Yellow,
            Color.Cyan,
            Color.Magenta,
            Color.LightGray
        )

        fun createJournal() = createJournal(null)

        fun createJournal(summary: String?): ICalObject = ICalObject(
            component = Component.VJOURNAL.name,
            module = Module.JOURNAL.name,
            dtstart = DateTimeUtils.getTodayAsLong(),
            dtstartTimezone = TZ_ALLDAY,
            status = StatusJournal.FINAL.name,
            summary = summary,
            dirty = true
        )

        fun createNote() = createNote(null)

        fun createNote(summary: String?) = ICalObject(
            component = Component.VJOURNAL.name,
            module = Module.NOTE.name,
            status = StatusJournal.FINAL.name,
            summary = summary,
            dirty = true
        )

        fun createTodo() = createTask(null)

        fun createTask(summary: String?) = ICalObject(
            component = Component.VTODO.name,
            module = Module.TODO.name,
            summary = summary,
            status = null,
            percent = null,
            priority = null,
            dueTimezone = TZ_ALLDAY,
            dtstartTimezone = TZ_ALLDAY,
            completedTimezone = TZ_ALLDAY,
            dirty = true
        )

        fun generateNewUID() = UUID.randomUUID().toString()

        /**
         * Create a new [ICalObject] from the specified [ContentValues].
         *
         * @param values A [ICalObject] that at least contain [.COLUMN_NAME].
         * @return A newly created [ICalObject] instance.
         */
        fun fromContentValues(values: ContentValues?): ICalObject? {

            if (values == null)
                return null

            if (values.getAsLong(COLUMN_ICALOBJECT_COLLECTIONID) == null)
                throw IllegalArgumentException("CollectionId cannot be null.")

            return ICalObject().applyContentValues(values)
        }


        fun getRecurId(dtstart: Long?, dtstartTimezone: String?): String? {
            if(dtstart == null)
                return null

            return when {
                dtstartTimezone == TZ_ALLDAY -> DtStart(Date(dtstart)).value
                dtstartTimezone.isNullOrEmpty() -> DtStart(DateTime(dtstart)).value
                else -> {
                    val timezone = TimeZoneRegistryFactory.getInstance().createRegistry()
                        .getTimeZone(dtstartTimezone)
                    val withTimezone = DtStart(DateTime(dtstart))
                    withTimezone.timeZone = timezone
                    withTimezone.value + withTimezone.parameters
                }
            }
        }


        /**
         * this function takes a parent [id], the function recursively calls itself and deletes all items and linked children (for local collections)
         * or updates the linked children and marks them as deleted.
         */
        suspend fun deleteItemWithChildren(id: Long, database: ICalDatabaseDao) {

            if (id == 0L)
                return // do nothing, the item was never saved in DB

            val children = database.getRelatedChildren(id)
            children.forEach { childId ->
                    deleteItemWithChildren(childId, database)    // call the function again to recursively delete all children, then delete the item
            }

            database.deleteRecurringInstances(id)      // recurring instances are always physically deleted
            val item = database.getSync(id)?: return   // if the item could not be found, just return (this can happen on mass deletion from the list view, when a recur-instance was passed to delete, but it was already deleted through the original entry
            when {
                item.property.isRecurLinkedInstance -> {
                    makeRecurringException(item.property, database)   // if the current item
                    database.deleteICalObjectsbyId(id)
                }
                item.ICalCollection?.accountType == LOCAL_ACCOUNT_TYPE -> database.deleteICalObjectsbyId(item.property.id) // Elements in local collection are physically deleted
                else -> database.updateToDeleted(item.property.id, System.currentTimeMillis())
            }
        }


        /**
         * @param [id] the id of the item for which the collection needs to be updated
         * @param [parentId] is needed for the recursive call in order to provide it for the movItemToNewCollection(...) function. For the initial call this would be null as the function should initially always be called from the top parent.
         *
         * this function takes care of
         * 1. moving the item to a new collection (by copying and deleting the current item)
         * 2. determining the children of this item and calling itself recusively to to the same again for each child.
         *
         * @return The new id of the item in the new collection
         */
        suspend fun updateCollectionWithChildren(id: Long, parentId: Long?, newCollectionId: Long, database: ICalDatabaseDao, context: Context): Long {

            val newParentId = moveItemToNewCollection(id, parentId, newCollectionId, database, context)

            // then determine the children and recursively call the function again. The possible child becomes the new parent and is added to the list until there are no more children.
            val children = database.getRelatedChildren(id)
            children.forEach { childId ->
                updateCollectionWithChildren(childId, newParentId, newCollectionId, database, context)
            }
            return newParentId
        }


        /**
         * @param [id] is the id of the original item that should be moved to another collection. On the recursive call this is the id of the original child.
         * @param [newParentId] is the id of the parent that was already copied into the new collection. This is needed in order to re-create the relation between the parent and the child.
         *
         * This function creates a copy of an item with all it's children in the new collection and then
         * deletes (or marks as deleted) the original item.
         *
         * @return the new id of the item that was inserted (that becomes the newParentId)
         *
         */
        private suspend fun moveItemToNewCollection(id: Long, newParentId: Long?, newCollectionId: Long, database: ICalDatabaseDao, context: Context): Long =
            withContext(Dispatchers.IO) {

                val item = database.getSync(id)
                if (item != null) {

                    item.property.id = 0L
                    item.property.collectionId = newCollectionId
                    item.property.sequence = 0
                    item.property.dirty = true
                    item.property.lastModified = System.currentTimeMillis()
                    item.property.created = System.currentTimeMillis()
                    item.property.dtstamp = System.currentTimeMillis()
                    item.property.uid = generateNewUID()
                    item.property.flags = null
                    item.property.scheduleTag = null
                    item.property.eTag = null
                    item.property.fileName = null

                    val newId = database.insertICalObject(item.property)

                    item.attendees?.forEach {
                        it.icalObjectId = newId
                        database.insertAttendee(it)
                    }

                    item.resources?.forEach {
                        it.icalObjectId = newId
                        database.insertResource(it)
                    }

                    item.categories?.forEach {
                        it.icalObjectId = newId
                        database.insertCategory(it)
                    }

                    item.comments?.forEach {
                        it.icalObjectId = newId
                        database.insertComment(it)
                    }

                    if (item.organizer?.caladdress != null) {
                        item.organizer?.icalObjectId = newId
                        database.insertOrganizer(item.organizer!!)
                    }

                    item.attachments?.forEach {
                        it.icalObjectId = newId
                        database.insertAttachment(it)
                    }

                    item.alarms?.forEach {
                        it.icalObjectId = newId
                        database.insertAlarm(it)
                    }

                    // relations need to be rebuilt from the new child to the parent
                    if (newParentId != null) {
                        val parent = database.getSync(newParentId)
                        val relParent2Child = Relatedto()
                        relParent2Child.icalObjectId = newId
                        relParent2Child.reltype = Reltype.PARENT.name
                        relParent2Child.text = parent?.property?.uid
                        database.insertRelatedto(relParent2Child)
                    }
                    Alarm.scheduleNextNotifications(context)
                    return@withContext newId
                }
                return@withContext 0L
            }

        fun makeRecurringException(item: ICalObject, database: ICalDatabaseDao) {
            if(item.isRecurLinkedInstance) {

                item.recurOriginalIcalObjectId?.let { originalId ->
                    val newExceptionList = addLongToCSVString(database.getRecurExceptions(originalId), item.dtstart)
                    database.setRecurExceptions(
                        originalId,
                        newExceptionList,
                        System.currentTimeMillis()
                    )
                    database.setAsRecurException(item.id, System.currentTimeMillis())
                }
            }
        }

        /**
         * This function checks if the given timezone is a timezone that can be processed by the app
         * @param [tz] the timezone that needs to be validated
         * @return If the string is null or TZ_ALLDAY, then the input parameter is just returned.
         * Else the timezone is checked if it can be transformed into a Java Timezone and the
         * Id of the timezone is returned. This would be the same as the input or GMT if the
         * Timezone is unknown.
         */
        @VisibleForTesting
        fun getValidTimezoneOrNull(tz: String?): String? {

            if(tz == null || tz == TZ_ALLDAY)
                return tz

            return TimeZone.getTimeZone(tz).id
        }


        fun getMapLink(geoLat: Double?, geoLong: Double?, flavor: String): Uri? {
            return if(geoLat != null || geoLong != null) {
                try {
                    if (flavor == MainActivity2.BUILD_FLAVOR_GOOGLEPLAY)
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=$geoLat%2C$geoLong")
                    else
                        Uri.parse("https://www.openstreetmap.org/#map=15/$geoLat/$geoLong")
                } catch (e: java.lang.IllegalArgumentException) { null }
            } else null
        }

        /**
         * @param geoLat  Latitude as Double
         * @param geoLong  Longitude as Double
         * @return A textual representation of the Latitude and Logitude e.g. (1.234, 5.677)
         */
        fun getLatLongString(geoLat: Double?, geoLong: Double?): String? {
            return if(geoLat != null && geoLong != null) {
                "(${String.format("%.5f", geoLat)}, ${String.format("%.5f", geoLong)})"
            } else {
               null
            }
        }

        /**
         * @return true if the current entry is overdue and not completed,
         * null if no due date is set and not completed, false otherwise
         */
        fun isOverdue(percent: Int?, due: Long?, dueTimezone: String?): Boolean? {

            if(percent == 100)
                return false
            if(due == null)
                return null

            val zonedDue = ZonedDateTime.ofInstant(Instant.ofEpochMilli(due), requireTzId(dueTimezone)).toInstant().toEpochMilli()
            val millisLeft = if(dueTimezone == TZ_ALLDAY) zonedDue - DateTimeUtils.getTodayAsLong() else zonedDue - System.currentTimeMillis()

            return millisLeft < 0L
        }

        fun getDtstartTextInfo(module: Module, dtstart: Long?, dtstartTimezone: String?, daysOnly: Boolean = false, context: Context): String {

            if(dtstart == null && module == Module.TODO)
                return context.getString(R.string.list_start_without)
            else if(dtstart == null)
                return context.getString(R.string.list_date_without)

            val localNow = LocalDateTime.now()
            val localTomorrow = localNow.plusDays(1)
            val localStart = LocalDateTime.ofInstant(Instant.ofEpochMilli(dtstart), requireTzId(dtstartTimezone))
            val daysLeft = ChronoUnit.DAYS.between(localNow, localStart)
            val hoursLeft = ChronoUnit.HOURS.between(localNow, localStart)

            return if(module == Module.TODO) {
                 when {
                    localStart.year == localNow.year && localStart.month == localNow.month && localStart.dayOfMonth == localNow.dayOfMonth && (daysOnly || dtstartTimezone == TZ_ALLDAY) -> context.getString(R.string.list_start_today)
                    daysLeft <= 0L && hoursLeft < 0L -> context.getString(R.string.list_start_past)
                    localStart.year == localNow.year && localStart.month == localNow.month && localStart.dayOfMonth == localNow.dayOfMonth -> context.getString(R.string.list_start_inXhours, hoursLeft)
                    localStart.year == localTomorrow.year && localStart.month == localTomorrow.month && localStart.dayOfMonth == localTomorrow.dayOfMonth -> context.getString(R.string.list_start_tomorrow)
                    else -> context.getString(R.string.list_start_inXdays, daysLeft+1)
                }
            } else {
                when {
                    localStart.year == localNow.year && localStart.month == localNow.month && localStart.dayOfMonth == localNow.dayOfMonth -> context.getString(R.string.list_date_today)
                    localStart.year == localTomorrow.year && localStart.month == localTomorrow.month && localStart.dayOfMonth == localTomorrow.dayOfMonth -> context.getString(R.string.list_date_tomorrow)
                    else -> DateTimeUtils.convertLongToMediumDateString(dtstart, dtstartTimezone)
                }
            }
        }

        fun getDueTextInfo(due: Long?, dueTimezone: String?, percent: Int?, daysOnly: Boolean = false, context: Context): String {

            if(percent == 100)
                return context.getString(R.string.completed)
            if(due == null)
                return context.getString(R.string.list_due_without)

            val localNow = LocalDateTime.now()
            val localTomorrow = localNow.plusDays(1)
            val localDue = LocalDateTime.ofInstant(Instant.ofEpochMilli(due), requireTzId(dueTimezone))
            val daysLeft = ChronoUnit.DAYS.between(localNow, localDue)
            val hoursLeft = ChronoUnit.HOURS.between(localNow, localDue)

            return when {
                localDue.year == localNow.year && localDue.month == localNow.month && localDue.dayOfMonth == localNow.dayOfMonth && (daysOnly || dueTimezone == TZ_ALLDAY) -> context.getString(R.string.list_due_today)
                daysLeft <= 0L && hoursLeft < 0L -> context.getString(R.string.list_due_overdue)
                localDue.year == localNow.year && localDue.month == localNow.month && localDue.dayOfMonth == localNow.dayOfMonth -> context.getString(R.string.list_due_inXhours, hoursLeft)
                localDue.year == localTomorrow.year && localDue.month == localTomorrow.month && localDue.dayOfMonth == localTomorrow.dayOfMonth -> context.getString(R.string.list_due_tomorrow)
                else -> context.getString(R.string.list_due_inXdays, daysLeft+1)
            }
        }
    }


    fun applyContentValues(values: ContentValues): ICalObject {

        values.getAsString(COLUMN_COMPONENT)?.let { component -> this.component = component }
        values.getAsString(COLUMN_SUMMARY)?.let { summary -> this.summary = summary }
        values.getAsString(COLUMN_DESCRIPTION)
            ?.let { description -> this.description = description }
        values.getAsLong(COLUMN_DTSTART)?.let { dtstart ->
            this.dtstart = dtstart
        }
        values.getAsString(COLUMN_DTSTART_TIMEZONE)?.let { dtstartTimezone ->
            this.dtstartTimezone = getValidTimezoneOrNull(dtstartTimezone)
        }
        values.getAsLong(COLUMN_DTEND)?.let { dtend -> this.dtend = dtend }
        values.getAsString(COLUMN_DTEND_TIMEZONE)?.let { dtendTimezone ->
            this.dtendTimezone = getValidTimezoneOrNull(dtendTimezone)
        }
        values.getAsString(COLUMN_STATUS)?.let { status -> this.status = status }
        values.getAsString(COLUMN_CLASSIFICATION)
            ?.let { classification -> this.classification = classification }
        values.getAsString(COLUMN_URL)?.let { url -> this.url = url }
        values.getAsString(COLUMN_CONTACT)?.let { contact -> this.contact = contact }
        values.getAsDouble(COLUMN_GEO_LAT)?.let { geoLat -> this.geoLat = geoLat }
        values.getAsDouble(COLUMN_GEO_LONG)?.let { geoLong -> this.geoLong = geoLong }
        values.getAsString(COLUMN_LOCATION)?.let { location -> this.location = location }
        values.getAsString(COLUMN_LOCATION_ALTREP)?.let { locationAltrep -> this.locationAltrep = locationAltrep }
        values.getAsInteger(COLUMN_PERCENT)?.let { percent ->
            if(percent in 1..100)
                this.percent = percent
            else
                this.percent = null }
        values.getAsInteger(COLUMN_PRIORITY)?.let { priority -> this.priority = priority }
        values.getAsLong(COLUMN_DUE)?.let { due -> this.due = due }
        values.getAsString(COLUMN_DUE_TIMEZONE)
            ?.let { dueTimezone -> this.dueTimezone = getValidTimezoneOrNull(dueTimezone) }
        values.getAsLong(COLUMN_COMPLETED)?.let { completed -> this.completed = completed }
        values.getAsString(COLUMN_COMPLETED_TIMEZONE)
            ?.let { completedTimezone -> this.completedTimezone = getValidTimezoneOrNull(completedTimezone) }
        values.getAsString(COLUMN_DURATION)?.let { duration -> this.duration = duration }
        values.getAsString(COLUMN_RRULE)?.let { rrule -> this.rrule = rrule }
        values.getAsString(COLUMN_RDATE)?.let { rdate -> this.rdate = rdate }
        values.getAsString(COLUMN_EXDATE)?.let { exdate -> this.exdate = exdate }
        values.getAsString(COLUMN_RECURID)?.let { recurid -> this.recurid = recurid }
        values.getAsString(COLUMN_RSTATUS)?.let { rstatus -> this.rstatus = rstatus }
        values.getAsString(COLUMN_UID)?.let { uid -> this.uid = uid }
        values.getAsLong(COLUMN_CREATED)?.let { created -> this.created = created }
        values.getAsLong(COLUMN_DTSTAMP)?.let { dtstamp -> this.dtstamp = dtstamp }
        values.getAsLong(COLUMN_LAST_MODIFIED)
            ?.let { lastModified -> this.lastModified = lastModified }
        values.getAsLong(COLUMN_SEQUENCE)?.let { sequence -> this.sequence = sequence }
        values.getAsInteger(COLUMN_COLOR)?.let { color -> this.color = color }
        //values.getAsString(COLUMN_OTHER)?.let { other -> this.other = other }
        values.getAsLong(COLUMN_ICALOBJECT_COLLECTIONID)
            ?.let { collectionId -> this.collectionId = collectionId }
        values.getAsString(COLUMN_DIRTY)?.let { dirty -> this.dirty = dirty == "1" || dirty == "true" }
        values.getAsString(COLUMN_DELETED)?.let { deleted -> this.deleted = deleted == "1" || deleted == "true" }
        values.getAsString(COLUMN_FILENAME)?.let { fileName -> this.fileName = fileName }
        values.getAsString(COLUMN_ETAG)?.let { eTag -> this.eTag = eTag }
        values.getAsString(COLUMN_SCHEDULETAG)
            ?.let { scheduleTag -> this.scheduleTag = scheduleTag }
        values.getAsInteger(COLUMN_FLAGS)?.let { flags -> this.flags = flags }


        if (this.component == Component.VJOURNAL.name && this.dtstart != null)
            this.module = Module.JOURNAL.name
        else if (this.component == Component.VJOURNAL.name && this.dtstart == null)
            this.module = Module.NOTE.name
        else if (this.component == Component.VTODO.name)
            this.module = Module.TODO.name
        else
            throw IllegalArgumentException("Unsupported component: ${this.component}. Supported components: ${Component.values()}.")

        return this
    }


    fun setUpdatedProgress(newPercent: Int?) {

        percent = if(newPercent == 0) null else newPercent
        if(status?.isNotEmpty() == true)                   // we only update the status if it was set to a value, if it's null, we skip this part
            status = when (newPercent) {
                100 -> StatusTodo.COMPLETED.name
                in 1..99 -> StatusTodo.`IN-PROCESS`.name
                0 -> StatusTodo.`NEEDS-ACTION`.name
                else -> StatusTodo.`NEEDS-ACTION`.name      // should never happen!
            }

        if (completed == null && percent == 100) {
            completedTimezone = dueTimezone?:dtstartTimezone
            completed = if(completedTimezone == TZ_ALLDAY)
                DateTimeUtils.getTodayAsLong()
            else
                System.currentTimeMillis()
        } else if (completed != null && percent != 100) {
            completed = null
            completedTimezone = null
        }

        makeDirty()

        return
    }

    /**
     * Sets the dirty flag, updates sequence and lastModified value, makes recurring entry an exception
     */
    fun makeDirty() {
        lastModified = System.currentTimeMillis()
        sequence += 1
        dirty = true
        isRecurLinkedInstance = false     // in any case on update of the progress, the item becomes an exception
    }

    /**
     * @return a Recur Object based on the given rrule or null
     */
    fun getRecur(): Recur? {
        if(this.rrule.isNullOrEmpty())
            return null

        return try {
            Recur(this.rrule)
        } catch (e: ParseException) {
            Log.w("getRrule", "Illegal representation of UNTIL\n$e")
            null
        } catch (e: IllegalArgumentException) {
            Log.w("getRrule", "Unrecognized rrule\n$e")
            null
        }
    }

    fun getInstancesFromRrule(): List<Long> {
        val recurList = mutableListOf<Long>()

        // don't continue if this function is called with an empty dtstart
        if(dtstart == null || this.rrule.isNullOrEmpty())
            return recurList

        try {
            val rRule = Recur(this.rrule)
            var zonedDtstart = ZonedDateTime.ofInstant(Instant.ofEpochMilli(dtstart?:0L), requireTzId(dtstartTimezone))
            val interval = if(rRule.interval < 1) 1L else rRule.interval.toLong()
            val count = retrieveCount()

            when (rRule.frequency)
            {
                Recur.Frequency.SECONDLY ->
                {
                    for(i in 1..count) {
                        recurList.add(zonedDtstart.toInstant().toEpochMilli())
                        zonedDtstart = zonedDtstart.plusSeconds(interval)
                    }
                }
                Recur.Frequency.MINUTELY ->
                {
                    for(i in 1..count) {
                        recurList.add(zonedDtstart.toInstant().toEpochMilli())
                        zonedDtstart = zonedDtstart.plusMinutes(interval)
                    }
                }
                Recur.Frequency.HOURLY ->
                {
                    for(i in 1..count) {
                        recurList.add(zonedDtstart.toInstant().toEpochMilli())
                        zonedDtstart = zonedDtstart.plusHours(interval)
                    }
                }

                Recur.Frequency.DAILY ->
                {
                    if(rRule.dayList.isEmpty()) {
                        for (i in 1..count) {
                            recurList.add(zonedDtstart.toInstant().toEpochMilli())
                            Log.d("calculatedDay", convertLongToFullDateTimeString(zonedDtstart.toInstant().toEpochMilli(), dtstartTimezone))
                            zonedDtstart = zonedDtstart.plusDays(interval)
                        }
                    } else {
                        // Considering a day list. This is currently not possible to be entered in jtx Board, but might come from Thunderbird
                        var iteration = 0
                        while (iteration < count) {
                            if(rRule.dayList.any { weekday -> weekday.asDayOfWeek() == zonedDtstart.dayOfWeek}) {
                                recurList.add(zonedDtstart.toInstant().toEpochMilli())
                                Log.d("calculatedDay", convertLongToFullDateTimeString(zonedDtstart.toInstant().toEpochMilli(), dtstartTimezone))
                                iteration += 1
                            }
                            zonedDtstart = zonedDtstart.plusDays(interval)
                        }
                    }
                }
                Recur.Frequency.WEEKLY -> {

                    val selectedWeekdays = mutableListOf<DayOfWeek>()

                    rRule.dayList.forEach { weekDay ->
                        weekDay.asDayOfWeek()?.let { selectedWeekdays.add(it) }
                    }

                    for(i in 1..count) {
                        var zonedDtstartWeekloop = zonedDtstart
                        for (j in 1..7) {
                            if(zonedDtstartWeekloop.dayOfWeek in selectedWeekdays || zonedDtstartWeekloop.dayOfWeek == zonedDtstart.dayOfWeek) {

                                if(rRule.until != null
                                    && zonedDtstartWeekloop
                                        .withHour(0)
                                        .withMinute(0)
                                        .withSecond(0)
                                        .withNano(0)
                                        .toInstant()
                                        .toEpochMilli() > rRule.until.time)
                                    break

                                recurList.add(zonedDtstartWeekloop.toInstant().toEpochMilli())
                                Log.d("calculatedDay", convertLongToFullDateTimeString(zonedDtstartWeekloop.toInstant().toEpochMilli(), dtstartTimezone))
                            }
                            zonedDtstartWeekloop = zonedDtstartWeekloop.plusDays(1)
                        }
                        zonedDtstart = zonedDtstart.plusWeeks(interval)
                    }
                }
                Recur.Frequency.MONTHLY ->
                {
                    zonedDtstart = zonedDtstart.withDayOfMonth(rRule.monthDayList[0] ?:1)
                    for(i in 1..count) {
                        recurList.add(zonedDtstart.toInstant().toEpochMilli())
                        Log.d("calculatedDay", convertLongToFullDateTimeString(zonedDtstart.toInstant().toEpochMilli(), dtstartTimezone))
                        zonedDtstart = zonedDtstart.plusMonths(interval)
                    }
                }
                Recur.Frequency.YEARLY ->
                {
                    for(i in 1..count) {
                        recurList.add(zonedDtstart.toInstant().toEpochMilli())
                        Log.d("calculatedDay", convertLongToFullDateTimeString(zonedDtstart.toInstant().toEpochMilli(), dtstartTimezone))
                        zonedDtstart = zonedDtstart.plusYears(interval)
                    }
                }
                else -> Log.w("LoadRRule", "Unsupported recurrence frequency found (${rRule.frequency}")
            }
        } catch (e: Exception) {
            Log.w("LoadRRule", "Failed to get Instances from the provided RRule\n$e")
        }

        //now remove exceptions
        val exceptions = getLongListfromCSVString(this.exdate).toSet()
        recurList.removeAll(exceptions)

        //now add additions (this is not in use in jtx, but can theoretically come through the sync
        val additions = getLongListfromCSVString(this.rdate)
        recurList.addAll(additions)

        return recurList
    }

    fun retrieveCount(): Int {
        val rRule = Recur(this.rrule)
        val interval = if(rRule.interval < 1) 1L else rRule.interval.toLong()

        return if(rRule.count >= 1)
            rRule.count
        else if (rRule.count == -1 && rRule.until != null) {
            var counter = 0
            var date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(dtstart?:0L), requireTzId(dtstartTimezone))
            date = date.withNano(0).withSecond(0).withMinute(0).withHour(0)
            while (date.toInstant().toEpochMilli() <= rRule.until.time) {
                counter += 1
                date = when(rRule.frequency) {
                    Recur.Frequency.DAILY -> date.plusDays(1*interval)
                    Recur.Frequency.WEEKLY -> date.plusWeeks(1*interval)
                    Recur.Frequency.MONTHLY -> date.plusMonths(1*interval)
                    Recur.Frequency.YEARLY -> date.plusYears(1*interval)
                    else -> return 1
                }
            }
            counter
        }
        else
            DEFAULT_MAX_RECUR_INSTANCES
    }


    fun recreateRecurring(context: Context) {

        val database = ICalDatabase.getInstance(context).iCalDatabaseDao
        database.deleteRecurringInstances(id)
        if(dtstart == null || rrule.isNullOrEmpty())
            return

        val original = database.getSync(id) ?: return
        val timeToDue = if(original.property.component == Component.VTODO.name && original.property.due != null)
            original.property.due!! - original.property.dtstart!!
        else
            0L

        getInstancesFromRrule().forEach { recurrenceDate ->
            val instance = original.copy()
            instance.property.id = 0L
            instance.property.recurOriginalIcalObjectId = id
            instance.property.uid = generateNewUID()
            instance.property.dtstamp = System.currentTimeMillis()
            instance.property.created = System.currentTimeMillis()
            instance.property.lastModified = System.currentTimeMillis()
            instance.property.rrule = null
            instance.property.rdate = null
            instance.property.exdate = null
            instance.property.sequence = 0
            instance.property.fileName = null
            instance.property.eTag = null
            instance.property.scheduleTag = null
            instance.property.dirty = false
            instance.property.isRecurLinkedInstance = true

            instance.property.recurid = when {
                    instance.property.dtstartTimezone == TZ_ALLDAY -> DtStart(Date(instance.property.dtstart!!)).value
                    instance.property.dtstartTimezone.isNullOrEmpty() -> DtStart(DateTime(instance.property.dtstart!!)).value
                    else -> {
                        val timezone =
                            TimeZoneRegistryFactory.getInstance().createRegistry()
                                .getTimeZone(instance.property.dtstartTimezone)
                        val withTimezone =
                            DtStart(DateTime(instance.property.dtstart!!))
                        withTimezone.timeZone = timezone
                        withTimezone.value
                }
            }

            instance.property.dtstart = recurrenceDate

            if(instance.property.component == Component.VTODO.name && original.property.due != null)
                instance.property.due = recurrenceDate + timeToDue

            val instanceId = database.insertICalObjectSync(instance.property)

            instance.categories?.forEach {
                it.categoryId = 0L
                it.icalObjectId = instanceId
                database.insertCategorySync(it)
            }
            instance.comments?.forEach {
                it.commentId = 0L
                it.icalObjectId = instanceId
                database.insertCommentSync(it)
            }
            instance.attachments?.forEach {
                it.attachmentId = 0L
                it.icalObjectId = instanceId
                database.insertAttachmentSync(it)
            }
            instance.organizer.apply {
                this?.organizerId = 0L
                this?.icalObjectId = instanceId
                this?.let { database.insertOrganizerSync(it) }
            }
            instance.attendees?.forEach {
                it.attendeeId = 0L
                it.icalObjectId = instanceId
                database.insertAttendeeSync(it)
            }
            instance.resources?.forEach {
                it.resourceId = 0L
                it.icalObjectId = instanceId
                database.insertResourceSync(it)
            }
            instance.alarms?.forEach {
                if(it.triggerRelativeDuration != null) {    // only relative alarms are considered
                    it.alarmId = 0L
                    it.icalObjectId = instanceId

                    try {
                        val dur = Duration.parse(it.triggerRelativeDuration!!)
                        if(it.triggerRelativeTo == AlarmRelativeTo.END.name) {
                            it.triggerTime = instance.property.due!! + dur.inWholeMilliseconds
                            it.triggerTimezone = instance.property.dueTimezone
                        } else {
                            it.triggerTime = instance.property.dtstart!! + dur.inWholeMilliseconds
                            it.triggerTimezone = instance.property.dtstartTimezone
                        }
                        database.insertAlarmSync(it)
                    } catch (e: IllegalArgumentException) {
                        Log.w("DurationParsing", "Duration could not be parsed for instance, skipping this alarm.")
                    }
                }
            }
            Alarm.scheduleNextNotifications(context)
        }
    }

    /**
     * Takes a string, extracts the first line to the summary, the remaining lines to the description
     * @param [text] that should be parsed
     */
    fun parseSummaryAndDescription(text: String?) {
        if (text == null)
            return

        text.split(System.lineSeparator(), limit = 2).let {
            if (it.isNotEmpty())
                this.summary = it[0]
            if (it.size >= 2)
                this.description = it[1]
        }
    }

    /**
     * Takes a string, extracts the first link matching the regex from the string and puts it into the URL field of the ICalObject
     * @param [text] that should be parsed
     */
    fun parseURL(text: String?) {
        if (text == null)
            return

        val matcher = PatternsCompat.WEB_URL.matcher(text)
        while (matcher.find()) {
            this.url = matcher.group()
            return
        }
    }

    fun getRecurInfo(context: Context?): String? {
        if(context == null)
            return null

        var recurInfo = ""

        if(recurOriginalIcalObjectId != null && !this.isRecurLinkedInstance)
            recurInfo += context.getString(R.string.view_share_exception_of_series) + System.lineSeparator()
        else if (recurOriginalIcalObjectId != null && this.isRecurLinkedInstance)
            recurInfo += context.getString(R.string.view_share_part_of_series) + System.lineSeparator()

        val recur: Recur
        try {
            recur = Recur(this.rrule)
        } catch (e: Exception) {
            return if(recurInfo.isEmpty())
                null
            else
                recurInfo + System.lineSeparator()
        }

        recurInfo += context.getString(R.string.view_share_repeats) + " "
        recurInfo += recur.interval.toString() + " "
        when (recur.frequency) {
            Recur.Frequency.YEARLY -> recurInfo += context.getString(R.string.edit_recur_year) + " "
            Recur.Frequency.MONTHLY -> {
                recurInfo += context.getString(R.string.edit_recur_month) + " "
                recurInfo += context.getString(R.string.edit_recur_on_the_x_day_of_month) + recur.monthDayList.first().toString() + context.getString(R.string.edit_recur_x_day_of_the_month)
            }
            Recur.Frequency.WEEKLY -> {
                recurInfo += context.getString(R.string.edit_recur_week) + " "
                recurInfo += context.getString(R.string.edit_recur_on_weekday) + " "
                val dayList = mutableListOf<String>()
                recur.dayList.forEach { weekday ->
                    when(weekday) {
                        WeekDay.MO -> dayList.add(DayOfWeek.MONDAY.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()))
                        WeekDay.TU -> dayList.add(DayOfWeek.MONDAY.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()))
                        WeekDay.WE -> dayList.add(DayOfWeek.MONDAY.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()))
                        WeekDay.TH -> dayList.add(DayOfWeek.MONDAY.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()))
                        WeekDay.FR -> dayList.add(DayOfWeek.MONDAY.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()))
                        WeekDay.SA -> dayList.add(DayOfWeek.MONDAY.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()))
                        WeekDay.SU -> dayList.add(DayOfWeek.MONDAY.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()))
                    }
                }
                recurInfo += dayList.joinToString(separator = ", ")
            }
            Recur.Frequency.DAILY -> recurInfo += context.getString(R.string.edit_recur_day) + " "
            else -> return null
        }
        recurInfo += recur.count.toString() + " " + context.getString(R.string.edit_recur_x_times)

        //TODO: Consider also Exceptions and additions in the future?
        return if(recurInfo.isEmpty())
            null
        else
            recurInfo + System.lineSeparator()
    }

    fun setDefaultStartDateFromSettings(context: Context) {
        val default = PreferenceManager.getDefaultSharedPreferences(context).getString(
            DropdownSetting.SETTING_DEFAULT_START_DATE.key, null) ?: return
        if(default == "null")
            return
        try {
            this.dtstart = DateTimeUtils.getTodayAsLong() + Duration.parse(default).inWholeMilliseconds
            this.dtstartTimezone = TZ_ALLDAY
        } catch (e: java.lang.IllegalArgumentException) {
            Log.d("DurationParsing", "Could not parse duration from settings")
        }
    }

    fun setDefaultDueDateFromSettings(context: Context) {
        val default = PreferenceManager.getDefaultSharedPreferences(context).getString(
            DropdownSetting.SETTING_DEFAULT_DUE_DATE.key, null) ?: return
        if(default == "null")
            return
        try {
            this.due = DateTimeUtils.getTodayAsLong() + Duration.parse(default).inWholeMilliseconds
            this.dueTimezone = TZ_ALLDAY
        } catch (e: java.lang.IllegalArgumentException) {
            Log.d("DurationParsing", "Could not parse duration from settings")
        }
    }

    /**
     * @return The Module of the current [ICalObject] as [Module] enum.
     * Fallback is [Module.NOTE]
     */
    fun getModuleFromString(): Module {
        return when (this.module) {
            Module.JOURNAL.name -> Module.JOURNAL
            Module.NOTE.name -> Module.NOTE
            Module.TODO.name -> Module.TODO
            else -> Module.NOTE
        }
    }
}

/** This enum class defines the possible values for the attribute [ICalObject.status] for Notes/Journals
 * The possible values differ for Todos and Journals/Notes
 * @param [stringResource] is a reference to the String Resource within JTX
 */
@Parcelize
enum class StatusJournal(val stringResource: Int) : Parcelable {

    DRAFT(R.string.journal_status_draft),
    FINAL(R.string.journal_status_final),
    CANCELLED(R.string.journal_status_cancelled);

    companion object {

        fun getStringResource(context: Context, name: String?): String {
            return if(name == null)
                context.getString(R.string.status_no_status)
            else if(values().any { it.name == name })
                context.getString(valueOf(name).stringResource)
            else
                name
        }

        fun getListFromStringList(stringList: Set<String>?): MutableList<StatusJournal> {
            val list = mutableListOf<StatusJournal>()
            stringList?.forEach { string ->
                when (string) {
                    DRAFT.name -> list.add(DRAFT)
                    FINAL.name -> list.add(FINAL)
                    CANCELLED.name -> list.add(CANCELLED)
                }
            }
            return list
        }

        fun getStringSetFromList(list: List<StatusJournal>): Set<String> {
            val set = mutableListOf<String>()
            list.forEach {
                set.add(it.name)
            }
            return set.toSet()
        }
    }
}

/** This enum class defines the possible values for the attribute [ICalObject.status] for Todos
 * The possible values differ for Todos and Journals/Notes
 * @param [stringResource] is a reference to the String Resource within JTX
 */
@Parcelize
enum class StatusTodo(val stringResource: Int) : Parcelable {

    `NEEDS-ACTION`(R.string.todo_status_needsaction),
    COMPLETED(R.string.todo_status_completed),
    `IN-PROCESS`(R.string.todo_status_inprocess),
    CANCELLED(R.string.todo_status_cancelled);

    companion object {

        fun getStringResource(context: Context, name: String?): String {
            return if(name == null)
                context.getString(R.string.status_no_status)
            else if(values().any { it.name == name })
                context.getString(valueOf(name).stringResource)
            else
                name
        }

        fun getListFromStringList(stringList: Set<String>?): MutableList<StatusTodo> {
            val list = mutableListOf<StatusTodo>()
            stringList?.forEach { string ->
                when (string) {
                    `NEEDS-ACTION`.name -> list.add(`NEEDS-ACTION`)
                    COMPLETED.name -> list.add(COMPLETED)
                    `IN-PROCESS`.name -> list.add(`IN-PROCESS`)
                    CANCELLED.name -> list.add(CANCELLED)
                }
            }
            return list
        }

        fun getStringSetFromList(list: List<StatusTodo>): Set<String> {
            val set = mutableListOf<String>()
            list.forEach {
                set.add(it.name)
            }
            return set.toSet()
        }
    }
}

/** This enum class defines the possible values for the attribute [ICalObject.classification]
 * @param [stringResource] is a reference to the String Resource within JTX
 */
@Parcelize
enum class Classification(val stringResource: Int) : Parcelable {

    PUBLIC(R.string.classification_public),
    PRIVATE(R.string.classification_private),
    CONFIDENTIAL(R.string.classification_confidential);

    companion object {

        fun getStringResource(context: Context, name: String?): String {

            return if(name == null)
                context.getString(R.string.classification_no_classification)
            else if(values().any { it.name == name })
                context.getString(valueOf(name).stringResource)
            else
                name
        }

        fun getListFromStringList(stringList: Set<String>?): MutableList<Classification> {
            val list = mutableListOf<Classification>()
            stringList?.forEach { string ->
                when (string) {
                    PUBLIC.name -> list.add(PUBLIC)
                    PRIVATE.name -> list.add(PRIVATE)
                    CONFIDENTIAL.name -> list.add(CONFIDENTIAL)
                }
            }
            return list
        }

        fun getStringSetFromList(list: List<Classification>): Set<String> {
            val set = mutableListOf<String>()
            list.forEach {
                set.add(it.name)
            }
            return set.toSet()
        }
    }

}

/** This enum class defines the possible values for the attribute [ICalObject.component]  */
enum class Component {
    VJOURNAL, VTODO
}

/** This enum class defines the possible values for the attribute [ICalObject.module]  */
enum class Module {
    JOURNAL, NOTE, TODO
}



