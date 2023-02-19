package at.techbee.jtx.ui.reusable.dialogs

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
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
                    mainAxisAlignment = FlowMainAxisAlignment.Center,
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp
                ) {
                    Module.values().forEach { module ->
                        ElevatedFilterChip(
                            selected = module == selected,
                            onClick = { selected = module },
                            label = { Text(
                                when(module) {
                                    Module.JOURNAL -> context.getString(R.string.journal)
                                    Module.NOTE -> context.getString(R.string.note)
                                    Module.TODO -> context.getString(R.string.task)
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
            files = listOf(Uri.parse("https://www.techbee.at/datei.txt"), Uri.parse("content://xxx/datei2.md")),
            onModuleSelected = { },
            onDismiss = { }
        )
    }
}
