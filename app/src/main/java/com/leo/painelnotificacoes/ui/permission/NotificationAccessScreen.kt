package com.leo.painelnotificacoes.ui.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.leo.painelnotificacoes.ui.theme.Accent
import com.leo.painelnotificacoes.ui.theme.Background
import com.leo.painelnotificacoes.ui.theme.TextDim
import com.leo.painelnotificacoes.ui.theme.TextPrimary

@Composable
fun NotificationAccessScreen(onOpenSettingsClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Acesso às notificações",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Para agrupar suas notificações e gerar resumos, o Painel de Notificações " +
                "precisa da permissão especial de acesso a notificações. Todo o processamento " +
                "acontece no aparelho — nenhum dado sai do seu celular.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextDim,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, bottom = 28.dp),
        )
        Button(
            onClick = onOpenSettingsClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Background),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Abrir configurações de notificação",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
        Text(
            text = "Painel de Notificações > permitir acesso",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}
