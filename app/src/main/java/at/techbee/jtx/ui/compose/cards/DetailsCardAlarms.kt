/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.AlarmAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.AlarmRelativeTo
import at.techbee.jtx.ui.compose.dialogs.DatePickerDialog
import at.techbee.jtx.ui.compose.dialogs.DurationPickerDialog
import at.techbee.jtx.ui.compose.elements.HeadlineWithIcon
import at.techbee.jtx.util.DateTimeUtils
import java.time.Duration


@Composable
fun DetailsCardAlarms(
    initialAlarms: List<Alarm>,
    icalObject: ICalObject,
    isEditMode: Boolean,
    onAlarmsUpdated: (List<Alarm>) -> Unit,
    modifier: Modifier = Modifier
) {

    var alarms by remember { mutableStateOf(initialAlarms) }
    val headline = stringResource(id = R.string.alarms)

    var showDateTimePicker by rememberSaveable { mutableStateOf(false) }
    var showDurationPicker by rememberSaveable { mutableStateOf(false) }


    if(showDateTimePicker) {
        val initialDateTime = if(icalObject.module == Module.JOURNAL.name) icalObject.dtstart ?: System.currentTimeMillis() else icalObject.due ?: System.currentTimeMillis()
        val initialTimeZone = if(icalObject.module == Module.JOURNAL.name) icalObject.dtstartTimezone else icalObject.dueTimezone
        DatePickerDialog(
            datetime = initialDateTime,
            timezone = initialTimeZone,
            allowNull = false,
            minDate = DateTimeUtils.getTodayAsLong(),
            onConfirm = { newDateTime, newTimeZone ->
                val newAlarm = Alarm.createDisplayAlarm(newDateTime!!, newTimeZone)
                alarms = alarms.plus(newAlarm)
                onAlarmsUpdated(alarms) },
            onDismiss = { showDateTimePicker = false }
        )
    }

    if(showDurationPicker) {
        DurationPickerDialog(
            alarm = Alarm.createDisplayAlarm(
                dur = Duration.ofMinutes(15),
                alarmRelativeTo = if(icalObject.due != null) AlarmRelativeTo.END else AlarmRelativeTo.START
            ),
            icalObject = icalObject,
            onConfirm = { newAlarm ->
                alarms = alarms.plus(newAlarm)
                onAlarmsUpdated(alarms)
            },
            onDismiss = { showDurationPicker = false })
    }

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            HeadlineWithIcon(icon = Icons.Outlined.Alarm, iconDesc = headline, text = headline)

            AnimatedVisibility(alarms.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    alarms.forEach { alarm ->

                        AlarmCard(
                            alarm = alarm,
                            icalObject = icalObject,
                            isEditMode = isEditMode,
                            onAlarmDeleted = {
                                alarms = alarms.minus(alarm)
                                onAlarmsUpdated(alarms)
                                             },
                            onAlarmChanged = { changedAlarm ->
                                changedAlarm.alarmId = 0L
                                alarms = alarms.minus(alarm)
                                alarms = alarms.plus(changedAlarm)
                                onAlarmsUpdated(alarms)
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(isEditMode) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 8.dp)
                ) {

                    TextButton(onClick = { showDateTimePicker = true }) {
                        Icon(Icons.Outlined.AlarmAdd, null, modifier = Modifier.padding(end = 8.dp))
                        Text("Date/Time")
                    }

                    AnimatedVisibility(icalObject.dtstart != null || icalObject.due != null) {
                        TextButton(onClick = { showDurationPicker = true }) {
                            Icon(Icons.Outlined.AlarmAdd, null, modifier = Modifier.padding(end = 8.dp))
                            Text("Duration")
                        }
                    }

                    val alarmOnStart = Alarm.createDisplayAlarm(Duration.ZERO, AlarmRelativeTo.START)
                    AnimatedVisibility(icalObject.dtstart != null && !alarms.contains(alarmOnStart)) {
                        TextButton(onClick = {
                            alarms = alarms.plus(Alarm.createDisplayAlarm(Duration.ZERO, AlarmRelativeTo.START))
                            onAlarmsUpdated(alarms)
                        }) {
                            Icon(Icons.Outlined.AlarmAdd, null, modifier = Modifier.padding(end = 8.dp))
                            Text(stringResource(id = R.string.alarms_onstart))
                        }
                    }

                    val alarmOnDue = Alarm.createDisplayAlarm(Duration.ZERO, AlarmRelativeTo.END)
                    AnimatedVisibility(icalObject.due != null && !alarms.contains(alarmOnDue)) {
                        TextButton(onClick = {
                            alarms = alarms.plus(Alarm.createDisplayAlarm(Duration.ZERO, AlarmRelativeTo.END))
                            onAlarmsUpdated(alarms)
                        }) {
                            Icon(Icons.Outlined.AlarmAdd, null, modifier = Modifier.padding(end = 8.dp))
                            Text(stringResource(id = R.string.alarms_ondue))
                        }
                    }
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardAlarms_Preview() {
    MaterialTheme {

        DetailsCardAlarms(
            initialAlarms = listOf(
                        Alarm.createDisplayAlarm(System.currentTimeMillis(), null),
                        Alarm.createDisplayAlarm(Duration.ofDays(1), AlarmRelativeTo.START)
            ),
            icalObject = ICalObject.createTodo().apply {
                dtstart = System.currentTimeMillis()
                dtstartTimezone = null
                due = System.currentTimeMillis()
                dueTimezone = null
                                                       },
            isEditMode = false,
            onAlarmsUpdated = {  }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardAlarms_Preview_edit() {
    MaterialTheme {
        DetailsCardAlarms(
            initialAlarms = listOf(
                        Alarm.createDisplayAlarm(System.currentTimeMillis(), null),
                        Alarm.createDisplayAlarm(Duration.ofDays(1), AlarmRelativeTo.START)
            ),
            icalObject = ICalObject.createTodo().apply {
                dtstart = System.currentTimeMillis()
                dtstartTimezone = null
                due = System.currentTimeMillis()
                dueTimezone = null
            },
            isEditMode = true,
            onAlarmsUpdated = {  }
        )
    }
}