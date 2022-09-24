/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.util.UiUtil


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentCard(
    attachment: Attachment,
    isEditMode: Boolean,
    modifier: Modifier = Modifier,
    onAttachmentDeleted: () -> Unit
) {

    val context = LocalContext.current
    //val preview = attachment.getPreview(context)
    val filesize = attachment.getFilesize(context)

    if (isEditMode) {
        OutlinedCard(modifier = modifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                /*
                if (preview == null)
                    Icon(Icons.Outlined.FileOpen, attachment.fmttype, modifier = Modifier.width(24.dp))
                else
                    Image(bitmap = preview.asImageBitmap(), contentDescription = null, modifier = Modifier.width(24.dp))
                 */
                Text(
                    attachment.getFilenameOrLink() ?: "",
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .align(alignment = Alignment.CenterVertically)
                        .weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                /*
                if (preview == null)
                    Icon(Icons.Outlined.FileOpen, attachment.fmttype, modifier = Modifier.width(24.dp))
                else
                    Image(bitmap = preview.asImageBitmap(), contentDescription = null, modifier = Modifier.width(24.dp))
                 */

                Text(
                    attachment.getFilenameOrLink() ?: "",
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .align(alignment = Alignment.CenterVertically)
                        .weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                filesize?.let {
                    Text(
                        text = UiUtil.getAttachmentSizeString(it),
                        maxLines = 1,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.secondary
                    )
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
            onAttachmentDeleted = { }
        )
    }
}
