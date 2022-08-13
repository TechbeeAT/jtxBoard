/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.cards

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Attendee
import at.techbee.jtx.database.properties.Role
import at.techbee.jtx.ui.compose.dialogs.RequestContactsPermissionDialog
import at.techbee.jtx.ui.compose.elements.HeadlineWithIcon
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.UiUtil
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun DetailsCardAttendees(
    attendees: MutableState<List<Attendee>>,
    isEditMode: MutableState<Boolean>,
    onAttendeesUpdated: () -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    // preview would break if rememberPermissionState is used for preview, so we set it to null only for preview!
    val contactsPermissionState = if (!LocalInspectionMode.current) rememberPermissionState(permission = Manifest.permission.READ_CONTACTS) else null
    var showContactsPermissionDialog by rememberSaveable { mutableStateOf(false) }

    var searchAttendees = emptyList<Attendee>()

    val headline = stringResource(id = R.string.attendees)
    val newAttendee = remember { mutableStateOf("") }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            HeadlineWithIcon(icon = Icons.Outlined.Groups, iconDesc = headline, text = headline)

            AnimatedVisibility(attendees.value.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    attendees.value.asReversed().forEach { attendee ->

                        val overflowMenuExpanded = remember { mutableStateOf(false) }

                        InputChip(
                            onClick = {
                                if(isEditMode.value)
                                    overflowMenuExpanded.value = true
                                      },
                            label = { Text(attendee.getDisplayString()) },
                            leadingIcon = {
                                if (Role.values().any { role -> role.name == attendee.role })
                                    Role.valueOf(attendee.role ?: Role.`REQ-PARTICIPANT`.name)
                                        .Icon()
                                else
                                    Role.`REQ-PARTICIPANT`.Icon()
                            },
                            trailingIcon = {
                                if (isEditMode.value)
                                    IconButton(
                                        onClick = { attendees.value = attendees.value.filter { it != attendee } },
                                        content = { Icon(Icons.Outlined.Close, stringResource(id = R.string.delete)) },
                                        modifier = Modifier.size(24.dp)
                                    )
                            },
                            selected = false
                        )


                        DropdownMenu(
                            expanded = overflowMenuExpanded.value,
                            onDismissRequest = { overflowMenuExpanded.value = false }
                        ) {
                            Role.values().forEach { role ->
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
                                        attendees.value = attendees.value  //notify
                                        overflowMenuExpanded.value = false
                                        /*TODO: Save it*/
                                    })
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(isEditMode.value && newAttendee.value.isNotEmpty()) {


                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {

                        if(attendees.value.none { existing -> existing.getDisplayString() == newAttendee.value }) {
                            InputChip(
                                onClick = {
                                    val newAttendeeObject = if(DateTimeUtils.isValidEmail(newAttendee.value))
                                        Attendee(caladdress = "mailto:" + newAttendee)
                                    else
                                        Attendee(cn = newAttendee.value)
                                    attendees.value = attendees.value.plus(newAttendeeObject)
                                },
                                label = { Text(newAttendee.value) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        stringResource(id = R.string.add)
                                    )
                                },
                                selected = false,
                                modifier = Modifier.onPlaced {
                                    coroutineScope.launch {
                                        bringIntoViewRequester.bringIntoView()
                                    }
                                }
                            )
                        }

                    searchAttendees.filter { all ->
                        all.getDisplayString().lowercase()
                            .contains(newAttendee.value.lowercase()) && attendees.value.none { existing ->
                            existing.getDisplayString().lowercase() == all.getDisplayString()
                                .lowercase()
                        }
                    }.forEach { attendee ->
                        InputChip(
                            onClick = {
                                attendees.value = attendees.value.plus(attendee)
                            },
                            label = { Text(attendee.getDisplayString()) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    stringResource(id = R.string.add)
                                )
                            },
                            selected = false
                        )
                    }
                }
            }


            Crossfade(isEditMode) {
                if (it.value) {

                    OutlinedTextField(
                        value = newAttendee.value,
                        leadingIcon = { Icon(Icons.Outlined.Group, headline) },
                        trailingIcon = {
                            if (newAttendee.value.isNotEmpty()) {
                                IconButton(onClick = { newAttendee.value = "" }) {
                                    Icon(
                                        Icons.Outlined.Close,
                                        stringResource(id = R.string.delete)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        label = { Text(headline) },
                        onValueChange = { newValue ->
                            newAttendee.value = newValue

                            coroutineScope.launch {
                                if(newValue.length >= 3 && contactsPermissionState?.status?.isGranted == true)
                                    searchAttendees = UiUtil.getLocalContacts(context, newValue)
                                else
                                    emptyList<Attendee>()
                            }
                        },
                        colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.dp, Color.Transparent)
                            .onFocusChanged { focusState ->
                                if (focusState.hasFocus && contactsPermissionState?.status?.shouldShowRationale == false && !contactsPermissionState.status.isGranted) {   // second part = permission is NOT permanently denied!
                                    showContactsPermissionDialog = true
                                }
                            }
                            .bringIntoViewRequester(bringIntoViewRequester),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            //if(newAttendee.value.isNotEmpty() && attendees.value.none { existing -> existing.getDisplayString() == newAttendee.value } )
                            val newAttendeeObject = if(DateTimeUtils.isValidEmail(newAttendee.value))
                                Attendee(caladdress = "mailto:" + newAttendee)
                            else
                                Attendee(cn = newAttendee.value)
                            attendees.value = attendees.value.plus(newAttendeeObject)
                            newAttendee.value = ""
                        })
                    )
                }
            }
        }
    }

    if(showContactsPermissionDialog) {
        RequestContactsPermissionDialog(
            onConfirm = { contactsPermissionState?.launchPermissionRequest() },
            onDismiss = { showContactsPermissionDialog = false })
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardAttendees_Preview() {
    MaterialTheme {

        DetailsCardAttendees(
            attendees = remember { mutableStateOf(listOf(Attendee(caladdress = "mailto:patrick@techbee.at", cn = "Patrick"))) },
            isEditMode = remember { mutableStateOf(false) },
            onAttendeesUpdated = { /*TODO*/ }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardAttendees_Preview_edit() {
    MaterialTheme {
        DetailsCardAttendees(
            attendees = remember { mutableStateOf(listOf(Attendee(caladdress = "mailto:patrick@techbee.at", cn = "Patrick"))) },
            isEditMode = remember { mutableStateOf(true) },
            onAttendeesUpdated = { /*TODO*/ }
        )
    }
}