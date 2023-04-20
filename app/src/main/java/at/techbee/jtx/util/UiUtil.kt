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
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.util.PatternsCompat
import at.techbee.jtx.database.properties.Attendee
import net.fortuna.ical4j.model.WeekDay
import java.time.DayOfWeek
import java.util.regex.Matcher


object UiUtil {

    fun isValidURL(urlString: String?): Boolean {
        return PatternsCompat.WEB_URL.matcher(urlString.toString()).matches()
    }

    fun isValidEmail(emailString: String?): Boolean {
        return emailString?.isNotEmpty() == true && PatternsCompat.EMAIL_ADDRESS.matcher(emailString).matches()
    }


    /**
     * Extracts links out of a text using Patterns.WEB_URL.matcher(text)
     * @param [text] the input text out of which links should be extracted
     * @return a list of links as strings
     */
    fun extractLinks(text: String?): List<String> {
        if(text.isNullOrEmpty())
            return emptyList()
        val links = mutableListOf<String>()
        val matcher: Matcher = PatternsCompat.WEB_URL.matcher(text)
        while (matcher.find()) {
            val url: String = matcher.group()
            Log.d("extractLinks", "URL extracted: $url")
            links.add(url)
        }
        return links
    }


    /**
     * Returns contacts from the local phone contact storage
     * Template: https://stackoverflow.com/questions/10117049/get-only-email-address-from-contact-list-android
     * @param context
     * @param searchString to search in the DISPLAYNAME and E-MAIL field
     * @return a list of Contacts as [Attendee] filtered by the given searchString
     */
    fun getLocalContacts(context: Context, searchString: String): List<Attendee> {

        val allContacts = mutableSetOf<Attendee>()

        val cr = context.contentResolver
        val projection = arrayOf(
            ContactsContract.RawContacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Email.DATA
        )
        val order = ContactsContract.Contacts.DISPLAY_NAME
        val filter = ContactsContract.CommonDataKinds.Email.DATA + " LIKE '%$searchString%' " +
                "OR " + ContactsContract.Contacts.DISPLAY_NAME + " LIKE '%$searchString%'"
        cr.query(
            ContactsContract.CommonDataKinds.Contactables.CONTENT_URI,
            projection,
            filter,
            null,
            order
        )?.use { cur ->
            while (cur.count > 0 && cur.moveToNext()) {
                allContacts.add(Attendee(
                    cn = cur.getString(1).ifEmpty { null },  // according to projection 1 = DISPLAY_NAME, 2 = Email.DATA
                    caladdress = if(isValidEmail(cur.getString(2))) "mailto:" + cur.getString(2) else ""
                ))
            }
            cur.close()
        }
        return allContacts.toList()
    }

    /**
     * @param [filesize] in Bytes that should be transformed in the shortest possible Bytes, KB or MB units
     * @return A String with the Bytes converted to Bytes, KB or MB
     */
    fun getAttachmentSizeString(filesize: Long): String {
        return when {
            filesize < 1024 -> "$filesize Bytes"
            filesize / 1024 < 1024 -> "${filesize / 1024} KB"
            else -> "${filesize / 1024 / 1024} MB"
        }
    }

    fun WeekDay.asDayOfWeek(): DayOfWeek? {
        return when(this) {
            WeekDay.MO -> DayOfWeek.MONDAY
            WeekDay.TU -> DayOfWeek.TUESDAY
            WeekDay.WE -> DayOfWeek.WEDNESDAY
            WeekDay.TH -> DayOfWeek.THURSDAY
            WeekDay.FR -> DayOfWeek.FRIDAY
            WeekDay.SA -> DayOfWeek.SATURDAY
            WeekDay.SU -> DayOfWeek.SUNDAY
            else -> null
        }
    }

    /**
     * This function can be used to determine whether a color is perceived as dark or light
     * and can be useful to determine a contrast color for a background.
     * The algorithm is copied from https://stackoverflow.com/questions/1855884/determine-font-color-based-on-background-color
     * @param color as int for which it should be determined if a color should be seen as dark
     * @return true if the color is likely to be perceived as dark, otherwise false
     */
    fun isDarkColor(color: Color): Boolean {
        // Counting the perceptive luminance - human eye favors green color...
        val a = 1 - ((0.299 * color.red + 0.586 * color.green + 0.115 * color.blue) / color.colorSpace.getMaxValue(0))
        println(color.toString() + " " + a*100)
        return a > 0.5
    }
}