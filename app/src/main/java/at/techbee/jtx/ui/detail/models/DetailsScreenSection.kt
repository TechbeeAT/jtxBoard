package at.techbee.jtx.ui.detail.models

import androidx.annotation.StringRes
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
}