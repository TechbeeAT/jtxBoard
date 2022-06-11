/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.ui.theme.JtxBoardTheme
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CollectionsScreen(
    collectionsLive: LiveData<List<CollectionsView>>,
    onCollectionChanged: (CollectionsView) -> Unit
) {

    val list by collectionsLive.observeAsState(emptyList())
    val listState = rememberLazyListState()

    val collectionToEdit = remember { mutableStateOf<CollectionsView?>(null) }


    val grouped = list.groupBy { it.accountName?:it.accountType?:"Account" }

    if(collectionToEdit.value != null)
        CollectionsAddOrEditDialog(current = collectionToEdit, onCollectionChanged = onCollectionChanged)



    Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 16.dp)) {

        Text(stringResource(id = R.string.collections_info), textAlign = TextAlign.Center)

        LazyColumn(
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            state = listState,
        ) {

            grouped.forEach { (account, collectionsInAccount) ->
                stickyHeader {
                    Text(
                        account,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, start = 8.dp, end = 16.dp, bottom = 8.dp)
                    )
                }

                items(
                    items = collectionsInAccount,
                    key = { collection -> collection.collectionId }
                ) { collection ->

                    CollectionCard(
                        collection = collection,
                        collectionToEdit = collectionToEdit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .animateItemPlacement()
                    )
                }

                /*
                ICalObjectListCard(
                    iCalObject,
                    currentSubtasks,
                    currentSubnotes,
                    navController,
                    isSubtasksExpandedDefault = settings.getBoolean(SettingsFragment.EXPAND_SUBTASKS_DEFAULT, false),
                    isSubnotesExpandedDefault = settings.getBoolean(SettingsFragment.EXPAND_SUBNOTES_DEFAULT, false),
                    isAttachmentsExpandedDefault = settings.getBoolean(SettingsFragment.EXPAND_ATTACHMENTS_DEFAULT, false),
                    settingShowProgressMaintasks = settings.getBoolean(SettingsFragment.SHOW_PROGRESS_FOR_MAINTASKS_IN_LIST, false),
                    settingShowProgressSubtasks = settings.getBoolean(SettingsFragment.SHOW_PROGRESS_FOR_SUBTASKS_IN_LIST, true),
                    onEditRequest = { id -> model.postDirectEditEntity(id) },
                    onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance -> model.updateProgress(itemId, newPercent, isLinkedRecurringInstance)  },
                    onExpandedChanged = { itemId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isAttachmentsExpanded: Boolean -> model.updateExpanded(itemId, isSubtasksExpanded, isSubnotesExpanded, isAttachmentsExpanded)},
                    player = mediaPlayer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .animateItemPlacement()
                        .combinedClickable(
                            onClick = {
                                navController.navigate(
                                    IcalListFragmentDirections
                                        .actionIcalListFragmentToIcalViewFragment()
                                        .setItem2show(iCalObject.property.id)
                                )
                            },
                            onLongClick = {
                                if (!iCalObject.property.isReadOnly && BillingManager.getInstance()?.isProPurchased?.value == true)
                                    model.postDirectEditEntity(iCalObject.property.id)
                            }
                        )
                )

                 */
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CollectionsScreen_Preview() {
    JtxBoardTheme {

        val collection1 = CollectionsView(
            collectionId = 1L,
            color = Color.Cyan.toArgb(),
            displayName = "Test",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )

        val collection2 = CollectionsView(
            collectionId = 2L,
            displayName = "Test Number 2",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL",
            numJournals = 5,
            numNotes = 19,
            numTodos = 8989,
            supportsVJOURNAL = true,
            supportsVTODO = true
        )

        val collection3 = CollectionsView(
            collectionId = 3L,
            displayName = "Test",
            description = "Here comes the desc",
            accountName = "Another account",
            accountType = "at.bitfire.davx5"
        )
        CollectionsScreen(
            MutableLiveData(listOf(collection1, collection2, collection3)),
            onCollectionChanged = {_ -> }
        )
    }
}




@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CollectionsAddOrEditDialog(
    current: MutableState<CollectionsView?>,
    onCollectionChanged: (CollectionsView) -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    var collectionName by remember { mutableStateOf(current.value?.displayName ?: current.value?.accountName ?: "") }
    var collectionColor by remember { mutableStateOf(current.value?.color) }
    var colorActivated by remember { mutableStateOf(current.value?.color != null) }


    AlertDialog(
        onDismissRequest = { current.value = null },
        title = {
            if(current.value?.collectionId == 0L)
                Text(text = stringResource(id = R.string.collections_dialog_add_local_collection_title))
            else
                Text(text = stringResource(id = R.string.collections_dialog_edit_local_collection_title))
        },
        text = {
            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                )
                 {
                    OutlinedTextField(
                        value = collectionName,
                        onValueChange = { collectionName = it },
                        label = { Text(stringResource(id = R.string.collection)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = colorActivated,
                        onCheckedChange = {
                            colorActivated = it
                        },
                        thumbContent = { Icon(Icons.Outlined.ColorLens, stringResource(id = R.string.color)) },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                AnimatedVisibility(visible = colorActivated) {
                    HarmonyColorPicker(
                        color = collectionColor?.let { Color(it) }?: Color.White,
                        harmonyMode = ColorHarmonyMode.NONE,
                        modifier = Modifier.size(300.dp),
                        onColorChanged = { hsvColor ->
                            collectionColor = hsvColor.toColor().toArgb()
                            //currentColor.value = hsvColor.toColor()
                            //extraColors.value = hsvColor.getColors(colorHarmonyMode = harmonyMode.value)
                        })
                }

            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCollectionChanged(current.value!!)
                    current.value = null
                }
            ) {
                Text(stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    current.value = null
                }
            ) {
                Text( stringResource(id = R.string.cancel))
            }
        }
    )

 }

@Preview(showBackground = true)
@Composable
fun CollectionsEditDialog_Preview() {
    JtxBoardTheme {

        val collection1 = CollectionsView(
            collectionId = 1L,
            color = Color.Cyan.toArgb(),
            displayName = "Collection Display Name",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )

        CollectionsAddOrEditDialog(
            remember { mutableStateOf(collection1) },
            onCollectionChanged = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CollectionsEditDialog_Preview2() {
    JtxBoardTheme {

        val collection1 = CollectionsView(
            collectionId = 0L,
            color = null,
            displayName = "Collection Display Name",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )

        CollectionsAddOrEditDialog(
            remember { mutableStateOf(collection1) },
            onCollectionChanged = { }
        )
    }
}