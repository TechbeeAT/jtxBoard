package at.techbee.jtx.ui.reusable.elements

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.flavored.BillingManager


@Composable
fun CollectionsSpinner(
    collections: List<ICalCollection>,
    preselected: ICalCollection,
    enableSelector: Boolean,
    modifier: Modifier = Modifier,
    includeReadOnly: Boolean,
    showSyncButton: Boolean,
    showColorPicker: Boolean,
    onColorPicked: (Int?) -> Unit,
    includeVJOURNAL: Boolean? = null,
    includeVTODO: Boolean? = null,
    border: BorderStroke? = null,
    onSelectionChanged: (collection: ICalCollection) -> Unit
) {

    val context = LocalContext.current
    var selected by remember { mutableStateOf(preselected) }
    var expanded by remember { mutableStateOf(false) } // initial value
    val isProPurchased by if(LocalInspectionMode.current) remember { mutableStateOf(true)} else BillingManager.getInstance().isProPurchased.observeAsState(true)

    Card(
        colors = CardDefaults.elevatedCardColors(),
        elevation = CardDefaults.elevatedCardElevation(),
        modifier = modifier,
        border = border,
        onClick = {
            if (!selected.readonly && enableSelector)
                expanded = !expanded
        }
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            ListBadge(
                icon = Icons.Outlined.FolderOpen,
                iconDesc = stringResource(id = R.string.collection),
                containerColor = selected.color?.let {Color(it) } ?: MaterialTheme.colorScheme.primaryContainer,
                isAccessibilityMode = true
            )
            CollectionInfoColumn(
                collection = selected,
                showSyncButton = showSyncButton,
                showColorPicker = showColorPicker,
                onColorPicked = onColorPicked,
                showDropdownArrow = !selected.readonly,
                //modifier = Modifier.alpha(if (!enabled) 0.5f else 1f)
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                collections.forEach { collection ->

                    if (collection.readonly && !includeReadOnly)
                        return@forEach

                    if (selected.collectionId == collection.collectionId)
                        return@forEach

                    includeVJOURNAL?.let { if (!it || (it && !collection.supportsVJOURNAL)) return@forEach }
                    includeVTODO?.let { if (!it || (it && !collection.supportsVTODO)) return@forEach }

                    DropdownMenuItem(
                        onClick = {
                            if(collection.accountType != ICalCollection.LOCAL_ACCOUNT_TYPE && !isProPurchased) {
                                expanded = false
                                Toast.makeText(context, context.getString(R.string.collections_dialog_buypro_info), Toast.LENGTH_LONG).show()
                            } else {
                                selected = collection
                                expanded = false
                                onSelectionChanged(selected)
                            }
                        },
                        text = {
                            CollectionInfoColumn(
                                collection = collection,
                                showSyncButton = false,
                                showColorPicker = false,
                                showDropdownArrow = false,
                                onColorPicked = { }
                            )
                        }
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CollectionsSpinner_Preview() {
    MaterialTheme {
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
            enableSelector = true,
            includeReadOnly = true,
            includeVJOURNAL = true,
            includeVTODO = true,
            showSyncButton = true,
            showColorPicker = true,
            onColorPicked = { },
            onSelectionChanged = { },
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CollectionsSpinner_Preview_notenabled() {
    MaterialTheme {
        val collection1 = ICalCollection(
            collectionId = 1L,
            color = Color.Cyan.toArgb(),
            displayName = "Collection Display Name",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )
        CollectionsSpinner(
            listOf(collection1),
            preselected = collection1,
            enableSelector = false,
            includeReadOnly = true,
            includeVJOURNAL = true,
            includeVTODO = true,
            showSyncButton = false,
            showColorPicker = false,
            onColorPicked = { },
            onSelectionChanged = { },
            modifier = Modifier.fillMaxWidth()
        )
    }
}



@Preview(showBackground = true)
@Composable
fun CollectionsSpinner_Preview_no_color() {
    MaterialTheme {
        val collection1 = ICalCollection(
            collectionId = 1L,
            color = null,
            displayName = "Collection Display Name",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )
        CollectionsSpinner(
            listOf(collection1),
            preselected = collection1,
            enableSelector = true,
            includeReadOnly = true,
            includeVJOURNAL = true,
            includeVTODO = true,
            showSyncButton = true,
            showColorPicker = true,
            onColorPicked = { },
            onSelectionChanged = { },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
