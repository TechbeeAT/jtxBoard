/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.media.MediaPlayer
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NavigateBefore
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getTextBeforeSelection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.NotificationPublisher
import at.techbee.jtx.R
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredListSettingData
import at.techbee.jtx.database.locals.StoredResource
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.AlarmRelativeTo
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Attendee
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.properties.Comment
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.reusable.elements.ProgressElement
import at.techbee.jtx.ui.settings.DropdownSettingOption
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.util.DateTimeUtils
import com.arnyminerz.markdowntext.MarkdownText
import kotlinx.coroutines.delay
import org.apache.commons.lang3.StringUtils
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

enum class DetailsScreenSection(
    @StringRes val stringRes: Int
) {
    COLLECTION(R.string.collection),
    DATES(R.string.date),
    SUMMARYDESCRIPTION(R.string.summary_description),
    PROGRESS(R.string.progress),
    STATUSCLASSIFICATIONPRIORITY(R.string.status_classification_priority),
    CATEGORIES(R.string.categories),
    PARENTS(R.string.linked_parents),
    SUBTASKS(R.string.subtasks),
    SUBNOTES(R.string.view_feedback_linked_notes),
    RESOURCES(R.string.resources),
    ATTENDEES(R.string.attendees),
    CONTACT(R.string.contact),
    URL(R.string.url),
    LOCATION(R.string.location),
    COMMENTS(R.string.comments),
    ATTACHMENTS(R.string.attachments),
    ALARMS(R.string.alarms),
    RECURRENCE(R.string.recurrence);

    companion object {
        fun entriesFor(module: Module): List<DetailsScreenSection> {
            return when(module) {
                Module.JOURNAL -> listOf(COLLECTION, DATES, SUMMARYDESCRIPTION, STATUSCLASSIFICATIONPRIORITY, CATEGORIES, PARENTS, SUBTASKS, SUBNOTES, ATTENDEES, CONTACT, URL, LOCATION, COMMENTS, ATTACHMENTS, RECURRENCE)
                Module.NOTE -> listOf(COLLECTION, SUMMARYDESCRIPTION, STATUSCLASSIFICATIONPRIORITY, CATEGORIES, PARENTS, SUBTASKS, SUBNOTES, ATTENDEES, CONTACT, URL, LOCATION, COMMENTS, ATTACHMENTS)
                Module.TODO -> listOf(COLLECTION, DATES, SUMMARYDESCRIPTION, PROGRESS, STATUSCLASSIFICATIONPRIORITY, CATEGORIES, PARENTS, SUBTASKS, SUBNOTES, RESOURCES, ATTENDEES, CONTACT, URL, LOCATION, COMMENTS, ATTACHMENTS, ALARMS, RECURRENCE)
            }
        }
    }
}

