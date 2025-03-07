package at.techbee.jtx

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.locals.StoredListSettingData
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.flavored.JtxReviewManager
import at.techbee.jtx.ui.GlobalStateHolder
import at.techbee.jtx.ui.about.AboutScreen
import at.techbee.jtx.ui.about.AboutViewModel
import at.techbee.jtx.ui.buypro.BuyProScreen
import at.techbee.jtx.ui.collections.CollectionsScreen
import at.techbee.jtx.ui.collections.CollectionsViewModel
import at.techbee.jtx.ui.detail.DetailViewModel
import at.techbee.jtx.ui.detail.DetailsScreen
import at.techbee.jtx.ui.donate.DonateScreen
import at.techbee.jtx.ui.list.ListScreenTabContainer
import at.techbee.jtx.ui.list.ListSettings
import at.techbee.jtx.ui.list.ListViewModel
import at.techbee.jtx.ui.presets.PresetsScreen
import at.techbee.jtx.ui.reusable.destinations.DetailDestination
import at.techbee.jtx.ui.reusable.destinations.FilteredListDestination
import at.techbee.jtx.ui.reusable.destinations.NavigationDrawerDestination
import at.techbee.jtx.ui.reusable.dialogs.OSERequestDonationDialog
import at.techbee.jtx.ui.reusable.dialogs.ProInfoDialog
import at.techbee.jtx.ui.settings.DropdownSettingOption
import at.techbee.jtx.ui.settings.SettingsScreen
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.ui.sync.SyncScreen
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.ui.theme.montserratAlternatesFont
import at.techbee.jtx.ui.theme.notoFont
import at.techbee.jtx.ui.theme.robotoFont
import at.techbee.jtx.util.SyncUtil
import at.techbee.jtx.util.getParcelableExtraCompat
import at.techbee.jtx.widgets.ListWidget
import at.techbee.jtx.widgets.ListWidgetConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import net.fortuna.ical4j.model.TimeZoneRegistryFactory
import java.io.FileNotFoundException
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.minutes


const val AUTHORITY_FILEPROVIDER = "at.techbee.jtx.fileprovider"

enum class BuildFlavor(val flavor: String, val hasBilling: Boolean, val hasGeofence: Boolean, val hasReview: Boolean, val hasDonation: Boolean) {
    OSE("ose", false, false, false, true),
    GPLAY("gplay", true, true, true, false),
    AMAZON("amazon", true, false, false, false),
    HUAWEI("huawei", true, false, false, false),
    GENERIC("generic", false, false, false, false);

    companion object {
        fun getCurrent() = entries.find { it.flavor == BuildConfig.FLAVOR } ?: OSE
    }
}

//class MainActivity2 : ComponentActivity() {   // Using AppCompatActivity activity instead of ComponentActivity
class MainActivity2 : AppCompatActivity() {

    private var lastProcessedIntentHash: Int? = null
    private lateinit var globalStateHolder: GlobalStateHolder
    private lateinit var settingsStateHolder: SettingsStateHolder

