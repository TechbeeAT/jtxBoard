/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.screens

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.compose.cards.*
import at.techbee.jtx.ui.compose.dialogs.ColorPickerDialog
import at.techbee.jtx.ui.compose.elements.CollectionsSpinner
import at.techbee.jtx.ui.compose.elements.ColoredEdge


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreenContent(
    iCalEntity: State<ICalEntity?>,
    isEditMode: MutableState<Boolean>,
    subtasks: State<List<ICal4List>>,
    subnotes: State<List<ICal4List>>,
    allCollections: List<ICalCollection>,
    modifier: Modifier = Modifier,
    player: MediaPlayer?,
    saveIcalObject: (changedICalObject: ICalObject, changedCategories: List<Category>, changedComments: List<Comment>, changedAttendees: List<Attendee>, changedResources: List<Resource>, changedAttachments: List<Attachment>, changedAlarms: List<Alarm>) -> Unit,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit,
    onSubEntryAdded: (icalObject: ICalObject, attachment: Attachment?) -> Unit,
    onSubEntryDeleted: (icalObjectId: Long) -> Unit,
    onSubEntryUpdated: (icalObjectId: Long, newText: String) -> Unit
) {
    if(iCalEntity.value == null)
        return


    var color by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.color) }
    var summary by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.summary ?: "") }
    var description by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.description ?: "") }

    val contact = rememberSaveable { mutableStateOf(iCalEntity.value?.property?.contact ?: "") }
    val url = rememberSaveable { mutableStateOf(iCalEntity.value?.property?.url ?: "") }
    val location = rememberSaveable { mutableStateOf(iCalEntity.value?.property?.location ?: "") }
    val geoLat = rememberSaveable { mutableStateOf(iCalEntity.value?.property?.geoLat) }
    val geoLong = rememberSaveable { mutableStateOf(iCalEntity.value?.property?.geoLong) }
    var rrule by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.rrule) }

    val icalObject by rememberSaveable { mutableStateOf(iCalEntity.value?.property ?: ICalObject()) }
    val categories = rememberSaveable { mutableStateOf(iCalEntity.value?.categories ?: emptyList()) }
    val resources = rememberSaveable { mutableStateOf(iCalEntity.value?.resources ?: emptyList()) }
    val attendees = rememberSaveable { mutableStateOf(iCalEntity.value?.attendees ?: emptyList()) }
    val comments = rememberSaveable { mutableStateOf(iCalEntity.value?.comments ?: emptyList()) }
    val attachments = rememberSaveable { mutableStateOf(iCalEntity.value?.attachments ?: emptyList()) }
    val alarms = rememberSaveable { mutableStateOf(iCalEntity.value?.alarms ?: emptyList()) }

    var showColorPicker by rememberSaveable { mutableStateOf(false) }
    val previousIsEditModeState = rememberSaveable { mutableStateOf(isEditMode.value) }
    if(previousIsEditModeState.value && !isEditMode.value)  //changed from edit to view mode
        saveIcalObject(icalObject, categories.value, comments.value, attendees.value, resources.value, attachments.value, alarms.value)
    previousIsEditModeState.value = isEditMode.value


    /*
    var markwon = Markwon.builder(LocalContext.current)
        .usePlugin(StrikethroughPlugin.create())
        .build()
     */
    if(showColorPicker) {
        ColorPickerDialog(
            initialColor = color,
            onColorChanged = { newColor ->
                color = newColor
                icalObject.color = newColor
            },
            onDismiss = {
                showColorPicker = false
            }
        )
    }

    Box(modifier = modifier.verticalScroll(rememberScrollState())) {

        ColoredEdge(color, iCalEntity.value?.ICalCollection?.color)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            AnimatedVisibility(!isEditMode.value) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            Text(iCalEntity.value?.ICalCollection?.displayName + iCalEntity.value?.ICalCollection?.accountName?.let { " (" + it + ")" })
                        }
                    }
                }
            }

            AnimatedVisibility(isEditMode.value) {

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        CollectionsSpinner(
                            collections = allCollections,
                            preselected = iCalEntity.value?.ICalCollection
                                ?: allCollections.first(),   // TODO: Load last used collection for new entries
                            includeReadOnly = false,
                            includeVJOURNAL = false,
                            includeVTODO = false,
                            onSelectionChanged = { /* TODO */ },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showColorPicker = true }) {
                            Icon(Icons.Outlined.ColorLens, stringResource(id = R.string.color))
                        }
                    }
                }
            }

            DetailsCardDates(
                icalObject = icalObject,
                isEditMode = isEditMode.value,
                onDtstartChanged = { datetime, timezone ->
                    icalObject.dtstart = datetime
                    icalObject.dtstartTimezone = timezone
                },
                onDueChanged = { datetime, timezone ->
                    icalObject.due = datetime
                    icalObject.dueTimezone = timezone
                },
                onCompletedChanged = { datetime, timezone ->
                    icalObject.completed = datetime
                    icalObject.completedTimezone = timezone
                },
            )

            AnimatedVisibility(!isEditMode.value) {
                SelectionContainer {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
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
            }

            AnimatedVisibility(isEditMode.value) {

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    OutlinedTextField(
                        value = summary,
                        onValueChange = {
                            summary = it
                            icalObject.summary = it.ifEmpty { null }
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
                            icalObject.description = it.ifEmpty { null }
                        },
                        label = { Text(stringResource(id = R.string.description)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }


            DetailsCardStatusClassificationPriority(
                icalObject = icalObject,
                isEditMode = isEditMode.value,
                onStatusChanged = { newStatus -> icalObject.status = newStatus },
                onClassificationChanged = { newClassification -> icalObject.classification = newClassification },
                onPriorityChanged = { newPriority -> icalObject.priority = newPriority },
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(subtasks.value.isNotEmpty() || isEditMode.value) {
                DetailsCardSubtasks(
                    subtasks = subtasks.value,
                    isEditMode = isEditMode,
                    onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance ->
                        onProgressChanged(itemId, newPercent, isLinkedRecurringInstance)
                    },
                    onSubtaskAdded = { subtask -> onSubEntryAdded(subtask, null) },
                    onSubtaskUpdated = { icalObjectId, newText -> onSubEntryUpdated(icalObjectId, newText) },
                    onSubtaskDeleted = { icalObjectId -> onSubEntryDeleted(icalObjectId) }
                )
            }

            AnimatedVisibility(subnotes.value.isNotEmpty() || isEditMode.value) {
                DetailsCardSubnotes(
                    subnotes = subnotes.value,
                    isEditMode = isEditMode,
                    onSubnoteAdded = { subnote, attachment -> onSubEntryAdded(subnote, attachment) },
                    onSubnoteUpdated = { icalObjectId, newText -> onSubEntryUpdated(icalObjectId, newText) },
                    onSubnoteDeleted = { icalObjectId -> onSubEntryDeleted(icalObjectId) },
                    player = player
                )
            }


            AnimatedVisibility(categories.value.isNotEmpty() || isEditMode.value) {
                DetailsCardCategories(
                    categories = categories,
                    isEditMode = isEditMode,
                    onCategoriesUpdated = { /*TODO*/ },
                    allCategories = listOf(
                        Category(text = "category1"),
                        Category(text = "category2"),
                        Category(text = "Whatever")
                    ),
                )
            }

            AnimatedVisibility(resources.value.isNotEmpty() || isEditMode.value) {
                DetailsCardResources(
                    resources = resources,
                    isEditMode = isEditMode,
                    onResourcesUpdated = { /*TODO*/ },
                    allResources = listOf(
                        Resource(text = "projector"),
                        Resource(text = "overhead-thingy"),
                        Resource(text = "Whatever")
                    ),
                )
            }


            AnimatedVisibility(attendees.value.isNotEmpty() || isEditMode.value) {
                DetailsCardAttendees(
                    attendees = attendees,
                    isEditMode = isEditMode,
                    onAttendeesUpdated = { /*TODO*/ }
                )
            }

            AnimatedVisibility(contact.value.isNotBlank() || isEditMode.value) {
                DetailsCardContact(
                    contact = contact,
                    isEditMode = isEditMode,
                    onContactUpdated = { icalObject.contact = contact.value.ifEmpty { null } },
                )
            }


            AnimatedVisibility(url.value.isNotEmpty() || isEditMode.value) {
                DetailsCardUrl(
                    url = url,
                    isEditMode = isEditMode,
                    onUrlUpdated = { icalObject.url = url.value.ifEmpty { null } },
                )
            }

            AnimatedVisibility((location.value.isNotEmpty() || (geoLat.value != null && geoLong.value != null)) || isEditMode.value) {
                DetailsCardLocation(
                    location = location,
                    geoLat = geoLat,
                    geoLong = geoLong,
                    isEditMode = isEditMode,
                    onLocationUpdated = {
                        icalObject.geoLat = geoLat.value
                        icalObject.geoLong = geoLong.value
                        icalObject.location = location.value
                    },
                )
            }

            AnimatedVisibility(comments.value.isNotEmpty() || isEditMode.value) {
                DetailsCardComments(
                    comments = comments,
                    isEditMode = isEditMode,
                    onCommentsUpdated = { /*TODO*/ }
                )
            }


            AnimatedVisibility(attachments.value.isNotEmpty() || isEditMode.value) {
                DetailsCardAttachments(
                    attachments = attachments,
                    isEditMode = isEditMode,
                    onAttachmentsUpdated = { /*TODO*/ }
                )
            }

            AnimatedVisibility(alarms.value.isNotEmpty() || isEditMode.value) {
                DetailsCardAlarms(
                    alarms = alarms,
                    icalObject = iCalEntity.value?.property!!,
                    isEditMode = isEditMode,
                    onAlarmsUpdated = { /*TODO*/ })
            }

            AnimatedVisibility(rrule != null || isEditMode.value) {
                DetailsCardRecur(
                    icalObject = iCalEntity.value?.property!!,
                    isEditMode = isEditMode,
                    onRecurUpdated = { /*TODO*/ })
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
            Category(1, 1, "MyCategory1", null, null),
            Category(2, 1, "My Dog likes Cats", null, null),
            Category(3, 1, "This is a very long category", null, null),
        )

        DetailScreenContent(
            iCalEntity = remember { mutableStateOf(entity) },
            isEditMode = remember { mutableStateOf(false) },
            subtasks = remember { mutableStateOf(emptyList()) },
            subnotes = remember { mutableStateOf(emptyList()) },
            player = null,
            allCollections = listOf(ICalCollection.createLocalCollection(LocalContext.current)),
            saveIcalObject = { _, _, _, _, _, _, _ ->   },
            onProgressChanged = { _, _, _ -> },
            onSubEntryAdded = { _, _ -> },
            onSubEntryDeleted = { },
            onSubEntryUpdated = { _, _ -> }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailScreenContent_TODO_editInitially() {
    MaterialTheme {
        val entity = ICalEntity().apply {
            this.property = ICalObject.createTask("MySummary")
        }
        entity.property.description = "Hello World, this \nis my description."
        entity.property.contact = "John Doe, +1 555 5545"

        DetailScreenContent(
            iCalEntity = remember { mutableStateOf(entity) },
            isEditMode = remember { mutableStateOf(true) },
            subtasks = remember { mutableStateOf(emptyList()) },
            subnotes = remember { mutableStateOf(emptyList()) },
            player = null,
            allCollections = listOf(ICalCollection.createLocalCollection(LocalContext.current)),
            saveIcalObject = { _, _, _, _, _, _, _ ->   },
            onProgressChanged = { _, _, _ -> },
            onSubEntryAdded = { _, _ ->  },
            onSubEntryDeleted = { },
            onSubEntryUpdated = { _, _ -> }
        )
    }
}

