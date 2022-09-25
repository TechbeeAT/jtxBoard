/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.cards

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.theme.JtxBoardTheme

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtaskCardCompact(
    subtask: ICal4List,
    modifier: Modifier = Modifier,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit
) {

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {

        var subtaskText = subtask.summary?:subtask.description?:""
        if(subtask.numSubtasks > 0)
            subtaskText += " (+${subtask.numSubtasks})"

        Text(
            subtaskText,
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Checkbox(
            checked = subtask.percent == 100,
            onCheckedChange = {
                onProgressChanged(subtask.id, if(subtask.percent == 100) 0 else 100, subtask.isLinkedRecurringInstance)
            },
            enabled = !subtask.isReadOnly,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubtaskCardCompactPreview() {
    JtxBoardTheme {
        SubtaskCardCompact(ICal4List.getSample().apply {
            this.summary = null
            this.description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            this.component = Component.VTODO.name
            this.module = Module.TODO.name
            this.percent = 34
            this.isReadOnly = false
            this.numSubtasks = 0
        },
            onProgressChanged = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubtaskCardCompactPreview_readonly() {
    JtxBoardTheme {
        SubtaskCardCompact(ICal4List.getSample().apply {
            this.component = Component.VTODO.name
            this.module = Module.TODO.name
            this.percent = 34
            this.isReadOnly = true
            this.numSubtasks = 7
        },
            onProgressChanged = { _, _, _ -> }
        )
    }
}
