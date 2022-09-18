/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.ui.reusable.cards.AttachmentCard
import at.techbee.jtx.ui.reusable.elements.CollectionsSpinner
import java.util.*
import kotlin.collections.ArrayList


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddDialog(
    presetModule: Module?,
    presetText: String = "",
    presetAttachment: Attachment? = null,
    allCollections: List<ICalCollection>,
    onEntrySaved: (newEntry: ICalObject, categories: List<Category>, attachment: Attachment?, editAfterSaving: Boolean) -> Unit,
    onDismiss: () -> Unit
) {

    if(allCollections.isEmpty())   // don't compose if there are no collections
        return

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {  }
    var showAudioPermissionDialog by rememberSaveable { mutableStateOf(false) }
    var currentCollection by rememberSaveable { mutableStateOf(allCollections.first()) }
    // TODO: Load last used collection!

    var currentModule by rememberSaveable { mutableStateOf(presetModule ?: Module.JOURNAL) }
    var currentText by rememberSaveable { mutableStateOf(presetText) }
    var currentAttachment by rememberSaveable { mutableStateOf(presetAttachment) }
    var noTextError by rememberSaveable { mutableStateOf(false) }
    var editAfterSaving by rememberSaveable { mutableStateOf(false) }
    // TODO save in settings


    val sr: SpeechRecognizer? = when {
        LocalInspectionMode.current -> null   // enables preview
        SpeechRecognizer.isRecognitionAvailable(context) -> SpeechRecognizer.createSpeechRecognizer(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(context) -> SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        else -> null
    }
    var srTextResult by remember { mutableStateOf("")}
    var srListening by remember { mutableStateOf(false) }



    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(
            stringResource(id = when(currentModule) {
                Module.JOURNAL -> R.string.menu_list_quick_journal
                Module.NOTE -> R.string.menu_list_quick_note
                Module.TODO -> R.string.menu_list_quick_todo
            }))
                },
        text = {
            Box {

                Column {
                    CollectionsSpinner(
                        collections = allCollections,
                        preselected = allCollections.find { it == currentCollection } ?: allCollections.first(),
                        includeReadOnly = false,
                        includeVJOURNAL = if(currentModule == Module.JOURNAL || currentModule == Module.NOTE) true else null,
                        includeVTODO = if(currentModule == Module.TODO) true else null,
                        onSelectionChanged = { selected -> currentCollection = selected }
                    )

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
                           label = { Text(stringResource(id = R.string.journal))}
                       )
                        FilterChip(
                            selected = currentModule == Module.NOTE,
                            onClick = { currentModule = Module.NOTE },
                            label = { Text(stringResource(id = R.string.note))}
                        )
                        FilterChip(
                            selected = currentModule == Module.TODO,
                            onClick = { currentModule = Module.TODO },
                            label = { Text(stringResource(id = R.string.task))}
                        )
                    }

                    OutlinedTextField(
                        value = currentText,
                        onValueChange = {
                            currentText = it
                            noTextError = false
                                        },
                        label = { Text(stringResource(id = R.string.list_quickadd_dialog_summary_description_hint)) },
                        trailingIcon = {
                            if(sr != null) {
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
                                            override fun onReadyForSpeech(p0: Bundle?) {  }
                                            override fun onBeginningOfSpeech() { srListening = true }
                                            override fun onEndOfSpeech() { srListening = false  }
                                            override fun onRmsChanged(p0: Float) {}
                                            override fun onBufferReceived(p0: ByteArray?) {}
                                            override fun onError(errorCode: Int) { }
                                            override fun onPartialResults(bundle: Bundle?) {
                                                val data: ArrayList<String>? = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                                // the bundle contains multiple possible results with the result of the highest probability on top. We show only the must likely result at position 0.
                                                if (data?.isNotEmpty() == true) {
                                                    srTextResult = data[0]
                                                }
                                            }
                                            override fun onEvent(p0: Int, p1: Bundle?) {}
                                            override fun onResults(bundle: Bundle?) {
                                                srTextResult = ""
                                                val data: ArrayList<String>? = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                                if(currentText.isNotBlank())   // add a return if there is already text present to add it in a new line
                                                    currentText += "\n"
                                                // the bundle contains multiple possible results with the result of the highest probability on top. We show only the must likely result at position 0.
                                                if (data?.isNotEmpty() == true)
                                                    currentText += data[0]
                                            }
                                        })
                                        sr.startListening(srIntent)
                                    }
                                 }) {
                                    Icon(Icons.Outlined.Mic, stringResource(id = R.string.list_quickadd_dialog_sr_start))
                                }
                            }
                        },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth(),
                        isError = noTextError,
                    )

                    Text(stringResource(id = R.string.list_quickadd_dialog_summary_description_helper), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp))

                    AnimatedVisibility(srListening) {
                        Text(stringResource(id = R.string.list_quickadd_dialog_sr_listening), modifier = Modifier.padding(vertical = 4.dp))
                    }
                    AnimatedVisibility(srTextResult.isNotBlank()) {
                        Text(srTextResult, modifier = Modifier.padding(vertical = 8.dp))
                    }

                    AnimatedVisibility(currentAttachment != null) {
                        AttachmentCard(
                            attachment = currentAttachment!!,
                            isEditMode = false,
                            onAttachmentDeleted = { /* no editing here */ },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(id = R.string.save_and_edit), modifier = Modifier.padding(8.dp))
                        Switch(
                            checked = editAfterSaving,
                            onCheckedChange = { editAfterSaving = it },
                            thumbContent = { Icons.Outlined.Edit }
                        )
                    }

                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if(currentText.isNotBlank()) {
                        val newICalObject = when(currentModule) {
                            Module.JOURNAL -> ICalObject.createJournal()
                            Module.NOTE -> ICalObject.createNote()
                            Module.TODO -> ICalObject.createTodo().apply {
                                this.setDefaultDueDateFromSettings(context)
                                this.setDefaultStartDateFromSettings(context)
                            }
                        }
                        newICalObject.collectionId = currentCollection.collectionId
                        newICalObject.parseSummaryAndDescription(currentText)
                        newICalObject.parseURL(currentText)
                        val categories = Category.extractHashtagsFromText(currentText)
                        onEntrySaved(newICalObject, categories, currentAttachment, editAfterSaving)
                        onDismiss()
                    } else {
                        noTextError = true
                    }
                }
            ) {
                Text(stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text( stringResource(id = R.string.cancel))
            }
        }
    )

    if(showAudioPermissionDialog) {
        RequestAudioPermissionDialog(
            onConfirm = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
            onDismiss = { showAudioPermissionDialog = false })
    }
}

@Preview(showBackground = true)
@Composable
fun QuickAddDialog_Preview() {

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

        QuickAddDialog(
            presetModule = Module.JOURNAL,
            allCollections = listOf(collection1, collection2, collection3),
            onDismiss = { },
            onEntrySaved = { _, _, _, _ -> },
            presetText = "This is my preset text",
            presetAttachment = Attachment(filename = "My File.PDF")
        )
    }
}

