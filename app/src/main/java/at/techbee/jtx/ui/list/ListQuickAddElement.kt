/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.ui.reusable.cards.AttachmentCard
import at.techbee.jtx.ui.reusable.dialogs.RequestPermissionDialog
import at.techbee.jtx.ui.reusable.elements.AudioRecordElement
import at.techbee.jtx.ui.reusable.elements.CollectionsSpinner
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListQuickAddElement(
    presetModule: Module?,
    enabledModules: List<Module>,
    modifier: Modifier = Modifier,
    presetText: String = "",
    presetAttachment: Attachment? = null,
    allWriteableCollections: List<ICalCollection>,
    presetCollectionId: Long,
    player: MediaPlayer?,
    onSaveEntry: (module: Module, newEntryText: String, attachments: List<Attachment>, collectionId: Long, editAfterSaving: Boolean) -> Unit,
    onDismiss: (String) -> Unit,
    keepDialogOpen: () -> Unit
) {

    if (allWriteableCollections.isEmpty())   // don't compose if there are no collections
        return

    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    var showAudioPermissionDialog by rememberSaveable { mutableStateOf(false) }
    var currentCollection by rememberSaveable {
        mutableStateOf(
            allWriteableCollections.find { collection -> collection.collectionId == presetCollectionId }
                ?: allWriteableCollections.firstOrNull { collection -> !collection.readonly })
    }

    var currentModule by rememberSaveable {
        mutableStateOf(
            if(presetModule!= null
                && ((presetModule == Module.JOURNAL && currentCollection?.supportsVJOURNAL == true)
                        || (presetModule == Module.NOTE && currentCollection?.supportsVJOURNAL == true)
                        || (presetModule == Module.TODO && currentCollection?.supportsVTODO == true))
                && enabledModules.contains(presetModule)
            )
                presetModule
            else if (enabledModules.contains(Module.JOURNAL) && currentCollection?.supportsVJOURNAL == true)
                Module.JOURNAL
            else if (enabledModules.contains(Module.NOTE) && currentCollection?.supportsVJOURNAL == true)
                Module.NOTE
            else if (enabledModules.contains(Module.TODO) && currentCollection?.supportsVTODO == true)
                Module.TODO
            else
                null
        )
    }
    var currentText by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue(text = presetText, selection = TextRange(presetText.length))) }
    val currentAttachments = remember { mutableStateListOf(presetAttachment) }
    val focusRequester = remember { FocusRequester() }
    val sr = remember {
        when {
            isPreview -> null   // enables preview
            SpeechRecognizer.isRecognitionAvailable(context) -> SpeechRecognizer.createSpeechRecognizer(
                context
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(
                context
            ) -> SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            else -> null
        }
    }
    var srTextResult by remember { mutableStateOf("") }
    var srListening by remember { mutableStateOf(false) }
    val srIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    LaunchedEffect(Unit) {
        // try catch block as the FocsRequester might not yet be initialized when the focus is requested (see https://github.com/TechbeeAT/jtxBoard/issues/121)
        try { focusRequester.requestFocus() } catch (e: IllegalStateException) { Log.w("ListQuickAddElement", e.stackTraceToString())}

        sr?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {}
            override fun onBeginningOfSpeech() { srListening = true }
            override fun onEndOfSpeech() { srListening = false }
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onError(errorCode: Int) { srListening = false }
            override fun onPartialResults(bundle: Bundle?) {
                val data: ArrayList<String>? = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                // the bundle contains multiple possible results with the result of the highest probability on top. We show only the must likely result at the first position.
                srTextResult = data?.firstOrNull() ?: ""
            }
            override fun onEvent(p0: Int, p1: Bundle?) {}
            override fun onResults(bundle: Bundle?) {
                srListening = false
                srTextResult = ""
                val data: ArrayList<String>? =
                    bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (currentText.text.isNotBlank())   // add a return if there is already text present to add it in a new line
                    currentText = TextFieldValue(currentText.text + System.lineSeparator())
                // the bundle contains multiple possible results with the result of the highest probability on top. We show only the must likely result at position 0.
                if (data?.isNotEmpty() == true) {
                    currentText = TextFieldValue(currentText.text + data[0], selection = TextRange(currentText.text.length + data[0].length))
                    sr.startListening(srIntent)
                    srListening = true
                }
            }
        })
    }


    fun saveEntry(goToEdit: Boolean) {
        if(currentCollection == null || currentModule == null)
            return

        onSaveEntry(currentModule!!, currentText.text, currentAttachments.filterNotNull(), currentCollection!!.collectionId, goToEdit)
        currentText = TextFieldValue(text = "")
        if(goToEdit)
            onDismiss("")
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
        properties = DialogProperties(usePlatformDefaultWidth = false),   // Workaround due to Google Issue: https://issuetracker.google.com/issues/194911971?pli=1
        onDismissRequest = { onDismiss(currentText.text) },
        text = {
            Column(modifier = modifier.verticalScroll(rememberScrollState())) {

                if(currentModule == null || enabledModules.size > 1) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .align(Alignment.CenterHorizontally)
                    ) {
                        if (enabledModules.contains(Module.JOURNAL)) {
                            FilterChip(
                                selected = currentModule == Module.JOURNAL,
                                onClick = { currentModule = Module.JOURNAL },
                                label = { Text(stringResource(id = R.string.journal)) },
                                enabled = currentCollection?.supportsVJOURNAL == true
                            )
                        }
                        if (enabledModules.contains(Module.NOTE)) {
                            FilterChip(
                                selected = currentModule == Module.NOTE,
                                onClick = { currentModule = Module.NOTE },
                                label = { Text(stringResource(id = R.string.note)) },
                                enabled = currentCollection?.supportsVJOURNAL == true
                            )
                        }
                        if (enabledModules.contains(Module.TODO)) {
                            FilterChip(
                                selected = currentModule == Module.TODO,
                                onClick = { currentModule = Module.TODO },
                                label = { Text(stringResource(id = R.string.task)) },
                                enabled = currentCollection?.supportsVTODO == true
                            )
                        }
                    }
                }

                CollectionsSpinner(
                    collections = allWriteableCollections,
                    preselected = allWriteableCollections.find { it == currentCollection } ?: allWriteableCollections.first(),
                    includeReadOnly = false,
                    includeVJOURNAL = if (currentModule == Module.JOURNAL || currentModule == Module.NOTE) true else null,
                    includeVTODO = if (currentModule == Module.TODO) true else null,
                    onSelectionChanged = { selected ->
                        currentCollection = selected
                        if ((currentModule == Module.JOURNAL || currentModule == Module.NOTE) && currentCollection?.supportsVJOURNAL == false)
                            currentModule = Module.TODO
                        else if (currentModule == Module.TODO && currentCollection?.supportsVTODO == false)
                            currentModule = Module.NOTE
                    }
                )

                OutlinedTextField(
                    value = currentText,
                    onValueChange = {currentText = it },
                    label = { Text(stringResource(id = R.string.list_quickadd_dialog_summary_description_hint)) },
                    trailingIcon = {
                        if (sr != null) {
                            IconButton(onClick = {
                                // Check if the permission to record audio is already granted, otherwise make a dialog to ask for permission
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                                    showAudioPermissionDialog = true
                                else if (srListening) {
                                    sr.stopListening()
                                    srListening = false
                                }
                                else {
                                    srListening = true
                                    //sr.stopListening()
                                    sr.startListening(srIntent)
                                }
                            }) {
                                Crossfade(srListening, label = "quickAddSrListening") { listening ->
                                    if (listening)
                                        Icon(Icons.Outlined.Mic, stringResource(id = R.string.list_quickadd_dialog_sr_listening), tint = MaterialTheme.colorScheme.primary)
                                    else
                                        Icon(Icons.Outlined.MicOff, stringResource(id = R.string.list_quickadd_dialog_sr_start))
                                }
                            }
                        }
                    },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Default
                    )
                )

                Text(
                    stringResource(id = R.string.list_quickadd_dialog_summary_description_helper),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                AnimatedVisibility(srTextResult.isNotBlank()) {
                    Text(srTextResult, modifier = Modifier.padding(vertical = 8.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    AudioRecordElement(
                        onRecorded = { cachedRecordingUri ->
                            Attachment.fromCachedRecordingUri(cachedRecordingUri, context)?.let {
                                currentAttachments.add(it)
                            }
                        },
                        recorder = recorder
                    )
                    Text(
                        text = stringResource(R.string.list_quickadd_add_audio_attachment),
                        fontStyle = FontStyle.Italic
                    )

                }


                currentAttachments.forEach { attachment ->
                    if(attachment == null)
                        return@forEach
                    AttachmentCard(
                        attachment = attachment,
                        isEditMode = true,
                        isRemoteCollection = currentCollection?.accountType != LOCAL_ACCOUNT_TYPE,
                        player = player,
                        onAttachmentDeleted = { currentAttachments.remove(attachment) },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {

                        TextButton(
                            onClick = {
                                sr?.destroy()
                                saveEntry(goToEdit = true)
                            },
                            enabled = (currentText.text.isNotEmpty() || currentAttachments.isNotEmpty()) && currentCollection?.readonly == false
                        ) {
                            Text(stringResource(id = R.string.save_and_edit), textAlign = TextAlign.Center)
                        }

                        TextButton(
                            onClick = {
                                saveEntry(goToEdit = false)
                                currentText = TextFieldValue("")
                                currentAttachments.clear()
                                keepDialogOpen()
                            },
                            enabled = (currentText.text.isNotEmpty() || currentAttachments.isNotEmpty()) && currentCollection?.readonly == false
                        ) {
                            Text(stringResource(id = R.string.save_and_new), textAlign = TextAlign.Center)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        TextButton(
                            onClick = {
                                sr?.destroy()
                                saveEntry(goToEdit = false)
                                onDismiss("")
                            },
                            enabled = (currentText.text.isNotEmpty() || currentAttachments.isNotEmpty()) && currentCollection?.readonly == false
                        ) {
                            Text(stringResource(id = R.string.save_and_close), textAlign = TextAlign.Center)
                        }

                        TextButton(
                            onClick = {
                                sr?.destroy()
                                onDismiss("")
                            }
                        ) {
                            Text(stringResource(id = R.string.close), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        },
        confirmButton = { }
    )


    if (showAudioPermissionDialog) {
        RequestPermissionDialog(
            text = stringResource(id = R.string.view_fragment_audio_permission_message),
            onConfirm = {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                showAudioPermissionDialog = false
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListQuickAddElement_Preview() {
    MaterialTheme {
        val collection1 = ICalCollection(
            collectionId = 1L,
            color = Color.Cyan.toArgb(),
            displayName = "Collection Display Name",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL",
            supportsVJOURNAL = true,
            supportsVTODO = true
        )
        val collection2 = ICalCollection(
            collectionId = 2L,
            color = Color.Cyan.toArgb(),
            displayName = "Hmmmm",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )
        val collection3 = ICalCollection(
            collectionId = 3L,
            color = Color.Cyan.toArgb(),
            displayName = null,
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )

        ListQuickAddElement(
            presetModule = Module.JOURNAL,
            enabledModules = Module.entries,
            allWriteableCollections = listOf(collection1, collection2, collection3),
            onDismiss = { },
            onSaveEntry = { _, _, _, _, _ -> },
            keepDialogOpen = { },
            presetText = "This is my preset text",
            presetAttachment = Attachment(filename = "My File.PDF"),
            presetCollectionId = 0L,
            player = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}



@Preview(showBackground = true)
@Composable
fun ListQuickAddElement_Preview_empty() {
    MaterialTheme {

        val collection3 = ICalCollection(
            collectionId = 3L,
            color = Color.Cyan.toArgb(),
            displayName = null,
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL",
            readonly = true
        )

        ListQuickAddElement(
            presetModule = Module.JOURNAL,
            enabledModules = Module.entries,
            allWriteableCollections = listOf(collection3),
            onDismiss = { },
            onSaveEntry = { _, _, _, _, _ -> },
            keepDialogOpen = { },
            presetText = "",
            presetAttachment = null,
            presetCollectionId = 0L,
            player = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ListQuickAddElement_Preview_only_one_enabled() {
    MaterialTheme {

        val collection3 = ICalCollection(
            collectionId = 3L,
            color = Color.Cyan.toArgb(),
            displayName = null,
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL",
            readonly = false,
            supportsVJOURNAL = true
        )

        ListQuickAddElement(
            presetModule = Module.JOURNAL,
            enabledModules = listOf(Module.JOURNAL),
            allWriteableCollections = listOf(collection3),
            onDismiss = { },
            onSaveEntry = { _, _, _, _, _ -> },
            keepDialogOpen = { },
            presetText = "",
            presetAttachment = null,
            presetCollectionId = 0L,
            player = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}
