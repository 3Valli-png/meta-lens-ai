package com.metalens.app.conversation

import java.util.UUID

data class ConversationUiState(
    val status: ConversationStatus = ConversationStatus.Idle,
    val isUserSpeaking: Boolean = false,
    /**
     * Local conversation identity used for history persistence.
     * Set when a new live session starts; cleared after it is persisted.
     */
    val activeConversationId: String? = null,
    /**
     * Epoch millis when the conversation started (used as the history list title).
     */
    val conversationStartedAtMs: Long? = null,
    /**
     * Snapshot of messages.size when the conversation started.
     * This allows us to keep transcript in UI while persisting only the new segment per session.
     */
    val conversationStartMessageIndex: Int? = null,
    val messages: List<ChatMessage> = emptyList(),
    val recentError: String? = null,
)

enum class ChatRole {
    User,
    Ai,
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: ChatRole,
    val text: String,
)

enum class ConversationStatus {
    Idle,
    Connecting,
    Listening,
    Speaking,
    Error,
}

