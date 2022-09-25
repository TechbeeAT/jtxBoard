/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.flavored

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MapComposable(
    initialLocation: String?,
    initialGeoLat: Double?,
    initialGeoLong: Double?,
    enableCurrentLocation: Boolean,
    isEditMode: Boolean,
    onLocationUpdated: (String?, Double?, Double?) -> Unit,
    modifier: Modifier = Modifier
) {    }

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
            modifier = Modifier.fillMaxWidth()
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

