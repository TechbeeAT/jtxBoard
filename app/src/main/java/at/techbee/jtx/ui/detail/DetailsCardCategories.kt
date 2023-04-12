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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import at.techbee.jtx.R
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredListSettingData
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsCardCategories(
    initialCategories: List<Category>,
    isEditMode: Boolean,
    allCategories: List<String>,
    storedCategories: List<StoredCategory>,
    onCategoriesUpdated: (List<Category>) -> Unit,
    onGoToFilteredList: (StoredListSettingData) -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.categories)
    var newCategory by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf(initialCategories) }

    val mergedCategories = mutableListOf<StoredCategory>()
    mergedCategories.addAll(storedCategories)
    allCategories.forEach { cat -> if(mergedCategories.none { it.category == cat }) mergedCategories.add(StoredCategory(cat, null)) }


    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            HeadlineWithIcon(icon = Icons.Outlined.Label, iconDesc = headline, text = headline)

            AnimatedVisibility(categories.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories.asReversed()) { category ->
                        if(!isEditMode) {
                            ElevatedAssistChip(
                                onClick = { onGoToFilteredList(StoredListSettingData(searchCategories = listOf(category.text))) },
                                label = { Text(category.text) },
                                colors = StoredCategory.getColorForCategory(category.text, storedCategories)?.let { AssistChipDefaults.elevatedAssistChipColors(
                                    containerColor = it,
                                    labelColor = contentColorFor(it)
                                ) }?: AssistChipDefaults.elevatedAssistChipColors(),
                            )
                        } else {
                            InputChip(
                                onClick = { },
                                label = { Text(category.text) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            categories = categories.filter { it != category }
                                            onCategoriesUpdated(categories)
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
                                    labelColor = contentColorFor(it)
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
                                categories = categories.plus(Category(text = category.category))
                                onCategoriesUpdated(categories)
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
                                labelColor = contentColorFor(Color(it))
                            ) }?: InputChipDefaults.inputChipColors(),
                            modifier = Modifier.alpha(0.4f)
                        )
                    }
                }
            }

            Crossfade(isEditMode) {
                if (it) {

                    OutlinedTextField(
                        value = newCategory,
                        leadingIcon = { Icon(Icons.Outlined.Label, headline) },
                        trailingIcon = {
                            if (newCategory.isNotEmpty()) {
                                IconButton(onClick = {
                                    categories = categories.plus(Category(text = newCategory))
                                    onCategoriesUpdated(categories)
                                    newCategory = ""
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
                        onValueChange = { newCategoryName ->
                            newCategory = newCategoryName
                            onCategoriesUpdated(categories)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = newCategory.isNotEmpty(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newCategory.isNotEmpty() && categories.none { existing -> existing.text == newCategory })
                                categories = categories.plus(Category(text = newCategory))
                            newCategory = ""
                            onCategoriesUpdated(categories)
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
            initialCategories = listOf(Category(text = "asdf")),
            isEditMode = false,
            allCategories = listOf("category1", "category2", "Whatever"),
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
            initialCategories = listOf(Category(text = "asdf")),
            isEditMode = true,
            allCategories = listOf("category1", "category2", "Whatever"),
            storedCategories = listOf(StoredCategory("category1", Color.Green.toArgb())),
            onCategoriesUpdated = { },
            onGoToFilteredList = { }
        )
    }
}