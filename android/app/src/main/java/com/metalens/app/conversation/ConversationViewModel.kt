package com.metalens.app.conversation

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow
import androidx.core.content.ContextCompat
import com.metalens.app.history.ConversationHistoryStorage

class ConversationViewModel(
    application: Application,
) : AndroidViewModel(application) {
    val uiState: StateFlow<ConversationUiState> = ConversationRuntime.uiState

    fun start() {
        val intent =
            Intent(getApplication(), ConversationForegroundService::class.java).apply {
                action = ConversationForegroundService.ACTION_START
            }
        ContextCompat.startForegroundService(getApplication(), intent)
    }

    fun stop() {
        // Persist transcript BEFORE stopping the service; stopping can race with resets/navigation.
        ConversationHistoryStorage(getApplication()).saveFromRuntime()
        val intent =
            Intent(getApplication(), ConversationForegroundService::class.java).apply {
                action = ConversationForegroundService.ACTION_STOP
            }
        getApplication<Application>().startService(intent)
    }

    fun stopAndReset() {
        // Persist first, then stop and reset UI state.
        ConversationHistoryStorage(getApplication()).saveFromRuntime()
        val intent =
            Intent(getApplication(), ConversationForegroundService::class.java).apply {
                action = ConversationForegroundService.ACTION_STOP
            }
        getApplication<Application>().startService(intent)
        ConversationRuntime.reset()
    }

    fun sendText(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        val intent =
            Intent(getApplication(), ConversationForegroundService::class.java).apply {
                action = ConversationForegroundService.ACTION_SEND_TEXT
                putExtra(ConversationForegroundService.EXTRA_TEXT, trimmed)
            }
        getApplication<Application>().startService(intent)
    }
}

