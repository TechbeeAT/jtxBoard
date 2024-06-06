/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.buypro

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.R
import at.techbee.jtx.ui.reusable.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.reusable.appbars.JtxTopAppBar
import at.techbee.jtx.ui.reusable.cards.BuyProCard
import at.techbee.jtx.ui.reusable.cards.BuyProCardPurchased
import at.techbee.jtx.ui.theme.Typography
import at.techbee.jtx.ui.theme.pacifico


@Composable
fun BuyProScreen(
    isPurchased: State<Boolean?>,
    priceLive: LiveData<String?>,
    purchaseDateLive: LiveData<String?>,
    orderIdLive: LiveData<String?>,
    launchBillingFlow: () -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    Scaffold(
        topBar = {
            JtxTopAppBar(
                drawerState = drawerState,
                title = stringResource(id = R.string.navigation_drawer_buypro)
            )
        },
        content = { paddingValues ->
            JtxNavigationDrawer(
                drawerState = drawerState,
                mainContent = {
                    BuyProScreenContent(
                        isPurchased = isPurchased,
                        priceLive = priceLive,
                        purchaseDateLive = purchaseDateLive,
                        orderIdLive = orderIdLive,
                        launchBillingFlow = launchBillingFlow,
                        modifier = modifier
                    )
                },
                navController = navController,
                paddingValues = paddingValues
            )

        }
    )
}

@Composable
fun BuyProScreenContent(
    isPurchased: State<Boolean?>,
    priceLive: LiveData<String?>,
    purchaseDateLive: LiveData<String?>,
    orderIdLive: LiveData<String?>,
    launchBillingFlow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.ic_jtx_logo),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 8.dp, bottom = 8.dp)
        )
        Text(
            text = stringResource(id = R.string.buypro_text),
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
            style = Typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Crossfade(targetState = isPurchased.value, label = "isPurchased") {

            Column(modifier = Modifier.padding(top = 16.dp)) {
                if (it == false) {
                    BuyProCard(
                        priceLive = priceLive,
                        onClick = { launchBillingFlow() }
                    )
                } else if(it == true) {
                    BuyProCardPurchased(
                        purchaseDateLive = purchaseDateLive,
                        orderIdLive = orderIdLive
                    )
                }
            }
        }

        Text(
            text = stringResource(id = R.string.buypro_success_thankyou),
            modifier = Modifier
                .padding(top = 48.dp)
                .fillMaxWidth(),
            style = Typography.displaySmall.copy(fontFamily = pacifico),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BuyProScreen_Preview() {
    MaterialTheme {
        BuyProScreen(
            isPurchased = remember { mutableStateOf(false) },
            priceLive = MutableLiveData("€ 3,29"),
            orderIdLive = MutableLiveData("93287z4"),
            purchaseDateLive = MutableLiveData("Thursday, 1. Jannuary 2021"),
            launchBillingFlow = { },
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BuyProScreen_Preview_purchased() {
    MaterialTheme {
        BuyProScreen(
            isPurchased = remember { mutableStateOf(true) },
            priceLive = MutableLiveData("€ 3,29"),
            orderIdLive = MutableLiveData("93287z4"),
            purchaseDateLive = MutableLiveData("Thursday, 1. Jannuary 2021"),
            launchBillingFlow = { },
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BuyProScreen_Preview_null() {
    MaterialTheme {
        BuyProScreen(
            isPurchased = remember { mutableStateOf(null) },
            priceLive = MutableLiveData("€ 3,29"),
            orderIdLive = MutableLiveData("93287z4"),
            purchaseDateLive = MutableLiveData("Thursday, 1. Jannuary 2021"),
            launchBillingFlow = { },
            navController = rememberNavController()
        )
    }
}