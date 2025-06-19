package at.techbee.jtx.ui.reusable.dialogs

/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection


@Composable
fun DeleteObsoleteCollectionsDialog(
    collections: List<ICalCollection>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.dialog_delete_obsolete_collections_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.dialog_delete_obsolete_collections_message),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = collections.joinToString(separator = System.lineSeparator()) { "${it.displayName ?: ""} (${it.accountName})" },
                    fontStyle = FontStyle.Italic
                )
            }
               },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
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
                Text( stringResource(id = R.string.cancel))
            }
        }
    )

}

@Preview(showBackground = true)
@Composable
fun DeleteObsoleteCollectionsDialog_Preview() {
    MaterialTheme {

        DeleteObsoleteCollectionsDialog(
            collections = listOf(
                ICalCollection(
                    displayName = "Collection 1",
                    accountName = "Account 1"),
                ICalCollection(
                    displayName = "Collection 2",
                    accountName = "Account 2"
                )
            ),
            onConfirm = { },
            onDismiss = { }
        )
    }
}
