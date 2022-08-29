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
import java.time.Duration


@Composable
fun DetailsCardAlarms(
    alarms: MutableState<List<Alarm>>,
    icalObject: ICalObject,
    isEditMode: MutableState<Boolean>,
    onAlarmsUpdated: () -> Unit,
    modifier: Modifier = Modifier
) {

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
            onConfirm = { newDateTime, newTimeZone ->
                val newAlarm = Alarm.createDisplayAlarm(newDateTime!!, newTimeZone)
                alarms.value = alarms.value.plus(newAlarm)
                onAlarmsUpdated() },
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
                alarms.value = alarms.value.plus(newAlarm)
                onAlarmsUpdated()
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

            AnimatedVisibility(alarms.value.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    alarms.value.forEach { alarm ->

                        AlarmCard(
                            alarm = alarm,
                            icalObject = icalObject,
                            isEditMode = isEditMode,
                            onAlarmDeleted = {
                                alarms.value = alarms.value.minus(alarm)
                                onAlarmsUpdated()
                                             },
                            onAlarmChanged = { changedAlarm ->
                                changedAlarm.alarmId = 0L
                                alarms.value = alarms.value.minus(alarm)
                                alarms.value = alarms.value.plus(changedAlarm)
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(isEditMode.value) {

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
                    AnimatedVisibility(icalObject.dtstart != null && !alarms.value.contains(alarmOnStart)) {
                        TextButton(onClick = {
                            alarms.value = alarms.value.plus(Alarm.createDisplayAlarm(Duration.ZERO, AlarmRelativeTo.START))
                            onAlarmsUpdated()
                        }) {
                            Icon(Icons.Outlined.AlarmAdd, null, modifier = Modifier.padding(end = 8.dp))
                            Text(stringResource(id = R.string.alarms_onstart))
                        }
                    }

                    val alarmOnDue = Alarm.createDisplayAlarm(Duration.ZERO, AlarmRelativeTo.END)
                    AnimatedVisibility(icalObject.due != null && !alarms.value.contains(alarmOnDue)) {
                        TextButton(onClick = {
                            alarms.value = alarms.value.plus(Alarm.createDisplayAlarm(Duration.ZERO, AlarmRelativeTo.END))
                            onAlarmsUpdated()
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
            alarms = remember {
                mutableStateOf(
                    listOf(
                        Alarm.createDisplayAlarm(System.currentTimeMillis(), null),
                        Alarm.createDisplayAlarm(Duration.ofDays(1), AlarmRelativeTo.START
                        )
                    )
                )
            },
            icalObject = ICalObject.createTodo().apply {
                dtstart = System.currentTimeMillis()
                dtstartTimezone = null
                due = System.currentTimeMillis()
                dueTimezone = null
                                                       },
            isEditMode = remember { mutableStateOf(false) },
            onAlarmsUpdated = { /*TODO*/ }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardAlarms_Preview_edit() {
    MaterialTheme {
        DetailsCardAlarms(
            alarms = remember {
                mutableStateOf(
                    listOf(
                        Alarm.createDisplayAlarm(System.currentTimeMillis(), null),
                        Alarm.createDisplayAlarm(Duration.ofDays(1), AlarmRelativeTo.START
                        )
                    )
                )
            },
            icalObject = ICalObject.createTodo().apply {
                dtstart = System.currentTimeMillis()
                dtstartTimezone = null
                due = System.currentTimeMillis()
                dueTimezone = null
            },
            isEditMode = remember { mutableStateOf(true) },
            onAlarmsUpdated = { /*TODO*/ }
        )
    }
}