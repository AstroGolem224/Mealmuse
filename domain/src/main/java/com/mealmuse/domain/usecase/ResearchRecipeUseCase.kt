package com.mealmuse.domain.usecase

import com.mealmuse.core.common.Result
import com.mealmuse.core.common.suspendResult
import com.mealmuse.domain.model.Recipe
import com.mealmuse.domain.repository.LLMRepository
import com.mealmuse.domain.repository.RecipeSearchRepository
import javax.inject.Inject

class ResearchRecipeUseCase @Inject constructor(
    private val recipeSearchRepository: RecipeSearchRepository,
    private val llmRepository: LLMRepository
) {
    suspend operator fun invoke(query: String): Result<List<Recipe>> = suspendResult {
        val settingsResult = llmRepository.getLLMSettings()
        val settings = (settingsResult as? Result.Success)?.data
            ?: throw IllegalStateException("LLM not configured")

        val webResultsResult = recipeSearchRepository.searchWeb(query)
        val webRecipes = (webResultsResult as? Result.Success)?.data
        if (!webRecipes.isNullOrEmpty()) {
            return@suspendResult webRecipes
        }

        // If web search failed or returned empty, fall back to AI research
        val prompt = buildString {
            appendLine("Research and suggest 3 recipes for: $query")
            appendLine("Return as JSON array of Recipe objects with id, name, description, instructions, macros.")
        }
        val aiResult = llmRepository.researchRecipes(prompt, settings)
        when (aiResult) {
            is Result.Success -> aiResult.data
            is Result.Failure -> throw aiResult.exception
            else -> throw IllegalStateException("AI research returned unknown result")
        }
    }
}
