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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.AlarmRelativeTo
import at.techbee.jtx.ui.reusable.dialogs.DatePickerDialog
import at.techbee.jtx.ui.reusable.dialogs.DurationPickerDialog
import at.techbee.jtx.util.DateTimeUtils
import java.time.ZoneId
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes


@Composable
fun AlarmCard(
    alarm: Alarm,
    icalObject: ICalObject,
    isReadOnly: Boolean,
    modifier: Modifier = Modifier,
    onAlarmDeleted: () -> Unit,
    onAlarmChanged: (Alarm) -> Unit
) {

    val context = LocalContext.current

    var showDateTimePickerDialog by rememberSaveable { mutableStateOf(false) }
    var showDurationPickerDialog by rememberSaveable { mutableStateOf(false) }

    if (showDateTimePickerDialog && alarm.triggerTime != null) {
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

    if (showDurationPickerDialog && alarm.triggerRelativeDuration != null) {
        DurationPickerDialog(
            alarm = alarm,
            icalObject = icalObject,
            onConfirm = { changedAlarm ->
                onAlarmChanged(changedAlarm)
            },
            onDismiss = { showDurationPickerDialog = false }
        )
    }

    ElevatedCard(
        onClick = {
            if(isReadOnly)
                return@ElevatedCard
            else if (alarm.triggerRelativeDuration != null)
                showDurationPickerDialog = true
            else if (alarm.triggerTime != null)
                showDateTimePickerDialog = true
        },
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
                if (alarm.triggerRelativeDuration != null) {
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
                }
                if (alarm.triggerTime != null) {
                    Text(
                        text = DateTimeUtils.convertLongToFullDateTimeString(
                            alarm.triggerTime,
                            alarm.triggerTimezone
                        ),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if(!isReadOnly) {
                IconButton(onClick = { onAlarmDeleted() }) {
                    Icon(Icons.Outlined.Delete, stringResource(id = R.string.delete))
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
            isReadOnly = false,
            onAlarmDeleted = { },
            onAlarmChanged = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmCardPreview_Duration_START_view() {
    MaterialTheme {
        AlarmCard(
            alarm = Alarm.createDisplayAlarm(
                (-15).minutes,
                AlarmRelativeTo.START,
                System.currentTimeMillis(),
                null
            ),
            icalObject = ICalObject.createTodo().apply { dtstart = System.currentTimeMillis() },
            isReadOnly = false,
            onAlarmDeleted = { },
            onAlarmChanged = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AlarmCardPreview_Duration_END_view() {
    MaterialTheme {
        AlarmCard(
            alarm = Alarm.createDisplayAlarm(
                (1).days,
                AlarmRelativeTo.END,
                System.currentTimeMillis(),
                null
            ),
            icalObject = ICalObject.createTodo().apply {
                due = System.currentTimeMillis()
                dueTimezone = null
            },
            isReadOnly = false,
            onAlarmDeleted = { },
            onAlarmChanged = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AlarmCardPreview_Duration_END_view_timezone() {
    MaterialTheme {
        AlarmCard(
            alarm = Alarm.createDisplayAlarm(
                (1).days,
                AlarmRelativeTo.END,
                System.currentTimeMillis(),
                ZoneId.of("Mexico/General").id
            ),
            icalObject = ICalObject.createTodo().apply {
                due = System.currentTimeMillis()
                dueTimezone = ZoneId.of("Mexico/General").id
            },
            isReadOnly = true,
            onAlarmDeleted = { },
            onAlarmChanged = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmCardPreview_readOnly() {
    MaterialTheme {
        AlarmCard(
            alarm = Alarm(
                triggerTime = System.currentTimeMillis()
            ),
            icalObject = ICalObject.createTodo().apply { dtstart = System.currentTimeMillis() },
            isReadOnly = true,
            onAlarmDeleted = { },
            onAlarmChanged = { }
        )
    }
}
