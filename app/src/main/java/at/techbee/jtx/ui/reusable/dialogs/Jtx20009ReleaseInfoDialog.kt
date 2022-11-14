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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R
import com.arnyminerz.markdowntext.MarkdownText


@Composable
fun Jtx20009ReleaseInfoDialog(
    onOK: () -> Unit,
) {

    AlertDialog(
        onDismissRequest = { onOK() },
        title = { Text("jtx Board 2.0.9") },
        text = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                MarkdownText("Dear fellow jtx Board users," + System.lineSeparator() + System.lineSeparator() +
                        "your jtx Board has been updated to version 2.0.9!" + System.lineSeparator() + System.lineSeparator() +
                        "In this version settings have been moved to other places:" + System.lineSeparator() +
                        "- The new flat view option in the list replaces the setting to show subtasks of journals and notes in tasklist" + System.lineSeparator() +
                        "- Markdown can now be enabled/disabled within the detail view of an entry" + System.lineSeparator() +
                        "- Autosave can now be enabled/disabled within the edit view of an entry" + System.lineSeparator() +
                        "- The setting to show only one recurring entry in the future is now available in the list view as \"limit recurring\"" + System.lineSeparator() + System.lineSeparator() +
                        "**IMPORTANT: Due to the changes in the settings the changed options might have been restored to default values.**" + System.lineSeparator() + System.lineSeparator() +
                        "By the way the widget is almost ready ;-)" + System.lineSeparator() +
                        "- Patrick")
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
fun Jtx20009ReleaseInfoDialog_Preview() {
    MaterialTheme {
        Jtx20009ReleaseInfoDialog(
            onOK = { },
        )
    }
}
