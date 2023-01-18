/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddLink
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.util.UiUtil


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAttachmentLinkDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {

    var currentText by rememberSaveable { mutableStateOf("") }
    var noValidUrlError by rememberSaveable { mutableStateOf(false) }


    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.edit_attachment_add_link_dialog)) },
        text = {

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.fillMaxWidth()
            ) {

                OutlinedTextField(
                    value = currentText,
                    onValueChange = { newUrl ->
                        currentText = newUrl
                        noValidUrlError = !UiUtil.isValidURL(currentText)
                    },
                    singleLine = true,
                    isError = noValidUrlError,
                    leadingIcon = { Icon(Icons.Outlined.AddLink, null) },
                    placeholder = { Text("https://www.example.com") }
                )

                AnimatedVisibility(noValidUrlError) {
                    Text(
                        text = stringResource(id = R.string.invalid_url_message),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }

            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (currentText.isNotBlank() && UiUtil.isValidURL(currentText)) {
                        onConfirm(currentText)
                        onDismiss()
                    } else {
                        noValidUrlError = true
                    }
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
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AddAttachmentLinkDialog_Preview() {

    MaterialTheme {
        AddAttachmentLinkDialog(
            onConfirm = { },
            onDismiss = { }
        )
    }
}

