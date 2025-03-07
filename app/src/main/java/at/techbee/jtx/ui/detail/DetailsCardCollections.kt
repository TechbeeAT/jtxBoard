package at.techbee.jtx.ui.detail

import androidx.compose.foundation.layout.fillMaxWidth
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.ui.reusable.dialogs.ColorPickerDialog
import at.techbee.jtx.ui.reusable.elements.CollectionsSpinner


@Composable
fun DetailsCardCollections(
    iCalObject: ICalObject?,
    seriesElement: ICalObject?,
    isChild: Boolean,
    originalCollection: ICalCollection,
    color: MutableState<Int?>,
    includeVJOURNAL: Boolean?,
    includeVTODO: Boolean?,
    changeState: MutableState<DetailViewModel.DetailChangeState>,
    allPossibleCollections: List<ICalCollection>,
    onMoveToNewCollection: (newCollection: ICalCollection) -> Unit,
    modifier: Modifier = Modifier
    ) {

    var showColorPicker by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    if (showColorPicker) {
        ColorPickerDialog(
            initialColorInt = color.value,
            onColorChanged = { newColor ->
                color.value = newColor
                iCalObject?.color = newColor
                changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
            },
            onDismiss = {
                showColorPicker = false
            },
            additionalColorsInt = ICalDatabase
                .getInstance(context)
                .iCalDatabaseDao()
                .getAllColors()
                .observeAsState(
                    initial = emptyList()
                ).value
        )
    }


    CollectionsSpinner(
        collections = allPossibleCollections,
        preselected = originalCollection,
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




@Preview(showBackground = true)
@Composable
fun DetailsCardCollections_edit() {
    MaterialTheme {
        val context = LocalContext.current

        DetailsCardCollections(
            iCalObject = ICalObject.createJournal("MySummary"),
            seriesElement = null,
            isChild = false,
            originalCollection = ICalCollection.createLocalCollection(context).apply { this.displayName = "Test" },
            color = remember { mutableStateOf(Color.Blue.toArgb()) },
            includeVJOURNAL = null,
            includeVTODO = null,
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGEUNSAVED) },
            allPossibleCollections = listOf(ICalCollection.createLocalCollection(context)),
            onMoveToNewCollection = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardCollections_read() {
    MaterialTheme {
        val context = LocalContext.current

        DetailsCardCollections(
            iCalObject = ICalObject.createJournal("MySummary"),
            seriesElement = null,
            isChild = false,
            originalCollection = ICalCollection.createLocalCollection(context).apply { this.displayName = "Test" },
            color = remember { mutableStateOf(Color.Blue.toArgb()) },
            includeVJOURNAL = null,
            includeVTODO = null,
            changeState = remember { mutableStateOf(DetailViewModel.DetailChangeState.CHANGEUNSAVED) },
            allPossibleCollections = listOf(ICalCollection.createLocalCollection(context)),
            onMoveToNewCollection = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
