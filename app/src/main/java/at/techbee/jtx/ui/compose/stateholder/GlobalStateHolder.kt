package at.techbee.jtx.ui.compose.stateholder

import android.content.ContentResolver
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import at.techbee.jtx.util.SyncUtil

class GlobalStateHolder(context: Context) {

    var isSyncInProgress = mutableStateOf(false)

    init {
        ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE) {
            isSyncInProgress.value = SyncUtil.isJtxSyncRunning(context)
        }
    }

}