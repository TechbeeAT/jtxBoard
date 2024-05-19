/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Comment
import at.techbee.jtx.ui.reusable.cards.CommentCard
import at.techbee.jtx.ui.reusable.dialogs.EditCommentDialog
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon


@Composable
fun DetailsCardComments(
    comments: SnapshotStateList<Comment>,
    isReadOnly: Boolean,
    onCommentsUpdated: () -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.comments)
    var showAddCommentDialog by rememberSaveable { mutableStateOf(false) }

    if(showAddCommentDialog) {
        EditCommentDialog(
            comment = Comment(),
            onConfirm = { newComment ->
                comments.add(newComment)
                onCommentsUpdated()
            },
            onDismiss = { showAddCommentDialog = false },
            onDelete = { })
    }


    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                HeadlineWithIcon(
                    icon = Icons.AutoMirrored.Outlined.Comment,
                    iconDesc = headline,
                    text = headline
                )

                if(!isReadOnly) {
                    IconButton(onClick = {
                        showAddCommentDialog = true
                    }) {
                        Icon(Icons.Outlined.AddComment, stringResource(id = R.string.edit_comment_helper))
                    }
                }
            }

            AnimatedVisibility(comments.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    comments.forEach { comment ->
                        CommentCard(
                            comment = comment,
                            isReadOnly = isReadOnly,
                            onCommentDeleted = {
                                comments.remove(comment)
                                onCommentsUpdated()
                                               },
                            onCommentUpdated = { updatedComment ->
                                comment.text = updatedComment.text
                                onCommentsUpdated()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardComments_Preview() {
    MaterialTheme {

        DetailsCardComments(
            comments = remember { mutableStateListOf(
                Comment(text = "First comment"),
                Comment(text = "Second comment\nthat's a bit longer. Here's also a bit more text to see how it reacts when there should be a line break.")
            ) },
            isReadOnly = false,
            onCommentsUpdated = {  }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardComments_Preview_readonly() {
    MaterialTheme {
        DetailsCardComments(
            comments = remember { mutableStateListOf(
                Comment(text = "First comment"),
                Comment(text = "Second comment\nthat's a bit longer. Here's also a bit more text to see how it reacts when there should be a line break.")
            ) },
            isReadOnly = true,
            onCommentsUpdated = {  }
        )
    }
}