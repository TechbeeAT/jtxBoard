/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ContactMail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Attendee
import at.techbee.jtx.ui.reusable.dialogs.RequestPermissionDialog
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.util.UiUtil
import com.google.accompanist.permissions.*
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun DetailsCardContact(
    initialContact: String,
    isEditMode: Boolean,
    onContactUpdated: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    // preview would break if rememberPermissionState is used for preview, so we set it to null only for preview!
    val contactsPermissionState = if (!LocalInspectionMode.current) rememberPermissionState(permission = Manifest.permission.READ_CONTACTS) else null

    var contact by rememberSaveable { mutableStateOf(initialContact) }
    val headline = stringResource(id = R.string.contact)

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    var searchContacts = emptyList<Attendee>()


    ElevatedCard(modifier = modifier) {
    Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),) {

            Crossfade(isEditMode) {
                if(!it) {

                    Column {
                        HeadlineWithIcon(icon = Icons.Outlined.ContactMail, iconDesc = headline, text = headline)
                        Text(contact)
                    }
                } else {

                    Column(modifier = Modifier.fillMaxWidth()) {
                        AnimatedVisibility(contact.isNotEmpty() && searchContacts.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {

                                searchContacts.forEach { searchContact ->

                                    if(searchContact.getDisplayString() == contact)
                                        return@forEach

                                    InputChip(
                                        onClick = {
                                            contact = searchContact.getDisplayString()
                                            onContactUpdated(contact)
                                        },
                                        label = { Text(searchContact.getDisplayString()) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.ContactMail, stringResource(id = R.string.contact))
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
                            value = contact,
                            leadingIcon = { Icon(Icons.Outlined.ContactMail, headline) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    contact = ""
                                    onContactUpdated(contact)
                                }) {
                                    AnimatedVisibility(contact.isNotEmpty()) {
                                        Icon(Icons.Outlined.Clear, stringResource(id = R.string.delete))
                                    }
                                }
                            },
                            singleLine = true,
                            label = { Text(headline) },
                            onValueChange = { newValue ->
                                contact = newValue
                                onContactUpdated(contact)

                                coroutineScope.launch {
                                    if(newValue.length >= 3 && contactsPermissionState?.status?.isGranted == true)
                                        searchContacts = UiUtil.getLocalContacts(context, newValue)
                                    else
                                        emptyList<Attendee>()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.dp, Color.Transparent)
                                .bringIntoViewRequester(bringIntoViewRequester)
                        )
                    }
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
fun DetailsCardContact_Preview() {
    MaterialTheme {
        DetailsCardContact(
            initialContact = "John Doe, +1 555 5545",
            isEditMode = false,
            onContactUpdated = {  }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardContact_Preview_edit() {
    MaterialTheme {
        DetailsCardContact(
            initialContact = "John Doe, +1 555 5545",
            isEditMode = true,
            onContactUpdated = {  }
        )
    }
}