/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FilePresent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Attachment


@Composable
fun AttachmentCard(
    attachment: Attachment,
    isEditMode: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    onAttachmentDeleted: () -> Unit
) {

    if (isEditMode.value) {
        OutlinedCard(modifier = modifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {

                val preview = attachment.getPreview(LocalContext.current)
                if (preview == null)
                    Icon(Icons.Outlined.FilePresent, stringResource(R.string.attachments))
                else
                    Image(bitmap = preview.asImageBitmap(), contentDescription = null)
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
        ElevatedCard(modifier = modifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {

                val preview = attachment.getPreview(LocalContext.current)
                if (preview == null)
                    Icon(Icons.Outlined.FilePresent, stringResource(R.string.attachments))
                else
                    Image(bitmap = preview.asImageBitmap(), contentDescription = null)
                Text(
                    attachment.getFilenameOrLink() ?: "",
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .align(alignment = Alignment.CenterVertically),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
            isEditMode = remember { mutableStateOf(false) },
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
            isEditMode = remember { mutableStateOf(true) },
            onAttachmentDeleted = { }
        )
    }
}
