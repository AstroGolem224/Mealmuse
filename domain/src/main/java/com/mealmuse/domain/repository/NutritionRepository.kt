package com.mealmuse.domain.repository

import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.Macros

interface NutritionRepository {
    suspend fun getNutrition(foodName: String): Result<Macros>
}
