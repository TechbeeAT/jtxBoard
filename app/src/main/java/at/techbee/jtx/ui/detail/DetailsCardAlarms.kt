/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.Manifest
import android.os.Build
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.AlarmRelativeTo
import at.techbee.jtx.ui.reusable.cards.AlarmCard
import at.techbee.jtx.ui.reusable.dialogs.DatePickerDialog
import at.techbee.jtx.ui.reusable.dialogs.DurationPickerDialog
import at.techbee.jtx.ui.reusable.dialogs.RequestPermissionDialog
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DetailsCardAlarms(
    alarms: SnapshotStateList<Alarm>,
    icalObject: ICalObject,
    isEditMode: Boolean,
    onAlarmsUpdated: () -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.alarms)

    var showDateTimePicker by rememberSaveable { mutableStateOf(false) }
    var showDurationPicker by rememberSaveable { mutableStateOf(false) }

    val notificationsPermissionState = if (!LocalInspectionMode.current && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS) else null
    //val exactAlarmPermissionState = if (!LocalInspectionMode.current && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) rememberPermissionState(permission = Manifest.permission.SCHEDULE_EXACT_ALARM) else null


    if(showDateTimePicker) {
        val initialDateTime = if(icalObject.module == Module.JOURNAL.name) icalObject.dtstart ?: System.currentTimeMillis() else icalObject.due ?: System.currentTimeMillis()
        val initialTimeZone = if(icalObject.module == Module.JOURNAL.name) icalObject.dtstartTimezone else icalObject.dueTimezone
        DatePickerDialog(
            datetime = initialDateTime,
            timezone = initialTimeZone,
            allowNull = false,
            minDate = ZonedDateTime.now().minusDays(1),
            onConfirm = { newDateTime, newTimeZone ->
                val newAlarm = Alarm.createDisplayAlarm(newDateTime!!, newTimeZone)
                alarms.add(newAlarm)
                onAlarmsUpdated() },
            onDismiss = { showDateTimePicker = false }
        )
    }

    if(showDurationPicker) {
        DurationPickerDialog(
            alarm = Alarm.createDisplayAlarm(
                dur = (-15).minutes,
                alarmRelativeTo = if(icalObject.due != null) AlarmRelativeTo.END else AlarmRelativeTo.START,
                referenceDate = if(icalObject.due != null) icalObject.due!! else icalObject.dtstart!!,
                referenceTimezone = if(icalObject.due != null) icalObject.dueTimezone else icalObject.dtstartTimezone
            ),
            icalObject = icalObject,
            onConfirm = { newAlarm ->
                if(alarms.none { alarm ->
                        alarm.triggerRelativeDuration == newAlarm.triggerRelativeDuration
                                && alarm.triggerRelativeTo == newAlarm.triggerRelativeTo
                                && alarm.triggerTime == newAlarm.triggerTime
                                && alarm.triggerTimezone == newAlarm.triggerTimezone
                }) {
                    alarms.add(newAlarm)
                    onAlarmsUpdated()
                }
            },
            onDismiss = { showDurationPicker = false }
        )
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
                                alarms.remove(alarm)
                                onAlarmsUpdated()
                                             },
                            onAlarmChanged = { changedAlarm ->
                                changedAlarm.alarmId = 0L
                                alarms.remove(alarm)
                                alarms.add(changedAlarm)
                                onAlarmsUpdated()
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
                        Text(stringResource(R.string.alarms_date_time))
                    }

                    AnimatedVisibility(icalObject.dtstart != null || icalObject.due != null) {
                        TextButton(onClick = { showDurationPicker = true }) {
                            Icon(Icons.Outlined.AlarmAdd, null, modifier = Modifier.padding(end = 8.dp))
                            Text(stringResource(id = R.string.alarms_duration))
                        }
                    }

                    val alarmOnStart = Alarm.createDisplayAlarm((0).minutes, AlarmRelativeTo.START, icalObject.dtstart ?: System.currentTimeMillis(), icalObject.dtstartTimezone)
                    AnimatedVisibility(icalObject.dtstart != null && alarms.none { it.triggerTime == alarmOnStart.triggerTime }) {
                        TextButton(onClick = {
                            alarms.add(Alarm.createDisplayAlarm((0).minutes, AlarmRelativeTo.START, icalObject.dtstart!!, icalObject.dtstartTimezone))
                            onAlarmsUpdated()
                        }) {
                            Icon(Icons.Outlined.AlarmAdd, null, modifier = Modifier.padding(end = 8.dp))
                            Text(stringResource(id = R.string.alarms_onstart))
                        }
                    }

                    val alarmOnDue = Alarm.createDisplayAlarm((0).minutes, AlarmRelativeTo.END, icalObject.due ?: System.currentTimeMillis(), icalObject.dueTimezone)
                    AnimatedVisibility(icalObject.due != null && alarms.none { it.triggerTime == alarmOnDue.triggerTime }) {
                        TextButton(onClick = {
                            alarms.add(Alarm.createDisplayAlarm((0).minutes, AlarmRelativeTo.END, icalObject.due!!, icalObject.dueTimezone))
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

    if(notificationsPermissionState?.status?.shouldShowRationale == false && !notificationsPermissionState.status.isGranted) {   // second part = permission is NOT permanently denied!
        RequestPermissionDialog(
            text = stringResource(id = R.string.edit_fragment_app_notification_permission_message),
            onConfirm = { notificationsPermissionState.launchPermissionRequest() }
        )
    } /* else if(exactAlarmPermissionState?.status?.shouldShowRationale == false && !exactAlarmPermissionState.status.isGranted) {   // second part = permission is NOT permanently denied!
        RequestPermissionDialog(
            text = stringResource(id = R.string.exact_alarm_permission_dialog_message),
            onConfirm = { exactAlarmPermissionState.launchPermissionRequest() }
        )
    }
    */
}

@Preview(showBackground = true)
@Composable
fun DetailsCardAlarms_Preview() {
    MaterialTheme {

        DetailsCardAlarms(
            alarms = remember { mutableStateListOf(
                        Alarm.createDisplayAlarm(System.currentTimeMillis(), null),
                        Alarm.createDisplayAlarm((0).days, AlarmRelativeTo.START, System.currentTimeMillis(), null)
                ) },
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
            alarms = remember { mutableStateListOf(
                        Alarm.createDisplayAlarm(System.currentTimeMillis(), null),
                        Alarm.createDisplayAlarm((1).days, AlarmRelativeTo.START, System.currentTimeMillis(), null)
            )},
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