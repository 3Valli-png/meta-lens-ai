package com.metalens.app.history

data class ConversationHistoryMessage(
    val role: ConversationHistoryRole,
    val text: String,
)

enum class ConversationHistoryRole {
    User,
    Ai,
}

data class ConversationHistoryRecord(
    val id: String,
    val startedAtMs: Long,
    val messages: List<ConversationHistoryMessage>,
)

