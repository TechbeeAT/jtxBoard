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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import at.techbee.jtx.ui.detail.DetailTopAppBarMode.ADD_SUBNOTE
import at.techbee.jtx.ui.detail.DetailTopAppBarMode.ADD_SUBTASK

enum class DetailTopAppBarMode { ADD_SUBTASK, ADD_SUBNOTE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsTopAppBar(
    detailTopAppBarMode: DetailTopAppBarMode = ADD_SUBTASK,
    readonly: Boolean,
    goBack: () -> Unit,
    onAddSubtask: (String) -> Unit,
    onAddSubnote: (String) -> Unit,
    actions: @Composable () -> Unit = { }
) {
    var textFieldText by rememberSaveable { mutableStateOf("") }

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
                Crossfade(detailTopAppBarMode, label = "detailTopAppBarMode") {
                    when(it) {
                        ADD_SUBTASK -> Text(stringResource(id = R.string.detail_top_app_bar_quick_add_subtask))
                        ADD_SUBNOTE -> Text(stringResource(id = R.string.detail_top_app_bar_quick_add_subnote))
                    }
                }
                    },
                singleLine = true,
                maxLines = 1,
                leadingIcon = {
                    IconButton(onClick = { goBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
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
                                        ADD_SUBTASK -> onAddSubtask(textFieldText)
                                        ADD_SUBNOTE -> onAddSubnote(textFieldText)
                                    }
                                }
                                textFieldText = ""
                            }) {
                                Crossfade(detailTopAppBarMode, label = "detailTopAppBarMode") {
                                    when(it) {
                                        ADD_SUBTASK -> Icon(Icons.Outlined.AddTask, stringResource(id = R.string.detail_top_app_bar_quick_add_subtask))
                                        ADD_SUBNOTE -> Icon(Icons.AutoMirrored.Outlined.NoteAdd, stringResource(id = R.string.detail_top_app_bar_quick_add_subnote))
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
                            ADD_SUBTASK -> onAddSubtask(textFieldText)
                            ADD_SUBNOTE -> onAddSubnote(textFieldText)
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
                    onAddSubnote = {},
                    onAddSubtask = {}
                )
            },
            content = { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) { }
            }
        )
    }
}
