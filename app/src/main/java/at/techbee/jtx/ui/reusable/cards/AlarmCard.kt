/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import at.techbee.jtx.ui.reusable.dialogs.DatePickerDialog
import at.techbee.jtx.util.DateTimeUtils
import java.time.Duration


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmCard(
    alarm: Alarm,
    icalObject: ICalObject,
    isEditMode: Boolean,
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
                onAlarmChanged(alarm)
            },
            onDismiss = {
                showDateTimePickerDialog = false
            }
        )
    }

    if (isEditMode) {
        OutlinedCard(
            onClick = {
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
            isEditMode = false,
            onAlarmDeleted = {  },
            onAlarmChanged = {  }
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
            isEditMode = false,
            onAlarmDeleted = {  },
            onAlarmChanged = {  }
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
            isEditMode = false,
            onAlarmDeleted = {  },
            onAlarmChanged = {  }
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
            isEditMode = true,
            onAlarmDeleted = {  },
            onAlarmChanged = {  }
        )
    }
}
