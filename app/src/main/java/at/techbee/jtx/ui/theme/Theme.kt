package at.techbee.jtx.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
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
    onSurfaceVariant = md_theme_dark_onSurfaceVariant
)

private val TrueDarkColorScheme = darkColorScheme(
        primary = DarkColorScheme.primary,
        secondary = DarkColorScheme.secondary,
        tertiary = DarkColorScheme.tertiary,

        /* Other default colors to override */
        background = Color.Black,
        surface = Color.Black,
        onPrimary = DarkColorScheme.onPrimary,
        primaryContainer = DarkColorScheme.primaryContainer,
        onPrimaryContainer = DarkColorScheme.onPrimaryContainer,
        onSecondary = DarkColorScheme.onSecondary,
        secondaryContainer = DarkColorScheme.secondaryContainer,
        onSecondaryContainer = DarkColorScheme.onSecondaryContainer,
        onTertiary = DarkColorScheme.onTertiary,
        onBackground = DarkColorScheme.onBackground,
        onSurface = DarkColorScheme.onSurface,
        surfaceVariant = DarkColorScheme.surfaceVariant,
        onSurfaceVariant = DarkColorScheme.onSurfaceVariant
    )


private val LightColorScheme = lightColorScheme(
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
    onSurfaceVariant = md_theme_light_onSurfaceVariant
)

@Composable
fun JtxBoardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Dynamic color is available on Android 12+ and only if purchased
    trueDarkTheme: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val activity  = view.context as Activity
    val context = LocalContext.current

    val colorScheme = when {
        // dynamic colors are only loaded in pro!
        trueDarkTheme -> TrueDarkColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
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