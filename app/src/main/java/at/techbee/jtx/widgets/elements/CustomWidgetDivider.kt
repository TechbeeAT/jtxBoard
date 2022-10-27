/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets.elements


import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.unit.ColorProvider


@Composable
fun CustomWidgetDivider(
    color: ColorProvider,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(modifier) {
        Box(
            modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(color)
        ) {}
    }
}
