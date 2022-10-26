/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.glance.unit.ColorProvider
import at.techbee.jtx.R

/**
 * Temporary implementation of theme object for Glance-appwidgets.
 *
 * Important: It will change!
 */
object WidgetTheme {
    val lightColors: ColorProviders
        @Composable
        @ReadOnlyComposable
        get() = dynamicLightThemeColorProviders()

    val darkColors: ColorProviders
        @Composable
        @ReadOnlyComposable
        get() = dynamicDarkThemeColorProviders()
}

/**
 * Holds a set of Glance-specific [ColorProvider] following Material naming conventions.
 */
data class ColorProviders(
    val primary: ColorProvider,
    val onPrimary: ColorProvider,
    val primaryContainer: ColorProvider,
    val onPrimaryContainer: ColorProvider,
    val secondary: ColorProvider,
    val onSecondary: ColorProvider,
    val secondaryContainer: ColorProvider,
    val onSecondaryContainer: ColorProvider,
    val tertiary: ColorProvider,
    val onTertiary: ColorProvider,
    val tertiaryContainer: ColorProvider,
    val onTertiaryContainer: ColorProvider,
    val error: ColorProvider,
    val errorContainer: ColorProvider,
    val onError: ColorProvider,
    val onErrorContainer: ColorProvider,
    val background: ColorProvider,
    val onBackground: ColorProvider,
    val surface: ColorProvider,
    val onSurface: ColorProvider,
    val surfaceVariant: ColorProvider,
    val onSurfaceVariant: ColorProvider,
    val outline: ColorProvider,
    val textColorPrimary: ColorProvider,
    val textColorSecondary: ColorProvider,
    val inverseOnSurface: ColorProvider,
    val inverseSurface: ColorProvider,
    val inversePrimary: ColorProvider,
    val inverseTextColorPrimary: ColorProvider,
    val inverseTextColorSecondary: ColorProvider,
)

/**
 * Creates a set of color providers that represents a Material3 style dynamic color theme. On
 * devices that support it, the theme is derived from the user specific platform colors, on other
 * devices this falls back to the Material3 baseline theme.
 */
@SuppressLint("PrivateResource")
fun dynamicLightThemeColorProviders(): ColorProviders {
    return ColorProviders(
        primary = ColorProvider(R.color.m3_sys_color_dynamic_light_primary),
        onPrimary = ColorProvider(R.color.m3_sys_color_dynamic_light_on_primary),
        primaryContainer = ColorProvider(R.color.m3_sys_color_dynamic_light_primary_container),
        onPrimaryContainer = ColorProvider(R.color.m3_sys_color_dynamic_light_on_primary_container),
        secondary = ColorProvider(R.color.m3_sys_color_dynamic_light_secondary),
        onSecondary = ColorProvider(R.color.m3_sys_color_dynamic_light_on_secondary),
        secondaryContainer = ColorProvider(R.color.m3_sys_color_dynamic_light_secondary_container),
        onSecondaryContainer = ColorProvider(R.color.m3_sys_color_dynamic_light_on_secondary_container),
        tertiary = ColorProvider(R.color.m3_sys_color_dynamic_light_tertiary),
        onTertiary = ColorProvider(R.color.m3_sys_color_dynamic_light_on_tertiary),
        tertiaryContainer = ColorProvider(R.color.m3_sys_color_dynamic_light_tertiary_container),
        onTertiaryContainer = ColorProvider(R.color.m3_sys_color_dynamic_light_on_tertiary_container),
        error = ColorProvider(R.color.m3_sys_color_light_error),
        errorContainer = ColorProvider(R.color.m3_sys_color_light_error_container),
        onError = ColorProvider(R.color.m3_sys_color_light_on_error),
        onErrorContainer = ColorProvider(R.color.m3_sys_color_light_on_error_container),
        background = ColorProvider(R.color.m3_sys_color_dynamic_light_background),
        onBackground = ColorProvider(R.color.m3_sys_color_dynamic_light_on_background),
        surface = ColorProvider(R.color.m3_sys_color_dynamic_light_surface),
        onSurface = ColorProvider(R.color.m3_sys_color_dynamic_light_on_surface),
        surfaceVariant = ColorProvider(R.color.m3_sys_color_dynamic_light_surface_variant),
        onSurfaceVariant = ColorProvider(R.color.m3_sys_color_dynamic_light_on_surface_variant),
        outline = ColorProvider(R.color.m3_sys_color_dynamic_light_outline),
        textColorPrimary = ColorProvider(R.color.m3_sys_color_dynamic_light_primary),
        textColorSecondary = ColorProvider(R.color.m3_sys_color_dynamic_light_secondary),
        inverseOnSurface = ColorProvider(R.color.m3_sys_color_dynamic_light_inverse_on_surface),
        inverseSurface = ColorProvider(R.color.m3_sys_color_dynamic_light_inverse_surface),
        inversePrimary = ColorProvider(R.color.m3_sys_color_dynamic_light_inverse_primary),
        inverseTextColorPrimary = ColorProvider(R.color.m3_sys_color_dynamic_light_inverse_primary),
        inverseTextColorSecondary = ColorProvider(R.color.m3_sys_color_dynamic_light_inverse_primary),
    )
}

