package com.leo.painelnotificacoes.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.leo.painelnotificacoes.PainelNotificacoesApp
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

/** Periodically purges notifications older than the user-configured retention window. */
class RetentionCleanupWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as PainelNotificacoesApp).container
        val retentionDays = container.settingsRepository.retentionDays.first()
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(retentionDays.toLong())
        container.notificationRepository.purgeOlderThan(cutoff)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "retention_cleanup"
    }
}
