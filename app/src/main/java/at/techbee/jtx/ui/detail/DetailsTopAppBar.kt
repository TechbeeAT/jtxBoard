package at.techbee.jtx.ui.detail

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsTopAppBar(
    goBack: () -> Unit,
    onAddSubtask: (String) -> Unit,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.Transparent,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant),
                shape = RoundedCornerShape(32.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = { Text(stringResource(id = R.string.edit_subtasks_add_helper))},
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
                                if(textFieldText.isNotBlank())
                                    onAddSubtask(textFieldText)
                                textFieldText = ""
                            }) {
                                Icon(Icons.Outlined.AddTask, stringResource(id = R.string.edit_subtasks_add_helper))
                            }
                        }
                        actions()
                    }
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if(textFieldText.isNotBlank())
                        onAddSubtask(textFieldText)
                    textFieldText = ""
                })
            )
        }
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
                    goBack = { },
                    onAddSubtask = { }
                )
            },
            content = {}
        )
    }
}
