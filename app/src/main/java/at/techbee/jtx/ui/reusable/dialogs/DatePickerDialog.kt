/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import android.os.Build
import android.text.format.DateFormat
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreTime
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import at.techbee.jtx.R
import at.techbee.jtx.contract.JtxContract.JtxICalObject.TZ_ALLDAY
import at.techbee.jtx.ui.reusable.elements.TimezoneAutocompleteTextfield
import at.techbee.jtx.util.DateTimeUtils
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.contains


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DatePickerDialog(
    datetime: Long?,
    timezone: String?,
    allowNull: Boolean,
    dateOnly: Boolean = false,
    enforceTime: Boolean = false,
    minDate: Long? = null,
    maxDate: Long? = null,
    onConfirm: (newDateTime: Long?, newTimezone: String?) -> Unit,
    onDismiss: () -> Unit
) {

    val tabIndexDate = 0
    val tabIndexTime = 1
    val tabIndexTimezone = 2
    var selectedTab by remember { mutableStateOf(0) }

    var newDateTime by rememberSaveable {
        mutableStateOf(
            datetime?.let {
                ZonedDateTime.ofInstant(
                    Instant.ofEpochMilli(it), DateTimeUtils.requireTzId(timezone)
                )
            }
        )
    }
    var newTimezone by rememberSaveable {
        mutableStateOf(
            if(enforceTime)
                null
            else
                timezone
        )
    }
    val defaultTimezone =
        if (LocalInspectionMode.current) "Europe/Vienna" else TimeZone.getDefault().id
    val defaultDateTime = ZonedDateTime.now()

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),   // Workaround due to Google Issue: https://issuetracker.google.com/issues/194911971?pli=1
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.edit_datepicker_dialog_select_date)) },
        text = {

            Column {

                if (!dateOnly || allowNull) {    // show the tabs if not date only

                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(selected = selectedTab == tabIndexDate,
                            onClick = { selectedTab = tabIndexDate },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    if (allowNull) {
                                        Checkbox(
                                            checked = newDateTime != null,
                                            onCheckedChange = {
                                                newDateTime = if (it) defaultDateTime else null
                                                newTimezone = if (it && !enforceTime) TZ_ALLDAY else null
                                                selectedTab = tabIndexDate
                                            },
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Icon(
                                        Icons.Outlined.Today,
                                        stringResource(id = R.string.date),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
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
                                        checked = (newDateTime != null && newTimezone != TZ_ALLDAY) || enforceTime,
                                        enabled = newDateTime != null && !dateOnly,
                                        onCheckedChange = {
                                            newTimezone = if (it) null else TZ_ALLDAY
                                            selectedTab = if (it || enforceTime) tabIndexTime else tabIndexDate
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
                                        enabled = newTimezone != TZ_ALLDAY && !dateOnly,
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


                AnimatedVisibility(selectedTab == tabIndexDate && newDateTime != null) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        AndroidView(
                            //modifier = Modifier.fillMaxSize(), // Occupy the max size in the Compose UI tree
                            factory = { context ->
                                // Creates custom view
                                val datepicker = DatePicker(context)
                                newDateTime?.let { dateTime ->
                                    datepicker.init(
                                        dateTime.year,
                                        dateTime.monthValue - 1,
                                        dateTime.dayOfMonth
                                    ) { _, year, monthOfYear, dayOfMonth ->
                                        newDateTime =
                                            dateTime.withYear(year).withMonth(monthOfYear + 1)
                                                .withDayOfMonth(dayOfMonth)
                                    }
                                    datepicker.updateDate(
                                        dateTime.year,
                                        dateTime.monthValue - 1,
                                        dateTime.dayOfMonth
                                    )
                                    minDate?.let { datepicker.minDate = it }
                                    maxDate?.let { datepicker.maxDate = it }
                                }
                                datepicker.rootView
                            }
                        )
                    }
                }
                AnimatedVisibility(selectedTab == tabIndexDate && newDateTime == null) {
                    Text(
                        stringResource(id = R.string.not_set2),
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    )
                }

                AnimatedVisibility(selectedTab == tabIndexTime) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        newDateTime?.let { dateTime ->
                            AndroidView(
                                //modifier = Modifier.fillMaxSize(), // Occupy the max size in the Compose UI tree
                                factory = { context ->
                                    // Creates custom view
                                    val timepicker = TimePicker(context)
                                    timepicker.setIs24HourView(DateFormat.is24HourFormat(context))
                                    @Suppress("DEPRECATION")
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        timepicker.hour = dateTime.hour
                                        timepicker.minute = dateTime.minute
                                    } else {
                                        timepicker.currentHour = dateTime.hour
                                        timepicker.currentMinute = dateTime.minute
                                    }

                                    timepicker.setOnTimeChangedListener { _, hour, minute ->
                                        newDateTime = dateTime.withHour(hour).withMinute(minute)
                                    }
                                    timepicker.rootView
                                }
                            )
                        }
                    }
                }
                AnimatedVisibility(selectedTab == tabIndexTimezone) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        TimezoneAutocompleteTextfield(
                            timezone = newTimezone,
                            onTimezoneChanged = { tz -> newTimezone = tz }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newTimezone != null && newTimezone != TZ_ALLDAY && !TimeZone.getAvailableIDs()
                            .contains(newTimezone)
                    ) {
                        selectedTab = tabIndexTimezone
                        return@TextButton
                    }

                    if(enforceTime && newTimezone == TZ_ALLDAY)
                        newTimezone = null

                    newDateTime?.let { dateTime ->
                        when (newTimezone) {
                            TZ_ALLDAY -> newDateTime = dateTime
                                .withHour(0)
                                .withMinute(0)
                                .withSecond(0)
                                .withNano(0)
                                .withZoneSameLocal(ZoneId.of("UTC"))
                            in TimeZone.getAvailableIDs() -> newDateTime =
                                dateTime.withZoneSameLocal(ZoneId.of(newTimezone)).withNano(0).withSecond(0)
                            null -> newDateTime = dateTime.withZoneSameLocal(ZoneId.systemDefault()).withNano(0).withSecond(0)
                        }
                    }

                    onConfirm(newDateTime?.toInstant()?.toEpochMilli(), newTimezone)
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
fun DatePickerDialog_Preview_not_null() {
    MaterialTheme {

        DatePickerDialog(
            datetime = 1660500481224,   // 14 Aug 2022  18:09
            timezone = TZ_ALLDAY,
            allowNull = false,
            onConfirm = { _, _ -> },
            onDismiss = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DatePickerDialog_Preview_allowNull() {
    MaterialTheme {

        DatePickerDialog(
            datetime = 1660500481224,   // 14 Aug 2022  18:09
            timezone = TZ_ALLDAY,
            allowNull = true,
            onConfirm = { _, _ -> },
            onDismiss = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DatePickerDialog_Preview_allowNull_null() {
    MaterialTheme {

        DatePickerDialog(
            datetime = null,
            timezone = null,
            allowNull = true,
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
            allowNull = false,
            onConfirm = { _, _ -> },
            onDismiss = { }
        )
    }
}
