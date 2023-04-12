/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SubdirectoryArrowRight
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.reusable.cards.SubnoteCard
import at.techbee.jtx.ui.reusable.cards.SubtaskCard
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.ui.theme.jtxCardCornerShape


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailsCardParents(
    module: Module,
    parents: List<ICal4List>,
    isEditMode: MutableState<Boolean>,
    sliderIncrement: Int,
    showSlider: Boolean,
    onProgressChanged: (itemId: Long, newPercent: Int) -> Unit,
    goToDetail: (itemId: Long, editMode: Boolean, list: List<Long>) -> Unit,
    modifier: Modifier = Modifier
) {
    if(isEditMode.value)
        return

    val headline = stringResource(id = if(module == Module.TODO) R.string.view_subtask_of else R.string.view_linked_note_of)

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(), 
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeadlineWithIcon(icon = Icons.Outlined.SubdirectoryArrowRight, iconDesc = headline, text = headline, modifier = Modifier.weight(1f))
            }

            AnimatedVisibility(parents.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    parents.forEach { parent ->

                        if (parent.component == Component.VJOURNAL.name) {
                            SubnoteCard(
                                subnote = parent,
                                selected = false,
                                isEditMode = isEditMode.value,
                                onDeleteClicked = { },
                                onUnlinkClicked = { },
                                player = null,
                                modifier = Modifier
                                    .clip(jtxCardCornerShape)
                                    .combinedClickable(
                                        onClick = { goToDetail(parent.id, false, parents.map { it.id }) },
                                        onLongClick = {
                                            if (!isEditMode.value && !parent.isReadOnly && BillingManager.getInstance().isProPurchased.value == true)
                                                goToDetail(parent.id, true, parents.map { it.id })
                                        }
                                    )
                            )
                        } else if (parent.component == Component.VTODO.name) {
                            SubtaskCard(
                                subtask = parent,
                                selected = false,
                                isEditMode = false,
                                showProgress = showSlider,
                                sliderIncrement = sliderIncrement,
                                onProgressChanged = onProgressChanged,
                                onDeleteClicked = { },
                                onUnlinkClicked = { },
                                modifier = Modifier
                                    .clip(jtxCardCornerShape)
                                    .combinedClickable(
                                        onClick = { goToDetail(parent.id, false, parents.map { it.id }) },
                                        onLongClick = {
                                            if (!isEditMode.value &&!parent.isReadOnly && BillingManager.getInstance().isProPurchased.value == true)
                                                goToDetail(parent.id, true, parents.map { it.id })
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardParents_Preview_Journal() {
    MaterialTheme {

        DetailsCardParents(
            module = Module.JOURNAL,
            parents = listOf(
                        ICal4List.getSample().apply {
                            this.component = Component.VJOURNAL.name
                            this.module = Module.NOTE.name
                            this.summary = "My Subnote"
                        }
                    ),
            isEditMode = remember { mutableStateOf(false) },
            sliderIncrement = 10,
            showSlider = true,
            onProgressChanged = { _, _ -> },
            goToDetail = { _, _, _ -> },
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardParents_Preview_tasksedit() {
    MaterialTheme {
        DetailsCardParents(
            module = Module.TODO,
            parents = listOf(
                ICal4List.getSample().apply {
                    this.component = Component.VTODO.name
                    this.module = Module.TODO.name
                    this.summary = "My Subtask"
                }
            ),
            isEditMode = remember { mutableStateOf(false) },
            sliderIncrement = 10,
            showSlider = true,
            onProgressChanged = { _, _ -> },
            goToDetail = { _, _, _ -> },
        )
    }
}