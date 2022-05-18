/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilePresent
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.ui.theme.JtxBoardTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentCard(attachment: Attachment) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {


            Row(modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()) {

                Icon(Icons.Outlined.FilePresent, stringResource(R.string.attachments))
                Text(attachment.filename + "." + attachment.extension, modifier = Modifier.padding(start = 8.dp))
                // TODO: handle filesize!
                // TODO: handle image!

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
