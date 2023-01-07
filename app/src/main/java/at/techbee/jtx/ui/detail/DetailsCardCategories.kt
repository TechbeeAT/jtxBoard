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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsCardCategories(
    initialCategories: List<Category>,
    isEditMode: Boolean,
    allCategories: List<String>,
    onCategoriesUpdated: (List<Category>) -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.categories)
    var newCategory by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf(initialCategories) }


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
                                onClick = { },
                                label = { Text(category.text) }
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
                                selected = false
                            )
                        }
                    }
                }
            }

            val categoriesToSelect = allCategories.filter { all ->
                all.lowercase()
                    .contains(newCategory.lowercase()) && categories.none { existing -> existing.text.lowercase() == all.lowercase() }
            }
            AnimatedVisibility(categoriesToSelect.isNotEmpty() && isEditMode) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    items(categoriesToSelect) { category ->
                        InputChip(
                            onClick = {
                                categories = categories.plus(Category(text = category))
                                onCategoriesUpdated(categories)
                                newCategory = ""
                            },
                            label = { Text(category) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.NewLabel,
                                    stringResource(id = R.string.add)
                                )
                            },
                            selected = false,
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
            onCategoriesUpdated = { }
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
            onCategoriesUpdated = { }
        )
    }
}