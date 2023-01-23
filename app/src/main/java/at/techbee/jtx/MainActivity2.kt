package at.techbee.jtx

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.MainActivity2.Companion.BUILD_FLAVOR_GOOGLEPLAY
import at.techbee.jtx.MainActivity2.Companion.BUILD_FLAVOR_OSE
import at.techbee.jtx.database.Module
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
import at.techbee.jtx.ui.reusable.destinations.DetailDestination
import at.techbee.jtx.ui.reusable.destinations.NavigationDrawerDestination
import at.techbee.jtx.ui.reusable.dialogs.OSERequestDonationDialog
import at.techbee.jtx.ui.reusable.dialogs.ProInfoDialog
import at.techbee.jtx.ui.settings.DropdownSettingOption
import at.techbee.jtx.ui.settings.SettingsScreen
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.ui.sync.SyncScreen
import at.techbee.jtx.ui.sync.SyncViewModel
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.util.getParcelableExtraCompat
import at.techbee.jtx.widgets.ListWidgetReceiver
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.fortuna.ical4j.model.TimeZoneRegistryFactory
import java.time.ZonedDateTime


const val AUTHORITY_FILEPROVIDER = "at.techbee.jtx.fileprovider"

//class MainActivity2 : ComponentActivity() {   // Using AppCompatActivity activity instead of ComponentActivity
class MainActivity2 : AppCompatActivity() {

    private var lastProcessedIntentHash: Int? = null
    private lateinit var globalStateHolder: GlobalStateHolder
    private lateinit var settingsStateHolder: SettingsStateHolder

    companion object {
        const val CHANNEL_REMINDER_DUE = "REMINDER_DUE"

        const val BUILD_FLAVOR_OSE = "ose"
        const val BUILD_FLAVOR_GOOGLEPLAY = "gplay"
        const val BUILD_FLAVOR_AMAZON = "amazon"
        const val BUILD_FLAVOR_HUAWEI = "huawei"
        const val BUILD_FLAVOR_GENERIC = "generic"

        const val INTENT_ACTION_ADD_JOURNAL = "addJournal"
        const val INTENT_ACTION_ADD_NOTE = "addNote"
        const val INTENT_ACTION_ADD_TODO = "addTodo"
        const val INTENT_ACTION_OPEN_ICALOBJECT = "openICalObject"
        const val INTENT_EXTRA_ITEM2SHOW = "item2show"
    }

    override fun onNewIntent(intent: Intent?) {
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
        createNotificationChannel()   // Register Notification Channel for Reminders
        BillingManager.getInstance().initialise(this)

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
                    CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.merge(TextStyle(textDirection = TextDirection.Content))) {
                        MainNavHost(this, globalStateHolder, settingsStateHolder)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        //handle intents, but only if it wasn't already handled
        if (intent.hashCode() != lastProcessedIntentHash) {
            //intent?.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

            // handle the intents for the shortcuts
            when (intent?.action) {
                INTENT_ACTION_ADD_JOURNAL -> {
                    globalStateHolder.icalFromIntentModule.value = Module.JOURNAL
                    globalStateHolder.icalFromIntentString.value = ""
                }
                INTENT_ACTION_ADD_NOTE -> {
                    globalStateHolder.icalFromIntentModule.value = Module.NOTE
                    globalStateHolder.icalFromIntentString.value = ""
                }
                INTENT_ACTION_ADD_TODO -> {
                    globalStateHolder.icalFromIntentModule.value = Module.TODO
                    globalStateHolder.icalFromIntentString.value = ""
                }
                INTENT_ACTION_OPEN_ICALOBJECT -> {
                    val id = intent.getLongExtra(INTENT_EXTRA_ITEM2SHOW, 0L)
                    if (id > 0L)
                        globalStateHolder.icalObject2Open.value = id
                }

                // Take data also from other sharing intents
                Intent.ACTION_SEND -> {
                    when {
                        intent.type == "text/plain" -> globalStateHolder.icalFromIntentString.value =
                            intent.getStringExtra(Intent.EXTRA_TEXT)
                        intent.type?.startsWith("image/") == true || intent.type == "application/pdf" -> {
                            intent.getParcelableExtraCompat(Intent.EXTRA_STREAM, Uri::class)
                                ?.let { uri ->
                                    Attachment.getNewAttachmentFromUri(uri, this)
                                        ?.let { newAttachment ->
                                            globalStateHolder.icalFromIntentAttachment.value =
                                                newAttachment
                                        }
                                }
                        }
                        intent.type == "text/markdown" -> {
                            intent.getParcelableExtraCompat(Intent.EXTRA_STREAM, Uri::class)
                                ?.let { uri ->
                                    this.contentResolver.openInputStream(uri)?.use { stream ->
                                        globalStateHolder.icalFromIntentString.value =
                                            stream.readBytes().decodeToString()
                                    }
                                }
                        }
                    }
                }

                Intent.ACTION_VIEW -> {
                    if (intent.type == "text/calendar") {
                        val ics = intent.data ?: return
                        this.contentResolver.openInputStream(ics)?.use { stream ->
                            globalStateHolder.icalString2Import.value =
                                stream.readBytes().decodeToString()
                        }
                    }
                }
            }
            setResult(Activity.RESULT_OK)
        }
        lastProcessedIntentHash = intent.hashCode()

        if(BuildConfig.FLAVOR == BUILD_FLAVOR_HUAWEI)
            BillingManager.getInstance().initialise(this)  // only Huawei needs to call the update functions again
    }

