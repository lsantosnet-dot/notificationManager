package com.leo.painelnotificacoes.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leo.painelnotificacoes.data.ai.SummarizationManager
import com.leo.painelnotificacoes.data.ai.SummarizationUnavailableException
import com.leo.painelnotificacoes.data.local.GroupSummaryEntity
import com.leo.painelnotificacoes.data.local.NotificationEntity
import com.leo.painelnotificacoes.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    private val summarizationManager: SummarizationManager,
) : ViewModel() {

    val items: StateFlow<List<NotificationEntity>> = repository.observeGroupItems(packageName)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val summarizing = MutableStateFlow(false)
    private val summarizationError = MutableStateFlow<String?>(null)
    private val aiAvailable = MutableStateFlow<Boolean?>(null)

    val summaryCardState: StateFlow<SummaryCardUiState> = combine(
        items,
        repository.observeSummary(packageName),
        summarizing,
        summarizationError,
        aiAvailable,
    ) { currentItems, summary, isSummarizing, error, available ->
        when {
            isSummarizing -> SummaryCardUiState.Processing
            error != null -> SummaryCardUiState.Error(error)
            available == false -> SummaryCardUiState.Unavailable
            available == null -> SummaryCardUiState.CheckingAvailability
            summary != null -> {
                val stale = currentItems.size > summary.notificationCountAtGeneration
                SummaryCardUiState.Summarized(summary.summaryText, stale)
            }
            else -> SummaryCardUiState.ReadyToSummarize
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SummaryCardUiState.CheckingAvailability)

    init {
        viewModelScope.launch {
            aiAvailable.value = summarizationManager.isFeaturePossible()
        }
    }

    fun generateSummary() {
        val currentItems = items.value
        if (currentItems.isEmpty() || summarizing.value) return
        viewModelScope.launch {
            summarizationError.value = null
            summarizing.value = true
            summarizationManager.summarizeGroup(currentItems)
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
                        aiAvailable.value = false
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
