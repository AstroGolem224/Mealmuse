package com.mealmuse.data.ai.provider

import com.mealmuse.data.ai.LLMProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AnthropicProvider @Inject constructor() : LLMProvider {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://api.anthropic.com/v1"

    override suspend fun generateContent(prompt: String, apiKey: String, model: String): String = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("model", model)
            put("max_tokens", 4096)
            put("system", "You are a helpful assistant. Always respond with valid JSON only, no markdown.")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }.toString()

        val request = Request.Builder()
            .url("$baseUrl/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response from Anthropic")

        if (!response.isSuccessful) {
            throw Exception("Anthropic error: ${response.code} - $responseBody")
        }

        val json = JSONObject(responseBody)
        val content = json.getJSONArray("content")
        content.getJSONObject(0).getString("text")
    }

    override suspend fun validateKey(apiKey: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val body = JSONObject().apply {
                put("model", "claude-3-haiku-20240307")
                put("max_tokens", 1)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "hi")
                    })
                })
            }.toString()

            val request = Request.Builder()
                .url("$baseUrl/messages")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("Content-Type", "application/json")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}