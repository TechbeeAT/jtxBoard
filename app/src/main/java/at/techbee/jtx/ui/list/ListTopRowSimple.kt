package at.techbee.jtx.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.ContactMail
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.PublishedWithChanges
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.views.ICal4List
import kotlin.time.Duration.Companion.days


@Composable
fun ListTopRowSimple(
    ical4List: ICal4List,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val db = ICalDatabase.getInstance(context).iCalDatabaseDao()

    val storedCategories by db.getStoredCategories().observeAsState(emptyList())
    //val storedResources by db.getStoredResources().observeAsState(emptyList())
    val extendedStatusesAll by db.getStoredStatuses().observeAsState(emptyList())
    val extendedStatuses = extendedStatusesAll.filter { it.module == ical4List.getModule() }


    val splitCategories = ical4List.categories?.split(", ") ?: emptyList()
    //val splitResources = ical4List.resources?.split(", ") ?: emptyList()

    val defaultIconModifier = Modifier.size(12.dp)

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .alpha(0.5f)
    ) {

        if(ical4List.dtstart != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                if(ical4List.getModule() == Module.JOURNAL) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_start2),
                        contentDescription = stringResource(id = R.string.date),
                        modifier = defaultIconModifier
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_widget_start),
                        contentDescription = stringResource(id = R.string.started),
                        modifier = defaultIconModifier
                    )
                }
                Text(
                    text = ICalObject.getDtstartTextInfo(
                        module = ical4List.getModule(),
                        dtstart = ical4List.dtstart,
                        dtstartTimezone = ical4List.dtstartTimezone,
                        shortStyle = true,
                        context = LocalContext.current
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if(ical4List.due != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_widget_due),
                    contentDescription = stringResource(id = R.string.due),
                    modifier = defaultIconModifier
                )
                Text(
                    text = ICalObject.getDueTextInfo(
                        due = ical4List.due,
                        dueTimezone = ical4List.dueTimezone,
                        percent = ical4List.percent,
                        status = ical4List.status,
                        context = LocalContext.current
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (ICalObject.isOverdue(
                            ical4List.status,
                            ical4List.percent,
                            ical4List.due,
                            ical4List.dueTimezone
                        ) == true
                    ) MaterialTheme.colorScheme.error else Color.Unspecified,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.FolderOpen,
                contentDescription = stringResource(id = R.string.collection),
                modifier = defaultIconModifier,
                tint = ical4List.colorCollection?.let { Color(it) } ?: LocalContentColor.current
            )
            Text(
                text = ical4List.collectionDisplayName ?:"",
                style = MaterialTheme.typography.bodySmall,
                color = ical4List.colorCollection?.let { Color(it) } ?: Color.Unspecified,
                modifier = Modifier.widthIn(max = 50.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if(ical4List.isReadOnly) {
            Icon(
                painter = painterResource(id = R.drawable.ic_readonly),
                contentDescription = stringResource(id = R.string.readyonly),
                modifier = defaultIconModifier
            )
        }

        AnimatedVisibility(ical4List.uploadPending) {
            Icon(
                imageVector = Icons.Outlined.CloudSync,
                contentDescription = stringResource(id = R.string.upload_pending),
                modifier = defaultIconModifier
            )
        }

        AnimatedVisibility(ical4List.rrule != null || (ical4List.recurid != null && ical4List.sequence == 0L)) {
            Icon(
                imageVector = Icons.Outlined.EventRepeat,
                contentDescription = stringResource(id = R.string.list_item_recurring),
                modifier = defaultIconModifier
            )
        }

        AnimatedVisibility(ical4List.recurid != null && ical4List.sequence > 0L) {
            Icon(
                painter = painterResource(id = R.drawable.ic_recur_exception),
                contentDescription = stringResource(id = R.string.list_item_recurring),
                modifier = defaultIconModifier
            )
        }

        splitCategories.forEach { category ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Label,
                    contentDescription = stringResource(id = R.string.category),
                    modifier = defaultIconModifier,
                    tint = StoredCategory.getColorForCategory(category, storedCategories) ?: LocalContentColor.current
                )
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodySmall,
                    color = StoredCategory.getColorForCategory(category, storedCategories) ?: Color.Unspecified
                )
            }
        }

        AnimatedVisibility(ical4List.xstatus.isNullOrEmpty() && ical4List.status !in listOf(Status.FINAL.status, Status.NO_STATUS.status)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.PublishedWithChanges,
                    contentDescription = stringResource(id = R.string.status),
                    modifier = defaultIconModifier
                )
                Text(
                    text = Status.getStatusFromString(ical4List.status)?.stringResource?.let { stringResource(id = it) } ?: ical4List.status?:"",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        AnimatedVisibility(!ical4List.xstatus.isNullOrEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.PublishedWithChanges,
                    contentDescription = stringResource(id = R.string.status),
                    modifier = defaultIconModifier,
                    tint = ExtendedStatus.getColorForStatus(ical4List.xstatus, extendedStatuses, ical4List.module) ?: LocalContentColor.current
                )
                Text(
                    text = ical4List.xstatus?:"",
                    style = MaterialTheme.typography.bodySmall,
                    color = ExtendedStatus.getColorForStatus(ical4List.xstatus, extendedStatuses, ical4List.module) ?: Color.Unspecified
                )
            }
        }

        AnimatedVisibility(ical4List.classification in listOf(Classification.CONFIDENTIAL.classification, Classification.PRIVATE.classification)) {
            Icon(
                imageVector = Icons.Outlined.AdminPanelSettings,
                contentDescription = stringResource(R.string.classification),
                modifier = defaultIconModifier
            )
        }

        AnimatedVisibility(ical4List.priority in 1..9) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {                Icon(
                    imageVector = Icons.Outlined.WorkOutline,
                    contentDescription = stringResource(id = R.string.priority),
                    modifier = defaultIconModifier
                )
                Text(
                    text = ical4List.priority.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        AnimatedVisibility(ical4List.numAttendees > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Group,
                    contentDescription = stringResource(id = R.string.attendees),
                    modifier = defaultIconModifier
                )
                Text(
                    text = ical4List.numAttendees.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        AnimatedVisibility(ical4List.numAttachments > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Attachment,
                    contentDescription = stringResource(id = R.string.attachments),
                    modifier = defaultIconModifier
                )
                Text(
                    text = ical4List.numAttachments.toString(),
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        AnimatedVisibility(ical4List.numComments > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Comment,
                    contentDescription = stringResource(id = R.string.comments),
                    modifier = defaultIconModifier
                )
                Text(
                    text = ical4List.numComments.toString(),
                    style = MaterialTheme.typography.bodySmall)
            }
        }

        AnimatedVisibility(ical4List.numResources > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.WorkOutline,
                    contentDescription = stringResource(id = R.string.resources),
                    modifier = defaultIconModifier
                )
                Text(
                    text = ical4List.numResources.toString(),
                    style = MaterialTheme.typography.bodySmall)
            }
        }

        AnimatedVisibility(ical4List.numAlarms > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Alarm,
                    contentDescription = stringResource(id = R.string.alarms),
                    modifier = defaultIconModifier
                )
                Text(
                    text = ical4List.numAlarms.toString(),
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        AnimatedVisibility(!ical4List.url.isNullOrEmpty()) {
            Icon(
                imageVector = Icons.Outlined.Link,
                contentDescription = stringResource(id = R.string.url),
                modifier = defaultIconModifier
            )
        }
        AnimatedVisibility(!ical4List.location.isNullOrEmpty()) {
            Icon(
                imageVector = Icons.Outlined.Map,
                contentDescription = stringResource(id = R.string.location),
                modifier = defaultIconModifier
            )
        }
        AnimatedVisibility(ical4List.geoLat != null || ical4List.geoLong != null) {
            Icon(
                imageVector = Icons.Outlined.PinDrop,
                contentDescription = stringResource(id = R.string.location),
                modifier = defaultIconModifier
            )
        }
        AnimatedVisibility(!ical4List.contact.isNullOrEmpty()) {
            Icon(
                imageVector = Icons.Outlined.ContactMail,
                contentDescription = stringResource(id = R.string.contact),
                modifier = defaultIconModifier
            )
        }
        AnimatedVisibility(ical4List.numSubtasks > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.TaskAlt,
                    contentDescription = stringResource(id = R.string.subtasks),
                    modifier = defaultIconModifier
                )
                Text(
                    text = ical4List.numSubtasks.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        AnimatedVisibility(ical4List.numSubnotes > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Note,
                    contentDescription = stringResource(id = R.string.note),
                    modifier = defaultIconModifier
                )
                Text(
                    text = ical4List.numSubnotes.toString(),
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun ListTopRowSimple_Preview() {
    MaterialTheme {
        ListTopRowSimple(
            ical4List = ICal4List.getSample().apply {
                module = Module.TODO.name
                dtstart = System.currentTimeMillis()
                due = System.currentTimeMillis()-(1).days.inWholeMilliseconds
            }
        )
    }
}
