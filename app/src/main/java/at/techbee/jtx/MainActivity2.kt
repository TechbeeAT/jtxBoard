package at.techbee.jtx

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.flavored.AdManager
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.settings.DropdownSettingOption
import at.techbee.jtx.ui.AboutViewModel
import at.techbee.jtx.ui.CollectionsViewModel
import at.techbee.jtx.ui.SyncViewModel
import at.techbee.jtx.ui.compose.destinations.NavigationDrawerDestination
import at.techbee.jtx.ui.compose.screens.*
import at.techbee.jtx.ui.compose.stateholder.GlobalStateHolder
import at.techbee.jtx.ui.compose.stateholder.SettingsStateHolder
import at.techbee.jtx.ui.theme.JtxBoardTheme
import net.fortuna.ical4j.model.TimeZoneRegistryFactory

class MainActivity2 : AppCompatActivity() {       // fragment activity instead of ComponentActivity to inflate Fragment-XMLs
//class MainActivity2 : ComponentActivity() {
    // or maybe FragmentActivity() was also proposed...

    private var lastProcessedIntentHash: Int? = null
    private lateinit var globalStateHolder: GlobalStateHolder
    private lateinit var settingsStateHolder: SettingsStateHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        globalStateHolder = GlobalStateHolder(this)
        settingsStateHolder = SettingsStateHolder(this)

        TimeZoneRegistryFactory.getInstance().createRegistry() // necessary for ical4j
        checkThemeSetting()
        BillingManager.getInstance()?.initialise(this)
        /* TODO
        billingManager?.isProPurchased?.observe(this) { isPurchased ->
            if(!isPurchased)
                AdManager.getInstance()?.checkOrRequestConsentAndLoadAds(this, applicationContext)
        }
         */


        setContent {
            JtxBoardTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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
                /*
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
                 */
                Intent.ACTION_VIEW -> {
                    if (intent.type == "text/calendar") {
                        val ics = intent.data ?: return
                        globalStateHolder.icalString2Import.value = this.contentResolver.openInputStream(ics)?.readBytes()?.decodeToString()
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavHost(
    activity: Activity,
    globalStateHolder: GlobalStateHolder,
    settingsStateHolder: SettingsStateHolder
) {

    val navController = rememberNavController()

    Scaffold { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = NavigationDrawerDestination.BOARD.name
        ) {
            composable(NavigationDrawerDestination.BOARD.name) {
                ListScreenTabContainer(
                    navController = navController,
                    globalStateHolder = globalStateHolder
                )
            }
            composable(NavigationDrawerDestination.DETAILS.name) {
                DetailsScreen(
                    navController = navController
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
                    isPurchasedLive = BillingManager.getInstance()?.isProPurchased
                        ?: MutableLiveData(true),
                    priceLive = BillingManager.getInstance()?.proPrice ?: MutableLiveData(""),
                    purchaseDateLive = BillingManager.getInstance()?.proPurchaseDate
                        ?: MutableLiveData("-"),
                    orderIdLive = BillingManager.getInstance()?.proOrderId ?: MutableLiveData("-"),
                    launchBillingFlow = {
                        BillingManager.getInstance()?.launchBillingFlow(activity)
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
                    defaultStartDate = settingsStateHolder.settingDefaultStartDate,
                    defaultDueDate = settingsStateHolder.settingDefaultDueDate,
                    stepForProgress = settingsStateHolder.settingStepForProgress
                )
            }
        }

        if(globalStateHolder.icalString2Import.value != null)
            navController.navigate(NavigationDrawerDestination.COLLECTIONS.name)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    JtxBoardTheme {
        val context = LocalContext.current
        MainNavHost(context as Activity, GlobalStateHolder(context), SettingsStateHolder(context))
    }
}