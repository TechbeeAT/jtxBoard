/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.theme.JtxBoardTheme

@Composable
fun ListStatusBar(numAttendees: Int, numAttachments: Int, numComments: Int, isReadOnly: Boolean, uploadPending: Boolean, isRecurringOriginal: Boolean, isRecurringInstance: Boolean, isLinkedRecurringInstance: Boolean, modifier: Modifier = Modifier ) {
    Row(modifier = modifier) {

        if (numAttendees > 0)
            IconWithText(
                icon = Icons.Outlined.Group,
                iconDesc = stringResource(R.string.attendees),
                text = numAttendees.toString(),
                modifier = Modifier.padding(end = 4.dp)
            )
        if (numAttachments > 0)
            IconWithText(
                icon = Icons.Outlined.Attachment,
                iconDesc = stringResource(R.string.attachments),
                text = numAttachments.toString(),
                modifier = Modifier.padding(end = 4.dp)
            )
        if (numComments > 0)
            IconWithText(
                icon = Icons.Outlined.Comment,
                iconDesc = stringResource(R.string.comments),
                text = numComments.toString(),
                modifier = Modifier.padding(end = 4.dp)
            )

        if (isReadOnly)
            Icon(
                painter = painterResource(id = R.drawable.ic_readonly),
                contentDescription = stringResource(id = R.string.readyonly),
                modifier = Modifier.size(14.dp)
            )
        if (uploadPending)
            Icon(Icons.Outlined.CloudSync, stringResource(R.string.upload_pending), modifier = Modifier.size(14.dp))

        if (isRecurringOriginal || (isRecurringInstance && isLinkedRecurringInstance))
            Icon(
                Icons.Outlined.EventRepeat,
                stringResource(R.string.list_item_recurring),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(14.dp)
            )
        if (isRecurringInstance && !isLinkedRecurringInstance)
            Icon(
                painter = painterResource(R.drawable.ic_recur_exception),
                stringResource(R.string.list_item_recurring),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(14.dp)
            )
    }
}


@Preview(showBackground = true)
@Composable
fun ListStatusBar_Preview1() {
    JtxBoardTheme {
        ListStatusBar(
            numAttendees = 3,
            numAttachments = 4,
            numComments = 11,
            isReadOnly = false,
            uploadPending = true,
            isRecurringOriginal = false,
            isRecurringInstance = false,
            isLinkedRecurringInstance = false
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ListStatusBar_Preview2() {
    JtxBoardTheme {
        ListStatusBar(
            numAttendees = 155,
            numAttachments = 0,
            numComments = 2345,
            isReadOnly = true,
            uploadPending = false,
            isRecurringOriginal = true,
            isRecurringInstance = false,
            isLinkedRecurringInstance = false
        )
    }
}



@Preview(showBackground = true)
@Composable
fun ListStatusBar_Preview3() {
    JtxBoardTheme {
        ListStatusBar(
            numAttendees = 0,
            numAttachments = 0,
            numComments = 0,
            isReadOnly = false,
            uploadPending = true,
            isRecurringOriginal = false,
            isRecurringInstance = true,
            isLinkedRecurringInstance = false
        )
    }
}