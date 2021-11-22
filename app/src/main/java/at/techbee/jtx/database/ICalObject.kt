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
import android.os.Parcelable
import android.provider.BaseColumns
import android.util.Log
import androidx.room.*
import at.techbee.jtx.R
import at.techbee.jtx.addLongToCSVString
import at.techbee.jtx.convertLongToDateString
import at.techbee.jtx.getLongListfromCSVString
import kotlinx.parcelize.Parcelize
import net.fortuna.ical4j.model.*
import net.fortuna.ical4j.model.Date
import net.fortuna.ical4j.model.property.DtStart
import net.fortuna.ical4j.util.MapTimeZoneCache
import java.util.*
import java.util.Calendar
import java.util.TimeZone
import kotlin.IllegalArgumentException


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

    @ColumnInfo(name = COLUMN_STATUS) var status: String? = null,     // 0 = DRAFT, 1 = FINAL, 2 = CANCELLED, -1 = NOT SUPPORTED (value in statusX)
    @ColumnInfo(name = COLUMN_CLASSIFICATION) var classification: String? = null,    // 0 = PUBLIC, 1 = PRIVATE, 2 = CONFIDENTIAL, -1 = NOT SUPPORTED (value in classificationX)

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

    @ColumnInfo(name = COLUMN_COLOR) var color: Int? = null,

    @ColumnInfo(index = true, name = COLUMN_ICALOBJECT_COLLECTIONID) var collectionId: Long = 1L,

    @ColumnInfo(name = COLUMN_DIRTY) var dirty: Boolean = false,
    @ColumnInfo(name = COLUMN_DELETED) var deleted: Boolean = false,

    @ColumnInfo(name = COLUMN_FILENAME) var fileName: String? = null,
    @ColumnInfo(name = COLUMN_ETAG) var eTag: String? = null,
    @ColumnInfo(name = COLUMN_SCHEDULETAG) var scheduleTag: String? = null,
    @ColumnInfo(name = COLUMN_FLAGS) var flags: Int? = null


) : Parcelable {


    companion object Factory {

        const val TZ_ALLDAY = "ALLDAY"

        fun createJournal(): ICalObject = ICalObject(
            component = Component.VJOURNAL.name,
            module = Module.JOURNAL.name,
            dtstart = System.currentTimeMillis(),
            dtstartTimezone = TZ_ALLDAY,
            status = StatusJournal.FINAL.name,
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
            status = StatusTodo.`NEEDS-ACTION`.name,
            percent = null,
            priority = null,
            dueTimezone = TZ_ALLDAY,
            dtstartTimezone = TZ_ALLDAY,
            completedTimezone = TZ_ALLDAY,
            dirty = true
        )

        fun generateNewUID() =
            "${System.currentTimeMillis()}-${UUID.randomUUID()}@at.techbee.jtx"

        /**
         * Create a new [ICalObject] from the specified [ContentValues].
         *
         * @param values A [ICalObject] that at least contain [.COLUMN_NAME].
         * @return A newly created [ICalObject] instance.
         */
        fun fromContentValues(values: ContentValues?): ICalObject? {

            // TODO initialize specific component based on values!
            // TODO validate some inputs, especially Int Inputs!

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
                    // fix for crash when Timezones are needed for ical4j, see https://github.com/ical4j/ical4j/issues/195
                    System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache::class.java.name)

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
            children.forEach {
                it?.id?.let { child ->
                    deleteItemWithChildren(child, database)    // call the function again to recursively delete all children, then delete the item
                }
            }

            database.deleteRecurringInstances(id)      // recurring instances are always physically deleted
            val item = database.getSync(id)!!
            when {
                item.property.isRecurLinkedInstance -> {
                    makeRecurringException(item.property, database)   // if the current item
                    database.deleteICalObjectsbyId(id)
                }
                item.property.collectionId == 1L -> database.deleteICalObjectsbyId(item.property.id) // Elements in local collection are physically deleted
                else -> database.updateToDeleted(item.property.id, System.currentTimeMillis())
            }
        }

        fun makeRecurringException(item: ICalObject, database: ICalDatabaseDao) {
            if(item.isRecurLinkedInstance) {

                item.recurOriginalIcalObjectId?.let { originalId ->
                    val newExceptionList =
                        addLongToCSVString(database.getRecurExceptions(originalId), item.dtstart)
                    database.setRecurExceptions(
                        originalId,
                        newExceptionList,
                        System.currentTimeMillis()
                    )
                    database.setAsRecurException(item.id, System.currentTimeMillis())
                }
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
            this.dtstartTimezone = dtstartTimezone
        }   //TODO: Validieren auf gültige Timezone!
        values.getAsLong(COLUMN_DTEND)?.let { dtend -> this.dtend = dtend }
        values.getAsString(COLUMN_DTEND_TIMEZONE)?.let { dtendTimezone ->
            this.dtendTimezone = dtendTimezone
        }   //TODO: Validieren auf gültige Timezone!
        values.getAsString(COLUMN_STATUS)?.let { status -> this.status = status }
        values.getAsString(COLUMN_CLASSIFICATION)
            ?.let { classification -> this.classification = classification }
        values.getAsString(COLUMN_URL)?.let { url -> this.url = url }
        values.getAsString(COLUMN_CONTACT)?.let { contact -> this.contact = contact }
        values.getAsDouble(COLUMN_GEO_LAT)?.let { geoLat -> this.geoLat = geoLat }
        values.getAsDouble(COLUMN_GEO_LONG)?.let { geoLong -> this.geoLong = geoLong }
        values.getAsString(COLUMN_LOCATION)?.let { location -> this.location = location }
        values.getAsString(COLUMN_LOCATION_ALTREP)?.let { locationAltrep -> this.locationAltrep = locationAltrep }
        values.getAsInteger(COLUMN_PERCENT)?.let { percent -> this.percent = percent }
        values.getAsInteger(COLUMN_PRIORITY)?.let { priority -> this.priority = priority }
        values.getAsLong(COLUMN_DUE)?.let { due -> this.due = due }
        values.getAsString(COLUMN_DUE_TIMEZONE)
            ?.let { dueTimezone -> this.dueTimezone = dueTimezone }
        values.getAsLong(COLUMN_COMPLETED)?.let { completed -> this.completed = completed }
        values.getAsString(COLUMN_COMPLETED_TIMEZONE)
            ?.let { completedTimezone -> this.completedTimezone = completedTimezone }
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
        values.getAsBoolean(COLUMN_DIRTY)?.let { dirty -> this.dirty = dirty }
        values.getAsBoolean(COLUMN_DELETED)?.let { deleted -> this.deleted = deleted }
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


    fun setUpdatedProgress(newPercent: Int): ICalObject {

        if (percent == newPercent || (percent == null && newPercent == 0))
            return this

        percent = if(newPercent == 0) null else newPercent
        status = when (newPercent) {
            100 -> StatusTodo.COMPLETED.name
            in 1..99 -> StatusTodo.`IN-PROCESS`.name
            0 -> StatusTodo.`NEEDS-ACTION`.name
            else -> StatusTodo.`NEEDS-ACTION`.name      // should never happen!
        }
        lastModified = System.currentTimeMillis()

        /** We set the dtstart if it was not set yet, but it MUST have the same
         * format as due (DATE ie. TZ_ALLDAY or DATE-TIME). Additionally the
         * dtstart date must be BEFORE the due-date
         */
        /*
        if (dtstart == null && percent != null && percent!! > 0) {
            when {
                due == null -> {
                    dtstart = System.currentTimeMillis()
                    dtstartTimezone = TZ_ALLDAY
                }
                due!! <= System.currentTimeMillis() -> {
                    dtstart = due
                    dtstartTimezone = dueTimezone
                }
                due!! > System.currentTimeMillis() -> {
                    dtstart = System.currentTimeMillis()
                    dtstartTimezone = dueTimezone
                }
            }
        }
         */

        if (completed == null && percent != null && percent!! == 100)
            completed = System.currentTimeMillis()

        sequence++
        dirty = true

        isRecurLinkedInstance = false     // in any case on update of the progress, the item becomes an exception

        return this
    }



    fun getInstancesFromRrule(): List<Long> {

        val recurList = mutableListOf<Long>()

        try {
            val rRule = Recur(this.rrule)
            val interval = rRule.interval                       //interval and count MUST be present
            val count = rRule.count

            val start = Calendar.getInstance().apply {
                if(dtstartTimezone?.isNotEmpty() == true && dtstartTimezone != TZ_ALLDAY)
                    this.timeZone = TimeZone.getTimeZone(dtstartTimezone)
                else
                    timeZone = TimeZone.getTimeZone("UTC")
                //TODO: Continue here checking the timezone

                timeInMillis = dtstart?: return emptyList()
            }


            when (rRule.frequency)
            {
                Recur.Frequency.DAILY ->
                {
                    for(i in 1..count) {
                        recurList.add(start.timeInMillis)
                        Log.d("calculatedDay", convertLongToDateString(start.timeInMillis))
                        start.add(Calendar.DATE, interval)
                    }
                }
                Recur.Frequency.WEEKLY -> {

                    val selectedWeekdays = mutableListOf<Int>()

                    rRule.dayList.forEach { day ->
                        when(day.day) {
                            WeekDay.Day.MO -> selectedWeekdays.add(Calendar.MONDAY)
                            WeekDay.Day.TU -> selectedWeekdays.add(Calendar.TUESDAY)
                            WeekDay.Day.WE -> selectedWeekdays.add(Calendar.WEDNESDAY)
                            WeekDay.Day.TH -> selectedWeekdays.add(Calendar.THURSDAY)
                            WeekDay.Day.FR -> selectedWeekdays.add(Calendar.FRIDAY)
                            WeekDay.Day.SA -> selectedWeekdays.add(Calendar.SATURDAY)
                            WeekDay.Day.SU -> selectedWeekdays.add(Calendar.SUNDAY)
                            else -> Log.w("LoadRRule", "Error on processing day list ($day)")
                        }
                    }

                    for(i in 1..count) {
                        val startWeekloop = start.clone() as Calendar
                        for (j in 1..7) {
                            if(startWeekloop.get(Calendar.DAY_OF_WEEK) in selectedWeekdays) {
                                recurList.add(startWeekloop.timeInMillis)
                                Log.d("calculatedDay", convertLongToDateString(startWeekloop.timeInMillis))
                            }
                            startWeekloop.add(Calendar.DATE, 1)
                        }
                        start.add(Calendar.WEEK_OF_YEAR, interval)
                    }
                }
                Recur.Frequency.MONTHLY ->
                {
                    for(i in 1..count) {
                        start.set(Calendar.DAY_OF_MONTH, rRule.monthDayList[0] ?:1)
                        recurList.add(start.timeInMillis)
                        Log.d("calculatedDay", convertLongToDateString(start.timeInMillis))
                        start.add(Calendar.MONTH, interval)
                    }
                }
                Recur.Frequency.YEARLY ->
                {
                    for(i in 1..count) {
                        recurList.add(start.timeInMillis)
                        Log.d("calculatedDay", convertLongToDateString(start.timeInMillis))
                        start.add(Calendar.YEAR, interval)
                    }
                }
                else -> Log.w("LoadRRule", "Unsupported recurrence frequency found (${rRule.frequency}")
            }
        } catch (e: Exception) {
            Log.w("LoadRRule", "Failed to preset UI according to provided RRule\n$e")
        }

        //now remove exceptions
        val exceptions = getLongListfromCSVString(this.exdate).toSet()
        recurList.removeAll(exceptions)

        //now add additions (this is not in use in jtx, but can theoretically come through the sync
        val additions = getLongListfromCSVString(this.rdate)
        recurList.addAll(additions)

        return recurList
    }



    fun recreateRecurring(database: ICalDatabaseDao) {

        val original = database.getSync(id) ?: return
        database.deleteRecurringInstances(id)

        if(original.property.dtstart == null)
            return

        val timeToDue = if(original.property.component == Component.VTODO.name && original.property.due != null)
            original.property.due!! - original.property.dtstart!!
        else
            0L

        getInstancesFromRrule().forEach { recurrenceDate ->

            if(original.property.dtstart == recurrenceDate)
                return@forEach    // skip entry as it is the original event

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

            //TODO: How to deal with relatedTo?

            //TODO Check further attributes!
            //TODO Check also rdate

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

        fun getStringResource(context: Context, name: String?): String? {
            values().forEach {
                if (it.name == name)
                    return context.getString(it.stringResource)
            }
            return null
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

        fun getStringResource(context: Context, name: String?): String? {
            values().forEach {
                if (it.name == name)
                    return context.getString(it.stringResource)
            }
            return null
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

        fun getFromString(string: String?): StatusTodo? {
            return when (string) {
                `NEEDS-ACTION`.name -> `NEEDS-ACTION`
                COMPLETED.name -> COMPLETED
                `IN-PROCESS`.name -> `IN-PROCESS`
                CANCELLED.name -> CANCELLED
                else -> null
            }
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

        fun getStringResource(context: Context, name: String?): String? {
            values().forEach {
                if (it.name == name)
                    return context.getString(it.stringResource)
            }
            return null
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



