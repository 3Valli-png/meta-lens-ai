package com.metalens.app.pictureanalysis

import android.graphics.Bitmap
import android.util.Base64
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ClaudeImageAnalysisService(
    private val client: OkHttpClient = defaultClient(),
) {
    companion object {
        private const val ENDPOINT = "https://api.anthropic.com/v1/messages"
        private const val ANTHROPIC_VERSION = "2023-06-01"
        private const val MAX_TOKENS = 512
        private val JSON = "application/json; charset=utf-8".toMediaType()

        private fun defaultClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .callTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }
    }

    fun analyzeImage(
        apiKey: String,
        model: String,
        prompt: String,
        bitmap: Bitmap,
    ): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("Missing Anthropic API key"))
        }

        return runCatching {
            val jpegBytes = bitmap.toJpegBytes(quality = 85)
            val base64 = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)

            val imageBlock =
                JSONObject()
                    .put("type", "image")
                    .put(
                        "source",
                        JSONObject()
                            .put("type", "base64")
                            .put("media_type", "image/jpeg")
                            .put("data", base64),
                    )

            val userContent =
                JSONArray()
                    .put(imageBlock)
                    .put(
                        JSONObject()
                            .put("type", "text")
                            .put("text", if (prompt.isBlank()) "Describe the image." else prompt),
                    )

            val messages =
                JSONArray()
                    .put(
                        JSONObject()
                            .put("role", "user")
                            .put("content", userContent),
                    )

            val payload =
                JSONObject()
                    .put("model", model)
                    .put("max_tokens", MAX_TOKENS)
                    .put("messages", messages)
                    .toString()

            val request =
                Request.Builder()
                    .url(ENDPOINT)
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", ANTHROPIC_VERSION)
                    .addHeader("content-type", "application/json")
                    .post(payload.toRequestBody(JSON))
                    .build()

            client.newCall(request).execute().use { resp ->
                val body = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    throw IllegalStateException("HTTP ${resp.code}: ${body.take(500)}")
                }
                parseResponseText(body)
            }
        }
    }

    private fun parseResponseText(rawJson: String): String {
        val obj = JSONObject(rawJson)
        val content = obj.optJSONArray("content") ?: JSONArray()
        val sb = StringBuilder()
        for (i in 0 until content.length()) {
            val block = content.optJSONObject(i) ?: continue
            if (block.optString("type") != "text") continue
            val text = block.optString("text")
            if (text.isNotBlank()) {
                if (sb.isNotEmpty()) sb.append('\n')
                sb.append(text.trim())
            }
        }
        val result = sb.toString().trim()
        if (result.isBlank()) {
            throw IllegalStateException("Empty response")
        }
        return result
    }
}

private fun Bitmap.toJpegBytes(quality: Int): ByteArray {
    val out = java.io.ByteArrayOutputStream()
    if (!compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(1, 100), out)) {
        throw IllegalStateException("Failed to encode JPEG")
    }
    return out.toByteArray()
}
