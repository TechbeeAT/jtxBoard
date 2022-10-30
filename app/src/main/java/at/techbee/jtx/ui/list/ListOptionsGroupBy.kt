/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ViewHeadline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.ListSettings
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import com.google.accompanist.flowlayout.FlowRow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOptionsGroupBy(
    module: Module,
    listSettings: ListSettings,
    onListSettingsChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        HeadlineWithIcon(
            icon = Icons.Outlined.ViewHeadline,
            iconDesc = stringResource(id = R.string.filter_group_by),
            text = stringResource(id = R.string.filter_group_by),
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = stringResource(id = R.string.filter_group_by_info),
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic
        )

        FlowRow(modifier = Modifier.fillMaxWidth()) {
            GroupBy.getValuesFor(module).forEach { groupBy ->
                FilterChip(
                    selected = listSettings.groupBy.value == groupBy,
                    onClick = {
                        if (listSettings.groupBy.value != groupBy)
                            listSettings.groupBy.value = groupBy
                        else
                            listSettings.groupBy.value = null
                        onListSettingsChanged()
                    },
                    label = { Text(stringResource(id = groupBy.stringResource)) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ListOptionsGroupBy_Preview() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(
            ListViewModel.PREFS_LIST_JOURNALS,
            Context.MODE_PRIVATE
        )
        val listSettings = ListSettings(prefs)

        ListOptionsGroupBy(
            module = Module.TODO,
            listSettings = listSettings,
            onListSettingsChanged = { },
            modifier = Modifier.fillMaxWidth()

        )
    }
}
