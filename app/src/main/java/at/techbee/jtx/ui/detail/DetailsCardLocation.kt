/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.LocationListener
import android.location.LocationManager
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
import com.google.accompanist.permissions.*
import java.net.URLEncoder
import java.util.*


@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DetailsCardLocation(
    initialLocation: String?,
    initialGeoLat: Double?,
    initialGeoLong: Double?,
    isEditMode: Boolean,
    setCurrentLocation: Boolean,
    onLocationUpdated: (String, Double?, Double?) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val headline = stringResource(id = R.string.location)
    var showLocationPickerDialog by rememberSaveable { mutableStateOf(false) }

    var location by remember { mutableStateOf(initialLocation ?: "") }
    var geoLat by remember { mutableStateOf(initialGeoLat) }
    var geoLong by remember { mutableStateOf(initialGeoLong) }
    var geoLatText by remember { mutableStateOf(initialGeoLat?.toString() ?: "") }
    var geoLongText by remember { mutableStateOf(initialGeoLong?.toString() ?: "") }

    val locationPermissionState = if (!LocalInspectionMode.current) rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    ) else null
    var locationUpdateState by remember { mutableStateOf(
        if(setCurrentLocation && locationPermissionState?.permissions?.any { it.status.isGranted } == true)
                LocationUpdateState.LOCATION_REQUESTED
        else if(setCurrentLocation)
                LocationUpdateState.PERMISSION_NEEDED
        else
            LocationUpdateState.IDLE
    )}

    if (showLocationPickerDialog) {
        if (locationPermissionState?.permissions?.all { it.status.shouldShowRationale } == false && locationPermissionState.permissions.none { it.status.isGranted }) {   // second part = permission is NOT permanently denied!
            RequestPermissionDialog(
                text = stringResource(id = R.string.edit_fragment_app_coarse_location_permission_message),
                onConfirm = { locationPermissionState.launchMultiplePermissionRequest() }
            )
        } else {
            LocationPickerDialog(
                initialLocation = location,
                initialGeoLat = geoLat,
                initialGeoLong = geoLong,
                enableCurrentLocation = locationPermissionState?.permissions?.any { it.status.isGranted } == true,
                onConfirm = { newLocation, newLat, newLong ->
                    location = newLocation ?: ""
                    geoLat = newLat
                    geoLong = newLong
                    geoLatText = String.format("%.5f", geoLat)
                    geoLongText = String.format("%.5f", geoLong)
                    onLocationUpdated(location, geoLat, geoLong)
                },
                onDismiss = {
                    showLocationPickerDialog = false
                }
            )
        }
    }

    LaunchedEffect(locationUpdateState, locationPermissionState?.permissions?.any { it.status.isGranted }) {
        when (locationUpdateState) {
            LocationUpdateState.IDLE -> {}
            LocationUpdateState.LOCATION_REQUESTED -> {
                // Get the location manager, avoiding using fusedLocationClient here to not use proprietary libraries
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val bestProvider = locationManager.getBestProvider(Criteria(), false) ?: return@LaunchedEffect
                val locListener = LocationListener { }
                locationManager.requestLocationUpdates(bestProvider, 0, 0f, locListener)
                locationManager.getLastKnownLocation(bestProvider)?.let { lastKnownLocation ->
                    geoLat = lastKnownLocation.latitude
                    geoLong = lastKnownLocation.longitude
                    geoLatText = lastKnownLocation.latitude.toString()
                    geoLongText = lastKnownLocation.longitude.toString()
                    onLocationUpdated(location, geoLat, geoLong)
                    locationUpdateState = LocationUpdateState.IDLE
                }
            }
            LocationUpdateState.PERMISSION_NEEDED -> {
                locationPermissionState?.launchMultiplePermissionRequest()
                locationUpdateState = LocationUpdateState.LOCATION_REQUESTED
            }

        }
    }


    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            Crossfade(isEditMode) {
                if (!it) {
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

                        if (BuildConfig.FLAVOR == MainActivity2.BUILD_FLAVOR_GOOGLEPLAY || BuildConfig.FLAVOR == MainActivity2.BUILD_FLAVOR_AMAZON) {
                            IconButton(onClick = { showLocationPickerDialog = true }) {
                                Icon(Icons.Outlined.Map, stringResource(id = R.string.location))
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = geoLatText,
                        singleLine = true,
                        label = { Text(stringResource(R.string.latitude)) },
                        onValueChange = { newLat ->
                            geoLatText = newLat
                            geoLat = newLat.toDoubleOrNull()
                            onLocationUpdated(location, geoLat, geoLong)
                        },
                        trailingIcon = {
                            AnimatedVisibility(geoLat != null) {
                                IconButton(onClick = {
                                    geoLat = null
                                    geoLatText = ""
                                }) {
                                    Icon(Icons.Outlined.Close, stringResource(id = R.string.delete))
                                }
                            }
                        },
                        isError = (geoLatText.isNotEmpty() && geoLatText.toDoubleOrNull() == null)
                                || (geoLatText.isEmpty() && geoLongText.isNotEmpty())
                                || (geoLatText.isNotEmpty() && geoLongText.isEmpty()),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = geoLongText,
                        singleLine = true,
                        label = { Text(stringResource(R.string.longitude)) },
                        onValueChange = { newLong ->
                            geoLongText = newLong
                            geoLong = newLong.toDoubleOrNull()
                            onLocationUpdated(location, geoLat, geoLong)
                        },
                        trailingIcon = {
                            AnimatedVisibility(geoLong != null) {
                                IconButton(onClick = {
                                    geoLong = null
                                    geoLongText = ""
                                }) {
                                    Icon(Icons.Outlined.Close, stringResource(id = R.string.delete))
                                }
                            }
                        },
                        isError = (geoLongText.isNotEmpty() && geoLongText.toDoubleOrNull() == null)
                                || (geoLatText.isEmpty() && geoLongText.isNotEmpty())
                                || (geoLatText.isNotEmpty() && geoLongText.isEmpty()),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = {
                        locationUpdateState = if (locationPermissionState?.permissions?.any { it.status.isGranted } == true) {
                            LocationUpdateState.LOCATION_REQUESTED
                        } else {
                            LocationUpdateState.PERMISSION_NEEDED
                        }
                    }) {
                        Icon(Icons.Outlined.MyLocation, stringResource(R.string.current_location))
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
                        val latLngParam = "%.5f".format(Locale.ENGLISH, geoLat)  + ","  + "%.5f".format(Locale.ENGLISH, geoLong)
                        val geoUri = if (location.isNotEmpty())
                            Uri.parse("geo:0,0?q=$latLngParam(${URLEncoder.encode(location, Charsets.UTF_8.name())})")
                        else
                            Uri.parse("geo:$latLngParam")

                        val geoIntent = Intent(Intent.ACTION_VIEW, geoUri)
                        try {
                            context.startActivity(geoIntent)
                        } catch (e: ActivityNotFoundException) {
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

enum class LocationUpdateState { IDLE, LOCATION_REQUESTED, PERMISSION_NEEDED }

@Preview(showBackground = true)
@Composable
fun DetailsCardLocation_Preview() {
    MaterialTheme {
        DetailsCardLocation(
            initialLocation = "Vienna, Stephansplatz",
            initialGeoLat = null,
            initialGeoLong = null,
            isEditMode = false,
            setCurrentLocation = false,
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
            setCurrentLocation = false,
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
            setCurrentLocation = false,
            onLocationUpdated = { _, _, _ -> }
        )
    }
}