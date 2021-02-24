package at.bitfire.notesx5.database

import android.content.ContentValues
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.*
import at.bitfire.notesx5.R
import kotlinx.android.parcel.Parcelize
import java.util.*


/** The name of the the table.  */
const val TABLE_NAME_ICALOBJECT = "icalobject"

/** The name of the ID column.  */
const val COLUMN_ID = BaseColumns._ID

/** The names of all the other columns  */
const val COLUMN_COMPONENT = "component"
//const val COLUMN_COLLECTION = "collection"
const val COLUMN_SUMMARY = "summary"
const val COLUMN_DESCRIPTION = "description"
const val COLUMN_DTSTART = "dtstart"
const val COLUMN_DTSTART_TIMEZONE = "dtstarttimezone"
const val COLUMN_DTEND = "dtend"
const val COLUMN_DTEND_TIMEZONE = "dtendtimezone"
const val COLUMN_STATUS = "status"
const val COLUMN_CLASSIFICATION = "classification"
const val COLUMN_URL = "url"
const val COLUMN_CONTACT = "contact"
const val COLUMN_GEO_LAT = "geolat"
const val COLUMN_GEO_LONG = "geolong"
const val COLUMN_LOCATION = "location"
const val COLUMN_PERCENT = "percent"
const val COLUMN_PRIORITY = "priority"
const val COLUMN_DUE = "due"
const val COLUMN_DUE_TIMEZONE = "duetimezone"
const val COLUMN_COMPLETED = "completed"
const val COLUMN_COMPLETED_TIMEZONE = "completedtimezone"
const val COLUMN_UID = "uid"
const val COLUMN_CREATED = "created"
const val COLUMN_DTSTAMP = "dtstamp"
const val COLUMN_LAST_MODIFIED = "lastmodified"
const val COLUMN_SEQUENCE = "sequence"
const val COLUMN_COLOR = "color"
const val COLUMN_ICALOBJECT_COLLECTIONID = "collectionId"
const val COLUMN_DIRTY = "dirty"
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


        //var ianaProperty: String,
        //var xProperty: String,

        //var vAlarm: String?,
        //var vTimezone: Long?,
        //var ianaComponent: String?,
        //var xComponent: String?

        @ColumnInfo(name = COLUMN_COLOR) var color: String? = null,

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
                /*
                if (values?.containsKey(COLUMN_CONTACT) == true && values.getAsString(COLUMN_CONTACT).isNotBlank()) {
                        this.contact = values.getAsString(COLUMN_CONTACT)
                }

                 */
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
                if (values?.containsKey(COLUMN_DIRTY) == true && values.getAsBoolean(COLUMN_DIRTY) != null) {
                        this.dirty = values.getAsBoolean(COLUMN_DIRTY)
                }
                if (values?.containsKey(COLUMN_DELETED) == true && values.getAsBoolean(COLUMN_DELETED) != null) {
                        this.deleted = values.getAsBoolean(COLUMN_DELETED)
                }

                return this
        }
}


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

enum class Component {
        JOURNAL, NOTE, TODO
}




