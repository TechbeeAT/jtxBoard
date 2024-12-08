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
import android.icu.util.LocaleData
import android.icu.util.ULocale
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.EditLocation
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.ColumnInfo
import at.techbee.jtx.BuildFlavor
import at.techbee.jtx.R
import at.techbee.jtx.database.COLUMN_GEO_LAT
import at.techbee.jtx.database.COLUMN_GEO_LONG
import at.techbee.jtx.database.COLUMN_LOCATION
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.flavored.MapComposable
import at.techbee.jtx.ui.reusable.dialogs.LocationPickerDialog
import at.techbee.jtx.ui.reusable.dialogs.RequestPermissionDialog
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.util.UiUtil
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.roundToInt


@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DetailsCardLocation(
    initialLocation: String?,
    initialGeoLat: Double?,
    initialGeoLong: Double?,
    initialGeofenceRadius: Int?,
    isEditMode: Boolean,
    onLocationUpdated: (String, Double?, Double?) -> Unit,
    onGeofenceRadiusUpdatd: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val headline = stringResource(id = R.string.location)
    var showLocationPickerDialog by rememberSaveable { mutableStateOf(false) }
    var showRequestGeofencePermissionsDialog by rememberSaveable { mutableStateOf(false) }
    val openPermissionsIntent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))

    var location by rememberSaveable { mutableStateOf(initialLocation ?: "") }
    var geoLat by rememberSaveable { mutableStateOf(initialGeoLat) }
    var geoLong by rememberSaveable { mutableStateOf(initialGeoLong) }
    var geoLatText by rememberSaveable { mutableStateOf(initialGeoLat?.toString() ?: "") }
    var geoLongText by rememberSaveable { mutableStateOf(initialGeoLong?.toString() ?: "") }
    var geofenceRadius by rememberSaveable { mutableStateOf(initialGeofenceRadius) }

    val locationPermissionState = if (!LocalInspectionMode.current) rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    ) else null

    val contactsPermissionState = if (!LocalInspectionMode.current) rememberPermissionState(permission = Manifest.permission.READ_CONTACTS) else null

    val allLocations by ICalDatabase.getInstance(context).iCalDatabaseDao().getAllLocationsLatLng().observeAsState(emptyList())
    val allLocalAddresses by remember { derivedStateOf {
        if(contactsPermissionState?.status?.isGranted == true)
            UiUtil.getLocalAddresses(context, location)
        else
            emptySet()
    } }


    val geofencePermissionState = if (!LocalInspectionMode.current) rememberMultiplePermissionsState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            listOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        else
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
    ) else null


    var locationUpdateRequested by rememberSaveable { mutableStateOf(false) }

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
                    geoLatText = geoLat?.toString() ?:""
                    geoLongText = geoLong?.toString() ?:""
                    onLocationUpdated(location, geoLat, geoLong)
                },
                onDismiss = {
                    showLocationPickerDialog = false
                }
            )
        }
    }

    if(showRequestGeofencePermissionsDialog) {
        RequestPermissionDialog(
            text = stringResource(id = R.string.geofence_request_permission_dialog_message),
            onConfirm = {
                if(geofencePermissionState?.shouldShowRationale == true)
                    context.startActivity(openPermissionsIntent)
                else
                    geofencePermissionState?.launchMultiplePermissionRequest()
                showRequestGeofencePermissionsDialog = false
            }
        )
    }

    LaunchedEffect(locationUpdateRequested, locationPermissionState?.permissions?.any { it.status.isGranted }) {
        if(locationUpdateRequested) {
            if(locationPermissionState?.permissions?.any { it.status.isGranted } == true) {
                // Get the location manager, avoiding using fusedLocationClient here to not use proprietary libraries
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val bestProvider = locationManager.getProviders(true).firstOrNull() ?: return@LaunchedEffect
                val locListener = LocationListener { }
                locationManager.requestLocationUpdates(bestProvider, 0, 0f, locListener)
                locationManager.getLastKnownLocation(bestProvider)?.let { lastKnownLocation ->
                    geoLat = lastKnownLocation.latitude
                    geoLong = lastKnownLocation.longitude
                    geoLatText = lastKnownLocation.latitude.toString()
                    geoLongText = lastKnownLocation.longitude.toString()
                    onLocationUpdated(location, geoLat, geoLong)
                    locationUpdateRequested = false
                }
            } else {
                locationPermissionState?.launchMultiplePermissionRequest()
            }
        }
    }


    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            Crossfade(isEditMode, label = "toggleEditModeForMap") { editMode ->
                if (!editMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            HeadlineWithIcon(
                                icon = Icons.Outlined.Place,
                                iconDesc = headline,
                                text = headline
                            )
                            Text(
                                text = location,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if(geoLat != null && geoLong != null) {
                                Text(ICalObject.getLatLongString(geoLat, geoLong) ?: "")
                            }
                        }

                        if((geoLat != null && geoLong != null) || location.isNotEmpty()) {

                            IconButton(onClick = {
                                val uri = if(geoLat == null && geoLong == null && location.isNotEmpty()) {
                                    Uri.parse("geo:0,0?q=$location")
                                } else if (geoLat != null && geoLong != null && location.isEmpty()) {
                                    val latLngParam = "%.5f".format(Locale.ENGLISH, geoLat) + "," + "%.5f".format(Locale.ENGLISH, geoLong)
                                    Uri.parse("geo:0,0?q=$latLngParam(${URLEncoder.encode(location, Charsets.UTF_8.name())})")
                                } else {
                                    val latLngParam = "%.5f".format(Locale.ENGLISH, geoLat) + "," + "%.5f".format(Locale.ENGLISH, geoLong)
                                    Uri.parse("geo:$latLngParam")
                                }

                                val geoIntent = Intent(Intent.ACTION_VIEW, uri)
                                try {
                                    context.startActivity(geoIntent)
                                } catch (e: ActivityNotFoundException) {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, ICalObject.getMapLink(geoLat, geoLong, location, BuildFlavor.getCurrent()))
                                    )
                                }
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.OpenInNew,
                                    stringResource(id = R.string.open_in_browser)
                                )
                            }
                        }

                    }
                } else {
                    Column {
                        if (allLocations.isNotEmpty() || allLocalAddresses.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(allLocations.filter { it.location?.lowercase()?.contains(location.lowercase()) == true }) { locationLatLng ->
                                    AssistChip(
                                        onClick = {
                                            location = locationLatLng.location ?: ""
                                            geoLat = locationLatLng.geoLat
                                            geoLong = locationLatLng.geoLong
                                            geoLatText = geoLat?.toString()?:""
                                            geoLongText = geoLong?.toString()?:""
                                            onLocationUpdated(location, geoLat, geoLong)
                                        },
                                        label = {
                                            val displayString =
                                                if (locationLatLng.geoLat != null && locationLatLng.geoLong != null)
                                                    "${locationLatLng.location} (${String.format("%.5f", locationLatLng.geoLat)},${String.format("%.5f", locationLatLng.geoLong)})"
                                                else
                                                    "${locationLatLng.location}"
                                            Text(displayString)
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.EditLocation, null)
                                        },
                                        modifier = Modifier.alpha(
                                            if (locationLatLng.location == location && locationLatLng.geoLat == geoLat && locationLatLng.geoLong == geoLong) 1f else 0.4f
                                        )
                                    )
                                }

                                items(allLocalAddresses.toList()) { addressBookLocation ->
                                    AssistChip(
                                        onClick = {
                                            location = addressBookLocation
                                            geoLat = null
                                            geoLong = null
                                            geoLatText = ""
                                            geoLongText = ""
                                            onLocationUpdated(location, null, null)
                                        },
                                        label = { Text(addressBookLocation) },
                                        leadingIcon = { Icon(Icons.Outlined.Contacts, null) },
                                        modifier = Modifier.alpha(
                                            if (addressBookLocation == location) 1f else 0.4f
                                        )
                                    )
                                }
                            }
                        }
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
                                || (geoLongText.isNotEmpty() && geoLongText.toDoubleOrNull() == null)
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
                        locationUpdateRequested = true
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

            if (BuildFlavor.getCurrent() == BuildFlavor.GPLAY) {
                AnimatedVisibility(geoLat != null && geoLong != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        var geofenceOptionsExpanded by remember { mutableStateOf(false) }
                        val useFeet = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && LocaleData.getMeasurementSystem(ULocale.getDefault()) != LocaleData.MeasurementSystem.SI
                        fun Int.metersInFeet() = (((this * 3.281) / 50).roundToInt() * 50)

                        Icon(
                            painter = painterResource(R.drawable.ic_geofence_radius),
                            contentDescription = null,
                            modifier = Modifier.padding(horizontal = if (isEditMode) 8.dp else 0.dp)
                        )
                        Text(
                            stringResource(
                                R.string.geofence_selection,
                                when {
                                    geofenceRadius == null -> stringResource(R.string.off)
                                    useFeet -> stringResource(R.string.geofence_radius_feet, geofenceRadius!!.metersInFeet())
                                    else -> stringResource(R.string.geofence_radius_meter, geofenceRadius!!)
                                }
                            )
                        )

                        AnimatedVisibility(isEditMode) {
                            IconButton(
                                onClick = { geofenceOptionsExpanded = true },
                            ) {

                                DropdownMenu(
                                    expanded = geofenceOptionsExpanded,
                                    onDismissRequest = { geofenceOptionsExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.off)) },
                                        onClick = {
                                            geofenceRadius = null
                                            onGeofenceRadiusUpdatd(null)
                                            geofenceOptionsExpanded = false
                                        }
                                    )

                                    listOf(50, 200, 500).forEach {
                                        DropdownMenuItem(
                                            text = {
                                                val metersInFeet = (((it * 3.281) / 50).roundToInt() * 50)
                                                if (useFeet)
                                                    Text(text = stringResource(R.string.geofence_radius_feet, metersInFeet))
                                                else
                                                    Text(text = stringResource(R.string.geofence_radius_meter, it))
                                            },
                                            onClick = {
                                                if (geofencePermissionState?.allPermissionsGranted != true)
                                                    showRequestGeofencePermissionsDialog = true
                                                geofenceRadius = it
                                                onGeofenceRadiusUpdatd(it)
                                                geofenceOptionsExpanded = false
                                            }
                                        )
                                    }
                                }

                                Icon(Icons.Outlined.ArrowDropDown, stringResource(R.string.geofence_options))
                            }
                        }
                    }
                }


                AnimatedVisibility(geofenceRadius != null && (LocalInspectionMode.current || geofencePermissionState?.allPermissionsGranted != true)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.geofence_missing_permission_info),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { context.startActivity(openPermissionsIntent) }
                        ) {
                            Text(stringResource(id = R.string.permissions))
                        }
                    }
                }
            }
        }
    }
}

