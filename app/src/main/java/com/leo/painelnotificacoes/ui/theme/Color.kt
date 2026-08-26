package com.leo.painelnotificacoes.ui.theme

import androidx.compose.ui.graphics.Color

// Mirrors the HTML prototype's palette 1:1.
val Background = Color(0xFF12151A)
val Surface = Color(0xFF1C2029)
val Surface2 = Color(0xFF232833)
val Divider = Color(0xFF262B35)

val TextPrimary = Color(0xFFEDEFF2)
val TextDim = Color(0xFF8B93A1)
val TextFaint = Color(0xFF5B6270)

val Accent = Color(0xFF4FD1C5)
val AccentDim = Color(0xFF2E4A47)
val Alert = Color(0xFFF2A65A)
val AlertDim = Color(0xFF4A3B28)
val Danger = Color(0xFFE2685E)

/** Deterministic accent colors for group avatars, cycling by app package hash. */
val AvatarPalette = listOf(
    Color(0xFF3FBF7F),
    Color(0xFFE2685E),
    Color(0xFF9B7EDE),
    Color(0xFFD4739A),
    Color(0xFF4FD1C5),
    Color(0xFFF2A65A),
    Color(0xFF5B9BD5),
    Color(0xFFC9A227),
)
