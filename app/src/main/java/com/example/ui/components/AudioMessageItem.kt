package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AudioMessageItem(
    durationSec: Int,
    isMe: Boolean,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgressSec by remember { mutableIntStateOf(0) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (currentProgressSec < durationSec) {
                delay(1000)
                currentProgressSec++
            }
            isPlaying = false
            currentProgressSec = 0
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val waveAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveHeight"
    )

    val contentColor = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val progressColor = if (isMe) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary
    val trackColor = if (isMe) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)

    Row(
        modifier = modifier
            .padding(vertical = 4.dp)
            .testTag("audio_message_item"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                if (isPlaying) {
                    isPlaying = false
                } else {
                    if (currentProgressSec >= durationSec) {
                        currentProgressSec = 0
                    }
                    isPlaying = true
                }
            },
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isMe) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer)
                .testTag("audio_play_pause_button")
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Lire l'audio",
                tint = if (isMe) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Waveform bars simulation
        val barHeights = remember { listOf(0.4f, 0.7f, 1f, 0.5f, 0.8f, 0.3f, 0.9f, 0.6f, 0.4f, 0.7f, 0.5f, 0.9f) }
        Row(
            modifier = Modifier
                .height(24.dp)
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            barHeights.forEachIndexed { index, baseHeight ->
                val dynamicFactor = if (isPlaying) ((waveAnim + index * 0.1f) % 1f).coerceIn(0.2f, 1f) else baseHeight
                val isPassed = (index.toFloat() / barHeights.size.toFloat()) <= (currentProgressSec.toFloat() / durationSec.coerceAtLeast(1).toFloat())
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(dynamicFactor)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isPassed) progressColor else trackColor)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        val displaySec = if (isPlaying) currentProgressSec else durationSec
        Text(
            text = String.format("%02d:%02d", displaySec / 60, displaySec % 60),
            fontSize = 12.sp,
            color = contentColor
        )
    }
}
