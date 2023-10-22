/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.contract.JtxContract.JtxICalObject.TZ_ALLDAY
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.ui.settings.DropdownSettingOption
import at.techbee.jtx.ui.theme.Typography
import at.techbee.jtx.util.DateTimeUtils
import java.util.TimeZone


@Composable
fun VerticalDateBlock(
    datetime: Long?,
    timezone: String?,
    settingDisplayTimezone: DropdownSettingOption,
    modifier: Modifier = Modifier,
    labelTop: String? = null
) {

    val timezone2show =
        if(timezone == null || timezone == TZ_ALLDAY || settingDisplayTimezone == DropdownSettingOption.DISPLAY_TIMEZONE_ORIGINAL)
            timezone
        else
            null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        labelTop?.let {
            Text(
                it,
                style = Typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if(datetime != null) {
            Text(
                text = DateTimeUtils.convertLongToDayString(datetime, timezone2show),
                style = Typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = DateTimeUtils.convertLongToWeekdayString(datetime, timezone2show),
                style = Typography.labelSmall,
                textAlign = TextAlign.Center
            )
            Text(
                DateTimeUtils.convertLongToMonthString(datetime, timezone2show),
                style = Typography.labelMedium,
                textAlign = TextAlign.Center
            )

            Text(
                DateTimeUtils.convertLongToYearString(datetime, timezone2show),
                style = Typography.labelSmall,
                textAlign = TextAlign.Center
            )
            if (timezone != ICalObject.TZ_ALLDAY)
                Text(
                    DateTimeUtils.convertLongToShortTimeString(datetime, timezone2show),
                    style = Typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            if (timezone2show != ICalObject.TZ_ALLDAY && timezone2show?.isNotEmpty() == true && TimeZone.getTimeZone(
                    timezone
                ).getDisplayName(true, TimeZone.SHORT) != null
            )
                Text(
                    TimeZone.getTimeZone(timezone2show).getDisplayName(true, TimeZone.SHORT),
                    style = Typography.labelSmall,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
        } else {
            Icon(
                Icons.Outlined.DateRange,
                stringResource(id = R.string.not_set2),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DateBlock_Preview_Allday() {
    MaterialTheme {
        VerticalDateBlock(System.currentTimeMillis(), ICalObject.TZ_ALLDAY, DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL)
    }
}

@Preview(showBackground = true)
@Composable
fun DateBlock_Preview_Allday_with_label() {
    MaterialTheme {
        VerticalDateBlock(
            datetime = System.currentTimeMillis(),
            timezone = ICalObject.TZ_ALLDAY,
            settingDisplayTimezone =  DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL,
            labelTop = stringResource(id = R.string.started))
    }
}

@Preview(showBackground = true)
@Composable
fun DateBlock_Preview_WithTime() {
    MaterialTheme {
        VerticalDateBlock(System.currentTimeMillis(), null, DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL)
    }
}

@Preview(showBackground = true)
@Composable
fun DateBlock_Preview_WithTimezone() {
    MaterialTheme {
        VerticalDateBlock(System.currentTimeMillis(), "Europe/Vienna", DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL)
    }
}

@Preview(showBackground = true)
@Composable
fun DateBlock_Preview_WithTimezone2() {
    MaterialTheme {
        VerticalDateBlock(System.currentTimeMillis(), "Africa/Addis_Ababa", DropdownSettingOption.DISPLAY_TIMEZONE_ORIGINAL)
    }
}

@Preview(showBackground = true)
@Composable
fun DateBlock_Preview_null() {
    MaterialTheme {
        VerticalDateBlock(null, null, DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL)
    }
}

@Preview(showBackground = true)
@Composable
fun DateBlock_Preview_null_withLabel() {
    MaterialTheme {
        VerticalDateBlock(null, null, DropdownSettingOption.DISPLAY_TIMEZONE_LOCAL, labelTop = stringResource(id = R.string.due))
    }
}