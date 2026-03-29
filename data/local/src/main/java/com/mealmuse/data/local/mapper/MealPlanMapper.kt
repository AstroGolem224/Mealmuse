package com.mealmuse.data.local.mapper

import com.mealmuse.data.local.entity.MealPlanEntity
import com.mealmuse.data.local.entity.MealPlanEntryEntity
import com.mealmuse.domain.model.DietaryMode
import com.mealmuse.domain.model.MealPlan
import com.mealmuse.domain.model.MealPlanEntry
import com.mealmuse.domain.model.MealType
import com.mealmuse.domain.model.Recipe

fun MealPlanEntity.toDomain(entries: List<MealPlanEntryEntity> = emptyList()): MealPlan = MealPlan(
    id = id,
    name = name,
    weekStart = weekStart,
    weekEnd = weekEnd,
    entries = entries.map { it.toDomain() },
    dietaryMode = parseDietaryMode(dietaryMode),
    createdAt = createdAt
)

fun MealPlan.toEntity(): MealPlanEntity = MealPlanEntity(
    id = id,
    name = name,
    weekStart = weekStart,
    weekEnd = weekEnd,
    dietaryMode = dietaryMode.toName(),
    createdAt = createdAt
)

fun MealPlanEntryEntity.toDomain(): MealPlanEntry = MealPlanEntry(
    id = id,
    mealPlanId = mealPlanId,
    recipe = null,
    mealType = try {
        MealType.valueOf(mealType)
    } catch (e: Exception) {
        MealType.BREAKFAST
    },
    dayOfWeek = dayOfWeek
)

fun MealPlanEntry.toEntity(): MealPlanEntryEntity = MealPlanEntryEntity(
    id = id,
    mealPlanId = mealPlanId,
    recipeId = recipe?.id,
    mealType = mealType.name,
    dayOfWeek = dayOfWeek,
    scheduledAt = null
)

private fun parseDietaryMode(name: String): DietaryMode = when (name) {
    "KETO" -> DietaryMode.Keto
    "LOW_CARB" -> DietaryMode.LowCarb
    "VEGETARIAN" -> DietaryMode.Vegetarian
    "VEGAN" -> DietaryMode.Vegan
    "PALEO" -> DietaryMode.Paleo
    "CALORIE_DEFICIT" -> DietaryMode.CalorieDeficit
    else -> DietaryMode.Custom(name)
}

private fun DietaryMode.toName(): String = when (this) {
    is DietaryMode.Keto -> "KETO"
    is DietaryMode.LowCarb -> "LOW_CARB"
    is DietaryMode.Vegetarian -> "VEGETARIAN"
    is DietaryMode.Vegan -> "VEGAN"
    is DietaryMode.Paleo -> "PALEO"
    is DietaryMode.CalorieDeficit -> "CALORIE_DEFICIT"
    is DietaryMode.Custom -> name
}