    override fun onPause() {
        super.onPause()
        ListWidgetReceiver.setOneTimeWork(this)
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
    val isProPurchased = BillingManager.getInstance().isProPurchased.observeAsState(false)
    var showOSEDonationDialog by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = NavigationDrawerDestination.BOARD.name
    ) {
        composable(NavigationDrawerDestination.BOARD.name) {
            ListScreenTabContainer(
                navController = navController,
                globalStateHolder = globalStateHolder,
                settingsStateHolder = settingsStateHolder
            )
        }
        composable(
            DetailDestination.Detail.route,
            arguments = DetailDestination.Detail.args
        ) { backStackEntry ->

            val icalObjectId = backStackEntry.arguments?.getLong(DetailDestination.argICalObjectId) ?: return@composable
            val icalObjectIdList = backStackEntry.arguments?.getString(DetailDestination.argICalObjectIdList)?.let { Json.decodeFromString<List<Long>>(it)} ?: listOf(icalObjectId)
            val editImmediately = backStackEntry.arguments?.getBoolean(DetailDestination.argIsEditMode) ?: false
            val returnToLauncher = backStackEntry.arguments?.getBoolean(DetailDestination.argReturnToLauncher) ?: false

            /*
            backStackEntry.savedStateHandle[DetailDestination.argICalObjectId] = icalObjectId
            backStackEntry.savedStateHandle[DetailDestination.argIsEditMode] = editImmediately
             */

            val detailViewModel: DetailViewModel = viewModel()
            detailViewModel.load(icalObjectId)

            DetailsScreen(
                navController = navController,
                detailViewModel = detailViewModel,
                editImmediately = editImmediately,
                returnToLauncher = returnToLauncher,
                icalObjectIdList = icalObjectIdList,
                onRequestReview = {
                    if (BuildConfig.FLAVOR == BUILD_FLAVOR_GOOGLEPLAY)
                        JtxReviewManager(activity).showIfApplicable()
                    else if (BuildConfig.FLAVOR == BUILD_FLAVOR_OSE)
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
                navController = navController
            )
        }
        composable(NavigationDrawerDestination.DONATE.name) { DonateScreen(navController) }
        composable(NavigationDrawerDestination.ABOUT.name) {
            val viewModel: AboutViewModel = viewModel()
            AboutScreen(
                translatorsPoeditor = viewModel.translatorsPoeditor,
                translatorsCrowdin = viewModel.translatorsCrowdin,
                releaseinfo = viewModel.releaseinfos,
                libraries = viewModel.libraries,
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
                settingsStateHolder = settingsStateHolder
            )
        }
    }

    globalStateHolder.icalString2Import.value?.let {
        navController.navigate(NavigationDrawerDestination.COLLECTIONS.name)
    }

    globalStateHolder.icalObject2Open.value?.let { id ->
        globalStateHolder.icalObject2Open.value = null
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