/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.cards

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddLink
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.ui.compose.dialogs.AddAttachmentLinkDialog
import at.techbee.jtx.ui.compose.elements.HeadlineWithIcon


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailsCardAttachments(
    attachments: MutableState<List<Attachment>>,
    isEditMode: MutableState<Boolean>,
    onAttachmentsUpdated: () -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    // preview would break if rememberPermissionState is used for preview, so we set it to null only for preview!
    //val contactsPermissionState = if (!LocalInspectionMode.current) rememberPermissionState(permission = Manifest.permission.READ_CONTACTS) else null
    //var showContactsPermissionDialog by rememberSaveable { mutableStateOf(false) }

    val pickFileLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Attachment.getNewAttachmentFromUri(uri, context)?.let { newAttachment ->
                attachments.value = attachments.value.plus(newAttachment)
                //onAttachmentsUpdated()   // TODO
            }
        }
    }
    val newPictureUri = remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { taken ->
        if(taken) {
            newPictureUri.value?.let {
                Attachment.getNewAttachmentFromUri(it, context)?.let { newAttachment ->
                    attachments.value = attachments.value.plus(newAttachment)
                    newPictureUri.value = null
                    //onAttachmentsUpdated()   // TODO
                }
            }
        }
    }
    var showAddLinkAttachmentDialog by remember { mutableStateOf(false) }

    val headline = stringResource(id = R.string.attachments)
    //val newAttendee = remember { mutableStateOf("") }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    if(showAddLinkAttachmentDialog) {
        AddAttachmentLinkDialog(
            onConfirm = { attachmentLink ->
                val newAttachment = Attachment(uri = attachmentLink)
                attachments.value = attachments.value.plus(newAttachment)
                // TODO: Save!
            },
            onDismiss = { showAddLinkAttachmentDialog = false }
        )
    }

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            HeadlineWithIcon(icon = Icons.Outlined.Attachment, iconDesc = headline, text = headline)

            AnimatedVisibility(attachments.value.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    attachments.value.asReversed().forEach { attachment ->

                        AttachmentCard(
                            attachment = attachment,
                            isEditMode = isEditMode,
                            onAttachmentDeleted = { /* TODO */ }
                        )
                    }
                }
            }

            AnimatedVisibility(isEditMode.value) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 8.dp)
                ) {

                    Button(onClick = { pickFileLauncher.launch("*/*") }) {
                        Icon(Icons.Outlined.Upload, stringResource(id = R.string.edit_attachment_button_text))
                    }
                    Button(onClick = {
                        Attachment.getNewAttachmentUriForPhoto(context)?.let {
                            newPictureUri.value = it
                            takePictureLauncher.launch(newPictureUri.value)
                        }
                    }) {
                        Icon(Icons.Outlined.CameraAlt, stringResource(id = R.string.edit_take_picture_button_text))
                    }
                    Button(onClick = { showAddLinkAttachmentDialog = true }) {
                        Icon(Icons.Outlined.AddLink, stringResource(id = R.string.edit_add_link_button_text))
                    }
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardAttachments_Preview() {
    MaterialTheme {

        DetailsCardAttachments(
            attachments = remember { mutableStateOf(listOf(Attachment(filename = "test.pdf"))) },
            isEditMode = remember { mutableStateOf(false) },
            onAttachmentsUpdated = { /*TODO*/ }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardAttachments_Preview_edit() {
    MaterialTheme {
        DetailsCardAttachments(
            attachments = remember { mutableStateOf(listOf(Attachment(filename = "test.pdf"))) },
            isEditMode = remember { mutableStateOf(true) },
            onAttachmentsUpdated = { /*TODO*/ }
        )
    }
}