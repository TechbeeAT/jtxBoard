/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx

import android.icu.text.MessageFormat
import android.os.Build
import android.util.Log
import androidx.core.util.PatternsCompat
import at.techbee.jtx.database.ICalObject
import java.lang.NumberFormatException
import java.text.DateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*



fun convertLongToDateString(date: Long?): String {

    if (date == null || date == 0L)
        return ""
    return DateFormat.getDateInstance(DateFormat.LONG).format(date)
}

fun convertLongToFullDateString(date: Long?): String {
    if (date == null || date == 0L)
        return ""
    return DateFormat.getDateInstance(DateFormat.FULL).format(date)
}

fun convertLongToTimeString(time: Long?, timezone: String?): String {
    if (time == null || time == 0L)
        return ""
    val zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), requireTzId(timezone))
    val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    return zonedDateTime.toLocalTime().format(formatter)
}

fun convertLongToDayString(date: Long?, timezone: String?): String {
    if (date == null || date == 0L)
        return ""
    val zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), requireTzId(timezone))
    val formatter = DateTimeFormatter.ofPattern("dd", Locale.getDefault())
    return zonedDateTime.toLocalDateTime().format(formatter)
}


fun convertLongToMonthString(date: Long?, timezone: String?): String {
    if (date == null || date == 0L)
        return ""
    val zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), requireTzId(timezone))
    val formatter = DateTimeFormatter.ofPattern("MMMM", Locale.getDefault())
    return zonedDateTime.toLocalDateTime().format(formatter)
}


fun convertLongToYearString(date: Long?, timezone: String?): String {
    if (date == null || date == 0L)
        return ""
    val zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), requireTzId(timezone))
    val formatter = DateTimeFormatter.ofPattern("yyyy", Locale.getDefault())
    return zonedDateTime.toLocalDateTime().format(formatter)
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


fun getLocalizedOrdinal(from: Int, to: Int, includeEmpty: Boolean): Array<String> {

    val ordinalValues: MutableList<String> = mutableListOf()
    if(includeEmpty)
        ordinalValues.add("-")

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

fun addLongToCSVString(listAsString: String?, value: Long?): String? {

    if(value == null)
        return null

    if(listAsString.isNullOrBlank())
        return value.toString()

    val stringList = listAsString.split(",")
    val newStringList = mutableListOf<String>()
    newStringList.addAll(stringList)
    if(!newStringList.contains(value.toString()))
        newStringList.add(value.toString())

    return if(newStringList.isEmpty())
        null
    else
        newStringList.joinToString(",")
}

fun getLongListfromCSVString(listAsString: String?): List<Long> {

    if(listAsString == null)
        return emptyList()

    val stringList = listAsString.split(",")
    val longList = mutableListOf<Long>()

    stringList.forEach {
        try {
            longList.add(it.toLong())
        } catch(e: NumberFormatException) {
            Log.w("NumberFormatException", "Failed to convert Long to String ($it)\n$e")
        }
    }
    return longList
}

fun getOffsetStringFromTimezone(timezone: String?): String {

    timezone?.let {
        return TimeZone.getTimeZone(it).getDisplayName(false, TimeZone.SHORT) ?: "" }

    return ""
}


/**
 * Gets a [ZoneId] from a String
 * @return ZoneId.systemDefault if the Timezone is not set or if it is an all-day event,
 * The ZoneId of the given String or "UTC" if the string could not be parsed
 */
fun requireTzId(timezone: String?): ZoneId {
    return if(timezone == null || timezone == ICalObject.TZ_ALLDAY)
        ZoneId.systemDefault()
    else
        try {
            ZoneId.of(timezone)
        } catch (e: DateTimeException) {
            ZoneId.of("UTC")
        }
}
