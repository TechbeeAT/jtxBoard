package at.techbee.jtx.database.properties

import android.content.ContentValues
import org.junit.Assert
import org.junit.Test


class UnknownTest {
// Android Test as Content Values need Android libraries to run


    @Test
    fun createFromContentValues() {

        val sampleUnknown = Unknown(
            icalObjectId = 1L,
            value = "[\"X-UNKNOWN\",\"PropValue\",{\"X-PARAM1\":\"value1\",\"X-PARAM2\":\"value2\"}]",
        )

        val cv = ContentValues(5).apply {
            put(COLUMN_UNKNOWN_ICALOBJECT_ID, sampleUnknown.icalObjectId)
            put(COLUMN_UNKNOWN_VALUE, sampleUnknown.value)
        }

        val cvUnknown = Alarm.fromContentValues(cv)
        Assert.assertEquals(sampleUnknown, cvUnknown)
    }

    @Test
    fun createFromContentValuesWithoutIcalobjectId() {

        val cv = ContentValues(1).apply {
            put(COLUMN_UNKNOWN_VALUE,  "[\"X-UNKNOWN\",\"PropValue\",{\"X-PARAM1\":\"value1\",\"X-PARAM2\":\"value2\"}]")
        }

        val cvUnknown = Unknown.fromContentValues(cv)
        Assert.assertNull(cvUnknown)
    }

    @Test
    fun createFromContentValuesWithoutText() {


        val cv = ContentValues(1).apply {
            put(COLUMN_UNKNOWN_ICALOBJECT_ID, 1L)
        }

        val cvUnknown = Unknown.fromContentValues(cv)
        Assert.assertNull(cvUnknown)
    }
}