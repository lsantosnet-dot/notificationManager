package com.leo.painelnotificacoes.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

/** Full absolute date + time, e.g. "27/08/2026 às 11:14". SimpleDateFormat isn't thread-safe, so a fresh instance per call. */
fun formatFullDateTime(timestampMillis: Long): String =
    SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")).format(Date(timestampMillis))
