/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import at.techbee.jtx.R
import at.techbee.jtx.contract.JtxContract.JtxICalObject.TZ_ALLDAY
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.ui.reusable.dialogs.DatePickerDialog
import at.techbee.jtx.ui.settings.DropdownSetting
import at.techbee.jtx.ui.settings.DropdownSettingOption
import at.techbee.jtx.util.DateTimeUtils
import java.time.ZonedDateTime


@Composable
fun HorizontalDateCard(
    datetime: Long?,
    timezone: String?,
    allowNull: Boolean,
    dateOnly: Boolean,
    isReadOnly: Boolean,
    modifier: Modifier = Modifier,
    labelTop: String? = null,
    pickerMinDate: ZonedDateTime? = null,
    pickerMaxDate: ZonedDateTime? = null,
    onDateTimeChanged: (Long?, String?) -> Unit
) {

    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val settingDisplayTimezone = DropdownSetting.SETTING_DISPLAY_TIMEZONE.getSetting(prefs)


    ElevatedCard(
        onClick = {
            if(!isReadOnly)
                showDatePickerDialog = true
        },
        modifier = modifier
    ) {

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)) {

            labelTop?.let { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            if (datetime != null) {
                if(timezone == TZ_ALLDAY
                    || timezone == null
                    || settingDisplayTimezone == DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL
                    || settingDisplayTimezone == DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL_AND_ORIGINAL
                    ) {
                    Text(
                        DateTimeUtils.convertLongToFullDateTimeString(
                            datetime,
                            when (timezone) {
                                TZ_ALLDAY -> TZ_ALLDAY
                                null -> null
                                else -> null
                            }
                        ),
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold
                    )
                }
                if((timezone != null && timezone != TZ_ALLDAY &&
                    (settingDisplayTimezone == DropdownSettingOption.DISPLAY_TIMEZONE_ORIGINAL
                        || settingDisplayTimezone == DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL_AND_ORIGINAL))
                    ) {
                    Text(
                        DateTimeUtils.convertLongToFullDateTimeString(
                            datetime,
                            timezone
                        ),
                        fontStyle = if (settingDisplayTimezone == DropdownSettingOption.DISPLAY_TIMEZONE_ORIGINAL) FontStyle.Italic else null,
                        fontWeight = if (settingDisplayTimezone == DropdownSettingOption.DISPLAY_TIMEZONE_ORIGINAL) FontWeight.Bold else null
                    )
                }
            } else {
                Text(
                    text = stringResource(id = R.string.not_set2),
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }

    if (showDatePickerDialog) {
        DatePickerDialog(
            datetime = datetime,
            timezone = timezone,
            allowNull = allowNull,
            minDate = pickerMinDate,
            maxDate = pickerMaxDate,
            dateOnly = dateOnly,
            onConfirm = { time, tz ->
                onDateTimeChanged(time, tz)
            },
            onDismiss = { showDatePickerDialog = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HorizontalDateCard_Preview_Allday() {
    MaterialTheme {
        HorizontalDateCard(
            datetime = System.currentTimeMillis(),
            timezone = ICalObject.TZ_ALLDAY,
            allowNull = true,
            isReadOnly = false,
            dateOnly = false,
            onDateTimeChanged = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HorizontalDateCard_Preview_Allday_edit() {
    MaterialTheme {
        HorizontalDateCard(
            datetime = System.currentTimeMillis(),
            timezone = ICalObject.TZ_ALLDAY,
            allowNull = true,
            isReadOnly = true,
            dateOnly = false,
            onDateTimeChanged = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HorizontalDateCard_Preview_WithTime() {
    MaterialTheme {
        HorizontalDateCard(
            datetime = System.currentTimeMillis(),
            timezone = null,
            isReadOnly = false,
            allowNull = true,
            dateOnly = false,
            onDateTimeChanged = { _, _ -> },
            labelTop = stringResource(id = R.string.completed)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HorizontalDateCard_Preview_WithTimezone() {
    MaterialTheme {
        HorizontalDateCard(
            datetime = System.currentTimeMillis(),
            timezone = "Europe/Vienna",
            isReadOnly = false,
            allowNull = true,
            dateOnly = false,
            onDateTimeChanged = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HorizontalDateCard_Preview_WithTimezone2() {
    MaterialTheme {
        HorizontalDateCard(
            datetime = System.currentTimeMillis(),
            timezone = "Africa/Addis_Ababa",
            isReadOnly = false,
            allowNull = true,
            dateOnly = false,
            onDateTimeChanged = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HorizontalDateCard_Preview_SameOffset() {
    MaterialTheme {
        HorizontalDateCard(
            datetime = System.currentTimeMillis(),
            timezone = "Europe/Rome",
            isReadOnly = false,
            allowNull = true,
            dateOnly = false,
            onDateTimeChanged = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HorizontalDateCard_Preview_NotSet() {
    MaterialTheme {
        HorizontalDateCard(
            datetime = null,
            timezone = null,
            isReadOnly = false,
            allowNull = true,
            dateOnly = false,
            onDateTimeChanged = { _, _ -> },
            labelTop = stringResource(id = R.string.due)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun HorizontalDateCard_Preview_edit_NotSet() {
    MaterialTheme {
        HorizontalDateCard(
            datetime = null,
            timezone = null,
            isReadOnly = true,
            allowNull = true,
            dateOnly = false,
            onDateTimeChanged = { _, _ -> },
            labelTop = stringResource(id = R.string.due)
        )
    }
}
