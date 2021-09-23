/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx

import android.icu.text.MessageFormat
import android.os.Build
import androidx.core.util.PatternsCompat
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*



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
    return PatternsCompat.EMAIL_ADDRESS.matcher(emailString.toString()).matches()

}

fun isValidURL(urlString: String?): Boolean {
    return PatternsCompat.WEB_URL.matcher(urlString.toString()).matches()
}

fun getAttachmentSizeString(filesize: Long): String {
    return when {
        filesize < 1024 -> "$filesize Bytes"
        filesize / 1024 < 1024 -> "${filesize / 1024} KB"
        else -> "${filesize / 1024 / 1024} MB"
    }
}


fun getLocalizedOrdinal(from: Int, to: Int): Array<String> {

    val ordinalValues: MutableList<String> = mutableListOf()

    for (i in from..to) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val formatter = MessageFormat("{0,ordinal}", Locale.getDefault())
            ordinalValues.add(formatter.format(arrayOf(i)))
        } else {
            when (i) {
                1 -> ordinalValues.add("1st")
                2 -> ordinalValues.add("2nd")
                3 -> ordinalValues.add("3rd")
                else -> ordinalValues.add("${i}th")
            }

        }
    }

    return ordinalValues.toTypedArray()

}

fun getLocalizedWeekdays(): Array<String> {

    val weekdays = mutableListOf<String>()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val wf: WeekFields = WeekFields.of(Locale.getDefault())
        val day: DayOfWeek = wf.firstDayOfWeek
        for(i in 0L..6L) {
            weekdays.add(day.plus(i).getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault()))
        }
    } else {
        weekdays.addAll(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
    }

    return weekdays.toTypedArray()
}


fun isLocalizedWeekstartMonday(): Boolean {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val wf: WeekFields = WeekFields.of(Locale.getDefault())
        val day: DayOfWeek = wf.firstDayOfWeek

        if(day == DayOfWeek.MONDAY)
            return true
    }
    return false
}