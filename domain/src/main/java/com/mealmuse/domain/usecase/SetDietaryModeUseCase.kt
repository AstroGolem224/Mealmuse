package com.mealmuse.domain.usecase

import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.DietaryMode
import com.mealmuse.domain.model.UserPreferences
import javax.inject.Inject

class SetDietaryModeUseCase @Inject constructor() {
    operator fun invoke(currentPreferences: UserPreferences, mode: DietaryMode): Result<UserPreferences> {
        return try {
            val updated = currentPreferences.copy(
                dietaryModes = listOf(mode),
                maxCalories = when (mode) {
                    is DietaryMode.Keto -> 1800
                    is DietaryMode.LowCarb -> 2000
                    is DietaryMode.Vegetarian -> 2200
                    is DietaryMode.Vegan -> 2200
                    is DietaryMode.Paleo -> 2000
                    is DietaryMode.CalorieDeficit -> 1500
                    is DietaryMode.Custom -> mode.maxCalories ?: currentPreferences.maxCalories
                },
                maxCarbs = when (mode) {
                    is DietaryMode.Keto -> 20
                    is DietaryMode.LowCarb -> 100
                    is DietaryMode.Paleo -> 150
                    is DietaryMode.CalorieDeficit -> 150
                    is DietaryMode.Custom -> mode.maxCarbs ?: currentPreferences.maxCarbs
                    else -> currentPreferences.maxCarbs
                },
                minProtein = when (mode) {
                    is DietaryMode.CalorieDeficit -> 100
                    is DietaryMode.Custom -> mode.minProtein ?: currentPreferences.minProtein
                    else -> currentPreferences.minProtein
                }
            )
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
