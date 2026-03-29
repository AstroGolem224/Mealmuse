package com.mealmuse.domain.repository

import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.Ingredient
import com.mealmuse.domain.model.IngredientCategory
import kotlinx.coroutines.flow.Flow

interface FridgeRepository {
    fun getIngredients(): Flow<Result<List<Ingredient>>>
    suspend fun getIngredientById(id: String): Result<Ingredient?>
    fun getByCategory(category: IngredientCategory): Flow<Result<List<Ingredient>>>
    fun getExpiringSoon(): Flow<Result<List<Ingredient>>>
    suspend fun addIngredient(ingredient: Ingredient): Result<Ingredient>
    suspend fun updateIngredient(ingredient: Ingredient): Result<Ingredient>
    suspend fun deleteIngredient(id: String): Result<Unit>
    fun searchIngredients(query: String): Flow<Result<List<Ingredient>>>
}
