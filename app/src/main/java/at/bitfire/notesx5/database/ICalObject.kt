package at.bitfire.notesx5.database

import android.content.ContentValues
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.*
import at.bitfire.notesx5.R
import kotlinx.android.parcel.Parcelize
import java.util.*


/** The name of the the table for IcalObjects.
 * This is a general purpose table containing general columns
 * for Journals, Notes and Todos */
const val TABLE_NAME_ICALOBJECT = "icalobject"

/** The name of the ID column.
 * This is the unique identifier of an ICalObject
 * Type: [Long]*/
const val COLUMN_ID = BaseColumns._ID

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
 * Type: [Float]
 */
const val COLUMN_GEO_LAT = "geolat"
/**
 * Purpose:  This property specifies information related to the global position for the activity specified by a calendar component.
 * This property is split in the fields [COLUMN_GEO_LAT] for the latitude
 * and [COLUMN_GEO_LONG] for the longitude coordinates.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.1.6]
 * Type: [Float]
 */
const val COLUMN_GEO_LONG = "geolong"
/**
 * Purpose:  This property defines the intended venue for the activity defined by a calendar component.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.1.7]
 * Type: [String]
 */
const val COLUMN_LOCATION = "location"
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
 * Purpose:  This property specifies a color used for displaying the calendar, event, todo, or journal data.
 * See [https://tools.ietf.org/html/rfc7986#section-5.9]
 * Type: [String]
 */
const val COLUMN_COLOR = "color"
/**
 * Purpose:  To specify other properties for the ICalObject.
 * This is especially used for additional attributes relevant for the synchronization
 * Type: [String]
 */
const val COLUMN_OTHER = "other"
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


@Parcelize
@Entity(tableName = TABLE_NAME_ICALOBJECT,
        indices = [Index(value = ["_id", "summary", "description"])],
        foreignKeys = [ForeignKey(entity = ICalCollection::class,
                parentColumns = arrayOf(COLUMN_COLLECTION_ID),
                childColumns = arrayOf(COLUMN_ICALOBJECT_COLLECTIONID),
                onDelete = ForeignKey.CASCADE)]
)
data class ICalObject(

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(index = true, name = COLUMN_ID)
        var id: Long = 0L,

        @ColumnInfo(name = COLUMN_COMPONENT) var component: String = Component.NOTE.name,          // JOURNAL or NOTE
        //@ColumnInfo(name = COLUMN_COLLECTION) var collection: String = "LOCAL",
        @ColumnInfo(name = COLUMN_SUMMARY) var summary: String? = null,
        @ColumnInfo(name = COLUMN_DESCRIPTION) var description: String? = null,
        @ColumnInfo(name = COLUMN_DTSTART) var dtstart: Long? = null,
        @ColumnInfo(name = COLUMN_DTSTART_TIMEZONE) var dtstartTimezone: String? = null,

        @ColumnInfo(name = COLUMN_DTEND) var dtend: Long? = null,
        @ColumnInfo(name = COLUMN_DTEND_TIMEZONE) var dtendTimezone: String? = null,

        @ColumnInfo(name = COLUMN_STATUS) var status: String = StatusJournal.FINAL.param,     // 0 = DRAFT, 1 = FINAL, 2 = CANCELLED, -1 = NOT SUPPORTED (value in statusX)
        @ColumnInfo(name = COLUMN_CLASSIFICATION) var classification: String = Classification.PUBLIC.param,    // 0 = PUBLIC, 1 = PRIVATE, 2 = CONFIDENTIAL, -1 = NOT SUPPORTED (value in classificationX)

        @ColumnInfo(name = COLUMN_URL) var url: String? = null,
        @ColumnInfo(name = COLUMN_CONTACT) var contact: String? = null,
        @ColumnInfo(name = COLUMN_GEO_LAT) var geoLat: Float? = null,
        @ColumnInfo(name = COLUMN_GEO_LONG) var geoLong: Float? = null,
        @ColumnInfo(name = COLUMN_LOCATION) var location: String? = null,

        @ColumnInfo(name = COLUMN_PERCENT) var percent: Int? = null,    // VTODO only!
        @ColumnInfo(name = COLUMN_PRIORITY) var priority: Int? = null,   // VTODO and VEVENT

        @ColumnInfo(name = COLUMN_DUE) var due: Long? = null,      // VTODO only!
        @ColumnInfo(name = COLUMN_DUE_TIMEZONE) var dueTimezone: String? = null, //VTODO only!
        @ColumnInfo(name = COLUMN_COMPLETED) var completed: Long? = null, // VTODO only!
        @ColumnInfo(name = COLUMN_COMPLETED_TIMEZONE) var completedTimezone: String? = null, //VTODO only!
        @ColumnInfo(name = COLUMN_DURATION) var duration: String? = null, //VTODO only!


        @ColumnInfo(name = COLUMN_UID) var uid: String = "${System.currentTimeMillis()}-${UUID.randomUUID()}@at.bitfire.notesx5",                              //unique identifier, see https://tools.ietf.org/html/rfc5545#section-3.8.4.7

        /*
         The following properties specify change management information in  calendar components.
         https://tools.ietf.org/html/rfc5545#section-3.8.7
         */
        @ColumnInfo(name = COLUMN_CREATED) var created: Long = System.currentTimeMillis(),   // see https://tools.ietf.org/html/rfc5545#section-3.8.7.1
        @ColumnInfo(name = COLUMN_DTSTAMP) var dtstamp: Long = System.currentTimeMillis(),   // see https://tools.ietf.org/html/rfc5545#section-3.8.7.2
        @ColumnInfo(name = COLUMN_LAST_MODIFIED) var lastModified: Long = System.currentTimeMillis(), // see https://tools.ietf.org/html/rfc5545#section-3.8.7.3
        @ColumnInfo(name = COLUMN_SEQUENCE) var sequence: Long = 0,                             // increase on every change (+1), see https://tools.ietf.org/html/rfc5545#section-3.8.7.4

        //var exdate: Long? = System.currentTimeMillis(),   //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5.1
        //var rdate: Long? = System.currentTimeMillis()     //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5.2
        //var recurrenceId: String? = null,                          //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5
        //var rrule: String?,                               //only for recurring events, see https://tools.ietf.org/html/rfc5545#section-3.8.5.3


        @ColumnInfo(name = COLUMN_COLOR) var color: String? = null,
        @ColumnInfo(name = COLUMN_OTHER) var other: String? = null,

        @ColumnInfo(index = true, name = COLUMN_ICALOBJECT_COLLECTIONID)    var collectionId: Long? = 1L,

        @ColumnInfo(name = COLUMN_DIRTY) var dirty: Boolean = false,
        @ColumnInfo(name = COLUMN_DELETED) var deleted: Boolean = false


        ): Parcelable

{
        companion object Factory {


                fun createJournal(): ICalObject = ICalObject(component = "JOURNAL", dtstart = System.currentTimeMillis(), status = StatusJournal.FINAL.param)
                fun createNote(): ICalObject = ICalObject(component = "NOTE", status = StatusJournal.FINAL.param)
                fun createNote(summary: String) = ICalObject(component = "NOTE", status = StatusJournal.FINAL.param, summary = summary)
                fun createTodo() = ICalObject(component = "TODO", status = StatusTodo.NEEDSACTION.param, percent = 0, priority = 0, dueTimezone = "ALLDAY")
                fun createSubtask(summary: String) = ICalObject(component = "TODO", summary = summary, status = StatusTodo.NEEDSACTION.param, percent = 0, dueTimezone = "ALLDAY")


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

                        return ICalObject().applyContentValues(values)

                }

        }




        fun applyContentValues(values: ContentValues?):ICalObject {
                if (values?.containsKey(COLUMN_COMPONENT) == true && values.getAsString(COLUMN_COMPONENT).isNotBlank()) {
                        this.component = values.getAsString(COLUMN_COMPONENT)
                }
                /*
                if (values?.containsKey(COLUMN_COLLECTION) == true && values.getAsString(COLUMN_COLLECTION).isNotBlank()) {
                        this.collection = values.getAsString(COLUMN_COLLECTION)
                }

                 */
                if (values?.containsKey(COLUMN_SUMMARY) == true && values.getAsString(COLUMN_SUMMARY).isNotBlank()) {
                        this.summary = values.getAsString(COLUMN_SUMMARY)
                }

                //TODO: Add validation, Journals MUST have a DTSTART!
                if (values?.containsKey(COLUMN_DTSTART) == true && values.getAsLong(COLUMN_DTSTART) != null) {
                        this.dtstart = values.getAsLong(COLUMN_DTSTART)
                }

                //TODO: Validieren auf gültige Timezone!
                if (values?.containsKey(COLUMN_DTSTART_TIMEZONE) == true && values.getAsString(COLUMN_DTSTART_TIMEZONE).isNotBlank()) {
                        this.dtstartTimezone = values.getAsString(COLUMN_DTSTART_TIMEZONE)
                }
                if (values?.containsKey(COLUMN_DTEND) == true && values.getAsLong(COLUMN_DTEND) != null ) {
                        this.dtend = values.getAsLong(COLUMN_DTEND)
                }
                //TODO: Validieren auf gültige Timezone!
                if (values?.containsKey(COLUMN_DTEND_TIMEZONE) == true && values.getAsString(COLUMN_DTEND_TIMEZONE).isNotBlank()) {
                        this.dtendTimezone = values.getAsString(COLUMN_DTEND_TIMEZONE)
                }

                if (values?.containsKey(COLUMN_STATUS) == true && values.getAsString(COLUMN_STATUS).isNotBlank()) {
                        this.status = values.getAsString(COLUMN_STATUS)
                }

                if (values?.containsKey(COLUMN_CLASSIFICATION) == true && values.getAsString(COLUMN_CLASSIFICATION).isNotBlank()) {
                        this.classification = values.getAsString(COLUMN_CLASSIFICATION)
                }
                if (values?.containsKey(COLUMN_URL) == true && values.getAsString(COLUMN_URL).isNotBlank()) {
                        this.url = values.getAsString(COLUMN_URL)
                }

                if (values?.containsKey(COLUMN_GEO_LAT) == true && values.getAsFloat(COLUMN_GEO_LAT) != null) {
                        this.geoLat = values.getAsFloat(COLUMN_GEO_LAT)
                }
                if (values?.containsKey(COLUMN_GEO_LONG) == true && values.getAsFloat(COLUMN_GEO_LONG) != null) {
                        this.geoLong = values.getAsFloat(COLUMN_GEO_LONG)
                }
                if (values?.containsKey(COLUMN_LOCATION) == true && values.getAsString(COLUMN_LOCATION).isNotBlank() ) {
                        this.location = values.getAsString(COLUMN_LOCATION)
                }
                if (values?.containsKey(COLUMN_PERCENT) == true && values.getAsInteger(COLUMN_PERCENT) != null) {
                        this.percent = values.getAsInteger(COLUMN_PERCENT)
                }
                if (values?.containsKey(COLUMN_PRIORITY) == true && values.getAsInteger(COLUMN_PRIORITY) != null) {
                        this.priority = values.getAsInteger(COLUMN_PRIORITY)
                }
                if (values?.containsKey(COLUMN_DUE) == true && values.getAsLong(COLUMN_DUE) != null) {
                        this.due = values.getAsLong(COLUMN_DUE)
                }
                if (values?.containsKey(COLUMN_DUE_TIMEZONE) == true && values.getAsString(COLUMN_DUE_TIMEZONE) != null) {
                        this.dueTimezone = values.getAsString(COLUMN_DUE_TIMEZONE)
                }
                if (values?.containsKey(COLUMN_COMPLETED) == true && values.getAsLong(COLUMN_COMPLETED) != null) {
                        this.completed = values.getAsLong(COLUMN_COMPLETED)
                }
                if (values?.containsKey(COLUMN_COMPLETED_TIMEZONE) == true && values.getAsString(COLUMN_COMPLETED_TIMEZONE) != null) {
                        this.completedTimezone = values.getAsString(COLUMN_COMPLETED_TIMEZONE)
                }
                if (values?.containsKey(COLUMN_DURATION) == true && values.getAsString(COLUMN_DURATION) != null) {
                        this.duration = values.getAsString(COLUMN_DURATION)
                }
                if (values?.containsKey(COLUMN_UID) == true && values.getAsString(COLUMN_UID).isNotBlank()) {
                        this.uid = values.getAsString(COLUMN_UID)
                }
                if (values?.containsKey(COLUMN_CREATED) == true && values.getAsLong(COLUMN_CREATED) != null) {
                        this.created = values.getAsLong(COLUMN_CREATED)
                }
                if (values?.containsKey(COLUMN_DTSTAMP) == true && values.getAsLong(COLUMN_DTSTAMP) != null) {
                        this.dtstamp = values.getAsLong(COLUMN_DTSTAMP)
                }
                if (values?.containsKey(COLUMN_LAST_MODIFIED) == true && values.getAsLong(COLUMN_LAST_MODIFIED) != null) {
                        this.lastModified = values.getAsLong(COLUMN_LAST_MODIFIED)
                }
                if (values?.containsKey(COLUMN_SEQUENCE) == true && values.getAsLong(COLUMN_SEQUENCE) != null) {
                        this.sequence = values.getAsLong(COLUMN_SEQUENCE)
                }
                if (values?.containsKey(COLUMN_COLOR) == true && values.getAsString(COLUMN_COLOR).isNotBlank()) {
                        this.color = values.getAsString(COLUMN_COLOR)
                }
                if (values?.containsKey(COLUMN_OTHER) == true && values.getAsString(COLUMN_OTHER).isNotBlank()) {
                        this.other = values.getAsString(COLUMN_OTHER)
                }
                if (values?.containsKey(COLUMN_DIRTY) == true && values.getAsBoolean(COLUMN_DIRTY) != null) {
                        this.dirty = values.getAsBoolean(COLUMN_DIRTY)
                }
                if (values?.containsKey(COLUMN_DELETED) == true && values.getAsBoolean(COLUMN_DELETED) != null) {
                        this.deleted = values.getAsBoolean(COLUMN_DELETED)
                }

                return this
        }


        fun setUpdatedProgress(newPercent: Int):ICalObject {

                if (percent == newPercent)
                        return this

                percent = newPercent
                status = when (newPercent) {
                        100 -> StatusTodo.COMPLETED.param
                        in 1..99 -> StatusTodo.INPROCESS.param
                        0 -> StatusTodo.NEEDSACTION.param
                        else -> StatusTodo.NEEDSACTION.param      // should never happen!
                }
                lastModified = System.currentTimeMillis()
                if(dtstart == null && percent != null && percent!! > 0)
                        dtstart = System.currentTimeMillis()
                if(dtend == null && percent != null && percent!! == 100)
                        dtend = System.currentTimeMillis()
                if(completed == null && percent != null && percent!! == 100)
                        completed = System.currentTimeMillis()
                sequence++
                dirty = true

                return this
        }


        }

