package at.techbee.jtx.database.properties

import android.content.ContentValues
import org.junit.Assert
import org.junit.Test


class AlarmTest {
// Android Test as Content Values need Android libraries to run


    @Test
    fun createFromContentValues() {

        val sampleAlarm = Alarm(
            icalObjectId = 1L,
            value = "BEGIN:VALARM\n" +
                    "TRIGGER;VALUE=DATE-TIME:19970317T133000Z\n" +
                    "REPEAT:4\n" +
                    "DURATION:PT15M\n" +
                    "ACTION:AUDIO\n" +
                    "ATTACH;FMTTYPE=audio/basic:ftp://example.com/pub/\n" +
                    " sounds/bell-01.aud\n" +
                    "END:VALARM",
        )

        val cv = ContentValues(5).apply {
            put(COLUMN_ALARM_ICALOBJECT_ID, sampleAlarm.icalObjectId)
            put(COLUMN_ALARM_VALUE, sampleAlarm.value)
        }

        val cvAlarm = Alarm.fromContentValues(cv)
        Assert.assertEquals(sampleAlarm, cvAlarm)
    }

    @Test
    fun createFromContentValuesWithoutIcalobjectId() {

        val cv = ContentValues(1).apply {
            put(COLUMN_ALARM_VALUE,  "alarm")
        }

        val cvAlarm = Alarm.fromContentValues(cv)
        Assert.assertNull(cvAlarm)
    }

    @Test
    fun createFromContentValuesWithoutText() {


        val cv = ContentValues(1).apply {
            put(COLUMN_ALARM_ICALOBJECT_ID, 1L)
        }

        val cvAlarm = Alarm.fromContentValues(cv)
        Assert.assertNull(cvAlarm)
    }
}