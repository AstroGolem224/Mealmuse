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

class NIMProvider @Inject constructor() : LLMProvider {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .build()

    private val defaultBaseUrl = "https://integrate.api.nvidia.com/v1"

    override suspend fun generateContent(prompt: String, apiKey: String, model: String): String = withContext(Dispatchers.IO) {
        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", "You are a helpful assistant. Always respond with valid JSON only, no markdown.")
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })
        }

        val body = JSONObject().apply {
            put("model", model)
            put("messages", messages)
            put("temperature", 0.7)
            put("max_tokens", 4096)
        }.toString()

        val request = Request.Builder()
            .url("$defaultBaseUrl/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response from NIM")

        if (!response.isSuccessful) {
            throw Exception("NIM error: ${response.code} - $responseBody")
        }

        val json = JSONObject(responseBody)
        json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    override suspend fun validateKey(apiKey: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val request = Request.Builder()
                .url("$defaultBaseUrl/models")
                .addHeader("Authorization", "Bearer $apiKey")
                .get()
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getAvailableModels(apiKey: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$defaultBaseUrl/models")
                .addHeader("Authorization", "Bearer $apiKey")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            if (!response.isSuccessful) return@withContext emptyList()

            val data = JSONObject(body).getJSONArray("data")
            (0 until data.length()).map { data.getJSONObject(it).getString("id") }
        } catch (e: Exception) {
            emptyList()
        }
    }
}