/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.flavored

import android.app.Activity
import androidx.preference.PreferenceManager
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.flavored.ReviewManagerDefinition.Companion.PREFS_NEXT_REQUEST
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class JtxReviewManager(val activity: Activity) : ReviewManagerDefinition {

    private val daysToFirstDialog = if(BuildConfig.DEBUG) 1L else 30L

    override var nextRequestOn: Long
        get() = PreferenceManager.getDefaultSharedPreferences(activity).getLong(PREFS_NEXT_REQUEST, 0L)
        set(value) {
            PreferenceManager.getDefaultSharedPreferences(activity).edit().putLong(PREFS_NEXT_REQUEST, value).apply()
        }

    override fun showIfApplicable(): Boolean {

        if(nextRequestOn == 0L) // first request for donation 30 days after install
            nextRequestOn = ZonedDateTime.now().plusDays(daysToFirstDialog).toInstant().toEpochMilli()

        return Instant.ofEpochMilli(nextRequestOn).atZone(ZoneId.systemDefault()) <= ZonedDateTime.now()
    }
}