package com.mealmuse.domain.model

data class UserPreferences(
    val dietaryModes: List<DietaryMode>,
    val maxCalories: Int = 2000,
    val minProtein: Int = 50,
    val maxCarbs: Int = 300,
    val maxFat: Int = 70
)

sealed class DietaryMode {
    data object Keto : DietaryMode()
    data object LowCarb : DietaryMode()
    data object Vegetarian : DietaryMode()
    data object Vegan : DietaryMode()
    data object Paleo : DietaryMode()
    data object CalorieDeficit : DietaryMode()
    data class Custom(
        val name: String,
        val maxCarbs: Int? = null,
        val minProtein: Int? = null,
        val maxCalories: Int? = null
    ) : DietaryMode()
}
