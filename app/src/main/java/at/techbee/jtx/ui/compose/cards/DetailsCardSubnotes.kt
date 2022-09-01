/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.cards

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Note
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
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.compose.elements.HeadlineWithIcon
import net.fortuna.ical4j.model.Component


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsCardSubnotes(
    subnotes: List<ICal4List>,
    isEditMode: MutableState<Boolean>,
    onSubnoteAdded: (subnote: ICalObject) -> Unit,
    onSubnoteUpdated: (icalobjectId: Long, text: String) -> Unit,
    onSubnoteDeleted: (icalobjectId: Long) -> Unit,
    player: MediaPlayer?,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.view_feedback_linked_notes)
    var newSubnoteText by rememberSaveable { mutableStateOf("") }


    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            HeadlineWithIcon(icon = Icons.Outlined.Note, iconDesc = headline, text = headline)

            AnimatedVisibility(subnotes.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    subnotes.forEach { subnote ->
                        SubnoteCard(
                            subnote = subnote,
                            isEditMode = isEditMode.value,
                            onDeleteClicked = { icalObjectId ->  onSubnoteDeleted(icalObjectId) },
                            onSubnoteUpdated = { newText -> onSubnoteUpdated(subnote.id, newText) },
                            player = player
                        )
                    }
                }
            }

            AnimatedVisibility(isEditMode.value) {
                OutlinedTextField(
                    value = newSubnoteText,
                    trailingIcon = {
                        AnimatedVisibility(newSubnoteText.isNotEmpty()) {
                            IconButton(onClick = {
                                if(newSubnoteText.isNotEmpty())
                                    onSubnoteAdded(ICalObject.createNote(newSubnoteText))
                                newSubnoteText = ""
                            }) {
                                Icon(
                                    Icons.Outlined.EditNote,
                                    stringResource(id = R.string.edit_subnote_add_helper)
                                )
                            }
                        }
                    },
                    label = { Text(stringResource(id = R.string.edit_subnote_add_helper)) },
                    onValueChange = { newValue -> newSubnoteText = newValue },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.dp, Color.Transparent),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if(newSubnoteText.isNotEmpty())
                            onSubnoteAdded(ICalObject.createNote(newSubnoteText))
                        newSubnoteText = ""
                    })
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardSubnotes_Preview() {
    MaterialTheme {

        DetailsCardSubnotes(
            subnotes = listOf(
                        ICal4List.getSample().apply {
                            this.component = Component.VJOURNAL
                            this.module = Module.NOTE.name
                            this.summary = "My Subnote"
                        }
                    ),
            isEditMode = remember { mutableStateOf(false) },
            onSubnoteAdded = { },
            onSubnoteUpdated = { _, _ ->  },
            onSubnoteDeleted = { },
            player = null
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardSubnotes_Preview_edit() {
    MaterialTheme {
        DetailsCardSubnotes(
            subnotes = listOf(
                ICal4List.getSample().apply {
                    this.component = Component.VJOURNAL
                    this.module = Module.NOTE.name
                    this.summary = "My Subnote"
                }
            ),
            isEditMode = remember { mutableStateOf(true) },
            onSubnoteAdded = { },
            onSubnoteUpdated = { _, _ ->  },
            onSubnoteDeleted = { },
            player = null
        )
    }
}