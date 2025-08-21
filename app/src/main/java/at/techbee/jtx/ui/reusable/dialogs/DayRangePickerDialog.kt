/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import at.techbee.jtx.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayRangePickerDialog(
    dayRangeStart: Int,
    dayRangeEnd: Int,
    onConfirm: (newDayRangeStart: Int, newDayRangeEnd: Int) -> Unit,
    onDismiss: () -> Unit
) {

    var newDayRangeStart by rememberSaveable { mutableIntStateOf(dayRangeStart) }
    var newDayRangeEnd by rememberSaveable { mutableIntStateOf(dayRangeEnd) }

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),   // Workaround due to Google Issue: https://issuetracker.google.com/issues/194911971?pli=1
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.edit_day_range_picker_dialog_select_day_range)) },
        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.edit_day_range_picker_dialog_DATE_WITHIN))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = {
                            if(newDayRangeStart < newDayRangeEnd || (newDayRangeStart == 0 && newDayRangeEnd == 0))
                                newDayRangeStart-=1
                        },
                        enabled = newDayRangeStart-1 < newDayRangeEnd
                    ) {
                        Icon(Icons.Outlined.ChevronLeft, stringResource(R.string.filter_options_less_entries))
                    }

                    Text(newDayRangeStart.toString())

                    IconButton(
                        onClick = {
                            if(newDayRangeStart < newDayRangeEnd || (newDayRangeStart == 0 && newDayRangeEnd == 0))
                                newDayRangeStart+=1
                        },
                        enabled = newDayRangeStart+1 < newDayRangeEnd
                    ) {
                        Icon(Icons.Outlined.ChevronRight, stringResource(R.string.filter_options_less_entries))
                    }
                }

                Text(stringResource(R.string.edit_day_range_picker_dialog_date_within_x_AND_x))


                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = {
                            if(newDayRangeStart < newDayRangeEnd || (newDayRangeStart == 0 && newDayRangeEnd == 0))
                                newDayRangeEnd-=1
                        },
                        enabled = newDayRangeEnd-1 > newDayRangeStart
                    ) {
                        Icon(Icons.Outlined.ChevronLeft, stringResource(R.string.filter_options_less_entries))
                    }

                    Text(newDayRangeEnd.toString())

                    IconButton(
                        onClick = {
                            if(newDayRangeStart < newDayRangeEnd || (newDayRangeStart == 0 && newDayRangeEnd == 0))
                                newDayRangeEnd+=1
                        },
                        enabled = newDayRangeEnd+1 > newDayRangeStart
                    ) {
                        Icon(Icons.Outlined.ChevronRight, stringResource(R.string.filter_options_less_entries))
                    }
                }

                Text(stringResource(R.string.edit_day_range_picker_dialog_date_within_x_and_x_DAYS))


            }
        },
        confirmButton = {

            if(dayRangeStart != 0 || dayRangeEnd != 0) {
                TextButton(onClick = {
                    onConfirm(0, 0)
                    onDismiss()
                }) {
                    Text(stringResource(id = R.string.clear_selection))
                }
            }

            TextButton(
                enabled = newDayRangeEnd > newDayRangeStart,
                onClick = {
                    onConfirm(newDayRangeStart, newDayRangeEnd)
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
fun DayRangePickerDialog_Preview() {
    MaterialTheme {

        DayRangePickerDialog(
            dayRangeStart = 0,
            dayRangeEnd = 0,
            onConfirm = { _, _ -> },
            onDismiss = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DayRangePickerDialog_Preview_preselected() {
    MaterialTheme {

        DayRangePickerDialog(
            dayRangeStart = -2,
            dayRangeEnd = 7,
            onConfirm = { _, _ -> },
            onDismiss = { }
        )
    }
}


