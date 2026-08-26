package com.leo.painelnotificacoes.util

import androidx.compose.ui.graphics.Color
import com.leo.painelnotificacoes.ui.theme.AvatarPalette
import kotlin.math.abs

/** Deterministic color per package name, so a given app always gets the same avatar color. */
fun avatarColorFor(packageName: String): Color =
    AvatarPalette[abs(packageName.hashCode()) % AvatarPalette.size]

/** "WhatsApp" -> "WA", "Slack" -> "SL", single-word short names -> first two letters. */
fun initialsFor(appName: String): String {
    val words = appName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    val initials = when {
        words.isEmpty() -> "?"
        words.size == 1 -> words[0].take(2)
        else -> "${words[0].first()}${words[1].first()}"
    }
    return initials.uppercase()
}
