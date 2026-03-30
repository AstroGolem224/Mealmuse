package com.mealmuse.domain.usecase

import com.mealmuse.domain.model.LLMProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ManageLLMSettingsUseCaseTest {

    private lateinit var useCase: ManageLLMSettingsUseCase

    @Before
    fun setup() {
        // We can't test with real repository, so we test getModelsForProvider which doesn't need one
        // For full integration tests, we'd need a test double repository
        useCase = ManageLLMSettingsUseCase(null)
    }

    @Test
    fun `getModelsForProvider returns OpenAI models`() {
        val models = useCase.getModelsForProvider(LLMProvider.OPENAI)

        assertTrue(models.isNotEmpty())
        assertTrue(models.contains("gpt-4o-mini"))
        assertTrue(models.contains("gpt-4o"))
    }

    @Test
    fun `getModelsForProvider returns Anthropic models`() {
        val models = useCase.getModelsForProvider(LLMProvider.ANTHROPIC)

        assertTrue(models.isNotEmpty())
        assertTrue(models.any { it.contains("claude") })
    }

    @Test
    fun `getModelsForProvider returns OpenRouter models`() {
        val models = useCase.getModelsForProvider(LLMProvider.OPENROUTER)

        assertTrue(models.isNotEmpty())
        assertTrue(models.contains("openrouter/free"))
        assertTrue(models.any { it.contains(":free") })
    }

    @Test
    fun `getModelsForProvider returns NIM models`() {
        val models = useCase.getModelsForProvider(LLMProvider.NIM)

        assertTrue(models.isNotEmpty())
        assertTrue(models.contains("meta/llama-3.1-70b-instruct"))
    }

    @Test
    fun `getAvailableDietaryModes returns all modes`() {
        val modes = useCase.getAvailableDietaryModes()

        assertTrue(modes.isNotEmpty())
        assertTrue(modes.any { it.first is com.mealmuse.domain.model.DietaryMode.Keto })
        assertTrue(modes.any { it.first is com.mealmuse.domain.model.DietaryMode.Vegetarian })
        assertTrue(modes.any { it.first is com.mealmuse.domain.model.DietaryMode.Vegan })
    }
}
