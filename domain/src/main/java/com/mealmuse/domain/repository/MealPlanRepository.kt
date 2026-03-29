package com.mealmuse.domain.repository

import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.MealPlan
import kotlinx.coroutines.flow.Flow

interface MealPlanRepository {
    fun getMealPlans(): Flow<Result<List<MealPlan>>>
    suspend fun getMealPlanById(id: String): Result<MealPlan?>
    suspend fun saveMealPlan(mealPlan: MealPlan): Result<MealPlan>
    suspend fun deleteMealPlan(id: String): Result<Unit>
}
