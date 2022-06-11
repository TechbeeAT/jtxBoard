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
import androidx.compose.material.icons.outlined.FilePresent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import at.techbee.jtx.ui.theme.JtxBoardTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentCard(attachment: Attachment) {

    val context = LocalContext.current

    ElevatedCard(
        modifier = Modifier
            .wrapContentSize(align = Alignment.CenterStart)
            .padding(8.dp)
            .widthIn(max = 150.dp),
        onClick = {
            attachment.openFile(context)
        }
    ) {

        Row(modifier = Modifier
            .padding(8.dp)
            .wrapContentWidth(align = Alignment.Start),
        horizontalArrangement = Arrangement.Start) {

            val preview = attachment.getPreview(LocalContext.current)
            if(preview == null)
                Icon(Icons.Outlined.FilePresent, stringResource(R.string.attachments))
            else
                Image(bitmap = preview.asImageBitmap(), contentDescription = null)
            Text(attachment.getFilenameOrLink() ?: "",
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .align(alignment = Alignment.CenterVertically),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AttachmentCardPreview() {
    JtxBoardTheme {
        AttachmentCard(Attachment.getSample())
    }
}
