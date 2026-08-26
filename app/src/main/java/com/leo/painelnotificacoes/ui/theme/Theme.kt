package com.leo.painelnotificacoes.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// The app is designed dark-only, matching the prototype (no light theme variant).
private val PainelDarkColorScheme = darkColorScheme(
    background = Background,
    surface = Surface,
    surfaceVariant = Surface2,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextDim,
    primary = Accent,
    onPrimary = Background,
    primaryContainer = AccentDim,
    onPrimaryContainer = Accent,
    secondary = Alert,
    secondaryContainer = AlertDim,
    error = Danger,
    onError = TextPrimary,
    outline = Divider,
)

@Composable
fun PainelNotificacoesTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    // Content is drawn edge-to-edge (see MainActivity); this only sets status/nav bar icon
    // contrast, since the bars themselves are transparent and the app's own background shows
    // through.
    if (!view.isInEditMode) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = PainelDarkColorScheme,
        typography = Typography,
        content = content,
    )
}
