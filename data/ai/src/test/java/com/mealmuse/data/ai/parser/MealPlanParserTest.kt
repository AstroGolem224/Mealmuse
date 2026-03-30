package com.mealmuse.data.ai.parser

import com.mealmuse.data.ai.ResponseParser
import com.mealmuse.domain.model.MealType
import org.junit.Assert.*
import org.junit.Test

class MealPlanParserTest {

    @Test
    fun `parseMealPlan extracts meal plan from valid JSON`() {
        val json = """
        {
            "id": "test-plan-1",
            "name": "Weekly Plan",
            "weekStart": 1734000000000,
            "weekEnd": 1734600000000,
            "dietaryMode": "BALANCED",
            "entries": [
                {
                    "id": "e1",
                    "dayOfWeek": 1,
                    "mealType": "BREAKFAST",
                    "recipe": {
                        "id": "r1",
                        "name": "Oatmeal",
                        "description": "Healthy breakfast",
                        "instructions": ["Cook oats", "Add toppings"],
                        "prepTimeMinutes": 5,
                        "cookTimeMinutes": 10,
                        "servings": 2,
                        "calories": 350,
                        "protein": 12,
                        "carbs": 55,
                        "fat": 8
                    }
                },
                {
                    "id": "e2",
                    "dayOfWeek": 1,
                    "mealType": "LUNCH",
                    "recipe": {
                        "id": "r2",
                        "name": "Salad",
                        "description": "Fresh salad",
                        "instructions": ["Mix greens"],
                        "prepTimeMinutes": 10,
                        "cookTimeMinutes": 0,
                        "servings": 1,
                        "calories": 250,
                        "protein": 8,
                        "carbs": 20,
                        "fat": 15
                    }
                }
            ]
        }
        """.trimIndent()

        val result = ResponseParser.parseMealPlan(json)

        assertNotNull(result)
        assertEquals("test-plan-1", result.id)
        assertEquals("Weekly Plan", result.name)
        assertEquals(2, result.entries.size)

        val breakfast = result.entries[0]
        assertEquals(1, breakfast.dayOfWeek)
        assertEquals(MealType.BREAKFAST, breakfast.mealType)
        assertEquals("Oatmeal", breakfast.recipe.name)
        assertEquals(350, breakfast.recipe.calories, 0.01)
        assertEquals(12, breakfast.recipe.protein, 0.01)
        assertEquals(2, breakfast.recipe.instructions.size)
    }

    @Test
    fun `parseMealPlan handles markdown code fences`() {
        val json = """
        ```json
        {
            "id": "test-plan-2",
            "name": "Keto Week",
            "weekStart": 1734000000000,
            "weekEnd": 1734600000000,
            "dietaryMode": "KETO",
            "entries": []
        }
        ```
        """.trimIndent()

        val result = ResponseParser.parseMealPlan(json)

        assertNotNull(result)
        assertEquals("test-plan-2", result.id)
        assertEquals("Keto Week", result.name)
    }

    @Test
    fun `parseMealPlan handles multiple days`() {
        val json = """
        {
            "id": "multi-day",
            "name": "Multi Day Plan",
            "weekStart": 1734000000000,
            "weekEnd": 1734600000000,
            "dietaryMode": "BALANCED",
            "entries": [
                {"id": "e1", "dayOfWeek": 1, "mealType": "BREAKFAST", "recipe": {"id": "r1", "name": "R1", "description": "", "instructions": [], "prepTimeMinutes": 0, "cookTimeMinutes": 0, "servings": 1, "calories": 400, "protein": 20, "carbs": 50, "fat": 10}},
                {"id": "e2", "dayOfWeek": 1, "mealType": "LUNCH", "recipe": {"id": "r2", "name": "R2", "description": "", "instructions": [], "prepTimeMinutes": 0, "cookTimeMinutes": 0, "servings": 1, "calories": 500, "protein": 25, "carbs": 60, "fat": 15}},
                {"id": "e3", "dayOfWeek": 2, "mealType": "BREAKFAST", "recipe": {"id": "r3", "name": "R3", "description": "", "instructions": [], "prepTimeMinutes": 0, "cookTimeMinutes": 0, "servings": 1, "calories": 350, "protein": 15, "carbs": 45, "fat": 12}},
                {"id": "e4", "dayOfWeek": 3, "mealType": "DINNER", "recipe": {"id": "r4", "name": "R4", "description": "", "instructions": [], "prepTimeMinutes": 0, "cookTimeMinutes": 0, "servings": 1, "calories": 600, "protein": 35, "carbs": 40, "fat": 25}}
            ]
        }
        """.trimIndent()

        val result = ResponseParser.parseMealPlan(json)

        assertNotNull(result)
        assertEquals(4, result.entries.size)

        val day1Meals = result.entries.filter { it.dayOfWeek == 1 }
        assertEquals(2, day1Meals.size)
        assertTrue(day1Meals.any { it.mealType == MealType.BREAKFAST })
        assertTrue(day1Meals.any { it.mealType == MealType.LUNCH })
    }

    @Test
    fun `parseMealPlan throws on invalid JSON`() {
        val json = "not valid json at all!!!"

        try {
            ResponseParser.parseMealPlan(json)
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("Failed to parse") == true)
        }
    }

    @Test
    fun `parseMealPlan handles missing fields gracefully`() {
        val json = """
        {
            "id": "partial",
            "name": "Partial Plan",
            "weekStart": 1734000000000,
            "weekEnd": 1734600000000,
            "dietaryMode": "BALANCED",
            "entries": [
                {"id": "e1", "dayOfWeek": 1, "mealType": "BREAKFAST", "recipe": {"id": "r1", "name": "Test"}}
            ]
        }
        """.trimIndent()

        val result = ResponseParser.parseMealPlan(json)

        assertNotNull(result)
        assertEquals("partial", result.id)
        assertEquals(1, result.entries.size)
        assertNotNull(result.entries[0].recipe)
    }
}
