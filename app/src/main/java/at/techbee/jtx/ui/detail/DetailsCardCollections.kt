package at.techbee.jtx.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.ui.reusable.elements.CollectionsSpinner
import at.techbee.jtx.ui.theme.jtxCardBorderStrokeWidth


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

    fun onColorPicked(newColor: Int?) {
        color.value = newColor
        iCalObject?.color = newColor
        changeState.value = DetailViewModel.DetailChangeState.CHANGEUNSAVED
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
            showColorPicker = !originalCollection.readonly,
            onColorPicked = { newColor -> onColorPicked(newColor)},
            showSyncButton = (originalCollection.accountType != LOCAL_ACCOUNT_TYPE
                    && seriesElement?.dirty ?: iCalObject?.dirty ?: false),
            enableSelector = !originalCollection.readonly  && !isChild && iCalObject?.recurid.isNullOrEmpty(),
            modifier = modifier,
            border = color.value?.let { BorderStroke(jtxCardBorderStrokeWidth, Color(it)) }
        )
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
