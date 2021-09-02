/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.notesx5

import android.util.Patterns
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


/**
 * Take the Long milliseconds returned by the system and stored in Room,
 * and convert it to a nicely formatted string for display.
 *
 * EEEE - Display the long letter version of the weekday
 * MMM - Display the letter abbreviation of the nmotny
 * dd-yyyy - day in month and full year numerically
 * HH:mm - Hours and minutes in 24hr format
 */

/*
@SuppressLint("SimpleDateFormat")
fun convertLongToDateString2(systemTime: Long): String {
    return SimpleDateFormat("EEEE MMM-dd-yyyy' Time: 'HH:mm")
            .format(systemTime).toString()
}

 */


fun convertLongToDateString(date: Long?): String {
    if (date == null || date == 0L)
        return ""
    return DateFormat.getDateInstance(DateFormat.LONG).format(date)
}

fun convertLongToTimeString(time: Long?): String {
    if (time == null || time == 0L)
        return ""
    return DateFormat.getTimeInstance(DateFormat.SHORT).format(time)
}

/*
fun convertLongToHourString(time: Long?): String {
    if (time == null || time == 0L)
        return ""
    val hour_formatter = SimpleDateFormat("HH")
    return hour_formatter.format(Date(time)).toString()
}


fun convertLongToMinuteString(time: Long): String {
    if (time == 0L)
        return ""
    val minute_formatter = SimpleDateFormat("mm")
    return minute_formatter.format(Date(time)).toString()
}

 */

fun convertLongToDayString(date: Long?): String {
    if (date == null || date == 0L)
        return ""
    val dayFormatter = SimpleDateFormat("dd", Locale.getDefault())
    return dayFormatter.format(Date(date)).toString()
}


fun convertLongToMonthString(date: Long?): String {
    if (date == null || date == 0L)
        return ""
    val monthFormatter = SimpleDateFormat("MMMM", Locale.getDefault())
    return monthFormatter.format(Date(date)).toString()
}


fun convertLongToYearString(date: Long?): String {
    if (date == null || date == 0L)
        return ""
    val yearFormatter = SimpleDateFormat("yyyy", Locale.getDefault())
    return yearFormatter.format(Date(date)).toString()
}


fun isValidEmail(emailString: String?): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(emailString.toString()).matches()
}

fun isValidURL(urlString: String?): Boolean {
    return Patterns.WEB_URL.matcher(urlString.toString()).matches()
}

fun getAttachmentSizeString(filesize: Long): String {
    return when {
        filesize < 1024 -> "$filesize Bytes"
        filesize / 1024 < 1024 -> "${filesize / 1024} KB"
        else -> "${filesize / 1024 / 1024} MB"
    }
}