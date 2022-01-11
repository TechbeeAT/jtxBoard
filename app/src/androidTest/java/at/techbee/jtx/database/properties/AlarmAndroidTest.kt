/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.properties

import android.content.ContentValues
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import at.techbee.jtx.JtxContract
import at.techbee.jtx.R
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.time.Duration


class AlarmAndroidTest {
// Android Test as Content Values need Android libraries to run

    private lateinit var context: Context

    @Before
    fun createDb() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

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

    @Test
    fun getTriggerDurationAsString_onStart() {
        val alarm = Alarm.createDisplayAlarm(Duration.ZERO, null)
        val durText = alarm.getTriggerDurationAsString(context)
        Assert.assertEquals(context.getString(R.string.alarms_onstart), durText)
    }

    @Test
    fun getTriggerDurationAsString_onStart_explicit() {
        val alarm = Alarm.createDisplayAlarm(Duration.ZERO, AlarmRelativeTo.START)
        val durText = alarm.getTriggerDurationAsString(context)
        Assert.assertEquals(context.getString(R.string.alarms_onstart), durText)
    }

    @Test
    fun getTriggerDurationAsString_onDue() {
        val alarm = Alarm.createDisplayAlarm(Duration.ZERO, AlarmRelativeTo.END)
        val durText = alarm.getTriggerDurationAsString(context)
        Assert.assertEquals(context.getString(R.string.alarms_ondue), durText)
    }

    @Test
    fun getTriggerDurationAsString_15minBeforeStart() {
        val alarm = Alarm.createDisplayAlarm(Duration.ofMinutes(-15), AlarmRelativeTo.START)
        val durText = alarm.getTriggerDurationAsString(context)
        val expectedArg1 = 15
        val expectedArg2 = context.getString(R.string.alarms_minutes)
        val expectedArg3 = context.getString(R.string.alarms_before_start)
        val durTextExpected = context.getString(R.string.alarms_duration_full_string, expectedArg1, expectedArg2, expectedArg3)
        Assert.assertEquals(durTextExpected, durText)
    }

    @Test
    fun getTriggerDurationAsString_12hoursAfterStart() {
        val alarm = Alarm.createDisplayAlarm(Duration.ofHours(12), AlarmRelativeTo.START)
        val durText = alarm.getTriggerDurationAsString(context)
        val expectedArg1 = 12
        val expectedArg2 = context.getString(R.string.alarms_hours)
        val expectedArg3 = context.getString(R.string.alarms_after_start)
        val durTextExpected = context.getString(R.string.alarms_duration_full_string, expectedArg1, expectedArg2, expectedArg3)
        Assert.assertEquals(durTextExpected, durText)
    }

    @Test
    fun getTriggerDurationAsString_1dayAfterDue() {
        val alarm = Alarm.createDisplayAlarm(Duration.ofDays(1), AlarmRelativeTo.END)
        val durText = alarm.getTriggerDurationAsString(context)
        val expectedArg1 = 1
        val expectedArg2 = context.getString(R.string.alarms_days)
        val expectedArg3 = context.getString(R.string.alarms_after_due)
        val durTextExpected = context.getString(R.string.alarms_duration_full_string, expectedArg1, expectedArg2, expectedArg3)
        Assert.assertEquals(durTextExpected, durText)
    }

    /*
    @Test
    fun getAlarmCardBinding() {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val alarm = Alarm.createDisplayAlarm(Duration.ofMinutes(-15), AlarmRelativeTo.START)
        val linLayout = LinearLayout(context)
        val time = ZonedDateTime.now()
        //val timeTrigger = time.minusMinutes(15)

        val alarmCard = alarm.getAlarmCardBinding(inflater, linLayout, time.toInstant().toEpochMilli(), null)
        Assert.assertNotNull(alarmCard)
    }
     */
}