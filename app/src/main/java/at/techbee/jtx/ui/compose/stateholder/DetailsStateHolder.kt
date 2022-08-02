package at.techbee.jtx.ui.compose.stateholder

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



    private fun loadContacts(context: Context) {

        /*
        Template: https://stackoverflow.com/questions/10117049/get-only-email-address-from-contact-list-android
         */

        val cr = context.contentResolver
        val projection = arrayOf(
            ContactsContract.RawContacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Email.DATA
        )
        val order = ContactsContract.Contacts.DISPLAY_NAME
        val filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''"
        val cur = cr.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            projection,
            filter,
            null,
            order
        )

        if (cur!!.count > 0) {
            while (cur.moveToNext()) {

                val name = cur.getString(1)    // according to projection 0 = DISPLAY_NAME, 1 = Email.DATA
                val email = cur.getString(2)
                if(email.isNotBlank()) {
                    val attendee = Attendee(cn = name, caladdress = "mailto:$email")
                    //allContactsAsAttendees.add(attendee)  // TODO
                    //allContactsSpinner.add(attendee.getDisplayString())
                }
            }
            cur.close()
        }
    }
}