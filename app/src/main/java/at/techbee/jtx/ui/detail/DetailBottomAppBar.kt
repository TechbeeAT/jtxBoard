/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.TextFormat
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.util.SyncApp

@Composable
fun DetailBottomAppBar(
    iCalObject: ICalObject?,
    collection: ICalCollection?,
    markdownState: MutableState<MarkdownState>,
    isProActionAvailable: Boolean,
    changeState: MutableState<DetailViewModel.DetailChangeState>,
    onRevertClicked: () -> Unit
) {

    if (iCalObject == null || collection == null)
        return

    val context = LocalContext.current


    BottomAppBar(
        actions = {
            AnimatedVisibility(markdownState.value == MarkdownState.CLOSED) {
                IconButton(onClick = { markdownState.value = MarkdownState.OBSERVING }) {
                    Icon(
                        Icons.Outlined.TextFormat,
                        contentDescription = stringResource(id = R.string.menu_view_markdown_formatting)
                    )
                }
            }

            AnimatedVisibility(
                changeState.value != DetailViewModel.DetailChangeState.UNCHANGED
                        && (markdownState.value == MarkdownState.DISABLED || markdownState.value == MarkdownState.CLOSED)
            ) {
                IconButton(onClick = { onRevertClicked() }) {
                    Icon(
                        painterResource(id = R.drawable.ic_revert),
                        contentDescription = stringResource(id = R.string.revert)
                    )
                }
            }


            AnimatedVisibility((changeState.value == DetailViewModel.DetailChangeState.CHANGEUNSAVED
                    || changeState.value == DetailViewModel.DetailChangeState.CHANGESAVING
                    || changeState.value == DetailViewModel.DetailChangeState.CHANGESAVED)
                    && (markdownState.value == MarkdownState.DISABLED || markdownState.value == MarkdownState.CLOSED)) {
                IconButton(
                    onClick = { },
                    enabled = false
                ) {
                    Crossfade(changeState.value, label = "saving_change_state") { state ->
                        when(state) {
                            DetailViewModel.DetailChangeState.CHANGEUNSAVED -> {
                                Icon(
                                    Icons.Outlined.DriveFileRenameOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.alpha(0.3f)
                                )
                            }
                            DetailViewModel.DetailChangeState.CHANGESAVING -> {
                                IconButton(onClick = { /* no action, icon button just to keep the same style */  }) {
                                    CircularProgressIndicator(modifier = Modifier
                                        .alpha(0.3f)
                                        .size(24.dp))
                                }
                            }
                            DetailViewModel.DetailChangeState.CHANGESAVED -> {
                                Icon(
                                    painterResource(id = R.drawable.ic_save_check_outline),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.alpha(0.3f)
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }

            // Icons for Markdown formatting
            AnimatedVisibility(markdownState.value != MarkdownState.DISABLED && markdownState.value != MarkdownState.CLOSED) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { markdownState.value = MarkdownState.CLOSED }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
                    }
                    VerticalDivider(modifier = Modifier.height(40.dp))
                }
            }
            AnimatedVisibility(markdownState.value != MarkdownState.DISABLED && markdownState.value != MarkdownState.CLOSED) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { markdownState.value = MarkdownState.BOLD }) {
                        Icon(Icons.Outlined.FormatBold, stringResource(R.string.markdown_bold))
                    }
                    IconButton(onClick = { markdownState.value = MarkdownState.ITALIC  }) {
                        Icon(Icons.Outlined.FormatItalic, stringResource(R.string.markdown_italic))
                    }
                    /*
                    IconButton(onClick = { markdownState.value = MarkdownState.UNDERLINED  }) {
                        Icon(Icons.Outlined.FormatUnderlined, stringResource(R.string.markdown_underlined))
                    }
                     */
                    IconButton(onClick = { markdownState.value = MarkdownState.STRIKETHROUGH  }) {
                        Icon(Icons.Outlined.FormatStrikethrough, stringResource(R.string.markdown_strikethrough))
                    }
                    IconButton(onClick = { markdownState.value = MarkdownState.H1  }) {
                        Icon(painterResource(id = R.drawable.ic_h1), stringResource(R.string.markdown_heading1))
                    }
                    IconButton(onClick = { markdownState.value = MarkdownState.H2  }) {
                        Icon(painterResource(id = R.drawable.ic_h2), stringResource(R.string.markdown_heading2))
                    }
                    IconButton(onClick = { markdownState.value = MarkdownState.H3  }) {
                        Icon(painterResource(id = R.drawable.ic_h3), stringResource(R.string.markdown_heading3))
                    }
                    IconButton(onClick = { markdownState.value = MarkdownState.HR  }) {
                        Icon(Icons.Outlined.HorizontalRule, stringResource(R.string.markdown_horizontal_ruler))
                    }
                    IconButton(onClick = { markdownState.value = MarkdownState.UNORDEREDLIST  }) {
                        Icon(Icons.AutoMirrored.Outlined.List, stringResource(R.string.markdown_unordered_list))
                    }
                    IconButton(onClick = { markdownState.value = MarkdownState.CODE  }) {
                        Icon(Icons.Outlined.Code, stringResource(R.string.markdown_code))
                    }
                }
            }
        },
        floatingActionButton = {
            // TODO(b/228588827): Replace with Secondary FAB when available.
            FloatingActionButton(
                onClick = {
                    if (!isProActionAvailable)
                        Toast.makeText(
                            context,
                            context.getText(R.string.buypro_snackbar_remote_entries_blocked),
                            Toast.LENGTH_LONG
                        ).show()

                    //TODO

                },
                //containerColor = if (collection.readonly || !isProActionAvailable) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer
            ) {
                //TODO: restrict editing on remote collections!!!
                Icon(painterResource(id = R.drawable.ic_save_move_outline), stringResource(id = R.string.save))
                /*
                Crossfade(targetState = isEditMode.value, label = "fab_icon_content") { isEditMode ->
                    if (isEditMode) {
                        Icon(painterResource(id = R.drawable.ic_save_move_outline), stringResource(id = R.string.save))
                    } else {
                        if (collection.readonly || !isProActionAvailable)
                            Icon(Icons.Filled.EditOff, stringResource(id = R.string.readyonly))
                        else
                            Icon(Icons.Filled.Edit, stringResource(id = R.string.edit))
                    }
                }
                 */
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun DetailBottomAppBar_Preview_View() {
    MaterialTheme {

        val collection = ICalCollection().apply {
            this.readonly = false
            this.accountType = SyncApp.DAVX5.accountType
        }

        DetailBottomAppBar(
            iCalObject = ICalObject.createNote().apply { dirty = true },
            collection = collection,
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            isProActionAvailable = true,
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGEUNSAVED) },
            onRevertClicked = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailBottomAppBar_Preview_edit() {
    MaterialTheme {

        val collection = ICalCollection().apply {
            this.readonly = false
            this.accountType = SyncApp.DAVX5.accountType
        }

        DetailBottomAppBar(
            iCalObject = ICalObject.createNote().apply { dirty = true },
            collection = collection,
            isProActionAvailable = true,
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVING) },
            onRevertClicked = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailBottomAppBar_Preview_edit_markdown() {
    MaterialTheme {

        val collection = ICalCollection().apply {
            this.readonly = false
            this.accountType = SyncApp.DAVX5.accountType
        }

        DetailBottomAppBar(
            iCalObject = ICalObject.createNote().apply { dirty = true },
            collection = collection,
            isProActionAvailable = true,
            markdownState = remember { mutableStateOf(MarkdownState.OBSERVING) },
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVING) },
            onRevertClicked = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailBottomAppBar_Preview_View_readonly() {
    MaterialTheme {

        val collection = ICalCollection().apply {
            this.readonly = true
            this.accountType = SyncApp.DAVX5.accountType
        }

        DetailBottomAppBar(
            iCalObject = ICalObject.createNote().apply { dirty = false },
            collection = collection,
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            isProActionAvailable = true,
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVED) },
            onRevertClicked = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailBottomAppBar_Preview_View_proOnly() {
    MaterialTheme {

        val collection = ICalCollection().apply {
            this.readonly = false
            this.accountType = SyncApp.DAVX5.accountType
        }

        DetailBottomAppBar(
            iCalObject = ICalObject.createNote().apply { dirty = false },
            collection = collection,
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            isProActionAvailable = false,
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVED) },
            onRevertClicked = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailBottomAppBar_Preview_View_local() {
    MaterialTheme {

        val collection = ICalCollection().apply {
            this.readonly = false
            this.accountType = LOCAL_ACCOUNT_TYPE
        }

        BillingManager.getInstance().initialise(LocalContext.current.applicationContext)

        DetailBottomAppBar(
            iCalObject = ICalObject.createNote().apply { dirty = true },
            collection = collection,
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            isProActionAvailable = true,
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGESAVING) },
            onRevertClicked = { }
        )
    }
}
