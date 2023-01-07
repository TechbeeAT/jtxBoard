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
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.flavored.MapComposable
import at.techbee.jtx.ui.reusable.dialogs.LocationPickerDialog
import at.techbee.jtx.ui.reusable.dialogs.RequestPermissionDialog
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.net.URLEncoder


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DetailsCardLocation(
    initialLocation: String?,
    initialGeoLat: Double?,
    initialGeoLong: Double?,
    isEditMode: Boolean,
    onLocationUpdated: (String, Double?, Double?) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val headline = stringResource(id = R.string.location)
    var showLocationPickerDialog by rememberSaveable { mutableStateOf(false) }

    var location by remember { mutableStateOf(initialLocation ?: "")}
    var geoLat by remember { mutableStateOf(initialGeoLat)}
    var geoLong by remember { mutableStateOf(initialGeoLong)}

    val coarseLocationPermissionState = if (!LocalInspectionMode.current) rememberPermissionState(permission = Manifest.permission.ACCESS_COARSE_LOCATION) else null

    if(showLocationPickerDialog) {
        if(coarseLocationPermissionState?.status?.shouldShowRationale == false && !coarseLocationPermissionState.status.isGranted) {   // second part = permission is NOT permanently denied!
            RequestPermissionDialog(
                text = stringResource(id = R.string.edit_fragment_app_coarse_location_permission_message),
                onConfirm = { coarseLocationPermissionState.launchPermissionRequest() }
            )
        } else {
            LocationPickerDialog(
                initialLocation = location,
                initialGeoLat = geoLat,
                initialGeoLong = geoLong,
                enableCurrentLocation = coarseLocationPermissionState?.status?.isGranted == true,
                onConfirm = { newLocation, newLat, newLong ->
                    location = newLocation ?: ""
                    geoLat = newLat
                    geoLong = newLong
                    onLocationUpdated(location, geoLat, geoLong)
                },
                onDismiss = {
                    showLocationPickerDialog = false
                }
            )
        }
    }


    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),) {

            Crossfade(isEditMode) {
                if(!it) {
                    Column {
                        HeadlineWithIcon(icon = Icons.Outlined.Place, iconDesc = headline, text = headline)
                        Text(
                            text = location,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = location,
                            leadingIcon = { Icon(Icons.Outlined.EditLocation, headline) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    location = ""
                                    onLocationUpdated(location, geoLat, geoLong)
                                }) {
                                    if (location.isNotEmpty())
                                        Icon(
                                            Icons.Outlined.Clear,
                                            stringResource(id = R.string.delete)
                                        )
                                }
                            },
                            singleLine = true,
                            label = { Text(headline) },
                            onValueChange = { newLocation ->
                                location = newLocation
                                onLocationUpdated(newLocation, geoLat, geoLong)
                            },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                            modifier = Modifier.weight(1f)
                        )

                        if(BuildConfig.FLAVOR == MainActivity2.BUILD_FLAVOR_GOOGLEPLAY) {
                            IconButton(onClick = { showLocationPickerDialog = true }) {
                                Icon(Icons.Outlined.Map, stringResource(id = R.string.location))
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(geoLat != null && geoLong != null && !isEditMode && !LocalInspectionMode.current) {
                MapComposable(
                    initialLocation = location,
                    initialGeoLat = geoLat,
                    initialGeoLong = geoLong,
                    isEditMode = false,
                    enableCurrentLocation = false,
                    onLocationUpdated = { _, _, _ -> /* only view, no update here */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 8.dp)
                )
            }

            AnimatedVisibility(geoLat != null && geoLong != null && !isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(ICalObject.getLatLongString(geoLat, geoLong) ?: "")

                        IconButton(onClick = {
                            val geoUri = if(location.isNotEmpty())
                                Uri.parse("geo:0,0?q=$geoLat,$geoLong(${URLEncoder.encode(location, Charsets.UTF_8.name())})")
                            else
                                Uri.parse("geo:$geoLat,$geoLong")

                            val geoIntent = Intent(Intent.ACTION_VIEW).apply {
                                data = geoUri
                            }
                            if (geoIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(geoIntent)
                            } else {
                                context.startActivity(Intent(Intent.ACTION_VIEW, ICalObject.getMapLink(geoLat, geoLong, BuildConfig.FLAVOR)))
                            }
                        }) {
                            Icon(Icons.Outlined.OpenInNew, stringResource(id = R.string.open_in_browser))
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
            initialLocation = "Vienna, Stephansplatz",
            initialGeoLat = null,
            initialGeoLong = null,
            isEditMode = false,
            onLocationUpdated = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardLocation_Preview_withGEo() {
    MaterialTheme {
        DetailsCardLocation(
            initialLocation = "Vienna, Stephansplatz",
            initialGeoLat = 23.447378,
            initialGeoLong = 73.272838,
            isEditMode = false,
            onLocationUpdated = { _, _, _ -> }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardLocation_Preview_edit() {
    MaterialTheme {
        DetailsCardLocation(
            initialLocation = "Vienna, Stephansplatz",
            initialGeoLat = null,
            initialGeoLong = null,
            isEditMode = true,
            onLocationUpdated = { _, _, _ -> }
        )
    }
}