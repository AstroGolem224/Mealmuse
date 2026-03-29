package com.mealmuse.data.ai.prompt

object PromptFactory {
    
    fun mealPlan(ingredients: List<String>, dietaryMode: String, maxCalories: Int, minProtein: Int, maxCarbs: Int): String = buildString {
        appendLine("You are a professional nutritionist and chef. Generate a structured 7-day meal plan.")
        appendLine()
        appendLine("REQUIREMENTS:")
        appendLine("- Dietary mode: $dietaryMode")
        appendLine("- Max calories per day: $maxCalories kcal")
        appendLine("- Min protein per day: ${minProtein}g")
        appendLine("- Max carbs per day: ${maxCarbs}g")
        appendLine("- 4 meals per day: breakfast, lunch, dinner, snack")
        appendLine()
        if (ingredients.isNotEmpty()) {
            appendLine("AVAILABLE INGREDIENTS: ${ingredients.joinToString(", ")}")
            appendLine("Prioritize using these ingredients when possible.")
            appendLine()
        }
        appendLine("RESPONSE FORMAT (JSON only, no markdown):")
        appendLine("""{
  "id": "generated-uuid",
  "name": "Weekly Meal Plan",
  "weekStart": "today_timestamp",
  "weekEnd": "seven_days_later_timestamp",
  "entries": [
    {
      "id": "entry-uuid",
      "dayOfWeek": 1,
      "mealType": "BREAKFAST",
      "recipe": {
        "id": "recipe-uuid",
        "name": "Recipe Name",
        "description": "Short description",
        "instructions": ["Step 1", "Step 2"],
        "prepTimeMinutes": 10,
        "cookTimeMinutes": 20,
        "servings": 2,
        "calories": 400,
        "protein": 25,
        "carbs": 40,
        "fat": 15
      }
    }
  ],
  "dietaryMode": "KETO"
}""")
        appendLine()
        appendLine("Return ONLY valid JSON. No explanatory text.")
    }

    fun researchRecipes(query: String): String = buildString {
        appendLine("You are a recipe research expert. Find and rank recipes matching the query.")
        appendLine()
        appendLine("QUERY: $query")
        appendLine()
        appendLine("Find 3 relevant recipes. Score each 0-100 on health, taste, and ingredient availability.")
        appendLine()
        appendLine("RESPONSE FORMAT (JSON only):")
        appendLine("""[
  {
    "id": "uuid",
    "name": "Recipe Name",
    "description": "Description with key highlights",
    "instructions": ["Step 1", "Step 2"],
    "prepTimeMinutes": 10,
    "cookTimeMinutes": 20,
    "servings": 4,
    "calories": 500,
    "protein": 30,
    "carbs": 50,
    "fat": 20,
    "sourceUrl": "https://source.com",
    "imageUrl": null
  }
]""")
        appendLine()
        appendLine("Return ONLY valid JSON array. No markdown or explanatory text.")
    }

    fun improveRecipe(name: String, instructions: List<String>, calories: Float, protein: Float, carbs: Float, fat: Float, focus: String): String = buildString {
        appendLine("You are a cookbook author. Improve this recipe focusing on: $focus")
        appendLine()
        appendLine("ORIGINAL RECIPE:")
        appendLine("Name: $name")
        appendLine("Instructions: ${instructions.joinToString(" → ")}")
        appendLine("Macros: ${calories}cal, ${protein}g protein, ${carbs}g carbs, ${fat}g fat")
        appendLine()
        appendLine("RESPONSE FORMAT (JSON only):")
        appendLine("""{
  "improvedRecipe": {
    "id": "uuid",
    "name": "Improved Recipe Name",
    "description": "What changed and why",
    "instructions": ["Improved step 1", "Improved step 2"],
    "prepTimeMinutes": 10,
    "cookTimeMinutes": 15,
    "servings": 2,
    "calories": 450,
    "protein": 35,
    "carbs": 45,
    "fat": 18
  },
  "changes": [
    {
      "field": "instructions",
      "oldValue": "Original step",
      "newValue": "Improved step"
    }
  ],
  "score": 85
}""")
        appendLine()
        appendLine("Return ONLY valid JSON. No markdown or explanatory text.")
    }
}