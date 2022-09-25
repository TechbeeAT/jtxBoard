/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.elements

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun AudioPlaybackElement(
    uri: Uri,
    player: MediaPlayer?,
    modifier: Modifier = Modifier
) {

    var playing by remember { mutableStateOf(false) }
    var position by remember { mutableStateOf(0F) }
    var duration by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    suspend fun updatePosition() {
        val delayInMillis = 25L
        val curDuration = player?.duration

        while (player?.isPlaying == true && curDuration == player.duration) {
            position = player.currentPosition.toFloat()
            delay(delayInMillis)
        }
        player?.stop()
        player?.reset()
        playing = false
        position = 0F
    }

    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {

        SmallFloatingActionButton(
            onClick = {
                try {
                    if (player?.isPlaying == true) {
                        player.stop()
                        playing = false
                    } else {
                        player?.reset()
                        player?.setDataSource(context, uri)
                        player?.prepare()
                        duration = player?.duration ?: 0
                        player?.start()
                        playing = true
                        coroutineScope.launch {
                            updatePosition()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        context.getText(R.string.audio_error_toast),
                        Toast.LENGTH_LONG
                    ).show()
                    Log.w(
                        "AudioPlayer",
                        "Failed starting/stopping playback\n${e.stackTraceToString()}"
                    )
                }
            },
        ) {

            Box(contentAlignment = Alignment.BottomCenter) {
                Crossfade(playing) {
                    Icon(
                        if (!it) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        stringResource(if (!it) R.string.audio_play else R.string.audio_pause)
                    )
                }
            }
        }

        Slider(
            value = position,
            valueRange = 0F..duration.toFloat(),
            steps = duration / 100,
            onValueChange = {
                position = it
                player?.seekTo(it.toInt())
            },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )
    }


}


@Preview(showBackground = true)
@Composable
fun SubnoteCardPreview() {
    MaterialTheme {

        AudioPlaybackElement(
            uri = Uri.parse("www.orf.at"),
            player = null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
