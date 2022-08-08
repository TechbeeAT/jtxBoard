/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.util

import android.content.Context
import android.provider.ContactsContract
import androidx.core.util.PatternsCompat
import at.techbee.jtx.database.properties.Attendee

object UiUtil {

    fun isValidURL(urlString: String?): Boolean {
        return PatternsCompat.WEB_URL.matcher(urlString.toString()).matches()
    }


    /**
     * Returns contacts from the local phone contact storage
     * Template: https://stackoverflow.com/questions/10117049/get-only-email-address-from-contact-list-android
     * @param context
     * @param searchString to search in the DISPLAYNAME and E-MAIL field
     * @return a list of Contacts as [Attendee] filtered by the given searchString
     */
    fun getLocalContacts(context: Context, searchString: String): List<Attendee> {

        val allContacts = mutableListOf<Attendee>()

        val cr = context.contentResolver
        val projection = arrayOf(
            ContactsContract.RawContacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Email.DATA
        )
        val order = ContactsContract.Contacts.DISPLAY_NAME
        val filter = ContactsContract.CommonDataKinds.Email.DATA + " LIKE '%$searchString%' " +
                "OR " + ContactsContract.Contacts.DISPLAY_NAME + " LIKE '%$searchString%'"
        val cur = cr.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            projection,
            filter,
            null,
            order
        )?.use { cur ->
            while (cur.count > 0 && cur.moveToNext()) {

                val name = cur.getString(1)    // according to projection 0 = DISPLAY_NAME, 1 = Email.DATA
                val email = cur.getString(2)
                if(email.isNotBlank()) {
                    val attendee = Attendee(cn = name, caladdress = "mailto:$email")
                    allContacts.add(attendee)
                }
            }
            cur.close()
        }
        return allContacts
    }
}