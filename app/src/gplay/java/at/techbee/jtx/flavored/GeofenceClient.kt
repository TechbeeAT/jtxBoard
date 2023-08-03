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
import androidx.core.app.NotificationManagerCompat
import at.techbee.jtx.GeofenceBroadcastReceiver
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.properties.Alarm
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class GeofenceClient(context: Context) : GeofenceClientDefinition(context) {

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

        val geofencePendingIntent = PendingIntent.getBroadcast(
            context,
            iCalObjectId.toInt(),
            Intent(context, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

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

    override fun processOnReceive(intent: Intent) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e("GeofenceClient", errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Get the geofences that were triggered. A single event can trigger multiple geofences.
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return
        val notificationManager = NotificationManagerCompat.from(context)

        CoroutineScope(Dispatchers.IO).launch {
            val db = ICalDatabase.getInstance(context).iCalDatabaseDao

            triggeringGeofences.forEach { geofence ->
                val iCalObjectId = geofence.requestId.toLongOrNull() ?: return@forEach
                val iCalObject = db.getICalObjectById(iCalObjectId) ?: return@forEach
                // Test that the reported transition was of interest.
                when (geofenceTransition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        val notification = Alarm.createNotification(
                            iCalObjectId = iCalObject.id,
                            alarmId = 0L,
                            notificationSummary = context.getString(R.string.geofence_notification_summary),
                            notificationDescription = iCalObject.summary ?: iCalObject.description,
                            isReadOnly = true,
                            notificationChannel = MainActivity2.NOTIFICATION_CHANNEL_GEOFENCES,
                            isSticky = false,
                            context = context
                        )
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                            && notificationManager.activeNotifications.none {iCalObject.id.toInt() == it.id }) {
                            notificationManager.notify(iCalObject.id.toInt(), notification)
                        }
                    }
                    Geofence.GEOFENCE_TRANSITION_EXIT -> notificationManager.cancel(iCalObject.id.toInt())
                }
            }
        }
    }
}