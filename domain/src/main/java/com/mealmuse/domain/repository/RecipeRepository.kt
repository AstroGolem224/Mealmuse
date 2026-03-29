package com.mealmuse.domain.repository

import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getRecipes(): Flow<Result<List<Recipe>>>
    suspend fun getRecipeById(id: String): Result<Recipe?>
    fun searchRecipes(query: String): Flow<Result<List<Recipe>>>
    suspend fun saveRecipe(recipe: Recipe): Result<Recipe>
    suspend fun updateRecipe(recipe: Recipe): Result<Recipe>
    suspend fun deleteRecipe(id: String): Result<Unit>
    fun getFavorites(): Flow<Result<List<Recipe>>>
    suspend fun toggleFavorite(id: String): Result<Unit>
}
