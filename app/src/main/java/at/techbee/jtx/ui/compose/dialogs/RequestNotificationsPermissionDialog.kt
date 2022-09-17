/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.dialogs

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R


@Composable
fun RequestNotificationsPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    val readContactsGrantedText = stringResource(id = R.string.permission_read_contacts_granted)
    val readContactsDeniedText = stringResource(id = R.string.permission_read_contacts_denied)

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, readContactsGrantedText, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, readContactsDeniedText, Toast.LENGTH_LONG).show()
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.edit_fragment_app_permission)) },
        text = { Text(stringResource(id = R.string.edit_fragment_app_notification_permission_message)) },
        confirmButton = {
            TextButton(
                onClick = {
                    launcher.launch(Manifest.permission.READ_CONTACTS)
                    onConfirm()
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
                Text( stringResource(id = R.string.cancel))
            }
        }
    )

 }

@Preview(showBackground = true)
@Composable
fun RequestNotificationsPermissionDialog_Preview() {
    MaterialTheme {

        RequestNotificationsPermissionDialog(
            onConfirm = { },
            onDismiss = { }
        )
    }
}