@SuppressLint("PrivateResource")
fun dynamicDarkThemeColorProviders(): ColorProviders {
    return ColorProviders(
        primary = ColorProvider(R.color.m3_sys_color_dynamic_dark_primary),
        onPrimary = ColorProvider(R.color.m3_sys_color_dynamic_dark_on_primary),
        primaryContainer = ColorProvider(R.color.m3_sys_color_dynamic_dark_primary_container),
        onPrimaryContainer = ColorProvider(R.color.m3_sys_color_dynamic_dark_on_primary_container),
        secondary = ColorProvider(R.color.m3_sys_color_dynamic_dark_secondary),
        onSecondary = ColorProvider(R.color.m3_sys_color_dynamic_dark_on_secondary),
        secondaryContainer = ColorProvider(R.color.m3_sys_color_dynamic_dark_secondary_container),
        onSecondaryContainer = ColorProvider(R.color.m3_sys_color_dynamic_dark_on_secondary_container),
        tertiary = ColorProvider(R.color.m3_sys_color_dynamic_dark_tertiary),
        onTertiary = ColorProvider(R.color.m3_sys_color_dynamic_dark_on_tertiary),
        tertiaryContainer = ColorProvider(R.color.m3_sys_color_dynamic_dark_tertiary_container),
        onTertiaryContainer = ColorProvider(R.color.m3_sys_color_dynamic_dark_on_tertiary_container),
        error = ColorProvider(R.color.m3_sys_color_dark_error),
        errorContainer = ColorProvider(R.color.m3_sys_color_dark_error_container),
        onError = ColorProvider(R.color.m3_sys_color_dark_on_error),
        onErrorContainer = ColorProvider(R.color.m3_sys_color_dark_on_error_container),
        background = ColorProvider(R.color.m3_sys_color_dynamic_dark_background),
        onBackground = ColorProvider(R.color.m3_sys_color_dynamic_dark_on_background),
        surface = ColorProvider(R.color.m3_sys_color_dynamic_dark_surface),
        onSurface = ColorProvider(R.color.m3_sys_color_dynamic_dark_on_surface),
        surfaceVariant = ColorProvider(R.color.m3_sys_color_dynamic_dark_surface_variant),
        onSurfaceVariant = ColorProvider(R.color.m3_sys_color_dynamic_dark_on_surface_variant),
        outline = ColorProvider(R.color.m3_sys_color_dynamic_dark_outline),
        textColorPrimary = ColorProvider(R.color.m3_sys_color_dynamic_dark_primary),
        textColorSecondary = ColorProvider(R.color.m3_sys_color_dynamic_dark_secondary),
        inverseOnSurface = ColorProvider(R.color.m3_sys_color_dynamic_dark_inverse_on_surface),
        inverseSurface = ColorProvider(R.color.m3_sys_color_dynamic_dark_inverse_surface),
        inversePrimary = ColorProvider(R.color.m3_sys_color_dynamic_dark_inverse_primary),
        inverseTextColorPrimary = ColorProvider(R.color.m3_sys_color_dynamic_dark_inverse_primary),
        inverseTextColorSecondary = ColorProvider(R.color.m3_sys_color_dynamic_dark_inverse_primary),
    )
}
