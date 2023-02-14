/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.list.ListCardGrid


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LinkExistingSubentryDialog(
    allEntriesLive: LiveData<List<ICal4List>>,
    onSubentriesAdded: (newSubentries: List<ICal4List>) -> Unit,
    onDismiss: () -> Unit
) {
    val allEntries by allEntriesLive.observeAsState(emptyList())
    var allEntriesSearchText by remember { mutableStateOf("") }
    val selectedEntries = remember { mutableStateListOf<ICal4List>() }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(stringResource(R.string.details_link_existing_subentry_dialog_title)) },
        text = {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                    Text(
                        text = stringResource(R.string.details_link_existing_subentry_dialog_info),
                        style = MaterialTheme.typography.labelMedium
                    )

                    OutlinedTextField(
                        value = allEntriesSearchText,
                        onValueChange = {
                            allEntriesSearchText = it
                            //onSubtaskAdded(it)
                            TODO()
                        },
                        label = { Text(stringResource(R.string.search)) },
                        trailingIcon = {
                            AnimatedVisibility(allEntriesSearchText.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        allEntriesSearchText = ""
                                    }
                                ) {
                                    Icon(Icons.Outlined.Close, stringResource(R.string.delete))
                                }
                            }
                        }
                    )

                    Column {
                        allEntries.forEach { entry ->
                            ListCardGrid(
                                iCalObject = entry,
                                selected = selectedEntries.contains(entry),
                                progressUpdateDisabled = true,
                                onProgressChanged = {_, _ -> },
                                modifier = Modifier.clickable {
                                    if(selectedEntries.contains(entry))
                                        selectedEntries.remove(entry)
                                    else
                                        selectedEntries.add(entry)
                                }
                            )
                        }
                    }

            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubentriesAdded(selectedEntries)
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
fun LinkExistingSubentryDialog_Preview() {
    MaterialTheme {

        LinkExistingSubentryDialog(
            allEntriesLive = MutableLiveData(listOf()),
            onSubentriesAdded = {},
            onDismiss = { }
        )
    }
}
