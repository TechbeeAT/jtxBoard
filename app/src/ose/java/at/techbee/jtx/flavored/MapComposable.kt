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
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import at.techbee.jtx.BuildConfig
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayItem


@SuppressLint("UNUSED_PARAMETER")
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
    var presetGeoLat by remember { mutableStateOf(initialGeoLat) }
    var presetGeoLong by remember { mutableStateOf(initialGeoLong) }

    if (initialGeoLat == null && initialGeoLong == null && enableCurrentLocation
        && (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    ) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val bestProvider = locationManager.getProviders(false).firstOrNull()
        val locListener = LocationListener { }
        if(bestProvider != null) {
            locationManager.requestLocationUpdates(bestProvider, 0, 0f, locListener)
            locationManager.getLastKnownLocation(bestProvider)?.let { lastKnownLocation ->
                presetGeoLat = lastKnownLocation.latitude
                presetGeoLong = lastKnownLocation.longitude
                onLocationUpdated(null, presetGeoLat, presetGeoLong)
            }
        }
    }

    Card(modifier = modifier) {
        AndroidView(
            factory = { context ->
                Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
                val map = MapView(context)

                var pinOverlay: Overlay? = null

                if (presetGeoLat != null && presetGeoLong != null) {
                    map.controller.setZoom(15.0)
                    map.controller.setCenter(GeoPoint(presetGeoLat!!, presetGeoLong!!))
                    val overlayItem = OverlayItem("", "", GeoPoint(presetGeoLat!!, presetGeoLong!!))
                    pinOverlay = ItemizedIconOverlay(context, arrayListOf(overlayItem), object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                        override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean { return true }
                        override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean { return false }
                    })
                    map.overlays.add(pinOverlay)
                } else {
                    map.controller.setZoom(2.5)
                }


                val tapOverlay = MapEventsOverlay(object: MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        if(isEditMode && p != null) {
                            //Log.d("MapOverlay", "clicked on ${p.latitude?:0}, ${p.longitude?:0}")
                            map.overlays.remove(pinOverlay)
                            val overlayItem = OverlayItem("", "", p)
                            pinOverlay = ItemizedIconOverlay(context, arrayListOf(overlayItem), object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean { return true }
                                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean { return false }
                            })
                            map.overlays.add(pinOverlay)
                            onLocationUpdated(initialLocation, p.latitude, p.longitude)
                        }
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint?): Boolean {
                        return true
                    }
                })
                map.overlays.add(tapOverlay)
                map
            },
        )
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
