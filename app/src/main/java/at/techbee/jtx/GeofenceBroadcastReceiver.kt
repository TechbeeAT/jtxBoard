package at.techbee.jtx

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.preference.PreferenceManager
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.flavored.GeofenceClient

class GeofenceBroadcastReceiver: BroadcastReceiver() {


    companion object {
        /*
        const val ALARM_ID = "alarm-id"
        const val ICALOBJECT_ID = "icalobject-id"
        const val NOTIFICATION = "alarmNotification"
         */

        const val PREFS_ACTIVE_GEOFENCES = "prefsGeofences"  // ICalObjectIds as StringSet
        private const val MAX_GEOFENCES = 99


        fun setGeofences(context: Context) {

            // Due to necessity of PendingIntent.FLAG_IMMUTABLE, the notification functionality can only be used from Build Versions > M (Api-Level 23)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                return

            val database = ICalDatabase.getInstance(context).iCalDatabaseDao
            val geofenceObjects = database.getICalObjectsWithGeofence(MAX_GEOFENCES)
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val geofenceClient = GeofenceClient(context)

            // determine obsolete Request Codes
            val activeGeofences = mutableListOf<Long>()
            prefs.getStringSet(PREFS_ACTIVE_GEOFENCES, emptySet())
                ?.map { try { it.toLong()} catch (e: NumberFormatException) { return } }
                ?.let { activeGeofences.addAll(it) }


            geofenceClient.removeGeofence(activeGeofences.filter { supposedActive -> geofenceObjects.none { foundGeofence -> supposedActive == foundGeofence.id } })
            geofenceObjects.filter { found -> activeGeofences.none { active -> found.id == active  } }.forEach { iCalObject ->
                geofenceClient.addGeofence(iCalObject.geoLat!!, iCalObject.geoLong!!, iCalObject.geofenceRadius!!, iCalObject.id)
                activeGeofences.add(iCalObject.id)
            }
            prefs.edit().putStringSet(PREFS_ACTIVE_GEOFENCES, activeGeofences.map { it.toString() }.toSet()).apply()
            Log.d("GeofenceBroadcastRec", "Geofence set for ${activeGeofences.joinToString(separator = ", ")}")
        }
    }



    override fun onReceive(context: Context, intent: Intent) {
        Log.d("GeofenceBroadcastRec", "GeofenceBroadcastReceiver received")
        //todo
    }
}