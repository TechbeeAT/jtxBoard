package at.techbee.jtx.ui.detail

import android.content.Context
import android.provider.ContactsContract
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import at.techbee.jtx.database.properties.Attendee

class DetailsStateHolder(context: Context) {

    var isSyncInProgress = mutableStateOf(false)
    var icalString2Import: MutableState<String?> = mutableStateOf(null)

    init {


    }
}