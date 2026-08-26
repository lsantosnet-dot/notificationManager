package com.leo.painelnotificacoes.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leo.painelnotificacoes.data.local.NotificationEntity
import com.leo.painelnotificacoes.ui.theme.Accent
import com.leo.painelnotificacoes.ui.theme.AccentDim
import com.leo.painelnotificacoes.ui.theme.Danger
import com.leo.painelnotificacoes.ui.theme.Divider
import com.leo.painelnotificacoes.ui.theme.Surface
import com.leo.painelnotificacoes.ui.theme.Surface2
import com.leo.painelnotificacoes.ui.theme.TextDim
import com.leo.painelnotificacoes.ui.theme.TextFaint
import com.leo.painelnotificacoes.ui.theme.TextPrimary
import com.leo.painelnotificacoes.util.formatRelativeTime

@Composable
fun GroupDetailScreen(
    viewModel: GroupDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val summaryState by viewModel.summaryCardState.collectAsStateWithLifecycle()
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        DetailAppBar(
            appName = items.firstOrNull()?.appName ?: viewModel.initialAppName,
            count = items.size,
            onBack = onBack,
            onDeleteAllClick = { showDeleteAllDialog = true },
        )

        SummaryCard(
            state = summaryState,
            onGenerateClick = viewModel::generateSummary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        if (items.isNotEmpty()) {
            Text(
                text = "Arraste um item para a esquerda ou toque no × para excluir",
                style = MaterialTheme.typography.bodySmall,
                color = TextFaint,
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                textAlign = TextAlign.Center,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items, key = { it.id }) { item ->
                NotificationItemRow(
                    item = item,
                    onDelete = { viewModel.deleteNotification(item.id) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Excluir tudo?") },
            text = { Text("Todas as notificações deste app serão excluídas permanentemente.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteAllDialog = false
                    viewModel.deleteGroup()
                    onBack()
                }) {
                    Text("Excluir", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun DetailAppBar(
    appName: String,
    count: Int,
    onBack: () -> Unit,
    onDeleteAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Surface)
                .border(1.dp, Divider, RoundedCornerShape(12.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Voltar",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = appName,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "$count notificações",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim,
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Danger.copy(alpha = 0.1f))
                .border(1.dp, Danger.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .clickable(onClick = onDeleteAllClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(text = "Excluir tudo", style = MaterialTheme.typography.bodySmall, color = Danger, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SummaryCard(
    state: SummaryCardUiState,
    onGenerateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(colors = listOf(AccentDim, Surface)))
            .border(1.dp, Accent.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = "RESUMO · GEMINI NANO (ON-DEVICE)",
                style = MaterialTheme.typography.labelSmall,
                color = Accent,
                fontWeight = FontWeight.Bold,
            )
        }

        Box(modifier = Modifier.padding(top = 10.dp)) {
            when (state) {
                SummaryCardUiState.CheckingAvailability -> Text(
                    text = "Verificando disponibilidade do modelo…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDim,
                )

                SummaryCardUiState.Unavailable -> Text(
                    text = "O resumo por IA local não está disponível neste dispositivo. " +
                        "Esse recurso depende do modelo Gemini Nano (ML Kit GenAI), presente apenas " +
                        "em alguns aparelhos compatíveis.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDim,
                )

                SummaryCardUiState.ReadyToSummarize -> SummaryCta(
                    label = "Resumir grupo com IA local",
                    onClick = onGenerateClick,
                )

                SummaryCardUiState.Processing -> SummaryCta(
                    label = "Processando no dispositivo…",
                    onClick = {},
                    loading = true,
                )

                is SummaryCardUiState.Error -> Column {
                    Text(text = state.message, style = MaterialTheme.typography.bodyMedium, color = Danger)
                    SummaryCta(
                        label = "Tentar novamente",
                        onClick = onGenerateClick,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }

                is SummaryCardUiState.Summarized -> Column {
                    Text(text = state.text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    if (state.stale) {
                        Text(
                            text = "Chegaram notificações novas desde este resumo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextFaint,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        SummaryCta(
                            label = "Atualizar resumo",
                            onClick = onGenerateClick,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCta(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Accent.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable(enabled = !loading, onClick = onClick)
            .padding(13.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Accent, strokeWidth = 2.dp)
        } else {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Accent,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationItemRow(
    item: NotificationEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
            }
            true
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Danger),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = "Excluir",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(end = 20.dp),
                )
            }
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Surface)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Accent),
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = item.title?.takeIf { it.isNotBlank() } ?: item.appName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Text(
                        text = formatRelativeTime(item.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextFaint,
                    )
                }
                if (!item.text.isNullOrBlank()) {
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDim,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Surface2)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Excluir",
                    tint = TextFaint,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}
