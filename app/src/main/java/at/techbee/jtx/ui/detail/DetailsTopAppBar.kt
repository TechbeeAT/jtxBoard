package at.techbee.jtx.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R


enum class DetailTopAppBarMode { ADD_SUBTASK, ADD_SUBNOTE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsTopAppBar(
    detailTopAppBarMode: DetailTopAppBarMode = DetailTopAppBarMode.ADD_SUBTASK,
    readonly: Boolean,
    goBack: () -> Unit,
    onAddSubtask: (String) -> Unit,
    onAddSubnote: (String) -> Unit,
    actions: @Composable () -> Unit = { }
) {

    var textFieldText by remember { mutableStateOf("") }

    CenterAlignedTopAppBar(
        title = {
            OutlinedTextField(
                value = textFieldText,
                onValueChange = {
                    textFieldText = it
                },
                enabled = !readonly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(32.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = {
                Crossfade(detailTopAppBarMode) {
                    when(it) {
                        DetailTopAppBarMode.ADD_SUBTASK -> Text(stringResource(id = R.string.detail_top_app_bar_quick_add_subtask))
                        DetailTopAppBarMode.ADD_SUBNOTE -> Text(stringResource(id = R.string.detail_top_app_bar_quick_add_subnote))
                    }
                }
                    },
                singleLine = true,
                maxLines = 1,
                leadingIcon = {
                    IconButton(onClick = { goBack() }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                trailingIcon = {
                    Row {
                        AnimatedVisibility(textFieldText.isNotBlank()) {
                            IconButton(onClick = {
                                if(textFieldText.isNotBlank()) {
                                    when(detailTopAppBarMode) {
                                        DetailTopAppBarMode.ADD_SUBTASK -> onAddSubtask(textFieldText)
                                        DetailTopAppBarMode.ADD_SUBNOTE -> onAddSubnote(textFieldText)
                                    }
                                }
                                textFieldText = ""
                            }) {
                                Crossfade(detailTopAppBarMode) {
                                    when(it) {
                                        DetailTopAppBarMode.ADD_SUBTASK -> Icon(Icons.Outlined.AddTask, stringResource(id = R.string.detail_top_app_bar_quick_add_subtask))
                                        DetailTopAppBarMode.ADD_SUBNOTE -> Icon(Icons.Outlined.NoteAdd, stringResource(id = R.string.detail_top_app_bar_quick_add_subnote))
                                    }
                                }
                            }
                        }
                        actions()
                    }
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if(textFieldText.isNotBlank()) {
                        when(detailTopAppBarMode) {
                            DetailTopAppBarMode.ADD_SUBTASK -> onAddSubtask(textFieldText)
                            DetailTopAppBarMode.ADD_SUBNOTE -> onAddSubnote(textFieldText)
                        }
                    }
                    textFieldText = ""
                })
            )
        }
    )
}



@Preview(showBackground = true)
@Composable
fun DetailsTopAppBar_Preview_withSubtitle() {
    MaterialTheme {
        Scaffold(
            topBar = {
                DetailsTopAppBar(
                    readonly = false,
                    goBack = { },
                    onAddSubnote = { },
                    onAddSubtask = { }
                )
            },
            content = { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) { }
            }
        )
    }
}
