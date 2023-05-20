/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.flavored

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.preference.PreferenceManager
import at.techbee.jtx.database.ICalDatabase

abstract class GeofenceClientDefinition(val context: Context) {

    companion object {
        private const val PREFS_ACTIVE_GEOFENCES = "prefsGeofences"  // ICalObjectIds as StringSet
        private const val MAX_GEOFENCES = 99
    }

    fun setGeofences() {

        // Due to necessity of PendingIntent.FLAG_IMMUTABLE, the notification functionality can only be used from Build Versions > M (Api-Level 23)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return

        val database = ICalDatabase.getInstance(context).iCalDatabaseDao
        val geofenceObjects = database.getICalObjectsWithGeofence(MAX_GEOFENCES)
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val activeGeofences = prefs.getStringSet(PREFS_ACTIVE_GEOFENCES, emptySet())
            ?.map {
                try { it.toLong() } catch (e: NumberFormatException) { return }
            }?: emptyList()


        removeGeofence(activeGeofences)
        geofenceObjects.forEach { iCalObject ->
            addGeofence(iCalObject.geoLat!!, iCalObject.geoLong!!, iCalObject.geofenceRadius!!, iCalObject.id)
        }
        prefs.edit().putStringSet(PREFS_ACTIVE_GEOFENCES, geofenceObjects.map { it.id.toString() }.toSet()).apply()
        Log.d("GeofenceBroadcastRec", "Geofence set for ${activeGeofences.joinToString(separator = ", ")}")
    }


    abstract val isGeofenceAvailable: Boolean

    /**
     * Adds a new geofence to the GeofenceClient
     */
    abstract fun addGeofence(lat: Double, long: Double, radius: Int, iCalObjectId: Long)

    /**
     * Removes the given geofences from the GeofenceClient
     * The [iCalObjectIds] correspond to the geofence-ids
     */
    abstract fun removeGeofence(iCalObjectIds: List<Long>)

    /**
     * This function takes care of the intent received in the GeofenceBroadcastReceiver
     * and makes sure that a notification is displayed or removed.
     */
    abstract fun processOnReceive(intent: Intent)
}