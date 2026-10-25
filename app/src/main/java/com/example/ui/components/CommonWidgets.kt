package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaRose
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.TextMutedDark

@Composable
fun MessageStatusIcon(status: String, modifier: Modifier = Modifier) {
    when (status) {
        "PENDING" -> {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Pending",
                tint = TextMutedDark,
                modifier = modifier.size(13.dp)
            )
        }
        "SENT" -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Sent",
                tint = TextMutedDark,
                modifier = modifier.size(14.dp)
            )
        }
        "DELIVERED" -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Delivered",
                tint = TextMutedDark,
                modifier = modifier.size(14.dp)
            )
        }
        "READ" -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Read",
                tint = NovaCyan,
                modifier = modifier.size(14.dp)
            )
        }
        "FAILED" -> {
            Text(text = "!", color = NovaRose, fontSize = 12.sp, modifier = modifier)
        }
    }
}

@Composable
fun VoiceWaveformBar(
    progress: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val barCount = 28
    val staticAmplitudes = listOf(
        0.3f, 0.5f, 0.8f, 0.4f, 0.9f, 0.6f, 0.3f, 0.7f,
        1.0f, 0.8f, 0.5f, 0.3f, 0.6f, 0.9f, 0.7f, 0.4f,
        0.8f, 0.6f, 0.3f, 0.7f, 0.9f, 0.5f, 0.4f, 0.8f,
        0.5f, 0.3f, 0.6f, 0.4f
    )

    val transition = rememberInfiniteTransition(label = "waveform")
    val waveOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveOffset"
    )

    Row(
        modifier = modifier
            .height(26.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val baseAmp = staticAmplitudes[i % staticAmplitudes.size]
            val animatedAmp = if (isPlaying) {
                val shift = (waveOffset * 0.4f) * (if (i % 2 == 0) 1 else -1)
                (baseAmp + shift).coerceIn(0.2f, 1.0f)
            } else {
                baseAmp
            }
            val isPassed = (i.toFloat() / barCount) <= progress
            val barColor = if (isPassed) NovaCyan else Color.White.copy(alpha = 0.35f)

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(animatedAmp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
fun ReactionChip(
    emoji: String,
    count: Int,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFF1E2638))
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(text = emoji, fontSize = 12.sp)
        if (count > 1) {
            Text(
                text = count.toString(),
                fontSize = 11.sp,
                color = NovaCyan
            )
        }
    }
}

val WallpaperGradients = listOf(
    // 0: Obsidian Nebula (Default)
    listOf(Color(0xFF080B11), Color(0xFF0F172A), Color(0xFF080B11)),
    // 1: Cyber Violet Deep
    listOf(Color(0xFF090814), Color(0xFF1E1136), Color(0xFF090814)),
    // 2: Midnight Emerald
    listOf(Color(0xFF061210), Color(0xFF0D2821), Color(0xFF061210)),
    // 3: Minimalist Charcoal
    listOf(Color(0xFF111418), Color(0xFF181D24), Color(0xFF111418)),
    // 4: Pure AMOLED Black
    listOf(Color(0xFF000000), Color(0xFF050508), Color(0xFF000000))
)
