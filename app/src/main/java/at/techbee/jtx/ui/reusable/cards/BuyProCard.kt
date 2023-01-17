/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.ui.theme.Typography


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyProCard(
    priceLive: LiveData<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val price by priceLive.observeAsState("")

    ElevatedCard(
        onClick = onClick,
        modifier = modifier
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painterResource(id = R.drawable.ic_jtx_logo),
                null,
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 16.dp)
            )

            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(id = R.string.buypro_purchase_header),
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        price,
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                }

                Text(
                    stringResource(id = R.string.buypro_purchase_description),
                    style = Typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BuyProCard_Preview() {
    MaterialTheme {
        BuyProCard(
            priceLive = MutableLiveData("€ 3,29"),
            onClick = { },
            )
    }
}


@Composable
fun BuyProCardPurchased(
    purchaseDateLive: LiveData<String>,
    orderIdLive: LiveData<String>,
    modifier: Modifier = Modifier
) {

    val purchaseDate by purchaseDateLive.observeAsState("-")
    val orderId by orderIdLive.observeAsState("-")

    ElevatedCard(
        modifier = modifier
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painterResource(id = R.drawable.ic_jtx_logo),
                null,
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 16.dp)
            )

            SelectionContainer {
                Column {
                    Text(
                        stringResource(id = R.string.buypro_success_header),
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(id = R.string.buypro_success_description),
                        style = Typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Text(
                        stringResource(id = R.string.buypro_order_id, orderId),
                        style = Typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        stringResource(id = R.string.buypro_purchase_date, purchaseDate),
                        style = Typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BuyProCardPurchased_Preview() {
    MaterialTheme {
        BuyProCardPurchased(
            orderIdLive = MutableLiveData("93287z4"),
            purchaseDateLive = MutableLiveData("Thursday, 1. Jannuary 2021")
        )
    }
}
