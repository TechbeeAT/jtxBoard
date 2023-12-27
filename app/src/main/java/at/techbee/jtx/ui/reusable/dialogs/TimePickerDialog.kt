/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import at.techbee.jtx.R
import java.time.LocalTime
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    time: LocalTime?,
    @StringRes titleRes: Int,
    onConfirm: (LocalTime?) -> Unit,
    onDismiss: () -> Unit
) {

    val timePickerState = rememberTimePickerState(time?.hour?:0, time?.minute?:0)

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),   // Workaround due to Google Issue: https://issuetracker.google.com/issues/194911971?pli=1
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = titleRes)) },
        text = {

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {

            Row {
                TextButton(
                    onClick = {
                        onConfirm(null)
                        onDismiss()
                    }
                ) {
                    Text(stringResource(id = R.string.setting_no_time))
                }
                TextButton(
                    onClick = {
                        onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
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
fun TimePickerDialog_Preview() {
    MaterialTheme {

        TimePickerDialog(
            time = null,
            titleRes = R.string.settings_default_start_time,
            onConfirm = { },
            onDismiss = { }
        )
    }
}

