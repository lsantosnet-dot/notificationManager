package com.leo.painelnotificacoes.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One captured notification. [statusBarKey] is the system's StatusBarNotification#getKey(),
 * used to de-duplicate catch-up scans and to correlate onNotificationRemoved callbacks.
 * Deletion is soft ([isDeleted]) so the UI (backed by a Flow query) updates instantly while the
 * row is actually reclaimed by the next retention pass.
 */
@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["statusBarKey"], unique = true),
        Index(value = ["packageName"]),
        Index(value = ["timestamp"]),
        Index(value = ["isDeleted"]),
    ],
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "statusBarKey")
    val statusBarKey: String,
    val packageName: String,
    val appName: String,
    val appIconUri: String?,
    val title: String?,
    val text: String?,
    val timestamp: Long,
    val isDeleted: Boolean = false,
)
