package com.leo.painelnotificacoes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.leo.painelnotificacoes.util.avatarColorFor
import com.leo.painelnotificacoes.util.initialsFor

@Composable
fun GroupAvatar(
    appName: String,
    packageName: String,
    iconUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
) {
    val color = avatarColorFor(packageName)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        if (iconUri != null) {
            AsyncImage(
                model = iconUri,
                contentDescription = appName,
                modifier = Modifier.size(size),
            )
        } else {
            Text(
                text = initialsFor(appName),
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF0A0B0D),
            )
        }
    }
}
