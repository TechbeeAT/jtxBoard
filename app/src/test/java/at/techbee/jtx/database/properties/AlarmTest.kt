/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.properties

import org.junit.Assert.*
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class AlarmTest {

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
        val alarm = Alarm.createDisplayAlarm((0).minutes, AlarmRelativeTo.START, System.currentTimeMillis(), null)
        assertEquals(AlarmAction.DISPLAY.name, alarm.action)
        assertEquals("PT0S", alarm.triggerRelativeDuration)
        assertEquals(AlarmRelativeTo.START.name, alarm.triggerRelativeTo)
        assertNotNull(alarm.triggerTime)
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
    fun getTriggerAsDuration() {
        val alarm = Alarm.createDisplayAlarm()
        alarm.triggerRelativeDuration = (0).minutes.toString()
        assertEquals((0).minutes, alarm.getTriggerAsDuration())
    }

    @Test
    fun getTriggerAsDuration_error() {
        val alarm = Alarm.createDisplayAlarm()
        alarm.triggerRelativeDuration = "asdf"
        assertNull(alarm.getTriggerAsDuration())
    }
}