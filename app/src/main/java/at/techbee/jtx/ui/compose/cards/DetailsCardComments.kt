/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Comment
import at.techbee.jtx.ui.compose.elements.HeadlineWithIcon


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsCardComments(
    comments: MutableState<List<Comment>>,
    isEditMode: MutableState<Boolean>,
    onCommentsUpdated: () -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.comments)
    var newComment by rememberSaveable { mutableStateOf("") }


    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            HeadlineWithIcon(icon = Icons.Outlined.Comment, iconDesc = headline, text = headline)

            AnimatedVisibility(comments.value.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    comments.value.forEach { comment ->
                        CommentCard(
                            comment = comment,
                            isEditMode = isEditMode,
                            onCommentDeleted = { /* TODO */ }
                        )
                    }
                }
            }

            AnimatedVisibility(isEditMode.value) {
                OutlinedTextField(
                    value = newComment,
                    trailingIcon = {
                        AnimatedVisibility(newComment.isNotEmpty()) {
                            IconButton(onClick = {
                                comments.value = comments.value.plus(Comment(text = newComment))
                                newComment = ""
                            }) {
                                Icon(
                                    Icons.Outlined.AddComment,
                                    stringResource(id = R.string.edit_comment_helper)
                                )
                            }
                        }
                    },
                    label = { Text(stringResource(id = R.string.edit_comment_helper)) },
                    onValueChange = { newValue -> newComment = newValue },
                    //colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.dp, Color.Transparent),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        comments.value = comments.value.plus(Comment(text = newComment))
                        newComment = ""
                    })
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardComments_Preview() {
    MaterialTheme {

        DetailsCardComments(
            comments = remember { mutableStateOf(listOf(
                Comment(text = "First comment"),
                Comment(text = "Second comment\nthat's a bit longer. Here's also a bit more text to see how it reacts when there should be a line break.")
            )) },            isEditMode = remember { mutableStateOf(false) },
            onCommentsUpdated = {  }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardComments_Preview_edit() {
    MaterialTheme {
        DetailsCardComments(
            comments = remember { mutableStateOf(listOf(
                Comment(text = "First comment"),
                Comment(text = "Second comment\nthat's a bit longer. Here's also a bit more text to see how it reacts when there should be a line break.")
            )) },
            isEditMode = remember { mutableStateOf(true) },
            onCommentsUpdated = {  }
        )
    }
}