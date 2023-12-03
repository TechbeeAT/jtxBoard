package at.techbee.jtx.ui.detail

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.ui.reusable.dialogs.ColorPickerDialog
import at.techbee.jtx.ui.reusable.elements.CollectionInfoColumn
import at.techbee.jtx.ui.reusable.elements.CollectionsSpinner
import at.techbee.jtx.ui.reusable.elements.ListBadge
import at.techbee.jtx.ui.theme.jtxCardBorderStrokeWidth


@Composable
fun DetailsCardCollections(
    iCalObject: ICalObject?,
    isEditMode: Boolean,
    isChild: Boolean,
    originalICalEntity: ICalEntity?,
    color: MutableState<Int?>,
    includeVJOURNAL: Boolean?,
    includeVTODO: Boolean?,
    changeState: MutableState<DetailViewModel.DetailChangeState>,
    allPossibleCollections: List<ICalCollection>,
    onMoveToNewCollection: (newCollection: ICalCollection) -> Unit,
    ) {

    var showColorPicker by rememberSaveable { mutableStateOf(false) }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = color.value,
            onColorChanged = { newColor ->
                color.value = newColor
                iCalObject?.color = newColor
                changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
            },
            onDismiss = {
                showColorPicker = false
            }
        )
    }

    AnimatedVisibility(!isEditMode || isChild) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                colors = CardDefaults.elevatedCardColors(),
                elevation = CardDefaults.elevatedCardElevation(),
                border = color.value?.let { BorderStroke(jtxCardBorderStrokeWidth, Color(it)) },
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ListBadge(
                        icon = Icons.Outlined.FolderOpen,
                        iconDesc = stringResource(id = R.string.collection),
                        containerColor = originalICalEntity?.ICalCollection?.color?.let {
                            Color(
                                it
                            )
                        } ?: MaterialTheme.colorScheme.primaryContainer,
                        isAccessibilityMode = true
                    )
                    originalICalEntity?.ICalCollection?.let {
                        CollectionInfoColumn(
                            collection = it,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    AnimatedVisibility(isEditMode && !isChild) {
        Card(
            colors = CardDefaults.elevatedCardColors(),
            elevation = CardDefaults.elevatedCardElevation(),
            border = color.value?.let { BorderStroke(jtxCardBorderStrokeWidth, Color(it)) },
            modifier = Modifier.fillMaxWidth()
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                CollectionsSpinner(
                    collections = allPossibleCollections,
                    preselected = originalICalEntity?.ICalCollection
                        ?: allPossibleCollections.first(),
                    includeReadOnly = false,
                    includeVJOURNAL = includeVJOURNAL,
                    includeVTODO = includeVTODO,
                    onSelectionChanged = { newCollection ->
                        if (iCalObject?.collectionId != newCollection.collectionId) {
                            onMoveToNewCollection(newCollection)
                        }
                    },
                    enabled = iCalObject?.recurid.isNullOrEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                )
                IconButton(onClick = { showColorPicker = true }) {
                    Icon(Icons.Outlined.ColorLens, stringResource(id = R.string.color))
                }
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun DetailsCardCollections_edit() {
    MaterialTheme {
        val entity = ICalEntity().apply {
            this.property = ICalObject.createJournal("MySummary")
            //this.property.dtstart = System.currentTimeMillis()
        }
        entity.property.description = "Hello World, this \nis my description."
        entity.property.contact = "John Doe, +1 555 5545"
        entity.categories = listOf(
            Category(1, 1, "MyCategory1", null, null),
            Category(2, 1, "My Dog likes Cats", null, null),
            Category(3, 1, "This is a very long category", null, null),
        )
        entity.property.color = Color.Blue.toArgb()

        val context = LocalContext.current

        DetailsCardCollections(
            iCalObject = entity.property,
            isEditMode = true,
            isChild = false,
            originalICalEntity = entity,
            color = remember { mutableStateOf(entity.property.color) },
            includeVJOURNAL = null,
            includeVTODO = null,
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGEUNSAVED) },
            allPossibleCollections = listOf(ICalCollection.createLocalCollection(context)),
            onMoveToNewCollection = {}
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardCollections_read() {
    MaterialTheme {
        val entity = ICalEntity().apply {
            this.property = ICalObject.createJournal("MySummary")
            //this.property.dtstart = System.currentTimeMillis()
        }
        entity.property.description = "Hello World, this \nis my description."
        entity.property.contact = "John Doe, +1 555 5545"
        entity.categories = listOf(
            Category(1, 1, "MyCategory1", null, null),
            Category(2, 1, "My Dog likes Cats", null, null),
            Category(3, 1, "This is a very long category", null, null),
        )
        entity.property.color = Color.Blue.toArgb()

        val context = LocalContext.current

        DetailsCardCollections(
            iCalObject = entity.property,
            isEditMode = false,
            isChild = false,
            originalICalEntity = entity,
            color = remember { mutableStateOf(entity.property.color) },
            includeVJOURNAL = null,
            includeVTODO = null,
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGEUNSAVED) },
            allPossibleCollections = listOf(ICalCollection.createLocalCollection(context)),
            onMoveToNewCollection = {}
        )
    }
}
