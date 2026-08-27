package com.leo.painelnotificacoes.data.ai

import com.leo.painelnotificacoes.data.local.NotificationEntity

// Shared by both the on-device (ML Kit GenAI) and cloud (Gemini API) summarizers; stay
// comfortably under typical model input limits and prefer chunking by recency over a hard
// mid-notification cut.
private const val MAX_INPUT_CHARS = 4000

/**
 * Builds summarizer input text, keeping the most recent notifications (they matter most) and
 * truncating older ones to stay under [MAX_INPUT_CHARS].
 */
fun buildNotificationSummaryInput(notifications: List<NotificationEntity>): String {
    val chronological = notifications.sortedBy { it.timestamp }
    val lines = ArrayDeque<String>()
    var budget = MAX_INPUT_CHARS
    for (notification in chronological.asReversed()) {
        val sender = notification.title?.takeIf { it.isNotBlank() } ?: notification.appName
        val line = "$sender: ${notification.text.orEmpty()}"
        val cost = line.length + 1
        if (budget - cost < 0 && lines.isNotEmpty()) break
        lines.addFirst(line)
        budget -= cost
    }
    return lines.joinToString("\n")
}
