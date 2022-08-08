/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.cards

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ContactMail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Attendee
import at.techbee.jtx.ui.compose.dialogs.RequestContactsPermissionDialog
import at.techbee.jtx.ui.compose.elements.HeadlineWithIcon
import at.techbee.jtx.util.UiUtil
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailsCardContact(
    contact: MutableState<String>,
    isEditMode: MutableState<Boolean>,
    onContactUpdated: () -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.contact)
    var showContactsPermissionDialog by remember { mutableStateOf(false) }
    var contactsPermissionDialogShown by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {  }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var searchContacts = emptyList<Attendee>()


    ElevatedCard(modifier = modifier) {
    Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),) {

            Crossfade(isEditMode) {
                if(!it.value) {

                    Column {
                        HeadlineWithIcon(icon = Icons.Outlined.ContactMail, iconDesc = headline, text = headline)
                        Text(contact.value)
                    }
                } else {

                    Column(modifier = Modifier.fillMaxWidth()) {
                        AnimatedVisibility(contact.value.isNotEmpty() && searchContacts.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {

                                searchContacts.forEach { searchContact ->
                                    InputChip(
                                        onClick = {
                                            contact.value = searchContact.getDisplayString()
                                        },
                                        label = { Text(searchContact.getDisplayString()) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.ContactMail,
                                                stringResource(id = R.string.contact)
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
                            }
                        }


                        OutlinedTextField(
                            value = contact.value,
                            leadingIcon = { Icon(Icons.Outlined.ContactMail, headline) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    contact.value = ""
                                    /*TODO*/
                                }) {
                                    Icon(Icons.Outlined.Clear, stringResource(id = R.string.delete))
                                }
                            },
                            singleLine = true,
                            label = { Text(headline) },
                            onValueChange = { newValue ->
                                contact.value = newValue

                                coroutineScope.launch {
                                    if(newValue.length >= 3)
                                        searchContacts = UiUtil.getLocalContacts(context, newValue)
                                    else
                                        emptyList<Attendee>()
                                }
                                /* TODO */
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.dp, Color.Transparent)
                                .onFocusChanged { focusState ->
                                    if (focusState.hasFocus && !showContactsPermissionDialog && !contactsPermissionDialogShown) {
                                        showContactsPermissionDialog = true
                                        contactsPermissionDialogShown = true
                                    }
                                }
                                .bringIntoViewRequester(bringIntoViewRequester)
                        )
                    }
                }
            }
        }
    }

    if(showContactsPermissionDialog) {
        RequestContactsPermissionDialog(
            onConfirm = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) },
            onDismiss = { showContactsPermissionDialog = false })
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardContact_Preview() {
    MaterialTheme {
        DetailsCardContact(
            contact = remember { mutableStateOf("John Doe, +1 555 5545") },
            isEditMode = remember { mutableStateOf(false) },
            onContactUpdated = { /*TODO*/ }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardContact_Preview_edit() {
    MaterialTheme {
        DetailsCardContact(
            contact = remember { mutableStateOf("John Doe, +1 555 5545") },
            isEditMode = remember { mutableStateOf(true) },
            onContactUpdated = { /*TODO*/ }
        )
    }
}