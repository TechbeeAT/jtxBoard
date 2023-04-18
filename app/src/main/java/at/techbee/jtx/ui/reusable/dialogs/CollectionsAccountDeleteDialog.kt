/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import android.accounts.Account
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R
import at.techbee.jtx.util.SyncApp


@Composable
fun CollectionsAccountDeleteDialog(
    account: Account,
    onDeleteAccount: (Account) -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                stringResource(
                    R.string.collections_account_delete_dialog_title,
                    account.name
                )
            )
        },
        text = { Text(stringResource(R.string.collections_account_delete_dialog_message)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDeleteAccount(account)
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.delete))
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
fun AccountDeleteDialog_preview() {
    MaterialTheme {

        CollectionsAccountDeleteDialog(
            Account("Test Account Name", SyncApp.DAVX5.accountType),
            onDeleteAccount = { },
            onDismiss = { }
        )
    }
}


