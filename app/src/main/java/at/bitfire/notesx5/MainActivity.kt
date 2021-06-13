/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.notesx5

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import at.bitfire.notesx5.database.ICalObject
import at.bitfire.notesx5.database.relations.ICalEntity
import at.bitfire.notesx5.ui.IcalListFragmentDirections
import at.bitfire.notesx5.ui.SettingsFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.ump.*
import java.util.concurrent.TimeUnit


// this is necessary for the app permission, 100  and 200 ist just a freely chosen value
const val CONTACT_READ_PERMISSION_CODE = 100
const val RECORD_AUDIO_PERMISSION_CODE = 200

const val PICKFILE_RESULT_CODE = 301
const val REQUEST_IMAGE_CAPTURE_CODE = 401

const val AUTHORITY_FILEPROVIDER = "at.bitfire.notesx5.fileprovider"


/**
 * This main activity is just a container for our fragments,
 * where the real action is.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        const val CHANNEL_REMINDER_DUE = "REMINDER_DUE"
        const val TRIAL_PERIOD_DAYS = 14L

        const val ADMOB_UNIT_ID_BANNER_TEST = "ca-app-pub-3940256099942544/6300978111"
        const val ADMOB_UNIT_ID_BANNER_PROD = "ca-app-pub-4426141011962540/8164268500"

    }

    lateinit var toolbar: Toolbar
    var consentInformation: ConsentInformation? = null
    var consentForm: ConsentForm? = null
    var settings: SharedPreferences? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //load settings
        settings = PreferenceManager.getDefaultSharedPreferences(this)


        // Register Notification Channel for Reminders
        createNotificationChannel()

        // Set up the toolbar with the navigation drawer
        toolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)

        setUpDrawer()
        checkThemeSetting()

        // if trial period ended, then initialize the consent to show ads
        // TODO: opt out and other options
        if (!isTrialPeriod()) {

            //MaterialAlertDialogBuilder(applicationContext, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered)
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.list_dialog_contribution_title))
                .setMessage(resources.getString(R.string.list_dialog_contribution_message))
                .setNegativeButton(resources.getString(R.string.list_dialog_contribution_buyadfree)) { dialog, which ->
                    // Respond to negative button press
                    settings!!.edit().putBoolean(SettingsFragment.SHOW_ADS, false).apply()
                    //TODO: open in app-buying for ad-free option
                }
                .setPositiveButton(resources.getString(R.string.list_dialog_contribution_acceptads)) { dialog, which ->
                    // Respond to positive button press
                    // Ads are accepted, load user consent
                    settings!!.edit().putBoolean(SettingsFragment.SHOW_ADS, true).apply()
                    initializeUserConsent()

                }
                .show()
        }

    }


    override fun onResume() {
        super.onResume()

        // handle the intents for the shortcuts
        when (intent.action) {
            "addJournal" -> {
                findNavController(R.id.nav_host_fragment)
                    .navigate(
                        IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(
                            ICalEntity(ICalObject.createJournal())
                        )
                    )
            }
            "addNote" -> {
                findNavController(R.id.nav_host_fragment)
                    .navigate(
                        IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(
                            ICalEntity(ICalObject.createNote())
                        )
                    )
            }
            "addTodo" -> {
                findNavController(R.id.nav_host_fragment)
                    .navigate(
                        IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(
                            ICalEntity(ICalObject.createTodo())
                        )
                    )
            }
        }

    }


    private fun setUpDrawer() {

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_main_layout)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        // React on selection in Navigation View
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener { menuItem ->

            //close drawer
            drawerLayout.close()

            // Handle menu item selected
            when (menuItem.itemId) {

                R.id.nav_about ->
                    findNavController(R.id.nav_host_fragment)
                        .navigate(R.id.action_global_aboutFragment)

                R.id.nav_app_settings ->
                    findNavController(R.id.nav_host_fragment)
                        .navigate(R.id.action_global_settingsFragment)

                R.id.nav_twitter ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bitfire.at")))

                R.id.nav_website ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bitfire.at")))

                R.id.nav_manual ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bitfire.at")))

                R.id.nav_faq ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bitfire.at")))

                R.id.nav_forums ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bitfire.at")))

                R.id.nav_donate ->
                    //if (BuildConfig.FLAVOR != App.FLAVOR_GOOGLE_PLAY)
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bitfire.at")))

                R.id.nav_privacy ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bitfire.at")))
            }
            true
        }
    }

    private fun checkThemeSetting() {
        // user interface settings
        val enforceDark = settings!!.getBoolean(SettingsFragment.ENFORCE_DARK_THEME, false)
        if (enforceDark)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    fun isTrialPeriod(): Boolean {
        val firstInstalled: Long = this.applicationContext.packageManager.getPackageInfo(
            applicationContext.packageName,
            0
        ).firstInstallTime
        // TODO come back here
        //val trialEnd = firstInstalled + TimeUnit.DAYS.toMillis(TRIAL_PERIOD_DAYS)
        val trialEnd = firstInstalled + TimeUnit.MINUTES.toMillis(5L)    // for testing

        return System.currentTimeMillis() <= trialEnd
    }


    fun setUpAds() {

        // TODO: replace adUnitId with production Unit Id
        MobileAds.initialize(this) {}

        // Section to retrieve the width of the device to set the adSize
        val outMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display!!.getRealMetrics(outMetrics)
        } else {
            val display = windowManager.defaultDisplay
            display.getMetrics(outMetrics)
        }
        val adWidth = (outMetrics.widthPixels.toFloat() / outMetrics.density).toInt()
        val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)

        // now that the adSize is determined we can place the add
        val adView = AdView(this)
        //adView.adSize = AdSize.SMART_BANNER          // adaptive ads replace the smart banner
        adView.adSize = adSize
        adView.adUnitId = ADMOB_UNIT_ID_BANNER_TEST  // for testing

        val adLinearLayout: LinearLayout =
            findViewById(R.id.main_adlinearlayout)  // add to the linear layout container
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        adLinearLayout.removeAllViews()
        adLinearLayout.addView(adView)
    }


    // this is called when the user accepts a permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == CONTACT_READ_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Contacts Read Permission Granted", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, "Contacts Read Permission Denied", Toast.LENGTH_SHORT).show()
        } else if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Record Audio Permission Granted", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, "Record Audio Permission Denied", Toast.LENGTH_SHORT).show()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_reminder_name)
            val descriptionText = getString(R.string.notification_channel_reminder_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_REMINDER_DUE, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    fun initializeUserConsent() {

        val debugSettings = ConsentDebugSettings.Builder(this)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            //.addTestDeviceHashedId("TEST-DEVICE-HASHED-ID")
            .build()


        // Set tag for underage of consent. false means users are not underage.
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .setConsentDebugSettings(debugSettings)      // only for testing, todo: remove for production!
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                // The consent information state was updated.
                // You are now ready to check if a form is available.
                Log.d("ConsentInformation", consentInformation.consentStatus.toString())
                loadForm(consentInformation)
            },
            {
                // Handle the error.
            })

        this@MainActivity.consentInformation = consentInformation

    }

    private fun loadForm(consentInformation: ConsentInformation) {

        //consentInformation.reset()
        if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED || consentInformation.consentStatus == ConsentInformation.ConsentStatus.UNKNOWN) {

            UserMessagingPlatform.loadConsentForm(
                this,
                { consentForm ->

                    consentForm.show(this) {
                        //Handle dismissal by reloading form
                        loadForm(consentInformation)
                    }
                    this.consentForm = consentForm
                    setUpAds()
                }
            ) {
                // Handle the error
                Log.d("consentForm", "Failed loading consent form")
                Log.d("consentForm", it.message)
            }
        } else {
            setUpAds()
        }
    }

    fun resetUserConsent() {

        // just show the message, the consentInformation is already loaded
        UserMessagingPlatform.loadConsentForm(
            this,
            { consentForm ->
                consentForm.show(this) {
                    //Handle dismissal by reloading form
                    loadForm(consentInformation!!)
                }
                this.consentForm = consentForm
            }
        ) {
            // Handle the error
            Log.d("consentForm", "Failed loading consent form")
            Log.d("consentForm", it.message)
        }

    }
}


