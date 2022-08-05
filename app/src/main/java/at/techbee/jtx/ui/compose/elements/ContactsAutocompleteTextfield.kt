/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.elements

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContactMail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ContactsAutocompleteTextfield(
    /*
    current: ICalCollection,
    onCollectionChanged: (ICalCollection) -> Unit,
    onDismiss: () -> Unit
     */
    modifier: Modifier = Modifier
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    var contactName by remember { mutableStateOf("") }

    Column(modifier = modifier) {

        OutlinedTextField(
            value = contactName ?: "",
            onValueChange = { contactName = it },
            label = { Text(stringResource(id = R.string.contact)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),
            leadingIcon = { Icon(Icons.Outlined.ContactMail, null) },
            //modifier = Modifier.weight(1f),

        )
    }

}


@Preview(showBackground = true)
@Composable
fun ContactsAutocompleteTextfield_Preview() {
    MaterialTheme {
        ContactsAutocompleteTextfield(
        )
    }
}