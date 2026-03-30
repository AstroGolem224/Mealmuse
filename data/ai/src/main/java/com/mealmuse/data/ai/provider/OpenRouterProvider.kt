package com.mealmuse.data.ai.provider

import android.util.Log
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

class OpenRouterProvider @Inject constructor() : LLMProvider {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://openrouter.ai/api/v1"

    companion object {
        private const val TAG = "OpenRouterProvider"
    }

    override suspend fun generateContent(prompt: String, apiKey: String, model: String): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "Building request for model: $model")
        
        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })
        }

        val bodyJson = """
            {
                "model": "$model",
                "messages": $messages,
                "temperature": 0.1,
                "max_tokens": 4000
            }
        """.trimIndent()

        Log.d(TAG, "Request body: $bodyJson")

        val request = Request.Builder()
            .url("$baseUrl/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://mealmuse.app")
            .addHeader("X-OpenRouter-Title", "MealMuse")
            .post(bodyJson.toRequestBody("application/json".toMediaType()))
            .build()

        Log.d(TAG, "Sending request...")
        val response = try {
            client.newCall(request).execute()
        } catch (e: Exception) {
            Log.e(TAG, "HTTP request failed: ${e::class.simpleName}: ${e.message}")
            throw Exception("Network error: ${e.message}")
        }
        Log.d(TAG, "Got response with code: ${response.code}")
        val responseBody = response.body?.string() ?: throw Exception("Empty response from OpenRouter")
        
        Log.d(TAG, "Response body length: ${responseBody.length}")
        Log.d(TAG, "Response body preview: ${responseBody.take(500)}")

        if (!response.isSuccessful) {
            val errorMsg = try {
                JSONObject(responseBody).optString("error", responseBody)
            } catch (e: Exception) {
                responseBody
            }
            throw Exception("OpenRouter error ${response.code}: $errorMsg")
        }

        val json = JSONObject(responseBody)
        
        if (json.has("error")) {
            val error = json.getJSONObject("error")
            val message = error.optString("message", responseBody)
            throw Exception("OpenRouter API error: $message")
        }
        
        if (!json.has("choices")) {
            throw Exception("Invalid response - no choices: $responseBody")
        }
        
        val content = json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .optString("content", "")
        
        Log.d(TAG, "Extracted content: ${content.take(200)}")
        
        content
    }

    override suspend fun validateKey(apiKey: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val request = Request.Builder()
                .url("$baseUrl/models")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("HTTP-Referer", "https://mealmuse.app")
                .addHeader("X-OpenRouter-Title", "MealMuse")
                .get()
                .build()

            val response = client.newCall(request).execute()
            response.code == 200
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getAvailableModels(apiKey: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/models")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("HTTP-Referer", "https://mealmuse.app")
                .addHeader("X-OpenRouter-Title", "MealMuse")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            if (!response.isSuccessful) return@withContext emptyList()

            val data = JSONObject(body).getJSONArray("data")
            val freeModels = mutableListOf<String>()
            for (i in 0 until data.length()) {
                val model = data.getJSONObject(i)
                val id = model.optString("id", "")
                val pricing = model.optJSONObject("pricing")
                val promptPrice = pricing?.optString("prompt", "1") ?: "1"
                if (promptPrice == "0" || id.endsWith(":free")) {
                    freeModels.add(id)
                }
            }
            Log.d(TAG, "Found ${freeModels.size} free models on OpenRouter")
            freeModels
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch OpenRouter models: ${e.message}")
            emptyList()
        }
    }
}