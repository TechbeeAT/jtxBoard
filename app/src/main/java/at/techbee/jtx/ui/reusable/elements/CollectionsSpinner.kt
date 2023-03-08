package at.techbee.jtx.ui.reusable.elements

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.flavored.BillingManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsSpinner(
    collections: List<ICalCollection>,
    preselected: ICalCollection,
    modifier: Modifier = Modifier,
    includeReadOnly: Boolean,
    includeVJOURNAL: Boolean? = null,
    includeVTODO: Boolean? = null,
    enabled: Boolean = true,
    onSelectionChanged: (collection: ICalCollection) -> Unit
) {

    val context = LocalContext.current
    var selected by remember { mutableStateOf(preselected) }
    var expanded by remember { mutableStateOf(false) } // initial value
    val isProPurchased by BillingManager.getInstance().isProPurchased.observeAsState(true)

    OutlinedCard(
        modifier = modifier,
        onClick = {
            if (enabled)
                expanded = !expanded
        }
    ) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            selected.color?.let {
                Badge(
                    containerColor = Color(it)
                ) {
                    Text(selected.displayName?.firstOrNull()?.toString() ?: " ")
                }
            }
            Text(
                text = selected.displayName + selected.accountName?.let { " ($it)" },
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (!enabled) 0.5f else 1f)
            )
            Icon(Icons.Outlined.ArrowDropDown, null)

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
                            Text(
                                text = (collection.displayName ?: collection.accountName)
                                    ?: " ",
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .align(Alignment.Start)
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
            includeReadOnly = true,
            includeVJOURNAL = true,
            includeVTODO = true,
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
            includeReadOnly = true,
            includeVJOURNAL = true,
            includeVTODO = true,
            onSelectionChanged = { },
            enabled = false,
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
            includeReadOnly = true,
            includeVJOURNAL = true,
            includeVTODO = true,
            onSelectionChanged = { },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
