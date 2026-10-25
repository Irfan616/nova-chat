package com.example.data

import android.content.Context
import com.example.data.entity.CallEntity
import com.example.data.entity.CommunitySpaceEntity
import com.example.data.entity.ConversationEntity
import com.example.data.entity.MessageEntity
import com.example.data.entity.StoryEntity
import com.example.data.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

data class ActiveCallInfo(
    val callId: String = UUID.randomUUID().toString(),
    val peerId: String,
    val peerName: String,
    val peerAvatar: String,
    val isVideo: Boolean,
    val status: String = "CONNECTING", // CONNECTING, RINGING, CONNECTED, ENDED
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeaker: Boolean = true,
    val isVideoEnabled: Boolean = true,
    val isFrontCamera: Boolean = true
)

class NovaChatRepository(private val database: AppDatabase, private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)

    // In-memory real-time typing indicators: conversationId -> typing user name or null
    private val _typingStatus = MutableStateFlow<Map<String, String?>>(emptyMap())
    val typingStatus = _typingStatus.asStateFlow()

    // Active call state
    private val _activeCall = MutableStateFlow<ActiveCallInfo?>(null)
    val activeCall = _activeCall.asStateFlow()
    private var callTimerJob: Job? = null

    // Active voice playback state: messageId -> progress (0.0 to 1.0), isPlaying
    private val _playingVoiceId = MutableStateFlow<String?>(null)
    val playingVoiceId = _playingVoiceId.asStateFlow()
    private val _voiceProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val voiceProgress = _voiceProgress.asStateFlow()
    private var voicePlaybackJob: Job? = null

    val activeConversations: Flow<List<ConversationEntity>> = database.conversationDao().getActiveConversations()
    val archivedConversations: Flow<List<ConversationEntity>> = database.conversationDao().getArchivedConversations()
    val allUsers: Flow<List<UserEntity>> = database.userDao().getAllUsers()
    val currentUser: Flow<UserEntity?> = database.userDao().getCurrentUser().map { list -> list.firstOrNull() }
    val allCalls: Flow<List<CallEntity>> = database.callDao().getAllCalls()
    val allStatuses: Flow<List<StoryEntity>> = database.storyDao().getAllStatuses()
    val allCommunities: Flow<List<CommunitySpaceEntity>> = database.communityDao().getAllCommunities()

    init {
        scope.launch {
            seedDatabaseIfEmpty()
        }
    }

    suspend fun seedDatabaseIfEmpty() {
        val existingUsers = database.userDao().getCurrentUserSync()
        if (existingUsers == null) {
            val users = listOf(
                UserEntity(
                    id = "user_me",
                    name = "Alex Vance",
                    handle = "@alex_vance",
                    phone = "+1 (555) 019-2834",
                    email = "alex.vance@novachat.network",
                    bio = "Building the future of private quantum-ready communications ⚡",
                    statusPhrase = "Available",
                    isOnline = true,
                    isCurrentUser = true
                ),
                UserEntity(
                    id = "user_sarah",
                    name = "Sarah Chen",
                    handle = "@sarah_ai",
                    phone = "+1 (555) 843-9210",
                    email = "sarah.chen@deepmind.ai",
                    bio = "Principal AI Architect 🧠 | Quantum algorithms",
                    statusPhrase = "Exploring latent spaces 🚀",
                    isOnline = true,
                    lastSeen = System.currentTimeMillis() - 60000
                ),
                UserEntity(
                    id = "user_marcus",
                    name = "Marcus Brody",
                    handle = "@marcus_design",
                    phone = "+1 (555) 912-3847",
                    email = "marcus@studiocreative.design",
                    bio = "Head of Product Design @ NeoStudio 🎨",
                    statusPhrase = "Deep in Figma drafts",
                    isOnline = true,
                    lastSeen = System.currentTimeMillis() - 300000
                ),
                UserEntity(
                    id = "user_elena",
                    name = "Elena Rostova",
                    handle = "@elena_crypto",
                    phone = "+1 (555) 472-8193",
                    email = "elena.r@zkproofs.org",
                    bio = "Cryptography & Double Ratchet Protocol auditor 🔐",
                    statusPhrase = "At the gym 🏃‍♀️",
                    isOnline = false,
                    lastSeen = System.currentTimeMillis() - 1800000
                ),
                UserEntity(
                    id = "user_bot",
                    name = "Nova Assistant",
                    handle = "@nova_core",
                    phone = "+0 (000) 000-0000",
                    email = "support@novachat.network",
                    bio = "Official NovaChat Core System & Encrypted Key Manager 🤖",
                    statusPhrase = "Systems operational 🟢",
                    isOnline = true
                )
            )
            database.userDao().insertUsers(users)

            val now = System.currentTimeMillis()
            val conversations = listOf(
                ConversationEntity(
                    id = "conv_sarah",
                    title = "Sarah Chen",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                    isGroup = false,
                    unreadCount = 1,
                    lastMessageSnippet = "Can you review the cryptographic proof for our Double Ratchet implementation?",
                    lastMessageTimestamp = now - 120000,
                    isPinned = true
                ),
                ConversationEntity(
                    id = "conv_marcus",
                    title = "Marcus Brody",
                    avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                    isGroup = false,
                    unreadCount = 0,
                    lastMessageSnippet = "The new glassmorphism bubble gradients look incredible! ✨",
                    lastMessageTimestamp = now - 900000,
                    isPinned = false
                ),
                ConversationEntity(
                    id = "conv_team",
                    title = "Nova Core Architecture 🚀",
                    avatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150",
                    isGroup = true,
                    unreadCount = 2,
                    lastMessageSnippet = "Elena: Audited the key distribution server—all tests passed! 💯",
                    lastMessageTimestamp = now - 1800000,
                    isPinned = true
                ),
                ConversationEntity(
                    id = "conv_elena",
                    title = "Elena Rostova",
                    avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150",
                    isGroup = false,
                    unreadCount = 0,
                    lastMessageSnippet = "Voice message (0:24)",
                    lastMessageTimestamp = now - 7200000
                ),
                ConversationEntity(
                    id = "conv_bot",
                    title = "Nova Assistant ⚡",
                    avatarUrl = "",
                    isGroup = false,
                    unreadCount = 0,
                    lastMessageSnippet = "Welcome to NovaChat! Your end-to-end encryption keys are verified.",
                    lastMessageTimestamp = now - 86400000
                )
            )
            database.conversationDao().insertConversations(conversations)

            // Seed Messages
            val messages = listOf(
                // Sarah Chen
                MessageEntity(
                    id = "msg_s1",
                    conversationId = "conv_sarah",
                    senderId = "user_sarah",
                    senderName = "Sarah Chen",
                    text = "Hey Alex! Did you get a chance to check the latest latency benchmarks on the streaming websocket layer?",
                    timestamp = now - 3600000,
                    status = "READ"
                ),
                MessageEntity(
                    id = "msg_s2",
                    conversationId = "conv_sarah",
                    senderId = "user_me",
                    senderName = "Alex Vance",
                    text = "Yes! Latency dropped below 18ms across all test nodes. The memory footprint in Compose is super lean too.",
                    timestamp = now - 3200000,
                    status = "READ",
                    reactions = "🔥:1,👍:1"
                ),
                MessageEntity(
                    id = "msg_s3",
                    conversationId = "conv_sarah",
                    senderId = "user_sarah",
                    senderName = "Sarah Chen",
                    text = "That is remarkable. Here is a quick voice note explaining the next phase:",
                    timestamp = now - 2400000,
                    status = "READ"
                ),
                MessageEntity(
                    id = "msg_s4",
                    conversationId = "conv_sarah",
                    senderId = "user_sarah",
                    senderName = "Sarah Chen",
                    text = "Voice note update",
                    type = "VOICE",
                    mediaDurationMs = 28000L,
                    timestamp = now - 2350000,
                    status = "READ"
                ),
                MessageEntity(
                    id = "msg_s5",
                    conversationId = "conv_sarah",
                    senderId = "user_sarah",
                    senderName = "Sarah Chen",
                    text = "Can you review the cryptographic proof for our Double Ratchet implementation?",
                    timestamp = now - 120000,
                    status = "DELIVERED"
                ),

                // Marcus Brody
                MessageEntity(
                    id = "msg_m1",
                    conversationId = "conv_marcus",
                    senderId = "user_me",
                    senderName = "Alex Vance",
                    text = "Marcus, check out the updated dark obsidian theme with cyan glowing borders.",
                    timestamp = now - 1200000,
                    status = "READ"
                ),
                MessageEntity(
                    id = "msg_m2",
                    conversationId = "conv_marcus",
                    senderId = "user_marcus",
                    senderName = "Marcus Brody",
                    text = "The new glassmorphism bubble gradients look incredible! ✨",
                    timestamp = now - 900000,
                    status = "READ",
                    reactions = "❤️:2"
                ),

                // Dev Core Team
                MessageEntity(
                    id = "msg_t1",
                    conversationId = "conv_team",
                    senderId = "user_bot",
                    senderName = "Nova System",
                    text = "Group created with end-to-end post-quantum encryption enabled 🛡️",
                    type = "SYSTEM",
                    timestamp = now - 7200000,
                    status = "READ"
                ),
                MessageEntity(
                    id = "msg_t2",
                    conversationId = "conv_team",
                    senderId = "user_me",
                    senderName = "Alex Vance",
                    text = "Welcome everyone to NovaChat engineering hub. Let's make sure our milestone 1 is flawless.",
                    timestamp = now - 3600000,
                    status = "READ",
                    isPinned = true
                ),
                MessageEntity(
                    id = "msg_t3",
                    conversationId = "conv_team",
                    senderId = "user_elena",
                    senderName = "Elena Rostova",
                    text = "Elena: Audited the key distribution server—all tests passed! 💯",
                    timestamp = now - 1800000,
                    status = "DELIVERED",
                    reactions = "🚀:3"
                ),

                // Elena
                MessageEntity(
                    id = "msg_e1",
                    conversationId = "conv_elena",
                    senderId = "user_elena",
                    senderName = "Elena Rostova",
                    text = "Voice message (0:24)",
                    type = "VOICE",
                    mediaDurationMs = 24000L,
                    timestamp = now - 7200000,
                    status = "READ"
                ),

                // Nova Assistant
                MessageEntity(
                    id = "msg_b1",
                    conversationId = "conv_bot",
                    senderId = "user_bot",
                    senderName = "Nova Assistant",
                    text = "Welcome to NovaChat! Your end-to-end encryption keys are verified. Type any prompt or query here to test real-time automated responses and smart tools.",
                    timestamp = now - 86400000,
                    status = "READ"
                )
            )
            database.messageDao().insertMessages(messages)

            // Calls
            val calls = listOf(
                CallEntity(
                    id = "call_1",
                    peerId = "user_sarah",
                    peerName = "Sarah Chen",
                    peerAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                    isVideo = true,
                    isOutgoing = false,
                    status = "COMPLETED",
                    timestamp = now - 7200000,
                    durationSeconds = 860
                ),
                CallEntity(
                    id = "call_2",
                    peerId = "user_marcus",
                    peerName = "Marcus Brody",
                    peerAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                    isVideo = false,
                    isOutgoing = true,
                    status = "COMPLETED",
                    timestamp = now - 28800000,
                    durationSeconds = 245
                ),
                CallEntity(
                    id = "call_3",
                    peerId = "user_elena",
                    peerName = "Elena Rostova",
                    peerAvatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150",
                    isVideo = false,
                    isOutgoing = false,
                    status = "MISSED",
                    timestamp = now - 43200000,
                    durationSeconds = 0
                )
            )
            calls.forEach { database.callDao().insertCall(it) }

            // Status updates (Stories)
            val statuses = listOf(
                StoryEntity(
                    id = "status_1",
                    authorId = "user_sarah",
                    authorName = "Sarah Chen",
                    authorAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                    caption = "Deploying the quantum-resistant key exchange protocol to staging! 🚀🔥",
                    bgGradientIndex = 0,
                    timestamp = now - 3600000,
                    viewCount = 18,
                    isViewed = false
                ),
                StoryEntity(
                    id = "status_2",
                    authorId = "user_marcus",
                    authorName = "Marcus Brody",
                    authorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                    caption = "New glassmorphic UI tokens in production. The cyber obsidian look is pristine! 🎨✨",
                    bgGradientIndex = 1,
                    timestamp = now - 14400000,
                    viewCount = 27,
                    isViewed = true
                ),
                StoryEntity(
                    id = "status_3",
                    authorId = "user_elena",
                    authorName = "Elena Rostova",
                    authorAvatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150",
                    caption = "Zero-knowledge privacy proofs passed 100% of integration audits 🔐",
                    bgGradientIndex = 2,
                    timestamp = now - 28800000,
                    viewCount = 42,
                    isViewed = false
                )
            )
            statuses.forEach { database.storyDao().insertStatus(it) }

            // Communities
            val communities = listOf(
                CommunitySpaceEntity(
                    id = "comm_ai",
                    name = "AI & Deep Learning Collective",
                    description = "Global community of AI researchers, model fine-tuners, and systems engineers.",
                    avatarUrl = "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=150",
                    memberCount = 1420,
                    channels = "announcements,general-chat,papers-and-research,gpu-benchmarks,model-weights"
                ),
                CommunitySpaceEntity(
                    id = "comm_android",
                    name = "Android & Jetpack Architects",
                    description = "Modern Compose developers, Kotlin coroutines enthusiasts, and mobile leaders.",
                    avatarUrl = "https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb?w=150",
                    memberCount = 890,
                    channels = "announcements,compose-ui,architecture-guidelines,performance-lab,showcase"
                ),
                CommunitySpaceEntity(
                    id = "comm_crypto",
                    name = "Privacy & Cryptography Hub",
                    description = "End-to-end encryption, Zero Knowledge proofs, and decentralized protocols.",
                    avatarUrl = "https://images.unsplash.com/photo-1639762681485-074b7f938ba0?w=150",
                    memberCount = 612,
                    channels = "announcements,zk-proofs,double-ratchet-protocol,security-audits"
                )
            )
            database.communityDao().insertCommunities(communities)
        }
    }

    fun getMessages(conversationId: String): Flow<List<MessageEntity>> {
        return database.messageDao().getMessagesForConversation(conversationId)
    }

    fun getConversation(conversationId: String): Flow<ConversationEntity?> {
        return database.conversationDao().getConversationById(conversationId).map { list -> list.firstOrNull() }
    }

    suspend fun markConversationRead(conversationId: String) {
        database.conversationDao().markAsRead(conversationId)
    }

    suspend fun updateDraft(conversationId: String, draft: String) {
        database.conversationDao().updateDraft(conversationId, draft)
    }

    suspend fun togglePin(conversationId: String, isPinned: Boolean) {
        database.conversationDao().setPinned(conversationId, !isPinned)
    }

    suspend fun toggleMute(conversationId: String, isMuted: Boolean) {
        database.conversationDao().setMuted(conversationId, !isMuted)
    }

    suspend fun toggleArchive(conversationId: String, isArchived: Boolean) {
        database.conversationDao().setArchived(conversationId, !isArchived)
    }

    suspend fun setDisappearingTimer(conversationId: String, seconds: Long) {
        database.conversationDao().setDisappearing(conversationId, seconds)
        // Add a system notice message
        val noticeText = if (seconds > 0) {
            val label = when (seconds) {
                86400L -> "24 hours"
                604800L -> "7 days"
                7776000L -> "90 days"
                else -> "$seconds seconds"
            }
            "⏱️ Disappearing messages enabled: messages will expire after $label."
        } else {
            "⏱️ Disappearing messages turned off."
        }
        val systemMsg = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = "system",
            senderName = "System",
            text = noticeText,
            type = "SYSTEM",
            timestamp = System.currentTimeMillis()
        )
        database.messageDao().insertMessage(systemMsg)
    }

    suspend fun sendMessage(
        conversationId: String,
        text: String,
        type: String = "TEXT",
        mediaUrl: String? = null,
        mediaDurationMs: Long = 0L,
        mediaSizeText: String? = null,
        replyToId: String? = null,
        replyToSnippet: String? = null,
        replyToSender: String? = null
    ) {
        val currentUser = database.userDao().getCurrentUserSync()
        val senderId = currentUser?.id ?: "user_me"
        val senderName = currentUser?.name ?: "Alex Vance"

        val messageId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val message = MessageEntity(
            id = messageId,
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            text = text,
            type = type,
            mediaUrl = mediaUrl,
            mediaDurationMs = mediaDurationMs,
            mediaSizeText = mediaSizeText,
            replyToId = replyToId,
            replyToSnippet = replyToSnippet,
            replyToSender = replyToSender,
            status = "PENDING",
            timestamp = now
        )

        database.messageDao().insertMessage(message)
        val snippet = when (type) {
            "VOICE" -> "🎤 Voice message"
            "IMAGE" -> "📷 Photo"
            "LOCATION" -> "📍 Location shared"
            "DOC" -> "📄 Document"
            else -> text
        }
        database.conversationDao().updateLastMessage(conversationId, snippet, now)
        database.conversationDao().updateDraft(conversationId, "")

        // Simulate network delivery progression
        scope.launch {
            delay(250)
            database.messageDao().updateStatus(messageId, "SENT")
            delay(500)
            database.messageDao().updateStatus(messageId, "DELIVERED")
            delay(600)
            database.messageDao().updateStatus(messageId, "READ")

            // Simulate Peer Automatic Contextual Reply
            triggerSimulatedPeerReply(conversationId, text, type)
        }
    }

    private suspend fun triggerSimulatedPeerReply(conversationId: String, userText: String, type: String) {
        val conv = database.conversationDao().getConversationByIdSync(conversationId) ?: return

        // Pick peer persona based on conversation
        val (peerId, peerName, replyText) = when (conversationId) {
            "conv_sarah" -> {
                val response = when {
                    type == "VOICE" -> "Listened to your audio note! The points you made about thread synchronization on Android are spot on. Let's merge the branch."
                    userText.contains("crypt", ignoreCase = true) || userText.contains("key", ignoreCase = true) ->
                        "I've validated the public key derivation with curve25519. The handshake is mathematically secure and passes our zero-knowledge benchmarks."
                    userText.contains("hello", ignoreCase = true) || userText.contains("hey", ignoreCase = true) ->
                        "Hey Alex! Hope your coding session is going great today. Ready for our team sync?"
                    else -> "Got your message! Processing the telemetry now. Let me know if you need me to benchmark anything else on the cluster."
                }
                Triple("user_sarah", "Sarah Chen", response)
            }
            "conv_marcus" -> {
                val response = when {
                    userText.contains("design", ignoreCase = true) || userText.contains("theme", ignoreCase = true) ->
                        "Obsidian dark mode looks so slick! I added micro-haptic feedback on the reactions bar as well."
                    else -> "Love the clean direction! Working on the animated waveform for voice notes next. 🎨"
                }
                Triple("user_marcus", "Marcus Brody", response)
            }
            "conv_team" -> {
                Triple("user_elena", "Elena Rostova", "Verified the pull request on github! CI/CD is all green. 🚀")
            }
            "conv_elena" -> {
                Triple("user_elena", "Elena Rostova", "Thanks for checking in! Key audit report is ready to download.")
            }
            "conv_bot" -> {
                val botReply = "⚡ [Nova AI System]: Command received. End-to-end encrypted packet verified with 256-bit post-quantum security."
                Triple("user_bot", "Nova Assistant", botReply)
            }
            else -> {
                Triple("user_peer", conv.title, "Thank you for reaching out! I've received your note and will get back to you shortly.")
            }
        }

        // Show typing indicator after 1 second
        delay(1000)
        _typingStatus.value = _typingStatus.value + (conversationId to peerName)

        // Simulate typing delay
        delay(1800)
        _typingStatus.value = _typingStatus.value - conversationId

        val replyMsgId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val replyMessage = MessageEntity(
            id = replyMsgId,
            conversationId = conversationId,
            senderId = peerId,
            senderName = peerName,
            text = replyText,
            timestamp = now,
            status = "READ"
        )
        database.messageDao().insertMessage(replyMessage)
        database.conversationDao().updateLastMessage(conversationId, replyText, now)
    }

    suspend fun addReaction(messageId: String, emoji: String) {
        val msg = database.messageDao().getMessageById(messageId) ?: return
        val currentReactions = msg.reactions
        val updated = if (currentReactions.isBlank()) {
            "$emoji:1"
        } else if (currentReactions.contains(emoji)) {
            // Already contains emoji, let's keep or increment
            val parts = currentReactions.split(",").map {
                val (e, countStr) = it.split(":")
                if (e == emoji) "$e:${(countStr.toIntOrNull() ?: 1) + 1}" else it
            }
            parts.joinToString(",")
        } else {
            "$currentReactions,$emoji:1"
        }
        database.messageDao().updateReactions(messageId, updated)
    }

    suspend fun togglePinMessage(messageId: String, isPinned: Boolean) {
        database.messageDao().setPinned(messageId, !isPinned)
    }

    suspend fun deleteMessage(messageId: String) {
        database.messageDao().deleteMessage(messageId)
    }

    suspend fun editMessage(messageId: String, newText: String) {
        val msg = database.messageDao().getMessageById(messageId) ?: return
        val updated = msg.copy(text = newText, isEdited = true)
        database.messageDao().updateMessage(updated)
    }

    // Voice playback simulation
    fun togglePlayVoice(messageId: String, durationMs: Long) {
        if (_playingVoiceId.value == messageId) {
            voicePlaybackJob?.cancel()
            _playingVoiceId.value = null
        } else {
            voicePlaybackJob?.cancel()
            _playingVoiceId.value = messageId
            voicePlaybackJob = scope.launch {
                val steps = 40
                val interval = (durationMs / steps).coerceAtLeast(50L)
                for (i in 0..steps) {
                    if (_playingVoiceId.value != messageId) break
                    val progress = i.toFloat() / steps
                    _voiceProgress.value = _voiceProgress.value + (messageId to progress)
                    delay(interval)
                }
                _playingVoiceId.value = null
                _voiceProgress.value = _voiceProgress.value + (messageId to 0f)
            }
        }
    }

    // Calling features
    fun startCall(peerId: String, peerName: String, peerAvatar: String, isVideo: Boolean) {
        val callInfo = ActiveCallInfo(
            peerId = peerId,
            peerName = peerName,
            peerAvatar = peerAvatar,
            isVideo = isVideo,
            status = "CONNECTING"
        )
        _activeCall.value = callInfo

        scope.launch {
            delay(1500)
            if (_activeCall.value?.callId == callInfo.callId) {
                _activeCall.value = _activeCall.value?.copy(status = "RINGING")
            }
            delay(2000)
            if (_activeCall.value?.callId == callInfo.callId) {
                _activeCall.value = _activeCall.value?.copy(status = "CONNECTED")
                startCallDurationTimer()
            }
        }
    }

    private fun startCallDurationTimer() {
        callTimerJob?.cancel()
        callTimerJob = scope.launch {
            while (_activeCall.value?.status == "CONNECTED") {
                delay(1000)
                _activeCall.value = _activeCall.value?.let { it.copy(durationSeconds = it.durationSeconds + 1) }
            }
        }
    }

    fun toggleCallMute() {
        _activeCall.value = _activeCall.value?.let { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleCallSpeaker() {
        _activeCall.value = _activeCall.value?.let { it.copy(isSpeaker = !it.isSpeaker) }
    }

    fun toggleCallVideo() {
        _activeCall.value = _activeCall.value?.let { it.copy(isVideoEnabled = !it.isVideoEnabled) }
    }

    fun flipCamera() {
        _activeCall.value = _activeCall.value?.let { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun endCall() {
        val current = _activeCall.value
        callTimerJob?.cancel()
        _activeCall.value = null

        if (current != null) {
            scope.launch {
                val callRecord = CallEntity(
                    id = current.callId,
                    peerId = current.peerId,
                    peerName = current.peerName,
                    peerAvatar = current.peerAvatar,
                    isVideo = current.isVideo,
                    isOutgoing = true,
                    status = if (current.durationSeconds > 0) "COMPLETED" else "DECLINED",
                    timestamp = System.currentTimeMillis(),
                    durationSeconds = current.durationSeconds
                )
                database.callDao().insertCall(callRecord)
            }
        }
    }

    // Story / Status operations
    suspend fun postStatus(caption: String, bgGradientIndex: Int) {
        val currentUser = database.userDao().getCurrentUserSync()
        val status = StoryEntity(
            id = UUID.randomUUID().toString(),
            authorId = currentUser?.id ?: "user_me",
            authorName = currentUser?.name ?: "You",
            authorAvatar = currentUser?.avatarUrl ?: "",
            caption = caption,
            bgGradientIndex = bgGradientIndex,
            timestamp = System.currentTimeMillis()
        )
        database.storyDao().insertStatus(status)
    }

    suspend fun markStatusViewed(statusId: String) {
        database.storyDao().markViewed(statusId)
    }

    // Multi-Account Switcher
    suspend fun switchUserAccount(targetUserId: String) {
        database.userDao().clearCurrentUserFlag()
        database.userDao().setCurrentUserFlag(targetUserId)
    }

    suspend fun updateProfile(name: String, bio: String, statusPhrase: String) {
        val current = database.userDao().getCurrentUserSync() ?: return
        val updated = current.copy(name = name, bio = bio, statusPhrase = statusPhrase)
        database.userDao().updateUser(updated)
    }

    suspend fun createNewConversation(title: String, isGroup: Boolean, initialMessage: String): String {
        val convId = "conv_" + UUID.randomUUID().toString().take(8)
        val now = System.currentTimeMillis()
        val conv = ConversationEntity(
            id = convId,
            title = title,
            isGroup = isGroup,
            unreadCount = 0,
            lastMessageSnippet = initialMessage,
            lastMessageTimestamp = now
        )
        database.conversationDao().insertConversation(conv)
        sendMessage(convId, initialMessage)
        return convId
    }

    fun searchAllMessages(query: String): Flow<List<MessageEntity>> {
        return database.messageDao().searchMessages(query)
    }
}
