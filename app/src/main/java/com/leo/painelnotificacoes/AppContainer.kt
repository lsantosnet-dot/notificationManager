package com.leo.painelnotificacoes

import android.content.Context
import com.leo.painelnotificacoes.data.ai.SummarizationManager
import com.leo.painelnotificacoes.data.local.AppDatabase
import com.leo.painelnotificacoes.data.repository.NotificationRepository
import com.leo.painelnotificacoes.data.repository.SettingsRepository

/** Simple hand-rolled dependency container; the app is small enough not to need a DI framework. */
class AppContainer(context: Context) {

    private val database = AppDatabase.getInstance(context)

    val notificationRepository = NotificationRepository(
        notificationDao = database.notificationDao(),
        groupSummaryDao = database.groupSummaryDao(),
    )

    val settingsRepository = SettingsRepository(context)

    val summarizationManager = SummarizationManager(context)
}
