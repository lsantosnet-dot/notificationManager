package com.leo.painelnotificacoes.util

import java.util.concurrent.TimeUnit

/** Short relative-time labels matching the prototype's style ("2 min", "1 h", "3 d"). */
fun formatRelativeTime(timestampMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
    val diff = (nowMillis - timestampMillis).coerceAtLeast(0)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        minutes < 1 -> "agora"
        minutes < 60 -> "$minutes min"
        hours < 24 -> "$hours h"
        days < 7 -> "$days d"
        else -> "${days / 7} sem"
    }
}
