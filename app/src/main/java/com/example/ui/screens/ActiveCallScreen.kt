package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatViewModel
import com.example.ui.components.AvatarView
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMint
import com.example.ui.theme.NovaRose
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import java.util.Locale

@Composable
fun ActiveCallScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val callInfo by viewModel.activeCall.collectAsState()

    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFF131D33),
                        ObsidianBackground
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top E2E Security Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = NovaCyan,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "End-to-End Encrypted WebRTC",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }

            // Peer Avatar & Calling Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing animated ring
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(listOf(NovaCyan, NovaViolet, NovaCyan)),
                                shape = CircleShape
                            )
                    )

                    AvatarView(
                        name = callInfo?.peerName ?: "Nova Contact",
                        avatarUrl = callInfo?.peerAvatar ?: "",
                        size = 110.dp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = callInfo?.peerName ?: "Nova Contact",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                val statusText = when (callInfo?.status) {
                    "CONNECTING" -> "Connecting secure tunnel..."
                    "RINGING" -> "Ringing..."
                    "CONNECTED" -> {
                        val duration = callInfo?.durationSeconds ?: 0
                        val min = duration / 60
                        val sec = duration % 60
                        String.format(Locale.getDefault(), "%02d:%02d", min, sec)
                    }
                    else -> "Call ending..."
                }

                Text(
                    text = statusText,
                    fontSize = 16.sp,
                    color = if (callInfo?.status == "CONNECTED") NovaMint else NovaCyan,
                    fontWeight = FontWeight.SemiBold
                )

                if (callInfo?.isVideo == true) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Encrypted HD 1080p 60fps Video",
                        fontSize = 12.sp,
                        color = TextMutedDark
                    )
                }
            }

            // Bottom Call Action Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute Microphone
                    IconButton(
                        onClick = { viewModel.toggleCallMute() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (callInfo?.isMuted == true) NovaRose else Color.White.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = if (callInfo?.isMuted == true) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Toggle Mute",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Speakerphone
                    IconButton(
                        onClick = { viewModel.toggleCallSpeaker() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (callInfo?.isSpeaker == true) NovaCyan else Color.White.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = if (callInfo?.isSpeaker == true) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Speaker",
                            tint = if (callInfo?.isSpeaker == true) Color.Black else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Video Toggle
                    if (callInfo?.isVideo == true) {
                        IconButton(
                            onClick = { viewModel.toggleCallVideo() },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (callInfo?.isVideoEnabled == true) NovaViolet else Color.White.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = if (callInfo?.isVideoEnabled == true) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = "Toggle Video",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Flip camera
                        IconButton(
                            onClick = { viewModel.flipCamera() },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cameraswitch,
                                contentDescription = "Flip Camera",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Big Red End Call button
                IconButton(
                    onClick = { viewModel.endCall() },
                    modifier = Modifier
                        .testTag("end_call_btn")
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(NovaRose)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
