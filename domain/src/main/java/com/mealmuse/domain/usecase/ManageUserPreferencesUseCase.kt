package com.mealmuse.domain.usecase

import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.DietaryMode
import com.mealmuse.domain.model.UserPreferences
import com.mealmuse.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageUserPreferencesUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    fun getPreferences(): Flow<UserPreferences> {
        return userPreferencesRepository.getPreferences()
    }

    suspend fun getPreferencesOnce(): UserPreferences {
        return userPreferencesRepository.getPreferencesOnce()
    }

    suspend fun savePreferences(preferences: UserPreferences): Result<Unit> {
        return userPreferencesRepository.savePreferences(preferences)
    }

    fun getAvailableDietaryModes(): List<Pair<DietaryMode, String>> = listOf(
        DietaryMode.Keto to "Keto",
        DietaryMode.LowCarb to "Low-Carb",
        DietaryMode.Vegetarian to "Vegetarian",
        DietaryMode.Vegan to "Vegan",
        DietaryMode.Paleo to "Paleo",
        DietaryMode.CalorieDeficit to "Calorie Deficit"
    )
}
