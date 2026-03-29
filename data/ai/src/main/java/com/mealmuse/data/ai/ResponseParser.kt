package com.mealmuse.data.ai

import com.mealmuse.domain.model.*
import com.mealmuse.domain.repository.RecipeImprovement
import com.mealmuse.domain.repository.RecipeChange
import org.json.JSONArray
import org.json.JSONObject

object MealPlanParser {
    fun parse(json: String): MealPlan {
        val obj = JSONObject(json)
        val now = System.currentTimeMillis()
        return MealPlan(
            id = obj.optString("id", "generated-$now"),
            name = obj.optString("name", "Weekly Meal Plan"),
            weekStart = now,
            weekEnd = now + (7 * 24 * 60 * 60 * 1000),
            entries = obj.optJSONArray("entries")?.let { arr ->
                (0 until arr.length()).map { parseEntry(arr.getJSONObject(it)) }
            } ?: emptyList(),
            dietaryMode = DietaryMode.Keto,
            createdAt = now
        )
    }

    private fun parseEntry(obj: JSONObject): MealPlanEntry = MealPlanEntry(
        id = obj.optString("id", "entry-${System.nanoTime()}"),
        mealPlanId = obj.optString("mealPlanId", ""),
        recipe = obj.optJSONObject("recipe")?.let { parseRecipe(it) },
        mealType = try { MealType.valueOf(obj.getString("mealType")) } catch (e: Exception) { MealType.BREAKFAST },
        dayOfWeek = obj.optInt("dayOfWeek", 1)
    )

    private fun parseRecipe(obj: JSONObject): Recipe = Recipe(
        id = obj.optString("id", "recipe-${System.nanoTime()}"),
        name = obj.getString("name"),
        description = obj.optString("description", ""),
        instructions = obj.optJSONArray("instructions")?.let { arr ->
            (0 until arr.length()).map { arr.getString(it) }
        } ?: emptyList(),
        prepTimeMinutes = obj.optInt("prepTimeMinutes", 0),
        cookTimeMinutes = obj.optInt("cookTimeMinutes", 0),
        servings = obj.optInt("servings", 1),
        calories = obj.optDouble("calories", 0.0).toFloat(),
        protein = obj.optDouble("protein", 0.0).toFloat(),
        carbs = obj.optDouble("carbs", 0.0).toFloat(),
        fat = obj.optDouble("fat", 0.0).toFloat(),
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}

object RecipeResearchParser {
    fun parse(json: String): List<Recipe> {
        val arr = JSONArray(json)
        val now = System.currentTimeMillis()
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            Recipe(
                id = obj.optString("id", "research-$now-$i"),
                name = obj.getString("name"),
                description = obj.optString("description", ""),
                instructions = obj.optJSONArray("instructions")?.let { instr ->
                    (0 until instr.length()).map { instr.getString(it) }
                } ?: emptyList(),
                prepTimeMinutes = obj.optInt("prepTimeMinutes", 0),
                cookTimeMinutes = obj.optInt("cookTimeMinutes", 0),
                servings = obj.optInt("servings", 1),
                calories = obj.optDouble("calories", 0.0).toFloat(),
                protein = obj.optDouble("protein", 0.0).toFloat(),
                carbs = obj.optDouble("carbs", 0.0).toFloat(),
                fat = obj.optDouble("fat", 0.0).toFloat(),
                sourceUrl = obj.optString("sourceUrl", null),
                imageUrl = obj.optString("imageUrl", null),
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

object RecipeImprovementParser {
    fun parse(json: String): RecipeImprovement {
        val obj = JSONObject(json)
        val now = System.currentTimeMillis()

        val recipeObj = obj.getJSONObject("improvedRecipe")
        val improvedRecipe = Recipe(
            id = recipeObj.optString("id", "improved-$now"),
            name = recipeObj.getString("name"),
            description = recipeObj.optString("description", ""),
            instructions = recipeObj.optJSONArray("instructions")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: emptyList(),
            prepTimeMinutes = recipeObj.optInt("prepTimeMinutes", 0),
            cookTimeMinutes = recipeObj.optInt("cookTimeMinutes", 0),
            servings = recipeObj.optInt("servings", 1),
            calories = recipeObj.optDouble("calories", 0.0).toFloat(),
            protein = recipeObj.optDouble("protein", 0.0).toFloat(),
            carbs = recipeObj.optDouble("carbs", 0.0).toFloat(),
            fat = recipeObj.optDouble("fat", 0.0).toFloat(),
            createdAt = now,
            updatedAt = now
        )

        val changes = obj.optJSONArray("changes")?.let { arr ->
            (0 until arr.length()).map { i ->
                val changeObj = arr.getJSONObject(i)
                RecipeChange(
                    field = changeObj.getString("field"),
                    oldValue = changeObj.getString("oldValue"),
                    newValue = changeObj.getString("newValue")
                )
            }
        } ?: emptyList()

        return RecipeImprovement(
            improvedRecipe = improvedRecipe,
            changes = changes,
            score = obj.optInt("score", 0)
        )
    }
}