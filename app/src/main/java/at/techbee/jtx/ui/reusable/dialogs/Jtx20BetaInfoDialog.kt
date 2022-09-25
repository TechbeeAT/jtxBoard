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
fun Jtx20BetaInfoDialog(
    onOK: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onOK() },
        title = { Text("jtx Board 2.0 Beta") },
        text = { Text("Dear fellow jtx Board users,\nI have been working on a new version of jtx Board that is completely refactored in the modern UI Toolkit Jetpack Compose :)\nIf you encounter any error or if you have any feedback, please feel free to send a request through the website or directly in Gitlab!\nI hope you enjoy the renewed detail view! In the upcoming weeks I will focus on new features like a widget. So stay tuned!\n- Patrick") },
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
fun Jtx20BetaInfoDialog_Preview() {
    MaterialTheme {
        Jtx20BetaInfoDialog(
            onOK = { },
        )
    }
}

