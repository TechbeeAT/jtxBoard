/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.util

import android.icu.text.MessageFormat
import android.util.Log
import androidx.core.text.util.LocalePreferences
import at.techbee.jtx.database.ICalObject.Companion.TZ_ALLDAY
import java.time.DateTimeException
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

object DateTimeUtils {

    fun convertLongToFullDateTimeString(date: Long?, timezone: String?): String {
        if (date == null || date == 0L)
            return ""
        val zonedDateTime =
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), requireTzId(timezone))
        val formatter = when (timezone) {
            null -> DateTimeFormatter.ofLocalizedDateTime(
                FormatStyle.FULL,
                FormatStyle.SHORT
            )  // short Format for time to not show the timezone info
            TZ_ALLDAY -> DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)   // only date
            else -> DateTimeFormatter.ofLocalizedDateTime(
                FormatStyle.FULL,
                FormatStyle.LONG
            )  // FormatStyle.LONG also shows seconds, maybe a solution could be found to remove this in the future
        }
        return zonedDateTime.format(formatter)
    }

    fun convertLongToShortDateTimeString(date: Long?, timezone: String?): String {
        if (date == null || date == 0L)
            return ""
        val zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), requireTzId(timezone))
        val formatter = when (timezone) {
            null -> DateTimeFormatter.ofLocalizedDateTime(
                FormatStyle.SHORT,
                FormatStyle.SHORT
            )  // short Format for time to not show the timezone info
            TZ_ALLDAY -> DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)   // only date
            else -> DateTimeFormatter.ofLocalizedDateTime(
                FormatStyle.SHORT,
                FormatStyle.LONG
            )  // FormatStyle.LONG also shows seconds, maybe a solution could be found to remove this in the future
        }
        return zonedDateTime.format(formatter)
    }

    /**
     * Creates a string from the date that can be used for the CSV export
     */
    fun convertLongToExcelDateTimeString(date: Long?, timezone: String?): String {
        if (date == null || date == 0L)
            return ""
        val zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), requireTzId(timezone))
        val formatter =  DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
        return zonedDateTime.format(formatter)
    }



    fun convertLongToFullDateString(date: Long?, timezone: String?): String {
        if (date == null || date == 0L)
            return ""
        val zonedDateTime =
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), requireTzId(timezone))
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
        return zonedDateTime.format(formatter)
    }

    fun convertLongToShortDateString(date: Long?, timezone: String?): String {
        if (date == null || date == 0L)
            return ""
        val zonedDateTime =
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), requireTzId(timezone))
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        return zonedDateTime.format(formatter)
    }

    fun convertLongToMediumDateShortTimeString(date: Long?, timezone: String?): String {
        return convertLongToMediumDateString(date, timezone) + if(timezone != TZ_ALLDAY) " " + convertLongToShortTimeString(date, timezone) else ""
    }

    fun convertLongToMediumDateString(date: Long?, timezone: String?): String {
        if (date == null || date == 0L)
            return ""
        val zonedDateTime =
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), requireTzId(timezone))
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        return zonedDateTime.format(formatter)
    }

    /*
    private fun convertLongToTimeString(time: Long?, timezone: String?): String {
        if (time == null || time == 0L || timezone == TZ_ALLDAY)
            return ""
        val zonedDateTime =
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), requireTzId(timezone))
        val formatter = if(timezone == null) DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) else  DateTimeFormatter.ofLocalizedTime(FormatStyle.LONG)
        return zonedDateTime.format(formatter)
    }
     */

    fun convertLongToShortTimeString(time: Long?, timezone: String?): String {
        if (time == null || time == 0L || timezone == TZ_ALLDAY)
            return ""
        val zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), requireTzId(timezone))
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        return zonedDateTime.format(formatter)
    }

    fun convertLongToDayString(date: Long?, timezone: String?): String {
        if (date == null || date == 0L)
            return ""
        val zonedDateTime =
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), requireTzId(timezone))
        val formatter = DateTimeFormatter.ofPattern("dd", Locale.getDefault())
        return zonedDateTime.toLocalDateTime().format(formatter)
    }

    fun convertLongToWeekdayString(date: Long?, timezone: String?): String {
        if (date == null || date == 0L)
            return ""
        val zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), requireTzId(timezone))
        return zonedDateTime.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }


    fun convertLongToMonthString(date: Long?, timezone: String?): String {
        if (date == null || date == 0L)
            return ""
        val zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), requireTzId(timezone))
        return zonedDateTime.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }


    fun convertLongToYearString(date: Long?, timezone: String?): String {
        if (date == null || date == 0L)
            return ""
        val zonedDateTime =
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), requireTzId(timezone))
        val formatter = DateTimeFormatter.ofPattern("yyyy", Locale.getDefault())
        return zonedDateTime.toLocalDateTime().format(formatter)
    }

    fun convertLongToYYYYMMDDString(date: Long?, timezone: String?): String {
        if (date == null || date == 0L)
            return ""
        val zonedDateTime =
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), requireTzId(timezone))
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
        return zonedDateTime.toLocalDateTime().format(formatter)
    }

    fun timestampAsFilenameAppendix(): String = convertLongToYYYYMMDDString(System.currentTimeMillis(),null)


    /**
     * Returns the localized ordinal string for a given number.
     *
     * Uses [MessageFormat] to get the localized ordinal string.
     *
     * @param number The number for which to get the ordinal string.
     * @return The localized ordinal string for the given number.
     */
    fun getLocalizedOrdinalFor(number: Int): String {
         val formatter = MessageFormat("{0,ordinal}", Locale.getDefault())
         return formatter.format(arrayOf(number))
    }


    /**
     * Determines the first day of the week for the current device.
     *
     * In contrast to [WeekFields.of] (which only derives the week start from the language/region
     * of the locale, e.g. en-US -> Sunday), this respects the user's system setting under
     * "Regional preferences -> First day of week" (available since Android 13). That setting is
     * exposed through the locale's "-u-fw-" unicode extension, which is read by
     * [LocalePreferences.getFirstDayOfWeek]. If the user did not override the setting, it falls
     * back to the locale's default (ICU) value.
     *
     * @return the first [DayOfWeek] of the week for the local device
     */
    fun getLocalizedFirstDayOfWeek(): DayOfWeek = when (LocalePreferences.getFirstDayOfWeek()) {
        LocalePreferences.FirstDayOfWeek.MONDAY -> DayOfWeek.MONDAY
        LocalePreferences.FirstDayOfWeek.TUESDAY -> DayOfWeek.TUESDAY
        LocalePreferences.FirstDayOfWeek.WEDNESDAY -> DayOfWeek.WEDNESDAY
        LocalePreferences.FirstDayOfWeek.THURSDAY -> DayOfWeek.THURSDAY
        LocalePreferences.FirstDayOfWeek.FRIDAY -> DayOfWeek.FRIDAY
        LocalePreferences.FirstDayOfWeek.SATURDAY -> DayOfWeek.SATURDAY
        LocalePreferences.FirstDayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
        else -> WeekFields.of(Locale.getDefault()).firstDayOfWeek  // fallback if the value is unknown/DEFAULT
    }

    /**
     * @return the seven [DayOfWeek]s of the week, ordered starting with the device's first day of
     * the week (respecting the "Regional preferences -> First day of week" system setting, see
     * [getLocalizedFirstDayOfWeek]).
     */
    fun getLocalizedDaysOfWeek(): List<DayOfWeek> {
        val firstDayOfWeek = getLocalizedFirstDayOfWeek()
        return (0L until 7L).map { firstDayOfWeek.plus(it) }
    }

    /**
     * @return [WeekFields] that use the device's first day of the week (respecting the
     * "Regional preferences -> First day of week" system setting, see [getLocalizedFirstDayOfWeek])
     * while keeping the locale's minimal days in the first week for week numbering.
     */
    fun getLocalizedWeekFields(): WeekFields = WeekFields.of(
        getLocalizedFirstDayOfWeek(),
        WeekFields.of(Locale.getDefault()).minimalDaysInFirstWeek
    )


    fun addLongToCSVString(listAsString: String?, value: Long?): String? {

        if (value == null)
            return null

        if (listAsString.isNullOrBlank())
            return value.toString()

        val stringList = listAsString.split(",")
        val newStringList = mutableListOf<String>()
        newStringList.addAll(stringList)
        if (!newStringList.contains(value.toString()))
            newStringList.add(value.toString())

        return if (newStringList.isEmpty())
            null
        else
            newStringList.joinToString(",")
    }

    fun getLongListfromCSVString(listAsString: String?): List<Long> {

        if (listAsString == null)
            return emptyList()

        val stringList = listAsString.split(",")
        val longList = mutableListOf<Long>()

        stringList.forEach {
            try {
                longList.add(it.toLong())
            } catch (e: NumberFormatException) {
                Log.w("NumberFormatException", "Failed to convert Long to String ($it)\n$e")
            }
        }
        return longList
    }


    /**
     * Gets a [ZoneId] from a String
     * @return ZoneId.systemDefault if the Timezone is not set or if it is an all-day event,
     * The ZoneId of the given String or "UTC" if the string could not be parsed
     */
    fun requireTzId(timezone: String?): ZoneId {
        return when(timezone) {
            null -> ZoneId.systemDefault()
            TZ_ALLDAY -> ZoneId.of("UTC")
            else -> try {
                ZoneId.of(timezone)
            } catch (e: DateTimeException) {
                Log.w("DateTimeException", "Failed to parse timezone $timezone\n$e")
                ZoneId.of("UTC")
            }
        }
    }

    /**
     * @return the current day as Long (the hour, minute, second and millisecond of the current datetime is set to 0)
     */
    fun getTodayAsLong() = LocalDate.now().atStartOfDay().atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()

    /**
     * @param datetime as Long
     * @param timezone as String or null
     * @return if the timezone is TZ_ALLDAY, this returns the ZonedDateTime at the beginning of the day in the local time,
     * if the timezone is empy, it returns the ZonedDateTime in the current timezone, otherwise in the given timezone
     */
    fun getZonedDateTimeInLocalTZ(datetime: Long?, timezone: String?): ZonedDateTime? {
        return if (datetime == null)
            null
        else if(timezone == TZ_ALLDAY)
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(datetime), ZoneId.of("UTC")).withZoneSameLocal(ZoneId.systemDefault())
        else
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(datetime), requireTzId(timezone)).withZoneSameInstant(ZoneId.systemDefault())
    }


    fun toZonedDateTime(epochMillis: Long, tz: String?): ZonedDateTime {
        val instant = Instant.ofEpochMilli(epochMillis)

        return when {
            tz == TZ_ALLDAY -> instant.atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.of("UTC"))
            tz.isNullOrEmpty() -> instant.atZone(ZoneId.systemDefault())
            else -> instant.atZone(ZoneId.of(tz))
        }
    }

    /*
    fun getDateWithoutTime(date: Long?, timezone: String?): Long? = date?.let {
        ZonedDateTime
            .ofInstant(Instant.ofEpochMilli(it), requireTzId(timezone))
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .withZoneSameLocal(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
     */

    /**
     * @param [seconds] that should be brought into a format like 00:00
     * @return the minutes and seconds as string like '00:00'
     */
    fun getMinutesSecondsFormatted(seconds: Int): String {
        var secondsMinutesText = ""
        if(seconds/60 < 10)
            secondsMinutesText += "0"
        secondsMinutesText += (seconds / 60).toString() + ":"
        if(seconds%60 < 10)
            secondsMinutesText += "0"
        secondsMinutesText += (seconds % 60).toString()
        return secondsMinutesText
    }
}