    companion object {
        const val NOTIFICATION_CHANNEL_ALARMS = "REMINDER_DUE"   // different name for legacy handling!
        const val NOTIFICATION_CHANNEL_GEOFENCES = "NOTIFICATION_CHANNEL_GEOFENCES"

        const val INTENT_ACTION_ADD_JOURNAL = "addJournal"
        const val INTENT_ACTION_ADD_NOTE = "addNote"
        const val INTENT_ACTION_ADD_TODO = "addTodo"
        const val INTENT_ACTION_OPEN_FILTERED_LIST = "openFilteredList"
        const val INTENT_ACTION_OPEN_ICALOBJECT = "openICalObject"
        const val INTENT_EXTRA_ITEM2SHOW = "item2show"
        const val INTENT_EXTRA_FORGET_NOTIFICATION = "forgetNotification"
        const val INTENT_EXTRA_COLLECTION2PRESELECT = "collection2preselect"
        const val INTENT_EXTRA_CATEGORIES2PRESELECT = "categories2preselect"
        const val INTENT_EXTRA_LISTWIDGETCONFIG = "listWidgetConfig"
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // hides the ugly action bar that was before hidden through the Theme XML
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        supportActionBar?.hide()

        globalStateHolder = GlobalStateHolder(this)
        settingsStateHolder = SettingsStateHolder(this)

        TimeZoneRegistryFactory.getInstance().createRegistry() // necessary for ical4j
        createNotificationChannels()   // Register Notification Channel for Reminders
        BillingManager.getInstance().initialise(this)

        /* START Initialise biometric prompt */
        globalStateHolder.biometricPrompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    globalStateHolder.isAuthenticated.value = false
                    Toast.makeText(applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT)
                        .show()
                }
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    globalStateHolder.isAuthenticated.value = true
                    Toast.makeText(applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT)
                        .show()
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    globalStateHolder.isAuthenticated.value = false
                    Toast.makeText(applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show()
                }
            })
        /* END Initialise biometric prompt */

        if(settingsStateHolder.settingSyncOnStart.value) {
            lifecycleScope.launch(Dispatchers.IO) {
                val remoteCollections = ICalDatabase.getInstance(applicationContext).iCalDatabaseDao().getAllRemoteCollections()
                SyncUtil.syncAccounts(remoteCollections.map { it.getAccount() }.toSet())
            }
        }

        setContent {
            val isProPurchased = BillingManager.getInstance().isProPurchased.observeAsState(false)
            JtxBoardTheme(
                darkTheme = when (settingsStateHolder.settingTheme.value) {
                    DropdownSettingOption.THEME_LIGHT -> false
                    DropdownSettingOption.THEME_DARK -> true
                    DropdownSettingOption.THEME_TRUE_DARK -> true
                    else -> isSystemInDarkTheme()
                },
                contrastTheme = settingsStateHolder.settingTheme.value == DropdownSettingOption.THEME_CONTRAST,
                trueDarkTheme = settingsStateHolder.settingTheme.value == DropdownSettingOption.THEME_TRUE_DARK,
                dynamicColor = isProPurchased.value
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            testTagsAsResourceId = true
                        },
                    color = MaterialTheme.colorScheme.background,
                ) {
                    CompositionLocalProvider(
                        LocalTextStyle provides LocalTextStyle.current.merge(
                            TextStyle(
                                textDirection = TextDirection.Content,
                                fontFamily = when(settingsStateHolder.settingFont.value) {
                                    DropdownSettingOption.FONT_ROBOTO -> robotoFont
                                    DropdownSettingOption.FONT_NOTO -> notoFont
                                    DropdownSettingOption.FONT_MONTSERRAT_ALTERNATES -> montserratAlternatesFont
                                    else -> robotoFont
                                }
                            )
                        )
                    ) {
                        MainNavHost(this, globalStateHolder, settingsStateHolder)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        /*
        if(settingsStateHolder.settingSyncOnStart.value) {
            lifecycleScope.launch(Dispatchers.IO) {
                val remoteCollections = ICalDatabase.getInstance(applicationContext).iCalDatabaseDao().getAllRemoteCollections()
                SyncUtil.syncAccounts(remoteCollections.map { Account(it.accountName, it.accountType) }.toSet())
            }
        }
         */

        //handle intents, but only if it wasn't already handled
        if (intent.hashCode() != lastProcessedIntentHash) {
            when (intent?.action) {
                INTENT_ACTION_ADD_JOURNAL -> {
                    globalStateHolder.icalFromIntentModule.value = Module.JOURNAL
                    globalStateHolder.icalFromIntentString.value = ""
                    globalStateHolder.icalFromIntentCollection.value = intent.getStringExtra(INTENT_EXTRA_COLLECTION2PRESELECT)
                    intent.removeExtra(INTENT_EXTRA_COLLECTION2PRESELECT)
                    intent.getStringArrayListExtra(INTENT_EXTRA_CATEGORIES2PRESELECT)?.let { globalStateHolder.icalFromIntentCategories.addAll(it) }
                    intent.removeExtra(INTENT_EXTRA_CATEGORIES2PRESELECT)
                }
                INTENT_ACTION_ADD_NOTE -> {
                    globalStateHolder.icalFromIntentModule.value = Module.NOTE
                    globalStateHolder.icalFromIntentString.value = ""
                    globalStateHolder.icalFromIntentCollection.value = intent.getStringExtra(INTENT_EXTRA_COLLECTION2PRESELECT)
                    intent.removeExtra(INTENT_EXTRA_COLLECTION2PRESELECT)
                    intent.getStringArrayListExtra(INTENT_EXTRA_CATEGORIES2PRESELECT)?.let { globalStateHolder.icalFromIntentCategories.addAll(it) }
                    intent.removeExtra(INTENT_EXTRA_CATEGORIES2PRESELECT)
                }
                INTENT_ACTION_ADD_TODO -> {
                    globalStateHolder.icalFromIntentModule.value = Module.TODO
                    globalStateHolder.icalFromIntentString.value = ""
                    globalStateHolder.icalFromIntentCollection.value = intent.getStringExtra(INTENT_EXTRA_COLLECTION2PRESELECT)
                    intent.removeExtra(INTENT_EXTRA_COLLECTION2PRESELECT)
                    intent.getStringArrayListExtra(INTENT_EXTRA_CATEGORIES2PRESELECT)?.let { globalStateHolder.icalFromIntentCategories.addAll(it) }
                    intent.removeExtra(INTENT_EXTRA_CATEGORIES2PRESELECT)
                }
                INTENT_ACTION_OPEN_FILTERED_LIST -> {
                    intent.getStringExtra(INTENT_EXTRA_LISTWIDGETCONFIG)?.let {
                        globalStateHolder.filteredList2Load.value = Json.decodeFromString<ListWidgetConfig>(it)
                        intent.removeExtra(INTENT_EXTRA_LISTWIDGETCONFIG)
                    }
                }
                INTENT_ACTION_OPEN_ICALOBJECT -> {
                    val id = intent.getLongExtra(INTENT_EXTRA_ITEM2SHOW, 0L)
                    if (id > 0L) {
                        globalStateHolder.icalObject2Open.value = id

                        if(intent.getBooleanExtra(INTENT_EXTRA_FORGET_NOTIFICATION, false)) {
                            lifecycleScope.launch(Dispatchers.IO) {
                                ICalDatabase.getInstance(applicationContext).iCalDatabaseDao().setAlarmNotification(id, false)
                            }
                        }
                    }
                }
                // Take data also from other sharing intents
                Intent.ACTION_VIEW -> {
                    if (intent.type == "text/calendar") {
                        val ics = intent.data ?: return
                        try {
                            this.contentResolver.openInputStream(ics)?.use { stream ->
                                globalStateHolder.icalString2Import.value =
                                    stream.readBytes().decodeToString()
                            }
                        } catch (e: FileNotFoundException) {
                            Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()
                            //Log.e("MainActivity2", e.stackTraceToString())
                        }
                    }
                }
                Intent.ACTION_SEND -> {
                    when (intent.type) {
                        "text/plain" -> globalStateHolder.icalFromIntentString.value = intent.getStringExtra(Intent.EXTRA_TEXT)
                        "text/markdown" -> intent.getParcelableExtraCompat(Intent.EXTRA_STREAM, Uri::class)?.let { uri ->
                                this.contentResolver.openInputStream(uri)?.use { stream ->
                                    globalStateHolder.icalFromIntentString.value =
                                        stream.readBytes().decodeToString()
                                }
                            }
                        else -> intent.getParcelableExtraCompat(Intent.EXTRA_STREAM, Uri::class)?.let { uri ->
                            Attachment.getNewAttachmentFromUri(uri, this)
                                ?.let { newAttachment ->
                                    globalStateHolder.icalFromIntentAttachment.value =
                                        newAttachment
                                }
                            }
                    }
                }
            }
            intent.removeExtra(Intent.EXTRA_TEXT)
            intent.removeExtra(Intent.EXTRA_STREAM)
            setResult(RESULT_OK)
        }
        lastProcessedIntentHash = intent.hashCode()

        if(BuildFlavor.getCurrent() == BuildFlavor.HUAWEI)
            BillingManager.getInstance().initialise(this)  // only Huawei needs to call the update functions again

        // reset authentication state if timeout was set and expired or remove timeout if onResume was done within timeout
        if(globalStateHolder.isAuthenticated.value && globalStateHolder.authenticationTimeout != null) {
            if((globalStateHolder.authenticationTimeout!!) < System.currentTimeMillis())
                globalStateHolder.isAuthenticated.value = false
            globalStateHolder.authenticationTimeout = null
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            ListWidget().updateAll(applicationContext)
        }
        globalStateHolder.authenticationTimeout = System.currentTimeMillis() + (10).minutes.inWholeMilliseconds
    }

    private fun createNotificationChannels() {
        val alarmChannel = NotificationChannelCompat.Builder(NOTIFICATION_CHANNEL_ALARMS, NotificationManagerCompat.IMPORTANCE_MAX)
            .setName(getString(R.string.notification_channel_alarms_name))
            .setLightsEnabled(true)
            .build()
        val geofenceChannel = NotificationChannelCompat.Builder(NOTIFICATION_CHANNEL_GEOFENCES, NotificationManagerCompat.IMPORTANCE_MAX)
            .setName(getString(R.string.notification_channel_geofences_name))
            .setLightsEnabled(true)
            .build()
        if(BuildFlavor.getCurrent().hasGeofence)
            NotificationManagerCompat.from(this).createNotificationChannelsCompat(listOf(alarmChannel, geofenceChannel))
        else
            NotificationManagerCompat.from(this).createNotificationChannelsCompat(listOf(alarmChannel))
    }
}

