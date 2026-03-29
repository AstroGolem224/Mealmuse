package com.mealmuse.domain.usecase

import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.Recipe
import com.mealmuse.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(query: String): Flow<Result<List<Recipe>>> {
        return if (query.isBlank()) {
            recipeRepository.getRecipes()
        } else {
            recipeRepository.searchRecipes(query)
        }
    }
}
