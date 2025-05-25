/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import at.techbee.jtx.util.UiUtil
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch


@OptIn(ExperimentalLayoutApi::class, ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
@Composable
fun EditAttendeesDialog(
    initialAttendees: List<Attendee>,
    allAttendees: List<Attendee>,
    onAttendeesUpdated: (List<Attendee>) -> Unit,
    onDismiss: () -> Unit
) {


    val context = LocalContext.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    // preview would break if rememberPermissionState is used for preview, so we set it to null only for preview!
    val contactsPermissionState =
        if (!LocalInspectionMode.current) rememberPermissionState(permission = Manifest.permission.READ_CONTACTS) else null
    val searchAttendees = remember { mutableStateListOf<Attendee>() }


    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }


    val currentAttendees = remember { mutableStateListOf<Attendee>().apply { addAll(initialAttendees) } }
    var newAttendee by rememberSaveable { mutableStateOf("") }

    fun addAttendee() {
        if (newAttendee.isEmpty())
            return

        val newAttendeeObject = if (UiUtil.isValidEmail(newAttendee))
            Attendee(caladdress = "mailto:$newAttendee")
        else
            Attendee(cn = newAttendee)
        currentAttendees.add(newAttendeeObject)
        newAttendee = ""
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Groups, null)
                Text(stringResource(id = R.string.attendees))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                AnimatedVisibility(currentAttendees.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        currentAttendees.forEach { attendee ->

                            val overflowMenuExpanded = remember { mutableStateOf(false) }

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
                                            currentAttendees.remove(attendee)
                                        },
                                        content = {
                                            Icon(
                                                Icons.Outlined.Close,
                                                stringResource(id = R.string.delete)
                                            )
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                selected = false
                            )

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
                                            overflowMenuExpanded.value = false
                                        }
                                    )
                                }
                            }

                        }
                    }
                }

                val possibleAttendeesToSelect = mutableListOf<Attendee>().apply {
                    if (newAttendee.isEmpty()) {
                        addAll(allAttendees)
                    } else {
                        addAll(allAttendees.filter {
                            (it.cn?.contains(newAttendee) == true || it.caladdress.contains(newAttendee))
                                    && currentAttendees.none { current -> current.getDisplayString() == it.getDisplayString() }
                        })
                        addAll(searchAttendees.filter { searched ->
                            searched.getDisplayString().lowercase()
                                .contains(newAttendee.lowercase()) && currentAttendees.none { current ->
                                current.getDisplayString().lowercase() == searched.getDisplayString().lowercase()
                            }
                        })
                    }
                }

                AnimatedVisibility(newAttendee.isNotEmpty() && possibleAttendeesToSelect.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(possibleAttendeesToSelect) { attendee ->
                            InputChip(
                                onClick = {
                                    currentAttendees.add(attendee)
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



                OutlinedTextField(
                    value = newAttendee,
                    leadingIcon = { Icon(Icons.Outlined.Group, null) },
                    trailingIcon = {
                        if (newAttendee.isNotEmpty()) {
                            IconButton(onClick = {
                                addAttendee()
                            }) {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    stringResource(id = R.string.add)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    label = { Text(stringResource(id = R.string.attendee)) },
                    onValueChange = { newValue ->
                        newAttendee = newValue

                        coroutineScope.launch {
                            searchAttendees.clear()
                            if (newValue.length >= 3 && contactsPermissionState?.status?.isGranted == true)
                                searchAttendees.addAll(UiUtil.getLocalContacts(context, newValue))
                            bringIntoViewRequester.bringIntoView()
                        }
                    },
                    isError = newAttendee.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.dp, Color.Transparent)
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        //if(newAttendee.value.isNotEmpty() && attendees.value.none { existing -> existing.getDisplayString() == newAttendee.value } )
                        addAttendee()
                        coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                    })
                )


                //TODO: show only for remote entries!
                Text(
                    text = stringResource(id = R.string.details_attendees_processing_info),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic
                )
            }

            if(contactsPermissionState?.status?.shouldShowRationale == false && !contactsPermissionState.status.isGranted) {   // second part = permission is NOT permanently denied!
                RequestPermissionDialog(
                    text = stringResource(id = R.string.edit_fragment_app_permission_message),
                    onConfirm = { contactsPermissionState.launchPermissionRequest() }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newAttendee.isNotEmpty())
                        addAttendee()
                    onAttendeesUpdated(currentAttendees)
                    onDismiss()
                },
            ) {
                Text(stringResource(id = R.string.save))
            }

        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.cancel))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun EditAttendeesDialog_Preview() {
    MaterialTheme {
        EditAttendeesDialog(
            initialAttendees = listOf(Attendee(cn = "asdf")),
            allAttendees = emptyList(),
            onAttendeesUpdated = {},
            onDismiss = {}
        )
    }
}

