package com.mealmuse.domain.model

data class MealPlan(
    val id: String,
    val name: String,
    val weekStart: Long,
    val weekEnd: Long,
    val entries: List<MealPlanEntry>,
    val dietaryMode: DietaryMode,
    val createdAt: Long
)

data class MealPlanEntry(
    val id: String,
    val mealPlanId: String,
    val recipe: Recipe?,
    val mealType: MealType,
    val dayOfWeek: Int // 1-7 (Mon-Sun)
)

enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack")
}
