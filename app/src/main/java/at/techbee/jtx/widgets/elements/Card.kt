/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets.elements


import androidx.compose.material.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.unit.ColorProvider
import at.techbee.jtx.widgets.WidgetTheme

/**
 * A Card composable until Glance releases an official one.
 * @author Arnau Mora
 * @since 20220110
 */
@Composable
fun Card(
    modifier: GlanceModifier = GlanceModifier,
    containerColor: ColorProvider? = null,
    // TODO: Add contentColor when something like LocalTextStyle is added to Glance
    // contentColor: ColorProvider? = null,
    cornerRadius: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val backgroundColor = containerColor ?: androidx.glance.appwidget.unit.ColorProvider(
        WidgetTheme.lightColors.primaryContainer.getColor(LocalContext.current),
        WidgetTheme.darkColors.primaryContainer.getColor(LocalContext.current),
    )
    // val foregroundColor = contentColor ?: androidx.glance.appwidget.unit.ColorProvider(
    //     WidgetTheme.lightColors.onPrimaryContainer.getColor(LocalContext.current),
    //     WidgetTheme.darkColors.onPrimaryContainer.getColor(LocalContext.current),
    // )
    Row(
        modifier = modifier,
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .cornerRadius(cornerRadius),
        ) {
            // Note: not exact, just template code until Glance provides an option
            // CompositionLocalProvider(LocalTextStyle provides foregroundColor) {
            content()
            // }
        }
    }
}
