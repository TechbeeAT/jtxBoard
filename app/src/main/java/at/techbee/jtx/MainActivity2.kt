package at.techbee.jtx

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.MainActivity2.Companion.BUILD_FLAVOR_GOOGLEPLAY
import at.techbee.jtx.MainActivity2.Companion.BUILD_FLAVOR_OSE
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.flavored.AdManager
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.flavored.JtxReviewManager
import at.techbee.jtx.ui.settings.DropdownSettingOption
import at.techbee.jtx.ui.about.AboutViewModel
import at.techbee.jtx.ui.collections.CollectionsViewModel
import at.techbee.jtx.ui.detail.DetailViewModel
import at.techbee.jtx.ui.sync.SyncViewModel
import at.techbee.jtx.ui.reusable.destinations.DetailDestination
import at.techbee.jtx.ui.reusable.destinations.NavigationDrawerDestination
import at.techbee.jtx.ui.reusable.dialogs.ProInfoDialog
import at.techbee.jtx.ui.reusable.screens.*
import at.techbee.jtx.ui.GlobalStateHolder
import at.techbee.jtx.ui.collections.CollectionsScreen
import at.techbee.jtx.ui.detail.DetailsScreen
import at.techbee.jtx.ui.list.ListScreenTabContainer
import at.techbee.jtx.ui.reusable.dialogs.OSERequestDonationDialog
import at.techbee.jtx.ui.settings.SettingsScreen
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.ui.theme.JtxBoardTheme
import net.fortuna.ical4j.model.TimeZoneRegistryFactory
import java.time.ZonedDateTime


const val AUTHORITY_FILEPROVIDER = "at.techbee.jtx.fileprovider"

class MainActivity2 : ComponentActivity() {
    //class MainActivity2 : AppCompatActivity() {       // fragment activity instead of ComponentActivity to inflate Fragment-XMLs
    // or maybe FragmentActivity() was also proposed...

    private var lastProcessedIntentHash: Int? = null
    private lateinit var globalStateHolder: GlobalStateHolder
    private lateinit var settingsStateHolder: SettingsStateHolder

