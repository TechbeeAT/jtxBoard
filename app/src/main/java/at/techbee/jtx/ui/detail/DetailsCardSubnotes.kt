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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
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
import at.techbee.jtx.ui.detail.DetailTopAppBarMode.ADD_SUBNOTE
import at.techbee.jtx.ui.reusable.cards.SubnoteCard
import at.techbee.jtx.ui.reusable.dialogs.AddAudioEntryDialog
import at.techbee.jtx.ui.reusable.dialogs.EditSubnoteDialog
import at.techbee.jtx.ui.reusable.dialogs.onSingleOrMultipleItemCreation
import at.techbee.jtx.ui.reusable.elements.DragHandle
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.ui.theme.jtxCardCornerShape
import net.fortuna.ical4j.model.Component
import sh.calvin.reorderable.ReorderableColumn


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailsCardSubnotes(
    subnotes: List<ICal4List>,
    isEditMode: MutableState<Boolean>,
    enforceSavingSubnote: Boolean,
    onSubnoteAdded: (subnote: ICalObject, attachment: Attachment?) -> Unit,
    onSubnoteUpdated: (icalobjectId: Long, text: String) -> Unit,
    onSubnoteDeleted: (icalobjectId: Long) -> Unit,
    onUnlinkSubEntry: (icalobjectId: Long) -> Unit,
    player: MediaPlayer?,
    isSubnoteDragAndDropEnabled: Boolean,
    goToDetail: (itemId: Long, editMode: Boolean, list: List<Long>) -> Unit,
    onShowLinkExistingDialog: () -> Unit,
    onUpdateSortOrder: (List<ICal4List>) -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.view_feedback_linked_notes)
    var newSubnoteText by rememberSaveable { mutableStateOf("") }
    val onSubnoteCreation = onSingleOrMultipleItemCreation(ADD_SUBNOTE) { onSubnoteAdded(ICalObject.createNote(it), null) }

    var showAddAudioNoteDialog by rememberSaveable { mutableStateOf(false) }
    if(showAddAudioNoteDialog) {
        AddAudioEntryDialog(
            module = Module.NOTE,
            player = player,
            onConfirm = { newEntry, attachment -> onSubnoteAdded(newEntry, attachment) },
            onDismiss = { showAddAudioNoteDialog = false }
        )
    }

    if (enforceSavingSubnote && newSubnoteText.isNotEmpty()) {
        onSubnoteAdded(ICalObject.createNote(newSubnoteText), null)
        newSubnoteText = ""
    }


    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(), 
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeadlineWithIcon(icon = Icons.AutoMirrored.Outlined.Note, iconDesc = headline, text = headline, modifier = Modifier.weight(1f))
                
                AnimatedVisibility(isEditMode.value) {
                        IconButton(onClick = { onShowLinkExistingDialog() }) {
                            Icon(painterResource(id = R.drawable.ic_link_variant_plus), stringResource(R.string.details_link_existing_subentry_dialog_title))
                        }
                }
                
            }


            AnimatedVisibility(isEditMode.value) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newSubnoteText,
                        trailingIcon = {
                            AnimatedVisibility(newSubnoteText.isNotEmpty()) {
                                IconButton(onClick = {
                                    if (newSubnoteText.isNotEmpty()) {
                                        onSubnoteCreation(newSubnoteText)
                                    }
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
                            .weight(1f)
                            .padding(bottom = 8.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newSubnoteText.isNotEmpty()) {
                                onSubnoteCreation(newSubnoteText)
                            }
                            newSubnoteText = ""
                        })
                    )
                    IconButton(onClick = { showAddAudioNoteDialog = true }) {
                        Icon(Icons.Outlined.Mic, stringResource(id = R.string.view_add_audio_note))
                    }
                }
            }

            AnimatedVisibility(subnotes.isNotEmpty()) {
                ReorderableColumn(
                    list = subnotes,
                    onSettle = { fromIndex, toIndex ->
                        val reordered = subnotes.toMutableList().apply {
                            add(toIndex, removeAt(fromIndex))
                        }
                        onUpdateSortOrder(reordered)
                    },
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {index, subnote, isDragging ->
                    key(subnote.id) {

                        var showEditSubnoteDialog by rememberSaveable { mutableStateOf(false) }

                        if (showEditSubnoteDialog) {
                            EditSubnoteDialog(
                                text = subnote.summary,
                                onConfirm = { newText -> onSubnoteUpdated(subnote.id, newText) },
                                onDismiss = { showEditSubnoteDialog = false }
                            )
                        }

                        SubnoteCard(
                            subnote = subnote,
                            selected = false,
                            isEditMode = isEditMode.value,
                            onDeleteClicked = { onSubnoteDeleted(subnote.id) },
                            onUnlinkClicked = { onUnlinkSubEntry(subnote.id) },
                            player = player,
                            dragHandle = { if(isSubnoteDragAndDropEnabled) DragHandle(scope = this) },
                            modifier = Modifier
                                .clip(jtxCardCornerShape)
                                .combinedClickable(
                                    onClick = {
                                        if (!isEditMode.value)
                                            goToDetail(subnote.id, false, subnotes.map { it.id })
                                        else showEditSubnoteDialog = true
                                    },
                                    onLongClick = {
                                        if (!isEditMode.value && !subnote.isReadOnly && BillingManager.getInstance().isProPurchased.value == true)
                                            goToDetail(subnote.id, true, subnotes.map { it.id })
                                    }
                                )
                        )
                    }
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
            enforceSavingSubnote = false,
            onSubnoteAdded = { _, _ -> },
            onSubnoteUpdated = { _, _ ->  },
            onSubnoteDeleted = { },
            onUnlinkSubEntry = { },
            player = null,
            isSubnoteDragAndDropEnabled = true,
            goToDetail = { _, _, _ -> },
            onShowLinkExistingDialog = {},
            onUpdateSortOrder = {}
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
            enforceSavingSubnote = false,
            onSubnoteAdded = { _, _ -> },
            onSubnoteUpdated = { _, _ ->  },
            onSubnoteDeleted = { },
            onUnlinkSubEntry = { },
            player = null,
            isSubnoteDragAndDropEnabled = true,
            goToDetail = { _, _, _ -> },
            onShowLinkExistingDialog = {},
            onUpdateSortOrder = { }
        )
    }
}