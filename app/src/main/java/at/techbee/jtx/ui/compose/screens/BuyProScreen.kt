/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.ui.compose.cards.BuyProCard
import at.techbee.jtx.ui.compose.cards.BuyProCardPurchased
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.ui.theme.Typography


@Composable
fun BuyProScreen(
    isPurchasedLive: LiveData<Boolean>,
    priceLive: LiveData<String>,
    purchaseDateLive: LiveData<String>,
    orderIdLive: LiveData<String>,
    launchBillingFlow: () -> Unit,
    modifier: Modifier = Modifier
) {

    val scrollState = rememberScrollState()
    val isPurchased by isPurchasedLive.observeAsState(false)

    Column(
        modifier = modifier
            .fillMaxSize()
            .scrollable(scrollState, orientation = Orientation.Vertical)
            .padding(8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Image(
            painter = painterResource(id = R.drawable.bg_adfree),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 32.dp, bottom = 32.dp) )
        Text(
            text = stringResource(id = R.string.buypro_text),
            modifier = Modifier.padding(top = 16.dp),
            style = Typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Crossfade(targetState = isPurchased) {

            Column(modifier = Modifier.padding(top = 16.dp)) {
            if (!it) {
                BuyProCard(
                    priceLive = priceLive,
                    Modifier.clickable {
                        launchBillingFlow()
                    }
                )
            } else {
                BuyProCardPurchased(
                    purchaseDateLive = purchaseDateLive,
                    orderIdLive = orderIdLive
                )
                Text(
                    text = stringResource(id = R.string.buypro_success_thankyou),
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                    style = Typography.displaySmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Image(
                    painter = painterResource(id = R.drawable.bg_thankyou),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(top = 32.dp, bottom = 32.dp)
                )
            }
        }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun BuyProScreen_Preview() {
    JtxBoardTheme {
        BuyProScreen(
            isPurchasedLive = MutableLiveData(false),
            priceLive = MutableLiveData("€ 3,29"),
            orderIdLive = MutableLiveData("93287z4"),
            purchaseDateLive = MutableLiveData("Thursday, 1. Jannuary 2021"),
            launchBillingFlow = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BuyProScreen_Preview_purchased() {
    JtxBoardTheme {
        BuyProScreen(
            isPurchasedLive = MutableLiveData(true),
            priceLive = MutableLiveData("€ 3,29"),
            orderIdLive = MutableLiveData("93287z4"),
            purchaseDateLive = MutableLiveData("Thursday, 1. Jannuary 2021"),
            launchBillingFlow = { }
        )
    }
}