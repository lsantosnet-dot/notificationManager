package com.leo.painelnotificacoes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leo.painelnotificacoes.data.local.GroupOverview
import com.leo.painelnotificacoes.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

enum class HomeSortOption {
    RECENT,
    NAME,
    COUNT,
}

class HomeViewModel(private val repository: NotificationRepository) : ViewModel() {

    private val sortOption = MutableStateFlow(HomeSortOption.RECENT)

    val groups: StateFlow<List<HomeGroupUi>> = combine(
        repository.observeGroups(),
        sortOption,
    ) { overviews, sort ->
        overviews.map { it.toUi() }.sortedWith(sort.comparator())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentSortOption: StateFlow<HomeSortOption> = sortOption

    fun setSortOption(option: HomeSortOption) {
        sortOption.value = option
    }

    fun deleteGroup(packageName: String) {
        viewModelScope.launch { repository.deleteGroup(packageName) }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch { repository.deleteAllNotifications() }
    }

    private fun HomeSortOption.comparator(): Comparator<HomeGroupUi> = when (this) {
        HomeSortOption.RECENT -> compareByDescending { it.lastTimestamp }
        HomeSortOption.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.appName }
        HomeSortOption.COUNT -> compareByDescending { it.notificationCount }
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
