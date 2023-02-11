package at.techbee.jtx.ui

import android.content.ContentResolver
import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.util.SyncUtil

class GlobalStateHolder(context: Context) {

    var isSyncInProgress = mutableStateOf(false)
    var icalString2Import: MutableState<String?> = mutableStateOf(null)

    var icalObject2Open: MutableState<Long?> = mutableStateOf(null)

    var icalFromIntentString: MutableState<String?> = mutableStateOf(null)
    var icalFromIntentAttachment: MutableState<Attachment?> = mutableStateOf(null)
    var icalFromIntentModule: MutableState<Module?> = mutableStateOf(null)
    var icalFromIntentCollection: MutableState<String?> = mutableStateOf(null)

    var isDAVx5compatible = mutableStateOf(true)

    init {
        try {
            ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE) {
                isSyncInProgress.value = SyncUtil.isJtxSyncRunning(context)
            }
        } catch(e: NullPointerException) {      // especially necessary as ContentResolver is not available in preview (would cause exception)
            Log.d("GlobalStateHolder", e.stackTraceToString())
        }
    }
}