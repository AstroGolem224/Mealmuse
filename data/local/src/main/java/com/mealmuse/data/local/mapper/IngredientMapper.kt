package com.mealmuse.data.local.mapper

import com.mealmuse.data.local.entity.IngredientEntity
import com.mealmuse.domain.model.Ingredient
import com.mealmuse.domain.model.IngredientCategory

fun IngredientEntity.toDomain(): Ingredient = Ingredient(
    id = id,
    name = name,
    quantity = quantity,
    unit = unit,
    expiryDate = expiryDate,
    category = try {
        IngredientCategory.valueOf(category)
    } catch (e: Exception) {
        IngredientCategory.OTHER
    },
    addedAt = addedAt
)

fun Ingredient.toEntity(): IngredientEntity = IngredientEntity(
    id = id,
    name = name,
    quantity = quantity,
    unit = unit,
    expiryDate = expiryDate,
    category = category.name,
    addedAt = addedAt
)
