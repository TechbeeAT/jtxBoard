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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R
import at.techbee.jtx.util.SyncApp


@Composable
fun SyncAppIncompatibleDialog(
    incompatibleSyncApps: List<SyncApp>,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.dialog_sync_app_outdated_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                incompatibleSyncApps.forEach { incompatibleSyncApp ->
                    Text(stringResource(id = R.string.dialog_sync_app_outdated_message, incompatibleSyncApp.appName, incompatibleSyncApp.minVersionName))
                }

            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text(stringResource(id = R.string.close))
            }
        }
    )
 }

@Preview(showBackground = true)
@Composable
fun DAVx5IncompatibleDialog_Preview1() {
    MaterialTheme {
        SyncAppIncompatibleDialog(
            listOf(SyncApp.DAVX5),
            onDismiss = { },
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DAVx5IncompatibleDialog_Preview2() {
    MaterialTheme {
        SyncAppIncompatibleDialog(
            SyncApp.values().toList(),
            onDismiss = { },
        )
    }
}
