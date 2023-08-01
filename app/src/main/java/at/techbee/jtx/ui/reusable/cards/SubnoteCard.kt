/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.cards

import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.reusable.dialogs.UnlinkEntryDialog
import at.techbee.jtx.ui.reusable.elements.AudioPlaybackElement
import at.techbee.jtx.util.DateTimeUtils


@Composable
fun SubnoteCard(
    subnote: ICal4List,
    selected: Boolean,
    player: MediaPlayer?,
    isEditMode: Boolean,
    modifier: Modifier = Modifier,
    allowDeletion: Boolean = true,
    onDeleteClicked: () -> Unit,
    onUnlinkClicked: () -> Unit
) {

    var showUnlinkFromParentDialog by rememberSaveable { mutableStateOf(false) }
    if(showUnlinkFromParentDialog) {
        UnlinkEntryDialog(
            onConfirm = { onUnlinkClicked() },
            onDismiss = { showUnlinkFromParentDialog = false }
        )
    }

    Card(
        modifier = modifier,
        colors = if(isEditMode) CardDefaults.outlinedCardColors()
                else if (selected) CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                else CardDefaults.elevatedCardColors(),
        elevation = if(isEditMode) CardDefaults.outlinedCardElevation() else CardDefaults.elevatedCardElevation(),
        border = if(isEditMode) CardDefaults.outlinedCardBorder() else null
    ) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                subnote.getAudioAttachmentAsUri()?.let {
                    AudioPlaybackElement(
                        uri = it,
                        player = player,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 4.dp)
                    )
                }

                if(subnote.dtstart != null) {
                    Text(
                        text = DateTimeUtils.convertLongToShortDateTimeString(subnote.dtstart, subnote.dtstartTimezone),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                if (subnote.summary?.isNotBlank() == true || subnote.description?.isNotBlank() == true) {
                    Text(
                        text = subnote.summary?.trim() ?: subnote.description?.trim() ?: "",
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (isEditMode) {
                VerticalDivider(modifier = Modifier.height(28.dp))

                if(allowDeletion) {
                    IconButton(onClick = { onDeleteClicked() }) {
                        Icon(Icons.Outlined.Delete, stringResource(id = R.string.delete))
                    }
                }
                IconButton(onClick = { showUnlinkFromParentDialog = true }) {
                    Icon(painterResource(id = R.drawable.ic_link_variant_remove), stringResource(R.string.dialog_unlink_from_parent_title))
                }
            }
        }
    }

}



@Preview(showBackground = true)
@Composable
fun SubnoteCardPreview() {
    MaterialTheme {
        SubnoteCard(
            subnote = ICal4List.getSample().apply {
                this.summary = null
                this.description =
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.isReadOnly = false
            },
            selected = false,
            player = null,
            isEditMode = false,
            onDeleteClicked = { },
            onUnlinkClicked = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SubnoteCardPreview_selected() {
    MaterialTheme {
        SubnoteCard(
            subnote = ICal4List.getSample().apply {
                this.summary = null
                this.description =
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.isReadOnly = false
            },
            selected = true,
            player = null,
            isEditMode = false,
            onDeleteClicked = { },
            onUnlinkClicked = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubnoteCardPreview_audio() {
    MaterialTheme {
        SubnoteCard(
            subnote = ICal4List.getSample().apply {
                this.summary = null
                this.description = null
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.isReadOnly = true
                this.numSubtasks = 7
                this.audioAttachment = "https://www.orf.at/blabla.mp3"
            },
            selected = false,
            player = null,
            isEditMode = false,
            onDeleteClicked = { },
            onUnlinkClicked = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubnoteCardPreview_audio_with_text() {
    MaterialTheme {
        SubnoteCard(
            subnote = ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
                this.audioAttachment = "https://www.orf.at/blabla.mp3"
            },
            selected = false,
            player = null,
            isEditMode = false,
            onDeleteClicked = { },
            onUnlinkClicked = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubnoteCardPreview_edit() {
    MaterialTheme {
        SubnoteCard(
            subnote = ICal4List.getSample().apply {
                this.summary = "this is to edit"
                this.description = null
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.isReadOnly = true
                this.numSubtasks = 7
            },
            selected = false,
            player = null,
            isEditMode = true,
            onDeleteClicked = { },
            onUnlinkClicked = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SubnoteCardPreview_journal() {
    MaterialTheme {
        SubnoteCard(
            subnote = ICal4List.getSample().apply {
                this.summary = null
                this.description =
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                this.component = Component.VJOURNAL.name
                this.module = Module.JOURNAL.name
                this.isReadOnly = false
            },
            selected = false,
            player = null,
            isEditMode = false,
            onDeleteClicked = { },
            onUnlinkClicked = { }
        )
    }
}
