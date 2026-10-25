package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMint
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.ObsidianBackground

@Composable
fun AvatarView(
    name: String,
    avatarUrl: String = "",
    size: Dp = 48.dp,
    isOnline: Boolean = false,
    hasStoryRing: Boolean = false,
    storyRingViewed: Boolean = false,
    modifier: Modifier = Modifier
) {
    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .ifEmpty { "?" }

    val ringBrush = if (hasStoryRing) {
        if (storyRingViewed) {
            Brush.sweepGradient(listOf(Color.Gray, Color.DarkGray, Color.Gray))
        } else {
            Brush.sweepGradient(listOf(NovaCyan, NovaViolet, NovaMint, NovaCyan))
        }
    } else null

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(if (hasStoryRing) size + 6.dp else size)
    ) {
        // Outer ring if story exists
        if (hasStoryRing && ringBrush != null) {
            Box(
                modifier = Modifier
                    .size(size + 6.dp)
                    .clip(CircleShape)
                    .background(ringBrush)
            )
        }

        // Inner Avatar
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            NovaViolet.copy(alpha = 0.8f),
                            NovaCyan.copy(alpha = 0.7f)
                        )
                    )
                )
        ) {
            if (avatarUrl.isNotBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape)
                )
            } else {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.38f).sp
                )
            }
        }

        // Online indicator dot
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(ObsidianBackground)
                    .border(2.dp, ObsidianBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(size * 0.22f)
                        .clip(CircleShape)
                        .background(NovaMint)
                )
            }
        }
    }
}
