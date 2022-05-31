/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.IcalListFragmentDirections
import at.techbee.jtx.ui.theme.JtxBoardTheme

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SubtaskCard(
    subtask: ICal4List,
    navController: NavController,
    showProgress: Boolean = true,
    onEditRequest: (Long) -> Unit,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit
) {

    //var sliderPosition by remember { mutableStateOf(subtask.percent?.toFloat() ?: 0f) }
    var sliderPosition by mutableStateOf(subtask.percent?.toFloat() ?: 0f)

    ElevatedCard(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
            .combinedClickable(
                onClick = {
                    navController.navigate(
                        IcalListFragmentDirections
                            .actionIcalListFragmentToIcalViewFragment()
                            .setItem2show(subtask.id)
                    )
                },
                onLongClick = {
                    if(!subtask.isReadOnly && BillingManager.getInstance()?.isProPurchased?.value == true)
                        onEditRequest(subtask.id)
                }
            ),

    ) {


    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {

        //use progress in state!

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
        if(showProgress) {
            Slider(
                value = sliderPosition,
                valueRange = 0F..100F,
                steps = 100,
                onValueChange = { sliderPosition = it },
                onValueChangeFinished = { onProgressChanged(subtask.id, sliderPosition.toInt(), subtask.isLinkedRecurringInstance) },
                modifier = Modifier.width(100.dp),
                enabled = !subtask.isReadOnly
                )
            Text(
                String.format("%.0f%%", sliderPosition),
                modifier = Modifier.padding(start = 8.dp, end = 8.dp)
            )
        }
        Checkbox(
            checked = sliderPosition==100f,
            onCheckedChange = {
                sliderPosition = if(it) 100f else 0f
                onProgressChanged(subtask.id, sliderPosition.toInt(), subtask.isLinkedRecurringInstance)
            },
            enabled = !subtask.isReadOnly
        )
    }
    }

}

@Preview(showBackground = true)
@Composable
fun SubtaskCardPreview() {
    JtxBoardTheme {
        SubtaskCard(ICal4List.getSample().apply {
            this.summary = null
            this.description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            this.component = Component.VTODO.name
            this.module = Module.TODO.name
            this.percent = 34
            this.isReadOnly = false
            this.numSubtasks = 0
        },
            rememberNavController(),
            onEditRequest = {  },
            onProgressChanged = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubtaskCardPreview_readonly() {
    JtxBoardTheme {
        SubtaskCard(ICal4List.getSample().apply {
            this.component = Component.VTODO.name
            this.module = Module.TODO.name
            this.percent = 34
            this.isReadOnly = true
            this.numSubtasks = 7
        },
            rememberNavController(),
            onEditRequest = {  },
            onProgressChanged = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubtaskCardPreview_without_progress() {
    JtxBoardTheme {
        SubtaskCard(ICal4List.getSample().apply {
            this.component = Component.VTODO.name
            this.module = Module.TODO.name
            this.percent = 34
        },
            rememberNavController(),
            showProgress = false,
            onEditRequest = {  },
            onProgressChanged = { _, _, _ -> }
        )
    }
}