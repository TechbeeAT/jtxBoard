/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredListSettingData
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.ui.reusable.dialogs.EditCategoriesDialog
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.ui.theme.getContrastSurfaceColorFor


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailsCardCategories(
    categories: SnapshotStateList<Category>,
    isReadOnly: Boolean,
    storedCategories: List<StoredCategory>,
    onCategoriesUpdated: (List<Category>) -> Unit,
    onGoToFilteredList: (StoredListSettingData) -> Unit,
    modifier: Modifier = Modifier
) {

    var showEditCategoriesDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    if(showEditCategoriesDialog) {
        EditCategoriesDialog(
            initialCategories = categories,
            allCategories = ICalDatabase
                .getInstance(context)
                .iCalDatabaseDao()
                .getAllCategoriesAsText()
                .observeAsState(emptyList()).value,
            storedCategories = storedCategories,
            onCategoriesUpdated = onCategoriesUpdated,
            onDismiss = { showEditCategoriesDialog = false }
        )
    }

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            HeadlineWithIcon(
                icon = Icons.AutoMirrored.Outlined.Label,
                iconDesc = null,
                text = stringResource(id = R.string.categories)
            )

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { category ->
                    ElevatedAssistChip(
                        onClick = { onGoToFilteredList(StoredListSettingData(searchCategories = listOf(category.text))) },
                        label = { Text(category.text) },
                        colors = StoredCategory.getColorForCategory(category.text, storedCategories)?.let { AssistChipDefaults.elevatedAssistChipColors(
                            containerColor = it,
                            labelColor = MaterialTheme.colorScheme.getContrastSurfaceColorFor(it)
                        ) }?: AssistChipDefaults.elevatedAssistChipColors(),
                    )
                }

                ElevatedAssistChip(
                    onClick = {
                              if(!isReadOnly)
                                  showEditCategoriesDialog = true
                              },
                    label = { Icon(
                        Icons.Outlined.Edit,
                        stringResource(id = R.string.edit)
                    ) },
                    modifier = Modifier.alpha(0.4f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardCategories_Preview() {
    MaterialTheme {
        DetailsCardCategories(
            categories = remember { mutableStateListOf(Category(text = "asdf")) },
            isReadOnly = false,
            storedCategories = listOf(StoredCategory("category1", Color.Green.toArgb())),
            onCategoriesUpdated = { },
            onGoToFilteredList = { }
        )
    }
}
