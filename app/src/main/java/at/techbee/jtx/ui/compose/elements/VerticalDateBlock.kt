/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.ui.theme.Typography
import at.techbee.jtx.util.DateTimeUtils
import java.util.*


@Composable
fun VerticalDateBlock(datetime: Long, timezone: String?, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = DateTimeUtils.convertLongToDayString(datetime, timezone),
            style = Typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            DateTimeUtils.convertLongToMonthString(datetime, timezone),
            style = Typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            DateTimeUtils.convertLongToYearString(datetime, timezone),
            style = Typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (timezone != ICalObject.TZ_ALLDAY)
            Text(
                DateTimeUtils.convertLongToTimeString(datetime, timezone),
                style = Typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        if (timezone != ICalObject.TZ_ALLDAY && timezone?.isNotEmpty() == true && TimeZone.getTimeZone(timezone).getDisplayName(true, TimeZone.SHORT) != null)
            Text(
                TimeZone.getTimeZone(timezone).getDisplayName(true, TimeZone.SHORT),
                style = Typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
    }
}

@Preview(showBackground = true)
@Composable
fun DateBlock_Preview_Allday() {
    JtxBoardTheme {
        VerticalDateBlock(System.currentTimeMillis(), ICalObject.TZ_ALLDAY)
    }
}

@Preview(showBackground = true)
@Composable
fun DateBlock_Preview_WithTime() {
    JtxBoardTheme {
        VerticalDateBlock(System.currentTimeMillis(), null)
    }
}

@Preview(showBackground = true)
@Composable
fun DateBlock_Preview_WithTimezone() {
    JtxBoardTheme {
        VerticalDateBlock(System.currentTimeMillis(), "Europe/Vienna")
    }
}

@Preview(showBackground = true)
@Composable
fun DateBlock_Preview_WithTimezone2() {
    JtxBoardTheme {
        VerticalDateBlock(System.currentTimeMillis(), "Africa/Addis_Ababa")
    }
}
