package com.mealmuse.domain.model

data class LLMSettings(
    val provider: LLMProvider,
    val apiKey: String,
    val model: String,
    val baseUrl: String? = null,
    val isActive: Boolean = true
)

enum class LLMProvider(val displayName: String) {
    OPENAI("OpenAI"),
    ANTHROPIC("Anthropic"),
    OPENROUTER("OpenRouter"),
    NIM("NVIDIA NIM")
}
