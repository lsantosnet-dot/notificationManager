package com.leo.painelnotificacoes.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached AI summary for one app's notification group. [notificationCountAtGeneration] lets the UI
 * detect staleness: if the group's current (non-deleted) count differs, the summary is outdated.
 */
@Entity(tableName = "group_summaries")
data class GroupSummaryEntity(
    @PrimaryKey
    val packageName: String,
    val summaryText: String,
    val generatedAt: Long,
    val notificationCountAtGeneration: Int,
)
