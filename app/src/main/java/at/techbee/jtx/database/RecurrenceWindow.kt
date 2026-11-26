/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database

/**
 * How far around its reference point recurrence instances are materialised.
 *
 * [Legacy] centres the window on DTSTART, and since nothing is generated before DTSTART that
 * reaches forward from the start of the series: for a weekly series older than a year the window
 * ends before today and no current occurrence is created at all.
 */
sealed interface RecurrenceWindow {

    data object Legacy : RecurrenceWindow

    data class AroundToday(val monthsBack: Long, val monthsAhead: Long) : RecurrenceWindow

    companion object {
        // Read from getInstancesFromRrule(), which is reached from Room DAO methods and from the
        // sync content provider - neither carries a Context, so passing the value in would thread a
        // parameter up into the ViewModels.
        @Volatile
        var current: RecurrenceWindow = Legacy
    }
}
