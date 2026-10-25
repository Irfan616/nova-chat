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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.StoryEntity
import com.example.ui.ChatViewModel
import com.example.ui.components.AvatarView
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMint
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChannelItem(
    val id: String,
    val name: String,
    val followers: String,
    val lastUpdate: String,
    val isVerified: Boolean = true
)

@Composable
fun UpdatesTab(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val statuses by viewModel.statuses.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val followedChannels = remember {
        mutableStateMapOf(
            "ch_nova" to true,
            "ch_tech" to false,
            "ch_design" to false
        )
    }

    val sampleChannels = listOf(
        ChannelItem("ch_nova", "NovaChat Official", "2.4M followers", "NovaChat v2.4 roll out: Quantum double-ratchet encryption now standard on all devices. 🚀"),
        ChannelItem("ch_tech", "Tech Horizon", "890K followers", "State of Android 2026: Jetpack Compose adoption reaches 88% among top apps."),
        ChannelItem("ch_design", "Modern UI & Cyberpunk Aesthetics", "450K followers", "Why glassmorphism on OLED dark backgrounds creates higher contrast focus.")
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Section Header
        item {
            Text(
                text = "Status Updates",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        }

        // My Status Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { viewModel.showCreateStoryDialog.value = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AvatarView(
                        name = currentUser?.name ?: "You",
                        avatarUrl = currentUser?.avatarUrl ?: "",
                        size = 52.dp
                    )
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(NovaCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add status",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Status",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Tap to add 24-hour status update",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }

                IconButton(
                    onClick = { viewModel.showCreateStoryDialog.value = true },
                    modifier = Modifier.testTag("create_text_status_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Text Status",
                        tint = NovaCyan
                    )
                }
            }
        }

        // Recent Status Updates from contacts
        items(statuses, key = { it.id }) { status ->
            StatusRow(
                status = status,
                onClick = { viewModel.openStoryViewer(status.id) }
            )
        }

        // Channels Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Broadcast Channels",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = "Explore",
                    fontSize = 13.sp,
                    color = NovaCyan,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Channels List
        items(sampleChannels, key = { it.id }) { channel ->
            val isFollowed = followedChannels[channel.id] == true
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianSurfaceElevated)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarView(name = channel.name, size = 42.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = channel.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                if (channel.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = NovaCyan,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Text(
                                text = channel.followers,
                                fontSize = 11.sp,
                                color = TextMutedDark
                            )
                        }
                    }

                    if (isFollowed) {
                        OutlinedButton(
                            onClick = { followedChannels[channel.id] = false },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Check, null, tint = NovaMint, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Following", fontSize = 11.sp, color = NovaMint)
                        }
                    } else {
                        Button(
                            onClick = { followedChannels[channel.id] = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NovaCyan),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Follow", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = channel.lastUpdate,
                    fontSize = 13.sp,
                    color = TextSecondaryDark,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun StatusRow(
    status: StoryEntity,
    onClick: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(status.timestamp) { timeFormat.format(Date(status.timestamp)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarView(
            name = status.authorName,
            avatarUrl = status.authorAvatar,
            size = 52.dp,
            hasStoryRing = true,
            storyRingViewed = status.isViewed
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = status.authorName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$formattedTime • ${status.caption.take(30)}...",
                fontSize = 12.sp,
                color = TextSecondaryDark
            )
        }
    }
}
