/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.flavored

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import at.techbee.jtx.BuildConfig
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
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

    Card(modifier = modifier) {
        AndroidView(
            factory = { context ->
                Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
                val map = MapView(context)
                map.controller.setZoom(15.0)

                val overlayItems = ArrayList<OverlayItem>()
                if (initialGeoLat != null && initialGeoLong != null) {
                    map.controller.setCenter(GeoPoint(initialGeoLat, initialGeoLong))
                    overlayItems.add(OverlayItem("", "", GeoPoint(initialGeoLat, initialGeoLong)))
                }

                val overlay = ItemizedIconOverlay(context, overlayItems, object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                    override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                        return true
                    }

                    override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                        return false
                    }
                })
                map.overlays.add(overlay)
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
