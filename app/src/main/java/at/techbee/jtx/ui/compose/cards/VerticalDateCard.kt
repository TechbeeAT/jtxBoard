/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.ui.compose.dialogs.DatePickerDialog
import at.techbee.jtx.ui.compose.elements.VerticalDateBlock


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalDateCard(
    datetime: Long?,
    timezone: String?,
    isEditMode: MutableState<Boolean>,
    allowNull: Boolean,
    modifier: Modifier = Modifier,
    labelTop: String? = null,
    onDateTimeChanged: (Long?, String?) -> Unit = { _, _ -> }
    ) {

    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    var newDateTime by rememberSaveable { mutableStateOf(datetime)}
    var newTimezone by rememberSaveable { mutableStateOf(timezone) }

    if(isEditMode.value) {
        OutlinedCard(
            onClick = { showDatePickerDialog = true },
            modifier = modifier
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VerticalDateBlock(
                    datetime = newDateTime,
                    timezone = newTimezone,
                    modifier = Modifier.padding(4.dp),
                    labelTop = labelTop,
                )
            }
        }
    } else {
        ElevatedCard(
            modifier = modifier
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VerticalDateBlock(
                    datetime = newDateTime,
                    timezone = newTimezone,
                    modifier = Modifier.padding(4.dp),
                    labelTop = labelTop
                )
            }
        }
    }

    if(showDatePickerDialog) {
        DatePickerDialog(
            datetime = newDateTime,
            timezone = newTimezone,
            allowNull = allowNull,
            onConfirm = { time, tz ->
                newDateTime = time
                newTimezone = tz
                onDateTimeChanged(newDateTime, newTimezone)
                        },
            onDismiss = { showDatePickerDialog = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VerticalDateCard_Preview_Allday() {
    MaterialTheme {
        VerticalDateCard(
            datetime = System.currentTimeMillis(),
            timezone = ICalObject.TZ_ALLDAY,
            allowNull = true,
            isEditMode = remember { mutableStateOf(false) },
            onDateTimeChanged = { _, _, -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VerticalDateCard_Preview_Allday_edit() {
    MaterialTheme {
        VerticalDateCard(
            datetime = System.currentTimeMillis(),
            timezone = ICalObject.TZ_ALLDAY,
            allowNull = true,
            isEditMode = remember { mutableStateOf(true) },
            onDateTimeChanged = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VerticalDateCard_Preview_WithTime() {
    MaterialTheme {
        VerticalDateCard(
            datetime = System.currentTimeMillis(),
            timezone = null,
            isEditMode = remember { mutableStateOf(false) },
            allowNull = true,
            onDateTimeChanged = { _, _ -> },
            labelTop = stringResource(id = R.string.completed)

        )
    }
}

@Preview(showBackground = true)
@Composable
fun VerticalDateCard_Preview_WithTimezone() {
    MaterialTheme {
        VerticalDateCard(
            datetime = System.currentTimeMillis(),
            timezone = "Europe/Vienna",
            isEditMode = remember { mutableStateOf(false) },
            allowNull = true,
            onDateTimeChanged = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VerticalDateCard_Preview_WithTimezone2() {
    MaterialTheme {
        VerticalDateCard(
            datetime = System.currentTimeMillis(),
            timezone = "Africa/Addis_Ababa",
            isEditMode = remember { mutableStateOf(false) },
            allowNull = true,
            onDateTimeChanged = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VerticalDateCard_Preview_NotSet() {
    MaterialTheme {
        VerticalDateCard(
            datetime = null,
            timezone = null,
            isEditMode = remember { mutableStateOf(false) },
            allowNull = true,
            onDateTimeChanged = { _, _ -> },
            labelTop = stringResource(id = R.string.due)
        )
    }
}
