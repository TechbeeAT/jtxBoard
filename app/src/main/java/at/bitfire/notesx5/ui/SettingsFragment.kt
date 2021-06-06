package at.bitfire.notesx5.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.get
import at.bitfire.notesx5.MainActivity
import at.bitfire.notesx5.R


class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        const val ENFORCE_DARK_THEME = "settings_enfore_dark_theme"
        const val SHOW_USER_CONSENT = "show_user_consent"
        const val SHOW_SUBTASKS_IN_LIST = "settings_show_subtasks_in_list"
        const val SHOW_ATTACHMENTS_IN_LIST = "settings_show_attachments_in_list"

    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        // register a change listener for the theme to update the UI immediately
        val settings = PreferenceManager.getDefaultSharedPreferences(requireContext())
        settings.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            val enforceDark = sharedPreferences.getBoolean(ENFORCE_DARK_THEME, false)
            if (enforceDark)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        preferenceScreen.get<Preference>(SHOW_USER_CONSENT)?.setOnPreferenceClickListener {
            Log.d("showUserConsent", "Clicked on Show User Consent")
            val mainActivity = requireActivity() as MainActivity
            mainActivity.resetUserConsent()
            return@setOnPreferenceClickListener true
        }

    }
}