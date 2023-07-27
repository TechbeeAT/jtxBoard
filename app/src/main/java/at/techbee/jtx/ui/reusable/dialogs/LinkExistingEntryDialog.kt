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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import at.techbee.jtx.ui.reusable.appbars.OverflowMenu
import at.techbee.jtx.ui.reusable.elements.CheckboxWithText


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LinkExistingEntryDialog(
    excludeCurrentId: Long?,
    preselectedLinkEntryModules: List<Module>,
    preselectedLinkEntryReltype: Reltype,
    allEntriesLive: LiveData<List<ICal4ListRel>>,
    storedCategories: List<StoredCategory>,
    storedResources: List<StoredResource>,
    extendedStatuses: List<ExtendedStatus>,
    player: MediaPlayer?,
    onAllEntriesSearchTextUpdated: (String, List<Module>, sameAccount: Boolean, sameCollection: Boolean) -> Unit,
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
    val menuExpanded = remember { mutableStateOf(false) }
    var searchWithinSameCollection by remember { mutableStateOf(true) }
    var searchWithinSameAccount by remember { mutableStateOf(true) }

    LaunchedEffect(true) {
        onAllEntriesSearchTextUpdated(allEntriesSearchText, linkEntryModules, searchWithinSameCollection, searchWithinSameAccount)
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(stringResource(R.string.link_selected_as)) },
        text = {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

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
                        onAllEntriesSearchTextUpdated(it, linkEntryModules, searchWithinSameCollection, searchWithinSameAccount)
                    },
                    label = { Text(stringResource(R.string.search)) },
                    trailingIcon = {

                        OverflowMenu(menuExpanded = menuExpanded) {

                            Text(stringResource(R.string.link_entry_search_within), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp))

                            HorizontalDivider()

                            Module.values().forEach { module ->
                                CheckboxWithText(
                                    text = stringResource(
                                        id = when (module) {
                                            Module.JOURNAL -> R.string.list_tabitem_journals
                                            Module.NOTE -> R.string.list_tabitem_notes
                                            Module.TODO -> R.string.list_tabitem_todos
                                        }
                                    ),
                                    isSelected = linkEntryModules.contains(module),
                                    onCheckedChange = {
                                        if(it) linkEntryModules.add(module) else linkEntryModules.remove(module)
                                        onAllEntriesSearchTextUpdated(allEntriesSearchText, linkEntryModules, searchWithinSameCollection, searchWithinSameAccount)
                                    },
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }

                            HorizontalDivider()

                            CheckboxWithText(
                                text = stringResource(R.string.link_entry_search_within_same_collection),
                                isSelected = searchWithinSameCollection,
                                onCheckedChange = {
                                    searchWithinSameCollection = it
                                    if(it)
                                        searchWithinSameAccount = true
                                    onAllEntriesSearchTextUpdated(allEntriesSearchText, linkEntryModules, searchWithinSameCollection, searchWithinSameAccount)
                                },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            CheckboxWithText(
                                text = stringResource(R.string.link_entry_search_within_same_account),
                                isSelected = searchWithinSameAccount,
                                onCheckedChange = {
                                    searchWithinSameAccount = it
                                    if(!it)
                                        searchWithinSameCollection = false
                                    onAllEntriesSearchTextUpdated(allEntriesSearchText, linkEntryModules, searchWithinSameCollection, searchWithinSameAccount)
                                },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
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

                            if(entry.iCal4List.id == excludeCurrentId)
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
            excludeCurrentId = null,
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
            onAllEntriesSearchTextUpdated = { _, _, _, _ -> },
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
            excludeCurrentId = null,
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
            onAllEntriesSearchTextUpdated = { _, _, _, _ -> },
            onEntriesToLinkConfirmed = { _, _ -> },
            onDismiss = { }
        )
    }
}
