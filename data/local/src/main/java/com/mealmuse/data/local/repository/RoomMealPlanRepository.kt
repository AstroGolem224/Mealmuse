package com.mealmuse.data.local.repository

import com.mealmuse.core.common.Result
import com.mealmuse.core.common.asResult
import com.mealmuse.data.local.dao.MealPlanDao
import com.mealmuse.data.local.mapper.toDomain
import com.mealmuse.data.local.mapper.toEntity
import com.mealmuse.domain.model.MealPlan
import com.mealmuse.domain.repository.MealPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomMealPlanRepository @Inject constructor(
    private val mealPlanDao: MealPlanDao
) : MealPlanRepository {

    override fun getMealPlans(): Flow<Result<List<MealPlan>>> =
        mealPlanDao.getAll()
            .map { entities ->
                entities.map { entity ->
                    val entries = mealPlanDao.getEntriesByPlanId(entity.id)
                    entity.toDomain(entries)
                }
            }
            .asResult()

    override suspend fun getMealPlanById(id: String): Result<MealPlan?> = try {
        val entity = mealPlanDao.getById(id)
        val entries = if (entity != null) {
            mealPlanDao.getEntriesByPlanId(id)
        } else emptyList()
        Result.success(entity?.toDomain(entries))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun saveMealPlan(mealPlan: MealPlan): Result<MealPlan> = try {
        mealPlanDao.insert(mealPlan.toEntity())
        mealPlanDao.deleteEntriesByPlanId(mealPlan.id)
        mealPlanDao.insertEntries(mealPlan.entries.map { it.toEntity() })
        Result.success(mealPlan)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteMealPlan(id: String): Result<Unit> = try {
        mealPlanDao.deleteById(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
