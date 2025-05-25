/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContactMail
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.properties.Attendee
import at.techbee.jtx.database.properties.Role
import at.techbee.jtx.ui.reusable.dialogs.EditAttendeesDialog
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon


@Composable
fun DetailsCardAttendees(
    attendees: SnapshotStateList<Attendee>,
    isReadOnly: Boolean,
    onAttendeesUpdated: (List<Attendee>) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    var showEditAttendeesDialog by rememberSaveable { mutableStateOf(false) }

    if(showEditAttendeesDialog) {
        EditAttendeesDialog(
            initialAttendees = attendees,
            allAttendees = ICalDatabase
                .getInstance(context)
                .iCalDatabaseDao()
                .getAllAttendees()
                .observeAsState(emptyList()).value,
            onAttendeesUpdated = onAttendeesUpdated,
            onDismiss = { showEditAttendeesDialog = false }
        )
    }

    fun launchSendEmailIntent(attendees: List<Attendee>) {

        val emails = attendees
            .filter { it.caladdress.startsWith("mailto:") }
            .map { it.caladdress.replaceFirst("mailto:", "") }
            .toTypedArray()

        if(emails.isEmpty())
            return

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emails))
        context.startActivity(intent)
    }

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            HeadlineWithIcon(icon = Icons.Outlined.Groups, iconDesc = null, text = stringResource(id = R.string.attendees))

            AnimatedVisibility(attendees.isNotEmpty() || !isReadOnly) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    attendees.forEach { attendee ->

                        ElevatedAssistChip(
                            onClick = { launchSendEmailIntent(listOf(attendee)) },
                            label = { Text(attendee.getDisplayString()) },
                            leadingIcon = {
                                if (Role.entries.any { role -> role.name == attendee.role })
                                    Role.valueOf(attendee.role ?: Role.`REQ-PARTICIPANT`.name)
                                        .Icon()
                                else
                                    Role.`REQ-PARTICIPANT`.Icon()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(48.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if(!isReadOnly) {
                            ElevatedAssistChip(
                                onClick = { showEditAttendeesDialog = true },
                                label = {
                                    Icon(
                                        Icons.Outlined.Edit,
                                        stringResource(id = R.string.edit)
                                    )
                                },
                                modifier = Modifier.alpha(0.4f)
                            )
                        }

                        if(attendees.any { it.caladdress.startsWith("mailto:") }) {
                            ElevatedAssistChip(
                                onClick = {
                                    launchSendEmailIntent(attendees)
                                },
                                label = { Icon(
                                    Icons.Outlined.ContactMail,
                                    stringResource(R.string.email_contact)
                                ) },
                                modifier = Modifier.alpha(0.4f)
                            )
                        }
                    }

                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardAttendees_Preview() {
    MaterialTheme {

        DetailsCardAttendees(
            attendees = remember { mutableStateListOf(Attendee(caladdress = "mailto:patrick@techbee.at", cn = "Patrick"), Attendee(caladdress = "mailto:info@techbee.at", cn = "Info")) },
            isReadOnly = false,
            onAttendeesUpdated = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardAttendees_Preview_readonly() {
    MaterialTheme {
        DetailsCardAttendees(
            attendees = remember { mutableStateListOf(Attendee(caladdress = "mailto:patrick@techbee.at", cn = "Patrick")) },
            isReadOnly = true,
            onAttendeesUpdated = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardAttendees_Preview_without_caladdress() {
    MaterialTheme {
        DetailsCardAttendees(
            attendees = remember { mutableStateListOf() },
            isReadOnly = true,
            onAttendeesUpdated = { }
        )
    }
}