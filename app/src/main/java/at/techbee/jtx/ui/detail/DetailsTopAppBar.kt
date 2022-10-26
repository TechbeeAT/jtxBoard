package at.techbee.jtx.ui.detail

import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsTopAppBar(
    title: String,
    goBack: () -> Unit,
    actions: @Composable () -> Unit = { }
) {

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

        },
        navigationIcon = {
            IconButton(onClick = { goBack() }) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = stringResource(id = R.string.back)
                )
            }
        },
        actions = { actions() }
    )
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DetailsTopAppBar_Preview_withSubtitle() {
    MaterialTheme {
        Scaffold(
            topBar = {
                DetailsTopAppBar(
                    title = "My Title comes here",
                    goBack = { }
                )
            },
            content = {}
        )
    }
}
