package at.techbee.jtx.ui.reusable.elements

import android.accounts.Account
import android.content.ContentResolver
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.EditOff
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.ui.reusable.dialogs.ColorPickerDialog
import at.techbee.jtx.util.SyncUtil


@Composable
fun CollectionInfoColumn(
    collection: ICalCollection,
    showSyncButton: Boolean,
    showColorPicker: Boolean,
    showDropdownArrow: Boolean,
    modifier: Modifier = Modifier,
    initialColor: Int? = null,
    onColorPicked: (Int?) -> Unit
) {

    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showColorPickerDialog by rememberSaveable { mutableStateOf(false) }

    val syncIconAnimation = rememberInfiniteTransition(label = "syncIconAnimation")
    val angle by syncIconAnimation.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
            }
        ), label = "syncIconAnimationAngle"
    )

    var isSyncInProgress by remember { mutableStateOf(false) }
    DisposableEffect(lifecycleOwner) {

        val listener = if (isPreview)
            null
        else {
            ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE) {
                isSyncInProgress = SyncUtil.isJtxSyncRunningFor(setOf(Account(collection.accountName, collection.accountType)))
            }
        }
        onDispose {
            if (!isPreview)
                ContentResolver.removeStatusChangeListener(listener)
        }
    }

    if (showColorPickerDialog) {
        ColorPickerDialog(
            initialColor = initialColor,
            onColorChanged = { newColor ->
                onColorPicked(newColor)
            },
            onDismiss = {
                showColorPickerDialog = false
            },
            additionalColorsInt = ICalDatabase
                .getInstance(context)
                .iCalDatabaseDao()
                .getAllColors()
                .observeAsState(initial = emptyList())
                .value
        )
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                collection.displayName?.let {
                    Text(
                        text = it,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                    )
                }
                collection.accountName?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .alpha(0.5f)
                            .weight(1f)
                    )
                }
            }
            if (collection.accountType != LOCAL_ACCOUNT_TYPE) {
                val url = try {
                    Uri.parse(collection.url).host
                } catch (e: NullPointerException) {
                    null
                }
                Text(
                    text = url ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.alpha(0.5f)
                )
            }
        }

        if(showDropdownArrow) {
            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        if(collection.readonly) {
            Icon(
                imageVector = Icons.Outlined.EditOff,
                contentDescription = stringResource(id = R.string.readyonly),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        AnimatedVisibility(showSyncButton || showColorPicker) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                VerticalDivider(modifier.height(30.dp))

                AnimatedVisibility(showSyncButton) {


                    IconButton(
                        onClick = {
                            if (!isSyncInProgress) {
                                collection.getAccount().let { SyncUtil.syncAccounts(setOf(it)) }
                                SyncUtil.showSyncRequestedToast(context)
                            }
                        },
                        enabled = showSyncButton && !isSyncInProgress
                    ) {
                        Crossfade(isSyncInProgress, label = "isSyncInProgress") { synchronizing ->
                            if (synchronizing) {
                                Icon(
                                    Icons.Outlined.Sync,
                                    contentDescription = stringResource(id = R.string.sync_in_progress),
                                    modifier = Modifier
                                        .graphicsLayer {
                                            rotationZ = angle
                                        }
                                        .alpha(0.3f),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.CloudSync,
                                    contentDescription = stringResource(id = R.string.upload_pending),
                                )
                            }
                        }
                    }
                }

                if(showColorPicker) {
                    IconButton(onClick = { showColorPickerDialog = true }) {
                        Icon(Icons.Outlined.ColorLens, stringResource(id = R.string.color))
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CollectionInfoColumn_Preview() {
    MaterialTheme {
        val collection1 = ICalCollection(
            collectionId = 1L,
            color = Color.Cyan.toArgb(),
            displayName = "Collection Display Name",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = LOCAL_ACCOUNT_TYPE
        )
        CollectionInfoColumn(
            collection = collection1,
            showSyncButton = false,
            showColorPicker = true,
            showDropdownArrow = false,
            onColorPicked = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CollectionInfoColumn_Preview_REMOTE() {
    MaterialTheme {
        val collection1 = ICalCollection(
            collectionId = 1L,
            color = Color.Cyan.toArgb(),
            displayName = "Collection Display Name",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "Remote",
            url = "https://www.example.com/whatever/219348729384/mine"
        )
        CollectionInfoColumn(
            collection = collection1,
            showSyncButton = true,
            showColorPicker = true,
            showDropdownArrow = true,
            onColorPicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionInfoColumn_Preview_READONLY() {
    MaterialTheme {
        val collection1 = ICalCollection(
            collectionId = 1L,
            color = Color.Cyan.toArgb(),
            displayName = "Collection Display Name",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "Remote",
            url = "https://www.example.com/whatever/219348729384/mine", 
            readonly = true
        )
        CollectionInfoColumn(
            collection = collection1,
            showSyncButton = false,
            showColorPicker = false,
            showDropdownArrow = false,
            onColorPicked = {}
        )
    }
}