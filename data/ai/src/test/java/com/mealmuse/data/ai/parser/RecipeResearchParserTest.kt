package com.mealmuse.data.ai.parser

import com.mealmuse.data.ai.ResponseParser
import org.junit.Assert.*
import org.junit.Test

class RecipeResearchParserTest {

    @Test
    fun `parseRecipes extracts list from JSON array`() {
        val json = """
        [
            {
                "id": "r1",
                "name": "Grilled Chicken",
                "description": "Healthy grilled chicken",
                "instructions": ["Season chicken", "Grill for 10 min", "Rest and serve"],
                "prepTimeMinutes": 10,
                "cookTimeMinutes": 20,
                "servings": 4,
                "calories": 350,
                "protein": 45,
                "carbs": 5,
                "fat": 15
            },
            {
                "id": "r2",
                "name": "Salmon Bowl",
                "description": "Omega-3 rich salmon",
                "instructions": ["Bake salmon", "Prepare rice", "Assemble bowl"],
                "prepTimeMinutes": 10,
                "cookTimeMinutes": 15,
                "servings": 2,
                "calories": 450,
                "protein": 35,
                "carbs": 40,
                "fat": 20
            }
        ]
        """.trimIndent()

        val result = ResponseParser.parseRecipes(json)

        assertEquals(2, result.size)
        assertEquals("Grilled Chicken", result[0].name)
        assertEquals(45, result[0].protein, 0.01)
        assertEquals("Salmon Bowl", result[1].name)
        assertEquals(450, result[1].calories, 0.01)
    }

    @Test
    fun `parseRecipes handles markdown code fences`() {
        val json = """
        ```json
        [
            {"id": "r1", "name": "Pasta", "description": "Italian dish", "instructions": ["Boil pasta"], "prepTimeMinutes": 5, "cookTimeMinutes": 15, "servings": 2, "calories": 600, "protein": 20, "carbs": 80, "fat": 15}
        ]
        ```
        """.trimIndent()

        val result = ResponseParser.parseRecipes(json)

        assertEquals(1, result.size)
        assertEquals("Pasta", result[0].name)
    }

    @Test
    fun `parseRecipes handles empty array`() {
        val json = "[]"

        val result = ResponseParser.parseRecipes(json)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseRecipes handles missing optional fields`() {
        val json = """
        [
            {"id": "r1", "name": "Simple Recipe"}
        ]
        """.trimIndent()

        val result = ResponseParser.parseRecipes(json)

        assertEquals(1, result.size)
        assertEquals("Simple Recipe", result[0].name)
        assertEquals(0.0, result[0].calories, 0.01)
        assertTrue(result[0].instructions.isEmpty())
    }

    @Test
    fun `parseRecipes throws on invalid JSON`() {
        val json = "not valid json"

        try {
            ResponseParser.parseRecipes(json)
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("Failed to parse") == true)
        }
    }
}
