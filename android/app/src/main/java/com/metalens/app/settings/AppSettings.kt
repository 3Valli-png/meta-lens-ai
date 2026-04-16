package com.metalens.app.settings

import android.content.Context
import com.meta.wearable.dat.camera.types.VideoQuality
import com.metalens.app.BuildConfig
import com.metalens.app.R
import com.metalens.app.conversation.OpenAIRealtimeClient
import com.metalens.app.pictureanalysis.ImageAnalysisProvider

object AppSettings {
    const val DEFAULT_ANTHROPIC_MODEL = "claude-opus-4-7"

    private const val PREFS_NAME = "meta_lens_ai_settings"
    private const val KEY_OPENAI_API_KEY = "openai_api_key"
    private const val KEY_OPENAI_MODEL = "openai_model"
    private const val KEY_ANTHROPIC_API_KEY = "anthropic_api_key"
    private const val KEY_ANTHROPIC_MODEL = "anthropic_model"
    private const val KEY_IMAGE_ANALYSIS_PROVIDER = "image_analysis_provider"
    private const val KEY_CAMERA_VIDEO_QUALITY = "camera_video_quality"
    private const val KEY_PICTURE_ANALYSIS_SYSTEM_INSTRUCTIONS = "picture_analysis_system_instructions_override"
    private const val KEY_CONVERSATION_SYSTEM_INSTRUCTIONS = "conversation_system_instructions_override"

    fun getOpenAiApiKey(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val fromPrefs = prefs.getString(KEY_OPENAI_API_KEY, null)
        if (!fromPrefs.isNullOrBlank()) return fromPrefs
        return BuildConfig.OPENAI_API_KEY
    }

    fun setOpenAiApiKey(context: Context, apiKey: String) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_OPENAI_API_KEY, apiKey.trim()).apply()
    }

    fun getOpenAiModel(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val fromPrefs = prefs.getString(KEY_OPENAI_MODEL, null)
        if (!fromPrefs.isNullOrBlank()) return fromPrefs
        val fromBuild = BuildConfig.OPENAI_MODEL
        if (fromBuild.isNotBlank()) return fromBuild
        return OpenAIRealtimeClient.DEFAULT_MODEL
    }

    fun setOpenAiModel(context: Context, model: String) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_OPENAI_MODEL, model.trim()).apply()
    }

    fun getAnthropicApiKey(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val fromPrefs = prefs.getString(KEY_ANTHROPIC_API_KEY, null)
        if (!fromPrefs.isNullOrBlank()) return fromPrefs
        return BuildConfig.ANTHROPIC_API_KEY
    }

    fun setAnthropicApiKey(context: Context, apiKey: String) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ANTHROPIC_API_KEY, apiKey.trim()).apply()
    }

    fun getAnthropicModel(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val fromPrefs = prefs.getString(KEY_ANTHROPIC_MODEL, null)
        if (!fromPrefs.isNullOrBlank()) return fromPrefs
        val fromBuild = BuildConfig.ANTHROPIC_MODEL
        if (fromBuild.isNotBlank()) return fromBuild
        return DEFAULT_ANTHROPIC_MODEL
    }

    fun setAnthropicModel(context: Context, model: String) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ANTHROPIC_MODEL, model.trim()).apply()
    }

    fun getImageAnalysisProvider(context: Context): ImageAnalysisProvider {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return ImageAnalysisProvider.fromStorage(prefs.getString(KEY_IMAGE_ANALYSIS_PROVIDER, null))
    }

    fun setImageAnalysisProvider(context: Context, provider: ImageAnalysisProvider) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_IMAGE_ANALYSIS_PROVIDER, provider.storageValue).apply()
    }

    fun getCameraVideoQuality(context: Context): VideoQuality {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_CAMERA_VIDEO_QUALITY, null)?.trim()?.uppercase()
        return when (raw) {
            "LOW" -> VideoQuality.LOW
            "HIGH" -> VideoQuality.HIGH
            "MEDIUM", null, "" -> VideoQuality.MEDIUM
            else -> VideoQuality.MEDIUM
        }
    }

    fun setCameraVideoQuality(context: Context, quality: VideoQuality) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CAMERA_VIDEO_QUALITY, quality.name).apply()
    }

    fun getPictureAnalysisSystemInstructions(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val fromPrefs = prefs.getString(KEY_PICTURE_ANALYSIS_SYSTEM_INSTRUCTIONS, null)?.trim()
        if (!fromPrefs.isNullOrBlank()) return fromPrefs
        return context.getString(R.string.picture_analysis_system_instructions).trim()
    }

    fun setPictureAnalysisSystemInstructions(context: Context, instructions: String) {
        val normalized = instructions.trim()
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (normalized.isBlank()) {
            prefs.edit().remove(KEY_PICTURE_ANALYSIS_SYSTEM_INSTRUCTIONS).apply()
        } else {
            prefs.edit().putString(KEY_PICTURE_ANALYSIS_SYSTEM_INSTRUCTIONS, normalized).apply()
        }
    }

    fun resetPictureAnalysisSystemInstructions(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_PICTURE_ANALYSIS_SYSTEM_INSTRUCTIONS).apply()
    }

    fun getConversationSystemInstructions(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val fromPrefs = prefs.getString(KEY_CONVERSATION_SYSTEM_INSTRUCTIONS, null)?.trim()
        if (!fromPrefs.isNullOrBlank()) return fromPrefs
        return context.getString(R.string.conversation_system_instructions).trim()
    }

    fun setConversationSystemInstructions(context: Context, instructions: String) {
        val normalized = instructions.trim()
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (normalized.isBlank()) {
            prefs.edit().remove(KEY_CONVERSATION_SYSTEM_INSTRUCTIONS).apply()
        } else {
            prefs.edit().putString(KEY_CONVERSATION_SYSTEM_INSTRUCTIONS, normalized).apply()
        }
    }

    fun resetConversationSystemInstructions(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_CONVERSATION_SYSTEM_INSTRUCTIONS).apply()
    }
}

