package com.mealmuse.data.ai

import com.mealmuse.core.common.Result
import com.mealmuse.core.common.suspendResult
import com.mealmuse.domain.model.LLMProvider
import com.mealmuse.domain.model.LLMSettings
import com.mealmuse.domain.model.MealPlan
import com.mealmuse.domain.model.Recipe
import com.mealmuse.domain.repository.LLMRepository
import com.mealmuse.domain.repository.RecipeImprovement
import javax.inject.Inject

class LLMRepositoryImpl @Inject constructor(
    private val providerFactory: LLMProviderFactory,
    private val settingsStore: LLMSettingsStore
) : LLMRepository {

    override suspend fun generateMealPlan(prompt: String, settings: LLMSettings): Result<MealPlan> =
        suspendResult {
            val provider = providerFactory.createProvider(settings.provider)
            val rawResponse = provider.generateContent(prompt, settings.apiKey, settings.model)
            val cleanedJson = cleanJsonResponse(rawResponse)
            MealPlanParser.parse(cleanedJson)
        }

    override suspend fun researchRecipes(prompt: String, settings: LLMSettings): Result<List<Recipe>> =
        suspendResult {
            val provider = providerFactory.createProvider(settings.provider)
            val rawResponse = provider.generateContent(prompt, settings.apiKey, settings.model)
            val cleanedJson = cleanJsonResponse(rawResponse)
            RecipeResearchParser.parse(cleanedJson)
        }

    override suspend fun improveRecipe(prompt: String, settings: LLMSettings): Result<RecipeImprovement> =
        suspendResult {
            val provider = providerFactory.createProvider(settings.provider)
            val rawResponse = provider.generateContent(prompt, settings.apiKey, settings.model)
            val cleanedJson = cleanJsonResponse(rawResponse)
            RecipeImprovementParser.parse(cleanedJson)
        }

    override suspend fun validateApiKey(provider: LLMProvider, apiKey: String): Result<Boolean> =
        suspendResult {
            val llmProvider = providerFactory.createProvider(provider)
            llmProvider.validateKey(apiKey)
        }

    override suspend fun getLLMSettings(): Result<LLMSettings> =
        suspendResult { settingsStore.getSettings() }

    override suspend fun saveLLMSettings(settings: LLMSettings): Result<Unit> =
        suspendResult { settingsStore.saveSettings(settings) }

    private fun cleanJsonResponse(raw: String): String {
        var cleaned = raw.trim()
        if (cleaned.startsWith("```json")) cleaned = cleaned.removePrefix("```json")
        if (cleaned.startsWith("```")) cleaned = cleaned.removePrefix("```")
        if (cleaned.endsWith("```")) cleaned = cleaned.removeSuffix("```")
        return cleaned.trim()
    }
}