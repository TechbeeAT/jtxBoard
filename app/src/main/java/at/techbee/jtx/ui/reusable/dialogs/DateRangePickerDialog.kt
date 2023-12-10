/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import at.techbee.jtx.R
import kotlin.time.Duration.Companion.days


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    dateRangeStart: Long?,
    dateRangeEnd: Long?,
    onConfirm: (newDateRangeStart: Long?, newDateRangeEnd: Long?) -> Unit,
    onDismiss: () -> Unit
) {

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = dateRangeStart,
        initialSelectedEndDateMillis = dateRangeEnd
    )

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),   // Workaround due to Google Issue: https://issuetracker.google.com/issues/194911971?pli=1
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.edit_date_range_picker_dialog_select_date_range)) },
        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier.requiredWidth(360.dp)  // from DatePickerModalTokens.ContainerWidth
                )
            }
        },
        confirmButton = {

            AnimatedVisibility(dateRangePickerState.selectedStartDateMillis != null || dateRangePickerState.selectedEndDateMillis != null) {
                TextButton(onClick = {
                    dateRangePickerState.setSelection(null, null)
                }) {
                    Text(stringResource(id = R.string.clear_selection))
                }
            }

            TextButton(
                //enabled = datePickerState.selectedDateMillis?.let { isValidDate(it) } ?: true,
                onClick = {
                    onConfirm(dateRangePickerState.selectedStartDateMillis, dateRangePickerState.selectedEndDateMillis)
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
fun DateRangePickerDialog_Preview() {
    MaterialTheme {

        DateRangePickerDialog(
            dateRangeStart = null,
            dateRangeEnd = null,
            onConfirm = { _, _ -> },
            onDismiss = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DateRangePickerDialog_Preview_preselected() {
    MaterialTheme {

        DateRangePickerDialog(
            dateRangeStart = System.currentTimeMillis(),
            dateRangeEnd = System.currentTimeMillis() + (3).days.inWholeMilliseconds
            ,
            onConfirm = { _, _ -> },
            onDismiss = { }
        )
    }
}


