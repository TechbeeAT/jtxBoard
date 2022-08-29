/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.compose.cards.*
import at.techbee.jtx.ui.compose.elements.CollectionsSpinner
import at.techbee.jtx.ui.compose.elements.ColoredEdge
import at.techbee.jtx.ui.compose.cards.VerticalDateCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreenContent(
    iCalEntity: State<ICalEntity?>,
    isEditMode: MutableState<Boolean>,
    subtasks: List<ICal4List>,
    subnotes: List<ICal4List>,
    allCollections: List<ICalCollection>,
    modifier: Modifier = Modifier,
    //player: MediaPlayer?,
    saveIcalObject: (ICalObject) -> Unit,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit,
    onExpandedChanged: (itemId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isAttachmentsExpanded: Boolean) -> Unit
) {
    if(iCalEntity.value == null)
        return

    val context = LocalContext.current

    var summary by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.summary ?: "") }
    var description by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.description ?: "") }

    var dtstart by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.dtstart) }
    var dtstartTimezone by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.dtstartTimezone) }
    var due by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.due) }
    var dueTimezone by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.dueTimezone) }
    var completed by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.completed) }
    var completedTimezone by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.completedTimezone) }

    val contact = rememberSaveable { mutableStateOf(iCalEntity.value?.property?.contact ?: "") }
    val url = rememberSaveable { mutableStateOf(iCalEntity.value?.property?.url ?: "") }
    val location = rememberSaveable { mutableStateOf(iCalEntity.value?.property?.location ?: "") }
    val geoLat = rememberSaveable { mutableStateOf(iCalEntity.value?.property?.geoLat) }
    val geoLong = rememberSaveable { mutableStateOf(iCalEntity.value?.property?.geoLong) }
    var status by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.status) }
    var classification by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.classification) }
    var priority by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.priority ?: 0) }
    var rrule by rememberSaveable { mutableStateOf(iCalEntity.value?.property?.rrule) }
    val categories = rememberSaveable { mutableStateOf(iCalEntity.value?.categories ?: emptyList()) }
    val resources = rememberSaveable { mutableStateOf(iCalEntity.value?.resources ?: emptyList()) }
    val attendees = rememberSaveable { mutableStateOf(iCalEntity.value?.attendees ?: emptyList()) }
    val comments = rememberSaveable { mutableStateOf(iCalEntity.value?.comments ?: emptyList()) }
    val attachments = rememberSaveable { mutableStateOf(iCalEntity.value?.attachments ?: emptyList()) }
    val alarms = rememberSaveable { mutableStateOf(iCalEntity.value?.alarms ?: emptyList()) }

    fun save() {
        iCalEntity.value?.property?.let {
            it.summary = summary.ifBlank { null }
            it.description = description.ifBlank { null }
            it.dtstart = dtstart
            it.dtstartTimezone = dtstartTimezone
            saveIcalObject(it)
        }
    }


    /*
    var markwon = Markwon.builder(LocalContext.current)
        .usePlugin(StrikethroughPlugin.create())
        .build()
     */

    Box(Modifier.verticalScroll(rememberScrollState())) {

        ColoredEdge(iCalEntity.value?.property?.color, iCalEntity.value?.ICalCollection?.color)

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
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Outlined.ColorLens, stringResource(id = R.string.color))
                        }
                    }
                }
            }

            if (iCalEntity.value?.property?.module == Module.JOURNAL.name && dtstart != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VerticalDateCard(
                        datetime = dtstart,
                        timezone = dtstartTimezone,
                        isEditMode = isEditMode,
                        onDateTimeChanged = { datetime, timezone ->
                            dtstart = datetime
                            dtstartTimezone = timezone
                            save()
                        }
                    )
                }
            }

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
                        },
                        label = { Text(stringResource(id = R.string.summary)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .onFocusChanged { focusState ->
                                if (!focusState.hasFocus) {
                                    iCalEntity.value?.property?.let {
                                        if (summary != it.summary) {
                                            it.summary = summary
                                            save()
                                        }
                                    }
                                }
                            }
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
                            .onFocusChanged { focusState ->
                                if (!focusState.hasFocus) {
                                    iCalEntity.value?.property?.let {
                                        if (description != it.description) {
                                            it.description = description
                                            save()
                                        }
                                    }
                                }
                            }
                    )
                }
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var statusMenuExpanded by remember { mutableStateOf(false) }
                var classificationMenuExpanded by remember { mutableStateOf(false) }
                var priorityMenuExpanded by remember { mutableStateOf(false) }

                AssistChip(
                    label = {
                        if (iCalEntity.value?.property?.component == Component.VJOURNAL.name)
                            Text(StatusJournal.getStringResource(context, status) ?: status ?: "-")
                        else
                            Text(StatusTodo.getStringResource(context, status) ?: status ?: "-")


                        DropdownMenu(
                            expanded = statusMenuExpanded,
                            onDismissRequest = { statusMenuExpanded = false }
                        ) {
                            if (iCalEntity.value?.property?.component == Component.VJOURNAL.name) {
                                StatusJournal.values().forEach { statusJournal ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(id = statusJournal.stringResource)) },
                                        onClick = {
                                            status = statusJournal.name
                                            statusMenuExpanded = false
                                            /*TODO: Save*/
                                        }
                                    )
                                }
                            } else {
                                StatusTodo.values().forEach { statusJournal ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(id = statusJournal.stringResource)) },
                                        onClick = {
                                            status = statusJournal.name
                                            statusMenuExpanded = false
                                            /*TODO: Save*/
                                        }
                                    )
                                }
                            }
                        }
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.PublishedWithChanges,
                            stringResource(id = R.string.status)
                        )
                    },
                    onClick = { statusMenuExpanded = true }
                )


                AssistChip(
                    label = {
                        Text(
                            Classification.getStringResource(context, classification)
                                ?: classification ?: ""
                        )

                        DropdownMenu(
                            expanded = classificationMenuExpanded,
                            onDismissRequest = { classificationMenuExpanded = false }
                        ) {
                            Classification.values().forEach { clazzification ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = clazzification.stringResource)) },
                                    onClick = {
                                        classification = clazzification.name
                                        classificationMenuExpanded = false
                                        /*TODO: Save*/
                                    }
                                )
                            }
                        }
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.GppMaybe,
                            stringResource(id = R.string.classification)
                        )
                    },
                    onClick = { classificationMenuExpanded = true }
                )

                val priorityStrings = stringArrayResource(id = R.array.priority)
                if (iCalEntity.value?.property?.component == Component.VTODO.name) {

                    AssistChip(
                        label = {
                            Text(
                                if (priority in priorityStrings.indices)
                                    stringArrayResource(id = R.array.priority)[priority]
                                else
                                    stringArrayResource(id = R.array.priority)[0]
                            )

                            DropdownMenu(
                                expanded = priorityMenuExpanded,
                                onDismissRequest = { priorityMenuExpanded = false }
                            ) {
                                stringArrayResource(id = R.array.priority).forEachIndexed { index, prio ->
                                    DropdownMenuItem(
                                        text = { Text(prio) },
                                        onClick = {
                                            priority = index
                                            priorityMenuExpanded = false
                                            /*TODO: Save*/
                                        }
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.AssignmentLate,
                                stringResource(id = R.string.priority)
                            )
                        },
                        onClick = { priorityMenuExpanded = true }
                    )
                }
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
                    ), // TODO
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
                    onContactUpdated = { /*TODO*/ },
                )
            }


            AnimatedVisibility(url.value.isNotEmpty() || isEditMode.value) {
                DetailsCardUrl(
                    url = url,
                    isEditMode = isEditMode,
                    onUrlUpdated = { /*TODO*/ },
                )
            }

            AnimatedVisibility((location.value.isNotEmpty() || (geoLat.value != null && geoLong.value != null)) || isEditMode.value) {
                DetailsCardLocation(
                    location = location,
                    geoLat = geoLat,
                    geoLong = geoLong,
                    isEditMode = isEditMode,
                    onLocationUpdated = { /*TODO*/ },
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
            subtasks = emptyList(),
            subnotes = emptyList(),
            //attachments = emptyList(),
            allCollections = listOf(ICalCollection.createLocalCollection(LocalContext.current)),
            //player = null,
            saveIcalObject = {  },
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
            iCalEntity = remember { mutableStateOf(entity) },
            isEditMode = remember { mutableStateOf(true) },
            subtasks = emptyList(),
            subnotes = emptyList(),
            //attachments = emptyList(),
            allCollections = listOf(ICalCollection.createLocalCollection(LocalContext.current)),
            //player = null,
            saveIcalObject = {  },
            onProgressChanged = { _, _, _ -> },
            onExpandedChanged = { _, _, _, _ -> }
        )
    }
}

