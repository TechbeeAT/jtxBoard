package at.techbee.jtx

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import at.techbee.jtx.flavored.GeofenceClient

class GeofenceBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("GeofenceBroadcastRec", "GeofenceBroadcastReceiver received")
        GeofenceClient(context).processOnReceive(intent)
    }
}