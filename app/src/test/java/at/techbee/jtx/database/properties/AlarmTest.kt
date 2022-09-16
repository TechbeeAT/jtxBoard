/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.properties

import android.content.Context
import at.techbee.jtx.database.ICalObject.Companion.TZ_ALLDAY
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.time.Duration
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class AlarmTest {

    @Mock
    private lateinit var mockContext: Context


    @Test
    fun createDisplayAlarm_empty() {
        val alarm = Alarm.createDisplayAlarm()
        assertEquals(AlarmAction.DISPLAY.name, alarm.action)
        assertNull(alarm.triggerTime)
        assertNull(alarm.triggerTimezone)
        assertNull(alarm.triggerRelativeTo)
        assertNull(alarm.triggerRelativeDuration)
    }

    @Test
    fun createDisplayAlarm_dur() {
        val alarm = Alarm.createDisplayAlarm(Duration.ZERO, AlarmRelativeTo.START)
        assertEquals(AlarmAction.DISPLAY.name, alarm.action)
        assertEquals(Duration.ZERO.toString(), alarm.triggerRelativeDuration)
        assertEquals(AlarmRelativeTo.START.name, alarm.triggerRelativeTo)
        assertNull(alarm.triggerTime)
        assertNull(alarm.triggerTimezone)
    }

    @Test
    fun createDisplayAlarm_absolute() {
        val datetime = System.currentTimeMillis()
        val alarm = Alarm.createDisplayAlarm(datetime, null)
        assertEquals(AlarmAction.DISPLAY.name, alarm.action)
        assertEquals(datetime, alarm.triggerTime)
        assertNull(alarm.triggerTimezone)
        assertNull(alarm.triggerRelativeTo)
        assertNull(alarm.triggerRelativeDuration)
    }

    @Test
    fun getDatetimeFromTriggerDuration_negative() {
        val alarm = Alarm.createDisplayAlarm(Duration.parse("-PT15M"), null)
        val datetime = alarm.getDatetimeFromTriggerDuration(1640992500000L, null)    // 1640992500000 = 1.1.2022 00:15
        assertEquals(1640991600000L, datetime)    // 1640991600000 = 1.1.2022 00:00
    }

    @Test
    fun getDatetimeFromTriggerDuration_positive() {
        val alarm = Alarm.createDisplayAlarm(Duration.parse("PT45M"), null)
        val datetime = alarm.getDatetimeFromTriggerDuration(1640992500000L, null)    // 1640992500000 = 1.1.2022 00:15
        assertEquals(1640995200000L, datetime)    // 1640991600000 = 1.1.2022 00:00
    }

    @Test
    fun getDateTimeFromTriggerDuration_allday() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Vienna"))
        val alarm = Alarm.createDisplayAlarm(Duration.parse("-PT0M"), null)
        val datetime = alarm.getDatetimeFromTriggerDuration(1645488000000L, TZ_ALLDAY)    // 1645484400000L = 22.2.2022 00.00 (UTC)
        assertEquals(1645484400000L, datetime)    // 1645484400000L = 21.2.2022 23:00 (UTC) - we must trigger the alarm one hour earlier because midnight in Vienna is at 23:00 UTC the day before
    }

    @Test
    fun getDatetimeFromTriggerDuration_error() {
        val alarm = Alarm.createDisplayAlarm()
        alarm.triggerRelativeDuration = "asdf"
        val datetime = alarm.getDatetimeFromTriggerDuration(1640992500000L, null)    // 1640992500000 = 1.1.2022 00:15
        assertNull(datetime)
    }

    @Test
    fun getTriggerAsDuration() {
        val alarm = Alarm.createDisplayAlarm()
        alarm.triggerRelativeDuration = Duration.ZERO.toString()
        assertEquals(Duration.ZERO, alarm.getTriggerAsDuration())
    }

    @Test
    fun getTriggerAsDuration_error() {
        val alarm = Alarm.createDisplayAlarm()
        alarm.triggerRelativeDuration = "asdf"
        assertNull(alarm.getTriggerAsDuration())
    }
}