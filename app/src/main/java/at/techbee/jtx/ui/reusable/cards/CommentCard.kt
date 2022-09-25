/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Comment
import at.techbee.jtx.ui.reusable.dialogs.EditCommentDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentCard(
    comment: Comment,
    isEditMode: Boolean,
    modifier: Modifier = Modifier,
    onCommentDeleted: () -> Unit,
    onCommentUpdated: (Comment) -> Unit
) {

    var showCommentEditDialog by remember { mutableStateOf(false) }

    if(showCommentEditDialog) {
        EditCommentDialog(
            comment = comment,
            onConfirm = { updatedComment -> onCommentUpdated(updatedComment) },
            onDismiss = { showCommentEditDialog = false }
        )
    }

    if (isEditMode) {
        OutlinedCard(
            modifier = modifier,
            onClick = {showCommentEditDialog = true}
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    comment.text,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .align(alignment = Alignment.CenterVertically)
                        .weight(1f)
                )
                IconButton(onClick = { onCommentDeleted() }) {
                    Icon(Icons.Outlined.Delete, stringResource(id = R.string.delete))
                }
            }
        }
    } else {
        ElevatedCard(
            modifier = modifier
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    comment.text,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .align(alignment = Alignment.CenterVertically)
                        .weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommentCardPreview_view() {
    MaterialTheme {
        CommentCard(
            comment = Comment(text = "This is my comment"),
            isEditMode = false,
            onCommentDeleted = { },
            onCommentUpdated = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommentCardPreview_edit() {
    MaterialTheme {
        CommentCard(
            comment = Comment(text = "This is my comment"),
            isEditMode = true,
            onCommentDeleted = { },
            onCommentUpdated = { }
        )
    }
}
