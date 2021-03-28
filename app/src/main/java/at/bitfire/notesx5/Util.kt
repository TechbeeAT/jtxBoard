/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.notesx5

import android.annotation.SuppressLint
import android.text.TextUtils
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


@SuppressLint("SimpleDateFormat")
fun convertLongToDateString(date: Long?): String {
    if (date == null || date == 0L)
        return ""
    return DateFormat.getDateInstance(DateFormat.LONG).format(date)
}

@SuppressLint("SimpleDateFormat")
fun convertLongToTimeString(time: Long?): String {
    if (time == null || time == 0L)
        return ""
    return DateFormat.getTimeInstance(DateFormat.SHORT).format(time)
}

/*
@SuppressLint("SimpleDateFormat")
fun convertLongToHourString(time: Long?): String {
    if (time == null || time == 0L)
        return ""
    val hour_formatter = SimpleDateFormat("HH")
    return hour_formatter.format(Date(time)).toString()
}


@SuppressLint("SimpleDateFormat")
fun convertLongToMinuteString(time: Long): String {
    if (time == 0L)
        return ""
    val minute_formatter = SimpleDateFormat("mm")
    return minute_formatter.format(Date(time)).toString()
}

 */

@SuppressLint("SimpleDateFormat")
fun convertLongToDayString(date: Long?): String {
    if (date == null || date == 0L)
        return ""
    val dayFormatter = SimpleDateFormat("dd")
    return dayFormatter.format(Date(date)).toString()
}


@SuppressLint("SimpleDateFormat")
fun convertLongToMonthString(date: Long?): String {
    if (date == null || date == 0L)
        return ""
    val monthFormatter = SimpleDateFormat("MMMM")
    return monthFormatter.format(Date(date)).toString()
}


@SuppressLint("SimpleDateFormat")
fun convertLongToYearString(date: Long?): String {
    if (date == null || date == 0L)
        return ""
    val yearFormatter = SimpleDateFormat("yyyy")
    return yearFormatter.format(Date(date)).toString()
}

/*
fun convertCategoriesCSVtoList(categoriesString: String): MutableList<String> {
    return categoriesString.split(",").map { it.trim() }.distinct() as MutableList<String>
}

fun convertCategoriesListtoCSVString(categoriesList: MutableList<String>): String {
    return categoriesList.sorted().joinToString(separator = ", ")
}
 */

fun isValidEmail(emailString: String?): Boolean {
    return !TextUtils.isEmpty(emailString) && Patterns.EMAIL_ADDRESS.matcher(emailString).matches()
}

fun isValidURL(urlString: String?): Boolean {
    return !TextUtils.isEmpty(urlString) && Patterns.WEB_URL.matcher(urlString).matches()
}