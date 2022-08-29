/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.AlarmRelativeTo
import at.techbee.jtx.ui.compose.dialogs.DatePickerDialog
import at.techbee.jtx.util.DateTimeUtils
import java.time.Duration


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmCard(
    alarm: Alarm,
    icalObject: ICalObject,
    isEditMode: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    onAlarmDeleted: () -> Unit,
    onAlarmChanged: (Alarm) -> Unit
) {

    val context = LocalContext.current

    var showDateTimePickerDialog by remember { mutableStateOf(false) }

    if(showDateTimePickerDialog && alarm.triggerTime != null) {
        DatePickerDialog(
            datetime = alarm.triggerTime!!,
            timezone = alarm.triggerTimezone,
            allowNull = false,
            onConfirm = { changedDateTime, changedTimezone ->
                alarm.triggerTime = changedDateTime
                alarm.triggerTimezone = changedTimezone
                onAlarmChanged(alarm) }) {
        }
    }

    if (isEditMode.value) {
        OutlinedCard(
            onClick = { /* TODO */
                      if(alarm.triggerTime != null)
                          showDateTimePickerDialog = true
                      },
            modifier = modifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(Icons.Outlined.Alarm, stringResource(R.string.alarms))

                Column(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .align(alignment = Alignment.CenterVertically)
                        .weight(1f)
                ) {
                    if(alarm.triggerTime != null) {
                        Text(
                            text = DateTimeUtils.convertLongToFullDateTimeString(alarm.triggerTime, alarm.triggerTimezone),
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .fillMaxWidth(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if(alarm.triggerRelativeDuration != null) {
                        alarm.getTriggerDurationAsString(context)?.let { durationText ->
                            Text(
                                text = durationText,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .fillMaxWidth(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        val referenceDateTime = if(alarm.triggerRelativeTo == AlarmRelativeTo.START.name) icalObject.dtstart else icalObject.due
                        val referenceTimeZone = if(alarm.triggerRelativeTo == AlarmRelativeTo.START.name) icalObject.dtstartTimezone else icalObject.dueTimezone
                        alarm.getDatetimeFromTriggerDuration(referenceDateTime, referenceTimeZone)?.let { durationDateTime ->
                            Text(
                                text = DateTimeUtils.convertLongToFullDateTimeString(durationDateTime, referenceTimeZone),
                                modifier = Modifier
                                    .padding(horizontal = 8.dp),
                                //.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                IconButton(onClick = { onAlarmDeleted() }) {
                    Icon(Icons.Outlined.Delete, stringResource(id = R.string.delete))
                }
            }
        }
    } else {
        ElevatedCard(
            modifier = modifier
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(Icons.Outlined.Alarm, stringResource(R.string.alarms))

                Column(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .align(alignment = Alignment.CenterVertically)
                        .weight(1f)
                ) {
                    if(alarm.triggerTime != null) {
                        Text(
                            text = DateTimeUtils.convertLongToFullDateTimeString(alarm.triggerTime, alarm.triggerTimezone),
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .fillMaxWidth(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if(alarm.triggerRelativeDuration != null) {
                        alarm.getTriggerDurationAsString(context)?.let { durationText ->
                            Text(
                                text = durationText,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .fillMaxWidth(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        val referenceDateTime = if(alarm.triggerRelativeTo == AlarmRelativeTo.START.name) icalObject.dtstart else icalObject.due
                        val referenceTimeZone = if(alarm.triggerRelativeTo == AlarmRelativeTo.START.name) icalObject.dtstartTimezone else icalObject.dueTimezone
                        alarm.getDatetimeFromTriggerDuration(referenceDateTime, referenceTimeZone)?.let { durationDateTime ->
                            Text(
                                text = DateTimeUtils.convertLongToFullDateTimeString(durationDateTime, referenceTimeZone),
                                modifier = Modifier
                                    .padding(horizontal = 8.dp),
                                    //.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmCardPreview_DateTime_view() {
    MaterialTheme {
        AlarmCard(
            alarm = Alarm.createDisplayAlarm(System.currentTimeMillis(), null),
            icalObject = ICalObject.createTodo().apply { dtstart = System.currentTimeMillis() },
            isEditMode = remember { mutableStateOf(false) },
            onAlarmDeleted = { /*TODO*/ },
            onAlarmChanged = { /*TODO*/ }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmCardPreview_Duration_START_view() {
    MaterialTheme {
        AlarmCard(
            alarm = Alarm.createDisplayAlarm(Duration.ofMinutes(-15), AlarmRelativeTo.START),
            icalObject = ICalObject.createTodo().apply { dtstart = System.currentTimeMillis() },
            isEditMode = remember { mutableStateOf(false) },
            onAlarmDeleted = { /*TODO*/ },
            onAlarmChanged = { /*TODO*/ }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AlarmCardPreview_Duration_END_view() {
    MaterialTheme {
        AlarmCard(
            alarm = Alarm.createDisplayAlarm(Duration.ofDays(1), AlarmRelativeTo.END),
            icalObject = ICalObject.createTodo().apply {
                due = System.currentTimeMillis()
                dueTimezone = null
                                                       },
            isEditMode = remember { mutableStateOf(false) },
            onAlarmDeleted = { /*TODO*/ },
            onAlarmChanged = { /*TODO*/ }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AlarmCardPreview_edit() {
    MaterialTheme {
        AlarmCard(
            alarm = Alarm(
                triggerTime = System.currentTimeMillis()
            ),
            icalObject = ICalObject.createTodo().apply { dtstart = System.currentTimeMillis() },
            isEditMode = remember { mutableStateOf(true) },
            onAlarmDeleted = { /*TODO*/ },
            onAlarmChanged = { /*TODO*/ }
        )
    }
}
