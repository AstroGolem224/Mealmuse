package com.mealmuse.domain.model

data class Ingredient(
    val id: String,
    val name: String,
    val quantity: Float,
    val unit: String,
    val expiryDate: Long? = null,
    val category: IngredientCategory,
    val addedAt: Long
) {
    val isExpired: Boolean
        get() = expiryDate?.let { it < System.currentTimeMillis() } ?: false

    val isExpiringSoon: Boolean
        get() = expiryDate?.let {
            val threeDays = 3 * 24 * 60 * 60 * 1000
            it < System.currentTimeMillis() + threeDays && !isExpired
        } ?: false
}

enum class IngredientCategory(val displayName: String) {
    PRODUCE("Produce"),
    DAIRY("Dairy"),
    PROTEIN("Protein"),
    PANTRY("Pantry"),
    FROZEN("Frozen"),
    OTHER("Other")
}
