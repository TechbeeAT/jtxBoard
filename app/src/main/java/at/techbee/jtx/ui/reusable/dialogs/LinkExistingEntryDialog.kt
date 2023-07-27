/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredResource
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.list.ListCardGrid


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LinkExistingEntryDialog(
    preselectedLinkEntryModules: List<Module>,
    preselectedLinkEntryReltype: Reltype,
    allEntriesLive: LiveData<List<ICal4ListRel>>,
    storedCategories: List<StoredCategory>,
    storedResources: List<StoredResource>,
    extendedStatuses: List<ExtendedStatus>,
    player: MediaPlayer?,
    onAllEntriesSearchTextUpdated: (String, List<Module>) -> Unit,
    onEntriesToLinkConfirmed: (newSubentries: List<ICal4List>, linkEntryReltype: Reltype) -> Unit,
    onDismiss: () -> Unit
) {
    val allEntries by allEntriesLive.observeAsState(emptyList())
    val linkEntryModules = remember { mutableStateListOf<Module>() }
    LaunchedEffect(preselectedLinkEntryModules) {
        linkEntryModules.addAll(preselectedLinkEntryModules)
    }
    var linkEntryReltype by remember { mutableStateOf(preselectedLinkEntryReltype) }

    var allEntriesSearchText by remember { mutableStateOf("") }
    val selectedEntries = remember { mutableStateListOf<ICal4List>() }
    var maxEntriesShown by remember { mutableIntStateOf(10) }

    LaunchedEffect(true) {
        onAllEntriesSearchTextUpdated(allEntriesSearchText, linkEntryModules)
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(stringResource(R.string.link_entry)) },
        text = {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                FlowRow {
                    Module.values().forEach { module ->
                        FilterChip(
                            selected = linkEntryModules.contains(module),
                            onClick = {
                                if(linkEntryModules.contains(module))
                                      linkEntryModules.remove(module)
                                else
                                    linkEntryModules.add(module)
                                onAllEntriesSearchTextUpdated(allEntriesSearchText, linkEntryModules)
                            },
                            label = {
                                Text(
                                    stringResource(
                                        id = when (module) {
                                            Module.JOURNAL -> R.string.list_tabitem_journals
                                            Module.NOTE -> R.string.list_tabitem_notes
                                            Module.TODO -> R.string.list_tabitem_todos
                                        }
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.link_selected_as),
                    style = MaterialTheme.typography.labelMedium
                )

                FlowRow {
                    listOf(Reltype.CHILD, Reltype.PARENT).forEach { reltype ->
                        FilterChip(
                            selected = linkEntryReltype == reltype,
                            onClick = { linkEntryReltype = reltype },
                            label = {
                                Text(
                                    when (reltype) {
                                        Reltype.PARENT -> stringResource(R.string.link_selected_as_parent)
                                        Reltype.CHILD -> stringResource(R.string.link_selected_as_subtask_note)
                                        Reltype.SIBLING -> ""
                                    },
                                    textAlign = TextAlign.Center
                                )
                            },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }

                Text(
                    text = when(linkEntryReltype) {
                        Reltype.CHILD -> stringResource(R.string.details_link_existing_subentry_dialog_info)
                        Reltype.PARENT -> stringResource(id = R.string.details_link_existing_parent_dialog_info)
                        Reltype.SIBLING -> ""
                    },
                    style = MaterialTheme.typography.labelMedium
                )

                OutlinedTextField(
                    value = allEntriesSearchText,
                    onValueChange = {
                        allEntriesSearchText = it
                        onAllEntriesSearchTextUpdated(it, linkEntryModules)
                    },
                    label = { Text(stringResource(R.string.search)) },
                    trailingIcon = {
                        AnimatedVisibility(allEntriesSearchText.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    allEntriesSearchText = ""
                                    onAllEntriesSearchTextUpdated("", linkEntryModules)
                                }
                            ) {
                                Icon(Icons.Outlined.Close, stringResource(R.string.delete))
                            }
                        }
                    }
                )

                AnimatedVisibility(allEntries.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        allEntries.forEachIndexed { index, entry ->
                            if(index > maxEntriesShown)
                                return@forEachIndexed

                            ListCardGrid(
                                iCalObject = entry.iCal4List,
                                categories = entry.categories,
                                resources = entry.resources,
                                storedCategories = storedCategories,
                                storedResources = storedResources,
                                storedStatuses = extendedStatuses,
                                selected = selectedEntries.contains(entry.iCal4List),
                                progressUpdateDisabled = true,
                                markdownEnabled = false,
                                onProgressChanged = { _, _ -> },
                                player = player,
                                modifier = Modifier.clickable {
                                    if (selectedEntries.contains(entry.iCal4List))
                                        selectedEntries.remove(entry.iCal4List)
                                    else
                                        selectedEntries.add(entry.iCal4List)
                                }
                            )
                        }
                    }
                }

                AnimatedVisibility(allEntries.isNotEmpty() && maxEntriesShown < allEntries.size) {
                    TextButton(onClick = { maxEntriesShown += 10 }) {
                        Text(stringResource(R.string.more))
                    }
                }

            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onEntriesToLinkConfirmed(selectedEntries, linkEntryReltype)
                    onDismiss()
                },
                enabled = selectedEntries.isNotEmpty()
            ) {
                Text(stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LinkExistingEntryDialog_Preview_CHILD() {
    MaterialTheme {

        LinkExistingEntryDialog(
            preselectedLinkEntryModules = listOf(Module.TODO),
            preselectedLinkEntryReltype = Reltype.PARENT,
            allEntriesLive = MutableLiveData(
                listOf(
                    ICal4ListRel(ICal4List.getSample(), emptyList(), emptyList(), emptyList()),
                    ICal4ListRel(ICal4List.getSample(), emptyList(), emptyList(), emptyList()),
                    ICal4ListRel(ICal4List.getSample(), emptyList(), emptyList(), emptyList())
                )
            ),
            storedCategories = emptyList(),
            storedResources = emptyList(),
            extendedStatuses = emptyList(),
            player = null,
            onAllEntriesSearchTextUpdated = { _, _ -> },
            onEntriesToLinkConfirmed = { _, _ ->  },
            onDismiss = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LinkExistingEntryDialog_Preview_PARENT() {
    MaterialTheme {

        LinkExistingEntryDialog(
            preselectedLinkEntryModules = listOf(Module.TODO),
            preselectedLinkEntryReltype = Reltype.PARENT,
            allEntriesLive = MutableLiveData(
                listOf(
                    ICal4ListRel(ICal4List.getSample(), emptyList(), emptyList(), emptyList()),
                    ICal4ListRel(ICal4List.getSample(), emptyList(), emptyList(), emptyList()),
                    ICal4ListRel(ICal4List.getSample(), emptyList(), emptyList(), emptyList())
                )
            ),
            storedCategories = emptyList(),
            storedResources = emptyList(),
            extendedStatuses = emptyList(),
            player = null,
            onAllEntriesSearchTextUpdated = { _, _ -> },
            onEntriesToLinkConfirmed = { _, _ -> },
            onDismiss = { }
        )
    }
}
