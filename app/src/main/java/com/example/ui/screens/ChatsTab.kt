package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ConversationEntity
import com.example.ui.ChatFilter
import com.example.ui.ChatViewModel
import com.example.ui.components.AvatarView
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaRose
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatsTab(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val conversations by viewModel.activeConversations.collectAsState()
    val statuses by viewModel.statuses.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentFilter by viewModel.chatFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val typingMap by viewModel.typingStatus.collectAsState()

    var selectedConvForOptions by remember { mutableStateOf<ConversationEntity?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val filteredConversations = conversations.filter { conv ->
        val matchesQuery = searchQuery.isBlank() ||
                conv.title.contains(searchQuery, ignoreCase = true) ||
                conv.lastMessageSnippet.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (currentFilter) {
            ChatFilter.ALL -> true
            ChatFilter.UNREAD -> conv.unreadCount > 0
            ChatFilter.GROUPS -> conv.isGroup
            ChatFilter.CHANNELS -> conv.isChannel
        }
        matchesQuery && matchesFilter
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Stories / Updates Carousel
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // My Story item
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .testTag("my_story_item")
                        .combinedClickable(
                            onClick = { viewModel.showCreateStoryDialog.value = true }
                        )
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        AvatarView(
                            name = currentUser?.name ?: "You",
                            avatarUrl = currentUser?.avatarUrl ?: "",
                            size = 54.dp
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
                                contentDescription = "Add story",
                                tint = Color.Black,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "My Status",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondaryDark
                    )
                }
            }

            // Contact Stories
            items(statuses) { status ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .testTag("story_${status.id}")
                        .combinedClickable(
                            onClick = { viewModel.openStoryViewer(status.id) }
                        )
                ) {
                    AvatarView(
                        name = status.authorName,
                        avatarUrl = status.authorAvatar,
                        size = 54.dp,
                        hasStoryRing = true,
                        storyRingViewed = status.isViewed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = status.authorName.split(" ").firstOrNull() ?: status.authorName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (status.isViewed) TextMutedDark else TextPrimaryDark
                    )
                }
            }
        }

        // Filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                ChatFilter.ALL to "All",
                ChatFilter.UNREAD to "Unread",
                ChatFilter.GROUPS to "Groups",
                ChatFilter.CHANNELS to "Channels"
            ).forEach { (filter, label) ->
                val selected = currentFilter == filter
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setChatFilter(filter) },
                    label = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NovaCyan.copy(alpha = 0.18f),
                        selectedLabelColor = NovaCyan,
                        containerColor = ObsidianSurfaceElevated,
                        labelColor = TextSecondaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = if (selected) NovaCyan.copy(alpha = 0.6f) else ObsidianBorder
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("filter_${filter.name.lowercase()}")
                )
            }
        }

        // Conversation List
        if (filteredConversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "No chats",
                        tint = TextMutedDark,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No conversations found",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Start a new encrypted chat with contacts or groups.",
                        fontSize = 13.sp,
                        color = TextMutedDark,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.showNewChatDialog.value = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaCyan),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("empty_start_chat_btn")
                    ) {
                        Text(text = "Start New Chat", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredConversations, key = { it.id }) { conv ->
                    val typingPeer = typingMap[conv.id]
                    ConversationRow(
                        conversation = conv,
                        typingPeer = typingPeer,
                        onClick = { viewModel.openConversation(conv.id) },
                        onLongClick = { selectedConvForOptions = conv }
                    )
                }
            }
        }
    }

    // Bottom Sheet for Conversation Actions
    selectedConvForOptions?.let { conv ->
        ModalBottomSheet(
            onDismissRequest = { selectedConvForOptions = null },
            sheetState = sheetState,
            containerColor = ObsidianSurfaceElevated,
            contentColor = TextPrimaryDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = conv.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                viewModel.togglePinConversation(conv.id, conv.isPinned)
                                selectedConvForOptions = null
                            }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pin",
                        tint = NovaCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = if (conv.isPinned) "Unpin conversation" else "Pin to top",
                        fontSize = 15.sp,
                        color = TextPrimaryDark
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                viewModel.toggleMuteConversation(conv.id, conv.isMuted)
                                selectedConvForOptions = null
                            }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (conv.isMuted) Icons.Default.VolumeMute else Icons.Default.NotificationsOff,
                        contentDescription = "Mute",
                        tint = NovaViolet,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = if (conv.isMuted) "Unmute notifications" else "Mute notifications",
                        fontSize = 15.sp,
                        color = TextPrimaryDark
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                viewModel.toggleArchiveConversation(conv.id, conv.isArchived)
                                selectedConvForOptions = null
                            }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = "Archive",
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Archive conversation",
                        fontSize = 15.sp,
                        color = TextPrimaryDark
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationRow(
    conversation: ConversationEntity,
    typingPeer: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(conversation.lastMessageTimestamp) {
        timeFormat.format(Date(conversation.lastMessageTimestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_row_${conversation.id}")
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarView(
            name = conversation.title,
            avatarUrl = conversation.avatarUrl,
            size = 52.dp,
            isOnline = typingPeer != null
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextPrimaryDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = if (conversation.unreadCount > 0) NovaCyan else TextMutedDark,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val snippetText = if (typingPeer != null) {
                    "Typing..."
                } else if (conversation.draft.isNotBlank()) {
                    "Draft: ${conversation.draft}"
                } else {
                    conversation.lastMessageSnippet
                }

                Text(
                    text = snippetText,
                    fontSize = 13.sp,
                    color = if (typingPeer != null) NovaCyan else if (conversation.draft.isNotBlank()) NovaRose else TextSecondaryDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (conversation.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = TextMutedDark,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    if (conversation.isMuted) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = "Muted",
                            tint = TextMutedDark,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    if (conversation.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(NovaCyan)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conversation.unreadCount.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
