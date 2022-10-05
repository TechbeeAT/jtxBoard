/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.about

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.ui.reusable.cards.ReleaseInfoCard
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.ui.theme.Typography


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AboutReleaseinfo(
    releaseinfoLive: MutableLiveData<MutableSet<Pair<String, String>>>,
    modifier: Modifier = Modifier
) {
    val list by releaseinfoLive.observeAsState(emptyList())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {


        item {
            Text(
                stringResource(id = R.string.about_tabitem_releasenotes),
                style = Typography.titleLarge,
            )
        }


        items(
            items = list.toList(),
            key = { item -> item.first }
        ) { releaseinfo ->
            ReleaseInfoCard(
                releaseName = releaseinfo.first,
                releaseText = releaseinfo.second,
                modifier = Modifier.padding(top = 8.dp).animateItemPlacement()
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AboutReleaseinfo_Preview() {
    MaterialTheme {
        AboutReleaseinfo(
            MutableLiveData(
                mutableSetOf(
                    Pair("v1.2.0", "- jtx Board now comes with a refactored list view with a more dynamic handling of subtasks, sub notes and attachments!\n- The new grid view option gives a more compact overview of journals, notes and tasks!\n- jtx Board is now also available in Spanish and Chinese!"),
                    Pair("v1.2.1", "- jtx Board now comes with a refactored list view with a more dynamic handling of subtasks, sub notes and attachments!\n- The new grid view option gives a more compact overview of journals, notes and tasks!\n- jtx Board is now also available in Spanish and Chinese!"),
                    Pair("v1.2.2", "- jtx Board now comes with a refactored list view with a more dynamic handling of subtasks, sub notes and attachments!\n- The new grid view option gives a more compact overview of journals, notes and tasks!\n- jtx Board is now also available in Spanish and Chinese!")
                )
            )
        )
    }
}
