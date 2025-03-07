/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.collections

import android.accounts.Account
import android.accounts.AccountManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.ui.theme.jtxCardCornerShape


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CollectionsScreenContent(
    collections: List<CollectionsView>,
    isProcessing: LiveData<Boolean>,
    settingAccessibilityMode: Boolean,
    onCollectionChanged: (ICalCollection) -> Unit,
    onCollectionDeleted: (ICalCollection) -> Unit,
    onEntriesMoved: (old: ICalCollection, new: ICalCollection) -> Unit,
    onImportFromICS: (CollectionsView) -> Unit,
    onImportFromTxt: (CollectionsView) -> Unit,
    onExportAsICS: (CollectionsView) -> Unit,
    onCollectionClicked: (CollectionsView) -> Unit,
    onDeleteAccount: (Account) -> Unit
) {

    val grouped = collections.groupBy { it.toICalCollection().getAccount() }
    val showProgressIndicator by isProcessing.observeAsState(false)

    val foundAccounts = mutableSetOf<Account>()
    collections.map { it.accountType }.distinct().forEach { accountType ->
        val account = AccountManager.get(LocalContext.current).getAccountsByType(accountType)
        foundAccounts.addAll(account)
    }


    Box {
        AnimatedVisibility(
            visible = showProgressIndicator,
            content = {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        )

        LazyColumn(
            modifier = Modifier.padding(8.dp)
        ) {

            item {
                Text(
                    stringResource(id = R.string.collections_info),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            grouped.forEach { (account, collectionsInAccount) ->
                //stickyHeader {
                item {
                    CollectionsAccountHeader(
                        account = account,
                        isFoundInAccountmanager = foundAccounts.contains(account) || account.type == ICalCollection.LOCAL_ACCOUNT_TYPE,
                        onDeleteAccount = onDeleteAccount,
                        modifier = Modifier.padding(
                            top = 16.dp,
                            start = 8.dp,
                            end = 16.dp,
                            bottom = 8.dp
                        )
                    )
                }

                items(
                    items = collectionsInAccount,
                    key = { collection -> collection.collectionId }
                ) { collection ->

                    CollectionCard(
                        collection = collection,
                        allCollections = collections,
                        settingAccessibilityMode = settingAccessibilityMode,
                        onCollectionChanged = onCollectionChanged,
                        onCollectionDeleted = onCollectionDeleted,
                        onEntriesMoved = onEntriesMoved,
                        onImportFromICS = onImportFromICS,
                        onImportFromTxt = onImportFromTxt,
                        onExportAsICS = onExportAsICS,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(jtxCardCornerShape)
                            .combinedClickable(
                                onClick = { onCollectionClicked(collection) })
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CollectionsScreenContent_Preview() {
    MaterialTheme {

        val collection1 = CollectionsView(
            collectionId = 1L,
            color = Color.Cyan.toArgb(),
            displayName = "Test",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )

        val collection2 = CollectionsView(
            collectionId = 2L,
            displayName = "Test Number 2",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL",
            numJournals = 5,
            numNotes = 19,
            numTodos = 8989,
            supportsVJOURNAL = true,
            supportsVTODO = true
        )

        val collection3 = CollectionsView(
            collectionId = 3L,
            displayName = "Test",
            description = "Here comes the desc",
            accountName = "Another account",
            accountType = "at.bitfire.davx5"
        )
        CollectionsScreenContent(
            collections = listOf(collection1, collection2, collection3),
            isProcessing = MutableLiveData(true),
            settingAccessibilityMode = false,
            onCollectionChanged = { },
            onCollectionDeleted = { },
            onEntriesMoved = { _, _ -> },
            onImportFromICS = { },
            onImportFromTxt = { },
            onExportAsICS = { },
            onCollectionClicked = { },
            onDeleteAccount = { }
        )
    }
}



