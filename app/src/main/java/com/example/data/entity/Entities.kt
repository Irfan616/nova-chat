package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val handle: String,
    val phone: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val bio: String = "Building the future with NovaChat ✨",
    val statusPhrase: String = "Available",
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val isCurrentUser: Boolean = false
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val avatarUrl: String = "",
    val isGroup: Boolean = false,
    val isChannel: Boolean = false,
    val unreadCount: Int = 0,
    val lastMessageSnippet: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val disappearingSeconds: Long = 0L,
    val draft: String = ""
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val type: String = "TEXT", // TEXT, IMAGE, VOICE, VIDEO, DOC, LOCATION, SYSTEM
    val mediaUrl: String? = null,
    val mediaDurationMs: Long = 0L,
    val mediaSizeText: String? = null,
    val replyToId: String? = null,
    val replyToSnippet: String? = null,
    val replyToSender: String? = null,
    val status: String = "READ", // PENDING, SENT, DELIVERED, READ, FAILED
    val timestamp: Long = System.currentTimeMillis(),
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val isPinned: Boolean = false,
    val reactions: String = "" // e.g. "❤️:2,🔥:1"
)

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey val id: String,
    val peerId: String,
    val peerName: String,
    val peerAvatar: String = "",
    val isVideo: Boolean = false,
    val isOutgoing: Boolean = true,
    val status: String = "COMPLETED", // MISSED, COMPLETED, DECLINED
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String = "",
    val mediaUrl: String? = null,
    val caption: String = "",
    val bgGradientIndex: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val viewCount: Int = 1,
    val isViewed: Boolean = false
)

@Entity(tableName = "communities")
data class CommunitySpaceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val avatarUrl: String = "",
    val memberCount: Int = 128,
    val channels: String = "general,announcements,ideas"
)