@Composable
fun MainNavHost(
    activity: Activity,
    globalStateHolder: GlobalStateHolder,
    settingsStateHolder: SettingsStateHolder
) {
    val navController = rememberNavController()
    val isProPurchased = BillingManager.getInstance().isProPurchased.observeAsState(false)
    var showOSEDonationDialog by rememberSaveable { mutableStateOf(false) }

    globalStateHolder.remoteCollections = ICalDatabase.getInstance(activity).iCalDatabaseDao().getAllRemoteCollectionsLive().observeAsState(emptyList())

    NavHost(
        navController = navController,
        startDestination = if(globalStateHolder.filteredList2Load.value != null)
            FilteredListDestination.FilteredListFromWidget.route
            else NavigationDrawerDestination.BOARD.name
    ) {
        composable(NavigationDrawerDestination.BOARD.name) {
            ListScreenTabContainer(
                navController = navController,
                globalStateHolder = globalStateHolder,
                settingsStateHolder = settingsStateHolder, 
                initialModule = settingsStateHolder.lastUsedModule.value
            )
        }
        composable(
            FilteredListDestination.FilteredList.route,
            arguments = FilteredListDestination.FilteredList.args
        ) { backStackEntry ->

            val module = Module.entries.find { it.name == backStackEntry.arguments?.getString(FilteredListDestination.ARG_MODULE) } ?: return@composable
            val storedListSettingData = backStackEntry.arguments?.getString(
                FilteredListDestination.ARG_STORED_LIST_SETTING_DATA)?.let {
                    Json.decodeFromString<StoredListSettingData>(it)
                }

            ListScreenTabContainer(
                navController = navController,
                globalStateHolder = globalStateHolder,
                settingsStateHolder = settingsStateHolder,
                initialModule = module,
                storedListSettingData = storedListSettingData
            )
        }
        composable(
            FilteredListDestination.FilteredListFromWidget.route,
            arguments = emptyList()
        ) {

            val storedListSettingData = globalStateHolder.filteredList2Load.value?.let { listWidgetConfig ->
                val listSettings = ListSettings.fromListWidgetConfig(listWidgetConfig)
                StoredListSettingData.fromListSettings(listSettings)
            }

            ListScreenTabContainer(
                navController = navController,
                globalStateHolder = globalStateHolder,
                settingsStateHolder = settingsStateHolder,
                initialModule = globalStateHolder.filteredList2Load.value?.module ?: Module.NOTE,
                storedListSettingData = storedListSettingData
            )
            //globalStateHolder.filteredList2Load.value = null
        }
        composable(
            DetailDestination.Detail.route,
            arguments = DetailDestination.Detail.args
        ) { backStackEntry ->

            val icalObjectId = backStackEntry.arguments?.getLong(DetailDestination.argICalObjectId) ?: return@composable
            val icalObjectIdList = backStackEntry.arguments?.getString(DetailDestination.argICalObjectIdList)?.let { Json.decodeFromString<List<Long>>(it)} ?: listOf(icalObjectId)
            val editImmediately = backStackEntry.arguments?.getBoolean(DetailDestination.argIsEditMode) == true
            val returnToLauncher = backStackEntry.arguments?.getBoolean(DetailDestination.argReturnToLauncher) == true

            /*
            backStackEntry.savedStateHandle[DetailDestination.argICalObjectId] = icalObjectId
            backStackEntry.savedStateHandle[DetailDestination.argIsEditMode] = editImmediately
             */
            val detailViewModel: DetailViewModel = viewModel()
            detailViewModel.load(icalObjectId, globalStateHolder.isAuthenticated.value)
            globalStateHolder.icalObject2Open.value = null  // reset (if it was set)

            DetailsScreen(
                navController = navController,
                detailViewModel = detailViewModel,
                editImmediately = editImmediately,
                returnToLauncher = returnToLauncher,
                icalObjectIdList = icalObjectIdList,
                onRequestReview = {
                    if (BuildFlavor.getCurrent().hasReview)
                        JtxReviewManager(activity).showIfApplicable()
                    if (BuildFlavor.getCurrent().hasDonation)
                        showOSEDonationDialog = JtxReviewManager(activity).showIfApplicable()
                },
                onLastUsedCollectionChanged = { module, collectionId ->
                    val prefs: SharedPreferences = when (module) {
                        Module.JOURNAL -> activity.getSharedPreferences(
                            ListViewModel.PREFS_LIST_JOURNALS,
                            Context.MODE_PRIVATE
                        )
                        Module.NOTE -> activity.getSharedPreferences(
                            ListViewModel.PREFS_LIST_NOTES,
                            Context.MODE_PRIVATE
                        )
                        Module.TODO -> activity.getSharedPreferences(
                            ListViewModel.PREFS_LIST_TODOS,
                            Context.MODE_PRIVATE
                        )
                    }
                    ListSettings.fromPrefs(prefs).saveLastUsedCollectionId(prefs, collectionId)
                }
            )
        }
        composable(NavigationDrawerDestination.PRESETS.name) {
            PresetsScreen(
                navController = navController
            )
        }
        composable(NavigationDrawerDestination.COLLECTIONS.name) {
            val collectionsViewModel: CollectionsViewModel = viewModel()

            CollectionsScreen(
                navController = navController,
                collectionsViewModel = collectionsViewModel,
                globalStateHolder = globalStateHolder,
                settingsStateHolder = settingsStateHolder
            )
        }
        composable(NavigationDrawerDestination.SYNC.name) {
            SyncScreen(
                isSyncInProgress = globalStateHolder.isSyncInProgress,
                navController = navController
            )
        }
        composable(NavigationDrawerDestination.DONATE.name) { DonateScreen(navController) }
        composable(NavigationDrawerDestination.ABOUT.name) {
            val viewModel: AboutViewModel = viewModel()
            AboutScreen(
                translators = viewModel.translatorsCrowdin,
                releaseinfo = viewModel.releaseinfos,
                contributors = viewModel.contributors,
                libraries = viewModel.libraries,
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
                settingsStateHolder = settingsStateHolder,
                globalStateHolder = globalStateHolder
            )
        }
    }

    globalStateHolder.icalString2Import.value?.let {
        navController.navigate(NavigationDrawerDestination.COLLECTIONS.name)
    }

    globalStateHolder.icalObject2Open.value?.let { id ->
        navController.navigate(DetailDestination.Detail.getRoute(iCalObjectId = id, icalObjectIdList = emptyList(), isEditMode = false, returnToLauncher = true))
    }

    if (!settingsStateHolder.proInfoShown.value && !isProPurchased.value) {
        ProInfoDialog(
            onOK = {
                settingsStateHolder.proInfoShown.value = true
                settingsStateHolder.proInfoShown =
                    settingsStateHolder.proInfoShown   // triggers saving
            }
        )
    }

    if (showOSEDonationDialog) {
        OSERequestDonationDialog(
            onOK = {
                // next dialog in 90 days
                JtxReviewManager(activity).nextRequestOn =
                    ZonedDateTime.now().plusDays(90L).toInstant().toEpochMilli()
                showOSEDonationDialog = false
            },
            onMore = {
                navController.navigate(NavigationDrawerDestination.DONATE.name)
            }
        )
    }

    /*
    if (settingsStateHolder.showJtx20releaseinfo.value) {
        Jtx20ReleaseInfoDialog(
            onOK = {
                settingsStateHolder.showJtx20releaseinfo.value = false
                settingsStateHolder.showJtx20releaseinfo = settingsStateHolder.showJtx20releaseinfo
            }
        )
    }

    if (settingsStateHolder.showV20009releaseInfo.value) {
        Jtx20009ReleaseInfoDialog(
            onOK = {
                settingsStateHolder.showV20009releaseInfo.value = false
                settingsStateHolder.showV20009releaseInfo = settingsStateHolder.showV20009releaseInfo
            }
        )
    }
     */
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        val context = LocalContext.current
        MainNavHost(context as Activity, GlobalStateHolder(context), SettingsStateHolder(context))
    }
}