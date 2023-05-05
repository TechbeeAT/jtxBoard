package at.techbee.jtx.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredResource
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.reusable.elements.ListBadge


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListTopFlowRow(
    ical4List: ICal4List,
    categories: List<Category>,
    resources: List<Resource>,
    storedCategories: List<StoredCategory>,
    storedResources: List<StoredResource>,
    extendedStatuses: List<ExtendedStatus>,
    modifier: Modifier = Modifier,
    includeJournalDate: Boolean = false
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
    ) {
        ListBadge(
            icon = Icons.Outlined.FolderOpen,
            iconDesc = stringResource(id = R.string.collection),
            text = ical4List.collectionDisplayName,
            containerColor = ical4List.colorCollection?.let { Color(it) } ?: MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        if (includeJournalDate && ical4List.module == Module.JOURNAL.name && ical4List.dtstart != null) {
            ListBadge(
                iconRes = R.drawable.ic_start2,
                iconDesc = stringResource(id = R.string.date),
                text = ICalObject.getDtstartTextInfo(
                    module = Module.JOURNAL,
                    dtstart = ical4List.dtstart,
                    dtstartTimezone = ical4List.dtstartTimezone,
                    context = LocalContext.current
                ),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
        if (ical4List.module == Module.TODO.name && ical4List.dtstart != null) {
            ListBadge(
                iconRes = R.drawable.ic_widget_start,
                iconDesc = stringResource(id = R.string.started),
                text = ICalObject.getDtstartTextInfo(
                    module = Module.TODO,
                    dtstart = ical4List.dtstart,
                    dtstartTimezone = ical4List.dtstartTimezone,
                    context = LocalContext.current
                ),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
        if (ical4List.module == Module.TODO.name && ical4List.due != null) {
            ListBadge(
                iconRes = R.drawable.ic_widget_due,
                iconDesc = stringResource(id = R.string.due),
                text = ICalObject.getDueTextInfo(
                    due = ical4List.due,
                    dueTimezone = ical4List.dueTimezone,
                    percent = ical4List.percent,
                    status = ical4List.status,
                    context = LocalContext.current
                ),
                containerColor = if (ICalObject.isOverdue(
                        ical4List.status,
                        ical4List.percent,
                        ical4List.due,
                        ical4List.dueTimezone
                    ) == true
                ) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        categories.forEach { category ->
            ListBadge(
                icon = Icons.Outlined.Label,
                iconDesc = stringResource(id = R.string.category),
                text = category.text,
                containerColor = StoredCategory.getColorForCategory(category.text, storedCategories) ?: MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        resources.forEach { resource ->
            ListBadge(
                icon = Icons.Outlined.WorkOutline,
                iconDesc = stringResource(id = R.string.resources),
                text = resource.text,
                containerColor = StoredResource.getColorForResource(resource.text?:"", storedResources) ?: MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        AnimatedVisibility(ical4List.xstatus.isNullOrEmpty() && ical4List.status !in listOf(Status.FINAL.status, Status.NO_STATUS.status)) {
            ListBadge(
                icon = Icons.Outlined.PublishedWithChanges,
                iconDesc = stringResource(R.string.status),
                text = Status.getStatusFromString(ical4List.status)?.stringResource?.let { stringResource(id = it) } ?: ical4List.status,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
        AnimatedVisibility(!ical4List.xstatus.isNullOrEmpty()) {
            ListBadge(
                icon = Icons.Outlined.PublishedWithChanges,
                iconDesc = stringResource(R.string.status),
                text = ical4List.xstatus?:"",
                containerColor = ExtendedStatus.getColorForStatus(ical4List.xstatus, extendedStatuses, ical4List.module) ?: MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        AnimatedVisibility(ical4List.classification in listOf(Classification.CONFIDENTIAL.classification, Classification.PRIVATE.classification)) {
            ListBadge(
                icon = Icons.Outlined.AdminPanelSettings,
                iconDesc = stringResource(R.string.classification),
                text = Classification.getClassificationFromString(ical4List.classification)?.stringResource?.let { stringResource(id = it) } ?: ical4List.classification,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        AnimatedVisibility(ical4List.priority in 1..9) {
            ListBadge(
                icon = Icons.Outlined.WorkOutline,
                iconDesc = stringResource(id = R.string.priority),
                text = if (ical4List.priority in 1..9) stringArrayResource(id = R.array.priority)[ical4List.priority!!] else null,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }


        AnimatedVisibility(ical4List.numAttendees > 0) {
            ListBadge(
                icon = Icons.Outlined.Group,
                iconDesc = stringResource(id = R.string.attendees),
                text = ical4List.numAttendees.toString(),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
        AnimatedVisibility(ical4List.numAttachments > 0) {
            ListBadge(
                icon = Icons.Outlined.Attachment,
                iconDesc = stringResource(id = R.string.attachments),
                text = ical4List.numAttachments.toString(),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
        AnimatedVisibility(ical4List.numComments > 0) {
            ListBadge(
                icon = Icons.Outlined.Comment,
                iconDesc = stringResource(id = R.string.comments),
                text = ical4List.numComments.toString(),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
        /*
        AnimatedVisibility(ical4List.numResources > 0) {
            ListBadge(
                icon = Icons.Outlined.WorkOutline,
                iconDesc = stringResource(id = R.string.resources),
                text = ical4List.numResources.toString()
            )
        }
         */
        AnimatedVisibility(ical4List.numAlarms > 0) {
            ListBadge(
                icon = Icons.Outlined.Alarm,
                iconDesc = stringResource(id = R.string.alarms),
                text = ical4List.numAlarms.toString(),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
        AnimatedVisibility(!ical4List.url.isNullOrEmpty()) {
            ListBadge(
                icon = Icons.Outlined.Link,
                iconDesc = stringResource(id = R.string.url),
                text = ical4List.url,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
        AnimatedVisibility(!ical4List.location.isNullOrEmpty()) {
            ListBadge(
                icon = Icons.Outlined.PinDrop,
                iconDesc = stringResource(id = R.string.location),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
        AnimatedVisibility(ical4List.geoLat != null || ical4List.geoLong != null) {
            ListBadge(
                icon = Icons.Outlined.Map,
                iconDesc = stringResource(id = R.string.location),
            )
        }
        AnimatedVisibility(!ical4List.contact.isNullOrEmpty()) {
            ListBadge(
                icon = Icons.Outlined.ContactMail,
                iconDesc = stringResource(id = R.string.contact),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
        AnimatedVisibility(ical4List.numSubtasks > 0) {
            ListBadge(
                icon = Icons.Outlined.TaskAlt,
                iconDesc = stringResource(id = R.string.subtasks),
                text = ical4List.numSubtasks.toString(),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
        AnimatedVisibility(ical4List.numSubnotes > 0) {
            ListBadge(
                icon = Icons.Outlined.Note,
                iconDesc = stringResource(id = R.string.note),
                text = ical4List.numSubnotes.toString(),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }


        AnimatedVisibility(ical4List.isReadOnly) {
            ListBadge(
                iconRes = R.drawable.ic_readonly,
                iconDesc = stringResource(id = R.string.readyonly),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        AnimatedVisibility(ical4List.uploadPending) {
            ListBadge(
                icon = Icons.Outlined.CloudSync,
                iconDesc = stringResource(id = R.string.upload_pending),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        AnimatedVisibility(ical4List.rrule != null || ical4List.recurid != null) {
            ListBadge(
                icon = if(ical4List.rrule != null || (ical4List.recurid != null && ical4List.sequence == 0L)) Icons.Outlined.EventRepeat else null,
                iconRes = if(ical4List.recurid != null && ical4List.sequence > 0L) R.drawable.ic_recur_exception else null,
                iconDesc = stringResource(id = R.string.list_item_recurring),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}




@Preview(showBackground = true)
@Composable
fun ListTopFlowRow_Preview() {
    MaterialTheme {
        ListTopFlowRow(
            ical4List = ICal4List.getSample(),
            categories = listOf(Category(text = "Category"), Category(text = "Test"), Category(text = "Another")),
            resources = listOf(Resource(text = "Resource"), Resource(text = "Projector")),
            storedCategories = listOf(StoredCategory("Test", Color.Cyan.toArgb())),
            storedResources = listOf(StoredResource("Projector", Color.Green.toArgb())),
            extendedStatuses = listOf(ExtendedStatus("Individual", Module.JOURNAL, Status.FINAL, Color.Green.toArgb()))
        )
    }
}
