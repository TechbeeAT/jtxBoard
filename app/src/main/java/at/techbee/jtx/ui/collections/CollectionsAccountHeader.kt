/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.collections

import android.accounts.Account
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R
import at.techbee.jtx.ui.reusable.dialogs.CollectionsAccountDeleteDialog
import at.techbee.jtx.util.SyncApp


@Composable
fun CollectionsAccountHeader(
    account: Account,
    isFoundInAccountmanager: Boolean,
    onDeleteAccount: (Account) -> Unit,
    modifier: Modifier = Modifier
) {

    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    if (showDeleteAccountDialog)
        CollectionsAccountDeleteDialog(
            account = account,
            onDeleteAccount = onDeleteAccount,
            onDismiss = { showDeleteAccountDialog = false }
        )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                account.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            if (!isFoundInAccountmanager) {
                Text(
                    stringResource(id = R.string.collections_account_not_found_info),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        if (!isFoundInAccountmanager)
            IconButton(onClick = { showDeleteAccountDialog = true }) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = stringResource(id = R.string.delete)
                )
            }
    }
}


@Preview(showBackground = true)
@Composable
fun CollectionsAccountHeader_Preview() {
    MaterialTheme {

        CollectionsAccountHeader(
            Account("Test Account Name", SyncApp.DAVX5.accountType),
            isFoundInAccountmanager = true,
            onDeleteAccount = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CollectionsAccountHeader_Preview2() {
    MaterialTheme {

        CollectionsAccountHeader(
            Account("Test Account Name", SyncApp.DAVX5.accountType),
            false,
            onDeleteAccount = { }
        )
    }
}

