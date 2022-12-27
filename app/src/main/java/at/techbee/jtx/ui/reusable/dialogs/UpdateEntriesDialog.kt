/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Category


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateEntriesDialog(
    allCategories: List<String>
    //current: ICalCollection,
    //onCollectionChanged: (ICalCollection) -> Unit,
    //onDismiss: () -> Unit
) {

    val categories = mutableListOf<Category>()

    AlertDialog(
        onDismissRequest = {
            //onDismiss()
                           },
        title = { Text(stringResource(R.string.categories))  },
        text = {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                  FilterChip(
                      selected = true,
                      onClick = { /*TODO*/ } ,
                      label =  { Text("Add") }
                  )
                    FilterChip(
                        selected = true,
                        onClick = { /*TODO*/ } ,
                        label =  { Text("Remove") }
                    )
                    FilterChip(
                        selected = true,
                        onClick = { /*TODO*/ } ,
                        label =  { Text("Set") }
                    )
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    FilterChip(
                        selected = true,
                        onClick = { /*TODO*/ } ,
                        label =  { Text(stringResource(id = R.string.categories)) }
                    )
                    FilterChip(
                        selected = true,
                        onClick = { /*TODO*/ } ,
                        label =  { Text(stringResource(id = R.string.resources)) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {

                }
            ) {
                Text(stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    //onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )

}

@Preview(showBackground = true)
@Composable
fun UpdateEntriesDialog_Preview() {
    MaterialTheme {

        UpdateEntriesDialog(
            allCategories = emptyList()
        )
    }
}
