package com.mealmuse.data.local.repository

import com.mealmuse.core.common.Result
import com.mealmuse.core.common.asResult
import com.mealmuse.data.local.dao.IngredientDao
import com.mealmuse.data.local.mapper.toDomain
import com.mealmuse.data.local.mapper.toEntity
import com.mealmuse.domain.model.Ingredient
import com.mealmuse.domain.model.IngredientCategory
import com.mealmuse.domain.repository.FridgeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomFridgeRepository @Inject constructor(
    private val ingredientDao: IngredientDao
) : FridgeRepository {

    override fun getIngredients(): Flow<Result<List<Ingredient>>> =
        ingredientDao.getAll()
            .map { entities -> entities.map { it.toDomain() } }
            .asResult()

    override suspend fun getIngredientById(id: String): Result<Ingredient?> = try {
        val entity = ingredientDao.getById(id)
        Result.success(entity?.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getByCategory(category: IngredientCategory): Flow<Result<List<Ingredient>>> =
        ingredientDao.getByCategory(category.name)
            .map { entities -> entities.map { it.toDomain() } }
            .asResult()

    override fun getExpiringSoon(): Flow<Result<List<Ingredient>>> {
        val threshold = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000)
        return ingredientDao.getExpiringSoon(threshold)
            .map { entities -> entities.map { it.toDomain() } }
            .asResult()
    }

    override suspend fun addIngredient(ingredient: Ingredient): Result<Ingredient> = try {
        ingredientDao.insert(ingredient.toEntity())
        Result.success(ingredient)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateIngredient(ingredient: Ingredient): Result<Ingredient> = try {
        ingredientDao.update(ingredient.toEntity())
        Result.success(ingredient)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteIngredient(id: String): Result<Unit> = try {
        ingredientDao.deleteById(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun searchIngredients(query: String): Flow<Result<List<Ingredient>>> =
        ingredientDao.search(query)
            .map { entities -> entities.map { it.toDomain() } }
            .asResult()
}
