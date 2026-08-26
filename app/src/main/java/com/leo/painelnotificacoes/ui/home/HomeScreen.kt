package com.leo.painelnotificacoes.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.leo.painelnotificacoes.ui.components.GroupAvatar
import com.leo.painelnotificacoes.ui.components.NoiseMeter
import com.leo.painelnotificacoes.ui.theme.Background
import com.leo.painelnotificacoes.ui.theme.Surface
import com.leo.painelnotificacoes.ui.theme.Surface2
import com.leo.painelnotificacoes.ui.theme.TextDim
import com.leo.painelnotificacoes.ui.theme.TextFaint
import com.leo.painelnotificacoes.ui.theme.TextPrimary
import com.leo.painelnotificacoes.util.formatRelativeTime

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onGroupClick: (packageName: String, appName: String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    HomeScreenContent(
        groups = groups,
        onGroupClick = onGroupClick,
        onSettingsClick = onSettingsClick,
        modifier = modifier,
    )
}

@Composable
private fun HomeScreenContent(
    groups: List<HomeGroupUi>,
    onGroupClick: (packageName: String, appName: String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().background(Background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 6.dp, bottom = 8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val totalNotifications = groups.sumOf { it.notificationCount }
                Text(
                    text = "${groups.size} apps · $totalNotifications notificações",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Notificações",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = "Agrupadas por app · resumo com IA local",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDim,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Ajustes", tint = TextDim)
            }
        }

        if (groups.isEmpty()) {
            EmptyGroupsState(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(groups, key = { it.packageName }) { group ->
                    GroupCard(
                        group = group,
                        onClick = { onGroupClick(group.packageName, group.appName) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupCard(group: HomeGroupUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GroupAvatar(appName = group.appName, packageName = group.packageName, iconUri = group.appIconUri)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = group.appName,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Text(
                        text = formatRelativeTime(group.lastTimestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextFaint,
                    )
                }
                Text(
                    text = group.lastText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Badge(count = group.notificationCount)
        }
        NoiseMeter(ratio = group.noiseRatio)
    }
}

@Composable
private fun Badge(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Surface2)
            .padding(horizontal = 9.dp, vertical = 2.dp),
    ) {
        Text(text = count.toString(), style = MaterialTheme.typography.labelLarge, color = TextPrimary)
    }
}

@Composable
private fun EmptyGroupsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Nenhuma notificação capturada ainda",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
        )
        Text(
            text = "Conceda acesso a notificações e elas aparecerão aqui, agrupadas por app.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextDim,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
