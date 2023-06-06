/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    datetime: Long?,
    timezone: String?,
    allowNull: Boolean,
    dateOnly: Boolean = false,
    minDate: ZonedDateTime? = null,
    maxDate: ZonedDateTime? = null,
    onConfirm: (newDateTime: Long?, newTimezone: String?) -> Unit,
    onDismiss: () -> Unit
) {

    val tabIndexDate = 0
    val tabIndexTime = 1
    val tabIndexTimezone = 2
    var selectedTab by remember { mutableStateOf(0) }

    val initialZonedDateTime = datetime
        ?.let { ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), DateTimeUtils.requireTzId(timezone)) }
        ?: minDate

    val datePickerState = rememberDatePickerState(initialZonedDateTime?.toInstant()?.toEpochMilli()?.plus(initialZonedDateTime.offset.totalSeconds*1000))
    val timePickerState = rememberTimePickerState(initialZonedDateTime?.hour?:0, initialZonedDateTime?.minute?:0)

    var newTimezone by rememberSaveable { mutableStateOf(timezone) }
    val defaultTimezone = if (LocalInspectionMode.current) "Europe/Vienna" else TimeZone.getDefault().id
    val defaultDateTime = ZonedDateTime.now()

    fun isValidDate(date: Long): Boolean {
        val zonedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), DateTimeUtils.requireTzId(timezone))
        return if(timezone == TZ_ALLDAY)
            zonedDate.year >= (minDate?.year ?:-1) && zonedDate.monthValue >= (minDate?.monthValue ?:-1) && zonedDate.dayOfMonth > (minDate?.dayOfMonth ?:-1)
                    && zonedDate.year <= (maxDate?.year ?:3000) && zonedDate.monthValue <= (maxDate?.monthValue ?:13) && zonedDate.dayOfMonth < (maxDate?.dayOfMonth ?: 367)
        else
            zonedDate.year >= (minDate?.year ?:-1) && zonedDate.monthValue >= (minDate?.monthValue ?:-1) && zonedDate.dayOfMonth >= (minDate?.dayOfMonth ?:-1)
                    && zonedDate.year <= (maxDate?.year ?:3000) && zonedDate.monthValue <= (maxDate?.monthValue ?:13) && zonedDate.dayOfMonth <= (maxDate?.dayOfMonth ?: 367)
    }

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),   // Workaround due to Google Issue: https://issuetracker.google.com/issues/194911971?pli=1
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.edit_datepicker_dialog_select_date)) },
        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

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
                                            checked = datePickerState.selectedDateMillis != null,
                                            onCheckedChange = {
                                                datePickerState.setSelection(if (it) defaultDateTime.withZoneSameLocal(ZoneId.of("UTC")).toInstant().toEpochMilli() else null)
                                                newTimezone = if (it) TZ_ALLDAY else null
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
                            }
                        )

                        Tab(selected = selectedTab == tabIndexTime,
                            onClick = { selectedTab = tabIndexTime },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Checkbox(
                                        checked = newTimezone != TZ_ALLDAY,
                                        enabled = datePickerState.selectedDateMillis != null && !dateOnly,
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
                            }
                        )

                        Tab(selected = selectedTab == tabIndexTimezone,
                            onClick = { selectedTab = tabIndexTimezone },
                            enabled = datePickerState.selectedDateMillis != null && newTimezone != TZ_ALLDAY && newTimezone != null,
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
                            }
                        )
                    }
                }

                AnimatedVisibility(selectedTab == tabIndexDate && datePickerState.selectedDateMillis == null) {
                    Text(
                        stringResource(id = R.string.not_set2),
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    )
                }

                AnimatedVisibility(selectedTab == tabIndexDate && datePickerState.selectedDateMillis != null) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        DatePicker(
                            state = datePickerState,
                            dateValidator = { date -> isValidDate(date) },
                            modifier = Modifier.requiredWidth(360.dp)  // from DatePickerModalTokens.ContainerWidth
                        )
                    }
                }

                AnimatedVisibility(selectedTab == tabIndexTime) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        TimePicker(state = timePickerState)
                    }
                }
                AnimatedVisibility(selectedTab == tabIndexTimezone) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        TimezoneAutocompleteTextfield(
                            timezone = newTimezone,
                            onTimezoneChanged = { tz -> newTimezone = tz }
                        )
                    }
                }
            }
        },
        confirmButton = {

            Row {

                AnimatedVisibility((!dateOnly || allowNull) && selectedTab == tabIndexDate) {
                    TextButton(
                        onClick = {
                            selectedTab = tabIndexTime
                            if(newTimezone == TZ_ALLDAY)
                                newTimezone = null
                        },
                        enabled = datePickerState.selectedDateMillis != null && !dateOnly
                    ) {
                        Text(stringResource(id = if(newTimezone == TZ_ALLDAY) R.string.edit_datepicker_dialog_add_time else R.string.edit_datepicker_dialog_edit_time))
                    }
                }

                AnimatedVisibility(selectedTab == tabIndexTime) {
                    TextButton(
                        onClick = {
                            selectedTab = tabIndexTimezone
                            if(newTimezone == null)
                                newTimezone = defaultTimezone
                                  },
                        enabled = datePickerState.selectedDateMillis != null
                    ) {
                        Text(stringResource(id = if(newTimezone == null) R.string.edit_datepicker_dialog_add_timezone else R.string.edit_datepicker_dialog_edit_timezone))
                    }
                }


                TextButton(
                    enabled = datePickerState.selectedDateMillis?.let { isValidDate(it) } ?: true,
                    onClick = {
                        if (newTimezone != null && newTimezone != TZ_ALLDAY && !TimeZone.getAvailableIDs()
                                .contains(newTimezone)
                        ) {
                            selectedTab = tabIndexTimezone
                            return@TextButton
                        }

                        val newZonedDateTime = datePickerState.selectedDateMillis?.let { dateTime ->
                            when (newTimezone) {
                                TZ_ALLDAY -> Instant.ofEpochMilli(dateTime).atZone(ZoneId.of("UTC")).withHour(0).withMinute(0).withSecond(0).withNano(0)
                                in TimeZone.getAvailableIDs() -> Instant.ofEpochMilli(dateTime).atZone(ZoneId.of(newTimezone)).withHour(timePickerState.hour).withMinute(timePickerState.minute).withSecond(0).withNano(0)
                                null -> Instant.ofEpochMilli(dateTime).atZone(ZoneId.systemDefault()).withHour(timePickerState.hour).withMinute(timePickerState.minute).withNano(0).withSecond(0)
                                else -> null
                            }
                        }

                        onConfirm(newZonedDateTime?.toInstant()?.toEpochMilli(), newTimezone)
                        onDismiss()
                    }
                ) {
                    Text(stringResource(id = R.string.ok))
                }
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
