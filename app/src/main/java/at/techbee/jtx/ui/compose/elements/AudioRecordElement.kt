/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.elements

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import at.techbee.jtx.R
import at.techbee.jtx.settings.DropdownSettingOption
import at.techbee.jtx.ui.compose.dialogs.RequestAudioPermissionDialog
import at.techbee.jtx.ui.compose.stateholder.SettingsStateHolder
import at.techbee.jtx.util.DateTimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException


@Composable
fun AudioRecordElement(
    recorder: MediaRecorder?,
    onRecorded: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxRecordingDurationMillis = 60000

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current


    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    var showAudioPermissionDialog by rememberSaveable { mutableStateOf(false) }

    var recording by remember { mutableStateOf(false) }


    // set default audio format (might be overwritten by settings)
    var audioFileExtension = "3gp"
    var audioOutputFormat = MediaRecorder.OutputFormat.THREE_GPP
    var audioEncoder = MediaRecorder.AudioEncoder.AMR_NB
    val settings = SettingsStateHolder(context)
    when (settings.settingAudioFormat.value) {
        DropdownSettingOption.AUDIO_FORMAT_3GPP -> {
            audioFileExtension = "3gp"
            audioOutputFormat = MediaRecorder.OutputFormat.THREE_GPP
            audioEncoder = MediaRecorder.AudioEncoder.AMR_NB
        }
        DropdownSettingOption.AUDIO_FORMAT_AAC -> {
            audioFileExtension = "aac"
            audioOutputFormat = MediaRecorder.OutputFormat.MPEG_4
            audioEncoder = MediaRecorder.AudioEncoder.AAC
        }
        DropdownSettingOption.AUDIO_FORMAT_OGG -> {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                audioFileExtension = "ogg"
                audioOutputFormat = MediaRecorder.OutputFormat.OGG
                audioEncoder = MediaRecorder.AudioEncoder.OPUS
            }
        }
        else -> {
            audioFileExtension = "3gp"
            audioOutputFormat = MediaRecorder.OutputFormat.THREE_GPP
            audioEncoder = MediaRecorder.AudioEncoder.AMR_NB
        }
    }

    val cachedFilename = Uri.parse("${context.cacheDir}/recorded.$audioFileExtension")

    var secondsRemaining by remember { mutableStateOf(maxRecordingDurationMillis/1000) }
    suspend fun updateTimer() {
        while (recording && secondsRemaining > 0) {
            secondsRemaining -= 1
            delay(1000L)
        }

        try {
            recorder?.reset()
        } catch (e: Exception) {
            Log.d("Recorder", e.stackTraceToString())
        }

        onRecorded(cachedFilename)
        recording = false
        secondsRemaining = maxRecordingDurationMillis/1000

    }

    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = {

                when {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED -> showAudioPermissionDialog = true
                    recording -> {   // recording - we stop here
                        recorder?.reset()
                        recording = false
                        onRecorded(cachedFilename)

                    }
                    else -> {    // not recording - we start recording
                        recorder?.apply {
                            reset()
                            setAudioSource(MediaRecorder.AudioSource.MIC)
                            setOutputFormat(audioOutputFormat)
                            setAudioEncoder(audioEncoder)
                            setOutputFile(cachedFilename.toString())
                            setMaxDuration(maxRecordingDurationMillis)

                            try {
                                prepare()
                            } catch (e: IOException) {
                                Log.e("startRecording()", "prepare() failed\n${e.stackTraceToString()}")
                                recorder.reset()
                            }
                            start()
                        }
                        recording = true
                        coroutineScope.launch {
                            updateTimer()
                        }
                    }
                }
            }) {
            Crossfade(recording) {
                if (it) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Stop, stringResource(R.string.audio_stop))
                        Text(
                            text = DateTimeUtils.getMinutesSecondsFormatted(secondsRemaining),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                else
                    Icon(
                        Icons.Filled.Mic,
                        stringResource(R.string.audio_record)
                    )
            }
        }
    }

    if(showAudioPermissionDialog) {
        RequestAudioPermissionDialog(
            onConfirm = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
            onDismiss = { showAudioPermissionDialog = false })
    }
}


@Preview(showBackground = true)
@Composable
fun AudioRecordElement_Preview() {

    MaterialTheme {
        AudioRecordElement(
            onRecorded = { },
            recorder = null
        )
    }
}

