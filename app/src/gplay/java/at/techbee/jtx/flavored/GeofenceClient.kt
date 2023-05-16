/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.flavored

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import at.techbee.jtx.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices


class GeofenceClient(val context: Context) : GeofenceClientDefinition {

    private val geofenceClient = LocationServices.getGeofencingClient(context)

    override fun addGeofence(lat: Double, long: Double, radius: Int, iCalObjectId: Long) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return

        val geofence = Geofence.Builder()
            .setRequestId(iCalObjectId.toString())
            .setCircularRegion(lat, long, radius.toFloat())
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val geofencePendingIntent by lazy {
            val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            PendingIntent.getBroadcast(context, iCalObjectId.toInt(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        geofenceClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                Log.d("GeofenceClient", "Geofence added")
            }
            addOnFailureListener {
                Log.d("GeofenceClient", "Adding Geofence failed:\n${it.stackTraceToString()}")
            }
        }
    }

    override fun removeGeofence(iCalObjectIds: List<Long>) {
        geofenceClient.removeGeofences(iCalObjectIds.map { it.toString() })
    }

    override fun processOnReceive(context: Context?, intent: Intent?) {
        TODO("Not yet implemented")
    }
}