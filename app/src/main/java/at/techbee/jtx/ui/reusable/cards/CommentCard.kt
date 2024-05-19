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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.database.properties.Comment
import at.techbee.jtx.ui.reusable.dialogs.EditCommentDialog


@Composable
fun CommentCard(
    comment: Comment,
    isReadOnly: Boolean,
    modifier: Modifier = Modifier,
    onCommentDeleted: () -> Unit,
    onCommentUpdated: (Comment) -> Unit
) {

    var showCommentEditDialog by rememberSaveable { mutableStateOf(false) }

    if(showCommentEditDialog) {
        EditCommentDialog(
            comment = comment,
            onConfirm = { updatedComment -> onCommentUpdated(updatedComment) },
            onDismiss = { showCommentEditDialog = false },
            onDelete = onCommentDeleted
        )
    }

        ElevatedCard(
            modifier = modifier,
            onClick = {
                if(!isReadOnly)
                    showCommentEditDialog = true
            }
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

@Preview(showBackground = true)
@Composable
fun CommentCardPreview() {
    MaterialTheme {
        CommentCard(
            comment = Comment(text = "This is my comment"),
            isReadOnly = false,
            onCommentDeleted = { },
            onCommentUpdated = { }
        )
    }
}

