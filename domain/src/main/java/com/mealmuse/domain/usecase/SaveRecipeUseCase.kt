package com.mealmuse.domain.usecase

import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.Recipe
import com.mealmuse.domain.repository.RecipeRepository
import javax.inject.Inject

class SaveRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(recipe: Recipe): Result<Recipe> {
        val now = System.currentTimeMillis()
        val recipeWithTimestamps = recipe.copy(
            createdAt = recipe.createdAt.takeIf { it > 0 } ?: now,
            updatedAt = now
        )
        return recipeRepository.saveRecipe(recipeWithTimestamps)
    }
}
