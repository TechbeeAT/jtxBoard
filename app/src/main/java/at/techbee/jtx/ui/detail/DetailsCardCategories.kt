/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredListSettingData
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.ui.theme.getContrastSurfaceColorFor


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailsCardCategories(
    categories: SnapshotStateList<Category>,
    isEditMode: Boolean,
    allCategoriesLive: LiveData<List<String>>,
    storedCategories: List<StoredCategory>,
    onCategoriesUpdated: () -> Unit,
    onGoToFilteredList: (StoredListSettingData) -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.categories)
    var newCategory by rememberSaveable { mutableStateOf("") }
    val allCategories by allCategoriesLive.observeAsState(emptyList())

    val mergedCategories = mutableListOf<StoredCategory>()
    mergedCategories.addAll(storedCategories)
    allCategories.forEach { cat -> if(mergedCategories.none { it.category == cat }) mergedCategories.add(StoredCategory(cat, null)) }

    fun addCategory() {
        if (newCategory.isNotEmpty() && categories.none { existing -> existing.text == newCategory }) {
            val careSensitiveCategory =
                allCategories.firstOrNull { it == newCategory }
                    ?: allCategories.firstOrNull { it.lowercase() == newCategory.lowercase() }
                    ?: newCategory
            categories.add(Category(text = careSensitiveCategory))
            onCategoriesUpdated()
        }
        newCategory = ""
    }


    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            HeadlineWithIcon(icon = Icons.AutoMirrored.Outlined.Label, iconDesc = headline, text = headline)

            AnimatedVisibility(categories.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { category ->
                        if(!isEditMode) {
                            ElevatedAssistChip(
                                onClick = { onGoToFilteredList(StoredListSettingData(searchCategories = listOf(category.text))) },
                                label = { Text(category.text) },
                                colors = StoredCategory.getColorForCategory(category.text, storedCategories)?.let { AssistChipDefaults.elevatedAssistChipColors(
                                    containerColor = it,
                                    labelColor = MaterialTheme.colorScheme.getContrastSurfaceColorFor(it)
                                ) }?: AssistChipDefaults.elevatedAssistChipColors(),
                            )
                        } else {
                            InputChip(
                                onClick = { },
                                label = { Text(category.text) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            categories.remove(category)
                                            onCategoriesUpdated()
                                        },
                                        content = {
                                            Icon(
                                                Icons.Outlined.Close,
                                                stringResource(id = R.string.delete)
                                            )
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                colors = StoredCategory.getColorForCategory(category.text, storedCategories)?.let { InputChipDefaults.inputChipColors(
                                    containerColor = it,
                                    labelColor = MaterialTheme.colorScheme.getContrastSurfaceColorFor(it)
                                ) }?: InputChipDefaults.inputChipColors(),
                                selected = false
                            )
                        }
                    }
                }
            }

            val categoriesToSelectFiltered = mergedCategories.filter { all ->
                all.category.lowercase().contains(newCategory.lowercase())
                        && categories.none { existing -> existing.text.lowercase() == all.category.lowercase() }
            }
            AnimatedVisibility(categoriesToSelectFiltered.isNotEmpty() && isEditMode) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    items(categoriesToSelectFiltered) { category ->
                        InputChip(
                            onClick = {
                                categories.add(Category(text = category.category))
                                onCategoriesUpdated()
                                newCategory = ""
                            },
                            label = { Text(category.category) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.NewLabel,
                                    stringResource(id = R.string.add)
                                )
                            },
                            selected = false,
                            colors = category.color?.let { InputChipDefaults.inputChipColors(
                                containerColor = Color(it),
                                labelColor = MaterialTheme.colorScheme.getContrastSurfaceColorFor(Color(it))
                            ) }?: InputChipDefaults.inputChipColors(),
                            modifier = Modifier.alpha(0.4f)
                        )
                    }
                }
            }

            Crossfade(isEditMode, label = "categoryEditMode") {
                if (it) {

                    OutlinedTextField(
                        value = newCategory,
                        leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Label, headline) },
                        trailingIcon = {
                            if (newCategory.isNotEmpty()) {
                                IconButton(onClick = {
                                    addCategory()
                                }) {
                                    Icon(
                                        Icons.Outlined.NewLabel,
                                        stringResource(id = R.string.add)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        label = { Text(headline) },
                        onValueChange = { newCategoryName -> newCategory = newCategoryName },
                        modifier = Modifier.fillMaxWidth(),
                        isError = newCategory.isNotEmpty(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            addCategory()
                        })
                    )
                }
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
            isEditMode = false,
            allCategoriesLive = MutableLiveData(listOf("category1", "category2", "Whatever")),
            storedCategories = listOf(StoredCategory("category1", Color.Green.toArgb())),
            onCategoriesUpdated = { },
            onGoToFilteredList = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardCategories_Preview_edit() {
    MaterialTheme {
        DetailsCardCategories(
            categories = remember { mutableStateListOf(Category(text = "asdf")) },
            isEditMode = true,
            allCategoriesLive = MutableLiveData(listOf("category1", "category2", "Whatever")),
            storedCategories = listOf(StoredCategory("category1", Color.Green.toArgb())),
            onCategoriesUpdated = { },
            onGoToFilteredList = { }
        )
    }
}