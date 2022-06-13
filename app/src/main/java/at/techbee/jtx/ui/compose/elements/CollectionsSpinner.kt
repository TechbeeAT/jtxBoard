package at.techbee.jtx.ui.compose.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.R
import at.techbee.jtx.ui.theme.JtxBoardTheme


@Composable
fun CollectionsSpinner(
    collections: List<ICalCollection>,
    preselected: ICalCollection,
    includeReadOnly: Boolean,
    includeVJOURNAL: Boolean,
    includeVTODO: Boolean,
    onSelectionChanged: (collection: ICalCollection) -> Unit
) {

    var selected by remember { mutableStateOf(preselected) }
    var expanded by remember { mutableStateOf(false) } // initial value

    Box {
        Column {
            OutlinedTextField(
                value = selected.displayName + selected.accountName?.let { " (" + it + ")" },
                onValueChange = { },
                label = { Text(text = stringResource(id = R.string.collection)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Outlined.ArrowDropDown, null) },
                readOnly = true
            )
            DropdownMenu(
                modifier = Modifier.fillMaxWidth(),
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                collections.forEach { collection ->

                    if((collection.readonly && !includeReadOnly)
                        || (collection.supportsVTODO && !includeVTODO)
                        || (collection.supportsVJOURNAL && ! includeVJOURNAL)
                    )
                        return@forEach

                    DropdownMenuItem(
                        //modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            selected = collection
                            expanded = false
                            onSelectionChanged(selected)
                        },
                        text = {
                            Text(
                                text = (collection.displayName?: collection.accountName) ?: " ",
                                modifier = Modifier.wrapContentWidth().align(Alignment.Start))
                        }
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent)
                .padding(10.dp)
                .clickable(
                    onClick = { expanded = !expanded }
                )
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CollectionsSpinner_Preview() {
    JtxBoardTheme {

        val collection1 = ICalCollection(
            collectionId = 1L,
            color = Color.Cyan.toArgb(),
            displayName = "Collection Display Name",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )
        val collection2 = ICalCollection(
            collectionId = 2L,
            color = Color.Cyan.toArgb(),
            displayName = "Hmmmm",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )
        val collection3 = ICalCollection(
            collectionId = 3L,
            color = Color.Cyan.toArgb(),
            displayName = null,
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )

        CollectionsSpinner(
            listOf(collection1, collection2, collection3),
            preselected = collection2,
            includeReadOnly = true,
            includeVJOURNAL = true,
            includeVTODO = true,
            onSelectionChanged = { }
        )
    }
}

