/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import at.techbee.jtx.MainActivity
import at.techbee.jtx.R
import java.lang.ClassCastException


class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        const val EXPAND_SUBTASKS_DEFAULT = "settings_expand_subtasks_default"
        const val EXPAND_SUBNOTES_DEFAULT = "settings_expand_subnotes_default"
        const val EXPAND_ATTACHMENTS_DEFAULT = "settings_expand_attachments_default"
        const val SHOW_PROGRESS_FOR_MAINTASKS_IN_LIST = "settings_show_progress_for_maintasks_in_list"
        const val SHOW_PROGRESS_FOR_SUBTASKS_IN_LIST = "settings_show_progress_for_subtasks_in_list"

        /**
         * Preferred theme (light/dark). Value must be one of [AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM]
         * (default if setting is missing), [AppCompatDelegate.MODE_NIGHT_NO] or [AppCompatDelegate.MODE_NIGHT_YES].
         */
        const val PREFERRED_THEME = "settings_theme"
        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
        const val THEME_SYSTEM = "system"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        // register a change listener for the theme to update the UI immediately
        val settings = PreferenceManager.getDefaultSharedPreferences(requireContext())
        settings.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            if(key == PREFERRED_THEME) {
                when(sharedPreferences.getString(PREFERRED_THEME, THEME_SYSTEM)) {
                    THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }
    }

    override fun onResume() {

        try {
            val activity = requireActivity() as MainActivity
            activity.setToolbarTitle(getString(R.string.toolbar_text_settings), null)
        } catch (e: ClassCastException) {
            Log.d("setToolbarText", "Class cast to MainActivity failed (this is common for tests but doesn't really matter)\n$e")
        }
        super.onResume()
    }

}