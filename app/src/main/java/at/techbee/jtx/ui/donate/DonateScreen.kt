/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.donate

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.R
import at.techbee.jtx.ui.reusable.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.reusable.appbars.JtxTopAppBar
import at.techbee.jtx.ui.theme.Typography


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonateScreen(navController: NavHostController) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    Scaffold(
        topBar = {
            JtxTopAppBar(
                drawerState = drawerState,
                title = stringResource(id = R.string.navigation_drawer_donate)
            )
        },
        content = { paddingValues ->
            JtxNavigationDrawer(
                drawerState = drawerState,
                mainContent = {
                    DonateScreenContent()
                },
                navController = navController,
                paddingValues = paddingValues
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DonateScreen_Preview() {
    MaterialTheme {
        DonateScreen(rememberNavController())
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DonateScreenContent(modifier: Modifier = Modifier) {

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Image(
            painter = painterResource(id = R.drawable.bg_donate),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(top = 32.dp, bottom = 32.dp)
        )
        Text(
            text = stringResource(id = R.string.donate_header_text),
            modifier = Modifier.padding(top = 16.dp),
            style = Typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(id = R.string.donate_thank_you),
            modifier = Modifier.padding(top = 16.dp),
            style = Typography.displaySmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(id = R.string.donate_donate_with),
            modifier = Modifier.padding(top = 16.dp),
            style = Typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Image(
            painter = painterResource(id = R.drawable.paypal),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp, 100.dp)
                .combinedClickable(
                    enabled = true,
                    onClickLabel = "PayPal",
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(context.getString(R.string.link_paypal))
                            )
                        )
                    }
                )
        )

        Text(
            text = stringResource(id = R.string.donate_other_donation_methods),
            modifier = Modifier.padding(top = 16.dp),
            style = Typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        TextButton(
            content = {
                Text(
                    text = stringResource(id = R.string.link_jtx_donate),
                    style = Typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            modifier = Modifier.padding(top = 8.dp),
            onClick = {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(context.getString(R.string.link_jtx_donate))
                    )
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DonateScreenContent_Preview() {
    MaterialTheme {
        DonateScreenContent()
    }
}