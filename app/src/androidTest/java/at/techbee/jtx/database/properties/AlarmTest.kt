/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.properties

import android.content.ContentValues
import at.techbee.jtx.JtxContract
import org.junit.Assert
import org.junit.Test


class AlarmTest {
// Android Test as Content Values need Android libraries to run



    @Test
    fun createFromContentValues() {

        val sampleAlarm = Alarm(
            icalObjectId = 1L,
            action = "AUDIO" ,
            description = "my description",
            triggerRelativeDuration = "-PT15M",
            triggerRelativeTo = JtxContract.JtxAlarm.AlarmRelativeTo.START.name,
            triggerTime = 1641560551926L,
            triggerTimezone = "Europe/Vienna",
            summary = "summary",
            duration = "PT15M",
            attach = "ftp://example.com/pub/sounds/bell-01.aud",
            attendee = "info@techbee.at",
            repeat = "4",
            other = "other"
        )

        val cv = ContentValues(10).apply {
            put(COLUMN_ALARM_ICALOBJECT_ID, sampleAlarm.icalObjectId)
            put(COLUMN_ALARM_DESCRIPTION, sampleAlarm.description)
            put(COLUMN_ALARM_ACTION, sampleAlarm.action)
            put(COLUMN_ALARM_TRIGGER_RELATIVE_DURATION, sampleAlarm.triggerRelativeDuration)
            put(COLUMN_ALARM_TRIGGER_RELATIVE_TO, sampleAlarm.triggerRelativeTo)
            put(COLUMN_ALARM_TRIGGER_TIME, sampleAlarm.triggerTime)
            put(COLUMN_ALARM_TRIGGER_TIMEZONE, sampleAlarm.triggerTimezone)
            put(COLUMN_ALARM_SUMMARY, sampleAlarm.summary)
            put(COLUMN_ALARM_DURATION, sampleAlarm.duration)
            put(COLUMN_ALARM_ATTACH, sampleAlarm.attach)
            put(COLUMN_ALARM_ATTENDEE, sampleAlarm.attendee)
            put(COLUMN_ALARM_REPEAT, sampleAlarm.repeat)
            put(COLUMN_ALARM_OTHER, sampleAlarm.other)
        }

        val cvAlarm = Alarm.fromContentValues(cv)
        Assert.assertEquals(sampleAlarm, cvAlarm)
    }

    @Test
    fun createFromContentValuesWithoutIcalobjectId() {

        val cv = ContentValues(1).apply {
            put(COLUMN_ALARM_SUMMARY,  "alarm")
        }

        val cvAlarm = Alarm.fromContentValues(cv)
        Assert.assertNull(cvAlarm)
    }
}