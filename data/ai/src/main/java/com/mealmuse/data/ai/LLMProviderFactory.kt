package com.mealmuse.data.ai

import com.mealmuse.data.ai.provider.OpenAIProvider
import com.mealmuse.data.ai.provider.AnthropicProvider
import com.mealmuse.data.ai.provider.OpenRouterProvider
import com.mealmuse.data.ai.provider.NIMProvider
import com.mealmuse.domain.model.LLMProvider as LLMProviderType
import javax.inject.Inject

class LLMProviderFactory @Inject constructor(
    private val openAIProvider: OpenAIProvider,
    private val anthropicProvider: AnthropicProvider,
    private val openRouterProvider: OpenRouterProvider,
    private val nimProvider: NIMProvider
) {
    fun createProvider(type: LLMProviderType): LLMProvider = when (type) {
        LLMProviderType.OPENAI -> openAIProvider
        LLMProviderType.ANTHROPIC -> anthropicProvider
        LLMProviderType.OPENROUTER -> openRouterProvider
        LLMProviderType.NIM -> nimProvider
    }
}
