/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R


@Composable
fun Jtx20ReleaseInfoDialog(
    onOK: () -> Unit,
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { onOK() },
        title = { Text("jtx Board 2.0") },
        text = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Dear fellow jtx Board users,\nI have been working on a new version of jtx Board that is completely refactored in the modern UI Toolkit Jetpack Compose.\nFor more information about this release, please follow the link below.\nI hope you enjoy the changes and the renewed detail view! In the upcoming weeks I will focus on new features like a widget. So stay tuned!\n- Patrick")
                TextButton(onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://jtx.techbee.at/category/news")
                        )
                    )
                }) {
                    Text("https://jtx.techbee.at/category/news")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onOK() }
            ) {
                Text(stringResource(id = R.string.close))
            }
        },
    )
 }

@Preview(showBackground = true)
@Composable
fun Jtx20BetaInfoDialog_Preview() {
    MaterialTheme {
        Jtx20ReleaseInfoDialog(
            onOK = { },
        )
    }
}

