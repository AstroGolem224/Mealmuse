package com.mealmuse.data.ai.prompt

object PromptFactory {

    fun researchRecipes(query: String): String = buildString {
        appendLine("You are a recipe research expert. Find and rank recipes matching the query.")
        appendLine()
        appendLine("IMPORTANT: Only respond with text. Do not attempt to process or read any images.")
        appendLine()
        appendLine("QUERY: $query")
        appendLine()
        appendLine("Find 3 relevant recipes. Score each 0-100 on health, taste, and ingredient availability.")
        appendLine()
        appendLine("RESPONSE FORMAT (text JSON only, no images):")
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
    "sourceUrl": null,
    "imageUrl": null
  }
]""")
        appendLine()
        appendLine("Return ONLY valid text JSON array. No markdown. No images.")
    }

    fun improveRecipe(name: String, instructions: List<String>, calories: Float, protein: Float, carbs: Float, fat: Float, focus: String): String = buildString {
        appendLine("You are a cookbook author. Improve this recipe focusing on: $focus")
        appendLine()
        appendLine("IMPORTANT: Only respond with text. Do not attempt to process or read any images.")
        appendLine()
        appendLine("ORIGINAL RECIPE:")
        appendLine("Name: $name")
        appendLine("Instructions: ${instructions.joinToString(" → ")}")
        appendLine("Macros: ${calories}cal, ${protein}g protein, ${carbs}g carbs, ${fat}g fat")
        appendLine()
        appendLine("RESPONSE FORMAT (text JSON only, no images):")
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
        appendLine("Return ONLY valid text JSON. No markdown. No images.")
    }
}