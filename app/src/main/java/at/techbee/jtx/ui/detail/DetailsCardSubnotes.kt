/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
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
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.reusable.cards.SubnoteCard
import at.techbee.jtx.ui.reusable.dialogs.AddAudioNoteDialog
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import net.fortuna.ical4j.model.Component


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailsCardSubnotes(
    subnotes: List<ICal4List>,
    isEditMode: MutableState<Boolean>,
    onSubnoteAdded: (subnote: ICalObject, attachment: Attachment?) -> Unit,
    onSubnoteUpdated: (icalobjectId: Long, text: String) -> Unit,
    onSubnoteDeleted: (icalobjectId: Long) -> Unit,
    player: MediaPlayer?,
    goToView: (itemId: Long) -> Unit,
    goToEdit: (itemId: Long) -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.view_feedback_linked_notes)
    var newSubnoteText by rememberSaveable { mutableStateOf("") }

    var showAddAudioNoteDialog by rememberSaveable { mutableStateOf(false) }
    if(showAddAudioNoteDialog) {
        AddAudioNoteDialog(
            player = player,
            onConfirm = { newEntry, attachment -> onSubnoteAdded(newEntry, attachment) },
            onDismiss = { showAddAudioNoteDialog = false }
        )
    }


    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
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
                            player = player,
                            modifier = Modifier.combinedClickable(
                                onClick = { if(!isEditMode.value) goToView(subnote.id) },
                                onLongClick = {
                                    if (!isEditMode.value &&!subnote.isReadOnly && BillingManager.getInstance().isProPurchased.value == true)
                                        goToEdit(subnote.id)
                                }
                            )
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
                                    onSubnoteAdded(ICalObject.createNote(newSubnoteText), null)
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
                    isError = newSubnoteText.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.dp, Color.Transparent),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if(newSubnoteText.isNotEmpty())
                            onSubnoteAdded(ICalObject.createNote(newSubnoteText), null)
                        newSubnoteText = ""
                    })
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { showAddAudioNoteDialog = true },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(Icons.Outlined.Mic, null, modifier = Modifier.padding(end = 4.dp))
                    Text(stringResource(id = R.string.view_add_audio_note))
                }
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
            onSubnoteAdded = { _, _ -> },
            onSubnoteUpdated = { _, _ ->  },
            onSubnoteDeleted = { },
            player = null,
            goToView = { },
            goToEdit = { }
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
            onSubnoteAdded = { _, _ -> },
            onSubnoteUpdated = { _, _ ->  },
            onSubnoteDeleted = { },
            player = null,
            goToView = { },
            goToEdit = { }
        )
    }
}