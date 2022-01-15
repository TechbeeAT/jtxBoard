/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.monetization.AdManager
import at.techbee.jtx.monetization.BillingManager
import at.techbee.jtx.ui.IcalListFragmentDirections
import at.techbee.jtx.ui.SettingsFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.fortuna.ical4j.model.TimeZoneRegistryFactory
import java.io.File
import java.io.IOException


// this is necessary for the app permission, 100  and 200 ist just a freely chosen value
const val CONTACT_READ_PERMISSION_CODE = 100
const val RECORD_AUDIO_PERMISSION_CODE = 200

const val AUTHORITY_FILEPROVIDER = "at.techbee.jtx.fileprovider"

/**
 * This main activity is just a container for our fragments,
 * where the real action is.
 */
class MainActivity : AppCompatActivity()  {

    companion object {
        const val CHANNEL_REMINDER_DUE = "REMINDER_DUE"

        const val BUILD_FLAVOR_OSE = "ose"
        const val BUILD_FLAVOR_GOOGLEPLAY = "gplay"
        const val BUILD_FLAVOR_HUAWEI = "huawei"
        const val BUILD_FLAVOR_GLOBAL = "global"

        private const val PREFS_MAIN = "sharedPreferencesMainActivity"
        private const val PREFS_MAIN_ADINFO_DIALOG_SHOWN = "adInfoDialogShown"
    }

    private lateinit var toolbar: Toolbar

    private var settings: SharedPreferences? = null
    private var mainActivityPrefs: SharedPreferences? = null

    private var lastProcessedIntentHash: Int? = null



    override fun onApplyThemeResource(theme: Resources.Theme?, resid: Int, first: Boolean) {
        // use dynamic color chosen by user
        DynamicColors.applyToActivitiesIfAvailable(this.application)
        super.onApplyThemeResource(theme, resid, first)
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //load settings
        settings = PreferenceManager.getDefaultSharedPreferences(this)
        mainActivityPrefs = getSharedPreferences(PREFS_MAIN, Context.MODE_PRIVATE)

        // Register Notification Channel for Reminders
        createNotificationChannel()

        // Set up the toolbar with the navigation drawer
        toolbar = findViewById(R.id.topAppBar)
        //setToolbarTitle("Board", null)
        toolbar.setTitleTextAppearance(this, R.style.jtx_MontserratFont)
        toolbar.setSubtitleTextAppearance(this, R.style.jtx_MontserratFont)

        setSupportActionBar(toolbar)

        // necessary for ical4j
        TimeZoneRegistryFactory.getInstance().createRegistry()

        adaptMenuToBuildFlavor()
        setUpDrawer()
        checkThemeSetting()

        BillingManager.getInstance()?.initialise(this)
        BillingManager.getInstance()?.isAdFreeSubscriptionPurchased?.observe(this) {
            if(it)
                AdManager.getInstance()?.checkOrRequestConsentAndLoadAds(this, applicationContext)
        }

        if(AdManager.getInstance()?.isAdFlavor() == true)                        // check if flavor is ad-Flavor and if ads should be shown
            showAdInfoDialogIfNecessary()  // show AdInfo Dialog and initialize ads

    }

