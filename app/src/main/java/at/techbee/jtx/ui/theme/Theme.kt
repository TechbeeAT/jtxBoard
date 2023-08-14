package at.techbee.jtx.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import at.techbee.jtx.util.UiUtil

private val _darkColorScheme = darkColorScheme(
    primary = md_theme_light_primary,
    secondary = md_theme_light_secondary,
    tertiary = md_theme_light_tertiary,

    /* Other default colors to override */
    background = md_theme_dark_background,
    surface = md_theme_dark_surface,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    onTertiary = md_theme_dark_onTertiary,
    onBackground = md_theme_dark_onBackground,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    inverseOnSurface = md_theme_dark_inverseOnSurface
)

private val _lightColorScheme = lightColorScheme(
    primary = md_theme_dark_primary,
    secondary = md_theme_dark_secondary,
    tertiary = md_theme_dark_tertiary,

    /* Other default colors to override */
    background = md_theme_light_background,
    surface = md_theme_light_surface,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    onTertiary = md_theme_light_onTertiary,
    onBackground = md_theme_light_onBackground,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    inverseOnSurface = md_theme_light_inverseOnSurface
)

private val _contrastColorScheme = lightColorScheme(
    primary = Color.Black,
    secondary = Color(0xFF272727),
    tertiary = Color(0xFF505050),

    /* Other default colors to override */
    background = Color.White,
    surface = Color.White,
    onSurface = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color.White,
    onPrimaryContainer = Color.Black,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF272727),
    onSecondaryContainer = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    surfaceVariant = Color(0xFFD1D1D1),
    onSurfaceVariant = Color.Black,
    inverseOnSurface = Color.White
)

/**
 * This extension function returns a surface color that has enough contrast to be
 * readable on the given background color
 * @param backgroundColor for which an appropriate content color should be returned
 * @return [Color] either the current onSurface or the current inverseOnSurface, whatever is more appropriate for good contrast
 */
fun ColorScheme.getContrastSurfaceColorFor(backgroundColor: Color): Color {
    return when {
        UiUtil.isDarkColor(backgroundColor) && UiUtil.isDarkColor(onSurface) -> inverseOnSurface
        UiUtil.isDarkColor(backgroundColor) && !UiUtil.isDarkColor(onSurface) -> onSurface
        !UiUtil.isDarkColor(backgroundColor) && UiUtil.isDarkColor(onSurface) -> onSurface
        !UiUtil.isDarkColor(backgroundColor) && !UiUtil.isDarkColor(onSurface) -> inverseOnSurface
        else -> onSurface
    }
}

@Composable
fun JtxBoardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Dynamic color is available on Android 12+ and only if purchased
    trueDarkTheme: Boolean = false,
    contrastTheme: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val activity  = view.context as Activity
    val context = LocalContext.current

    // dynamic colors are only loaded in pro!
    val colorScheme = when {
        contrastTheme -> _contrastColorScheme
        trueDarkTheme && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicDarkColorScheme(context).copy(background = Color.Black, surface = Color.Black)
        trueDarkTheme -> _darkColorScheme.copy(background = Color.Black, surface = Color.Black)
        darkTheme && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicDarkColorScheme(context)
        !darkTheme && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        darkTheme -> _darkColorScheme
        else -> _lightColorScheme
    }

    SideEffect {
        if (!view.isInEditMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.window.navigationBarColor = colorScheme.primary.copy(alpha = 0.08f).compositeOver(colorScheme.surface.copy()).toArgb()
            activity.window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(activity.window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    /* DEFAULT
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.primary.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = darkTheme
        }
    }
     */

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}