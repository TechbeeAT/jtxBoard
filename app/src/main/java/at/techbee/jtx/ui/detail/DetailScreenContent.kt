/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.DetailSettings
import at.techbee.jtx.R
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.reusable.dialogs.ColorPickerDialog
import at.techbee.jtx.ui.reusable.dialogs.UnsavedChangesDialog
import at.techbee.jtx.ui.reusable.elements.CollectionsSpinner
import at.techbee.jtx.ui.reusable.elements.ColoredEdge
import at.techbee.jtx.ui.reusable.elements.ProgressElement
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.util.DateTimeUtils
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreenContent(
    iCalEntity: State<ICalEntity?>,
    isEditMode: MutableState<Boolean>,
    changeState: MutableState<DetailViewModel.DetailChangeState>,
    subtasks: State<List<ICal4List>>,
    subnotes: State<List<ICal4List>>,
    isChild: Boolean,
    allCollections: List<ICalCollection>,
    allCategories: List<String>,
    allResources: List<String>,
    detailSettings: DetailSettings,
    modifier: Modifier = Modifier,
    player: MediaPlayer?,
    autosave: Boolean,
    goBackRequested: Boolean,    // Workaround to also go Back from Top menu
    saveICalObject: (changedICalObject: ICalObject, changedCategories: List<Category>, changedComments: List<Comment>, changedAttendees: List<Attendee>, changedResources: List<Resource>, changedAttachments: List<Attachment>, changedAlarms: List<Alarm>) -> Unit,
    deleteICalObject: () -> Unit,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit,
    onMoveToNewCollection: (icalObject: ICalObject, newCollection: ICalCollection) -> Unit,
    onSubEntryAdded: (icalObject: ICalObject, attachment: Attachment?) -> Unit,
    onSubEntryDeleted: (icalObjectId: Long) -> Unit,
    onSubEntryUpdated: (icalObjectId: Long, newText: String) -> Unit,
    goToView: (itemId: Long) -> Unit,
    goToEdit: (itemId: Long) -> Unit,
    goBack: () -> Unit
) {

    val context = LocalContext.current
    val localInspectionMode = LocalInspectionMode.current
    val isMarkdownEnabled by remember {
        if(!localInspectionMode)
            SettingsStateHolder(context).settingEnableMarkdownFormattting
        else
            mutableStateOf(false)
    }

    // item was not loaded yet or was deleted in the background
    if (iCalEntity.value == null) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(stringResource(id = R.string.sorry), style = MaterialTheme.typography.displayMedium)
            Text(stringResource(id = R.string.details_entry_could_not_be_loaded), textAlign = TextAlign.Center)
            Button(onClick = { goBack() }) {
                Text(stringResource(id = R.string.back))
            }
        }
        return
    }

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
    iCalEntity.value?.property?.eTag?.let { icalObject.eTag = it }    // make sure the eTag gets updated in the background if the sync is triggered, so that another sync won't overwrite the changes!
    val categories =
        rememberSaveable { mutableStateOf(iCalEntity.value?.categories ?: emptyList()) }
    val resources = rememberSaveable { mutableStateOf(iCalEntity.value?.resources ?: emptyList()) }
    val attendees = rememberSaveable { mutableStateOf(iCalEntity.value?.attendees ?: emptyList()) }
    val comments = rememberSaveable { mutableStateOf(iCalEntity.value?.comments ?: emptyList()) }
    val attachments =
        rememberSaveable { mutableStateOf(iCalEntity.value?.attachments ?: emptyList()) }
    val alarms = rememberSaveable { mutableStateOf(iCalEntity.value?.alarms ?: emptyList()) }

    var showColorPicker by rememberSaveable { mutableStateOf(false) }
    var showUnsavedChangesDialog by rememberSaveable { mutableStateOf(false) }
    var showAllOptions by rememberSaveable { mutableStateOf(false) }


    val previousIsEditModeState = rememberSaveable { mutableStateOf(isEditMode.value) }
    if (previousIsEditModeState.value && !isEditMode.value) {  //changed from edit to view mode
        saveICalObject(
            icalObject,
            categories.value,
            comments.value,
            attendees.value,
            resources.value,
            attachments.value,
            alarms.value
        )
    }
    previousIsEditModeState.value = isEditMode.value


    // save 10 seconds after changed, then reset value
    if (changeState.value == DetailViewModel.DetailChangeState.CHANGEUNSAVED && autosave) {
        LaunchedEffect(changeState) {
            delay((10).seconds.inWholeMilliseconds)
            saveICalObject(
                icalObject,
                categories.value,
                comments.value,
                attendees.value,
                resources.value,
                attachments.value,
                alarms.value
            )
        }
    }

    fun processGoBack() {
        if(changeState.value == DetailViewModel.DetailChangeState.CHANGEUNSAVED)
            showUnsavedChangesDialog = true
        else if(changeState.value == DetailViewModel.DetailChangeState.CHANGEUNSAVED && icalObject.sequence == 0L && icalObject.eTag == null && icalObject.summary.isNullOrEmpty() && icalObject.description.isNullOrEmpty())
            deleteICalObject()
        else
            goBack()
    }

    /**
     * Updates the alarms when the dates get changed
     */
    fun updateAlarms() {
        alarms.value.forEach { alarm ->
            if(alarm.triggerRelativeDuration.isNullOrEmpty())
                return@forEach

            val dur = try { Duration.parse(alarm.triggerRelativeDuration!!) } catch (e: IllegalArgumentException) { return@forEach }
            if(alarm.triggerRelativeTo == AlarmRelativeTo.END.name) {
                alarm.triggerTime = icalObject.due!! + dur.inWholeMilliseconds
                alarm.triggerTimezone = icalObject.dueTimezone
            } else {
                alarm.triggerTime = icalObject.dtstart!! + dur.inWholeMilliseconds
                alarm.triggerTimezone = icalObject.dtstartTimezone
            }
        }
    }

    if(goBackRequested)
        processGoBack()

    BackHandler {
        processGoBack()
    }

    if(showUnsavedChangesDialog) {
        UnsavedChangesDialog(
            onSave = {
                showUnsavedChangesDialog = false
                saveICalObject(
                    icalObject,
                    categories.value,
                    comments.value,
                    attendees.value,
                    resources.value,
                    attachments.value,
                    alarms.value
                )
                goBack()
            },
            onDiscard = {
                showUnsavedChangesDialog = false
                goBack()
            }
        )
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = color,
            onColorChanged = { newColor ->
                color = newColor
                icalObject.color = newColor
                changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
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

            AnimatedVisibility(!isEditMode.value || isChild) {

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

            AnimatedVisibility(isEditMode.value && !isChild) {

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
                            includeVJOURNAL = if (iCalEntity.value?.property?.component == Component.VJOURNAL.name || subnotes.value.isNotEmpty()) true else null,
                            includeVTODO = if (iCalEntity.value?.property?.component == Component.VTODO.name || subtasks.value.isNotEmpty()) true else null,
                            onSelectionChanged = { newCollection ->
                                if (icalObject.collectionId != newCollection.collectionId) {
                                    saveICalObject(
                                        icalObject,
                                        categories.value,
                                        comments.value,
                                        attendees.value,
                                        resources.value,
                                        attachments.value,
                                        alarms.value
                                    )
                                    onMoveToNewCollection(icalObject, newCollection)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
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
                    updateAlarms()
                    changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                },
                onDueChanged = { datetime, timezone ->
                    icalObject.due = datetime
                    icalObject.dueTimezone = timezone
                    updateAlarms()
                    changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                },
                onCompletedChanged = { datetime, timezone ->
                    icalObject.completed = datetime
                    icalObject.completedTimezone = timezone
                    changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
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

                        if (description.isNotBlank()) {
                            if(isMarkdownEnabled)
                                MarkdownText(
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.surface),
                                    markdown = description,
                                    modifier = Modifier.padding(8.dp)
                                )
                            else
                                Text(
                                    text = description,
                                    modifier = Modifier.padding(8.dp)
                                )
                        }
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
                            changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                        },
                        label = { Text(stringResource(id = R.string.summary)) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Default),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                            icalObject.description = it.ifEmpty { null }
                            changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                        },
                        label = { Text(stringResource(id = R.string.description)) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Default),
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
                        isReadOnly = iCalEntity.value?.ICalCollection?.readonly == true,
                        isLinkedRecurringInstance = icalObject.isRecurLinkedInstance,
                        sliderIncrement = 1,   // TODO
                        onProgressChanged = { itemId, newPercent, isLinked ->
                            onProgressChanged(itemId, newPercent, isLinked)
                            changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                        },
                        showProgressLabel = true,
                        showSlider = true,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }


            DetailsCardStatusClassificationPriority(
                icalObject = icalObject,
                isEditMode = isEditMode.value,
                onStatusChanged = { newStatus ->
                    icalObject.status = newStatus
                    changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                },
                onClassificationChanged = { newClassification ->
                    icalObject.classification = newClassification
                    changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                },
                onPriorityChanged = { newPriority ->
                    icalObject.priority = newPriority
                    changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                },
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(subtasks.value.isNotEmpty() || (isEditMode.value && iCalEntity.value?.ICalCollection?.supportsVTODO == true && (detailSettings.enableSubtasks.value || showAllOptions))) {
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
                    onSubtaskDeleted = { icalObjectId -> onSubEntryDeleted(icalObjectId) },
                    goToView = goToView,
                    goToEdit = goToEdit
                )
            }

            AnimatedVisibility(subnotes.value.isNotEmpty() || (isEditMode.value && iCalEntity.value?.ICalCollection?.supportsVJOURNAL == true && (detailSettings.enableSubnotes.value || showAllOptions))) {
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
                    player = player,
                    goToView = goToView,
                    goToEdit = goToEdit
                )
            }


            AnimatedVisibility(categories.value.isNotEmpty() || (isEditMode.value && (detailSettings.enableCategories.value || showAllOptions))) {
                DetailsCardCategories(
                    initialCategories = categories.value,
                    isEditMode = isEditMode.value,
                    onCategoriesUpdated = { newCategories ->
                        categories.value = newCategories
                        changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                    },
                    allCategories = allCategories,
                )
            }

            AnimatedVisibility(resources.value.isNotEmpty() || (isEditMode.value && (detailSettings.enableResources.value || showAllOptions))) {
                DetailsCardResources(
                    initialResources = resources.value,
                    isEditMode = isEditMode.value,
                    onResourcesUpdated = { newResources ->
                        resources.value = newResources
                        changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                    },
                    allResources = allResources,
                )
            }


            AnimatedVisibility(attendees.value.isNotEmpty() || (isEditMode.value && (detailSettings.enableAttendees.value || showAllOptions))) {
                DetailsCardAttendees(
                    initialAttendees = attendees.value,
                    isEditMode = isEditMode.value,
                    onAttendeesUpdated = { newAttendees ->
                        attendees.value = newAttendees
                        changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                    }
                )
            }

            AnimatedVisibility(icalObject.contact?.isNotBlank() == true || (isEditMode.value && (detailSettings.enableContact.value || showAllOptions))) {
                DetailsCardContact(
                    initialContact = icalObject.contact ?: "",
                    isEditMode = isEditMode.value,
                    onContactUpdated = { newContact ->
                        icalObject.contact = newContact.ifEmpty { null }
                        changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                    },
                )
            }


            AnimatedVisibility(icalObject.url?.isNotEmpty() == true || (isEditMode.value && (detailSettings.enableUrl.value || showAllOptions))) {
                DetailsCardUrl(
                    initialUrl = icalObject.url ?: "",
                    isEditMode = isEditMode.value,
                    onUrlUpdated = { newUrl ->
                        icalObject.url = newUrl.ifEmpty { null }
                        changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                    },
                )
            }

            AnimatedVisibility((icalObject.location?.isNotEmpty() == true || (icalObject.geoLat != null && icalObject.geoLong != null)) || (isEditMode.value && (detailSettings.enableLocation.value || showAllOptions))) {
                DetailsCardLocation(
                    initialLocation = icalObject.location,
                    initialGeoLat = icalObject.geoLat,
                    initialGeoLong = icalObject.geoLong,
                    isEditMode = isEditMode.value,
                    onLocationUpdated = { newLocation, newGeoLat, newGeoLong ->
                        icalObject.geoLat = newGeoLat
                        icalObject.geoLong = newGeoLong
                        icalObject.location = newLocation.ifEmpty { null }
                        changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                    },
                )
            }

            AnimatedVisibility(comments.value.isNotEmpty() || (isEditMode.value && (detailSettings.enableComments.value || showAllOptions))) {
                DetailsCardComments(
                    initialComments = comments.value,
                    isEditMode = isEditMode.value,
                    onCommentsUpdated = { newComments ->
                        comments.value = newComments
                        changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                    }
                )
            }


            AnimatedVisibility(attachments.value.isNotEmpty() || (isEditMode.value && (detailSettings.enableAttachments.value || showAllOptions))) {
                DetailsCardAttachments(
                    initialAttachments = attachments.value,
                    isEditMode = isEditMode.value,
                    isRemoteCollection = iCalEntity.value?.ICalCollection?.accountType != LOCAL_ACCOUNT_TYPE,
                    onAttachmentsUpdated = { newAttachments ->
                        attachments.value = newAttachments
                        changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                    }
                )
            }

            AnimatedVisibility(alarms.value.isNotEmpty() || (isEditMode.value && (detailSettings.enableAlarms.value || (showAllOptions && icalObject.module == Module.TODO.name)))) {
                DetailsCardAlarms(
                    alarms = alarms,
                    icalObject = icalObject,
                    isEditMode = isEditMode.value,
                    onAlarmsUpdated = { newAlarms ->
                        alarms.value = newAlarms
                        changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                    })
            }

            AnimatedVisibility(icalObject.rrule != null
                    || icalObject.isRecurLinkedInstance
                    || icalObject.recurOriginalIcalObjectId != null
                    || (isEditMode.value && (detailSettings.enableRecurrence.value || (showAllOptions && icalObject.module != Module.NOTE.name)))
            ) {   // only Todos have recur!
                DetailsCardRecur(
                    icalObject = icalObject,
                    isEditMode = isEditMode.value,
                    onRecurUpdated = { updatedRRule ->
                        icalObject.rrule = updatedRRule?.toString()
                        changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                    },
                    goToView = goToView
                )
            }

            AnimatedVisibility(isEditMode.value && !showAllOptions) {
                TextButton(
                    onClick = { showAllOptions = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.details_show_all_options))
                }
            }

            AnimatedVisibility(!isEditMode.value) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)) {
                    Text(
                        stringResource(id = R.string.view_created_text, DateTimeUtils.convertLongToFullDateTimeString(icalObject.created, null)),
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        stringResource(id = R.string.view_last_modified_text, DateTimeUtils.convertLongToFullDateTimeString(icalObject.lastModified, null)),
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic
                    )
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
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGEUNSAVED) },
            subtasks = remember { mutableStateOf(emptyList()) },
            subnotes = remember { mutableStateOf(emptyList()) },
            isChild = false,
            player = null,
            goBackRequested = false,
            allCollections = listOf(ICalCollection.createLocalCollection(LocalContext.current)),
            allCategories = emptyList(),
            allResources = emptyList(),
            detailSettings = detailSettings,
            saveICalObject = { _, _, _, _, _, _, _ -> },
            deleteICalObject = { },
            onProgressChanged = { _, _, _ -> },
            onMoveToNewCollection = { _, _ -> },
            onSubEntryAdded = { _, _ -> },
            onSubEntryDeleted = { },
            onSubEntryUpdated = { _, _ -> },
            goToView = { },
            goToEdit = { },
            goBack = { },
            autosave = true
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
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVING) },
            subtasks = remember { mutableStateOf(emptyList()) },
            subnotes = remember { mutableStateOf(emptyList()) },
            isChild = false,
            player = null,
            goBackRequested = false,
            allCollections = listOf(ICalCollection.createLocalCollection(LocalContext.current)),
            allCategories = emptyList(),
            allResources = emptyList(),
            detailSettings = detailSettings,
            saveICalObject = { _, _, _, _, _, _, _ -> },
            deleteICalObject = { },
            onProgressChanged = { _, _, _ -> },
            onMoveToNewCollection = { _, _ -> },
            onSubEntryAdded = { _, _ -> },
            onSubEntryDeleted = { },
            onSubEntryUpdated = { _, _ -> },
            goToView = { },
            goToEdit = { },
            goBack = { },
            autosave = true
        )
    }
}




