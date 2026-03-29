package com.mealmuse.domain.model

data class Recipe(
    val id: String,
    val name: String,
    val description: String,
    val instructions: List<String>,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val sourceUrl: String? = null,
    val imageUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isFavorite: Boolean = false
) {
    val totalTimeMinutes: Int get() = prepTimeMinutes + cookTimeMinutes
    val macros: Macros get() = Macros(calories, protein, carbs, fat)
}

data class Macros(
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float
)

data class RecipeIngredient(
    val id: String,
    val recipeId: String,
    val name: String,
    val amount: Float,
    val unit: String,
    val notes: String? = null
)
