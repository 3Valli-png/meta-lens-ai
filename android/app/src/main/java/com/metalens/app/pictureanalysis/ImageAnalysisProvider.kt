package com.metalens.app.pictureanalysis

enum class ImageAnalysisProvider(val storageValue: String) {
    OPENAI("openai"),
    ANTHROPIC("anthropic");

    companion object {
        fun fromStorage(value: String?): ImageAnalysisProvider =
            values().firstOrNull { it.storageValue == value } ?: OPENAI
    }
}
