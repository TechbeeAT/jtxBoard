/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.dialogs

import android.os.Build
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreTime
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import at.techbee.jtx.R
import at.techbee.jtx.contract.JtxContract.JtxICalObject.TZ_ALLDAY
import at.techbee.jtx.ui.compose.elements.TimezoneAutocompleteTextfield
import at.techbee.jtx.util.DateTimeUtils
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*


@Composable
fun DatePickerDialog(
    datetime: Long,
    timezone: String?,
    dateOnly: Boolean = false,
    onConfirm: (newDateTime: Long, newTimezone: String?) -> Unit,
    onDismiss: () -> Unit
) {

    val tabIndexDate = 0
    val tabIndexTime = 1
    val tabIndexTimezone = 2
    var selectedTab by remember { mutableStateOf(0) }

    var newDateTime by rememberSaveable {
        mutableStateOf(
            ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(
                    datetime
                ), DateTimeUtils.requireTzId(timezone)
            )
        )
    }
    var newTimezone by rememberSaveable { mutableStateOf(timezone) }
    val defaultTimezone = if (LocalInspectionMode.current) "Europe/Vienna" else TimeZone.getDefault().id

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.edit_datepicker_dialog_select_date)) },
        text = {

            Column {

                if(!dateOnly) {

                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(selected = selectedTab == tabIndexDate,
                            onClick = { selectedTab = tabIndexDate },
                            text = {
                                Icon(
                                    Icons.Outlined.Today,
                                    stringResource(id = R.string.date),
                                    modifier = Modifier.size(24.dp)
                                )
                            })

                        Tab(selected = selectedTab == tabIndexTime,
                            onClick = { selectedTab = tabIndexTime },
                            enabled = newTimezone != TZ_ALLDAY,
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Checkbox(
                                        checked = newTimezone != TZ_ALLDAY,
                                        enabled = true,
                                        onCheckedChange = {
                                            newTimezone = if (it) null else TZ_ALLDAY
                                            selectedTab = if (it) tabIndexTime else tabIndexDate
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Icon(
                                        Icons.Outlined.MoreTime,
                                        stringResource(id = R.string.add_time_switch),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            })

                        Tab(selected = selectedTab == tabIndexTimezone,
                            onClick = { selectedTab = tabIndexTimezone },
                            enabled = newTimezone != TZ_ALLDAY && newTimezone != null,
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Checkbox(
                                        checked = newTimezone != TZ_ALLDAY && newTimezone != null,
                                        enabled = newTimezone != TZ_ALLDAY,
                                        onCheckedChange = {
                                            newTimezone = if (it) defaultTimezone else null
                                            selectedTab = if (it) tabIndexTimezone else tabIndexTime
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Icon(
                                        Icons.Outlined.TravelExplore,
                                        stringResource(id = R.string.timezone),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            })
                    }
                }


                Crossfade(selectedTab) { tabIndex ->
                    when (tabIndex) {
                        tabIndexDate -> {
                            AndroidView(
                                //modifier = Modifier.fillMaxSize(), // Occupy the max size in the Compose UI tree
                                factory = { context ->
                                    // Creates custom view
                                    val datepicker = DatePicker(context)
                                    datepicker.init(
                                        newDateTime.year,
                                        newDateTime.monthValue-1,
                                        newDateTime.dayOfMonth
                                    ) { _, year, monthOfYear, dayOfMonth ->
                                        newDateTime =
                                            newDateTime.withYear(year).withMonth(monthOfYear+1)
                                                .withDayOfMonth(dayOfMonth)
                                    }
                                    datepicker.updateDate(
                                        newDateTime.year,
                                        newDateTime.monthValue-1,
                                        newDateTime.dayOfMonth
                                    )
                                    datepicker.rootView
                                }
                            )
                        }
                        tabIndexTime -> {
                            AndroidView(
                                //modifier = Modifier.fillMaxSize(), // Occupy the max size in the Compose UI tree
                                factory = { context ->
                                    // Creates custom view
                                    val timepicker = TimePicker(context)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        timepicker.hour = newDateTime.hour
                                        timepicker.minute = newDateTime.minute
                                    } else {
                                        timepicker.currentHour = newDateTime.hour
                                        timepicker.currentMinute = newDateTime.minute
                                    }

                                    timepicker.setOnTimeChangedListener { _, hour, minute ->
                                        newDateTime = newDateTime.withHour(hour).withMinute(minute)
                                    }
                                    timepicker.rootView
                                }
                            )
                        }
                        tabIndexTimezone -> {
                            TimezoneAutocompleteTextfield(
                                timezone = newTimezone,
                                onTimezoneChanged = { tz -> newTimezone = tz }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newTimezone != null && newTimezone != TZ_ALLDAY && !TimeZone.getAvailableIDs()
                            .contains(newTimezone)
                    )
                        selectedTab = tabIndexTimezone

                    when (newTimezone) {
                        TZ_ALLDAY -> newDateTime = newDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0).withZoneSameLocal(ZoneId.of("UTC"))
                        in TimeZone.getAvailableIDs() -> newDateTime = newDateTime.withZoneSameLocal(ZoneId.of(newTimezone))
                        null -> newDateTime = newDateTime.withZoneSameLocal(ZoneId.systemDefault())
                    }

                    onConfirm(newDateTime.toInstant().toEpochMilli(), newTimezone)
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
                Text(stringResource(id = R.string.cancel))
            }
        }
    )

}

@Preview(showBackground = true)
@Composable
fun DatePickerDialog_Preview() {
    MaterialTheme {

        DatePickerDialog(
            datetime = 1660500481224,   // 14 Aug 2022  18:09
            timezone = TZ_ALLDAY,
            onConfirm = { _, _ -> },
            onDismiss = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DatePickerDialog_Preview_dateonly() {
    MaterialTheme {

        DatePickerDialog(
            datetime = 1660500481224,   // 14 Aug 2022  18:09
            timezone = TZ_ALLDAY,
            dateOnly = true,
            onConfirm = { _, _ -> },
            onDismiss = { }
        )
    }
}
