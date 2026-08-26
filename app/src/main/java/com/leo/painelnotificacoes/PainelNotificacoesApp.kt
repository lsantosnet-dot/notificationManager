package com.leo.painelnotificacoes

import android.app.Application
import com.leo.painelnotificacoes.work.WorkScheduler

class PainelNotificacoesApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        WorkScheduler.scheduleRetentionCleanup(this)
    }
}
