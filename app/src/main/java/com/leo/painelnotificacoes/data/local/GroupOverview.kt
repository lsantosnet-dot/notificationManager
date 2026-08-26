package com.leo.painelnotificacoes.data.local

/** One row of the Home screen's grouped-by-app list; produced by [NotificationDao.observeGroups]. */
data class GroupOverview(
    val packageName: String,
    val appName: String,
    val appIconUri: String?,
    val notificationCount: Int,
    val lastTimestamp: Long,
    val lastText: String?,
    val countLast24h: Int,
)
