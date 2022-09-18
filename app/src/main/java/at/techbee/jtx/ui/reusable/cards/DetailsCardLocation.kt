/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*


@OptIn(ExperimentalMaterial3Api::class)
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
    var showMap by rememberSaveable { mutableStateOf(false) }

    var location by remember { mutableStateOf(initialLocation ?: "")}
    var geoLat by remember { mutableStateOf(initialGeoLat)}
    var geoLong by remember { mutableStateOf(initialGeoLong)}



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

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
                        
                        IconButton(onClick = {
                            showMap = !showMap
                            if(!showMap) {
                                geoLat = null
                                geoLong = null
                            }
                        }) {
                            Icon(Icons.Outlined.Map, stringResource(id = R.string.location))
                        }
                    }
                }
            }

            AnimatedVisibility((geoLat != null && geoLong != null) || (isEditMode && showMap)) {

                val uiSettings by remember {
                    mutableStateOf(
                        MapUiSettings(
                            compassEnabled = true,
                            //myLocationButtonEnabled = true,
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
                            //isMyLocationEnabled = true
                        )
                    )
                }

                val marker = if(geoLat != null && geoLong != null)
                    LatLng(geoLat!!, geoLong!!)
                else
                    null
                val cameraPositionState = rememberCameraPositionState {
                    position = if(marker != null)
                        CameraPosition.fromLatLngZoom(marker, 10f)
                    else
                        CameraPosition.fromLatLngZoom(LatLng(0.0,0.0), 0f)
                }
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 8.dp),
                    cameraPositionState = cameraPositionState,
                    onPOIClick = { poi ->
                        location = poi.name
                        geoLat = poi.latLng.latitude
                        geoLong = poi.latLng.longitude
                    },
                    properties = properties,
                    uiSettings = uiSettings
                ) {
                    if(marker != null) {
                        Marker(
                            state = MarkerState(position = marker),
                            title = location,
                            //snippet = "Marker in Singapore"
                        )
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