/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import at.techbee.jtx.R

enum class SwitchSetting(
    val key: String,
    val icon: @Composable () -> Unit,
    val title: Int,
    val subtitle: Int? = null,
    val default: Boolean
) {
    SETTING_AUTO_EXPAND_SUBTASKS(
        key = "settings_auto_expand_subtasks",
        icon = { Icon(Icons.Outlined.TaskAlt, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_default_expand_subtasks,
        default = false
    ),
    SETTING_AUTO_EXPAND_SUBNOTES(
        key = "settings_auto_expand_subnotes",
        icon = { Icon(Icons.Outlined.Note, null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_default_expand_subnotes,
        default = false
    ),
    SETTING_AUTO_EXPAND_ATTACHMENTS(
        key = "settings_auto_expand_attachments",
        icon = { Icon(
            Icons.Outlined.Attachment, null, modifier = Modifier.padding(
            16.dp
        ))  },
        title = R.string.settings_default_expand_attachments,
        default = false
    ),
    SETTING_SHOW_PROGRESS_FOR_MAINTASKS_IN_LIST(
        key = "settings_show_progress_for_maintasks_in_list",
        icon = { Icon(
            painterResource(id = R.drawable.ic_progress_task), null, modifier = Modifier.padding(
            16.dp
        ))  },
        title = R.string.settings_show_progress_for_maintasks_in_list,
        default = true
    ),
    SETTING_SHOW_PROGRESS_FOR_SUBTASKS(
        key = "settings_show_progress_for_subtasks_in_list",
        icon = { Icon(
            painterResource(id = R.drawable.ic_progress_subtask), null, modifier = Modifier.padding(
            16.dp
        ))  },
        title = R.string.settings_show_progress_for_subtasks_in_list,
        default = true
    ),
    SETTING_SHOW_SUBTASKS_IN_TASKLIST(
        key = "settings_show_subtasks_of_journals_and_todos_in_tasklist",
        icon = { androidx.compose.foundation.layout.Box(modifier = Modifier.size(24.dp).padding(16.dp))  },
        title = R.string.settings_show_subtasks_of_journals_and_todos_in_tasklist,
        default = false
    ),
    SETTING_SHOW_SUBNOTES_IN_NOTESLIST(
        key = "settings_show_subnotes_of_journals_and_tasks_in_noteslist",
        icon = { androidx.compose.foundation.layout.Box(modifier = Modifier.size(24.dp).padding(16.dp))  },
        title = R.string.settings_show_subnotes_of_journals_and_tasks_in_noteslist,
        default = false
    ),
    SETTING_SHOW_SUBJOURNALS_IN_JOURNALLIST(
        key = "settings_show_subjournals_of_notes_and_tasks_in_journallist",
        icon = { androidx.compose.foundation.layout.Box(modifier = Modifier.size(24.dp).padding(16.dp))  },
        title = R.string.settings_show_subjournals_of_notes_and_tasks_in_journallist,
        default = false
    ),
    SETTING_DETAILS_AUTOSAVE(
        key = "settings_details_autosave",
        icon = { Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_details_autosave_while_editing,
        default = true
    )
    ;
    fun save(newSwitchValue: Boolean, context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, newSwitchValue).apply()
    }
}
