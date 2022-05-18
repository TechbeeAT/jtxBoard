/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.theme.JtxBoardTheme

/*
class ListScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JtxBoardTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

 */


@Composable
fun JournalStack() {

    val icalobjects =
        listOf(
            ICal4List.getSample().apply {
                summary = "Entry1"
            },
            ICal4List.getSample().apply {
                summary = "Entry2"
            },
            ICal4List.getSample().apply {
                summary = "Entry3"
            },
        )

    LazyColumn(content = {
        items(icalobjects) { iCalObject ->
            ICalObjectListCard(iCalObject)
        }
    })

}


@Preview(showBackground = true)
@Composable
fun JournalStackPreview() {
    JtxBoardTheme {
        JournalStack()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ICalObjectListCard(iCalObject: ICal4List) {


    //Text(text = "Hello $name!")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {


        Box {

            ColoredEdge(iCalObject.colorItem, iCalObject.colorCollection)

            Column {

                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
                        Text(iCalObject.collectionDisplayName?:iCalObject.accountName?:"")
                        Text(iCalObject.categories?:"", modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(modifier = Modifier.padding(end = 8.dp, top = 8.dp)) {
                        if (iCalObject.uploadPending)
                            Icon(Icons.Outlined.CloudSync, stringResource(R.string.upload_pending))
                        if (iCalObject.isRecurringOriginal || (iCalObject.isRecurringInstance && iCalObject.isLinkedRecurringInstance))
                            Icon(
                                Icons.Default.EventRepeat,
                                stringResource(R.string.list_item_recurring),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        if (iCalObject.isRecurringInstance && !iCalObject.isLinkedRecurringInstance)
                            Icon(
                                painter = painterResource(R.drawable.ic_recur_exception),
                                stringResource(R.string.list_item_recurring),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                    }
                }

                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Top) {

                    if(iCalObject.module == Module.JOURNAL.name)
                        VerticalDateBlock(
                            iCalObject.dtstart ?: System.currentTimeMillis(),
                            iCalObject.dtstartTimezone
                        )

                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        iCalObject.summary?.let {
                            Text(text = it, fontWeight = FontWeight.Bold)
                        }
                        iCalObject.description?.let {
                            Text(text = it,
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis)
                        }
                    }
                }


                LazyRow(content = {

                    val attachments = listOf(
                        Attachment.getSample(),
                        Attachment.getSample(),
                        Attachment.getSample()
                    )

                    items(attachments) { attachment ->
                        AttachmentCard(attachment)
                    }

                },
                modifier = Modifier.padding(end = 8.dp))


                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    StatusClassificationBlock(
                        component = iCalObject.component,
                        status = iCalObject.status,
                        classification = iCalObject.classification,
                        modifier = Modifier.padding(8.dp)
                    )

                    Row(modifier = Modifier.padding(8.dp)) {

                        IconWithText(
                            icon = Icons.Outlined.Group,
                            iconDesc = stringResource(R.string.attendees),
                            text = iCalObject.numAttendees.toString(),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        IconWithText(
                            icon = Icons.Outlined.Attachment,
                            iconDesc = stringResource(R.string.attachments),
                            text = iCalObject.numAttachments.toString(),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        IconWithText(
                            icon = Icons.Outlined.Comment,
                            iconDesc = stringResource(R.string.comments),
                            text = iCalObject.numComments.toString()
                        )
                    }

                }

                if(iCalObject.component == Component.VTODO.name)
                    ProgressElement(iCalObject.percent)

                LazyColumn(content = {
                    val subtasks = listOf(
                        ICalObject.createTask("Subtask1"),
                        ICalObject.createTask("Subtask2"),
                        ICalObject.createTask("Subtask3").apply {
                            percent = 36
                        }
                    )

                    items(subtasks) { subtask ->
                        SubtaskCard(subtask)
                    }
                },
                modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ICalObjectListCardPreview() {
    JtxBoardTheme {

        val icalobject = ICal4List.getSample()
        ICalObjectListCard(icalobject)
    }
}


@Composable
fun StatusClassificationBlock(
    component: String,
    status: String?,
    classification: String?,
    modifier: Modifier = Modifier
) {

    val statusText: String? = when {
        //component == Component.VTODO.name && status == StatusTodo.`NEEDS-ACTION`.name -> stringResource(id = R.string.todo_status_needsaction)
        //component == Component.VTODO.name && status == StatusTodo.`IN-PROCESS`.name -> stringResource(id = R.string.todo_status_inprocess)
        component == Component.VTODO.name && status == StatusTodo.CANCELLED.name -> stringResource(
            id = R.string.todo_status_cancelled
        )
        //component == Component.VTODO.name && status == StatusTodo.COMPLETED.name -> stringResource(id = R.string.todo_status_completed)

        component == Component.VJOURNAL.name && status == StatusJournal.DRAFT.name -> stringResource(
            id = R.string.journal_status_draft
        )
        //component == Component.VJOURNAL.name && status == StatusJournal.FINAL.name -> stringResource(id = R.string.journal_status_final)
        component == Component.VJOURNAL.name && status == StatusJournal.CANCELLED.name -> stringResource(
            id = R.string.journal_status_cancelled
        )
        else -> null
    }
    val classificationText: String? = when (classification) {
        Classification.PRIVATE.name -> stringResource(id = R.string.classification_private)
        Classification.CONFIDENTIAL.name -> stringResource(id = R.string.classification_confidential)
        //Classification.PUBLIC.name -> stringResource(id = R.string.classification_public)
        else -> null
    }

    Row(modifier = modifier) {

        statusText?.let {
            IconWithText(
                icon = Icons.Outlined.PublishedWithChanges,
                iconDesc = stringResource(R.string.status),
                text = it,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        classificationText?.let {
            IconWithText(
                icon = Icons.Outlined.AdminPanelSettings,
                iconDesc = stringResource(R.string.classification),
                text = it,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatusClassificationBlock_Preview_nothingDisplayed() {
    JtxBoardTheme {
        StatusClassificationBlock(
            component = Component.VJOURNAL.name,
            status = StatusJournal.FINAL.name,
            classification = Classification.PUBLIC.name
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatusClassificationBlock_Preview_bothDisplayed() {
    JtxBoardTheme {
        StatusClassificationBlock(
            component = Component.VJOURNAL.name,
            status = StatusJournal.DRAFT.name,
            classification = Classification.CONFIDENTIAL.name
        )
    }
}