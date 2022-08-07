/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.cards

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ContactMail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.compose.elements.HeadlineWithIcon


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsCardContact(
    contact: MutableState<String>,
    isEditMode: MutableState<Boolean>,
    onContactUpdated: () -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.contact)


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
                    OutlinedTextField(
                        value = contact.value,
                        leadingIcon = { Icon(Icons.Outlined.ContactMail, headline) },
                        trailingIcon = {
                            IconButton(onClick = {
                                contact.value = ""
                            /*TODO*/
                            }) {
                                Icon(Icons.Outlined.Clear, stringResource(id = R.string.delete))
                        }},
                        singleLine = true,
                        label = { Text(headline) },
                        onValueChange = {
                                        contact.value = it
                                        /* TODO */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.dp, Color.Transparent)
                    )
                }

            }
        }
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