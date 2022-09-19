/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R


@Composable
fun RequestPermissionDialog(
    text: String,
    onConfirm: () -> Unit
) {

    AlertDialog(
        onDismissRequest = { },
        title = { Text(stringResource(id = R.string.view_fragment_audio_permission)) },
        text = { Text(text) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                }
            ) {
                Text(stringResource(id = R.string.ok))
            }
        },
    )

 }

@Preview(showBackground = true)
@Composable
fun RequestPermissionDialog_Audio_Preview() {
    MaterialTheme {
        RequestPermissionDialog(
            text = stringResource(id = R.string.view_fragment_audio_permission_message),
            onConfirm = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RequestPermissionDialog_CoarseLocation_Preview() {
    MaterialTheme {
        RequestPermissionDialog(
            text = stringResource(id = R.string.edit_fragment_app_coarse_location_permission_message),
            onConfirm = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RequestPermissionDialog_Contacts_Preview() {
    MaterialTheme {
        RequestPermissionDialog(
            text = stringResource(id = R.string.edit_fragment_app_permission_message),
            onConfirm = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RequestPermissionDialog_Notifications_Preview() {
    MaterialTheme {
        RequestPermissionDialog(
            text = stringResource(id = R.string.edit_fragment_app_notification_permission_message),
            onConfirm = { },
        )
    }
}
