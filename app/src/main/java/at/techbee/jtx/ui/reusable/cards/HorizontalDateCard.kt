/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalDateCard(
    datetime: Long?,
    timezone: String?,
    isEditMode: Boolean,
    allowNull: Boolean,
    dateOnly: Boolean,
    modifier: Modifier = Modifier,
    labelTop: String? = null,
    pickerMinDate: ZonedDateTime? = null,
    pickerMaxDate: ZonedDateTime? = null,
    enabled: Boolean = true,
    onDateTimeChanged: (Long?, String?) -> Unit,
    toggleEditMode: () -> Unit
) {

    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val settingDisplayTimezone = DropdownSetting.SETTING_DISPLAY_TIMEZONE.getSetting(prefs)


    Card(
        onClick = {
            if(!isEditMode)
                toggleEditMode()
            showDatePickerDialog = true
        },
        shape = if(isEditMode) CardDefaults.outlinedShape else CardDefaults.elevatedShape,
        colors = if(isEditMode) CardDefaults.outlinedCardColors() else CardDefaults.elevatedCardColors(),
        elevation = if(isEditMode) CardDefaults.outlinedCardElevation() else CardDefaults.elevatedCardElevation(),
        border = if(isEditMode) CardDefaults.outlinedCardBorder() else BorderStroke(0.dp, Color.Transparent),
        enabled = !isEditMode || enabled,
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
                        fontStyle = if (!isEditMode) FontStyle.Italic else null,
                        fontWeight = if (!isEditMode) FontWeight.Bold else null
                    )
                }
                if((isEditMode && timezone != null && timezone != TZ_ALLDAY)
                    || (timezone != null && timezone != TZ_ALLDAY &&
                    (settingDisplayTimezone == DropdownSettingOption.DISPLAY_TIMEZONE_ORIGINAL
                        || settingDisplayTimezone == DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL_AND_ORIGINAL)
                            )
                    ) {
                    Text(
                        DateTimeUtils.convertLongToFullDateTimeString(
                            datetime,
                            timezone
                        ),
                        fontStyle = if (!isEditMode && settingDisplayTimezone == DropdownSettingOption.DISPLAY_TIMEZONE_ORIGINAL) FontStyle.Italic else null,
                        fontWeight = if (!isEditMode && settingDisplayTimezone == DropdownSettingOption.DISPLAY_TIMEZONE_ORIGINAL) FontWeight.Bold else null
                    )
                }
            } else {
                Text(
                    text = stringResource(id = R.string.not_set2),
                    fontStyle = if(!isEditMode) FontStyle.Italic else null
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
            isEditMode = false,
            dateOnly = false,
            onDateTimeChanged = { _, _ -> },
            toggleEditMode = { }
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
            isEditMode = true,
            dateOnly = false,
            onDateTimeChanged = { _, _ -> },
            toggleEditMode = { }
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
            isEditMode = false,
            allowNull = true,
            dateOnly = false,
            onDateTimeChanged = { _, _ -> },
            labelTop = stringResource(id = R.string.completed),
            toggleEditMode = { }
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
            isEditMode = false,
            allowNull = true,
            dateOnly = false,
            onDateTimeChanged = { _, _ -> },
            toggleEditMode = { }
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
            isEditMode = false,
            allowNull = true,
            dateOnly = false,
            onDateTimeChanged = { _, _ -> },
            toggleEditMode = { }
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
            isEditMode = false,
            allowNull = true,
            dateOnly = false,
            onDateTimeChanged = { _, _ -> },
            toggleEditMode = { }
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
            isEditMode = false,
            allowNull = true,
            dateOnly = false,
            onDateTimeChanged = { _, _ -> },
            labelTop = stringResource(id = R.string.due),
            toggleEditMode = { }
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
            isEditMode = true,
            allowNull = true,
            dateOnly = false,
            onDateTimeChanged = { _, _ -> },
            labelTop = stringResource(id = R.string.due),
            toggleEditMode = { }
        )
    }
}
