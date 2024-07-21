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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertComment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Comment


@Composable
fun EditCommentDialog(
    comment: Comment,
    onConfirm: (Comment) -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {

    var currentText by rememberSaveable { mutableStateOf(comment.text) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Outlined.InsertComment, null)
                Text(stringResource(id = if(comment.commentId == 0L) R.string.edit_comment_helper else R.string.edit_comment))
            }
        },
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
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if(currentText.isNotBlank()) {
                            onConfirm(comment.apply { text = currentText })
                            onDismiss()
                        }
                    }),
                    modifier = Modifier.focusRequester(focusRequester)
                )
            }
        },
        confirmButton = {

            Row {

                if(comment.text.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        }
                    ) {
                        Text(stringResource(id = R.string.delete))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Text(stringResource(id = R.string.cancel))
                }

                TextButton(
                    enabled = currentText.isNotBlank(),
                    onClick = {
                            onConfirm(comment.apply { text = currentText })
                            onDismiss()
                    }
                ) {
                    Text(stringResource(id = R.string.save))
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun EditCommentDialog_Preview() {

    MaterialTheme {
        EditCommentDialog(
            comment = Comment(),
            onConfirm = { },
            onDismiss = { },
            onDelete = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditCommentDialog_Preview_new() {

    MaterialTheme {
        EditCommentDialog(
            comment = Comment(text = "asdf", commentId = 1L),
            onConfirm = { },
            onDismiss = { },
            onDelete = { }
        )
    }
}
