package at.techbee.jtx.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_light_primary,
    secondary = md_theme_light_secondary,
    tertiary = md_theme_light_tertiary,

    /* Other default colors to override */
    background = md_theme_dark_background,
    surface = md_theme_dark_surface,
    onPrimary = md_theme_dark_onPrimary,
    onSecondary = md_theme_dark_onSecondary,
    onTertiary = md_theme_dark_onTertiary,
    onBackground = md_theme_dark_onBackground,
    onSurface = md_theme_dark_onSurface
)

private val LightColorScheme = lightColorScheme(
    primary = md_theme_dark_primary,
    secondary = md_theme_dark_secondary,
    tertiary = md_theme_dark_tertiary,

    /* Other default colors to override */
    background = md_theme_light_background,
    surface = md_theme_light_surface,
    onPrimary = md_theme_light_onPrimary,
    onSecondary = md_theme_dark_onSecondary,
    onTertiary = md_theme_light_onTertiary,
    onBackground = md_theme_light_onBackground,
    onSurface = md_theme_light_onSurface

)

@Composable
fun JtxBoardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    /*
    val view = LocalView.current
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