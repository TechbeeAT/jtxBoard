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
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.EditLocation
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R
import at.techbee.jtx.flavored.MapComposable
import at.techbee.jtx.ui.reusable.dialogs.LocationPickerDialog
import at.techbee.jtx.ui.reusable.dialogs.RequestPermissionDialog
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale


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
                        Text(location)
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
                            colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
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

            AnimatedVisibility(geoLat != null && geoLong != null && !isEditMode) {
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