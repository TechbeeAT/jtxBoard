/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
fun AdInfoScreen(navController: NavHostController, modifier: Modifier = Modifier) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    Scaffold(
        topBar = {
            JtxTopAppBar(
                drawerState = drawerState,
                title = stringResource(id = R.string.navigation_drawer_adinfo)
            )
        },
        content = { paddingValues ->
            JtxNavigationDrawer(
                drawerState = drawerState,
                mainContent = {

                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {


                        Image(
                            painter = painterResource(id = R.drawable.bg_adfree),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .padding(top = 32.dp, bottom = 32.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.adinfo_text),
                            modifier = Modifier.padding(top = 16.dp),
                            style = Typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(id = R.string.adinfo_huwei_only_non_personalized_text),
                            modifier = Modifier.padding(top = 16.dp),
                            style = Typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navController = navController,
                paddingValues = paddingValues
            )


        },

        )
}

@Preview(showBackground = true)
@Composable
fun AdInfoScreen_Preview() {
    MaterialTheme {
        AdInfoScreen(rememberNavController())
    }
}