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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Comment
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
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Comment


@Composable
fun EditCommentDialog(
    comment: Comment,
    onConfirm: (Comment) -> Unit,
    onDismiss: () -> Unit
) {

    var currentText by rememberSaveable { mutableStateOf(comment.text) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.edit_comment)) },
        text = {

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.fillMaxWidth()
            ) {

                OutlinedTextField(
                    value = currentText,
                    isError = currentText.isBlank(),
                    onValueChange = { newText ->
                        currentText = newText
                    },
                    maxLines = 4,
                    leadingIcon = { Icon(Icons.Outlined.Comment, null) }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = currentText.isNotBlank(),
                onClick = {
                        onConfirm(comment.apply { text = currentText })
                        onDismiss()
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
fun EditCommentDialog_Preview() {

    MaterialTheme {
        EditCommentDialog(
            comment = Comment(text = "this is my comment"),
            onConfirm = { },
            onDismiss = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditCommentDialog_Preview_blank() {

    MaterialTheme {
        EditCommentDialog(
            comment = Comment(text = ""),
            onConfirm = { },
            onDismiss = { }
        )
    }
}
