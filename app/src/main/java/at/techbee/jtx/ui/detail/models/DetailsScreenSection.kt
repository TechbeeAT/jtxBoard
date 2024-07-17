package at.techbee.jtx.ui.detail.models

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertComment
import androidx.compose.material.icons.outlined.AlarmAdd
import androidx.compose.material.icons.outlined.AssignmentLate
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.ContactMail
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GppMaybe
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.PublishedWithChanges
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.ViewHeadline
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import at.techbee.jtx.R
import at.techbee.jtx.database.Module

enum class DetailsScreenSection(
    @StringRes val stringRes: Int
) {
    COLLECTION(R.string.collection),
    DATE(R.string.date),
    STARTED(R.string.started),
    DUE(R.string.due),
    COMPLETED(R.string.completed),
    SUMMARY(R.string.summary),
    DESCRIPTION(R.string.description),
    PROGRESS(R.string.progress),
    STATUS(R.string.status),
    CLASSIFICATION(R.string.classification),
    PRIORITY(R.string.priority),
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
                Module.JOURNAL -> listOf(COLLECTION, DATE, SUMMARY, DESCRIPTION, STATUS, CLASSIFICATION, CATEGORIES, PARENTS, SUBTASKS, SUBNOTES, ATTENDEES, CONTACT, URL, LOCATION, COMMENTS, ATTACHMENTS, RECURRENCE)
                Module.NOTE -> listOf(COLLECTION, SUMMARY, DESCRIPTION, STATUS, CLASSIFICATION, CATEGORIES, PARENTS, SUBTASKS, SUBNOTES, ATTENDEES, CONTACT, URL, LOCATION, COMMENTS, ATTACHMENTS)
                Module.TODO -> listOf(COLLECTION, STARTED, DUE, COMPLETED, SUMMARY, DESCRIPTION, PROGRESS, STATUS, CLASSIFICATION, PRIORITY, CATEGORIES, PARENTS, SUBTASKS, SUBNOTES, RESOURCES, ATTENDEES, CONTACT, URL, LOCATION, COMMENTS, ATTACHMENTS, ALARMS, RECURRENCE)
            }
        }
    }

    @Composable
    fun Icon() {
        when(this) {
            COLLECTION -> {
                Icon(Icons.Outlined.FolderOpen, stringResource(id = R.string.collection))
            }
            DATE -> {
                Icon(Icons.Outlined.Today, stringResource(id = R.string.date))
            }
            STARTED -> {
                Icon(painterResource(id = R.drawable.ic_widget_start), stringResource(id = R.string.started))
            }
            DUE -> {
                Icon(painterResource(id = R.drawable.ic_widget_due), stringResource(id = R.string.due))
            }
            COMPLETED -> {
                Icon(Icons.Outlined.DoneAll, stringResource(id = R.string.completed))
            }
            SUMMARY -> {
                Icon(Icons.Outlined.ViewHeadline, stringResource(id = R.string.summary))
            }
            DESCRIPTION -> {
                Icon(Icons.Outlined.Description, stringResource(id = R.string.description))
            }
            PROGRESS -> {
                Icon(Icons.Outlined.Percent, stringResource(id = R.string.progress))
            }
            STATUS -> {
                Icon(Icons.Outlined.PublishedWithChanges, stringResource(id = R.string.status))
            }
            CLASSIFICATION -> {
                Icon(Icons.Outlined.GppMaybe, stringResource(id = R.string.classification))
            }
            PRIORITY -> {
                Icon(Icons.Outlined.AssignmentLate, stringResource(id = R.string.priority))
            }
            CATEGORIES -> {
                Icon(Icons.Outlined.NewLabel, stringResource(id = R.string.categories))
            }
            PARENTS -> {
                //TODO
            }
            SUBTASKS -> {
                //TODO
            }
            SUBNOTES -> {
                //TODO
            }
            RESOURCES -> {
                Icon(Icons.Outlined.WorkOutline, stringResource(id = R.string.resources))
            }
            ATTENDEES -> {
                Icon(Icons.Outlined.Groups, stringResource(id = R.string.attendees))
            }
            CONTACT -> {
                Icon(Icons.Outlined.ContactMail, stringResource(id = R.string.contact))
            }
            URL -> {
                Icon(Icons.Outlined.Link, stringResource(id = R.string.url))
            }
            LOCATION -> {
                Icon(Icons.Outlined.Place, stringResource(id = R.string.location))
            }
            COMMENTS -> {
                Icon(Icons.AutoMirrored.Outlined.InsertComment, stringResource(id = R.string.comments))
            }
            ATTACHMENTS -> {
                Icon(Icons.Outlined.AttachFile, stringResource(id = R.string.attachments))
            }
            ALARMS -> {
                Icon(Icons.Outlined.AlarmAdd, stringResource(id = R.string.alarms))
            }
            RECURRENCE -> {
                Icon(Icons.Outlined.EventRepeat, stringResource(id = R.string.recurrence))
            }
        }
    }
}