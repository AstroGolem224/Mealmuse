package com.mealmuse.data.local.mapper

import com.mealmuse.data.local.entity.UserPreferencesEntity
import com.mealmuse.domain.model.DietaryMode
import com.mealmuse.domain.model.UserPreferences
import org.json.JSONArray

fun UserPreferencesEntity.toDomain(): UserPreferences {
    val modes = try {
        val json = JSONArray(dietaryModes)
        (0 until json.length()).map { name ->
            when (json.getString(name)) {
                "KETO" -> DietaryMode.Keto
                "LOW_CARB" -> DietaryMode.LowCarb
                "VEGETARIAN" -> DietaryMode.Vegetarian
                "VEGAN" -> DietaryMode.Vegan
                "PALEO" -> DietaryMode.Paleo
                "CALORIE_DEFICIT" -> DietaryMode.CalorieDeficit
                else -> DietaryMode.Custom(json.getString(name))
            }
        }
    } catch (e: Exception) {
        emptyList()
    }

    return UserPreferences(
        dietaryModes = modes,
        maxCalories = maxCalories,
        minProtein = minProtein,
        maxCarbs = maxCarbs,
        maxFat = maxFat
    )
}

fun UserPreferences.toEntity(): UserPreferencesEntity {
    val jsonModes = JSONArray(
        dietaryModes.map { mode ->
            when (mode) {
                is DietaryMode.Keto -> "KETO"
                is DietaryMode.LowCarb -> "LOW_CARB"
                is DietaryMode.Vegetarian -> "VEGETARIAN"
                is DietaryMode.Vegan -> "VEGAN"
                is DietaryMode.Paleo -> "PALEO"
                is DietaryMode.CalorieDeficit -> "CALORIE_DEFICIT"
                is DietaryMode.Custom -> mode.name
            }
        }
    ).toString()

    return UserPreferencesEntity(
        dietaryModes = jsonModes,
        maxCalories = maxCalories,
        minProtein = minProtein,
        maxCarbs = maxCarbs,
        maxFat = maxFat
    )
}
