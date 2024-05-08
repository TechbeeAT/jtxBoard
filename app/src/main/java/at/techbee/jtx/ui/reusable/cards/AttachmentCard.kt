/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.cards

import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import at.techbee.jtx.ui.reusable.elements.AudioPlaybackElement
import at.techbee.jtx.util.UiUtil
import java.io.IOException


@Composable
fun AttachmentCard(
    attachment: Attachment,
    isReadOnly: Boolean,
    isRemoteCollection: Boolean,
    player: MediaPlayer?,
    modifier: Modifier = Modifier,
    onAttachmentDeleted: () -> Unit
) {

    val context = LocalContext.current
    val preview = if(LocalInspectionMode.current) null else attachment.getPreview(context)
    val filesize = if(LocalInspectionMode.current) 10000000L else attachment.getFilesize(context)

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

    ElevatedCard(
        onClick = { attachment.openFile(context) },
        modifier = modifier
    ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                //horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (attachment.fmttype?.startsWith("image/") == true) {
                    //preview
                    if (preview == null)
                        Icon(
                            Icons.Outlined.ImageNotSupported,
                            attachment.fmttype,
                            modifier = Modifier
                                //.fillMaxWidth()
                                .heightIn(min = 50.dp, max = 100.dp)
                                .padding(4.dp)
                        )
                    else
                        Image(
                            bitmap = preview.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                //.fillMaxWidth()
                                .heightIn(min = 50.dp, max = 100.dp)
                                .padding(4.dp)
                        )
                } else if((attachment.fmttype?.startsWith("audio/") == true || attachment.fmttype?.startsWith("video/") == true) && attachment.uri != null) {
                    Uri.parse(attachment.uri)?.let {
                        AudioPlaybackElement(
                            uri = it,
                            player = player,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }
                }

                Text(
                    attachment.getFilenameOrLink() ?: "",
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .align(alignment = Alignment.CenterVertically)
                        .weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if(filesize != null) {
                    Text(
                        text = UiUtil.getAttachmentSizeString(filesize),
                        maxLines = 1,
                        fontStyle = FontStyle.Italic,
                        color = if(!isReadOnly && filesize > 100000 && isRemoteCollection) MaterialTheme.colorScheme.error else Color.Unspecified
                    )
                }

                if(attachment.uri?.startsWith("content://") == true) {
                    IconButton(onClick = {
                        launcherExportSingle.launch(attachment.filename)
                    }) {
                        Icon(Icons.Outlined.Download, stringResource(id = R.string.save))
                    }
                }
                if(attachment.uri?.startsWith("http") == true) {
                    IconButton(onClick = {
                        attachment.openFile(context)
                    }) {
                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, stringResource(id = R.string.open_in_browser))
                    }
                }

                if(!isReadOnly) {
                    IconButton(onClick = { onAttachmentDeleted() }) {
                        Icon(Icons.Outlined.Delete, stringResource(id = R.string.delete))
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
            isReadOnly = false,
            isRemoteCollection = true,
            player = null,
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
            isReadOnly = true,
            isRemoteCollection = true,
            player = null,
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
            isReadOnly = false,
            isRemoteCollection = true,
            player = null,
            onAttachmentDeleted = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AttachmentCardPreview_readOnly() {
    MaterialTheme {
        AttachmentCard(
            attachment = Attachment.getSample(),
            isReadOnly = true,
            isRemoteCollection = true,
            player = null,
            onAttachmentDeleted = { }
        )
    }
}