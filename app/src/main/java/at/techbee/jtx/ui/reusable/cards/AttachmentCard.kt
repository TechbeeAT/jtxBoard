/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.cards

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.util.UiUtil
import java.io.IOException


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentCard(
    attachment: Attachment,
    isEditMode: Boolean,
    isRemoteCollection: Boolean,
    withPreview: Boolean,
    modifier: Modifier = Modifier,
    onAttachmentDeleted: () -> Unit
) {

    val context = LocalContext.current
    val preview = attachment.getPreview(context)
    val filesize = attachment.getFilesize(context)

    val resultExportFilepath = remember { mutableStateOf<Uri?>(null) }
    val launcherExportSingle = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(attachment.fmttype?:"*/*")) {
        resultExportFilepath.value = it
    }

    LaunchedEffect(resultExportFilepath.value) {
        resultExportFilepath.value?.let {
            if(attachment.uri?.startsWith("content://") == false)
                return@LaunchedEffect

            try {
                val input = context.contentResolver.openInputStream(Uri.parse(attachment.uri)) ?: return@LaunchedEffect
                val output = context.contentResolver?.openOutputStream(it) ?: return@LaunchedEffect
                input.copyTo(output)
                output.flush()
                output.close()
                input.close()
                Toast.makeText(context, R.string.attachment_export_saved_successfully, Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                Toast.makeText(context, R.string.attachment_export_error_error_occurred, Toast.LENGTH_LONG).show()
            } finally {
                resultExportFilepath.value = null
            }
        }
    }

    if (isEditMode) {
        OutlinedCard(modifier = modifier) {

            if (withPreview) {
                //preview
                if (preview == null)
                    Icon(
                        Icons.Outlined.ImageNotSupported,
                        attachment.fmttype,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 140.dp)
                            .padding(4.dp)
                    )
                else
                    Image(
                        bitmap = preview.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 140.dp)
                            .padding(4.dp)
                    )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    attachment.getFilenameOrLink() ?: "",
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .align(alignment = Alignment.CenterVertically)
                        .weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (LocalInspectionMode.current || ((filesize
                        ?: 0) > 100000 && isRemoteCollection)
                ) {
                    Icon(Icons.Outlined.Warning, null, tint = MaterialTheme.colorScheme.error)
                }

                IconButton(onClick = { onAttachmentDeleted() }) {
                    Icon(Icons.Outlined.Delete, stringResource(id = R.string.delete))
                }
            }
        }
    } else {
        ElevatedCard(
            onClick = { attachment.openFile(context) },
            modifier = modifier
        ) {

            if (withPreview) {
                //preview
                if (preview == null)
                    Icon(
                        Icons.Outlined.ImageNotSupported,
                        attachment.fmttype,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                else
                    Image(
                        bitmap = preview.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    attachment.getFilenameOrLink() ?: "",
                    modifier = Modifier
                        .align(alignment = Alignment.CenterVertically)
                        .weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                filesize?.let {
                    Text(
                        text = UiUtil.getAttachmentSizeString(it),
                        maxLines = 1,
                        fontStyle = FontStyle.Italic
                    )
                }
                if(attachment.uri?.startsWith("content://") == true) {
                    IconButton(onClick = {
                        launcherExportSingle.launch(attachment.filename)
                    }) {
                        Icon(Icons.Outlined.Download, stringResource(id = R.string.save))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AttachmentCardPreview_view() {
    MaterialTheme {
        AttachmentCard(
            attachment = Attachment.getSample(),
            isEditMode = false,
            isRemoteCollection = true,
            withPreview = false,
            onAttachmentDeleted = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AttachmentCardPreview_edit() {
    MaterialTheme {
        AttachmentCard(
            attachment = Attachment.getSample(),
            isEditMode = true,
            isRemoteCollection = true,
            withPreview = false,
            onAttachmentDeleted = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AttachmentCardPreview_view_with_preview() {
    MaterialTheme {
        AttachmentCard(
            attachment = Attachment.getSample(),
            isEditMode = false,
            isRemoteCollection = true,
            withPreview = true,
            onAttachmentDeleted = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AttachmentCardPreview_edit_with_preview() {
    MaterialTheme {
        AttachmentCard(
            attachment = Attachment.getSample(),
            isEditMode = true,
            isRemoteCollection = true,
            withPreview = true,
            onAttachmentDeleted = { }
        )
    }
}