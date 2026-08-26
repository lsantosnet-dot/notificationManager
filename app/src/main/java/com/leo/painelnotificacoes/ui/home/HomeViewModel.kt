package com.leo.painelnotificacoes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leo.painelnotificacoes.data.local.GroupOverview
import com.leo.painelnotificacoes.data.repository.NotificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeGroupUi(
    val packageName: String,
    val appName: String,
    val appIconUri: String?,
    val notificationCount: Int,
    val lastTimestamp: Long,
    val lastText: String,
    val noiseRatio: Float,
)

class HomeViewModel(private val repository: NotificationRepository) : ViewModel() {

    val groups: StateFlow<List<HomeGroupUi>> = repository.observeGroups()
        .map { overviews -> overviews.map { it.toUi() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteGroup(packageName: String) {
        viewModelScope.launch { repository.deleteGroup(packageName) }
    }

    private fun GroupOverview.toUi() = HomeGroupUi(
        packageName = packageName,
        appName = appName,
        appIconUri = appIconUri,
        notificationCount = notificationCount,
        lastTimestamp = lastTimestamp,
        lastText = lastText.orEmpty(),
        noiseRatio = (countLast24h / NOISE_CAP_PER_24H).coerceIn(0f, 1f),
    )

    private companion object {
        // Apps posting this many (or more) notifications in 24h read as "loud" (100% on the meter).
        const val NOISE_CAP_PER_24H = 15f
    }
}
