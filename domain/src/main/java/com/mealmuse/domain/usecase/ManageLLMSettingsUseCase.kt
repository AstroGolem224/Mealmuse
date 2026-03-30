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

    suspend fun fetchModelsForProvider(provider: LLMProvider, apiKey: String): List<String> {
        if (apiKey.isBlank()) return getModelsForProvider(provider)
        val result = llmRepository.getAvailableModels(provider, apiKey)
        return if (result is Result.Success && result.data.isNotEmpty()) result.data
        else getModelsForProvider(provider)
    }

    fun getModelsForProvider(provider: LLMProvider): List<String> = when (provider) {
        LLMProvider.OPENAI -> listOf("gpt-4o", "gpt-4o-mini", "gpt-3.5-turbo")
        LLMProvider.ANTHROPIC -> listOf("claude-sonnet-4-20250514", "claude-3-5-sonnet-20241022", "claude-3-haiku-20240307")
        LLMProvider.OPENROUTER -> listOf(
            "google/gemma-3-4b-it:free",
            "google/gemma-3n-e2b-it:free",
            "google/gemma-3n-e4b-it:free",
            "google/gemma-3-12b-it:free",
            "google/gemma-3-27b-it:free",
            "meta-llama/llama-3.2-3b-instruct:free",
            "meta-llama/llama-3.3-70b-instruct:free",
            "nvidia/nemotron-nano-9b-v2:free",
            "nvidia/nemotron-nano-12b-v2-vl:free",
            "nvidia/nemotron-3-super-120b-a12b:free",
            "z-ai/glm-4.5-air:free",
            "qwen/qwen3-coder:free",
            "cognitivecomputations/dolphin-mistral-24b-venice-edition:free",
            "nousresearch/hermes-3-llama-3.1-405b:free",
            "qwen/qwen3-next-80b-a3b-instruct:free",
            "stepfun/step-3.5-flash:free",
            "openrouter/free"
        )
        LLMProvider.NIM -> listOf(
            "meta/llama-3.1-8b-instruct",   // fastest, recommended
            "meta/llama-3.2-3b-instruct",
            "meta/llama-3.1-70b-instruct",
            "google/gemma-2-9b-it",
            "google/gemma-2-27b-it",
            "qwen/qwen2.5-7b-instruct",
            "mistralai/mistral-7b-instruct-v0.3",
            "nvidia/nemotron-3-nano-30b-a3b",
            "nvidia/nemotron-3-super-120b-a12b"
        )
    }
}