    override fun onResume() {
        super.onResume()

        //hanlde intents, but only if it wasn't already handled
        if(intent.hashCode() != lastProcessedIntentHash) {

            intent?.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

            // handle the intents for the shortcuts
            when (intent?.action) {
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
                // Take data also from other sharing intents
                Intent.ACTION_SEND -> {

                    val options = arrayOf(
                        getString(R.string.intent_dialog_add_journal),
                        getString(R.string.intent_dialog_add_note),
                        getString(R.string.intent_dialog_add_task)
                    )
                    AlertDialog.Builder(this)
                        .setTitle(R.string.intent_dialog_title)
                        .setIcon(R.drawable.ic_fromshareintent)
                        .setItems(
                            options
                        ) { _, selection ->
                            val iCalObject = when (selection) {
                                0 -> ICalObject.createJournal()
                                1 -> ICalObject.createNote()
                                2 -> ICalObject.createTodo()
                                else -> return@setItems
                            }
                            val entity = ICalEntity(iCalObject)
                            if (intent.type == "text/plain") {
                                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                                iCalObject.parseSummaryAndDescription(text)
                                val categories = Category.extractHashtagsFromText(text)
                                entity.categories = categories
                            } else if (intent.type?.startsWith("image/") == true || intent.type == "application/pdf") {
                                (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {

                                    try {
                                        val extension = MimeTypeMap.getSingleton()
                                            .getExtensionFromMimeType(intent.type)
                                        val filename = "${System.currentTimeMillis()}.$extension"
                                        val newFile =
                                            File(Attachment.getAttachmentDirectory(this), filename)
                                        newFile.createNewFile()

                                        val attachmentDescriptor =
                                            this.contentResolver.openFileDescriptor(it, "r")
                                        val attachmentBytes =
                                            ParcelFileDescriptor.AutoCloseInputStream(
                                                attachmentDescriptor
                                            ).readBytes()
                                        newFile.writeBytes(attachmentBytes)

                                        val newAttachment = Attachment(
                                            uri = FileProvider.getUriForFile(
                                                this,
                                                AUTHORITY_FILEPROVIDER,
                                                newFile
                                            ).toString(),
                                            filename = newFile.name,
                                            extension = newFile.extension,
                                            fmttype = intent.type
                                        )
                                        entity.attachments = listOf(newAttachment)

                                    } catch (e: IOException) {
                                        Log.e("IOException", "Failed to process file\n$e")
                                    }
                                }
                            }
                            findNavController(R.id.nav_host_fragment)
                                .navigate(
                                    IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(
                                        entity
                                    )
                                )
                        }
                        .show()
                }
            }
            lastProcessedIntentHash = intent.hashCode()
        }
    }

    private fun adaptMenuToBuildFlavor() {
        val navView: NavigationView = findViewById(R.id.nav_view)

        when (BuildConfig.FLAVOR) {
            BUILD_FLAVOR_GOOGLEPLAY -> navView.menu.findItem(R.id.nav_donate).isVisible = false     // hide the donate menu for google play
            BUILD_FLAVOR_HUAWEI -> navView.menu.findItem(R.id.nav_donate).isVisible = false     // hide the donate menu for google play
            BUILD_FLAVOR_GLOBAL -> navView.menu.findItem(R.id.nav_donate).isVisible = false         // hide the donate menu for app stores with ads
            BUILD_FLAVOR_OSE -> navView.menu.findItem(R.id.nav_adinfo).isVisible = false            // hide the adinfo for the OSE-edition
            // BUILD_FLAVOR_ALPHA shows both menu items for testing
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

                R.id.nav_board ->
                    findNavController(R.id.nav_host_fragment)
                        .navigate(R.id.action_global_icalListFragment)

                R.id.nav_collections ->
                    findNavController(R.id.nav_host_fragment)
                        .navigate(R.id.action_global_collectionsFragment)

                R.id.nav_sync ->
                    findNavController(R.id.nav_host_fragment)
                        .navigate(R.id.action_global_syncFragment)

                R.id.nav_about ->
                    findNavController(R.id.nav_host_fragment)
                        .navigate(R.id.action_global_aboutFragment)

                R.id.nav_donate ->
                    findNavController(R.id.nav_host_fragment)
                        .navigate(R.id.action_global_donateFragment)

                R.id.nav_adinfo ->
                    findNavController(R.id.nav_host_fragment)
                        .navigate(R.id.action_global_adInfoFragment)

                R.id.nav_app_settings ->
                    findNavController(R.id.nav_host_fragment)
                        .navigate(R.id.action_global_settingsFragment)

                R.id.nav_twitter ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_jtx_twitter))))

                R.id.nav_website ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_jtx))))

                R.id.nav_support ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_jtx_support))))

                R.id.nav_privacy ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_jtx_privacy_policy))))
            }
            true
        }
    }

    /**
     * Checks in the settings if night mode is enforced and switches to it if applicable
     */
    private fun checkThemeSetting() {
        // user interface settings
        val enforceDark = settings?.getBoolean(SettingsFragment.ENFORCE_DARK_THEME, false) ?: false
        if (enforceDark)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }



    // this is called when the user accepts a permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == CONTACT_READ_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, R.string.permission_read_contacts_granted, Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, R.string.permission_read_contacts_denied, Toast.LENGTH_SHORT).show()
        } else if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, R.string.permission_record_audio_granted, Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, R.string.permission_record_audio_denied, Toast.LENGTH_SHORT).show()
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


    /**
     * Sets the toolbar with the given title and subtitle
     * @param [title] to be set in the toolbar
     * @param [subtitle] to be set in the toolbar
     */
    fun setToolbarTitle(title: String, subtitle: String?) {
        toolbar.title = title
        toolbar.subtitle = subtitle
    }


    /**
     * Shows a Dialog with Ad-Info and option to buy the subscription
     * Currently this should only be used for the GooglePlay flavor!
     */
    private fun showAdInfoDialogIfNecessary() {

        val adInfoShown = mainActivityPrefs?.getBoolean(PREFS_MAIN_ADINFO_DIALOG_SHOWN, false) ?: false
        if (!adInfoShown) {   // show a dialog if ads were not accepted yet
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.list_dialog_contribution_title))
                .setMessage(resources.getString(R.string.list_dialog_contribution_message))
                .setNegativeButton(resources.getString(R.string.list_dialog_contribution_more_information)) { _, _ ->
                    findNavController(R.id.nav_host_fragment)
                        .navigate(R.id.action_global_adInfoFragment)
                }
                .setPositiveButton(resources.getString(R.string.gotit)) { _, _ ->
                    AdManager.getInstance()?.checkOrRequestConsentAndLoadAds(this, applicationContext)
                }
                .show()

            mainActivityPrefs?.edit()?.putBoolean(PREFS_MAIN_ADINFO_DIALOG_SHOWN, true)?.apply()   // once shown, we don't show it again
        }
        // otherwise load the user consent (if necessary) and then load the ads
        else  {
            AdManager.getInstance()?.checkOrRequestConsentAndLoadAds(this, applicationContext)
            Log.d("AdInfoShown", "AdInfo was shown, loading consent form if necessary")
        }
    }
}
