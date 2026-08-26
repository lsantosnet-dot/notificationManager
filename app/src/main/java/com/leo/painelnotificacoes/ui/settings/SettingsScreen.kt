package com.leo.painelnotificacoes.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leo.painelnotificacoes.ui.theme.Accent
import com.leo.painelnotificacoes.ui.theme.Background
import com.leo.painelnotificacoes.ui.theme.Divider
import com.leo.painelnotificacoes.ui.theme.Surface
import com.leo.painelnotificacoes.ui.theme.TextDim
import com.leo.painelnotificacoes.ui.theme.TextPrimary

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val retentionDays by viewModel.retentionDays.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().background(Background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
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
            Text(text = "Ajustes", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Surface)
                .padding(16.dp),
        ) {
            Text(
                text = "Manter notificações por $retentionDays dias",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
            )
            Text(
                text = "Notificações mais antigas que isso são excluídas automaticamente.",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
            Slider(
                value = retentionDays.toFloat(),
                onValueChange = { viewModel.setRetentionDays(it.toInt()) },
                valueRange = 1f..90f,
                steps = 88,
                colors = SliderDefaults.colors(
                    thumbColor = Accent,
                    activeTrackColor = Accent,
                    inactiveTrackColor = Divider,
                ),
            )
        }
    }
}
