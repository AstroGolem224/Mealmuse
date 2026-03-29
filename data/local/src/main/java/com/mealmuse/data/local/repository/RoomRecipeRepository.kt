package com.mealmuse.data.local.repository

import com.mealmuse.core.common.Result
import com.mealmuse.core.common.asResult
import com.mealmuse.data.local.dao.RecipeDao
import com.mealmuse.data.local.mapper.toDomain
import com.mealmuse.data.local.mapper.toEntity
import com.mealmuse.domain.model.Recipe
import com.mealmuse.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomRecipeRepository @Inject constructor(
    private val recipeDao: RecipeDao
) : RecipeRepository {

    override fun getRecipes(): Flow<Result<List<Recipe>>> =
        recipeDao.getAll()
            .map { entities -> entities.map { it.toDomain() } }
            .asResult()

    override suspend fun getRecipeById(id: String): Result<Recipe?> = try {
        val entity = recipeDao.getById(id)
        Result.success(entity?.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun searchRecipes(query: String): Flow<Result<List<Recipe>>> =
        recipeDao.search(query)
            .map { entities -> entities.map { it.toDomain() } }
            .asResult()

    override suspend fun saveRecipe(recipe: Recipe): Result<Recipe> = try {
        recipeDao.insert(recipe.toEntity())
        Result.success(recipe)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateRecipe(recipe: Recipe): Result<Recipe> = try {
        recipeDao.update(recipe.toEntity())
        Result.success(recipe)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteRecipe(id: String): Result<Unit> = try {
        recipeDao.deleteById(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getFavorites(): Flow<Result<List<Recipe>>> =
        recipeDao.getFavorites()
            .map { entities -> entities.map { it.toDomain() } }
            .asResult()

    override suspend fun toggleFavorite(id: String): Result<Unit> = try {
        val recipe = recipeDao.getById(id)
        if (recipe != null) {
            recipeDao.setFavorite(id, !recipe.isFavorite)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
