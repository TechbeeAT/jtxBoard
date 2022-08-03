/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.screens

import android.Manifest
import android.media.MediaPlayer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.compose.cards.PropertyCardContact
import at.techbee.jtx.ui.compose.cards.PropertyCardUrl
import at.techbee.jtx.ui.compose.dialogs.RequestContactsPermissionDialog
import at.techbee.jtx.ui.compose.elements.*
import at.techbee.jtx.ui.compose.stateholder.GlobalStateHolder
import at.techbee.jtx.ui.theme.JtxBoardTheme


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailScreenContent(
    iCalEntity: State<ICalEntity>,
    isEditMode: MutableState<Boolean>,
    subtasks: List<ICal4List>,
    subnotes: List<ICal4List>,
    attachments: List<Attachment>,
    allCollections: List<ICalCollection>,
    modifier: Modifier = Modifier,
    //player: MediaPlayer?,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit,
    onExpandedChanged: (itemId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isAttachmentsExpanded: Boolean) -> Unit
) {

    val context = LocalContext.current
    // Read contacts permission
    val readContactsGrantedText = stringResource(id = R.string.permission_read_contacts_granted)
    val readContactsDeniedText = stringResource(id = R.string.permission_read_contacts_denied)
    var permissionsDialogShownOnce by rememberSaveable { mutableStateOf(true) }  // TODO: Set to false for release!

    var summary by remember { mutableStateOf(iCalEntity.value.property.summary ?: "") }
    var description by remember { mutableStateOf(iCalEntity.value.property.description ?: "") }
    var contact = remember { mutableStateOf(iCalEntity.value.property.contact ?: "") }
    val url = remember { mutableStateOf(iCalEntity.value.property.url ?: "") }
    var status by remember { mutableStateOf(iCalEntity.value.property.status) }
    var classification by remember { mutableStateOf(iCalEntity.value.property.classification) }
    var priority by remember { mutableStateOf(iCalEntity.value.property.priority ?: 0) }
    var categories by remember { mutableStateOf(iCalEntity.value.categories ?: emptyList()) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, readContactsGrantedText, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, readContactsDeniedText, Toast.LENGTH_LONG).show()
        }
    }
    if(!permissionsDialogShownOnce) {
        RequestContactsPermissionDialog(
            onConfirm = {
                launcher.launch(Manifest.permission.READ_CONTACTS)
                permissionsDialogShownOnce = true
            },
            onDismiss = { permissionsDialogShownOnce = true }
        )
    }

    /*
    var markwon = Markwon.builder(LocalContext.current)
        .usePlugin(StrikethroughPlugin.create())
        .build()
     */

    Box {

        ColoredEdge(iCalEntity.value.property.color, iCalEntity.value.ICalCollection?.color)


        Column(modifier = Modifier.fillMaxWidth()) {


            AnimatedVisibility(!isEditMode.value) {

                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ElevatedCard(
                        modifier = Modifier.weight(1f)
                    ) {

                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Folder, stringResource(id = R.string.collection))
                            Text(iCalEntity.value.ICalCollection?.displayName + iCalEntity.value.ICalCollection?.accountName?.let { " (" + it + ")" })
                        }
                    }

                    if (iCalEntity.value.property.dirty && iCalEntity.value.ICalCollection?.accountType != LOCAL_ACCOUNT_TYPE) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_readonly),
                            contentDescription = stringResource(id = R.string.readyonly),
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                    if (iCalEntity.value.ICalCollection?.readonly == true) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_readonly),
                            contentDescription = stringResource(id = R.string.readyonly),
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                }
            }

            AnimatedVisibility(isEditMode.value) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 8.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    CollectionsSpinner(
                        collections = allCollections,
                        preselected = iCalEntity.value.ICalCollection
                            ?: allCollections.first(),   // TODO: Load last used collection for new entries
                        includeReadOnly = false,
                        includeVJOURNAL = false,
                        includeVTODO = false,
                        onSelectionChanged = { /* TODO */ },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Outlined.ColorLens, stringResource(id = R.string.color))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 8.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(iCalEntity.value.property.module == Module.JOURNAL.name && iCalEntity.value.property.dtstart != null)
                    VerticalDateCard(datetime = iCalEntity.value.property.dtstart, timezone = iCalEntity.value.property.dtstartTimezone)
            }

            AnimatedVisibility(!isEditMode.value) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    if (summary.isNotBlank())
                        Text(
                            summary,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            //fontWeight = FontWeight.Bold
                        )
                    if (description.isNotBlank())
                        Text(
                            description,
                            modifier = Modifier.padding(8.dp)
                        )
                }
            }

            AnimatedVisibility(isEditMode.value) {

                ElevatedCard(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)) {

                    OutlinedTextField(
                        value = summary,
                        onValueChange = {
                            summary = it
                        },
                        label = { Text(stringResource(id = R.string.summary)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                        },
                        label = { Text(stringResource(id = R.string.description)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }

            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 8.dp, end = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    label = {
                        if (iCalEntity.value.property.component == Component.VJOURNAL.name)
                            Text(StatusJournal.getStringResource(context, status) ?: status ?: "")
                        else
                            Text(StatusTodo.getStringResource(context, status) ?: status ?: "")
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.PublishedWithChanges,
                            stringResource(id = R.string.status)
                        )
                    },
                    onClick = {
                        if (iCalEntity.value.property.component == Component.VJOURNAL.name) {
                            status = try {
                                StatusJournal.getNext(StatusJournal.valueOf(status ?: "")).name
                            } catch (e: IllegalArgumentException) {
                                StatusJournal.getNext(null).name
                            }
                        } else {
                            status = try {
                                StatusTodo.getNext(StatusTodo.valueOf(status ?: "")).name
                            } catch (e: IllegalArgumentException) {
                                StatusTodo.getNext(null).name
                            }
                        }
                    }
                )

                AssistChip(
                    label = {
                        Text(
                            Classification.getStringResource(context, classification)
                                ?: classification ?: ""
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.GppMaybe,
                            stringResource(id = R.string.classification)
                        )
                    },
                    onClick = {
                        classification = try {
                            Classification.getNext(
                                Classification.valueOf(
                                    classification ?: ""
                                )
                            ).name
                        } catch (e: IllegalArgumentException) {
                            Classification.getNext(null).name
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            if(iCalEntity.value.property.component == Component.VTODO.name) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 8.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        value = priority.toFloat(),
                        onValueChange = { priority = it.toInt() },
                        valueRange = 0f..9f,
                        steps = 9,
                        modifier = Modifier.width(200.dp)
                    )
                    Text(
                        stringArrayResource(id = R.array.priority)[priority],
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }


            /*
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 8.dp, end = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElevatedButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Outlined.NewLabel, stringResource(id = R.string.categories))
                }
                ElevatedButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Outlined.PersonAdd, stringResource(id = R.string.attendees))
                }
                ElevatedButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Outlined.MedicalServices, stringResource(id = R.string.resources))
                }
                ElevatedButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Outlined.ContactMail, stringResource(id = R.string.contact))
                }
                ElevatedButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Outlined.EditLocation, stringResource(id = R.string.location))
                }
                ElevatedButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Outlined.AddLink, stringResource(id = R.string.url))
                }
                ElevatedButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Outlined.AddTask, stringResource(id = R.string.subtasks))
                }
                ElevatedButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Outlined.Attachment, stringResource(id = R.string.attachments))
                }
                ElevatedButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Outlined.Repeat, stringResource(id = R.string.recurrence))
                }
                ElevatedButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Outlined.NotificationAdd, stringResource(id = R.string.alarms))
                }
                ElevatedButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Outlined.AddComment, stringResource(id = R.string.comments))
                }
            }

             */


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 8.dp, end = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                categories.forEach { category ->
                    InputChip(
                        onClick = {
                            categories = categories.filter { it != category }
                        },
                        label = { Text(category.text) },
                        leadingIcon = { Icon(Icons.Outlined.Label, stringResource(id = R.string.categories)) },
                        trailingIcon = { Icon(Icons.Outlined.Close, stringResource(id = R.string.delete)) }
                    )
                }
            }

            AnimatedVisibility(contact.value.isNotBlank() || isEditMode.value) {
                PropertyCardContact(
                    contact = contact,
                    isEditMode = isEditMode,
                    onContactUpdated = { /*TODO*/ },
                    modifier = Modifier.padding(8.dp)
                )
            }

            AnimatedVisibility(url.value.isNotEmpty() || isEditMode.value) {
                PropertyCardUrl(
                    url = url,
                    isEditMode = isEditMode,
                    onUrlUpdated = { /*TODO*/ },
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenContent_JOURNAL() {
    MaterialTheme {
        val entity = ICalEntity().apply {
            this.property = ICalObject.createJournal("MySummary")
            //this.property.dtstart = System.currentTimeMillis()
        }
        entity.property.description = "Hello World, this \nis my description."
        entity.property.contact = "John Doe, +1 555 5545"
        entity.categories = listOf(
            Category(1,1,"MyCategory1", null, null),
            Category(2,1,"My Dog likes Cats", null, null),
            Category(3,1,"This is a very long category", null, null),
        )

        DetailScreenContent(
            iCalEntity = mutableStateOf(entity),
            isEditMode = mutableStateOf(false),
            subtasks = emptyList(),
            subnotes = emptyList(),
            attachments = emptyList(),
            allCollections = listOf(ICalCollection.createLocalCollection(LocalContext.current)),
            //player = null,
            onProgressChanged = { _, _, _ -> },
            onExpandedChanged = { _, _, _, _ -> }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailScreenContent_TODO_editInitially() {
    MaterialTheme {
        val entity = ICalEntity().apply {
            this.property = ICalObject.createTask("MySummary")

            //this.property.dtstart = System.currentTimeMillis()
        }
        entity.property.description = "Hello World, this \nis my description."
        entity.property.contact = "John Doe, +1 555 5545"

        DetailScreenContent(
            iCalEntity = mutableStateOf(entity),
            isEditMode = mutableStateOf(true),
            subtasks = emptyList(),
            subnotes = emptyList(),
            attachments = emptyList(),
            allCollections = listOf(ICalCollection.createLocalCollection(LocalContext.current)),
            //player = null,
            onProgressChanged = { _, _, _ -> },
            onExpandedChanged = { _, _, _, _ -> }
        )
    }
}

