package at.bitfire.notesx5.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import at.bitfire.notesx5.MainActivity
import at.bitfire.notesx5.R


class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        const val ENFORCE_DARK_THEME = "settings_enfore_dark_theme"
        const val SHOW_USER_CONSENT = "show_user_consent"
        const val SHOW_SUBTASKS_IN_LIST = "settings_show_subtasks_in_list"
        const val SHOW_ATTACHMENTS_IN_LIST = "settings_show_attachments_in_list"
        const val SHOW_PROGRESS_IN_LIST = "settings_show_progress_in_list"
        const val ACCEPT_ADS = "settings_accept_ads"



    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val mainActivity = activity as MainActivity


        // register a change listener for the theme to update the UI immediately
        val settings = PreferenceManager.getDefaultSharedPreferences(requireContext())
        settings.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            if(key == ENFORCE_DARK_THEME) {
                val enforceDark = sharedPreferences.getBoolean(ENFORCE_DARK_THEME, false)
                if (enforceDark)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        preferenceScreen.get<Preference>(SHOW_USER_CONSENT)?.setOnPreferenceClickListener {
            Log.d("showUserConsent", "Clicked on Show User Consent")
            mainActivity.resetUserConsent()
            return@setOnPreferenceClickListener true
        }

        preferenceScreen.get<SwitchPreference>(ACCEPT_ADS)?.setOnPreferenceChangeListener { preference, adsEnabled ->
            preferenceScreen.get<Preference>(SHOW_USER_CONSENT)?.isEnabled = adsEnabled as Boolean
            if(adsEnabled)
                mainActivity.initializeUserConsent()     // show user consent if ads get enabled (despite the user bought the full version?)
            else
                Toast.makeText(activity, "Start the Intent for the play store", Toast.LENGTH_LONG).show()
            return@setOnPreferenceChangeListener true
        }

        if(mainActivity.isTrialPeriod()) {
            preferenceScreen.get<Preference>(ACCEPT_ADS)?.isEnabled = false
            preferenceScreen.get<Preference>(SHOW_USER_CONSENT)?.isEnabled = false
        }
    }

    override fun onResume() {

        val activity = requireActivity() as MainActivity
        activity.setToolbarText("Settings")

        super.onResume()
    }

}