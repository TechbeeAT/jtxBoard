/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.Manifest
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Attendee
import at.techbee.jtx.database.properties.Role
import at.techbee.jtx.ui.reusable.dialogs.RequestPermissionDialog
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.util.UiUtil
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class,
    ExperimentalPermissionsApi::class, ExperimentalLayoutApi::class
)
@Composable
fun DetailsCardAttendees(
    attendees: SnapshotStateList<Attendee>,
    isEditMode: Boolean,
    onAttendeesUpdated: () -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    // preview would break if rememberPermissionState is used for preview, so we set it to null only for preview!
    val contactsPermissionState = if (!LocalInspectionMode.current) rememberPermissionState(permission = Manifest.permission.READ_CONTACTS) else null

    val searchAttendees = remember { mutableStateListOf<Attendee>() }

    val headline = stringResource(id = R.string.attendees)
    var newAttendee by rememberSaveable { mutableStateOf("") }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            HeadlineWithIcon(icon = Icons.Outlined.Groups, iconDesc = headline, text = headline)

            AnimatedVisibility(attendees.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    attendees.forEach { attendee ->
                        val overflowMenuExpanded = remember { mutableStateOf(false) }

                        if(!isEditMode) {
                            ElevatedAssistChip(
                                onClick = {
                                          if(attendee.caladdress.startsWith("mailto:")) {
                                              val mail = attendee.caladdress.replaceFirst("mailto:", "")
                                              val intent = Intent(Intent.ACTION_SEND)
                                              intent.type = "message/rfc822"
                                              intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mail))
                                              context.startActivity(intent)
                                          }
                                },
                                label = { Text(attendee.getDisplayString()) },
                                leadingIcon = {
                                    if (Role.entries.any { role -> role.name == attendee.role })
                                        Role.valueOf(attendee.role ?: Role.`REQ-PARTICIPANT`.name)
                                            .Icon()
                                    else
                                        Role.`REQ-PARTICIPANT`.Icon()
                                }
                            )
                        } else {
                            InputChip(
                                onClick = { overflowMenuExpanded.value = true },
                                label = { Text(attendee.getDisplayString()) },
                                leadingIcon = {
                                    if (Role.entries.any { role -> role.name == attendee.role })
                                        Role.valueOf(attendee.role ?: Role.`REQ-PARTICIPANT`.name)
                                            .Icon()
                                    else
                                        Role.`REQ-PARTICIPANT`.Icon()
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            attendees.remove(attendee)
                                            onAttendeesUpdated()
                                        },
                                        content = { Icon(Icons.Outlined.Close, stringResource(id = R.string.delete)) },
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                selected = false
                            )
                        }


                        DropdownMenu(
                            expanded = overflowMenuExpanded.value,
                            onDismissRequest = { overflowMenuExpanded.value = false }
                        ) {
                            Role.entries.forEach { role ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                           role.Icon()
                                           Text(stringResource(id = role.stringResource))
                                        }
                                    },
                                    onClick = {
                                        attendee.role = role.name
                                        onAttendeesUpdated()
                                        overflowMenuExpanded.value = false
                                    })
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(isEditMode && newAttendee.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    val possibleAttendeesToSelect = searchAttendees.filter { all ->
                        all.getDisplayString().lowercase()
                            .contains(newAttendee.lowercase()) && attendees.none { existing ->
                            existing.getDisplayString().lowercase() == all.getDisplayString()
                                .lowercase()
                        }
                    }
                    items(possibleAttendeesToSelect) { attendee ->
                        InputChip(
                            onClick = {
                                attendees.add(attendee)
                                onAttendeesUpdated()
                                newAttendee = ""
                                coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                            },
                            label = { Text(attendee.getDisplayString()) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    stringResource(id = R.string.add)
                                )
                            },
                            selected = false,
                            modifier = Modifier.alpha(0.4f)
                        )
                    }
                }
            }


            Crossfade(isEditMode, label = "crossfade_attendee_edit") {
                if (it) {

                    OutlinedTextField(
                        value = newAttendee,
                        leadingIcon = { Icon(Icons.Outlined.Group, headline) },
                        trailingIcon = {
                            if (newAttendee.isNotEmpty()) {
                                IconButton(onClick = {
                                    val newAttendeeObject = if(UiUtil.isValidEmail(newAttendee))
                                        Attendee(caladdress = "mailto:$newAttendee")
                                    else
                                        Attendee(cn = newAttendee)
                                    attendees.add(newAttendeeObject)
                                    onAttendeesUpdated()
                                    newAttendee = ""
                                }) {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        stringResource(id = R.string.add)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        label = { Text(headline) },
                        onValueChange = { newValue ->
                            newAttendee = newValue

                            coroutineScope.launch {
                                searchAttendees.clear()
                                if(newValue.length >= 3 && contactsPermissionState?.status?.isGranted == true)
                                    searchAttendees.addAll(UiUtil.getLocalContacts(context, newValue))
                                bringIntoViewRequester.bringIntoView()
                            }
                        },
                        isError = newAttendee.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.dp, Color.Transparent)
                            .bringIntoViewRequester(bringIntoViewRequester),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            //if(newAttendee.value.isNotEmpty() && attendees.value.none { existing -> existing.getDisplayString() == newAttendee.value } )
                            val newAttendeeObject = if(UiUtil.isValidEmail(newAttendee))
                                Attendee(caladdress = "mailto:$newAttendee")
                            else
                                Attendee(cn = newAttendee)
                            attendees.add(newAttendeeObject)
                            onAttendeesUpdated()
                            newAttendee = ""
                            coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                        })
                    )
                }
            }

            Crossfade(isEditMode, label = "crossfade_attendee_info") {
                if(it) {
                    Text(
                        text = stringResource(id = R.string.details_attendees_processing_info),
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }

    if(contactsPermissionState?.status?.shouldShowRationale == false && !contactsPermissionState.status.isGranted) {   // second part = permission is NOT permanently denied!
        RequestPermissionDialog(
            text = stringResource(id = R.string.edit_fragment_app_permission_message),
            onConfirm = { contactsPermissionState.launchPermissionRequest() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardAttendees_Preview() {
    MaterialTheme {

        DetailsCardAttendees(
            attendees = remember { mutableStateListOf(Attendee(caladdress = "mailto:patrick@techbee.at", cn = "Patrick"), Attendee(caladdress = "mailto:info@techbee.at", cn = "Info")) },
            isEditMode = false,
            onAttendeesUpdated = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardAttendees_Preview_edit() {
    MaterialTheme {
        DetailsCardAttendees(
            attendees = remember { mutableStateListOf(Attendee(caladdress = "mailto:patrick@techbee.at", cn = "Patrick")) },
            isEditMode = true,
            onAttendeesUpdated = { }
        )
    }
}