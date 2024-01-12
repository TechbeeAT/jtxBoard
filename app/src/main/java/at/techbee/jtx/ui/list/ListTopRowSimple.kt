package at.techbee.jtx.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.PublishedWithChanges
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.views.ICal4List


@Composable
fun ListTopRowSimple(
    ical4List: ICal4List,
    categories: List<Category>,
    modifier: Modifier = Modifier,
) {

    val defaultIconModifier = Modifier.size(12.dp)

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.horizontalScroll(rememberScrollState())
    ) {


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

        AnimatedVisibility(categories.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Label,
                    contentDescription = stringResource(id = R.string.category),
                    modifier = defaultIconModifier
                )
                Text(
                    text = categories.size.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        AnimatedVisibility(ical4List.xstatus?.isNotEmpty() == true || ical4List.status in listOf(Status.DRAFT.status)) {
            Icon(
                imageVector = Icons.Outlined.PublishedWithChanges,
                contentDescription = stringResource(R.string.status),
                modifier = defaultIconModifier
            )
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
                due = System.currentTimeMillis()
            },
            categories = listOf(Category(text = "Category"), Category(text = "Test"), Category(text = "Another"))
        )
    }
}
