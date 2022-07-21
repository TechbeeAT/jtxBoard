/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.screens

import android.Manifest
import android.media.MediaPlayer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.compose.dialogs.RequestContactsPermissionDialog
import at.techbee.jtx.ui.compose.elements.CollectionsSpinner
import at.techbee.jtx.ui.compose.elements.ColoredEdge
import at.techbee.jtx.ui.compose.elements.VerticalDateBlock
import at.techbee.jtx.ui.compose.stateholder.GlobalStateHolder
import at.techbee.jtx.ui.theme.JtxBoardTheme
import net.fortuna.ical4j.model.Component


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    iCalEntityLive: LiveData<ICalEntity>,
    subtasks: List<ICal4List>,
    subnotes: List<ICal4List>,
    attachments: List<Attachment>,
    allCollections: List<ICalCollection>,
    modifier: Modifier = Modifier,
    //player: MediaPlayer?,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit,
    onExpandedChanged: (itemId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isAttachmentsExpanded: Boolean) -> Unit
) {

    val context = LocalContext.current
    // Read contacts permission
    val readContactsGrantedText = stringResource(id = R.string.permission_read_contacts_granted)
    val readContactsDeniedText = stringResource(id = R.string.permission_read_contacts_denied)
    var permissionsDialogShownOnce by rememberSaveable { mutableStateOf(true) }  // TODO: Set to false for release!
    var editMode by remember { mutableStateOf(false) }

    val iCalEntity by iCalEntityLive.observeAsState(ICalEntity())

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, readContactsGrantedText, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, readContactsDeniedText, Toast.LENGTH_LONG).show()
        }
    }
    if(!permissionsDialogShownOnce) {
        RequestContactsPermissionDialog(
            onConfirm = {
                launcher.launch(Manifest.permission.READ_CONTACTS)
                permissionsDialogShownOnce = true
            },
            onDismiss = { permissionsDialogShownOnce = true }
        )
    }

    /*
    var markwon = Markwon.builder(LocalContext.current)
        .usePlugin(StrikethroughPlugin.create())
        .build()
     */

    Box {

        ColoredEdge(iCalEntity.property.color, iCalEntity.ICalCollection?.color)

        Column(modifier = Modifier.fillMaxWidth()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 8.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                CollectionsSpinner(
                    collections = allCollections,
                    preselected = iCalEntity.ICalCollection
                        ?: allCollections.first(),   // TODO: Load last used collection for new entries
                    includeReadOnly = false,
                    includeVJOURNAL = false,
                    includeVTODO = false,
                    onSelectionChanged = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Outlined.ColorLens, stringResource(id = R.string.color))
                }
                if(iCalEntity.property.dirty && iCalEntity.ICalCollection?.accountType != LOCAL_ACCOUNT_TYPE) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_readonly),
                        contentDescription = stringResource(id = R.string.readyonly),
                    )
                }
                if(iCalEntity.ICalCollection?.readonly == true) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_readonly),
                        contentDescription = stringResource(id = R.string.readyonly),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 8.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(iCalEntity.property.module == Module.JOURNAL.name)
                    iCalEntity.property.dtstart?.let {
                        VerticalDateBlock(datetime = it, timezone = iCalEntity.property.dtstartTimezone)
                    }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreen_JOURNAL() {
    MaterialTheme {
        DetailScreen(
            iCalEntityLive = MutableLiveData(ICalEntity().apply {
                this.property = ICalObject.createJournal("MySummary")
            }),
            subtasks = emptyList(),
            subnotes = emptyList(),
            attachments = emptyList(),
            allCollections = listOf(ICalCollection.createLocalCollection(LocalContext.current)),
            //player = null,
            onProgressChanged = { _, _, _ -> },
            onExpandedChanged = { _, _, _, _ -> }
        )
    }
}
