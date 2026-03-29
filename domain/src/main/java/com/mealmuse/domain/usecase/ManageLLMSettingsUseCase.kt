package com.mealmuse.domain.usecase

import com.mealmuse.core.common.Result
import com.mealmuse.core.common.suspendResult
import com.mealmuse.domain.model.LLMProvider
import com.mealmuse.domain.model.LLMSettings
import com.mealmuse.domain.repository.LLMRepository
import javax.inject.Inject

class ManageLLMSettingsUseCase @Inject constructor(
    private val llmRepository: LLMRepository
) {
    suspend fun getSettings(): Result<LLMSettings> = llmRepository.getLLMSettings()

    suspend fun saveSettings(settings: LLMSettings): Result<Unit> =
        llmRepository.saveLLMSettings(settings)

    suspend fun validateKey(provider: LLMProvider, apiKey: String): Result<Boolean> =
        llmRepository.validateApiKey(provider, apiKey)

    suspend fun switchProvider(provider: LLMProvider, apiKey: String, model: String): Result<Unit> = suspendResult {
        val isValid = llmRepository.validateApiKey(provider, apiKey)
        if ((isValid as? Result.Success)?.data != true) {
            throw IllegalArgumentException("Invalid API key for ${provider.displayName}")
        }
        val settings = LLMSettings(
            provider = provider,
            apiKey = apiKey,
            model = model,
            isActive = true
        )
        llmRepository.saveLLMSettings(settings)
    }

    fun getModelsForProvider(provider: LLMProvider): List<String> = when (provider) {
        LLMProvider.OPENAI -> listOf("gpt-4o", "gpt-4o-mini", "gpt-3.5-turbo")
        LLMProvider.ANTHROPIC -> listOf("claude-sonnet-4-20250514", "claude-3-5-sonnet-20241022", "claude-3-haiku-20240307")
        LLMProvider.OPENROUTER -> listOf("auto", "meta-llama/llama-3.1-70b-instruct", "mistralai/mistral-7b-instruct")
        LLMProvider.NIM -> listOf("nvidia/nemotron-4-340b-instruct", "meta/llama-3.1-70b-instruct")
    }
}
