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

private val darkColorScheme = darkColorScheme(
    primary = primaryDark,
    secondary = secondaryDark,
    tertiary = tertiaryDark,

    /* Other default colors to override */
    background = backgroundDark,
    surface = surfaceDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiary = onTertiaryDark,
    onTertiaryContainer = onTertiaryContainerDark,
    onBackground = onBackgroundDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    inverseOnSurface = inverseOnSurfaceDark,

    inversePrimary = inversePrimaryDark,
    surfaceTint = surfaceVariantDark,
    inverseSurface = inverseSurfaceDark,
    error = errorDark,
    errorContainer = errorContainerDark,
    onError = onErrorDark,
    onErrorContainer = onErrorContainerDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark
)

private val lightColorScheme = lightColorScheme(
    primary = primaryLight,
    secondary = secondaryLight,
    tertiary = tertiaryLight,

    /* Other default colors to override */
    background = backgroundLight,
    surface = surfaceLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiary = onTertiaryLight,
    onTertiaryContainer = onTertiaryContainerLight,
    onBackground = onBackgroundLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    inverseOnSurface = inverseOnSurfaceLight,

    inversePrimary = inversePrimaryLight,
    surfaceTint = surfaceVariantLight,
    inverseSurface = inverseSurfaceLight,
    error = errorLight,
    errorContainer = errorContainerLight,
    onError = onErrorLight,
    onErrorContainer = onErrorContainerLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight
)

private val contrastColorScheme = lightColorScheme(
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
    secondaryContainer = Color(0xFFFAFAFA),
    onSecondaryContainer = Color.DarkGray,
    onTertiary = Color.White,
    onBackground = Color.Black,
    surfaceVariant = Color(0xFFFAFAFA),
    onSurfaceVariant = Color.Black,
    inverseOnSurface = Color.White,

    inversePrimary = Color.White,
    surfaceTint = Color.White,
    inverseSurface = Color.Black,
    error = Color.Red,
    onError = Color.White,
    onErrorContainer = Color.White,
    outline = Color.Black,

    outlineVariant = Color.DarkGray,
    scrim = Color.DarkGray,
    surfaceBright = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    surfaceDim = Color.White
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
        contrastTheme -> contrastColorScheme
        trueDarkTheme && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicDarkColorScheme(context).copy(background = Color.Black, surface = Color.Black)
        trueDarkTheme -> darkColorScheme.copy(background = Color.Black, surface = Color.Black)
        darkTheme && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicDarkColorScheme(context)
        !darkTheme && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        darkTheme -> darkColorScheme
        else -> lightColorScheme
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