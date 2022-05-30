/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose

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
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.IcalListFragmentDirections
import at.techbee.jtx.ui.theme.JtxBoardTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SubtaskCard(subtask: ICal4List, navController: NavController, modifier: Modifier = Modifier) {

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
                    //TODO
                }
            ),

    ) {


    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {

        //use progress in state!
        var sliderPosition by remember { mutableStateOf(subtask.percent?.toFloat() ?: 0f) }

        Text(
            subtask.summary?:subtask.description?:"",
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .width(150.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Slider(
            value = sliderPosition,
            valueRange = 0F..100F,
            steps = 100,
            onValueChange = { sliderPosition = it },
            modifier = Modifier.weight(1f)
        )
        Text(
            String.format("%.0f%%", sliderPosition),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
        )
        Checkbox(checked = sliderPosition==100f, onCheckedChange = {
            sliderPosition = if(it) 100f else 0f
        })
    }
    }

}

@Preview(showBackground = true)
@Composable
fun SubtaskCardPreview() {
    JtxBoardTheme {
        SubtaskCard(ICal4List.getSample().apply {
            this.component = Component.VTODO.name
            this.module = Module.TODO.name
            this.percent = 34
        }, rememberNavController())
    }
}