package at.techbee.jtx.ui.reusable.dialogs

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import at.techbee.jtx.R
import at.techbee.jtx.database.Module

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SelectModuleForTxtImportDialog(
    files: List<Uri>,
    onModuleSelected: (module: Module) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf<Module?>(null)}

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.dialog_select_module_txt_dialog_title, files.size)) },
        text = {

            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    val jounalText = stringResource(R.string.journal)
                    val noteText = stringResource(R.string.note)
                    val taskText = stringResource(R.string.task)

                    Module.entries.forEach { module ->
                        ElevatedFilterChip(
                            selected = module == selected,
                            onClick = { selected = module },
                            label = { Text(
                                when(module) {
                                    Module.JOURNAL -> jounalText
                                    Module.NOTE -> noteText
                                    Module.TODO -> taskText
                                }
                            ) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selected?.let { onModuleSelected(it) }
                    onDismiss()
                },
                enabled = selected != null
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )

}


@Preview(showBackground = true)
@Composable
fun SelectModuleForTxtImportDialog_Preview() {
    MaterialTheme {

        SelectModuleForTxtImportDialog(
            files = listOf("https://www.techbee.at/datei.txt".toUri(), "content://xxx/datei2.md".toUri()),
            onModuleSelected = { },
            onDismiss = { }
        )
    }
}
