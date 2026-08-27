package com.leo.painelnotificacoes.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leo.painelnotificacoes.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val retentionDays: StateFlow<Int> = settingsRepository.retentionDays
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SettingsRepository.DEFAULT_RETENTION_DAYS,
        )

    val geminiApiKey: StateFlow<String?> = settingsRepository.geminiApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setRetentionDays(days: Int) {
        viewModelScope.launch { settingsRepository.setRetentionDays(days) }
    }

    fun setGeminiApiKey(key: String) {
        viewModelScope.launch { settingsRepository.setGeminiApiKey(key) }
    }
}