data class LocationLatLng(
    @ColumnInfo(name = COLUMN_LOCATION) val location: String?,
    @ColumnInfo(name = COLUMN_GEO_LAT) val geoLat: Double?,
    @ColumnInfo(name = COLUMN_GEO_LONG) val geoLong: Double?
)

@Preview(showBackground = true)
@Composable
fun DetailsCardLocation_Preview() {
    MaterialTheme {
        DetailsCardLocation(
            initialLocation = "Vienna, Stephansplatz",
            initialGeoLat = null,
            initialGeoLong = null,
            initialGeofenceRadius = null,
            isEditMode = false,
            onLocationUpdated = { _, _, _ -> },
            onGeofenceRadiusUpdatd = {}
        )
    }
}

@Preview(showBackground = true, locale = "EN-en")
@Composable
fun DetailsCardLocation_Preview_withGeo() {
    MaterialTheme {
        DetailsCardLocation(
            initialLocation = "Vienna, Stephansplatz",
            initialGeoLat = 23.447378,
            initialGeoLong = 73.272838,
            initialGeofenceRadius = null,
            isEditMode = false,
            onLocationUpdated = { _, _, _ -> },
            onGeofenceRadiusUpdatd = {}
        )
    }
}

@Preview(showBackground = true, locale = "DE-de")
@Composable
fun DetailsCardLocation_Preview_withGeoDE() {
    MaterialTheme {
        DetailsCardLocation(
            initialLocation = "Vienna, Stephansplatz",
            initialGeoLat = 23.447378,
            initialGeoLong = 73.272838,
            initialGeofenceRadius = null,
            isEditMode = false,
            onLocationUpdated = { _, _, _ -> },
            onGeofenceRadiusUpdatd = {}
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
            initialGeofenceRadius = null,
            isEditMode = true,
            onLocationUpdated = { _, _, _ -> },
            onGeofenceRadiusUpdatd = {}
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardLocation_Preview_edit_with_geo() {
    MaterialTheme {
        DetailsCardLocation(
            initialLocation = "Vienna, Stephansplatz",
            initialGeoLat = 23.447378,
            initialGeoLong = 73.272838,
            initialGeofenceRadius = null,
            isEditMode = true,
            onLocationUpdated = { _, _, _ -> },
            onGeofenceRadiusUpdatd = {}
        )
    }
}