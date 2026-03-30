package com.mealmuse.domain.repository

import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.LLMSettings
import com.mealmuse.domain.model.MealPlan
import com.mealmuse.domain.model.Recipe

interface LLMRepository {
    suspend fun generateMealPlan(prompt: String, settings: LLMSettings): Result<MealPlan>
    suspend fun researchRecipes(prompt: String, settings: LLMSettings): Result<List<Recipe>>
    suspend fun improveRecipe(prompt: String, settings: LLMSettings): Result<RecipeImprovement>
    suspend fun validateApiKey(provider: com.mealmuse.domain.model.LLMProvider, apiKey: String): Result<Boolean>
    suspend fun getAvailableModels(provider: com.mealmuse.domain.model.LLMProvider, apiKey: String): Result<List<String>>
    suspend fun getLLMSettings(): Result<LLMSettings>
    suspend fun saveLLMSettings(settings: LLMSettings): Result<Unit>
}

data class RecipeImprovement(
    val improvedRecipe: Recipe,
    val changes: List<RecipeChange>,
    val score: Int
)

data class RecipeChange(
    val field: String,
    val oldValue: String,
    val newValue: String
)
