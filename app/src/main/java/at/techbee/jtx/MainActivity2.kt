package at.techbee.jtx

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
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
import at.techbee.jtx.databinding.FragmentCollectionsContainerBinding
import at.techbee.jtx.databinding.FragmentSettingsContainerBinding
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.AboutViewModel
import at.techbee.jtx.ui.CollectionsFragment
import at.techbee.jtx.ui.SettingsFragment
import at.techbee.jtx.ui.SyncViewModel
import at.techbee.jtx.ui.compose.destinations.NavigationDrawerDestination
import at.techbee.jtx.ui.compose.screens.*
import at.techbee.jtx.ui.compose.stateholder.GlobalStateHolder
import at.techbee.jtx.ui.theme.JtxBoardTheme

class MainActivity2 : FragmentActivity() {       // fragment activity instead of ComponentActivity to inflate Fragment-XMLs
//class MainActivity2 : ComponentActivity() {

    val activity = this


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
                    MainNavHost(this)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavHost(activity: Activity) {

    val navController = rememberNavController()
    val context = LocalContext.current
    val globalStateHolder = GlobalStateHolder(context)

    Scaffold {


        NavHost(
            navController = navController,
            startDestination = NavigationDrawerDestination.BOARD.name
        ) {
            composable(NavigationDrawerDestination.BOARD.name) {
                ListScreenTabContainer(navController)
            }
            composable(NavigationDrawerDestination.COLLECTIONS.name) {
                AndroidViewBinding(FragmentCollectionsContainerBinding::inflate) {
                    fragmentCollectionsContainerView.getFragment<CollectionsFragment>()
                }
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
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    JtxBoardTheme {
        MainNavHost(Activity())
    }
}