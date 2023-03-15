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
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredResource
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.reusable.cards.SubnoteCard
import at.techbee.jtx.ui.reusable.dialogs.AddAudioNoteDialog
import at.techbee.jtx.ui.reusable.dialogs.EditSubnoteDialog
import at.techbee.jtx.ui.reusable.dialogs.LinkExistingSubentryDialog
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.ui.theme.jtxCardCornerShape
import net.fortuna.ical4j.model.Component


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailsCardSubnotes(
    subnotes: List<ICal4List>,
    isEditMode: MutableState<Boolean>,
    selectFromAllListLive: LiveData<List<ICal4ListRel>>,
    storedCategories: List<StoredCategory>,
    storedResources: List<StoredResource>,
    onSubnoteAdded: (subnote: ICalObject, attachment: Attachment?) -> Unit,
    onSubnoteUpdated: (icalobjectId: Long, text: String) -> Unit,
    onSubnoteDeleted: (icalobjectId: Long) -> Unit,
    onUnlinkSubEntry: (icalobjectId: Long) -> Unit,
    onLinkSubEntries: (List<ICal4List>) -> Unit,
    onAllEntriesSearchTextUpdated: (String) -> Unit,
    player: MediaPlayer?,
    goToDetail: (itemId: Long, editMode: Boolean, list: List<Long>) -> Unit,
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
    var showLinkExistingSubentryDialog by rememberSaveable { mutableStateOf(false) }
    if(showLinkExistingSubentryDialog) {
        LinkExistingSubentryDialog(
            allEntriesLive = selectFromAllListLive,
            storedCategories = storedCategories,
            storedResources = storedResources,
            onAllEntriesSearchTextUpdated = onAllEntriesSearchTextUpdated,
            onNewSubentriesConfirmed = { selected -> onLinkSubEntries(selected) },
            onDismiss = { showLinkExistingSubentryDialog = false }
        )
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
                HeadlineWithIcon(icon = Icons.Outlined.Note, iconDesc = headline, text = headline, modifier = Modifier.weight(1f))
                
                AnimatedVisibility(isEditMode.value) {
                        IconButton(onClick = { showLinkExistingSubentryDialog = true }) {
                            Icon(painterResource(id = R.drawable.ic_link_variant_plus), stringResource(R.string.details_link_existing_subentry_dialog_title))
                        }
                }
                
            }

            AnimatedVisibility(subnotes.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    subnotes.forEach { subnote ->

                        var showEditSubnoteDialog by remember { mutableStateOf(false) }

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
                            onDeleteClicked = { icalObjectId ->  onSubnoteDeleted(icalObjectId) },
                            onUnlinkClicked = onUnlinkSubEntry,
                            player = player,
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
                                    if (newSubnoteText.isNotEmpty())
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
                            .weight(1f),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newSubnoteText.isNotEmpty())
                                onSubnoteAdded(ICalObject.createNote(newSubnoteText), null)
                            newSubnoteText = ""
                        })
                    )
                    IconButton(onClick = { showAddAudioNoteDialog = true }) {
                        Icon(Icons.Outlined.Mic, stringResource(id = R.string.view_add_audio_note))
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
            selectFromAllListLive = MutableLiveData(emptyList()),
            storedCategories = emptyList(),
            storedResources = emptyList(),
            onSubnoteAdded = { _, _ -> },
            onSubnoteUpdated = { _, _ ->  },
            onSubnoteDeleted = { },
            onUnlinkSubEntry = { },
            onLinkSubEntries = { },
            player = null,
            goToDetail = { _, _, _ -> },
            onAllEntriesSearchTextUpdated = { }
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
            selectFromAllListLive = MutableLiveData(emptyList()),
            storedCategories = emptyList(),
            storedResources = emptyList(),
            onSubnoteAdded = { _, _ -> },
            onSubnoteUpdated = { _, _ ->  },
            onSubnoteDeleted = { },
            onUnlinkSubEntry = { },
            onLinkSubEntries = { },
            player = null,
            goToDetail = { _, _, _ -> },
            onAllEntriesSearchTextUpdated = { }
        )
    }
}