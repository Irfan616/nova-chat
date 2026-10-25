package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.MessageEntity
import com.example.ui.ChatViewModel
import com.example.ui.components.AvatarView
import com.example.ui.components.MessageStatusIcon
import com.example.ui.components.ReactionChip
import com.example.ui.components.VoiceWaveformBar
import com.example.ui.components.WallpaperGradients
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMint
import com.example.ui.theme.NovaRose
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.OutgoingBubbleAccent
import com.example.ui.theme.OutgoingBubbleDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ConversationScreen(
    conversationId: String,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val conversation by viewModel.getConversationDetails(conversationId).collectAsState()
    val messages by viewModel.getMessagesForConversation(conversationId).collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val typingMap by viewModel.typingStatus.collectAsState()
    val playingVoiceId by viewModel.playingVoiceId.collectAsState()
    val voiceProgress by viewModel.voiceProgress.collectAsState()
    val wallpaperIndex by viewModel.wallpaperIndex.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var selectedMessageForOptions by remember { mutableStateOf<MessageEntity?>(null) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingDurationSec by remember { mutableLongStateOf(0L) }
    var showMenu by remember { mutableStateOf(false) }
    var showDisappearingSheet by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isPeerTyping = typingMap[conversationId] != null

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Timer for voice note recording
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordingDurationSec = 0L
            while (isRecordingVoice) {
                delay(1000)
                recordingDurationSec += 1
            }
        }
    }

    val backgroundColors = WallpaperGradients.getOrElse(wallpaperIndex) { WallpaperGradients[0] }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(backgroundColors))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianSurface.copy(alpha = 0.95f))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.testTag("chat_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimaryDark
                    )
                }

                conversation?.let { conv ->
                    AvatarView(
                        name = conv.title,
                        avatarUrl = conv.avatarUrl,
                        size = 40.dp,
                        isOnline = isPeerTyping
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = conv.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val statusText = when {
                            isPeerTyping -> "typing..."
                            conv.isGroup -> "End-to-End Encrypted Group"
                            else -> "Online • E2E Encrypted"
                        }
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            color = if (isPeerTyping) NovaCyan else TextSecondaryDark
                        )
                    }

                    // Audio Call button
                    IconButton(
                        onClick = {
                            viewModel.startCall(
                                peerId = conv.id,
                                peerName = conv.title,
                                peerAvatar = conv.avatarUrl,
                                isVideo = false
                            )
                        },
                        modifier = Modifier.testTag("audio_call_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Voice Call",
                            tint = NovaCyan
                        )
                    }

                    // Video Call button
                    IconButton(
                        onClick = {
                            viewModel.startCall(
                                peerId = conv.id,
                                peerName = conv.title,
                                peerAvatar = conv.avatarUrl,
                                isVideo = true
                            )
                        },
                        modifier = Modifier.testTag("video_call_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = NovaViolet
                        )
                    }

                    // More Menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("chat_more_menu_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = TextSecondaryDark
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(ObsidianSurfaceElevated)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Disappearing messages", color = TextPrimaryDark) },
                                leadingIcon = { Icon(Icons.Default.Timer, null, tint = NovaCyan) },
                                onClick = {
                                    showMenu = false
                                    showDisappearingSheet = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Verify encryption", color = TextPrimaryDark) },
                                leadingIcon = { Icon(Icons.Default.Security, null, tint = NovaMint) },
                                onClick = {
                                    showMenu = false
                                    viewModel.showSecurityInfoDialog.value = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Mute conversation", color = TextPrimaryDark) },
                                leadingIcon = { Icon(Icons.Default.Close, null, tint = TextMutedDark) },
                                onClick = {
                                    showMenu = false
                                    viewModel.toggleMuteConversation(conv.id, conv.isMuted)
                                }
                            )
                        }
                    }
                }
            }

            // Pinned message banner if any
            val pinnedMessage = messages.lastOrNull { it.isPinned }
            if (pinnedMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NovaCyan.copy(alpha = 0.12f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = NovaCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pinned message: ${pinnedMessage.senderName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaCyan
                        )
                        Text(
                            text = pinnedMessage.text,
                            fontSize = 12.sp,
                            color = TextPrimaryDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Messages LazyColumn
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Encryption notice at top
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ObsidianSurfaceElevated.copy(alpha = 0.85f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock",
                                tint = NovaCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "End-to-end encrypted with Post-Quantum Double Ratchet",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { message ->
                    val isMe = message.senderId == (currentUser?.id ?: "user_me")
                    MessageBubble(
                        message = message,
                        isMe = isMe,
                        isPlayingVoice = playingVoiceId == message.id,
                        voiceProgress = voiceProgress[message.id] ?: 0f,
                        onPlayVoice = { viewModel.togglePlayVoice(message.id, message.mediaDurationMs) },
                        onLongClick = { selectedMessageForOptions = message }
                    )
                }

                // Peer typing indicator bubble in list
                if (isPeerTyping) {
                    item {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(ObsidianSurfaceElevated)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "Typing", fontSize = 12.sp, color = NovaCyan)
                            PulsingTypingDots()
                        }
                    }
                }
            }

            // Quoted reply banner if replying
            AnimatedVisibility(visible = replyingToMessage != null) {
                replyingToMessage?.let { replyTarget ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianSurfaceElevated)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(32.dp)
                                .background(NovaCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = replyTarget.senderName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NovaCyan
                            )
                            Text(
                                text = replyTarget.text,
                                fontSize = 12.sp,
                                color = TextSecondaryDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { replyingToMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel reply",
                                tint = TextMutedDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Input Bar / Voice recording bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianSurface)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRecordingVoice) {
                    // Recording live UI
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(ObsidianSurfaceElevated)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(NovaRose)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val minutes = recordingDurationSec / 60
                        val seconds = recordingDurationSec % 60
                        Text(
                            text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                            color = NovaRose,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Recording voice note...",
                            color = TextSecondaryDark,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Cancel recording
                    IconButton(
                        onClick = { isRecordingVoice = false },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Voice",
                            tint = NovaRose
                        )
                    }

                    // Send recorded voice note
                    IconButton(
                        onClick = {
                            val duration = recordingDurationSec * 1000L
                            viewModel.sendMessage(
                                conversationId = conversationId,
                                text = "Voice message",
                                type = "VOICE",
                                mediaDurationMs = if (duration > 0) duration else 6000L
                            )
                            isRecordingVoice = false
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NovaCyan)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Voice",
                            tint = Color.Black
                        )
                    }
                } else {
                    // Quick Attachment / Media button
                    IconButton(
                        onClick = {
                            // Send quick sample photo or location
                            viewModel.sendMessage(
                                conversationId = conversationId,
                                text = "Shared current secure location 📍 [37.7749° N, 122.4194° W]",
                                type = "LOCATION"
                            )
                        },
                        modifier = Modifier.testTag("attach_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attachment",
                            tint = NovaCyan
                        )
                    }

                    // Text Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Encrypted message...",
                                fontSize = 14.sp,
                                color = TextMutedDark
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ObsidianSurfaceElevated,
                            unfocusedContainerColor = ObsidianSurfaceElevated,
                            focusedBorderColor = NovaCyan.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("message_input_field")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    if (inputText.isNotBlank()) {
                        // Send text button
                        IconButton(
                            onClick = {
                                val text = inputText.trim()
                                val reply = replyingToMessage
                                viewModel.sendMessage(
                                    conversationId = conversationId,
                                    text = text,
                                    replyToId = reply?.id,
                                    replyToSnippet = reply?.text,
                                    replyToSender = reply?.senderName
                                )
                                inputText = ""
                                replyingToMessage = null
                            },
                            modifier = Modifier
                                .testTag("send_message_btn")
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(NovaCyan)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.Black
                            )
                        }
                    } else {
                        // Voice recording button
                        IconButton(
                            onClick = { isRecordingVoice = true },
                            modifier = Modifier
                                .testTag("record_voice_btn")
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(NovaViolet)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Record",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Message Options Bottom Sheet (React, Reply, Copy, Edit, Pin, Delete)
    selectedMessageForOptions?.let { msg ->
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { selectedMessageForOptions = null },
            sheetState = sheetState,
            containerColor = ObsidianSurfaceElevated,
            contentColor = TextPrimaryDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Quick emoji reaction bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("❤️", "👍", "🔥", "😂", "😮", "🎉").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    viewModel.addReaction(msg.id, emoji)
                                    selectedMessageForOptions = null
                                }
                                .padding(8.dp)
                        )
                    }
                }

                // Reply
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            replyingToMessage = msg
                            selectedMessageForOptions = null
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.Reply, null, tint = NovaCyan)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Reply to message", color = TextPrimaryDark)
                }

                // Pin
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.togglePinMessage(msg.id, msg.isPinned)
                            selectedMessageForOptions = null
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PushPin, null, tint = NovaViolet)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(if (msg.isPinned) "Unpin message" else "Pin message", color = TextPrimaryDark)
                }

                // Delete
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.deleteMessage(msg.id)
                            selectedMessageForOptions = null
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Delete, null, tint = NovaRose)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Delete message", color = NovaRose)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Disappearing Messages Sheet
    if (showDisappearingSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showDisappearingSheet = false },
            sheetState = sheetState,
            containerColor = ObsidianSurfaceElevated,
            contentColor = TextPrimaryDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Disappearing Messages",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = "When turned on, new messages in this chat will disappear after the selected duration for all participants.",
                    fontSize = 13.sp,
                    color = TextSecondaryDark
                )

                listOf(
                    0L to "Off",
                    86400L to "24 Hours",
                    604800L to "7 Days",
                    7776000L to "90 Days"
                ).forEach { (seconds, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ObsidianBackground)
                            .clickable {
                                viewModel.setDisappearingTimer(conversationId, seconds)
                                showDisappearingSheet = false
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, color = TextPrimaryDark, fontSize = 15.sp)
                        Icon(Icons.Default.Timer, null, tint = NovaCyan)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: MessageEntity,
    isMe: Boolean,
    isPlayingVoice: Boolean,
    voiceProgress: Float,
    onPlayVoice: () -> Unit,
    onLongClick: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    val bubbleBackground = if (isMe) {
        Brush.linearGradient(listOf(OutgoingBubbleDark, OutgoingBubbleAccent))
    } else {
        Brush.linearGradient(listOf(ObsidianSurfaceElevated, ObsidianSurface))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("message_bubble_${message.id}"),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(bubbleShape)
                .background(bubbleBackground)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                // In group or non-me, show sender name
                if (!isMe) {
                    Text(
                        text = message.senderName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaCyan,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Quoted reply snippet if any
                if (!message.replyToSnippet.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height(26.dp)
                                .background(NovaCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = message.replyToSender ?: "Reply",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NovaCyan
                            )
                            Text(
                                text = message.replyToSnippet,
                                fontSize = 11.sp,
                                color = TextSecondaryDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Message content by type
                when (message.type) {
                    "VOICE" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            IconButton(
                                onClick = onPlayVoice,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlayingVoice) NovaCyan else Color.White.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = if (isPlayingVoice) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play voice note",
                                    tint = if (isPlayingVoice) Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            VoiceWaveformBar(
                                progress = voiceProgress,
                                isPlaying = isPlayingVoice,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    "LOCATION" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = message.text,
                                fontSize = 13.sp,
                                color = TextPrimaryDark
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = message.text,
                            fontSize = 14.sp,
                            color = TextPrimaryDark,
                            lineHeight = 19.sp
                        )
                    }
                }

                // Timestamp & status tick
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (message.isEdited) {
                        Text(text = "edited", fontSize = 9.sp, color = TextMutedDark)
                    }
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    if (isMe) {
                        MessageStatusIcon(status = message.status)
                    }
                }
            }
        }

        // Reactions under bubble
        if (message.reactions.isNotBlank()) {
            Row(
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                message.reactions.split(",").forEach { item ->
                    val parts = item.split(":")
                    if (parts.size == 2) {
                        ReactionChip(emoji = parts[0], count = parts[1].toIntOrNull() ?: 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun PulsingTypingDots() {
    val transition = rememberInfiniteTransition(label = "dots")
    val dotAlpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(NovaCyan.copy(alpha = dotAlpha))
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(NovaCyan.copy(alpha = ((dotAlpha + 0.5f) % 1f).coerceIn(0.2f, 1f)))
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(NovaCyan.copy(alpha = 1.2f - dotAlpha))
        )
    }
}
