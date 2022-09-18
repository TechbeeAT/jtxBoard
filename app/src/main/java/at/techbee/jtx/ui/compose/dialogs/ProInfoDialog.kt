/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R


@Composable
fun ProInfoDialog(
    onOK: () -> Unit,
) {

    AlertDialog(
        onDismissRequest = { onOK() },
        title = { Text(stringResource(id = R.string.buypro_initial_dialog_title)) },
        text = { Text(stringResource(id = R.string.buypro_initial_dialog_message)) },
        confirmButton = {
            TextButton(
                onClick = { onOK() }
            ) {
                Text(stringResource(id = R.string.ok))
            }
        },
    )

 }

@Preview(showBackground = true)
@Composable
fun ProInfoDialog_Preview() {
    MaterialTheme {
        ProInfoDialog(
            onOK = { },
        )
    }
}