    companion object {
        const val CHANNEL_REMINDER_DUE = "REMINDER_DUE"

        const val BUILD_FLAVOR_OSE = "ose"
        const val BUILD_FLAVOR_GOOGLEPLAY = "gplay"
        const val BUILD_FLAVOR_HUAWEI = "huawei"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // hides the ugly action bar that was before hidden through the Theme XML
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        actionBar?.hide()

        globalStateHolder = GlobalStateHolder(this)
        settingsStateHolder = SettingsStateHolder(this)

        TimeZoneRegistryFactory.getInstance().createRegistry() // necessary for ical4j
        checkThemeSetting()
        createNotificationChannel()   // Register Notification Channel for Reminders

        BillingManager.getInstance().initialise(this)

        /* TODO
        billingManager?.isProPurchased?.observe(this) { isPurchased ->
            if(!isPurchased)
                AdManager.getInstance()?.checkOrRequestConsentAndLoadAds(this, applicationContext)
        }
         */

        setContent {
            val isProPurchased = BillingManager.getInstance().isProPurchased.observeAsState()
            JtxBoardTheme(
                darkTheme = when(settingsStateHolder.settingTheme.value) {
                    DropdownSettingOption.THEME_LIGHT -> false
                    DropdownSettingOption.THEME_DARK -> true
                    else -> isSystemInDarkTheme()
                },
                dynamicColor = isProPurchased.value ?: false
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MainNavHost(this, globalStateHolder, settingsStateHolder)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AdManager.getInstance()?.resumeAds()

        //hanlde intents, but only if it wasn't already handled
        if(intent.hashCode() != lastProcessedIntentHash) {

            intent?.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

            // handle the intents for the shortcuts
            when (intent?.action) {
                "addJournal" -> {
                    globalStateHolder.icalFromIntentModule.value = Module.JOURNAL
                    globalStateHolder.icalFromIntentString.value = ""
                }
                "addNote" -> {
                    globalStateHolder.icalFromIntentModule.value = Module.NOTE
                    globalStateHolder.icalFromIntentString.value = ""
                }
                "addTodo" -> {
                    globalStateHolder.icalFromIntentModule.value = Module.TODO
                    globalStateHolder.icalFromIntentString.value = ""
                }
                "openICalObject" -> {
                    val id = intent.getLongExtra("item2show", 0L)
                    if(id > 0L)
                        globalStateHolder.icalObject2Open.value = id
                }

                // Take data also from other sharing intents
                Intent.ACTION_SEND -> {
                    when {
                        intent.type == "text/plain" -> globalStateHolder.icalFromIntentString.value = intent.getStringExtra(Intent.EXTRA_TEXT)
                        intent.type?.startsWith("image/") == true || intent.type == "application/pdf" -> {
                            (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let { uri ->
                                Attachment.getNewAttachmentFromUri(uri, this)?.let { newAttachment ->
                                    globalStateHolder.icalFromIntentAttachment.value = newAttachment
                                }
                            }
                        }
                    }
                }

                Intent.ACTION_VIEW -> {
                    if (intent.type == "text/calendar") {
                        val ics = intent.data ?: return
                        this.contentResolver.openInputStream(ics)?.use { stream ->
                            globalStateHolder.icalString2Import.value = stream.readBytes().decodeToString()
                        }
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

    /**
     * Checks in the settings if night mode is enforced and switches to it if applicable
     */
    private fun checkThemeSetting() {
        // user interface settings
        when(settingsStateHolder.settingTheme.value) {
            DropdownSettingOption.THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            DropdownSettingOption.THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            DropdownSettingOption.THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }


    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_reminder_name)
            val descriptionText = getString(R.string.notification_channel_reminder_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_REMINDER_DUE, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun MainNavHost(
    activity: Activity,
    globalStateHolder: GlobalStateHolder,
    settingsStateHolder: SettingsStateHolder
) {
    val navController = rememberNavController()
    val isProPurchased = BillingManager.getInstance().isProPurchased.observeAsState()
    var showOSEDonationDialog by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = NavigationDrawerDestination.BOARD.name
    ) {
        composable(NavigationDrawerDestination.BOARD.name) {
            ListScreenTabContainer(
                navController = navController,
                globalStateHolder = globalStateHolder,
                lastUsedCollectionId = settingsStateHolder.lastUsedCollection.value,
                saveAndEdit = settingsStateHolder.saveAndEdit.value,
                onLastUsedCollectionIdChanged = { collectionId ->
                    settingsStateHolder.lastUsedCollection.value = collectionId
                    settingsStateHolder.lastUsedCollection = settingsStateHolder.lastUsedCollection
                },
                onSaveAndEditChanged = { saveAndEdit ->
                    settingsStateHolder.saveAndEdit.value = saveAndEdit
                    settingsStateHolder.saveAndEdit = settingsStateHolder.saveAndEdit
                }
            )
        }
        composable(
            DetailDestination.Detail.route,
            arguments = DetailDestination.Detail.args
        ) { backStackEntry ->

            val icalObjectId = backStackEntry.arguments?.getLong(DetailDestination.argICalObjectId) ?: return@composable
            val editImmediately = backStackEntry.arguments?.getBoolean(DetailDestination.argIsEditMode) ?: false

            val detailViewModel: DetailViewModel = viewModel()
            detailViewModel.load(icalObjectId)

            DetailsScreen(
                navController = navController,
                detailViewModel = detailViewModel,
                editImmediately = editImmediately,
                autosave = settingsStateHolder.settingAutosave.value,
                onRequestReview = {
                    if(BuildConfig.FLAVOR == BUILD_FLAVOR_GOOGLEPLAY)
                        JtxReviewManager(activity).showIfApplicable()
                    else if (BuildConfig.FLAVOR == BUILD_FLAVOR_OSE)
                        showOSEDonationDialog = JtxReviewManager(activity).showIfApplicable() || BuildConfig.DEBUG
                },
                onLastUsedCollectionChanged = { collectionId ->
                    settingsStateHolder.lastUsedCollection.value = collectionId
                    settingsStateHolder.lastUsedCollection = settingsStateHolder.lastUsedCollection
                }
            )
        }
        composable(NavigationDrawerDestination.COLLECTIONS.name) {
            val collectionsViewModel: CollectionsViewModel = viewModel()

            CollectionsScreen(
                navController = navController,
                collectionsViewModel = collectionsViewModel,
                globalStateHolder = globalStateHolder
            )
        }
        composable(NavigationDrawerDestination.SYNC.name) {
            val viewModel: SyncViewModel = viewModel()
            SyncScreen(
                remoteCollectionsLive = viewModel.remoteCollections,
                isSyncInProgress = globalStateHolder.isSyncInProgress,
                navController = navController)
        }
        composable(NavigationDrawerDestination.DONATE.name) { DonateScreen(navController) }
        composable(NavigationDrawerDestination.ADINFO.name) { AdInfoScreen(navController) }
        composable(NavigationDrawerDestination.ABOUT.name) {
            val viewModel: AboutViewModel = viewModel()
            AboutScreen(
                translators = viewModel.translators,
                releaseinfo = viewModel.releaseinfos,
                navController = navController
            )
        }
        composable(NavigationDrawerDestination.BUYPRO.name) {
            BuyProScreen(
                isPurchased = isProPurchased,
                priceLive = BillingManager.getInstance().proPrice,
                purchaseDateLive = BillingManager.getInstance().proPurchaseDate,
                orderIdLive = BillingManager.getInstance().proOrderId,
                launchBillingFlow = {
                    BillingManager.getInstance().launchBillingFlow(activity)
                },
                navController = navController
            )
        }
        composable(NavigationDrawerDestination.SETTINGS.name) {
            SettingsScreen(
                navController = navController,
                currentTheme = settingsStateHolder.settingTheme,
                audioFormat = settingsStateHolder.settingAudioFormat,
                autoExpandSubtasks = settingsStateHolder.settingAutoExpandSubtasks,
                autoExpandSubnotes = settingsStateHolder.settingAutoExpandSubnotes,
                autoExpandAttachments = settingsStateHolder.settingAutoExpandAttachments,
                showProgressForMainTasks = settingsStateHolder.settingShowProgressForMainTasks,
                showProgressForSubTasks = settingsStateHolder.settingShowProgressForSubTasks,
                showSubtasksInTasklist = settingsStateHolder.settingShowSubtasksInTasklist,
                showSubnotesInNoteslist = settingsStateHolder.settingShowSubnotesInNoteslist,
                showSubjournalsInJournallist = settingsStateHolder.settingShowSubjournalsInJournallist,
                autosave = settingsStateHolder.settingAutosave,
                defaultStartDate = settingsStateHolder.settingDefaultStartDate,
                defaultDueDate = settingsStateHolder.settingDefaultDueDate,
                stepForProgress = settingsStateHolder.settingStepForProgress
            )
        }
    }

    globalStateHolder.icalString2Import.value?.let {
        navController.navigate(NavigationDrawerDestination.COLLECTIONS.name)
    }

    globalStateHolder.icalObject2Open.value?.let { id ->
        globalStateHolder.icalObject2Open.value = null
        navController.navigate("details/$id?isEditMode=false")
    }

    if(!settingsStateHolder.proInfoShown.value && isProPurchased.value == false) {
        ProInfoDialog(
            onOK = {
                settingsStateHolder.proInfoShown.value = true
                settingsStateHolder.proInfoShown = settingsStateHolder.proInfoShown   // triggers saving
                        }
        )
    }

    if(showOSEDonationDialog) {
        OSERequestDonationDialog(
            onOK = {
                // next dialog in 90 days
                JtxReviewManager(activity).nextRequestOn = ZonedDateTime.now().plusDays(90L).toInstant().toEpochMilli()
                showOSEDonationDialog = false
                   },
            onMore = {
                navController.navigate(NavigationDrawerDestination.DONATE.name)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        val context = LocalContext.current
        MainNavHost(context as Activity, GlobalStateHolder(context), SettingsStateHolder(context))
    }
}