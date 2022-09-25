/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddLink
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.ui.reusable.cards.AttachmentCard
import at.techbee.jtx.ui.reusable.dialogs.AddAttachmentLinkDialog
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon


@Composable
fun DetailsCardAttachments(
    initialAttachments: List<Attachment>,
    isEditMode: Boolean,
    isRemoteCollection: Boolean,
    onAttachmentsUpdated: (List<Attachment>) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    var attachments by remember { mutableStateOf(initialAttachments) }

    val pickFileLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Attachment.getNewAttachmentFromUri(uri, context)?.let { newAttachment ->
                attachments = attachments.plus(newAttachment)
                onAttachmentsUpdated(attachments)
            }
        }
    }
    val newPictureUri = remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { taken ->
        if(taken) {
            newPictureUri.value?.let {
                Attachment.getNewAttachmentFromUri(it, context)?.let { newAttachment ->
                    attachments = attachments.plus(newAttachment)
                    newPictureUri.value = null
                    onAttachmentsUpdated(attachments)
                }
            }
        }
    }
    var showAddLinkAttachmentDialog by remember { mutableStateOf(false) }
    val headline = stringResource(id = R.string.attachments)

    if(showAddLinkAttachmentDialog) {
        AddAttachmentLinkDialog(
            onConfirm = { attachmentLink ->
                val newAttachment = Attachment(uri = attachmentLink)
                attachments = attachments.plus(newAttachment)
                onAttachmentsUpdated(attachments)
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

            AnimatedVisibility(attachments.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    attachments.asReversed().forEach { attachment ->

                        AttachmentCard(
                            attachment = attachment,
                            isEditMode = isEditMode,
                            isRemoteCollection = isRemoteCollection,
                            onAttachmentDeleted = {
                                attachments = attachments.minus(attachment)
                                onAttachmentsUpdated(attachments)
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(isEditMode) {

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
                    // don't show the button if the device does not have a camera
                    if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                        Button(onClick = {
                            Attachment.getNewAttachmentUriForPhoto(context)?.let {
                                newPictureUri.value = it
                                takePictureLauncher.launch(newPictureUri.value)
                            }
                        }) {
                            Icon(
                                Icons.Outlined.CameraAlt,
                                stringResource(id = R.string.edit_take_picture_button_text)
                            )
                        }
                    }
                    Button(onClick = { showAddLinkAttachmentDialog = true }) {
                        Icon(Icons.Outlined.AddLink, stringResource(id = R.string.edit_add_link_button_text))
                    }
                }
            }

            AnimatedVisibility((isEditMode && attachments.any { (it.getFilesize(context)?:0) > 100000 } && isRemoteCollection) || LocalInspectionMode.current) {
                Text(
                    stringResource(id = R.string.details_attachment_beta_info),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardAttachments_Preview() {
    MaterialTheme {

        DetailsCardAttachments(
            initialAttachments = listOf(Attachment(filename = "test.pdf")),
            isEditMode = false,
            isRemoteCollection = true,
            onAttachmentsUpdated = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardAttachments_Preview_edit() {
    MaterialTheme {
        DetailsCardAttachments(
            initialAttachments = listOf(Attachment(filename = "test.pdf")),
            isEditMode = true,
            isRemoteCollection = true,
            onAttachmentsUpdated = { }
        )
    }
}