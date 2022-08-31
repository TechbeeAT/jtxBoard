/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.dialogs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.AlarmRelativeTo
import java.time.Duration
import kotlin.math.absoluteValue
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationPickerDialog(
    alarm: Alarm,
    icalObject: ICalObject,
    onConfirm: (Alarm) -> Unit,
    onDismiss: () -> Unit
) {

    val triggerAsDuration = alarm.getTriggerAsDuration() ?: Duration.ZERO

    var durationUnit by rememberSaveable { mutableStateOf(
        when {
            triggerAsDuration.toMillis()%(1000*60*60*24) == 0L -> DurationUnit.DAYS
            triggerAsDuration.toMillis()%(1000*60*60) == 0L -> DurationUnit.HOURS
            triggerAsDuration.toMillis()%(1000*60) == 0L -> DurationUnit.MINUTES
            else -> DurationUnit.MINUTES
        }
    )}

    var durationNumber by rememberSaveable { mutableStateOf(
        when {
            triggerAsDuration.toMillis()%(1000*60*60*24) == 0L -> triggerAsDuration.toDays().absoluteValue
            triggerAsDuration.toMillis()%(1000*60*60) == 0L -> triggerAsDuration.toHours().absoluteValue
            triggerAsDuration.toMillis()%(1000*60) == 0L -> triggerAsDuration.toMinutes().absoluteValue
            else -> 0
        }
    )}

    var durationBefore by rememberSaveable { mutableStateOf(triggerAsDuration.isNegative) }
    var durationStartEnd by rememberSaveable { mutableStateOf(
        if(alarm.triggerRelativeTo == AlarmRelativeTo.END.name) AlarmRelativeTo.END else AlarmRelativeTo.START
    )}



    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.alarms_alarm)) },
        text = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = durationNumber.toString(),
                    onValueChange = {
                        durationNumber = if(durationBefore)
                            (it.toLongOrNull() ?: 0L) * -1L
                        else
                            it.toLongOrNull() ?: 0L
                        },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .width(150.dp)
                        .align(Alignment.CenterHorizontally),
                    textStyle = TextStyle(textAlign = TextAlign.Center)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    FilterChip(
                        selected = durationUnit == DurationUnit.MINUTES,
                        onClick = { durationUnit = DurationUnit.MINUTES },
                        label = { Text(stringResource(id = R.string.alarms_minutes)) }
                    )
                    FilterChip(
                        selected = durationUnit == DurationUnit.HOURS,
                        onClick = { durationUnit = DurationUnit.HOURS },
                        label = { Text(stringResource(id = R.string.alarms_hours)) }
                    )
                    FilterChip(
                        selected = durationUnit == DurationUnit.DAYS,
                        onClick = { durationUnit = DurationUnit.DAYS },
                        label = { Text(stringResource(id = R.string.alarms_days)) }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    FilterChip(
                        selected = durationBefore,
                        onClick = { durationBefore = true },
                        label = { Text(stringResource(id = R.string.alarms_before)) }
                    )
                    FilterChip(
                        selected = !durationBefore,
                        onClick = { durationBefore = false },
                        label = { Text(stringResource(id = R.string.alarms_after)) }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {


                    if(icalObject.dtstart != null) {
                        FilterChip(
                            selected = durationStartEnd == AlarmRelativeTo.START,
                            onClick = { durationStartEnd = AlarmRelativeTo.START },
                            label = { Text(stringResource(id = R.string.alarms_start)) }
                        )
                    }
                    if(icalObject.due != null) {
                        FilterChip(
                            selected = durationStartEnd == AlarmRelativeTo.END,
                            onClick = { durationStartEnd = AlarmRelativeTo.END },
                            label = { Text(stringResource(id = R.string.alarms_due)) }
                        )
                    }
                }
            }
               },
        confirmButton = {
            TextButton(
                onClick = {
                    alarm.triggerRelativeTo = durationStartEnd.name
                    alarm.triggerRelativeDuration = durationNumber.toDuration(durationUnit).toIsoString()
                    onConfirm(alarm)
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text( stringResource(id = R.string.cancel))
            }
        }
    )

 }

@Preview(showBackground = true)
@Composable
fun DurationPickerDialog_Preview() {
    MaterialTheme {
        DurationPickerDialog(
            alarm = Alarm.createDisplayAlarm(Duration.ofMinutes(-15), AlarmRelativeTo.END),
            icalObject = ICalObject.createTodo().apply {
                dtstart = System.currentTimeMillis()
                dtstartTimezone = null
                due = System.currentTimeMillis()
                dueTimezone = null
            },
            onConfirm = { },
            onDismiss = { }
        )
    }
}

