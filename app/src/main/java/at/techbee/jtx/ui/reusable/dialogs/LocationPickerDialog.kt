/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.flavored.MapComposable
import at.techbee.jtx.util.UiUtil
import java.text.NumberFormat
import java.text.ParseException


@Composable
fun LocationPickerDialog(
    initialLocation: String?,
    initialGeoLat: Double?,
    initialGeoLong: Double?,
    enableCurrentLocation: Boolean,
    onConfirm: (String?, Double?, Double?) -> Unit,
    onDismiss: () -> Unit
) {

    var location by rememberSaveable { mutableStateOf(initialLocation) }
    var lat by rememberSaveable { mutableStateOf(UiUtil.doubleTo5DecimalString(initialGeoLat)?:"") }
    var long by rememberSaveable { mutableStateOf(UiUtil.doubleTo5DecimalString(initialGeoLong)?:"") }

    val latDouble = try { NumberFormat.getInstance().parse(lat)?.toDouble() } catch (e: ParseException) { null }
    val longDouble = try { NumberFormat.getInstance().parse(long)?.toDouble() } catch (e: ParseException) { null }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.location)) },
        text = {

            Column {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = lat,
                        singleLine = true,
                        label = { Text(stringResource(R.string.latitude)) },
                        onValueChange = { newLat ->
                            lat = newLat
                        },
                        isError = (latDouble != null && longDouble == null) || (latDouble == null && longDouble != null),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = long,
                        singleLine = true,
                        label = { Text(stringResource(R.string.longitude)) },
                        onValueChange = { newLong ->
                            long = newLong
                        },
                        isError = (latDouble != null && longDouble == null) || (latDouble == null && longDouble != null),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        modifier = Modifier.weight(1f)
                    )
                }


                MapComposable(
                    initialLocation = location,
                    initialGeoLat = latDouble,
                    initialGeoLong = longDouble,
                    isEditMode = true,
                    enableCurrentLocation = enableCurrentLocation,
                    onLocationUpdated = { newLocation, newLat, newLong ->
                        location = newLocation
                        lat = UiUtil.doubleTo5DecimalString(newLat)?:""
                        long = UiUtil.doubleTo5DecimalString(newLong)?:""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(location, latDouble, longDouble)
                    onDismiss()
                },
                enabled = (latDouble == null && longDouble == null) || (latDouble != null && longDouble != null)
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