/** This enum class defines the possible values for the attribute [ICalObject.status] for Notes/Journals
 * The possible values differ for Todos and Journals/Notes
 * @param [id] is an ID of the entry
 * @param [param] defines the [StatusTodo] how it is stored in the database, this also corresponds to the value that is used for the ICal format
 * @param [stringResource] is a reference to the String Resource within NotesX5
 */
@Parcelize
enum class StatusJournal (val id: Int, val param: String, val stringResource: Int): Parcelable {

        DRAFT(0,"DRAFT", R.string.journal_status_draft),
        FINAL(1,"FINAL", R.string.journal_status_final),
        CANCELLED(2,"CANCELLED", R.string.journal_status_cancelled);

        companion object {
                fun getParamById(id: Int): String? {
                        values().forEach {
                                if (it.id == id)
                                        return it.param
                        }
                        return null
                }

                fun getStringResourceByParam(param: String): Int? {
                        values().forEach {
                                if (it.param == param)
                                        return it.stringResource
                        }
                        return null
                }

                fun paramValues(): List<String> {
                        val paramValues: MutableList<String> = mutableListOf()
                        values().forEach { paramValues.add(it.param) }
                        return paramValues
                }

        }
}

/** This enum class defines the possible values for the attribute [ICalObject.status] for Todos
 * The possible values differ for Todos and Journals/Notes
 * @param [id] is an ID of the entry
 * @param [param] defines the [StatusTodo] how it is stored in the database, this also corresponds to the value that is used for the ICal format
 * @param [stringResource] is a reference to the String Resource within NotesX5
 */
