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
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leo.painelnotificacoes.ui.theme.Accent
import com.leo.painelnotificacoes.ui.theme.Background
import com.leo.painelnotificacoes.ui.theme.Divider
import com.leo.painelnotificacoes.ui.theme.Surface
import com.leo.painelnotificacoes.ui.theme.TextDim
import com.leo.painelnotificacoes.ui.theme.TextFaint
import com.leo.painelnotificacoes.ui.theme.TextPrimary

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val retentionDays by viewModel.retentionDays.collectAsStateWithLifecycle()
    val storedApiKey by viewModel.geminiApiKey.collectAsStateWithLifecycle()

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

        GeminiApiKeyCard(
            storedApiKey = storedApiKey,
            onSave = viewModel::setGeminiApiKey,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun GeminiApiKeyCard(
    storedApiKey: String?,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var apiKeyInput by remember { mutableStateOf("") }
    var seeded by remember { mutableStateOf(false) }
    var keyVisible by remember { mutableStateOf(false) }

    LaunchedEffect(storedApiKey) {
        if (!seeded) {
            apiKeyInput = storedApiKey.orEmpty()
            seeded = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .padding(16.dp),
    ) {
        Text(
            text = "Resumo por IA na nuvem (Gemini)",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
        )
        Text(
            text = "Usado como alternativa quando o resumo local (Gemini Nano) não está disponível " +
                "neste aparelho. Ao configurar uma chave, o texto das notificações passa a ser " +
                "enviado ao Google para gerar o resumo. Obtenha uma chave gratuita em " +
                "aistudio.google.com/apikey.",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        OutlinedTextField(
            value = apiKeyInput,
            onValueChange = { apiKeyInput = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Chave de API do Gemini") },
            singleLine = true,
            visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { keyVisible = !keyVisible }) {
                    Icon(
                        imageVector = if (keyVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (keyVisible) "Ocultar chave" else "Mostrar chave",
                        tint = TextDim,
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent,
                unfocusedBorderColor = Divider,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = Accent,
            ),
        )
        Button(
            onClick = { onSave(apiKeyInput) },
            modifier = Modifier.padding(top = 10.dp),
            enabled = apiKeyInput != storedApiKey.orEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
        ) {
            Text("Salvar chave")
        }
        if (!storedApiKey.isNullOrBlank()) {
            Text(
                text = "Chave configurada.",
                style = MaterialTheme.typography.bodySmall,
                color = TextFaint,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
