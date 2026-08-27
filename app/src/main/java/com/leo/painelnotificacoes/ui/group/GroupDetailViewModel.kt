package com.leo.painelnotificacoes.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leo.painelnotificacoes.data.ai.GeminiCloudSummarizer
import com.leo.painelnotificacoes.data.ai.SummarizationManager
import com.leo.painelnotificacoes.data.ai.SummarizationUnavailableException
import com.leo.painelnotificacoes.data.local.GroupSummaryEntity
import com.leo.painelnotificacoes.data.local.NotificationEntity
import com.leo.painelnotificacoes.data.repository.NotificationRepository
import com.leo.painelnotificacoes.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SummaryCardUiState {
    data object CheckingAvailability : SummaryCardUiState
    data object Unavailable : SummaryCardUiState
    data object ReadyToSummarize : SummaryCardUiState
    data object Processing : SummaryCardUiState
    data class Summarized(val text: String, val stale: Boolean) : SummaryCardUiState
    data class Error(val message: String) : SummaryCardUiState
}

class GroupDetailViewModel(
    val packageName: String,
    val initialAppName: String,
    private val repository: NotificationRepository,
    private val settingsRepository: SettingsRepository,
    private val summarizationManager: SummarizationManager,
    private val geminiCloudSummarizer: GeminiCloudSummarizer,
) : ViewModel() {

    val items: StateFlow<List<NotificationEntity>> = repository.observeGroupItems(packageName)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val summarizing = MutableStateFlow(false)
    private val summarizationError = MutableStateFlow<String?>(null)
    private val deviceAiAvailable = MutableStateFlow<Boolean?>(null)

    private data class EngineAvailability(val deviceAvailable: Boolean?, val cloudApiKey: String?)

    private val engineAvailability: Flow<EngineAvailability> = combine(
        deviceAiAvailable,
        settingsRepository.geminiApiKey,
    ) { deviceAvailable, cloudApiKey -> EngineAvailability(deviceAvailable, cloudApiKey) }

    /** True once we know the on-device summarizer is unavailable and a cloud fallback will be used instead. */
    val usingCloudEngine: StateFlow<Boolean> = engineAvailability
        .map { it.deviceAvailable == false && !it.cloudApiKey.isNullOrBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val summaryCardState: StateFlow<SummaryCardUiState> = combine(
        items,
        repository.observeSummary(packageName),
        summarizing,
        summarizationError,
        engineAvailability,
    ) { currentItems, summary, isSummarizing, error, availability ->
        when {
            isSummarizing -> SummaryCardUiState.Processing
            error != null -> SummaryCardUiState.Error(error)
            availability.deviceAvailable == null -> SummaryCardUiState.CheckingAvailability
            availability.deviceAvailable == false && availability.cloudApiKey.isNullOrBlank() -> SummaryCardUiState.Unavailable
            summary != null -> {
                val stale = currentItems.size > summary.notificationCountAtGeneration
                SummaryCardUiState.Summarized(summary.summaryText, stale)
            }
            else -> SummaryCardUiState.ReadyToSummarize
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SummaryCardUiState.CheckingAvailability)

    init {
        viewModelScope.launch {
            deviceAiAvailable.value = summarizationManager.isFeaturePossible()
        }
    }

    fun generateSummary() {
        val currentItems = items.value
        if (currentItems.isEmpty() || summarizing.value) return
        viewModelScope.launch {
            summarizationError.value = null
            summarizing.value = true

            val result = if (deviceAiAvailable.value == true) {
                summarizationManager.summarizeGroup(currentItems)
            } else {
                val apiKey = settingsRepository.geminiApiKey.first()
                if (apiKey.isNullOrBlank()) {
                    summarizing.value = false
                    summarizationError.value = "Configure uma chave de API do Gemini em Ajustes"
                    return@launch
                }
                geminiCloudSummarizer.summarize(apiKey, currentItems)
            }

            result
                .onSuccess { summaryText ->
                    repository.saveSummary(
                        GroupSummaryEntity(
                            packageName = packageName,
                            summaryText = summaryText,
                            generatedAt = System.currentTimeMillis(),
                            notificationCountAtGeneration = currentItems.size,
                        )
                    )
                }
                .onFailure { error ->
                    if (error is SummarizationUnavailableException) {
                        deviceAiAvailable.value = false
                    } else {
                        summarizationError.value = error.message ?: "Não foi possível gerar o resumo"
                    }
                }
            summarizing.value = false
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch { repository.deleteNotification(id) }
    }

    fun deleteGroup() {
        viewModelScope.launch { repository.deleteGroup(packageName) }
    }
}