@Parcelize
enum class StatusTodo (val id: Int, val param: String, val stringResource: Int): Parcelable {

        NEEDSACTION(0,"NEEDS-ACTION", R.string.todo_status_needsaction),
        COMPLETED(1,"COMPLETED", R.string.todo_status_completed),
        INPROCESS(2,"IN-PROCESS", R.string.todo_status_inprocess),
        CANCELLED(3,"CANCELLED", R.string.todo_status_cancelled);

        companion object {
                fun getParamById(id: Int): String? {
                        values().forEach {
                                if (it.id == id)
                                        return it.param
                        }
                        return null
                }

                fun getStringResourceByParam(param: String): Int? {
                        values().forEach {
                                if (it.param == param)
                                        return it.stringResource
                        }
                        return null
                }

                fun paramValues(): List<String> {
                        val paramValues: MutableList<String> = mutableListOf()
                        values().forEach { paramValues.add(it.param) }
                        return paramValues
                }
        }
}

/** This enum class defines the possible values for the attribute [ICalObject.classification]
 * @param [id] is an ID of the entry
 * @param [param] defines the [Classification] how it is stored in the database, this also corresponds to the value that is used for the ICal format
 * @param [stringResource] is a reference to the String Resource within NotesX5
 */
@Parcelize
enum class Classification (val id: Int, val param: String, val stringResource: Int): Parcelable {

        PUBLIC(0,"PUBLIC", R.string.classification_public),
        PRIVATE(1,"PRIVATE", R.string.classification_private),
        CONFIDENTIAL(2,"CONFIDENTIAL", R.string.classification_confidential);


        companion object {
                fun getParamById(id: Int): String? {
                        values().forEach {
                                if (it.id == id)
                                        return it.param
                        }
                        return null
                }

                fun getStringResourceByParam(param: String): Int? {
                        values().forEach {
                                if (it.param == param)
                                        return it.stringResource
                        }
                        return null
                }

                fun paramValues(): List<String> {
                        val paramValues: MutableList<String> = mutableListOf()
                        values().forEach { paramValues.add(it.param) }
                        return paramValues
                }
        }

}

/** This enum class defines the possible values for the attribute [ICalObject.component]  */
enum class Component {
        JOURNAL, NOTE, TODO
}




