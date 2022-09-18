/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.settings

import android.util.Log
import at.techbee.jtx.R
import java.lang.NumberFormatException

enum class DropdownSettingOption(
    val key: String,
    val text: Int
) {
    THEME_SYSTEM("system", R.string.settings_select_theme_system),
    THEME_LIGHT("light", R.string.settings_select_theme_light),
    THEME_DARK("dark", R.string.settings_select_theme_dark),

    AUDIO_FORMAT_3GPP("audio/3gpp", R.string.settings_audio_format_3gpp),
    AUDIO_FORMAT_AAC("audio/aac", R.string.settings_audio_format_aac),
    AUDIO_FORMAT_OGG("audio/ogg", R.string.settings_audio_format_ogg),

    DEFAULT_DATE_NONE("null", R.string.settings_default_date_none),
    DEFAULT_DATE_SAME_DAY("P0D", R.string.settings_default_date_same_day),
    DEFAULT_DATE_NEXT_DAY("P1D", R.string.settings_default_date_next_day),
    DEFAULT_DATE_TWO_DAYS("P2D", R.string.settings_default_date_two_days),
    DEFAULT_DATE_THREE_DAYS("P3D", R.string.settings_default_date_three_days),
    DEFAULT_DATE_ONE_WEEK("P7D", R.string.settings_default_date_one_week),
    DEFAULT_DATE_TWO_WEEKS("P14D", R.string.settings_default_date_two_weeks),
    DEFAULT_DATE_FOUR_WEEKS("P28D", R.string.settings_default_date_four_weeks),

    PROGRESS_STEP_1("1", R.string.settings_progress_step_1),
    PROGRESS_STEP_2("2", R.string.settings_progress_step_2),
    PROGRESS_STEP_5("5", R.string.settings_progress_step_5),
    PROGRESS_STEP_10("10", R.string.settings_progress_step_10),
    PROGRESS_STEP_20("20", R.string.settings_progress_step_20),
    PROGRESS_STEP_25("25", R.string.settings_progress_step_25),
    PROGRESS_STEP_50("50", R.string.settings_progress_step_50);


    fun getProgressStepKeyAsInt(): Int {
        return try {
            this.key.toInt()
        } catch(e: NumberFormatException) {
            Log.w("KeyIsNotInt", "Failed casting key to Int: Key $key")
            1
        }
    }
}

