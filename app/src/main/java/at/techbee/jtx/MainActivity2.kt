package at.techbee.jtx

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.databinding.FragmentSettingsContainerBinding
import at.techbee.jtx.flavored.AdManager
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.AboutViewModel
import at.techbee.jtx.ui.CollectionsViewModel
import at.techbee.jtx.ui.SettingsFragment
import at.techbee.jtx.ui.SyncViewModel
import at.techbee.jtx.ui.compose.destinations.NavigationDrawerDestination
import at.techbee.jtx.ui.compose.screens.*
import at.techbee.jtx.ui.compose.stateholder.GlobalStateHolder
import at.techbee.jtx.ui.theme.JtxBoardTheme
import com.google.android.material.elevation.SurfaceColors

class MainActivity2 : FragmentActivity() {       // fragment activity instead of ComponentActivity to inflate Fragment-XMLs
//class MainActivity2 : ComponentActivity() {
    // or maybe AppCompatActivity() was also proposed...

    private var lastProcessedIntentHash: Int? = null
    private val globalStateHolder = GlobalStateHolder(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BillingManager.getInstance()?.initialise(this)

        setContent {
            JtxBoardTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavHost(this, globalStateHolder)
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        AdManager.getInstance()?.resumeAds()

        // coloring notification bar and navigation icon bar, see https://gitlab.com/techbeeat1/jtx/-/issues/202
        //TODO: Check if there is a better way in jetpack compose
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)  // bottom navigation should fill
            window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)

            val nightModeFlags: Int = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if(nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                window.decorView.windowInsetsController?.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
                window.decorView.windowInsetsController?.setSystemBarsAppearance(0,WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS)
            } else {
                window.decorView.windowInsetsController?.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
                window.decorView.windowInsetsController?.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS, WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS)
            }
        }

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


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavHost(activity: Activity, globalStateHolder: GlobalStateHolder) {

    val navController = rememberNavController()
    val context = LocalContext.current

    Scaffold {


        NavHost(
            navController = navController,
            startDestination = NavigationDrawerDestination.BOARD.name
        ) {
            composable(NavigationDrawerDestination.BOARD.name) {
                ListScreenTabContainer(navController)
            }
            composable(NavigationDrawerDestination.COLLECTIONS.name) {
                val collectionsViewModel: CollectionsViewModel = viewModel()

                CollectionsScreen(
                    navController = navController,
                    collectionsViewModel = collectionsViewModel,
                    globalStateHolder = globalStateHolder
                )
                /*
                AndroidViewBinding(FragmentCollectionsContainerBinding::inflate) {
                    fragmentCollectionsContainerView.getFragment<CollectionsFragment>()
                }
                 */
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
                AndroidViewBinding(FragmentSettingsContainerBinding::inflate) {
                    fragmentSettingsContainerView.getFragment<SettingsFragment>()
                }
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
        MainNavHost(Activity(), GlobalStateHolder(LocalContext.current))
    }
}