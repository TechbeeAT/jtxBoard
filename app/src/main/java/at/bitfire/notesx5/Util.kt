/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
fun convertLongToDateString(date: Long): String {
    if (date == 0L)
        return ""
    return DateFormat.getDateInstance(DateFormat.LONG).format(date)
}

@SuppressLint("SimpleDateFormat")
fun convertLongToTimeString(time: Long): String {
    if (time == 0L)
        return ""
    return DateFormat.getTimeInstance(DateFormat.SHORT).format(time)
}


@SuppressLint("SimpleDateFormat")
fun convertLongToHourString(time: Long): String {
    if (time == 0L)
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

@SuppressLint("SimpleDateFormat")
fun convertLongToDayString(date: Long): String {
    if (date == 0L)
        return ""
    val day_formatter = SimpleDateFormat("dd")
    return day_formatter.format(Date(date)).toString()
}


@SuppressLint("SimpleDateFormat")
fun convertLongToMonthString(date: Long): String {
    if (date == 0L)
        return ""
    val month_formatter = SimpleDateFormat("MMMM")
    return month_formatter.format(Date(date)).toString()
}


@SuppressLint("SimpleDateFormat")
fun convertLongToYearString(date: Long): String {
    if (date == 0L)
        return ""
    val year_formatter = SimpleDateFormat("yyyy")
    return year_formatter.format(Date(date)).toString()
}

fun convertCategoriesCSVtoList(categoriesString: String): MutableList<String> {
    return categoriesString.split(",").map { it.trim() }.distinct() as MutableList<String>
}

fun convertCategoriesListtoCSVString(categoriesList: MutableList<String>): String {
    return categoriesList.sorted().joinToString(separator = ", ")
}

fun isValidEmail(emailString: String?): Boolean {
    return !TextUtils.isEmpty(emailString) && Patterns.EMAIL_ADDRESS.matcher(emailString).matches()
}

fun isValidURL(urlString: String?): Boolean {
    return !TextUtils.isEmpty(urlString) && Patterns.WEB_URL.matcher(urlString).matches()
}