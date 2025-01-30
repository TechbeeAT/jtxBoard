package at.techbee.jtx.ui

import android.content.ContentResolver
import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.util.SyncUtil
import at.techbee.jtx.widgets.ListWidgetConfig

private const val TAG = "GlobalStateHolder"
class GlobalStateHolder(context: Context) {

    var isSyncInProgress = mutableStateOf(false)
    var icalString2Import: MutableState<String?> = mutableStateOf(null)

    var icalObject2Open: MutableState<Long?> = mutableStateOf(null)
    var filteredList2Load: MutableState<ListWidgetConfig?> = mutableStateOf(null)

    var icalFromIntentString: MutableState<String?> = mutableStateOf(null)
    var icalFromIntentAttachment: MutableState<Attachment?> = mutableStateOf(null)
    var icalFromIntentModule: MutableState<Module?> = mutableStateOf(null)
    var icalFromIntentCollection: MutableState<String?> = mutableStateOf(null)
    var icalFromIntentCategories = mutableStateListOf<String>()

    var isAuthenticated = mutableStateOf(false)
    var authenticationTimeout: Long? = null
    private val biometricManager = BiometricManager.from(context)
    val biometricStatus = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
    var biometricPrompt: BiometricPrompt? = null

    var remoteCollections: State<List<ICalCollection>> = mutableStateOf(emptyList())

    init {
        try {
            ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE) {
                isSyncInProgress.value = SyncUtil.isJtxSyncRunningFor(remoteCollections.value.map { it.getAccount() }.toSet())
            }
        } catch(e: NullPointerException) {      // especially necessary as ContentResolver is not available in preview (would cause exception)
            Log.d(TAG, e.stackTraceToString())
        }

        when(biometricStatus) {
            BiometricManager.BIOMETRIC_SUCCESS -> Log.d(TAG, "App can authenticate using biometrics.")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Log.e(TAG, "No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Log.e(TAG, "Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Log.e(TAG, "Biometric features none enrolled.")
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> Log.e(TAG, "Biometric features not supported.")
        }
    }
}