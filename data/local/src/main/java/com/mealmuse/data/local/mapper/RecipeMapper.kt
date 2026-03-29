package com.mealmuse.data.local.mapper

import com.mealmuse.data.local.entity.RecipeEntity
import com.mealmuse.data.local.entity.RecipeIngredientEntity
import com.mealmuse.domain.model.Recipe
import com.mealmuse.domain.model.RecipeIngredient
import org.json.JSONArray
import org.json.JSONObject

fun RecipeEntity.toDomain(ingredients: List<RecipeIngredientEntity> = emptyList()): Recipe {
    val instructionsList = try {
        val json = JSONArray(instructions)
        (0 until json.length()).map { json.getString(it) }
    } catch (e: Exception) {
        instructions.split("\n").filter { it.isNotBlank() }
    }

    return Recipe(
        id = id,
        name = name,
        description = description,
        instructions = instructionsList,
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        servings = servings,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        sourceUrl = sourceUrl,
        imageUrl = imageUrl,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFavorite = isFavorite
    )
}

fun Recipe.toEntity(): RecipeEntity {
    val jsonInstructions = JSONArray(instructions).toString()
    return RecipeEntity(
        id = id,
        name = name,
        description = description,
        instructions = jsonInstructions,
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        servings = servings,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        sourceUrl = sourceUrl,
        imageUrl = imageUrl,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFavorite = isFavorite
    )
}

fun RecipeIngredientEntity.toDomain(): RecipeIngredient = RecipeIngredient(
    id = id,
    recipeId = recipeId,
    name = name,
    amount = amount,
    unit = unit,
    notes = notes
)

fun RecipeIngredient.toEntity(): RecipeIngredientEntity = RecipeIngredientEntity(
    id = id,
    recipeId = recipeId,
    name = name,
    amount = amount,
    unit = unit,
    notes = notes
)
