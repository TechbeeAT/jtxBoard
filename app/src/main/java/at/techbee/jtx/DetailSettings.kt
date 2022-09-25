package at.techbee.jtx

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf


data class DetailSettings(
    val prefs: SharedPreferences
) {

    val enableCategories = mutableStateOf(true)
    val enableAttendees = mutableStateOf(false)
    val enableResources = mutableStateOf(false)
    val enableContact = mutableStateOf(false)
    val enableLocation = mutableStateOf(false)
    val enableUrl = mutableStateOf(false)
    val enableSubtasks = mutableStateOf(true)
    val enableSubnotes = mutableStateOf(true)
    val enableAttachments = mutableStateOf(true)
    val enableRecurrence = mutableStateOf(false)
    val enableAlarms = mutableStateOf(false)
    val enableComments = mutableStateOf(false)

    init {
        load()
    }

    companion object {
        private const val ENABLE_CATEGORIES = "enableCategories"
        private const val ENABLE_ATTENDEES = "enableAttendees"
        private const val ENABLE_RESOURCES = "enableResources"
        private const val ENABLE_CONTACT = "enableContact"
        private const val ENABLE_LOCATION = "enableLocation"
        private const val ENABLE_URL = "enableURL"
        private const val ENABLE_SUBTASKS = "enableSubtasks"
        private const val ENABLE_SUBNOTES = "enableSubnotes"
        private const val ENABLE_ATTACHMENTS = "enableAttachments"
        private const val ENABLE_RECURRENCE = "enableRecurrence"
        private const val ENABLE_ALARMS = "enableAlarms"
        private const val ENABLE_COMMENTS = "enableComments"
    }

    private fun load() {
        enableCategories.value = prefs.getBoolean(ENABLE_CATEGORIES, true)
        enableAttendees.value = prefs.getBoolean(ENABLE_ATTENDEES, false)
        enableResources.value = prefs.getBoolean(ENABLE_RESOURCES, false)
        enableContact.value = prefs.getBoolean(ENABLE_CONTACT, false)
        enableLocation.value = prefs.getBoolean(ENABLE_LOCATION, false)
        enableUrl.value = prefs.getBoolean(ENABLE_URL, false)
        enableSubtasks.value = prefs.getBoolean(ENABLE_SUBTASKS, true)
        enableSubnotes.value = prefs.getBoolean(ENABLE_SUBNOTES, false)
        enableAttachments.value = prefs.getBoolean(ENABLE_ATTACHMENTS, false)
        enableRecurrence.value = prefs.getBoolean(ENABLE_RECURRENCE, false)
        enableAlarms.value = prefs.getBoolean(ENABLE_ALARMS, false)
        enableComments.value = prefs.getBoolean(ENABLE_COMMENTS, false)
    }

    fun save() {
        prefs.edit().apply {
            putBoolean(ENABLE_CATEGORIES, enableCategories.value)
            putBoolean(ENABLE_ATTENDEES, enableAttendees.value)
            putBoolean(ENABLE_RESOURCES, enableResources.value)
            putBoolean(ENABLE_CONTACT, enableContact.value)
            putBoolean(ENABLE_LOCATION, enableLocation.value)
            putBoolean(ENABLE_URL, enableUrl.value)
            putBoolean(ENABLE_SUBTASKS, enableSubtasks.value)
            putBoolean(ENABLE_SUBNOTES, enableSubnotes.value)
            putBoolean(ENABLE_ATTACHMENTS, enableAttachments.value)
            putBoolean(ENABLE_RECURRENCE, enableRecurrence.value)
            putBoolean(ENABLE_ALARMS, enableAlarms.value)
            putBoolean(ENABLE_COMMENTS, enableComments.value)
        }.apply()
    }
}