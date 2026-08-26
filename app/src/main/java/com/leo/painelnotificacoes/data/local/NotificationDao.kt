package com.leo.painelnotificacoes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    /**
     * [statusBarKey] carries a unique index, so re-inserting an already-known notification
     * (e.g. during onListenerConnected catch-up) is silently ignored.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(notification: NotificationEntity): Long

    @Query(
        """
        SELECT
          n.packageName AS packageName,
          (SELECT appName FROM notifications WHERE packageName = n.packageName AND isDeleted = 0 ORDER BY timestamp DESC LIMIT 1) AS appName,
          (SELECT appIconUri FROM notifications WHERE packageName = n.packageName AND isDeleted = 0 ORDER BY timestamp DESC LIMIT 1) AS appIconUri,
          COUNT(*) AS notificationCount,
          MAX(n.timestamp) AS lastTimestamp,
          (SELECT text FROM notifications WHERE packageName = n.packageName AND isDeleted = 0 ORDER BY timestamp DESC LIMIT 1) AS lastText,
          SUM(CASE WHEN n.timestamp >= :windowStart THEN 1 ELSE 0 END) AS countLast24h
        FROM notifications n
        WHERE n.isDeleted = 0
        GROUP BY n.packageName
        ORDER BY lastTimestamp DESC
        """
    )
    fun observeGroups(windowStart: Long): Flow<List<GroupOverview>>

    @Query(
        "SELECT * FROM notifications WHERE packageName = :packageName AND isDeleted = 0 ORDER BY timestamp DESC"
    )
    fun observeGroupItems(packageName: String): Flow<List<NotificationEntity>>

    @Query("SELECT statusBarKey FROM notifications WHERE isDeleted = 0")
    suspend fun getAllActiveKeys(): List<String>

    @Query("UPDATE notifications SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteById(id: Long)

    @Query("UPDATE notifications SET isDeleted = 1 WHERE packageName = :packageName")
    suspend fun softDeleteByPackage(packageName: String)

    @Query("DELETE FROM notifications WHERE isDeleted = 1")
    suspend fun purgeSoftDeleted()

    @Query("DELETE FROM notifications WHERE timestamp < :cutoffMillis")
    suspend fun purgeOlderThan(cutoffMillis: Long)
}
