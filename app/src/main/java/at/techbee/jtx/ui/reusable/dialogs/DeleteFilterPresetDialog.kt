package at.techbee.jtx.ui.reusable.dialogs

/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.locals.StoredListSetting
import at.techbee.jtx.database.locals.StoredListSettingData


@Composable
fun DeleteFilterPresetDialog(
    storedListSetting: StoredListSetting,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.edit_dialog_sure_to_delete_title, storedListSetting.name))
        },
        text = {
                Text(stringResource(id = R.string.edit_dialog_sure_to_delete_message, storedListSetting.name),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
               },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
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
                Text( stringResource(id = R.string.cancel))
            }
        }
    )

}

@Preview(showBackground = true)
@Composable
fun DeleteFilterPresetDialog_Preview() {
    MaterialTheme {
        DeleteFilterPresetDialog(
            storedListSetting = StoredListSetting(module = Module.TODO, name = "my setting", storedListSettingData = StoredListSettingData()),
            onConfirm = { },
            onDismiss = { }
        )
    }
}
