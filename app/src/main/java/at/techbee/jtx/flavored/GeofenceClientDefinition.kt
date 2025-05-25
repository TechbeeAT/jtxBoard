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
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import at.techbee.jtx.database.ICalDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class GeofenceClientDefinition(val context: Context) {

    companion object {
        private const val PREFS_ACTIVE_GEOFENCES = "prefsGeofences"  // ICalObjectIds as StringSet
        private const val MAX_GEOFENCES = 99
    }

    fun setGeofences() {

        CoroutineScope(Dispatchers.IO).launch {

            val database = ICalDatabase.getInstance(context).iCalDatabaseDao()
            val geofenceObjects = database.getICalObjectsWithGeofence(MAX_GEOFENCES)
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val activeGeofences = prefs.getStringSet(PREFS_ACTIVE_GEOFENCES, emptySet())
                ?.map {try { it.toLong() } catch (e: NumberFormatException) { return@launch } } ?: emptyList()

            removeGeofence(activeGeofences)
            geofenceObjects.forEach { iCalObject ->
                addGeofence(iCalObject.geoLat!!, iCalObject.geoLong!!, iCalObject.geofenceRadius!!, iCalObject.id)
            }
            prefs.edit {
                putStringSet(
                    PREFS_ACTIVE_GEOFENCES,
                    geofenceObjects.map { it.id.toString() }.toSet()
                )
            }
            Log.d("GeofenceBroadcastRec", "Geofence set for ${activeGeofences.joinToString(separator = ", ")}")
        }
    }

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