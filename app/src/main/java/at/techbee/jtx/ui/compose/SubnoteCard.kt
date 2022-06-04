/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.R
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.IcalListFragmentDirections
import at.techbee.jtx.ui.theme.JtxBoardTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun SubnoteCard(
    subnote: ICal4List,
    navController: NavController,
    onEditRequest: (Long) -> Unit,
    player: MediaPlayer,
) {

    var sliderPosition by remember { mutableStateOf(0f) }
    //var sliderPosition by mutableStateOf(subtask.percent?.toFloat() ?: 0f)
    var playing by remember { mutableStateOf(false) }
    var position by remember { mutableStateOf(0F) }
    var duration by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    suspend fun updatePosition() {
        val delayInMillis = 10L
        val curDuration = player.duration

        while(player.isPlaying && curDuration == player.duration) {
            position = player.currentPosition.toFloat()
            delay(delayInMillis)
        }
        player.stop()
        player.reset()
        playing = false
        position = 0F
    }


    ElevatedCard(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
            .combinedClickable(
                onClick = {
                    navController.navigate(
                        IcalListFragmentDirections
                            .actionIcalListFragmentToIcalViewFragment()
                            .setItem2show(subnote.id)
                    )
                },
                onLongClick = {
                    if (!subnote.isReadOnly && BillingManager.getInstance()?.isProPurchased?.value == true)
                        onEditRequest(subnote.id)
                }
            ),

        ) {

        Column() {

            subnote.audioAttachment?.let {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp)
                ) {
                    IconButton(onClick = {
                        if(player.isPlaying) {
                            player.stop()
                            playing = false
                        }
                        player.reset()
                        player.setDataSource(context, Uri.parse(subnote.audioAttachment))
                        player.prepare()
                        duration = player.duration
                        player.start()
                        playing = true
                        coroutineScope.launch {
                            updatePosition()
                        }
                    }) {
                        AnimatedVisibility(
                            visible = !playing,
                            enter = scaleIn(),
                            exit = scaleOut()
                        ) {
                            Icon(
                                Icons.Outlined.PlayCircle,
                                stringResource(R.string.view_comment_playbutton_content_desc),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        AnimatedVisibility(
                            visible = playing,
                            enter = scaleIn(),
                            exit = scaleOut()
                        ) {
                            Icon(
                                Icons.Outlined.PauseCircle,
                                stringResource(R.string.view_comment_pausebutton_content_desc),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }

                    }
                    Slider(
                        value = position,
                        valueRange = 0F..duration.toFloat(),
                        steps = duration/100,
                        onValueChange = {
                            position = it
                            player.seekTo(it.toInt())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                    )
                }
            }

            if(subnote.summary?.isNotBlank() == true || subnote.description?.isNotBlank() == true) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {

                    Text(
                        subnote.summary ?: subnote.description ?: "",
                        modifier = Modifier
                            .weight(1f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun SubnoteCardPreview() {
    JtxBoardTheme {
        SubnoteCard(
            subnote = ICal4List.getSample().apply {
                this.summary = null
                this.description =
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.isReadOnly = false
            },
            navController = rememberNavController(),
            onEditRequest = { },
            player = MediaPlayer(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubnoteCardPreview_audio() {
    JtxBoardTheme {
        SubnoteCard(
            subnote = ICal4List.getSample().apply {
                this.summary = null
                this.description = null
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.isReadOnly = true
                this.numSubtasks = 7
            },
            navController = rememberNavController(),
            onEditRequest = { },
            player = MediaPlayer(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubnoteCardPreview_audio_with_text() {
    JtxBoardTheme {
        SubnoteCard(
            subnote = ICal4List.getSample().apply {
                this.component = Component.VTODO.name
                this.module = Module.TODO.name
                this.percent = 34
            },
            navController = rememberNavController(),
            onEditRequest = { },
            player = MediaPlayer(),
        )
    }
}