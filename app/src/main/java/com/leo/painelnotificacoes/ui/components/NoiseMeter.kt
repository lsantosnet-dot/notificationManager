package com.leo.painelnotificacoes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.leo.painelnotificacoes.ui.theme.Accent
import com.leo.painelnotificacoes.ui.theme.Alert
import com.leo.painelnotificacoes.ui.theme.Divider
import com.leo.painelnotificacoes.ui.theme.TextFaint
import kotlin.math.roundToInt

/** Notifications-in-24h beyond this many read as "loud" (matches the prototype's ~0.6 threshold). */
private const val NOISE_ALERT_THRESHOLD = 0.6f

@Composable
fun NoiseMeter(ratio: Float, modifier: Modifier = Modifier) {
    val clamped = ratio.coerceIn(0f, 1f)
    val fillColor = if (clamped > NOISE_ALERT_THRESHOLD) Alert else Accent

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Divider),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(clamped)
                    .clip(RoundedCornerShape(99.dp))
                    .background(fillColor),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "VOLUME 24H", style = MaterialTheme.typography.labelSmall, color = TextFaint)
            Text(
                text = "${(clamped * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = TextFaint,
            )
        }
    }
}