@Composable
fun DetailScreenContent(
    observedICalEntity: State<ICalEntity?>,
    iCalObject: ICalObject?,
    categories: SnapshotStateList<Category>,
    resources: SnapshotStateList<Resource>,
    attendees: SnapshotStateList<Attendee>,
    comments: SnapshotStateList<Comment>,
    attachments: SnapshotStateList<Attachment>,
    alarms: SnapshotStateList<Alarm>,
    isEditMode: MutableState<Boolean>,
    changeState: MutableState<DetailViewModel.DetailChangeState>,
    subtasksLive: LiveData<List<ICal4List>>,
    subnotesLive: LiveData<List<ICal4List>>,
    parentsLive: LiveData<List<ICal4List>>,
    isChildLive: LiveData<Boolean>,
    allWriteableCollectionsLive: LiveData<List<ICalCollection>>,
    allCategoriesLive: LiveData<List<String>>,
    allResourcesLive: LiveData<List<String>>,
    storedCategories: List<StoredCategory>,
    storedResources: List<StoredResource>,
    extendedStatuses: List<ExtendedStatus>,
    detailSettings: DetailSettings,
    icalObjectIdList: List<Long>,
    seriesInstancesLive: LiveData<List<ICalObject>>,
    seriesElement: ICalObject?,
    sliderIncrement: Int,
    showProgressForMainTasks: Boolean,
    showProgressForSubTasks: Boolean,
    keepStatusProgressCompletedInSync: Boolean,
    linkProgressToSubtasks: Boolean,
    markdownState: MutableState<MarkdownState>,
    scrollToSectionState: MutableState<DetailsScreenSection?>,
    modifier: Modifier = Modifier,
    player: MediaPlayer?,
    isSubtaskDragAndDropEnabled: Boolean,
    isSubnoteDragAndDropEnabled: Boolean,
    saveEntry: () -> Unit,
    onProgressChanged: (itemId: Long, newPercent: Int) -> Unit,
    onMoveToNewCollection: (newCollection: ICalCollection) -> Unit,
    onSubEntryAdded: (icalObject: ICalObject, attachment: Attachment?) -> Unit,
    onSubEntryDeleted: (icalObjectId: Long) -> Unit,
    onSubEntryUpdated: (icalObjectId: Long, newText: String) -> Unit,
    onUnlinkSubEntry: (icalObjectId: Long, parentUID: String?) -> Unit,
    goToDetail: (itemId: Long, editMode: Boolean, list: List<Long>, popBackStack: Boolean) -> Unit,
    goBack: () -> Unit,
    goToFilteredList:  (StoredListSettingData) -> Unit,
    unlinkFromSeries: (instances: List<ICalObject>, series: ICalObject?, deleteAfterUnlink: Boolean) -> Unit,
    onShowLinkExistingDialog: (modules: List<Module>, reltype: Reltype) -> Unit,
    onUpdateSortOrder: (List<ICal4List>) -> Unit,
    ) {
    if(iCalObject == null)
        return

    val context = LocalContext.current
    val localInspectionMode = LocalInspectionMode.current

    val parents = parentsLive.observeAsState(emptyList())
    val subtasks = subtasksLive.observeAsState(emptyList())
    val subnotes = subnotesLive.observeAsState(emptyList())
    val seriesInstances = seriesInstancesLive.observeAsState(emptyList())
    val isChild = isChildLive.observeAsState(false)
    val allWriteableCollections = allWriteableCollectionsLive.observeAsState(emptyList())

    val autoAlarmSetting by remember {
        if (!localInspectionMode)
            SettingsStateHolder(context).settingAutoAlarm
        else
            mutableStateOf(false)
    }

    var timeout by remember { mutableStateOf(false) }
    LaunchedEffect(timeout, observedICalEntity.value) {
        if (observedICalEntity.value == null && !timeout) {
            delay((2).seconds)
            timeout = true
        }
    }

    // item was not loaded yet or was deleted in the background
    if (observedICalEntity.value == null && timeout) {
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
    } else if (observedICalEntity.value == null && !timeout) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val color = rememberSaveable { mutableStateOf(iCalObject.color) }
    var summary by rememberSaveable { mutableStateOf(iCalObject.summary ?:"") }
    var description by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue(iCalObject.description ?: "")) }


    // make sure the values get propagated in the iCalObject again when the orientation changes
    if(iCalObject.summary != summary.ifEmpty { null } || iCalObject.description != description.text.ifEmpty { null } || iCalObject.color != color.value) {
        iCalObject.summary = summary.ifEmpty { null }
        iCalObject.description = description.text.ifEmpty { null }
        iCalObject.color = color.value
    }

    // Apply Markdown on recomposition if applicable, then set back to OBSERVING
    if (markdownState.value != MarkdownState.DISABLED && markdownState.value != MarkdownState.CLOSED) {
        description = markdownState.value.format(description)
        markdownState.value = MarkdownState.OBSERVING
    }

    val isProPurchased = BillingManager.getInstance().isProPurchased.observeAsState(true)
    val allPossibleCollections = allWriteableCollections.value.filter {
        it.accountType == LOCAL_ACCOUNT_TYPE || isProPurchased.value            // filter remote collections if pro was not purchased
    }

    // Update some fields in the background that might have changed (e.g. by creating a copy)
    if ((observedICalEntity.value?.property?.sequence ?: 0) > iCalObject.sequence) {
        iCalObject.status = observedICalEntity.value?.property?.status
        iCalObject.percent = observedICalEntity.value?.property?.percent
        iCalObject.completed = observedICalEntity.value?.property?.completed
        iCalObject.completedTimezone = observedICalEntity.value?.property?.completedTimezone
        iCalObject.sequence = observedICalEntity.value?.property?.sequence ?: 0
        iCalObject.recurid = observedICalEntity.value?.property?.recurid
        iCalObject.uid = observedICalEntity.value?.property?.uid!!
    }
    observedICalEntity.value?.property?.id?.let { iCalObject.id = it }   //  the icalObjectId might also have changed (when moving the entry to a new collection)!
    observedICalEntity.value?.property?.uid?.let { iCalObject.uid = it }   //  the icalObjectId might also have changed (when moving the entry to a new collection)!
    observedICalEntity.value?.property?.collectionId?.let { iCalObject.collectionId = it }   //  the collectionId might also have changed (when moving the entry to a new collection)!

    var showAllOptions by rememberSaveable { mutableStateOf(false) }


    val previousIsEditModeState = rememberSaveable { mutableStateOf(isEditMode.value) }
    if (previousIsEditModeState.value && !isEditMode.value) {  //changed from edit to view mode
        saveEntry()

        // trigger alarm immediately if setting is active
        if(iCalObject.getModuleFromString() == Module.TODO && autoAlarmSetting == DropdownSettingOption.AUTO_ALARM_ALWAYS_ON_SAVE)
            NotificationPublisher.triggerImmediateAlarm(iCalObject, context)
    }
    previousIsEditModeState.value = isEditMode.value


    // save 10 seconds after changed, then reset value
    if (changeState.value == DetailViewModel.DetailChangeState.CHANGEUNSAVED && detailSettings.detailSetting[DetailSettingsOption.ENABLE_AUTOSAVE] != false) {
        LaunchedEffect(changeState) {
            delay((10).seconds.inWholeMilliseconds)
            saveEntry()
        }
    }

    /**
     * Updates the alarms when the dates get changed
     */
    fun updateAlarms() {
        alarms.forEach { alarm ->
            if (alarm.triggerRelativeDuration.isNullOrEmpty())
                return@forEach

            val dur = try {
                Duration.parse(alarm.triggerRelativeDuration!!)
            } catch (e: IllegalArgumentException) {
                return@forEach
            }
            if (alarm.triggerRelativeTo == AlarmRelativeTo.END.name) {
                iCalObject.due?.let { alarm.triggerTime = it + dur.inWholeMilliseconds }
                alarm.triggerTimezone = iCalObject.dueTimezone
            } else {
                iCalObject.dtstart?.let { alarm.triggerTime = it + dur.inWholeMilliseconds }
                alarm.triggerTimezone = iCalObject.dtstartTimezone
            }
        }

        //handle autoAlarm
        val autoAlarm = if (autoAlarmSetting == DropdownSettingOption.AUTO_ALARM_ON_DUE && iCalObject.due != null) {
            Alarm.createDisplayAlarm(
                dur = (0).minutes,
                alarmRelativeTo = AlarmRelativeTo.END,
                referenceDate = iCalObject.due!!,
                referenceTimezone = iCalObject.dueTimezone
            )
        } else if (autoAlarmSetting == DropdownSettingOption.AUTO_ALARM_ON_START && iCalObject.dtstart != null) {
            Alarm.createDisplayAlarm(
                dur = (0).minutes,
                alarmRelativeTo = null,
                referenceDate = iCalObject.dtstart!!,
                referenceTimezone = iCalObject.dtstartTimezone
            )
        } else null

        if (autoAlarm != null && alarms.none { alarm -> alarm.triggerRelativeDuration == autoAlarm.triggerRelativeDuration && alarm.triggerRelativeTo == autoAlarm.triggerRelativeTo })
            alarms.add(autoAlarm)
    }


    if (changeState.value == DetailViewModel.DetailChangeState.SAVINGREQUESTED) {
        saveEntry()
    }

    val detailElementModifier = Modifier
        .padding(top = 8.dp)
        .fillMaxWidth()
    val listState = rememberLazyListState()

    LaunchedEffect(scrollToSectionState.value) {
        val sectionIndex = detailSettings.detailSettingOrder.indexOf(scrollToSectionState.value)
        if(sectionIndex >= 0) {
            listState.animateScrollToItem(sectionIndex)
            scrollToSectionState.value = null
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        items(detailSettings.detailSettingOrder)  { detailsScreenSection ->

            when(detailsScreenSection) {
                DetailsScreenSection.COLLECTION -> {
                    DetailsCardCollections(
                        iCalObject = iCalObject,
                        isEditMode = isEditMode.value,
                        isChild = isChild.value,
                        originalICalEntity = observedICalEntity.value,
                        color = color,
                        changeState = changeState,
                        allPossibleCollections = allPossibleCollections,
                        includeVJOURNAL = if (observedICalEntity.value?.property?.component == Component.VJOURNAL.name || subnotes.value.isNotEmpty()) true else null,
                        includeVTODO = if (observedICalEntity.value?.property?.component == Component.VTODO.name || subtasks.value.isNotEmpty()) true else null,
                        onMoveToNewCollection = onMoveToNewCollection,
                        modifier = detailElementModifier
                    )
                }
                DetailsScreenSection.DATES -> {

                    DetailsCardDates(
                        icalObject = iCalObject,
                        isEditMode = isEditMode.value,
                        enableDtstart = detailSettings.detailSetting[DetailSettingsOption.ENABLE_DTSTART] ?: true || iCalObject.getModuleFromString() == Module.JOURNAL,
                        enableDue = detailSettings.detailSetting[DetailSettingsOption.ENABLE_DUE] ?: true,
                        enableCompleted = detailSettings.detailSetting[DetailSettingsOption.ENABLE_COMPLETED]
                            ?: true,
                        allowCompletedChange = !(linkProgressToSubtasks && subtasks.value.isNotEmpty()),
                        onDtstartChanged = { datetime, timezone ->
                            iCalObject.dtstart = datetime
                            iCalObject.dtstartTimezone = timezone
                            updateAlarms()
                            changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                        },
                        onDueChanged = { datetime, timezone ->
                            iCalObject.due = datetime
                            iCalObject.dueTimezone = timezone
                            updateAlarms()
                            changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                        },
                        onCompletedChanged = { datetime, timezone ->
                            iCalObject.completed = datetime
                            iCalObject.completedTimezone = timezone
                            if (keepStatusProgressCompletedInSync) {
                                if (datetime == null)
                                    iCalObject.setUpdatedProgress(null, true)
                                else
                                    iCalObject.setUpdatedProgress(100, true)
                            }
                            changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                        },
                        toggleEditMode = { isEditMode.value = !isEditMode.value },
                        modifier = detailElementModifier
                    )
                }

                DetailsScreenSection.SUMMARYDESCRIPTION -> {
                    if(!isEditMode.value && (summary.isNotBlank() || description.text.isNotBlank())) {
                        SelectionContainer(modifier = detailElementModifier) {
                            ElevatedCard(
                                onClick = {
                                    if (observedICalEntity.value?.ICalCollection?.readonly == false)
                                        isEditMode.value = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("benchmark:DetailSummary")
                            ) {

                                if (summary.isNotBlank())
                                    Text(
                                        summary.trim(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                if (description.text.isNotBlank()) {
                                    if (detailSettings.detailSetting[DetailSettingsOption.ENABLE_MARKDOWN] != false)
                                        MarkdownText(
                                            markdown = description.text.trim(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            style = TextStyle(
                                                textDirection = TextDirection.Content,
                                                fontFamily = LocalTextStyle.current.fontFamily
                                                ),
                                            onClick = {
                                                if (observedICalEntity.value?.ICalCollection?.readonly == false)
                                                    isEditMode.value = true
                                            }
                                        )
                                    else
                                        Text(
                                            text = description.text.trim(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                        )
                                }
                            }
                        }
                    }

                    if(isEditMode.value) {
                        ElevatedCard(
                            modifier = detailElementModifier
                                .fillMaxWidth()
                                .testTag("benchmark:DetailSummaryCardEdit")
                        ) {

                            if(summary.isNotEmpty() || detailSettings.detailSetting[DetailSettingsOption.ENABLE_SUMMARY] == true || showAllOptions) {
                                OutlinedTextField(
                                    value = summary,
                                    onValueChange = {
                                        summary = it
                                        iCalObject.summary = it.ifEmpty { null }
                                        changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                                    },
                                    label = { Text(stringResource(id = R.string.summary)) },
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences,
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Default
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                            }

                            if(description.text.isNotEmpty() || detailSettings.detailSetting[DetailSettingsOption.ENABLE_DESCRIPTION] == true || showAllOptions) {
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = {

                                        // START Create bulletpoint if previous line started with a bulletpoint
                                        val enteredCharIndex =
                                            StringUtils.indexOfDifference(it.text, description.text)
                                        val enteredCharIsReturn =
                                            enteredCharIndex >= 0
                                                    && it.text.substring(enteredCharIndex)
                                                .startsWith(System.lineSeparator())
                                                    && it.text.length > description.text.length  // excludes backspace!

                                        val before = it.getTextBeforeSelection(Int.MAX_VALUE)
                                        val after =
                                            if (it.selection.start < it.annotatedString.lastIndex) it.annotatedString.subSequence(
                                                it.selection.start,
                                                it.annotatedString.lastIndex + 1
                                            ) else AnnotatedString("")
                                        val lines = before.split(System.lineSeparator())
                                        val previous =
                                            if (lines.lastIndex > 1) lines[lines.lastIndex - 1] else before
                                        val nextLineStartWith = when {
                                            previous.startsWith("- [ ] ") || previous.startsWith("- [x]") -> "- [ ] "
                                            previous.startsWith("* ") -> "* "
                                            previous.startsWith("- ") -> "- "
                                            else -> null
                                        }

                                        description =
                                            if (description.text != it.text && (nextLineStartWith != null) && enteredCharIsReturn)
                                                TextFieldValue(
                                                    annotatedString = before.plus(
                                                        AnnotatedString(
                                                            nextLineStartWith
                                                        )
                                                    ).plus(after),
                                                    selection = TextRange(it.selection.start + nextLineStartWith.length)
                                                )
                                            else
                                                it
                                        // END Create bulletpoint if previous line started with a bulletpoint

                                        iCalObject.description = it.text.ifEmpty { null }
                                        changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                                    },
                                    label = { Text(stringResource(id = R.string.description)) },
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences,
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Default
                                    ),
                                    minLines = 3,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .onFocusChanged { focusState ->
                                            if (
                                                focusState.hasFocus
                                                && markdownState.value == MarkdownState.DISABLED
                                                && detailSettings.detailSetting[DetailSettingsOption.ENABLE_MARKDOWN] != false
                                            )
                                                markdownState.value = MarkdownState.OBSERVING
                                            else if (!focusState.hasFocus)
                                                markdownState.value = MarkdownState.DISABLED
                                        }
                                )
                            }
                        }
                    }
                }

                DetailsScreenSection.PROGRESS -> {
                    if(iCalObject.module == Module.TODO.name) {
                        ElevatedCard(modifier = detailElementModifier.fillMaxWidth()) {
                            ProgressElement(
                                label = null,
                                iCalObjectId = iCalObject.id,
                                progress = iCalObject.percent,
                                status = iCalObject.status,
                                isReadOnly = observedICalEntity.value?.ICalCollection?.readonly == true || (linkProgressToSubtasks && subtasks.value.isNotEmpty()),
                                sliderIncrement = sliderIncrement,
                                onProgressChanged = { itemId, newPercent ->
                                    iCalObject.setUpdatedProgress(
                                        newPercent,
                                        keepStatusProgressCompletedInSync
                                    )
                                    onProgressChanged(itemId, newPercent)
                                    changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                                },
                                showSlider = showProgressForMainTasks,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }

                DetailsScreenSection.STATUSCLASSIFICATIONPRIORITY -> {
                    if(
                        (!isEditMode.value && (!iCalObject.status.isNullOrEmpty() || !iCalObject.xstatus.isNullOrEmpty() || !iCalObject.classification.isNullOrEmpty() || iCalObject.priority in 1..9))
                                || (isEditMode.value
                                && (detailSettings.detailSetting[DetailSettingsOption.ENABLE_STATUS] != false
                                || detailSettings.detailSetting[DetailSettingsOption.ENABLE_CLASSIFICATION] != false
                                || (iCalObject.getModuleFromString() == Module.TODO && detailSettings.detailSetting[DetailSettingsOption.ENABLE_PRIORITY] != false)
                                || showAllOptions)
                                )
                    ) {

                        DetailsCardStatusClassificationPriority(
                            icalObject = iCalObject,
                            isEditMode = isEditMode.value,
                            enableStatus = detailSettings.detailSetting[DetailSettingsOption.ENABLE_STATUS] ?: true || showAllOptions,
                            enableClassification = detailSettings.detailSetting[DetailSettingsOption.ENABLE_CLASSIFICATION] ?: true || showAllOptions,
                            enablePriority = detailSettings.detailSetting[DetailSettingsOption.ENABLE_PRIORITY] ?: true || showAllOptions,
                            allowStatusChange = !(linkProgressToSubtasks && subtasks.value.isNotEmpty()),
                            extendedStatuses = extendedStatuses,
                            onStatusChanged = { newStatus ->
                                if (keepStatusProgressCompletedInSync && iCalObject.getModuleFromString() == Module.TODO) {
                                    when (newStatus) {
                                        Status.IN_PROCESS -> iCalObject.setUpdatedProgress(
                                            if (iCalObject.percent !in 1..99) 1 else iCalObject.percent,
                                            true
                                        )

                                        Status.COMPLETED -> iCalObject.setUpdatedProgress(100, true)
                                        else -> {}
                                    }
                                }
                                changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                            },
                            onClassificationChanged = { newClassification ->
                                iCalObject.classification = newClassification.classification
                                changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                            },
                            onPriorityChanged = { newPriority ->
                                iCalObject.priority = newPriority
                                changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                            },
                            modifier = detailElementModifier.fillMaxWidth()
                        )
                    }
                }

                DetailsScreenSection.CATEGORIES -> {
                    if(categories.isNotEmpty() || (isEditMode.value && (detailSettings.detailSetting[DetailSettingsOption.ENABLE_CATEGORIES] != false || showAllOptions))) {
                        DetailsCardCategories(
                            categories = categories,
                            storedCategories = storedCategories,
                            isEditMode = isEditMode.value,
                            onCategoriesUpdated = {
                                changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                            },
                            allCategoriesLive = allCategoriesLive,
                            onGoToFilteredList = goToFilteredList,
                            modifier = detailElementModifier
                        )
                    }
                }
                DetailsScreenSection.PARENTS -> {
                    if(parents.value.isNotEmpty() || (isEditMode.value && (detailSettings.detailSetting[DetailSettingsOption.ENABLE_PARENTS] != false || showAllOptions))) {
                        DetailsCardParents(
                            parents = parents.value,
                            isEditMode = isEditMode,
                            sliderIncrement = sliderIncrement,
                            showSlider = showProgressForSubTasks,
                            blockProgressUpdates = linkProgressToSubtasks,
                            onProgressChanged = { itemId, newPercent ->
                                onProgressChanged(itemId, newPercent)
                            },
                            goToDetail = { itemId, editMode, list ->
                                goToDetail(
                                    itemId,
                                    editMode,
                                    list,
                                    false
                                )
                            },
                            onUnlinkFromParent = { parentUID ->
                                onUnlinkSubEntry(
                                    iCalObject.id,
                                    parentUID
                                )
                            },
                            onShowLinkExistingDialog = {
                                onShowLinkExistingDialog(
                                    listOf(
                                        Module.JOURNAL,
                                        Module.NOTE,
                                        Module.TODO
                                    ), Reltype.PARENT
                                )
                            },
                            modifier = detailElementModifier
                        )
                    }
                }
                DetailsScreenSection.SUBTASKS -> {
                    if(subtasks.value.isNotEmpty() || (isEditMode.value && observedICalEntity.value?.ICalCollection?.supportsVTODO == true && (detailSettings.detailSetting[DetailSettingsOption.ENABLE_SUBTASKS] != false || showAllOptions))) {
                        DetailsCardSubtasks(
                            subtasks = subtasks.value,
                            isEditMode = isEditMode,
                            enforceSavingSubtask = changeState.value == DetailViewModel.DetailChangeState.SAVINGREQUESTED || changeState.value == DetailViewModel.DetailChangeState.CHANGESAVING,
                            sliderIncrement = sliderIncrement,
                            showSlider = showProgressForSubTasks,
                            isSubtaskDragAndDropEnabled = isSubtaskDragAndDropEnabled,
                            onProgressChanged = { itemId, newPercent ->
                                onProgressChanged(itemId, newPercent)
                            },
                            onSubtaskAdded = { subtask -> onSubEntryAdded(subtask, null) },
                            onSubtaskUpdated = { icalObjectId, newText ->
                                onSubEntryUpdated(
                                    icalObjectId,
                                    newText
                                )
                            },
                            onSubtaskDeleted = { icalObjectId -> onSubEntryDeleted(icalObjectId) },
                            onUnlinkSubEntry = { id -> onUnlinkSubEntry(id, iCalObject.uid) },
                            goToDetail = { itemId, editMode, list ->
                                goToDetail(
                                    itemId,
                                    editMode,
                                    list,
                                    false
                                )
                            },
                            onShowLinkExistingDialog = {
                                onShowLinkExistingDialog(
                                    listOf(Module.TODO),
                                    Reltype.CHILD
                                )
                            },
                            onUpdateSortOrder = onUpdateSortOrder,
                            modifier = detailElementModifier
                        )
                    }
                }
                DetailsScreenSection.SUBNOTES -> {
                    if(subnotes.value.isNotEmpty() || (isEditMode.value && observedICalEntity.value?.ICalCollection?.supportsVJOURNAL == true && (detailSettings.detailSetting[DetailSettingsOption.ENABLE_SUBNOTES] == true || showAllOptions))) {
                        DetailsCardSubnotes(
                            subnotes = subnotes.value,
                            isEditMode = isEditMode,
                            enforceSavingSubnote = changeState.value == DetailViewModel.DetailChangeState.SAVINGREQUESTED || changeState.value == DetailViewModel.DetailChangeState.CHANGESAVING,
                            isSubnoteDragAndDropEnabled = isSubnoteDragAndDropEnabled,
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
                            onUnlinkSubEntry = { id -> onUnlinkSubEntry(id, iCalObject.uid) },
                            player = player,
                            goToDetail = { itemId, editMode, list ->
                                goToDetail(
                                    itemId,
                                    editMode,
                                    list,
                                    false
                                )
                            },
                            onShowLinkExistingDialog = {
                                onShowLinkExistingDialog(
                                    listOf(
                                        Module.JOURNAL,
                                        Module.NOTE
                                    ), Reltype.CHILD
                                )
                            },
                            onUpdateSortOrder = onUpdateSortOrder,
                            modifier = detailElementModifier
                        )
                    }
                }
                DetailsScreenSection.RESOURCES -> {
                    if(resources.isNotEmpty() || (isEditMode.value && iCalObject.getModuleFromString() == Module.TODO && (detailSettings.detailSetting[DetailSettingsOption.ENABLE_RESOURCES] == true || showAllOptions))) {
                        DetailsCardResources(
                            resources = resources,
                            storedResources = storedResources,
                            isEditMode = isEditMode.value,
                            onResourcesUpdated = {
                                changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                            },
                            onGoToFilteredList = goToFilteredList,
                            allResourcesLive = allResourcesLive,
                            modifier = detailElementModifier
                        )
                    }
                }
                DetailsScreenSection.ATTENDEES -> {
                    if(attendees.isNotEmpty() || (isEditMode.value && (detailSettings.detailSetting[DetailSettingsOption.ENABLE_ATTENDEES] == true || showAllOptions))) {
                        DetailsCardAttendees(
                            attendees = attendees,
                            isEditMode = isEditMode.value,
                            onAttendeesUpdated = {
                                changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                            },
                            modifier = detailElementModifier
                        )
                    }
                }
                DetailsScreenSection.CONTACT -> {
                    if(iCalObject.contact?.isNotBlank() == true || (isEditMode.value && (detailSettings.detailSetting[DetailSettingsOption.ENABLE_CONTACT] == true || showAllOptions))) {
                        DetailsCardContact(
                            initialContact = iCalObject.contact ?: "",
                            isEditMode = isEditMode.value,
                            onContactUpdated = { newContact ->
                                iCalObject.contact = newContact.ifEmpty { null }
                                changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                            },
                            modifier = detailElementModifier
                        )
                    }
                }
                DetailsScreenSection.URL -> {
                    if(iCalObject.url?.isNotEmpty() == true || (isEditMode.value && (detailSettings.detailSetting[DetailSettingsOption.ENABLE_URL] == true || showAllOptions))) {
                        DetailsCardUrl(
                            initialUrl = iCalObject.url ?: "",
                            isEditMode = isEditMode.value,
                            onUrlUpdated = { newUrl ->
                                iCalObject.url = newUrl.ifEmpty { null }
                                changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                            },
                            modifier = detailElementModifier
                        )
                    }
                }
                DetailsScreenSection.LOCATION -> {
                    if((iCalObject.location?.isNotEmpty() == true || (iCalObject.geoLat != null && iCalObject.geoLong != null)) || (isEditMode.value && (detailSettings.detailSetting[DetailSettingsOption.ENABLE_LOCATION] == true || showAllOptions))) {
                        DetailsCardLocation(
                            initialLocation = iCalObject.location,
                            initialGeoLat = iCalObject.geoLat,
                            initialGeoLong = iCalObject.geoLong,
                            initialGeofenceRadius = iCalObject.geofenceRadius,
                            isEditMode = isEditMode.value,
                            onLocationUpdated = { newLocation, newGeoLat, newGeoLong ->
                                if (newGeoLat != null && newGeoLong != null) {
                                    iCalObject.geoLat = newGeoLat
                                    iCalObject.geoLong = newGeoLong
                                } else {
                                    iCalObject.geoLat = null
                                    iCalObject.geoLong = null
                                }
                                iCalObject.location = newLocation.ifEmpty { null }
                                changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                            },
                            onGeofenceRadiusUpdatd = { iCalObject.geofenceRadius = it },
                            modifier = detailElementModifier
                        )
                    }
                }
                DetailsScreenSection.COMMENTS -> {
                    if(comments.isNotEmpty() || (isEditMode.value && (detailSettings.detailSetting[DetailSettingsOption.ENABLE_COMMENTS] == true || showAllOptions))) {
                        DetailsCardComments(
                            comments = comments,
                            isEditMode = isEditMode.value,
                            onCommentsUpdated = {
                                changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                            },
                            modifier = detailElementModifier
                        )
                    }
                }
                DetailsScreenSection.ATTACHMENTS -> {
                    if(attachments.isNotEmpty() || (isEditMode.value && (detailSettings.detailSetting[DetailSettingsOption.ENABLE_ATTACHMENTS] == true || showAllOptions))) {
                        DetailsCardAttachments(
                            attachments = attachments,
                            isEditMode = isEditMode.value,
                            isRemoteCollection = observedICalEntity.value?.ICalCollection?.accountType != LOCAL_ACCOUNT_TYPE,
                            player = player,
                            onAttachmentsUpdated = {
                                changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                            },
                            modifier = detailElementModifier
                        )
                    }
                }
                DetailsScreenSection.ALARMS -> {
                    if(alarms.isNotEmpty() || (isEditMode.value && iCalObject.module == Module.TODO.name && (detailSettings.detailSetting[DetailSettingsOption.ENABLE_ALARMS] == true || showAllOptions))) {
                        DetailsCardAlarms(
                            alarms = alarms,
                            icalObject = iCalObject,
                            isEditMode = isEditMode.value,
                            onAlarmsUpdated = {
                                changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                            },
                            modifier = detailElementModifier
                        )
                    }
                }
                DetailsScreenSection.RECURRENCE -> {
                    if(
                        iCalObject.rrule != null
                                || iCalObject.recurid != null
                                || (isEditMode.value && (detailSettings.detailSetting[DetailSettingsOption.ENABLE_RECURRENCE] == true || (showAllOptions && iCalObject.module != Module.NOTE.name)))
                    ) {   // only Todos have recur!
                        DetailsCardRecur(
                            icalObject = iCalObject,
                            seriesInstances = seriesInstances.value,
                            seriesElement = seriesElement,
                            isEditMode = isEditMode.value,
                            hasChildren = subtasks.value.isNotEmpty() || subnotes.value.isNotEmpty(),
                            onRecurUpdated = { updatedRRule ->
                                iCalObject.rrule = updatedRRule?.toString()
                                if (updatedRRule == null) {
                                    iCalObject.rdate = null
                                    iCalObject.exdate = null
                                }
                                changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
                            },
                            goToDetail = { itemId, editMode, list ->
                                goToDetail(
                                    itemId,
                                    editMode,
                                    list,
                                    false
                                )
                            },
                            unlinkFromSeries = unlinkFromSeries,
                            modifier = detailElementModifier
                        )
                    }
                }
            }
        }

    if(isEditMode.value && !showAllOptions) {
        item {

                TextButton(
                    onClick = { showAllOptions = true },
                    modifier = detailElementModifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.details_show_all_options))
                }
            }
        }

        if(!isEditMode.value) {
            item {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        stringResource(
                            id = R.string.view_created_text,
                            DateTimeUtils.convertLongToFullDateTimeString(iCalObject.created, null)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        stringResource(
                            id = R.string.view_last_modified_text,
                            DateTimeUtils.convertLongToFullDateTimeString(
                                iCalObject.lastModified,
                                null
                            )
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }

        if(!isEditMode.value) {
            item {
                val curIndex = icalObjectIdList.indexOf(observedICalEntity.value?.property?.id ?: 0)
                if (icalObjectIdList.size > 1 && curIndex >= 0) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        if (curIndex > 0) {
                            IconButton(onClick = {
                                goToDetail(
                                    icalObjectIdList[curIndex - 1],
                                    false,
                                    icalObjectIdList,
                                    true
                                )
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.NavigateBefore,
                                    stringResource(id = R.string.previous)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(48.dp))
                        }
                        Text(text = "${icalObjectIdList.indexOf(observedICalEntity.value?.property?.id ?: 0) + 1}/${icalObjectIdList.size}")
                        if (curIndex != icalObjectIdList.lastIndex) {
                            IconButton(onClick = {
                                goToDetail(
                                    icalObjectIdList[curIndex + 1],
                                    false,
                                    icalObjectIdList,
                                    true
                                )
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.NavigateNext,
                                    stringResource(id = R.string.next)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(48.dp))
                        }
                    }
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
        entity.property.color = Color.Blue.toArgb()
        val detailSettings = DetailSettings()

        DetailScreenContent(
            observedICalEntity = remember { mutableStateOf(entity) },
            iCalObject = entity.property,
            categories = remember { mutableStateListOf<Category>().apply { this.addAll(entity.categories ?: emptyList()) } },
            resources = remember { mutableStateListOf() },
            attendees = remember { mutableStateListOf() },
            comments = remember { mutableStateListOf() },
            attachments = remember { mutableStateListOf() },
            alarms = remember { mutableStateListOf() },
            isEditMode = remember { mutableStateOf(false) },
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGEUNSAVED) },
            parentsLive = MutableLiveData(emptyList()),
            subtasksLive = MutableLiveData(emptyList()),
            subnotesLive = MutableLiveData(emptyList()),
            seriesInstancesLive = MutableLiveData(emptyList()),
            seriesElement = null,
            isChildLive = MutableLiveData(false),
            player = null,
            sliderIncrement = 10,
            showProgressForMainTasks = true,
            showProgressForSubTasks = true,
            keepStatusProgressCompletedInSync = true,
            linkProgressToSubtasks = false,
            isSubtaskDragAndDropEnabled = true,
            isSubnoteDragAndDropEnabled = true,
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            scrollToSectionState = remember { mutableStateOf(null) },
            allWriteableCollectionsLive = MutableLiveData(listOf(ICalCollection.createLocalCollection(LocalContext.current))),
            allCategoriesLive = MutableLiveData(emptyList()),
            allResourcesLive = MutableLiveData(emptyList()),
            storedCategories = emptyList(),
            storedResources = emptyList(),
            extendedStatuses = emptyList(),
            detailSettings = detailSettings,
            icalObjectIdList = emptyList(),
            saveEntry = { },
            onProgressChanged = { _, _ -> },
            onMoveToNewCollection = { },
            onSubEntryAdded = { _, _ -> },
            onSubEntryDeleted = { },
            onSubEntryUpdated = { _, _ -> },
            goToDetail = { _, _, _, _ -> },
            goBack = { },
            unlinkFromSeries = { _, _, _ -> },
            onUnlinkSubEntry = { _, _ ->  },
            goToFilteredList = { }, 
            onShowLinkExistingDialog = { _, _ -> },
            onUpdateSortOrder = { }
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

        val detailSettings = DetailSettings()

        DetailScreenContent(
            observedICalEntity = remember { mutableStateOf(entity) },
            iCalObject = entity.property,
            categories = remember { mutableStateListOf() },
            resources = remember { mutableStateListOf() },
            attendees = remember { mutableStateListOf() },
            comments = remember { mutableStateListOf() },
            attachments = remember { mutableStateListOf() },
            alarms = remember { mutableStateListOf() },
            isEditMode = remember { mutableStateOf(true) },
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVING) },
            parentsLive = MutableLiveData(emptyList()),
            subtasksLive = MutableLiveData(emptyList()),
            subnotesLive = MutableLiveData(emptyList()),
            seriesInstancesLive = MutableLiveData(emptyList()),
            seriesElement = null,
            isChildLive = MutableLiveData(false),
            player = null,
            allWriteableCollectionsLive = MutableLiveData(listOf(ICalCollection.createLocalCollection(LocalContext.current))),
            allCategoriesLive = MutableLiveData(emptyList()),
            allResourcesLive = MutableLiveData(emptyList()),
            storedCategories = emptyList(),
            storedResources = emptyList(),
            extendedStatuses = emptyList(),
            detailSettings = detailSettings,
            icalObjectIdList = emptyList(),
            sliderIncrement = 10,
            showProgressForMainTasks = true,
            showProgressForSubTasks = true,
            keepStatusProgressCompletedInSync = true,
            linkProgressToSubtasks = false,
            isSubtaskDragAndDropEnabled = true,
            isSubnoteDragAndDropEnabled = true,
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            scrollToSectionState = remember { mutableStateOf(null) },
            saveEntry = { },
            onProgressChanged = { _, _ -> },
            onMoveToNewCollection = { },
            onSubEntryAdded = { _, _ -> },
            onSubEntryDeleted = { },
            onSubEntryUpdated = { _, _ -> },
            goToDetail = { _, _, _, _ -> },
            goBack = { },
            unlinkFromSeries = { _, _, _ -> },
            onUnlinkSubEntry = { _, _ ->  },
            goToFilteredList = { },
            onShowLinkExistingDialog = { _, _ -> },
            onUpdateSortOrder = { }
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

        val detailSettings = DetailSettings()

        DetailScreenContent(
            observedICalEntity = remember { mutableStateOf(entity) },
            iCalObject = entity.property,
            categories = remember { mutableStateListOf() },
            resources = remember { mutableStateListOf() },
            attendees = remember { mutableStateListOf() },
            comments = remember { mutableStateListOf() },
            attachments = remember { mutableStateListOf() },
            alarms = remember { mutableStateListOf() },
            isEditMode = remember { mutableStateOf(true) },
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVING) },
            parentsLive = MutableLiveData(emptyList()),
            subtasksLive = MutableLiveData(emptyList()),
            subnotesLive = MutableLiveData(emptyList()),
            seriesInstancesLive = MutableLiveData(emptyList()),
            seriesElement = null,
            isChildLive = MutableLiveData(true),
            player = null,
            allWriteableCollectionsLive = MutableLiveData(listOf(ICalCollection.createLocalCollection(LocalContext.current))),
            allCategoriesLive = MutableLiveData(emptyList()),
            allResourcesLive = MutableLiveData(emptyList()),
            storedCategories = emptyList(),
            storedResources = emptyList(),
            extendedStatuses = emptyList(),
            detailSettings = detailSettings,
            icalObjectIdList = emptyList(),
            sliderIncrement = 10,
            showProgressForMainTasks = false,
            showProgressForSubTasks = false,
            keepStatusProgressCompletedInSync = true,
            linkProgressToSubtasks = false,
            isSubtaskDragAndDropEnabled = true,
            isSubnoteDragAndDropEnabled = true,
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            scrollToSectionState = remember { mutableStateOf(null) },
            saveEntry = { },
            onProgressChanged = { _, _ -> },
            onMoveToNewCollection = { },
            onSubEntryAdded = { _, _ -> },
            onSubEntryDeleted = { },
            onSubEntryUpdated = { _, _ -> },
            goToDetail = { _, _, _, _ -> },
            goBack = { },
            unlinkFromSeries = { _, _, _ -> },
            onUnlinkSubEntry = { _, _ ->  },
            goToFilteredList = { },
            onShowLinkExistingDialog = { _, _ -> },
            onUpdateSortOrder = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenContent_failedLoading() {
    MaterialTheme {

        val detailSettings = DetailSettings()

        DetailScreenContent(
            observedICalEntity = remember { mutableStateOf(null) },
            iCalObject = ICalObject.createJournal(),
            categories = remember { mutableStateListOf() },
            resources = remember { mutableStateListOf() },
            attendees = remember { mutableStateListOf() },
            comments = remember { mutableStateListOf() },
            attachments = remember { mutableStateListOf() },
            alarms = remember { mutableStateListOf() },
            isEditMode = remember { mutableStateOf(true) },
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVING) },
            parentsLive = MutableLiveData(emptyList()),
            subtasksLive = MutableLiveData(emptyList()),
            subnotesLive = MutableLiveData(emptyList()),
            seriesInstancesLive = MutableLiveData(emptyList()),
            seriesElement = null,
            isChildLive = MutableLiveData(true),
            player = null,
            allWriteableCollectionsLive = MutableLiveData(listOf(ICalCollection.createLocalCollection(LocalContext.current))),
            allCategoriesLive = MutableLiveData(emptyList()),
            allResourcesLive = MutableLiveData(emptyList()),
            storedCategories = emptyList(),
            storedResources = emptyList(),
            extendedStatuses = emptyList(),
            detailSettings = detailSettings,
            icalObjectIdList = emptyList(),
            sliderIncrement = 10,
            showProgressForMainTasks = true,
            showProgressForSubTasks = true,
            keepStatusProgressCompletedInSync = true,
            linkProgressToSubtasks = false,
            isSubtaskDragAndDropEnabled = true,
            isSubnoteDragAndDropEnabled = true,
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            scrollToSectionState = remember { mutableStateOf(null) },
            saveEntry = { },
            onProgressChanged = { _, _ -> },
            onMoveToNewCollection = { },
            onSubEntryAdded = { _, _ -> },
            onSubEntryDeleted = { },
            onSubEntryUpdated = { _, _ -> },
            goToDetail = { _, _, _, _ -> },
            goBack = { },
            unlinkFromSeries = { _, _, _ -> },
            onUnlinkSubEntry = { _, _ ->  },
            goToFilteredList = { },
            onShowLinkExistingDialog = { _, _ -> },
            onUpdateSortOrder = { }
        )
    }
}

