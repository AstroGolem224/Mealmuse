package com.mealmuse.domain.usecase

import com.mealmuse.core.common.Result
import com.mealmuse.core.common.suspendResult
import com.mealmuse.domain.model.Recipe
import com.mealmuse.domain.repository.LLMRepository
import com.mealmuse.domain.repository.RecipeImprovement
import javax.inject.Inject

class ImproveRecipeUseCase @Inject constructor(
    private val llmRepository: LLMRepository
) {
    suspend operator fun invoke(recipe: Recipe, focus: String = "health"): Result<RecipeImprovement> = suspendResult {
        val settingsResult = llmRepository.getLLMSettings()
        val settings = (settingsResult as? Result.Success)?.data
            ?: throw IllegalStateException("LLM not configured")

        val prompt = buildString {
            appendLine("Improve this recipe focusing on: $focus")
            appendLine("Recipe: ${recipe.name}")
            appendLine("Description: ${recipe.description}")
            appendLine("Ingredients: (from recipe)")
            appendLine("Instructions: ${recipe.instructions.joinToString("\n")}")
            appendLine("Current macros: ${recipe.calories} cal, ${recipe.protein}g protein, ${recipe.carbs}g carbs, ${recipe.fat}g fat")
            appendLine("Return improved recipe as JSON with improvedRecipe, changes, and score fields.")
        }

        val result = llmRepository.improveRecipe(prompt, settings)
        (result as? Result.Success)?.data ?: throw (result as Result.Failure).exception
    }
}
