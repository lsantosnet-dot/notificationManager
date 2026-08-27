package com.leo.painelnotificacoes.data.repository

import com.leo.painelnotificacoes.data.local.GroupOverview
import com.leo.painelnotificacoes.data.local.GroupSummaryDao
import com.leo.painelnotificacoes.data.local.GroupSummaryEntity
import com.leo.painelnotificacoes.data.local.NotificationDao
import com.leo.painelnotificacoes.data.local.NotificationEntity
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow

class NotificationRepository(
    private val notificationDao: NotificationDao,
    private val groupSummaryDao: GroupSummaryDao,
    private val settingsRepository: SettingsRepository,
) {
    fun observeGroups(): Flow<List<GroupOverview>> {
        val windowStart = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)
        return notificationDao.observeGroups(windowStart)
    }

    fun observeGroupItems(packageName: String): Flow<List<NotificationEntity>> =
        notificationDao.observeGroupItems(packageName)

    fun observeSummary(packageName: String): Flow<GroupSummaryEntity?> =
        groupSummaryDao.observeSummary(packageName)

    suspend fun captureNotification(entity: NotificationEntity) {
        val rowId = notificationDao.insert(entity)
        // Ignored duplicate (OnConflictStrategy.IGNORE) returns -1; only a real insert counts.
        if (rowId != -1L) {
            settingsRepository.recordFirstCaptureIfEarlier(entity.timestamp)
        }
    }

    /** Keys already stored, used to skip duplicates during onListenerConnected catch-up. */
    suspend fun knownActiveKeys(): Set<String> = notificationDao.getAllActiveKeys().toSet()

    suspend fun deleteNotification(id: Long) {
        notificationDao.softDeleteById(id)
        notificationDao.purgeSoftDeleted()
    }

    suspend fun deleteGroup(packageName: String) {
        notificationDao.softDeleteByPackage(packageName)
        notificationDao.purgeSoftDeleted()
        groupSummaryDao.deleteForPackage(packageName)
    }

    suspend fun deleteAllNotifications() {
        notificationDao.deleteAll()
        groupSummaryDao.deleteAll()
    }

    suspend fun saveSummary(summary: GroupSummaryEntity) {
        groupSummaryDao.upsert(summary)
    }

    suspend fun purgeOlderThan(cutoffMillis: Long) {
        notificationDao.purgeOlderThan(cutoffMillis)
        notificationDao.purgeSoftDeleted()
    }
}
