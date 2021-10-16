package at.techbee.jtx.database.properties

import android.content.ContentValues
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import at.techbee.jtx.database.COLUMN_ID
import at.techbee.jtx.database.ICalObject
import kotlinx.parcelize.Parcelize

/** The name of the the table for Alarms that are linked to an ICalObject.
 * [https://tools.ietf.org/html/rfc5545#section-3.8.1.10]*/
const val TABLE_NAME_ALARM = "alarm"

/** The name of the ID column for alarms.
 * This is the unique identifier of an Alarm
 * Type: [Long]*/
const val COLUMN_ALARM_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects.
 * Type: [Long] */
const val COLUMN_ALARM_ICALOBJECT_ID = "icalObjectId"


/* The names of all the other columns  */
/**
 * Purpose:  This property stores the unknown value as json
 * Type: [String]
 */
const val COLUMN_ALARM_VALUE = "value"


@Parcelize
@Entity(tableName = TABLE_NAME_ALARM,
    foreignKeys = [ForeignKey(entity = ICalObject::class,
        parentColumns = arrayOf(COLUMN_ID),
        childColumns = arrayOf(COLUMN_ALARM_ICALOBJECT_ID),
        onDelete = ForeignKey.CASCADE)])
data class Alarm (

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_ALARM_ID)
    var alarmId: Long = 0L,

    @ColumnInfo(index = true, name = COLUMN_ALARM_ICALOBJECT_ID)var icalObjectId: Long = 0L,
    @ColumnInfo(name = COLUMN_ALARM_VALUE)            var value: String? = null
): Parcelable


{
    companion object Factory {

        /**
         * Create a new [Alarm] Property from the specified [ContentValues].
         *
         * @param values A [Alarm] that at least contain [COLUMN_ALARM_VALUE].
         * @return A newly created [Alarm] instance.
         */
        fun fromContentValues(values: ContentValues?): Alarm? {

            if (values == null)
                return null

            if(values.getAsString(COLUMN_UNKNOWN_VALUE) == null || values.getAsLong(COLUMN_RESOURCE_ICALOBJECT_ID) == null)
                return null

            return Alarm().applyContentValues(values)
        }
    }

    fun applyContentValues(values: ContentValues): Alarm {

        values.getAsLong(COLUMN_UNKNOWN_ICALOBJECT_ID)?.let { icalObjectId -> this.icalObjectId = icalObjectId }
        values.getAsString(COLUMN_UNKNOWN_VALUE)?.let { value -> this.value = value }
        return this
    }
}