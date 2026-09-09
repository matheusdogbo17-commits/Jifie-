package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SenderType {
    ME,
    OTHER
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

enum class MessageType {
    TEXT,
    AUDIO,
    IMAGE,
    STICKER
}

data class Story(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatarColor: Long,
    val bgGradients: List<Long>,
    val caption: String,
    val stickerText: String? = null,
    val stickerEmoji: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isViewed: Boolean = false
)

data class UserStoryGroup(
    val userId: String,
    val userName: String,
    val userAvatarColor: Long,
    val isMe: Boolean = false,
    val stories: List<Story> = emptyList(),
    val hasUnviewed: Boolean = true
)

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String,
    val participantName: String,
    val participantPhone: String = "",
    val participantAvatarColor: Long = 0xFF2563EB,
    val participantStatus: String = "En ligne",
    val isOnline: Boolean = true,
    val isPinned: Boolean = false,
    val isGroup: Boolean = false,
    val groupMembersCount: Int = 0,
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isTyping: Boolean = false
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val text: String,
    val senderType: SenderType,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.READ,
    val messageType: MessageType = MessageType.TEXT,
    val mediaUri: String? = null,
    val audioDurationSec: Int = 0,
    val reaction: String? = null
)

data class Contact(
    val id: String,
    val name: String,
    val phone: String,
    val statusMessage: String,
    val avatarColor: Long,
    val isOnline: Boolean = false
)
