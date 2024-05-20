package at.techbee.jtx.ui.detail.models

import androidx.annotation.StringRes
import at.techbee.jtx.R
import at.techbee.jtx.database.Module

enum class DetailsScreenSection(
    @StringRes val stringRes: Int
) {
    COLLECTION(R.string.collection),
    DATES(R.string.date),
    SUMMARY(R.string.summary),
    DESCRIPTION(R.string.description),
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
                Module.JOURNAL -> listOf(COLLECTION, DATES, SUMMARY, DESCRIPTION, STATUSCLASSIFICATIONPRIORITY, CATEGORIES, PARENTS, SUBTASKS, SUBNOTES, ATTENDEES, CONTACT, URL, LOCATION, COMMENTS, ATTACHMENTS, RECURRENCE)
                Module.NOTE -> listOf(COLLECTION, SUMMARY, DESCRIPTION, STATUSCLASSIFICATIONPRIORITY, CATEGORIES, PARENTS, SUBTASKS, SUBNOTES, ATTENDEES, CONTACT, URL, LOCATION, COMMENTS, ATTACHMENTS)
                Module.TODO -> listOf(COLLECTION, DATES, SUMMARY, DESCRIPTION, PROGRESS, STATUSCLASSIFICATIONPRIORITY, CATEGORIES, PARENTS, SUBTASKS, SUBNOTES, RESOURCES, ATTENDEES, CONTACT, URL, LOCATION, COMMENTS, ATTACHMENTS, ALARMS, RECURRENCE)
            }
        }
    }
}