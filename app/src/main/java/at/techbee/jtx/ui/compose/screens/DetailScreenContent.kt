/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.screens

import android.content.Context
import android.content.SharedPreferences
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
import at.techbee.jtx.DetailSettings
import at.techbee.jtx.R
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.DetailViewModel
import at.techbee.jtx.ui.compose.cards.*
import at.techbee.jtx.ui.compose.dialogs.ColorPickerDialog
import at.techbee.jtx.ui.compose.dialogs.MoveItemToCollectionDialog
import at.techbee.jtx.ui.compose.elements.CollectionsSpinner
import at.techbee.jtx.ui.compose.elements.ColoredEdge
import at.techbee.jtx.ui.compose.elements.ProgressElement
import kotlinx.coroutines.delay
import java.time.Duration


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreenContent(
    iCalEntity: State<ICalEntity?>,
    isEditMode: MutableState<Boolean>,
    contentsChanged: MutableState<Boolean?>,
    subtasks: State<List<ICal4List>>,
    subnotes: State<List<ICal4List>>,
    allCollections: List<ICalCollection>,
    allCategories: List<String>,
    allResources: List<String>,
    detailSettings: DetailSettings,
    modifier: Modifier = Modifier,
    player: MediaPlayer?,
    saveIcalObject: (changedICalObject: ICalObject, changedCategories: List<Category>, changedComments: List<Comment>, changedAttendees: List<Attendee>, changedResources: List<Resource>, changedAttachments: List<Attachment>, changedAlarms: List<Alarm>) -> Unit,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit,
    onMoveToNewCollection: (icalObject: ICalObject, newCollection: ICalCollection) -> Unit,
    onSubEntryAdded: (icalObject: ICalObject, attachment: Attachment?) -> Unit,
    onSubEntryDeleted: (icalObjectId: Long) -> Unit,
    onSubEntryUpdated: (icalObjectId: Long, newText: String) -> Unit
) {
    if (iCalEntity.value == null)
        return

    var color by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.color) }
    var summary by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.summary ?: "") }
    var description by rememberSaveable {
        mutableStateOf(
            iCalEntity.value?.property?.description ?: ""
        )
    }

    val icalObject by rememberSaveable {
        mutableStateOf(
            iCalEntity.value?.property ?: ICalObject()
        )
    }
    val categories =
        rememberSaveable { mutableStateOf(iCalEntity.value?.categories ?: emptyList()) }
    val resources = rememberSaveable { mutableStateOf(iCalEntity.value?.resources ?: emptyList()) }
    val attendees = rememberSaveable { mutableStateOf(iCalEntity.value?.attendees ?: emptyList()) }
    val comments = rememberSaveable { mutableStateOf(iCalEntity.value?.comments ?: emptyList()) }
    val attachments =
        rememberSaveable { mutableStateOf(iCalEntity.value?.attachments ?: emptyList()) }
    val alarms = rememberSaveable { mutableStateOf(iCalEntity.value?.alarms ?: emptyList()) }

    var showColorPicker by rememberSaveable { mutableStateOf(false) }
    var showMoveItemToCollectionDialog by rememberSaveable { mutableStateOf<ICalCollection?>(null) }

    val previousIsEditModeState = rememberSaveable { mutableStateOf(isEditMode.value) }
    if (previousIsEditModeState.value && !isEditMode.value)  //changed from edit to view mode
        saveIcalObject(
            icalObject,
            categories.value,
            comments.value,
            attendees.value,
            resources.value,
            attachments.value,
            alarms.value
        )
    previousIsEditModeState.value = isEditMode.value

    // save 10 seconds after changed, then reset value
    val autosave = true //TODO put in settings
    if (contentsChanged.value == true && autosave) {
        LaunchedEffect(contentsChanged) {
            delay(Duration.ofSeconds(10).toMillis())
            saveIcalObject(
                icalObject,
                categories.value,
                comments.value,
                attendees.value,
                resources.value,
                attachments.value,
                alarms.value
            )
            contentsChanged.value = false
            delay(Duration.ofSeconds(2).toMillis())  // reset after a second
            contentsChanged.value = null
        }
    }

    showMoveItemToCollectionDialog?.let {
        MoveItemToCollectionDialog(
            newCollection = it,
            onMoveConfirmed = {
                saveIcalObject(
                    icalObject,
                    categories.value,
                    comments.value,
                    attendees.value,
                    resources.value,
                    attachments.value,
                    alarms.value
                )
                onMoveToNewCollection(icalObject, it)
            },
            onDismiss = { showMoveItemToCollectionDialog = null }
        )
    }

    /*
    var markwon = Markwon.builder(LocalContext.current)
        .usePlugin(StrikethroughPlugin.create())
        .build()
     */
    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = color,
            onColorChanged = { newColor ->
                color = newColor
                icalObject.color = newColor
                contentsChanged.value = true
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
                            includeVJOURNAL = if (iCalEntity.value?.property?.component == Component.VJOURNAL.name) true else null,
                            includeVTODO = if (iCalEntity.value?.property?.component == Component.VTODO.name) true else null,
                            onSelectionChanged = { newCollection ->
                                if (icalObject.collectionId != newCollection.collectionId)
                                    showMoveItemToCollectionDialog = newCollection
                            },
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
                    contentsChanged.value = true
                },
                onDueChanged = { datetime, timezone ->
                    icalObject.due = datetime
                    icalObject.dueTimezone = timezone
                    contentsChanged.value = true
                },
                onCompletedChanged = { datetime, timezone ->
                    icalObject.completed = datetime
                    icalObject.completedTimezone = timezone
                    contentsChanged.value = true
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
                            contentsChanged.value = true
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
                            contentsChanged.value = true
                        },
                        label = { Text(stringResource(id = R.string.description)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }

            if (icalObject.module == Module.TODO.name) {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    ProgressElement(
                        iCalObjectId = icalObject.id,
                        progress = icalObject.percent,
                        isReadOnly = !isEditMode.value,
                        isLinkedRecurringInstance = icalObject.isRecurLinkedInstance,
                        sliderIncrement = 1,   // TODO
                        onProgressChanged = { itemId, newPercent, isLinked ->
                            onProgressChanged(itemId, newPercent, isLinked)
                            contentsChanged.value = true
                        },
                        showProgressLabel = true,
                        showSlider = true
                    )
                }
            }


            DetailsCardStatusClassificationPriority(
                icalObject = icalObject,
                isEditMode = isEditMode.value,
                onStatusChanged = { newStatus ->
                    icalObject.status = newStatus
                    contentsChanged.value = true
                },
                onClassificationChanged = { newClassification ->
                    icalObject.classification = newClassification
                    contentsChanged.value = true
                },
                onPriorityChanged = { newPriority ->
                    icalObject.priority = newPriority
                    contentsChanged.value = true
                },
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(subtasks.value.isNotEmpty() || (isEditMode.value && iCalEntity.value?.ICalCollection?.supportsVTODO == true && detailSettings.enableSubtasks.value)) {
                DetailsCardSubtasks(
                    subtasks = subtasks.value,
                    isEditMode = isEditMode,
                    onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance ->
                        onProgressChanged(itemId, newPercent, isLinkedRecurringInstance)
                    },
                    onSubtaskAdded = { subtask -> onSubEntryAdded(subtask, null) },
                    onSubtaskUpdated = { icalObjectId, newText ->
                        onSubEntryUpdated(
                            icalObjectId,
                            newText
                        )
                    },
                    onSubtaskDeleted = { icalObjectId -> onSubEntryDeleted(icalObjectId) }
                )
            }

            AnimatedVisibility(subnotes.value.isNotEmpty() || (isEditMode.value && iCalEntity.value?.ICalCollection?.supportsVJOURNAL == true && detailSettings.enableSubnotes.value)) {
                DetailsCardSubnotes(
                    subnotes = subnotes.value,
                    isEditMode = isEditMode,
                    onSubnoteAdded = { subnote, attachment ->
                        onSubEntryAdded(
                            subnote,
                            attachment
                        )
                    },
                    onSubnoteUpdated = { icalObjectId, newText ->
                        onSubEntryUpdated(
                            icalObjectId,
                            newText
                        )
                    },
                    onSubnoteDeleted = { icalObjectId -> onSubEntryDeleted(icalObjectId) },
                    player = player
                )
            }


            AnimatedVisibility(categories.value.isNotEmpty() || (isEditMode.value && detailSettings.enableCategories.value)) {
                DetailsCardCategories(
                    initialCategories = categories.value,
                    isEditMode = isEditMode.value,
                    onCategoriesUpdated = { newCategories ->
                        categories.value = newCategories
                        contentsChanged.value = true
                    },
                    allCategories = allCategories,
                )
            }

            AnimatedVisibility(resources.value.isNotEmpty() || (isEditMode.value && detailSettings.enableResources.value)) {
                DetailsCardResources(
                    initialResources = resources.value,
                    isEditMode = isEditMode.value,
                    onResourcesUpdated = { newResources ->
                        resources.value = newResources
                        contentsChanged.value = true
                    },
                    allResources = allResources,
                )
            }


            AnimatedVisibility(attendees.value.isNotEmpty() || (isEditMode.value && detailSettings.enableAttendees.value)) {
                DetailsCardAttendees(
                    initialAttendees = attendees.value,
                    isEditMode = isEditMode.value,
                    onAttendeesUpdated = { newAttendees ->
                        attendees.value = newAttendees
                        contentsChanged.value = true
                    }
                )
            }

            AnimatedVisibility(icalObject.contact?.isNotBlank() == true || (isEditMode.value && detailSettings.enableContact.value)) {
                DetailsCardContact(
                    initialContact = icalObject.contact ?: "",
                    isEditMode = isEditMode.value,
                    onContactUpdated = { newContact ->
                        icalObject.contact = newContact.ifEmpty { null }
                        contentsChanged.value = true
                    },
                )
            }


            AnimatedVisibility(icalObject.url?.isNotEmpty() == true || (isEditMode.value && detailSettings.enableUrl.value)) {
                DetailsCardUrl(
                    initialUrl = icalObject.url ?: "",
                    isEditMode = isEditMode.value,
                    onUrlUpdated = { newUrl ->
                        icalObject.url = newUrl.ifEmpty { null }
                        contentsChanged.value = true
                    },
                )
            }

            AnimatedVisibility((icalObject.location?.isNotEmpty() == true || (icalObject.geoLat != null && icalObject.geoLong != null)) || (isEditMode.value && detailSettings.enableLocation.value)) {
                DetailsCardLocation(
                    initialLocation = icalObject.location,
                    initialGeoLat = icalObject.geoLat,
                    initialGeoLong = icalObject.geoLong,
                    isEditMode = isEditMode.value,
                    onLocationUpdated = { newLocation, newGeoLat, newGeoLong ->
                        icalObject.geoLat = newGeoLat
                        icalObject.geoLong = newGeoLong
                        icalObject.location = newLocation.ifEmpty { null }
                        contentsChanged.value = true
                    },
                )
            }

            AnimatedVisibility(comments.value.isNotEmpty() || (isEditMode.value && detailSettings.enableComments.value)) {
                DetailsCardComments(
                    initialComments = comments.value,
                    isEditMode = isEditMode.value,
                    onCommentsUpdated = { newComments ->
                        comments.value = newComments
                    }
                )
            }


            AnimatedVisibility(attachments.value.isNotEmpty() || (isEditMode.value && detailSettings.enableAttachments.value)) {
                DetailsCardAttachments(
                    initialAttachments = attachments.value,
                    isEditMode = isEditMode.value,
                    onAttachmentsUpdated = { newAttachments ->
                        attachments.value = newAttachments
                    }
                )
            }

            AnimatedVisibility(alarms.value.isNotEmpty() || (isEditMode.value && detailSettings.enableAlarms.value)) {
                DetailsCardAlarms(
                    initialAlarms = alarms.value,
                    icalObject = icalObject,
                    isEditMode = isEditMode.value,
                    onAlarmsUpdated = { newAlarms ->
                        alarms.value = newAlarms
                    })
            }

            AnimatedVisibility(icalObject.rrule != null || (isEditMode.value && detailSettings.enableRecurrence.value)) {   // only Todos have recur!
                DetailsCardRecur(
                    icalObject = icalObject,
                    isEditMode = isEditMode.value,
                    onRecurUpdated = { updatedRRule ->
                        icalObject.rrule = updatedRRule?.toString()
                    })
            }

            AnimatedVisibility(
                isEditMode.value
                        && !(detailSettings.enableRecurrence.value
                            && detailSettings.enableLocation.value
                            && detailSettings.enableComments.value
                            && detailSettings.enableAttachments.value
                            && detailSettings.enableUrl.value
                            && detailSettings.enableAttendees.value
                            && detailSettings.enableContact.value
                            && detailSettings.enableResources.value
                            && detailSettings.enableCategories.value
                            && detailSettings.enableSubnotes.value
                            && detailSettings.enableSubtasks.value
                            && detailSettings.enableAlarms.value
                        )
            ) {
                TextButton(
                    onClick = {
                        detailSettings.enableRecurrence.value = true
                        detailSettings.enableLocation.value = true
                        detailSettings.enableComments.value = true
                        detailSettings.enableAttachments.value = true
                        detailSettings.enableUrl.value = true
                        detailSettings.enableAttendees.value = true
                        detailSettings.enableContact.value = true
                        detailSettings.enableResources.value = true
                        detailSettings.enableCategories.value = true
                        detailSettings.enableSubnotes.value = true
                        detailSettings.enableSubtasks.value = true
                        detailSettings.enableAlarms.value = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.details_show_all_options))
                }
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

        val prefs: SharedPreferences = LocalContext.current.getSharedPreferences(
            DetailViewModel.PREFS_DETAIL_JOURNALS,
            Context.MODE_PRIVATE
        )
        val detailSettings = DetailSettings(prefs)

        DetailScreenContent(
            iCalEntity = remember { mutableStateOf(entity) },
            isEditMode = remember { mutableStateOf(false) },
            contentsChanged = remember { mutableStateOf(false) },
            subtasks = remember { mutableStateOf(emptyList()) },
            subnotes = remember { mutableStateOf(emptyList()) },
            player = null,
            allCollections = listOf(ICalCollection.createLocalCollection(LocalContext.current)),
            allCategories = emptyList(),
            allResources = emptyList(),
            detailSettings = detailSettings,
            saveIcalObject = { _, _, _, _, _, _, _ -> },
            onProgressChanged = { _, _, _ -> },
            onMoveToNewCollection = { _, _ -> },
            onSubEntryAdded = { _, _ -> },
            onSubEntryDeleted = { },
            onSubEntryUpdated = { _, _ -> },
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

        val prefs: SharedPreferences = LocalContext.current.getSharedPreferences(
            DetailViewModel.PREFS_DETAIL_TODOS,
            Context.MODE_PRIVATE
        )
        val detailSettings = DetailSettings(prefs)

        DetailScreenContent(
            iCalEntity = remember { mutableStateOf(entity) },
            isEditMode = remember { mutableStateOf(true) },
            contentsChanged = remember { mutableStateOf(false) },
            subtasks = remember { mutableStateOf(emptyList()) },
            subnotes = remember { mutableStateOf(emptyList()) },
            player = null,
            allCollections = listOf(ICalCollection.createLocalCollection(LocalContext.current)),
            allCategories = emptyList(),
            allResources = emptyList(),
            detailSettings = detailSettings,
            saveIcalObject = { _, _, _, _, _, _, _ -> },
            onProgressChanged = { _, _, _ -> },
            onMoveToNewCollection = { _, _ -> },
            onSubEntryAdded = { _, _ -> },
            onSubEntryDeleted = { },
            onSubEntryUpdated = { _, _ -> },
        )
    }
}

