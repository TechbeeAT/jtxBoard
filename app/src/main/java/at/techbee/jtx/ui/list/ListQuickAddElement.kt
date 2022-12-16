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
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
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
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.ui.reusable.cards.AttachmentCard
import at.techbee.jtx.ui.reusable.dialogs.RequestPermissionDialog
import at.techbee.jtx.ui.reusable.elements.CollectionsSpinner
import java.util.*


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ListQuickAddElement(
    presetModule: Module?,
    modifier: Modifier = Modifier,
    presetText: String = "",
    presetAttachment: Attachment? = null,
    allWriteableCollections: List<ICalCollection>,
    presetCollectionId: Long,
    onSaveEntry: (newEntry: ICalObject, categories: List<Category>, attachment: Attachment?, editAfterSaving: Boolean) -> Unit,
    onDismiss: () -> Unit
) {

    if (allWriteableCollections.isEmpty())   // don't compose if there are no collections
        return

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    var showAudioPermissionDialog by rememberSaveable { mutableStateOf(false) }
    var currentCollection by rememberSaveable {
        mutableStateOf(
            allWriteableCollections.find { collection -> collection.collectionId == presetCollectionId }
                ?: allWriteableCollections.firstOrNull { collection -> !collection.readonly })
    }
    // TODO: Load last used collection!

    var currentModule by rememberSaveable {
        mutableStateOf(
            if(presetModule!= null
                && ((presetModule == Module.JOURNAL && currentCollection?.supportsVJOURNAL == true)
                        || (presetModule == Module.NOTE && currentCollection?.supportsVJOURNAL == true)
                        || (presetModule == Module.TODO && currentCollection?.supportsVTODO == true))
                )
                presetModule
            else if (currentCollection?.supportsVJOURNAL == true)
                Module.JOURNAL
            else if (currentCollection?.supportsVTODO == true)
                Module.TODO
            else
                null
        )
    }
    var currentText by remember { mutableStateOf(TextFieldValue(text = presetText, selection = TextRange(presetText.length))) }
    val currentAttachment by rememberSaveable { mutableStateOf(presetAttachment) }
    var noTextError by rememberSaveable { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    val sr: SpeechRecognizer? = when {
        LocalInspectionMode.current -> null   // enables preview
        SpeechRecognizer.isRecognitionAvailable(context) -> SpeechRecognizer.createSpeechRecognizer(
            context
        )
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(
            context
        ) -> SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        else -> null
    }
    var srTextResult by remember { mutableStateOf("") }
    var srListening by remember { mutableStateOf(false) }

    fun saveEntry(goToEdit: Boolean) {
        if(currentCollection == null || currentModule == null)
            return

        if (currentText.text.isNotBlank()) {
            val newICalObject = when (currentModule) {
                Module.JOURNAL -> ICalObject.createJournal().apply {
                    this.setDefaultJournalDateFromSettings(context)
                }
                Module.NOTE -> ICalObject.createNote()
                Module.TODO -> ICalObject.createTodo().apply {
                    this.setDefaultDueDateFromSettings(context)
                    this.setDefaultStartDateFromSettings(context)
                }
                else -> ICalObject.createNote()  // Fallback, can't actually reach it
            }
            newICalObject.collectionId = currentCollection!!.collectionId
            newICalObject.parseSummaryAndDescription(currentText.text)
            newICalObject.parseURL(currentText.text)
            val categories = Category.extractHashtagsFromText(currentText.text)
            onSaveEntry(newICalObject, categories, currentAttachment, goToEdit)
            currentText = TextFieldValue(text = "")
            if(goToEdit)
                onDismiss()
        } else {
            noTextError = true
        }
    }

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),   // Workaround due to Google Issue: https://issuetracker.google.com/issues/194911971?pli=1
        onDismissRequest = onDismiss,
        text = {
            Column(modifier = modifier) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .align(Alignment.CenterHorizontally)
                ) {
                    FilterChip(
                        selected = currentModule == Module.JOURNAL,
                        onClick = { currentModule = Module.JOURNAL },
                        label = { Text(stringResource(id = R.string.journal)) },
                        enabled = currentCollection?.supportsVJOURNAL == true
                    )
                    FilterChip(
                        selected = currentModule == Module.NOTE,
                        onClick = { currentModule = Module.NOTE },
                        label = { Text(stringResource(id = R.string.note)) },
                        enabled = currentCollection?.supportsVJOURNAL == true
                    )
                    FilterChip(
                        selected = currentModule == Module.TODO,
                        onClick = { currentModule = Module.TODO },
                        label = { Text(stringResource(id = R.string.task)) },
                        enabled = currentCollection?.supportsVTODO == true
                    )
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
                    onValueChange = {
                        currentText = it
                        noTextError = false
                    },
                    label = { Text(stringResource(id = R.string.list_quickadd_dialog_summary_description_hint)) },
                    trailingIcon = {
                        if (sr != null) {
                            IconButton(onClick = {

                                // Check if the permission to record audio is already granted, otherwise make a dialog to ask for permission
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                                    showAudioPermissionDialog = true
                                else {
                                    val srIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                                    srIntent.putExtra(
                                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                    )
                                    srIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                    srIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

                                    sr.setRecognitionListener(object : RecognitionListener {
                                        override fun onReadyForSpeech(p0: Bundle?) {}
                                        override fun onBeginningOfSpeech() {
                                            srListening = true
                                        }

                                        override fun onEndOfSpeech() {
                                            srListening = false
                                        }

                                        override fun onRmsChanged(p0: Float) {}
                                        override fun onBufferReceived(p0: ByteArray?) {}
                                        override fun onError(errorCode: Int) { srListening = false }
                                        override fun onPartialResults(bundle: Bundle?) {
                                            val data: ArrayList<String>? =
                                                bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                            // the bundle contains multiple possible results with the result of the highest probability on top. We show only the must likely result at position 0.
                                            if (data?.isNotEmpty() == true) {
                                                srTextResult = data[0]
                                            }
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
                                    srListening = true
                                    sr.startListening(srIntent)
                                }
                            }) {
                                Crossfade(srListening) { listening ->
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
                    isError = noTextError,
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

                currentAttachment?.let {
                    AttachmentCard(
                        attachment = it,
                        isEditMode = false,
                        isRemoteCollection = currentCollection?.accountType != LOCAL_ACCOUNT_TYPE,
                        withPreview = false,
                        onAttachmentDeleted = { /* no editing here */ },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    TextButton(
                        onClick = {
                            onDismiss()
                        },
                        modifier = Modifier.weight(0.3f)
                    ) {
                        Text(stringResource(id = R.string.close), textAlign = TextAlign.Center)
                    }

                    TextButton(
                        onClick = { saveEntry(goToEdit = true) },
                        enabled = currentText.text.isNotEmpty() && currentCollection?.readonly == false,
                        modifier = Modifier.weight(0.4f)
                    ) {
                        Text(stringResource(id = R.string.save_and_edit), textAlign = TextAlign.Center)
                    }

                    TextButton(
                        onClick = {
                            saveEntry(goToEdit = false)
                            onDismiss()
                                  },
                        enabled = currentText.text.isNotEmpty() && currentCollection?.readonly == false,
                        modifier = Modifier.weight(0.3f)
                    ) {
                        Text(stringResource(id = R.string.save), textAlign = TextAlign.Center)
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
            accountType = "LOCAL"
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
            allWriteableCollections = listOf(collection1, collection2, collection3),
            onDismiss = { },
            onSaveEntry = { _, _, _, _ -> },
            presetText = "This is my preset text",
            presetAttachment = Attachment(filename = "My File.PDF"),
            presetCollectionId = 0L,
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
            allWriteableCollections = listOf(collection3),
            onDismiss = { },
            onSaveEntry = { _, _, _, _ -> },
            presetText = "",
            presetAttachment = null,
            presetCollectionId = 0L,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

