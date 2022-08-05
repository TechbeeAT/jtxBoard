/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.cards

import android.content.ActivityNotFoundException
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.util.UiUtil


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsCardLocation(
    location: MutableState<String>,
    geoLat: Double?,
    geoLong: Double?,
    isEditMode: MutableState<Boolean>,
    onLocationUpdated: () -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.location)



    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),) {

            Crossfade(isEditMode) {
                if(!it.value) {

                    Column {

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Place, headline)
                            Text(headline, style = MaterialTheme.typography.titleMedium)
                        }
                        Text(location.value)
                    }
                } else {

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = location.value,
                            leadingIcon = { Icon(Icons.Outlined.EditLocation, headline) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    location.value = ""
                                    /*TODO*/
                                }) {
                                    if (location.value.isNotEmpty())
                                        Icon(
                                            Icons.Outlined.Clear,
                                            stringResource(id = R.string.delete)
                                        )
                                }
                            },
                            singleLine = true,
                            label = { Text(headline) },
                            onValueChange = { newUrl ->
                                location.value = newUrl

                                /* TODO */
                            },
                            colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
                            modifier = Modifier.weight(1f)
                        )
                        
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Outlined.Map, stringResource(id = R.string.location))
                        }
                    }
                }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardLocation_Preview() {
    MaterialTheme {
        DetailsCardLocation(
            location = mutableStateOf("Vienna, Stephansplatz"),
            geoLat = null,
            geoLong = null,
            isEditMode = mutableStateOf(false),
            onLocationUpdated = { /*TODO*/ }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardLocation_Preview_edit() {
    MaterialTheme {
        DetailsCardLocation(
            location = mutableStateOf("Vienna, Stephansplatz"),
            geoLat = null,
            geoLong = null,
            isEditMode = mutableStateOf(true),
            onLocationUpdated = { /*TODO*/ }
        )
    }
}