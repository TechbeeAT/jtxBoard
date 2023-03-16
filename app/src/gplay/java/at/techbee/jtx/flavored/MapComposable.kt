/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.flavored

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*


@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapComposable(
    initialLocation: String?,
    initialGeoLat: Double?,
    initialGeoLong: Double?,
    enableCurrentLocation: Boolean,
    isEditMode: Boolean,
    onLocationUpdated: (String?, Double?, Double?) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    var location by remember { mutableStateOf(initialLocation ?: "") }
    val marker = rememberMarkerState(null, LatLng(initialGeoLat?:0.0, initialGeoLong?:0.0))
    val locationPermissionState = if(!LocalInspectionMode.current) rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    ) else null

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                compassEnabled = true,
                myLocationButtonEnabled = enableCurrentLocation,
                scrollGesturesEnabled = true,
                zoomControlsEnabled = true,
                zoomGesturesEnabled = true
            )
        )
    }
    val properties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = enableCurrentLocation
            )
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = if(marker.position != LatLng(0.0, 0.0))
            CameraPosition.fromLatLngZoom(marker.position, 15f)
        else
            CameraPosition.fromLatLngZoom(LatLng(54.5260,15.2551), 0f)
    }



    LaunchedEffect(locationPermissionState) {
        if (locationPermissionState?.permissions?.any { it.status.isGranted } == true) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { knownLocation: Location? ->
                knownLocation?.let { lastKnownLocation ->
                    if(marker.position != LatLng(0.0, 0.0))
                        return@addOnSuccessListener
                    cameraPositionState.move(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(
                                LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude),
                                15f
                            )
                        )
                    )
                }
            }
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        onPOIClick = { poi ->
            if(isEditMode) {
                location = poi.name
                marker.position = poi.latLng
                onLocationUpdated(location, marker.position.latitude, marker.position.longitude)
            }
        },
        onMapClick = {
            if(isEditMode) {
                location = ""
                marker.position = it
                onLocationUpdated(location, marker.position.latitude, marker.position.longitude)
            }
        },
        properties = properties,
        uiSettings = uiSettings
    ) {
        if(marker.position != LatLng(0.0, 0.0)) {
            Marker(
                state = marker,
                title = location,
                //snippet = "Marker in Singapore"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardLocation_Preview_Wien() {
    MaterialTheme {
        MapComposable(
            initialLocation = "Vienna, Stephansplatz",
            initialGeoLat = 48.208492,
            initialGeoLong = 16.373127,
            isEditMode = false,
            enableCurrentLocation = false,
            onLocationUpdated = { _, _, _ -> },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardLocation_Preview_empty() {
    MaterialTheme {
        MapComposable(
            initialLocation = null,
            initialGeoLat = null,
            initialGeoLong = null,
            isEditMode = false,
            enableCurrentLocation = false,
            onLocationUpdated = { _, _, _ -> },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 8.dp)
        )
    }
}