@Preview(showBackground = true)
@Composable
fun DetailScreenContent_TODO_editInitially_isChild() {
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
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVING) },
            subtasks = remember { mutableStateOf(emptyList()) },
            subnotes = remember { mutableStateOf(emptyList()) },
            isChild = true,
            player = null,
            goBackRequested = false,
            allCollections = listOf(ICalCollection.createLocalCollection(LocalContext.current)),
            allCategories = emptyList(),
            allResources = emptyList(),
            detailSettings = detailSettings,
            saveICalObject = { _, _, _, _, _, _, _ -> },
            deleteICalObject = { },
            onProgressChanged = { _, _, _ -> },
            onMoveToNewCollection = { _, _ -> },
            onSubEntryAdded = { _, _ -> },
            onSubEntryDeleted = { },
            onSubEntryUpdated = { _, _ -> },
            goToView = { },
            goToEdit = { },
            goBack = { },
            autosave = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenContent_failedLoading() {
    MaterialTheme {

        val prefs: SharedPreferences = LocalContext.current.getSharedPreferences(
            DetailViewModel.PREFS_DETAIL_TODOS,
            Context.MODE_PRIVATE
        )
        val detailSettings = DetailSettings(prefs)

        DetailScreenContent(
            iCalEntity = remember { mutableStateOf(null) },
            isEditMode = remember { mutableStateOf(true) },
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVING) },
            subtasks = remember { mutableStateOf(emptyList()) },
            subnotes = remember { mutableStateOf(emptyList()) },
            isChild = true,
            player = null,
            goBackRequested = false,
            allCollections = listOf(ICalCollection.createLocalCollection(LocalContext.current)),
            allCategories = emptyList(),
            allResources = emptyList(),
            detailSettings = detailSettings,
            saveICalObject = { _, _, _, _, _, _, _ -> },
            deleteICalObject = { },
            onProgressChanged = { _, _, _ -> },
            onMoveToNewCollection = { _, _ -> },
            onSubEntryAdded = { _, _ -> },
            onSubEntryDeleted = { },
            onSubEntryUpdated = { _, _ -> },
            goToView = { },
            goToEdit = { },
            goBack = { },
            autosave = true
        )
    }
}

