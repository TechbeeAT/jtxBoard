/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.flavored.MapComposable


@Composable
fun LocationPickerDialog(
    initialLocation: String?,
    initialGeoLat: Double?,
    initialGeoLong: Double?,
    enableCurrentLocation: Boolean,
    onConfirm: (String?, Double?, Double?) -> Unit,
    onDismiss: () -> Unit
) {

    var location by remember { mutableStateOf(initialLocation) }
    var lat by remember { mutableStateOf(initialGeoLat) }
    var long by remember { mutableStateOf(initialGeoLong) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.location)) },
        text = {
               MapComposable(
                   initialLocation = location,
                   initialGeoLat = lat,
                   initialGeoLong = long,
                   isEditMode = true,
                   enableCurrentLocation = enableCurrentLocation,
                   onLocationUpdated = { newLocation, newLat, newLong ->
                       location = newLocation
                       lat = newLat
                       long = newLong
                   },
                   modifier = Modifier
                       .fillMaxWidth()
                       .height(400.dp)
                       .padding(top = 8.dp)
               )
               },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(location, lat, long)
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text( stringResource(id = R.string.cancel))
            }
        }
    )
 }

@Preview(showBackground = true)
@Composable
fun LocationPickerDialog_Preview() {
    MaterialTheme {
        LocationPickerDialog(
            initialLocation = null,
            initialGeoLat = null,
            initialGeoLong = null,
            enableCurrentLocation = false,
            onConfirm = { _, _, _ -> },
            onDismiss = { }
        )
    }
}

