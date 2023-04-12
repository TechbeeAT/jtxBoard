/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.ui.reusable.elements.AudioPlaybackElement
import at.techbee.jtx.ui.reusable.elements.AudioRecordElement


@Composable
fun AddAudioEntryDialog(
    module: Module,
    player: MediaPlayer?,
    onConfirm: (newEntry: ICalObject, attachment: Attachment) -> Unit,
    onDismiss: () -> Unit
) {

    var cachedRecordingUri by remember {mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    val newEntry by remember {
        mutableStateOf(
            when (module) {
                Module.JOURNAL -> ICalObject.createJournal()
                Module.NOTE -> ICalObject.createNote()
                Module.TODO -> ICalObject.createTodo()
            }
        )
    }

    @Suppress("DEPRECATION") val recorder = remember {
        if (isPreview)
            null
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            MediaRecorder(context)
        else
            MediaRecorder()
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.dialog_add_audio_entry_title)) },
        properties = DialogProperties(usePlatformDefaultWidth = false),   // Workaround due to Google Issue: https://issuetracker.google.com/issues/194911971?pli=1
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                AudioRecordElement(
                    onRecorded = { uri -> cachedRecordingUri = uri },
                    recorder = recorder
                )

                AnimatedVisibility(cachedRecordingUri != null) {
                    AudioPlaybackElement(
                        uri = cachedRecordingUri!!,
                        player = player,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = cachedRecordingUri != null,
                onClick = {
                    val newAttachment = Attachment.fromCachedRecordingUri(cachedRecordingUri, context)
                    newAttachment?.let { onConfirm(newEntry, it) }
                    recorder?.reset()
                    recorder?.release()
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    try {
                        recorder?.reset()
                        recorder?.release()
                    } catch (e: Exception) {
                        Log.d("Recorder", e.stackTraceToString())
                    }
                    onDismiss()
                }
            ) {
                Text( stringResource(id = R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AddAudioNoteDialog_Preview() {

    MaterialTheme {
        AddAudioEntryDialog(
            module = Module.NOTE,
            player = null,
            onConfirm = { _, _ ->},
            onDismiss = { }
        )
    }
}

