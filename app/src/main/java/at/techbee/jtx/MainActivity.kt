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
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.util.Log
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
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
import at.techbee.jtx.flavored.AdManager
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.IcalListFragmentDirections
import at.techbee.jtx.ui.SettingsFragment
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.navigation.NavigationView
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

        private const val SETTINGS_PRO_INFO_SHOWN = "settingsProInfoShown"

    }

    private lateinit var toolbar: Toolbar
    private var settings: SharedPreferences? = null
    private var lastProcessedIntentHash: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        BillingManager.getInstance()?.initialise(this)
        if (!(BuildConfig.FLAVOR == BUILD_FLAVOR_GOOGLEPLAY && BillingManager.getInstance()?.isProPurchased?.value == false))
            DynamicColors.applyToActivityIfAvailable(this)    // Google play gets dynamic colors only in pro!

        setContentView(R.layout.activity_main)

        //load settings
        settings = PreferenceManager.getDefaultSharedPreferences(this)

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

        val billingManager = BillingManager.getInstance()
        billingManager?.isProPurchased?.observe(this) { isPurchased ->
            if(!isPurchased)
                AdManager.getInstance()?.checkOrRequestConsentAndLoadAds(this, applicationContext)

        }

        billingManager?.isProPurchasedLoaded?.observe(this) {
            // we show the dialog only when we are sure that the purchase was loaded
            if(it)
                showProInfoDialog(billingManager.isProPurchased.value?: false)
        }
    }

    override fun onResume() {
        super.onResume()
        AdManager.getInstance()?.resumeAds()

        // coloring notification bar and navigation icon bar, see https://gitlab.com/techbeeat1/jtx/-/issues/202
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)  // bottom navigation should fill
            window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)

            val nightModeFlags: Int = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if(nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                window.decorView.windowInsetsController?.setSystemBarsAppearance(0, APPEARANCE_LIGHT_STATUS_BARS)
                window.decorView.windowInsetsController?.setSystemBarsAppearance(0, APPEARANCE_LIGHT_NAVIGATION_BARS)
            } else {
                window.decorView.windowInsetsController?.setSystemBarsAppearance(APPEARANCE_LIGHT_STATUS_BARS, APPEARANCE_LIGHT_STATUS_BARS)
                window.decorView.windowInsetsController?.setSystemBarsAppearance(APPEARANCE_LIGHT_NAVIGATION_BARS,APPEARANCE_LIGHT_NAVIGATION_BARS)
            }
        }

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
                    if(intent.type == "text/plain" || intent.type?.startsWith("image/") == true || intent.type == "application/pdf")
                        showAddContentDialog()
                }
                Intent.ACTION_VIEW -> {
                    if (intent.type == "text/calendar") {
                        val ics = intent.data ?: return
                        val icsString = this.contentResolver.openInputStream(ics)?.readBytes()?.decodeToString()
                        findNavController(R.id.nav_host_fragment).navigate(
                            NavigationDirections.actionGlobalCollectionsFragment().apply {
                                this.iCalString = icsString
                            })
                    }
                }
            }
            lastProcessedIntentHash = intent.hashCode()
        }
    }

    override fun onPause() {
        super.onPause()
        AdManager.getInstance()?.pauseAds()
    }

    private fun adaptMenuToBuildFlavor() {
        val navView: NavigationView = findViewById(R.id.nav_view)

        when (BuildConfig.FLAVOR) {
            BUILD_FLAVOR_GOOGLEPLAY -> {
                navView.menu.findItem(R.id.nav_donate).isVisible = false
                navView.menu.findItem(R.id.nav_adinfo).isVisible = false
            }     // hide the donate menu for google play
            BUILD_FLAVOR_HUAWEI -> {
                navView.menu.findItem(R.id.nav_donate).isVisible = false
                navView.menu.findItem(R.id.nav_buypro).isVisible = false
            }     // hide the donate menu for google play
            BUILD_FLAVOR_OSE -> {
                navView.menu.findItem(R.id.nav_adinfo).isVisible = false
                navView.menu.findItem(R.id.nav_buypro).isVisible = false
            }            // hide the adinfo for the OSE-edition
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

                R.id.nav_board -> {
                    if(findNavController(R.id.nav_host_fragment).currentDestination?.label != "icalListFragment")
                        findNavController(R.id.nav_host_fragment)
                            .navigate(R.id.action_global_icalListFragment)
                }

                R.id.nav_collections ->
                    findNavController(R.id.nav_host_fragment)
                        .navigate(R.id.action_global_collectionsFragment)
                
                R.id.nav_app_settings ->
                    findNavController(R.id.nav_host_fragment)
                        .navigate(R.id.action_global_settingsFragment)
            }
            true
        }
    }

    /**
     * Checks in the settings if night mode is enforced and switches to it if applicable
     */
    private fun checkThemeSetting() {
        // user interface settings
        when(settings?.getString(SettingsFragment.PREFERRED_THEME, SettingsFragment.THEME_SYSTEM)) {
            SettingsFragment.THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            SettingsFragment.THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            SettingsFragment.THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
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



    private fun showAddContentDialog() {

        val options = arrayOf(
            getString(R.string.toolbar_text_add_journal),
            getString(R.string.toolbar_text_add_note),
            getString(R.string.toolbar_text_add_task)
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

    private fun showProInfoDialog(isPurchased: Boolean) {
        // show a one time message for users who did not buy the app
        val proInfoShown = settings?.getBoolean(SETTINGS_PRO_INFO_SHOWN, false) ?: false
        val jtxProDialogMessage =
            if(!proInfoShown && isPurchased && this.packageManager.getPackageInfo(this.packageName, 0).firstInstallTime < 1654034400000L) {
                "Hello! We have removed all ad-code from jtx Board and now offer a Pro-version in addition to the free download that has restricted sync-capabilities. As you had purchased the ad-free option anyway, we automatically migrated you to the Pro-version! There is no change for you, this is just to let you know! :)"
            } else if (!proInfoShown && !isPurchased && this.packageManager.getPackageInfo(this.packageName, 0).firstInstallTime < 1654034400000L) {
                "Hello! We have removed all ad-code from jtx Board and now offer a Pro-version in addition to the free version that has restricted sync-capabilities. However, your installation will remain fully functional with all Pro features, just without ads - this is just to let you know! :)"
            } else if (!proInfoShown && !isPurchased) {
                getString(R.string.buypro_initial_dialog_message)
            } else {
                null
            }

        // show dialog only if conditions for message were fulfilled
        jtxProDialogMessage?.let {  message ->
            MaterialAlertDialogBuilder(this)
                .setTitle("jtx Board Pro")
                .setMessage(message)
                .setIcon(R.drawable.ic_adinfo)
                .setPositiveButton(R.string.ok) { _, _ -> }
                .setNeutralButton(R.string.more) { _, _ ->
                    //findNavController(R.id.nav_host_fragment)
                    //    .navigate(R.id.action_global_buyProFragment)
                }
                .show()

            settings?.edit()?.putBoolean(SETTINGS_PRO_INFO_SHOWN, true)?.apply()
        }

    }
}
