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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.ui.reusable.dialogs.DatePickerDialog
import at.techbee.jtx.util.DateTimeUtils


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
    pickerMinDate: Long? = null,
    pickerMaxDate: Long? = null,
    onDateTimeChanged: (Long?, String?) -> Unit = { _, _ -> }
) {

    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }

    if (isEditMode) {
        OutlinedCard(
            onClick = { showDatePickerDialog = true },
            modifier = modifier
        ) {

            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)) {

                labelTop?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                if (datetime != null) {
                    Text(
                        DateTimeUtils.convertLongToFullDateTimeString(
                            datetime,
                            timezone
                        )
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.not_set2),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    } else {
        ElevatedCard(
            modifier = modifier
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                labelTop?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontStyle = FontStyle.Italic
                    )
                }

                if (datetime != null) {
                    Text(
                        DateTimeUtils.convertLongToFullDateTimeString(
                            datetime,
                            timezone
                        ),
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.not_set2)
                    )
                }
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
            isEditMode = true,
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
            isEditMode = false,
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
            isEditMode = false,
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
            isEditMode = false,
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
            isEditMode = false,
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
            isEditMode = true,
            allowNull = true,
            dateOnly = false,
            onDateTimeChanged = { _, _ -> },
            labelTop = stringResource(id = R.string.due)
        )
    }
}
