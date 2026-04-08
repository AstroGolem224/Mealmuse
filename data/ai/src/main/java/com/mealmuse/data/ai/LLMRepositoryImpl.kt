package com.mealmuse.data.ai

import android.util.Log
import com.mealmuse.core.common.Result
import com.mealmuse.core.common.suspendResult
import com.mealmuse.domain.model.LLMProvider
import com.mealmuse.domain.model.LLMSettings
import com.mealmuse.domain.model.MealPlan
import com.mealmuse.domain.model.Recipe
import com.mealmuse.domain.repository.LLMRepository
import com.mealmuse.domain.repository.RecipeImprovement
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class LLMRepositoryImpl @Inject constructor(
    private val providerFactory: LLMProviderFactory,
    @ApplicationContext private val context: android.content.Context
) : LLMRepository {

    companion object {
        private const val TAG = "LLMRepository"
    }

    private val settingsStore = LLMSettingsStore(context)
    private val _settingsFlow = MutableStateFlow(settingsStore.getSettings())
    override fun getLLMSettingsFlow(): Flow<LLMSettings> = _settingsFlow.asStateFlow()

    override suspend fun generateMealPlan(prompt: String, settings: LLMSettings): Result<MealPlan> =
        try {
            suspendResult {
                Log.d(TAG, "Generating meal plan with model: ${settings.model}, provider: ${settings.provider}")
                val provider = providerFactory.createProvider(settings.provider)
                val rawResponse = provider.generateContent(prompt, settings.apiKey, settings.model)
                Log.d(TAG, "LLM Response length: ${rawResponse.length}")
                Log.d(TAG, "LLM Response: ${rawResponse.take(300)}")

                if (rawResponse.contains("error", ignoreCase = true) ||
                    rawResponse.contains("Cannot read", ignoreCase = true) ||
                    rawResponse.isBlank()) {
                    throw Exception("LLM returned an error or empty response: $rawResponse")
                }

                val cleanedJson = cleanJsonResponse(rawResponse)
                MealPlanParser.parse(cleanedJson)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate meal plan: ${e.message}", e)
            Result.Failure(e)
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

    override suspend fun getAvailableModels(provider: LLMProvider, apiKey: String): Result<List<String>> =
        suspendResult {
            val llmProvider = providerFactory.createProvider(provider)
            llmProvider.getAvailableModels(apiKey)
        }

    override suspend fun getLLMSettings(): Result<LLMSettings> =
        suspendResult { _settingsFlow.value }

    override suspend fun saveLLMSettings(settings: LLMSettings): Result<Unit> =
        suspendResult {
            settingsStore.saveSettings(settings)
            _settingsFlow.update { settings }
        }

    private fun cleanJsonResponse(raw: String): String {
        var cleaned = raw.trim()
        if (cleaned.startsWith("```json")) cleaned = cleaned.removePrefix("```json")
        if (cleaned.startsWith("```")) cleaned = cleaned.removePrefix("```")
        if (cleaned.endsWith("```")) cleaned = cleaned.removeSuffix("```")
        return cleaned.trim()
    }
}