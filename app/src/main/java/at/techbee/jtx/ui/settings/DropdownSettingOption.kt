/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.settings

import android.util.Log
import androidx.annotation.StringRes
import at.techbee.jtx.R

enum class DropdownSettingOption(
    val key: String,
    @StringRes val text: Int
) {
    THEME_SYSTEM("system", R.string.settings_select_theme_system),
    THEME_LIGHT("light", R.string.settings_select_theme_light),
    THEME_DARK("dark", R.string.settings_select_theme_dark),
    THEME_TRUE_DARK("truedark", R.string.settings_select_theme_true_dark),
    THEME_CONTRAST("contrast", R.string.settings_select_theme_contrast),

    AUDIO_FORMAT_3GPP("audio/3gpp", R.string.settings_audio_format_3gpp),
    AUDIO_FORMAT_AAC("audio/aac", R.string.settings_audio_format_aac),
    AUDIO_FORMAT_OGG("audio/ogg", R.string.settings_audio_format_ogg),
    AUDIO_FORMAT_MP4("audio/mp4", R.string.settings_audio_format_mp4),

    DEFAULT_JOURNALS_DATE_PREVIOUS_DAY("previous_day", R.string.settings_default_journals_date_previous_day),
    DEFAULT_JOURNALS_DATE_CURRENT_DAY("current_day", R.string.settings_default_journals_date_current_day),
    DEFAULT_JOURNALS_DATE_CURRENT_HOUR("current_hour", R.string.settings_default_journals_date_current_hour),
    DEFAULT_JOURNALS_DATE_CURRENT_15MIN("current_15min", R.string.settings_default_journals_date_current_15min),
    DEFAULT_JOURNALS_DATE_CURRENT_5MIN("current_5min", R.string.settings_default_journals_date_current_5min),
    DEFAULT_JOURNALS_DATE_CURRENT_MIN("current_min", R.string.settings_default_journals_date_current_min),

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
    PROGRESS_STEP_50("50", R.string.settings_progress_step_50),

    AUTO_ALARM_OFF("off", R.string.off),
    AUTO_ALARM_ON_START("on_start", R.string.alarms_onstart),
    AUTO_ALARM_ON_DUE("on_due", R.string.alarms_ondue),
    AUTO_ALARM_ALWAYS_ON_DUE("always_on_due", R.string.alarms_always_ondue),
    AUTO_ALARM_ALWAYS_ON_SAVE("always_on_save", R.string.alarms_always_on_save),

    PROTECT_BIOMETRIC_OFF("protect_biometric_off", R.string.off),
    PROTECT_BIOMETRIC_CONFIDENTIAL("protect_biometric_confidential", R.string.settings_protect_biometric_confidential),
    PROTECT_BIOMETRIC_PRIVATE_CONFIDENTIAL("protect_biometric_private_confidential", R.string.settings_protect_biometric_private_confidential),
    PROTECT_BIOMETRIC_ALL("protect_biometric_all", R.string.settings_protect_biometric_all),

    DISPLAY_TIMEZONE_LOCAL("settings_timezone_display_local", R.string.settings_timezone_display_local),
    DISPLAY_TIMEZONE_ORIGINAL("settings_timezone_display_original", R.string.settings_timezone_display_original),
    DISPLAY_TIMEZONE_LOCAL_AND_ORIGINAL("settings_timezone_display_local_and_original", R.string.settings_timezone_display_local_and_original),

    FONT_ROBOTO("font_roboto", R.string.font_roboto),
    FONT_NOTO("font_noto", R.string.font_noto),
    FONT_MONTSERRAT_ALTERNATES("font_montserrat_alternates", R.string.font_montserrat_alternates)
    ;

    fun getProgressStepKeyAsInt(): Int {
        return try {
            this.key.toInt()
        } catch(e: NumberFormatException) {
            Log.w("KeyIsNotInt", "Failed casting key to Int: Key $key")
            1
        }
    }

    companion object {

        /**
         * Returns the DropdownSettingOption for a given key or null.
         */
        fun fromKey(key: String?): DropdownSettingOption? {
            return entries.find { it.key == key }
        }
    }
}

