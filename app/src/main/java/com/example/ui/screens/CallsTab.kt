package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CallEntity
import com.example.ui.ChatViewModel
import com.example.ui.components.AvatarView
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMint
import com.example.ui.theme.NovaRose
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallsTab(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val calls by viewModel.calls.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // Create Call Link Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ObsidianSurfaceElevated)
                .clickable {
                    // Pick first contact to call
                    allUsers.firstOrNull { !it.isCurrentUser }?.let { peer ->
                        viewModel.startCall(peer.id, peer.name, peer.avatarUrl, isVideo = false)
                    }
                }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(NovaCyan),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = "Call Link",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Create Encrypted Call Link",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = "Share an invite link with Post-Quantum WebRTC",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
            }
        }

        Text(
            text = "Recent Calls",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondaryDark,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(calls, key = { it.id }) { call ->
                CallRow(
                    call = call,
                    onRedial = {
                        viewModel.startCall(
                            peerId = call.peerId,
                            peerName = call.peerName,
                            peerAvatar = call.peerAvatar,
                            isVideo = call.isVideo
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun CallRow(
    call: CallEntity,
    onRedial: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val formattedTime = remember(call.timestamp) { timeFormat.format(Date(call.timestamp)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRedial() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarView(
            name = call.peerName,
            avatarUrl = call.peerAvatar,
            size = 48.dp
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.peerName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (call.status == "MISSED") NovaRose else TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val icon = when {
                    call.status == "MISSED" -> Icons.AutoMirrored.Filled.CallMissed
                    call.isOutgoing -> Icons.AutoMirrored.Filled.CallMade
                    else -> Icons.AutoMirrored.Filled.CallReceived
                }
                val iconColor = when {
                    call.status == "MISSED" -> NovaRose
                    call.isOutgoing -> NovaCyan
                    else -> NovaMint
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(14.dp)
                )

                Text(
                    text = formattedTime,
                    fontSize = 12.sp,
                    color = TextMutedDark
                )

                if (call.durationSeconds > 0) {
                    val min = call.durationSeconds / 60
                    val sec = call.durationSeconds % 60
                    Text(
                        text = "• ${min}m ${sec}s",
                        fontSize = 11.sp,
                        color = TextMutedDark
                    )
                }
            }
        }

        IconButton(
            onClick = onRedial,
            modifier = Modifier.testTag("redial_btn_${call.id}")
        ) {
            Icon(
                imageVector = if (call.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                contentDescription = "Redial",
                tint = if (call.isVideo) NovaViolet else NovaCyan
            )
        }
    }
}
