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
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ContactMail
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import at.techbee.jtx.ui.reusable.dialogs.RequestPermissionDialog
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.util.UiUtil
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun DetailsCardContact(
    initialContact: String,
    isReadOnly: Boolean,
    onContactUpdated: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    // preview would break if rememberPermissionState is used for preview, so we set it to null only for preview!
    val contactsPermissionState = if (!LocalInspectionMode.current) rememberPermissionState(permission = Manifest.permission.READ_CONTACTS) else null
    val focusRequester = remember { FocusRequester() }
    var checkPermission by remember { mutableStateOf(false) }

    var contact by rememberSaveable { mutableStateOf(initialContact) }
    val headline = stringResource(id = R.string.contact)

    val coroutineScope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val searchContacts = remember { mutableStateListOf<Attendee>() }

    val foundTelephoneNumber = UiUtil.extractTelephoneNumbers(contact).firstOrNull()
    val foundEmail = UiUtil.extractEmailAddresses(contact).firstOrNull()

    ElevatedCard(
        modifier = modifier,
        onClick = { focusRequester.requestFocus() }
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {


                    Row {
                        Column(modifier = Modifier.weight(1f)) {
                            HeadlineWithIcon(
                                icon = Icons.Outlined.ContactMail,
                                iconDesc = headline,
                                text = headline
                            )



                            BasicTextField(
                                value = contact,
                                textStyle = LocalTextStyle.current,
                                onValueChange = { newValue ->
                                    contact = newValue
                                    onContactUpdated(contact)

                                    coroutineScope.launch {
                                        searchContacts.clear()
                                        if(newValue.length >= 3 && contactsPermissionState?.status?.isGranted == true)
                                            searchContacts.addAll(UiUtil.getLocalContacts(context, newValue))
                                    }
                                },
                                enabled = !isReadOnly,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { searchContacts.clear() }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .focusRequester(focusRequester)
                                    .onFocusChanged { focusState ->
                                        if (focusState.hasFocus)
                                            checkPermission = true
                                    }
                            )

                            AnimatedVisibility(searchContacts.isNotEmpty()) {
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
                        }

                        AnimatedVisibility(!foundTelephoneNumber.isNullOrEmpty()) {
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$foundTelephoneNumber")
                                }
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                }
                            }) {
                                Icon(Icons.Outlined.Call, stringResource(
                                    id = R.string.call_contact,
                                    foundTelephoneNumber?:""
                                ))
                            }
                        }

                        AnimatedVisibility(!foundEmail.isNullOrEmpty()) {
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_SEND)
                                intent.type = "message/rfc822"
                                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(foundEmail))
                                context.startActivity(intent)
                            }) {
                                Icon(Icons.Outlined.Mail, stringResource(
                                    id = R.string.email_contact,
                                    foundEmail?:""
                                ))
                            }
                        }

            }
        }
    }

    if(checkPermission && contactsPermissionState?.status?.shouldShowRationale == false && !contactsPermissionState.status.isGranted) {   // second part = permission is NOT permanently denied!
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
            isReadOnly = false,
            onContactUpdated = {  }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardContact_Preview_tel() {
    MaterialTheme {
        DetailsCardContact(
            initialContact = "John Doe, +43 676 12 34 567, john@doe.com",
            isReadOnly = false,
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
            isReadOnly = true,
            onContactUpdated = {  }
        )
    }
}