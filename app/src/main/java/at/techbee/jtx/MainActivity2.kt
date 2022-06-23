package at.techbee.jtx

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.AboutViewModel
import at.techbee.jtx.ui.SyncViewModel
import at.techbee.jtx.ui.compose.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.compose.appbars.JtxTopAppBar
import at.techbee.jtx.ui.compose.destinations.NavigationDrawerDestination
import at.techbee.jtx.ui.compose.screens.*
import at.techbee.jtx.ui.theme.JtxBoardTheme

class MainActivity2 : ComponentActivity() {

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
                    MainScaffold(this)

                    //Greeting("Android")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(activity: Activity) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val navController = rememberNavController()


    Scaffold(
        topBar = { JtxTopAppBar(
            drawerState = drawerState
        ) },
        content = {
            Column {
                Spacer(modifier = Modifier.padding(top = 64.dp))
                JtxNavigationDrawer(drawerState,
                mainContent = {
                    NavHost(navController = navController, startDestination = NavigationDrawerDestination.BOARD.name) {
                        composable(NavigationDrawerDestination.BOARD.name) {
                            ListScreenTabContainer()
                        }
                        composable(NavigationDrawerDestination.SYNC.name) {
                            val viewModel: SyncViewModel = viewModel()
                            SyncScreen(
                                isDAVx5availableLive = viewModel.isDavx5Available,
                                remoteCollectionsLive = viewModel.remoteCollections,
                                isSyncInProgress = viewModel.isSyncInProgress,
                                goToCollections = { navController.navigate(NavigationDrawerDestination.COLLECTIONS.name) })
                        }
                        composable(NavigationDrawerDestination.DONATE.name) { DonateScreen() }
                        composable(NavigationDrawerDestination.ADINFO.name) { AdInfoScreen() }
                        composable(NavigationDrawerDestination.ABOUT.name) {
                            val viewModel: AboutViewModel = viewModel()
                            AboutScreen(
                                translators = viewModel.translators,
                                releaseinfo = viewModel.releaseinfos
                            ) }
                        composable(NavigationDrawerDestination.BUYPRO.name) {
                            BuyProScreen(
                                isPurchasedLive = BillingManager.getInstance()?.isProPurchased ?: MutableLiveData(true),
                                priceLive = BillingManager.getInstance()?.proPrice ?: MutableLiveData(""),
                                purchaseDateLive = BillingManager.getInstance()?.proPurchaseDate ?: MutableLiveData("-"),
                                orderIdLive = BillingManager.getInstance()?.proOrderId ?: MutableLiveData("-"),
                                launchBillingFlow = { BillingManager.getInstance()?.launchBillingFlow(activity)
                                }
                            )
                        }
                    }
                },
                navController = navController
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    JtxBoardTheme {
        MainScaffold(Activity())
    }
}