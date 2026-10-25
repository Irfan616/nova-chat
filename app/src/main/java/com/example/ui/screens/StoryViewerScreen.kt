package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatViewModel
import com.example.ui.components.AvatarView
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMint
import com.example.ui.theme.NovaRose
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StoryViewerScreen(
    statusId: String,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val statuses by viewModel.statuses.collectAsState()
    val status = statuses.firstOrNull { it.id == statusId } ?: statuses.firstOrNull()

    val progress = remember { Animatable(0f) }
    var replyText by remember { mutableStateOf("") }
    var hasLiked by remember { mutableStateOf(false) }

    // Auto-advance progress
    LaunchedEffect(statusId) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 6000, easing = LinearEasing)
        )
        viewModel.navigateBack()
    }

    val gradientList = when (status?.bgGradientIndex ?: 0) {
        0 -> listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
        1 -> listOf(Color(0xFF2E0854), Color(0xFF5B146F), Color(0xFF861657))
        2 -> listOf(Color(0xFF0B3024), Color(0xFF135A3D), Color(0xFF1B8A5A))
        else -> listOf(Color(0xFF1A1A24), Color(0xFF2B2B3D), Color(0xFF1A1A24))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientList))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Progress and user info
            Column {
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = NovaCyan,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarView(
                            name = status?.authorName ?: "User",
                            avatarUrl = status?.authorAvatar ?: "",
                            size = 40.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = status?.authorName ?: "User",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                            val timeStr = status?.timestamp?.let { timeFormat.format(Date(it)) } ?: "Just now"
                            Text(
                                text = timeStr,
                                fontSize = 11.sp,
                                color = TextMutedDark
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("close_story_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close story",
                            tint = Color.White
                        )
                    }
                }
            }

            // Center story caption
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = status?.caption ?: "Enjoying NovaChat!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )
            }

            // Bottom bar: reply and view count
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Visibility, null, tint = NovaMint, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${status?.viewCount ?: 1} views • 24h expiration",
                        fontSize = 12.sp,
                        color = NovaMint,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Reply to status...", fontSize = 13.sp, color = TextMutedDark) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.4f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.4f),
                            focusedBorderColor = NovaCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.weight(1f)
                    )

                    if (replyText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                viewModel.createNewChat(
                                    title = status?.authorName ?: "Contact",
                                    isGroup = false,
                                    initialMessage = "Replying to story '${status?.caption?.take(20)}...': $replyText"
                                )
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(NovaCyan)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.Black)
                        }
                    } else {
                        IconButton(
                            onClick = { hasLiked = !hasLiked },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (hasLiked) NovaRose else Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Like",
                                tint = if (hasLiked) Color.White else NovaRose
                            )
                        }
                    }
                }
            }
        }
    }
}
