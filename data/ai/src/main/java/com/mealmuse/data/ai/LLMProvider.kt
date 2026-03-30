package com.mealmuse.data.ai

interface LLMProvider {
    suspend fun generateContent(prompt: String, apiKey: String, model: String): String
    suspend fun validateKey(apiKey: String): Boolean
    suspend fun getAvailableModels(apiKey: String): List<String> = emptyList()
}
